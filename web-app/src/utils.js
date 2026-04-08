export function getCampaignState(c) {
  const now = Date.now();
  const start = new Date(c.subscriptionsStartAt).getTime();
  const end = new Date(c.subscriptionsEndAt).getTime();
  if (now < start) return "abre_em_breve";
  if (now > end || c.status === "CLOSED") return "fechada";
  return "aberta";
}

const CAMPAIGN_STATE_LABELS = {
  aberta: "Aberta",
  fechada: "Fechada"
};

const FALLBACK_OPENS_SOON = "Abre em breve";

/**
 * Quanto falta até `subscriptionsStartAt` — só a maior unidade (instante da chamada; não é contador com segundos).
 * Sempre com prefixo "Abre em ". Ex.: 1h30m → "Abre em 1h"; 29m → "Abre em 29 minutos".
 */
export function subscriptionsOpenInRoughLabel(subscriptionsStartAtIso) {
  if (!subscriptionsStartAtIso) return FALLBACK_OPENS_SOON;
  const ms = new Date(subscriptionsStartAtIso).getTime() - Date.now();
  if (ms <= 0) return FALLBACK_OPENS_SOON;

  let part;
  const days = Math.floor(ms / 86400000);
  if (days >= 1) {
    part = days === 1 ? "1 dia" : `${days} dias`;
  } else {
    const hours = Math.floor(ms / 3600000);
    if (hours >= 1) {
      part = `${hours}h`;
    } else {
      const minutes = Math.floor(ms / 60000);
      if (minutes >= 1) {
        part = minutes === 1 ? "1 minuto" : `${minutes} minutos`;
      } else {
        part = "menos de 1 minuto";
      }
    }
  }
  return `Abre em ${part}`;
}

/**
 * Texto do badge de estado. Para `abre_em_breve`, usa `campaignLike.subscriptionsStartAt` quando existir.
 * `campaignLike`: objeto com `subscriptionsStartAt` (ISO), p.ex. campanha da API ou rascunho do admin.
 */
export function campaignStateBadgeLabel(state, campaignLike) {
  if (!state) return "";
  if (state === "abre_em_breve") {
    const start = campaignLike?.subscriptionsStartAt;
    if (start) return subscriptionsOpenInRoughLabel(start);
    return FALLBACK_OPENS_SOON;
  }
  return CAMPAIGN_STATE_LABELS[state] ?? state.replace(/_/g, " ");
}

export function sortCampaigns(campaigns) {
  const rank = { aberta: 0, abre_em_breve: 1, fechada: 2 };
  return [...campaigns].sort((a, b) => {
    const sa = getCampaignState(a);
    const sb = getCampaignState(b);
    if (rank[sa] !== rank[sb]) return rank[sa] - rank[sb];
    return new Date(a.distributionAt).getTime() - new Date(b.distributionAt).getTime();
  });
}

/** Tempo até `targetIso`; só números/unidades, sem prefixos. */
export function countdownLabel(targetIso) {
  const ms = new Date(targetIso).getTime() - Date.now();
  if (ms <= 0) return "Agora";
  const totalSec = Math.floor(ms / 1000);
  const d = Math.floor(totalSec / 86400);
  const h = Math.floor((totalSec % 86400) / 3600);
  const m = Math.floor((totalSec % 3600) / 60);
  const s = totalSec % 60;
  const parts = [];
  if (d) parts.push(`${d}d`);
  if (h) parts.push(`${h}h`);
  if (m) parts.push(`${m}m`);
  if (s > 0 || parts.length === 0) parts.push(`${s}s`);
  return parts.join(" ");
}

/** Custo da campanha em moedas, linguagem simples para o utilizador. */
export function campaignEntryCostLabel(pointsCost) {
  const n = Math.max(0, Math.floor(Number(pointsCost) || 0));
  if (n === 0) {
    return "Voce nao gasta moedas nesta campanha.";
  }
  if (n === 1) {
    return "Apenas 1 moeda para participar.";
  }
  return `Apenas ${n} moedas para participar.`;
}

/** Texto amigável para a data/hora da distribuição (fuso local). */
export function distributionScheduleLabel(iso) {
  const d = new Date(iso);
  const day = String(d.getDate()).padStart(2, "0");
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const h = d.getHours();
  const mins = d.getMinutes();
  const timeStr = mins === 0 ? `${h}h` : `${h}h${String(mins).padStart(2, "0")}`;
  return `Acontecerá em ${day}/${month} às ${timeStr}.`;
}
