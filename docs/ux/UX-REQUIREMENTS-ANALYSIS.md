# ETAPA F.1 — Análisis de requisitos UX

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico del Hospital de Huaycán.

**Incremento:** Agenda Availability + Specialty Policy.

**Fecha:** 2026-09-30.
**Naturaleza:** línea base para un prototipo; este documento no acredita una interfaz construida ni aprobación institucional.

## 1. Resumen UX del sistema

El prototipo debe comunicar con precisión las tareas que hoy respalda la API: autenticación, consulta y gestión de citas por rol, gestión básica de profesionales y horarios por ADMIN, consulta operativa de disponibilidad por ADMIN y reserva con protección contra doble asignación. `IMPLEMENTADO` califica la capacidad de backend; **ninguna pantalla se declara implementada** por esta extracción. `PARCIAL` indica un subconjunto operativo; `CONCEPTUAL` una intención documental sin contrato ejecutable; `FUTURO` trabajo fuera del alcance vigente.

La brecha principal del recorrido del paciente es la búsqueda de disponibilidad: CU-D6 tiene actores y propósito aprobados por DEC-007, pero carece de endpoint, DTO, permiso configurado y vista sanitizada. CU-D7 permite reservar un `slotId` conocido sin exigir CU-D6. El prototipo puede mostrar el recorrido deseado etiquetado por estado, pero no debe presentar la búsqueda o selección de oferta como operación actual para PATIENT o RECEPTIONIST.

El sistema actual es backend académico. El diseño visual, navegación, textos finales y accesibilidad de las pantallas requieren especificación posterior; aquí solo se extraen tareas, datos y respuestas respaldados por las fuentes.

## 2. Personas y actores

| Actor | Objetivo y necesidad UX | Acciones respaldadas hoy | Acciones no disponibles en el alcance actual |
|---|---|---|---|
| **PATIENT** | Autenticarse, consultar sus citas y gestionar una reserva propia. Debe distinguir la recepción de la reserva del estado `CONFIRMED`. | Login; `/users/me`, `/patients/me`; lista/detalle de citas propias; reserva para el paciente activo vinculado sin enviar `patientId`; confirmar, cancelar y reprogramar cita propia según estado. | Descubrir disponibilidad sanitizada mediante API; administrar agenda, profesionales o especialidades; registrar cuenta o editar `/patients/me` mediante los contratos actuales. |
| **RECEPTIONIST** | Gestionar citas de pacientes activos y llevar una cita confirmada a check-in y espera. | Login; lista/detalle de citas; crear cita con `patientId` activo explícito; confirmar, cancelar, reprogramar; check-in y paso a espera. | Consultar disponibilidad mediante vista autorizada/sanitizada; administrar usuarios, pacientes, profesionales, horarios o catálogo; iniciar/finalizar atención. |
| **ADMIN** | Mantener datos operativos autorizados y consultar disponibilidad interna. | Login; gestión básica de profesionales y horarios; lista/detalle de slots existentes; crear, consultar, confirmar, cancelar y reprogramar citas; administración de usuarios y pacientes según API vigente. | Asociación Professional–Specialty y CRUD Specialty; generar slots automáticamente; ejecutar check-in, espera, inicio o fin de atención como sustituto de los actores asignados. |
| **PROFESSIONAL** | Ejecutar la atención de una cita asignada en el módulo Appointments. | Login; iniciar atención tras `WAITING` y completar tras `IN_ATTENTION` mediante operaciones existentes de cita asignada. | Crear o consultar recursos de citas mediante los GET actuales de Appointments; administrar profesionales, horarios o disponibilidad. La obtención UX de una cita asignada para esas acciones no está resuelta por un endpoint de consulta propio. |

`SYSTEM` en CU-D8 representa una invariante técnica de concurrencia dentro de la reserva, no una persona, rol de sesión ni pantalla. `TRIAGE` no se presenta como actor operativo.

## 3. User journeys

La notación `[IMPLEMENTADO]`, `[PARCIAL]` y `[CONCEPTUAL]` se refiere al **soporte funcional/API**, no a pantallas ya construidas.

### 3.1 Paciente: recorrido solicitado y frontera actual

