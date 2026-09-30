# Agenda Availability + Specialty Policy: casos de uso descriptivos

## 1. Alcance y autoridad

ETAPA C.2, analisis funcional documental para el caso de estudio academico
HOSPITALPLATFORM. No representa aprobacion institucional del Hospital de Huaycan,
ni autoriza implementacion. La autoridad de las decisiones es el proyecto
academico/docente, conforme a DEC-001. La fuente de trazabilidad es
`docs/agenda/AGENDA-AVAILABILITY-RF-UC-MATRIX.md` (C.1 aprobada). Se contrasta
con la SRS, `docs/DOMAIN-BASELINE.md`, `docs/DOMAIN-DECISION-REGISTER.md`,
`docs/06-API-SPECIFICATION.md`, ADR-001/005/006/007/009, contratos, codigo,
tests y migraciones V1-V3. El codigo/controller es evidencia de comportamiento
actual; una decision cerrada puede aprobar solo el diseno, no el codigo.

**No hay catalogo oficial de IDs UC.** Los nombres siguientes son etiquetas
descriptivas, no UC oficiales ni especificaciones ejecutables. `Actual` significa
implementado en este corte; `conceptual` significa aprobado sin implementacion;
`pendiente` no se resuelve por inferencia. Para flujos inexistentes se indica
`EVIDENCIA INSUFICIENTE` o `PENDIENTE DE DISENO`, sin simular pasos operativos.
Las rutas reales incorporan el context path `/api/v1` de `application.yml`.

| Etiqueta descriptiva | RF | Estado del flujo | Descomposicion respecto de C.1 |
|---|---|---|---|
| Mantener profesional | RF-007 | PARCIAL | Separado de la asignacion de especialidades. |
| Asociar profesional y especialidad | RF-007 | APROBADO CONCEPTUALMENTE / NO IMPLEMENTADO | N:M y propietario aprobados por DEC-006; flujo pendiente. |
| Mantener definiciones de especialidad | RF-008 | DOCUMENTADO / NO IMPLEMENTADO | Distinto de la asignacion N:M. |
| Configurar horarios | RF-011 | PARCIAL | Schedule existe; generacion de slots excluida. |
| Consultar disponibilidad operativa | RF-012 | PARCIAL | Lectura actual de ADMIN. |
| Consultar disponibilidad sanitizada | RF-012 | APROBADO CONCEPTUALMENTE / NO IMPLEMENTADO | Actores de DEC-007; detalle del contrato pendiente. |
| Reservar cita | RF-013 | IMPLEMENTADO, solo dependencia | No se rediseña Appointments. |
| Asegurar una sola reserva efectiva | RF-014 | IMPLEMENTADO para reserva actual; COBERTURA PENDIENTE para 20 solicitudes | Invariante tecnico, no UC humano independiente. |

RF-007, RF-011 y RF-012 abarcan capacidades distintas. Esta descomposicion es
analitica; la numeracion, delimitacion y aprobacion de UC oficiales siguen
`EVIDENCIA INSUFICIENTE`.

## 2. Casos de uso detallados

### Mantener profesional

- **Identificacion:** sin ID UC oficial; RF-007; estado PARCIAL.
- **Actor y objetivo:** ADMIN registra, consulta o actualiza la informacion basica
  de un profesional. Sistema como ejecutor tecnico; ningun otro actor aprobado
  para estas operaciones.
- **Precondiciones actuales:** ADMIN autenticado; `licenseNumber` requerido.
  En creacion, `userId` es opcional y su integridad se delega a la FK de V1.
- **Flujo actual:** 1. ADMIN envia `POST /api/v1/professionals` con
  `licenseNumber` y `userId` opcional; el servicio normaliza y verifica licencia
  duplicada. 2. Persiste `Professional` y devuelve `201 ProfessionalResponseDTO`.
  3. ADMIN puede listar/consultar por GET o cambiar **solo** `licenseNumber`
  mediante PUT sobre `{id}`. Son operaciones existentes, no un flujo de
  vinculacion de usuario.
- **Alternos sustentados:** licencia duplicada se rechaza; consulta o
  actualizacion de profesional inexistente/inactivo se rechaza. La FK limita
  un `userId` inexistente, pero no define su ciclo funcional (DEC-005 OPEN).
- **Postcondicion actual:** profesional creado o licencia actualizada; las
  consultas no alteran datos. No se asigna especialidad ni se crean slots.
