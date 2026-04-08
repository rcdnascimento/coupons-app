import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import BottomNav from "../components/BottomNav";

export default function AuthenticatedLayout({ auth, contextValue }) {
  if (!auth) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="app-authenticated">
      <Outlet context={contextValue} />
      <BottomNav />
    </div>
  );
}
