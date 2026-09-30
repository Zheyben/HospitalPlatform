# ETAPA D.3.2 — PARTIAL SCENARIOS REPORT

## 1. Estado Git

- Rama al inicio de la auditoría: `feat/agenda-availability-partial-scenarios`, sincronizada con `origin/feat/agenda-availability-partial-scenarios`; árbol de trabajo limpio.
- Alcance de esta etapa: tres archivos documentales nuevos en `docs/agenda/scenarios/`. No se hizo commit, push ni merge.

## 2. Fuentes auditadas

- Dominio y decisiones: [`DOMAIN-BASELINE.md`](../../DOMAIN-BASELINE.md), [`DOMAIN-DECISION-REGISTER.md`](../../DOMAIN-DECISION-REGISTER.md).
- Trazabilidad C.1–C.4: [`AGENDA-AVAILABILITY-RF-UC-MATRIX.md`](../AGENDA-AVAILABILITY-RF-UC-MATRIX.md), [`AGENDA-AVAILABILITY-USE-CASES.md`](../AGENDA-AVAILABILITY-USE-CASES.md), [`AGENDA-AVAILABILITY-CONTRACT-DESIGN.md`](../AGENDA-AVAILABILITY-CONTRACT-DESIGN.md) y [`AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md`](../AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md).
- D.1: [`AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.puml`](../uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.puml) y su [explicación](../uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.md). D.2: [`AGENDA-AVAILABILITY-USE-CASE-DETAILS.md`](../use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md). **D.3.1 no está presente en este checkout**: la búsqueda de rutas y archivos Git de escenarios implementados no devolvió artefactos; no se afirma haberlos validado.
- Requisitos y API: [`SRS-HOSPITALPLATFORM.md`](../../requisitos/SRS-HOSPITALPLATFORM.md) RF-007/RF-011 y [`06-API-SPECIFICATION.md`](../../06-API-SPECIFICATION.md) §§10–11.
- ADR relacionados: [`ADR-005`](../../adr/ADR-005-role-permission-model.md), [`ADR-006`](../../adr/ADR-006-agenda-slot-model.md), [`ADR-007`](../../adr/ADR-007-appointment-state-machine.md), [`ADR-009`](../../adr/ADR-009-database-identifier-strategy.md) y [`ADR-010`](../../adr/ADR-010-soft-delete-strategy.md). Los identificadores **DEC-005/006/008/010** pertenecen al registro de decisiones de dominio, no a los ADR de igual número.
- Java actual: `ProfessionalController`, `ProfessionalService`, `ProfessionalRepository`, `Professional` y sus DTO; `AgendaController`, `AgendaService`, `ScheduleRepository`, `Schedule` y sus DTO; `ProfessionalLookupService`, handlers y `SecurityConfiguration`.
- Evidencia ejecutable existente: tests de servicio, controller, seguridad de métodos e integración `HospitalPlatformApplicationIT`. Esquema: Flyway [`V1`](../../../database/migrations/V1__initial_schema.sql), [`V2`](../../../database/migrations/V2__create_refresh_tokens.sql) y [`V3`](../../../database/migrations/V3__support_appointment_lifecycle.sql).

## 3. CU-D1 Validation

**Objetivo y actores.** CU-D1 / RF-007 es **PARCIAL**. El actor HTTP actual es `ADMIN` autenticado; `PROFESSIONAL` no administra perfiles. Las tres operaciones son alternativas independientes, no un flujo secuencial de alta obligatorio.

| Flujo implementado | Contrato y efecto actuales |
|---|---|
| Crear | `POST /api/v1/professionals`, `CreateProfessionalRequestDTO(userId?, licenseNumber)`; `@NotBlank`, `@Size(max=100)` sobre la licencia. El servicio hace `trim`, consulta duplicado sin distinguir mayúsculas y guarda `Professional`. Responde `201` con `Location` y `ProfessionalResponseDTO(id, userId, licenseNumber, active)`. `userId` puede ser nulo; V1 exige FK/UNIQUE si se provee. |
| Consultar | `GET /api/v1/professionals` lista perfiles sin `deleted_at`; `GET /api/v1/professionals/{id}` obtiene uno vigente. La lectura no modifica datos. |
| Actualizar | `PUT /api/v1/professionals/{id}`, `UpdateProfessionalRequestDTO(licenseNumber)`; cambia **solo** la licencia. No permite cambiar `userId` ni especialidades. |

