# Testes de integração (`coupons-it`)

Ferramenta em Go que exercita os microsserviços por HTTP e valida dados no MySQL (TCP). Não usa Docker para falar com a base: liga-se ao MySQL configurado por variáveis de ambiente.

## Pré-requisitos

- **Go** (versão compatível com `go.mod` do projeto).
- **MySQL** acessível (por defeito `127.0.0.1:3307`, utilizador `root`).
- **Serviços** coupons-app a correr com health OK nas portas habituais (ver tabela abaixo).
- Cliente **`mysql`** na PATH apenas se usares scripts em `../scripts/integration/` (o `coupons-it` em Go não precisa do CLI `mysql` para `clean-db`).

## Onde executar

Todos os comandos assumem o diretório desta pasta:

```bash
cd coupons-app/integration
```

## Executar o comando (sem binário)

```bash
go run ./cmd/coupons-it <comando> [opções]
```

### Compilar um binário local (opcional)

```bash
mkdir -p bin
go build -o bin/coupons-it ./cmd/coupons-it
./bin/coupons-it help
```

## Comandos disponíveis

| Comando | Descrição |
|--------|-----------|
| `help` | Mostra a ajuda (igual a `-h` / `--help`). |
| `health` | Verifica `GET …/actuator/health` em BFF e em todos os serviços. **Não** exige MySQL. |
| `clean-db` | Apaga dados de desenvolvimento nas bases **auth**, **profile**, **campaigns**, **ledger**, **prizes** (via TCP ao MySQL). |
| `auth-register` | Registo em **auth-service** + validações no MySQL. |
| `auth-login` | Registo + login em **auth-service**. |
| `ledger-credit` | Fluxo que credita pontos no **ledger**. |
| `campaign-create-list` | Cria campanha e lista campanhas. |
| `campaign-subscribe` | Inscrição em campanha (inclui débito no ledger quando aplicável). |
| `prize-pipeline` | Fluxo de prémios (Kafka + **prizes-service**). |
| `all` | Corre a sequência completa de fluxos (inclui `prize-pipeline` por defeito). |

### Opções globais

| Opção | Efeito |
|--------|--------|
| `-yes` / `--yes` | Com **`clean-db`**: confirma automaticamente (não pede `sim` no stdin). |
| `-skip-prize` / `--skip-prize` | Com **`all`**: omite o fluxo `prize-pipeline`. |

## Exemplos úteis

Só verificar se os serviços respondem:

```bash
go run ./cmd/coupons-it health
```

Limpar bases de dev sem confirmação interativa:

```bash
go run ./cmd/coupons-it clean-db -yes
```

Limpar, depois correr toda a suíte:

```bash
go run ./cmd/coupons-it clean-db -yes
go run ./cmd/coupons-it all
```

Suíte completa sem Kafka / prémios:

```bash
go run ./cmd/coupons-it all -skip-prize
```

Um fluxo isolado:

```bash
go run ./cmd/coupons-it auth-login
```

## Variáveis de ambiente

Valores por defeito entre parênteses.

| Variável | Defeito | Uso |
|----------|---------|-----|
| `BFF_BASE_URL` | `http://localhost:8090` | Health do BFF. |
| `AUTH_SERVICE_URL` | `http://localhost:8081` | Auth (a maioria dos fluxos). |
| `PROFILE_SERVICE_URL` | `http://localhost:8082` | Indireto (auth chama profile ao registar). |
| `CAMPAIGNS_SERVICE_URL` | `http://localhost:8083` | Campanhas. |
| `LEDGER_SERVICE_URL` | `http://localhost:8084` | Ledger. |
| `PRIZES_SERVICE_URL` | `http://localhost:8085` | Prémios. |
| `MYSQL_HOST` | `127.0.0.1` | Host MySQL. |
| `MYSQL_PORT` | `3307` | Porta MySQL. |
| `MYSQL_ROOT_USER` | `root` | Utilizador MySQL. |
| `MYSQL_ROOT_PASSWORD` | `root` | Palavra-passe MySQL. |

Exemplo com URLs customizadas:

```bash
export AUTH_SERVICE_URL=http://localhost:9081
go run ./cmd/coupons-it auth-register
```

## Scripts Bash adicionais (repositório)

Na pasta `../scripts/integration/` existem utilitários que partilham as mesmas variáveis (ver `common.sh`), por exemplo:

```bash
cd ../scripts/integration
./check-services-health.sh
```

Requerem `curl` (e eventualmente `mysql` conforme o script).

## Notas

- Após alterações de código Java, **reinicia os serviços** antes de confiar nos resultados dos testes.
- `clean-db` é destrutivo: remove dados de desenvolvimento nas schemas usadas pelo projeto.
- O fluxo **`all`** sem `-skip-prize` depende de **Kafka** e do **prizes-service** estarem operacionais.
- **`campaign-subscribe`** e **`prize-pipeline`** dependem de **Kafka** para o débito assíncrono da inscrição (`campaign.subscription.debit.request` → ledger → `campaign.subscription.payment.succeeded|failed` → campaigns). O **ledger-service** também tem de estar a correr com consumer Kafka ativo.
