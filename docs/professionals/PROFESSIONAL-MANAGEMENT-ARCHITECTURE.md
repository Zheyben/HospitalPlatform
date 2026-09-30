# PROFESSIONAL MANAGEMENT ARCHITECTURE

## Hospital Platform

## Estado

Versión 1.1 - Diseño arquitectónico actualizado después de auditoría.

**Control de vigencia E.3.2 (30/09/2026):** este texto conserva partes del diseño previo a la implementación. Actualmente existen `ProfessionalController`, `ProfessionalService`, `ProfessionalRepository`, DTOs y `ProfessionalLookupService`. ADMIN puede crear, listar, detallar y actualizar los campos permitidos del profesional; no hay API de gestión de la asociación N:M ni ciclo controlado User–Professional (DEC-005 `OPEN`). DEC-006 asigna Catalogs como propietario conceptual de definiciones Specialty y Professionals como propietario conceptual de las asociaciones; la tabla V1 no es funcionalidad Java. Agenda y Appointments ya consumen contratos de Professionals. Las secciones que describen integración o tests como futuros se interpretan conforme a estas evidencias.

------------------------------------------------------------------------

# 1. Objetivo

Definir la arquitectura inicial del módulo `professionals` dentro de
Hospital Platform.

El módulo representa a las personas del área asistencial que participan
en procesos de atención clínica de pacientes.

Su objetivo es separar la identidad profesional clínica de:

-   usuarios del sistema;
-   autenticación;
-   permisos;
-   gestión de acceso.

------------------------------------------------------------------------

# 2. Definición del Dominio

Un Professional representa una persona profesional autorizada para
participar en la atención de pacientes.

Ejemplos:

-   médicos especialistas;
-   enfermeros;
-   otros profesionales clínicos.

Ejemplos de áreas médicas:

-   traumatología;
-   oftalmología;
-   cardiología;
-   otras especialidades clínicas.

El concepto `Professional` representa el dominio asistencial, no la
identidad de acceso al sistema.

------------------------------------------------------------------------

# 3. Separación con Users Module

El módulo `users` administra identidad de acceso.

Responsabilidades de users:

-   autenticación;
-   credenciales;
-   roles;
-   permisos;
-   seguridad.

El módulo `professionals` administra identidad profesional clínica.

Responsabilidades de professionals:

-   información profesional;
-   especialidad;
-   estado profesional;
-   relación futura con atención clínica.

Relación:

``` text
users

  |
  | identidad de acceso
  |

professionals

  |
  | identidad profesional clínica
  |

appointments

  |
  | atención del paciente
```

------------------------------------------------------------------------

# 4. Alcance del Módulo

## Incluye

-   información profesional;
-   identificación profesional;
-   asociación con especialidades solo como diseño conceptual pendiente (DEC-006);
-   estado activo/inactivo;
-   integración actual de consulta con Agenda mediante contrato público;
-   integración actual de consulta con Appointments mediante contrato público.

## No incluye

-   autenticación;
-   passwords;
-   JWT;
-   permisos;
-   roles;
-   reservas de citas;
-   calendario.

------------------------------------------------------------------------

# 5. Estado Actual

La fase 5.6 fue el diseño inicial. En el corte actual existen entidad,
repository, service, controller, DTOs y `ProfessionalLookupService` en Java.
V1 contiene:

-   tabla `professionals`;
-   columna `license_number`;
-   columna `user_id`;
-   columna `deleted_at`;
-   columnas de auditoría `created_at` y `updated_at`.

La API ADMIN actual permite alta, listado, detalle y actualización básica.
La desactivación existe como comportamiento de service pero no tiene mapping
HTTP. La asociación Professional–Specialty carece de flujo Java/API; la
columna y la tabla SQL son soporte físico. DEC-005 y la validación de specialty
activa/asignada permanecen pendientes.

------------------------------------------------------------------------

# 6. Relación con Contratos Públicos

El módulo debe exponer capacidades mediante contratos públicos.

Contrato definido:

``` text
professionals.contract

ProfessionalLookupService
```

Los consumidores externos no deben acceder directamente a:

-   Professional entity;
-   ProfessionalRepository;
-   detalles internos del módulo.

Ejemplo correcto:

``` text
appointments

      ↓

ProfessionalLookupService

      ↓

professionals
```

`ProfessionalLookupService` representa un contrato público.

