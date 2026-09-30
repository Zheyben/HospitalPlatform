# ETAPA F.2.2 — Flujos UX de portales privados

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico del Hospital de Huaycán.

**Fecha:** 2026-09-30.

**Rama:** `feat/hospitalplatform-user-flows`.

## 1. Objetivo de los portales privados

Describir recorridos de prototipo para `ADMIN`, `RECEPTIONIST` y `PROFESSIONAL` según los contratos actuales. `IMPLEMENTADO` califica una operación de backend; **no acredita una pantalla o portal ya construido**. `PARCIAL` indica un caso o requisito con una parte operativa; `CONCEPTUAL` una intención de dominio sin operación completa; `FUTURO` trabajo fuera del alcance actual. La navegación «panel» o «módulo» de las tablas siguientes es organización UX candidata, no un dashboard, recurso API ni permiso nuevo.

Las operaciones actuales de ADMIN son gestión básica de profesionales y horarios, y consulta interna de slots existentes. RECEPTIONIST consulta y gestiona citas, con check-in y espera. PROFESSIONAL tiene transiciones operativas de una cita asignada, pero no cuenta con un GET de citas autorizado para construir un recorrido de navegación propio. Ninguna vista descrita representa atención clínica, historia médica o aprobación institucional del hospital.

## 2. Mapa general de actores

| Actor | Papel en el alcance UX | Límite de autorización y estado |
|---|---|---|
| **ADMIN** | Configura perfiles profesionales y horarios básicos; lee disponibilidad operativa existente. Puede gestionar citas por contratos actuales, aunque no se amplía aquí un módulo administrativo completo. | CU-D1 y CU-D4 `PARCIAL`; CU-D5 `IMPLEMENTADO` solo para ADMIN. No puede ejecutar check-in, espera, inicio ni fin de atención mediante los endpoints de esos actores. |
| **RECEPTIONIST** | Consulta y opera citas de pacientes activos; registra check-in y espera. | Listado/detalle y transiciones de Appointments `IMPLEMENTADO` en su límite. RF-030 global `PARCIAL` porque falta consulta para PROFESSIONAL. Disponibilidad sanitizada CU-D6 solo `CONCEPTUAL`; no accede a GET de Agenda. |
| **PROFESSIONAL** | Rol real que inicia/finaliza estados operativos de una cita asignada. | Las mutaciones de Appointments existen, pero no hay consulta de citas por este rol ni flujo UX completo. No participa en CU-D1–CU-D8. Su navegación de portal es `NO IMPLEMENTADO`; no se crea dashboard. |

`SYSTEM` de CU-D8 es ejecutor técnico interno, no actor de portal. La autenticación usa el login compartido; «login administrativo» significa usuario autenticado con rol ADMIN, no una ruta de acceso distinta.

## 3. ADMIN USER FLOW

### Journey y límite funcional

**Entrada → login → área operativa → profesionales → horarios → disponibilidad.** La secuencia ordena temas para revisión del prototipo; el backend no obliga a crear profesional ni horario antes de consultar slots existentes. Tampoco genera slots al crear un horario.

| Paso / pantalla candidata | Objetivo | Datos mostrados | Acción ADMIN y respuesta | Estado funcional |
|---|---|---|---|---|
| 1. Acceso | Autenticarse con una cuenta ADMIN existente. | Resultado de sesión; no hay pantalla ya acreditada. | Enviar `email` y `password` a `POST /api/v1/auth/login`; credenciales o cuenta inválidas se rechazan. | Login backend `IMPLEMENTADO` — RF-002 |
| 2. Área operativa | Acceder a las tareas autorizadas. | Enlaces UX candidatos a profesionales, horarios y disponibilidad; sin métricas ni reportes. | Navegar según rol; cada API mantiene su control `ADMIN`. | Navegación UX `NO IMPLEMENTADA`; operaciones subyacentes delimitadas abajo |
| 3. Profesionales: lista/detalle/alta/edición básica | Consultar o mantener perfil profesional. | `id`, `userId`, `licenseNumber`, `active` del `ProfessionalResponseDTO`. | `GET/POST /api/v1/professionals`, `GET/PUT /api/v1/professionals/{id}`. Alta: licencia obligatoria y `userId` opcional; PUT cambia solo licencia. | CU-D1 / RF-007 `PARCIAL` |
| 4. Horarios: lista/detalle/alta/edición/estado | Configurar un Schedule de profesional activo. | `id`, `professionalId`, `specialtyId`, `dayOfWeek`, `startTime`, `endTime`, `active` del `AgendaResponseDTO`. | `GET/POST /api/v1/agendas`, `GET/PUT /api/v1/agendas/{id}`, `PATCH /api/v1/agendas/{id}/status`; lista filtrable por `professionalId`/`specialtyId`. | CU-D4 / RF-011 `PARCIAL` |
| 5. Disponibilidad operativa: lista/detalle | Leer slots existentes y su usabilidad derivada. | `id`, `scheduleId`, `slotDate`, `startTime`, `endTime`, `status`, `usable` del `AvailabilitySlotResponseDTO`; pueden aparecer `AVAILABLE`, `RESERVED`, `BLOCKED`. | `GET /api/v1/availability` con filtros opcionales `scheduleId`, `professionalId`, `slotDate`, `status`; `GET /api/v1/availability/{id}`. | CU-D5 `IMPLEMENTADO` para ADMIN; RF-012 global `PARCIAL` |