- **Reglas y datos:** V1 `professionals.id`, `license_number`, `user_id`,
  `deleted_at`; `CreateProfessionalRequestDTO`, `UpdateProfessionalRequestDTO`
  y `ProfessionalResponseDTO`. La desactivacion existe en el service pero no
  tiene endpoint (DEC-016 PROPOSED). Datos adicionales: PENDIENTE DE DISENO.
- **Evidencia:** `ProfessionalController`, `ProfessionalService`,
  `ProfessionalServiceTest`, `ProfessionalControllerAuthorizationTest`, V1.

### Asociar profesional y especialidad

- **Identificacion:** sin ID UC oficial; parte de RF-007; estado APROBADO
  CONCEPTUALMENTE / NO IMPLEMENTADO.
- **Actor y objetivo:** ADMIN, segun SRS, asocia especialidades habilitadas a un
  profesional. DEC-006 asigna la propiedad de la relacion a Professionals.
- **Precondiciones conceptuales:** profesional y especialidad aplicables segun
  SRS; validaciones concretas, reactivacion y cambios de asignacion:
  PENDIENTE DE DISENO. No se afirma que esas validaciones existan hoy.
- **Flujo principal:** NO EXISTE flujo Java/API. La secuencia operativa y el
  contrato publico sin firma definida corresponden a C.3: EVIDENCIA INSUFICIENTE.
- **Alternos:** la SRS menciona asociacion invalida, pero no hay errores ni
  tratamiento implementado; PENDIENTE DE DISENO.
- **Postcondicion:** conceptual: asociacion N:M consultable; no existe una
  postcondicion ejecutable del modulo. Una fila SQL preexistente no prueba el
  flujo de gestion.
- **Reglas y datos:** DEC-006 aprueba N:M; V1 `professional_specialties` usa
  PK compuesta (`professional_id`, `specialty_id`) y ambas FK. Sin entity,
  repository, service, controller ni contrato publico Java de asignacion.
- **Evidencia:** SRS RF-007, DEC-006, V1, inventario de Professionals.

### Mantener definiciones de especialidad

- **Identificacion:** sin ID UC oficial; RF-008; DOCUMENTADO / NO IMPLEMENTADO.
- **Actor y objetivo:** ADMIN administra definiciones para reserva segun SRS;
  DEC-006 asigna la propiedad a Catalogs.
- **Precondiciones:** la SRS exige permiso y datos validados. Campos de una
  futura API, controles de activacion y politica de historial: PENDIENTE DE
  DISENO. No existe precondicion Java ejecutable.
- **Flujo principal:** NO EXISTE. Crear, editar, activar y desactivar son
  intenciones de RF-008, no operaciones disponibles; pasos concretos:
  EVIDENCIA INSUFICIENTE.
- **Alternos:** la SRS menciona duplicado y acceso no autorizado. V1 impone
  `name UNIQUE`, no un campo `code`; respuesta funcional futura pendiente.
- **Postcondicion:** la SRS busca catalogo modificado sin borrar historia; no
  hay postcondicion Java actual. La FK no verifica especialidad activa.
- **Reglas y datos:** V1 `specialties.id`, `name`, `description`, `active`,
  `deleted_at`; sin Specialty entity/repository/service/controller ni contrato
  publico. DEC-006 es conceptual, no implementacion.
- **Evidencia:** SRS RF-008, DOMAIN-BASELINE RF-008, DEC-006, V1, Catalogs
  (solo estructura `package-info.java`).

### Configurar horarios

- **Identificacion:** sin ID UC oficial; RF-011; PARCIAL.
- **Actor y objetivo:** ADMIN mantiene schedules por profesional y
  `specialtyId`. No se deriva permiso de agenda para PROFESSIONAL.
- **Precondiciones actuales:** ADMIN autenticado, profesional activo validado
  mediante `ProfessionalLookupService.existsActiveProfessional`, dia 0..6,
  `endTime > startTime`; `specialtyId` UUID existente por FK de V1. Ni la
  especialidad activa ni su asignacion al profesional son verificadas.
- **Flujo actual:** 1. ADMIN crea con `POST /api/v1/agendas` indicando
  `professionalId`, `specialtyId`, `dayOfWeek`, `startTime`, `endTime`.
  2. `AgendaService` valida profesional y rango, guarda `Schedule` y devuelve
  `201 AgendaResponseDTO`. 3. ADMIN puede listar/filtrar, consultar, actualizar
  la configuracion o cambiar `active` mediante los mappings existentes.
