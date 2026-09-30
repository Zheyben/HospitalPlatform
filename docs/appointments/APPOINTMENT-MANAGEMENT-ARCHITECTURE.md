# APPOINTMENT MANAGEMENT ARCHITECTURE

## Hospital Platform

## Estado

Versión 1.5 - Appointment Operations implementado.

------------------------------------------------------------------------

# 1. Objetivo

Definir la arquitectura funcional y técnica del módulo `appointments`.

Este documento establece:

-   responsabilidades del módulo;
-   límites con otros módulos;
-   modelo de dominio;
-   integración con agenda;
-   ciclo de vida de citas;
-   reglas de negocio;
-   seguridad;
-   auditoría;
-   estrategia de testing.

------------------------------------------------------------------------

# 2. Contexto

El módulo appointments pertenece al dominio de atención ambulatoria.

Módulos relacionados:

-   auth;
-   users;
-   patients;
-   professionals;
-   agenda;
-   appointments;
-   waitlist;
-   priority;
-   notifications;
-   dashboard;
-   audit.

Appointments no debe acceder directamente a entidades internas de otros
módulos.

La comunicación debe realizarse mediante contratos o servicios públicos
definidos.

------------------------------------------------------------------------

# 3. Alcance

## Incluye

-   creación de citas;
-   consulta de citas;
-   confirmación;
-   cancelación;
-   reprogramación;
-   flujo operativo de check-in, espera, atención y finalización;
-   control de estados;
-   integración con availability slots.

## No incluye

-   historia clínica;
-   diagnósticos;
-   tratamientos;
-   recetas;
-   laboratorio;
-   imágenes médicas;
-   notificaciones automáticas;
-   disponibilidad avanzada.
-   no-show y reglas temporales;
-   triaje independiente y prioridad clínica;
-   lista de espera;
-   historia clínica, diagnósticos y notas de atención.

------------------------------------------------------------------------

# 4. Arquitectura del módulo

Estructura esperada:

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

El módulo administra la lógica propia de citas y no replica
responsabilidades de otros módulos.

------------------------------------------------------------------------

# 5. Modelo Agenda y Slots

Modelo oficial:

``` text
Schedule

↓

AvailabilitySlot

↓

Appointment
```

Los slots son discretos:

-   09:00
-   09:30
-   10:00

El uso de slots permite:

-   evitar doble reserva;
-   simplificar disponibilidad;
-   facilitar implementación MVP.

Una cita requiere un slot válido y disponible.

No se permiten reservas sobre slots ocupados o no disponibles.

------------------------------------------------------------------------

# 6. Entidad Appointment

Campos conceptuales:

``` text
id UUID

patient_id UUID

professional_id UUID

slot_id UUID

appointment_status

flow_stage

reason

cancelled_at

cancelled_by

created_at

updated_at
```

Los identificadores utilizan UUID.

------------------------------------------------------------------------

# 7. Relaciones del dominio

## Patient

Una cita pertenece a un paciente válido.

Appointments no debe importar directamente:

``` text
patients.entity.Patient
```

La validación debe realizarse mediante contratos públicos.

------------------------------------------------------------------------

## Professional

Una cita pertenece a un profesional válido.

Appointments no debe importar directamente entidades internas del módulo
professionals.

La validación debe realizarse mediante contratos públicos.

------------------------------------------------------------------------

## AvailabilitySlot

Un slot solo puede tener una reserva activa.

No se permite doble reserva del mismo slot.

------------------------------------------------------------------------

# 8. Estados de Appointment

El sistema separa estado administrativo y flujo operativo.

Son conceptos independientes.

Ejemplo:

``` text
status = CONFIRMED

flow_stage = WAITING
```

------------------------------------------------------------------------

# 9. AppointmentStatus

Estado administrativo:

``` text
SCHEDULED

CONFIRMED

CANCELLED

RESCHEDULED

COMPLETED
```

------------------------------------------------------------------------

