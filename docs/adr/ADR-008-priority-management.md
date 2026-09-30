# ADR-008 --- Gestión de Prioridad Ambulatoria

## Estado

Aceptado

## Decisión

Implementar prioridad ambulatoria validada.

No representa emergencia.

## Flujo

Paciente solicita prioridad

↓

Revisión autorizada

↓

Aprobación o rechazo

## Estados

-   REQUESTED
-   UNDER_REVIEW
-   APPROVED
-   REJECTED

## Reglas

-   El paciente no puede autoasignarse prioridad.
-   El sistema no realiza diagnóstico.
-   Emergencia queda fuera del MVP de citas.
