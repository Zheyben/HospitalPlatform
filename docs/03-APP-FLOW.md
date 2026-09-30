# 03 - APP FLOW v1.3 FINAL

## Estado del documento

**Versión:** 1.3 FINAL (FASE 5.11.1)

**Producto:** Plataforma web y móvil modular para la gestión de atención
ambulatoria, citas médicas y trazabilidad del flujo asistencial.

**Documentos relacionados:**

-   PRD v1.4 FINAL
-   TRD v1.2 FINAL ajustado
-   UI/UX DESIGN BRIEF v1.2.1 FINAL
-   BACKEND SCHEMA v1.3.1 FINAL
-   API SPECIFICATION v1.3 FINAL
-   ADR-007 v1.1

------------------------------------------------------------------------

# 1. Objetivo del documento

El APP FLOW define los recorridos funcionales principales del sistema,
conectando:

-   usuarios;
-   módulos;
-   procesos;
-   estados;
-   reglas de negocio.

Sirve como base para:

-   diseño UX/UI;
-   definición de pantallas;
-   casos de uso;
-   pruebas funcionales;
-   validación del comportamiento del sistema.

------------------------------------------------------------------------

# 2. Principios de navegación

## Separación de usuarios

La plataforma separa espacios según rol:

``` text
Usuarios externos

- Visitante
- Paciente


Usuarios internos

- Administrador
- Recepcionista
- Triaje
- Profesional
```

------------------------------------------------------------------------

## Flujo guiado

Las acciones principales deben conducir al usuario mediante pasos
claros:

``` text
Consultar información

↓

Seleccionar servicio

↓

Realizar acción

↓

Confirmar resultado
```

------------------------------------------------------------------------

## Seguridad por contexto

Las funcionalidades dependen de:

-   autenticación;
-   rol;
-   permisos;
-   autorización sobre recursos.

------------------------------------------------------------------------

# 3. Separación de estados del sistema

Se separan oficialmente dos conceptos:

-   estado administrativo de la cita;
-   estado operativo del flujo asistencial.

------------------------------------------------------------------------

# Appointment Status

Representa el estado administrativo de la cita.

Modelo canónico:

``` text
SCHEDULED
CONFIRMED
CANCELLED
RESCHEDULED
COMPLETED
```

`AVAILABLE` y `RESERVED` son estados del availability slot, no de la cita.
`NO_SHOW` permanece fuera del alcance aprobado.

------------------------------------------------------------------------

# Workflow Status

Representa el estado operativo del paciente dentro de una cita confirmada.

Modelo canónico de `FlowStage`:

``` text
CHECK_IN
WAITING
IN_ATTENTION
FINISHED
```

Antes de iniciar el flujo, `flowStage = null`. Una cita solo puede iniciar el
flujo cuando `appointmentStatus = CONFIRMED`.

------------------------------------------------------------------------

# 4. Flujo visitante

``` text
Ingreso al portal

↓

Página institucional

↓

Consulta información del hospital

↓

Consulta especialidades

↓

Consulta servicios disponibles

↓

Solicita reserva

↓

Registro o inicio de sesión
```

------------------------------------------------------------------------

# 5. Flujo paciente

## Nueva reserva

``` text
Inicio sesión

↓

Dashboard paciente

↓

Seleccionar especialidad

↓

Seleccionar profesional

↓

Consultar disponibilidad

↓

Seleccionar fecha y horario

↓

Validar disponibilidad nuevamente

↓

Crear reserva

↓

Appointment Status:
SCHEDULED

↓

Confirmación

↓

Appointment Status:
CONFIRMED

↓

Seguimiento asistencial
```

------------------------------------------------------------------------

# 6. Modificación de citas

El paciente podrá gestionar citas existentes.

Acciones:

-   cancelar;
-   reprogramar.

Flujo:

``` text
Consultar citas

↓

Seleccionar cita

↓

Elegir acción

        |
        |
        +---- Cancelar cita
        |
        +---- Reprogramar cita

↓

Actualizar estado

↓

Registrar historial

↓

Notificación futura (fuera de FASE 5.11)
```

------------------------------------------------------------------------

# 7. Flujo recepción

``` text
Paciente con cita CONFIRMED y flowStage = null

↓

RECEPTIONIST ejecuta CHECK_IN

↓

RECEPTIONIST ejecuta WAITING
```

Recepción no puede iniciar ni finalizar la atención profesional.

------------------------------------------------------------------------

# 8. Flujo triaje

Triaje clínico, prioridad y evaluación inicial permanecen como capacidades
futuras. FASE 5.11 no crea endpoints, payloads, estados ni transiciones de
triaje. `WAITING` representa que el paciente espera atención.

------------------------------------------------------------------------

# 9. Flujo profesional

