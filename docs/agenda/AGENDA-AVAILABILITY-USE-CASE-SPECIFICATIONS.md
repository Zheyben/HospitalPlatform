# ETAPA C.4 — USE CASE SPECIFICATIONS

## 1. Objetivo

Formalizar, solo para el caso de estudio academico HOSPITALPLATFORM, las ocho
etiquetas descriptivas de C.2 del incremento Agenda Availability + Specialty
Policy. La autoridad de decisiones es el proyecto/docente (DEC-001), no el
Hospital de Huaycan. El codigo/controller y V1-V3 prueban comportamiento
actual; SRS y decisiones de diseno no prueban implementacion. C.4 no autoriza
codigo, API, migracion, test ni permiso nuevo.

Fuentes: `AGENDA-AVAILABILITY-RF-UC-MATRIX.md` (C.1),
`AGENDA-AVAILABILITY-USE-CASES.md` (C.2),
`AGENDA-AVAILABILITY-CONTRACT-DESIGN.md` (C.3), SRS, `DOMAIN-BASELINE.md`,
`DOMAIN-DECISION-REGISTER.md`, API Specification, MODULE-CONTRACTS-ARCHITECTURE,
ADR-001/005/006/007/009, Java, tests, seguridad y migraciones V1/V2/V3.

## 2. Convenciones

`CU-D1` a `CU-D8` son identificadores **locales y descriptivos de C.4**, no
codigos UC canonicos del proyecto: C.1/C.2 no encontraron un catalogo oficial.
La aprobacion de IDs oficiales queda pendiente de definicion academica. Cada
ficha tiene un unico estado de esta lista: IMPLEMENTADO, PARCIALMENTE
IMPLEMENTADO, APROBADO CONCEPTUALMENTE, NO IMPLEMENTADO o PENDIENTE DE DECISION.
Una ficha implementada puede referir un RF mas amplio aun parcial. En casos
conceptuales, los pasos numerados describen **solo el objetivo autorizado**;
no constituyen un flujo API o una secuencia Java ejecutable. `PENDIENTE` no
concede autorizacion para completar por analogia con otro modulo.

Las URI actuales incorporan el context path `/api/v1` de `application.yml`.
Las rutas `/specialties` de la API Specification son documentales, no mappings
vigentes. `Sistema` designa un actor tecnico, no el rol operativo `SYSTEM`.

## 3. Actores

| Actor | Alcance en estas fichas | Evidencia |
|---|---|---|
| ADMIN | Gestion basica de profesionales, schedules y consulta operativa de slots; Catalogs y asignacion solo como intencion documentada. | Controllers actuales; SRS RF-007/008; DEC-006. |
| PATIENT | Reserva de cita para su propio perfil activo; futura consulta sanitizada solo conceptual. | `AppointmentController`/`AppointmentService`; DEC-007. |
| RECEPTIONIST | Reserva administrativa para paciente activo; futura consulta sanitizada solo conceptual. | Appointments; DEC-007. |
| PROFESSIONAL | No es actor principal de ninguno de estos ocho flujos; no recibe acceso nuevo a disponibilidad. | DEC-002/007 y controllers actuales. |
| Sistema | Ejecutor tecnico de exclusion de doble reserva, sin endpoint ni rol nuevo. | UPDATE condicional, V3 y RF-014. |

## 4. Estado de implementacion

