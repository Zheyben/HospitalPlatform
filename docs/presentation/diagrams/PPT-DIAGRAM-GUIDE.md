# HOSPITALPLATFORM — guía de diagramas técnicos para PPT

Fuentes: esquema PostgreSQL 16 con migraciones Flyway V1–V5, entidades y repositorios JPA, servicios y controladores Spring Boot, y rutas actuales de Next.js. Los diagramas son **PlantUML editable** (`.puml`) con exportaciones SVG y PNG de alta resolución. Para PowerPoint, insertar el SVG; usar el PNG si la versión de PowerPoint no admite SVG. El modelo físico completo es una lámina de referencia: para exposición usar 03A y 03B en dos diapositivas.

## 01 — Modelo conceptual

**Título sugerido:** «Cómo se relacionan pacientes, profesionales y citas»  
**Archivo:** [01-DATABASE-CONCEPTUAL.puml](01-DATABASE-CONCEPTUAL.puml) · [SVG](01-DATABASE-CONCEPTUAL.svg) · [PNG](01-DATABASE-CONCEPTUAL.png)

**Guion (20–30 s):** «Una cuenta puede tener rol de paciente o profesional. El paciente reserva un slot de un horario semanal asignado a un profesional y una especialidad. La cita une paciente, profesional y slot. La cuenta es opcional en los perfiles históricos, por eso la relación cuenta–perfil se muestra como cero o uno.»

**Señalar:** Usuario–Rol; Profesional–Especialidad; Horario–Slot–Cita. Una especialidad y un profesional pueden tener muchos horarios; un slot puede tener varias citas **históricas**, aunque V5 solo permite una ocupante vigente.

**Evidencia:** [V1 esquema inicial](../../../database/migrations/V1__initial_schema.sql) aprox. líneas 14–162; [V5 integridad](../../../database/migrations/V5__domain_integrity_hardening.sql) aprox. líneas 74–84 y 199–220; [Appointment](../../../apps/backend/src/main/java/com/hospital/platform/appointments/entity/Appointment.java) aprox. líneas 14–100.

## 02 — Modelo lógico

**Título sugerido:** «Claves y relaciones que sostienen el flujo de reserva»  
**Archivo:** [02-DATABASE-LOGICAL.puml](02-DATABASE-LOGICAL.puml) · [SVG](02-DATABASE-LOGICAL.svg) · [PNG](02-DATABASE-LOGICAL.png)

**Guion (20–30 s):** «El modelo lógico transforma esas relaciones en claves. UserRole y ProfessionalSpecialty resuelven relaciones muchos a muchos. Schedule referencia el par profesional–especialidad autorizado. Appointment guarda el paciente, profesional y slot elegidos; la autorreferencia conserva la cadena de reprogramaciones. La identidad documental es única por tipo y número juntos.»

**Señalar:** tablas puente, FK compuesta del horario, `slotId` en Appointment, clave documental compuesta, exclusión de solapamientos V5.

**Evidencia:** [V1 esquema inicial](../../../database/migrations/V1__initial_schema.sql) aprox. líneas 43–162; [V3 reprogramación](../../../database/migrations/V3__support_appointment_lifecycle.sql) aprox. líneas 7–22; [V5 integridad](../../../database/migrations/V5__domain_integrity_hardening.sql) aprox. líneas 74–78, 144–156 y 199–220.

## 03 — Modelo físico PostgreSQL V5

**Título sugerido:** «Esquema real: tipos, claves y protección de capacidad»  
**Archivo completo:** [03-DATABASE-PHYSICAL-V5.puml](03-DATABASE-PHYSICAL-V5.puml) · [SVG](03-DATABASE-PHYSICAL-V5.svg) · [PNG](03-DATABASE-PHYSICAL-V5.png)  
**Láminas para exposición:** [03A núcleo](03A-DATABASE-PHYSICAL-CORE.puml) · [SVG 03A](03A-DATABASE-PHYSICAL-CORE.svg) · [PNG 03A](03A-DATABASE-PHYSICAL-CORE.png); [03B seguridad y auxiliares](03B-DATABASE-PHYSICAL-SECURITY-AUX.puml) · [SVG 03B](03B-DATABASE-PHYSICAL-SECURITY-AUX.svg) · [PNG 03B](03B-DATABASE-PHYSICAL-SECURITY-AUX.png).