``` text
Inicio sesión profesional

↓

Visualizar agenda y citas asignadas

↓

Cita en WAITING vinculada al profesional autenticado

↓

PROFESSIONAL ejecuta IN_ATTENTION

↓

PROFESSIONAL ejecuta FINISHED

↓

Appointment Status:
COMPLETED
```

Al finalizar se conserva `flowStage = FINISHED`. Esta fase no registra
diagnósticos, indicaciones ni información clínica.

------------------------------------------------------------------------

# 10. Flujo administrativo

``` text
Inicio sesión administrativo

↓

Panel administrativo

↓

Gestionar usuarios y roles

↓

Gestionar especialidades

↓

Gestionar profesionales

↓

Configurar horarios

↓

Crear disponibilidad

↓

Supervisar citas

↓

Gestionar lista de espera

↓

Consultar indicadores
```

------------------------------------------------------------------------

# 11. Flujo completo de gestión de citas

``` text
Disponibilidad creada

↓

Paciente consulta y reserva horario

↓

Cita SCHEDULED con flowStage = null

↓

Confirmación

↓

Cita CONFIRMED con flowStage = null

↓

CHECK_IN

↓

WAITING

↓

IN_ATTENTION

↓

FINISHED + Appointment Status COMPLETED
```

El slot permanece `RESERVED` al completar porque representa capacidad
consumida.

------------------------------------------------------------------------

# 12. Flujo de lista de espera

Estado: futuro, fuera de FASE 5.11.

``` text
Paciente sin disponibilidad

↓

Solicitud ingreso a lista

↓

Registro de espera

↓

Liberación de cupo

↓

Identificación de candidatos

↓

Validación administrativa

↓

Asignación de cita
```

------------------------------------------------------------------------

# 13. Trazabilidad ambulatoria

``` text
APPOINTMENT_CHECKED_IN
    -> APPOINTMENT_WAITING
    -> APPOINTMENT_ATTENTION_STARTED
    -> APPOINTMENT_COMPLETED
```

Cada evento debe conservar actor y valores anterior/nuevo de
`appointmentStatus` y `flowStage` en la misma transacción. Un fallo de
auditoría revierte la transición y una repetición idempotente no duplica el
evento.

------------------------------------------------------------------------

# 14. Flujos alternativos y excepciones

## Cupo ocupado

``` text
Usuario intenta reservar

↓

Sistema detecta conflicto

↓

Rechaza operación

↓

Muestra horarios disponibles
```

------------------------------------------------------------------------

## Sesión expirada

``` text
Usuario realiza acción

↓

Sistema valida sesión

↓

Sesión inválida

↓

Solicita autenticación nuevamente
```

------------------------------------------------------------------------

## Cancelación no permitida

``` text
Usuario solicita cancelación

↓

Sistema valida reglas

↓

Acepta o rechaza operación
```

------------------------------------------------------------------------

## Reprogramación sin disponibilidad

``` text
Paciente solicita cambio

↓

Sistema consulta disponibilidad

↓

No existen horarios

↓

Mantiene cita actual

↓

Informa alternativas
```

------------------------------------------------------------------------

## Usuario sin permisos

``` text
Usuario intenta acceder

↓

Sistema valida rol

↓

Acceso rechazado
```

------------------------------------------------------------------------

# 15. Reglas generales

-   Los estados administrativos no deben mezclarse con estados operativos.
-   Solo una cita `CONFIRMED` puede iniciar el flujo.
-   Las transiciones deben respetar el orden exacto.
-   Una cita cancelada o reprogramada no puede continuar el workflow.
-   Repetir la operación de la etapa actual retorna éxito sin duplicar
    auditoría.
-   Una operación anterior aplicada sobre una etapa posterior retorna conflicto
    y nunca retrocede la cita.
-   `RECEPTIONIST` ejecuta únicamente `CHECK_IN` y `WAITING`.
-   `PROFESSIONAL` ejecuta únicamente `IN_ATTENTION` y `FINISHED` sobre
    citas vinculadas a su usuario.
-   `PATIENT` y `ADMIN` no modifican `FlowStage`.
-   Cada transición utiliza bloqueo pesimista y comparte transacción con su
    auditoría.
-   Los pacientes únicamente pueden gestionar sus propias citas en las
    operaciones ya aprobadas.
-   No se aplican reglas temporales ni zona horaria en FASE 5.11.

------------------------------------------------------------------------

# 16. Criterios UX principales

La experiencia debe priorizar:

-   navegación simple;
-   reducción de pasos innecesarios;
-   información clara;
-   prevención de errores;
-   confirmación de acciones importantes.

Los usuarios deben comprender:

-   qué acción realizan;
-   cuál es el resultado esperado;
-   qué estado tiene actualmente su solicitud.

------------------------------------------------------------------------

**Documento actualizado como 03-APP-FLOW v1.3 FINAL.**
