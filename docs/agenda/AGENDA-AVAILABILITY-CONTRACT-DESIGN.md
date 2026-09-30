# Agenda Availability + Specialty Policy
## Contract Design

### 1. Alcance

ETAPA C.3: diseno documental de contratos para RF-007/008/011/012 y sus
dependencias RF-013/014. `IMPLEMENTADO` describe codigo real, `EXISTENTE /
COMPATIBILIDAD` exige conservarlo, `PROPUESTO / DISENO` no es una API o interfaz
Java vigente y `PENDIENTE` requiere una decision posterior. Este documento no
autoriza codigo, endpoint, DTO, test, migracion ni cambio de seguridad.

### 2. Autoridad

HOSPITALPLATFORM es un caso de estudio academico con autoridad del proyecto
academico/docente (DEC-001). Ninguna regla aqui implica aprobacion institucional
del Hospital de Huaycan. DEC-006/007/009/019 estan CLOSED en el alcance de
diseno indicado, no por ello implementadas. Los controllers y contratos Java
son la fuente del comportamiento actual; la SRS expresa necesidades, no prueba
de implementacion.

### 3. Fuentes

- `docs/agenda/AGENDA-AVAILABILITY-RF-UC-MATRIX.md` (C.1) y
  `docs/agenda/AGENDA-AVAILABILITY-USE-CASES.md` (C.2).
- `docs/requisitos/SRS-HOSPITALPLATFORM.md`, `docs/DOMAIN-BASELINE.md`,
  `docs/DOMAIN-DECISION-REGISTER.md`, `docs/06-API-SPECIFICATION.md`.
- `docs/contracts/MODULE-CONTRACTS-ARCHITECTURE.md`, arquitectura de
  Appointments y ADR-001/005/006/007/009/010.
- Controllers, services, DTOs, contracts, repositories, entities y tests de
  Agenda, Professionals, Appointments y Patients; Catalogs solo contiene
  `package-info.java`. Migraciones oficiales V1, V2 y V3.

### 4. Principios contractuales

1. Catalogs posee definiciones de especialidad; Professionals posee la
   asignacion N:M; Agenda posee schedules/slots; Appointments posee citas.
2. Un consumidor entre modulos recibe identificadores o referencias minimas
   mediante un contrato publico; no usa entidades o repositories del proveedor.
3. Un contrato propuesto no modifica los contratos existentes ni concede un
   permiso. La autorizacion HTTP y el resultado de dominio son fronteras
   distintas.
4. FK a `specialties` acredita existencia fisica, no `active`, no ausencia de
   `deleted_at` ni asignacion al profesional. Una fila de slot no implica
   generacion o publicacion reservable.
5. La consulta es una fotografia; `POST /api/v1/appointments` vuelve a reservar
   condicionalmente. No se promete que un slot siga libre tras consultarlo.
6. No se definen plazos, zona aplicada, duracion, calendario, solapamiento,
   paginacion universal, estados nuevos ni auditoria adicional.

### 5. Contratos existentes

| Frontera real | Firma/capacidad verificada | Consumidor actual o alcance | Estado |
|---|---|---|---|
| `ProfessionalLookupService` (Professionals) | `existsActiveProfessional(UUID)`, `isActiveProfessionalLinkedToUser(UUID, UUID)` | Agenda valida profesional activo; Appointments valida profesional/ownership. | IMPLEMENTADO; EXISTENTE / COMPATIBILIDAD. |
| `PatientLookupService` (Patients) | `existsPatient(UUID)`, `existsActivePatient(UUID)`, `findPatientReference(UUID)`, `findActivePatientReferenceByUserId(UUID)` | Appointments; no se amplia en C.3. | IMPLEMENTADO; EXISTENTE / COMPATIBILIDAD. |
| `CurrentUserService` (Users) | `currentUserId()` | Appointments resuelve actor/paciente; no se amplia. | IMPLEMENTADO; EXISTENTE / COMPATIBILIDAD. |
| `AvailabilitySlotService` (Agenda) | `existsSlot(UUID)`, `isAvailable(UUID)`, `isUsable(UUID)` | Consulta puntual interna/publica entre modulos, **no** listado HTTP. `isAvailable` solo comprueba estado AVAILABLE; `isUsable` tambien exige schedule activo. | IMPLEMENTADO; EXISTENTE / COMPATIBILIDAD. |
| `AvailabilitySlotReservationService` (Agenda) | `reserveUsableSlot(UUID)` devuelve `AvailabilitySlotReference(slotId, professionalId, specialtyId, slotDate, startTime, endTime)` | Appointments reserva de forma condicional; no se renombra ni extiende. | IMPLEMENTADO; EXISTENTE / COMPATIBILIDAD. |
| `AvailabilitySlotReleaseService` (Agenda) | `releaseReservedSlot(UUID)` | Appointments cancela/reprograma; fuera del incremento. | IMPLEMENTADO; EXISTENTE / COMPATIBILIDAD. |