1. Entrada al prototipo → autenticación con email y contraseña `[IMPLEMENTADO: POST /api/v1/auth/login]`.
2. Consulta de disponibilidad para PATIENT `[CONCEPTUAL: CU-D6]`. No hay contrato para mostrar oferta sanitizada ni filtros definitivos.
3. Selección visual de una oferta `[CONCEPTUAL: depende de CU-D6]`. No afirmar que la interfaz entrega un `slotId` al paciente hoy.
4. Reserva con `slotId` conocido `[IMPLEMENTADO: CU-D7, POST /api/v1/appointments]`. El backend resuelve el paciente activo vinculado; no se envía `patientId` desde PATIENT. La fuente UX de un `slotId` seleccionable sigue pendiente.
5. Respuesta de reserva `[IMPLEMENTADO: 201, cita SCHEDULED]`. Es la respuesta técnica actual; RF-015 sigue `PARCIAL` porque no existe una constancia UX para el usuario y el DTO omite detalles del horario del slot. Tampoco es una transición a `CONFIRMED`; confirmar asistencia es una acción independiente.

Por ello, el encadenamiento completo **entrada → autenticación → disponibilidad → selección → reserva → respuesta** no está soportado de extremo a extremo para PATIENT. El prototipo funcional debe señalar el tramo conceptual; una reserva de prueba solo puede partir de un `slotId` obtenido por un medio autorizado fuera de la vista de paciente, sin fingir que ese medio es una función del producto.

### 3.2 Receptionist: gestión de cita

Entrada → autenticación `[IMPLEMENTADO]` → lista o detalle de citas `[IMPLEMENTADO]` → crear con `patientId` activo y `slotId` conocido, o confirmar/cancelar/reprogramar cita elegible `[IMPLEMENTADO]` → check-in de cita `CONFIRMED` → espera `[IMPLEMENTADO]`. La consulta de disponibilidad por este rol es `[CONCEPTUAL: CU-D6]`; no se añade búsqueda operativa, ni una pantalla de administración de pacientes. El `slotId` para alta/reprogramación es una dependencia UX no resuelta por una vista autorizada de disponibilidad.

### 3.3 Admin: operaciones de Agenda

Entrada → autenticación `[IMPLEMENTADO]` → gestión básica de profesional `[PARCIAL: CU-D1]` → creación/consulta/actualización/estado de horario `[PARCIAL: CU-D4]` → consulta operativa de slots existentes `[IMPLEMENTADO: CU-D5]`. No inferir que crear un horario genera slots, ni que la FK de `specialtyId` valida especialidad activa o asociación N:M. La reserva administrativa, cuando corresponda, es CU-D7 y requiere paciente activo explícito.

### 3.4 Professional: dependencia de navegación

El rol tiene acciones de inicio y fin de atención de cita asignada en Appointments; no participa en CU-D1–CU-D8. No se diseña aquí un listado de trabajo ni una vista de disponibilidad para este rol: la API de consulta de citas actual no le proporciona una ruta de navegación equivalente. Esta brecha debe resolverse en una etapa posterior sin inventar una pantalla operativa.

## 4. Inventario de pantallas del prototipo

Todas las filas son **requisitos de diseño**, no evidencia de UI existente. Las filas conceptuales son referencias visuales segregadas y no acciones conectables a la API actual.

