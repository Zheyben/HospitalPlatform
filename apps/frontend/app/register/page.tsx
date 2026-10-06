"use client";

import Link from "next/link";
import { FormEvent, useEffect, useState } from "react";
import { ApiRequestError, apiRequest } from "@/lib/api-client";
import { Brand } from "@/components/brand";

type Registration = {
  email: string; password: string; documentType: string; documentNumber: string;
  firstName: string; lastName: string; birthDate: string; phone: string; insuranceId: string;
  address: string; sex: string; maritalStatus: string; occupation: string; district: string;
  educationLevel: string; affiliationNumber: string; emergencyContactName: string;
  emergencyContactRelationship: string; emergencyContactPhone: string;
};

type InsuranceOption = { id: string; code: string; name: string };

const documentRules: Record<string, { pattern: string; hint: string; maxLength: number }> = {
  DNI: { pattern: "[0-9]{8}", hint: "8 dígitos", maxLength: 8 },
  CE: { pattern: "[A-Za-z0-9]{8,12}", hint: "8 a 12 caracteres alfanuméricos", maxLength: 12 },
  PASSPORT: { pattern: "[A-Za-z0-9]{6,12}", hint: "6 a 12 caracteres alfanuméricos", maxLength: 12 },
};

const initial: Registration = {
  email: "", password: "", documentType: "DNI", documentNumber: "", firstName: "", lastName: "",
  birthDate: "", phone: "", insuranceId: "", address: "", sex: "", maritalStatus: "",
  occupation: "", district: "", educationLevel: "", affiliationNumber: "",
  emergencyContactName: "", emergencyContactRelationship: "", emergencyContactPhone: "",
};

function yesterdayInLima() {
  const parts = new Intl.DateTimeFormat("en-US", { timeZone: "America/Lima", year: "numeric", month: "2-digit", day: "2-digit" }).formatToParts(new Date());
  const value = (part: string) => parts.find(item => item.type === part)?.value ?? "";
  return new Date(Date.parse(`${value("year")}-${value("month")}-${value("day")}T12:00:00Z`) - 86400000).toISOString().slice(0, 10);
}

