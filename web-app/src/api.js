function resolveBffBaseUrl() {
  const fromEnv = import.meta.env.VITE_BFF_URL;
  if (fromEnv) return fromEnv;
  if (typeof window === "undefined") return "http://localhost:8090";
  const { hostname, protocol } = window.location;
  if (hostname === "localhost" || hostname === "127.0.0.1") {
    return "http://localhost:8090";
  }
  return `${protocol}//${hostname}:8090`;
}

const BFF_BASE_URL = resolveBffBaseUrl();

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
    const msg = "Não foi possível ligar ao servidor. Verifique a rede ou tente mais tarde.";
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

export async function listPrizesByUser(userId, campaignId, options = {}) {
  const suffix = campaignId ? `?campaignId=${encodeURIComponent(campaignId)}` : "";
  return requestJson(`${BFF_BASE_URL}/api/prizes/users/${userId}${suffix}`, {
    method: "GET",
    ...options
  });
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

export async function listCompaniesAdmin() {
  return requestJson(`${BFF_BASE_URL}/api/companies`);
}

export async function createCompany(payload) {
  return requestJson(`${BFF_BASE_URL}/api/companies`, {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function uploadAdminImage(file) {
  if (!file) throw new Error("Ficheiro não informado");
  const form = new FormData();
  form.append("file", file);
  const resp = await fetch(`${BFF_BASE_URL}/api/uploads/images`, {
    method: "POST",
    body: form
  });
  if (!resp.ok) {
    let msg = `Erro HTTP ${resp.status}`;
    try {
      const data = await resp.json();
      if (data?.error) msg = data.error;
      if (typeof data?.message === "string" && data.message) msg = data.message;
    } catch (_) {
      // ignore parse
    }
    apiOnError?.(msg);
    throw new Error(msg);
  }
  return resp.json();
}

export async function getCampaign(campaignId, options = {}) {
  return requestJson(`${BFF_BASE_URL}/api/campaigns/${encodeURIComponent(campaignId)}`, {
    method: "GET",
    ...options
  });
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

/** Lista utilizadores por nome ou email (mín. 2 caracteres no backend). */
export async function searchUsersAdmin(query, options = {}) {
  const { silent = true, ...fetchOpts } = options;
  const q = typeof query === "string" ? query.trim() : "";
  if (q.length < 2) return [];
  const params = new URLSearchParams();
  params.set("q", q);
  return requestJson(`${BFF_BASE_URL}/api/admin/users/search?${params.toString()}`, {
    silent,
    ...fetchOpts
  });
}

export { BFF_BASE_URL };
