const BFF_BASE_URL = import.meta.env.VITE_BFF_URL || "http://localhost:8090";

/** Definido por NotificationProvider — mostra toast em falhas HTTP/rede. */
let apiOnError = null;

export function setApiNotificationHandlers(handlers) {
  apiOnError = handlers?.onError ?? null;
}

function stripSilent(options) {
  const { silent, ...rest } = options;
  return { silent: silent === true, fetchOpts: rest };
}

async function requestJson(url, options = {}) {
  const { silent, fetchOpts } = stripSilent(options);
  let resp;
  try {
    resp = await fetch(url, {
      headers: { "Content-Type": "application/json", ...(fetchOpts.headers || {}) },
      ...fetchOpts
    });
  } catch (err) {
    if (err?.name === "AbortError") throw err;
    const msg = "Nao foi possivel ligar ao servidor. Verifique a rede ou tente mais tarde.";
    if (!silent) apiOnError?.(msg);
    throw new Error(msg);
  }

  if (!resp.ok) {
    let msg = `Erro HTTP ${resp.status}`;
    try {
      const data = await resp.json();
      if (data?.error) msg = data.error;
      if (typeof data?.message === "string" && data.message) msg = data.message;
    } catch (_) {
      // ignore JSON parse failure
    }
    if (!silent) apiOnError?.(msg);
    throw new Error(msg);
  }
  if (resp.status === 204) return null;
  return resp.json();
}

export async function register(payload) {
  return requestJson(`${BFF_BASE_URL}/api/auth/register`, {
    method: "POST",
    body: JSON.stringify(payload),
    silent: true
  });
}

export async function login(payload) {
  return requestJson(`${BFF_BASE_URL}/api/auth/login`, {
    method: "POST",
    body: JSON.stringify(payload),
    silent: true
  });
}

export async function listCampaigns() {
  return requestJson(`${BFF_BASE_URL}/api/campaigns`);
}

export async function subscribeCampaign(campaignId, userId, options = {}) {
  return requestJson(`${BFF_BASE_URL}/api/campaigns/${campaignId}/subscriptions`, {
    method: "POST",
    body: JSON.stringify({ userId }),
    ...options
  });
}

/** Estado: NONE | PROCESSING | ACTIVE | PAYMENT_FAILED */
export async function getMyCampaignSubscriptionStatus(campaignId, userId, options = {}) {
  return requestJson(
    `${BFF_BASE_URL}/api/campaigns/${encodeURIComponent(campaignId)}/subscriptions/me?userId=${encodeURIComponent(userId)}`,
    { method: "GET", ...options }
  );
}

export async function listPrizesByUser(userId, campaignId) {
  const suffix = campaignId ? `?campaignId=${encodeURIComponent(campaignId)}` : "";
  return requestJson(`${BFF_BASE_URL}/api/prizes/users/${userId}${suffix}`);
}

export async function getMeBalance(userId) {
  return requestJson(`${BFF_BASE_URL}/api/me/balance?userId=${encodeURIComponent(userId)}`);
}

export async function getMeProfile({ userId, name, email }) {
  const q = new URLSearchParams({ userId });
  if (name) q.set("name", name);
  if (email) q.set("email", email);
  return requestJson(`${BFF_BASE_URL}/api/me/profile?${q.toString()}`);
}

export async function getCampaignSummary(campaignId) {
  return requestJson(`${BFF_BASE_URL}/api/campaigns/${campaignId}/summary`);
}

export async function getCampaignWinners(campaignId) {
  return requestJson(`${BFF_BASE_URL}/api/campaigns/${campaignId}/winners`);
}

export async function createCampaign(payload) {
  return requestJson(`${BFF_BASE_URL}/api/campaigns`, {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function getCampaign(campaignId) {
  return requestJson(`${BFF_BASE_URL}/api/campaigns/${encodeURIComponent(campaignId)}`);
}

export async function patchCampaign(campaignId, payload) {
  return requestJson(`${BFF_BASE_URL}/api/campaigns/${encodeURIComponent(campaignId)}`, {
    method: "PATCH",
    body: JSON.stringify(payload)
  });
}

export async function addCouponToCampaign(campaignId, payload) {
  return requestJson(`${BFF_BASE_URL}/api/campaigns/${encodeURIComponent(campaignId)}/coupons`, {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function listCouponsAdmin() {
  return requestJson(`${BFF_BASE_URL}/api/coupons`);
}

/** Pesquisa cupons por código ou título (autocomplete admin). `options.status`: ex. IN_INVENTORY */
export async function searchCoupons(query, options = {}) {
  const { status, silent = true, ...fetchOpts } = options;
  const params = new URLSearchParams();
  params.set("q", typeof query === "string" ? query.trim() : "");
  if (status) params.set("status", status);
  return requestJson(`${BFF_BASE_URL}/api/coupons/search?${params.toString()}`, {
    silent,
    ...fetchOpts
  });
}

export async function getCoupon(couponId) {
  return requestJson(`${BFF_BASE_URL}/api/coupons/${encodeURIComponent(couponId)}`);
}

export async function createCoupon(payload) {
  return requestJson(`${BFF_BASE_URL}/api/coupons`, {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function patchCoupon(couponId, payload) {
  return requestJson(`${BFF_BASE_URL}/api/coupons/${encodeURIComponent(couponId)}`, {
    method: "PATCH",
    body: JSON.stringify(payload)
  });
}

export async function creditUserLedger(payload) {
  return requestJson(`${BFF_BASE_URL}/api/admin/ledger/credits`, {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export { BFF_BASE_URL };