`AvailabilitySlotService.isUsable` no aplica fecha pasada ni especialidad
activa/asignada. El UPDATE de reserva exige slot AVAILABLE y schedule activo;
Appointments comprueba aparte profesional activo. No existe lookup Java de
specialty ni asignacion N:M ni consulta publica sanitizada.

### 6. Specialty contracts

**Estado: PROPUESTO / DISENO; NO IMPLEMENTADO.** RF-008 describe consulta,
creacion, edicion y activacion/desactivacion; DEC-006 fija Catalogs como
propietario. `docs/06-API-SPECIFICATION.md` enumera `/specialties`, pero ningun
controller actual lo expone: esas rutas no son contrato funcional vigente
(DEC-019). No se adopta ninguna URI en C.3.

| Capacidad contractual candidata | Actor/consumidor | Entrada minima respaldada | Salida minima propuesta | Error o limite pendiente |
|---|---|---|---|---|
| Consulta de definiciones | ADMIN para gestion segun RF-008; otros lectores PENDIENTE DE DECISION | `id` para detalle, o consulta de catalogo sin filtro fijado | Identidad `id`, `name`; `description` y `active` solo donde la vista administrativa lo necesite | Ausente/inactiva; alcance de lectura por rol PENDIENTE. |
| Crear definicion | ADMIN segun RF-008 | `name` obligatorio en V1; `description` opcional en V1 | `id`, `name`, `description`, `active` como representacion propuesta | Nombre duplicado (`name UNIQUE`); validaciones adicionales PENDIENTES. |
| Actualizar definicion | ADMIN segun RF-008 | `id`, campos de V1 cuya editabilidad se apruebe | Representacion actualizada, sin exponer `deleted_at` | Edicion parcial/total, colision de nombre e historial PENDIENTES. |
| Activar/desactivar | ADMIN segun RF-008 | `id`, `active` de V1 como dato candidato | Estado resultante | Efecto sobre schedules/slots/citas existentes PENDIENTE DE DECISION. |
| Consultar definiciones habilitadas para asociacion | Professionals como consumidor de Catalogs; ADMIN en borde de gestion | `specialtyId` para lookup o conjunto consultable | Veredicto/identidad minima; no entidad JPA | Semantica conjunta de `active` y `deleted_at` PENDIENTE DE DECISION. |

**Contrato publico candidato** de Catalogs para consultas entre modulos:
lookup por UUID de existencia/estado de especialidad, sin `Specialty` JPA.
Nombre, firma, excepciones, respuesta HTTP y DTO futuros: **PROPUESTOS,
PENDIENTES DE APROBACION**. Los comandos de gestion pertenecen a Catalogs;
el futuro borde HTTP no se deduce de la tabla. V1 solo tiene `id`, `name`,
`description`, `active`, `deleted_at` y timestamps; no tiene `code`. La SRS
menciona codigo duplicado: no se agrega un campo por inferencia.

### 7. Professional-Specialty contract

**Estado: PROPUESTO / DISENO; NO IMPLEMENTADO.** RF-007 y DEC-006 permiten
N:M y asignan su gestion a Professionals. V1 `professional_specialties` tiene
PK (`professional_id`, `specialty_id`) y FK a ambas tablas; no tiene columna de
estado. No existe entity/repository/service/controller ni contrato publico de
asignacion en Java.

