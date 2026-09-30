# ETAPA D.2 - Casos de uso detallados: Agenda Availability + Specialty Policy

## 1. Introduccion

Estas fichas detallan los ocho casos CU-D1 a CU-D8 representados en el diagrama
general D.1. Son identificadores descriptivos locales de C.4, no codigos UC
oficiales. El alcance corresponde al caso de estudio academico HOSPITALPLATFORM;
las decisiones del proyecto/docente no se atribuyen al Hospital de Huaycan.

`IMPLEMENTADO` significa comportamiento comprobable en Java y, cuando aplica,
HTTP. `PARCIAL` significa que existen operaciones basicas pero no todo el
alcance del RF. `CONCEPTUAL` significa diseno aprobado o documentado sin flujo
Java/API vigente. Una tabla SQL o un contrato propuesto no cambian ese estado.
Las capacidades futuras no se presentan como casos activos. Las rutas actuales
usan el context path `/api/v1`; ninguna ruta de las fichas conceptuales es un
endpoint propuesto por este documento.

Fuentes de control: `DOMAIN-BASELINE.md`, `DOMAIN-DECISION-REGISTER.md`, C.1
`AGENDA-AVAILABILITY-RF-UC-MATRIX.md`, C.2 `AGENDA-AVAILABILITY-USE-CASES.md`,
C.3 `AGENDA-AVAILABILITY-CONTRACT-DESIGN.md`, C.4
`AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md`, D.1
`AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.md` y su `.puml`, controllers, services,
DTOs, seguridad, contratos, entidades, tests y migraciones V1/V2/V3.

## 2. CU-D1 - Gestion profesional

| Campo | Contenido |
|---|---|
| ID | CU-D1; RF-007. |
| Nombre | Crear, consultar y actualizar profesional. |
| Estado | **PARCIAL**: las tres operaciones basicas existen; el dominio profesional completo no. |
| Actor principal | ADMIN. |
| Actores secundarios | Ningun actor humano adicional en estas operaciones. |
| Objetivo | Mantener la identidad profesional basica y consultar profesionales vigentes. |
| Precondiciones | ADMIN autenticado; `licenseNumber` requerido para crear o actualizar. `userId` es opcional solo al crear y esta sujeto a FK. |
| Flujo principal | **Crear:** ADMIN envia `POST /api/v1/professionals`; el servicio normaliza y comprueba licencia, guarda `Professional` y responde `201` con `ProfessionalResponseDTO`. **Consultar:** `GET /api/v1/professionals` lista vigentes y `GET /api/v1/professionals/{id}` obtiene uno. **Actualizar:** `PUT /api/v1/professionals/{id}` cambia solo `licenseNumber`. Son operaciones separadas, no pasos secuenciales obligatorios. |
| Flujos alternos | Licencia duplicada: 409 `DUPLICATE_PROFESSIONAL`; detalle/actualizacion de profesional ausente o eliminado: 404 `PROFESSIONAL_NOT_FOUND`; datos invalidos: rechazo de validacion. Una FK limita `userId` inexistente, sin definir ciclo de vinculacion. |
| Postcondiciones | Creacion o nueva licencia persistida; las consultas no modifican datos. No hay asignacion de especialidad. |
| Reglas de negocio | Licencia unica; `active` deriva de `deleted_at IS NULL`. El service tiene desactivacion, pero no existe endpoint para ella. DEC-005 y DEC-016 no cierran el ciclo Professional/User ni la desactivacion HTTP. |
| Contratos relacionados | Controller/Service internos de Professionals; `ProfessionalLookupService` existe para consumidores, pero estas operaciones no son un contrato publico de asociacion N:M. |
| Persistencia | `professionals` V1; `Professional` y `ProfessionalRepository` internos. `professional_specialties` V1 no constituye asignacion Java. |
| Seguridad | `@PreAuthorize("hasRole('ADMIN')")` en los cuatro mappings actuales; otros roles no gestionan profesionales. |
| Tests asociados | `ProfessionalServiceTest`, `ProfessionalControllerAuthorizationTest`; no hay prueba de gestion N:M. |

## 3. CU-D2 - Asociacion profesional-especialidad

