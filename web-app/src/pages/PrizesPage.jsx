import React, { useEffect, useMemo, useState } from "react";
import { useOutletContext, useSearchParams } from "react-router-dom";
import { listPrizesByUser } from "../api";
import "./PrizesPage.css";

export default function PrizesPage() {
  const { auth } = useOutletContext();
  const [searchParams] = useSearchParams();
  const filterCampaignId = searchParams.get("campaignId") || "";
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadFailed, setLoadFailed] = useState(false);

  const filteredItems = useMemo(() => {
    if (!filterCampaignId) return items;
    return items.filter((p) => String(p.campaignId) === String(filterCampaignId));
  }, [items, filterCampaignId]);

  useEffect(() => {
    async function load() {
      try {
        setLoading(true);
        setLoadFailed(false);
        const prizes = await listPrizesByUser(
          auth.userId,
          filterCampaignId || undefined
        );
        setItems(Array.isArray(prizes) ? prizes : []);
      } catch {
        setLoadFailed(true);
        setItems([]);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [auth.userId, filterCampaignId]);

  return (
    <main className="page prizes-page">
      <header className="prizes-page__header">
        <h1 className="prizes-page__title">Meus prêmios</h1>
      </header>

      {loading && <p className="muted">Carregando...</p>}
      {!loading && loadFailed && (
        <p className="muted prizes-page__empty">Não foi possível carregar a lista. Tente novamente.</p>
      )}

      {!loading && !loadFailed && filterCampaignId && filteredItems.length === 0 && (
        <p className="muted prizes-page__empty">
          Nenhum prêmio nesta campanha na sua conta.
        </p>
      )}

      {!loading && !loadFailed && !filterCampaignId && items.length === 0 && (
        <p className="muted prizes-page__empty">Você ainda não ganhou nenhum prêmio.</p>
      )}

      {!loading && !loadFailed && filteredItems.length > 0 && (
        <ul className="prizes-page__list">
          {filteredItems.map((p) => (
            <li key={p.id} className="prizes-page__item">
              <h3 className="prizes-page__item-title">Cupom {p.couponCode}</h3>
              <p className="muted prizes-page__item-meta">Status: {p.status}</p>
              <p className="muted prizes-page__item-meta">Campanha: {p.campaignId}</p>
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
