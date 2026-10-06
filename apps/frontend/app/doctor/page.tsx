"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { apiRequest } from "@/lib/api-client";

type Ready = { appointmentId: string; appointmentDate: string; startTime: string; flowStage: string };
type Context = { firstName: string; lastName: string; specialtyName: string; licenseNumber: string; simulatedRne: string; readyAppointments: Ready[] };

export default function DoctorHome() {
  const [context, setContext] = useState<Context | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const load = useCallback(async () => {
    setLoading(true); setError("");
    try { setContext(await apiRequest<Context>("/api/portal/medical/me/context")); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudo cargar la cola médica."); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { void load(); }, [load]);
  return <div className="page-stack"><div className="page-heading"><p className="eyebrow">PORTAL MÉDICO</p><h1>Atenciones de hoy</h1><p>Abre una cita asignada en espera para revisar su contexto clínico.</p></div>
    {error && <p className="alert error" role="alert">{error} <button className="button secondary small" onClick={() => void load()}>Reintentar</button></p>}
    {loading ? <p role="status">Cargando…</p> : context && <><section className="portal-panel"><h2>Profesional</h2><p>{context.firstName} {context.lastName} · {context.specialtyName} · CMP {context.licenseNumber}</p><p>{context.simulatedRne} (dato simulado)</p></section>
      <section className="portal-panel"><h2>Cola propia</h2>{context.readyAppointments.length === 0 ? <p>No hay citas en espera asignadas hoy.</p> : <ul className="portal-list">{context.readyAppointments.map(item => <li className="portal-row" key={item.appointmentId}><div><strong>{item.appointmentDate} · {item.startTime.slice(0, 5)}</strong><p>{item.flowStage === "WAITING" ? "En espera" : item.flowStage === "IN_ATTENTION" ? "En atención" : item.flowStage}</p></div><Link className="button primary small" href={`/doctor/appointments/${item.appointmentId}`}>Abrir contexto</Link></li>)}</ul>}</section></>}
  </div>;
}
