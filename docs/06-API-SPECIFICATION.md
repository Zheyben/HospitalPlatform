# 06 - API SPECIFICATION v1.3.1 FINAL

## Estado del documento

**Versión:** 1.3.1 FINAL (FASE 5.11.2)

**Producto:** Plataforma web y móvil modular para la gestión de atención
ambulatoria, citas médicas y trazabilidad del flujo asistencial.

**Documentos relacionados:**

-   PRD v1.4 FINAL
-   TRD v1.2 FINAL ajustado
-   BACKEND SCHEMA v1.3.1 FINAL

**Control de vigencia E.3.2 (30/09/2026):** este archivo conserva una propuesta histórica de API. Según DEC-019, el contrato funcional actual lo determinan los controllers y DTOs Java; no existe un envelope universal ni paginación universal. Las rutas de registro/autoconsulta en `auth`, mantenimiento de especialidades, dashboard, administración HTTP de permisos y autoupdate de paciente que aparecen abajo **NO ESTÁN IMPLEMENTADAS**. Los ejemplos de respuesta y permisos de esas secciones no son contratos ejecutables. Para Agenda rigen C.1–C.4 y D.1–D.4: CU-D1/D4 son parciales; CU-D2/D3/D6 son conceptuales; CU-D5/D7/D8 son implementados en su alcance delimitado. Ninguna decisión `OPEN` queda cerrada por esta especificación.

------------------------------------------------------------------------

# 1. Objetivo

Este documento define la especificación técnica de la API REST del
sistema.

Incluye:

-   endpoints;
-   métodos HTTP;
-   autenticación;
-   autorización;
-   permisos;
-   filtros;
-   paginación;
-   DTOs;
-   respuestas;
-   errores de negocio.

------------------------------------------------------------------------

# 2. Arquitectura API

Tipo:

``` text
REST API
```

Formato:

``` text
JSON
```

Comunicación:

``` text
HTTPS
```

Autenticación:

``` text
JWT Bearer Token
```

Base URL:

``` text
/api/v1
```

------------------------------------------------------------------------

# 3. Respuestas estándar

Los JSON siguientes son ejemplos de diseño anteriores. **No representan un envelope común vigente**; el comportamiento de cada controller/DTO Java es la fuente funcional actual (DEC-019).

## Success

``` json
{
 "success": true,
 "message": "Operación realizada correctamente",
 "data": {}
}
```

## Error

``` json
{
 "success": false,
 "message": "Error de operación",
 "errorCode": "ERROR_CODE",
 "timestamp": ""
}
```

------------------------------------------------------------------------

# 4. Paginación

Durante Appointment Foundation, la consulta de citas devuelve una lista JSON
simple de `AppointmentResponseDTO`:

``` http
GET /api/v1/appointments
```

Respuesta:

``` json
[]
```

Este endpoint no implementa parámetros ni metadatos de paginación en la fase
actual.

------------------------------------------------------------------------

# 5. Matriz de autorización

La matriz siguiente distingue autorizaciones vigentes de operaciones históricas sin endpoint. Los `@PreAuthorize` de los controllers y las verificaciones de ownership gobiernan el acceso actual; no hay autorización HTTP operativa por `SPECIALTIES_MANAGE` ni `DASHBOARD_VIEW`.

  Endpoint                  Rol             Control / estado
  ------------------------- --------------- ---------------------
  POST /appointments        PATIENT / ADMIN / RECEPTIONIST  Rol y ownership actuales
  POST /appointments/{id}/confirm PATIENT / ADMIN / RECEPTIONIST  Lifecycle según ownership
  POST /appointments/{id}/cancel PATIENT / ADMIN  Lifecycle según ownership; RECEPTIONIST recibe 403
  POST /appointments/{id}/reschedule PATIENT / ADMIN  Lifecycle según ownership; RECEPTIONIST recibe 403
  POST /appointments/{id}/check-in RECEPTIONIST Appointment Operations
  POST /appointments/{id}/waiting RECEPTIONIST Appointment Operations
  POST /appointments/{id}/start-attention PROFESSIONAL Own professional appointment
  POST /appointments/{id}/complete PROFESSIONAL Own professional appointment
  POST /specialties         Ninguno         NO IMPLEMENTADO; `SPECIALTIES_MANAGE` histórico
  GET /dashboard/metrics    Ninguno         NO IMPLEMENTADO; `DASHBOARD_VIEW` histórico

------------------------------------------------------------------------

# 6. Autenticación

## Login

``` http
POST /api/v1/auth/login
```

## Registro paciente

**NO IMPLEMENTADO:** no existe `POST /api/v1/auth/register`. La creación actual de usuario y paciente es administrativa; DEC-017 sigue `OPEN`.

``` http
POST /api/v1/auth/register
```

