# BPMN-02 — Consulta de disponibilidad y reserva

## Objetivo

Separar consulta, revalidación concurrente, creación de cita y constancia.

## Alcance

Entrada desde portal público; reserva por paciente o recepción autorizada en superficies de autoservicio/operación. Solo una cita activa puede adquirir un slot.

## Fuente

`docs/auditoria/TO-BE.md` T02, T04, T07–T08, T11–T12 y D02–D04/D06; SRS 0.1; RB-003–007; ADR-006.

## Actores

Visitante, paciente, recepción y sistema HospitalPlatform.

## Actividades

T02: consulta pública. T07–T08: catálogo y cupos. T04: acceso cuando se reserva. T11: revalidación y creación atómica de `Appointment SCHEDULED` con `flowStage=null` o conflicto. T12: constancia sin confirmación. T27 es referencia transversal de auditoría.

## Decisiones

D02 permiso; D03 especialidad activa; D04 cupo elegible; D06 slot aún libre dentro de transacción. Un conflicto vuelve a disponibilidad sin reserva parcial.

## Requisitos relacionados

RF-002–004, RF-008, RF-010, RF-012–015, RF-032; RNF-003, RNF-007, RNF-010–011.

## Reglas de negocio relacionadas

RB-002–007, RB-020, RB-022.

## Exclusiones

No se confirma asistencia por emitir constancia ni se crean dos citas activas para el mismo slot.

## Pendientes de validación

Filtros y políticas institucionales de disponibilidad.

## Archivo fuente del diagrama

`BPMN-02-RESERVA.mmd`; tabla de tareas/gateways en `../README.md`.