- **Alternos sustentados:** profesional no activo/inexistente y horario
  invertido se rechazan; FK rechaza referencia de especialidad inexistente.
  Solapamiento entre schedules: PENDIENTE DE DECISION (DEC-008).
- **Postcondicion actual:** schedule persistido/actualizado o consulta sin
  cambio; **no** se generan ni materializan slots.
- **Reglas y datos:** `Schedule`, `CreateAgendaRequestDTO`,
  `UpdateAgendaRequestDTO`, `AgendaResponseDTO`; V1 `schedules` y sus FK/CHECK.
  `AvailabilitySlot` preexistente no implica generacion. Duracion, horizonte,
  calendario y solapamiento siguen DEC-008 OPEN; ventanas temporales DEC-010 OPEN.
- **Evidencia:** `AgendaController`, `AgendaService`, `ScheduleRepository`,
  `AgendaServiceTest`, `HospitalPlatformApplicationIT`, V1 y ADR-006.

### Consultar disponibilidad operativa

- **Identificacion:** sin ID UC oficial; RF-012; PARCIAL (solo ADMIN).
- **Actor y objetivo:** ADMIN consulta slots ya persistidos y su usabilidad.
- **Precondiciones actuales:** autenticacion y rol ADMIN. No se exige fecha
  futura, especialidad activa ni estado AVAILABLE como filtro obligatorio.
- **Flujo actual:** 1. ADMIN llama `GET /api/v1/availability` con filtros
  opcionales `scheduleId`, `professionalId`, `slotDate`, `status`, o consulta
  `{id}`. 2. Agenda lee `availability_slots` con `Schedule` y devuelve
  `List<AvailabilitySlotResponseDTO>` o un DTO. 3. `usable` se deriva de
  `status = AVAILABLE && schedule.active`.
- **Alternos sustentados:** `{id}` inexistente produce error de slot no
  encontrado; lista sin coincidencias puede ser vacia. Slots RESERVED/BLOCKED
  o de schedule inactivo no son usables, pero la consulta no los excluye
  automaticamente si el filtro no los descarta.
- **Postcondicion actual:** ninguna mutacion ni reserva.
- **Reglas y datos:** DTO actual `id`, `scheduleId`, `slotDate`, `startTime`,
  `endTime`, `status`, `usable`; no existe filtro `specialtyId` para este GET.
  `AvailabilitySlotService.existsSlot/isAvailable/isUsable(UUID)` es contrato
  de consulta puntual, **no** contrato de listado. La consulta PostgreSQL de
  `findAvailability` no tiene prueba especifica; la de `isUsable` si.
- **Evidencia:** `AgendaController`, `AgendaService`, `AgendaMapper`,
  `AvailabilitySlotRepository`, `HospitalPlatformApplicationIT`,
  `AgendaControllerAuthorizationTest`, `AgendaServiceTest`.

### Consultar disponibilidad sanitizada

- **Identificacion:** sin ID UC oficial; RF-012; APROBADO CONCEPTUALMENTE /
  NO IMPLEMENTADO (DEC-007).
- **Actor y objetivo:** PATIENT y RECEPTIONIST autenticados descubren oferta
  para una futura reserva; ADMIN conserva consulta operativa adicional.
  PROFESSIONAL no recibe permiso por esta decision.
- **Precondicion conceptual:** usuario autenticado con uno de los dos roles
  aprobados por DEC-007. Filtros concretos y datos minimos por rol: PENDIENTE
  DE DISENO; no se atribuyen al endpoint ADMIN actual.
- **Flujo principal:** NO EXISTE endpoint ni vista sanitizada hoy. Descubrir
  slots y luego seleccionar uno es una relacion conceptual de RF-012/RF-013,
  no un recorrido implementado de UI ni una secuencia API nueva.
- **Alternos y postcondicion:** manejo de ausencia de cupos, filtrado de
  especialidad, estructura de respuesta y errores: PENDIENTE DE DISENO.
  La intencion es consulta sin reserva; no existe postcondicion ejecutable.
- **Reglas y datos:** DEC-007 aprueba el limite de exposicion, no URI, DTO,
  campos, filtros, paginacion ni contrato Java. La respuesta actual ADMIN es
  evidencia de datos existentes, **no** plantilla aprobada para los actores
  nuevos. Sanitizacion por campo: EVIDENCIA INSUFICIENTE.
- **Evidencia:** SRS RF-012, DEC-007/019, `AgendaController` ADMIN-only.

