import React, { useState } from "react";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import AuthenticatedLayout from "./layouts/AuthenticatedLayout";
import AccountPage from "./pages/AccountPage";
import CampaignPage from "./pages/CampaignPage";
import CampaignWinnersPage from "./pages/CampaignWinnersPage";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import PrizesPage from "./pages/PrizesPage";
import RegisterPage from "./pages/RegisterPage";
import { loadAuth, loadSubscriptions, saveAuth, saveSubscriptions } from "./session";

export default function App() {
  const [auth, setAuth] = useState(() => loadAuth());
  const [subscriptions, setSubscriptions] = useState(() => loadSubscriptions());

  function onAuth(next) {
    setAuth(next);
    saveAuth(next);
  }

  function onSubscribe(campaignId) {
    const next = { ...subscriptions, [campaignId]: true };
    setSubscriptions(next);
    saveSubscriptions(next);
  }

  function onLogout() {
    setAuth(null);
    saveAuth(null);
  }

  const authenticatedOutletContext = {
    auth,
    subscriptions,
    onSubscribe,
    onLogout
  };

  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/login"
          element={auth ? <Navigate to="/" replace /> : <LoginPage onAuth={onAuth} />}
        />
        <Route
          path="/register"
          element={auth ? <Navigate to="/" replace /> : <RegisterPage onAuth={onAuth} />}
        />
        <Route
          element={
            <AuthenticatedLayout auth={auth} contextValue={authenticatedOutletContext} />
          }
        >
          <Route index element={<HomePage />} />
          <Route path="campanhas/:campaignId/vencedores" element={<CampaignWinnersPage />} />
          <Route path="campanhas/:campaignId" element={<CampaignPage />} />
          <Route path="premios" element={<PrizesPage />} />
          <Route path="conta" element={<AccountPage />} />
        </Route>
        <Route path="*" element={<Navigate to={auth ? "/" : "/login"} replace />} />
      </Routes>
    </BrowserRouter>
  );
}