| Ficha | Nombre descriptivo | Estado de la ficha | Limite del estado |
|---|---|---|---|
| CU-D1 | Mantener profesional | PARCIALMENTE IMPLEMENTADO | Creacion, consulta y actualizacion basicas ADMIN existen; N:M y desactivacion HTTP no. |
| CU-D2 | Asociar profesional y especialidad | APROBADO CONCEPTUALMENTE | DEC-006 aprueba propietario/N:M; no hay flujo Java. |
| CU-D3 | Mantener definiciones de especialidad | NO IMPLEMENTADO | RF-008 describe gestion; DEC-006 solo cierra ownership. |
| CU-D4 | Configurar horarios | PARCIALMENTE IMPLEMENTADO | Schedule ADMIN existe; generacion y politica de specialty no. |
| CU-D5 | Consultar disponibilidad administrativa | IMPLEMENTADO | Solo los dos GET ADMIN actuales; RF-012 completo sigue parcial. |
| CU-D6 | Consultar disponibilidad sanitizada | APROBADO CONCEPTUALMENTE | DEC-007 aprueba actores/exposicion; no existe vista/API. |
| CU-D7 | Reservar cita | IMPLEMENTADO | Dependencia actual de Appointments; no se rediseña. |
| CU-D8 | Evitar doble reserva | IMPLEMENTADO | Invariante actual; cobertura SRS de 20 solicitudes pendiente. |

## 5. CU-D1 — Mantener profesional

- **Identificacion:** RF-007; fuentes C.1/C.2/C.3, SRS RF-007, controller,
  service y V1. Estado: PARCIALMENTE IMPLEMENTADO.
- **Actores/objetivo:** ADMIN registra, lista, consulta o cambia licencia de
  profesional. Sistema es ejecutor tecnico; otros roles no autorizados.
- **Precondiciones/disparador actuales:** ADMIN autenticado envia una de las
  operaciones existentes; creacion/PUT reciben `licenseNumber` requerido.
  `userId` es opcional solo en creacion; su FK no define ciclo de vinculacion.
- **Flujo principal actual:** 1. ADMIN envia `POST /api/v1/professionals` con
  `CreateProfessionalRequestDTO`. 2. `ProfessionalService` normaliza y verifica
  licencia duplicada, guarda `Professional`. 3. Devuelve `201` con
  `ProfessionalResponseDTO`. `GET` lista/detalla y `PUT` cambia solo
  `licenseNumber`; son variantes actuales, no pasos obligatorios del POST.
- **Alternos/excepciones actuales:** licencia duplicada -> 409
  `DUPLICATE_PROFESSIONAL`; detalle/PUT de profesional inexistente o eliminado
  -> 404 `PROFESSIONAL_NOT_FOUND`; acceso sin ADMIN -> denegado. FK de `userId`
  existe, sin contrato funcional de vinculacion en esta ficha.
- **Reglas/postcondiciones:** licencia unica (V1); consultas sin cambio;
  creacion o actualizacion de licencia persistida. **Futuro no ejecutable:**
  asociacion N:M es CU-D2; `deactivateProfessional` existe en service, pero
  carece de endpoint (DEC-016 PROPOSED). DEC-005 OPEN para usuario-profesional.
- **Persistencia/contratos:** `professionals`, `Professional`,
  `ProfessionalRepository` internos; no se usa repository externo.
- **Seguridad/tests:** ADMIN en `ProfessionalController`;
  `ProfessionalServiceTest` y `ProfessionalControllerAuthorizationTest`
  cubren operaciones basicas/rol, no asignacion N:M.
- **TRAZABILIDAD:** RF-007 -> CU-D1 -> ADMIN -> controller/service internos
  Professionals -> `professionals` V1 -> ADMIN actual -> tests indicados ->
  DEC-005 OPEN, DEC-016 PROPOSED.

## 6. CU-D2 — Asociar profesional y especialidad

- **Identificacion:** RF-007; C.1/C.2/C.3, SRS y DEC-006. Estado: APROBADO
  CONCEPTUALMENTE; **NO IMPLEMENTADO**.
- **Actores/objetivo:** ADMIN (SRS) busca asociar un profesional a una
  especialidad habilitada. Professionals posee la relacion N:M; Catalogs
  posee la definicion de specialty.
- **Precondiciones/disparador:** pareja conceptual `professionalId` y
  `specialtyId` (ambos UUID por V1). El comando/disparador HTTP, la semantica
  de profesional/especialidad aplicable y su validacion son PENDIENTES.
- **Flujo conceptual, no ejecutable:** 1. Se identifica la pareja deseada.
  2. Professionals seria responsable de validar y gestionar la asociacion
  mediante contratos publicos aprobados. 3. Forma de respuesta y consulta
  funcional quedan PENDIENTES. No existe secuencia Java/API hoy.