| Campo | Contenido |
|---|---|
| ID | CU-D2; RF-007, DEC-006. |
| Nombre | Asociar profesional con especialidad. |
| Estado | **CONCEPTUAL**; aprobado en cardinalidad y propiedad, no implementado. |
| Actor principal | ADMIN esperado por RF-007; no hay operacion autorizada vigente. |
| Actores secundarios | Catalogs seria propietario de la definicion, no actor humano. |
| Objetivo | Permitir la asociacion N:M aprobada entre profesionales y especialidades. |
| Precondiciones | No existen precondiciones ejecutables. Identidad y elegibilidad de la pareja, y politica de especialidad habilitada, requieren contrato y reglas concretas. |
| Flujo principal | **No ejecutable:** se identifica conceptualmente la pareja profesional-especialidad; Professionals seria propietario de su gestion y Catalogs de la definicion. No existe secuencia Java, servicio, DTO o endpoint de asociacion. |
| Flujos alternos | Asociacion invalida o duplicada figura como problema de diseno; codigos, precedencia, idempotencia y desasociacion no estan definidos como comportamiento actual. |
| Postcondiciones | Objetivo conceptual: asociacion consultable. Ninguna postcondicion funcional existe hoy. |
| Reglas de negocio | DEC-006 aprueba N:M y separacion Catalogs/Professionals; no aprueba firma Java ni validacion de estado. |
| Contratos relacionados | Consulta de Catalogs y contrato de asignacion/consulta de Professionals **propuestos en C.3**, sin firma implementada. `ProfessionalLookupService` actual no consulta N:M. |
| Persistencia | `professional_specialties` V1 tiene PK `(professional_id, specialty_id)` y dos FK. Capacidad fisica, no flujo de gestion. |
| Seguridad | ADMIN es actor documental; no existe permiso HTTP especifico de asociacion. |
| Tests asociados | Ninguno de asignacion N:M; cobertura pendiente. |

## 4. CU-D3 - Gestion del catalogo de especialidades

| Campo | Contenido |
|---|---|
| ID | CU-D3; RF-008, DEC-006. |
| Nombre | Administrar definiciones de especialidad. |
| Estado | **CONCEPTUAL** en D.1/D.2: ownership aprobado; gestion documentada y **no implementada**. |
| Actor principal | ADMIN previsto por RF-008, sin operacion vigente. |
| Actores secundarios | Ninguno. Catalogs es modulo propietario, no actor. |
| Objetivo | Mantener definiciones de especialidad conforme al RF, sin atribuir CRUD real al sistema actual. |
| Precondiciones | No existen precondiciones de una API vigente; validaciones y permisos concretos siguen pendientes. |
| Flujo principal | **No existe flujo ejecutable.** Consulta, creacion, edicion y cambio de estado son alcance documental del RF, no mappings Java. |
| Flujos alternos | La SRS menciona duplicados y acceso no autorizado; su respuesta funcional futura no esta definida. La restriccion SQL `name UNIQUE` no equivale a manejo de error API. |
| Postcondiciones | Ningun cambio funcional actual en Catalogs. |
| Reglas de negocio | DEC-006 asigna definiciones a Catalogs; no fija CRUD, efecto de desactivacion ni politica sobre agendas/citas. V1 no tiene columna `code` aunque la SRS la menciona. |
| Contratos relacionados | Lookup de Catalogs **propuesto en C.3**, sin interfaz Java ni firma aprobada para ejecucion. |
| Persistencia | `specialties` V1 contiene `id`, `name`, `description`, `active`, `deleted_at` y timestamps; no hay entidad/repository/service/controller funcional en Catalogs. |
| Seguridad | ADMIN conceptual; no hay `@PreAuthorize` de Catalogs ni permiso instalado para este caso. |
| Tests asociados | Ninguna prueba funcional de Catalogs. |

## 5. CU-D4 - Gestion de horarios profesionales

