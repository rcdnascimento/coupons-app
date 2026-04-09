import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { useNotification } from "../context/NotificationProvider";
import {
  addCouponToCampaign,
  createCampaign,
  createCompany,
  createCoupon,
  creditUserLedger,
  getCampaign,
  getCoupon,
  listCampaigns,
  listCompaniesAdmin,
  listCouponsAdmin,
  patchCampaign,
  patchCoupon,
  searchCoupons,
  searchUsersAdmin,
  uploadAdminImage,
  BFF_BASE_URL
} from "../api";
import {
  campaignCardDescriptionText,
  campaignPointsCornerLabel,
  campaignStateBadgeLabel,
  distributionRedemptionInRoughLabel,
  getCampaignState,
  sortCampaigns
} from "../utils";
import "./AdminPage.css";

function formatDateForDatetimeLocal(d) {
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function toDatetimeLocalValue(iso) {
  if (!iso) return "";
  return formatDateForDatetimeLocal(new Date(iso));
}

function presetDatetimeLocalFromNow(offsetMinutes) {
  const d = new Date();
  d.setMinutes(d.getMinutes() + offsetMinutes);
  return formatDateForDatetimeLocal(d);
}

function fromDatetimeLocal(str) {
  if (!str) return null;
  return new Date(str).toISOString();
}

function cnpjDigitsOnly(v) {
  return String(v || "").replace(/\D+/g, "").slice(0, 14);
}

function formatCnpjMask(v) {
  const d = cnpjDigitsOnly(v);
  if (!d) return "";
  if (d.length <= 2) return d;
  if (d.length <= 5) return `${d.slice(0, 2)}.${d.slice(2)}`;
  if (d.length <= 8) return `${d.slice(0, 2)}.${d.slice(2, 5)}.${d.slice(5)}`;
  if (d.length <= 12) return `${d.slice(0, 2)}.${d.slice(2, 5)}.${d.slice(5, 8)}/${d.slice(8)}`;
  return `${d.slice(0, 2)}.${d.slice(2, 5)}.${d.slice(5, 8)}/${d.slice(8, 12)}-${d.slice(12)}`;
}

function resolveMediaUrl(pathLike) {
  const s = typeof pathLike === "string" ? pathLike.trim() : "";
  if (!s) return "";
  if (/^https?:\/\//i.test(s)) return s;
  if (s.startsWith("/")) return `${BFF_BASE_URL}${s}`;
  return `${BFF_BASE_URL}/${s}`;
}

const ADMIN_DATETIME_PRESETS = [
  { label: "Agora", minutes: 0 },
  { label: "Em 2 minutos", minutes: 2 },
  { label: "Em 5 minutos", minutes: 5 },
  { label: "Em 10 minutos", minutes: 10 }
];

function AdminDatetimeField({ label, value, onChange, required }) {
  return (
    <label className="admin-datetime-field">
      <span className="admin-datetime-field__label">{label}</span>
      <input
        type="datetime-local"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required={required}
      />
      <div className="admin-datetime-presets" role="group" aria-label={`Atalhos: ${label}`}>
        {ADMIN_DATETIME_PRESETS.map((p) => (
          <button
            key={p.minutes}
            type="button"
            className="admin-datetime-preset"
            onClick={() => onChange(presetDatetimeLocalFromNow(p.minutes))}
          >
            {p.label}
          </button>
        ))}
      </div>
    </label>
  );
}

function CampaignPreviewCard({ draft }) {
  if (!draft?.title) {
    return (
      <p className="muted admin-preview__placeholder">
        Preencha o título para começar a pré-visualização.
      </p>
    );
  }

  const c =
    draft.datesComplete && draft.subscriptionsStartAt && draft.subscriptionsEndAt && draft.distributionAt
      ? {
          title: draft.title,
          pointsCost: draft.pointsCost,
          subscriptionsStartAt: draft.subscriptionsStartAt,
          subscriptionsEndAt: draft.subscriptionsEndAt,
          imageUrl: draft.imageUrl || "",
          distributionAt: draft.distributionAt,
          status: draft.status || "ACTIVE"
        }
      : null;

  const state = c ? getCampaignState(c) : null;
  const resgateRough =
    state === "aberta" && c ? distributionRedemptionInRoughLabel(c.distributionAt) : null;
  const cost = Math.max(0, Math.floor(Number(draft.pointsCost) || 0));

  return (
    <article className="campaign-card admin-preview-card" aria-hidden={true}>
      {c && state && (
        <div className="campaign-card__media">
          {c.imageUrl ? (
            <img className="campaign-card__media-img" src={resolveMediaUrl(c.imageUrl)} alt={c.title} />
          ) : (
            <div className="campaign-card__media-placeholder" aria-hidden={true} />
          )}
          <div className="campaign-card__top-row campaign-card__top-row--overlay">
            <div className="campaign-card__top-left">
              {(state === "fechada" ||
                state === "abre_em_breve" ||
                (state === "aberta" && resgateRough)) && (
                <p
                  className={`badge badge--campaign-state ${
                    state === "aberta" && resgateRough ? "aberta" : state
                  }`}
                  aria-live={
                    state === "abre_em_breve" || (state === "aberta" && resgateRough)
                      ? "polite"
                      : undefined
                  }
                >
                  {state === "aberta" && resgateRough
                    ? resgateRough
                    : campaignStateBadgeLabel(state, c)}
                </p>
              )}
            </div>
            <span className="campaign-card__coins">{campaignPointsCornerLabel(cost)}</span>
          </div>
        </div>
      )}

      <h3>{draft.title}</h3>
      <p className="muted campaign-card__description">{campaignCardDescriptionText(draft.description)}</p>

      {!draft.datesComplete && (
        <p className="muted admin-preview__hint">
          Defina início e fim de inscrições e a data de distribuição para ver o estado e o calendário.
        </p>
      )}

      <div className="campaign-card__actions">
        <button type="button" className="primary" disabled tabIndex={-1}>
          Inscrever
        </button>
        <button type="button" className="secondary" disabled tabIndex={-1}>
          {state === "fechada" ? "Ver vencedores" : "Ver detalhes"}
        </button>
      </div>
    </article>
  );
}

/** Operação ativa no painel inferior (escolhida a partir dos cards). */
const PANEL = {
  COMPANY_CREATE: "company_create",
  CAMP_CREATE: "camp_create",
  CAMP_EDIT: "camp_edit",
  CAMP_ATTACH: "camp_attach",
  COUPON_CREATE: "coupon_create",
  COUPON_EDIT: "coupon_edit",
  COUPON_LIST: "coupon_list",
  POINTS_CREDIT: "points_credit"
};

export default function AdminPage() {
  const { notifyError, notifySuccess } = useNotification();
  const [panel, setPanel] = useState(null);
  const [companies, setCompanies] = useState([]);
  const [campaigns, setCampaigns] = useState([]);
  const [coupons, setCoupons] = useState([]);

  const [newCompany, setNewCompany] = useState({ name: "", cnpj: "", logoUrl: "" });
  const [uploadingCompanyLogo, setUploadingCompanyLogo] = useState(false);

  const [newCamp, setNewCamp] = useState({
    title: "",
    description: "",
    pointsCost: "0",
    companyId: "",
    imageUrl: "",
    subStart: "",
    subEnd: "",
    distribution: "",
    visibleUntil: ""
  });

  const [editId, setEditId] = useState("");
  const [editCamp, setEditCamp] = useState({
    title: "",
    description: "",
    pointsCost: "0",
    companyId: "",
    imageUrl: "",
    subStart: "",
    subEnd: "",
    distribution: "",
    visibleUntil: "",
    status: "ACTIVE"
  });

  const [attach, setAttach] = useState({
    campaignId: "",
    code: "",
    title: "",
    expires: "",
    priority: ""
  });
  const [attachCouponSuggest, setAttachCouponSuggest] = useState([]);
  const [attachCouponSuggestLoading, setAttachCouponSuggestLoading] = useState(false);
  const [attachCodeFocused, setAttachCodeFocused] = useState(false);
  /** Cupom escolhido na pesquisa (inventário); obrigatório para associar à campanha. */
  const [attachResolvedCoupon, setAttachResolvedCoupon] = useState(null);
  const attachSuggestBlurTimer = useRef(null);

  const [newCoupon, setNewCoupon] = useState({ code: "", title: "", expires: "" });
  const [editCouponId, setEditCouponId] = useState("");
  const [editCoupon, setEditCoupon] = useState({ title: "", expires: "" });

  const [pointsForm, setPointsForm] = useState({ userId: "", amount: "", reason: "" });
  const [pointsUserQuery, setPointsUserQuery] = useState("");
  const [pointsUserSuggest, setPointsUserSuggest] = useState([]);
  const [pointsUserSuggestLoading, setPointsUserSuggestLoading] = useState(false);
  const [pointsUserFocused, setPointsUserFocused] = useState(false);
  const [pointsResolvedUser, setPointsResolvedUser] = useState(null);
  const pointsUserSuggestBlurTimer = useRef(null);
  const [uploadingCampaignImage, setUploadingCampaignImage] = useState(false);

  const previewDraft = useMemo(() => {
    if (!newCamp.title.trim()) return null;
    const subStart = fromDatetimeLocal(newCamp.subStart);
    const subEnd = fromDatetimeLocal(newCamp.subEnd);
    const distribution = fromDatetimeLocal(newCamp.distribution);
    const datesComplete = Boolean(subStart && subEnd && distribution);
    return {
      title: newCamp.title.trim(),
      description: newCamp.description,
      pointsCost: Math.max(0, parseInt(newCamp.pointsCost, 10) || 0),
      imageUrl: newCamp.imageUrl,
      subscriptionsStartAt: subStart,
      subscriptionsEndAt: subEnd,
      distributionAt: distribution,
      status: "ACTIVE",
      datesComplete
    };
  }, [newCamp]);

  const editPreviewDraft = useMemo(() => {
    if (!editId || !editCamp.title.trim()) return null;
    const subStart = fromDatetimeLocal(editCamp.subStart);
    const subEnd = fromDatetimeLocal(editCamp.subEnd);
    const distribution = fromDatetimeLocal(editCamp.distribution);
    const datesComplete = Boolean(subStart && subEnd && distribution);
    return {
      title: editCamp.title.trim(),
      description: editCamp.description,
      pointsCost: Math.max(0, parseInt(editCamp.pointsCost, 10) || 0),
      imageUrl: editCamp.imageUrl,
      subscriptionsStartAt: subStart,
      subscriptionsEndAt: subEnd,
      distributionAt: distribution,
      status: editCamp.status || "ACTIVE",
      datesComplete
    };
  }, [editId, editCamp]);

  const canSubmitCreateCamp = useMemo(() => {
    if (!newCamp.title.trim()) return false;
    if (!newCamp.description.trim()) return false;
    if (!newCamp.subStart?.trim() || !newCamp.subEnd?.trim() || !newCamp.distribution?.trim()) return false;
    return Boolean(
      fromDatetimeLocal(newCamp.subStart) &&
        fromDatetimeLocal(newCamp.subEnd) &&
        fromDatetimeLocal(newCamp.distribution)
    );
  }, [newCamp]);

  const canSubmitEditCamp = useMemo(() => {
    if (!editId) return false;
    if (!editCamp.title.trim()) return false;
    if (!editCamp.description.trim()) return false;
    if (!editCamp.subStart?.trim() || !editCamp.subEnd?.trim() || !editCamp.distribution?.trim()) return false;
    return Boolean(
      fromDatetimeLocal(editCamp.subStart) &&
        fromDatetimeLocal(editCamp.subEnd) &&
        fromDatetimeLocal(editCamp.distribution)
    );
  }, [editId, editCamp]);

  const canSubmitAttachCamp = useMemo(() => {
    if (!attach.campaignId) return false;
    if (!attachResolvedCoupon || attachResolvedCoupon.code !== attach.code.trim()) return false;
    if (!attach.expires?.trim()) return false;
    return Boolean(fromDatetimeLocal(attach.expires));
  }, [attach, attachResolvedCoupon]);

  const canSubmitCreateCoupon = useMemo(
    () => Boolean(newCoupon.code.trim() && newCoupon.expires?.trim() && fromDatetimeLocal(newCoupon.expires)),
    [newCoupon]
  );

  const canSubmitCreateCompany = useMemo(() => {
    const cnpjDigits = cnpjDigitsOnly(newCompany.cnpj);
    return Boolean(newCompany.name.trim() && cnpjDigits.length === 14);
  }, [newCompany]);

  const canSubmitEditCoupon = useMemo(
    () => Boolean(editCouponId && editCoupon.expires?.trim() && fromDatetimeLocal(editCoupon.expires)),
    [editCouponId, editCoupon]
  );

  const canSubmitCreditPoints = useMemo(() => {
    const amount = parseInt(pointsForm.amount, 10);
    return Boolean(
      pointsForm.userId.trim() &&
        Number.isFinite(amount) &&
        amount >= 1 &&
        pointsForm.reason.trim()
    );
  }, [pointsForm]);

  const refreshCampaigns = useCallback(async () => {
    const list = await listCampaigns();
    setCampaigns(sortCampaigns(list));
  }, []);

  const refreshCompanies = useCallback(async () => {
    const list = await listCompaniesAdmin();
    setCompanies(Array.isArray(list) ? list : []);
  }, []);

  const refreshCoupons = useCallback(async () => {
    setCoupons(await listCouponsAdmin());
  }, []);

  useEffect(() => {
    (async () => {
      try {
        await Promise.all([refreshCompanies(), refreshCampaigns(), refreshCoupons()]);
      } catch {
        /* erros: toast via api.js */
      }
    })();
  }, [refreshCampaigns, refreshCoupons, refreshCompanies]);

  useEffect(() => {
    if (panel !== PANEL.CAMP_ATTACH) {
      setAttachCouponSuggest([]);
      setAttachCouponSuggestLoading(false);
      setAttachCodeFocused(false);
      setAttachResolvedCoupon(null);
      if (attachSuggestBlurTimer.current) {
        clearTimeout(attachSuggestBlurTimer.current);
        attachSuggestBlurTimer.current = null;
      }
    }
  }, [panel]);

  useEffect(() => {
    if (panel !== PANEL.POINTS_CREDIT) {
      setPointsUserSuggest([]);
      setPointsUserSuggestLoading(false);
      setPointsUserFocused(false);
      setPointsResolvedUser(null);
      setPointsUserQuery("");
      setPointsForm({ userId: "", amount: "", reason: "" });
      if (pointsUserSuggestBlurTimer.current) {
        clearTimeout(pointsUserSuggestBlurTimer.current);
        pointsUserSuggestBlurTimer.current = null;
      }
    }
  }, [panel]);

  useEffect(() => {
    if (panel !== PANEL.POINTS_CREDIT) return;
    const q = pointsUserQuery.trim();
    if (q.length < 2) {
      setPointsUserSuggest([]);
      setPointsUserSuggestLoading(false);
      return;
    }
    const ac = new AbortController();
    const tid = setTimeout(async () => {
      setPointsUserSuggestLoading(true);
      try {
        const list = await searchUsersAdmin(q, { signal: ac.signal });
        setPointsUserSuggest(Array.isArray(list) ? list : []);
      } catch (e) {
        if (e?.name === "AbortError") return;
        if (!ac.signal.aborted) setPointsUserSuggest([]);
      } finally {
        if (!ac.signal.aborted) setPointsUserSuggestLoading(false);
      }
    }, 300);
    return () => {
      clearTimeout(tid);
      ac.abort();
    };
  }, [pointsUserQuery, panel]);

  useEffect(() => {
    if (panel !== PANEL.CAMP_ATTACH) return;
    const q = attach.code.trim();
    if (q.length < 1) {
      setAttachCouponSuggest([]);
      setAttachCouponSuggestLoading(false);
      return;
    }
    const ac = new AbortController();
    const tid = setTimeout(async () => {
      setAttachCouponSuggestLoading(true);
      try {
        const list = await searchCoupons(q, { signal: ac.signal, status: "IN_INVENTORY" });
        setAttachCouponSuggest(Array.isArray(list) ? list : []);
      } catch (e) {
        if (e?.name === "AbortError") return;
        if (!ac.signal.aborted) setAttachCouponSuggest([]);
      } finally {
        if (!ac.signal.aborted) setAttachCouponSuggestLoading(false);
      }
    }, 300);
    return () => {
      clearTimeout(tid);
      ac.abort();
    };
  }, [attach.code, panel]);

  function onAttachCodeFocus() {
    if (attachSuggestBlurTimer.current) {
      clearTimeout(attachSuggestBlurTimer.current);
      attachSuggestBlurTimer.current = null;
    }
    setAttachCodeFocused(true);
  }

  function onAttachCodeBlur() {
    attachSuggestBlurTimer.current = setTimeout(() => {
      setAttachCodeFocused(false);
      attachSuggestBlurTimer.current = null;
    }, 180);
  }

  function pickAttachCoupon(c) {
    if (attachSuggestBlurTimer.current) {
      clearTimeout(attachSuggestBlurTimer.current);
      attachSuggestBlurTimer.current = null;
    }
    setAttachResolvedCoupon({ id: c.id, code: c.code });
    setAttach((s) => ({
      ...s,
      code: c.code,
      title: c.title || "",
      expires: toDatetimeLocalValue(c.expiresAt)
    }));
    setAttachCouponSuggest([]);
    setAttachCodeFocused(false);
  }

  function onPointsUserFocus() {
    if (pointsUserSuggestBlurTimer.current) {
      clearTimeout(pointsUserSuggestBlurTimer.current);
      pointsUserSuggestBlurTimer.current = null;
    }
    setPointsUserFocused(true);
  }

  function onPointsUserBlur() {
    pointsUserSuggestBlurTimer.current = setTimeout(() => {
      setPointsUserFocused(false);
    }, 150);
  }

  function pickPointsUser(u) {
    if (pointsUserSuggestBlurTimer.current) {
      clearTimeout(pointsUserSuggestBlurTimer.current);
      pointsUserSuggestBlurTimer.current = null;
    }
    setPointsResolvedUser({ userId: u.userId, name: u.name, email: u.email });
    setPointsForm((s) => ({ ...s, userId: u.userId }));
    setPointsUserQuery(u.name);
    setPointsUserSuggest([]);
    setPointsUserFocused(false);
  }

  async function onUploadCompanyLogo(file) {
    if (!file) return;
    try {
      setUploadingCompanyLogo(true);
      const out = await uploadAdminImage(file);
      if (out?.path) setNewCompany((s) => ({ ...s, logoUrl: out.path }));
    } finally {
      setUploadingCompanyLogo(false);
    }
  }

  async function onUploadCampaignImage(file, mode) {
    if (!file) return;
    try {
      setUploadingCampaignImage(true);
      const out = await uploadAdminImage(file);
      if (!out?.path) return;
      if (mode === "new") setNewCamp((s) => ({ ...s, imageUrl: out.path }));
      else setEditCamp((s) => ({ ...s, imageUrl: out.path }));
    } finally {
      setUploadingCampaignImage(false);
    }
  }

  async function onCreateCompany(e) {
    e.preventDefault();
    try {
      const cnpjDigits = cnpjDigitsOnly(newCompany.cnpj);
      if (!newCompany.name.trim() || cnpjDigits.length !== 14) {
        notifyError("Nome e CNPJ com 14 dígitos são obrigatórios.");
        return;
      }
      const body = {
        name: newCompany.name.trim(),
        cnpj: cnpjDigits,
        logoUrl: newCompany.logoUrl.trim() || undefined
      };
      const created = await createCompany(body);
      notifySuccess(`Empresa criada: ${created.name}`);
      setNewCompany({ name: "", cnpj: "", logoUrl: "" });
      await refreshCompanies();
    } catch {
      /* toast em api.js */
    }
  }

  async function onCreateCampaign(e) {
    e.preventDefault();
    try {
      const body = {
        title: newCamp.title.trim(),
        description: newCamp.description.trim(),
        pointsCost: Math.max(0, parseInt(newCamp.pointsCost, 10) || 0),
        subscriptionsStartAt: fromDatetimeLocal(newCamp.subStart),
        subscriptionsEndAt: fromDatetimeLocal(newCamp.subEnd),
        distributionAt: fromDatetimeLocal(newCamp.distribution)
      };
      if (newCamp.companyId) body.companyId = newCamp.companyId;
      if (newCamp.imageUrl?.trim()) body.imageUrl = newCamp.imageUrl.trim();
      if (!body.subscriptionsStartAt || !body.subscriptionsEndAt || !body.distributionAt) {
        notifyError("Preencha todas as datas da campanha.");
        return;
      }
      if (newCamp.visibleUntil?.trim()) {
        const vu = fromDatetimeLocal(newCamp.visibleUntil);
        if (vu) body.visibleUntil = vu;
      }
      const created = await createCampaign(body);
      notifySuccess(`Campanha criada: ${created.id}`);
      setNewCamp({
        title: "",
        description: "",
        pointsCost: "0",
        companyId: "",
        imageUrl: "",
        subStart: "",
        subEnd: "",
        distribution: "",
        visibleUntil: ""
      });
      await refreshCampaigns();
    } catch {
      /* API: toast em api.js */
    }
  }

  async function loadEditCampaign(id) {
    setEditId(id);
    if (!id) return;
    try {
      const c = await getCampaign(id);
      setEditCamp({
        title: c.title || "",
        description: c.description != null ? String(c.description) : "",
        pointsCost: String(c.pointsCost ?? 0),
        companyId: c.companyId || "",
        imageUrl: c.imageUrl || "",
        subStart: toDatetimeLocalValue(c.subscriptionsStartAt),
        subEnd: toDatetimeLocalValue(c.subscriptionsEndAt),
        distribution: toDatetimeLocalValue(c.distributionAt),
        visibleUntil: c.visibleUntil ? toDatetimeLocalValue(c.visibleUntil) : "",
        status: c.status || "ACTIVE"
      });
    } catch {
      /* toast em api.js */
    }
  }

  async function onPatchCampaign(e) {
    e.preventDefault();
    if (!editId) return;
    try {
      const body = {
        title: editCamp.title.trim(),
        description: editCamp.description.trim(),
        pointsCost: Math.max(0, parseInt(editCamp.pointsCost, 10) || 0),
        subscriptionsStartAt: fromDatetimeLocal(editCamp.subStart),
        subscriptionsEndAt: fromDatetimeLocal(editCamp.subEnd),
        distributionAt: fromDatetimeLocal(editCamp.distribution),
        status: editCamp.status
      };
      if (editCamp.companyId) body.companyId = editCamp.companyId;
      else body.clearCompany = true;
      if (editCamp.imageUrl?.trim()) body.imageUrl = editCamp.imageUrl.trim();
      else body.clearImageUrl = true;
      if (editCamp.visibleUntil?.trim()) {
        const vu = fromDatetimeLocal(editCamp.visibleUntil);
        if (vu) body.visibleUntil = vu;
      }
      await patchCampaign(editId, body);
      notifySuccess("Campanha atualizada.");
      await refreshCampaigns();
    } catch {
      /* toast em api.js */
    }
  }

  async function onClearEditVisibleUntil() {
    if (!editId) return;
    try {
      await patchCampaign(editId, { clearVisibleUntil: true });
      notifySuccess("Data limite de visibilidade removida.");
      setEditCamp((s) => ({ ...s, visibleUntil: "" }));
      await refreshCampaigns();
    } catch {
      /* toast em api.js */
    }
  }

  async function onAttachCoupon(e) {
    e.preventDefault();
    try {
      const expiresAt = fromDatetimeLocal(attach.expires);
      if (
        !attach.campaignId ||
        !attachResolvedCoupon ||
        attachResolvedCoupon.code !== attach.code.trim() ||
        !expiresAt
      ) {
        notifyError("Escolha uma campanha, selecione um cupom da lista e preencha a validade.");
        return;
      }
      const body = {
        code: attach.code.trim(),
        expiresAt,
        priority: attach.priority === "" ? undefined : parseInt(attach.priority, 10)
      };
      if (attach.title.trim()) body.title = attach.title.trim();
      await addCouponToCampaign(attach.campaignId, body);
      notifySuccess("Cupom associado a campanha.");
      setAttachResolvedCoupon(null);
      setAttach({ campaignId: attach.campaignId, code: "", title: "", expires: "", priority: "" });
      await refreshCampaigns();
    } catch {
      /* toast em api.js */
    }
  }

  async function onCreateCoupon(e) {
    e.preventDefault();
    try {
      const expiresAt = fromDatetimeLocal(newCoupon.expires);
      if (!newCoupon.code.trim() || !expiresAt) {
        notifyError("Código e validade são obrigatórios.");
        return;
      }
      await createCoupon({
        code: newCoupon.code.trim(),
        expiresAt,
        title: newCoupon.title.trim() || undefined
      });
      notifySuccess("Cupom criado.");
      setNewCoupon({ code: "", title: "", expires: "" });
      await refreshCoupons();
    } catch {
      /* toast em api.js */
    }
  }

  async function loadEditCoupon(id) {
    setEditCouponId(id);
    if (!id) return;
    try {
      const c = await getCoupon(id);
      setEditCoupon({
        title: c.title || "",
        expires: toDatetimeLocalValue(c.expiresAt)
      });
    } catch {
      /* toast em api.js */
    }
  }

  async function onPatchCoupon(e) {
    e.preventDefault();
    if (!editCouponId) return;
    try {
      const body = {};
      if (editCoupon.title !== undefined) body.title = editCoupon.title;
      if (editCoupon.expires) body.expiresAt = fromDatetimeLocal(editCoupon.expires);
      await patchCoupon(editCouponId, body);
      notifySuccess("Cupom atualizado.");
      await refreshCoupons();
    } catch {
      /* toast em api.js */
    }
  }

  async function onCreditPoints(e) {
    e.preventDefault();
    try {
      const amount = parseInt(pointsForm.amount, 10);
      if (!pointsForm.userId.trim() || !amount || !pointsForm.reason.trim()) {
        notifyError("Utilizador (pesquisa ou UUID), quantidade e motivo são obrigatórios.");
        return;
      }
      const idempotencyKey =
        typeof crypto !== "undefined" && crypto.randomUUID
          ? crypto.randomUUID()
          : `admin-${Date.now()}`;
      await creditUserLedger({
        userId: pointsForm.userId.trim(),
        amount,
        reason: pointsForm.reason.trim(),
        refType: "ADMIN_ADJUSTMENT",
        idempotencyKey
      });
      notifySuccess("Pontos creditados.");
      setPointsForm({ userId: "", amount: "", reason: "" });
      setPointsUserQuery("");
      setPointsResolvedUser(null);
    } catch {
      /* toast em api.js */
    }
  }

  function openPanel(key) {
    setPanel(key);
  }

  return (
    <main className="page admin-page">
      <header className="admin-page__header">
        <Link to="/" className="admin-page__back tiny">
          Voltar
        </Link>
        <h1>Painel admin</h1>
        <p className="muted tiny">Escolha a área e depois a operação.</p>
      </header>

      <div className="admin-hub" aria-label="Áreas do painel">
        <article className="card admin-hub__card">
          <h2 className="admin-hub__title">Campanhas</h2>
          <div className="admin-hub__actions">
            <button
              type="button"
              className={`ghost admin-hub__btn${panel === PANEL.COMPANY_CREATE ? " admin-hub__btn--active" : ""}`}
              onClick={() => openPanel(PANEL.COMPANY_CREATE)}
            >
              Cadastrar empresa
            </button>
            <button
              type="button"
              className={`ghost admin-hub__btn${panel === PANEL.CAMP_CREATE ? " admin-hub__btn--active" : ""}`}
              onClick={() => openPanel(PANEL.CAMP_CREATE)}
            >
              Criar campanha
            </button>
            <button
              type="button"
              className={`ghost admin-hub__btn${panel === PANEL.CAMP_EDIT ? " admin-hub__btn--active" : ""}`}
              onClick={() => openPanel(PANEL.CAMP_EDIT)}
            >
              Editar campanha
            </button>
            <button
              type="button"
              className={`ghost admin-hub__btn${panel === PANEL.CAMP_ATTACH ? " admin-hub__btn--active" : ""}`}
              onClick={() => openPanel(PANEL.CAMP_ATTACH)}
            >
              Associar cupom
            </button>
          </div>
        </article>

        <article className="card admin-hub__card">
          <h2 className="admin-hub__title">Cupons</h2>
          <div className="admin-hub__actions">
            <button
              type="button"
              className={`ghost admin-hub__btn${panel === PANEL.COUPON_CREATE ? " admin-hub__btn--active" : ""}`}
              onClick={() => openPanel(PANEL.COUPON_CREATE)}
            >
              Criar cupom
            </button>
            <button
              type="button"
              className={`ghost admin-hub__btn${panel === PANEL.COUPON_EDIT ? " admin-hub__btn--active" : ""}`}
              onClick={() => openPanel(PANEL.COUPON_EDIT)}
            >
              Editar cupom
            </button>
            <button
              type="button"
              className={`ghost admin-hub__btn${panel === PANEL.COUPON_LIST ? " admin-hub__btn--active" : ""}`}
              onClick={() => openPanel(PANEL.COUPON_LIST)}
            >
              Ver inventário
            </button>
          </div>
        </article>

        <article className="card admin-hub__card">
          <h2 className="admin-hub__title">Pontos</h2>
          <div className="admin-hub__actions">
            <button
              type="button"
              className={`ghost admin-hub__btn${panel === PANEL.POINTS_CREDIT ? " admin-hub__btn--active" : ""}`}
              onClick={() => openPanel(PANEL.POINTS_CREDIT)}
            >
              Creditar pontos
            </button>
          </div>
        </article>
      </div>

      <section className="card admin-workspace" aria-live="polite">
        {!panel && <p className="muted admin-workspace__hint">Selecione uma operação em um dos cards acima.</p>}

        {panel === PANEL.COMPANY_CREATE && (
          <>
            <h2 className="admin-workspace__heading">Cadastrar empresa parceira</h2>
            <form className="admin-form" onSubmit={onCreateCompany}>
              <label>
                Nome da empresa
                <input
                  value={newCompany.name}
                  onChange={(e) => setNewCompany((s) => ({ ...s, name: e.target.value }))}
                  required
                />
              </label>
              <label>
                CNPJ (14 dígitos)
                <input
                  value={newCompany.cnpj}
                  onChange={(e) =>
                    setNewCompany((s) => ({ ...s, cnpj: formatCnpjMask(e.target.value) }))
                  }
                  inputMode="numeric"
                  placeholder="00.000.000/0000-00"
                  maxLength={18}
                  required
                />
              </label>
              <label>
                URL do logo (opcional)
                <input
                  value={newCompany.logoUrl}
                  onChange={(e) => setNewCompany((s) => ({ ...s, logoUrl: e.target.value }))}
                />
              </label>
              <label>
                Upload do logo (opcional)
                <input
                  type="file"
                  accept="image/*"
                  onChange={(e) => onUploadCompanyLogo(e.target.files?.[0])}
                />
                {uploadingCompanyLogo && <span className="tiny">A enviar logo...</span>}
              </label>
              <button type="submit" className="primary" disabled={!canSubmitCreateCompany}>
                Cadastrar empresa
              </button>
            </form>
          </>
        )}

        {panel === PANEL.CAMP_CREATE && (
          <>
            <h2 className="admin-workspace__heading">Criar campanha</h2>
            <div className="admin-create-grid">
              <form className="admin-form" onSubmit={onCreateCampaign}>
                <label>
                  Título
                  <input
                    value={newCamp.title}
                    onChange={(e) => setNewCamp((s) => ({ ...s, title: e.target.value }))}
                    required
                  />
                </label>
                <label>
                  Descrição (exibida no app)
                  <textarea
                    value={newCamp.description}
                    onChange={(e) => setNewCamp((s) => ({ ...s, description: e.target.value }))}
                    required
                    rows={4}
                    maxLength={2000}
                  />
                </label>
                <label>
                  Custo (moedas)
                  <input
                    type="number"
                    min="0"
                    value={newCamp.pointsCost}
                    onChange={(e) => setNewCamp((s) => ({ ...s, pointsCost: e.target.value }))}
                  />
                </label>
                <label>
                  Empresa parceira (opcional)
                  <select
                    value={newCamp.companyId}
                    onChange={(e) => setNewCamp((s) => ({ ...s, companyId: e.target.value }))}
                  >
                    <option value="">Sem empresa</option>
                    {companies.map((co) => (
                      <option key={co.id} value={co.id}>
                        {co.name}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Imagem do card (opcional)
                  <input
                    value={newCamp.imageUrl}
                    onChange={(e) => setNewCamp((s) => ({ ...s, imageUrl: e.target.value }))}
                    placeholder="/api/uploads/images/..."
                  />
                </label>
                <label>
                  Upload da imagem do card (opcional)
                  <input
                    type="file"
                    accept="image/*"
                    onChange={(e) => onUploadCampaignImage(e.target.files?.[0], "new")}
                  />
                  {uploadingCampaignImage && <span className="tiny">A enviar imagem...</span>}
                </label>
                <AdminDatetimeField
                  label="Início das inscrições"
                  value={newCamp.subStart}
                  onChange={(v) => setNewCamp((s) => ({ ...s, subStart: v }))}
                  required
                />
                <AdminDatetimeField
                  label="Fim das inscrições"
                  value={newCamp.subEnd}
                  onChange={(v) => setNewCamp((s) => ({ ...s, subEnd: v }))}
                  required
                />
                <AdminDatetimeField
                  label="Distribuição"
                  value={newCamp.distribution}
                  onChange={(v) => setNewCamp((s) => ({ ...s, distribution: v }))}
                  required
                />
                <AdminDatetimeField
                  label="Visível na lista até (opcional)"
                  value={newCamp.visibleUntil}
                  onChange={(v) => setNewCamp((s) => ({ ...s, visibleUntil: v }))}
                />
                <p className="muted tiny">
                  Após esta data/hora a campanha deixa de aparecer na lista inicial do app (detalhe por URL
                  continua acessível).
                </p>
                <button type="submit" className="primary" disabled={!canSubmitCreateCamp}>
                  Criar campanha
                </button>
              </form>
              <div className="admin-preview">
                <h3 className="admin-preview__title">Pré-visualização no app</h3>
                <CampaignPreviewCard draft={previewDraft} />
              </div>
            </div>
          </>
        )}

        {panel === PANEL.CAMP_EDIT && (
          <>
            <h2 className="admin-workspace__heading">Editar campanha</h2>
            <label className="admin-inline-label">
              Campanha
              <select value={editId} onChange={(e) => loadEditCampaign(e.target.value)}>
                <option value="">Selecione...</option>
                {campaigns.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.title}
                  </option>
                ))}
              </select>
            </label>
            {editId ? (
              <div className="admin-create-grid">
                <form className="admin-form" onSubmit={onPatchCampaign}>
                  <label>
                    Título
                    <input
                      value={editCamp.title}
                      onChange={(e) => setEditCamp((s) => ({ ...s, title: e.target.value }))}
                      required
                    />
                  </label>
                  <label>
                    Descrição (exibida no app)
                    <textarea
                      value={editCamp.description}
                      onChange={(e) => setEditCamp((s) => ({ ...s, description: e.target.value }))}
                      required
                      rows={4}
                      maxLength={2000}
                    />
                  </label>
                  <label>
                    Custo (moedas)
                    <input
                      type="number"
                      min="0"
                      value={editCamp.pointsCost}
                      onChange={(e) => setEditCamp((s) => ({ ...s, pointsCost: e.target.value }))}
                    />
                  </label>
                  <label>
                    Empresa parceira (opcional)
                    <select
                      value={editCamp.companyId}
                      onChange={(e) => setEditCamp((s) => ({ ...s, companyId: e.target.value }))}
                    >
                      <option value="">Sem empresa</option>
                      {companies.map((co) => (
                        <option key={co.id} value={co.id}>
                          {co.name}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label>
                    Imagem do card (opcional)
                    <input
                      value={editCamp.imageUrl}
                      onChange={(e) => setEditCamp((s) => ({ ...s, imageUrl: e.target.value }))}
                      placeholder="/api/uploads/images/..."
                    />
                  </label>
                  <label>
                    Upload da imagem do card (opcional)
                    <input
                      type="file"
                      accept="image/*"
                      onChange={(e) => onUploadCampaignImage(e.target.files?.[0], "edit")}
                    />
                    {uploadingCampaignImage && <span className="tiny">A enviar imagem...</span>}
                  </label>
                  <AdminDatetimeField
                    label="Início das inscrições"
                    value={editCamp.subStart}
                    onChange={(v) => setEditCamp((s) => ({ ...s, subStart: v }))}
                    required
                  />
                  <AdminDatetimeField
                    label="Fim das inscrições"
                    value={editCamp.subEnd}
                    onChange={(v) => setEditCamp((s) => ({ ...s, subEnd: v }))}
                    required
                  />
                  <AdminDatetimeField
                    label="Distribuição"
                    value={editCamp.distribution}
                    onChange={(v) => setEditCamp((s) => ({ ...s, distribution: v }))}
                    required
                  />
                  <AdminDatetimeField
                    label="Visível na lista até (opcional)"
                    value={editCamp.visibleUntil}
                    onChange={(v) => setEditCamp((s) => ({ ...s, visibleUntil: v }))}
                  />
                  {editCamp.visibleUntil?.trim() ? (
                    <button
                      type="button"
                      className="secondary tiny admin-clear-visible-until"
                      onClick={onClearEditVisibleUntil}
                    >
                      Remover data limite de visibilidade
                    </button>
                  ) : null}
                  <p className="muted tiny">
                    Após esta data/hora a campanha deixa de aparecer na lista inicial do app.
                  </p>
                  <label>
                    Status
                    <select
                      value={editCamp.status}
                      onChange={(e) => setEditCamp((s) => ({ ...s, status: e.target.value }))}
                    >
                      <option value="ACTIVE">ACTIVE</option>
                      <option value="CLOSED">CLOSED</option>
                    </select>
                  </label>
                  <button type="submit" className="primary" disabled={!canSubmitEditCamp}>
                    Salvar alterações
                  </button>
                </form>
                <div className="admin-preview">
                  <h3 className="admin-preview__title">Pré-visualização no app</h3>
                  <CampaignPreviewCard draft={editPreviewDraft} />
                </div>
              </div>
            ) : (
              <p className="muted">Escolha uma campanha na lista.</p>
            )}
          </>
        )}

        {panel === PANEL.CAMP_ATTACH && (
          <>
            <h2 className="admin-workspace__heading">Associar cupom à campanha</h2>
            <form className="admin-form" onSubmit={onAttachCoupon}>
              <label>
                Campanha
                <select
                  value={attach.campaignId}
                  onChange={(e) => setAttach((s) => ({ ...s, campaignId: e.target.value }))}
                  required
                >
                  <option value="">Selecione...</option>
                  {campaigns.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.title}
                    </option>
                  ))}
                </select>
              </label>
              <label className="admin-attach-coupon-field">
                Código do cupom
                <div className="admin-attach-coupon-wrap">
                  <input
                    autoComplete="off"
                    value={attach.code}
                    onChange={(e) => {
                      const v = e.target.value;
                      setAttach((s) => ({ ...s, code: v }));
                      setAttachResolvedCoupon((prev) => (prev && prev.code === v.trim() ? prev : null));
                    }}
                    onFocus={onAttachCodeFocus}
                    onBlur={onAttachCodeBlur}
                    required
                    aria-autocomplete="list"
                    aria-controls="admin-attach-coupon-suggest-list"
                    aria-expanded={
                      attachCodeFocused && attach.code.trim().length >= 1 ? true : false
                    }
                  />
                  {attachCodeFocused && attach.code.trim().length >= 1 && (
                    <ul
                      id="admin-attach-coupon-suggest-list"
                      className="admin-attach-coupon-suggest"
                      role="listbox"
                    >
                      {attachCouponSuggestLoading && (
                        <li className="admin-attach-coupon-suggest__status muted" role="presentation">
                          A buscar...
                        </li>
                      )}
                      {!attachCouponSuggestLoading && attachCouponSuggest.length === 0 && (
                        <li className="admin-attach-coupon-suggest__status muted" role="presentation">
                          Nenhum cupom encontrado.
                        </li>
                      )}
                      {!attachCouponSuggestLoading &&
                        attachCouponSuggest.map((c) => (
                          <li key={c.id} role="option">
                            <button
                              type="button"
                              className="admin-attach-coupon-suggest__btn"
                              onMouseDown={(ev) => ev.preventDefault()}
                              onClick={() => pickAttachCoupon(c)}
                            >
                              <span className="admin-attach-coupon-suggest__code">{c.code}</span>
                              {c.title ? (
                                <span className="admin-attach-coupon-suggest__title muted">
                                  {c.title}
                                </span>
                              ) : null}
                              <span className="admin-attach-coupon-suggest__meta muted">{c.status}</span>
                            </button>
                          </li>
                        ))}
                    </ul>
                  )}
                </div>
              </label>
              <label>
                Título público (opcional)
                <input
                  value={attach.title}
                  onChange={(e) => setAttach((s) => ({ ...s, title: e.target.value }))}
                />
              </label>
              <AdminDatetimeField
                label="Validade"
                value={attach.expires}
                onChange={(v) => setAttach((s) => ({ ...s, expires: v }))}
                required
              />
              <label>
                Prioridade (opcional)
                <input
                  type="number"
                  value={attach.priority}
                  onChange={(e) => setAttach((s) => ({ ...s, priority: e.target.value }))}
                />
              </label>
              <p className="muted tiny admin-attach-coupon-hint">
                Selecione um cupom na lista de sugestões (inventário). Editar o código após selecionar limpa a
                seleção.
              </p>
              <button type="submit" className="secondary" disabled={!canSubmitAttachCamp}>
                Associar
              </button>
            </form>
          </>
        )}

        {panel === PANEL.COUPON_CREATE && (
          <>
            <h2 className="admin-workspace__heading">Novo cupom (inventário)</h2>
            <form className="admin-form" onSubmit={onCreateCoupon}>
              <label>
                Código
                <input
                  value={newCoupon.code}
                  onChange={(e) => setNewCoupon((s) => ({ ...s, code: e.target.value }))}
                  required
                />
              </label>
              <label>
                Título (opcional)
                <input
                  value={newCoupon.title}
                  onChange={(e) => setNewCoupon((s) => ({ ...s, title: e.target.value }))}
                />
              </label>
              <AdminDatetimeField
                label="Validade"
                value={newCoupon.expires}
                onChange={(v) => setNewCoupon((s) => ({ ...s, expires: v }))}
                required
              />
              <button type="submit" className="primary" disabled={!canSubmitCreateCoupon}>
                Criar cupom
              </button>
            </form>
          </>
        )}

        {panel === PANEL.COUPON_EDIT && (
          <>
            <h2 className="admin-workspace__heading">Editar cupom</h2>
            <label className="admin-inline-label">
              Cupom
              <select value={editCouponId} onChange={(e) => loadEditCoupon(e.target.value)}>
                <option value="">Selecione...</option>
                {coupons.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.code} {c.title ? `— ${c.title}` : ""}
                  </option>
                ))}
              </select>
            </label>
            {editCouponId ? (
              <form className="admin-form" onSubmit={onPatchCoupon}>
                <label>
                  Título
                  <input
                    value={editCoupon.title}
                    onChange={(e) => setEditCoupon((s) => ({ ...s, title: e.target.value }))}
                  />
                </label>
                <AdminDatetimeField
                  label="Validade"
                  value={editCoupon.expires}
                  onChange={(v) => setEditCoupon((s) => ({ ...s, expires: v }))}
                  required
                />
                <button type="submit" className="primary" disabled={!canSubmitEditCoupon}>
                  Salvar cupom
                </button>
              </form>
            ) : (
              <p className="muted">Escolha um cupom na lista.</p>
            )}
          </>
        )}

        {panel === PANEL.COUPON_LIST && (
          <>
            <h2 className="admin-workspace__heading">Cupons no inventário</h2>
            <ul className="admin-coupon-list">
              {coupons.length === 0 && <li className="muted">Nenhum cupom listado.</li>}
              {coupons.map((c) => (
                <li key={c.id}>
                  <strong>{c.code}</strong>
                  <span className="muted"> {c.status}</span>
                  {c.title && <span className="muted"> — {c.title}</span>}
                </li>
              ))}
            </ul>
          </>
        )}

        {panel === PANEL.POINTS_CREDIT && (
          <>
            <h2 className="admin-workspace__heading">Creditar pontos</h2>
            <form className="admin-form" onSubmit={onCreditPoints}>
              <label className="admin-attach-coupon-field">
                Utilizador
                <div className="admin-attach-coupon-wrap">
                  <input
                    autoComplete="off"
                    value={pointsUserQuery}
                    onChange={(e) => {
                      const v = e.target.value;
                      setPointsUserQuery(v);
                      setPointsResolvedUser(null);
                      setPointsForm((s) => ({ ...s, userId: "" }));
                    }}
                    onFocus={onPointsUserFocus}
                    onBlur={onPointsUserBlur}
                    placeholder="Nome ou email (mín. 2 caracteres)"
                    aria-autocomplete="list"
                    aria-controls="admin-points-user-suggest-list"
                    aria-expanded={
                      pointsUserFocused && pointsUserQuery.trim().length >= 2 ? true : false
                    }
                  />
                  {pointsUserFocused && pointsUserQuery.trim().length >= 2 && (
                    <ul
                      id="admin-points-user-suggest-list"
                      className="admin-attach-coupon-suggest"
                      role="listbox"
                    >
                      {pointsUserSuggestLoading && (
                        <li className="admin-attach-coupon-suggest__status muted" role="presentation">
                          A buscar...
                        </li>
                      )}
                      {!pointsUserSuggestLoading && pointsUserSuggest.length === 0 && (
                        <li className="admin-attach-coupon-suggest__status muted" role="presentation">
                          Nenhum utilizador encontrado.
                        </li>
                      )}
                      {!pointsUserSuggestLoading &&
                        pointsUserSuggest.map((u) => (
                          <li key={u.userId} role="option">
                            <button
                              type="button"
                              className="admin-attach-coupon-suggest__btn admin-points-user-row"
                              onMouseDown={(ev) => ev.preventDefault()}
                              onClick={() => pickPointsUser(u)}
                            >
                              <span className="admin-points-user-row__name">{u.name}</span>
                              <span className="admin-points-user-row__email muted">{u.email}</span>
                            </button>
                          </li>
                        ))}
                    </ul>
                  )}
                </div>
              </label>
              {pointsResolvedUser && (
                <p className="muted tiny admin-points-user-resolved">
                  <span className="admin-points-user-resolved__name">{pointsResolvedUser.name}</span>
                  <span className="admin-points-user-resolved__email"> — {pointsResolvedUser.email}</span>
                </p>
              )}
              <label>
                User ID (UUID)
                <input
                  value={pointsForm.userId}
                  onChange={(e) => {
                    setPointsForm((s) => ({ ...s, userId: e.target.value }));
                    setPointsResolvedUser(null);
                  }}
                  placeholder="00000000-0000-0000-0000-000000000000"
                  required
                />
              </label>
              <label>
                Quantidade
                <input
                  type="number"
                  min="1"
                  value={pointsForm.amount}
                  onChange={(e) => setPointsForm((s) => ({ ...s, amount: e.target.value }))}
                  required
                />
              </label>
              <label>
                Motivo
                <input
                  value={pointsForm.reason}
                  onChange={(e) => setPointsForm((s) => ({ ...s, reason: e.target.value }))}
                  required
                />
              </label>
              <button type="submit" className="primary" disabled={!canSubmitCreditPoints}>
                Creditar
              </button>
            </form>
          </>
        )}
      </section>
    </main>
  );
}
