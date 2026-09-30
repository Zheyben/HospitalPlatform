# IDEF0 A0 — Descomposición de HospitalPlatform

## Objetivo

Descomponer A-0 en seis funciones cohesionadas y mantener sus flechas de frontera.

## Alcance

Modelo funcional TO-BE; no expresa el orden temporal rígido de BPMN. Las flechas entre funciones son intercambios de información/estado. La figura Mermaid es una vista auxiliar; las tablas de funciones e ICOM son la especificación replicable.

## Fuente

`docs/auditoria/TO-BE.md` T01–T29; SRS 0.1; RB-001–024; `IDEF0-A-0.md`; ADR-001/005–008.

## Actores

M1 aporta paciente, visitante y personal autorizado; M2 aporta la plataforma y sus componentes. No se trasladan actores a entradas.

## Actividades

| Nodo | Función | T asignadas | Entrada y salida interna principal |
|---|---|---|---|
| A1 | Gestionar identidad y acceso | T03–T04 | I1/I3 → cuenta, sesión y permiso para A2–A6 |
| A2 | Gestionar información pública, catálogo y agenda | T01–T02, T05–T08 | contenido/agenda → O5 y slot consultable para A3/A4 |
| A3 | Gestionar ciclo de cita y comunicaciones | T11–T18 | I2 y slot → O2; slot liberado → A4; confirmada → A5 |
| A4 | Gestionar espera, oferta y revisión manual de prioridad | T09–T10, T19–T21 | I3 y slot liberado → O3; aceptación válida → A3 |
| A5 | Gestionar flujo operativo | T22–T26 | cita CONFIRMED → O4 y cita COMPLETED; slot RESERVED |
| A6 | Asegurar trazabilidad, indicadores y accesibilidad | T27–T29 | eventos de A1–A5 → O4; preferencias → O1 |

T13 está en A3 porque un aviso deriva de un cambio de cita, pero su fallo no revierte la transacción persistida. T27 es mecanismo transversal de persistencia crítica y no una actividad manual posterior. T29 afecta UX de todas las superficies, aunque su función responsable sea A6.

## Decisiones

| Función | Decisiones |
|---|---|
| A1 | D02 |
| A2 | D01, D03 |
| A3 | D04, D06–D09 |
| A4 | D05, D10–D11; D06 al adjudicar oferta |
| A5 | D12–D13 |
| A6 | Aplica controles C1–C4; no añade gateway de negocio |

## Requisitos relacionados

RF-001–033; RNF-001–013 se conservan como criterios/controles, con verificaciones de QA fuera del recorrido.

## Reglas de negocio relacionadas

RB-001–024 distribuidas según `docs/auditoria/TO-BE.md` §3.

## Exclusiones

No se agrega función clínica ni política institucional supuesta. A6 no altera por sí misma una cita ni convierte una preferencia de interfaz en etapa clínica.

## Pendientes de validación

C3 y los detalles de espera/prioridad, plazos y roles finales; A0 no les asigna valores.

## Archivo fuente del diagrama

`IDEF0-A0.mmd`; el balance de fronteras se prueba en `IDEF0-TRAZABILIDAD.md`.

## Intercambios internos

| Origen | Destino | Flecha interna | Regla |
|---|---|---|---|
| A1 | A2–A6 | Identidad y permiso | RB-002 |
| A2 | A3/A4 | Especialidad activa y slot derivado de agenda | RB-003–004 |
| A3 | A4 | Slot liberado elegible | RB-009/011–013 |
| A4 | A3 | Oferta aceptada con solicitud de adjudicación | RB-013–014; A3/A4 revalidan slot |
| A3 | A5 | Cita CONFIRMED | RB-016–017 |
| A1–A5 | A6 | Eventos y estados auditables | RB-020/024 |

Estos intercambios no son flechas nuevas de la frontera A-0. La disponibilidad interna tampoco se convierte en entrada externa.