| Campo | Contenido |
|---|---|
| ID | CU-D4; RF-011. |
| Nombre | Gestionar horario profesional. |
| Estado | **PARCIAL**: se administran `Schedule` existentes; no se materializan slots. |
| Actor principal | ADMIN. |
| Actores secundarios | Ninguno; PROFESSIONAL no recibe permiso para estos mappings. |
| Objetivo | Configurar y consultar horario, profesional y `specialtyId` de una agenda. |
| Precondiciones | ADMIN autenticado; creacion/PUT requieren `professionalId`, `specialtyId`, `dayOfWeek` 0..6 y horas. El profesional debe existir y estar activo; `endTime > startTime`. La FK exige especialidad existente, no activa/asignada. |
| Flujo principal | **Crear:** `POST /api/v1/agendas` valida profesional mediante `ProfessionalLookupService`, rango horario y guarda `Schedule`; responde `201 AgendaResponseDTO`. **Consultar:** `GET /api/v1/agendas` con filtros opcionales `professionalId`/`specialtyId`, o `GET /api/v1/agendas/{id}`. **Actualizar:** `PUT /api/v1/agendas/{id}` cambia configuracion. **Estado:** `PATCH /api/v1/agendas/{id}/status` cambia `active`. Operaciones independientes. |
| Flujos alternos | Profesional no disponible: 404 `PROFESSIONAL_NOT_AVAILABLE`; horas invalidas: 400 `INVALID_SCHEDULE_TIME`; agenda ausente: 404 `AGENDA_NOT_FOUND`; DTO invalido: 400 `VALIDATION_ERROR`; referencia de especialidad que viola FK: 400 `INVALID_SPECIALTY_REFERENCE` en el caso identificado por el handler. |
| Postcondiciones | Schedule creado/actualizado o lectura sin cambio; PATCH modifica `active`. No se crean `availability_slots`. |
| Reglas de negocio | `specialtyId` permanece UUID; la FK no valida `active`, `deleted_at` ni asociacion N:M. Generacion, calendario, duracion y solapamiento permanecen DEC-008 OPEN. DEC-009 aprueba zona IANA configurable para reglas futuras, pero no hay aplicacion operacional en este flujo; DEC-010 temporal sigue OPEN. |
| Contratos relacionados | `ProfessionalLookupService.existsActiveProfessional(UUID)` real; lookups de Catalogs/asociacion Professionals solo propuestos en C.3. |
| Persistencia | `schedules` V1 con FK a `professionals` y `specialties`, CHECK de dia/rango; `ScheduleRepository` interno. |
| Seguridad | Todos los mappings de Agenda son ADMIN-only mediante `@PreAuthorize`. |
| Tests asociados | `AgendaServiceTest`, `AgendaControllerTest`, `AgendaControllerAuthorizationTest`, `HospitalPlatformApplicationIT` para integridad FK; sin prueba de reglas de especialidad activa/asignada. |

## 6. CU-D5 - Consulta administrativa de disponibilidad

| Campo | Contenido |
|---|---|
| ID | CU-D5; porcion ADMIN de RF-012. |
| Nombre | Consultar disponibilidad operativa. |
| Estado | **IMPLEMENTADO** para ADMIN; RF-012 completo permanece parcial por CU-D6. |
| Actor principal | ADMIN. |
| Actores secundarios | Ninguno. |
| Objetivo | Ver slots preexistentes y su usabilidad derivada sin reservarlos. |
| Precondiciones | ADMIN autenticado. Filtros opcionales actuales: `scheduleId`, `professionalId`, `slotDate`, `status`; detalle por UUID. No se exige fecha futura. |
| Flujo principal | `GET /api/v1/availability` consulta y devuelve `List<AvailabilitySlotResponseDTO>`; `GET /api/v1/availability/{id}` devuelve un DTO. `AgendaService` lee slots/schedules; `AgendaMapper` expone `id`, `scheduleId`, fecha, horas, estado y `usable`. |
| Flujos alternos | Lista sin coincidencias: vacia; detalle inexistente: 404 `AVAILABILITY_SLOT_NOT_FOUND`; anonimo/rol no autorizado: 401/403. |
| Postcondiciones | Solo lectura; no cambia el estado del slot ni se crea cita. |
| Reglas de negocio | `usable = status == AVAILABLE && schedule.active`. El listado no filtra automaticamente RESERVED/BLOCKED ni schedules inactivos. No existe filtro `specialtyId` en este GET. |
| Contratos relacionados | HTTP usa `AgendaService` interno; `AvailabilitySlotService` publico existe para consultas puntuales y **no** implementa este listado. |
| Persistencia | `availability_slots` y `schedules` V1; `AvailabilitySlotRepository.findAvailability` interno. |
| Seguridad | Ambos GET de disponibilidad requieren ADMIN. No habilitan PATIENT/RECEPTIONIST. |
| Tests asociados | `AgendaServiceTest.findsAvailabilityAndExposesUsableState`, `AgendaControllerTest.returnsAvailabilityResponse`, `AgendaControllerAuthorizationTest`; `HospitalPlatformApplicationIT` prueba `isUsable` en PostgreSQL, no el listado `findAvailability`. |

## 7. CU-D6 - Consulta sanitizada para reserva

