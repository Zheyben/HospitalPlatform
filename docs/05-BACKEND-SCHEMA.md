# 05 - BACKEND SCHEMA v1.4.1 FINAL

## Estado del documento

**Versión:** 1.4.1 FINAL (Refinada)

**Control de vigencia E.3.2 (30/09/2026):** este modelo lógico previo usa nombres/tablas de ejemplo que **no son el esquema físico vigente**. Las migraciones `database/migrations/V1__initial_schema.sql`, `V2__create_refresh_tokens.sql` y `V3__support_appointment_lifecycle.sql` gobiernan tablas, columnas y restricciones. ADR-007 y el baseline no incluyen `NO_SHOW`/`NO_ASISTIO` como estado actual ni evaluación clínica de triaje. DEC-002 limita roles de negocio a ADMIN, PATIENT, RECEPTIONIST y PROFESSIONAL. DEC-006 asigna definiciones a Catalogs y asociación N:M a Professionals solo en el diseño; las tablas no crean CRUD Java. Este documento conserva antecedentes, no sustituye SQL real.

**Producto:** Plataforma web y móvil modular para la gestión de atención
ambulatoria, citas médicas y trazabilidad del flujo asistencial.

**Documentos relacionados:**

-   PRD v1.4 FINAL
-   TRD v1.2 FINAL ajustado
-   APP FLOW v1.2.1 FINAL
-   UI/UX DESIGN BRIEF v1.2.1 FINAL
-   API SPECIFICATION v1.2.2 FINAL

------------------------------------------------------------------------

# Ajustes aplicados en esta versión

-   Incorporación explícita de tablas catálogo de estados.
-   Refinamiento de restricción de citas activas.
-   Incorporación de auditoría de eventos y notificaciones.
-   Mejora de definición del modelo relacional.

------------------------------------------------------------------------

# 1. Regla de usuarios y roles

Un usuario podrá tener múltiples roles mediante la entidad UsuarioRol.

Sin embargo, deberá existir un rol principal obligatorio para definir su
comportamiento inicial dentro del sistema.

Ejemplo:

``` text
Usuario:

Carlos Pérez

Rol principal:
PROFESIONAL

Roles adicionales:
ADMINISTRADOR
```

------------------------------------------------------------------------

# 2. Modelo de autorización

## Rol

Campos:

``` text
id_rol
nombre
descripcion
```

Roles del modelo histórico (no equivalen a permisos vigentes; aplicar DEC-002):

``` text
PACIENTE

ADMINISTRADOR

RECEPCIONISTA

TRIAJE

PROFESIONAL
```

------------------------------------------------------------------------

## Permiso

Formato:

``` text
RECURSO_ACCION
```

Ejemplos:

``` text
USERS_MANAGE

SPECIALTIES_MANAGE

APPOINTMENTS_CREATE

PATIENT_CHECKIN

ATTENTION_REGISTER

DASHBOARD_VIEW
```

------------------------------------------------------------------------

## RolPermiso

Campos:

``` text
id_rol_permiso
id_rol
id_permiso
```

Relación:

``` text
Rol N ---- N Permiso
```

------------------------------------------------------------------------

# 3. Especialidades y contenido público

## Especialidad

Campos:

``` text
id_especialidad
nombre
codigo
estado
```

------------------------------------------------------------------------

## EspecialidadContenido

Campos:

``` text
id_contenido
id_especialidad
titulo
descripcion_publica
imagen_url
orden_visualizacion
estado
```

------------------------------------------------------------------------

# 4. Dashboard

## DashboardMetric

Campos:

``` text
id_metric
nombre
tipo
descripcion
```

------------------------------------------------------------------------

## DashboardConfig

Campos:

``` text
id_config
nombre
filtros
tipo_visualizacion
estado
```

Las métricas podrán calcularse mediante consultas agregadas.

Ejemplo:

``` text
COUNT(citas)

GROUP BY especialidad
```

------------------------------------------------------------------------

# 5. Restricciones generales

## Reserva única

Una disponibilidad solo puede tener una cita activa.

El índice de V3 limita citas activas por slot. La cancelación y la
reprogramación pueden liberar el slot mediante la lógica actual;
completar la atención no lo libera. La posible reutilización posterior
no se deduce solo del índice.

Restricción física vigente en V3 (no usar `NO_ASISTIO` del ejemplo anterior):

``` sql
CREATE UNIQUE INDEX uq_appointments_active_slot
ON appointments(slot_id)
WHERE appointment_status IN ('SCHEDULED', 'CONFIRMED');
```

