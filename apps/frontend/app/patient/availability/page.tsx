"use client";

import Link from "next/link";
import { FormEvent, useCallback, useEffect, useRef, useState } from "react";
import { apiRequest, ApiRequestError } from "@/lib/api-client";
import type { Appointment, AvailabilitySlot } from "@/lib/types";
import { StatusBadge } from "@/components/status-badge";

function dateLabel(date: string) {
  const label = new Intl.DateTimeFormat("es-CO", { dateStyle: "full", timeZone: "UTC" }).format(new Date(`${date}T12:00:00Z`));
  return label.charAt(0).toUpperCase() + label.slice(1);
}

function timeLabel(time: string) { return time.slice(0, 5); }

export default function AvailabilityPage() {
  const [slots, setSlots] = useState<AvailabilitySlot[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [selected, setSelected] = useState<AvailabilitySlot | null>(null);
  const [reason, setReason] = useState("");
  const [booking, setBooking] = useState(false);
  const [bookingError, setBookingError] = useState("");
  const [created, setCreated] = useState<Appointment | null>(null);
  const [specialtyId, setSpecialtyId] = useState("");
  const [professionalId, setProfessionalId] = useState("");
  const [date, setDate] = useState("");
  const closeButton = useRef<HTMLButtonElement>(null);
  const dialog = useRef<HTMLElement>(null);
  const trigger = useRef<HTMLButtonElement>(null);
  const bookingRef = useRef(false);
  const specialties = [...new Map(slots.map(slot => [slot.specialtyId, { id: slot.specialtyId, name: slot.specialtyName }])).values()];
  const professionals = [...new Map(slots.filter(slot => slot.specialtyId === specialtyId).map(slot => [slot.professionalId, { id: slot.professionalId, name: slot.professionalName }])).values()];
  const dates = [...new Set(slots.filter(slot => slot.specialtyId === specialtyId && slot.professionalId === professionalId).map(slot => slot.slotDate))].sort();
  const visibleSlots = slots.filter(slot => slot.specialtyId === specialtyId && slot.professionalId === professionalId && slot.slotDate === date);
  const groups = Object.entries(visibleSlots.reduce<Record<string, AvailabilitySlot[]>>((result, slot) => {
    (result[slot.slotDate] ??= []).push(slot);
    return result;
  }, {})).sort(([left], [right]) => left.localeCompare(right));

  const loadSlots = useCallback(async () => {
    setLoading(true);
    setLoadError("");
    try {
      const data = await apiRequest<AvailabilitySlot[]>("/api/patient/availability");
      setSlots(data.filter(slot => slot.status === "AVAILABLE" && slot.usable)
        .sort((a, b) => `${a.slotDate}${a.startTime}${a.professionalName}`.localeCompare(`${b.slotDate}${b.startTime}${b.professionalName}`)));
    } catch (cause) {
      setLoadError(cause instanceof Error ? cause.message : "No se pudo cargar la disponibilidad.");
    } finally { setLoading(false); }
  }, []);

  useEffect(() => { void loadSlots(); }, [loadSlots]);
  useEffect(() => { bookingRef.current = booking; }, [booking]);
  useEffect(() => {
    if (!selected) return;
    closeButton.current?.focus();
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape" && !bookingRef.current) setSelected(null);
      if (event.key !== "Tab") return;
      const focusable = Array.from(dialog.current?.querySelectorAll<HTMLElement>("button:not([disabled]), textarea") ?? []);
      if (!focusable.length) return;
      const first = focusable[0];
      const last = focusable[focusable.length - 1];
      if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus(); }
      else if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus(); }
    };
    window.addEventListener("keydown", onKeyDown);
    return () => { window.removeEventListener("keydown", onKeyDown); if (trigger.current?.isConnected) trigger.current.focus(); };
  }, [selected]);

  async function book(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selected) return;
    setBooking(true);
    setBookingError("");
    try {
      const result = await apiRequest<Appointment>("/api/patient/appointments", {
        method: "POST", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ slotId: selected.slotId, reason: reason.trim() }),
      });
      setCreated(result);
      setSelected(null);
      setReason("");
      void loadSlots();
    } catch (cause) {
      if (cause instanceof ApiRequestError && cause.status === 409 && cause.code === "SLOT_UNAVAILABLE") {
        setBookingError("Este horario ya no está disponible. Selecciona otro horario.");
        setSelected(null);
        void loadSlots();
      } else setBookingError(cause instanceof Error ? cause.message : "No se pudo registrar la cita.");
    } finally { setBooking(false); }
  }

  return <div className="page-stack">
    <div className="page-heading"><p className="eyebrow">RESERVA DE CITA MÉDICA</p><h1>Encuentra tu próximo horario</h1><p>Explora la disponibilidad real, selecciona un horario y registra el motivo de tu consulta.</p></div>
    {created && <section className="appointment-success" role="status"><span className="success-icon" aria-hidden="true">✓</span><div><p className="eyebrow">RESERVA REGISTRADA</p><h2>Cita registrada correctamente</h2><p>Estado: <StatusBadge status={created.appointmentStatus} /></p><p>Motivo: {created.reason}</p><p className="reference-line">Número de cita: {created.id}</p></div><Link className="button primary" href="/patient/appointments">Ver mis citas <span aria-hidden="true">→</span></Link></section>}
    {bookingError && !selected && <p className="alert error" role="alert">{bookingError}</p>}
    <section className="availability-section" aria-labelledby="availability-title"><div className="section-heading"><div><p className="eyebrow">AGENDA ABIERTA</p><h2 id="availability-title">Horarios disponibles</h2></div><button className="button secondary small" type="button" onClick={() => void loadSlots()} disabled={loading}>{loading ? "Actualizando…" : "↻  Actualizar"}</button></div>
    {loading ? <div className="state-card" role="status"><span className="spinner" /> Cargando disponibilidad…</div> : loadError ? <div className="state-card error-state" role="alert"><div className="state-icon" aria-hidden="true">!</div><h3>No se pudo cargar la disponibilidad</h3><p>{loadError}</p><button className="button secondary" onClick={() => void loadSlots()}>Reintentar</button></div> : slots.length === 0 ? <div className="state-card"><div className="state-icon" aria-hidden="true">○</div><h3>No hay horarios disponibles</h3><p>Vuelve a consultar más adelante o actualiza la disponibilidad.</p><button className="button secondary" onClick={() => void loadSlots()}>Actualizar horarios</button></div> : <><div className="form-grid portal-panel"><label className="field">Especialidad<select value={specialtyId} onChange={event => { setSpecialtyId(event.target.value); setProfessionalId(""); setDate(""); }}><option value="">Selecciona</option>{specialties.map(item => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label><label className="field">Profesional<select value={professionalId} disabled={!specialtyId} onChange={event => { setProfessionalId(event.target.value); setDate(""); }}><option value="">Selecciona</option>{professionals.map(item => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label><label className="field">Fecha<select value={date} disabled={!professionalId} onChange={event => setDate(event.target.value)}><option value="">Selecciona</option>{dates.map(value => <option key={value} value={value}>{dateLabel(value)}</option>)}</select></label></div>{!date ? <p>Selecciona especialidad, profesional y fecha para ver las horas disponibles.</p> : visibleSlots.length === 0 ? <p>No hay horas disponibles para esta selección. Actualiza la disponibilidad.</p> : <div className="date-groups">{groups.map(([date, daySlots]) => <section className="date-group" key={date} aria-label={dateLabel(date)}><div className="date-group-heading"><h3>{dateLabel(date)}</h3><span>{daySlots.length} {daySlots.length === 1 ? "horario" : "horarios"}</span></div><div className="slot-grid">{daySlots.map(slot => <article className="slot-card" key={slot.slotId}>
      <div className="slot-top"><span className="pill">{slot.specialtyName}</span><span className="availability-dot">Disponible</span></div>
      <h4>{slot.professionalName}</h4>
      <p className="slot-date">{dateLabel(slot.slotDate)}</p>
      <div className="slot-time"><span aria-hidden="true">◷</span> {timeLabel(slot.startTime)} – {timeLabel(slot.endTime)}</div>
      <button className="button secondary" type="button" data-slot-id={slot.slotId} onClick={event => { trigger.current = event.currentTarget; setSelected(slot); setCreated(null); setBookingError(""); }}>Seleccionar</button>
    </article>)}</div></section>)}</div>}</>}</section>
    {selected && <div className="dialog-backdrop" role="presentation" onMouseDown={event => { if (event.target === event.currentTarget && !booking) setSelected(null); }}><section ref={dialog} className="booking-dialog" role="dialog" aria-modal="true" aria-labelledby="booking-title">
      <button ref={closeButton} className="close-button" type="button" aria-label="Cerrar" disabled={booking} onClick={() => setSelected(null)}>×</button>
      <p className="eyebrow">TU SELECCIÓN</p><h2 id="booking-title">Confirma tu cita</h2><p className="dialog-intro">Revisa el horario y cuéntanos brevemente el motivo de tu consulta.</p>
      <dl className="summary-list"><div><dt>Especialidad</dt><dd>{selected.specialtyName}</dd></div><div><dt>Profesional</dt><dd>{selected.professionalName}</dd></div><div><dt>Fecha</dt><dd>{dateLabel(selected.slotDate)}</dd></div><div><dt>Hora</dt><dd>{timeLabel(selected.startTime)} – {timeLabel(selected.endTime)}</dd></div></dl>
      <form onSubmit={book} className="form-stack"><label className="field">Motivo de consulta<textarea required maxLength={1000} rows={4} value={reason} onChange={e => setReason(e.target.value)} placeholder="Describe brevemente el motivo de la cita" /></label>
        {bookingError && <p className="alert error" role="alert">{bookingError}</p>}
        <div className="dialog-actions"><button className="button ghost" type="button" onClick={() => setSelected(null)}>Volver</button><button className="button primary" disabled={booking || !reason.trim()} type="submit">{booking ? "Registrando…" : "Confirmar reserva"}</button></div>
      </form>
    </section></div>}
  </div>;
}