La implementación actual pertenece al módulo `professionals`; los consumidores
no deben acceder a su repository o entidad JPA.

No deben crearse implementaciones falsas o temporales para satisfacer
consumidores externos.

------------------------------------------------------------------------

# 7. ProfessionalLookupService

Responsabilidad:

Permitir consultas mínimas sobre profesionales desde otros módulos.

Casos de uso:

-   validar existencia;
-   validar estado activo;
-   validar vínculo del profesional activo con el usuario autenticado.

No debe exponer:

-   información interna completa;
-   datos sensibles;
-   lógica propia del dominio.

Métodos implementados en `ProfessionalLookupService`:

``` java
existsActiveProfessional(UUID professionalId)
isActiveProfessionalLinkedToUser(UUID professionalId, UUID userId)
```

------------------------------------------------------------------------

# 8. Modelo Futuro del Dominio

La entidad Professional puede evolucionar para representar diferentes
perfiles clínicos.

Ejemplo conceptual:

``` text
Professional

- id
- identification data
- professional–specialty association (N:M conceptual; no Java/API actual)
- status
- audit information
```

Esta estructura no representa una entidad definitiva.

La modelación de especialidades puede evolucionar en futuras decisiones
de dominio.

Ejemplo futuro:

``` text
Professional

        |
        |
ProfessionalSpecialty

        |
        |
SpecialtyCatalog
```

No se implementa en esta fase.

------------------------------------------------------------------------

# 9. Estado Profesional

El módulo debe seguir la estrategia general de soft delete definida por
arquitectura.

La persistencia actual utiliza V1 y debe conservar estas reglas:

-   utilizar `deleted_at`;
-   evitar eliminación física;
-   mantener trazabilidad histórica.

El estado activo debe derivarse del ciclo de vida del registro.

Ejemplo:

``` text
active = deleted_at == null
```

No crear múltiples fuentes de verdad.

------------------------------------------------------------------------

# 10. Integración actual con Appointments

Appointments valida profesionales mediante el contrato público actual.

Flujo actual:

``` text
appointments

      ↓

ProfessionalLookupService

      ↓

professionals
```

Appointments no debe conocer:

-   estructura interna de professionals;
-   base de datos;
-   entidades JPA.

------------------------------------------------------------------------

# 11. Integración actual con Agenda

Agenda consume información profesional mínima para:

-   asignación de disponibilidad;
-   planificación de atención;
-   relación profesional-slot.

La lógica de agenda debe permanecer dentro del módulo correspondiente.

Agenda consume información profesional, pero no administra
profesionales.

Ejemplo correcto:

``` text
agenda

      ↓

ProfessionalLookupService

      ↓

professionals
```

No permitido:

``` text
agenda

      ↓

crear/modificar profesionales
```

------------------------------------------------------------------------

# 12. Seguridad

El módulo professionals no administra autenticación.

No debe almacenar:

-   passwords;
-   tokens;
-   credenciales.

La seguridad pertenece al módulo users.

------------------------------------------------------------------------

# 13. Testing actual y pendiente

`ProfessionalServiceTest`, `ProfessionalControllerAuthorizationTest` y
`DatabaseProfessionalLookupServiceTest` aportan evidencia de operaciones
actuales. La cobertura de asociación N:M, ciclo DEC-005 y validación ampliada
de specialty aún no existe. Una evolución futura deberá validar:

-   creación de profesionales;
-   consulta de profesionales;
-   estados activos/inactivos;
-   integración mediante contratos.

Los tests de consumidores deben utilizar contratos públicos, no clases
internas.

------------------------------------------------------------------------

# 14. Evolución Futura

Posibles capacidades, sujetas a decisiones y ownership vigentes:

-   asociación N:M administrada por Professionals; definiciones a cargo de Catalogs (DEC-006);
-   asociaciones con centros médicos;
-   disponibilidad profesional;
-   certificaciones;
-   historial profesional.

Estas capacidades deben agregarse según necesidades reales del dominio.

------------------------------------------------------------------------

# Historial de cambios

## v1.1

Cambios aplicados después de auditoría:

-   aclarada la diferencia entre contrato público e implementación real;
-   aclarado que ProfessionalLookupService no debe tener
    implementaciones falsas;
-   flexibilizado el modelo de especialidades;
-   aclarado que Agenda consume profesionales pero no los administra.
