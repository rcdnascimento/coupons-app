package config

import (
	"fmt"
	"os"
)

// Config lê URLs e MySQL a partir do ambiente (valores alinhados aos application.yml).
type Config struct {
	BFFBase          string
	AuthService      string
	ProfileService   string
	CampaignsService string
	LedgerService    string
	PrizesService    string

	MySQLHost     string
	MySQLPort     string
	MySQLUser     string
	MySQLPassword string
}

func getenv(key, def string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return def
}

func Load() Config {
	return Config{
		BFFBase:          getenv("BFF_BASE_URL", "http://localhost:8090"),
		AuthService:      getenv("AUTH_SERVICE_URL", "http://localhost:8081"),
		ProfileService:   getenv("PROFILE_SERVICE_URL", "http://localhost:8082"),
		CampaignsService: getenv("CAMPAIGNS_SERVICE_URL", "http://localhost:8083"),
		LedgerService:    getenv("LEDGER_SERVICE_URL", "http://localhost:8084"),
		PrizesService:    getenv("PRIZES_SERVICE_URL", "http://localhost:8085"),

		MySQLHost:     getenv("MYSQL_HOST", "127.0.0.1"),
		MySQLPort:     getenv("MYSQL_PORT", "3307"),
		MySQLUser:     getenv("MYSQL_ROOT_USER", "root"),
		MySQLPassword: getenv("MYSQL_ROOT_PASSWORD", "root"),
	}
}

func (c Config) MySQLDSN() string {
	// Nota: não usar allowPublicKeyRetrieval aqui — o servidor MySQL 8 trata-o como variável
	// de sessão inválida com este driver; em dev o Docker já usa mysql_native_password.
	return fmt.Sprintf("%s:%s@tcp(%s:%s)/?parseTime=true&allowNativePasswords=true",
		c.MySQLUser, c.MySQLPassword, c.MySQLHost, c.MySQLPort)
}
