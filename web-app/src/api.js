import { loadAuth, saveAuth } from "./session";

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

function parseRequestOptions(options = {}) {
  const { silent, skipAuth, ...fetchOpts } = options;
  return {
    silent: silent === true,
    skipAuth: skipAuth === true,
    fetchOpts
  };
}

/** Cabeçalho Bearer a partir da sessão (login/registo não devem usar token antigo). */
function bearerHeaders() {
  const token = loadAuth()?.token;
  if (!token || typeof token !== "string") return {};
  return { Authorization: `Bearer ${token.trim()}` };
}

function handleSessionExpired() {
  saveAuth(null);
  if (typeof window === "undefined") return;
  const path = window.location.pathname || "";
  if (!path.endsWith("/login") && !path.endsWith("/register")) {
    window.location.assign("/login");
  }
}

async function requestJson(url, options = {}) {
  const { silent, skipAuth, fetchOpts } = parseRequestOptions(options);
  const headers = {
    ...(fetchOpts.body !== undefined && fetchOpts.body !== null
      ? { "Content-Type": "application/json" }
      : {}),
    ...(!skipAuth ? bearerHeaders() : {}),
    ...(fetchOpts.headers || {})
  };
  let resp;
  try {
    resp = await fetch(url, {
      ...fetchOpts,
      headers
    });
  } catch (err) {
    if (err?.name === "AbortError") throw err;
    const msg = "Não foi possível ligar ao servidor. Verifique a rede ou tente mais tarde.";
    if (!silent) apiOnError?.(msg);
    throw new Error(msg);
  }

  if (resp.status === 401 && !skipAuth) {
    handleSessionExpired();
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
    silent: true,
    skipAuth: true
  });
}

export async function login(payload) {
  return requestJson(`${BFF_BASE_URL}/api/auth/login`, {
    method: "POST",
    body: JSON.stringify(payload),
    silent: true,
    skipAuth: true
  });
}

export async function listCampaigns() {
  return requestJson(`${BFF_BASE_URL}/api/campaigns`);
}

export async function subscribeCampaign(campaignId, options = {}) {
  return requestJson(`${BFF_BASE_URL}/api/campaigns/${campaignId}/subscriptions`, {
    method: "POST",
    body: JSON.stringify({}),
    ...options
  });
}

/** Estado: NONE | PROCESSING | ACTIVE | PAYMENT_FAILED */
export async function getMyCampaignSubscriptionStatus(campaignId, options = {}) {
  return requestJson(
    `${BFF_BASE_URL}/api/campaigns/${encodeURIComponent(campaignId)}/subscriptions/me`,
    { method: "GET", ...options }
  );
}

/** Prémios do utilizador autenticado (JWT). */
export async function listMyPrizes(campaignId, options = {}) {
  const suffix = campaignId ? `?campaignId=${encodeURIComponent(campaignId)}` : "";
  return requestJson(`${BFF_BASE_URL}/api/prizes/me${suffix}`, {
    method: "GET",
    ...options
  });
}

export async function getMeBalance(options = {}) {
  return requestJson(`${BFF_BASE_URL}/api/me/balance`, { method: "GET", ...options });
}

export async function getDailyChestToday(options = {}) {
  return requestJson(`${BFF_BASE_URL}/api/daily-chest/today`, { method: "GET", ...options });
}

export async function openDailyChest(options = {}) {
  return requestJson(`${BFF_BASE_URL}/api/daily-chest/open`, {
    method: "POST",
    body: JSON.stringify({}),
    ...options
  });
}

export async function getMeProfile({ name, email } = {}, options = {}) {
  const q = new URLSearchParams();
  if (name) q.set("name", name);
  if (email) q.set("email", email);
  const qs = q.toString();
  const path = qs ? `${BFF_BASE_URL}/api/me/profile?${qs}` : `${BFF_BASE_URL}/api/me/profile`;
  return requestJson(path, { method: "GET", ...options });
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
    body: form,
    headers: bearerHeaders()
  });
  if (resp.status === 401) {
    handleSessionExpired();
  }
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

export async function listCampaignCoupons(campaignId, options = {}) {
  return requestJson(`${BFF_BASE_URL}/api/campaigns/${encodeURIComponent(campaignId)}/coupons`, {
    method: "GET",
    ...options
  });
}

export async function removeCouponFromCampaign(campaignId, couponId, options = {}) {
  return requestJson(
    `${BFF_BASE_URL}/api/campaigns/${encodeURIComponent(campaignId)}/coupons/${encodeURIComponent(couponId)}`,
    { method: "DELETE", ...options }
  );
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

export async function deleteCoupon(couponId, options = {}) {
  return requestJson(`${BFF_BASE_URL}/api/coupons/${encodeURIComponent(couponId)}`, {
    method: "DELETE",
    ...options
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