## Refresh token

``` http
POST /api/v1/auth/refresh
```

## Logout

``` http
POST /api/v1/auth/logout
```

## Usuario actual

**RUTA HISTÓRICA:** no existe `GET /api/v1/auth/me`. Existe `GET /api/v1/users/me` para el usuario autenticado; este ejemplo se conserva como antecedente.

``` http
GET /api/v1/auth/me
```

Response incluye:

-   usuario;
-   rol principal;
-   roles adicionales;
-   permisos.

------------------------------------------------------------------------

# 7. DTO Contracts

Los payloads de esta sección son ejemplos históricos. `AppointmentResponseDTO` y `PatientResponseDTO` existen en Java, pero sus campos reales prevalecen sobre estos ejemplos. `DashboardMetricResponse` no tiene DTO Java actual.

## AppointmentResponse

``` json
{
 "id":"77777777-7777-7777-7777-777777777777",
 "patientId":"88888888-8888-8888-8888-888888888888",
 "professionalId":"99999999-9999-9999-9999-999999999999",
 "slotId":"22222222-2222-2222-2222-222222222222",
 "appointmentStatus":"SCHEDULED",
 "flowStage":null,
 "reason":"Control",
 "cancelledAt":null,
 "cancelledBy":null,
 "createdAt":"2026-09-20T10:00:00",
 "updatedAt":"2026-09-20T10:00:00"
}
```

------------------------------------------------------------------------

## PatientResponse

``` json
{
 "id":"88888888-8888-8888-8888-888888888888",
 "name":"",
 "document":"",
 "phone":""
}
```

------------------------------------------------------------------------

## DashboardMetricResponse

``` json
{
 "metric":"APPOINTMENTS_CREATED",
 "value":250,
 "period":"MONTH"
}
```

------------------------------------------------------------------------

# 8. Usuarios, roles y permisos

## Usuarios

Las rutas de creación, listado y actualización de usuarios existen bajo `UserController`; los filtros enumerados aquí no se acreditan como contrato actual sin verificar la firma del controller.

``` http
GET /api/v1/users
```

Filtros:

``` text
name
email
role
status
```

------------------------------------------------------------------------

``` http
POST /api/v1/users
```

------------------------------------------------------------------------

``` http
PUT /api/v1/users/{id}
```

------------------------------------------------------------------------

## Roles

**RUTAS HISTÓRICAS:** no existe `GET /api/v1/roles` ni `PUT /api/v1/users/{id}/roles`. La asignación actual se expone mediante `POST /api/v1/users/{id}/roles` con autorización ADMIN; DEC-020 no instala administración granular de permisos.

``` http
GET /api/v1/roles
```

------------------------------------------------------------------------

``` http
PUT /api/v1/users/{id}/roles
```

------------------------------------------------------------------------

## Permisos

**NO IMPLEMENTADO:** las dos rutas HTTP siguientes no existen en controllers actuales.

``` http
GET /api/v1/permissions
```

------------------------------------------------------------------------

``` http
PUT /api/v1/roles/{id}/permissions
```

------------------------------------------------------------------------

# 9. Pacientes

`GET /api/v1/patients/me` existe para PATIENT. **NO IMPLEMENTADO:** `PUT /api/v1/patients/me`; la actualización actual es administrativa y DEC-017 sigue `OPEN`.

``` http
GET /api/v1/patients/me
```

``` http
PUT /api/v1/patients/me
```

------------------------------------------------------------------------

# 10. Especialidades

**CONCEPTUAL — NO IMPLEMENTADO:** DEC-006 aprueba Catalogs como propietario de definiciones y Professionals como propietario de la asociación N:M. V1 contiene tablas, pero no existe controller, DTO ni servicio funcional de catálogo. Todas las rutas y filtros de esta sección son propuestas históricas, no API vigente.

``` http
GET /api/v1/specialties
```

Filtros:

``` text
status
search
```

------------------------------------------------------------------------

``` http
POST /api/v1/specialties
```

Permiso:

``` text
SPECIALTIES_MANAGE
```

------------------------------------------------------------------------

``` http
PUT /api/v1/specialties/{id}
```

------------------------------------------------------------------------

Contenido público:

``` http
GET /api/v1/specialties/{id}/content
```

``` http
PUT /api/v1/specialties/{id}/content
```

------------------------------------------------------------------------

# 11. Agenda y disponibilidad

Todos los endpoints de esta sección requieren rol `ADMIN`.

## Crear agenda

``` http
POST /api/v1/agendas
```

Request:

``` json
{
 "professionalId":"33333333-3333-3333-3333-333333333333",
 "specialtyId":"44444444-4444-4444-4444-444444444444",
 "dayOfWeek":1,
 "startTime":"09:00:00",
 "endTime":"12:00:00"
}
```