**Guion 03A (20–30 s):** «Esta es la estructura real de las diez tablas centrales. PostgreSQL valida documento, vinculación profesional–especialidad y duración de slot de 30 minutos. Las restricciones de exclusión impiden horarios y slots incompatibles. El índice parcial protege la ocupación de un slot por una cita vigente.»

**Guion 03B (20–30 s):** «Las otras siete tablas sostienen permisos, refresh tokens, auditoría, lista de espera, solicitudes prioritarias y configuración horaria. Su existencia en SQL no significa que todas tengan una interfaz funcional; el flujo de la demo usa sobre todo cuentas, horarios, slots y citas.»

**Señalar:** en 03A, `patients(document_type, document_number)`, FK de `schedules` a `professional_specialties`, restricciones `EXCLUDE`, `appointments.slot_id`; en 03B, `role_permissions`, `refresh_tokens`, `audit_logs` y `hospital_business_config`.

**Cómo leerlo:** `PK` = clave primaria; `FK` = clave foránea; `UQ` = unicidad individual; `NULL` = columna opcional. `varchar[n]` representa `varchar(n)` de PostgreSQL; esa notación evita que PlantUML interprete el tipo como método. `FK>tabla` en 03B apunta a una tabla dibujada en 03A. Las FK, tipos y nulabilidad proceden del esquema PostgreSQL V5; las notas de restricciones resumen SQL y no reemplazan su DDL. La edición de `.puml` no requiere regenerar desde la base de datos.

**Evidencia:** [V1 esquema inicial](../../../database/migrations/V1__initial_schema.sql) aprox. líneas 14–213; [V2 refresh tokens](../../../database/migrations/V2__create_refresh_tokens.sql) aprox. líneas 7–23; [V3 reprogramación](../../../database/migrations/V3__support_appointment_lifecycle.sql) aprox. líneas 7–22; [V4 seguro](../../../database/migrations/V4__patient_self_registration.sql) aprox. líneas 1–4; [V5 integridad](../../../database/migrations/V5__domain_integrity_hardening.sql) aprox. líneas 74–84, 119–220 y 397–603. La protección transaccional de reservas está en `capacity_reserve` (V5, aprox. líneas 440–456).

## 04 — MVC en el sistema real

**Título sugerido:** «Vista Next.js, controladores REST y modelo de negocio»  
**Archivo:** [04-MVC-ARCHITECTURE.puml](04-MVC-ARCHITECTURE.puml) · [SVG](04-MVC-ARCHITECTURE.svg) · [PNG](04-MVC-ARCHITECTURE.png)

**Guion (20–30 s):** «La vista está en Next.js y muestra registro, disponibilidad y citas. Sus Route Handlers llaman a la API Spring Boot. Los controladores reciben DTO y delegan las reglas a servicios; los repositorios obtienen entidades desde PostgreSQL. La respuesta vuelve como JSON y la vista muestra el estado real.»

**Señalar:** View → Route Handler → REST Controller → Service/DTO/Entity → Repository → PostgreSQL. El término MVC describe responsabilidades distribuidas entre **dos aplicaciones**, no un `View` de Spring MVC.

**Evidencia:** [vista de disponibilidad](../../../apps/frontend/app/patient/availability/page.tsx) aprox. líneas 16–105; [Route Handler de disponibilidad](../../../apps/frontend/app/api/patient/availability/route.ts) aprox. líneas 1–7; [AgendaController](../../../apps/backend/src/main/java/com/hospital/platform/agenda/controller/AgendaController.java) aprox. líneas 29–100; [AppointmentController](../../../apps/backend/src/main/java/com/hospital/platform/appointments/controller/AppointmentController.java) aprox. líneas 22–48; [AppointmentService](../../../apps/backend/src/main/java/com/hospital/platform/appointments/service/AppointmentService.java) aprox. líneas 37–93.

## 05 — DAO / Repository

**Título sugerido:** «Acceso a datos con Spring Data JPA»  
**Archivo:** [05-DAO-REPOSITORY-PATTERN.puml](05-DAO-REPOSITORY-PATTERN.puml) · [SVG](05-DAO-REPOSITORY-PATTERN.svg) · [PNG](05-DAO-REPOSITORY-PATTERN.png)

