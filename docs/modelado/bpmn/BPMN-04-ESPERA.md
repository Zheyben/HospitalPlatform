# BPMN-04 — Lista de espera y recuperación de cupo

## Objetivo

Hacer explícito que entrada, oferta y cita son objetos distintos y que adjudicar exige aceptación y revalidación.

## Alcance

Se inicia por falta de cupo y por una liberación posterior. `WaitlistEntry` se mantiene separada; `SlotOffer=PENDING` puede terminar `ACCEPTED`, `REJECTED` o `EXPIRED`. Solo aceptación vigente más transacción crea cita.

## Fuente

`docs/auditoria/TO-BE.md` T19–T21 y D10–D11; SRS 0.1; RB-005/011–014; roadmap §§11.2, 18.2.

## Actores

Paciente y sistema HospitalPlatform; personal autorizado si la política de selección lo requiere.

## Actividades

T19 crea/consulta/cancela WaitlistEntry; T20 determina elegibilidad y crea SlotOffer, sin cita; T21 registra respuesta o expiración, revalida slot y crea una cita solo ante aceptación válida. T13 puede avisar, sin presunción de lectura.

## Decisiones

D10 existencia de entrada elegible; D11 respuesta vigente; D06 disponibilidad final del slot. Rechazo y expiración no producen cita.

## Requisitos relacionados

RF-014, RF-020, RF-023–025; RNF-001–002, RNF-010–011.

## Reglas de negocio relacionadas

RB-005, RB-011–014, RB-021.

## Exclusiones

No hay reasignación automática, cita implícita, timer fijo de expiración ni lectura inferida de un aviso.

## Pendientes de validación

Ingreso/elegibilidad, orden de candidatos, canal, expiración, rechazo y siguiente oferta.

## Archivo fuente del diagrama

`BPMN-04-ESPERA.mmd`. La línea punteada desde la entrada hacia D10 es dependencia de datos, no una secuencia que garantice liberación.
