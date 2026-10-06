"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import { apiRequest } from "@/lib/api-client";

type Professional = { id: string; firstName: string; lastName: string; specialtyId: string; specialtyName: string; active: boolean };
type Schedule = { id: string; professionalId: string; specialtyId: string; dayOfWeek: number; startTime: string; endTime: string; active: boolean };
type Publication = { createdSlots: number; horizonDays: number };
type Slot = { scheduleId: string; status: string; usable: boolean };
const days = ["Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"];

export default function SchedulesPage() {
  const [professionals, setProfessionals] = useState<Professional[]>([]);
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [slots, setSlots] = useState<Slot[]>([]);
  const [professionalId, setProfessionalId] = useState("");
  const [selectedDays, setSelectedDays] = useState<number[]>([]);
  const [startTime, setStartTime] = useState("09:00");
  const [endTime, setEndTime] = useState("12:00");
  const [editing, setEditing] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const professional = professionals.find(item => item.id === professionalId);

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try {
      const [people, agendas, availability] = await Promise.all([
        apiRequest<Professional[]>("/api/portal/professionals"),
        apiRequest<Schedule[]>("/api/portal/agendas"),
        apiRequest<Slot[]>("/api/portal/availability"),
      ]);
      setProfessionals(people); setSchedules(agendas); setSlots(availability);
    } catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudieron cargar los horarios."); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { void load(); }, [load]);

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError(""); setNotice("");
    if (!professional?.active) { setError("Selecciona un profesional activo."); return; }
    if (startTime >= endTime) { setError("La hora de inicio debe ser anterior a la hora de fin."); return; }
    if (!selectedDays.length) { setError("Selecciona al menos un día."); return; }
    setBusy(true);
    let created = 0;
    try {
      for (const dayOfWeek of selectedDays) {
        await apiRequest(`/api/portal/agendas${editing ? `/${editing}` : ""}`, {
          method: editing ? "PUT" : "POST", headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ professionalId, specialtyId: professional.specialtyId, dayOfWeek, startTime, endTime }),
        });
        created++;
      }
      setNotice(editing ? "Horario actualizado." : `${created} ${created === 1 ? "horario creado" : "horarios creados"}. Publica cada horario para generar turnos.`);
      setEditing(null); setSelectedDays([]); await load();
    } catch (cause) { setError(`${cause instanceof Error ? cause.message : "No se pudo guardar."}${created ? ` Se guardaron ${created} días antes del error.` : ""}`); await load(); }
    finally { setBusy(false); }
  }

  async function publish(schedule: Schedule) {
    setBusy(true); setError(""); setNotice("");
    try {
      const result = await apiRequest<Publication>(`/api/portal/agendas/${schedule.id}/publish`, { method: "POST" });
      setNotice(`Se publicaron ${result.createdSlots} turnos para los próximos ${result.horizonDays} días.`); await load();
    } catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudo publicar."); }
    finally { setBusy(false); }
  }

  async function changeStatus(schedule: Schedule) {
    if (!window.confirm(`${schedule.active ? "Desactivar" : "Activar"} el horario de ${days[schedule.dayOfWeek]}?`)) return;
    setBusy(true); setError("");
    try {
      await apiRequest(`/api/portal/agendas/${schedule.id}/status`, { method: "PATCH", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ active: !schedule.active }) });
      setNotice("Estado actualizado."); await load();
    } catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudo cambiar el estado."); }
    finally { setBusy(false); }
  }

  return <div className="page-stack"><div className="page-heading"><p className="eyebrow">F04 · ADMINISTRACIÓN</p><h1>Horarios y turnos</h1><p>Configura días de atención y publica la disponibilidad que verá el paciente.</p></div>
    {error && <p className="alert error" role="alert">{error}</p>}{notice && <p className="alert success" role="status">{notice}</p>}
    <section className="portal-panel"><h2>{editing ? "Editar horario" : "Crear horarios"}</h2><form className="form-grid" onSubmit={save}>
      <label className="field full">Profesional<select required value={professionalId} onChange={e => setProfessionalId(e.target.value)}><option value="">Selecciona un profesional</option>{professionals.filter(item => item.active || item.id === professionalId).map(item => <option key={item.id} value={item.id}>{item.firstName} {item.lastName}</option>)}</select></label>
      <p className="full">Especialidad: <strong>{professional?.specialtyName ?? "Selecciona un profesional"}</strong></p>
      <fieldset className="full portal-days"><legend>Días de atención</legend>{days.map((day, index) => <label key={day}><input type="checkbox" checked={selectedDays.includes(index)} disabled={!!editing && !selectedDays.includes(index)} onChange={() => setSelectedDays(previous => previous.includes(index) ? previous.filter(value => value !== index) : [...previous, index])} /> {day}</label>)}</fieldset>
      <label className="field">Inicio<input type="time" required value={startTime} onChange={e => setStartTime(e.target.value)} /></label>
      <label className="field">Fin<input type="time" required value={endTime} onChange={e => setEndTime(e.target.value)} /></label>
      <div className="portal-actions full"><button className="button primary" type="submit" disabled={busy}>{busy ? "Guardando…" : editing ? "Guardar cambios" : "Crear horarios"}</button>{editing && <button className="button secondary" type="button" onClick={() => { setEditing(null); setSelectedDays([]); }}>Cancelar edición</button>}</div>
    </form></section>
    <section className="portal-panel"><h2>Horarios registrados</h2>{loading ? <p role="status">Cargando…</p> : schedules.length === 0 ? <p>Aún no hay horarios.</p> : <ul className="portal-list">{schedules.map(schedule => {
      const owner = professionals.find(item => item.id === schedule.professionalId);
      const available = slots.filter(slot => slot.scheduleId === schedule.id && slot.status === "AVAILABLE" && slot.usable).length;
      return <li className="portal-row" key={schedule.id}><div><strong>{owner ? `${owner.firstName} ${owner.lastName}` : "Profesional no disponible"}</strong><p>{days[schedule.dayOfWeek]} · {schedule.startTime.slice(0, 5)}–{schedule.endTime.slice(0, 5)} · {schedule.active ? "Activo" : "Inactivo"}</p><p>{available} turnos disponibles publicados</p></div><div className="portal-actions"><button className="button secondary small" type="button" disabled={busy} onClick={() => { setEditing(schedule.id); setProfessionalId(schedule.professionalId); setSelectedDays([schedule.dayOfWeek]); setStartTime(schedule.startTime.slice(0, 5)); setEndTime(schedule.endTime.slice(0, 5)); window.scrollTo({ top: 0, behavior: "smooth" }); }}>Editar</button><button className="button secondary small" type="button" disabled={busy} onClick={() => void changeStatus(schedule)}>{schedule.active ? "Desactivar" : "Activar"}</button><button className="button primary small" type="button" disabled={busy || !schedule.active} onClick={() => void publish(schedule)}>Publicar turnos</button></div></li>;
    })}</ul>}</section>
  </div>;
}