### Reservar cita (dependencia existente)

- **Identificacion:** sin ID UC oficial; RF-013; IMPLEMENTADO. Se registra
  solo como dependencia y no se rediseña Appointments.
- **Actor y objetivo:** PATIENT para si mismo; ADMIN/RECEPTIONIST para paciente
  activo. El cliente elige un `slotId` sin poder fijar `professionalId` ni estado.
- **Precondiciones actuales:** rol permitido; paciente propio activo o
  `patientId` activo aportado por rol administrativo; slot usable y profesional
  activo. Consulta previa de disponibilidad/UI: conceptual, no precondicion
  tecnica implementada.
- **Flujo actual:** 1. `POST /api/v1/appointments` recibe `slotId`,
  `patientId` segun actor y `reason` opcional. 2. Appointments resuelve paciente
  por `PatientLookupService`/`CurrentUserService`. 3. Invoca
  `AvailabilitySlotReservationService.reserveUsableSlot(UUID)`: Agenda ejecuta
  UPDATE condicional y retorna `AvailabilitySlotReference` con contexto minimo.
  4. Appointments comprueba profesional activo y persiste cita `SCHEDULED`,
  `flowStage = null`, en la transaccion de negocio.
- **Alternos sustentados:** slot no usable, paciente/profesional no disponible,
  falta de ownership o colision de unicidad se rechazan; fallo posterior
  revierte la reserva. No se impone ventana temporal no aprobada.
- **Postcondicion actual:** cita creada y slot RESERVED, o ningun cambio por
  rollback. No implica generacion ni descubrimiento publico de slots.
- **Reglas y datos:** `CreateAppointmentRequestDTO(slotId, patientId, reason)`,
  `AppointmentResponseDTO`, `AvailabilitySlotReference`; V1/V3. DEC-010 sigue
  OPEN para reglas temporales.
- **Evidencia:** `AppointmentController`, `AppointmentService`,
  `AppointmentModuleIT`, contrato Agenda, V1/V3.

### Asegurar una sola reserva efectiva (invariante tecnico)

- **Identificacion:** sin ID UC oficial; RF-014; IMPLEMENTADO para reserva
  actual, `COBERTURA PENDIENTE` para el escenario SRS de 20 solicitudes.
- **Actor y objetivo:** Sistema como actor tecnico; solicitudes competidoras
  de actores admitidos por RF-013. No es un endpoint ni rol nuevo.
- **Precondicion actual:** varias solicitudes intentan reservar un mismo slot
  usable. No se supone sincronizacion del cliente.
- **Flujo actual:** 1. Agenda ejecuta UPDATE condicional de AVAILABLE a
  RESERVED si el schedule esta activo. 2. Appointments crea una cita activa
  dentro de su transaccion. 3. V3 protege la unicidad activa por `slot_id`.
- **Alternos sustentados:** quien no obtiene la transicion recibe conflicto
  controlado; no debe quedar cita parcial. La persistencia rechaza una segunda
  cita activa incluso si se intenta insertar directamente.
- **Postcondicion actual:** a lo sumo una reserva/cita activa para el slot;
  no se crea un UC humano adicional.
- **Reglas y datos:** `availability_slots.status`, `appointments.slot_id`,
  indice parcial V3 `uq_appointments_active_slot`.
- **Cobertura:** `AppointmentModuleIT.allowsOnlyOneOfTwoConcurrentReservations`
  usa PostgreSQL con **2** solicitudes concurrentes y observa un exito/un
  conflicto. `AppointmentPersistenceIT.rejectsTwoActiveAppointmentsForTheSameSlot`
  prueba la restriccion. La SRS RF-014 exige **20** solicitudes simultaneas:
  validacion posterior pendiente; C.2 no afirma que ese criterio este cubierto.

## 3. Cuatro limites de especialidad

| Tema | Actual | Decision conceptual | Pendiente / limite |
|---|---|---|---|
| A. Definicion | V1 `specialties`; Catalogs solo `package-info.java`. | DEC-006: Catalogs administra definiciones. | Entity/API/contrato sin implementar; no se asume `code` porque V1 solo impone `name UNIQUE`. |
| B. Asignacion N:M | V1 `professional_specialties`, sin flujo Java. | DEC-006: Professionals administra asignaciones por frontera publica. | Firma de contrato, API, validaciones y pruebas: C.3/diseno posterior; no se derivan de la FK. |
| C. `Schedule.specialtyId` | UUID persistido; V1 FK a `specialties`. | DEC-006 fija propietarios modulares, no validacion actual. | No se verifica `active` ni asociacion profesional-especialidad; tratamiento futuro requiere diseno. |
| D. Disponibilidad por especialidad | `GET /agendas` filtra `specialtyId`; `GET /availability` **no**. | RF-012/DEC-006/007 motivan busqueda futura. | Filtro, reglas y datos sanitizados: PENDIENTE DE DISENO; no se propone URI. |

