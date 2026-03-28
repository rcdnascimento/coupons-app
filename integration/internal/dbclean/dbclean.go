package dbclean

import (
	"context"
	"fmt"

	"coupons.app/integration/internal/config"
	"coupons.app/integration/internal/db"
)

// CleanDevData apaga todos os dados dos schemas de desenvolvimento (equivalente a clean-dev-databases.sql).
// Liga por TCP ao MySQL (ex.: localhost:3307) — não usa Docker nem cliente mysql na shell.
func CleanDevData(ctx context.Context, cfg config.Config) error {
	d, err := db.Open(ctx, cfg)
	if err != nil {
		return err
	}
	defer d.Close()

	stmts := []string{
		"SET FOREIGN_KEY_CHECKS = 0",
		"TRUNCATE TABLE prizes.prize_deliveries",
		"TRUNCATE TABLE ledger.ledger_entries",
		"TRUNCATE TABLE campaigns.outbox",
		"TRUNCATE TABLE campaigns.campaign_allocations",
		"TRUNCATE TABLE campaigns.campaign_subscriptions",
		"TRUNCATE TABLE campaigns.campaign_coupons",
		"TRUNCATE TABLE campaigns.coupons",
		"TRUNCATE TABLE campaigns.campaigns",
		"TRUNCATE TABLE profile.profiles",
		"TRUNCATE TABLE auth.users",
		"SET FOREIGN_KEY_CHECKS = 1",
	}

	for _, s := range stmts {
		if _, err := d.ExecContext(ctx, s); err != nil {
			return fmt.Errorf("%s: %w", s, err)
		}
	}
	return nil
}