- **Actor/ownership:** ADMIN para gestion segun SRS; Professionals es
  propietario. Agenda seria consumidor de una consulta minima, no del join ni
  de `ProfessionalRepository`.
- **Operacion contractual candidata de gestion:** asociar profesional y
  especialidad. Entrada minima: dos UUID (`professionalId`, `specialtyId`).
  Salida minima propuesta: confirmacion de la pareja asociada, sin entidad.
  Desasociar, reemplazar conjunto, reactivar e idempotencia: PENDIENTE DE
  DECISION; no se presumen comandos aprobados.
- **Consulta publica candidata para Agenda:** verificar si la pareja UUID esta
  asociada. Respuesta minima propuesta: booleano o veredicto equivalente.
  Nombre/firma Java y error concreto: PENDIENTES; no se atribuye a
  `ProfessionalLookupService` actual.
- **Dependencia con Catalogs:** Professionals necesitara una consulta publica
  de definicion/estado para cumplir la SRS sobre especialidades habilitadas.
  Reglas precisas sobre `active`, `deleted_at`, asignaciones antiguas y
  desactivacion concurrente: PENDIENTES.
- **Errores propuestos, no vigentes:** profesional inexistente/inactivo,
  especialidad inexistente/inactiva, asociacion duplicada/invalida, datos
  invalidos o acceso sin ADMIN. HTTP, codigos y precedencia: PENDIENTES.

### 8. Schedule validation contract

| Condicion | Actual (IMPLEMENTADO) | Candidato PROPUESTO / DISENO | Fuente / pendiente |
|---|---|---|---|
| Profesional existe y activo | `AgendaService` usa `ProfessionalLookupService.existsActiveProfessional(UUID)`. | Conservar firma y comportamiento. | Codigo; DEC-005 no autoriza nuevo ciclo User-Professional. |
| Especialidad existe | FK `schedules.specialty_id -> specialties.id`; referencia inexistente produce `INVALID_SPECIALTY_REFERENCE` en caso identificado. | Lookup de Catalogs permitiria error de dominio antes de persistir. | V1; contrato/error futuros PENDIENTES. |
| Especialidad habilitada | **No se comprueba** `active` ni `deleted_at` en Agenda. | Consultar estado a Catalogs antes de aceptar una specialty. | RF-007/008/011, RB-003, DEC-006; semantica exacta PENDIENTE. |
| Profesional asociado a especialidad | **No se comprueba** `professional_specialties`. | Consultar asignacion mediante contrato publico de Professionals. | RF-007/011, DEC-006; error/politica PENDIENTES. |
| Horario valido | DTO exige dia 0..6; service/V1 exigen `endTime > startTime`. | Conservar. | Codigo/V1; solapamiento DEC-008 OPEN. |

`Schedule` actual guarda `professionalId`, `specialtyId`, `dayOfWeek`,
`startTime`, `endTime`, `active`. `POST/PUT /api/v1/agendas` son ADMIN-only.
No se propone importar `SpecialtyRepository`, `ProfessionalRepository` ni
`Specialty` JPA a Agenda. El orden de validaciones, su consistencia frente a
cambios concurrentes del catalogo/asignacion y el efecto sobre schedules
preexistentes requieren aprobacion antes de implementar.

### 9. Availability ADMIN contract

**Estado: IMPLEMENTADO; EXISTENTE / COMPATIBILIDAD.** Mantener sin cambio:

| Ruta | Actor | Request actual | Response actual | Error verificado |
|---|---|---|---|---|
| `GET /api/v1/availability` | ADMIN autenticado | Sin body; query opcional `scheduleId` UUID, `professionalId` UUID, `slotDate` ISO date, `status` enum `AVAILABLE/RESERVED/BLOCKED` | `200 List<AvailabilitySlotResponseDTO>`; lista vacia posible | Seguridad: anonimo 401, rol no admitido 403. Errores de binding no se formalizan aqui. |
| `GET /api/v1/availability/{id}` | ADMIN autenticado | `id` UUID | `200 AvailabilitySlotResponseDTO` | Slot inexistente: 404 `AVAILABILITY_SLOT_NOT_FOUND`; seguridad 401/403. |

