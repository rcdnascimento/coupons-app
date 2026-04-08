import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login } from "../api";
import "./LoginPage.css";

export default function LoginPage({ onAuth }) {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function submit(e) {
    e.preventDefault();
    try {
      setLoading(true);
      setError("");
      const out = await login(form);
      onAuth(out);
      navigate("/");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="page auth-page login-page">
      <header className="login-page__header">
        <p className="brand auth-page__brand login-page__brand" aria-label="Coupons">
          <span className="brand-coup">Coup</span>
          <span className="brand-ons">ons</span>
        </p>
      </header>
      <div className="login-page__body">
        <div className="login-page__center">
          <div className="login-page__intro">
            <p className="login-page__intro-title">Bem-vindo de volta</p>
            <p className="login-page__intro-text">
              Entre com seu e-mail para ver campanhas, acompanhar seus premios e gerenciar sua conta com
              tranquilidade.
            </p>
          </div>
          <form onSubmit={submit} aria-label="Login">
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
            <button disabled={loading} type="submit" className="primary">
              {loading ? "Entrando..." : "Entrar"}
            </button>
          </form>
          {error && <p className="error">{error}</p>}
          <p className="muted tiny auth-page__register-line">
            <span>Nao tem conta?</span>
            <Link to="/register" className="auth-page__register-link">
              Registrar
            </Link>
          </p>
        </div>
      </div>
    </main>
  );
}
