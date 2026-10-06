"use client";

import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import { apiRequest } from "@/lib/api-client";

type Context = { appointmentId: string; appointmentDate: string; startTime: string; flowStage: string; reason: string | null; firstName: string; lastName: string; documentType: string; documentNumber: string; birthDate: string; phone: string; insurance: string; address: string | null; sex: string | null; maritalStatus: string | null; occupation: string | null; district: string | null; educationLevel: string | null; affiliationNumber: string | null; emergencyContactName: string | null; emergencyContactRelationship: string | null; emergencyContactPhone: string | null; clinicalRecordNumber: string };
type Prior = { encounterId: string; appointmentDate: string; doctorName: string; specialtyName: string; primaryDiagnosis: string; icd10Code: string };
type Page = { items: Prior[]; hasMore: boolean };
type Start = { encounterId: string };
type PriorDetail = { encounter: { appointmentDate: string; doctorName: string; specialtyName: string; primaryDiagnosis: string; icd10Code: string; icd10Description: string; symptomsAndCurrentIllness: string | null; therapeuticPlan: string | null; generalIndications: string | null; warningSigns: string | null }; history: Record<string, string | number | null> | null; order: { procedureCode: string; procedureName: string; status: string } | null; prescription: { prescriptionNumber: string; items: { genericName: string; presentation: string; dose: number; doseUnit: string; frequency: string }[] } | null };
const historyLabels: Record<string, string> = { pathological: "Patológicos", surgical: "Quirúrgicos", allergies_and_reactions: "Alergias y reacciones", usual_medication: "Medicación habitual", transfusions: "Transfusiones", relevant_habits: "Hábitos", hospitalizations: "Hospitalizaciones declaradas", other_personal: "Otros personales", father_history: "Padre", mother_history: "Madre", siblings_history: "Hermanos", children_history: "Hijos", grandparents_history: "Abuelos", other_family: "Otros familiares", family_observation: "Observación familiar", other_alerts: "Otras alertas" };

