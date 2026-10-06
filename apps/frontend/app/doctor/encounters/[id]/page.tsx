"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import { ApiRequestError, apiRequest } from "@/lib/api-client";

type History = { personal: Record<string, string>; family: Record<string, string>; gynecologic: Record<string, number | string | null>; otherAlerts: string };
type Assessment = { presentation: Record<string, string | number | null>; vitalSigns: Record<string, number | null>; examination: Record<string, string>; diagnosis: Record<string, string | null>; treatmentPlan: Record<string, string | null> };
type Item = { medicationId: string; presentationId: string; dose: number; doseUnit: string; frequency: string; route: string; duration: number; durationUnit: string; quantity: number; usageInstructions: string };
type Prescription = { additionalPrecautions: string; nonPharmacologicalRecommendations: string; warningSigns: string; additionalCare: string; followUpObservations: string; items: Item[] };
type Draft<T> = { version: number; history?: T; assessment?: T; prescription?: T };
type Option = { id: string; code?: string; description?: string; name?: string; genericName?: string; commercialName?: string };
type Presentation = { id: string; name: string; concentration: string; pharmaceuticalForm: string };

const emptyHistory: History = { personal: { pathological: "", surgical: "", allergiesAndReactions: "", usualMedication: "", transfusions: "", relevantHabits: "", hospitalizations: "", other: "" }, family: { father: "", mother: "", siblings: "", children: "", grandparents: "", other: "", source: "", observation: "" }, gynecologic: {}, otherAlerts: "" };
const emptyAssessment: Assessment = { presentation: { reason: "", symptomsAndCurrentIllness: "", illnessDuration: null, illnessDurationUnit: null, biologicalFunctions: "", reviewedHistoryAndAllergies: "", priorTreatmentAndResponse: "" }, vitalSigns: {}, examination: {}, diagnosis: { primaryDiagnosis: "", icd10CodeId: null, diagnosisType: "PRESUMPTIVE", observations: "", procedureId: null, priority: null }, treatmentPlan: { therapeuticPlan: "", generalIndications: "", patientEducation: "", warningSigns: "", referralType: "NONE", referralSpecialtyId: null, suggestedFollowUpDate: null, followUpReason: "", pendingResultsAndPlan: "", complementaryObservations: "" } };
const emptyPrescription: Prescription = { additionalPrecautions: "", nonPharmacologicalRecommendations: "", warningSigns: "", additionalCare: "", followUpObservations: "", items: [] };
const emptyItem: Item = { medicationId: "", presentationId: "", dose: 1, doseUnit: "MG", frequency: "EVERY_24_HOURS", route: "ORAL", duration: 1, durationUnit: "DAYS", quantity: 1, usageInstructions: "" };