Validaciones y errores actuales modelados: licencia duplicada `409 DUPLICATE_PROFESSIONAL`; detalle/PUT ausente o eliminado `404 PROFESSIONAL_NOT_FOUND`; acceso sujeto a `hasRole('ADMIN')`. La validación de entrada está declarada con `@Valid`, pero `ProfessionalExceptionHandler` no define un `VALIDATION_ERROR` propio; el UML no le atribuye ese código. La entidad deriva `active` de `deleted_at IS NULL`. `ProfessionalService.deactivateProfessional` y su test existen, pero **NO IMPLEMENTADO en API**: `ProfessionalController` no expone esa operación.

**Flujo pendiente y separación.** DEC-005 está **OPEN**: V1 soporta `user_id` opcional y `ProfessionalLookupService` comprueba ownership para otros consumidores, pero no hay operación aprobada para vincular, desvincular o sincronizar cuentas, ni flujo de aprobación. La asociación profesional–especialidad pertenece a CU-D2: **CONCEPTUAL / NO IMPLEMENTADO** en Java/API; `professional_specialties` solo aporta capacidad SQL. No se dibujó secuencia para ninguno de esos pendientes.

## 4. CU-D4 Validation

**Objetivo y actores.** CU-D4 / RF-011 es **PARCIAL**. `ADMIN` autenticado configura y consulta `Schedule`; `PROFESSIONAL` no tiene estos mappings.

| Flujo implementado | Contrato y efecto actuales |
|---|---|
| Crear | `POST /api/v1/agendas`, `CreateAgendaRequestDTO(professionalId, specialtyId, dayOfWeek, startTime, endTime)`; responde `201`, `Location` y `AgendaResponseDTO(id, professionalId, specialtyId, dayOfWeek, startTime, endTime, active)`. |
| Consultar | `GET /api/v1/agendas` con filtros opcionales `professionalId`/`specialtyId`; `GET /api/v1/agendas/{id}` por UUID. Son lecturas de schedules, no consultas de slots. |
| Actualizar | `PUT /api/v1/agendas/{id}` usa `UpdateAgendaRequestDTO` con los mismos cinco campos; `PATCH /api/v1/agendas/{id}/status` usa `UpdateAgendaStatusRequestDTO(active)` y cambia solo el `active` del schedule. |

`@NotNull` cubre los cinco campos de POST/PUT; `dayOfWeek` tiene `@Min(0)`/`@Max(6)`. `AgendaService` exige `ProfessionalLookupService.existsActiveProfessional`, comprueba `endTime > startTime` en POST/PUT y V1 repite CHECK/FK. Errores verificables: `404 PROFESSIONAL_NOT_AVAILABLE`, `400 INVALID_SCHEDULE_TIME`, `404 AGENDA_NOT_FOUND`, `400 VALIDATION_ERROR`. La FK de `schedules.specialty_id` prueba existencia física; el handler traduce **solo la violación de esa FK identificada** a `400 INVALID_SPECIALTY_REFERENCE`. No equivale a validar specialty activa, no eliminada o asociada al profesional.

**Flujo pendiente y separación.** Validar specialty activa y asociación N:M está **PENDIENTE / NO IMPLEMENTADO**; DEC-006 fija solo ownership conceptual: Catalogs posee definiciones, Professionals posee asociaciones. DEC-008 **OPEN** bloquea generación/materialización de slots, duración, horizonte, calendario y solapamiento; `AgendaService.createAgenda` guarda un Schedule y no inserta `availability_slots`. DEC-010 **OPEN** deja sin regla aplicada de zona horaria, fecha futura, anticipación y ventanas temporales. No-show es futuro según DEC-011; no se dibujó estado ni automatización.

