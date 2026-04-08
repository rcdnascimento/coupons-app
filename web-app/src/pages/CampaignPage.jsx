import React, { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { getCampaignSummary, listCampaigns } from "../api";
import { campaignEntryCostLabel, countdownLabel, distributionScheduleLabel, getCampaignState } from "../utils";
import "./CampaignPage.css";

export default function CampaignPage() {
  const { campaignId } = useParams();
  const navigate = useNavigate();
  const [campaign, setCampaign] = useState(null);
  const [summary, setSummary] = useState(null);

  useEffect(() => {
    async function loadCampaign() {
      const all = await listCampaigns();
      const c = all.find((item) => item.id === campaignId) || null;
      setCampaign(c);
      if (!c) {
        setSummary(null);
        return;
      }
      const s = await getCampaignSummary(campaignId).catch(() => null);
      setSummary(s);
    }
    loadCampaign();
  }, [campaignId]);

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
          <p className="campaign-detail__empty">Campanha nao encontrada.</p>
        </div>
      </main>
    );
  }

  const campaignState = getCampaignState(campaign);
  const closed = campaignState === "fechada";

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
            <p className={`badge ${campaignState}`}>{campaignState.replace("_", " ")}</p>
            {campaignState === "aberta" && (
              <span className="campaign-card__time">{countdownLabel(campaign.distributionAt)}</span>
            )}
          </div>
        </div>

        <h2 className="campaign-detail__title">{campaign.title}</h2>
        <p className="muted campaign-detail__cost">{campaignEntryCostLabel(campaign.pointsCost)}</p>
        {!closed && (
          <p className="muted campaign-detail__date">{distributionScheduleLabel(campaign.distributionAt)}</p>
        )}

        <div className="campaign-detail__prizes">
          <h3 className="campaign-detail__prizes-heading">Premios possiveis</h3>
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
