# ADR-006 --- Modelo de Agenda y Slots

## Estado

Aceptado

## Decisión

Utilizar slots discretos.

Ejemplo:

-   09:00
-   09:30
-   10:00

## Modelo

Schedule

↓

AvailabilitySlot

↓

Appointment

## Motivo

Permite:

-   evitar doble reserva;
-   simplificar disponibilidad;
-   facilitar implementación MVP.

## Consideración futura

La duración del slot podrá configurarse según especialidad si el negocio
lo requiere.