**Guion (20–30 s):** «Para Mis citas, el controlador llama al servicio. El servicio identifica al paciente autenticado y usa AppointmentRepository para consultar sus citas. Spring Data JPA implementa el repositorio y Hibernate emite SQL a PostgreSQL. El servicio mapea entidades a DTO antes de responder.»

**Señalar:** `AppointmentRepository extends JpaRepository`, método `findAllByPatientIdOrderByCreatedAtDesc`, retorno como `AppointmentResponseDTO`. La **reserva** utiliza además `CapacityGateway` con JDBC y la función PostgreSQL `capacity_reserve`; no debe presentarse como un `save` simple del repositorio.

**Evidencia:** [AppointmentController](../../../apps/backend/src/main/java/com/hospital/platform/appointments/controller/AppointmentController.java) aprox. líneas 44–48; [AppointmentService](../../../apps/backend/src/main/java/com/hospital/platform/appointments/service/AppointmentService.java) aprox. líneas 70–93; [AppointmentRepository](../../../apps/backend/src/main/java/com/hospital/platform/appointments/repository/AppointmentRepository.java) aprox. líneas 13–25; [PostgresCapacityGateway](../../../apps/backend/src/main/java/com/hospital/platform/agenda/contract/PostgresCapacityGateway.java) aprox. líneas 10–75; [V5 reserva](../../../database/migrations/V5__domain_integrity_hardening.sql) aprox. líneas 440–456.

## 06 — Conexión a base de datos

**Título sugerido:** «Del navegador a PostgreSQL y Flyway»  
**Archivo:** [06-DATABASE-CONNECTION-FLOW.puml](06-DATABASE-CONNECTION-FLOW.puml) · [SVG](06-DATABASE-CONNECTION-FLOW.svg) · [PNG](06-DATABASE-CONNECTION-FLOW.png)

**Guion (20–30 s):** «El navegador entra a Next.js por el puerto 3000. Su servidor llama a Spring Boot por el puerto 18080 del host. En Docker, el backend se conecta a `postgres:5432`; el host publica PostgreSQL en 55432 para inspección local. Spring Data JPA o el gateway JDBC ejecutan SQL. Al iniciar, Flyway aplica V1 a V5 y Hibernate valida el esquema.»

**Señalar:** tres puertos, `BACKEND_URL`, `DATABASE_URL`, Flyway V1–V5, `ddl-auto: validate`. Los valores de secretos no aparecen en los diagramas. `55432` es el puerto de la **configuración local de demo** (`POSTGRES_HOST_PORT`), no un puerto fijo de `docker-compose.yml`; por defecto el compose publica 5432.

**Evidencia:** [docker-compose.yml](../../../docker-compose.yml) aprox. líneas 1–18; [application.yml](../../../apps/backend/src/main/resources/application.yml) aprox. líneas 1–25; [backend.ts](../../../apps/frontend/lib/backend.ts) aprox. líneas 1–35; [.env.example](../../../apps/frontend/.env.example) línea 1; [guía de demo](../../../apps/frontend/DEMO-GUIDE.md) aprox. líneas 7–59; [migraciones Flyway](../../../database/migrations/) V1–V5.

## Orden recomendado y evidencias en la exposición

1. **01 conceptual:** explicar actores y cadena horario → slot → cita.
2. **02 lógico:** mostrar claves compuestas y relaciones de implementación.
3. **03A físico:** ampliar restricciones V5; mostrar 03B como segunda lámina o anexo. Mantener 03 completo como evidencia de las 17 tablas.
4. **04 MVC:** recorrer una solicitud real desde la vista hasta JSON.
5. **05 Repository:** abrir el método del repositorio y la llamada exacta del servicio.
6. **06 conexión:** enseñar `docker-compose.yml`, `application.yml` y, en pgAdmin, las tablas `appointments`, `availability_slots`, `patients` y `flyway_schema_history`.

Para demostrar persistencia sin exponer credenciales: seleccionar una cita desde la interfaz y consultar en pgAdmin su `appointment_status`, `patient_id`, `professional_id` y `slot_id`; consultar el mismo `availability_slots.id` y su `status`. Mostrar los nombres de columnas y UUID, no contraseñas, hashes ni tokens.
