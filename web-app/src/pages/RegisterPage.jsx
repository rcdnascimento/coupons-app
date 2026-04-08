import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../api";
import "./RegisterPage.css";

function getReferralFromUrl() {
  const p = new URLSearchParams(window.location.search);
  return p.get("ref") || "";
}

export default function RegisterPage({ onAuth }) {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    email: "",
    password: "",
    name: "",
    referralCode: getReferralFromUrl()
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function submit(e) {
    e.preventDefault();
    try {
      setLoading(true);
      setError("");
      const payload = {
        email: form.email,
        password: form.password,
        name: form.name
      };
      if (form.referralCode?.trim()) payload.referralCode = form.referralCode.trim();
      const out = await register(payload);
      onAuth(out);
      navigate("/");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="page auth-page register-page">
      <header className="register-page__header">
        <p className="brand auth-page__brand register-page__brand" aria-label="Coupons">
          <span className="brand-coup">Coup</span>
          <span className="brand-ons">ons</span>
        </p>
      </header>
      <div className="register-page__body">
        <div className="register-page__center">
          <div className="register-page__intro">
            <p className="register-page__intro-title">Que bom ter você aqui</p>
            <p className="register-page__intro-text">
              Cadastre-se em poucos passos para participar das campanhas, acompanhar seus prêmios e usar seu
              código de indicação com amigos.
            </p>
          </div>
          <form onSubmit={submit} aria-label="Criar conta">
            <label>
              Nome
              <input
                required
                value={form.name}
                onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
              />
            </label>
            <label>
              E-mail
              <input
                required
                type="email"
                value={form.email}
                onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
              />
            </label>
            <label>
              Senha
              <input
                required
                type="password"
                minLength={8}
                value={form.password}
                onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
              />
            </label>
            <label>
              Código de indicação (opcional)
              <input
                value={form.referralCode}
                onChange={(e) => setForm((f) => ({ ...f, referralCode: e.target.value }))}
                placeholder="ABCD1234"
              />
            </label>
            <button disabled={loading} type="submit" className="primary">
              {loading ? "Criando..." : "Criar conta"}
            </button>
          </form>
          {error && <p className="error">{error}</p>}
          <p className="muted tiny">
            Ja tem conta? <Link to="/login">Entrar</Link>
          </p>
        </div>
      </div>
    </main>
  );
}