Campos reales del DTO: `id`, `scheduleId`, `slotDate`, `startTime`, `endTime`,
`status`, `usable`. `usable = (status == AVAILABLE && schedule.active)`;
`isAvailable(UUID)` del contrato puntual **no** incluye `schedule.active`.
El listado no filtra automaticamente por usabilidad ni acepta `specialtyId`.
No hay envelope ni paginacion universal (DEC-019). No se modifica authorization
ni forma de respuesta. `AgendaService.findAvailability` tiene test unitario y
controller test con mock; no hay test PostgreSQL especifico de listado.

### 10. Availability sanitizada propuesta

**Estado: PROPUESTO / DISENO; NO IMPLEMENTADO.** DEC-007 aprueba solamente
PATIENT y RECEPTIONIST autenticados para descubrimiento sanitizado; ADMIN
conserva consulta operativa. PROFESSIONAL, TRIAGE, SYSTEM y anonimos no
reciben permiso nuevo. No hay URI, controller, metodo de contrato, DTO ni
consulta de listado para estos actores hoy. C.3 no fija una URI candidata.

| Aspecto | Propuesta delimitada | Fuente / pendiente |
|---|---|---|
| Objetivo y actor | Descubrir slots ofertables para seleccionar `slotId` antes de una reserva; PATIENT/RECEPTIONIST autenticados. | RF-012/013 y DEC-007. Consulta previa no es precondicion tecnica del POST actual. |
| Request/filtros candidatos | `slotDate` y `specialtyId` son datos existentes (V1/Schedule) y criterios narrados por RF-012; `professionalId` aparece en RF-012. | Obligatoriedad, combinaciones, rango y formato de filtro futuro: PENDIENTE DE DISENO; sin ventana temporal DEC-010. |
| Response minima candidata | `slotId`, `slotDate`, `startTime`, `endTime` para reconocer/seleccionar un slot; tipos ya existen en `AvailabilitySlotReference` y DTO ADMIN. | Exponer `specialtyId`, nombre o `professionalId` en la respuesta requiere decision por rol; no se presume. No hay clase DTO aprobada. |
| Criterio de oferta candidato | No ofrecer como reservable slots RESERVED/BLOCKED o schedule inactivo; volver a validar al reservar. | SRS RF-012; regla actual `isUsable`. Efecto de specialty inactiva/no asociada depende de contratos propuestos y politica pendiente. |
| No exponer por defecto | `scheduleId`, estado operativo bruto, `active` interno, `deleted_at`, `userId`, `licenseNumber`, datos personales/clinicos o detalles de persistencia. | DEC-007 exige minimizacion. La lista exacta de campos sanitizados requiere aprobacion; esto es limite de diseno, no DTO actual. |
| Errores/seguridad | Consulta solo autenticada y autorizada por rol; validacion de filtros, ausencia de datos y recurso inactivo requieren contrato de error. | 401/403 son patrones actuales, no respuestas ya probadas para un endpoint futuro; codigos y 404/409 futuros: PENDIENTES. |

El contrato de consulta publica de Agenda para esta vista es **PROPUESTO**,
separado de `AvailabilitySlotService` puntual y del listado ADMIN. Su firma,
respuesta HTTP, ruta, filtros definitivos, paginacion especifica e interaccion
con specialty/professional: PENDIENTES DE DISENO/APROBACION. Una vista puede
quedar desactualizada entre lectura y reserva; nunca sustituye
`reserveUsableSlot(UUID)`.

### 11. Relacion con Appointment reservation

