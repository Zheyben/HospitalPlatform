# ETAPA F.2.2 — Auditoría posterior de flujos de portales privados

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico.

**Fecha:** 2026-09-30.

**Rama:** `feat/hospitalplatform-user-flows`.

**Archivo auditado:** [PRIVATE-PORTALS-USER-FLOWS.md](PRIVATE-PORTALS-USER-FLOWS.md).

## 1. Estado Git

Al iniciar, `git status --short --branch` confirmó la rama indicada y mostró `?? docs/ux/user-flows/`; no había cambios rastreados. La carpeta ya contenía los entregables F.2.1 y el flujo F.2.2 sin seguimiento. Esta auditoría crea el presente informe y corrige una expresión factual en el flujo F.2.2. No se hace commit, push ni merge.

## 2. Fuentes revisadas

- [F.1 requisitos UX](../UX-REQUIREMENTS-ANALYSIS.md), [F.1.1 post-audit](../UX-REQUIREMENTS-POST-AUDIT.md) y [F.2.1 portal público](PUBLIC-PORTAL-USER-FLOWS.md) para la separación entre API existente, pantalla candidata y capacidad conceptual.
- [Domain Baseline](../../DOMAIN-BASELINE.md), especialmente roles, RF-002/007/011–014/016–019/026–030 y tabla de endpoints; [Decision Register](../../DOMAIN-DECISION-REGISTER.md), en particular DEC-005 `OPEN`, DEC-006/007 `CLOSED` en diseño y DEC-008/010 `OPEN`.
- Etapa C: [C.1 RF–UC](../../agenda/AGENDA-AVAILABILITY-RF-UC-MATRIX.md), [C.2 casos](../../agenda/AGENDA-AVAILABILITY-USE-CASES.md), [C.3 contratos](../../agenda/AGENDA-AVAILABILITY-CONTRACT-DESIGN.md), [C.4 especificaciones](../../agenda/AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md).
- Etapa D: [D.2 detalles](../../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [D.3.1 implementados](../../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md), [D.3.2 parciales](../../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md), [D.3.3 conceptuales](../../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md), [D.4 catálogo](../../agenda/consolidation/AGENDA-USE-CASE-CATALOG.md), [matriz de actores](../../agenda/consolidation/AGENDA-ACTOR-USECASE-MATRIX.md) y [trazabilidad](../../agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md).
- Contratos y seguridad actuales en lectura: [ProfessionalController](../../../apps/backend/src/main/java/com/hospital/platform/professionals/controller/ProfessionalController.java), [AgendaController](../../../apps/backend/src/main/java/com/hospital/platform/agenda/controller/AgendaController.java), [AppointmentController](../../../apps/backend/src/main/java/com/hospital/platform/appointments/controller/AppointmentController.java), [PatientController](../../../apps/backend/src/main/java/com/hospital/platform/patients/controller/PatientController.java), [SecurityConfiguration](../../../apps/backend/src/main/java/com/hospital/platform/security/config/SecurityConfiguration.java), `ProfessionalResponseDTO`, `AgendaResponseDTO`, `AvailabilitySlotResponseDTO` y `AppointmentResponseDTO`.

## 3. Validación ADMIN

**Correcta tras la precisión indicada abajo.** CU-D1/RF-007 permanece `PARCIAL`: ADMIN puede crear, listar, detallar y actualizar la licencia de profesional; no hay API de asociación N:M ni desactivación HTTP. CU-D4/RF-011 permanece `PARCIAL`: los endpoints de horarios crean, consultan, actualizan y cambian `active`, pero no generan slots ni validan specialty activa/asociada. CU-D5 implementa dos GET de disponibilidad operativa **solo para ADMIN**; RF-012 global sigue parcial por CU-D6. La vista «área operativa» se declara navegación candidata sin métricas, reportes o dashboard implementado. No se introducen módulos administrativos nuevos.

## 4. Validación RECEPTIONIST

**Correcta.** El rol puede listar/detallar citas; crear para paciente activo con `patientId` y `slotId` conocidos; confirmar, cancelar o reprogramar según elegibilidad; y realizar check-in y espera en las transiciones exactas. El documento identifica que no hay lista de pacientes para este rol ni una consulta autorizada de slots, de modo que obtener esos identificadores es una dependencia UX. CU-D6 sigue `CONCEPTUAL` para RECEPTIONIST según DEC-007; los GET `/api/v1/availability` actuales son ADMIN. No aparecen permisos de gestión clínica, pacientes, profesionales ni horarios.

## 5. Validación PROFESSIONAL

**Correcta con brecha expresada.** El rol puede invocar inicio y fin de **estado operacional** de cita asignada mediante dos POST de Appointments; RF-028/029 son backend vigente. Los GET de citas no autorizan PROFESSIONAL y RF-030 es `PARCIAL`. El documento no crea portal completo, dashboard, agenda personal, listado de pacientes asignados, historia clínica ni atención médica. El flujo UX de navegación se marca `CONCEPTUAL / NO IMPLEMENTADO` y DEC-005 permanece `OPEN` para el ciclo User–Professional. Las mutaciones actuales no se degradan a conceptuales por esa brecha de UI.

## 6. Pantallas, estados y trazabilidad

Cada fila del inventario tiene pantalla candidata, actor, objetivo, datos, acciones y estado. Las áreas de entrada son organización del prototipo, sin API o interfaz existente atribuida. Los campos listados coinciden con los DTO de Professional, Schedule, slot y cita revisados. No se encontró pantalla de un módulo inexistente.

La matriz conserva **pantalla → caso de uso → RF → estado**: CU-D1/RF-007 `PARCIAL`, CU-D4/RF-011 `PARCIAL`, CU-D5/RF-012 `IMPLEMENTADO` solo ADMIN, CU-D6/RF-012 `CONCEPTUAL`, CU-D7/RF-013 `IMPLEMENTADO`, CU-D8/RF-014 interno; las operaciones de Appointments fuera de CU-D1–D8 se indican con `—`. RF-030 permanece `PARCIAL` como requisito global. `loading`, vacío, error, éxito y no autorizado son presentación, no estados persistidos nuevos; los estados de cita/slot se nombran como datos de dominio.

## 7. Hallazgos y correcciones

| Nivel | Hallazgo | Corrección |
|---|---|---|
| **BAJO, factual** | La celda de trazabilidad de profesionales decía «CRUD básico», término que sugiere borrado, aunque `ProfessionalController` expone POST, GET y PUT sin DELETE. | Se sustituyó por «alta, consulta y actualización básicas», sin cambiar estado CU-D1 ni agregar operación. |
| **Observación** | RECEPTIONIST requiere `patientId` y `slotId` para crear cita, pero carece de búsquedas autorizadas de pacientes y disponibilidad. | Sin cambio: el flujo ya declara la dependencia. |
| **Observación** | PROFESSIONAL puede mutar una cita asignada, pero no consultar sus citas por GET; no hay recorrido de portal ejecutable. | Sin cambio: el flujo ya distingue backend y UX. |

Hallazgos **CRÍTICOS: 0**; **MEDIOS: 0**. Archivos existentes fuera del flujo base modificados: **0**.

## 8. Validación final y gate

Se ejecutaron `git status` y `git diff --check`. La rama sigue `feat/hospitalplatform-user-flows` y Git muestra solo `?? docs/ux/user-flows/`; no hay cambios rastreados. `git diff --check` no informó errores en archivos rastreados. La revisión directa de los dos Markdown de F.2.2 no encontró espacios finales ni enlaces locales rotos; Git aún no incluye esos archivos sin seguimiento en el diff ordinario. Archivo creado en este post-audit: `PRIVATE-PORTALS-USER-FLOW-POST-AUDIT.md`. Archivo corregido: `PRIVATE-PORTALS-USER-FLOWS.md` (una expresión documental).

**Java: 0; SQL: 0; tests: 0; migraciones: 0; seguridad: 0.** No se ejecutó Maven ni se realizó commit, push o merge.

**🟡 PRIVATE PORTALS FLOW APPROVED WITH OBSERVATIONS.** El diseño mantiene los permisos y estados reales. Las brechas de identificadores para RECEPTIONIST y de navegación para PROFESSIONAL siguen visibles como dependencias, sin convertirse en funcionalidades supuestas.