## 5. UML Validation

- Ambos `.puml` son diagramas de secuencia con **bloques de peticiones independientes**, actor `ADMIN`, boundary/controller, service y repositorio reales. CU-D4 muestra además el contrato `ProfessionalLookupService` efectivamente invocado.
- Las flechas muestran únicamente llamadas del código actual. Los conceptos pendientes están en notas sin lifelines, mensajes, respuestas ni endpoints. `PARCIAL`, `CONCEPTUAL`, `PENDIENTE` y `NO IMPLEMENTADO` distinguen alcance y ausencia de ejecución.
- CU-D1 separa creación, lista/detalle y PUT; CU-D4 separa POST, lista/detalle, PUT y PATCH. Ninguno convierte una consulta en precondición obligatoria de otra operación.
- Las rutas del UML usan `/api/v1` como context path del controller; no agrega rutas. La excepción de FK en CU-D4 se limita al caso identificado por el handler.
- Validación sintáctica de PlantUML: **pendiente de renderizador local** si no hay ejecutable/JAR disponible. Se revisó estructura `@startuml`/`@enduml`, participantes, bloques y cierres `alt`/`end` por inspección de fuente.

## 6. Evidencia código

| Capacidad | Fuente decisiva | Límite comprobado |
|---|---|---|
| CU-D1 HTTP y rol | [`ProfessionalController.java`](../../../apps/backend/src/main/java/com/hospital/platform/professionals/controller/ProfessionalController.java) | POST, dos GET y PUT, todos `ADMIN`; ningún mapping de desactivación o N:M. |
| CU-D1 datos/validación | [`ProfessionalService.java`](../../../apps/backend/src/main/java/com/hospital/platform/professionals/service/ProfessionalService.java), [`CreateProfessionalRequestDTO.java`](../../../apps/backend/src/main/java/com/hospital/platform/professionals/dto/CreateProfessionalRequestDTO.java), [`UpdateProfessionalRequestDTO.java`](../../../apps/backend/src/main/java/com/hospital/platform/professionals/dto/UpdateProfessionalRequestDTO.java), [`ProfessionalResponseDTO.java`](../../../apps/backend/src/main/java/com/hospital/platform/professionals/dto/ProfessionalResponseDTO.java) | `userId` solo en creación; PUT cambia licencia; sin especialidades. |
| CU-D4 HTTP y rol | [`AgendaController.java`](../../../apps/backend/src/main/java/com/hospital/platform/agenda/controller/AgendaController.java) | POST/GET/GET por ID/PUT/PATCH de schedules con `ADMIN`; GET de availability es otro caso (CU-D5). |
| CU-D4 datos/validación | [`AgendaService.java`](../../../apps/backend/src/main/java/com/hospital/platform/agenda/service/AgendaService.java), [`CreateAgendaRequestDTO.java`](../../../apps/backend/src/main/java/com/hospital/platform/agenda/dto/CreateAgendaRequestDTO.java), [`UpdateAgendaRequestDTO.java`](../../../apps/backend/src/main/java/com/hospital/platform/agenda/dto/UpdateAgendaRequestDTO.java), [`UpdateAgendaStatusRequestDTO.java`](../../../apps/backend/src/main/java/com/hospital/platform/agenda/dto/UpdateAgendaStatusRequestDTO.java), [`AgendaResponseDTO.java`](../../../apps/backend/src/main/java/com/hospital/platform/agenda/dto/AgendaResponseDTO.java) | Profesional activo y rango; `specialtyId` sin lookup de Catalogs/asociación N:M; sin generación de slots. |
| Frontera y seguridad | [`ProfessionalLookupService.java`](../../../apps/backend/src/main/java/com/hospital/platform/professionals/contract/ProfessionalLookupService.java), [`SecurityConfiguration.java`](../../../apps/backend/src/main/java/com/hospital/platform/security/config/SecurityConfiguration.java), handlers de [Professionals](../../../apps/backend/src/main/java/com/hospital/platform/professionals/exception/ProfessionalExceptionHandler.java) y [Agenda](../../../apps/backend/src/main/java/com/hospital/platform/agenda/exception/AgendaExceptionHandler.java), [`application.yml`](../../../apps/backend/src/main/resources/application.yml) | Autenticación general, RBAC por método y context path `/api/v1`; los códigos de error citados tienen alcance de su controller. |
| Persistencia | V1 `professionals`, `specialties`, `professional_specialties`, `schedules`, `availability_slots`; V2 refresh tokens; V3 ciclo de citas/índice de slot | La tabla N:M y las FK no prueban gestión Java; V2/V3 no agregan esa gestión ni generación de slots. |