- **Alternos/excepciones:** la SRS menciona asociacion invalida; duplicidad
  fisica queda limitada por PK compuesta. Error de dominio/HTTP, precedencia,
  idempotencia, desasociacion y reactivacion: PENDIENTES, no existentes.
- **Reglas/postcondiciones:** DEC-006 cierra propietario y cardinalidad N:M,
  no el flujo. Postcondicion deseada: asociacion consultable; **no existe
  postcondicion funcional implementada**.
- **Persistencia/contratos:** V1 `professional_specialties` con PK
  `(professional_id, specialty_id)` y dos FK; no entity/repository/service/
  controller Java de asignacion. Lookup de Catalogs y consulta publica de
  Professionals son PROPUESTOS en C.3, sin firma/DTO/URI definitiva.
- **Seguridad/tests:** ADMIN es actor documentado, no permiso instalado para
  esta operacion; no hay test especifico de N:M.
- **TRAZABILIDAD:** RF-007 -> CU-D2 -> ADMIN conceptual -> contratos
  Catalogs/Professionals propuestos -> `professional_specialties` V1 (solo
  capacidad fisica) -> seguridad futura pendiente -> sin test -> DEC-006
  CLOSED; politica concreta pendiente.

## 7. CU-D3 — Mantener definiciones de especialidad

- **Identificacion:** RF-008; C.1/C.2/C.3, SRS, DEC-006 y V1. Estado: NO
  IMPLEMENTADO; ownership APROBADO CONCEPTUALMENTE.
- **Actores/objetivo:** ADMIN segun SRS busca consultar, crear, editar o
  activar/desactivar definiciones para reservas. Catalogs es el propietario.
- **Precondiciones/disparador:** permiso/datos validados son requisitos SRS,
  no precondiciones de codigo actual; no existe disparador API vigente.
- **Flujo principal:** NO EXISTE. La enumeracion consulta/creacion/edicion/
  cambio de estado es alcance documental RF-008, no pasos ejecutables.
- **Alternos/excepciones:** SRS menciona duplicado y acceso no autorizado;
  V1 impone `name UNIQUE`. Errores funcionales, codigos HTTP y semantica de
  `active`/`deleted_at`: PENDIENTES. La SRS menciona `code`, pero V1 no lo tiene.
- **Reglas/postcondiciones:** DEC-006 fija ownership, no CRUD Java. Estado
  buscado por SRS: catalogo actualizado sin borrar historia; **ningun efecto
  funcional actual**. Efectos sobre schedules/slots/citas: PENDIENTES.
- **Persistencia/contratos:** V1 `specialties(id, name, description, active,
  deleted_at, timestamps)`; Catalogs no tiene entity/repository/service/
  controller funcional. Lookup y gestion son PROPUESTOS en C.3.
- **Seguridad/tests:** ADMIN conceptual de RF-008; sin `@PreAuthorize` de
  Catalogs ni pruebas funcionales de Catalogs.
- **TRAZABILIDAD:** RF-008 -> CU-D3 -> ADMIN conceptual -> contratos Catalogs
  propuestos/no existentes -> `specialties` V1 (esquema, no API) -> permiso
  futuro pendiente -> sin test -> DEC-006 CLOSED, DEC-003 OPEN.

## 8. CU-D4 — Configurar horarios

- **Identificacion:** RF-011; C.1/C.2/C.3, SRS, ADR-006 y Agenda. Estado:
  PARCIALMENTE IMPLEMENTADO.
- **Actores/objetivo:** ADMIN configura/consulta un Schedule por profesional
  y `specialtyId`; no se concede gestion a PROFESSIONAL.
- **Precondiciones/disparador actuales:** ADMIN autenticado; en creacion/PUT,
  profesional activo, `dayOfWeek` 0..6, `endTime > startTime` y specialty UUID
  existente por FK. No se verifica `specialties.active`, `deleted_at` ni
  asociacion N:M.
