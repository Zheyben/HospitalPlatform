"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { apiRequest } from "@/lib/api-client";
import type { Appointment } from "@/lib/types";
import { StatusBadge } from "@/components/status-badge";

export default function AppointmentsPage() {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try { setAppointments(await apiRequest<Appointment[]>("/api/patient/appointments")); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudieron cargar las citas."); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { void load(); }, [load]);

  return <div className="page-stack">
    <div className="page-heading"><p className="eyebrow">TUS RESERVAS</p><h1>Mis citas</h1><p>Consulta las citas registradas y el estado que devuelve el sistema.</p></div>
    <section className="appointments-section" aria-labelledby="appointments-title"><div className="section-heading"><div><p className="eyebrow">HISTORIAL PERSONAL</p><h2 id="appointments-title">Citas registradas</h2></div><Link className="button primary small" href="/patient/availability">+ Nueva cita</Link></div>
    {loading ? <div className="state-card" role="status"><span className="spinner" /> Cargando citas…</div> : error ? <div className="state-card error-state" role="alert"><div className="state-icon" aria-hidden="true">!</div><h3>No se pudieron cargar tus citas</h3><p>{error}</p><button className="button secondary" onClick={() => void load()}>Reintentar</button></div> : appointments.length === 0 ? <div className="state-card"><div className="state-icon" aria-hidden="true">○</div><h3>Aún no tienes citas</h3><p>Consulta la disponibilidad para reservar tu primer horario.</p><Link className="button primary" href="/patient/availability">Buscar horarios</Link></div> : <div className="appointment-list">{appointments.map(appointment => <article className="appointment-card" key={appointment.id}>
      <div className="appointment-card-top"><div><span className="muted-label">TU RESERVA</span><h3>Cita médica</h3></div><StatusBadge status={appointment.appointmentStatus} /></div>
      <div className="appointment-body"><div><span className="muted-label">Motivo de consulta</span><p className="appointment-reason">{appointment.reason || "No se indicó un motivo."}</p></div><div><span className="muted-label">Registro</span><p>{appointment.createdAt ? appointment.createdAt.replace("T", " ").slice(0, 16) + " · hora del servidor" : "Sin fecha de registro"}</p></div></div>
      <div className="appointment-meta"><div><span>Referencia de cita</span><code>{appointment.id}</code></div><div><span>Profesional ID</span><code>{appointment.professionalId}</code></div><div><span>Slot ID</span><code>{appointment.slotId}</code></div></div>
    </article>)}</div>}</section>
  </div>;
}