## 7. Evidencia tests

- [`ProfessionalServiceTest.java`](../../../apps/backend/src/test/java/com/hospital/platform/professionals/service/ProfessionalServiceTest.java): alta y normalización de licencia, duplicado, lista/detalle vigente, PUT y soft delete interno. [`ProfessionalControllerAuthorizationTest.java`](../../../apps/backend/src/test/java/com/hospital/platform/professionals/controller/ProfessionalControllerAuthorizationTest.java): ADMIN permitido y PROFESSIONAL rechazado para listado; **no** prueba individualmente cada mapping profesional.
- [`AgendaServiceTest.java`](../../../apps/backend/src/test/java/com/hospital/platform/agenda/service/AgendaServiceTest.java): alta con profesional activo, rechazo de profesional/rango, filtros, ausencia, PUT y PATCH. [`AgendaControllerTest.java`](../../../apps/backend/src/test/java/com/hospital/platform/agenda/controller/AgendaControllerTest.java): `201`, validación de DTO y `404`; [`AgendaControllerAuthorizationTest.java`](../../../apps/backend/src/test/java/com/hospital/platform/agenda/controller/AgendaControllerAuthorizationTest.java): ADMIN y rechazo de PROFESSIONAL para los mappings. [`HospitalPlatformApplicationIT.java`](../../../apps/backend/src/test/java/com/hospital/platform/HospitalPlatformApplicationIT.java): error de FK specialty en POST y rechazo HTTP para rol no autorizado/no autenticado.
- **Cobertura pendiente:** no hay prueba funcional de gestión N:M, specialty activa/asignada, generación de slots ni ventanas temporales. Esta etapa no creó ni ejecutó pruebas; la validación es de trazabilidad documental contra código y pruebas existentes.

## 8. Decisiones relacionadas

| Decisión | Estado vigente | Aplicación a estos escenarios |
|---|---|---|
| DEC-005 | **OPEN** | `userId` opcional y FK actuales; ciclo de vinculación, aprobación, auto-sincronización y self-service **PENDIENTES / NO IMPLEMENTADOS**. |
| DEC-006 | **CLOSED solo para diseño conceptual** | Cardinalidad N:M y propietarios Catalogs/Professionals aprobados; contrato/API/flujo Java de asignación **NO IMPLEMENTADOS**. No se la trata como cierre funcional. |
| DEC-008 | **OPEN** | Duración, horizonte, calendario, solapamiento y generación de slots **PENDIENTES**. |
| DEC-010 | **OPEN** | Zona aplicada y ventanas/restricciones temporales **PENDIENTES**. DEC-009 cierra elección conceptual de zona IANA, no su aplicación aquí. |

## 9. Contradicciones

1. La SRS RF-007 describe asociar especialidades dentro de administrar profesionales; Java expone solo alta, consulta y cambio de licencia. El escenario trata N:M como CU-D2 **CONCEPTUAL / NO IMPLEMENTADO**.
2. La SRS RF-011 describe crear horarios **y slots** con especialidad habilitada; Java guarda schedules, y la FK solo comprueba que exista el UUID de specialty. La generación y las reglas de specialty quedan **PENDIENTES**.
3. La API Specification enumera rutas `/api/v1/specialties` y un permiso `SPECIALTIES_MANAGE`; no hay controller funcional de Catalogs. No se reutilizan como acciones de CU-D1/CU-D4 ni se agregan permisos.
4. `DOMAIN-BASELINE.md` resume RF-007 como actualización de referencia de usuario; el `UpdateProfessionalRequestDTO` y `ProfessionalService.updateProfessional` actuales solo cambian `licenseNumber`. Prevalece la evidencia Java para el UML.
5. D.3.1 figura en el alcance de auditoría pedido, pero no existe en el checkout. La comparación con sus escenarios queda **PENDIENTE** hasta disponer de ese artefacto.

