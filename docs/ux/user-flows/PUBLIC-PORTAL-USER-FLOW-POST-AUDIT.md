# ETAPA F.2.1 — Auditoría posterior del flujo del portal público

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico.

**Fecha:** 2026-09-30.

**Rama:** `feat/hospitalplatform-user-flows`.

**Archivo auditado:** [PUBLIC-PORTAL-USER-FLOWS.md](PUBLIC-PORTAL-USER-FLOWS.md).

## 1. Estado Git

Al iniciar la auditoría, `git status --short --branch` confirmó la rama indicada y mostró únicamente `?? docs/ux/user-flows/`. El flujo F.2.1 estaba en esa carpeta como documento nuevo sin seguimiento. Esta etapa crea solo el presente informe; no modifica el flujo base ni archivos productivos.

## 2. Fuentes contrastadas

- [UX Requirements Analysis](../UX-REQUIREMENTS-ANALYSIS.md) y [su post-audit](../UX-REQUIREMENTS-POST-AUDIT.md): corte entre capacidad de backend, pantalla candidata y brechas de experiencia; RF-015 y RF-030 parciales.
- [Domain Baseline](../../DOMAIN-BASELINE.md): RF-001/002/005/006/010/012–015, flujos de autenticación/reserva y API vigente; [Decision Register](../../DOMAIN-DECISION-REGISTER.md): DEC-007 `CLOSED` para actores/exposición conceptual, DEC-017 `OPEN` para autorregistro, DEC-008/010 `OPEN`.
- Agenda C.1–C.4: [matriz RF–UC](../../agenda/AGENDA-AVAILABILITY-RF-UC-MATRIX.md), [casos](../../agenda/AGENDA-AVAILABILITY-USE-CASES.md), [contratos](../../agenda/AGENDA-AVAILABILITY-CONTRACT-DESIGN.md) y [especificaciones](../../agenda/AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md).
- Agenda D.2 y D.3: [detalles](../../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [escenarios implementados](../../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md), [parciales](../../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md), [conceptuales](../../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md), y [trazabilidad D.4](../../agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md).
- Contrato y seguridad actuales, leídos sin cambios: [AuthController](../../../apps/backend/src/main/java/com/hospital/platform/auth/controller/AuthController.java), [PatientController](../../../apps/backend/src/main/java/com/hospital/platform/patients/controller/PatientController.java), [AgendaController](../../../apps/backend/src/main/java/com/hospital/platform/agenda/controller/AgendaController.java), [AppointmentController](../../../apps/backend/src/main/java/com/hospital/platform/appointments/controller/AppointmentController.java), [SecurityConfiguration](../../../apps/backend/src/main/java/com/hospital/platform/security/config/SecurityConfiguration.java), `LoginRequestDTO`, `CreateAppointmentRequestDTO` y `AppointmentResponseDTO`.

## 3. Validación del usuario existente

**Aprobada con límite explícito.** El login usa los campos reales `email` y `password`; `POST /api/v1/auth/login` es público y valida una cuenta existente habilitada. Para reservar como PATIENT se requiere perfil de paciente activo vinculado; el cuerpo actual incluye `slotId` obligatorio, `reason` opcional y no debe enviar otro `patientId`. `POST /api/v1/appointments` crea una cita `SCHEDULED` cuando el slot es usable. El documento no añade un paso API obligatorio entre login y reserva: identifica la selección de disponibilidad como brecha conceptual y no afirma un portal ejecutable de extremo a extremo.

## 4. Validación del usuario nuevo

**Aprobada como recorrido condicional.** RF-001 está `OPEN` y DEC-017 sigue `OPEN`. No existe `POST /api/v1/auth/register`; la creación y vinculación actuales de User/Patient son administrativas. El formulario del flujo no fija campos, reglas de identidad, consentimiento ni estados de aprobación. El login posterior se marca como operación independiente y no como prueba de autorregistro. RF-010 conserva su condición de portal no implementado.

## 5. Reserva, disponibilidad y estados

| Punto auditado | Resultado |
|---|---|
| `SCHEDULED` frente a `CONFIRMED` | Correcto: `201` de reserva devuelve `SCHEDULED` y `flowStage=null`; `CONFIRMED` requiere la operación de confirmación posterior. «Confirmar envío» no se usa como transición de estado. |
| Disponibilidad PATIENT | Correcto: CU-D6 está `CONCEPTUAL`; DEC-007 aprueba actores y exposición sanitizada, sin URI/DTO/filtros/permiso actual. Los GET `/api/v1/availability` y `/{id}` exigen ADMIN. |
| Doble reserva | Correcto: CU-D8 es invariante interna de CU-D7, sin pantalla ni endpoint propio. El conflicto no crea una cita parcial. |
| Constancia | Correcto: RF-015 permanece `PARCIAL`; `AppointmentResponseDTO` y `Location` existen, pero el documento no declara una constancia UX final construida. |
| Estados UX | Vacío, cargando y error se definen como presentación, no estados de dominio. «Pendiente de definición» es etiqueta documental, no estado de cuenta o cita. No aparecen `NO_SHOW` ni aprobación inventada. |

## 6. Pantallas y trazabilidad

El inventario informa para cada vista candidata nombre, actor, objetivo, datos/acciones y estado funcional. «Entrada del portal» y «registro» no se declaran activos; login, envío de reserva y consulta de citas propias se califican por su **backend**, sin afirmar UI implementada. No hay dashboard, módulo clínico, catálogo o calendario generado. El actor principal es PATIENT; autenticación y HospitalPlatform se describen como componentes técnicos, no roles nuevos.

La matriz mantiene **pantalla → CU → RF → estado**: CU-D6/RF-012 conceptual, CU-D7/RF-013 implementado en su límite, CU-D8/RF-014 interno implementado, RF-015 parcial, RF-001/DEC-017 abiertos y RF-010 no implementado. Login RF-002 y consulta propia RF-006 usan `—` porque no son CU-D1–CU-D8. No se detectaron enlaces locales rotos en el documento auditado.

## 7. Hallazgos y correcciones

**Críticos: 0. Medios: 0. Bajos: 0. Correcciones al flujo base: 0.**

**Observación 1:** un prototipo que conecte registro → búsqueda → reserva necesita decisiones y contratos posteriores para RF-001/CU-D6, además del frontend RF-010. El flujo F.2.1 lo declara sin presentarlo como capacidad actual.

**Observación 2:** la respuesta `201` permite mostrar el estado de la cita, pero no completa RF-015 como constancia de usuario. El documento conserva esa diferencia.

## 8. Validación final y gate

Se ejecutaron `git status` y `git diff --check`. La rama continúa `feat/hospitalplatform-user-flows`; el único directorio de trabajo sin seguimiento es `docs/ux/user-flows/`. `git diff --check` no informó errores sobre archivos rastreados. La revisión directa de los dos Markdown nuevos no encontró espacios finales ni enlaces locales rotos; esos archivos aún no entran en el diff ordinario.

Archivos creados en F.2.1 post-audit: `PUBLIC-PORTAL-USER-FLOW-POST-AUDIT.md`. Archivos existentes modificados por esta auditoría: **0**. **Java: 0; SQL: 0; tests: 0; migraciones: 0; seguridad: 0**. No se ejecutó Maven ni se hizo commit, push o merge.

**🟡 PUBLIC PORTAL FLOW APPROVED WITH OBSERVATIONS.** El diseño documental es consistente con el dominio y los contratos actuales. El portal, el autorregistro y el descubrimiento de disponibilidad del paciente permanecen fuera del flujo ejecutable hasta contar con las decisiones y la implementación pertinentes.