**Dependencias y límites:** el alta/edición de Schedule requiere `professionalId` activo y `specialtyId` existente por FK; no hay consulta funcional de Specialty Catalog ni validación actual de specialty activa/asociada. El identificador de especialidad necesario para el formulario no justifica inventar selector de catálogo. La asociación Professional–Specialty CU-D2 y gestión de Specialty Catalog CU-D3 son `CONCEPTUAL`; DEC-006 cierra ownership/cardinalidad, no API. DEC-005 permanece `OPEN` sobre ciclo User–Professional. DEC-008/010 siguen `OPEN`; no se propone generación, duración o ventana horaria.

**Estados y errores relevantes:** lista vacía si no hay registros; carga transitoria al consultar; `404` en detalle inexistente; `400` por campos/rango horario inválido o FK de specialty no válida; `409` por licencia duplicada. Las respuestas `active` de Professional/Schedule son datos reales, pero no se infiere efecto automático sobre slots o citas al cambiar el Schedule.

**Trazabilidad ADMIN:** profesionales → CU-D1 → RF-007 → `PARCIAL`; horarios → CU-D4 → RF-011 → `PARCIAL`; disponibilidad → CU-D5 → RF-012 (porción ADMIN) → `IMPLEMENTADO`. El área operativa y login no crean CU-D adicional.

## 4. RECEPTIONIST USER FLOW

### Journey y límite funcional

**Entrada → login → módulo de citas → lista/detalle permitidos → operaciones de cita.** «Consultar información disponible» significa consultar citas accesibles por su rol. No significa consultar `/api/v1/availability`, que actualmente exige ADMIN.

| Paso / pantalla candidata | Objetivo | Datos mostrados | Acción RECEPTIONIST y respuesta | Estado funcional |
|---|---|---|---|---|
| 1. Acceso | Autenticarse con cuenta RECEPTIONIST existente. | Resultado de sesión. | `POST /api/v1/auth/login` con `email` y `password`. | Login backend `IMPLEMENTADO` — RF-002 |
| 2. Entrada a citas | Orientar al trabajo permitido. | Enlaces UX candidatos a lista, detalle y acciones de citas; no hay dashboard operativo. | Navegar a las consultas de Appointments autorizadas. | Navegación UX `NO IMPLEMENTADA`; consulta API vigente |
| 3. Lista/detalle de citas | Revisar datos operativos accesibles. | `AppointmentResponseDTO`: `id`, `patientId`, `professionalId`, `slotId`, `appointmentStatus`, `flowStage`, `reason` y fechas devueltas. | `GET /api/v1/appointments`, `GET /api/v1/appointments/{id}`. | GET `IMPLEMENTADO`; RF-030 global `PARCIAL` |
| 4. Crear cita para paciente activo | Registrar una reserva con identificadores conocidos. | Datos de solicitud: `patientId` explícito, `slotId` obligatorio, `reason` opcional; respuesta `SCHEDULED`. | `POST /api/v1/appointments` revalida y reserva el slot. El rol no tiene catálogo de pacientes ni consulta sanitizada de disponibilidad para obtener esos identificadores por UI. | CU-D7 `IMPLEMENTADO` en backend; selección UX pendiente; CU-D8 interno |
| 5. Confirmar, cancelar o reprogramar | Cambiar una cita elegible según estado actual. | Estado de cita y, para reprogramar, nuevo `slotId` conocido. | `POST /api/v1/appointments/{id}/confirm`, `/cancel`, `/reschedule`; la reprogramación crea sucesora `SCHEDULED` si procede. | Operaciones Appointments `IMPLEMENTADO`; temporalidad DEC-010 `OPEN` |
| 6. Check-in y espera | Registrar llegada y avance operacional. | `appointmentStatus=CONFIRMED`; `flowStage` actual (`null`, `CHECK_IN`, `WAITING`). | `POST /api/v1/appointments/{id}/check-in` solo desde `CONFIRMED/null`; `/waiting` desde `CONFIRMED/CHECK_IN`. | RF-026 y RF-027 `IMPLEMENTADO` |

