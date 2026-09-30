# BPMN-03 — Confirmación, cancelación y reprogramación

## Objetivo

Mostrar acciones alternativas sobre una cita y sus efectos transaccionales.

## Alcance

Una cita `SCHEDULED` puede confirmarse, permanecer pendiente, cancelarse o reprogramarse según permiso y política. El cupo anterior se libera solo si es elegible.

## Fuente

`docs/auditoria/TO-BE.md` T13–T18, D07–D09 y D06; SRS 0.1; RB-007–011/016/020–021; ADR-007.

## Actores

Paciente, recepción autorizada y sistema HospitalPlatform.

## Actividades

T14 consulta propia; T15 deja `CONFIRMED`; T16 deja `CANCELLED`; T17 cambia atómicamente a anterior `RESCHEDULED` y sucesora `SCHEDULED`, o conserva original; T18 libera cupo elegible; T13 intenta aviso, cuyo fallo no revierte la cita. T27 se aplica a cambios críticos.

## Decisiones

D07 confirmar; D08 cancelar/reprogramar; D06 nuevo slot adquirible; D09 liberación permitida. La consulta de permiso D02 está implícita como precondición de cada acción protegida.

## Requisitos relacionados

RF-006, RF-014, RF-016–019, RF-023, RF-032; RNF-001–002, RNF-010–011, RNF-013.

## Reglas de negocio relacionadas

RB-002, RB-005, RB-007–011, RB-016, RB-020–021.

## Exclusiones

Sin `NO_SHOW` como estado ni plazos temporales fijos. Una cita `COMPLETED` no libera su slot.

## Pendientes de validación

Ventanas de confirmación, cancelación, reprogramación y elegibilidad de liberación.

## Archivo fuente del diagrama

`BPMN-03-GESTION-CITA.mmd`; T13 se dibuja como intento independiente de aviso posterior al cambio persistido.