## 4. Slots y limites temporales

V1 persiste `availability_slots` con `schedule_id`, `slot_date`, horas, estado
`AVAILABLE/RESERVED/BLOCKED`, unicidad (`schedule_id`, `slot_date`, `start_time`)
y `end_time > start_time`. `AvailabilitySlot.isUsable()` y
`AvailabilitySlotService.isUsable(UUID)` aplican `AVAILABLE && schedule.active`;
`isAvailable(UUID)` comprueba solo `AVAILABLE`. La reserva ejecuta UPDATE
condicional equivalente. No hay generacion Java ni API de materializacion.

DEC-008 OPEN: duracion, horizonte, calendario, generacion y solapamiento entre
schedules **no** se definen aqui. DEC-010 OPEN: pasado/futuro, anticipacion y
ventanas de reserva/cancelacion **no** se definen aqui. DEC-009 CLOSED aprueba
zona IANA configurable con valor academico inicial `America/Lima`, pero no hay
configuracion de zona de negocio ni regla temporal implementada.

## 5. Dependencias UC -> RF -> actor -> modulo -> contrato -> persistencia

| Etiqueta UC (no oficial) | RF | Actor | Modulo propietario | Contrato existente utilizado | Persistencia actual | Depende de |
|---|---|---|---|---|---|---|
| Mantener profesional | 007 | ADMIN | Professionals | Ninguno externo para CRUD | `professionals` V1 | Identidad opcional; ciclo DEC-005 OPEN. |
| Asociar profesional-especialidad | 007 | ADMIN (SRS) | Professionals | Ninguno para asignacion; futuro contrato publico DEC-006 sin firma | `professional_specialties`, `professionals`, `specialties` V1 | Definiciones Catalogs; UC de mantenimiento profesional. |
| Mantener definiciones | 008 | ADMIN (SRS) | Catalogs | Ninguno | `specialties` V1 | Ninguno implementado; DEC-003 para bootstrap operativo. |
| Configurar horarios | 011 | ADMIN | Agenda | `ProfessionalLookupService` | `schedules` V1; FK `specialties` | Profesional activo; definicion/asignacion de specialty aun no validadas. |
| Consultar disponibilidad operativa | 012 | ADMIN | Agenda | Ningun contrato entre modulos: listado por `AgendaService` interno; `AvailabilitySlotService` es consulta puntual distinta | `availability_slots`, `schedules` V1 | Slots preexistentes. |
| Consultar disponibilidad sanitizada | 012 | PATIENT, RECEPTIONIST (DEC-007) | Agenda | Ninguno para vista sanitizada | Tablas de Agenda preexistentes; uso futuro sin decidir | Configurar horarios/slots existentes; datos por rol pendientes. |
| Reservar cita (referencia) | 013 | PATIENT, ADMIN, RECEPTIONIST | Appointments | `PatientLookupService`, `CurrentUserService`, `ProfessionalLookupService`, `AvailabilitySlotReservationService` | `appointments` V1/V3 y slot V1 | Slot usable; no exige endpoint de consulta previa. |
| Exclusion de doble reserva (invariante) | 014 | Sistema tecnico | Agenda + Appointments | `AvailabilitySlotReservationService` | UPDATE slot V1, indice parcial V3 | Reserva RF-013; ensayo 20 pendiente. |

La direccion entre modulos permanece por contratos publicos. Appointments no
importa entidades ni repositories externos. La FK a Catalogs no equivale a
contrato Java ni a politica de specialty activa.

## 6. Matriz actual vs diseno aprobado vs pendiente