- **Flujo principal actual:** 1. ADMIN envia `POST /api/v1/agendas` con
  `CreateAgendaRequestDTO(professionalId, specialtyId, dayOfWeek, startTime,
  endTime)`. 2. `AgendaService` usa `ProfessionalLookupService` y valida rango;
  V1 comprueba FK/CHECK. 3. Guarda `Schedule` y devuelve `201
  AgendaResponseDTO`. GET lista/detalle, PUT actualiza y PATCH cambia `active`
  en operaciones separadas; ninguna crea slots.
- **Alternos/excepciones actuales:** profesional inactivo/inexistente -> 404
  `PROFESSIONAL_NOT_AVAILABLE`; rango invalido -> 400
  `INVALID_SCHEDULE_TIME`; FK specialty identificada -> 400
  `INVALID_SPECIALTY_REFERENCE`; agenda inexistente -> 404
  `AGENDA_NOT_FOUND`; entrada DTO invalida -> 400 `VALIDATION_ERROR`.
- **Reglas/postcondiciones:** Schedule persistido/actualizado o consulta sin
  cambio. **Pendiente:** specialty activa/asignada, generacion, duracion,
  calendario, solapamiento, horizonte y ventanas temporales; no se insertan
  `availability_slots` desde este flujo.
- **Persistencia/contratos:** `schedules`, `Schedule`, `ScheduleRepository`
  internos; `ProfessionalLookupService.existsActiveProfessional(UUID)` real.
  Lookups de Catalogs y asignacion de Professionals: PROPUESTOS en C.3.
- **Seguridad/tests:** ADMIN en `AgendaController`; `AgendaServiceTest`,
  `AgendaControllerTest`, `AgendaControllerAuthorizationTest` y
  `HospitalPlatformApplicationIT` (FK invalida, no actividad specialty).
- **TRAZABILIDAD:** RF-011 -> CU-D4 -> ADMIN ->
  `ProfessionalLookupService`/AgendaService actuales; lookups specialty
  propuestos -> `schedules` V1 -> ADMIN actual -> tests indicados -> DEC-006
  CLOSED, DEC-008/010 OPEN.

## 9. CU-D5 — Consultar disponibilidad administrativa

- **Identificacion:** RF-012 (porcion ADMIN); C.1/C.2/C.3, controller y
  repository. Estado: IMPLEMENTADO para esta consulta; RF-012 sigue parcial.
- **Actores/objetivo:** ADMIN consulta slots existentes y la usabilidad
  derivada; no reserva ni crea slots.
- **Precondiciones/disparador:** ADMIN autenticado llama `GET
  /api/v1/availability` con filtros opcionales `scheduleId`, `professionalId`,
  `slotDate`, `status`, o `GET /api/v1/availability/{id}`. No se exige fecha
  futura, especialidad activa ni filtro AVAILABLE.
- **Flujo principal actual:** 1. `AgendaController` recibe la consulta.
  2. `AgendaService` obtiene slots y schedule desde `AvailabilitySlotRepository`.
  3. `AgendaMapper` devuelve `List<AvailabilitySlotResponseDTO>` o un DTO
  (`id`, `scheduleId`, `slotDate`, `startTime`, `endTime`, `status`, `usable`).
  `usable = AVAILABLE && schedule.active`; la lista no excluye automaticamente
  filas RESERVED/BLOCKED o de schedule inactivo.
- **Alternos/excepciones:** lista sin coincidencias -> lista vacia; detalle
  inexistente -> 404 `AVAILABILITY_SLOT_NOT_FOUND`; anonimo/rol no permitido
  -> 401/403. Errores de binding de filtros no se formalizan aqui.
- **Reglas/postcondiciones:** lectura sin reserva ni mutacion.
  `AvailabilitySlotService.isAvailable(UUID)` comprueba **solo** AVAILABLE;
  `isUsable(UUID)` comprueba AVAILABLE y schedule activo. Ninguno es contrato
  del listado HTTP. No existe filtro `specialtyId` en este GET.
