# BPMN-05 — Priorización ambulatoria manual

## Objetivo

Representar solicitud, revisión humana y decisión auditable.

## Alcance

Rama opcional de reserva estándar, activable solo con política institucional aprobada.

## Fuente

`docs/auditoria/TO-BE.md` T09–T10 y D05; SRS 0.1; RB-015; ADR-008.

## Actores

Paciente, revisor de salud autorizado y sistema que registra auditoría.

## Actividades

T09 crea solicitud pendiente, sin conceder prioridad. T10 revisa y aprueba/rechaza manualmente, con motivo y actor. T27 registra el cambio crítico.

## Decisiones

D05 cubre tanto entrada bajo política aprobada como resultado manual. Las dos preguntas del diagrama son refinamientos de la misma decisión de negocio, sin política clínica adicional.

## Requisitos relacionados

RF-021–022, RF-032; RNF-001–002, RNF-010.

## Reglas de negocio relacionadas

RB-002, RB-015, RB-020.

## Exclusiones

Sin diagnóstico, triaje automático, prioridad de emergencia ni aprobación algorítmica.

## Pendientes de validación

Criterios, evidencias admisibles y rol institucional definitivo del revisor.

## Archivo fuente del diagrama

`BPMN-05-PRIORIDAD.mmd`.
