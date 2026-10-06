# G.1 — Five Forms + ADMIN/RECEPTIONIST Scope & API Audit

Fecha: 2026-10-02, America/Lima. Resultado: **YELLOW**.
Auditoría estática del checkout real; no implementación.

## 1. Executive Summary

F01 y F02 están IMPLEMENTADOS y cuentan con evidencia histórica de funcionamiento real sobre PostgreSQL V5. F03, F04 y F05 son PARTIAL: hay operaciones backend, pero faltan contratos y portales para completar los recorridos de negocio. “Mis citas” funciona como listado propio, pero no ofrece datos suficientes para presentar la consulta reservada.

No se necesita V6 para el MVP recomendado: reutilizar users, patients, professionals, specialties, professional_specialties, schedules, availability_slots y appointments. Esto no autoriza ampliar lifecycle ni modificar V5. Un profesional sin User no tiene dónde almacenar nombres propios; exigir nombres para ese caso cambiaría esta conclusión.

El bloqueo principal de recepción es la obtención autorizada de patientId y slotId: puede crear citas con ambos UUID, pero no puede descubrirlos mediante las APIs disponibles. ADMIN puede crear horarios, pero crear un horario no genera slots. La generación solo tiene un invocador de producción de código en el seed de desarrollo, no un endpoint operativo.

**Estado del entorno:** el pedido menciona chore/forms-portals-scope-audit, pero git branch --show-current devuelve main, HEAD 5681a6cdc05944b68c821eb6c0ebb59bfe1f7ff6. No se creó ni cambió rama. Antes de auditar ya había una modificación en apps/frontend/next-env.d.ts; se conserva. El único cambio de esta auditoría es este documento.

**Límite probatorio:** no se arrancaron servicios, no se ejecutaron tests ni se consultó/modificó PostgreSQL en G.1. Ejecutar las suites E2E existentes crearía datos, incompatible con esta etapa sin cambios de DB. IMPLEMENTADO significa trazabilidad en código, no validación runtime nueva. Los resultados C1-C son DOCUMENTADOS, no repetidos aquí. Los diseños de este informe son DISEÑADOS/FUTURO y no endpoints existentes.

### Registro de fuentes

Las referencias [Sxx] identifican rutas reales relativas a la raíz del repositorio; los nombres de DTO en inventarios se resuelven en el directorio dto del mismo módulo. Estas referencias se aplican a las filas de cada tabla.

| Ref. | Archivos reales / evidencia |
|---|---|
| S01 | apps/backend/src/main/java/com/hospital/platform/security/config/SecurityConfiguration.java; security/filter/JwtAuthenticationFilter.java; users/service/PlatformUserDetailsService.java; users/entity/RoleName.java |
| S02 | apps/backend/src/main/java/com/hospital/platform/auth/controller/AuthController.java; auth/service/PatientRegistrationService.java; auth/service/AuthService.java; auth/service/RefreshTokenService.java; auth/dto/RegisterPatientRequestDTO.java |
| S03 | apps/backend/src/main/java/com/hospital/platform/users/controller/UserController.java; users/service/UserService.java; users/repository/UserRepository.java; users/repository/RoleRepository.java; users/entity/User.java |
| S04 | apps/backend/src/main/java/com/hospital/platform/patients/controller/PatientController.java; patients/service/PatientService.java; patients/repository/PatientRepository.java; patients/domain/DocumentIdentity.java; patients/contract/DatabasePatientLookupService.java |
| S05 | apps/backend/src/main/java/com/hospital/platform/professionals/controller/ProfessionalController.java; professionals/service/ProfessionalService.java; professionals/entity/Professional.java; professionals/repository/ProfessionalRepository.java; professionals/contract/DatabaseProfessionalLookupService.java; professionals/exception/ProfessionalExceptionHandler.java |
| S06 | apps/backend/src/main/java/com/hospital/platform/agenda/controller/AgendaController.java; agenda/service/AgendaService.java; agenda/service/SlotGenerationService.java; agenda/repository/ScheduleRepository.java; agenda/repository/AvailabilitySlotRepository.java; agenda/mapper/AgendaMapper.java; agenda/entity/Schedule.java; agenda/entity/AvailabilitySlot.java; agenda/exception/AgendaExceptionHandler.java |
| S07 | apps/backend/src/main/java/com/hospital/platform/appointments/controller/AppointmentController.java; appointments/service/AppointmentService.java; appointments/repository/AppointmentRepository.java; appointments/mapper/AppointmentMapper.java; appointments/entity/Appointment.java; appointments/dto/AppointmentResponseDTO.java; appointments/exception/AppointmentExceptionHandler.java |
| S08 | apps/backend/src/main/java/com/hospital/platform/agenda/contract/CapacityGateway.java; agenda/contract/PostgresCapacityGateway.java |
| S09 | database/migrations/V1__initial_schema.sql; V2__create_refresh_tokens.sql; V3__support_appointment_lifecycle.sql; V4__patient_self_registration.sql; V5__domain_integrity_hardening.sql |
| S10 | apps/frontend/app/register/page.tsx; apps/frontend/app/login/page.tsx; apps/frontend/app/patient/layout.tsx; apps/frontend/app/patient/availability/page.tsx; apps/frontend/app/patient/appointments/page.tsx |
| S11 | apps/frontend/lib/backend.ts; lib/api-client.ts; lib/types.ts; app/api/session/login/route.ts; app/api/session/register/route.ts; app/api/session/logout/route.ts; app/api/patient/availability/route.ts; app/api/patient/appointments/route.ts |
| S12 | apps/frontend/components/patient-navigation.tsx; components/status-badge.tsx; components/logout-button.tsx; components/brand.tsx; apps/frontend/app/globals.css; apps/frontend/tests/patient-core.spec.ts |
| S13 | docs/architecture/C1-C-V5-CUTOVER-REPORT.md; docs/architecture/C1-B-IMPLEMENTATION-REPORT.md; docs/architecture/C1-B-PRE-IMPLEMENTATION-AUDIT.md; docs/architecture/C1-A2-20-POSTGRESQL-CAPACITY-DESIGN.md; docs/architecture/validation/C1-B-APP-RUNTIME-ROLE.sql |
| S14 | apps/backend/src/main/java/com/hospital/platform/agenda/demo/DevDemoDataSeeder.java; apps/backend/src/main/resources/application.yml |

## 2. Current Functional Baseline

Recorrido implementado [S02–S12]:

1. /register envía nueve campos a /api/session/register; el BFF reenvía POST /auth/register.
2. PatientRegistrationService abre una transacción, UserService crea User con BCrypt, email normalizado, username sintético y exclusivamente PATIENT; PatientService crea Patient vinculado con insurance. Duplicar documento revierte también User.
3. Login independiente crea tokens. El BFF guarda hp_access y hp_refresh HttpOnly, SameSite=Lax, Secure en producción.
4. /patient/availability consulta /api/patient/availability → GET /availability. Para PATIENT se usa una proyección SQL con horario activo, profesional activo, especialidad activa, usuario habilitado cuando existe, slot AVAILABLE futuro y sin cita ocupante.
5. Selección de slot + reason → POST /appointments. AppointmentService resuelve Patient por usuario autenticado; enviar patientId siendo solo PATIENT produce 403.
6. CapacityGateway → PostgresCapacityGateway → capacity_reserve en V5 reserva el slot y crea Appointment SCHEDULED sin flowStage en una sola transacción.
7. /patient/appointments → GET /appointments aplica filtro server-side por patientId para PATIENT.

C1-C documenta Flyway V1–V5 exitoso, Hibernate validate, smoke con reserva 201 y conflicto 409 SLOT_UNAVAILABLE, PostgreSQL con slot RESERVED, 217/217 pruebas Maven y 2/2 Playwright. También documenta funcionamiento de un backend con rol runtime restringido para el flujo paciente [S13]. No extrapolar ese resultado a todos los CRUD ADMIN.

Baseline considerado estable para este bloque; no rediseñar registro ni reserva. Login no cuenta como formulario, y “Mis citas” es mejora adicional. Los cinco formularios son procesos de negocio; los filtros auxiliares no incrementan el conteo.

## 3. Five Forms Audit

### F01 — Registro de paciente: READY, regresión

Campos reales: email, password, documentType, documentNumber, firstName, lastName, birthDate, phone e insurance. Todos obligatorios en UI y DTO [S02,S10].

Validación backend: email hasta 255; password 8–128; nombres hasta 100; fecha @Past; teléfono opcionalmente + seguido de 7–15 dígitos; insurance hasta 150. DTO rechaza campos desconocidos; el usuario no puede asignarse roles. DNI: 8 dígitos; CE: 8–12 alfanuméricos; PASSPORT: 6–12. Normalización documental a mayúsculas y trim, unicidad compuesta documentType + documentNumber en V5, sin perder ceros iniciales [S02,S04,S09].