# 10. FlowStage

Flujo operativo de atención:

``` text
CHECK_IN

WAITING

IN_ATTENTION

FINISHED
```

------------------------------------------------------------------------

# 11. Máquina de estados

`AppointmentStatus` y `FlowStage` son máquinas distintas y coordinadas.
Una cita confirmada inicia el flujo con `flowStage = null`:

``` text
AppointmentStatus = CONFIRMED, FlowStage = null
    -> CHECK_IN
    -> WAITING
    -> IN_ATTENTION
    -> FINISHED
```

La transición final cambia simultáneamente `AppointmentStatus` de `CONFIRMED`
a `COMPLETED` y conserva `flowStage = FINISHED`.

Una cita `CANCELLED` o `RESCHEDULED` no puede iniciar ni continuar el flujo.
La cita sucesora de una reprogramación inicia como `SCHEDULED` y
`flowStage = null`.

Repetir la operación correspondiente a la etapa actual es idempotente: retorna
éxito sin escribir ni auditar nuevamente. Intentar una operación anterior
después de avanzar, omitir una etapa o ejecutar cualquier otra transición
inválida se rechaza con `409 Conflict`.

------------------------------------------------------------------------

# 12. Reglas de negocio

## Creación

Validar:

-   paciente existente;
-   profesional existente;
-   slot existente;
-   slot disponible;
-   ausencia de doble reserva mediante UPDATE condicional;
-   profesional activo derivado del slot.

La validación de slots pasados queda pendiente hasta que exista una zona
horaria hospitalaria explícita. No se utiliza la zona del sistema como valor
implícito.

------------------------------------------------------------------------

## Confirmación

Una cita `SCHEDULED` puede pasar a `CONFIRMED`. La operación es idempotente
cuando la cita ya está confirmada: no repite la transición ni la auditoría.
Las citas `CANCELLED` y `RESCHEDULED` no pueden confirmarse.

------------------------------------------------------------------------

## Cancelación

Las cancelaciones deben conservar historial.

Una cita `SCHEDULED` o `CONFIRMED` puede pasar a `CANCELLED`. La operación
establece el actor y el instante de cancelación, libera el slot reservado
mediante `AvailabilitySlotReleaseService` y registra la transición mediante
`AuditLogService`.

Registrar:

``` text
cancelled_at

cancelled_by
```

La cancelación repetida es idempotente: conserva los datos originales y no
repite la liberación ni la auditoría.

------------------------------------------------------------------------

## Reprogramación

Una cita `SCHEDULED` o `CONFIRMED` puede reprogramarse. La cita original pasa
a `RESCHEDULED` y se crea una nueva cita `SCHEDULED`, con el mismo paciente,
el profesional derivado del nuevo slot, `flowStage = null` y
`rescheduledFromId` apuntando a la cita original.

El modelo permite cadenas `A → B → C`, pero una cita solo puede tener una
sucesora directa. El service no acepta referencias arbitrarias del cliente, por
lo que no puede introducir ciclos.

`AppointmentService` bloquea la cita original con `PESSIMISTIC_WRITE`, reserva
atómicamente el nuevo slot mediante `AvailabilitySlotReservationService`,
persiste ambas citas, libera el slot anterior mediante
`AvailabilitySlotReleaseService` y registra la relación mediante
`AuditLogService`.

Confirmación, cancelación y reprogramación se ejecutan en una única transacción.
Los contratos de reserva, liberación y auditoría participan en ella. Cualquier
fallo revierte estados, slots, sucesora y auditoría sin compensaciones manuales.

------------------------------------------------------------------------

## Finalización

Solo `PROFESSIONAL`, vinculado al `professionalId` de la cita, puede ejecutar
`IN_ATTENTION -> FINISHED`. La operación establece también
`AppointmentStatus = COMPLETED`, mantiene el slot `RESERVED` y registra
`APPOINTMENT_COMPLETED` en la misma transacción.