`POST /api/v1/appointments` esta IMPLEMENTADO con
`CreateAppointmentRequestDTO(slotId, patientId, reason)`. PATIENT reserva para
su perfil activo; ADMIN/RECEPTIONIST pueden indicar paciente activo. El
service invoca `AvailabilitySlotReservationService.reserveUsableSlot(UUID)`,
recibe `AvailabilitySlotReference`, verifica profesional activo y persiste
`Appointment` SCHEDULED en una transaccion. No recibe especialidad o
professionalId del cliente como autoridad. No se altera endpoint, DTO,
contrato, estado ni reglas de ownership. Descubrimiento -> seleccion -> POST
es una relacion conceptual de producto, no una UI implementada.

RF-014: UPDATE condicional de Agenda y `uq_appointments_active_slot` de V3
protegen la reserva actual. `AppointmentModuleIT` demuestra 2 solicitudes
concurrentes sobre PostgreSQL; la SRS exige 20 simultaneas. **COBERTURA
PENDIENTE** para 20; no se crea SLA, metrica nueva ni contrato adicional.

### 12. Errores

| Categoria | EXISTENTE / evidencia actual | PROPUESTO / PENDIENTE |
|---|---|---|
| Autenticacion/autorizacion | SecurityFilterChain requiere autenticacion; Agenda/Professionals ADMIN; anonimo 401 y rol rechazado 403 en endpoints actuales. | Vista sanitizada PATIENT/RECEPTIONIST y comandos Catalogs/asignacion necesitan autorizacion futura; no esta instalada. |
| Datos invalidos | Agenda mapea validacion 400 `VALIDATION_ERROR`, rango 400 `INVALID_SCHEDULE_TIME`; FK specialty identificada 400 `INVALID_SPECIALTY_REFERENCE`. | Formato, obligatoriedad y error de filtros o campos futuros PENDIENTES. |
| Recurso inexistente/inactivo | Agenda: 404 `AGENDA_NOT_FOUND`, `AVAILABILITY_SLOT_NOT_FOUND`, `PROFESSIONAL_NOT_AVAILABLE`; Professional: 404 `PROFESSIONAL_NOT_FOUND`. | Specialty inexistente/inactiva y asociacion ausente requieren politica de error; no hay codigo vigente. |
| Conflicto | Professional: 409 `DUPLICATE_PROFESSIONAL`; Appointments: conflicto de slot no reservable. | `name UNIQUE` de V1 puede producir colision Catalogs; duplicado N:M por PK. Representacion HTTP e idempotencia de comandos futuros PENDIENTES. |

No se copian codigos existentes a Catalogs por analogia. Para cada contrato
propuesto, error de dominio, HTTP, precedencia y privacidad de mensajes
deberan aprobarse antes de implementarlo.

### 13. Seguridad

- Actual: `SecurityFilterChain` exige autenticacion salvo login/refresh;
  `@PreAuthorize(hasRole('ADMIN'))` protege los endpoints de Agenda y
  Professionals enumerados. Appointments aplica PATIENT/ADMIN/RECEPTIONIST y
  ownership actual para crear/consultar citas.
- PROPUESTO: solo ADMIN gestiona definiciones/asignaciones segun SRS; el borde
  HTTP de Catalogs/Professionals aun no existe. DEC-007 aprueba vista
  sanitizada para PATIENT/RECEPTIONIST autenticados, no acceso anonimo ni
  permiso para PROFESSIONAL. No se cambia SecurityFilterChain.
- PENDIENTE: lectura de catalogo por otros roles, campos por rol,
  granularidad de permisos (DEC-020 PROPOSED) y ataques por enumeracion/abuso.
  No se infiere privilegio de una FK ni del enum de roles.

### 14. Ownership modular

| Modulo | Propiedad | Frontera permitida / prohibida |
|---|---|---|
| Catalogs | Definiciones `specialties` (DEC-006). | Proponer lookup publico minimo; no exportar JPA/SQL. |
| Professionals | Perfil profesional y asignaciones `professional_specialties` (DEC-006). | Mantener `ProfessionalLookupService`; proponer consulta de asignacion separada sin exponer join/repository. |
| Agenda | `schedules`, `availability_slots`, consulta y transiciones de slot. | Consume contratos de Catalogs/Professionals cuando se aprueben; no accede a sus repositories/entities. |
| Appointments | `appointments` y orquestacion de reserva/lifecycle. | Mantiene contratos de Agenda/Patients/Professionals/Users; no importa `SpecialtyRepository`, `ProfessionalRepository`, `ScheduleRepository` ni entidades externas. |

