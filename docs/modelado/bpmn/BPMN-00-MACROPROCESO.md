# BPMN-00 — HospitalPlatform — Gestión digital de citas y consulta externa programada

## Objetivo

Mostrar el recorrido TO-BE y sus salidas alternativas sin imponer que todas las actividades ocurran en una cita.

## Alcance

Pool conceptual «HospitalPlatform / Consulta externa programada». Los subprocesos BPMN-01–07 desarrollan la figura. Incluye consulta, reserva, gestión de cita, espera/oferta, prioridad opcional y flujo operativo; administración y auditoría son habilitadores.

## Fuente

`docs/auditoria/TO-BE.md` §§1–4; SRS 0.1; RB; roadmap; ADR-001/007/008. Mermaid representa flujo, no BPMN 2.0 formal ni lanes nativos.

## Actores

Paciente/visitante; sistema HospitalPlatform; recepción; profesional; administración/revisor autorizado.

## Actividades

T02, T07–T08, T12 y referencias a los siete subprocesos. La cobertura de T01–T29 está en `../README.md`.

## Decisiones

D04 se muestra; D01–D03 y D05–D13 se desarrollan en las figuras hijas.

## Requisitos relacionados

RF-001–033; RNF-001–013 según función y controles. Las verificaciones de disponibilidad, escalabilidad y recuperación no son tareas del paciente.

## Reglas de negocio relacionadas

RB-001–024, distribuidas por subproceso.

## Exclusiones

No representa historia clínica, triaje clínico, diagnóstico, inasistencia como estado ni una ruta AS-IS real.

## Pendientes de validación

Plazos, selección/expiración de ofertas, prioridad, contenido público y roles institucionales finales.

## Archivo fuente del diagrama

`BPMN-00-MACROPROCESO.mmd`. La salida «cita SCHEDULED pendiente» cierra esta instancia de observación, sin cancelar la cita; el proceso puede retomarse ante una acción posterior.