Persistencia: UserRepository/RoleRepository y PatientRepository → users, roles, user_roles, patients; User + Patient atómicos. Nombres pertenecen a users, no a patients. No login automático tras registro [S02–S04].

Tests: RegisterPatientRequestDTOTest, PatientRegistrationServiceTest, AuthRegistrationControllerTest, PatientRegistrationIT, UserServiceTest, PatientServiceTest y patient-core.spec.ts. PatientRegistrationIT comprueba rollback por documento duplicado y dominio documental [ruta base de tests en §11].

Deuda pequeña: UI calcula fecha máxima con UTC del navegador; revisión de borde civil Lima recomendable en regresión, no refactor obligatorio. Placeholders telefónicos y locale es-CO son presentación; no cambiar reglas de negocio por ello. No se detectó una brecha que justifique rehacer F01.

### F02 — Reserva PATIENT: READY, regresión

La UI ya muestra especialidad, profesional, fecha, inicio y fin en tarjetas y resumen modal, y recoge motivo. El request final usa slotId + reason; los demás campos se derivan del slot, no deben poder falsificarse [S06,S07,S10].

CreateAppointmentRequestDTO exige slotId, admite patientId opcional y reason sin @NotBlank ni límite. UI exige motivo no vacío y máximo 1000. Esta diferencia es deuda de contrato, no motivo para romper compatibilidad durante G.1.


Concurrencia: V5 usa advisory transaction lock, locks de contexto y UPDATE condicional de AVAILABLE a RESERVED; índice único de ocupación incluye SCHEDULED, CONFIRMED y COMPLETED. Los fallos revierten la transacción; servicio convierte DataIntegrityViolationException de creación en SlotUnavailableException y handler en 409 SLOT_UNAVAILABLE [S07–S09]. Esa traducción amplia puede presentar otros fallos de integridad como slot no disponible; considerar precisión futura sin alterar happy path.

Tests reales de DB: PatientAvailabilityFlowIT, AppointmentModuleIT, AppointmentPersistenceIT, CapacityGatewayIT y suites lifecycle. CapacityGatewayIT incluye dos reservas simultáneas con un ganador y sin deadlock; Playwright también reproduce slot tomado entre selección y envío [§11]. No confundir el escenario secuencial del navegador con prueba de simultaneidad DB.

No requiere esquema ni endpoints nuevos para reserva propia.

### F03 — Gestión de profesional ADMIN: PARTIAL

| Operación | Código actual | Estado |
|---|---|---|
| Listar | GET /professionals, registros deleted_at IS NULL | IMPLEMENTADO |
| Consultar | GET /professionals/{id}, activo por soft-delete | IMPLEMENTADO |
| Crear | POST /professionals, userId opcional + licenseNumber | IMPLEMENTADO |
| Editar | PUT /professionals/{id}, solo licenseNumber | IMPLEMENTADO |
| Desactivar | ProfessionalService.deactivateProfessional → gateway/DB; ningún mapping HTTP | PARCIAL |
| Reactivar | No controller/service/gateway específico; no listado de inactivos | AUSENTE |
| Asociar/quitar/listar especialidades | Tablas y SQL existen, ninguna API específica | AUSENTE |

Fuente: [S05,S08,S09]. **No existen entidades JPA Specialty ni ProfessionalSpecialty en el árbol auditado**, ni controllers/repositorios específicos; son tablas reales consultadas con SQL desde agenda/gateway y seed. No atribuir CRUD por existencia de tabla.

Colegiatura es String/VARCHAR, regex 4–6 dígitos en DTO, servicio y CHECK DB; UNIQUE preserva ceros iniciales. Professional no contiene nombres ni apellidos; userId es nullable y UNIQUE. Crear no valida explícitamente usuario activo o rol PROFESSIONAL ni crea User; FK y UNIQUE protegen existencia/unicidad, no la política de cuenta. PUT no cambia userId [S05,S09].

CreateUserRequestDTO tampoco acepta nombres; UpdateUserRequestDTO solo username/email y UserResponseDTO no devuelve firstName/lastName. Por tanto, combinar CRUD existentes de users y professionals no basta para un alta nominal de profesional [S03]. Los nombres de disponibilidad vienen de users con fallback license_number, no de un DTO de gestión profesional [S06].

Campos F03 actuales: colegiatura editable; usuario elegible solo al crear; id/userId/estado de lectura. Nombres y especialidades necesitan contratos nuevos. Estado inactivo deriva de deleted_at, no de active boolean en la tabla.

Recomendación MVP, pendiente de aprobación: profesional nominal ligado a User con rol PROFESSIONAL y nombres en users; alta orquestada o selección de cuenta elegible, sin introducir un segundo almacén de nombres. Permitir lectura y edición de colegiatura, asociación explícita y desactivación lógica. Reactivación queda FUTURO hasta fijar semántica. Si el requisito exige activar y desactivar ahora, F03 permanece BLOCKED para ese subalcance.

V5 impide desactivar con cita CONFIRMED en CHECK_IN/WAITING/IN_ATTENTION; también protege deshabilitar el User asociado durante atención. Conserva historial y bloquea nuevas reservas/horarios/asociaciones cuando el profesional no es operativo [S09]. La eliminación física no tiene endpoint y no debe introducirse. No afirmar una prohibición DB universal de DELETE professionals: su preservación depende también de FKs y política; la ruta soportada es soft-delete.

Quitar especialidad usada por cualquier schedule chocará con la FK compuesta V5, incluso cuando el horario está inactivo. MVP: rechazar desvinculación si hay schedules referenciándola, mantener asociación histórica; no cascadas ni borrado/recreación.

### F04 — Gestión de horario ADMIN: PARTIAL

Listar, consultar, crear, PUT y PATCH status están expuestos; filtros professionalId y specialtyId existen en GET /agendas. Create/UpdateAgendaRequestDTO contienen professionalId, specialtyId, dayOfWeek 0–6, startTime y endTime; activo se maneja mediante UpdateAgendaStatusRequestDTO [S06].

Relación DB: professionals → professional_specialties (PK compuesta) → schedules (FK compuesta V5) → availability_slots. Schedule usa UUID escalares; AvailabilitySlot sí tiene ManyToOne a Schedule. No inventar relaciones JPA a Specialty [S06,S09].

Reglas V5 [S09]:

- Intervalos [start,end), exclusión GiST por profesional y día; adyacencia válida y solapes prohibidos incluso entre especialidades.
- La exclusión aplica a active OR capacity_protected; no basta desactivar para reutilizar inmediatamente capacidad comprometida.
- Edición estructural se bloquea si existe **cualquier slot**, no únicamente RESERVED. Servicio PUT llama capacity_schedule_reconfigure y recibe 409 AGENDA_CAPACITY_CONFLICT.
- Desactivar cambia active=false y protege capacidad; preserva slots y citas futuras. Lectura paciente excluye el horario desactivado.
- capacity_schedule_status no reconcilia slots ni libera capacity_protected. No asumir liberación automática al pasar la última cita.
- Slots históricos con citas no se pueden borrar/mover silenciosamente; COMPLETED continúa consumiendo slot RESERVED.
- Slots duran 30 minutos; civil timezone America/Lima. El DTO usa domingo=0, sábado=6.
- SlotGenerationService genera del día siguiente a +14 días, en bloques completos de 30 minutos, idempotente por fecha/inicio; no crea el día actual ni el remanente inferior a 30 minutos.

Brecha operativa decisiva: AgendaService.createAgenda no invoca generate; no endpoint de generación ni tarea recurrente detectada. DevDemoDataSeeder es el único invocador actual [S14]. Un horario creado por API puede existir sin disponibilidad reservable.

MVP seguro: listar/detallar, crear con asociaciones obtenidas por API, editar solo antes de tener slots, cambiar estado preservando capacidad y publicar slots mediante operación explícita autorizada que reutilice el generador. Mostrar restricciones de edición y conflicto de capacidad. Si se exige reconciliar free slots, diseñarlo aparte: no aplicar DELETE seguido de UPDATE para esquivar V5. Cambio futuro de las funciones/guards necesitaría migración nueva y pruebas PostgreSQL; está fuera del MVP.

Faltan UI completa, catálogo de especialidades/relaciones, feedback estructural y de capacidad, y disparador operativo de slots. Opcional proyección de resumen con canEditStructure/capacityProtected para evitar UI que prometa operaciones imposibles; columnas ya existen.

### F05 — Gestión de cita RECEPTIONIST: PARTIAL; creación asistida BLOCKED end-to-end