export default function EncounterPage() {
  const { id } = useParams<{ id: string }>();
  const [history, setHistory] = useState<History>(emptyHistory);
  const [assessment, setAssessment] = useState<Assessment>(emptyAssessment);
  const [prescription, setPrescription] = useState<Prescription>(emptyPrescription);
  const [version, setVersion] = useState(0);
  const [saved, setSaved] = useState({ history: false, assessment: false, prescription: true });
  const [dirty, setDirty] = useState({ history: false, assessment: false, prescription: false });
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [finalized, setFinalized] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [icdQuery, setIcdQuery] = useState("");
  const [icdOptions, setIcdOptions] = useState<Option[]>([]);
  const [procedureQuery, setProcedureQuery] = useState("");
  const [procedureOptions, setProcedureOptions] = useState<Option[]>([]);
  const [medQuery, setMedQuery] = useState("");
  const [medOptions, setMedOptions] = useState<Option[]>([]);
  const [presentations, setPresentations] = useState<Presentation[]>([]);
  const [newItem, setNewItem] = useState<Item>(emptyItem);

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try {
      const [h, a, p] = await Promise.all([
        apiRequest<Draft<History>>(`/api/portal/medical/encounters/${id}/draft/history`),
        apiRequest<Draft<Assessment>>(`/api/portal/medical/encounters/${id}/draft/assessment`),
        apiRequest<Draft<Prescription>>(`/api/portal/medical/encounters/${id}/draft/prescription`),
      ]);
      if (h.version !== a.version || h.version !== p.version) throw new Error("Las versiones de los borradores no coinciden. Recarga la atención.");
      setVersion(h.version); setHistory(h.history ?? emptyHistory); setAssessment(a.assessment ?? emptyAssessment); setPrescription(p.prescription ?? emptyPrescription);
      setSaved({ history: !!h.history, assessment: !!a.assessment, prescription: !!p.prescription || !p.prescription });
      setDirty({ history: false, assessment: false, prescription: false });
    } catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudieron cargar los borradores."); }
    finally { setLoading(false); }
  }, [id]);
  useEffect(() => { void load(); }, [load]);

  async function save(section: "history" | "assessment" | "prescription") {
    if (busy) return;
    setBusy(true); setError(""); setNotice("");
    const value = section === "history" ? history : section === "assessment" ? assessment : prescription;
    try {
      const response = await apiRequest<Draft<unknown>>(`/api/portal/medical/encounters/${id}/draft/${section}`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ version, [section]: value }) });
      setVersion(response.version); setSaved(previous => ({ ...previous, [section]: true })); setDirty(previous => ({ ...previous, [section]: false })); setNotice("Borrador guardado.");
    } catch (cause) { setError(cause instanceof ApiRequestError && cause.status === 409 ? "Otra edición cambió esta atención. Copia tus cambios antes de recargar para resolver el conflicto." : cause instanceof Error ? cause.message : "No se pudo guardar."); }
    finally { setBusy(false); }
  }

  async function searchCatalog(kind: "icd10" | "procedures" | "medications", query: string) {
    if (query.trim().length < 2) { setError("Escribe al menos dos caracteres para buscar."); return; }
    setError("");
    try {
      const result = await apiRequest<Option[]>(`/api/portal/medical/catalogs/${kind}?q=${encodeURIComponent(query.trim())}&limit=20`);
      if (kind === "icd10") setIcdOptions(result); else if (kind === "procedures") setProcedureOptions(result); else setMedOptions(result);
      if (!result.length) setNotice("No hay resultados activos en el catálogo.");
    } catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudo consultar el catálogo."); }
  }

  async function chooseMedication(medicationId: string) {
    setNewItem({ ...newItem, medicationId, presentationId: "" }); setPresentations([]);
    if (!medicationId) return;
    try { setPresentations(await apiRequest<Presentation[]>(`/api/portal/medical/catalogs/medications/${medicationId}/presentations?limit=50`)); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudieron cargar las presentaciones."); }
  }

  async function finalize() {
    if (!saved.history || !saved.assessment || Object.values(dirty).some(Boolean) || !assessment.diagnosis.icd10CodeId || !String(assessment.diagnosis.primaryDiagnosis ?? "").trim() || !String(assessment.treatmentPlan.therapeuticPlan ?? "").trim() || !String(assessment.treatmentPlan.generalIndications ?? "").trim()) {
      setError("Guarda historia y evaluación completas antes de cerrar. El diagnóstico CIE-10 y el plan son obligatorios."); return;
    }
    if (!window.confirm("¿Finalizar esta atención? El cierre clínico es irreversible.")) return;
    setBusy(true); setError(""); setNotice("");
    try { await apiRequest(`/api/portal/medical/encounters/${id}/finalize`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ version }) }); setFinalized(true); setNotice("Atención finalizada correctamente."); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudo finalizar. Revisa el borrador y el catálogo."); }
    finally { setBusy(false); }
  }

  function historyField(section: "personal" | "family", key: string, value: string) {
    setHistory(previous => ({ ...previous, [section]: { ...previous[section], [key]: value } })); setDirty(previous => ({ ...previous, history: true }));
  }
  function assessmentField(section: "presentation" | "diagnosis" | "treatmentPlan", key: string, value: string | null) {
    setAssessment(previous => ({ ...previous, [section]: { ...previous[section], [key]: value } })); setDirty(previous => ({ ...previous, assessment: true }));
  }
  function prescriptionField(key: keyof Omit<Prescription, "items">, value: string) {
    setPrescription(previous => ({ ...previous, [key]: value })); setDirty(previous => ({ ...previous, prescription: true }));
  }

  return <div className="page-stack"><Link href="/doctor">← Volver a la cola</Link><div className="page-heading"><p className="eyebrow">ATENCIÓN MÉDICA</p><h1>Historia, diagnóstico y receta</h1><p>Los borradores se guardan por sección. Versión actual: {version}.</p></div>
    {error && <p className="alert error" role="alert">{error}</p>}{notice && <p className="alert success" role="status">{notice}</p>}
    {loading ? <p role="status">Cargando borradores…</p> : finalized ? <p className="alert success">La atención está finalizada y ya puede ser consultada por el paciente.</p> : <>
      <section className="portal-panel" id="historia"><h2>Historia clínica {dirty.history ? "· cambios sin guardar" : saved.history ? "· guardada" : ""}</h2>
        <div className="form-grid">{([ ["pathological", "Antecedentes patológicos"], ["surgical", "Antecedentes quirúrgicos"], ["allergiesAndReactions", "Alergias y reacciones"], ["usualMedication", "Medicación habitual"], ["transfusions", "Transfusiones"], ["relevantHabits", "Hábitos"], ["hospitalizations", "Hospitalizaciones declaradas"], ["other", "Otros antecedentes"] ] as const).map(([key, label]) => <label className="field" key={key}>{label}<textarea maxLength={4000} value={history.personal[key] ?? ""} onChange={e => historyField("personal", key, e.target.value)} /></label>)}
          {([ ["father", "Padre"], ["mother", "Madre"], ["siblings", "Hermanos"], ["children", "Hijos"], ["grandparents", "Abuelos"], ["other", "Otros familiares"], ["observation", "Observación familiar"] ] as const).map(([key, label]) => <label className="field" key={key}>{label}<textarea maxLength={4000} value={history.family[key] ?? ""} onChange={e => historyField("family", key, e.target.value)} /></label>)}
          <label className="field full">Otras alertas<textarea maxLength={4000} value={history.otherAlerts ?? ""} onChange={e => { setHistory({ ...history, otherAlerts: e.target.value }); setDirty(previous => ({ ...previous, history: true })); }} /></label></div>
        <button className="button primary" disabled={busy} onClick={() => void save("history")}>Guardar historia</button>
      </section>
      <section className="portal-panel" id="evaluacion"><h2>Diagnóstico y tratamiento {dirty.assessment ? "· cambios sin guardar" : saved.assessment ? "· guardados" : ""}</h2><div className="form-grid">
        <label className="field">Motivo de consulta<textarea maxLength={4000} value={assessment.presentation.reason ?? ""} onChange={e => assessmentField("presentation", "reason", e.target.value)} /></label>
        <label className="field">Síntomas y enfermedad actual<textarea maxLength={4000} value={assessment.presentation.symptomsAndCurrentIllness ?? ""} onChange={e => assessmentField("presentation", "symptomsAndCurrentIllness", e.target.value)} /></label>
        <label className="field">Diagnóstico principal<textarea maxLength={4000} value={assessment.diagnosis.primaryDiagnosis ?? ""} onChange={e => assessmentField("diagnosis", "primaryDiagnosis", e.target.value)} /></label>
        <label className="field">Tipo de diagnóstico<select value={assessment.diagnosis.diagnosisType ?? "PRESUMPTIVE"} onChange={e => assessmentField("diagnosis", "diagnosisType", e.target.value)}><option value="PRESUMPTIVE">Presuntivo</option><option value="DEFINITIVE">Definitivo</option></select></label>
        <div className="full"><label className="field">Buscar CIE-10<input value={icdQuery} onChange={e => setIcdQuery(e.target.value)} /></label><button className="button secondary small" type="button" onClick={() => void searchCatalog("icd10", icdQuery)}>Buscar</button><label className="field">Código CIE-10<select value={assessment.diagnosis.icd10CodeId ?? ""} onChange={e => assessmentField("diagnosis", "icd10CodeId", e.target.value || null)}><option value="">Selecciona un resultado</option>{icdOptions.map(option => <option key={option.id} value={option.id}>{option.code} · {option.description}</option>)}{assessment.diagnosis.icd10CodeId && !icdOptions.some(option => option.id === assessment.diagnosis.icd10CodeId) && <option value={assessment.diagnosis.icd10CodeId}>Código previamente guardado</option>}</select></label></div>
        <div className="full"><label className="field">Buscar procedimiento opcional<input value={procedureQuery} onChange={e => setProcedureQuery(e.target.value)} /></label><button className="button secondary small" type="button" onClick={() => void searchCatalog("procedures", procedureQuery)}>Buscar</button><label className="field">Procedimiento<select value={assessment.diagnosis.procedureId ?? ""} onChange={e => assessmentField("diagnosis", "procedureId", e.target.value || null)}><option value="">Ninguno</option>{procedureOptions.map(option => <option key={option.id} value={option.id}>{option.code} · {option.name}</option>)}{assessment.diagnosis.procedureId && !procedureOptions.some(option => option.id === assessment.diagnosis.procedureId) && <option value={assessment.diagnosis.procedureId}>Procedimiento previamente guardado</option>}</select></label></div>
        <label className="field">Plan terapéutico<textarea maxLength={4000} value={assessment.treatmentPlan.therapeuticPlan ?? ""} onChange={e => assessmentField("treatmentPlan", "therapeuticPlan", e.target.value)} /></label>
        <label className="field">Indicaciones generales<textarea maxLength={4000} value={assessment.treatmentPlan.generalIndications ?? ""} onChange={e => assessmentField("treatmentPlan", "generalIndications", e.target.value)} /></label>
        <label className="field">Educación al paciente<textarea maxLength={4000} value={assessment.treatmentPlan.patientEducation ?? ""} onChange={e => assessmentField("treatmentPlan", "patientEducation", e.target.value)} /></label>
        <label className="field">Signos de alerta<textarea maxLength={4000} value={assessment.treatmentPlan.warningSigns ?? ""} onChange={e => assessmentField("treatmentPlan", "warningSigns", e.target.value)} /></label>
      </div><button className="button primary" disabled={busy} onClick={() => void save("assessment")}>Guardar evaluación</button></section>
      <section className="portal-panel" id="receta"><h2>Receta opcional {dirty.prescription ? "· cambios sin guardar" : ""}</h2><p>Si no se prescribe un medicamento, no se emite receta.</p>
        <div className="form-grid"><label className="field">Buscar medicamento<input value={medQuery} onChange={e => setMedQuery(e.target.value)} /></label><button className="button secondary small" type="button" onClick={() => void searchCatalog("medications", medQuery)}>Buscar</button>
          <label className="field">Medicamento<select value={newItem.medicationId} onChange={e => void chooseMedication(e.target.value)}><option value="">Selecciona</option>{medOptions.map(option => <option key={option.id} value={option.id}>{option.genericName} {option.commercialName ?? ""}</option>)}</select></label>
          <label className="field">Presentación<select value={newItem.presentationId} disabled={!newItem.medicationId} onChange={e => setNewItem({ ...newItem, presentationId: e.target.value })}><option value="">Selecciona</option>{presentations.map(option => <option key={option.id} value={option.id}>{option.name} · {option.concentration}</option>)}</select></label>
          <label className="field">Dosis<input type="number" min="0.001" step="0.001" value={newItem.dose} onChange={e => setNewItem({ ...newItem, dose: Number(e.target.value) })} /></label>
          <label className="field">Unidad de dosis<select value={newItem.doseUnit} onChange={e => setNewItem({ ...newItem, doseUnit: e.target.value })}>{["MG", "G", "ML", "DROPS", "TABLET", "CAPSULE", "IU"].map(value => <option key={value}>{value}</option>)}</select></label>
          <label className="field">Frecuencia<select value={newItem.frequency} onChange={e => setNewItem({ ...newItem, frequency: e.target.value })}>{["EVERY_6_HOURS", "EVERY_8_HOURS", "EVERY_12_HOURS", "EVERY_24_HOURS", "ONCE"].map(value => <option key={value}>{value}</option>)}</select></label>
          <label className="field">Vía<select value={newItem.route} onChange={e => setNewItem({ ...newItem, route: e.target.value })}>{["ORAL", "SUBLINGUAL", "INTRAMUSCULAR", "INTRAVENOUS", "SUBCUTANEOUS", "TOPICAL", "INHALATION", "OPHTHALMIC", "OTIC", "RECTAL", "VAGINAL"].map(value => <option key={value}>{value}</option>)}</select></label>
          <label className="field">Duración<input type="number" min="0.001" step="0.001" value={newItem.duration} onChange={e => setNewItem({ ...newItem, duration: Number(e.target.value) })} /></label>
          <label className="field">Unidad de duración<select value={newItem.durationUnit} onChange={e => setNewItem({ ...newItem, durationUnit: e.target.value })}><option value="DAYS">Días</option><option value="WEEKS">Semanas</option></select></label>
          <label className="field">Cantidad<input type="number" min="1" step="1" value={newItem.quantity} onChange={e => setNewItem({ ...newItem, quantity: Number(e.target.value) })} /></label>
          <label className="field full">Instrucciones de uso<textarea maxLength={4000} value={newItem.usageInstructions} onChange={e => setNewItem({ ...newItem, usageInstructions: e.target.value })} /></label>
        </div><button className="button secondary" type="button" disabled={!newItem.medicationId || !newItem.presentationId || !newItem.usageInstructions.trim() || newItem.dose <= 0 || newItem.duration <= 0 || newItem.quantity < 1 || prescription.items.length >= 50} onClick={() => { setPrescription(previous => ({ ...previous, items: [...previous.items, newItem] })); setNewItem(emptyItem); setMedOptions([]); setPresentations([]); setDirty(previous => ({ ...previous, prescription: true })); }}>Añadir medicamento</button>
        {prescription.items.length > 0 && <ol className="portal-list">{prescription.items.map((item, index) => <li className="portal-row" key={`${item.medicationId}-${index}`}><div><strong>Medicamento {index + 1}</strong><p>{item.medicationId} · {item.dose} {item.doseUnit} · {item.frequency} · {item.usageInstructions}</p></div><button className="button secondary small" type="button" onClick={() => { setPrescription(previous => ({ ...previous, items: previous.items.filter((_, position) => position !== index) })); setDirty(previous => ({ ...previous, prescription: true })); }}>Quitar</button></li>)}</ol>}
        {([ ["additionalPrecautions", "Precauciones"], ["nonPharmacologicalRecommendations", "Recomendaciones no farmacológicas"], ["warningSigns", "Signos de alerta"], ["additionalCare", "Cuidados adicionales"], ["followUpObservations", "Seguimiento"] ] as const).map(([key, label]) => <label className="field" key={key}>{label}<textarea maxLength={4000} value={prescription[key]} onChange={e => prescriptionField(key, e.target.value)} /></label>)}
        <button className="button primary" disabled={busy} onClick={() => void save("prescription")}>Guardar receta</button></section>
      <section className="portal-panel"><h2>Cierre</h2><p>Revisa las tres secciones y confirma el cierre único. La fecha y la etapa se validan en el servidor.</p><div className="portal-actions"><button className="button secondary" type="button" onClick={() => { if (window.confirm("¿Descartar cambios locales y recargar borradores?")) void load(); }}>Recargar borradores</button><button className="button primary" disabled={busy || !saved.history || !saved.assessment || Object.values(dirty).some(Boolean)} onClick={() => void finalize()}>Finalizar atención</button></div></section>
    </>}
  </div>;
}