| Campo | Contenido |
|---|---|
| ID | CU-D6; porcion futura de RF-012, DEC-007. |
| Nombre | Consultar disponibilidad para reserva. |
| Estado | **CONCEPTUAL**; actores/exposicion aprobados, sin implementacion. |
| Actor principal | PATIENT o RECEPTIONIST autenticado, segun DEC-007. |
| Actores secundarios | Ninguno; ADMIN conserva CU-D5 y PROFESSIONAL no recibe acceso nuevo. |
| Objetivo | Descubrir oferta de slots mediante una vista minimizada separada de la consulta operativa ADMIN. |
| Precondiciones | Identidad autenticada con uno de los roles conceptualmente aprobados; no hay precondicion HTTP ejecutable para esta vista hoy. |
| Flujo principal | **No ejecutable:** el actor solicita conocer oferta y Agenda deberia presentar una vista sanitizada. No existen endpoint, DTO, filtros definitivos ni respuesta funcional actual. |
| Flujos alternos | Ausencia de cupos, referencias invalidas y errores por rol requieren contrato futuro; no se les asignan codigos HTTP aqui. |
| Postcondiciones | Objetivo de lectura sin mutacion; la reserva posterior debera revalidar atomicamente el slot como ya hace CU-D7. |
| Reglas de negocio | DEC-007 aprueba minimizacion y separacion de exposicion, no campos concretos. Una consulta no garantiza que el slot siga disponible al reservar. DEC-010 no define ventanas temporales. |
| Contratos relacionados | Contrato de consulta de Agenda propuesto en C.3, sin firma ni implementacion; no es `AvailabilitySlotService` puntual ni el listado ADMIN. |
| Persistencia | `availability_slots`/`schedules` V1 serian fuente futura; no existe persistencia nueva ni vista funcional Java. |
| Seguridad | PATIENT/RECEPTIONIST son actores conceptuales; no hay permiso activo para consulta sanitizada. No se aprueba acceso anonimo o PROFESSIONAL. |
| Tests asociados | Ninguna prueba de sanitizacion, roles o filtros para esta vista. |

## 8. CU-D7 - Reserva de cita

| Campo | Contenido |
|---|---|
| ID | CU-D7; RF-013. |
| Nombre | Solicitar cita medica e incluir reserva de slot disponible. |
| Estado | **IMPLEMENTADO** para RF-013 actual; no representa todo el ciclo posterior de Appointments. |
| Actor principal | PATIENT para su perfil activo, o ADMIN/RECEPTIONIST para paciente activo indicado. |
| Actores secundarios | Ninguno; Agenda, Patients y Professionals son proveedores modulares, no actores. |
| Objetivo | Crear una cita `SCHEDULED` en un slot utilizable sin doble asignacion. |
| Precondiciones | Rol autorizado; `slotId` requerido. PATIENT omite `patientId` y se resuelve su perfil activo; ADMIN/RECEPTIONIST lo indican explicitamente. La usabilidad y profesional activo se verifican dentro de la operacion, no mediante consulta previa obligatoria. |
| Flujo principal | 1. `POST /api/v1/appointments` recibe `CreateAppointmentRequestDTO(slotId, patientId?, reason?)`. 2. `AppointmentService` resuelve paciente via `PatientLookupService`/`CurrentUserService`. 3. `AvailabilitySlotReservationService.reserveUsableSlot(slotId)` cambia AVAILABLE a RESERVED si el schedule esta activo y devuelve `AvailabilitySlotReference`. 4. Se valida profesional activo con `ProfessionalLookupService`; se persiste cita `SCHEDULED`, `flowStage=null`. 5. Controller devuelve `201 AppointmentResponseDTO` y `Location`. |
| Flujos alternos | Paciente ausente/inactivo: 404 `PATIENT_NOT_AVAILABLE`; slot no usable/contencion: 409 `SLOT_UNAVAILABLE`; profesional inactivo: 409 `PROFESSIONAL_NOT_AVAILABLE`; PATIENT con `patientId` explicito: 403; `slotId` ausente: 400 de validacion. Un fallo posterior revierte la reserva. |
| Postcondiciones | Exito: cita persistida y slot RESERVED. Fallo: sin cita ni reserva parcial. |
| Reglas de negocio | Profesional y especialidad se derivan del slot/schedule; el cliente no los fija. No se exige consulta CU-D6 ni se aplica ventana temporal. Reserva y cita comparten transaccion. |
| Contratos relacionados | `PatientLookupService`, `CurrentUserService`, `AvailabilitySlotReservationService` y `AvailabilitySlotReference`, `ProfessionalLookupService`; sin repositories externos en Appointments. |
| Persistencia | `appointments` V1/V3 y `availability_slots` V1; `AppointmentRepository` y repository de Agenda permanecen dentro de sus modulos. |
| Seguridad | `@PreAuthorize` permite PATIENT, ADMIN y RECEPTIONIST; el service controla perfil propio frente a creacion administrativa. |
| Tests asociados | `AppointmentModuleIT.createsAppointmentAndReservesSlotAgainstPostgreSql`, `rejectsReservedBlockedAndInactiveScheduleSlots`, `rollsBackReservedSlotWhenAppointmentInsertFails`; tests de controller, service y autorizacion. |