Respuestas obligatorias, con permisos de un usuario exclusivamente RECEPTIONIST [S01,S04–S07]:

| Pregunta | Respuesta actual |
|---|---|
| 1. ¿Buscar pacientes? | No; GET /patients y /patients/{id} son ADMIN. No búsqueda específica. |
| 2. ¿Criterio? | Ninguno expuesto. Repository solo tiene existencia documental y lookup por UUID/User; no búsqueda recepción. |
| 3. ¿Obtener patientId sin introducirlo? | No mediante API de pacientes autorizada. GET /appointments revela patientId para pacientes con citas previas; no sustituye búsqueda ni cubre paciente sin citas. |
| 4. ¿Consultar disponibilidad? | No; GET /availability admite ADMIN/PATIENT. |
| 5. ¿Obtener slotId autorizado? | No para un slot libre mediante discovery. Puede leer slotId de citas existentes en GET /appointments; no permite seleccionar capacidad nueva. |
| 6. ¿Crear para paciente? | Sí POST /appointments con patientId obligatorio para creación administrativa y slotId válido conocidos externamente. |
| 7. ¿Consultar citas? | Sí GET /appointments devuelve todas, sin filtro/paginación; GET /appointments/{id} también. |
| 8. ¿Confirmar? | Sí, propia restricción de estado y futuro; no exige ownership paciente para recepción. |
| 9. ¿Cancelar? | Sí, SCHEDULED/CONFIRMED futuras, sin flujo activo. |
| 10. ¿Reprogramar? | Sí con slot nuevo, mismas barreras; falta discovery autorizado. |
| 11. ¿CHECK_IN? | Sí, exclusivamente RECEPTIONIST en controller; CONFIRMED y flowStage null. |
| 12. ¿WAITING? | Sí, exclusivamente RECEPTIONIST; requiere CHECK_IN. |
| 13. ¿IN_ATTENTION? | No; POST start-attention exige PROFESSIONAL vinculado y activo. |
| 14. ¿Finalizar? | No; POST complete exige ese PROFESSIONAL, IN_ATTENTION y slot no futuro. |
| 15. ¿Responsabilidad? | Recepción: creación asistida, confirmación/cancelación/reprogramación ordinarias, ingreso y espera. Profesional: iniciar/finalizar atención operativa, sin introducir diagnóstico ni historia clínica. |

AppointmentService.hasAdministrativeRole engloba ADMIN y RECEPTIONIST solo para citas; no les concede CRUD de usuarios, profesionales o pacientes. Para consultas PATIENT hay ownership; el rol PROFESSIONAL tiene operaciones asignadas pero **no GET /appointments** como rol único.

Creación normal siempre SCHEDULED. Confirmación es una operación separada. CHECK_IN → WAITING → IN_ATTENTION → FINISHED corresponde a flowStage; FINISHED se combina con appointmentStatus COMPLETED. Cancelar/reprogramar están bloqueados desde CHECK_IN y en citas cuyo inicio ya pasó. COMPLETED conserva ocupación e historial; reprogramar crea sucesor SCHEDULED y original RESCHEDULED [S07–S09].

No hay condición de día/hora en métodos checkInAppointment ni moveAppointmentToWaiting aparte de estado; capacity_appointment_stage tampoco la impone. Por ello pueden ejecutarse anticipadamente sobre cita confirmada. Requiere decisión humana sobre ventana de ingreso; no añadir una regla de tiempo inventada en la UI y presentarla como dominio actual.


MVP recomendado F05: búsqueda exacta por documentType+documentNumber; seleccionar paciente encontrado; elegir slot operativo; reason; crear; tabla de citas filtrada por paciente/fecha y acciones confirmar/cancelar/reprogramar. CHECK_IN/WAITING pueden incluirse si se acepta expresamente su política temporal actual o se aprueba una nueva. IN_ATTENTION y finalizar no pertenecen al portal recepción.

Búsqueda futura (DISEÑADA): devolver patientId, identidad mínima y nombre cuando User exista; no birthDate/address/insurance por defecto. Para pacientes administrativos sin User, no hay nombres en patients: mostrar identificación documental como fallback y decidir elegibilidad de atención. No dar GET /users ni listado irrestricto /patients a recepción.

## 4. My Appointments Audit

La página /patient/appointments solicita /api/patient/appointments; el BFF hace GET /appointments; el servicio filtra por Patient resuelto desde User autenticado [S07,S10,S11].

Response exacto: id, patientId, professionalId, slotId, appointmentStatus, flowStage, reason, cancelledAt, cancelledBy, createdAt, updatedAt. No appointmentDate, horas, specialtyId, nombres. Appointment no tiene join JPA con slot/profesional: UUID escalares. Mapper no puede derivar esos datos solo desde Appointment. createdAt es registro, no fecha de cita; la UI lo etiqueta como registro y muestra IDs de profesional/slot [S07,S10].

| Alternativa | Impacto / valoración |
|---|---|
| A. Enriquecer AppointmentResponseDTO | Menos contratos, pero afecta create/get/list y todos los endpoints de transición; exige resolución adicional en respuestas de escritura. Válida si se quiere resumen universal. |
| B. DTO específico de resumen | Recomendada: separar lectura de presentación del contrato de comandos y reservar cambios a una consulta. Compatible con proyección SQL ya usada por availability. |
| C. Requests frontend adicionales | No recomendada: PATIENT no tiene acceso a professionals/{id}, availability/{id} ni especialidades; disponibilidad pública al paciente excluye RESERVED/pasados. Caché del último slot no cubre recarga/historial. También crea fan-out. |

Diseño mínimo B, FUTURO: consulta propia de resumen, por ejemplo GET /appointments/me/summary (no existe hoy), DTO propuesto PatientAppointmentSummaryDTO con appointmentId, status, flowStage nullable, reason nullable, specialtyId, specialtyName, professionalId, professionalName, slotId, appointmentDate, startTime y endTime.

Consulta única proyectada: appointments JOIN availability_slots JOIN schedules JOIN specialties JOIN professionals LEFT JOIN users. Filtrar por patientId autenticado antes de devolver. Usar tablas históricas sin filtrar active/deleted_at para conservar citas canceladas/reprogramadas/completadas o profesional desactivado. Fallback de nombre a colegiatura si no existe nombre; mantener fecha/hora civil Lima. No devolver entidades JPA ni cargar cada slot dentro de un bucle.

Este modelo entrega los nombres **actuales** de los catálogos, no snapshots históricos. Si se exige el nombre inmutable al reservar, sería otro requisito y podría requerir migración; no es parte del MVP.

Impacto: controller añade lectura propia con PATIENT; service resuelve patientId; repository/proyección realiza joins acotados; DTO nuevo; tests de ownership, nulos, histórico y número constante de consultas; BFF consume el nuevo endpoint; types y tarjetas muestran especialidad/profesional/fecha/horas. Mantener AppointmentResponseDTO de comandos evita tocar F02.

No sumar botones patient cancelar/reprogramar en G.2 automáticamente: aunque APIs existen, el alcance solicitado es mejora informativa y requiere aceptar UI de acciones.

## 5. ADMIN Portal Audit

Todo el portal está AUSENTE en frontend [S10–S12]. No hay dashboard operativo ni métricas reales.

| Sección MVP DISEÑADA | Existente | Falta mínima | UI / acciones | Riesgo |
|---|---|---|---|---|
| Profesionales | CRUD básico POST/GET/PUT ADMIN [S05] | Identidad nominal y cuenta; catálogo/relación; exponer desactivación; precisión de conflictos | Lista con colegiatura/nombre/estado, formulario crear/editar, asociación y desactivar | Duplicidad de cuentas, soft-delete, quitar relación histórica |
| Horarios | POST/GET/PUT/PATCH /agendas ADMIN [S06] | Selectores reales, restricciones visibles y publicación de slots | Tabla con profesional/especialidad/día/horas/activo; F04; activar/desactivar | Horario con slots no editable; capacidad protegida |
| Disponibilidad | GET /availability y /{id}, filtros reales [S06] | Resumen de nombres y generación operativa autorizada | Tabla por profesional/fecha/estado; publicar mediante acción explícita | Mapper ADMIN deja nombres/IDs adicionales nulos y usable no verifica futuro/profesional |

ADMIN availability usa AgendaMapper con constructor corto: professionalId/name y specialtyId/name son null. isUsable solo verifica AVAILABLE + schedule.active, no futuro, User habilitado ni Professional/Specialty operativos [S06]. No usar esa bandera como autorización para reservar o como equivalente de disponibilidad PATIENT; DB sigue validando.

