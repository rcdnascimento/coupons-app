import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate, useOutletContext } from "react-router-dom";
import {
  getCampaign,
  getMeBalance,
  getMeProfile,
  getMyCampaignSubscriptionStatus,
  listCampaigns,
  listMyPrizes,
  subscribeCampaign,
  BFF_BASE_URL
} from "../api";
import { useNotification } from "../context/NotificationProvider";
import {
  campaignCardDescriptionText,
  campaignPointsCornerLabel,
  campaignStateBadgeLabel,
  distributionRedemptionInRoughLabel,
  getCampaignState,
  isCampaignVisibleInList,
  isCampaignClosed,
  isDistributionDrawAnimationPhase,
  isDistributionTimeReached,
  sleep,
  sortCampaigns,
  userHasAdminRole
} from "../utils";
import "./HomePage.css";

const POLL_INTERVAL_MS = 1500;
const POLL_MAX_MS = 90_000;

const DIST_DRAW_MIN_MS = 10_000;
const DIST_POLL_MS = 2_000;
const DIST_POLL_MAX_MS = 120_000;

const DIST_DRAW_MESSAGES = [
  "Os cupons estão entrando na disputa…",
  "Preparando o sorteio… quem será o escolhido?",
  "Tem muita gente na disputa…",
  "Será que é você?",
  "Tem um vencedor se aproximando…",
  "Segura aí… estamos quase descobrindo!"
];

const DIST_POLL_MESSAGES = [
  "Ainda há um véu sobre o desfecho...",
  "Quase. O enigma está prestes a se abrir.",
  "Só mais um instante até a revelação."
];

