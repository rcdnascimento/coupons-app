import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getCampaignWinners, listCampaigns } from "../api";
import "./CampaignPage.css";

export default function CampaignWinnersPage() {
  const { campaignId } = useParams();
  const [campaign, setCampaign] = useState(null);
  const [winners, setWinners] = useState(null);

  useEffect(() => {
    async function load() {
      const all = await listCampaigns();
      const c = all.find((item) => item.id === campaignId) || null;
      setCampaign(c);
      if (!c) {
        setWinners(null);
        return;
      }
      const w = await getCampaignWinners(campaignId).catch(() => ({ entries: [] }));
      setWinners(w);
    }
    load();
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
        </div>

        <h2 className="campaign-detail__title">Vencedores</h2>
        <p className="muted campaign-detail__date">{campaign.title}</p>

        <div className="campaign-detail__winners">
          {!winners?.entries?.length ? (
            <p className="muted">Nenhum vencedor divulgado ainda.</p>
          ) : (
            <ul className="campaign-detail__winner-list">
              {winners.entries.map((w) => (
                <li key={`${w.rank}-${w.userId}`} className="campaign-detail__winner-row">
                  <span className="campaign-detail__winner-rank muted">{w.rank}.</span>
                  <div className="campaign-detail__winner-main">
                    <span className="campaign-detail__winner-name">{w.winnerDisplayName || "Nome indisponivel"}</span>
                    <span className="campaign-detail__winner-prize">{w.couponTitle}</span>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

        <p className="muted tiny campaign-detail__cross-link">
          <Link to={`/campanhas/${campaignId}`}>Ver detalhes da campanha</Link>
        </p>
      </div>
    </main>
  );
}