- **Persistencia/contratos:** `availability_slots`, `schedules`,
  `AvailabilitySlot`, `AvailabilitySlotRepository` y `AgendaService` internos.
  `AvailabilitySlotService` es contrato publico puntual existente, distinto
  de la consulta de listado.
- **Seguridad/tests:** ADMIN real; `AgendaServiceTest.findsAvailabilityAndExposesUsableState`,
  `AgendaControllerTest.returnsAvailabilityResponse`,
  `AgendaControllerAuthorizationTest`. `HospitalPlatformApplicationIT`
  prueba `isUsable` con PostgreSQL, **no** `findAvailability`.
- **TRAZABILIDAD:** RF-012 -> CU-D5 -> ADMIN -> AgendaService interno +
  `AvailabilitySlotService` puntual distinto -> slots/schedules V1 -> ADMIN
  actual -> tests indicados; IT de `findAvailability` pendiente -> DEC-019
  CLOSED, DEC-007 para vista separada.

## 10. CU-D6 — Consultar disponibilidad sanitizada

- **Identificacion:** RF-012 (porcion futura); C.1/C.2/C.3, SRS y DEC-007.
  Estado: APROBADO CONCEPTUALMENTE; **NO IMPLEMENTADO**.
- **Actores/objetivo:** PATIENT y RECEPTIONIST autenticados podrian descubrir
  slots ofertables para una futura reserva. ADMIN conserva CU-D5;
  PROFESSIONAL y anonimo no reciben permiso nuevo.
- **Precondiciones/disparador:** identidad con rol conceptual aprobado por
  DEC-007; disparador HTTP, filtros definitivos y criterios de seleccion:
  PENDIENTES DE DISENO. No se reutiliza el GET ADMIN como si diera acceso.
- **Flujo conceptual, no ejecutable:** 1. El actor necesita descubrir oferta.
  2. Agenda seria propietaria de una vista minimizada separada.
  3. Datos devueltos, filtros, URI, DTO y errores finales requieren decision
  academica. No existe llamada o respuesta HTTP actual para este actor.
- **Alternos/excepciones:** ausencia de cupos, recurso inactivo, datos
  invalidos, 401/403 y manejo por rol requeriran contrato futuro; ninguno
  esta implementado para esta vista. No se definen 404/409 futuros.
- **Reglas/postcondiciones:** objetivo de lectura sin mutacion; el POST de
  reserva conserva su revalidacion atomica. Sanitizacion exacta, efectos de
  specialty inactiva/asociacion y temporalidad: PENDIENTES. No se declaran
  campos, paginacion ni filtros oficiales.
- **Persistencia/contratos:** uso futuro de slots/schedules existentes,
  **sin persistencia funcional de la vista hoy**. Contrato de consulta de
  Agenda PROPUESTO en C.3, sin firma; no es el `AvailabilitySlotService`
  puntual ni el listado ADMIN.
- **Seguridad/tests:** actores solo conceptuales por DEC-007; no hay
  `@PreAuthorize` ni test especifico para vista sanitizada.
- **TRAZABILIDAD:** RF-012 -> CU-D6 -> PATIENT/RECEPTIONIST conceptuales ->
  contrato Agenda propuesto -> V1 slots/schedules solo como posible fuente ->
  seguridad futura pendiente -> sin test -> DEC-007 CLOSED, DEC-010 OPEN.

## 11. CU-D7 — Reservar cita

- **Identificacion:** RF-013; C.1/C.2/C.3, SRS y Appointments. Estado:
  IMPLEMENTADO; dependencia existente, sin rediseño.
- **Actores/objetivo:** PATIENT para su propio perfil activo; ADMIN o
  RECEPTIONIST para un paciente activo explicitado por `patientId`.
- **Precondiciones/disparador:** rol autorizado; `POST /api/v1/appointments`
  recibe `CreateAppointmentRequestDTO(slotId, patientId, reason)`; con
  `patientId` explicito exige ADMIN/RECEPTIONIST, y sin el exige PATIENT para
  resolver el perfil propio. Slot utilizable y
  profesional activo se verifican durante la operacion, no mediante consulta
  previa obligatoria. Sin regla de fecha pasada/anticipacion vigente.