Todas las secciones exigen ADMIN y navegación por rol obtenido del backend, no solo presencia de cookie. El MVP no ofrece eliminación física ni bypass de V5.

FUTURO: reactivación profesional aprobada, reconciliación/versionado de horarios, horizonte de slots renovable con política, reportes operativos simples después de datos reales. Fuera del bloque: farmacia, inventario, clínica, diagnósticos, tratamientos, facturación y analítica compleja.

## 6. RECEPTIONIST Portal Audit

Frontend recepción AUSENTE. Backend soporta acciones sobre citas pero no el flujo de selección completo [S04,S06,S07,S10].

Propuesta DISEÑADA: /reception/appointments con búsqueda documental exacta, resultados mínimos, filtro de citas y listado humano; /reception/appointments/new con pasos en una página: paciente → disponibilidad → motivo → confirmación del registro SCHEDULED. Confirmar cita utiliza acción posterior, no una etiqueta ambigua que prometa CONFIRMED al reservar.

Brechas mínimas: búsqueda paciente autorizada y DTO reducido; lectura de disponibilidad operativa para recepción; listado/sumario de citas con filtro server-side, paginación o límite razonable; BFF y role guards; acciones con 409/control de estado; contratos de errores coherentes.

No habilitar recepción en todas las rutas ADMIN. Consultar catálogos completos de profesionales/especialidades no es necesario para crear si availability retorna IDs/nombres y filtros suficientes. El actual GET /appointments global expone todas las citas y motivos; decisión de privacidad pendiente para preservar, limitar o sustituir esa lectura. No ocultar datos en UI como sustituto de restricción backend.

## 7. API Inventory

Rutas de controller; prefijo real /api/v1 en application.yml [S14]. Todos son IMPLEMENTADOS en código salvo filas explícitas AUSENTE/PARCIAL. “—” en request significa sin body, NO APLICA. Roles listados son autorización de controller más matices de servicio; no resultados runtime de G.1.

### Auth / users / patients

| HTTP | Ruta | Roles | Request → Response DTO | Servicio/método | Persistencia | Estado |
|---|---|---|---|---|---|---|
| POST | /auth/register | Público | RegisterPatientRequestDTO → RegisterPatientResponseDTO | PatientRegistrationService.register | User/Role/PatientRepository; users/user_roles/roles/patients | IMPLEMENTADO |
| POST | /auth/login | Público | LoginRequestDTO → LoginResponseDTO | AuthService.login | UserRepository, RefreshTokenService/RefreshTokenRepository; users/roles/permissions/refresh_tokens | IMPLEMENTADO |
| POST | /auth/refresh | Público; token válido | RefreshTokenRequestDTO → LoginResponseDTO | AuthService.refresh | RefreshTokenRepository, UserRepository | IMPLEMENTADO |
| POST | /auth/logout | Autenticado | — → 204 | AuthService.logout | RefreshTokenService revoca refresh_tokens | IMPLEMENTADO |
| POST | /users | ADMIN | CreateUserRequestDTO → UserResponseDTO | UserService.createUser | UserRepository/RoleRepository → users/user_roles | IMPLEMENTADO |
| GET | /users | ADMIN | — → List<UserResponseDTO> | UserService.findUsers | UserRepository.findAllActiveWithRoles | IMPLEMENTADO |
| GET | /users/me | Autenticado | — → UserResponseDTO | UserService.findCurrentUser | CurrentUser + UserRepository | IMPLEMENTADO |
| GET | /users/{id} | ADMIN | — → UserResponseDTO | UserService.findUserById | UserRepository | IMPLEMENTADO |
| PUT | /users/{id} | ADMIN | UpdateUserRequestDTO → UserResponseDTO | UserService.updateUser | UserRepository/JPA dirty checking | IMPLEMENTADO |
| PATCH | /users/{id}/status | ADMIN | UpdateUserStatusRequestDTO → UserResponseDTO | UserService.updateStatus | CapacityGateway.capacity_user_status; users | IMPLEMENTADO |
| POST | /users/{id}/roles | ADMIN | AssignRoleRequestDTO → UserResponseDTO | UserService.assignRoles | capacity_lock_user + RoleRepository; user_roles | IMPLEMENTADO |
| POST | /patients | ADMIN | CreatePatientRequestDTO → PatientResponseDTO | PatientService.createPatient | PatientRepository → patients | IMPLEMENTADO |
| GET | /patients | ADMIN | — → List<PatientResponseDTO> | PatientService.findPatients | PatientRepository sin deleted_at | IMPLEMENTADO |
| GET | /patients/me | PATIENT | — → PatientResponseDTO | PatientService.findCurrentPatient | PatientRepository por current User | IMPLEMENTADO |
| GET | /patients/{id} | ADMIN | — → PatientResponseDTO | PatientService.findPatientById | PatientRepository | IMPLEMENTADO |
| PUT | /patients/{id} | ADMIN | UpdatePatientRequestDTO → PatientResponseDTO | PatientService.updatePatient | PatientRepository/JPA | IMPLEMENTADO |
| PATCH | /patients/{id}/status | ADMIN | UpdatePatientStatusRequestDTO → PatientResponseDTO | PatientService.updateStatus | Patient.deactivate/JPA | IMPLEMENTADO |
| POST | /patients/{id}/user | ADMIN | LinkUserRequestDTO → PatientResponseDTO | PatientService.linkUser | PatientRepository/UserLookupService | IMPLEMENTADO |

| — | Búsqueda de pacientes para recepción | Ninguno | No contrato | No servicio expuesto | Tabla existe | AUSENTE |

Fuentes [S01–S04,S08]. UserResponseDTO tiene id/username/email/enabled/roles/createdAt/updatedAt, sin nombres. PatientResponseDTO tiene id/userId/documentType/documentNumber/birthDate/phone/insurance/address/active, sin nombres. PatientStatus solo INACTIVE: no activar paciente por esa API. linkUser no asigna PATIENT; no sustituye registro [S04].

### Professionals / specialties / agenda

| HTTP | Ruta | Roles | Request → Response DTO | Servicio/método | Persistencia | Estado |
|---|---|---|---|---|---|---|
| POST | /professionals | ADMIN | CreateProfessionalRequestDTO → ProfessionalResponseDTO | ProfessionalService.createProfessional | ProfessionalRepository.save → professionals | IMPLEMENTADO |
| GET | /professionals | ADMIN | — → List<ProfessionalResponseDTO> | ProfessionalService.findProfessionals | ProfessionalRepository | IMPLEMENTADO |
| GET | /professionals/{id} | ADMIN | — → ProfessionalResponseDTO | ProfessionalService.findProfessionalById | ProfessionalRepository | IMPLEMENTADO |
| PUT | /professionals/{id} | ADMIN | UpdateProfessionalRequestDTO → ProfessionalResponseDTO | ProfessionalService.updateProfessional | ProfessionalRepository/JPA | IMPLEMENTADO |
| — | Desactivar profesional: ruta AUSENTE | Ninguno por HTTP | No request | deactivateProfessional existe internamente | capacity_professional_deactivate → professionals | PARCIAL |
| — | Reactivar profesional | Ninguno | No contrato | No operación específica | deleted_at existe | AUSENTE |
| — | CRUD/lectura specialties y professional_specialties | Ninguno | No DTO | No controller propio | Tablas V1; SQL agenda/seed/gateway | AUSENTE |
| POST | /agendas | ADMIN | CreateAgendaRequestDTO → AgendaResponseDTO | AgendaService.createAgenda | capacity_schedule_create + ScheduleRepository | IMPLEMENTADO |
| GET | /agendas | ADMIN | —; professionalId/specialtyId query → List<AgendaResponseDTO> | findAgendas | ScheduleRepository.findSchedules | IMPLEMENTADO |
| GET | /agendas/{id} | ADMIN | — → AgendaResponseDTO | findAgendaById | ScheduleRepository | IMPLEMENTADO |
| PUT | /agendas/{id} | ADMIN | UpdateAgendaRequestDTO → AgendaResponseDTO | updateAgenda | capacity_schedule_reconfigure | IMPLEMENTADO con restricción V5 |
| PATCH | /agendas/{id}/status | ADMIN | UpdateAgendaStatusRequestDTO → AgendaResponseDTO | changeAgendaStatus | capacity_schedule_status | IMPLEMENTADO |
| GET | /availability | ADMIN/PATIENT | —; scheduleId/professionalId/slotDate/status → List<AvailabilitySlotResponseDTO> | findAvailability | AvailabilitySlotRepository; proyección PATIENT o join fetch ADMIN | IMPLEMENTADO |
| GET | /availability/{id} | ADMIN | — → AvailabilitySlotResponseDTO | findAvailabilitySlotById | AvailabilitySlotRepository | IMPLEMENTADO |
| — | Generación operativa de slots | Ninguno por HTTP | No contrato | SlotGenerationService.generate interno | capacity_generate_slot_created | PARCIAL |

