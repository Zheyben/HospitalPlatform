# BPMN-06 — Flujo operativo de consulta externa

## Objetivo

Mostrar las cuatro transiciones aprobadas desde cita confirmada hasta cierre.

## Alcance

`CONFIRMED` con `flowStage=null` permite `CHECK_IN → WAITING → IN_ATTENTION → FINISHED`; el último paso establece `COMPLETED` y conserva el slot `RESERVED`.

## Fuente

`docs/auditoria/TO-BE.md` T22–T26 y D12–D13; SRS 0.1; RB-016–020; ADR-007.

## Actores

Recepción (`RECEPTIONIST`), profesional titular (`PROFESSIONAL`) y sistema HospitalPlatform.

## Actividades

T22 consulta operación filtrada; T23/T24 recepción registra CHECK_IN/WAITING; T25/T26 profesional vinculado registra IN_ATTENTION/FINISHED. T27 representa auditoría transaccional para cada transición, sin tarea manual repetida.

## Decisiones

D12 exige `CONFIRMED`; D13 comprueba rol, propiedad y transición exacta antes de **cada** T23–T26. La figura comprime esa verificación en un gateway inicial por legibilidad; el control se reevalúa en todas las transiciones. Repetir etapa actual es idempotente; salto/retroceso se rechaza.

## Requisitos relacionados

RF-026–030, RF-032; RNF-001–002, RNF-010–011.

## Reglas de negocio relacionadas

RB-002, RB-011, RB-016–020.

## Exclusiones

Sin ADMISSION, TRIAGE, CALLED ni NO_SHOW como estados de software; sin dato clínico.

## Pendientes de validación

Responsabilidades institucionales reales y modalidad operativa de check-in; la asignación de software sigue ADR-007.

## Archivo fuente del diagrama

`BPMN-06-FLUJO-CONSULTA.mmd`.
