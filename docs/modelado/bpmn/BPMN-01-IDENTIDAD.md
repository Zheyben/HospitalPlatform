# BPMN-01 — Gestión de identidad y acceso

## Objetivo

Describir alta, perfil, sesión y autorización previa a operaciones protegidas.

## Alcance

Reservas web, app móvil y web operativa; el portal público se consulta sin cuenta. El acceso se comprueba por rol y propiedad del recurso.

## Fuente

`docs/auditoria/TO-BE.md` T03–T04 y D02; SRS 0.1; RB-001–002; ADR-003–005.

## Actores

Paciente, personal autorizado y sistema HospitalPlatform.

## Actividades

T03: registrar/actualizar perfil, cuenta única o error. T04: autenticar, renovar/cerrar sesión y autorizar, acceso o rechazo. La flecha de T04 a sí misma ilustra nuevas operaciones de sesión, no un bucle automático.

## Decisiones

D02: sesión y permiso válidos; denegación o paso a autenticación cuando corresponda.

## Requisitos relacionados

RF-001–005; RNF-001–002.

## Reglas de negocio relacionadas

RB-001–002.

## Exclusiones

No se modela identidad institucional real ni acceso a datos clínicos.

## Pendientes de validación

Política de alta asistida y mapeo de roles institucionales finales.

## Archivo fuente del diagrama

`BPMN-01-IDENTIDAD.mmd`; la tabla de trazabilidad está en `../README.md`.