- **Flujo principal actual:** 1. `AppointmentService` resuelve paciente via
  `PatientLookupService`/`CurrentUserService`. 2. Solicita
  `AvailabilitySlotReservationService.reserveUsableSlot(slotId)`; Agenda
  cambia condicionalmente AVAILABLE -> RESERVED si schedule activo y devuelve
  `AvailabilitySlotReference`. 3. Appointments valida profesional activo con
  `ProfessionalLookupService`, crea cita `SCHEDULED` con `flowStage=null` y
  persiste en la misma transaccion. 4. Controller responde `201` con
  `AppointmentResponseDTO` y `Location`.
- **Alternos/excepciones actuales:** paciente ausente/inactivo -> 404
  `PATIENT_NOT_AVAILABLE`; slot no utilizable o colision -> 409
  `SLOT_UNAVAILABLE`; profesional inactivo -> 409
  `PROFESSIONAL_NOT_AVAILABLE`; PATIENT que intenta elegir otro paciente ->
  403. Fallo posterior revierte la reserva. Error futuro de specialty activa
  no se inserta en este POST por C.4.
- **Reglas/postcondiciones:** cliente no fija `professionalId`,
  `specialtyId` ni estado; los deriva el servidor. Exito: una cita activa
  SCHEDULED y slot RESERVED; fallo: sin efectos parciales. No se genera slot
  ni se exige UI de descubrimiento.
- **Persistencia/contratos:** `appointments` V1/V3, `availability_slots` V1;
  Appointments usa contratos publicos de Patients/Professionals/Agenda y su
  propio repository, no entidades/repositories externos.
- **Seguridad/tests:** PATIENT propio, ADMIN, RECEPTIONIST en controller y
  service; `AppointmentModuleIT.createsAppointmentAndReservesSlotAgainstPostgreSql`,
  `rejectsReservedBlockedAndInactiveScheduleSlots`,
  `rollsBackReservedSlotWhenAppointmentInsertFails` y tests de autorizacion.
- **TRAZABILIDAD:** RF-013 -> CU-D7 -> PATIENT propio/ADMIN/RECEPTIONIST ->
  `PatientLookupService`, `CurrentUserService`, `ProfessionalLookupService`,
  `AvailabilitySlotReservationService` -> appointments V1/V3, slot V1 ->
  seguridad/ownership actuales -> `AppointmentModuleIT` -> DEC-010 OPEN.

## 12. CU-D8 — Evitar doble reserva

- **Identificacion:** RF-014; C.1/C.2/C.3, SRS y V3. Estado: IMPLEMENTADO
  como **invariante tecnico** de CU-D7, no UC humano ni endpoint propio.
- **Actores/objetivo:** Sistema como ejecutor tecnico impide dos citas
  activas para el mismo slot. Los solicitantes son actores de CU-D7.
- **Precondiciones/disparador:** dos o mas peticiones de reserva compiten por
  el mismo slot inicialmente usable; no se exige sincronizacion del cliente.
- **Flujo principal actual:** 1. Cada peticion entra en la transaccion de
  reserva existente. 2. Agenda intenta UPDATE condicional del slot AVAILABLE
  con schedule activo. 3. Una transicion efectiva permite persistir cita;
  V3 agrega indice unico parcial `uq_appointments_active_slot` como barrera
  adicional para citas SCHEDULED/CONFIRMED.
- **Alternos/excepciones:** perdedor -> conflicto de slot no disponible;
  fallo de persistencia revierte transaccion y reserva. No se promete orden
  de ganadores ni un comportamiento temporal nuevo.
- **Reglas/postcondiciones:** a lo sumo una cita activa por slot y ninguna
  segunda asignacion efectiva. Esto es comportamiento vigente; **la cobertura
  de carga SRS permanece pendiente**, no el endpoint de reserva.