### 15. Persistencia

V1: `specialties(id UUID, name UNIQUE, description, active, deleted_at,
created_at, updated_at)`; `professional_specialties` tiene PK compuesta de
dos UUID y FK hacia Catalogs/Professionals; `schedules` posee `specialty_id`
FK, `professional_id` FK, dia 0..6, horas y `active`; `availability_slots`
posee `slot_date`, estado AVAILABLE/RESERVED/BLOCKED y unicidad por
`(schedule_id, slot_date, start_time)`. V3 sustituye la unicidad global
de `appointments.slot_id` por indice parcial de cita activa; V2 crea
`refresh_tokens` y no participa en C.3. No existe V4. No se propone migracion
por la sola existencia de una capacidad conceptual; una futura implementacion
debera comprobar si V1 soporta las reglas finalmente aprobadas.

### 16. Compatibilidad

- Conservar rutas, filtros, roles y DTO de ambos GET ADMIN; no aplicar
  sanitizacion retrospectiva ni introducir envelope/paginacion universal.
- Conservar firmas de `ProfessionalLookupService`, `PatientLookupService`,
  `CurrentUserService`, `AvailabilitySlotService`, contratos de reserva y
  liberacion. Los contratos futuros serian aditivos y publicos por modulo.
- `POST /api/v1/appointments` conserva `CreateAppointmentRequestDTO` y
  revalidacion atomica. Una futura politica de specialty activa/asignada
  podria afectar elegibilidad de schedule, consulta y reserva: impacto a
  evaluar, no comportamiento aprobado ni cambio automatico de Appointments.
- La FK existente no elimina la necesidad de decidir consistencia si se
  desactiva una specialty o retira una asignacion concurrentemente.
- No se requiere por decreto una migracion, un estado nuevo o un cambio global
  de seguridad. Si una regla futura los exige, debe documentarse y aprobarse
  por separado.

### 17. Matriz RF -> UC -> Contract

`UC` es etiqueta descriptiva de C.2, no identificador oficial. En las filas
PROPUESTAS, request/response/error son candidatos de diseno, no clases ni
codigos HTTP existentes.