La disponibilidad para RECEPTIONIST está aprobada **conceptualmente** por DEC-007 como futura vista sanitizada CU-D6, sin endpoint, DTO, filtros ni permiso configurado. No se muestra el GET operativo de ADMIN como fuente de selección. La creación y reprogramación requieren un `slotId`; obtenerlo mediante una experiencia autorizada es una dependencia UX pendiente. El rol no administra pacientes, profesionales ni horarios y no ejecuta inicio o fin de atención.

**Estados y errores relevantes:** carga/vacío de lista; `401` sin sesión, `403` sin rol autorizado; `404` de cita inexistente; `409` cuando el slot deja de ser usable o una transición no es válida. Un alta exitosa queda `SCHEDULED`, mientras que check-in requiere `CONFIRMED` previo. No se crea aprobación, notificación ni cambio clínico.

**Trazabilidad RECEPTIONIST:** lista/detalle → sin CU-D1–D8 → RF-030 (porción recepción) → GET `IMPLEMENTADO`, RF global `PARCIAL`; alta → CU-D7 → RF-013 → `IMPLEMENTADO`; exclusión técnica → CU-D8 → RF-014 → `IMPLEMENTADO`; confirmar/cancelar/reprogramar → sin CU-D del incremento → RF-016/017/019 → `IMPLEMENTADO` en su límite; check-in/espera → RF-026/027 → `IMPLEMENTADO`; consulta sanitizada → CU-D6 → RF-012 → `CONCEPTUAL`.

## 5. PROFESSIONAL SCOPE

`PROFESSIONAL` es un rol actual, pero no actor de CU-D1–CU-D8. El contrato de Appointments permite a un profesional activo y asignado usar `POST /api/v1/appointments/{id}/start-attention` tras `WAITING` y `POST /api/v1/appointments/{id}/complete` tras `IN_ATTENTION`. Son cambios de **estado operacional de cita** (`IN_ATTENTION`; después `FINISHED` y cita `COMPLETED`), no documentación clínica ni una interfaz de atención médica.

Los GET de citas de Appointments autorizan PATIENT, ADMIN y RECEPTIONIST, **no PROFESSIONAL**. Por eso no se diseña lista de pacientes asignados, agenda personal, dashboard ni secuencia UX ejecutable para encontrar el `id` de una cita. Un prototipo puede indicar la existencia del rol y esta dependencia, pero no representar una pantalla funcional de trabajo profesional. El flujo de portal profesional es **CONCEPTUAL / NO IMPLEMENTADO** como experiencia; RF-028/029 son `IMPLEMENTADO` solamente como mutaciones de backend, y RF-030 permanece `PARCIAL`. DEC-005 sigue `OPEN` para el ciclo de vínculo User–Professional; no se infiere autoservicio.

## 6. Inventario consolidado de pantallas candidatas

Cada fila describe un requisito de diseño; ninguna declara UI existente.

