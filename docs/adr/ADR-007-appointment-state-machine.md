# ADR-007 - Máquina de Estados de Citas

## Estado

Aceptado - Versión 1.1 (FASE 5.11.1)

## Contexto

Appointments necesita representar por separado el ciclo administrativo de la
cita y el avance operativo del paciente durante una atención confirmada. Ambos
conceptos se persisten en `appointments`, pero no forman una sola enumeración.

## Decisión

Mantener dos máquinas coordinadas:

-   `AppointmentStatus` representa el estado administrativo de la cita;
-   `FlowStage` representa exclusivamente el flujo operativo asistencial.

### AppointmentStatus

``` text
SCHEDULED
CONFIRMED
CANCELLED
RESCHEDULED
COMPLETED
```

### FlowStage

``` text
CHECK_IN
WAITING
IN_ATTENTION
FINISHED
```

Antes de iniciar el flujo, `flowStage` es `null`.

## Transiciones aprobadas para FASE 5.11

El flujo operativo solo puede comenzar sobre una cita `CONFIRMED`:

``` text
AppointmentStatus = CONFIRMED, FlowStage = null
    -> CHECK_IN
    -> WAITING
    -> IN_ATTENTION
    -> FINISHED
```

La transición `IN_ATTENTION -> FINISHED` también cambia
`AppointmentStatus` de `CONFIRMED` a `COMPLETED`. El valor final se conserva
como `flowStage = FINISHED`.

Las operaciones existentes de confirmación, cancelación y reprogramación no se
alteran. Una cita `CANCELLED` o `RESCHEDULED` no puede iniciar ni continuar el
flujo. Una cita sucesora creada por reprogramación comienza como `SCHEDULED` y
`flowStage = null`.

## Actores y ownership

-   `RECEPTIONIST` puede ejecutar `CHECK_IN` y `WAITING`.
-   `PROFESSIONAL` puede ejecutar `IN_ATTENTION` y `FINISHED` únicamente cuando
    el `professionalId` de la cita está vinculado al usuario autenticado.
-   `PATIENT` no puede modificar el flujo.
-   `ADMIN` no tiene override sobre estas operaciones.
-   Un profesional no puede ejecutar check-in ni waiting.
-   Un recepcionista no puede iniciar ni finalizar la atención.

El vínculo profesional-usuario se validará mediante un contrato público del
módulo professionals, sin exponer entidades ni repositories.

## Idempotencia y errores

Repetir la operación correspondiente a la etapa actual devuelve éxito sin
cambiar datos ni duplicar auditoría. Intentar una operación anterior cuando la
cita ya avanzó, omitir una etapa o ejecutar cualquier otra transición inválida
se rechaza con conflicto `409`.

## Persistencia y slots

La decisión reutiliza `appointment_status`, `flow_stage`, `updated_at` y
`audit_logs`. No requiere una migración `V4`. En esta fase no se agregan
`completed_at`, `completed_by`, `check_in_at`, `waiting_at`,
`attention_started_at` ni `finished_at`.

Completar una cita no libera el availability slot. El slot permanece
`RESERVED`, porque representa capacidad consumida por una atención realizada.

## Transacciones y concurrencia

Cada transición debe:

1. bloquear la cita mediante `PESSIMISTIC_WRITE`;
2. validar actor, ownership, estado administrativo y etapa actual;
3. persistir transición y auditoría en la misma transacción.

Un fallo de auditoría, persistencia o validación crítica revierte por completo
la transición, sin compensaciones manuales. Este mecanismo evita dobles avances
y actualizaciones perdidas ante solicitudes concurrentes.

## Auditoría

Eventos aprobados:

``` text
APPOINTMENT_CHECKED_IN
APPOINTMENT_WAITING
APPOINTMENT_ATTENTION_STARTED
APPOINTMENT_COMPLETED
```

Cada evento conserva actor y valores anterior/nuevo de
`appointmentStatus` y `flowStage` mediante la infraestructura de auditoría
existente. Una repetición idempotente no genera un segundo evento. FASE 5.11 no
añade un mecanismo nuevo para capturar IP o user-agent.

## API aprobada

``` http
POST /api/v1/appointments/{id}/check-in
POST /api/v1/appointments/{id}/waiting
POST /api/v1/appointments/{id}/start-attention
POST /api/v1/appointments/{id}/complete
```

Los cuatro comandos no reciben body y responden `200 OK` con
`AppointmentResponseDTO`. Esta sección aprueba el contrato para su
implementación posterior; no declara que los endpoints ya existan.

## Fuera de alcance

-   `NO_SHOW` y reglas temporales;
-   zona horaria hospitalaria;
-   triaje independiente o estados adicionales;
-   prioridad clínica;
-   waitlist y notificaciones;
-   liberación del slot al completar;
-   historia clínica, diagnósticos o notas de atención.

## Consecuencias

La máquina queda alineada con las enumeraciones y constraints persistentes ya
existentes. Cualquier ampliación de estados, reglas temporales o pasos clínicos
requerirá una nueva decisión arquitectónica y, cuando corresponda, una
migración explícita.
