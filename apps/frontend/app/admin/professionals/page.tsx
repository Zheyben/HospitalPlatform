"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import { apiRequest } from "@/lib/api-client";

type Specialty = { id: string; name: string };
type Professional = { id: string; firstName: string; lastName: string; licenseNumber: string; specialtyId: string; specialtyName: string; active: boolean };
type Form = { firstName: string; lastName: string; email: string; password: string; licenseNumber: string; specialtyId: string };
const empty: Form = { firstName: "", lastName: "", email: "", password: "", licenseNumber: "", specialtyId: "" };

export default function ProfessionalsPage() {
  const [items, setItems] = useState<Professional[]>([]);
  const [specialties, setSpecialties] = useState<Specialty[]>([]);
  const [form, setForm] = useState<Form>(empty);
  const [editing, setEditing] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try {
      const [professionals, options] = await Promise.all([
        apiRequest<Professional[]>("/api/portal/professionals"),
        apiRequest<Specialty[]>("/api/portal/specialties"),
      ]);
      setItems(professionals); setSpecialties(options);
    } catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudieron cargar los profesionales."); }
    finally { setLoading(false); }
  }, []);
  useEffect(() => { void load(); }, [load]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!/^[0-9]{4,6}$/.test(form.licenseNumber)) { setError("El CMP debe tener de 4 a 6 dígitos."); return; }
    setSaving(true); setError(""); setNotice("");
    try {
      const payload = editing
        ? { firstName: form.firstName.trim(), lastName: form.lastName.trim(), licenseNumber: form.licenseNumber, specialtyId: form.specialtyId }
        : { ...form, firstName: form.firstName.trim(), lastName: form.lastName.trim(), email: form.email.trim() };
      await apiRequest(`/api/portal/professionals${editing ? `/${editing}` : ""}`, {
        method: editing ? "PUT" : "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload),
      });
      setForm(empty); setEditing(null); setNotice(editing ? "Profesional actualizado." : "Profesional creado.");
      await load();
    } catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudo guardar."); }
    finally { setSaving(false); }
  }

  async function changeStatus(item: Professional) {
    if (!window.confirm(`${item.active ? "Desactivar" : "Reactivar"} a ${item.firstName} ${item.lastName}?`)) return;
    setError(""); setNotice("");
    try {
      await apiRequest(`/api/portal/professionals/${item.id}/status`, { method: "PATCH", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ active: !item.active }) });
      setNotice("Estado actualizado."); await load();
    } catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudo cambiar el estado."); }
  }

  const filtered = items.filter(item => `${item.firstName} ${item.lastName} ${item.licenseNumber} ${item.specialtyName}`.toLocaleLowerCase("es-PE").includes(search.toLocaleLowerCase("es-PE")));
  return <div className="page-stack">
    <div className="page-heading"><p className="eyebrow">F03 · ADMINISTRACIÓN</p><h1>Profesionales</h1><p>Registra y administra profesionales con una especialidad activa.</p></div>
    {error && <p className="alert error" role="alert">{error}</p>}{notice && <p className="alert success" role="status">{notice}</p>}
    <section className="portal-panel"><h2>{editing ? "Editar profesional" : "Nuevo profesional"}</h2>
      <form className="form-grid" onSubmit={submit}>
        <label className="field">Nombre<input required maxLength={100} value={form.firstName} onChange={e => setForm({ ...form, firstName: e.target.value })} /></label>
        <label className="field">Apellido<input required maxLength={100} value={form.lastName} onChange={e => setForm({ ...form, lastName: e.target.value })} /></label>
        {!editing && <><label className="field">Correo de acceso<input type="email" required maxLength={255} value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} /></label><label className="field">Contraseña inicial<input type="password" required minLength={8} maxLength={128} value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} /></label></>}
        <label className="field">CMP<input required inputMode="numeric" pattern="[0-9]{4,6}" maxLength={6} value={form.licenseNumber} onChange={e => setForm({ ...form, licenseNumber: e.target.value })} /></label>
        <label className="field">Especialidad<select required value={form.specialtyId} onChange={e => setForm({ ...form, specialtyId: e.target.value })}><option value="">Selecciona una especialidad</option>{specialties.map(option => <option key={option.id} value={option.id}>{option.name}</option>)}</select></label>
        <div className="portal-actions full"><button className="button primary" type="submit" disabled={saving || !specialties.length}>{saving ? "Guardando…" : editing ? "Guardar cambios" : "Crear profesional"}</button>{editing && <button className="button secondary" type="button" onClick={() => { setEditing(null); setForm(empty); }}>Cancelar edición</button>}</div>
      </form>
    </section>
    <section className="portal-panel"><h2>Profesionales registrados</h2><label className="field">Buscar<input type="search" value={search} onChange={e => setSearch(e.target.value)} placeholder="Nombre, CMP o especialidad" /></label>
      {loading ? <p role="status">Cargando…</p> : filtered.length === 0 ? <p>No hay profesionales que coincidan.</p> : <ul className="portal-list">{filtered.map(item => <li className="portal-row" key={item.id}><div><strong>{item.firstName} {item.lastName}</strong><p>CMP {item.licenseNumber} · {item.specialtyName} · {item.active ? "Activo" : "Inactivo"}</p></div><div className="portal-actions"><button className="button secondary small" type="button" onClick={() => { setEditing(item.id); setForm({ firstName: item.firstName, lastName: item.lastName, email: "", password: "", licenseNumber: item.licenseNumber, specialtyId: item.specialtyId }); window.scrollTo({ top: 0, behavior: "smooth" }); }}>Editar</button><button className="button secondary small" type="button" onClick={() => void changeStatus(item)}>{item.active ? "Desactivar" : "Reactivar"}</button></div></li>)}</ul>}
    </section>
  </div>;
}
