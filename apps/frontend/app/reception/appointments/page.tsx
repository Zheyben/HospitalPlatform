"use client";

import Link from "next/link";
import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { ApiRequestError, apiRequest } from "@/lib/api-client";
import { StatusBadge } from "@/components/status-badge";

type Patient = { patientId: string; documentType: string; documentNumber: string; patientName: string };
type Slot = { slotId: string; slotDate: string; startTime: string; endTime: string; professionalId: string; professionalName: string; specialtyId: string; specialtyName: string; status: string; usable: boolean };
type Appointment = { appointmentId: string; patientId: string; patientName: string; professionalName: string; specialtyName: string; appointmentDate: string; startTime: string; endTime: string; status: string; flowStage: string | null };
type Waiting = { appointmentId: string; patientDisplay: string; startTime: string; professionalName: string; specialtyName: string; flowStage: string };

function todayInLima() {
  const parts = new Intl.DateTimeFormat("en-US", { timeZone: "America/Lima", year: "numeric", month: "2-digit", day: "2-digit" }).formatToParts(new Date());
  const part = (kind: string) => parts.find(value => value.type === kind)?.value ?? "";
  return `${part("year")}-${part("month")}-${part("day")}`;
}

export default function ReceptionAppointmentsPage() {
  const [documentType, setDocumentType] = useState("DNI");
  const [documentNumber, setDocumentNumber] = useState("");
  const [patient, setPatient] = useState<Patient | null>(null);
  const [searched, setSearched] = useState(false);
  const [slots, setSlots] = useState<Slot[]>([]);
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [waiting, setWaiting] = useState<Waiting[]>([]);
  const [specialtyId, setSpecialtyId] = useState("");
  const [professionalId, setProfessionalId] = useState("");
  const [date, setDate] = useState("");
  const [slotId, setSlotId] = useState("");
  const [reason, setReason] = useState("");
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [offset, setOffset] = useState(0);

  const loadAvailability = useCallback(async () => {
    try {
      const data = await apiRequest<Slot[]>("/api/portal/reception/availability");
      setSlots(data.filter(slot => slot.status === "AVAILABLE" && slot.usable));
    } catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudo cargar la disponibilidad."); }
  }, []);
  const loadWaiting = useCallback(async (page: number) => {
    try { setWaiting(await apiRequest<Waiting[]>(`/api/portal/appointments/reception/waiting-room?limit=20&offset=${page}`)); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudo cargar la sala de espera."); }
  }, []);
  const loadAppointments = useCallback(async (patientId: string) => {
    try { setAppointments(await apiRequest<Appointment[]>(`/api/portal/appointments/reception?patientId=${patientId}&limit=50`)); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudieron cargar las citas del paciente."); }
  }, []);
  useEffect(() => {
    Promise.all([loadAvailability(), loadWaiting(0)]).finally(() => setLoading(false));
  }, [loadAvailability, loadWaiting]);

  async function search(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); setError(""); setNotice(""); setPatient(null); setAppointments([]); setSearched(false);
    try {
      const found = await apiRequest<Patient>(`/api/portal/patients/search?documentType=${encodeURIComponent(documentType)}&documentNumber=${encodeURIComponent(documentNumber.trim())}`);
      setPatient(found); await loadAppointments(found.patientId);
    } catch (cause) {
      if (!(cause instanceof ApiRequestError && cause.status === 404)) setError(cause instanceof Error ? cause.message : "No se pudo buscar el paciente.");
    } finally { setSearched(true); setBusy(false); }
  }

  const specialties = useMemo(() => [...new Map(slots.map(slot => [slot.specialtyId, { id: slot.specialtyId, name: slot.specialtyName }])).values()], [slots]);
  const professionals = useMemo(() => [...new Map(slots.filter(slot => slot.specialtyId === specialtyId).map(slot => [slot.professionalId, { id: slot.professionalId, name: slot.professionalName }])).values()], [slots, specialtyId]);
  const dates = useMemo(() => [...new Set(slots.filter(slot => slot.specialtyId === specialtyId && slot.professionalId === professionalId).map(slot => slot.slotDate))].sort(), [slots, specialtyId, professionalId]);
  const times = slots.filter(slot => slot.specialtyId === specialtyId && slot.professionalId === professionalId && slot.slotDate === date);
  const selectedSlot = times.find(slot => slot.slotId === slotId);

  async function book(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!patient || !selectedSlot || busy) return;
    setBusy(true); setError(""); setNotice("");
    try {
      await apiRequest("/api/portal/appointments", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ patientId: patient.patientId, slotId, reason: reason.trim() }) });
      setNotice("Cita presencial registrada. Revisa el estado antes de confirmar la llegada.");
      setSlotId(""); setReason(""); await Promise.all([loadAppointments(patient.patientId), loadAvailability()]);
    } catch (cause) {
      setError(cause instanceof ApiRequestError && cause.status === 409 ? "El horario ya no está disponible. Selecciona otro." : cause instanceof Error ? cause.message : "No se pudo reservar.");
      if (cause instanceof ApiRequestError && cause.status === 409) await loadAvailability();
    } finally { setBusy(false); }
  }

  async function transition(item: Appointment, action: "confirm" | "check-in" | "waiting") {
    setBusy(true); setError(""); setNotice("");
    try {
      await apiRequest(`/api/portal/appointments/${item.appointmentId}/${action}`, { method: "POST" });
      setNotice(action === "confirm" ? "Cita confirmada." : action === "check-in" ? "Llegada registrada." : "Paciente en sala de espera.");
      if (patient) await loadAppointments(patient.patientId);
      await loadWaiting(offset);
    } catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudo actualizar la cita."); }
    finally { setBusy(false); }
  }

  return <div className="page-stack"><div className="page-heading"><p className="eyebrow">F05 · RECEPCIÓN</p><h1>Gestión de citas</h1><p>Busca al paciente por su documento, registra una cita presencial y controla la llegada.</p></div>
    {error && <p className="alert error" role="alert">{error}</p>}{notice && <p className="alert success" role="status">{notice}</p>}
    <section className="portal-panel"><h2>Buscar paciente</h2><form className="form-grid" onSubmit={search}><label className="field">Tipo de documento<select value={documentType} onChange={e => { setDocumentType(e.target.value); setPatient(null); setSearched(false); }}><option value="DNI">DNI</option><option value="CE">Carné de Extranjería</option><option value="PASSPORT">Pasaporte</option></select></label><label className="field">Número exacto<input required value={documentNumber} onChange={e => { setDocumentNumber(e.target.value.toUpperCase()); setPatient(null); setSearched(false); }} /></label><button className="button primary" disabled={busy} type="submit">{busy ? "Buscando…" : "Buscar"}</button></form>
      {patient ? <p role="status"><strong>{patient.patientName}</strong> · {patient.documentType} {patient.documentNumber}</p> : searched && <p role="status">No se encontró un paciente activo. Debe completar el <Link href="/register">registro de paciente</Link>.</p>}
    </section>
    {patient && <><section className="portal-panel"><h2>Nueva cita presencial</h2>{loading ? <p role="status">Cargando disponibilidad…</p> : slots.length === 0 ? <p>No hay horarios disponibles.</p> : <form className="form-grid" onSubmit={book}>
      <label className="field">Especialidad<select required value={specialtyId} onChange={e => { setSpecialtyId(e.target.value); setProfessionalId(""); setDate(""); setSlotId(""); }}><option value="">Selecciona</option>{specialties.map(item => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label>
      <label className="field">Profesional<select required value={professionalId} disabled={!specialtyId} onChange={e => { setProfessionalId(e.target.value); setDate(""); setSlotId(""); }}><option value="">Selecciona</option>{professionals.map(item => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label>
      <label className="field">Fecha<select required value={date} disabled={!professionalId} onChange={e => { setDate(e.target.value); setSlotId(""); }}><option value="">Selecciona</option>{dates.map(value => <option key={value} value={value}>{value}</option>)}</select></label>
      <label className="field">Hora<select required value={slotId} disabled={!date} onChange={e => setSlotId(e.target.value)}><option value="">Selecciona</option>{times.map(item => <option key={item.slotId} value={item.slotId}>{item.startTime.slice(0, 5)}–{item.endTime.slice(0, 5)}</option>)}</select></label>
      <label className="field full">Motivo de consulta<textarea required maxLength={1000} rows={3} value={reason} onChange={e => setReason(e.target.value)} /></label>
      {selectedSlot && <p className="full">Revisa: <strong>{selectedSlot.specialtyName} · {selectedSlot.professionalName} · {selectedSlot.slotDate} · {selectedSlot.startTime.slice(0, 5)}</strong></p>}
      <button className="button primary" disabled={busy || !selectedSlot || !reason.trim()} type="submit">{busy ? "Registrando…" : "Registrar cita"}</button>
    </form>}</section>
    <section className="portal-panel"><h2>Citas del paciente</h2>{appointments.length === 0 ? <p>Sin citas en esta consulta.</p> : <ul className="portal-list">{appointments.map(item => <li className="portal-row" key={item.appointmentId}><div><strong>{item.specialtyName} · {item.professionalName}</strong><p>{item.appointmentDate} · {item.startTime.slice(0, 5)} · <StatusBadge status={item.status} /> {item.flowStage ?? ""}</p></div><div className="portal-actions">{item.status === "SCHEDULED" && <button className="button secondary small" disabled={busy} onClick={() => void transition(item, "confirm")}>Confirmar</button>}{item.status === "CONFIRMED" && !item.flowStage && item.appointmentDate === todayInLima() && <button className="button secondary small" disabled={busy} onClick={() => void transition(item, "check-in")}>Registrar llegada</button>}{item.status === "CONFIRMED" && item.flowStage === "CHECK_IN" && <button className="button primary small" disabled={busy} onClick={() => void transition(item, "waiting")}>Pasar a espera</button>}</div></li>)}</ul>}</section></>}
    <section className="portal-panel"><h2>Sala de espera de hoy</h2>{waiting.length === 0 ? <p>La sala de espera está vacía.</p> : <ul className="portal-list">{waiting.map(item => <li className="portal-row" key={item.appointmentId}><div><strong>{item.patientDisplay}</strong><p>{item.specialtyName} · {item.professionalName} · {item.startTime.slice(0, 5)} · {item.flowStage}</p></div></li>)}</ul>}<div className="portal-actions"><button className="button secondary small" disabled={offset === 0} onClick={() => { const next = Math.max(0, offset - 20); setOffset(next); void loadWaiting(next); }}>Anterior</button><button className="button secondary small" disabled={waiting.length < 20} onClick={() => { const next = offset + 20; setOffset(next); void loadWaiting(next); }}>Siguiente</button></div></section>
  </div>;
}