Fuentes [S05,S06,S08,S09,S14]. AvailabilitySlotResponseDTO: id y alias JSON slotId, scheduleId, slotDate, startTime, endTime, status, usable, professionalId/name, specialtyId/name; los cuatro últimos son null en mapping ADMIN. No filtro specialtyId en /availability; status se ignora en rama PATIENT que siempre retorna AVAILABLE operativo.

### Appointments

Todas las respuestas usan AppointmentResponseDTO; repositorio lee appointments y gateways escriben appointments/availability_slots y, según operación, audit_logs a través de AuditLogService. PatientLookupService y ProfessionalLookupService encapsulan people repositories [S07,S08].

| HTTP | Ruta | Roles | Request | Servicio/método | Persistencia principal | Estado |
|---|---|---|---|---|---|---|
| POST | /appointments | PATIENT/ADMIN/RECEPTIONIST | CreateAppointmentRequestDTO | createAppointment | capacity_reserve + AppointmentRepository | IMPLEMENTADO |
| GET | /appointments | PATIENT/ADMIN/RECEPTIONIST | — | findAppointments | findAllByPatientIdOrderByCreatedAtDesc o findAllByOrderByCreatedAtDesc | IMPLEMENTADO |
| GET | /appointments/{id} | PATIENT/ADMIN/RECEPTIONIST | — | findAppointmentById | AppointmentRepository.findById + ownership | IMPLEMENTADO |
| POST | /appointments/{id}/confirm | PATIENT/ADMIN/RECEPTIONIST | — | confirmAppointment | capacity_lock_appointment/appointment_confirm + audit | IMPLEMENTADO |
| POST | /appointments/{id}/cancel | PATIENT/ADMIN/RECEPTIONIST | — | cancelAppointment | capacity_lock_appointment/cancel(uuid,uuid) + audit | IMPLEMENTADO |
| POST | /appointments/{id}/reschedule | PATIENT/ADMIN/RECEPTIONIST | RescheduleAppointmentRequestDTO | rescheduleAppointment | capacity_lock_reschedule/reschedule + successor query + audit | IMPLEMENTADO |
| POST | /appointments/{id}/check-in | RECEPTIONIST | — | checkInAppointment | capacity_lock_appointment/appointment_stage + audit | IMPLEMENTADO |
| POST | /appointments/{id}/waiting | RECEPTIONIST | — | moveAppointmentToWaiting | capacity_lock_appointment/appointment_stage + audit | IMPLEMENTADO |
| POST | /appointments/{id}/start-attention | PROFESSIONAL | — | startAppointmentAttention | capacity_lock_appointment/appointment_stage + ownership + audit | IMPLEMENTADO |
| POST | /appointments/{id}/complete | PROFESSIONAL | — | completeAppointment | capacity_lock_appointment/appointment_complete + audit | IMPLEMENTADO |

No búsqueda/filtro por patientId/fecha en GET actual, ni resumen nominal de citas. Professional puro no está autorizado para GET; no inferir listado propio de su permiso para iniciar atención.

### Trazabilidad entidades/tablas

| Módulo | Entidades JPA verificadas | Tablas afectadas relevantes |
|---|---|---|
| auth/users | User, Role, Permission, RefreshToken | users, roles, permissions, user_roles, role_permissions, refresh_tokens |
| patients | Patient | patients; enlace users |
| professionals | Professional | professionals; consulta users para cuenta operativa |
| specialties | No entidad JPA específica | specialties, professional_specialties |
| agenda | Schedule, AvailabilitySlot | schedules, availability_slots; joins professionals/specialties/users; hospital_business_config vía reloj DB |
| appointments/audit | Appointment, AuditLog | appointments, availability_slots, audit_logs |

Fuentes [S03–S09]. Gateway hace flush/clear del EntityManager antes/después de mutaciones SQL, evitando respuestas desde entidades cacheadas. No sustituir por escrituras JPA directas a capacidad.

## 8. RBAC Matrix

P = permitido hoy; X = prohibido al rol único por contrato HTTP; A = operación HTTP ausente. Propio/asignado = restricción adicional en servicio. Público = sin rol requerido. Recomendación es FUTURO, no permiso actual. Usuarios multirrol acumulan permisos; probarlos aparte.

| FUNCIÓN | PATIENT | ADMIN | RECEPTIONIST | PROFESSIONAL | ESTADO ACTUAL / RECOMENDACIÓN DEL BLOQUE |
|---|---|---|---|---|---|
| self-register | Público | Público | Público | Público | Crea solo PATIENT; conservar |
| login | Público | Público | Público | Público | Cuenta activa y credenciales; conservar |
| consultar disponibilidad | P operativa | P administrativa | X | X | Habilitar lectura operativa mínima recepción |
| crear propia cita | P propio | P con patientId | P con patientId | X | Conservar resolución server-side |
| ver propias citas | P propio | P global | P global | X | Conservar PATIENT; limitar vista recepción según decisión |
| listar pacientes | X | P | X | X | Mantener ADMIN |
| buscar paciente por criterio | A | A | A | A | Añadir búsqueda documental mínima ADMIN/RECEPTIONIST |
| listar profesionales | X | P | X | X | Mantener ADMIN |
| crear profesional | X | P | X | X | Completar identidad/cuenta ADMIN |
| editar profesional | X | P colegiatura | X | X | No asumir nombres editables |
| desactivar profesional | A | A | A | A | Exponer servicio solo ADMIN |
| reactivar profesional | A | A | A | A | FUTURO hasta decisión |
| gestionar especialidades de profesional | A | A | A | A | Añadir ADMIN con FK histórica |
| listar horarios | X | P | X | X | Mantener ADMIN |
| crear horario | X | P | X | X | Añadir selectores y publicación |
| editar horario | X | P condicionado V5 | X | X | No editar estructura con slots |
| desactivar/activar horario | X | P | X | X | Conservar gateway/capacidad protegida |
| generar slots por HTTP | A | A | A | A | Acción operativa solo ADMIN |
| crear cita para otro paciente | X | P | P | X | Recepción necesita búsqueda/slots |
| consultar citas de paciente | P propio | P todas/detalle | P todas/detalle | X | Filtro específico ausente; proponerlo |
| confirmar cita | P propio | P | P | X | Estado/futuro; conservar |
| cancelar cita | P propio | P | P | X | Futuro/sin flow activo; conservar |
| reprogramar cita | P propio | P | P | X | Futuro/sin flow activo; slots recepción pendientes |
| check-in | X | X | P | X | Ventana temporal pendiente |
| waiting | X | X | P | X | Ventana temporal pendiente |
| in-attention | X | X | X | P asignado | Mantener PROFESSIONAL |
| finalizar | X | X | X | P asignado | Mantener PROFESSIONAL |

Fuentes [S01,S04–S07]. @EnableMethodSecurity está activo; salvo register/login/refresh, SecurityFilterChain exige autenticación. JWT filter recarga User y autoridades desde DB y comprueba enabled; no basar seguridad solo en claims de rol del navegador. RoleName incluye también TRIAGE y SYSTEM; no se incorporan a los portales de este bloque.

Hallazgo multirrol: findAppointments prioriza ADMIN/RECEPTIONIST frente a PATIENT, mientras AgendaService.isPatient prioriza presencia PATIENT. El BFF /api/patient/appointments no añade ownership: un usuario ADMIN o RECEPTIONIST puede usar esa ruta y recibir listado global. PatientLayout solo verifica cookies, y login siempre dirige a /patient/availability. Se necesita routing y verificación efectiva de rol; un resumen propio dedicado también evita reutilizar la consulta global como “Mis citas”.

## 9. Database Impact


| Entrega | ¿Cambio de esquema MVP? | Justificación |
|---|---|---|
| F03 | No, si nombres residen en User vinculado | users ya tiene first_name/last_name, professionals user_id nullable/UNIQUE y relación en professional_specialties |
| F04 | No con lifecycle V5 actual | schedules, capacity_protected, slots y funciones necesarias ya existen |
| F05 | No | Búsqueda/proyecciones/permisos reutilizan identidad y citas; no nuevas etapas |
| Mis citas | No | Joins/proyección sobre tablas existentes |
| Grants runtime | No requiere nuevas columnas/tablas | Operación de privilegios distinta de DDL; revisar antes de liberar ADMIN |

**No crear V6 para el MVP recomendado.** No modificar V1–V5. Fuentes [S09,S13].