Respuesta: `201 Created` con `AgendaResponseDTO`.

Errores relevantes: `400 Bad Request`, `403 Forbidden` y `404 Not Found`.

------------------------------------------------------------------------

## Consultar agendas

``` http
GET /api/v1/agendas
```

Filtros opcionales:

``` text
professionalId UUID
specialtyId UUID
```

Respuesta: `200 OK` con una lista de `AgendaResponseDTO`.

------------------------------------------------------------------------

## Consultar agenda por identificador

``` http
GET /api/v1/agendas/{id}
```

`id` utiliza UUID.

Respuesta: `200 OK`. Si no existe: `404 Not Found`.

------------------------------------------------------------------------

## Actualizar configuración de agenda

``` http
PUT /api/v1/agendas/{id}
```

Utiliza los mismos campos y validaciones que la creación.

Respuesta: `200 OK`. Si la agenda no existe: `404 Not Found`.

------------------------------------------------------------------------

## Activar o desactivar agenda

``` http
PATCH /api/v1/agendas/{id}/status
```

Request:

``` json
{
 "active":false
}
```

Esta operación solo modifica `schedules.active`.

Respuesta: `200 OK`. Si la agenda no existe: `404 Not Found`.

------------------------------------------------------------------------

## Consultar disponibilidad

``` http
GET /api/v1/availability
```

Filtros opcionales:

``` text
scheduleId UUID
professionalId UUID
slotDate date (ISO-8601)
status AVAILABLE | RESERVED | BLOCKED
```

Respuesta: `200 OK` con una lista de `AvailabilitySlotResponseDTO`.

Cada resultado contiene:

``` json
{
 "id":"22222222-2222-2222-2222-222222222222",
 "scheduleId":"11111111-1111-1111-1111-111111111111",
 "slotDate":"2026-10-01",
 "startTime":"09:00:00",
 "endTime":"09:30:00",
 "status":"AVAILABLE",
 "usable":true
}
```

------------------------------------------------------------------------

## Consultar slot por identificador

``` http
GET /api/v1/availability/{id}
```

`id` utiliza UUID.

Respuesta: `200 OK`. Si no existe: `404 Not Found`.

------------------------------------------------------------------------

# 12. Citas

## Crear

``` http
POST /api/v1/appointments
```

Request:

``` json
{
 "slotId":"22222222-2222-2222-2222-222222222222",
 "reason":"Control"
}
```

PATIENT no envía `patientId`; el backend lo resuelve desde el usuario
autenticado. ADMIN y RECEPTIONIST agregan `patientId` para crear la cita en
nombre de un paciente activo. `professionalId`, estados y especialidad son
determinados por el servidor.

Respuesta: `201 Created`. Un slot no reservable responde `409 Conflict` con
el código `SLOT_UNAVAILABLE`.

------------------------------------------------------------------------

## Consultar

``` http
GET /api/v1/appointments
```

PATIENT recibe únicamente sus citas. ADMIN y RECEPTIONIST reciben las citas
permitidas por su rol.

## Consultar por id

``` http
GET /api/v1/appointments/{id}
```

Respuesta: `200 OK`. Si no existe: `404 Not Found`. Un paciente intentando
consultar una cita ajena recibe `403 Forbidden`.

## Confirmar

``` http
POST /api/v1/appointments/{id}/confirm
```

No recibe body. Una cita `SCHEDULED` pasa a `CONFIRMED`. Si ya está confirmada,
responde `200 OK` de forma idempotente con `AppointmentResponseDTO` y no repite
la auditoría.

## Cancelar

``` http
POST /api/v1/appointments/{id}/cancel
```

No recibe body ni motivo de cancelación. Una cita `SCHEDULED` o `CONFIRMED`
pasa a `CANCELLED`. Si ya está cancelada, responde `200 OK` de forma idempotente
sin modificar `cancelledAt` o `cancelledBy`, liberar nuevamente el slot ni
repetir la auditoría.

## Reprogramar

``` http
POST /api/v1/appointments/{id}/reschedule
```

Request:

``` json
{
 "slotId":"22222222-2222-2222-2222-222222222222"
}
```

`slotId` es obligatorio. El servidor conserva el paciente y motivo, deriva el
profesional desde el nuevo slot y controla estados, auditoría y relación de
sucesión. Responde `201 Created`, incluye `Location` hacia la cita sucesora y
devuelve su `AppointmentResponseDTO`.

PATIENT opera solo sobre citas propias. ADMIN y RECEPTIONIST operan según las
reglas existentes para pacientes activos. PROFESSIONAL no tiene acceso.

