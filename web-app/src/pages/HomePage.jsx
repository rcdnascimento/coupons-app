import React, { useEffect, useMemo, useState } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import { getMeBalance, getMeProfile, listCampaigns, subscribeCampaign } from "../api";
import {
  campaignEntryCostLabel,
  countdownLabel,
  distributionScheduleLabel,
  getCampaignState,
  sortCampaigns
} from "../utils";
import "./HomePage.css";

export default function HomePage() {
  const { auth, subscriptions, onSubscribe, onLogout } = useOutletContext();
  const navigate = useNavigate();
  const [campaigns, setCampaigns] = useState([]);
  const [balance, setBalance] = useState(null);
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [tick, setTick] = useState(0);
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    const id = setInterval(() => setTick((v) => v + 1), 1000);
    return () => clearInterval(id);
  }, []);

  async function loadData() {
    try {
      setLoading(true);
      setError("");
      const [camps, bal] = await Promise.all([
        listCampaigns(),
        getMeBalance(auth.userId).catch(() => null)
      ]);
      setCampaigns(sortCampaigns(camps));
      setBalance(bal?.balance ?? null);
      const profileOut = await getMeProfile({
        userId: auth.userId,
        name: auth.name,
        email: auth.email
      }).catch(() => null);
      setProfile(profileOut);
    } catch (err) {
      setError(err.message);
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
    try {
      setLoading(true);
      setError("");
      await subscribeCampaign(campaignId, auth.userId);
      onSubscribe(campaignId);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function shareReferralLink() {
    if (!referralLink) {
      setError("Codigo de indicacao indisponivel no momento.");
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
        {error && <p className="error">{error}</p>}
        <div className="carousel" data-tick={tick}>
          <div className="carousel-track">
            {campaigns.map((c) => {
              const state = getCampaignState(c);
              const subscribed = !!subscriptions[c.id];
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
                    <p className={`badge ${state}`}>{state.replace("_", " ")}</p>
                    {state === "aberta" && (
                      <span className="campaign-card__time">{countdownLabel(c.distributionAt)}</span>
                    )}
                  </div>
                  <h3>{c.title}</h3>
                  <p className="muted">{campaignEntryCostLabel(c.pointsCost)}</p>
                  {state !== "fechada" && (
                    <p className="muted campaign-card__distribution">
                      {distributionScheduleLabel(c.distributionAt)}
                    </p>
                  )}
                  {!subscribed ? (
                    <button
                      type="button"
                      className="primary"
                      onClick={(e) => {
                        e.stopPropagation();
                        subscribe(c.id);
                      }}
                      disabled={state !== "aberta" || loading}
                    >
                      Inscrever
                    </button>
                  ) : (
                    <p className="ok" onClick={(e) => e.stopPropagation()}>
                      Inscricao registrada
                    </p>
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
                </article>
              );
            })}
          </div>
        </div>
      </section>
    </main>
  );
}
