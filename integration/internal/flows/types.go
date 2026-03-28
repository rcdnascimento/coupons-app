package flows

import "time"

type authResponse struct {
	Token  string `json:"token"`
	UserID string `json:"userId"`
	Email  string `json:"email"`
	Name   string `json:"name"`
}

type registerRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
	Name     string `json:"name"`
}

type loginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type balanceResponse struct {
	UserID  string `json:"userId"`
	Balance int    `json:"balance"`
}

type entryRequest struct {
	UserID         string `json:"userId"`
	Amount         int    `json:"amount"`
	Reason         string `json:"reason"`
	RefType        string `json:"refType,omitempty"`
	RefID          string `json:"refId,omitempty"`
	IdempotencyKey string `json:"idempotencyKey"`
}

type createCampaignRequest struct {
	Title                string    `json:"title"`
	SubscriptionsStartAt time.Time `json:"subscriptionsStartAt"`
	SubscriptionsEndAt   time.Time `json:"subscriptionsEndAt"`
	DistributionAt       time.Time `json:"distributionAt"`
	PointsCost           int       `json:"pointsCost"`
}

type campaignResponse struct {
	ID                   string    `json:"id"`
	Title                string    `json:"title"`
	SubscriptionsStartAt time.Time `json:"subscriptionsStartAt"`
	SubscriptionsEndAt   time.Time `json:"subscriptionsEndAt"`
	DistributionAt       time.Time `json:"distributionAt"`
	PointsCost           int       `json:"pointsCost"`
	Status               string    `json:"status"`
}

type addCouponRequest struct {
	Code      string    `json:"code"`
	ExpiresAt time.Time `json:"expiresAt"`
	Priority  int       `json:"priority"`
}

type userIDRequest struct {
	UserID string `json:"userId"`
}

type allocationResponse struct {
	ID           string `json:"id"`
	CampaignID   string `json:"campaignId"`
	UserID       string `json:"userId"`
	CouponID     string `json:"couponId"`
	CodeSnapshot string `json:"codeSnapshot"`
}

type prizeDeliveryDTO struct {
	CouponID string `json:"couponId"`
	Status   string `json:"status"`
}
