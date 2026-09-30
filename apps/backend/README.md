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