Completar no libera el slot: la capacidad fue consumida por la atención.
No se crean timestamps adicionales por etapa ni reglas temporales en esta fase.

Todas las transiciones de flujo bloquean la cita mediante `PESSIMISTIC_WRITE` y
validan rol, ownership, estado y etapa antes de persistir. Un fallo de auditoría
revierte la transición completa.

------------------------------------------------------------------------

# 13. Historial y Soft Delete

Las citas históricas no deben eliminarse físicamente.

El sistema debe conservar:

-   información de la cita;
-   cambios de estado;
-   trazabilidad.

El uso de `deleted_at` aplica a las entidades definidas por la
estrategia general de soft delete.

------------------------------------------------------------------------

# 14. Auditoría

Las operaciones importantes deben registrarse mediante:

``` text
audit_logs
```

Modelo:

``` text
id

user_id

action

entity_name

entity_id

old_values JSONB

new_values JSONB

ip_address

user_agent

created_at
```

Eventos esperados:

``` text
APPOINTMENT_CREATED

APPOINTMENT_CONFIRMED

APPOINTMENT_CANCELLED

APPOINTMENT_RESCHEDULED

APPOINTMENT_CHECKED_IN

APPOINTMENT_WAITING

APPOINTMENT_ATTENTION_STARTED

APPOINTMENT_COMPLETED
```

El lifecycle core implementa actualmente `APPOINTMENT_CONFIRMED`,
`APPOINTMENT_CANCELLED` y `APPOINTMENT_RESCHEDULED`. Los cuatro eventos del
flujo operativo están implementados en FASE 5.11.2.
Cada uno debe incluir actor y valores anterior/nuevo de `appointmentStatus` y
`flowStage`; una repetición idempotente no genera auditoría duplicada.

------------------------------------------------------------------------

# 15. Seguridad y permisos

Roles relacionados:

``` text
ADMIN

RECEPTIONIST

PROFESSIONAL

PATIENT
```

Reglas iniciales:

  Acción               ADMIN       RECEPTIONIST   PROFESSIONAL     PATIENT
  -------------------- ----------- -------------- ---------------- ------------
  Crear cita           Sí          Sí             No               Solo propia
  Consultar citas      Todas       Operativas     No               Solo propias
  Confirmar cita       Sí          Sí             No               Solo propia
  Cancelar cita        Sí          Sí             No               Solo propia
  Reprogramar cita     Sí          Sí             No               Solo propia
  Check-in             No          Sí             No               No
  Pasar a waiting      No          Sí             No               No
  Iniciar atención     No          No             Solo asignada    No
  Completar atención   No          No             Solo asignada    No

Las validaciones de ownership permanecen en `AppointmentService`. El paciente
se resuelve desde el usuario autenticado mediante `CurrentUserService` y
`PatientLookupService`; ADMIN y RECEPTIONIST operan sobre pacientes activos.

`PROFESSIONAL` solo puede iniciar y completar la atención cuando la cita está
asociada a su perfil; no puede ejecutar check-in ni waiting. `ADMIN` no tiene
override sobre el flujo operativo.

El ownership profesional se valida mediante el contrato público aprobado:

``` java
boolean isActiveProfessionalLinkedToUser(UUID professionalId, UUID userId)
```

El contrato está implementado dentro de professionals y no expone entidades,
repositories, JPA ni SQL.

Los permisos se aplican mediante el RBAC existente.

------------------------------------------------------------------------

# 16. API REST

Base:

``` text
/api/v1/appointments
```

Endpoints implementados en Foundation:

``` http
POST   /api/v1/appointments

GET    /api/v1/appointments

GET    /api/v1/appointments/{id}

POST   /api/v1/appointments/{id}/confirm

POST   /api/v1/appointments/{id}/cancel

POST   /api/v1/appointments/{id}/reschedule
```

