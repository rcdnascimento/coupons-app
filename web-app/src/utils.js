export function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms));
}

/** `true` quando já passou o instante agendado da distribuição (sorteio). */
export function isDistributionTimeReached(c) {
  if (!c?.distributionAt) return false;
  return Date.now() >= new Date(c.distributionAt).getTime();
}

/** Campanha encerrada no backend (sorteio e fecho concluídos). */
export function isCampaignClosed(c) {
  return c?.status === "CLOSED";
}

/** Janela após distributionAt em que a animação mínima ainda pode aparecer, mesmo se já fechou. */
export const DISTRIBUTION_DRAW_ANIM_WINDOW_MS = 10_000;

/**
 * Se deve exibir a animação de sorteio no card: após `distributionAt`, durante a janela
 * {@link DISTRIBUTION_DRAW_ANIM_WINDOW_MS} **ou** enquanto a campanha ainda não está CLOSED.
 * Antes de `distributionAt` → false.
 */
export function isDistributionDrawAnimationPhase(c, nowMs = Date.now()) {
  if (!c?.distributionAt) return false;
  const distMs = new Date(c.distributionAt).getTime();
  if (nowMs < distMs) return false;
  const withinAnimWindowAfterDist = nowMs < distMs + DISTRIBUTION_DRAW_ANIM_WINDOW_MS;
  const notClosed = !isCampaignClosed(c);
  return withinAnimWindowAfterDist || notClosed;
}

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
  fechada: "Encerrada"
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
        part = "1 minuto";
      }
    }
  }
  return `Abre em ${part}`;
}

const FALLBACK_RESGATE = "Resgate em breve";

const RESGATE_DETAIL_WINDOW_MS = 2 * 60 * 1000;

/**
 * Quanto falta até `distributionAt` (sorteio/resgate) — maior unidade (dias, horas, minutos);
 * com **menos de 2 minutos**: formato curto `Resgate em 1m 30s` (só segundos se `m === 0`).
 * `null` quando já passou o instante da distribuição (não mostrar badge "Resgate em").
 */
export function distributionRedemptionInRoughLabel(distributionAtIso, nowMs = Date.now()) {
  if (!distributionAtIso) return FALLBACK_RESGATE;
  const ms = new Date(distributionAtIso).getTime() - nowMs;
  if (ms <= 0) return null;

  const days = Math.floor(ms / 86400000);
  if (days >= 1) {
    const part = days === 1 ? "1 dia" : `${days} dias`;
    return `Resgate em ${part}`;
  }
  const hours = Math.floor(ms / 3600000);
  if (hours >= 1) {
    return `Resgate em ${hours}h`;
  }

  if (ms < RESGATE_DETAIL_WINDOW_MS) {
    const m = Math.floor(ms / 60000);
    const s = Math.floor((ms % 60000) / 1000);
    if (m <= 0) {
      return `Resgate em ${Math.max(1, s)}s`;
    }
    return `Resgate em ${m}m ${s}s`;
  }

  const minutes = Math.floor(ms / 60000);
  const part = minutes === 1 ? "1 minuto" : `${minutes} minutos`;
  return `Resgate em ${part}`;
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

/** ACTIVE antes de CLOSED; ACTIVE por `distributionAt` crescente, CLOSED por `distributionAt` decrescente. */
export function sortCampaigns(campaigns) {
  const rankClosed = (c) => (c?.status === "CLOSED" ? 1 : 0);
  return [...campaigns].sort((a, b) => {
    const ra = rankClosed(a);
    const rb = rankClosed(b);
    if (ra !== rb) return ra - rb;
    const ta = a?.distributionAt ? new Date(a.distributionAt).getTime() : Number.MAX_SAFE_INTEGER;
    const tb = b?.distributionAt ? new Date(b.distributionAt).getTime() : Number.MAX_SAFE_INTEGER;
    if (ra === 1) {
      return tb - ta;
    }
    return ta - tb;
  });
}

/** Lista inicial: ocultar após `visibleUntil` (se definido). */
export function isCampaignVisibleInList(c, nowMs = Date.now()) {
  if (!c?.visibleUntil) return true;
  return new Date(c.visibleUntil).getTime() > nowMs;
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

const FALLBACK_DIST_OPENS = "Abre em breve";

/**
 * Tempo até o sorteio/distribuição (`distributionAt`) em texto fixo para o utilizador:
 * sem contador em tempo real, sem segundos. Para a página de detalhe da campanha.
 * `null` quando o instante já passou ou não há data.
 */
export function distributionCountdownVagueLabel(distributionAtIso) {
  if (!distributionAtIso) return null;
  const ms = new Date(distributionAtIso).getTime() - Date.now();
  if (ms <= 0) return null;

  const days = Math.floor(ms / 86400000);
  if (days >= 1) {
    return days === 1 ? "Abre em 1 dia" : `Abre em ${days} dias`;
  }
  const hours = Math.floor(ms / 3600000);
  if (hours >= 1) {
    return hours === 1 ? "Abre em 1 hora" : `Abre em ${hours} horas`;
  }
  const minutes = Math.floor(ms / 60000);
  if (minutes >= 1) {
    return minutes === 1 ? "Abre em 1 minuto" : `Abre em ${minutes} minutos`;
  }
  return FALLBACK_DIST_OPENS;
}

/** Custo da campanha em moedas, linguagem simples para o utilizador. */
export function campaignEntryCostLabel(pointsCost) {
  const n = Math.max(0, Math.floor(Number(pointsCost) || 0));
  if (n === 0) {
    return "Você não gasta moedas nesta campanha.";
  }
  if (n === 1) {
    return "Apenas 1 moeda para participar.";
  }
  return `Apenas ${n} moedas para participar.`;
}

/** Texto curto para o canto do card (entrada em moedas). */
export function campaignPointsCornerLabel(pointsCost) {
  const n = Math.max(0, Math.floor(Number(pointsCost) || 0));
  if (n === 0) return "Grátis";
  if (n === 1) return "1 moeda";
  return `${n} moedas`;
}

/** Descrição exibida no card; legado sem campo usa em dash. */
export function campaignCardDescriptionText(description) {
  const s = typeof description === "string" ? description.trim() : "";
  return s || "—";
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