- **Persistencia/contratos:** `availability_slots` V1, `appointments` V1/V3;
  `AvailabilitySlotReservationService` publico y UPDATE/indice internos.
- **Seguridad/tests:** hereda actores, rol y ownership de CU-D7; no hay
  permiso o endpoint para `Sistema`. `AppointmentModuleIT.allowsOnlyOneOfTwoConcurrentReservations`
  usa PostgreSQL con **2** solicitudes; `AppointmentPersistenceIT.rejectsTwoActiveAppointmentsForTheSameSlot`
  verifica unicidad. SRS RF-014 exige **20** solicitudes simultaneas;
  **COBERTURA PENDIENTE** para ese escenario de validacion posterior.
- **TRAZABILIDAD:** RF-014 -> CU-D8 (invariante de CU-D7) -> Sistema tecnico +
  actores de reserva -> `AvailabilitySlotReservationService` -> slot V1 e
  indice parcial V3 -> seguridad heredada CU-D7 -> dos tests indicados;
  ensayo 20 pendiente -> DEC-023 OPEN para metas operativas.

## 13. Reglas globales

1. Una decision CLOSED de diseno no implica Java. DEC-006 cierra propiedad
   Catalogs/Professionals y N:M; DEC-007 cierra actores de vista futura;
   ninguna implementa Catalogs, N:M o vista sanitizada.
2. FK a specialty comprueba existencia fisica, no `active`, `deleted_at` ni
   asociacion N:M. `isUsable = AVAILABLE && schedule.active`; no aplica fecha
   pasada o anticipacion. Una consulta de oferta no reserva y puede quedar
   obsoleta antes del POST.
3. La reserva actual utiliza UPDATE condicional y unicidad parcial V3. Los
   detalles de generacion, duracion, calendario, solapamiento y ventanas
   temporales permanecen DEC-008/010 OPEN; no se usan para rechazar hoy.
4. Cada error HTTP mencionado como actual se limita a su controller vigente;
   no es un contrato automatico de Catalogs ni de la futura vista.

## 14. Dependencias

| Consumidor | Proveedor/frontera | Actual o futuro |
|---|---|---|
| Agenda | `ProfessionalLookupService` | Actual: existencia activa para Schedule. |
| Professionals | Catalogs lookup de definicion/estado | PROPUESTO C.3 para N:M, sin firma. |
| Agenda | Catalogs lookup y consulta de asignacion Professionals | PROPUESTOS C.3 para politica de specialty; no aplicados. |
| Appointments | Patients, Professionals y reserva de Agenda por contratos publicos | Actual; no usa repositories/entities externos. |
| Consulta sanitizada | Datos de Agenda; reglas de specialty futuras | PROPUESTO; no requiere un endpoint existente de consulta para reservar. |

## 15. Matriz de trazabilidad

| RF | Ficha / actor | Contrato | Persistencia | Seguridad | Evidencia de test / brecha | Decisiones |
|---|---|---|---|---|---|---|
| RF-007 | CU-D1 / ADMIN | ProfessionalController/Service actuales | `professionals` V1 | ADMIN actual | `ProfessionalServiceTest`, `ProfessionalControllerAuthorizationTest` | DEC-005 OPEN, DEC-016 PROPOSED |
| RF-007 | CU-D2 / ADMIN conceptual | Catalogs/Professionals propuestos, sin firma | `professional_specialties` V1, no flujo Java | Pendiente | Sin test N:M | DEC-006 CLOSED; politica pendiente |
| RF-008 | CU-D3 / ADMIN conceptual | Catalogs propuesto | `specialties` V1, no flujo Java | Pendiente | Sin test Catalogs | DEC-006 CLOSED, DEC-003 OPEN |
| RF-011 | CU-D4 / ADMIN | `ProfessionalLookupService` actual; otros lookups propuestos | `schedules` V1 | ADMIN actual | `AgendaServiceTest`, `HospitalPlatformApplicationIT`; sin prueba specialty activa | DEC-006 CLOSED; DEC-008/010 OPEN |
| RF-012 | CU-D5 / ADMIN | AgendaService interno; `AvailabilitySlotService` puntual distinto | slots/schedules V1 | ADMIN actual | Unit/controller/method security; sin IT PostgreSQL `findAvailability` | DEC-019 CLOSED |
| RF-012 | CU-D6 / PATIENT, RECEPTIONIST conceptuales | Consulta Agenda propuesta sin firma | Slots/schedules V1 solo como fuente futura | Pendiente | Sin prueba de vista/rol/sanitizacion | DEC-007 CLOSED, DEC-010 OPEN |
| RF-013 | CU-D7 / PATIENT propio, ADMIN, RECEPTIONIST | Contratos Patients/Professionals/Agenda actuales | appointments V1/V3, slot V1 | Actual | `AppointmentModuleIT`, tests de autorizacion | DEC-010 OPEN |
| RF-014 | CU-D8 / Sistema tecnico, actores CU-D7 | `AvailabilitySlotReservationService` actual | slot V1, indice V3 | Heredada CU-D7 | 2 solicitudes PostgreSQL; ensayo SRS 20 pendiente | DEC-023 OPEN |