Confirmación y cancelación reciben únicamente el identificador de la cita y
responden `200 OK` con `AppointmentResponseDTO`. Reprogramación recibe solamente
el nuevo `slotId` y responde `201 Created`, `Location` y el DTO de la sucesora.
Los tres endpoints admiten PATIENT sobre citas propias, ADMIN y RECEPTIONIST
según las reglas existentes; PROFESSIONAL permanece rechazado. El flujo
asistencial implementado es:

``` http
POST /api/v1/appointments/{id}/check-in
POST /api/v1/appointments/{id}/waiting
POST /api/v1/appointments/{id}/start-attention
POST /api/v1/appointments/{id}/complete
```

Estos comandos no reciben body y responden `200 OK` con
`AppointmentResponseDTO`.

------------------------------------------------------------------------

# 17. DTOs

No exponer entidades JPA directamente.

DTOs esperados:

``` text
AppointmentResponseDTO

CreateAppointmentRequestDTO

RescheduleAppointmentRequestDTO

AppointmentErrorResponseDTO
```

------------------------------------------------------------------------

# 18. Testing

Debe cubrir:

## Service

-   creación correcta;
-   paciente inexistente;
-   profesional inexistente;
-   slot ocupado;
-   doble reserva;
-   rollback de la reserva;
-   estado inicial.
-   confirmación y cancelación idempotentes;
-   reprogramación y cadenas de sucesión;
-   rollback de cada efecto transaccional;
-   doble confirmación, cancelación y reprogramación concurrentes.
-   transición exacta de cada etapa del flujo;
-   idempotencia sin auditoría duplicada;
-   rechazo de saltos, retrocesos y estados incompatibles;
-   ownership del profesional;
-   rollback ante fallo de auditoría;
-   concurrencia con transacciones PostgreSQL independientes y
    `CountDownLatch`, sin `Thread.sleep()`.

## Security

-   ADMIN autorizado;
-   RECEPTIONIST autorizado;
-   PATIENT solo acceso propio.

## Integration

-   PostgreSQL Testcontainers;
-   Flyway;
-   restricciones de base de datos.

------------------------------------------------------------------------

# 19. Consideraciones futuras Frontend

Preparar para:

-   calendario diario;
-   calendario semanal;
-   filtros por profesional;
-   filtros por especialidad;
-   vista profesional;
-   vista paciente.

------------------------------------------------------------------------

# Historial de cambios

## v1.1

Cambios aplicados después de auditoría:

-   aclarada separación entre status y flow_stage;
-   agregado comportamiento de reprogramación;
-   agregado manejo de historial;
-   agregado restricción de slots pasados;
-   ampliada matriz inicial de permisos;
-   ampliada estrategia de pruebas.

## v1.2

-   documentado el lifecycle core implementado;
-   documentada la idempotencia de confirmación y cancelación;
-   documentadas transacciones, locks, ownership, auditoría y liberación de
    slots;
-   aclarado que el lifecycle todavía no expone endpoints.

## v1.3

-   expuestos confirmación, cancelación y reprogramación mediante HTTP;
-   documentados request, respuestas, autorización, ownership e idempotencia;
-   documentados los códigos de error del lifecycle.

## v1.4

-   aprobada la máquina operativa `CHECK_IN -> WAITING -> IN_ATTENTION ->
    FINISHED`;
-   definida la finalización coordinada como `AppointmentStatus = COMPLETED`;
-   aprobados actores, ownership profesional, idempotencia, auditoría,
    concurrencia y rollback;
-   aprobados los cuatro comandos HTTP de FASE 5.11 sin declararlos todavía
    implementados;
-   excluidos no-show, reglas temporales, triaje, prioridad, waitlist y
    notificaciones.

## v1.5

-   implementados check-in, waiting, inicio y finalización de atención;
-   implementados roles y ownership profesional mediante contrato público;
-   implementadas idempotencia, auditoría transaccional y protección mediante
    bloqueo pesimista;
-   agregada cobertura unitaria, HTTP e integración PostgreSQL para rollback y
    concurrencia.
