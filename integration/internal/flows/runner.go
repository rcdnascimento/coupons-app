package flows

import (
	"context"
	"database/sql"
	"fmt"
	"strings"
	"time"

	"coupons.app/integration/internal/config"
	"coupons.app/integration/internal/db"
	"coupons.app/integration/internal/httpx"
)

type Runner struct {
	Cfg config.Config
	DB  *sql.DB
}

func NewRunner(ctx context.Context, cfg config.Config) (*Runner, error) {
	d, err := db.Open(ctx, cfg)
	if err != nil {
		return nil, err
	}
	return &Runner{Cfg: cfg, DB: d}, nil
}

func (r *Runner) Close() error {
	if r.DB == nil {
		return nil
	}
	return r.DB.Close()
}

// waitSubscriptionActive aguarda o fluxo assíncrono débito (Kafka → ledger → callback).
func (r *Runner) waitSubscriptionActive(ctx context.Context, campaignID, userID string) error {
	deadline := time.Now().Add(90 * time.Second)
	for time.Now().Before(deadline) {
		n, err := db.ScalarInt(ctx, r.DB,
			`SELECT COUNT(*) FROM campaigns.campaign_subscriptions WHERE campaign_id = ? AND user_id = ? AND subscription_status = 'ACTIVE'`,
			campaignID, userID)
		if err == nil && n == 1 {
			return nil
		}
		time.Sleep(400 * time.Millisecond)
	}
	return fmt.Errorf("timeout: inscrição não ficou ACTIVE (Kafka/ledger)")
}

// Health verifica actuator/health em todos os serviços.
func (r *Runner) Health(ctx context.Context) error {
	urls := []string{
		r.Cfg.BFFBase + "/actuator/health",
		r.Cfg.AuthService + "/actuator/health",
		r.Cfg.ProfileService + "/actuator/health",
		r.Cfg.CampaignsService + "/actuator/health",
		r.Cfg.LedgerService + "/actuator/health",
		r.Cfg.PrizesService + "/actuator/health",
	}
	for _, u := range urls {
		code, err := httpx.GetJSON(u, nil)
		if err != nil {
			return fmt.Errorf("%s: %w", u, err)
		}
		if code != 200 {
			return fmt.Errorf("%s: HTTP %d", u, code)
		}
	}
	return nil
}

// AuthRegister chama auth-service (/v1/auth/register → profile); valida MySQL.
func (r *Runner) AuthRegister(ctx context.Context) (email, password, userID string, err error) {
	suf := fmt.Sprintf("%d", time.Now().UnixNano())
	email = fmt.Sprintf("it-go-%s@example.test", suf)
	password = "It-Go-pass-99!"
	name := "IT Go " + suf

	var out authResponse
	code, _, err := httpx.PostJSON(r.Cfg.AuthService+"/v1/auth/register", registerRequest{
		Email: email, Password: password, Name: name,
	}, &out)
	if err != nil {
		return "", "", "", err
	}
	if code != 201 {
		return "", "", "", fmt.Errorf("register: HTTP %d", code)
	}
	if out.UserID == "" {
		return "", "", "", fmt.Errorf("register: userId vazio")
	}
	userID = out.UserID

	// auth.users.id pode ser CHAR(36) ou ainda BINARY(16) em bases antigas — o WHERE id = ? com UUID
	// da API falha nesse último caso. Validamos utilizador por email (único) e perfil por user_id (API).
	if err := db.MustCount(ctx, r.DB, 1,
		"SELECT COUNT(*) FROM auth.users WHERE email = ?", email); err != nil {
		return "", "", "", err
	}
	if err := db.MustCount(ctx, r.DB, 1,
		"SELECT COUNT(*) FROM profile.profiles WHERE user_id = ?", userID); err != nil {
		return "", "", "", err
	}
	return email, password, userID, nil
}

// AuthRegisterOnly apenas registo + validação DB (expõe fluxo isolado).
func (r *Runner) AuthRegisterOnly(ctx context.Context) error {
	_, _, _, err := r.AuthRegister(ctx)
	return err
}