| RF / UC descriptivo | Actor / propietario | Contrato y estado | Request -> response | Errores | Persistencia / dependencia / decision | Test existente -> pendiente |
|---|---|---|---|---|---|---|
| RF-007 / Mantener profesional | ADMIN / Professionals | Controller/service actual: IMPLEMENTADO parcial | Create `(licenseNumber, userId?)`; PUT `(licenseNumber)` -> `ProfessionalResponseDTO` | 404 not found, 409 duplicate license actuales | `professionals` V1; DEC-005 OPEN, DEC-016 PROPOSED | `ProfessionalServiceTest`, `ProfessionalControllerAuthorizationTest` -> ciclo User/Professional pendiente. |
| RF-007 / Asociar profesional-especialidad | ADMIN / Professionals | Gestion y lookup publico: PROPUESTO / NO IMPLEMENTADO | Par `(professionalId, specialtyId)` -> confirmacion/veredicto propuestos | Inexistente, inactiva, duplicado, asociacion invalida: PENDIENTES | `professional_specialties` V1; Catalogs lookup futuro; DEC-006 CLOSED, politica concreta pendiente | Ninguno especifico -> pruebas de N:M, roles, invalidos y concurrencia pendientes. |
| RF-008 / Mantener definiciones | ADMIN / Catalogs | Gestion/lookup Catalogs: PROPUESTO / NO IMPLEMENTADO | `name`, `description?`, `id/active` segun operacion -> representacion minima propuesta | Nombre duplicado, ausente/inactiva, invalidos: PENDIENTES | `specialties` V1; DEC-006 CLOSED, DEC-003 OPEN | Ninguno de Catalogs -> CRUD/estado/FK/seguridad pendientes. |
| RF-011 / Configurar horarios | ADMIN / Agenda | `ProfessionalLookupService` + `AgendaService`: IMPLEMENTADO parcial; lookups Specialty/asignacion PROPUESTOS | `Create/UpdateAgendaRequestDTO` -> `AgendaResponseDTO` actuales | `INVALID_SCHEDULE_TIME`, `PROFESSIONAL_NOT_AVAILABLE`, `INVALID_SPECIALTY_REFERENCE` actuales; inactiva/no asignada PENDIENTES | `schedules` V1; DEC-006, DEC-008/010 OPEN | `AgendaServiceTest`, `HospitalPlatformApplicationIT` -> validacion por contratos futuros pendiente. |
| RF-012 / Consultar disponibilidad operativa | ADMIN / Agenda | `AgendaController`/`AgendaService`: EXISTENTE / COMPATIBILIDAD; `AvailabilitySlotService` es puntual separado | Query opcional actual -> `List<AvailabilitySlotResponseDTO>`; `{id}` -> DTO | 404 `AVAILABILITY_SLOT_NOT_FOUND` por id; 401/403 actuales | `availability_slots`, `schedules` V1; DEC-019 | Unit/controller/method security; `isUsable` PostgreSQL -> `findAvailability` PostgreSQL especifico pendiente. |
| RF-012 / Consultar disponibilidad sanitizada | PATIENT, RECEPTIONIST / Agenda | Consulta publica de Agenda: PROPUESTO / NO IMPLEMENTADO | Filtros candidatos fecha/especialidad; detalle definitivo PENDIENTE -> slotId/fecha/horas candidatos | 401/403 como requisito de acceso propuesto; invalidos, ausente/inactivo PENDIENTES | Slots/schedules V1; Catalogs/Professionals futuros; DEC-007 CLOSED, DEC-010 OPEN | Ninguno especifico -> roles, sanitizacion, filtros, revalidacion pendientes. |
| RF-013 / Reservar cita | PATIENT propio, ADMIN/RECEPTIONIST / Appointments | `AvailabilitySlotReservationService` y otros contratos actuales: IMPLEMENTADO; sin cambio | `CreateAppointmentRequestDTO` -> `AppointmentResponseDTO` | Slot no usable, paciente/profesional no disponible, ownership: actuales | `appointments` V1/V3, slot V1; DEC-010 OPEN | `AppointmentModuleIT` -> no se agrega contrato nuevo. |
| RF-014 / Una reserva efectiva | Sistema tecnico / Agenda + Appointments | UPDATE condicional + indice V3: IMPLEMENTADO para reserva actual | Mismo `slotId` competido -> un exito, otros conflictos | Conflicto actual, no nuevo codigo | Slot V1, indice V3; RF-013 | `AppointmentModuleIT` con 2 y `AppointmentPersistenceIT` -> ensayo SRS de 20 PENDIENTE. |

### 18. Implementado vs Propuesto

| IMPLEMENTADO / EXISTENTE-COMPATIBILIDAD | PROPUESTO / DISENO | PENDIENTE |
|---|---|---|
| Profesionales basicos ADMIN, schedules ADMIN, disponibilidad ADMIN, contratos puntuales/reserva/liberacion, cita y exclusion actual. | Gestion Catalogs, lookup de definicion/estado, gestion/lookup N:M de Professionals, validacion cruzada en Agenda y vista sanitizada de Agenda. | Rutas/DTO/firma/errores definitivos, campos por rol, semantica de `active`+`deleted_at`, asignacion y efectos sobre datos existentes. |
| V1/V2/V3 y sus constraints; `isUsable = AVAILABLE && schedule.active`. | Datos minimos candidatos respaldados por V1 y contratos existentes. | Generacion/solapamiento DEC-008, ventanas DEC-010, ciclo usuario DEC-005, ensayo 20 RF-014. |

### 19. Pruebas existentes

- Professionals: `ProfessionalServiceTest` y
  `ProfessionalControllerAuthorizationTest` verifican CRUD basico/rol, no
  asignacion N:M.
