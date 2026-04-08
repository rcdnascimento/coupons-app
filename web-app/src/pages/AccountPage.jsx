import React, { useEffect, useMemo, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { getMeBalance, getMeProfile } from "../api";
import "./AccountPage.css";

export default function AccountPage() {
  const { auth, onLogout } = useOutletContext();
  const [balance, setBalance] = useState(null);
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function load() {
    try {
      setLoading(true);
      setError("");
      const bal = await getMeBalance(auth.userId).catch(() => null);
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
    load();
  }, [auth.userId]);

  const referralLink = useMemo(() => {
    const code = profile?.referralCode || auth?.referralCode;
    if (!code) return "";
    const url = new URL(window.location.origin + "/register");
    url.searchParams.set("ref", code);
    return url.toString();
  }, [auth, profile]);

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
          Pontos atuais:{" "}
          {loading ? "..." : balance != null ? balance : "indisponivel"}
        </p>
        {error && <p className="error">{error}</p>}
      </section>

      <section className="account-page__section account-page__actions">
        <button type="button" className="primary" onClick={shareReferralLink} disabled={!referralLink}>
          Compartilhar link de indicacao
        </button>
        <button type="button" className="ghost" onClick={onLogout}>
          Sair
        </button>
      </section>
    </main>
  );
}
