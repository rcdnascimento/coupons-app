import React, { useEffect, useMemo, useState } from "react";
import { useOutletContext, useSearchParams } from "react-router-dom";
import { BFF_BASE_URL, listMyPrizes } from "../api";
import "./PrizesPage.css";

function resolveMediaUrl(pathLike) {
  const s = typeof pathLike === "string" ? pathLike.trim() : "";
  if (!s) return "";
  if (/^https?:\/\//i.test(s)) return s;
  if (s.startsWith("/")) return `${BFF_BASE_URL}${s}`;
  return `${BFF_BASE_URL}/${s}`;
}

function formatPrizeDate(iso) {
  if (!iso) return "";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "";
  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "long",
    timeStyle: "short"
  }).format(d);
}

function statusLabelForUser(status) {
  const s = typeof status === "string" ? status.trim().toUpperCase() : "";
  switch (s) {
    case "DELIVERED":
      return "Entregue";
    case "PENDING":
      return "Em processamento";
    case "FAILED":
    case "ERROR":
      return "Não foi possível entregar";
    default:
      return s ? status : "—";
  }
}

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
        const prizes = await listMyPrizes(filterCampaignId || undefined);
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
          {filteredItems.map((p) => {
            const logoUrl = resolveMediaUrl(p.companyLogoUrl);
            const campaignName =
              (p.campaignTitle && String(p.campaignTitle).trim()) || "Campanha";
            const partnerName = (p.companyName && String(p.companyName).trim()) || "";
            const prizeHeading =
              (p.couponTitle && String(p.couponTitle).trim()) || "Seu cupom";
            const when = formatPrizeDate(p.processedAt);

            return (
              <li key={p.id} className="prizes-page__item">
                <div className="prizes-page__item-row">
                  <div className="prizes-page__brand">
                    {logoUrl ? (
                      <img
                        className="prizes-page__logo"
                        src={logoUrl}
                        alt={partnerName ? `Logo ${partnerName}` : "Logo da empresa parceira"}
                        loading="lazy"
                      />
                    ) : (
                      <div className="prizes-page__logo prizes-page__logo--placeholder" aria-hidden />
                    )}
                  </div>
                  <div className="prizes-page__body">
                    <h3 className="prizes-page__item-title">{prizeHeading}</h3>
                    <p className="prizes-page__campaign">{campaignName}</p>
                    {partnerName ? (
                      <p className="muted prizes-page__item-meta">
                        Parceiro: {partnerName}
                      </p>
                    ) : null}
                    {when ? (
                      <p className="muted prizes-page__item-meta">Recebido em {when}</p>
                    ) : null}
                    <p className="muted prizes-page__item-meta">
                      Situação: {statusLabelForUser(p.status)}
                    </p>
                    {p.couponCode ? (
                      <p className="muted prizes-page__item-meta prizes-page__code">
                        Código para usar na loja:{" "}
                        <span className="prizes-page__code-value">{p.couponCode}</span>
                      </p>
                    ) : null}
                  </div>
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </main>
  );
}