| Pantalla o vista candidata | Actor | Objetivo, datos mostrados y acciones sustentadas | Soporte funcional |
|---|---|---|---|
| Acceso | Cuatro roles | Email y contraseña; enviar login; éxito con sesión o error de autenticación. El refresh y logout son contratos de sesión, no nuevos módulos de negocio. | `IMPLEMENTADO` |
| Mis citas / detalle | PATIENT | Lista limitada a citas propias; detalle del `AppointmentResponseDTO` (`id`, `patientId`, `professionalId`, `slotId`, `appointmentStatus`, `flowStage`, `reason`, fechas). Confirmar, cancelar o reprogramar solo cuando corresponda. | `IMPLEMENTADO` |
| Gestión de citas / detalle | RECEPTIONIST, ADMIN | Citas accesibles por rol; mismo DTO; alta, confirmación, cancelación y reprogramación. RECEPTIONIST además check-in y espera en estados exactos. | `IMPLEMENTADO` |
| Solicitud de reserva | PATIENT; RECEPTIONIST y ADMIN | Capturar `slotId`; `reason` opcional; `patientId` explícito solo para RECEPTIONIST/ADMIN. En PATIENT se resuelve desde la sesión. Acción crear; respuesta `SCHEDULED`. La obtención UX del slot para PATIENT/RECEPTIONIST queda sin flujo propio. | `IMPLEMENTADO` en API; journey de selección **incompleto** |
| Resultado de reserva | PATIENT, RECEPTIONIST, ADMIN | Mostrar cita creada y estado `SCHEDULED` de la respuesta `201`; enlazar al detalle si el rol puede consultarlo. Evitar titularlo «cita confirmada» en sentido de `CONFIRMED`. Una constancia UX completa no está respaldada por RF-015 actual. | `IMPLEMENTADO` para respuesta API; `PARCIAL` RF-015 |
| Profesionales: lista, detalle, alta, edición básica | ADMIN | `id`, `userId`, `licenseNumber`, `active`; alta con licencia obligatoria y `userId` opcional; actualización de licencia. Sin desactivación por HTTP ni asociación de especialidades. | `PARCIAL` — CU-D1 |
| Horarios: lista, detalle, alta, edición, estado | ADMIN | `id`, `professionalId`, `specialtyId`, `dayOfWeek`, `startTime`, `endTime`, `active`; filtros de lista por profesional/especialidad; cambio `active`. | `PARCIAL` — CU-D4 |
| Disponibilidad operativa: lista/detalle | ADMIN | Slots existentes: `id`, `scheduleId`, `slotDate`, `startTime`, `endTime`, `status`, `usable`; filtros opcionales `scheduleId`, `professionalId`, `slotDate`, `status`; consulta de detalle. Puede listar `AVAILABLE`, `RESERVED` y `BLOCKED`; `usable` es derivado. | `IMPLEMENTADO` — CU-D5 |
| Disponibilidad sanitizada y selección | PATIENT, RECEPTIONIST | Objetivo de descubrir oferta reservable sin datos internos. Campos, filtros y navegación no definidos; ningún control de consulta/reserva enlazado aquí se presenta como vigente. | `CONCEPTUAL` — CU-D6 |
| Asociación Professional–Specialty y catálogo | ADMIN conceptual | Objetivos CU-D2/CU-D3 únicamente; sin formularios ejecutables, CRUD, campos adicionales ni controles de asociación definitivos. | `CONCEPTUAL` |

El perfil propio `/users/me` y `/patients/me` son datos consultables por el rol correspondiente; una pantalla separada de edición de perfil no está respaldada. Las acciones de PROFESSIONAL tienen contratos de mutación, pero el acceso UX a una cita asignada es una dependencia pendiente, por lo que no se enumera una pantalla ejecutable de trabajo profesional.

## 5. Estados UX visibles

`loading`, vacío y error son **estados de presentación transitorios**, no estados persistidos de dominio ni nuevas reglas. Solo deben mostrarse al invocar una operación real del rol.

| Contexto | Tratamiento UX respaldado |
|---|---|
| Cargando | Indicador mientras espera login, lista, detalle o mutación existente; bloquear reenvío accidental sin prometer idempotencia de alta. |
| Vacío | Lista sin resultados para consultas habilitadas, por ejemplo citas del rol, profesionales, horarios o slots ADMIN; no interpretar «vacío» como generación de oferta. |
| Error de validación | Mostrar que la solicitud fue rechazada y conservar datos editables; usar errores reales `400`, sin inventar mensajes literales del backend. |
| Error de autorización/autenticación | `401` o `403` conforme al contrato; no ofrecer acción restringida como opción habilitada. |
| Conflicto/no disponible | `409` de reserva, como `SLOT_UNAVAILABLE`, significa que no se obtuvo esa reserva; no mostrar comprobante ni asignación provisional. |
| No encontrado | `404` para recurso consultado inexistente o no disponible conforme a la respuesta actual. |
| Reserva creada | `201` y cita `SCHEDULED`; la confirmación de asistencia `CONFIRMED` solo aparece tras la operación de confirmar. |
| Cita confirmada, cancelada, reprogramada o completada | Reflejar `appointmentStatus` recibido (`CONFIRMED`, `CANCELLED`, `RESCHEDULED`, `COMPLETED`) y `flowStage` cuando exista (`CHECK_IN`, `WAITING`, `IN_ATTENTION`, `FINISHED`); las acciones disponibles dependen del rol y transición soportada. |
| Slot consultado | Reflejar `status` (`AVAILABLE`, `RESERVED`, `BLOCKED`) y `usable` devueltos por ADMIN; no tratar `AVAILABLE` por sí solo como garantía de reserva. |

No se incorporan estados `NO_SHOW`, espera de aprobación, prioridad ni ventanas temporales. La doble reserva CU-D8 se manifiesta al usuario como éxito para una solicitud y conflicto para la otra, no como pantalla técnica.

## 6. Validaciones UX