Errores lifecycle:

  HTTP   errorCode                         Condición
  ------ --------------------------------- -----------------------------------
  400    VALIDATION_ERROR                  UUID o request inválido
  401    AUTHENTICATION_REQUIRED           Falta autenticación
  403    APPOINTMENT_ACCESS_DENIED         Rol u ownership rechazado
  404    APPOINTMENT_NOT_FOUND             Cita inexistente
  404    PATIENT_NOT_AVAILABLE             Paciente activo no disponible
  409    INVALID_APPOINTMENT_TRANSITION    Estado incompatible
  409    SLOT_UNAVAILABLE                  Nuevo slot no reservable
  409    APPOINTMENT_SUCCESSOR_EXISTS      Ya existe sucesora directa
  409    PROFESSIONAL_NOT_AVAILABLE        Profesional activo no disponible
  409    APPOINTMENT_SLOT_RELEASE_FAILED   No se pudo liberar el slot anterior

------------------------------------------------------------------------

# 13. Appointment Operations

Los siguientes endpoints están implementados en Appointment Operations.

## Check-in

``` http
POST /api/v1/appointments/{id}/check-in
```

Rol: `RECEPTIONIST`.

Precondición: `AppointmentStatus = CONFIRMED` y `flowStage = null`.

Respuesta: `200 OK` con `AppointmentResponseDTO` y
`flowStage = CHECK_IN`.

## Pasar a espera

``` http
POST /api/v1/appointments/{id}/waiting
```

Rol: `RECEPTIONIST`.

Precondición: `AppointmentStatus = CONFIRMED` y
`flowStage = CHECK_IN`.

Respuesta: `200 OK` con `AppointmentResponseDTO` y
`flowStage = WAITING`.

## Iniciar atención

``` http
POST /api/v1/appointments/{id}/start-attention
```

Rol: `PROFESSIONAL`, únicamente cuando su usuario esté vinculado al
`professionalId` de la cita.

Precondición: `AppointmentStatus = CONFIRMED` y
`flowStage = WAITING`.

Respuesta: `200 OK` con `AppointmentResponseDTO` y
`flowStage = IN_ATTENTION`.

## Completar atención

``` http
POST /api/v1/appointments/{id}/complete
```

Rol: `PROFESSIONAL`, únicamente sobre su propia cita profesional.

Precondición: `AppointmentStatus = CONFIRMED` y
`flowStage = IN_ATTENTION`.

Respuesta: `200 OK` con `AppointmentResponseDTO`,
`appointmentStatus = COMPLETED` y `flowStage = FINISHED`.

Los cuatro comandos no reciben request body. Repetir el comando de la etapa
actual es idempotente y no duplica auditoría. Omitir una etapa, retroceder o
operar desde un estado incompatible retorna `409 Conflict`. `PATIENT` y `ADMIN`
no pueden ejecutar estas transiciones. Completar no libera el slot reservado.

------------------------------------------------------------------------

# 14. Workflow

El modelo aprobado es:

``` text
CONFIRMED + flowStage null
    -> CHECK_IN
    -> WAITING
    -> IN_ATTENTION
    -> FINISHED + AppointmentStatus COMPLETED
```

Triaje, prioridad, no-show, reglas temporales, waitlist, notificaciones y datos
clínicos quedan fuera del alcance de FASE 5.11.

------------------------------------------------------------------------

# 15. Dashboard

**NO IMPLEMENTADO:** no existe controller ni DTO funcional de dashboard. La ruta, filtros y agrupaciones siguientes son diseño histórico.

``` http
GET /api/v1/dashboard/metrics
```

Filtros:

``` text
startDate
endDate
specialtyId
professionalId
groupBy
```

Valores:

``` text
DAY
WEEK
MONTH
YEAR
```

------------------------------------------------------------------------

# 16. Códigos de error de negocio

Lista de diseño histórico; los códigos efectivamente devueltos se comprueban en los exception handlers actuales. `SPECIALTY_INACTIVE` no acredita validación activa de Specialty en Agenda.

  Código                         Descripción
  ------------------------------ ----------------------------
  APPOINTMENT_ALREADY_RESERVED   Disponibilidad ocupada
  INVALID_APPOINTMENT_TRANSITION Cambio de estado inválido
  USER_PERMISSION_DENIED         Sin permisos
  SPECIALTY_INACTIVE             Especialidad no disponible
  PATIENT_NOT_FOUND              Paciente inexistente

------------------------------------------------------------------------

# 17. Seguridad

La lista incluye controles objetivo. JWT y autorización por roles existen; HTTPS de despliegue, rate limiting y políticas de auditoría se verifican por separado, sin inferir implementación por su mención aquí.

Incluye:

-   JWT;
-   refresh tokens;
-   RBAC;
-   HTTPS;
-   rate limiting;
-   validación entrada;
-   auditoría.

------------------------------------------------------------------------

**Documento actualizado como 06-API-SPECIFICATION v1.3.1 FINAL.**
