import React, { useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { listPrizesByUser } from "../api";
import "./PrizesPage.css";

export default function PrizesPage() {
  const { auth } = useOutletContext();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadFailed, setLoadFailed] = useState(false);

  useEffect(() => {
    async function load() {
      try {
        setLoading(true);
        setLoadFailed(false);
        const prizes = await listPrizesByUser(auth.userId);
        setItems(prizes);
      } catch {
        setLoadFailed(true);
        setItems([]);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [auth.userId]);

  return (
    <main className="page prizes-page">
      <header className="prizes-page__header">
        <h1 className="prizes-page__title">Meus premios</h1>
      </header>

      {loading && <p className="muted">Carregando...</p>}
      {!loading && loadFailed && (
        <p className="muted prizes-page__empty">Nao foi possivel carregar a lista. Tente novamente.</p>
      )}

      {!loading && !loadFailed && items.length === 0 && (
        <p className="muted prizes-page__empty">Voce ainda nao ganhou nenhum premio.</p>
      )}

      {!loading && !loadFailed && items.length > 0 && (
        <ul className="prizes-page__list">
          {items.map((p) => (
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