Posibles migraciones futuras justificadas: nombres de profesional independiente de User si ese caso se exige; nombres snapshot de historial; versionado/reconciliación estructural de horarios con slots; cambio de funciones DB para nueva política temporal. Cambiar funciones/guards también se versiona con migración aunque no se agreguen columnas.

C1-B-APP-RUNTIME-ROLE.sql concede SELECT global y EXECUTE gateways, pero no INSERT/UPDATE directo a professionals ni mutación de professional_specialties/specialties. ProfessionalService crea/edita vía JPA, por lo que RBAC ADMIN no garantiza ejecución con ese runtime role. C1-C confirma esta limitación y ejecución demo con propietario de esquema. Requiere grants mínimos o gateway para estos casos, validación con rol restringido, y procedimiento operativo aprobado; G.1 no cambia privilegios.

Preservación: V5 impide DELETE appointments, protege slots con historia y COMPLETED, y bloquea desactivación profesional/User durante atención. Asociación usada por schedule no se elimina por FK. Repositorios actuales no son prueba de permisos SQL reales.

## 10. Frontend Impact

Rutas reales: / (app/page.tsx), /login, /register, /patient/availability y /patient/appointments; layouts global y patient. BFF: /api/session/login/register/logout y /api/patient/availability/appointments [S10,S11]. No rutas ADMIN, reception ni professional.

Componentes reales: Brand, PatientNavigation, LogoutButton y StatusBadge. No componentes CRUD profesionales/horarios/citas recepción. Formularios reales: registro, login y motivo/reserva modal. No contar login entre los cinco [S10,S12].

Auth actual: cookies HttpOnly; authorizedRequest renueva con refresh y reintenta, limpia sesión si falla; sameOrigin controla POST desde BFF, acepta ausencia de Origin; api-client redirige 401 solo para paths /api/patient/. No verificación de rol en layout ni selección de portal en login. Backend mantiene barrera de autorización, pero acceso visual/routing es incompleto para portales nuevos [S11].

Responsive implementado por CSS con breakpoints 1100/850/650px: sidebar pasa a navegación horizontal; cards y formulario se apilan; modal adapta espaciado; reduced-motion. Reserva incluye foco inicial, trap de Tab y Escape. Se verificó código CSS/DOM, no apariencia con navegador ni viewport real en G.1 [S10,S12].

Rutas DISEÑADAS mínimas:

| Ruta futura | Patrón recomendado | Propósito |
|---|---|---|
| /admin/professionals | Tabla + formulario inline o panel simple; alta /new si incluye cuenta | F03 y asociación/desactivación |
| /admin/professionals/new | Página de formulario | Alta cuenta/profesional de varios campos sin modal complejo |
| /admin/schedules | Tabla + formulario pequeño modal/inline | F04 y filtros; restricción de edición visible |
| /admin/availability | Tabla filtrada + acción publicar | Disponibilidad real, sin estadísticas inventadas |
| /reception/appointments | Búsqueda documental + tabla + acciones con confirmación breve | Consulta/gestión |
| /reception/appointments/new | Página de formulario secuencial | F05 asistido |

No construir tabla+drawer como framework obligatorio. Reutilizar estilos y componentes pequeños; página de alta para procesos largos y modal para cambios cortos. UI debe manejar loading/empty/401/403/409, fechas Lima, errores accesibles, navegación teclado y prevención de doble envío. Adaptar api-client para nuevos prefijos y role guards server-side a partir de /users/me, con política multirrol aprobada.

## 11. Test Impact

Base real de tests Java: apps/backend/src/test/java/com/hospital/platform/. Los nombres siguientes son archivos .java dentro del módulo/subdirectorio indicado.

| Área | Pruebas existentes examinadas / localizadas | Cobertura relevante |
|---|---|---|
| auth | auth/dto/RegisterPatientRequestDTOTest; auth/service/PatientRegistrationServiceTest; AuthServiceTest; RefreshTokenServiceTest; auth/controller/AuthRegistrationControllerTest; auth/PatientRegistrationIT; RefreshTokenConcurrencyIT | DTO, rollback User/Patient, rol PATIENT, login y refresh concurrente |
| patients/users | patients/service/PatientServiceTest; patients/controller/PatientControllerAuthorizationTest; patients/contract/DatabasePatientLookupServiceTest; users/service/UserServiceTest; PlatformUserDetailsServiceTest; users/controller/UserControllerAuthorizationTest | CRUD, identidad, vinculación, self profile y RBAC |
| professionals | professionals/service/ProfessionalServiceTest; professionals/controller/ProfessionalControllerAuthorizationTest; professionals/contract/DatabaseProfessionalLookupServiceTest | Colegiatura, CRUD, deactivación interna y ownership; no API de asociaciones |
| agenda | agenda/service/AgendaServiceTest; SlotGenerationServiceTest; agenda/controller/AgendaControllerTest; AgendaControllerAuthorizationTest; agenda/PatientAvailabilityFlowIT; agenda/contract/DatabaseAvailabilitySlotServiceTest; CapacityGatewayIT | Filtros paciente, generación, RBAC, reserva concurrente, capacidad al desactivar, Lima |
| appointments | appointments/service/AppointmentServiceTest; AppointmentLifecycleServiceTest; AppointmentOperationsServiceTest; appointments/controller/AppointmentControllerTest; AppointmentControllerAuthorizationTest; appointments/mapper/AppointmentMapperTest; appointments/AppointmentPersistenceIT; AppointmentModuleIT; AppointmentLifecycleIT | Creación, ownership, transiciones, rollback, idempotencia y concurrencia |
| security/audit | security/config/RoleAuthorizationTest; security/jwt/JwtServiceTest; security/AuthenticationSecurityIT; audit/contract/DatabaseAuditLogServiceTest; AuditLogServiceIT | JWT activo/deshabilitado, RBAC y persistencia audit |
| arranque | HospitalPlatformApplicationTests; HospitalPlatformApplicationIT | Arranque/DB e integración agenda |
| frontend | apps/frontend/tests/patient-core.spec.ts | Dos tests: validación documental y flujo registro/login/disponibilidad/reserva/conflicto/listado/logout |

Las suites IT usan PostgreSQL/Testcontainers; controller/service con Mockito no demuestran persistencia. AppointmentLifecycleIT incluye rollback por fallos en slots/appointment/audit, doble confirmación, cancelación y reprogramación concurrentes, ownership profesional y flujo completo que conserva RESERVED al completar. CapacityGatewayIT incluye una carrera simultánea de reservas. Playwright mezcla recorrido real con intercepts para loading/empty/error: las respuestas interceptadas prueban UI, no DB.

C1-C reporta 148 unit + 69 IT y 2 E2E aprobados históricamente. G.1 no los vuelve a ejecutar ni declara que hoy pasen.

### Pruebas y aceptación futuras por fase

| Fase | Unit/controller/RBAC | PostgreSQL/integración/concurrencia | Frontend E2E / salida |
|---|---|---|---|
| G.2 | Resumen propio, nulos, 401/403, ownership | Joins conservan historial, constante número de consultas, nombres fallback | Fecha de cita correcta, tarjetas humanas, recarga y paciente A no ve B |
| G.3 | DTOs nominales, usuario elegible, asociaciones, rol ADMIN; 4–6 dígitos | Alta atómica, duplicados concurrentes, vínculo UNIQUE, eliminación asociación referenciada rechazada, desactivación con flujo activo bloqueada; runtime role | Crear/editar profesional nominal y asociación sin UUID manual; estados y 409 |
| G.4 | DTO día/hora, autorización publicar, edición condicionada | Solape vs adyacencia, especialidad no asociada, estructura con slots rechazada, reservado/histórico preservado, generación idempotente; carrera reserva/desactivación/generación | Horario nuevo produce slots visibles PATIENT; desactivar no cancela cita |
| G.5 | Session/role routing y manejo 403/refresh | Gates efectivos en BFF/backend para multirrol | ADMIN llega a tablas reales; PATIENT no entra a gestión |
| G.6 | Búsqueda exacta, DTO mínimo, filtros, permisos negativos | Paciente sin cita, sin User/inactivo, slot operativo, concurrencia reserva recepción/paciente; límites de lectura | Contratos listos sin ampliar CRUD ADMIN |
| G.7 | BFF acciones y errores | Reusar regresión lifecycle sobre PostgreSQL | Recepción descubre IDs, crea, consulta, confirma/cancela/reprograma; check-in/waiting solo si aprobados; no atención |
| G.8 | Matriz completa de roles y casos negativos | Verify y prueba con runtime role; invariantes slot/cita, rollback y carreras | Cinco formularios contra DB real, móvil/desktop/teclado y regresión F01/F02 |

