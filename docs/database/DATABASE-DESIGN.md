# DATABASE DESIGN & CONSTRAINTS

## Estado

Versión 1.1 - Diseño actualizado después de auditoría.

**Control de vigencia E.3.2 (30/09/2026):** este diseño contiene objetivos y ejemplos anteriores. El esquema físico actual se determina por V1, `V2__create_refresh_tokens.sql` y `V3__support_appointment_lifecycle.sql`. La presencia de tablas `specialties`, `professional_specialties`, `waitlist_entries` o `priority_requests` no acredita módulos Java funcionales. Las decisiones DEC-005/008/010/022/023 permanecen abiertas en su alcance. No usar nombres de migración o restricciones de ejemplo de este documento por encima de los archivos SQL reales.

------------------------------------------------------------------------

# 1. Objetivo

Definir el diseño de persistencia de Hospital Platform antes de la
implementación física en PostgreSQL.

Incluye:

-   modelo relacional;
-   entidades;
-   relaciones;
-   restricciones;
-   índices;
-   estrategia de identificación;
-   eliminación lógica;
-   auditoría.

------------------------------------------------------------------------

# 2. Stack de persistencia

## Base de datos

PostgreSQL

## ORM

JPA / Hibernate

## Migraciones

Flyway

------------------------------------------------------------------------

# 3. Convenciones

## Tablas

Usan:

``` text
snake_case
```

Ejemplo de nombre de tabla existente en V1:

``` text
availability_slots
```

## Identificadores

Todas las entidades utilizan:

``` text
UUID
```

como clave primaria.

La generación de UUID será responsabilidad de la aplicación backend.

## Fechas

Entidades principales:

``` text
created_at
updated_at
```

Entidades con eliminación lógica:

``` text
deleted_at
```

------------------------------------------------------------------------

# 4. Módulos contemplados en el diseño (no todos implementados)

-   auth
-   users
-   patients
-   professionals
-   catalogs
-   agenda
-   appointments
-   waitlist (tabla física `waitlist_entries`; sin módulo funcional)
-   priority (tabla física `priority_requests`; sin módulo funcional)
-   notifications
-   dashboard
-   audit

------------------------------------------------------------------------

# 5. Entidades principales

## Seguridad

-   users
-   roles
-   permissions
-   user_roles
-   role_permissions

## Personas

-   patients
-   professionals
-   specialties
-   professional_specialties

## Agenda

-   schedules
-   availability_slots

## Atención ambulatoria

-   appointments
-   waitlist_entries
-   priority_requests

## Auditoría

-   audit_logs

------------------------------------------------------------------------

# 6. Relación profesional-especialidad

Un profesional puede tener múltiples especialidades.

Modelo:

``` text
professionals

N ---- M

specialties
```

Tabla intermedia:

``` text
professional_specialties
```

Campos:

-   professional_id
-   specialty_id

------------------------------------------------------------------------

# 7. Reglas de integridad

## Usuarios

-   email único.
-   username único.

## Pacientes

-   documento único.

## Profesionales

-   licencia profesional única.

## Agenda

No permitir slots duplicados:

``` text
schedule_id
slot_date
start_time
```

deben ser únicos.

## Citas

Un slot no puede tener dos reservas activas.

------------------------------------------------------------------------

# 8. Citas

La entidad appointments debe soportar:

-   creación;
-   confirmación;
-   cancelación;
-   reprogramación;
-   finalización.

Campos adicionales:

-   cancelled_at
-   cancelled_by

para trazabilidad de cancelaciones.

------------------------------------------------------------------------

# 9. Estrategia Soft Delete

Se utiliza eliminación lógica.

Aplicable a:

-   users;
-   professionals;
-   specialties.

Campo:

``` text
deleted_at
```

No se eliminan físicamente:

-   audit_logs;
-   citas históricas.

------------------------------------------------------------------------

# 10. Estrategia de Auditoría

La auditoría utiliza:

``` text
audit_logs
```

con JSONB.

Modelo:

-   id
-   user_id
-   action
-   entity_name
-   entity_id
-   old_values JSONB
-   new_values JSONB
-   ip_address
-   user_agent
-   created_at

Permite:

-   trazabilidad;
-   historial;
-   análisis posterior.

------------------------------------------------------------------------

# 11. Índices iniciales

## appointments

-   patient_id
-   professional_id
-   appointment_status (índice V1)
-   slot_id (índice único parcial V3 para `SCHEDULED` y `CONFIRMED`)

## availability_slots

-   slot_date
-   status

## audit_logs

-   entity_name
-   entity_id
-   created_at

------------------------------------------------------------------------

# 12. Migraciones

Ubicación:

``` text
database/migrations/
```

Ejemplo:

``` text
V1__initial_schema.sql
V2__create_refresh_tokens.sql
V3__support_appointment_lifecycle.sql
```
