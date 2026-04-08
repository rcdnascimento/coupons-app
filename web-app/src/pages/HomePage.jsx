import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate, useOutletContext } from "react-router-dom";
import {
  getMeBalance,
  getMeProfile,
  getMyCampaignSubscriptionStatus,
  listCampaigns,
  subscribeCampaign
} from "../api";
import { useNotification } from "../context/NotificationProvider";
import {
  campaignEntryCostLabel,
  countdownLabel,
  distributionScheduleLabel,
  campaignStateBadgeLabel,
  getCampaignState,
  sortCampaigns
} from "../utils";
import "./HomePage.css";

const POLL_INTERVAL_MS = 1500;
const POLL_MAX_MS = 90_000;

function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms));
}

function pointsCostNumber(c) {
  return Math.max(0, Math.floor(Number(c?.pointsCost) || 0));
}

export default function HomePage() {
  const { auth, subscriptions, onSubscribe, replaceSubscriptions, onLogout } = useOutletContext();
  const { notifyError } = useNotification();
  const navigate = useNavigate();
  const [campaigns, setCampaigns] = useState([]);
  const [balance, setBalance] = useState(null);
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [processingByCampaign, setProcessingByCampaign] = useState({});
  const [tick, setTick] = useState(0);
  const [menuOpen, setMenuOpen] = useState(false);
  const pollingCampaignsRef = useRef(new Set());

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
            const r = await getMyCampaignSubscriptionStatus(campaignId, auth.userId, { silent: true });
            status = r?.status;
          } catch {
            continue;
          }
          if (status === "ACTIVE") {
            onSubscribe(campaignId);
            const bal = await getMeBalance(auth.userId).catch(() => null);
            setBalance(bal?.balance ?? null);
            return;
          }
          if (status === "PAYMENT_FAILED") {
            notifyError("O pagamento falhou. Pode tentar novamente.");
            return;
          }
        }
        notifyError("Nao foi possivel confirmar a inscricao a tempo. Atualize a pagina ou tente mais tarde.");
      } finally {
        pollingCampaignsRef.current.delete(campaignId);
        setCampaignProcessing(campaignId, false);
      }
    },
    [auth.userId, notifyError, onSubscribe, setCampaignProcessing]
  );

  async function loadData() {
    try {
      setLoading(true);
      const [camps, bal] = await Promise.all([
        listCampaigns(),
        getMeBalance(auth.userId).catch(() => null)
      ]);
      const sorted = sortCampaigns(camps);
      setCampaigns(sorted);
      setBalance(bal?.balance ?? null);
      const profileOut = await getMeProfile({
        userId: auth.userId,
        name: auth.name,
        email: auth.email
      }).catch(() => null);
      setProfile(profileOut);

      if (sorted.length) {
        const pairs = await Promise.all(
          sorted.map(async (c) => {
            try {
              const r = await getMyCampaignSubscriptionStatus(c.id, auth.userId, { silent: true });
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
      await subscribeCampaign(campaignId, auth.userId);
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
      notifyError("Codigo de indicacao indisponivel no momento.");
      return;
    }
    try {
      if (navigator.share) {
        await navigator.share({
          title: "Coupons",
          text: "Use meu codigo de indicacao no Coupons!",
          url: referralLink
        });
      } else {
        await navigator.clipboard.writeText(referralLink);
      }
      setMenuOpen(false);
    } catch {
      // Usuario pode cancelar o share nativo; nao trata como erro bloqueante.
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
              <Link
                className="menu-item menu-item--link"
                to="/admin"
                onClick={() => setMenuOpen(false)}
              >
                Painel admin
              </Link>
              <button type="button" className="menu-item" onClick={shareReferralLink}>
                Compartilhar link de indicacao
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
        <h2>Ola, {auth.name}</h2>
        <p className="muted">Pontos atuais: {balance ?? "indisponivel"}</p>
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
              const subscribed = !!subscriptions[c.id];
              const cost = pointsCostNumber(c);
              const insufficientFunds =
                !loading && balance !== null && cost > 0 && balance < cost;
              const balanceUnavailable = !loading && balance === null && cost > 0;
              const blockForBalance = cost > 0 && (loading || balance === null || balance < cost);
              const processing = !!processingByCampaign[c.id];
              const subscribeDisabled =
                state !== "aberta" || blockForBalance || processing;

              return (
                <article
                  className="campaign-card campaign-card--clickable"
                  key={c.id}
                  role="button"
                  tabIndex={0}
                  aria-label={`Ver detalhes da campanha: ${c.title}`}
                  onClick={() => navigate(`/campanhas/${c.id}`)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" || e.key === " ") {
                      e.preventDefault();
                      navigate(`/campanhas/${c.id}`);
                    }
                  }}
                >
                  <div className="campaign-card__top">
                    <p className={`badge badge--campaign-state ${state}`}>{campaignStateBadgeLabel(state, c)}</p>
                    {state === "aberta" && (
                      <span className="campaign-card__time">{countdownLabel(c.distributionAt)}</span>
                    )}
                  </div>
                  <h3>{c.title}</h3>
                  <p className="muted">{campaignEntryCostLabel(c.pointsCost)}</p>
                  {cost > 0 && insufficientFunds && state === "aberta" && (
                    <p className="campaign-card__balance-hint" onClick={(e) => e.stopPropagation()}>
                      Saldo insuficiente para esta inscricao.
                    </p>
                  )}
                  {cost > 0 && balanceUnavailable && state === "aberta" && (
                    <p className="campaign-card__balance-hint" onClick={(e) => e.stopPropagation()}>
                      Nao foi possivel verificar o saldo. Atualize a pagina.
                    </p>
                  )}
                  {state !== "fechada" && (
                    <p className="muted campaign-card__distribution">
                      {distributionScheduleLabel(c.distributionAt)}
                    </p>
                  )}
                  <div className="campaign-card__actions" onClick={(e) => e.stopPropagation()}>
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
                      <p className="ok">Inscricao registrada</p>
                    )}
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
