"use client";

import { useCallback, useEffect, useState } from "react";
import { apiRequest } from "@/lib/api-client";

type Summary = { encounterId: string; appointmentDate: string; doctorName: string; specialtyName: string; primaryDiagnosis: string; icd10Code: string };
type Detail = Summary & { symptomsAndCurrentIllness: string | null; reason: string | null; icd10Description: string | null; therapeuticPlan: string | null; generalIndications: string | null; patientEducation: string | null; warningSigns: string | null; suggestedFollowUpDate: string | null; followUpReason: string | null; pendingResultsAndPlan: string | null; complementaryObservations: string | null };
type Page = { items: Summary[]; hasMore: boolean };

export default function RecordsPage() {
  const [items, setItems] = useState<Summary[]>([]);
  const [detail, setDetail] = useState<Detail | null>(null);
  const [offset, setOffset] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const load = useCallback(async (next: number) => {
    setLoading(true); setError(""); setDetail(null);
    try { const page = await apiRequest<Page>(`/api/portal/medical/me/encounters?limit=20&offset=${next}`); setItems(page.items); setHasMore(page.hasMore); setOffset(next); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudieron cargar las atenciones."); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { void load(0); }, [load]);
  async function open(id: string) {
    setError(""); setDetail(null);
    try { setDetail(await apiRequest<Detail>(`/api/portal/medical/me/encounters/${id}`)); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudo abrir la atención."); }
  }
  return <div className="page-stack"><div className="page-heading"><p className="eyebrow">HISTORIA CLÍNICA PROPIA</p><h1>Mis atenciones</h1><p>Solo se muestran atenciones finalizadas de tu cuenta.</p></div>{error && <p className="alert error" role="alert">{error}</p>}
    <section className="portal-panel">{loading ? <p role="status">Cargando atenciones…</p> : items.length === 0 ? <p>No tienes atenciones finalizadas.</p> : <ul className="portal-list">{items.map(item => <li className="portal-row" key={item.encounterId}><div><strong>{item.appointmentDate} · {item.specialtyName}</strong><p>{item.doctorName} · {item.primaryDiagnosis} ({item.icd10Code})</p></div><button className="button secondary small" onClick={() => void open(item.encounterId)}>Ver detalle</button></li>)}</ul>}<div className="portal-actions"><button className="button secondary small" disabled={offset === 0 || loading} onClick={() => void load(Math.max(0, offset - 20))}>Anterior</button><button className="button secondary small" disabled={!hasMore || loading} onClick={() => void load(offset + 20)}>Siguiente</button></div></section>
    {detail && <section className="portal-panel" aria-label="Detalle de atención"><h2>{detail.appointmentDate} · {detail.specialtyName}</h2><p>Profesional: {detail.doctorName}</p><p>Diagnóstico: {detail.primaryDiagnosis} ({detail.icd10Code}) · {detail.icd10Description ?? "Sin descripción"}</p><dl className="clinical-detail">{[
      ["Motivo", detail.reason], ["Síntomas y enfermedad actual", detail.symptomsAndCurrentIllness],
      ["Plan terapéutico", detail.therapeuticPlan], ["Indicaciones generales", detail.generalIndications],
      ["Educación", detail.patientEducation], ["Signos de alerta", detail.warningSigns],
      ["Seguimiento", detail.suggestedFollowUpDate], ["Motivo de seguimiento", detail.followUpReason],
      ["Resultados pendientes y plan", detail.pendingResultsAndPlan], ["Observaciones", detail.complementaryObservations],
    ].map(([label, value]) => <div key={label}><dt>{label}</dt><dd>{value || "No registrado"}</dd></div>)}</dl><button className="button secondary" onClick={() => setDetail(null)}>Cerrar detalle</button></section>}
  </div>;
}