export default function RegisterPage() {
  const [form, setForm] = useState(initial);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [done, setDone] = useState(false);
  const [insuranceOptions, setInsuranceOptions] = useState<InsuranceOption[]>([]);
  const [insuranceError, setInsuranceError] = useState("");
  const selectedInsurance = insuranceOptions.find(option => option.id === form.insuranceId);

  useEffect(() => {
    let live = true;
    apiRequest<InsuranceOption[]>("/api/public/insurance-providers")
      .then(options => { if (live) setInsuranceOptions(options); })
      .catch(() => { if (live) setInsuranceError("No se pudieron cargar los seguros. Recarga la página."); });
    return () => { live = false; };
  }, []);

  function clearField(name: string) {
    setFieldErrors(previous => ({ ...previous, [name]: "" }));
  }

  function validationMessage(name: string) {
    return name === "documentNumber" ? `Ingresa ${documentRules[form.documentType].hint}.`
      : name === "phone" ? "Ingresa entre 7 y 15 dígitos; puedes incluir + al inicio."
      : name === "password" ? "La contraseña debe tener al menos 8 caracteres."
      : name === "email" ? "Ingresa un correo electrónico válido."
      : name === "birthDate" ? "Selecciona una fecha de nacimiento pasada."
      : "Este campo es obligatorio.";
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    const invalidInputs = Array.from(event.currentTarget.querySelectorAll<HTMLInputElement | HTMLSelectElement>("input, select"))
      .filter(input => !input.checkValidity());
    if (invalidInputs.length > 0) {
      setFieldErrors(Object.fromEntries(invalidInputs.map(input => [input.name, validationMessage(input.name)])));
      invalidInputs[0].focus();
      return;
    }
    setPending(true);
    try {
      const payload = Object.fromEntries(Object.entries(form).filter(([, value]) => value.trim() !== ""));
      await apiRequest("/api/session/register", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) });
      setDone(true);
    } catch (cause) {
      if (cause instanceof ApiRequestError) {
        if (cause.code === "EMAIL_ALREADY_EXISTS") {
          setFieldErrors(previous => ({ ...previous, email: "Ya existe una cuenta con este correo." }));
          setError("Este correo ya está registrado.");
        } else if (cause.code === "DUPLICATE_DOCUMENT") {
          setFieldErrors(previous => ({ ...previous, documentNumber: "Ya existe una cuenta asociada a este documento." }));
          setError("Este documento ya está registrado.");
        } else setError(cause.status === 400 ? "Revisa los datos del formulario e inténtalo de nuevo." : cause.message);
      } else setError("No se pudo crear la cuenta.");
    } finally { setPending(false); }
  }

  return <main className="auth-page register-page">
    <section className="auth-story" aria-label="Información del portal"><Brand /><div className="auth-story-body"><span className="story-index">02 / REGISTRO</span><p className="eyebrow light">COMIENZA AQUÍ</p><p className="story-title">Reserva con confianza, a tu ritmo.</p><p>Crea tu cuenta de paciente para consultar horarios reales y registrar tus citas.</p><div className="story-line" aria-hidden="true"><span /></div></div><p className="auth-story-foot">HOSPITALPLATFORM · Portal de pacientes</p></section>
    <section className="auth-panel"><div className="auth-card auth-card-wide">
    <div className="auth-mobile-brand"><Brand /></div>
    {done ? <div className="success-panel" role="status"><div className="success-icon">✓</div><h1>Tu cuenta ya está lista</h1><p>Cuenta creada correctamente. Inicia sesión para continuar.</p><Link className="button primary" href="/login">Ir a iniciar sesión</Link></div> : <>
      <div className="auth-heading"><p className="eyebrow">NUEVA CUENTA DE PACIENTE</p><h1>Crea tu cuenta</h1><p>Completa tus datos de acceso e identificación. Los datos adicionales son opcionales.</p></div>
      <form onSubmit={submit} noValidate className="form-grid">
        <div className="form-section-title full"><span>01</span><div><h2>Datos de acceso</h2><p>Usarás este correo para iniciar sesión.</p></div></div>
        <label className="field full">Correo electrónico<input name="email" autoComplete="email" type="email" maxLength={255} required aria-invalid={!!fieldErrors.email} aria-describedby={fieldErrors.email ? "error-email" : undefined} value={form.email} onChange={e => { setForm({ ...form, email: e.target.value }); clearField("email"); }} placeholder="nombre@ejemplo.com" />{fieldErrors.email && <small className="field-error" id="error-email">{fieldErrors.email}</small>}</label>
        <label className="field full">Contraseña<input name="password" autoComplete="new-password" type="password" minLength={8} maxLength={128} required aria-invalid={!!fieldErrors.password} aria-describedby={fieldErrors.password ? "error-password" : "hint-password"} value={form.password} onChange={e => { setForm({ ...form, password: e.target.value }); clearField("password"); }} placeholder="Mínimo 8 caracteres" /><small id={fieldErrors.password ? "error-password" : "hint-password"} className={fieldErrors.password ? "field-error" : ""}>{fieldErrors.password || "Mínimo 8 caracteres."}</small></label>
        <div className="form-section-title full"><span>02</span><div><h2>Identificación</h2><p>Ingresa los datos de tu documento.</p></div></div>
        <label className="field">Tipo de documento<select name="documentType" required value={form.documentType} onChange={e => { setForm({ ...form, documentType: e.target.value, documentNumber: "" }); clearField("documentNumber"); }}><option value="DNI">DNI</option><option value="CE">Carné de Extranjería</option><option value="PASSPORT">Pasaporte</option></select></label>
        <label className="field">Número de documento<input name="documentNumber" required pattern={documentRules[form.documentType].pattern} title={documentRules[form.documentType].hint} maxLength={documentRules[form.documentType].maxLength} aria-invalid={!!fieldErrors.documentNumber} aria-describedby={fieldErrors.documentNumber ? "error-documentNumber" : "hint-documentNumber"} value={form.documentNumber} onChange={e => { setForm({ ...form, documentNumber: e.target.value.toUpperCase() }); clearField("documentNumber"); }} placeholder={form.documentType === "DNI" ? "12345678" : "Número de documento"} /><small id={fieldErrors.documentNumber ? "error-documentNumber" : "hint-documentNumber"} className={fieldErrors.documentNumber ? "field-error" : ""}>{fieldErrors.documentNumber || `${documentRules[form.documentType].hint}.`}</small></label>
        <div className="form-section-title full"><span>03</span><div><h2>Datos personales</h2><p>Información necesaria para tu cuenta de paciente.</p></div></div>
        <label className="field">Nombre<input name="firstName" autoComplete="given-name" required maxLength={100} aria-invalid={!!fieldErrors.firstName} aria-describedby={fieldErrors.firstName ? "error-firstName" : undefined} value={form.firstName} onChange={e => { setForm({ ...form, firstName: e.target.value }); clearField("firstName"); }} placeholder="Tu nombre" />{fieldErrors.firstName && <small className="field-error" id="error-firstName">{fieldErrors.firstName}</small>}</label>
        <label className="field">Apellido<input name="lastName" autoComplete="family-name" required maxLength={100} aria-invalid={!!fieldErrors.lastName} aria-describedby={fieldErrors.lastName ? "error-lastName" : undefined} value={form.lastName} onChange={e => { setForm({ ...form, lastName: e.target.value }); clearField("lastName"); }} placeholder="Tu apellido" />{fieldErrors.lastName && <small className="field-error" id="error-lastName">{fieldErrors.lastName}</small>}</label>
        <label className="field">Fecha de nacimiento<input name="birthDate" type="date" required max={yesterdayInLima()} aria-invalid={!!fieldErrors.birthDate} aria-describedby={fieldErrors.birthDate ? "error-birthDate" : undefined} value={form.birthDate} onChange={e => { setForm({ ...form, birthDate: e.target.value }); clearField("birthDate"); }} />{fieldErrors.birthDate && <small className="field-error" id="error-birthDate">{fieldErrors.birthDate}</small>}</label>
        <label className="field">Teléfono<input name="phone" autoComplete="tel" type="tel" required pattern="\+?[0-9]{7,15}" title="Ingresa entre 7 y 15 dígitos; puedes incluir + al inicio." maxLength={50} aria-invalid={!!fieldErrors.phone} aria-describedby={fieldErrors.phone ? "error-phone" : undefined} value={form.phone} onChange={e => { setForm({ ...form, phone: e.target.value }); clearField("phone"); }} placeholder="+573001234567" />{fieldErrors.phone && <small className="field-error" id="error-phone">{fieldErrors.phone}</small>}</label>
        <label className="field full">Seguro<select name="insuranceId" required value={form.insuranceId} onChange={e => { setForm({ ...form, insuranceId: e.target.value, affiliationNumber: "" }); clearField("insuranceId"); }}><option value="">Selecciona tu seguro</option>{insuranceOptions.map(option => <option key={option.id} value={option.id}>{option.name}</option>)}</select>{fieldErrors.insuranceId && <small className="field-error">Selecciona un seguro.</small>}</label>
        {insuranceError && <p className="alert error full" role="alert">{insuranceError}</p>}
        <div className="form-section-title full"><span>04</span><div><h2>Datos adicionales</h2><p>Opcionales; ayudan a completar tu historia clínica.</p></div></div>
        <label className="field full">Dirección<input name="address" maxLength={500} value={form.address} onChange={e => setForm({ ...form, address: e.target.value })} /></label>
        <label className="field">Sexo declarado<input name="sex" maxLength={50} value={form.sex} onChange={e => setForm({ ...form, sex: e.target.value })} /></label>
        <label className="field">Estado civil<input name="maritalStatus" maxLength={80} value={form.maritalStatus} onChange={e => setForm({ ...form, maritalStatus: e.target.value })} /></label>
        <label className="field">Ocupación<input name="occupation" maxLength={120} value={form.occupation} onChange={e => setForm({ ...form, occupation: e.target.value })} /></label>
        <label className="field">Distrito<input name="district" maxLength={120} value={form.district} onChange={e => setForm({ ...form, district: e.target.value })} /></label>
        <label className="field">Grado de instrucción<input name="educationLevel" maxLength={120} value={form.educationLevel} onChange={e => setForm({ ...form, educationLevel: e.target.value })} /></label>
        {selectedInsurance && selectedInsurance.code !== "PARTICULAR" && <label className="field">Número de afiliación<input name="affiliationNumber" maxLength={60} value={form.affiliationNumber} onChange={e => setForm({ ...form, affiliationNumber: e.target.value })} /></label>}
        <label className="field">Contacto de emergencia<input name="emergencyContactName" maxLength={150} value={form.emergencyContactName} onChange={e => setForm({ ...form, emergencyContactName: e.target.value })} /></label>
        <label className="field">Parentesco<input name="emergencyContactRelationship" maxLength={80} value={form.emergencyContactRelationship} onChange={e => setForm({ ...form, emergencyContactRelationship: e.target.value })} /></label>
        <label className="field">Teléfono de emergencia<input name="emergencyContactPhone" type="tel" pattern="\+?[0-9]{7,15}" maxLength={50} value={form.emergencyContactPhone} onChange={e => setForm({ ...form, emergencyContactPhone: e.target.value })} /></label>
        {error && <p className="alert error full" role="alert">{error}</p>}
        <button className="button primary full auth-submit" disabled={pending || !insuranceOptions.length} type="submit">{pending ? "Creando cuenta…" : "Crear cuenta"}<span aria-hidden="true">→</span></button>
      </form>
      <p className="auth-footer">¿Ya tienes cuenta? <Link href="/login">Inicia sesión</Link></p>
    </>}
  </div></section></main>;
}
