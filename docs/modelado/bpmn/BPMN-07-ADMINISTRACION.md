# BPMN-07 — Administración, auditoría e indicadores

## Objetivo

Separar preparación administrativa y controles transversales del recorrido de una cita.

## Alcance

Publicación, catálogo, agenda, AuditLog, indicadores de ensayo y preferencias visuales en las cuatro superficies. Las entradas separadas del diagrama son instancias independientes, no un flujo lineal.

## Fuente

`docs/auditoria/TO-BE.md` T01, T05–T06, T27–T29 y D01; SRS 0.1; RB-002–005/020/022–024; ADR-001/005/006/007.

## Actores

Admin/editor, auditor autorizado, usuario de interfaz y sistema HospitalPlatform.

## Actividades

T01 publica solo contenido autorizado; T05 mantiene profesionales/especialidades; T06 configura slots; T27 guarda AuditLog junto a transición crítica o la revierte; T28 calcula indicador con cohorte/denominador o declara «no medible»; T29 aplica preferencia accesible, sin depender solo del color.

## Decisiones

D01: autorizar contenido antes de publicar. Los permisos D02 y la validez de agenda/slots se comprueban por sus RF/RB en cada operación, aunque el esquema administrativo los comprime.

## Requisitos relacionados

RF-007–009, RF-011, RF-031–033; RNF-002, RNF-006, RNF-008, RNF-010–013.

## Reglas de negocio relacionadas

RB-002–005, RB-020, RB-022–024.

## Exclusiones

No se presentan indicadores académicos como línea base hospitalaria, ni auditoría como tarea humana posterior a cada acción, ni accesibilidad como etapa clínica.

## Pendientes de validación

Fuente/autorización del contenido, indicadores y denominadores, preferencias de interfaz, política de retención de auditoría.

## Archivo fuente del diagrama

`BPMN-07-ADMINISTRACION.mmd`.
