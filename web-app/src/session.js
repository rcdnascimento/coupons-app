export const STORAGE_KEY = "coupons_web_auth_v1";
export const SUBS_STORAGE_KEY = "coupons_web_subscriptions_v1";

export function loadAuth() {
  const raw = localStorage.getItem(STORAGE_KEY);
  return raw ? JSON.parse(raw) : null;
}

export function saveAuth(auth) {
  if (!auth) {
    localStorage.removeItem(STORAGE_KEY);
    return;
  }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
}

export function loadSubscriptions() {
  const raw = localStorage.getItem(SUBS_STORAGE_KEY);
  return raw ? JSON.parse(raw) : {};
}

export function saveSubscriptions(subs) {
  localStorage.setItem(SUBS_STORAGE_KEY, JSON.stringify(subs));
}
