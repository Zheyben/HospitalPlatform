# ETAPA D.3.1 - Escenarios implementados de Agenda y reserva

## Alcance y autoridad

Estos diagramas de secuencia detallan solo CU-D5 (porcion ADMIN de RF-012),
CU-D7 (RF-013) y CU-D8 (invariante RF-014) del modelo D.1 y las fichas D.2.
Son identificadores descriptivos locales del caso academico HOSPITALPLATFORM;
no representan aprobacion institucional del Hospital de Huaycan. Una
intercalacion ilustrativa de concurrencia no equivale a una traza medida.

Fuentes: `DOMAIN-BASELINE.md`, `DOMAIN-DECISION-REGISTER.md`, C.1/C.2/C.3,
diagrama D.1, fichas D.2, SRS RF-012/013/014, `06-API-SPECIFICATION.md`,
ADR-001/005/006/007/009, controllers, services, DTOs, entidades, seguridad,
contratos, tests y migraciones V1/V2/V3. Las URI incluyen el context path
actual `/api/v1` de `application.yml`.

## CU-D5 - Consulta de disponibilidad ADMIN

**Diagrama:** `CU-D5-ADMIN-AVAILABILITY-SCENARIO.puml`. **Estado:** IMPLEMENTADO
solo para la consulta operativa ADMIN; RF-012 completo sigue parcial.

- **Actor y precondiciones:** ADMIN autenticado. La lista admite filtros
  opcionales `scheduleId`, `professionalId`, `slotDate` y `status`; el detalle
  recibe UUID. Los slots consultados ya existen. No se exige fecha futura ni
  schedule activo para listar.
- **Flujo normal:** `AgendaController` atiende `GET /api/v1/availability` o
  `GET /api/v1/availability/{id}`. `AgendaService` consulta
  `AvailabilitySlotRepository`; `AgendaMapper` devuelve
  `AvailabilitySlotResponseDTO(id, scheduleId, slotDate, startTime, endTime,
  status, usable)`. `usable` deriva de `status == AVAILABLE && schedule.active`.
  Lista y detalle responden 200. La lista puede estar vacia y puede incluir
  RESERVED/BLOCKED o slots de schedule inactivo; no hay filtro `specialtyId`.
- **Alternos:** anonimo 401; rol no ADMIN 403; detalle inexistente 404
  `AVAILABILITY_SLOT_NOT_FOUND`. Una lista sin coincidencias devuelve `[]`, no
  404. No se documenta un codigo de error de binding no verificado.
- **Postcondicion:** solo lectura; no se reserva slot ni se crea cita.
- **Evidencia:** `AgendaController.findAvailability/findAvailabilitySlotById`,
  `AgendaService`, `AgendaMapper`, `AvailabilitySlot.isUsable` y V1. Pruebas:
  `AgendaServiceTest.findsAvailabilityAndExposesUsableState`,
  `AgendaControllerTest.returnsAvailabilityResponse`,
  `AgendaControllerAuthorizationTest` y `HospitalPlatformApplicationIT` para
  `AvailabilitySlotService.isUsable` con PostgreSQL. Esta ultima **no** prueba
  `findAvailability` sobre PostgreSQL.

## CU-D7 - Reserva actual de cita medica

**Diagrama:** `CU-D7-APPOINTMENT-RESERVATION-SCENARIO.puml`.
**Estado:** IMPLEMENTADO para RF-013 actual.

- **Actores y precondiciones:** PATIENT para su perfil activo, o ADMIN y
  RECEPTIONIST para un paciente activo identificado por `patientId`.
  `CreateAppointmentRequestDTO` exige `slotId`; `patientId` y `reason` son
  opcionales en el DTO, aunque ADMIN/RECEPTIONIST deben indicar `patientId`.
  No se requiere una consulta previa a CU-D5/CU-D6.
- **Flujo normal:** `POST /api/v1/appointments` entra por
  `AppointmentController`. `AppointmentService.createAppointment` resuelve el
  paciente mediante `PatientLookupService` y, para PATIENT, `CurrentUserService`.
  Usa `AvailabilitySlotReservationService.reserveUsableSlot(slotId)`, que
  actualiza condicionalmente el slot AVAILABLE de schedule activo y devuelve
  `AvailabilitySlotReference`. El service comprueba profesional activo con
  `ProfessionalLookupService`; persiste `Appointment` con
  `AppointmentStatus=SCHEDULED`, `FlowStage=null` y responde 201 con
  `AppointmentResponseDTO` y `Location`.