No escribir ni modificar tests en esta etapa. Ejecutarlos en futuras fases sobre DB de prueba/entorno autorizado, no sobre datos de negocio sin control.

## 12. Gap Matrix

READY=recorrido implementado; PARTIAL=capas presentes pero falta recorrido; MISSING=no capacidad expuesta; BLOCKED=depende de contrato/política imprescindible; OUT_OF_SCOPE=no entrega del bloque.

| ITEM | ACTOR | FRONTEND ACTUAL | ENDPOINT ACTUAL | BACKEND ACTUAL | DB ACTUAL | SECURITY ACTUAL | ESTADO | BRECHA | CAMBIO NECESARIO | RIESGO |
|---|---|---|---|---|---|---|---|---|---|---|
| F01 | Público/PATIENT | Registro real | POST /auth/register | Orquestación transaccional | User/Patient V5 | Pública, rol fijo | READY | Regresión civil/presentación | Preservar y probar | Romper baseline |

| F02 | PATIENT | Slots/modal real | GET /availability; POST /appointments | Patient server-side/gateway | Reserva atómica V5 | Propio | READY | Motivo UI/DTO distinto | Regresión; contrato futuro si aprobado | Conflicto ocupado |
| Mis citas | PATIENT | IDs/motivo/registro | GET /appointments | Filtro propio, DTO técnico | Datos en joins | Ownership | PARTIAL | Fecha/nombres faltan | Resumen propio proyectado | Filtrar histórico/IDOR |
| Alta nominal F03 | ADMIN | Ausente | POST /professionals y /users | No nombres en DTOs ADMIN | Nombres users, vínculo opcional | ADMIN | PARTIAL | No cuenta nominal completa | Contrato/orquestación identidad | User duplicado/parcial |
| Colegiatura F03 | ADMIN | Ausente | PUT /professionals/{id} | String regex/UNIQUE | V5 protege | ADMIN | PARTIAL | UI faltante | Formulario texto | Perder ceros |
| Estado profesional | ADMIN | Ausente | No mapping | Desactivar interno | Soft-delete/trigger | Sin rol HTTP | MISSING | Exponer deactivación | Endpoint ADMIN futuro | Atención activa |
| Reactivar profesional | ADMIN | Ausente | Ausente | Ausente | deleted_at existente | Ausente | BLOCKED | Política lifecycle | Aprobar o diferir | Reactivación insegura |
| Especialidades | ADMIN | Ausente | Ausente | SQL consultable, sin CRUD | Tablas/PK/FK | Ausente | MISSING | Catálogo y asociación API | Lectura/acciones ADMIN | Romper FK histórica |
| F04 CRUD | ADMIN | Ausente | /agendas GET/POST/PUT/PATCH | Gateway/validación | V5 capacidad | ADMIN | PARTIAL | Selectores/UX | UI F04 | Solape/inmutabilidad |
| Publicar slots | ADMIN | Ausente | Ausente | Generador interno | Función idempotente | Sin HTTP | MISSING | Horario no reservable | Disparador operativo | Capacidad vacía/duplicada |
| Edición con slots | ADMIN | Ausente | PUT existe | Reconfigure | Guard bloquea cualquier slot | ADMIN | OUT_OF_SCOPE | Reconciliación no existe | Mantener prohibición; diseño futuro | Pérdida de historial |
| Disponibilidad ADMIN | ADMIN | Ausente | GET /availability | Mapper reducido | Slots reales | ADMIN | PARTIAL | Nombres nulos/usable insuficiente | Resumen/admin filtros | Interpretación falsa de operativo |
| Búsqueda paciente | RECEPTIONIST | Ausente | Ausente; /patients ADMIN | No search | Identidad documental | Prohibido listar | BLOCKED | patientId descubrible | Search exacto mínimo | Privacidad |
| Disponibilidad recepción | RECEPTIONIST | Ausente | /availability prohibido | Query operativa reutilizable | Slots V5 | ADMIN/PATIENT | BLOCKED | slotId libre descubrible | Lectura operativa RECEPTIONIST | Exponer reserved/histórico |
| F05 crear | RECEPTIONIST | Ausente | POST /appointments | patientId requerido | V5 reserva | Permitido | PARTIAL | Selectores no autorizados | Search + disponibilidad + F05 | UUID manual |
| Consulta recepción | RECEPTIONIST | Ausente | GET /appointments | Todas sin filtro | Citas reales | Permitido global | PARTIAL | Nominal/filtros/límites | Resumen filtrado | Motivos/datos masivos |
| Confirmar/cancelar/reprogramar | RECEPTIONIST | Ausente | POST acciones | Reglas/gateway/audit | V5 histórico | Permitido | PARTIAL | UI y nuevo slot | Acciones controladas | Estado obsoleto/409 |
| CHECK_IN/WAITING | RECEPTIONIST | Ausente | POST check-in/waiting | Orden de stages | CONFIRMED | Exclusivo recepción | PARTIAL | Política temporal | Decisión + UI si aprobada | Ingreso anticipado |
| IN_ATTENTION/FINISHED | PROFESSIONAL | Ausente | POST start-attention/complete | Ownership/estados | COMPLETED reservado | Exclusivo profesional | OUT_OF_SCOPE | No portal en bloque | Preservar endpoints | Recepción sobreautorizada |
| Portales/rol | ADMIN/RECEPTIONIST | Solo patient shell | /users/me disponible | Roles DB | Roles existentes | Layout cookies solo | MISSING | Login dirige patient | Routing/guards/BFF | Usuario multirrol ve global |
| Runtime SQL ADMIN | ADMIN | No aplica | CRUD existentes | Professional JPA directo | Grants incompletos | RBAC no basta | BLOCKED | INSERT/UPDATE professions y relaciones | Grants/gateway mínimos futuros | Usar owner como solución permanente |
| Clínica/facturación | Todos | Ausente en bloque | No auditados como entrega | No entrega prevista | No cambio | No ampliar | OUT_OF_SCOPE | No corresponde | Excluir | Scope creep |

Fuentes: baseline [S02–S12]; F03 [S03,S05,S09]; F04 [S06,S08,S09,S14]; recepción [S04,S06,S07]; runtime [S13].

## 13. Recommended Phase Breakdown

Mantener numeración G.2–G.8, pero **G.3 y G.4 incluyen explícitamente las brechas backend**, y G.4 entrega publicación operativa. G.6 puede comenzar tras decisiones de privacidad sin esperar estética ADMIN; G.7 espera contratos G.6 y guard común G.5.

| Fase / objetivo | Backend | Frontend | Database | Security | Tests | Dependencias | Riesgo | Criterio de salida |
|---|---|---|---|---|---|---|---|---|
| G.2 Patient My Appointments Enhancement | DTO/proyección y lectura propia | Tarjetas con datos humanos | Sin migración | PATIENT/ownership | Resumen, histórico, E2E | Baseline G.1 | Bajo/medio | Datos completos sin fan-out, F02 intacto |
| G.3 ADMIN Professional & Specialty Management | Alta nominal/cuenta, catálogo/relaciones y desactivar | F03/listado; acceso provisional controlado | Sin esquema; grants runtime | ADMIN; usuario elegible | Unit/controller/PG/RBAC | Decisión User/nombres y asociaciones | Alto | Profesional nombrado/colegiatura/asociación usable, historial intacto |
| G.4 ADMIN Schedule & Slot Publication | Lifecycle V5 y publicar generador | F04/listado/disponibilidad | Sin migración si no reconciliar | ADMIN | Solape/adyacencia/histórico/idempotencia/PG | G.3 catálogos; decisión publicación | Alto | Horario nuevo produce slots; ocupado protegido; no edición con slots |
| G.5 ADMIN Portal Integration & Role Routing | Mantener contratos; sesión/rol si requiere BFF | Shell ADMIN, navegación y routing roles | Sin esquema | Guards/refresh/multirrol | E2E ADMIN/PATIENT y 403 | G.3/G.4; política multirrol | Medio | Tres secciones reales; login aterriza correctamente |
| G.6 RECEPTIONIST Backend Gaps | Search documental, disponibilidad operativa y resumen filtrado | Contratos/types preparados | Sin esquema; grants revisados | Permisos mínimos/privacidad | Unit/controller/PG/RBAC/concurrency | Decisiones privacidad y lectura; reutiliza diseño G.2 | Alto | IDs descubribles legalmente por API, sin CRUD ADMIN |
| G.7 RECEPTIONIST Portal | Reutilizar lifecycle | F05/consulta/acciones; stages si aprobados | Sin esquema | RECEPTIONIST, atención excluida | E2E assisted y negativos | G.6 + guard común G.5 | Medio/alto | Crear/gestionar sin UUID manual y sin permisos clínicos |
| G.8 Five Forms E2E & Regression | Correcciones acotadas si tests lo exigen | QA móvil/desktop/teclado | Validación DB de pruebas, sin esquema esperado | Matriz completa/runtime | Verify, E2E cinco procesos, carreras | G.2–G.7 | Medio | Cinco formularios reales + Mis citas, F01/F02 preservados |