// AuthLogin regista um utilizador e faz login no auth-service.
func (r *Runner) AuthLogin(ctx context.Context) error {
	email, pass, _, err := r.AuthRegister(ctx)
	if err != nil {
		return err
	}
	var out authResponse
	code, _, err := httpx.PostJSON(r.Cfg.AuthService+"/v1/auth/login", loginRequest{
		Email: email, Password: pass,
	}, &out)
	if err != nil {
		return err
	}
	if code != 200 {
		return fmt.Errorf("login: HTTP %d", code)
	}
	if out.Token == "" {
		return fmt.Errorf("login: token vazio")
	}
	return nil
}

// LedgerCredit regista utilizador, credita pontos e confirma saldo (API + opcional MySQL se houver entradas).
func (r *Runner) LedgerCredit(ctx context.Context) error {
	_, _, uid, err := r.AuthRegister(ctx)
	if err != nil {
		return err
	}
	const amount = 500
	key := fmt.Sprintf("it-go-ledger-%d", time.Now().UnixNano())
	code, _, err := httpx.PostJSON(r.Cfg.LedgerService+"/v1/ledger/credit", entryRequest{
		UserID: uid, Amount: amount, Reason: "IT_GO_TOPUP",
		RefType: "IT", RefID: key, IdempotencyKey: key,
	}, nil)
	if err != nil {
		return err
	}
	if code != 201 {
		return fmt.Errorf("ledger credit: HTTP %d", code)
	}
	var bal balanceResponse
	code, err = httpx.GetJSON(r.Cfg.LedgerService+"/v1/ledger/balance/"+uid, &bal)
	if err != nil {
		return err
	}
	if code != 200 {
		return fmt.Errorf("balance: HTTP %d", code)
	}
	if bal.Balance != amount {
		return fmt.Errorf("saldo: esperado %d, obtido %d", amount, bal.Balance)
	}
	return db.MustCount(ctx, r.DB, 1,
		"SELECT COUNT(*) FROM ledger.ledger_entries WHERE user_id = ? AND reason = ? AND amount = ?",
		uid, "IT_GO_TOPUP", amount)
}

// CampaignCreateList cria campanha via campaigns-service e verifica na listagem.
func (r *Runner) CampaignCreateList(ctx context.Context) error {
	now := time.Now().UTC().Truncate(time.Second)
	req := createCampaignRequest{
		Title:                fmt.Sprintf("IT Go Campaign %d", now.Unix()),
		SubscriptionsStartAt: now.Add(-time.Hour),
		SubscriptionsEndAt:   now.Add(7 * 24 * time.Hour),
		DistributionAt:       now.Add(14 * 24 * time.Hour),
		PointsCost:           10,
	}
	var created campaignResponse
	code, _, err := httpx.PostJSON(r.Cfg.CampaignsService+"/v1/campaigns", req, &created)
	if err != nil {
		return err
	}
	if code != 201 {
		return fmt.Errorf("create campaign: HTTP %d", code)
	}
	if created.ID == "" {
		return fmt.Errorf("campaign id vazio")
	}

	var list []campaignResponse
	code, err = httpx.GetJSON(r.Cfg.CampaignsService+"/v1/campaigns", &list)
	if err != nil {
		return err
	}
	if code != 200 {
		return fmt.Errorf("list campaigns: HTTP %d", code)
	}
	for _, c := range list {
		if c.ID == created.ID {
			return nil
		}
	}
	return fmt.Errorf("campanha criada não aparece na listagem")
}

