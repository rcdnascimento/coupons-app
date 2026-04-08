import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getCampaignWinners, listCampaigns } from "../api";
import "./CampaignPage.css";

export default function CampaignWinnersPage() {
  const { campaignId } = useParams();
  const [loadFailed, setLoadFailed] = useState(false);
  const [winnersError, setWinnersError] = useState(false);
  const [campaign, setCampaign] = useState(null);
  const [winners, setWinners] = useState(undefined);

  useEffect(() => {
    setLoadFailed(false);
    setWinnersError(false);
    async function load() {
      try {
        const all = await listCampaigns();
        const c = all.find((item) => item.id === campaignId) || null;
        setCampaign(c);
        if (!c) {
          setWinners(null);
          return;
        }
        try {
          const w = await getCampaignWinners(campaignId);
          setWinners(w);
        } catch {
          setWinnersError(true);
          setWinners(null);
        }
      } catch {
        setLoadFailed(true);
        setCampaign(null);
        setWinners(null);
      }
    }
    load();
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
          {winners === undefined && !winnersError ? (
            <p className="muted">...</p>
          ) : winnersError ? (
            <p className="muted">Não foi possível carregar a lista de vencedores.</p>
          ) : !winners?.entries?.length ? (
            <p className="muted">Nenhum vencedor divulgado ainda.</p>
          ) : (
            <ul className="campaign-detail__winner-list">
              {winners.entries.map((w) => (
                <li key={`${w.rank}-${w.userId}`} className="campaign-detail__winner-row">
                  <span className="campaign-detail__winner-rank muted">{w.rank}.</span>
                  <div className="campaign-detail__winner-main">
                    <span className="campaign-detail__winner-name">{w.winnerDisplayName || "Nome indisponível"}</span>
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
