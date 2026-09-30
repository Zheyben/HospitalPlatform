# ETAPA F.1.1 — Auditoría posterior de requisitos UX

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico.

**Fecha:** 2026-09-30.

**Rama:** `feat/hospitalplatform-ux-prototype`.

**Objeto:** [UX-REQUIREMENTS-ANALYSIS.md](UX-REQUIREMENTS-ANALYSIS.md).

## 1. Estado Git

Al inicio, `git status --short --branch` mostró la rama indicada y `?? docs/ux/`, sin cambios rastreados. El análisis F.1 ya estaba en esa carpeta como archivo nuevo sin seguimiento. Esta auditoría añade el presente informe y corrige únicamente dos clasificaciones factuales en el análisis. No se hizo commit, push ni merge.

## 2. Documentos y evidencia revisados

- Autoridad: [DOMAIN-BASELINE](../DOMAIN-BASELINE.md), en especial RF-002/003/006/007/011–019/026–030, roles, flujos y tabla API; [DOMAIN-DECISION-REGISTER](../DOMAIN-DECISION-REGISTER.md), especialmente DEC-005/006/007/008/010/017.
- Etapa C: [C.1 matriz RF–UC](../agenda/AGENDA-AVAILABILITY-RF-UC-MATRIX.md), [C.2 casos](../agenda/AGENDA-AVAILABILITY-USE-CASES.md), [C.3 contratos](../agenda/AGENDA-AVAILABILITY-CONTRACT-DESIGN.md), [C.4 especificaciones](../agenda/AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md).
- Etapa D: [D.1 UML](../agenda/uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.md), [D.2 detalles](../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [D.3.1 implementados](../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md), [D.3.2 parciales](../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md), [D.3.3 conceptuales](../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md), [D.4 catálogo](../agenda/consolidation/AGENDA-USE-CASE-CATALOG.md), [actores](../agenda/consolidation/AGENDA-ACTOR-USECASE-MATRIX.md) y [trazabilidad](../agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md).
- Exportación y contratos: [Word maestro final](../documentation-export/HOSPITALPLATFORM-COMPLETE-DOCUMENTATION.docx) abierto y examinado en lectura (182 párrafos, 19 tablas y secciones CU-D1–CU-D8); [API Specification alineada](../06-API-SPECIFICATION.md).
- Ejecución actual, solo lectura: [AppointmentController](../../apps/backend/src/main/java/com/hospital/platform/appointments/controller/AppointmentController.java), [AgendaController](../../apps/backend/src/main/java/com/hospital/platform/agenda/controller/AgendaController.java), [ProfessionalController](../../apps/backend/src/main/java/com/hospital/platform/professionals/controller/ProfessionalController.java), [SecurityConfiguration](../../apps/backend/src/main/java/com/hospital/platform/security/config/SecurityConfiguration.java), DTO y manejadores de errores citados por F.1.

## 3. Hallazgos y validación

| Nivel | Tema | Resultado y evidencia |
|---|---|---|
| **MEDIO, corregido** | RF-015 constancia | F.1 presentaba la respuesta `201` como comprobante y agrupaba RF-015 bajo `IMPLEMENTADO`. El baseline marca RF-015 `PARTIAL`: existen `AppointmentResponseDTO` y `Location`, pero no constancia UX al usuario y faltan detalles del horario del slot. Se distinguió respuesta API existente de constancia completa. |
| **MEDIO, corregido** | RF-030 consulta por rol | La fila F.1 agrupaba PATIENT/ADMIN/RECEPTIONIST con RF-006 y RF-030 bajo `IMPLEMENTADO`. RF-006 propio de PATIENT sí está implementado; RF-030 global es `PARTIAL` porque falta consulta operacional de PROFESSIONAL. Se separaron las filas y estados. |
| **Observación, sin corrección funcional** | Journey PATIENT/RECEPTIONIST | La consulta sanitizada y selección de oferta son CU-D6 `CONCEPTUAL`. Los GET de disponibilidad de Agenda exigen ADMIN. F.1 ya marcaba el corte antes de CU-D7 y la dependencia de un `slotId` conocido. DEC-007 está `CLOSED` en dirección de dominio, sin API/DTO/permiso para estos actores. |
| **Observación, sin corrección funcional** | PROFESSIONAL | El rol puede iniciar/completar atención asignada mediante Appointments, pero no tiene GET de citas autorizado. F.1 no inventa listado profesional ni gestión clínica. DEC-005 permanece `OPEN` para el ciclo User–Professional. |

**Actores:** solo PATIENT, RECEPTIONIST, ADMIN y PROFESSIONAL son personas/roles UX actuales. `SYSTEM` aparece exclusivamente como ejecutor técnico de CU-D8; `TRIAGE` no recibe operaciones. La matriz D.4 no atribuye CU-D1–CU-D8 a PROFESSIONAL, y F.1 conserva sus acciones de Appointments fuera de ese incremento.

**Flujos y pantallas:** CU-D1 y CU-D4 se identifican `PARCIAL`; CU-D5, CU-D7 y CU-D8 se mantienen `IMPLEMENTADO` en su límite; CU-D2, CU-D3 y CU-D6 permanecen `CONCEPTUAL`. El inventario de pantallas declara que son requisitos de prototipo, no UI ya implementada. Actores, objetivos, datos y acciones coinciden con los DTO, mappings y restricciones citados; no hay dashboard, módulo Specialty funcional ni pantalla de generación de slots añadidos.

**Trazabilidad:** la tabla pantalla → CU → RF → contrato/API → estado coincide con D.4 para CU-D1–CU-D8. Las operaciones de identidad y ciclo de cita fuera de esos ocho casos usan `—` en CU y los RF del baseline. Tras la corrección, RF-015 y RF-030 constan `PARCIAL` sin degradar las respuestas y GET que sí existen.

**Estados y mensajes:** la respuesta de alta de cita es `SCHEDULED`; `CONFIRMED` requiere transición separada. Los estados de presentación como cargando/vacío no se tratan como estados de dominio. Los códigos citados (`SLOT_UNAVAILABLE`, `INVALID_APPOINTMENT_TRANSITION`, etc.) tienen correspondencia en los manejadores; no se fijó microcopy final ni ventanas temporales DEC-010.

## 4. Correcciones realizadas

Se modificó solo [UX-REQUIREMENTS-ANALYSIS.md](UX-REQUIREMENTS-ANALYSIS.md): se aclaró que el `201` es respuesta técnica y RF-015 continúa `PARCIAL`; se separó RF-006 de RF-030 y se anotó la falta de consulta de PROFESSIONAL. No se añadió flujo, permiso, pantalla ejecutable ni decisión.

## 5. Validación y control de alcance

Se verificaron las rutas y `@PreAuthorize` de los tres controllers pertinentes, la autenticación general de `SecurityConfiguration`, el estado de las decisiones y la consistencia de enlaces locales Markdown. `git status` mostró únicamente `?? docs/ux/`; `git diff --check` no reportó errores en archivos rastreados. Se comprobaron explícitamente los espacios finales de ambos Markdown nuevos, que Git aún no incluye en el diff ordinario.

Cambios productivos: **Java 0; SQL 0; tests 0; migraciones 0; seguridad 0**. No se ejecutó Maven.

**Gate F.1.1: 🟡 UX REQUIREMENTS APPROVED WITH OBSERVATIONS.** El análisis corregido sirve para diseñar las vistas sustentadas por la API. El descubrimiento de disponibilidad de PATIENT/RECEPTIONIST y la navegación de cita asignada de PROFESSIONAL siguen siendo dependencias documentadas, no funcionalidades del prototipo ejecutable actual.
