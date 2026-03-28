package db

import (
	"context"
	"database/sql"
	"fmt"

	_ "github.com/go-sql-driver/mysql"
	"coupons.app/integration/internal/config"
)

func Open(ctx context.Context, cfg config.Config) (*sql.DB, error) {
	dsn := cfg.MySQLDSN()
	d, err := sql.Open("mysql", dsn)
	if err != nil {
		return nil, err
	}
	if err := d.PingContext(ctx); err != nil {
		_ = d.Close()
		return nil, fmt.Errorf("mysql ping: %w", err)
	}
	return d, nil
}

func ScalarInt(ctx context.Context, db *sql.DB, query string, args ...any) (int, error) {
	var n int
	err := db.QueryRowContext(ctx, query, args...).Scan(&n)
	return n, err
}

func MustCount(ctx context.Context, db *sql.DB, want int, query string, args ...any) error {
	got, err := ScalarInt(ctx, db, query, args...)
	if err != nil {
		return err
	}
	if got != want {
		return fmt.Errorf("mysql: esperado count=%d, obtido %d (%s)", want, got, query)
	}
	return nil
}
