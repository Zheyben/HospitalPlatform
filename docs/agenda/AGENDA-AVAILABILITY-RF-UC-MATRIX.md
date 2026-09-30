# Agenda Availability + Specialty Policy: matriz RF -> UC

## 1. Corte y criterio de lectura

ETAPA C.1, solo trazabilidad y diseño. HOSPITALPLATFORM es un caso de estudio academico; las
decisiones del proyecto no representan aprobacion del Hospital de Huaycan. Corte auditado:
`feat/agenda-availability-specialty-design`, HEAD `81aa536` (merge PR #24). Antes de crear este
archivo, el arbol de trabajo estaba limpio.

Las fuentes de RF son los identificadores canonicos RF-001 a RF-033 de
`docs/requisitos/SRS-HOSPITALPLATFORM.md`, contrastados con `docs/DOMAIN-BASELINE.md` seccion 8.
Los nombres de casos de uso que siguen son **etiquetas descriptivas de esta matriz**, derivadas
del texto de esos RF y de operaciones existentes; no son IDs UC oficiales ni una nueva
especificacion funcional. `IMPLEMENTADO` describe codigo actual; `APROBADO CONCEPTUALMENTE`
describe DEC-001/002/006/007/009/019 en `docs/DOMAIN-DECISION-REGISTER.md` sin implicar codigo.

Las rutas efectivas incluyen `server.servlet.context-path: /api/v1` de
`apps/backend/src/main/resources/application.yml`. Ninguna ruta de este documento es una
propuesta: solo se enumeran mappings existentes. Cuando no hay mapping se indica
`NO EXISTE - PENDIENTE DE DISENO`.

## 2. Matriz principal

| ID | RF (texto canonico resumido) | Estado | Actor | Caso de uso (etiqueta descriptiva) | Flujo verificado / pendiente | Endpoint / contrato | Persistencia / modulo | Prueba | Evidencia |
|---|---|---|---|---|---|---|---|---|---|
| RF-007 | Administrar profesionales | `PARCIAL IMPLEMENTADO`; asociacion N:M `APROBADA CONCEPTUALMENTE / NO IMPLEMENTADA` | ADMIN actual | Mantener profesional y su asociacion de especialidades | Crear con `licenseNumber` y `userId` opcional; consultar; actualizar solo `licenseNumber`; asociar N:M aun sin flujo | `POST/GET /api/v1/professionals`, `GET/PUT /api/v1/professionals/{id}` reales; asociacion: `NO EXISTE - PENDIENTE DE DISENO`; `ProfessionalLookupService` solo valida existencia/vinculo de usuario | `professionals` tiene entidad/repository/service; `professional_specialties` existe solo en V1; Catalogs carece de implementacion funcional | UNIT `ProfessionalServiceTest.createsProfessionalWithUniqueLicense`, `updatesProfessionalBasicInfo`; METHOD-SECURITY `ProfessionalControllerAuthorizationTest`; asociacion: `NO EXISTE - COBERTURA PENDIENTE` | SRS RF-007; Baseline RF-007; DEC-006; `ProfessionalController`, `ProfessionalService`, V1 |
| RF-008 | Administrar catálogo de especialidades | `DOCUMENTADO / NO IMPLEMENTADO`; definiciones bajo Catalogs `APROBADAS CONCEPTUALMENTE` | ADMIN documentado; sin operacion real de Catalogs | Mantener definiciones de especialidad | El RF describe crear/editar/activar/desactivar; no hay flujo Java del catalogo | `NO EXISTE - PENDIENTE DE DISENO` para API y contrato publico de especialidades | V1 `specialties` con `active`, `deleted_at` y nombre unico; sin Specialty entity/repository/service/controller | `NO EXISTE - COBERTURA PENDIENTE` especifica del catalogo | SRS RF-008; Baseline RF-008; DEC-006; V1; inventario de `apps/backend/src/main/java/com/hospital/platform/catalogs` |
| RF-011 | Configurar agenda y slots | `PARCIAL IMPLEMENTADO`; generacion excluida y `BLOQUEADA POR DEC-008`; ventanas/calendario `BLOQUEADOS POR DEC-008 / DEC-010` | ADMIN actual | Configurar y consultar horarios | Crear/consultar/actualizar/cambiar `active` del Schedule; valida profesional activo y `endTime > startTime`; `specialtyId` pasa como UUID y la FK controla existencia; no crea slots | `POST/GET /api/v1/agendas`, `GET/PUT /api/v1/agendas/{id}`, `PATCH /api/v1/agendas/{id}/status` reales; `ProfessionalLookupService.existsActiveProfessional(UUID)` real; generacion: `NO EXISTE - PENDIENTE DE DISENO` y fuera del incremento | V1 `schedules` y `availability_slots`; `Schedule`, `AvailabilitySlot`, repositories de Agenda; `specialties` por FK, sin lookup de catalogo | UNIT `AgendaServiceTest.createsAgendaForActiveProfessional`, `rejectsInvalidTimeRange`, `changesAgendaStatus`; CONTROLLER `AgendaControllerTest`; METHOD-SECURITY `AgendaControllerAuthorizationTest`; generacion: `NO EXISTE - COBERTURA PENDIENTE` | SRS RF-011; Baseline RF-011; ADR-006; DEC-008/009/010; `AgendaService`, V1 |
| RF-012 | Consultar disponibilidad reservable | `PARCIAL IMPLEMENTADO` para ADMIN; vistas PATIENT/RECEPTIONIST `APROBADAS CONCEPTUALMENTE / NO IMPLEMENTADAS` | ADMIN actual; PATIENT y RECEPTIONIST solo conceptuales para esta operacion | Consultar slots existentes y distinguir usabilidad | Lectura de slots existentes por filtros `scheduleId`, `professionalId`, `slotDate`, `status`; respuesta lista con `usable = AVAILABLE && schedule.active`; no hay filtro `specialtyId`, sanitizacion diferenciada ni consulta por los nuevos actores | `GET /api/v1/availability`, `GET /api/v1/availability/{id}` reales, ambos ADMIN-only; `AvailabilitySlotService.existsSlot/isAvailable/isUsable(UUID)` es contrato publico de Agenda para consumidores de otros modulos, no endpoint de listado; vista sanitizada: `NO EXISTE - PENDIENTE DE DISENO` | `availability_slots` -> `schedules` -> `professionals`/`specialties` via FKs; `AvailabilitySlotRepository.findAvailability`, `AgendaService`; no Specialty entity | UNIT `AgendaServiceTest.findsAvailabilityAndExposesUsableState`; CONTROLLER `AgendaControllerTest.returnsAvailabilityResponse`; METHOD-SECURITY `AgendaControllerAuthorizationTest` prueba ADMIN y rechazo PROFESSIONAL; PostgreSQL `HospitalPlatformApplicationIT` prueba `isUsable` para slots activos/inactivos/reservados/bloqueados, no el listado; PATIENT/RECEPTIONIST y sanitizacion: `NO EXISTE - COBERTURA PENDIENTE`; no IT PostgreSQL especifica de consulta | SRS RF-012; Baseline RF-012; DEC-007/019; `AgendaController`, `AvailabilitySlotResponseDTO`, `AvailabilitySlotRepository` |
| RF-013 | Reservar cita | `IMPLEMENTADO` como dependencia existente; no se rediseña en C.1 | PATIENT propio; ADMIN/RECEPTIONIST para paciente activo | Reservar cita desde un slot elegido | Appointments vuelve a validar y reserva condicionalmente el slot, deriva profesional/especialidad y crea cita `SCHEDULED`; descubrir disponibilidad antes es una interaccion futura, no una dependencia ya implementada en la UI | `POST /api/v1/appointments` real; `AvailabilitySlotReservationService.reserveUsableSlot(UUID)` y `AvailabilitySlotReference` reales | `appointments`, `availability_slots`, `schedules`; Appointments consume contrato publico de Agenda | INTEGRATION PostgreSQL `AppointmentModuleIT.createsAppointmentAndReservesSlotAgainstPostgreSql`, `rejectsReservedBlockedAndInactiveScheduleSlots` | SRS RF-013; Baseline RF-013; `AppointmentController`, `AppointmentService`, contrato Agenda, V1/V3 |
| RF-014 | Impedir doble asignación concurrente | `IMPLEMENTADO` para reserva actual; cobertura SRS pendiente; fuera de nuevo diseno | Sistema (actor tecnico), solicitudes competidoras | Asegurar una sola reserva efectiva | UPDATE condicional de slot y unicidad de cita activa; perdedor recibe conflicto sin cita parcial | `AvailabilitySlotReservationService.reserveUsableSlot(UUID)` real, invocado desde `POST /api/v1/appointments`; no API propia | `availability_slots.status`; indice parcial `uq_appointments_active_slot` en V3; modulos Agenda/Appointments | PostgreSQL `AppointmentModuleIT.allowsOnlyOneOfTwoConcurrentReservations` demuestra exclusion mutua con 2 solicitudes concurrentes; `AppointmentPersistenceIT.rejectsTwoActiveAppointmentsForTheSameSlot` demuestra la restriccion de unicidad. Requisito SRS: 20 solicitudes simultaneas. Estado de cobertura: `COBERTURA PENDIENTE` para ese escenario, a validar en una etapa posterior de calidad; C.1 no afirma su cumplimiento. | SRS RF-014; Baseline RF-014; `AvailabilitySlotRepository`, V3 |

RF-004 (autorizacion) es dependencia transversal ya parcialmente implementada; no se deriva un UC
nuevo para roles. RF-015 (constancia), RF-018 (liberacion), RF-019 (reprogramacion) y RF-030
(consulta operacional por rol) se relacionan con slots/agenda, pero su ampliacion no pertenece a
este incremento. Su funcionalidad actual permanece en Appointments/Agenda y sus partes abiertas
siguen el baseline. Ninguna de esas referencias autoriza no-show, waitlist o notificaciones.

## 3. Actores y casos de uso

| Actor | Evidencia actual | Alcance en esta matriz |
|---|---|---|
| ADMIN | `@PreAuthorize` en Agenda y Professionals; controller/security tests | Gestiona profesionales y schedules; consulta los slots actuales. Podra recibir detalle operativo adicional segun DEC-007, sin implementacion nueva aun. |
| PATIENT | Rol y operaciones propias de citas; `AppointmentControllerAuthorizationTest` y `AppointmentModuleIT` | Reserva propia implementada. Consulta sanitizada de disponibilidad solo aprobada conceptualmente; no autorizada hoy por `AgendaController`. |
| RECEPTIONIST | Rol y operaciones de citas; annotations en `AppointmentController` | Reserva para paciente activo implementada. Consulta sanitizada de disponibilidad solo aprobada conceptualmente. |
| PROFESSIONAL | Rol y ownership de atencion; `ProfessionalLookupService.isActiveProfessionalLinkedToUser` | No recibe permiso de consulta de disponibilidad por DEC-007. No se agrega UC profesional de agenda. |
| Sistema | Actualizacion condicional de slot y restricciones SQL | Actor tecnico de RF-014; no rol humano ni endpoint propio. |
| TRIAGE / SYSTEM / reviewer / auditor | Menciones o enums, sin operacion aprobada para este incremento | Documentales/historicos fuera del alcance de DEC-002. |

Los nombres "Mantener profesional y su asociacion", "Mantener definiciones", "Configurar horarios",
"Consultar slots" y "Reservar cita" resumen RF existentes. **EVIDENCIA INSUFICIENTE** para
asignarles codigos UC oficiales: ninguna fuente auditada establece un catalogo UC canonico.

## 4. Inventario de endpoints y contratos reales

El prefijo `/api/v1` proviene del context path; las rutas de annotations en controllers no lo
incluyen literalmente. Las consultas de Agenda/Professionals retornan DTO o `List<DTO>` sin
envelope ni paginacion universal (DEC-019).

| Metodo y URI real | Controller / rol actual | Request -> response real | Service y dependencia | Prueba relevante |
|---|---|---|---|---|
| `POST /api/v1/professionals` | `ProfessionalController` / ADMIN | `CreateProfessionalRequestDTO` -> `201 ProfessionalResponseDTO` | `ProfessionalService`, `ProfessionalRepository` | `ProfessionalServiceTest.createsProfessionalWithUniqueLicense`; `ProfessionalControllerAuthorizationTest` |
| `GET /api/v1/professionals` | `ProfessionalController` / ADMIN | Sin body -> `List<ProfessionalResponseDTO>` | `ProfessionalService`, `ProfessionalRepository` | `ProfessionalServiceTest.findsActiveProfessionals`; `ProfessionalControllerAuthorizationTest` |
| `GET /api/v1/professionals/{id}` | `ProfessionalController` / ADMIN | UUID -> `ProfessionalResponseDTO` | `ProfessionalService`, `ProfessionalRepository` | `ProfessionalServiceTest.findsProfessionalById`; `ProfessionalControllerAuthorizationTest` |
| `PUT /api/v1/professionals/{id}` | `ProfessionalController` / ADMIN | `UpdateProfessionalRequestDTO` -> `ProfessionalResponseDTO` | `ProfessionalService`, `ProfessionalRepository` | `ProfessionalServiceTest.updatesProfessionalBasicInfo`; `ProfessionalControllerAuthorizationTest` |
| `POST /api/v1/agendas` | `AgendaController` / ADMIN | `CreateAgendaRequestDTO` -> `201 AgendaResponseDTO` | `AgendaService`, `ScheduleRepository`, `ProfessionalLookupService`; specialty FK | `AgendaServiceTest.createsAgendaForActiveProfessional`; `AgendaControllerTest.createsAgendaAndReturnsCreatedResponse` |
| `GET /api/v1/agendas` | `AgendaController` / ADMIN | Filtros opcionales `professionalId`, `specialtyId` -> `List<AgendaResponseDTO>` | `AgendaService`, `ScheduleRepository` | `AgendaServiceTest.findsAgendasUsingFilters`; `AgendaControllerAuthorizationTest` |
| `GET /api/v1/agendas/{id}` | `AgendaController` / ADMIN | UUID -> `AgendaResponseDTO` | `AgendaService`, `ScheduleRepository` | `AgendaServiceTest.rejectsUnknownAgenda`; `AgendaControllerAuthorizationTest` |
| `PUT /api/v1/agendas/{id}` | `AgendaController` / ADMIN | `UpdateAgendaRequestDTO` -> `AgendaResponseDTO` | `AgendaService`, `ScheduleRepository`, `ProfessionalLookupService`; specialty FK | `AgendaServiceTest.updatesAgendaConfiguration`; `AgendaControllerAuthorizationTest` |
| `PATCH /api/v1/agendas/{id}/status` | `AgendaController` / ADMIN | `UpdateAgendaStatusRequestDTO` -> `AgendaResponseDTO` | `AgendaService`, `ScheduleRepository` | `AgendaServiceTest.changesAgendaStatus`; `AgendaControllerAuthorizationTest` |
| `GET /api/v1/availability` | `AgendaController` / ADMIN | Filtros opcionales `scheduleId`, `professionalId`, `slotDate`, `status` -> `List<AvailabilitySlotResponseDTO>` | `AgendaService`, `AvailabilitySlotRepository` | `AgendaServiceTest.findsAvailabilityAndExposesUsableState`; `AgendaControllerTest.returnsAvailabilityResponse`; `AgendaControllerAuthorizationTest` |
| `GET /api/v1/availability/{id}` | `AgendaController` / ADMIN | UUID -> `AvailabilitySlotResponseDTO` | `AgendaService`, `AvailabilitySlotRepository` | `AgendaServiceTest.rejectsUnknownAvailabilitySlot`; `AgendaControllerAuthorizationTest` |
| `POST /api/v1/appointments` | `AppointmentController` / PATIENT, ADMIN, RECEPTIONIST | `CreateAppointmentRequestDTO` -> `201 AppointmentResponseDTO` | `AppointmentService`, contratos `PatientLookupService`, `ProfessionalLookupService`, `AvailabilitySlotReservationService` | `AppointmentModuleIT.createsAppointmentAndReservesSlotAgainstPostgreSql`; `AppointmentControllerAuthorizationTest` |
| `GET /api/v1/appointments` | `AppointmentController` / PATIENT, ADMIN, RECEPTIONIST | Sin body -> `List<AppointmentResponseDTO>` | `AppointmentService`, ownership mediante `PatientLookupService`/`CurrentUserService` | `AppointmentControllerAuthorizationTest`; `AppointmentModuleIT` |
| `GET /api/v1/appointments/{id}` | `AppointmentController` / PATIENT, ADMIN, RECEPTIONIST | UUID -> `AppointmentResponseDTO` | `AppointmentService`, ownership mediante `PatientLookupService`/`CurrentUserService` | `AppointmentControllerAuthorizationTest`; `AppointmentModuleIT` |

**NO EXISTE endpoint de disponibilidad para PATIENT o RECEPTIONIST implementado.** Si la frase
"endpoint de disponibilidad" se entiende sin distinguir actor, si existen dos endpoints ADMIN
de lectura. No existe endpoint de catalogo de especialidades ni de asignacion N:M. Las rutas de
especialidades presentes en `docs/06-API-SPECIFICATION.md` no son mappings actuales.

Contratos publicos verificados: `ProfessionalLookupService` expone existencia activa y vinculo
profesional-usuario; `AvailabilitySlotService` expone existencia, disponibilidad persistida y
usabilidad; `AvailabilitySlotReservationService` retorna `AvailabilitySlotReference` minimo;
`AvailabilitySlotReleaseService` libera un slot reservado. `PatientLookupService` y
`CurrentUserService` son dependencias de Appointments/ownership. No hay contrato publico para
catalogo de especialidades, asignacion N:M ni consulta sanitizada de disponibilidad.

## 5. Persistencia real

| Tabla / migracion | Entidad y repository | Relacion / constraint relevante | Limite de evidencia |
|---|---|---|---|
| `specialties` / V1 | Sin entidad ni repository Java de Catalogs | UUID PK, `name UNIQUE`, `active`, `deleted_at`; FK desde `professional_specialties` y `schedules` | Una FK valida existencia, no especialidad activa ni asociacion al profesional. |
| `professional_specialties` / V1 | Sin entidad ni repository Java | PK compuesta (`professional_id`, `specialty_id`), ambas FK; soporta N:M fisica | DEC-006 aprueba N:M de negocio, pero aun no hay asignacion funcional. |
| `professionals` / V1 | `Professional`, `ProfessionalRepository` | UUID PK, `license_number UNIQUE`, `user_id UNIQUE` nullable, `deleted_at` | No hay coleccion Java de especialidades. |
| `schedules` / V1 | `Schedule`, `ScheduleRepository` | FK a profesional y especialidad; `day_of_week 0..6`, `end_time > start_time`, `active` | La FK no comprueba especialidad activa ni asignacion N:M. |
| `availability_slots` / V1 | `AvailabilitySlot`, `AvailabilitySlotRepository` | FK a schedule, estado `AVAILABLE/RESERVED/BLOCKED`, `UNIQUE(schedule_id, slot_date, start_time)`, `end_time > start_time`, indices por fecha/estado | No hay generacion de filas ni restriccion de solapamiento entre schedules. |
| `appointments` / V1 + V3 | `Appointment`, `AppointmentRepository` | FK a slot/paciente/profesional; V3 crea vinculo de sucesora e indice unico parcial de cita activa por slot | Reserva/lifecycle existentes; fuera del nuevo diseno. |
| `refresh_tokens` / V2 | Auth | Sin relacion con especialidades/disponibilidad | Auditada; no participa en C.1. |

No se hallo V4 ni infraestructura productiva de creacion/generacion de slots. La zona IANA
`America/Lima` esta aprobada conceptualmente en DEC-009; no hay configuracion de zona de negocio
ni conversion temporal implementada. UTC tecnico no equivale a esa politica.

## 6. GAPS IDENTIFICADOS

### GAP-A - Requerimiento sin implementacion

- RF-008: Catalogs carece de entidad, repository, service, controller, contrato y pruebas especificas.
- RF-007: asociacion profesional-especialidad N:M carece de flujo Java y prueba.
- RF-012: PATIENT/RECEPTIONIST no pueden consultar disponibilidad; no hay vista sanitizada ni sus pruebas.
- RF-011: la parte de generacion del SRS no existe y queda fuera de C.1; `BLOQUEADO POR DEC-008 / DEC-010` para calendario, duracion y reglas temporales.

### GAP-B - Capacidad sin RF

No se identifico una capacidad implementada de este nucleo sin RF: CRUD basico profesional
corresponde a RF-007; horarios/lectura de slots a RF-011/012; reserva y unicidad a RF-013/014.
La semantica especifica `isUsable = AVAILABLE && schedule.active` se cubre parcialmente por
RF-012/RB-004 y contrato Agenda; no se le asigna un RF nuevo.

### GAP-C - RF sin caso de uso claro

RF-007 mezcla mantenimiento profesional y asociacion N:M; RF-011 mezcla horarios y generacion;
RF-012 mezcla consulta, especialidad y seleccion reservable. El nombre e identificador UC
canonicos, sus alternos detallados y la descomposicion final requieren C.2. **EVIDENCIA
INSUFICIENTE** para imponer una numeracion UC oficial desde estas fuentes.

### GAP-D - Caso de uso sin endpoint

Catalogo de especialidades, asignacion profesional-especialidad y consulta sanitizada por
PATIENT/RECEPTIONIST carecen de endpoint implementado. No se propone una URI en C.1.

### GAP-E - Endpoint sin prueba suficiente

La disponibilidad ADMIN tiene tests unitarios de servicio, controller con mock y seguridad de
metodo (ADMIN permitido; PROFESSIONAL rechazado). `HospitalPlatformApplicationIT` prueba
`isUsable` con PostgreSQL real, pero no se hallo prueba especifica PostgreSQL de
`findAvailability`, ni prueba explicita de rechazo PATIENT/RECEPTIONIST ni de sanitizacion por rol.
La asociacion de especialidades no tiene endpoint ni prueba. No se equiparan tests de reserva
con cobertura de lectura publica.

### GAP-F - Documentacion contradictoria

- La SRS RF-011 describe generacion de slots; el codigo solo configura schedules y lee slots
  preexistentes. DEC-008 mantiene la generacion abierta y fuera de C.1.
- La SRS RF-012 y el flujo de producto presentan consulta PATIENT/RECEPTIONIST; los mappings
  reales `/availability` son ADMIN-only. DEC-007 aprueba el cambio solo conceptualmente.
- La API Specification enumera rutas `/specialties`; no existe controller de Catalogs. DEC-019
  hace del comportamiento actual de controllers el contrato funcional vigente.
- La SRS RF-008 contempla especialidades activas; Agenda comprueba FK de `specialtyId`, no
  actividad ni vinculo profesional-especialidad. La migracion tampoco impone esas reglas.
- `DOMAIN-BASELINE.md` seccion 18 aun cataloga como abiertas decisiones que B.1.1 cerro en
  `DOMAIN-DECISION-REGISTER.md`; baseline es el corte historico y registro es la decision vigente.
- La SRS conserva etiquetas estaticas `[PARCIAL]` para RF-013/014; codigo y pruebas PostgreSQL
  sustentan `IMPLEMENTADO` para la reserva/unicidad actuales en el baseline. La matriz conserva
  el ID y titulo de la SRS y usa el estado actual del baseline.
- El roadmap/ADR-005 menciona TRIAGE para operacion; el codigo y DEC-002 usan solo los cuatro
  roles actuales. TRIAGE no entra en C.1.

### GAP-G - Dependencia bloqueada por decision OPEN

- DEC-008 bloquea generacion/materializacion, duracion, horizonte, calendario y reglas de
  solapamiento. No se deriva generacion desde la existencia de la tabla.
- DEC-010 bloquea reglas sobre slot pasado, anticipacion y ventanas de reserva/cancelacion.
  DEC-009 solo fija la zona de negocio para un diseno posterior.
- DEC-003 bloquea un entorno piloto limpio con bootstrap; no bloquea la trazabilidad C.1.
- DEC-005 bloquea cambios al ciclo de vinculacion profesional-usuario; la validacion de
  ownership ya implementada no resuelve ese ciclo.

## 7. Gate para C.2

La trazabilidad permite pasar a C.2 para precisar casos de uso del incremento aprobado. C.2
debera resolver descomposicion RF-007/008/011/012, actor y datos minimos por vista, comportamiento
ante especialidad inactiva o no asociada y cobertura exigible, sin convertir este inventario en
endpoints/DTOs. El diseno de contratos concretos corresponde a C.3. DEC-008 y DEC-010 permanecen
fuera del incremento hasta aprobacion humana expresa.

No se ha implementado Agenda Availability para PATIENT/RECEPTIONIST ni Specialty Policy en Java.