Generación recurrente de horizonte no es requisito automático: MVP puede publicar explícitamente. Si se exige disponibilidad continua, aprobar tarea/ventana de renovación y añadirla al alcance G.4 antes de implementar.

## 14. Branch Strategy

No ramas creadas, no commits, no push, no merge. La rama de auditoría solicitada no está checkout: main se conserva. La promoción de este documento a chore/forms-portals-scope-audit es una operación futura del usuario/implementación.

Propuesta de ramas pequeñas, usando los nombres sugeridos por el pedido:

1. chore/forms-portals-scope-audit: documento únicamente.
2. feat/patient-appointments-enhancement: G.2.
3. feat/admin-professional-management: G.3.
4. feat/admin-schedule-management: G.4, después de merge G.3.
5. feat/admin-portal: G.5, después de G.3/G.4.
6. feat/receptionist-backend-gaps: G.6.
7. feat/receptionist-appointment-workflow: G.7, después de G.5/G.6.
8. test/five-forms-regression: G.8.

Orden de merge recomendado: auditoría → G.2 → G.3 → G.4 → G.5 → G.6 → G.7 → G.8. G.6 puede desarrollarse/mergearse antes de G.5 si no toca la UI compartida; rebasar contra base integrada y preservar contratos. No apilar todo en una mega-rama. Si G.3 o G.4 se hacen grandes, separar PR backend y UI dentro de esa fase manteniendo criterio de salida integral.

## 15. Open Decisions

### Cerradas por dominio/código

- Cinco formularios F01–F05; login excluido y Mis citas adicional.
- Registro exclusivamente PATIENT, patientId de reserva propia resuelto server-side.
- Creación SCHEDULED sin flowStage; stages CHECK_IN/WAITING/IN_ATTENTION/FINISHED.
- Recepción no inicia ni completa atención; esas operaciones son PROFESSIONAL asignado.
- Cancelar/reprogramar futuro y antes de CHECK_IN; COMPLETED conserva historial/ocupación.
- Colegiatura texto 4–6 dígitos/UNIQUE; documentos según V5; ceros iniciales.
- Asociación profesional–especialidad explícita y FK compuesta; intervalos [start,end); 30 minutos; America/Lima.
- No edición estructural con slots bajo V5 actual; no eliminación silenciosa histórica.
- Nada clínico, refactors o migración en G.1.

Fuentes [S02,S05–S09] y alcance del pedido.

### Resolubles técnicamente al implementar dentro del alcance aprobado

- DTO de resumen de lectura frente a fan-out; proyección SQL con número constante de consultas.
- Separación DTO/comandos, fallback nominal y tipos nullable.
- Formularios simples, componentes reutilizables, loading/error/empty y manejo 409.
- Guard server-side con roles efectivos; ownership, filtros antes de serializar.
- Queries/índices evaluados con datos y pruebas; no añadir índice sin evidencia.
- Reusar gateway y orden de locks; no mutación JPA directa de capacidad.

### Requieren aprobación humana antes de implementar

| Decisión | Recomendación / consecuencia |
|---|---|
| Identidad del profesional | Nominal ligado a User con PROFESSIONAL; aprobar alta de cuenta vs selección existente y manejo de credenciales. Sin User no hay nombres propios hoy. |

| Editar nombres / vínculo | Aprobar quién cambia nombres y si userId permanece inmutable; no asumir PUT actual permite ambos. |
| Reactivación | Diferir; si exigida en F03, diseñar cuenta, asociaciones y capacidad antes de exponer. |
| Especialidades | Lectura catálogo + alta/baja asociación ADMIN; baja rechazada cuando hay schedules históricos. No CRUD completo de catálogo por defecto. |
| Lifecycle horario | Conservar restricción V5 con slots; aprobar publicación explícita y horizonte. Reconciliación/versionado es futuro distinto. |
| Patient search/privacy | Buscar exacto por tipo+número; DTO mínimo; decidir nombres/eligibilidad cuando Patient no tiene User. Búsqueda por nombre/parcial queda FUTURO. |
| Lectura de citas recepción | Aprobar acceso global actual vs filtro/límites; reason puede contener información sensible aportada por paciente. No expandir payload sin necesidad. |
| CHECK_IN/WAITING | Aprobar inclusión MVP y ventana temporal; backend permite adelanto hoy. Sin decisión, mantener fuera de UI inicial y no alterar regla. |
| Rol multirrol | Elegir prioridad/selector de portal y alcance de “Mis citas”; evitar vista global bajo etiqueta personal. |
| Runtime/ADMIN bootstrap | Aprobar procedimiento de cuenta ADMIN de producción (DEC-003 sigue abierta en C1-C) y operación con SQL role mínimo. Demo owner no valida hardening completo. |
| Motivo | Decidir obligatoriedad/límite backend para nuevas operaciones; preservar compatibilidad de F02. |

## 16. Risks

1. **Alto — discovery recepción bloqueado:** CRUD citas no equivale a flujo F05; prohibir UUIDs manuales como solución final.
2. **Alto — capacidad:** editar estructura con slots o borrar asociaciones históricas rompe V5; el frontend debe explicar 409, no ocultar integridad.
3. **Alto — identidad profesional:** alta por APIs actuales no crea nombres; rol/cuenta activa no se valida en ProfessionalService.createProfessional.
4. **Alto — runtime SQL:** profesional/relaciones requieren permisos distintos de los ya validados; probar con rol restringido.
5. **Alto — privacidad:** recepción lee todas las citas/motivos actualmente; búsqueda debe minimizar datos.
6. **Medio — temporalidad:** check-in/waiting sin ventana; UTC en timestamps JPA y fecha UI no equivale a fecha civil de cita. Usar slot_date/start_time/end_time para presentación.
7. **Medio — publicación:** sin generación, F04 crea horarios vacíos; el seed no prueba administración operativa.
8. **Medio — capacidad protegida:** desactivar no libera solape; no existe liberación automática de capacity_protected en el flujo de aplicación actual.
9. **Medio — autorización visual/multirrol:** cookie no garantiza rol; BFF patient puede devolver listado administrativo si backend identifica rol administrativo.
10. **Medio — historia nominal:** resumen refleja nombres actuales; snapshot inmutable no está implementado.
11. **Bajo/medio — motivo y error:** contrato reason difiere entre UI/DTO y traducción de integridad de creación es amplia.
12. **Trazabilidad — rama/working tree:** main difiere del contexto solicitado y next-env.d.ts ya estaba modificado; no atribuir ese cambio a G.1.

No riesgos hipotéticos de módulos clínicos como justificación para ampliarlos: están excluidos.

## 17. Go / No-Go Assessment per Next Phase

| Fase | Evaluación | Gate concreto |
|---|---|---|
| G.2 | GO técnico | Mantener ownership y contrato de comandos; verificar joins/histórico |
| G.3 | CONDITIONAL GO | Aprobar identidad User/Professional, asociaciones y desactivación; runtime mínimo. Reactivación NO-GO mientras no definida |
| G.4 | CONDITIONAL GO | Aprobar publicación; mantener inmutabilidad con slots y capacidad protegida. Reconciliación NO-GO |
| G.5 | CONDITIONAL GO | Esperar APIs G.3/G.4; aprobar política multirrol y guards |
| G.6 | CONDITIONAL GO | Aprobar búsqueda/privacidad, disponibilidad operativa y alcance de lectura |
| G.7 | NO-GO hoy para flujo completo | Cerrar G.6 y routing; decidir check-in/waiting si se incluyen |
| G.8 | NO-GO como certificación hoy | Cinco formularios completos e integración PostgreSQL/runtime disponibles |

G.1: GO como auditoría y planificación; YELLOW para readiness del bloque. No autorización de implementación implícita en este documento.

### Control final de cambios

Auditoría realizada únicamente con lectura de archivos y Git. No se modificaron código, migraciones, tests, configuración ni DB por esta tarea; no se ejecutaron operaciones de negocio ni suites que crean datos. No commit, push ni merge.

Estado inicial documentado: M apps/frontend/next-env.d.ts. Cambio esperado de G.1: este archivo docs/architecture/G1-FIVE-FORMS-ADMIN-RECEPTIONIST-SCOPE-AUDIT.md. La comparación final de Git debe conservar exactamente ese cambio previo y añadir solo el informe. No afirmar working tree limpio.

