const BFF_BASE_URL = import.meta.env.VITE_BFF_URL || "http://localhost:8090";

async function requestJson(url, options = {}) {
  const resp = await fetch(url, {
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options
  });
  if (!resp.ok) {
    let msg = `Erro HTTP ${resp.status}`;
    try {
      const data = await resp.json();
      if (data?.error) msg = data.error;
    } catch (_) {
      // ignore JSON parse failure
    }
    throw new Error(msg);
  }
  if (resp.status === 204) return null;
  return resp.json();
}

export async function register(payload) {
  return requestJson(`${BFF_BASE_URL}/api/auth/register`, {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function login(payload) {
  return requestJson(`${BFF_BASE_URL}/api/auth/login`, {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function listCampaigns() {
  return requestJson(`${BFF_BASE_URL}/api/campaigns`);
}

export async function subscribeCampaign(campaignId, userId) {
  return requestJson(`${BFF_BASE_URL}/api/campaigns/${campaignId}/subscriptions`, {
    method: "POST",
    body: JSON.stringify({ userId })
  });
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

export { BFF_BASE_URL };