| Pantalla | Actor | Objetivo | Datos | Acciones | Estado |
|---|---|---|---|---|---|
| Acceso | ADMIN, RECEPTIONIST, PROFESSIONAL | Autenticar cuenta existente. | `email`, `password`; respuesta de sesión. | Enviar login. | Backend `IMPLEMENTADO`; pantalla no construida |
| Área operativa ADMIN | ADMIN | Orientar a profesionales, horarios y disponibilidad. | Navegación sin métricas ni reportes. | Abrir vistas autorizadas. | Navegación UX `NO IMPLEMENTADA` |
| Profesionales | ADMIN | Consultar/crear/editar perfil básico. | `id`, `userId`, `licenseNumber`, `active`. | GET, POST, PUT licencia. | CU-D1 `PARCIAL` |
| Horarios | ADMIN | Mantener Schedule básico. | `id`, `professionalId`, `specialtyId`, día, rango, `active`. | GET, POST, PUT, PATCH estado. | CU-D4 `PARCIAL` |
| Disponibilidad operativa | ADMIN | Consultar slots existentes. | Slot, horario/fecha, `status`, `usable`. | GET lista/detalle con filtros existentes. | CU-D5 `IMPLEMENTADO` |
| Entrada a citas | RECEPTIONIST | Orientar hacia operaciones permitidas. | Navegación sin dashboard. | Abrir lista/detalle. | Navegación UX `NO IMPLEMENTADA` |
| Citas: lista/detalle | RECEPTIONIST | Consultar citas accesibles. | `AppointmentResponseDTO`. | GET lista/detalle; iniciar acciones autorizadas. | GET `IMPLEMENTADO`; RF-030 global `PARCIAL` |
| Cita: alta/gestión | RECEPTIONIST | Crear o cambiar cita elegible. | `patientId`, `slotId`, `reason`, estado y etapa de respuesta. | Crear, confirmar, cancelar, reprogramar, check-in, espera según estado. | API `IMPLEMENTADO`; descubrimiento de `slotId` pendiente |
| Portal profesional | PROFESSIONAL | Solo registrar brecha de navegación. | Sin vista de citas asignadas respaldada por GET. | Ninguna acción UX conectable en esta etapa. | UX `CONCEPTUAL / NO IMPLEMENTADO`; mutaciones RF-028/029 existentes |

## 7. Estados UX visibles

| Estado de presentación | Uso permitido y límite |
|---|---|
| **Loading / cargando** | Espera de login, lista, detalle o mutación existente. Es transitorio de UI, no estado de negocio. |
| **Vacío** | Lista GET sin registros para ADMIN o RECEPTIONIST; no implica que se generen slots ni que exista una búsqueda profesional. |
| **Error** | Rechazo real `400`, `404` o `409` según operación; conservar los datos que el usuario pueda corregir. No fijar microcopy backend inexistente. |
| **Éxito** | Reflejar el DTO devuelto: profesional/horario actualizado, slots leídos, cita `SCHEDULED`/`CONFIRMED` o etapa `CHECK_IN`/`WAITING` cuando la transición procede. |
| **No autorizado** | `401` para falta de autenticación y `403` para rol/propiedad insuficiente. No ofrecer como habilitada la consulta de disponibilidad a RECEPTIONIST ni GET de citas a PROFESSIONAL. |

`AVAILABLE`, `RESERVED` y `BLOCKED` son estados de slot existentes; `SCHEDULED`, `CONFIRMED`, `CANCELLED`, `RESCHEDULED`, `COMPLETED` y `flowStage` son datos del dominio, no nuevos estados UX. No se añaden `NO_SHOW`, ventanas de tiempo, aprobación ni espera de notificación.

## 8. Trazabilidad pantalla → caso de uso → RF → estado

`—` indica que la función existe en Auth/Appointments fuera de CU-D1–D8; no se crea un caso local nuevo. Las rutas actuales citadas arriba comparten el prefijo `/api/v1`.

