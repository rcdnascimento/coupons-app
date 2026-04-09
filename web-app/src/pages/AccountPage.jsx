import React, { useCallback, useEffect, useMemo, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { getMeBalance, getMeProfile } from "../api";
import { useNotification } from "../context/NotificationProvider";
import "./AccountPage.css";

export default function AccountPage() {
  const { auth, onLogout } = useOutletContext();
  const { notifyError } = useNotification();
  const [balance, setBalance] = useState(null);
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    try {
      setLoading(true);
      const bal = await getMeBalance().catch(() => null);
      setBalance(bal?.balance ?? null);
      const profileOut = await getMeProfile({
        name: auth.name,
        email: auth.email
      }).catch(() => null);
      setProfile(profileOut);
    } finally {
      setLoading(false);
    }
  }, [auth.email, auth.name, auth.userId]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    const onRefreshBalance = () => {
      load();
    };
    window.addEventListener("coupons:balance-refresh", onRefreshBalance);
    return () => window.removeEventListener("coupons:balance-refresh", onRefreshBalance);
  }, [load]);

  const referralLink = useMemo(() => {
    const code = profile?.referralCode || auth?.referralCode;
    if (!code) return "";
    const url = new URL(window.location.origin + "/register");
    url.searchParams.set("ref", code);
    return url.toString();
  }, [auth, profile]);

  async function shareReferralLink() {
    if (!referralLink) {
      notifyError("Código de indicação indisponível no momento.");
      return;
    }
    try {
      if (navigator.share) {
        await navigator.share({
          title: "Coupons",
          text: "Use meu código de indicação no Coupons!",
          url: referralLink
        });
      } else {
        await navigator.clipboard.writeText(referralLink);
      }
    } catch {
      // cancelamento do share nativo
    }
  }

  return (
    <main className="page account-page">
      <header className="account-page__header">
        <h1 className="account-page__title">Minha conta</h1>
      </header>

      <section className="account-page__section">
        <h2 className="account-page__name">{auth.name}</h2>
        <p className="muted">{auth.email}</p>
        <p className="muted account-page__balance">
          Você tem {loading ? "..." : balance != null ? balance : "indisponível"} moedas
        </p>
      </section>

      <section className="account-page__section account-page__actions">
        <button type="button" className="primary" onClick={shareReferralLink} disabled={!referralLink}>
          Compartilhar link de indicação
        </button>
        <button type="button" className="ghost" onClick={onLogout}>
          Sair
        </button>
      </section>
    </main>
  );
}
