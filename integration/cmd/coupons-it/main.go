// Comando coupons-it: testes de integração por fluxo (HTTP + MySQL).
//
// Uso:
//
//	go run ./cmd/coupons-it/ <comando> [-skip-prize]
//
// Variáveis de ambiente: ver internal/config/config.go (BFF_BASE_URL, MYSQL_*, etc.).
package main

import (
	"bufio"
	"context"
	"fmt"
	"os"
	"strings"

	"coupons.app/integration/internal/config"
	"coupons.app/integration/internal/dbclean"
	"coupons.app/integration/internal/flows"
)

func main() {
	var skipPrize bool
	var forceYes bool
	var args []string
	for _, a := range os.Args[1:] {
		switch a {
		case "-skip-prize", "--skip-prize":
			skipPrize = true
		case "-yes", "--yes":
			forceYes = true
		default:
			args = append(args, a)
		}
	}

	if len(args) < 1 {
		usage()
		os.Exit(2)
	}

	cmd := args[0]
	cfg := config.Load()
	ctx := context.Background()

	if cmd == "help" || cmd == "-h" || cmd == "--help" {
		usage()
		return
	}

	if cmd == "clean-db" {
		if !forceYes {
			fmt.Fprintf(os.Stderr, "Isto apaga TODOS os dados em auth, profile, campaigns, ledger, prizes.\n")
			fmt.Fprintf(os.Stderr, "Escreve 'sim' para continuar: ")
			line, _ := bufio.NewReader(os.Stdin).ReadString('\n')
			if strings.TrimSpace(strings.ToLower(line)) != "sim" {
				fmt.Fprintln(os.Stderr, "Cancelado.")
				os.Exit(1)
			}
		}
		if err := dbclean.CleanDevData(ctx, cfg); err != nil {
			fmt.Fprintf(os.Stderr, "Falha: %v\n", err)
			os.Exit(1)
		}
		fmt.Println("OK clean-db")
		return
	}

	if cmd == "health" {
		r := &flows.Runner{Cfg: cfg}
		if err := r.Health(ctx); err != nil {
			fmt.Fprintf(os.Stderr, "Falha: %v\n", err)
			os.Exit(1)
		}
		fmt.Println("OK health")
		return
	}

	runner, err := flows.NewRunner(ctx, cfg)
	if err != nil {
		fmt.Fprintf(os.Stderr, "MySQL: %v\n", err)
		os.Exit(1)
	}
	defer runner.Close()

	var runErr error
	switch cmd {
	case "auth-register":
		runErr = runner.AuthRegisterOnly(ctx)
	case "auth-login":
		runErr = runner.AuthLogin(ctx)
	case "ledger-credit":
		runErr = runner.LedgerCredit(ctx)
	case "campaign-create-list":
		runErr = runner.CampaignCreateList(ctx)
	case "campaign-subscribe":
		runErr = runner.CampaignSubscribe(ctx)
	case "prize-pipeline":
		runErr = runner.PrizePipeline(ctx)
	case "all":
		runErr = runner.All(ctx, skipPrize)
	default:
		fmt.Fprintf(os.Stderr, "Comando desconhecido: %s\n\n", cmd)
		usage()
		os.Exit(2)
	}

	if runErr != nil {
		fmt.Fprintf(os.Stderr, "Falha: %v\n", runErr)
		os.Exit(1)
	}
	fmt.Printf("OK %s\n", cmd)
}

func usage() {
	fmt.Fprintf(os.Stderr, `coupons-it — testes de integração (coupons-app)

Uso:
  coupons-it health
  coupons-it auth-register
  coupons-it auth-login
  coupons-it ledger-credit
  coupons-it campaign-create-list
  coupons-it campaign-subscribe
  coupons-it prize-pipeline
  coupons-it all [-skip-prize]
  coupons-it clean-db [-yes]

-skip-prize  Com o comando "all", omite prize-pipeline (Kafka + prizes).
-yes        Com clean-db, não pede confirmação (usa TCP ao MySQL; não invoca Docker).

Requisitos: serviços nas portas por defeito; MySQL (exceto só "health").
Variáveis: BFF_BASE_URL, AUTH_SERVICE_URL, …, MYSQL_HOST, MYSQL_PORT, MYSQL_ROOT_PASSWORD

`)
}
