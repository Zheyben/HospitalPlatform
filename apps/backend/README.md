# Hospital Platform Backend

Backend Spring Boot para Hospital Platform.

## Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL 16+
- Docker, para pruebas de integracion con Testcontainers

## Instalacion

Desde `apps/backend`:

```bash
./mvnw dependency:resolve
```

En Windows:

```powershell
.\mvnw.cmd dependency:resolve
```

## Variables de entorno

Crear las variables tomando como referencia `.env.example`:

```text
DATABASE_URL=
DATABASE_USERNAME=
DATABASE_PASSWORD=
JWT_SECRET=
JWT_EXPIRATION=
REFRESH_TOKEN_EXPIRATION=
SPRING_PROFILE=
SERVER_PORT=
```

No se deben versionar secretos reales.

## Ejecucion local

Perfil por defecto:

```powershell
.\mvnw.cmd spring-boot:run
```

Perfil explicito:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

La API utilizara versionado bajo `/api/v1` cuando se implementen endpoints funcionales.

## Comandos Maven

```powershell
.\mvnw.cmd clean compile
.\mvnw.cmd test
.\mvnw.cmd verify
.\mvnw.cmd package
```

`verify` ejecuta las pruebas de integracion `*IT` con PostgreSQL mediante Testcontainers.

## Demo de disponibilidad para PATIENT (B2)

`Schedule.dayOfWeek` usa la convencion SQL: `0=domingo` hasta `6=sabado`.
`SlotGenerationService.generate(scheduleId)` es una operacion explicita reutilizable; no hay
job ni endpoint para generarla. Produce intervalos de 30 minutos desde `startTime`, sin
exceder `endTime`, para los siguientes 14 dias UTC (desde manana). La insercion usa la
clave unica `(schedule_id, slot_date, start_time)` y no modifica slots existentes.

Para datos locales reproducibles, iniciar el backend con perfil `dev` y
`DEMO_SEED_ENABLED=true`. Por defecto el seed esta desactivado y nunca se ejecuta en
`test` o `prod`. Crea una especialidad, un profesional con cuenta deshabilitada,
la relacion profesional-especialidad, siete horarios semanales (09:00-12:00 UTC)
y slots futuros. Repetir el inicio agrega solo fechas nuevas y conserva los slots
ya reservados. No crea una contrasena conocida para el profesional de demo.

Con el PostgreSQL de `docker-compose.yml` iniciado desde la raiz del repositorio:

```powershell
docker build -f apps/backend/Dockerfile -t hospital-platform-backend:b2 .
docker run --rm --name hospital-platform-b2-api --network hospital-platform_default `
  --env-file .env -e DATABASE_URL=jdbc:postgresql://postgres:5432/hospital_platform `
  -e SPRING_PROFILE=dev -e SERVER_PORT=8080 -e DEMO_SEED_ENABLED=true `
  -p 127.0.0.1:18080:8080 hospital-platform-backend:b2
```

Un PATIENT autenticado puede consultar `GET /api/v1/availability`. El servidor
solo devuelve slots futuros `AVAILABLE` de horarios, profesionales y especialidades
activos; el filtro `status` del cliente no amplia esos resultados. Cada fila incluye
`slotId` (alias de `id` por compatibilidad), fecha, horas, profesional y especialidad.
La reserva usa `POST /api/v1/appointments` con `slotId` y `reason`; el paciente
se obtiene del token. Un segundo intento sobre el mismo slot devuelve 409.

## Migraciones

La migracion oficial vive en:

```text
database/migrations/V1__initial_schema.sql
```

El `pom.xml` la incluye como recurso de build en `classpath:db/migration` para que Flyway la ejecute sin duplicarla ni modificarla.

## Docker

Construir desde la raiz del repositorio para incluir la migracion oficial:

```powershell
docker build -f apps/backend/Dockerfile -t hospital-platform-backend .
```

Ejecutar:

```powershell
docker run --rm -p 8080:8080 --env-file apps/backend/.env hospital-platform-backend
```

## Estructura

```text
src/main/java/com/hospital/platform
|-- common
|-- security
|-- auth
|-- users
|-- patients
|-- professionals
|-- catalogs
|-- agenda
|-- appointments
|-- waitlist
|-- priority
|-- notifications
|-- dashboard
`-- audit
```

Cada modulo funcional se organiza internamente por feature con `controller`, `service`, `repository`, `entity`, `dto`, `mapper` y `exception`.
