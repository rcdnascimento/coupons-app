import React from "react";
import { NavLink } from "react-router-dom";
import "./BottomNav.css";

function IconHome() {
  return (
    <svg
      className="bottom-nav__icon"
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden={true}
    >
      <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
      <polyline points="9 22 9 12 15 12 15 22" />
    </svg>
  );
}

function IconGift() {
  return (
    <svg
      className="bottom-nav__icon"
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden={true}
    >
      <polyline points="20 12 20 22 4 22 4 12" />
      <rect x="2" y="7" width="20" height="5" />
      <line x1="12" y1="22" x2="12" y2="7" />
      <path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z" />
      <path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z" />
    </svg>
  );
}

function IconUser() {
  return (
    <svg
      className="bottom-nav__icon"
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden={true}
    >
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
      <circle cx="12" cy="7" r="4" />
    </svg>
  );
}

export default function BottomNav() {
  return (
    <nav className="bottom-nav" aria-label="Navegação principal">
      <NavLink
        to="/"
        end
        aria-label="Início"
        className={({ isActive }) => "bottom-nav__link" + (isActive ? " bottom-nav__link--active" : "")}
      >
        <IconHome />
      </NavLink>
      <NavLink
        to="/premios"
        aria-label="Meus prêmios"
        className={({ isActive }) => "bottom-nav__link" + (isActive ? " bottom-nav__link--active" : "")}
      >
        <IconGift />
      </NavLink>
      <NavLink
        to="/conta"
        aria-label="Minha conta"
        className={({ isActive }) => "bottom-nav__link" + (isActive ? " bottom-nav__link--active" : "")}
      >
        <IconUser />
      </NavLink>
    </nav>
  );
}