| Pantalla / tarea | CU | RF | Estado exacto |
|---|---|---|---|
| Acceso de roles internos | — | RF-002 | Login backend `IMPLEMENTADO`; portal UX no construido |
| Área operativa ADMIN | — | — | Organización UX candidata, sin dashboard/API propio |
| Profesionales ADMIN | CU-D1 | RF-007 | `PARCIAL`; alta, consulta y actualización básicas, sin N:M ni desactivación HTTP |
| Asociación Professional–Specialty | CU-D2 | RF-007 | `CONCEPTUAL`; sin pantalla funcional |
| Specialty Catalog | CU-D3 | RF-008 | `CONCEPTUAL`; sin pantalla funcional |
| Horarios ADMIN | CU-D4 | RF-011 | `PARCIAL`; no genera slots |
| Disponibilidad ADMIN | CU-D5 | RF-012, porción ADMIN | `IMPLEMENTADO`; RF-012 global `PARCIAL` |
| Entrada a citas RECEPTIONIST | — | — | Organización UX candidata; sin API de dashboard |
| Citas lista/detalle RECEPTIONIST | — | RF-030, porción recepción | GET `IMPLEMENTADO`; RF-030 global `PARCIAL` |
| Cita alta RECEPTIONIST | CU-D7; CU-D8 interno | RF-013/014 | Reserva `IMPLEMENTADO`; selección UX de slot pendiente |
| Confirmar/cancelar/reprogramar | — | RF-016/017/019 | `IMPLEMENTADO` según rol/estado; RF-018 libera slot internamente cuando corresponde |
| Check-in y espera RECEPTIONIST | — | RF-026/027 | `IMPLEMENTADO` según estado exacto |
| Disponibilidad sanitizada RECEPTIONIST | CU-D6 | RF-012, porción futura | `CONCEPTUAL`; sin endpoint/permiso |
| Inicio y fin operativos PROFESSIONAL | — | RF-028/029; RF-030 | Mutaciones `IMPLEMENTADO`; consulta RF-030 `PARCIAL`; flujo UX `NO IMPLEMENTADO` |

## 9. Exclusiones y fuentes

Quedan excluidos historia clínica, diagnóstico, prescripciones, interfaz de atención médica, dashboard profesional, agenda personal, gestión de usuarios completa, configuración del hospital, reportes avanzados, generación automática de slots, notificaciones, waitlist, prioridad, módulos clínicos futuros y permisos no existentes. Ninguna tabla SQL o decisión `CLOSED` convierte CU-D2/CU-D3/CU-D6 en operación de portal. DEC-005/008/010 siguen `OPEN`.

Fuentes: [F.1 requisitos UX](../UX-REQUIREMENTS-ANALYSIS.md), [F.1.1 auditoría](../UX-REQUIREMENTS-POST-AUDIT.md), [F.2.1 portal público](PUBLIC-PORTAL-USER-FLOWS.md), [F.2.1 post-audit](PUBLIC-PORTAL-USER-FLOW-POST-AUDIT.md), [Word maestro](../../documentation-export/HOSPITALPLATFORM-COMPLETE-DOCUMENTATION.docx), [Domain Baseline](../../DOMAIN-BASELINE.md), [Decision Register](../../DOMAIN-DECISION-REGISTER.md), [C.1](../../agenda/AGENDA-AVAILABILITY-RF-UC-MATRIX.md), [C.2](../../agenda/AGENDA-AVAILABILITY-USE-CASES.md), [C.3](../../agenda/AGENDA-AVAILABILITY-CONTRACT-DESIGN.md), [C.4](../../agenda/AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md), [D.2](../../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [D.3.1](../../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md), [D.3.2](../../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md), [D.3.3](../../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md), [D.4 catálogo](../../agenda/consolidation/AGENDA-USE-CASE-CATALOG.md) y [D.4 actores](../../agenda/consolidation/AGENDA-ACTOR-USECASE-MATRIX.md). Las rutas, DTO y roles se contrastaron con la descripción de API del baseline y las fichas D.2; no se modificó código.

**Gate F.2.2: 🟡 PRIVATE PORTALS FLOW BASELINE WITH OBSERVATIONS.** Se pueden prototipar las vistas administrativas parciales y la operación de citas de RECEPTIONIST, marcando que el backend existe pero la UI no. La disponibilidad de RECEPTIONIST y la navegación de PROFESSIONAL requieren diseño/contratos posteriores antes de presentarse como interacción ejecutable.

## Validación de alcance

Archivo creado: `docs/ux/user-flows/PRIVATE-PORTALS-USER-FLOWS.md`. Archivos existentes modificados por F.2.2: **0**. `git status` confirmó la rama `feat/hospitalplatform-user-flows` y mostró `?? docs/ux/user-flows/`, que contiene también los entregables anteriores de F.2.1 aún sin seguimiento. `git diff --check` no reportó errores en archivos rastreados; la revisión directa de este Markdown nuevo no encontró espacios finales ni enlaces locales rotos. **Java: 0; SQL: 0; tests: 0; migraciones: 0; seguridad: 0**. No se ejecutó Maven ni se hizo commit, push o merge.