function CampaignSubscribedDistributionBlock({
  campaign,
  userId,
  navigate,
  onSettled,
  onAnimationFocusChange,
  onResultLabelChange
}) {
  const campaignId = campaign.id;
  /** Recalculado a cada render do pai (ex.: tick 1s) — sem useMemo para `Date.now()` não ficar “preso”. */
  const distStarted = isDistributionTimeReached(campaign);
  const inAnimationPhase = isDistributionDrawAnimationPhase(campaign, Date.now());

  const [ui, setUi] = useState("registered");
  const [won, setWon] = useState(null);
  const [lineIndex, setLineIndex] = useState(0);
  const runSerialRef = useRef(0);
  const onSettledRef = useRef(onSettled);
  onSettledRef.current = onSettled;
  const onAnimFocusRef = useRef(onAnimationFocusChange);
  onAnimFocusRef.current = onAnimationFocusChange;
  const onResultLabelRef = useRef(onResultLabelChange);
  onResultLabelRef.current = onResultLabelChange;

  /** Card só com animação: dentro da janela (15s pós-dist ou campanha ainda não fechada) e antes do resultado. */
  const animationFullCard = inAnimationPhase && ui !== "result";

  useEffect(() => {
    onAnimFocusRef.current?.(campaignId, animationFullCard);
    return () => {
      onAnimFocusRef.current?.(campaignId, false);
    };
  }, [campaignId, animationFullCard]);

  /* Deps só distStarted / campaignId / userId: incluir tick ou status reiniciava o fluxo a cada segundo e causava flash. */
  useEffect(() => {
    if (!distStarted) {
      runSerialRef.current += 1;
      setUi("registered");
      setWon(null);
      setLineIndex(0);
      onResultLabelRef.current?.(campaignId, null);
      return;
    }

    let cancelled = false;
    const serial = ++runSerialRef.current;
    const runWithAnimation = isDistributionDrawAnimationPhase(campaign, Date.now());

    if (runWithAnimation) {
      setUi("drawing");
    }
    setWon(null);

    (async () => {
      if (runWithAnimation) {
        await sleep(DIST_DRAW_MIN_MS);
        if (cancelled || serial !== runSerialRef.current) return;
      }

      const deadline = Date.now() + DIST_POLL_MAX_MS;
      let last = null;
      while (Date.now() < deadline && !cancelled && serial === runSerialRef.current) {
        try {
          last = await getCampaign(campaignId, { silent: true });
        } catch {
          await sleep(DIST_POLL_MS);
          continue;
        }
        if (isCampaignClosed(last)) break;
        if (isDistributionDrawAnimationPhase(last, Date.now())) {
          setUi("polling");
        }
        await sleep(DIST_POLL_MS);
      }

      if (cancelled || serial !== runSerialRef.current) return;

      if (!last || !isCampaignClosed(last)) {
        setWon(false);
        setUi("result");
        onResultLabelRef.current?.(campaignId, "Não foi dessa vez");
        onSettledRef.current?.();
        return;
      }

      let didWin = false;
      try {
        const prizes = await listMyPrizes(campaignId, { silent: true });
        const arr = Array.isArray(prizes) ? prizes : [];
        didWin = arr.some((p) => String(p.campaignId) === String(campaignId));
        setWon(didWin);
      } catch {
        setWon(false);
      }
      onResultLabelRef.current?.(
        campaignId,
        didWin ? "Você ganhou!" : "Não foi dessa vez"
      );
      setUi("result");
      onSettledRef.current?.();
    })();

    return () => {
      cancelled = true;
      onResultLabelRef.current?.(campaignId, null);
    };
  }, [distStarted, campaignId, userId]);

  useEffect(() => {
    if (ui !== "drawing" && ui !== "polling" && ui !== "registered") return;
    if (!inAnimationPhase) return;
    const msgs =
      ui === "polling" ? DIST_POLL_MESSAGES : DIST_DRAW_MESSAGES;
    if (msgs.length <= 1) return;
    const t = setInterval(() => setLineIndex((i) => i + 1), 2800);
    return () => clearInterval(t);
  }, [ui, inAnimationPhase]);

  useEffect(() => {
    setLineIndex(0);
  }, [ui]);

  if (!distStarted) {
    return null;
  }

  if (ui === "result") {
    return (
      <div className="distribution-result">
        <div className="distribution-result__actions">
          {won && (
            <button
              type="button"
              className="primary"
              onClick={(e) => {
                e.stopPropagation();
                navigate(`/premios?campaignId=${encodeURIComponent(campaignId)}`);
              }}
            >
              Ver prêmio
            </button>
          )}
          <button
            type="button"
            className={won ? "secondary" : "primary"}
            onClick={(e) => {
              e.stopPropagation();
              navigate(`/campanhas/${campaignId}/vencedores`);
            }}
          >
            Ver vencedores
          </button>
          {!won && (
            <button
              type="button"
              className="secondary"
              onClick={(e) => {
                e.stopPropagation();
                navigate(`/campanhas/${campaignId}`);
              }}
            >
              Ver detalhes
            </button>
          )}
        </div>
      </div>
    );
  }

  const showDraw =
    ui === "drawing" ||
    ui === "polling" ||
    (inAnimationPhase && distStarted && ui === "registered");
  if (!showDraw) {
    return null;
  }

  const msgs = ui === "polling" ? DIST_POLL_MESSAGES : DIST_DRAW_MESSAGES;
  const msgIndex = lineIndex % msgs.length;
  const msgKey = `${ui}-${msgIndex}`;
  return (
    <div className="distribution-draw" role="status" aria-live="polite">
      <div className="distribution-draw__visual" aria-hidden={true}>
        <span className="distribution-draw__orb distribution-draw__orb--a" />
        <span className="distribution-draw__orb distribution-draw__orb--b" />
        <span className="distribution-draw__orb distribution-draw__orb--c" />
      </div>
      <p className="distribution-draw__msg">
        <span className="distribution-draw__msg-inner" key={msgKey}>
          {msgs[msgIndex]}
        </span>
      </p>
    </div>
  );
}

function pointsCostNumber(c) {
  return Math.max(0, Math.floor(Number(c?.pointsCost) || 0));
}