## 16. Gaps

- RF-007: N:M sin Java/API/contrato; Professional-User sin ciclo aprobado.
- RF-008: `specialties` existe en V1, pero Catalogs carece de flujo Java. SRS
  menciona `code` no presente en V1. La API Specification enumera rutas
  `/specialties` sin controller real.
- RF-011: Agenda no verifica specialty activa/asignada ni genera slots. No
  existe politica aprobada de duracion, calendario o solapamiento.
- RF-012: consulta ADMIN real; vista PATIENT/RECEPTIONIST no existe. No hay IT
  PostgreSQL especifica de `findAvailability`; `isUsable` IT no la sustituye.
- RF-014: dos solicitudes concurrentes probadas; criterio SRS de veinte
  solicitudes simultaneas pendiente. No se afirma cobertura plena de RF-014.
- `DOMAIN-BASELINE.md` conserva un corte historico de decisiones; el registro
  de decisiones es la fuente de estados vigentes. Ninguna fuente historica se
  modifica desde C.4.

## 17. Decisiones abiertas

| Estado vigente | Decisiones | No se resuelve en C.4 |
|---|---|---|
| CLOSED | DEC-006, DEC-007, DEC-009, DEC-019 | Solo su alcance conceptual/API aprobado. |
| OPEN | DEC-003, DEC-005, DEC-008, DEC-010, DEC-017, DEC-022, DEC-023 | Bootstrap, ciclo User-Professional, slots/calendario, temporalidad, onboarding, auditoria, operabilidad. |
| PROPOSED | DEC-004, DEC-015, DEC-016, DEC-020 | Frontera Auth, eventos, desactivacion HTTP, permisos granulares. |

DEC-009 fija una zona IANA configurable con `America/Lima` inicial academica,
no una regla temporal aplicada en Java. No hay aprobacion institucional.

## 18. Exclusiones

No se incluye generacion/materializacion de slots, validacion de solapamiento,
ventanas temporales, cambio de estado nuevo, consulta sanitaria por otros roles,
CRUD de specialty, endpoint N:M, permisos nuevos, UI de descubrimiento,
waitlist, notificaciones, auditoria nueva, migraciones ni despliegue. CU-D7 y
CU-D8 documentan dependencias actuales, no amplian Appointments.

## 19. Gate para ETAPA D

Estas ocho fichas y su matriz bastan para representar en UML **lo existente,
lo conceptual y lo pendiente como categorias separadas**. `CU-D*` no se
eleva a numeracion oficial por dibujarlo. ETAPA D no debe mostrar Catalogs,
asignacion N:M o vista sanitizada como implementadas; tampoco convertir la
tabla de slots en generacion funcional. URI, DTO, contrato Java, politica de
specialty y decisiones DEC-008/010 requieren definicion/aprobacion academica
antes de implementacion. C.4 no autoriza cambios fuera de documentacion.