| Capacidad | Estado actual | Diseno aprobado | Pendiente |
|---|---|---|---|
| CRUD basico de profesional | PARCIAL IMPLEMENTADO: crear/listar/consultar/cambiar licencia ADMIN | RF-007 | Asignacion N:M; ciclo usuario DEC-005; deactivacion HTTP DEC-016 PROPOSED. |
| Catalogo de especialidades | DOCUMENTADO / NO IMPLEMENTADO en Java | DEC-006: Catalogs propietario | API/contrato y reglas especificas; bootstrap DEC-003. |
| Asignacion profesional-especialidad | NO IMPLEMENTADO en Java | DEC-006: Professionals propietario y N:M | Contrato sin firma, flujo y validaciones. |
| Configuracion de schedules | PARCIAL IMPLEMENTADO ADMIN; specialty solo por FK | RF-011/ADR-006 | Validacion specialty activa/asignada; generacion DEC-008 OPEN. |
| Consulta ADMIN de slots | PARCIAL IMPLEMENTADO: lista/detalle de slots existentes | DEC-019 mantiene contrato actual | Prueba PostgreSQL especifica `findAvailability`. |
| Vista PATIENT/RECEPTIONIST | NO IMPLEMENTADO | APROBADO CONCEPTUALMENTE por DEC-007 | URI, campos, filtros, sanitizacion y pruebas: PENDIENTE DE DISENO. |
| Reserva/unicidad actual | IMPLEMENTADO por Appointments/Agenda | RF-013/014 existentes | Cobertura RF-014 de 20 solicitudes: COBERTURA PENDIENTE. |
| Zona de negocio y reglas temporales | NO IMPLEMENTADO | DEC-009 CLOSED: IANA configurable, `America/Lima` inicial academico | DEC-008/010 OPEN; no se define duracion ni ventana. |

## 7. Decisiones no cerradas por C.2

| Decision | Estado vigente | Relevancia sin resolverla |
|---|---|---|
| DEC-003 | OPEN | Bootstrap de ADMIN/catalogos para entorno limpio; no bloquea el analisis. |
| DEC-005 | OPEN | Ciclo de vinculacion profesional-usuario; no se infiere de FK. |
| DEC-008 | OPEN | Generacion, duracion, calendario y solapamiento de slots. |
| DEC-010 | OPEN | Reglas temporales de disponibilidad/citas. |
| DEC-017 | OPEN | Auto-registro/edicion PATIENT; no forma parte de consulta futura. |
| DEC-022 | OPEN | Acceso/retencion de auditoria; no se crea auditoria nueva. |
| DEC-023 | OPEN | Metas operativas antes de despliegue productivo, no C.2. |
| DEC-004 | PROPOSED | Auth -> Users; sin alteracion. |
| DEC-015 | PROPOSED | Inventario de eventos de auditoria; sin alteracion. |
| DEC-016 | PROPOSED | Desactivacion HTTP de profesional; sin endpoint nuevo. |
| DEC-020 | PROPOSED | Granularidad de permisos; se conservan roles actuales. |

DEC-006/007/009/019 estan CLOSED para el alcance indicado; CLOSED no significa
que sus capacidades futuras esten implementadas. Ninguna decision OPEN/PROPOSED
se eleva a CLOSED en este documento.

## 8. Contradicciones y gate siguiente

- SRS RF-011 incluye generar slots; Agenda solo configura schedules y consulta
  slots preexistentes. DEC-008 permanece OPEN.
- SRS RF-012/flujo de producto requiere consulta por paciente/recepcion;
  `AgendaController` protege ambos GET de disponibilidad con ADMIN. DEC-007
  aprueba solo el diseno de acceso sanitizado.
- SRS RF-008 menciona codigo duplicado; V1 impone unicidad de `name`, sin campo
  `code`. No se inventa ese campo.
- API Specification enumera `/specialties`, pero Catalogs carece de controller.
  DEC-019 mantiene controllers actuales como contrato funcional vigente.
- `DOMAIN-BASELINE.md` conserva un corte historico de decisiones abiertas; el
  `DOMAIN-DECISION-REGISTER.md` refleja los cierres B.1.1. RF-014 del baseline
  describe reserva implementada, pero la aceptacion SRS de 20 sigue sin probarse.
- ADR-005/roadmap mencionan TRIAGE; DEC-002 restringe actores actuales a
  ADMIN, PATIENT, RECEPTIONIST y PROFESSIONAL.

**Gate para C.3:** esta descomposicion permite disenar fronteras y contratos
publicos sin tratar relaciones SQL como APIs. C.3 debera precisar datos minimos
de la vista sanitizada, reglas de validacion de specialty, firmas/errores de
contratos y compatibilidad con endpoints actuales. Los aspectos dependientes
de DEC-008/010 no pueden cerrarse sin aprobacion humana. No se aprueba
implementacion, pruebas nuevas, migracion ni endpoint mediante C.2.
