"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { FormEvent, Suspense, useState } from "react";
import { ApiRequestError, apiRequest } from "@/lib/api-client";
import { Brand } from "@/components/brand";

function LoginForm() {
  const router = useRouter();
  const search = useSearchParams();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPending(true);
    setError("");
    try {
      await apiRequest("/api/session/login", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ email, password }) });
      setPassword("");
      router.replace("/patient/availability");
      router.refresh();
    } catch (cause) {
      setError(cause instanceof ApiRequestError && cause.status === 401 ? "Correo o contraseña incorrectos." : cause instanceof Error ? cause.message : "No se pudo iniciar sesión.");
    } finally { setPending(false); }
  }

  return <main className="auth-page">
    <section className="auth-story" aria-label="Información del portal">
      <Brand /><div className="auth-story-body"><span className="story-index">01 / ACCESO</span><p className="eyebrow light">TU SALUD, TU TIEMPO</p><p className="story-title">Tu próxima cita comienza aquí.</p><p>Consulta horarios disponibles y reserva tu cita médica desde un solo lugar.</p><div className="story-line" aria-hidden="true"><span /></div></div>
      <p className="auth-story-foot">HOSPITALPLATFORM · Portal de pacientes</p>
    </section>
    <section className="auth-panel"><div className="auth-card">
      <div className="auth-mobile-brand"><Brand /></div>
      <div className="auth-heading"><p className="eyebrow">PORTAL DE PACIENTES</p><h1>Bienvenido de nuevo</h1><p>Inicia sesión para consultar disponibilidad y ver tus citas.</p></div>
      {search?.get("expired") && <p className="alert error" role="alert">Tu sesión ha terminado. Inicia sesión de nuevo.</p>}
      <form onSubmit={submit} className="form-stack">
        <label className="field">Correo electrónico<input autoComplete="email" type="email" required value={email} onChange={e => setEmail(e.target.value)} placeholder="nombre@ejemplo.com" /></label>
        <label className="field">Contraseña<input autoComplete="current-password" type="password" required value={password} onChange={e => setPassword(e.target.value)} placeholder="Ingresa tu contraseña" /></label>
        {error && <p className="alert error" role="alert">{error}</p>}
        <button className="button primary auth-submit" disabled={pending} type="submit">{pending ? "Ingresando…" : "Iniciar sesión"}<span aria-hidden="true">→</span></button>
      </form>
      <p className="auth-footer">¿Aún no tienes cuenta? <Link href="/register">Crea tu cuenta</Link></p>
    </div></section>
  </main>;
}

export default function LoginPage() {
  return <Suspense><LoginForm /></Suspense>;
}