## 9. CU-D8 - Control de doble reserva

| Campo | Contenido |
|---|---|
| ID | CU-D8; RF-014, invariante incluido por CU-D7. |
| Nombre | Validar disponibilidad atomica al reservar. |
| Estado | **IMPLEMENTADO** como regla tecnica, no interaccion humana ni endpoint independiente. |
| Actor principal | Ninguno; Sistema designa el ejecutor tecnico, no un rol. |
| Actores secundarios | Los solicitantes de CU-D7 originan la competencia por un mismo slot. |
| Objetivo | Evitar dos reservas efectivas del mismo slot y dos citas activas sobre el. |
| Precondiciones | Solicitudes compiten por un slot inicialmente utilizable; el cliente no coordina la exclusion. |
| Flujo principal | Cada transaccion intenta el UPDATE condicional `AVAILABLE -> RESERVED` con schedule activo. Solo la que modifica una fila continua; V3 refuerza con indice unico parcial para citas activas `SCHEDULED`/`CONFIRMED`. |
| Flujos alternos | El competidor sin actualizacion efectiva recibe conflicto `SLOT_UNAVAILABLE`; un fallo de persistencia revierte reserva y cita dentro de la transaccion. No se promete orden de llegada/ganador. |
| Postcondiciones | A lo sumo una reserva efectiva y una cita activa por slot; sin efecto parcial del perdedor. |
| Reglas de negocio | La contencion es interna de CU-D7. RF-014 pide 20 solicitudes simultaneas en SRS; **la evidencia actual usa 2**, por lo que la cobertura de ese escenario permanece pendiente. |
| Contratos relacionados | `AvailabilitySlotReservationService.reserveUsableSlot(UUID)`; UPDATE interno de Agenda. No existe contrato HTTP propio de CU-D8. |
| Persistencia | `availability_slots` V1; `appointments` V1/V3; indice parcial `uq_appointments_active_slot` V3. V2 solo crea refresh tokens y no participa en este caso. |
| Seguridad | Hereda roles y ownership de CU-D7; Sistema no requiere permiso separado. |
| Tests asociados | `AppointmentModuleIT.allowsOnlyOneOfTwoConcurrentReservations` usa PostgreSQL y 2 solicitudes; `AppointmentPersistenceIT.rejectsTwoActiveAppointmentsForTheSameSlot` verifica restriccion. No existe ensayo de 20 solicitudes. |

## 10. Trazabilidad y limite para D.3

| RF | Caso | Estado D.2 | Evidencia o decision principal |
|---|---|---|---|
| RF-007 | CU-D1 | PARCIAL | ProfessionalController/Service y V1; DEC-005/016 pendientes. |
| RF-007 | CU-D2 | CONCEPTUAL | DEC-006 y tabla N:M V1; sin flujo Java. |
| RF-008 | CU-D3 | CONCEPTUAL | DEC-006 fija Catalogs; gestion no implementada, sin CRUD Java. |
| RF-011 | CU-D4 | PARCIAL | AgendaController/Service, `schedules` V1; DEC-008/010 abiertas. |
| RF-012 | CU-D5 | IMPLEMENTADO para ADMIN | Dos GET actuales; `usable` derivado. |
| RF-012 | CU-D6 | CONCEPTUAL | DEC-007; sin vista sanitizada Java/API. |
| RF-013 | CU-D7 | IMPLEMENTADO | Reserva/cita transaccionales actuales. |
| RF-014 | CU-D8 | IMPLEMENTADO | UPDATE condicional, indice V3 y prueba con 2 solicitudes; cobertura SRS de 20 pendiente. |

D.3 puede modelar estos flujos manteniendo separados los pasos ejecutables de
los objetivos conceptuales. No debe convertir las fichas CU-D2, CU-D3 o CU-D6
en endpoints, DTOs, permisos o contratos Java existentes ni inferir reglas
temporales de DEC-009. No se crea funcionalidad ni se cambia el esquema.