function resolveCardImageUrl(pathLike) {
  const s = typeof pathLike === "string" ? pathLike.trim() : "";
  if (!s) return "";
  if (/^https?:\/\//i.test(s)) return s;
  if (s.startsWith("/")) return `${BFF_BASE_URL}${s}`;
  return `${BFF_BASE_URL}/${s}`;
}

export default function HomePage() {
  const { auth, subscriptions, onSubscribe, replaceSubscriptions, onLogout } = useOutletContext();
  const { notifyError } = useNotification();
  const navigate = useNavigate();
  const [distributionAnimFocus, setDistributionAnimFocus] = useState({});
  const [distributionResultByCampaign, setDistributionResultByCampaign] = useState({});
  const [campaigns, setCampaigns] = useState([]);
  const [balance, setBalance] = useState(null);
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [processingByCampaign, setProcessingByCampaign] = useState({});
  const [tick, setTick] = useState(0);
  const [menuOpen, setMenuOpen] = useState(false);
  const pollingCampaignsRef = useRef(new Set());

  const reloadBalance = useCallback(async () => {
    const bal = await getMeBalance().catch(() => null);
    setBalance(bal?.balance ?? null);
  }, []);

  useEffect(() => {
    const id = setInterval(() => setTick((v) => v + 1), 1000);
    return () => clearInterval(id);
  }, []);

  const setCampaignProcessing = useCallback((campaignId, busy) => {
    setProcessingByCampaign((prev) => {
      const next = { ...prev };
      if (busy) next[campaignId] = true;
      else delete next[campaignId];
      return next;
    });
  }, []);

  const pollUntilResolved = useCallback(
    async (campaignId) => {
      if (pollingCampaignsRef.current.has(campaignId)) return;
      pollingCampaignsRef.current.add(campaignId);
      setCampaignProcessing(campaignId, true);
      const deadline = Date.now() + POLL_MAX_MS;
      try {
        while (Date.now() < deadline) {
          await sleep(POLL_INTERVAL_MS);
          let status;
          try {
            const r = await getMyCampaignSubscriptionStatus(campaignId, { silent: true });
            status = r?.status;
          } catch {
            continue;
          }
          if (status === "ACTIVE") {
            onSubscribe(campaignId);
            const bal = await getMeBalance().catch(() => null);
            setBalance(bal?.balance ?? null);
            return;
          }
          if (status === "PAYMENT_FAILED") {
            notifyError("O pagamento falhou. Pode tentar novamente.");
            return;
          }
        }
        notifyError(
          "Não foi possível confirmar a inscrição a tempo. Atualize a página ou tente mais tarde."
        );
      } finally {
        pollingCampaignsRef.current.delete(campaignId);
        setCampaignProcessing(campaignId, false);
      }
    },
    [notifyError, onSubscribe, setCampaignProcessing]
  );

  async function loadData() {
    try {
      setLoading(true);
      const [camps, bal] = await Promise.all([
        listCampaigns(),
        getMeBalance().catch(() => null)
      ]);
      const sorted = sortCampaigns(camps).filter((c) => isCampaignVisibleInList(c));
      setCampaigns(sorted);
      setBalance(bal?.balance ?? null);
      const profileOut = await getMeProfile({
        name: auth.name,
        email: auth.email
      }).catch(() => null);
      setProfile(profileOut);

      if (sorted.length) {
        const pairs = await Promise.all(
          sorted.map(async (c) => {
            try {
              const r = await getMyCampaignSubscriptionStatus(c.id, { silent: true });
              return [c.id, r?.status];
            } catch {
              return [c.id, null];
            }
          })
        );
        const nextSubs = {};
        for (const [id, st] of pairs) {
          if (st === "ACTIVE") nextSubs[id] = true;
        }
        replaceSubscriptions(nextSubs);
        for (const [id, st] of pairs) {
          if (st === "PROCESSING") pollUntilResolved(id);
        }
      } else {
        replaceSubscriptions({});
      }
    } catch {
      /* Erro de API: toast global em api.js */
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, [auth.userId]);

  useEffect(() => {
    const onRefreshBalance = () => {
      reloadBalance();
    };
    window.addEventListener("coupons:balance-refresh", onRefreshBalance);
    return () => window.removeEventListener("coupons:balance-refresh", onRefreshBalance);
  }, [reloadBalance]);

  const onDistributionAnimationFocus = useCallback((campaignId, active) => {
    setDistributionAnimFocus((prev) => {
      const next = { ...prev };
      if (active) next[campaignId] = true;
      else delete next[campaignId];
      return next;
    });
  }, []);

  const onDistributionResultLabel = useCallback((campaignId, label) => {
    setDistributionResultByCampaign((prev) => {
      const next = { ...prev };
      if (label) next[campaignId] = label;
      else delete next[campaignId];
      return next;
    });
  }, []);

  const referralLink = useMemo(() => {
    const code = profile?.referralCode || auth?.referralCode;
    if (!code) return "";
    const url = new URL(window.location.origin + "/register");
    url.searchParams.set("ref", code);
    return url.toString();
  }, [auth, profile]);

  async function subscribe(campaignId) {
    const msgProcessamento = "inscrição em processamento";
    try {
      await subscribeCampaign(campaignId);
    } catch (err) {
      const m = (err && err.message) || "";
      if (m.toLowerCase().includes(msgProcessamento)) {
        await pollUntilResolved(campaignId);
        return;
      }
      return;
    }
    await pollUntilResolved(campaignId);
  }

  async function shareReferralLink() {
    if (!referralLink) {
      notifyError("Código de indicação indisponível no momento.");
      return;
    }
    try {
      if (navigator.share) {
        await navigator.share({
          title: "Coupons",
          text: "Use meu código de indicação no Coupons!",
          url: referralLink
        });
      } else {
        await navigator.clipboard.writeText(referralLink);
      }
      setMenuOpen(false);
    } catch {
      // Usuário pode cancelar o share nativo; não tratar como erro bloqueante.
    }
  }

  return (
    <main className="page home-page">
      <header className="topbar">
        <h1 className="brand" aria-label="Coupons">
          <span className="brand-coup">Coup</span>
          <span className="brand-ons">ons</span>
        </h1>
        <button
          onClick={() => setMenuOpen(true)}
          className="hamburger-btn"
          type="button"
          aria-label="Abrir menu"
          aria-expanded={menuOpen}
        >
          <span />
          <span />
          <span />
        </button>
      </header>

      {menuOpen && (
        <aside className="menu-overlay" role="dialog" aria-modal="true" aria-label="Menu principal">
          <div className="menu-panel">
            <div className="menu-panel__top">
              <button
                type="button"
                className="menu-close-btn"
                onClick={() => setMenuOpen(false)}
                aria-label="Fechar menu"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="22"
                  height="22"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  aria-hidden={true}
                >
                  <path d="M18 6L6 18M6 6l12 12" />
                </svg>
              </button>
            </div>
            <div className="menu-panel__actions">
              {userHasAdminRole(auth) ? (
                <Link
                  className="menu-item menu-item--link"
                  to="/admin"
                  onClick={() => setMenuOpen(false)}
                >
                  Painel admin
                </Link>
              ) : null}
              <button type="button" className="menu-item" onClick={shareReferralLink}>
                Compartilhar link de indicação
              </button>
              <button
                type="button"
                className="menu-item"
                onClick={() => {
                  setMenuOpen(false);
                  onLogout();
                }}
              >
                Sair
              </button>
            </div>
          </div>
        </aside>
      )}

      <section className="card">
        <h2>Olá, {auth.name}</h2>
        <p className="muted">Você tem {balance ?? "indisponível"} moedas</p>
      </section>

      <section className="card">
        <div className="row">
          <h2>Campanhas</h2>
          <button
            type="button"
            className="sync-btn"
            onClick={loadData}
            disabled={loading}
            aria-label={loading ? "Atualizando campanhas" : "Atualizar campanhas"}
            aria-busy={loading}
          >
            {loading ? (
              <span className="sync-btn__dots" aria-hidden={true}>
                <span />
                <span />
                <span />
              </span>
            ) : (
              <svg
                className="sync-btn__icon"
                xmlns="http://www.w3.org/2000/svg"
                width="22"
                height="22"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden={true}
              >
                <path d="M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" />
                <path d="M3 3v5h5" />
                <path d="M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16" />
                <path d="M16 16h5v5" />
              </svg>
            )}
          </button>
        </div>
        <div className="carousel" data-tick={tick}>
          <div className="carousel-track">
            {campaigns.map((c) => {
              const state = getCampaignState(c);
              const resgateRough =
                state === "aberta" ? distributionRedemptionInRoughLabel(c.distributionAt) : null;
              const subscribed = !!subscriptions[c.id];
              const cost = pointsCostNumber(c);
              const insufficientFunds =
                !loading && balance !== null && cost > 0 && balance < cost;
              const balanceUnavailable = !loading && balance === null && cost > 0;
              const blockForBalance = cost > 0 && (loading || balance === null || balance < cost);
              const processing = !!processingByCampaign[c.id];
              const subscribeDisabled =
                state !== "aberta" || blockForBalance || processing;
              const distPhase =
                subscribed && isDistributionTimeReached(c);
              const animCenterOnly = !!distributionAnimFocus[c.id];
              const cardImageUrl = resolveCardImageUrl(c.imageUrl);
              const resultBadgeLabel = distributionResultByCampaign[c.id] || null;
              const resultKey = (resultBadgeLabel || "").toLowerCase();
              const isResultWon = Boolean(resultBadgeLabel && resultKey.includes("ganhou"));
              const isResultLost = Boolean(
                resultBadgeLabel &&
                  (resultKey.includes("não foi") || resultKey.includes("nao foi"))
              );
              const showParticipandoBadge = subscribed && !resultBadgeLabel;
              const bottomResultBadgeText = resultBadgeLabel
                ? resultBadgeLabel
                : showParticipandoBadge
                  ? "Participando"
                  : state === "fechada"
                    ? campaignStateBadgeLabel(state, c)
                    : null;

              return (
                <article
                  className={
                    "campaign-card campaign-card--clickable" +
                    (animCenterOnly ? " campaign-card--anim-only" : "")
                  }
                  key={c.id}
                  role="button"
                  tabIndex={0}
                  aria-label={
                    animCenterOnly
                      ? `Sorteio em andamento: ${c.title}`
                      : `Ver detalhes da campanha: ${c.title}`
                  }
                  onClick={() => {
                    if (animCenterOnly) return;
                    navigate(`/campanhas/${c.id}`);
                  }}
                  onKeyDown={(e) => {
                    if (animCenterOnly) return;
                    if (e.key === "Enter" || e.key === " ") {
                      e.preventDefault();
                      navigate(`/campanhas/${c.id}`);
                    }
                  }}
                >
                  {!animCenterOnly && (
                    <>
                      <div className="campaign-card__media">
                        {cardImageUrl ? (
                          <img className="campaign-card__media-img" src={cardImageUrl} alt={c.title} />
                        ) : (
                          <div className="campaign-card__media-placeholder" aria-hidden={true} />
                        )}
                        <div className="campaign-card__top-row campaign-card__top-row--overlay">
                          <div className="campaign-card__top-left">
                            {(state === "fechada" ||
                              state === "abre_em_breve" ||
                              (state === "aberta" && resgateRough)) && (
                              <p
                                className={`badge badge--campaign-state ${
                                  state === "aberta" && resgateRough ? "aberta" : state
                                }`}
                                aria-live={
                                  state === "abre_em_breve" || (state === "aberta" && resgateRough)
                                    ? "polite"
                                    : undefined
                                }
                              >
                                {state === "aberta" && resgateRough
                                  ? resgateRough
                                  : campaignStateBadgeLabel(state, c)}
                              </p>
                            )}
                          </div>
                          <span className="campaign-card__coins" title="Custo de entrada na campanha">
                            {campaignPointsCornerLabel(cost)}
                          </span>
                        </div>
                        {bottomResultBadgeText && (
                          <p
                            className={
                              "badge badge--campaign-result-badge" +
                              (isResultWon
                                ? " badge--campaign-result-won"
                                : isResultLost
                                  ? " badge--campaign-result-lost"
                                  : showParticipandoBadge
                                    ? " badge--campaign-result-neutral"
                                    : "")
                            }
                          >
                            {bottomResultBadgeText}
                          </p>
                        )}
                      </div>
                      <h3>{c.title}</h3>
                      <p className="muted campaign-card__description">
                        {campaignCardDescriptionText(c.description)}
                      </p>
                      {cost > 0 && insufficientFunds && state === "aberta" && (
                        <p className="campaign-card__balance-hint" onClick={(e) => e.stopPropagation()}>
                          Saldo insuficiente para esta inscrição.
                        </p>
                      )}
                      {cost > 0 && balanceUnavailable && state === "aberta" && (
                        <p className="campaign-card__balance-hint" onClick={(e) => e.stopPropagation()}>
                          Não foi possível verificar o saldo. Atualize a página.
                        </p>
                      )}
                    </>
                  )}
                  <div
                    className={
                      "campaign-card__actions" +
                      (animCenterOnly ? " campaign-card__actions--anim-only" : "")
                    }
                    onClick={(e) => e.stopPropagation()}
                  >
                    {!subscribed ? (
                      <button
                        type="button"
                        className="primary"
                        onClick={(e) => {
                          e.stopPropagation();
                          subscribe(c.id);
                        }}
                        disabled={subscribeDisabled}
                        aria-busy={processing}
                      >
                        {processing ? "Processando..." : "Inscrever"}
                      </button>
                    ) : (
                      <CampaignSubscribedDistributionBlock
                        key={`${c.id}-${auth.userId}`}
                        campaign={c}
                        userId={auth.userId}
                        navigate={navigate}
                        onSettled={loadData}
                        onAnimationFocusChange={onDistributionAnimationFocus}
                        onResultLabelChange={onDistributionResultLabel}
                      />
                    )}
                    {!distPhase && (
                      <button
                        type="button"
                        className="secondary"
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(
                            state === "fechada" ? `/campanhas/${c.id}/vencedores` : `/campanhas/${c.id}`
                          );
                        }}
                      >
                        {state === "fechada" ? "Ver vencedores" : "Ver detalhes"}
                      </button>
                    )}
                  </div>
                </article>
              );
            })}
          </div>
        </div>
      </section>
    </main>
  );
}
