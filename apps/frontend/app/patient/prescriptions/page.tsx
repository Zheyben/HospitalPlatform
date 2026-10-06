"use client";

import { useCallback, useEffect, useState } from "react";
import { apiRequest } from "@/lib/api-client";

type Summary = { prescriptionId: string; prescriptionNumber: string; issuedAt: string; doctorName: string; specialtyName: string; primaryDiagnosis: string };
type Medication = { itemNumber: number; genericName: string; commercialName: string | null; presentation: string; concentration: string | null; dose: number; doseUnit: string; frequency: string; route: string; duration: number; durationUnit: string; quantity: number; usageInstructions: string | null };
type Detail = Summary & { patientName: string; items: Medication[]; additionalPrecautions: string | null; nonPharmacologicalRecommendations: string | null; warningSigns: string | null; additionalCare: string | null; followUpObservations: string | null };
type Page = { items: Summary[]; hasMore: boolean };

export default function PrescriptionsPage() {
  const [items, setItems] = useState<Summary[]>([]);
  const [detail, setDetail] = useState<Detail | null>(null);
  const [offset, setOffset] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const load = useCallback(async (next: number) => {
    setLoading(true); setError(""); setDetail(null);
    try { const page = await apiRequest<Page>(`/api/portal/medical/me/prescriptions?limit=20&offset=${next}`); setItems(page.items); setHasMore(page.hasMore); setOffset(next); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudieron cargar las recetas."); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { void load(0); }, [load]);
  async function open(id: string) {
    setError(""); setDetail(null);
    try { setDetail(await apiRequest<Detail>(`/api/portal/medical/me/prescriptions/${id}`)); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudo abrir la receta."); }
  }
  return <div className="page-stack"><div className="page-heading"><p className="eyebrow">RECETAS PROPIAS</p><h1>Mis recetas</h1><p>Solo se muestran recetas emitidas en atenciones finalizadas.</p></div>{error && <p className="alert error" role="alert">{error}</p>}
    <section className="portal-panel">{loading ? <p role="status">Cargando recetas…</p> : items.length === 0 ? <p>No tienes recetas emitidas.</p> : <ul className="portal-list">{items.map(item => <li className="portal-row" key={item.prescriptionId}><div><strong>Receta {item.prescriptionNumber}</strong><p>{new Date(item.issuedAt).toLocaleDateString("es-PE", { timeZone: "America/Lima" })} · {item.doctorName} · {item.specialtyName}</p></div><button className="button secondary small" onClick={() => void open(item.prescriptionId)}>Ver receta</button></li>)}</ul>}<div className="portal-actions"><button className="button secondary small" disabled={offset === 0 || loading} onClick={() => void load(Math.max(0, offset - 20))}>Anterior</button><button className="button secondary small" disabled={!hasMore || loading} onClick={() => void load(offset + 20)}>Siguiente</button></div></section>
    {detail && detail.items.length > 0 && <section className="portal-panel" aria-label="Detalle de receta"><h2>Receta {detail.prescriptionNumber}</h2><p>{detail.patientName} · {detail.doctorName} · {detail.specialtyName}</p><p>Diagnóstico: {detail.primaryDiagnosis}</p><ol>{detail.items.map(item => <li key={item.itemNumber}><strong>{item.genericName}</strong> {item.presentation} {item.concentration ?? ""}<p>Dosis: {item.dose} {item.doseUnit} · Frecuencia: {item.frequency} · Vía: {item.route} · Duración: {item.duration} {item.durationUnit} · Cantidad: {item.quantity}</p>{item.usageInstructions && <p>{item.usageInstructions}</p>}</li>)}</ol><dl className="clinical-detail">{[
      ["Precauciones", detail.additionalPrecautions], ["Recomendaciones no farmacológicas", detail.nonPharmacologicalRecommendations],
      ["Signos de alerta", detail.warningSigns], ["Cuidados adicionales", detail.additionalCare], ["Seguimiento", detail.followUpObservations],
    ].filter(([, value]) => value).map(([label, value]) => <div key={label}><dt>{label}</dt><dd>{value}</dd></div>)}</dl><button className="button secondary" onClick={() => setDetail(null)}>Cerrar receta</button></section>}
  </div>;
}
