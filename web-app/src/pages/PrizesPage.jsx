import React, { useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { listPrizesByUser } from "../api";
import "./PrizesPage.css";

export default function PrizesPage() {
  const { auth } = useOutletContext();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      try {
        setLoading(true);
        setError("");
        const prizes = await listPrizesByUser(auth.userId);
        setItems(prizes);
      } catch (err) {
        setError(err.message);
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
      {error && <p className="error">{error}</p>}

      {!loading && !error && items.length === 0 && (
        <p className="muted prizes-page__empty">Voce ainda nao ganhou nenhum premio.</p>
      )}

      {!loading && items.length > 0 && (
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
