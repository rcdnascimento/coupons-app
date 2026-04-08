import React, { createContext, useCallback, useContext, useEffect, useState } from "react";
import { setApiNotificationHandlers } from "../api";
import "./NotificationProvider.css";

const NotificationContext = createContext(null);

export function useNotification() {
  const ctx = useContext(NotificationContext);
  if (!ctx) {
    return {
      notifyError: () => {},
      notifySuccess: () => {},
      notify: () => {}
    };
  }
  return ctx;
}

/**
 * Toast inferior (estilo lorie-web: barra fixa, animação, auto-dismiss).
 */
export function NotificationProvider({ children }) {
  const [toast, setToast] = useState(null);
  const [exiting, setExiting] = useState(false);

  const dismiss = useCallback(() => {
    setExiting(true);
    window.setTimeout(() => {
      setToast(null);
      setExiting(false);
    }, 280);
  }, []);

  const pushToast = useCallback((message, variant = "error") => {
    if (!message || !String(message).trim()) return;
    setExiting(false);
    setToast({ message: String(message).trim(), variant, key: Date.now() });
  }, []);

  const notifyError = useCallback((message) => pushToast(message, "error"), [pushToast]);
  const notifySuccess = useCallback((message) => pushToast(message, "success"), [pushToast]);

  useEffect(() => {
    setApiNotificationHandlers({
      onError: notifyError
    });
    return () => setApiNotificationHandlers(null);
  }, [notifyError]);

  useEffect(() => {
    if (!toast) return undefined;
    const durationMs = toast.variant === "success" ? 3800 : 5200;
    const id = window.setTimeout(() => dismiss(), durationMs);
    return () => window.clearTimeout(id);
  }, [toast, dismiss]);

  const value = { notifyError, notifySuccess, notify: pushToast };

  return (
    <NotificationContext.Provider value={value}>
      {children}
      {toast && (
        <div className="bottom-notification" role="status" aria-live="polite">
          <div
            className={`bottom-notification__panel bottom-notification__panel--${toast.variant} ${
              exiting ? "bottom-notification__panel--out" : ""
            }`}
          >
            <span className="bottom-notification__icon" aria-hidden={true}>
              {toast.variant === "success" ? (
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                  <polyline points="22 4 12 14.01 9 11.01" />
                </svg>
              ) : (
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
              )}
            </span>
            <p className="bottom-notification__text">{toast.message}</p>
            <button type="button" className="bottom-notification__close" onClick={dismiss} aria-label="Fechar">
              ×
            </button>
          </div>
        </div>
      )}
    </NotificationContext.Provider>
  );
}
