import React, { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { getCampaignSummary, listCampaigns } from "../api";
import {
  campaignCardDescriptionText,
  campaignPointsCornerLabel,
  countdownLabel,
  distributionScheduleLabel,
  getCampaignState
} from "../utils";
import "./CampaignPage.css";

export default function CampaignPage() {
  const { campaignId } = useParams();
  const navigate = useNavigate();
  const [loadFailed, setLoadFailed] = useState(false);
  const [campaign, setCampaign] = useState(null);
  const [summary, setSummary] = useState(null);

  useEffect(() => {
    setLoadFailed(false);
    async function loadCampaign() {
      try {
        const all = await listCampaigns();
        const c = all.find((item) => item.id === campaignId) || null;
        setCampaign(c);
        if (!c) {
          setSummary(null);
          return;
        }
        try {
          const s = await getCampaignSummary(campaignId);
          setSummary(s);
        } catch {
          setSummary(null);
        }
      } catch {
        setLoadFailed(true);
        setCampaign(null);
        setSummary(null);
      }
    }
    loadCampaign();
  }, [campaignId]);

  if (loadFailed) {
    return (
      <main className="page campaign-page">
        <div className="campaign-detail">
          <Link to="/" className="campaign-back-btn" aria-label="Voltar">
            <svg
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
              <path d="M15 18l-6-6 6-6" />
            </svg>
          </Link>
          <p className="campaign-detail__empty">Não foi possível carregar as campanhas.</p>
        </div>
      </main>
    );
  }

  if (!campaign) {
    return (
      <main className="page campaign-page">
        <div className="campaign-detail">
          <Link to="/" className="campaign-back-btn" aria-label="Voltar">
            <svg
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
              <path d="M15 18l-6-6 6-6" />
            </svg>
          </Link>
          <p className="campaign-detail__empty">Campanha não encontrada.</p>
        </div>
      </main>
    );
  }

  const campaignState = getCampaignState(campaign);
  const closed = campaignState === "fechada";
  const cost = Math.max(0, Math.floor(Number(campaign.pointsCost) || 0));

  return (
    <main className="page campaign-page">
      <div className="campaign-detail">
        <div className="campaign-detail__header">
          <Link to="/" className="campaign-back-btn" aria-label="Voltar">
            <svg
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
              <path d="M15 18l-6-6 6-6" />
            </svg>
          </Link>
          <div className="campaign-card__top campaign-detail__meta">
            <div className="campaign-card__top-row">
              <div className="campaign-card__top-left">
                {campaignState === "fechada" && (
                  <p className={`badge badge--campaign-state ${campaignState}`}>Finalizada</p>
                )}
              </div>
              <span className="campaign-card__coins" title="Custo de entrada na campanha">
                {campaignPointsCornerLabel(cost)}
              </span>
            </div>
            {campaignState === "aberta" && (
              <p className="campaign-card__countdown-line" aria-live="polite">
                {countdownLabel(campaign.distributionAt)}
              </p>
            )}
          </div>
        </div>

        <h2 className="campaign-detail__title">{campaign.title}</h2>
        <p className="muted campaign-card__description">{campaignCardDescriptionText(campaign.description)}</p>
        {!closed && (
          <p className="muted campaign-detail__date">{distributionScheduleLabel(campaign.distributionAt)}</p>
        )}

        <div className="campaign-detail__prizes">
          <h3 className="campaign-detail__prizes-heading">Prêmios possíveis</h3>
          {!summary?.possiblePrizes?.length ? (
            <p className="muted">...</p>
          ) : (
            <ul className="campaign-detail__prize-list">
              {summary.possiblePrizes.map((p, idx) => (
                <li key={`${idx}-${p.title}`} className="campaign-detail__prize-row">
                  <span className="campaign-detail__prize-title">{p.title}</span>
                  <span className="campaign-detail__prize-qty muted">
                    {p.quantity} {p.quantity === 1 ? "unidade" : "unidades"}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>

        {closed && (
          <button
            type="button"
            className="secondary"
            onClick={() => navigate(`/campanhas/${campaignId}/vencedores`)}
          >
            Ver vencedores
          </button>
        )}
      </div>
    </main>
  );
}