| Operación | Validación respaldada | Respuesta UX ante fallo |
|---|---|---|
| Login | `email` y `password` obligatorios; email con formato validado. Cuenta activa y credenciales válidas se comprueban en backend. | Mostrar fallo de campos `400` o autenticación `401`; no atribuir una causa específica si la respuesta no la identifica. |
| Alta profesional | `licenseNumber` no vacío, máximo 100 caracteres; `userId` opcional. Duplicado de licencia puede devolver `409 DUPLICATE_PROFESSIONAL`. | Corregir licencia o mostrar conflicto; no sugerir creación de usuario automática. |
| Edición profesional | Solo cambio de `licenseNumber` en el PUT vigente. | `404 PROFESSIONAL_NOT_FOUND` o validación/conflicto según respuesta. |
| Alta/edición horario | `professionalId`, `specialtyId`, `dayOfWeek` 0–6, `startTime` y `endTime` obligatorios; fin posterior al inicio; profesional activo. | Reflejar `VALIDATION_ERROR`, `INVALID_SCHEDULE_TIME`, `PROFESSIONAL_NOT_AVAILABLE` o `INVALID_SPECIALTY_REFERENCE` si el backend los devuelve. Existencia FK de especialidad no equivale a actividad ni asociación. |
| Cambio de estado horario | `active` booleano en contrato de estado. | Mostrar resultado devuelto, sin asociarlo a generación/cancelación automática de slots. |
| Reserva | `slotId` obligatorio; `reason` opcional; PATIENT no envía `patientId`; RECEPTIONIST/ADMIN lo proporcionan para paciente activo. El slot debe seguir siendo usable al reservar. | Ante `SLOT_UNAVAILABLE`/`409`, no confirmar ni conservar una reserva supuesta; permitir nueva decisión solo si se dispone de una fuente autorizada de slot. |
| Reprogramación | Nuevo `slotId` obligatorio; elegibilidad de cita, ausencia de sucesor directo y disponibilidad del nuevo slot se comprueban en backend. | Reflejar rechazo/rollback sin mostrar sucesor ficticio. |
| Confirmar, cancelar, check-in, espera, iniciar y completar | Sin cuerpo para transiciones indicadas; rol, titularidad/asignación y estados exactos verificados por backend. | Reflejar `403`, `404` o `409 INVALID_APPOINTMENT_TRANSITION` según operación; no habilitar secuencias incompatibles. |

Los textos definitivos de microcopy, formato local de fecha/hora y política de zona horaria no quedan decididos por esta extracción. DEC-010 permanece `OPEN`.

## 7. Dependencias UX y trazabilidad

La cadena es **pantalla → caso de uso → RF → contrato/API existente**. `—` indica que la operación procede del baseline de Appointments o identidad y no tiene un CU-D1–D8 propio; no crea un caso nuevo. Todas las rutas siguientes usan el prefijo `/api/v1`.

| Pantalla / tarea | CU | RF del baseline | Contrato vigente o ausencia de contrato | Estado |
|---|---|---|---|---|
| Acceso / sesión | — | RF-002, RF-003 | `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout` | `IMPLEMENTADO` |
| Mis citas de PATIENT | — | RF-006 | `GET /appointments`, `GET /appointments/{id}` | `IMPLEMENTADO` para PATIENT propio |
| Consulta operativa de citas | — | RF-030 para RECEPTIONIST; consulta ADMIN vigente | `GET /appointments`, `GET /appointments/{id}` | GET `IMPLEMENTADO` para RECEPTIONIST/ADMIN; RF-030 global `PARCIAL` porque falta consulta de PROFESSIONAL |
| Solicitud de reserva | CU-D7 | RF-013 | `POST /appointments` | `IMPLEMENTADO`; selección UX previa pendiente para PATIENT/RECEPTIONIST |
| Respuesta de reserva | CU-D7 | RF-015 | `201`, `AppointmentResponseDTO` y `Location` desde `POST /appointments` | `PARCIAL` como constancia al usuario; respuesta API existente |
| Protección de doble asignación | CU-D8 | RF-014 | Sin endpoint separado; opera dentro de `POST /appointments` | `IMPLEMENTADO` técnico; 2 solicitudes probadas, 20 del SRS pendientes |
| Confirmar / cancelar / reprogramar | — | RF-016, RF-017, RF-018, RF-019 | `POST /appointments/{id}/confirm`, `/cancel`, `/reschedule` | `IMPLEMENTADO` según rol/estado |
| Check-in / espera | — | RF-026, RF-027 | `POST /appointments/{id}/check-in`, `/waiting` | `IMPLEMENTADO` RECEPTIONIST |
| Iniciar / completar atención | — | RF-028, RF-029 | `POST /appointments/{id}/start-attention`, `/complete` | `IMPLEMENTADO` PROFESSIONAL asignado; navegación UX pendiente |
| Profesionales | CU-D1 | RF-007 | `POST/GET /professionals`, `GET/PUT /professionals/{id}` | `PARCIAL` |
| Horarios | CU-D4 | RF-011 | `POST/GET /agendas`, `GET/PUT /agendas/{id}`, `PATCH /agendas/{id}/status` | `PARCIAL` |
| Disponibilidad operativa | CU-D5 | RF-012, porción ADMIN | `GET /availability`, `GET /availability/{id}` | `IMPLEMENTADO` ADMIN |
| Oferta sanitizada / selección | CU-D6 | RF-012, porción PATIENT/RECEPTIONIST | **Sin API/DTO/permiso actual** | `CONCEPTUAL` |
| Asociación Professional–Specialty | CU-D2 | RF-007, porción asociación | **Sin API actual** | `CONCEPTUAL` |
| Specialty Catalog | CU-D3 | RF-008 | **Sin API CRUD actual** | `CONCEPTUAL` |