- Agenda: `AgendaServiceTest`, `AgendaControllerTest` y
  `AgendaControllerAuthorizationTest` cubren consulta y rol ADMIN con mocks;
  `HospitalPlatformApplicationIT` verifica FK specialty invalida y
  `isUsable` con PostgreSQL, no `findAvailability` con PostgreSQL.
- Reserva: `AppointmentModuleIT` verifica creacion/rollback y dos solicitudes
  competidoras en PostgreSQL; `AppointmentPersistenceIT` verifica el indice
  parcial. Ninguna de ellas prueba catalogo, vista sanitizada o 20 solicitudes.

### 20. Pruebas pendientes

- Catalogs: unicidad `name`, estados, errores/roles y compatibilidad con FK
  cuando exista implementacion autorizada.
- Professionals N:M: asignacion/consulta, pares invalidos/duplicados,
  definicion inactiva y carreras de actualizacion cuando se apruebe politica.
- Agenda: rechazo por specialty inactiva/no asociada bajo contratos aprobados;
  `findAvailability` con PostgreSQL; vista sanitizada PATIENT/RECEPTIONIST y
  ausencia de datos internos; acceso denegado a roles no incluidos.
- Reserva: conservar tests existentes y ejecutar mas adelante el criterio
  academico SRS RF-014 de 20 solicitudes simultaneas. Las pruebas futuras no
  estan creadas ni autorizan cambios productivos desde C.3.

### 21. Decisiones abiertas

| Estado vigente | Decisiones | Efecto en C.3 |
|---|---|---|
| CLOSED (diseno, no implementacion) | DEC-006, DEC-007, DEC-009, DEC-019 | Propiedad de specialty, actores/vista, zona IANA academica, compatibilidad API. |
| OPEN | DEC-003, DEC-005, DEC-008, DEC-010, DEC-017, DEC-022, DEC-023 | Bootstrap, ciclo Professional-User, generacion/solapamiento, reglas temporales, onboarding, auditoria y operabilidad permanecen sin cerrar. |
| PROPOSED | DEC-004, DEC-015, DEC-016, DEC-020 | Auth/Users, eventos, desactivacion HTTP de profesional y granularidad de permisos no se elevan a CLOSED. |

En particular, DEC-008 no autoriza duracion/horizonte/calendario/solapamiento,
DEC-010 no autoriza fecha pasada o ventanas, DEC-005 no define vinculacion
Professional-User. DEC-009 aprueba `America/Lima` como valor academico inicial
de una zona configurable; no existe configuracion de zona de negocio aplicada.

### 22. Impactos futuros

1. Una implementacion de Catalogs/assignacion requeriria fronteras publicas
   propias y cobertura de errores, roles y FK. Se debe verificar si V1 basta
   para la politica finalmente aprobada; no se crea migracion automaticamente.
2. Una politica de specialty habilitada/asignada podria cambiar la elegibilidad
   de nuevos schedules y de slots existentes. Debe resolverse efecto sobre
   agenda/citas historicas, desactivacion concurrente e integridad de reserva
   antes de autorizar codigo.
3. La vista sanitizada necesita minimizacion por campo/rol, filtros finales,
   regla de oferta, errores y pruebas. No se modifica el GET ADMIN para
   construirla por suposicion.
4. DEC-003 afecta operabilidad de un entorno limpio; DEC-008/010/023 afectan
   generacion, reglas temporales y pretensiones de despliegue. No bloquean
   este diseno acotado, si la implementacion que dependa de ellas.

### 23. Gate para C.4

C.3 deja inventario de contratos reales y candidatos acotados para que C.4
formalice casos de uso **solo** dentro de la evidencia aprobada. Antes de
tratar un candidato como contrato ejecutable, C.4/autoridad academica debera
resolver: firmas/errores, campos sanitizados por rol, filtros, semantica de
specialty activa/eliminada y asociacion, efectos de desactivacion y
compatibilidad con reserva. Lo dependiente de DEC-008/010 permanece fuera sin
aprobacion humana. No hay autorizacion de implementacion, endpoints, DTOs,
tests, migraciones o cambios de seguridad en este gate.
