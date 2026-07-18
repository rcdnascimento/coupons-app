import React, { useEffect, useState } from "react";
import { getDailyChestToday, openDailyChest } from "../api";
import { sleep } from "../utils";
import openedChestImg from "../images/opened-chest.png";
import halfOpenedChestImg from "../images/half-opened-chest.png";
import closedChestImg from "../images/closed-chest.png";

const FAB_PULSE_CYCLE_MS = 1600;
const IDLE_FRAME_COUNT = 2;
const OPENING_FRAME_COUNT = 4;

function rewardLabel(value) {
  const n = Math.max(0, Number(value) || 0);
  if (n === 1) return "1 moeda";
  return `${n} moedas`;
}

export default function DailyChestFab({ userId }) {
  const [loading, setLoading] = useState(true);
  const [today, setToday] = useState(null);
  const [opening, setOpening] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [frameIndex, setFrameIndex] = useState(0);

  async function loadToday(silent = true) {
    try {
      setLoading(true);
      const data = await getDailyChestToday({ silent });
      setToday(data);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!userId) return;
    loadToday(true);
  }, [userId]);

  const openedToday = !!today?.openedToday;
  const rewardCoins = today?.rewardCoins ?? null;

  useEffect(() => {
    if (openedToday) {
      setFrameIndex(0);
      return;
    }
    const frames = opening
      ? [closedChestImg, halfOpenedChestImg, openedChestImg, halfOpenedChestImg]
      : [closedChestImg, halfOpenedChestImg];
    const intervalMs = opening
      ? Math.round(FAB_PULSE_CYCLE_MS / OPENING_FRAME_COUNT)
      : Math.round(FAB_PULSE_CYCLE_MS / IDLE_FRAME_COUNT);
    const id = window.setInterval(() => {
      setFrameIndex((prev) => (prev + 1) % frames.length);
    }, intervalMs);
    return () => window.clearInterval(id);
  }, [openedToday, opening]);

  const idleFrames = [closedChestImg, halfOpenedChestImg];
  const openingFrames = [closedChestImg, halfOpenedChestImg, openedChestImg, halfOpenedChestImg];
  const buttonChestImg = openedToday
    ? openedChestImg
    : (opening ? openingFrames : idleFrames)[frameIndex % (opening ? openingFrames.length : idleFrames.length)];
  const modalChestImg = openedToday
    ? openedChestImg
    : (opening ? openingFrames : [closedChestImg])[frameIndex % (opening ? openingFrames.length : 1)];

  async function handleOpenChest() {
    if (opening || openedToday) return;
    setModalOpen(true);
    setOpening(true);

    const animMs = 1500 + Math.floor(Math.random() * 1000);
    try {
      const [result] = await Promise.all([openDailyChest(), sleep(animMs)]);
      setToday(result);
      window.dispatchEvent(new CustomEvent("coupons:balance-refresh"));
    } finally {
      setOpening(false);
    }
  }

  function handleFabClick() {
    if (opening || loading) return;
    if (openedToday) {
      setModalOpen(true);
      return;
    }
    handleOpenChest();
  }

  if (!userId) return null;

  return (
    <>
      <button
        type="button"
        className={`daily-chest-fab${openedToday ? "" : " daily-chest-fab--available"}`}
        onClick={handleFabClick}
        disabled={loading || opening}
        aria-label={openedToday ? "Ver prêmio do baú de hoje" : "Abrir baú diário"}
        title={openedToday ? "Baú já aberto — ver prêmio" : "Abrir baú diário"}
      >
        <img className="daily-chest-fab__img" src={buttonChestImg} alt="" aria-hidden={true} />
      </button>

      {modalOpen && (
        <div
          className="daily-chest-modal"
          role="dialog"
          aria-modal="true"
          aria-label="Baú da sorte"
          onClick={() => {
            if (!opening) setModalOpen(false);
          }}
        >
          <div className="daily-chest-modal__panel" onClick={(e) => e.stopPropagation()}>
            <button
              type="button"
              className="daily-chest-modal__close"
              aria-label="Fechar modal"
              onClick={() => setModalOpen(false)}
            >
              ×
            </button>
            {!openedToday && opening && (
              <div className="daily-chest-modal__opening" aria-live="polite">
                <img className="daily-chest-modal__chest-img" src={modalChestImg} alt="" aria-hidden={true} />
                <p>Abrindo o baú...</p>
              </div>
            )}

            {openedToday && !opening && (
              <p className="daily-chest-modal__result">
                <img
                  className="daily-chest-modal__chest-img daily-chest-modal__chest-img--static"
                  src={openedChestImg}
                  alt=""
                  aria-hidden={true}
                />
                Você ganhou <strong>{rewardLabel(rewardCoins)}</strong> hoje!
              </p>
            )}
          </div>
        </div>
      )}
    </>
  );
}