export default function DoctorAppointmentPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const [context, setContext] = useState<Context | null>(null);
  const [history, setHistory] = useState<Prior[]>([]);
  const [priorDetail, setPriorDetail] = useState<PriorDetail | null>(null);
  const [offset, setOffset] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [starting, setStarting] = useState(false);
  const [error, setError] = useState("");
  const loadHistory = useCallback(async (next: number) => {
    try { const page = await apiRequest<Page>(`/api/portal/medical/appointments/${id}/history?limit=20&offset=${next}`); setHistory(page.items); setHasMore(page.hasMore); setOffset(next); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudo cargar el historial."); }
  }, [id]);
  useEffect(() => {
    let live = true;
    Promise.all([apiRequest<Context>(`/api/portal/medical/appointments/${id}/context`), apiRequest<Page>(`/api/portal/medical/appointments/${id}/history?limit=20&offset=0`)])
      .then(([found, page]) => { if (live) { setContext(found); setHistory(page.items); setHasMore(page.hasMore); } })
      .catch(cause => { if (live) setError(cause instanceof Error ? cause.message : "La cita no está disponible."); })
      .finally(() => { if (live) setLoading(false); });
    return () => { live = false; };
  }, [id]);
  async function start() {
    setStarting(true); setError("");
    try { const encounter = await apiRequest<Start>(`/api/portal/medical/appointments/${id}/start`, { method: "POST" }); router.push(`/doctor/encounters/${encounter.encounterId}`); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudo iniciar la atención."); setStarting(false); }
  }
  async function openPrior(encounterId: string) {
    setError(""); setPriorDetail(null);
    try { setPriorDetail(await apiRequest<PriorDetail>(`/api/portal/medical/appointments/${id}/history/${encounterId}`)); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudo abrir la atención previa."); }
  }
  return <div className="page-stack"><Link href="/doctor">← Volver a la cola</Link><div className="page-heading"><p className="eyebrow">CONTEXTO MÉDICO</p><h1>Atención asignada</h1></div>
    {error && <p className="alert error" role="alert">{error}</p>}{loading ? <p role="status">Cargando contexto…</p> : context && <><section className="portal-panel"><h2>{context.firstName} {context.lastName}</h2><p>{context.documentType} {context.documentNumber} · Historia Clínica {context.clinicalRecordNumber}</p><dl className="clinical-detail">{[
      ["Cita", `${context.appointmentDate} · ${context.startTime.slice(0, 5)}`], ["Motivo", context.reason], ["Fecha de nacimiento", context.birthDate], ["Teléfono", context.phone], ["Seguro", context.insurance], ["Dirección", context.address], ["Sexo", context.sex], ["Estado civil", context.maritalStatus], ["Ocupación", context.occupation], ["Distrito", context.district], ["Instrucción", context.educationLevel], ["Afiliación", context.affiliationNumber], ["Contacto de emergencia", context.emergencyContactName], ["Parentesco", context.emergencyContactRelationship], ["Teléfono de emergencia", context.emergencyContactPhone],
    ].map(([label, value]) => <div key={label}><dt>{label}</dt><dd>{value || "No declarado"}</dd></div>)}</dl><button className="button primary" disabled={starting} onClick={() => void start()}>{starting ? "Iniciando…" : context.flowStage === "IN_ATTENTION" ? "Continuar atención" : "Iniciar atención"}</button></section>
    <section className="portal-panel"><h2>Atenciones previas finalizadas</h2>{history.length === 0 ? <p>Sin atenciones previas finalizadas.</p> : <ul className="portal-list">{history.map(item => <li className="portal-row" key={item.encounterId}><div><strong>{item.appointmentDate} · {item.primaryDiagnosis}</strong><p>{item.icd10Code} · {item.doctorName} · {item.specialtyName}</p></div><button className="button secondary small" onClick={() => void openPrior(item.encounterId)}>Ver detalle</button></li>)}</ul>}<div className="portal-actions"><button className="button secondary small" disabled={offset === 0} onClick={() => void loadHistory(Math.max(0, offset - 20))}>Anterior</button><button className="button secondary small" disabled={!hasMore} onClick={() => void loadHistory(offset + 20)}>Siguiente</button></div></section>
    {priorDetail && <section className="portal-panel" aria-label="Atención previa"><h2>{priorDetail.encounter.appointmentDate} · {priorDetail.encounter.specialtyName}</h2><p>{priorDetail.encounter.doctorName}</p><p>Diagnóstico: {priorDetail.encounter.primaryDiagnosis} ({priorDetail.encounter.icd10Code}) · {priorDetail.encounter.icd10Description}</p><dl className="clinical-detail">{[
      ["Enfermedad actual", priorDetail.encounter.symptomsAndCurrentIllness], ["Plan terapéutico", priorDetail.encounter.therapeuticPlan], ["Indicaciones", priorDetail.encounter.generalIndications], ["Signos de alerta", priorDetail.encounter.warningSigns],
    ].map(([label, value]) => <div key={label}><dt>{label}</dt><dd>{value || "No registrado"}</dd></div>)}{Object.entries(historyLabels).map(([key, label]) => <div key={key}><dt>{label}</dt><dd>{priorDetail.history?.[key] || "No registrado"}</dd></div>)}</dl>{priorDetail.order && <p>Orden solicitada: {priorDetail.order.procedureCode} · {priorDetail.order.procedureName} ({priorDetail.order.status}). No representa un resultado.</p>}{priorDetail.prescription && <div><h3>Receta {priorDetail.prescription.prescriptionNumber}</h3><ul>{priorDetail.prescription.items.map((item, index) => <li key={index}>{item.genericName} · {item.presentation} · {item.dose} {item.doseUnit} · {item.frequency}</li>)}</ul></div>}<button className="button secondary" onClick={() => setPriorDetail(null)}>Cerrar detalle</button></section>}</>}
  </div>;
}