## 10. Hallazgos

- `active` de Professional se deriva de soft delete; `active` de Schedule es campo mutable por PATCH. No se mezclan estos estados.
- La consulta de agendas puede filtrar por `specialtyId`, pero ese filtro no valida elegibilidad de la especialidad ni asociación N:M. CU-D4 no incluye la consulta de disponibilidad de CU-D5.
- ADR-006 adopta slots discretos como modelo y menciona duración configurable solo como consideración futura. No hay algoritmo o calendario en el código de CU-D4.
- V1 impone integridad física de `user_id`, licencia, specialty y horarios. V2 y V3 atienden refresh tokens y citas; no completan RF-007/RF-011.

## 11. Correcciones realizadas

- Se crearon dos escenarios de secuencia parciales con operaciones HTTP reales y notas explícitas para alcance conceptual/pendiente. Se documentó la brecha de D.3.1 y se ajustaron las afirmaciones del informe a DTO, service, handler y test existentes.
- No se corrigió código ni documentación previa: las contradicciones de fuentes se registran aquí para preservar la trazabilidad del corte auditado.

## 12. Archivos creados

1. [`CU-D1-PROFESSIONAL-MANAGEMENT-PARTIAL-SCENARIO.puml`](CU-D1-PROFESSIONAL-MANAGEMENT-PARTIAL-SCENARIO.puml).
2. [`CU-D4-SCHEDULE-CONFIGURATION-PARTIAL-SCENARIO.puml`](CU-D4-SCHEDULE-CONFIGURATION-PARTIAL-SCENARIO.puml).
3. [`AGENDA-PARTIAL-SCENARIOS.md`](AGENDA-PARTIAL-SCENARIOS.md).

## 13. Archivos NO modificados

Java productivo, DTOs, controllers, services, security, migraciones V1/V2/V3, permisos, pruebas y artefactos previos D.1/D.2. No hubo cambios productivos.

## 14. Validación final

- Trazabilidad comprobada manualmente: RF-007 → CU-D1 → ADMIN → ProfessionalController/Service/DTO → `professionals` V1 → tests existentes; RF-011 → CU-D4 → ADMIN → AgendaController/Service/DTO + `ProfessionalLookupService` → `schedules` V1 → tests existentes.
- Pendientes explícitos sin flechas ejecutables: ciclo usuario, asociación N:M, specialty activa, slots, calendario, solapamiento, zona aplicada, ventanas y no-show.
- La validación de sintaxis/render PlantUML se limita a revisión textual hasta contar con renderizador. Las pruebas existentes se leyeron; no se ejecutaron suites por ser un cambio solo documental.
- Git debe mostrar únicamente los tres archivos nuevos de `docs/agenda/scenarios/` y ninguna modificación de Java, migraciones o tests.

## 15. Gate para D.3.3

**Apto para continuar con escenarios documentales** manteniendo el mismo criterio de evidencia: fuente Java/API vigente para flechas ejecutables; SQL y decisiones conceptuales solo como notas. Antes de afirmar coherencia con D.3.1, incorporar o localizar ese artefacto y compararlo. DEC-005, DEC-008 y DEC-010 continúan OPEN; DEC-006 está CLOSED únicamente en el alcance de diseño conceptual indicado.

## 16. Veredicto final

🟢 **APPROVED** para los dos escenarios parciales de D.3.2: lo implementado y lo pendiente están separados, las secuencias se limitan a código/API existentes y no se crean funcionalidades ficticias. La ausencia de D.3.1 y la validación visual de PlantUML quedan expresamente pendientes; este veredicto no los declara cubiertos.
