# BACKEND ARCHITECTURE

## Hospital Platform

## Estado

Versión 1.1 - Diseño actualizado después de auditoría.

**Control de vigencia E.3.2 (30/09/2026):** el monolito modular Java/Spring Boot ya existe; el checklist de creación al final es histórico. Los controllers actuales cubren Auth, Users, Patients, Professionals, Agenda y Appointments; `catalogs`, `waitlist`, `priority`, `notifications` y `dashboard` contienen paquetes marcador sin servicios/controladores funcionales. Los límites del dominio vigente se leen con `DOMAIN-BASELINE.md` y `DOMAIN-DECISION-REGISTER.md`; una carpeta o tabla no acredita un módulo implementado.

------------------------------------------------------------------------

# 1. Objetivo

Definir la arquitectura interna del backend de Hospital Platform antes
de iniciar la implementación.

Este documento establece:

-   estructura del proyecto;
-   organización de módulos;
-   responsabilidades;
-   patrones utilizados;
-   reglas de desarrollo;
-   comunicación entre componentes.

------------------------------------------------------------------------

# 2. Stack tecnológico

## Lenguaje

Java 21

## Framework

Spring Boot 3.x

## Seguridad

-   Spring Security
-   JWT
-   Refresh Token

## Persistencia

-   Spring Data JPA
-   Hibernate
-   PostgreSQL
-   Flyway

## Validación

Jakarta Validation

## Documentación API

OpenAPI / Swagger

## Testing

-   JUnit 5
-   Mockito
-   Testcontainers

------------------------------------------------------------------------

# 3. Estilo arquitectónico

El backend utiliza:

``` text
Monolito Modular
```

con organización:

``` text
Package by Feature
```

Cada módulo representa un dominio funcional del sistema.

------------------------------------------------------------------------

# 4. Principios arquitectónicos

## Separación por dominio

Cada módulo contiene su propia lógica.

Ejemplo:

``` text
appointments/

├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
└── exception
```

------------------------------------------------------------------------

## Bajo acoplamiento

Los módulos no deben acceder directamente a tablas o clases internas de
otros módulos.

La comunicación debe realizarse mediante:

-   servicios públicos;
-   interfaces;
-   contratos definidos.

Ejemplo correcto:

``` text
appointments

↓

PatientService Interface
```

Ejemplo incorrecto:

``` text
appointments

↓

patients.entity.Patient
```

------------------------------------------------------------------------

## DTO como frontera API

Las entidades JPA no deben exponerse directamente.

Flujo:

``` text
Request DTO

↓

Service

↓

Entity

↓

Mapper

↓

Response DTO
```

------------------------------------------------------------------------

# 5. Estructura del backend

Ubicación:

``` text
apps/backend/
```

Estructura:

``` text
backend/

├── pom.xml

├── src/
│
│   ├── main/
│   │
│   │   ├── java/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       └── application-prod.yml
│   │
│   └── test/

├── Dockerfile

├── README.md

└── .env.example
```

------------------------------------------------------------------------

# 6. Organización de paquetes

Paquete raíz:

``` text
com.hospital.platform
```

Estructura:

``` text
com.hospital.platform

├── common
├── security
├── auth
├── users
├── patients
├── professionals
├── catalogs
├── agenda
├── appointments
├── waitlist
├── priority
├── notifications
├── dashboard
└── audit
```

------------------------------------------------------------------------

# 7. Módulo common

Contiene componentes compartidos no pertenecientes a un dominio
específico.

Estructura:

``` text
common/

├── exception
├── response
├── constants
├── utils
└── validation
```

No debe utilizarse como contenedor genérico de lógica de negocio.

------------------------------------------------------------------------

# 8. Responsabilidad de módulos

## auth

Responsable de:

-   login;
-   registro;
-   autenticación;
-   generación de tokens.

## security

Responsable de:

-   JWT;
-   filtros;
-   autorización;
-   configuración Spring Security.

## users

Responsable de:

-   usuarios;
-   perfiles;
-   gestión administrativa.

## patients

Responsable de:

-   información del paciente;
-   datos personales.

## professionals

Responsable de:

-   profesionales;
-   relaciones con especialidades.

## catalogs

Responsable de:

-   especialidades;
-   catálogos generales.

## agenda

Responsable de:

-   horarios;
-   disponibilidad;
-   slots.

## appointments

Responsable de:

-   creación de citas;
-   estados;
-   cancelación;
-   reprogramación.

## waitlist

Responsable de:

-   lista de espera;
-   seguimiento.

## priority

Responsable de:

-   solicitudes de prioridad ambulatoria.

## notifications

Responsable de:

-   avisos;
-   comunicaciones.

## dashboard

Responsable de:

-   métricas;
-   información operativa.

## audit

Responsable de:

-   registro de acciones;
-   trazabilidad.

------------------------------------------------------------------------

# 9. Estructura interna de módulos

Cada módulo utiliza:

``` text
module/

├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
└── exception
```

------------------------------------------------------------------------

# 10. Capas internas

## Controller

Responsable de:

-   recibir solicitudes HTTP;
-   validar entrada;
-   retornar respuestas.

No contiene lógica de negocio.

------------------------------------------------------------------------

## Service

Responsable de:

-   reglas de negocio;
-   coordinación entre componentes;
-   transacciones.

------------------------------------------------------------------------

## Repository

Responsable de:

-   acceso a datos;
-   consultas JPA.

------------------------------------------------------------------------

## Entity

Representación persistente.

No debe exponerse directamente.

------------------------------------------------------------------------

## DTO

Objetos de entrada y salida de API.

------------------------------------------------------------------------

## Mapper

Responsable de transformar:

``` text
Entity ↔ DTO
```

------------------------------------------------------------------------

# 11. Configuración por ambientes

Archivos:

``` text
application.yml

application-dev.yml

application-test.yml

application-prod.yml
```

------------------------------------------------------------------------

# 12. Migraciones

La estructura de base de datos será controlada por:

``` text
Flyway
```

Regla:

No utilizar:

``` yaml
ddl-auto: create
```

Se utilizará:

``` yaml
ddl-auto: validate
```

------------------------------------------------------------------------

# 13. Seguridad y autorización

Principios:

-   RBAC;
-   JWT;
-   Refresh Token;
-   protección de endpoints;
-   auditoría de acciones críticas.

Además del rol, las operaciones deben validar propiedad del recurso.

Ejemplo:

Un usuario con rol PATIENT solo puede gestionar sus propias citas.

------------------------------------------------------------------------

# 14. Auditoría

Las operaciones críticas deben generar registros:

-   creación de usuarios;
-   modificación de citas;
-   cancelaciones;
-   cambios de prioridad.

------------------------------------------------------------------------

# 15. Testing

Capas:

## Unit Testing

Servicios y reglas de negocio.

## Integration Testing

Persistencia y componentes.

## API Testing

Endpoints REST.

## End-to-End

Flujos completos.

------------------------------------------------------------------------

# 16. Reglas de desarrollo

-   Mantener separación por módulos.
-   No mezclar responsabilidades.
-   No exponer entidades.
-   Documentar decisiones importantes.
-   Mantener pruebas junto al código.
-   Validar antes de fusionar cambios.

------------------------------------------------------------------------

# 17. Próximos pasos

Después de aprobar esta arquitectura:

1.  Crear proyecto Spring Boot.
2.  Configurar Maven.
3.  Crear estructura de paquetes.
4.  Configurar seguridad base.
5.  Conectar PostgreSQL.
6.  Ejecutar primera migración Flyway.