// CampaignSubscribe registo, crédito, campanha, cupom, subscrição; valida subscrição e débito no ledger.
func (r *Runner) CampaignSubscribe(ctx context.Context) error {
	_, _, uid, err := r.AuthRegister(ctx)
	if err != nil {
		return err
	}

	pointsCost := 50
	credit := 500
	key := fmt.Sprintf("it-go-sub-%d", time.Now().UnixNano())
	code, _, err := httpx.PostJSON(r.Cfg.LedgerService+"/v1/ledger/credit", entryRequest{
		UserID: uid, Amount: credit, Reason: "IT_GO_SUB",
		RefType: "IT", RefID: key, IdempotencyKey: key,
	}, nil)
	if err != nil {
		return err
	}
	if code != 201 {
		return fmt.Errorf("credit: HTTP %d", code)
	}

	now := time.Now().UTC().Truncate(time.Second)
	campReq := createCampaignRequest{
		Title:                fmt.Sprintf("IT Sub %d", now.Unix()),
		SubscriptionsStartAt: now.Add(-time.Hour),
		SubscriptionsEndAt:   now.Add(7 * 24 * time.Hour),
		DistributionAt:       now.Add(14 * 24 * time.Hour),
		PointsCost:           pointsCost,
	}
	var camp campaignResponse
	if code, _, err = httpx.PostJSON(r.Cfg.CampaignsService+"/v1/campaigns", campReq, &camp); err != nil {
		return err
	}
	if code != 201 {
		return fmt.Errorf("campaign: HTTP %d", code)
	}

	couponCode := fmt.Sprintf("IT-COUP-%d", now.UnixNano())
	exp := now.Add(365 * 24 * time.Hour)
	couponCreate := createCouponRequest{Code: couponCode, ExpiresAt: exp, Title: "IT coupon"}
	if code, _, err = httpx.PostJSON(r.Cfg.CampaignsService+"/v1/coupons", couponCreate, nil); err != nil {
		return err
	}
	if code != 201 {
		return fmt.Errorf("create coupon: HTTP %d", code)
	}
	add := addCouponRequest{Code: couponCode, Priority: 1}
	code, _, err = httpx.PostJSON(
		fmt.Sprintf("%s/v1/campaigns/%s/coupons", r.Cfg.CampaignsService, camp.ID), add, nil)
	if err != nil {
		return err
	}
	if code != 200 {
		return fmt.Errorf("add coupon: HTTP %d", code)
	}

	sub := userIDRequest{UserID: uid}
	code, _, err = httpx.PostJSON(
		fmt.Sprintf("%s/v1/campaigns/%s/subscriptions", r.Cfg.CampaignsService, camp.ID), sub, nil)
	if err != nil {
		return err
	}
	if code != 204 {
		return fmt.Errorf("subscribe: HTTP %d", code)
	}

	if err := r.waitSubscriptionActive(ctx, camp.ID, uid); err != nil {
		return err
	}

	if err := db.MustCount(ctx, r.DB, 1,
		"SELECT COUNT(*) FROM campaigns.campaign_subscriptions WHERE campaign_id = ? AND user_id = ?",
		camp.ID, uid); err != nil {
		return err
	}
	if err := db.MustCount(ctx, r.DB, 1,
		"SELECT COUNT(*) FROM ledger.ledger_entries WHERE user_id = ? AND reason = ? AND amount = ?",
		uid, "CAMPAIGN_SUBSCRIPTION", -pointsCost); err != nil {
		return err
	}
	var bal balanceResponse
	code, err = httpx.GetJSON(r.Cfg.LedgerService+"/v1/ledger/balance/"+uid, &bal)
	if err != nil {
		return err
	}
	if code != 200 {
		return fmt.Errorf("balance após subscrição: HTTP %d", code)
	}
	if bal.Balance != credit-pointsCost {
		return fmt.Errorf("saldo após subscrição: esperado %d, obtido %d", credit-pointsCost, bal.Balance)
	}
	return nil
}