Las vistas de cita no deben heredar el permiso de disponibilidad ADMIN. RF-012 completo no es `IMPLEMENTADO` porque CU-D6 carece de contrato. RF-015 y RF-030 permanecen `PARCIAL` pese a las respuestas y consultas existentes. CU-D8 demuestra la exclusión mutua con dos solicitudes; no cumple aún el ensayo de veinte solicitudes de la SRS.

## 8. Exclusiones y decisiones abiertas

- Generación automática de slots, algoritmo de duración, ventanas/calendario y reglas temporales (`DEC-008`, `DEC-010`, `OPEN`).
- Notificaciones, waitlist, ofertas de cupos y prioridad ambulatoria (`FUTURO` o pendiente según registro de decisiones).
- Autoservicio futuro de registro/actualización de paciente (`DEC-017`, `OPEN`), portal público adicional y módulos no implementados.
- Alta o gestión de Specialty Catalog y asociación N:M por UI/API: CU-D2/CU-D3 son `CONCEPTUAL`; una tabla SQL no constituye funcionalidad. `DEC-005` sobre vínculo User–Professional continúa `OPEN`.
- Disponibilidad sanitizada y selección para PATIENT/RECEPTIONIST como flujo ejecutable; CU-D6 es `CONCEPTUAL`, aunque DEC-007 cierre la dirección de dominio.
- Permisos, endpoints, DTO, estados de aprobación, sincronización automática o reglas de accesibilidad no respaldadas por el contrato actual.

## Fuentes y criterio de lectura

Fuentes principales: [documento maestro Word](../documentation-export/HOSPITALPLATFORM-COMPLETE-DOCUMENTATION.docx), [Domain Baseline](../DOMAIN-BASELINE.md), [Decision Register](../DOMAIN-DECISION-REGISTER.md), [D.2 fichas](../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [D.3.1 implementados](../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md), [D.3.2 parciales](../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md), [D.3.3 conceptuales](../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md), [D.4 catálogo](../agenda/consolidation/AGENDA-USE-CASE-CATALOG.md), [D.4 matriz actor–UC](../agenda/consolidation/AGENDA-ACTOR-USECASE-MATRIX.md), [D.4 trazabilidad](../agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md), [API Specification alineada](../06-API-SPECIFICATION.md) y [diseño C.3 de contratos](../agenda/AGENDA-AVAILABILITY-CONTRACT-DESIGN.md). Se contrastaron rutas/DTO y autorizaciones con el backend actual en lectura. Ante una propuesta documental, prevalece la delimitación de API/código vigente y el estado explícito de la decisión.

## Validación de alcance

Documento creado para F.1: `docs/ux/UX-REQUIREMENTS-ANALYSIS.md`. En la rama `feat/hospitalplatform-ux-prototype`, `git status` muestra únicamente `?? docs/ux/`; `git diff --check` no reporta errores sobre archivos rastreados y la inspección del Markdown nuevo no detecta espacios finales ni enlaces locales rotos. Java modificado: **0**; SQL modificado: **0**; tests modificados: **0**; migraciones modificadas: **0**; seguridad modificada: **0**. No se ejecutó Maven.

**Gate F.1: 🟡 UX REQUIREMENTS BASELINE WITH OBSERVATIONS.** Se puede iniciar el diseño de las vistas respaldadas por API. El journey completo de descubrimiento y selección de disponibilidad para PATIENT/RECEPTIONIST, y la navegación de citas asignadas para PROFESSIONAL, requieren contratos/decisiones posteriores antes de presentarse como interacción ejecutable.