------------------------------------------------------------------------

## Separación de estados

EstadoCita y EstadoAtencion representan conceptos diferentes.

Ejemplo:

``` text
Cita:
CONFIRMADA

Atención:
EN_ESPERA
```

------------------------------------------------------------------------

## Especialidades

**REGLA PROPUESTA, NO IMPLEMENTADA:** el backend actual no consulta el estado
activo de Specialty al reservar; la política de elegibilidad queda pendiente.

------------------------------------------------------------------------

# 6. Modelo de dominio histórico (no equivalente a entidades vigentes)

``` text
Usuario

├── Paciente
└── Profesional


Usuario

└── UsuarioRol

UsuarioRol

└── Rol

Rol

└── RolPermiso

RolPermiso

└── Permiso


Profesional

├── ProfesionalEspecialidad
├── HorarioAtencion
└── Agenda


Especialidad

└── EspecialidadContenido


Agenda

└── Disponibilidad


Paciente

└── Cita


Cita

├── HistorialCita
├── EstadoCita
├── EstadoAtencion
├── RegistroRecepcion
├── EvaluacionTriaje
└── AtencionProfesional


Sistema

├── Notificacion
└── AuditoriaEvento
```

------------------------------------------------------------------------

# 7. Validaciones principales

## Reserva única

Una disponibilidad no puede tener múltiples citas activas.

------------------------------------------------------------------------

## Seguridad

Lista de controles objetivo del diseño anterior; la autorización actual
se verifica en los controllers y servicios. No existe aplicación universal
de permiso granular ni de un rol principal obligatorio para cada operación.

El diseño anterior enumeraba:

-   identidad;
-   rol principal;
-   permisos;
-   reglas de negocio.

------------------------------------------------------------------------

# 8. Database Design

## 8.1 Convenciones PostgreSQL

Las tablas utilizarán:

-   nombres en plural;
-   snake_case;
-   claves primarias numéricas;
-   timestamps de auditoría.

Campos estándar:

``` text
created_at

updated_at
```

Entidades críticas:

``` text
created_by

updated_by
```

------------------------------------------------------------------------

# 8.2 Modelo relacional principal

Tablas:

``` text
usuarios

roles

permisos

usuario_roles

rol_permisos

pacientes

profesionales

especialidades

especialidad_contenidos

agendas

disponibilidades

citas

historial_citas

registro_recepcion

evaluaciones_triaje

atenciones_profesionales

notificaciones

auditoria_eventos

estado_cita

estado_atencion
```

------------------------------------------------------------------------

# 8.3 Claves únicas

## Usuarios

``` sql
correo UNIQUE NOT NULL

numero_documento UNIQUE NOT NULL
```

------------------------------------------------------------------------

## Roles

``` sql
nombre UNIQUE NOT NULL
```

------------------------------------------------------------------------

## Especialidades

``` sql
codigo UNIQUE NOT NULL
```

------------------------------------------------------------------------

# 8.4 Índices

## Usuarios

``` sql
INDEX(correo)

INDEX(numero_documento)
```

------------------------------------------------------------------------

## Citas

``` sql
INDEX(id_paciente)

INDEX(id_profesional)

INDEX(fecha)

INDEX(estado)
```

------------------------------------------------------------------------

## Dashboard

``` sql
INDEX(fecha_creacion)

INDEX(id_especialidad)
```

------------------------------------------------------------------------

# 8.5 Reglas de eliminación

## Usuarios

No eliminar físicamente.

Usar:

``` text
estado = INACTIVO
```

------------------------------------------------------------------------

## Especialidades

No eliminar si tienen historial.

Usar:

``` text
estado = INACTIVA
```

------------------------------------------------------------------------

## Citas

No eliminar físicamente.

Conservar:

``` text
HistorialCita
```

------------------------------------------------------------------------

# 8.6 Estados del sistema

Los estados deben mantenerse como catálogos.

Tablas:

``` text
estado_cita

estado_atencion
```

Esto permite evolución futura sin modificar estructuras principales.

------------------------------------------------------------------------

# 8.7 Auditoría de cambios

Las entidades críticas deben conservar trazabilidad:

``` text
usuarios

citas

permisos

atenciones_profesionales

auditoria_eventos
```

Campos:

``` text
created_at

updated_at

created_by

updated_by
```

------------------------------------------------------------------------

# Resultado

El documento queda como:

05-BACKEND-SCHEMA v1.4.1 FINAL

Preparado para:

06-API-SPECIFICATION v1.2.2 FINAL

07-IMPLEMENTATION-PLAN