// PrizePipeline subscrição + alocação + espera entrega no prizes (Kafka) e GET BFF.
func (r *Runner) PrizePipeline(ctx context.Context) error {
	_, _, uid, err := r.AuthRegister(ctx)
	if err != nil {
		return err
	}

	pointsCost := 50
	credit := 500
	key := fmt.Sprintf("it-go-prize-%d", time.Now().UnixNano())
	code, _, err := httpx.PostJSON(r.Cfg.LedgerService+"/v1/ledger/credit", entryRequest{
		UserID: uid, Amount: credit, Reason: "IT_GO_PRIZE",
		RefType: "IT", RefID: key, IdempotencyKey: key,
	}, nil)
	if err != nil {
		return err
	}
	if code != 201 {
		return fmt.Errorf("credit: HTTP %d", code)
	}

	now := time.Now().UTC().Truncate(time.Second)
	campReq := createCampaignRequest{
		Title:                fmt.Sprintf("IT Prize %d", now.Unix()),
		SubscriptionsStartAt: now.Add(-time.Hour),
		SubscriptionsEndAt:   now.Add(7 * 24 * time.Hour),
		DistributionAt:       now.Add(14 * 24 * time.Hour),
		PointsCost:           pointsCost,
	}
	var camp campaignResponse
	if code, _, err = httpx.PostJSON(r.Cfg.CampaignsService+"/v1/campaigns", campReq, &camp); err != nil {
		return err
	}
	if code != 201 {
		return fmt.Errorf("campaign: HTTP %d", code)
	}

	couponCode := fmt.Sprintf("IT-PRZ-%d", now.UnixNano())
	exp := now.Add(365 * 24 * time.Hour)
	couponCreate := createCouponRequest{Code: couponCode, ExpiresAt: exp, Title: "IT prize coupon"}
	if code, _, err = httpx.PostJSON(r.Cfg.CampaignsService+"/v1/coupons", couponCreate, nil); err != nil {
		return err
	}
	if code != 201 {
		return fmt.Errorf("create coupon: HTTP %d", code)
	}
	add := addCouponRequest{Code: couponCode, Priority: 1}
	if code, _, err = httpx.PostJSON(
		fmt.Sprintf("%s/v1/campaigns/%s/coupons", r.Cfg.CampaignsService, camp.ID), add, nil); err != nil {
		return err
	}
	if code != 200 {
		return fmt.Errorf("add coupon: HTTP %d", code)
	}

	sub := userIDRequest{UserID: uid}
	if code, _, err = httpx.PostJSON(
		fmt.Sprintf("%s/v1/campaigns/%s/subscriptions", r.Cfg.CampaignsService, camp.ID), sub, nil); err != nil {
		return err
	}
	if code != 204 {
		return fmt.Errorf("subscribe: HTTP %d", code)
	}

	if err := r.waitSubscriptionActive(ctx, camp.ID, uid); err != nil {
		return err
	}

	var alloc allocationResponse
	url := fmt.Sprintf("%s/v1/campaigns/%s/allocations", r.Cfg.CampaignsService, camp.ID)
	if code, _, err = httpx.PostJSON(url, sub, &alloc); err != nil {
		return err
	}
	if code != 200 {
		return fmt.Errorf("allocate: HTTP %d", code)
	}
	if alloc.CouponID == "" {
		return fmt.Errorf("couponId vazio")
	}

	if err := db.MustCount(ctx, r.DB, 1,
		"SELECT COUNT(*) FROM campaigns.campaign_allocations WHERE campaign_id = ? AND user_id = ? AND coupon_id = ?",
		camp.ID, uid, alloc.CouponID); err != nil {
		return err
	}

	deadline := time.Now().Add(90 * time.Second)
	ok := false
	for time.Now().Before(deadline) {
		n, qerr := db.ScalarInt(ctx, r.DB,
			`SELECT COUNT(*) FROM prizes.prize_deliveries
			 WHERE campaign_id = ? AND user_id = ? AND coupon_id = ? AND status = 'DELIVERED'`,
			camp.ID, uid, alloc.CouponID)
		if qerr == nil && n == 1 {
			ok = true
			break
		}
		time.Sleep(2 * time.Second)
	}
	if !ok {
		return fmt.Errorf("timeout: prize_deliveries DELIVERED não encontrado")
	}

	pzURL := fmt.Sprintf("%s/v1/prizes/users/%s?campaignId=%s", r.Cfg.PrizesService, uid, camp.ID)
	var deliveries []prizeDeliveryDTO
	code, err = httpx.GetJSON(pzURL, &deliveries)
	if err != nil {
		return err
	}
	if code != 200 {
		return fmt.Errorf("bff prizes: HTTP %d", code)
	}
	cid := strings.ToLower(alloc.CouponID)
	for _, d := range deliveries {
		if strings.ToLower(d.CouponID) == cid && strings.EqualFold(d.Status, "DELIVERED") {
			return nil
		}
	}
	return fmt.Errorf("BFF /api/prizes sem entrega DELIVERED esperada")
}

// All executa todos os fluxos em sequência (exceto PrizePipeline se skipPrize).
func (r *Runner) All(ctx context.Context, skipPrize bool) error {
	steps := []struct {
		name string
		fn   func(context.Context) error
	}{
		{"health", r.Health},
		{"auth-register", r.AuthRegisterOnly},
		{"auth-login", r.AuthLogin},
		{"ledger-credit", r.LedgerCredit},
		{"campaign-create-list", r.CampaignCreateList},
		{"campaign-subscribe", r.CampaignSubscribe},
	}
	if !skipPrize {
		steps = append(steps, struct {
			name string
			fn   func(context.Context) error
		}{"prize-pipeline", r.PrizePipeline})
	}
	for _, s := range steps {
		if err := s.fn(ctx); err != nil {
			return fmt.Errorf("[%s] %w", s.name, err)
		}
	}
	return nil
}