- **Transaccion y postcondicion:** `createAppointment` tiene `@Transactional`;
  la implementacion Agenda requiere transaccion existente (`MANDATORY`). Exito:
  cita persistida y slot RESERVED. Si falla una operacion posterior a la
  reserva, la transaccion revierte ambos efectos. La creacion **no** registra
  un evento de auditoria en este metodo; no se infiere de otros endpoints.
- **Alternos reales:** anonimato 401; rol sin permiso o PATIENT que envia
  `patientId` explicito 403; `slotId` ausente 400 `VALIDATION_ERROR`;
  ADMIN/RECEPTIONIST sin `patientId` 400 `INVALID_APPOINTMENT_REQUEST`;
  paciente no disponible 404 `PATIENT_NOT_AVAILABLE`; slot no utilizable o
  carrera perdida 409 `SLOT_UNAVAILABLE`; profesional no disponible 409
  `PROFESSIONAL_NOT_AVAILABLE`. El cliente no elige `professionalId`,
  `specialtyId` ni estado de la cita.
- **Evidencia:** `AppointmentController`, `AppointmentService`,
  `CreateAppointmentRequestDTO`, `Appointment`, contratos publicos de
  Patients/Professionals/Agenda y V1/V3. `AppointmentModuleIT` cubre exito
  HTTP/PostgreSQL, slots RESERVED/BLOCKED/schedule inactivo, rollback del
  INSERT y profesional inactivo; tests de controller/service/autorizacion
  cubren otras ramas. No se aplican ventanas temporales (DEC-010 OPEN).

## CU-D8 - Exclusion de doble reserva

**Diagrama:** `CU-D8-CONCURRENT-RESERVATION-SCENARIO.puml`.
**Estado:** IMPLEMENTADO como regla tecnica interna de CU-D7, no caso de actor
humano ni endpoint. Sus solicitudes heredan roles y ownership de CU-D7.

- **Disparador y regla:** dos solicitudes compiten por el mismo `slotId`
  inicialmente utilizable. Agenda ejecuta un UPDATE condicional
  `AVAILABLE -> RESERVED` solo si `schedule.active=true`; exactamente una fila
  modificada permite seguir. Cero filas causa `SlotReservationRejectedException`,
  traducida por Appointments a 409 `SLOT_UNAVAILABLE`.
- **Transaccion y persistencia:** el contrato de Agenda participa en la
  transaccion de `AppointmentService.createAppointment`. PostgreSQL serializa
  las actualizaciones de una misma fila. V3 agrega el indice unico parcial
  `uq_appointments_active_slot` para citas `SCHEDULED`/`CONFIRMED` sobre un
  mismo slot. No hay `PESSIMISTIC_WRITE` en este flujo de creacion; su uso en
  otros flujos de Appointments no se traslada a CU-D8.
- **Escenario ilustrativo:** A modifica una fila y finalmente persiste la
  cita; B intenta la misma condicion y obtiene cero filas tras la resolucion
  de la contienda, por lo que no persiste una segunda cita. El diagrama no
  afirma que el test observe ese orden exacto ni mida el tiempo de bloqueo.
- **Postcondicion y evidencia:** a lo sumo una cita activa por slot; la
  solicitud perdedora no deja efectos parciales. La prueba PostgreSQL
  `AppointmentModuleIT.allowsOnlyOneOfTwoConcurrentReservations` usa dos
  tareas y comprueba un exito, un conflicto, una cita y slot RESERVED.
  `AppointmentPersistenceIT.rejectsTwoActiveAppointmentsForTheSameSlot`
  verifica el indice. **Cobertura actual: 2 solicitudes concurrentes.
  Requisito SRS RF-014: 20 simultaneas. Validacion de 20: PENDIENTE.**

## Limites y contradicciones conservadas

La SRS RF-012 describe consulta reservable por paciente/recepcion; la API
real de `/availability` sigue siendo ADMIN-only. DEC-007 aprueba una vista
sanitizada futura, **NO IMPLEMENTADA**: no aparece en estos diagramas.
La SRS RF-014 conserva el criterio de 20 solicitudes; los tests actuales
solo cubren dos. DEC-008 no autoriza materializacion automatica de slots y
DEC-010 no define fecha pasada, anticipacion ni ventanas de reserva. Ni la
consulta ADMIN ni la reserva hacen validacion de specialty activa/asignada.
V2 crea `refresh_tokens` y no interviene en estos escenarios. No se definen
endpoint, DTO, actor, permiso o estado adicional.
