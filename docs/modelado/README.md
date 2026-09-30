# Modelado textual editable — HospitalPlatform

**Estado:** TO-BE propuesto, 23/09/2026. Los diagramas `.mmd` son fuentes editables de *flujo de procesos* con convenciones BPMN. Mermaid no expresa todos los tipos de tarea, eventos, pools/lanes, intercambio de mensajes ni semántica de token de BPMN 2.0; estos archivos no son BPMN XML ejecutable ni modelos institucionales aprobados. Las etiquetas `User Task`, `Service Task`, `Start`, `End` y `Dxx` indican la semántica prevista para una futura transcripción BPMN 2.0. Las líneas punteadas son referencias o reglas transversales, no flujo de secuencia. Los eventos de aviso/expiración son conceptuales; no hay timer de plazo fijo mientras la política esté [PENDIENTE_VALIDACION].

## Fuentes y autoridad

`docs/requisitos/SRS-HOSPITALPLATFORM.md` (RF-001–033, RNF-001–013); `docs/requisitos/REGLAS_NEGOCIO.md` (RB-001–024); `docs/requisitos/EQUIVALENCIAS_REQUISITOS.md`; **solo** «Matriz canónica SRS 0.1» de `docs/auditoria/MATRIZ_TRAZABILIDAD_BASE.md`; `docs/auditoria/AS-IS.md`; `docs/auditoria/TO-BE.md`; `docs/00-PROJECT-ROADMAP.md`; ADR aceptadas, especialmente 001, 005–008; `docs/03-APP-FLOW.md`. Los ID T y D proceden del TO-BE. Los documentos anteriores con numeración de dos dígitos no son autoridad de requisitos en estos modelos.

## Índice de figuras

| Figura | Función | Fuente editable |
|---|---|---|
| BPMN-00 | Macroproceso de cita y consulta externa | `bpmn/BPMN-00-MACROPROCESO.mmd` |
| BPMN-01 | Identidad y acceso | `bpmn/BPMN-01-IDENTIDAD.mmd` |
| BPMN-02 | Consulta, disponibilidad y reserva | `bpmn/BPMN-02-RESERVA.mmd` |
| BPMN-03 | Confirmación, cancelación y reprogramación | `bpmn/BPMN-03-GESTION-CITA.mmd` |
| BPMN-04 | Lista de espera y recuperación de cupo | `bpmn/BPMN-04-ESPERA.mmd` |
| BPMN-05 | Priorización ambulatoria manual | `bpmn/BPMN-05-PRIORIDAD.mmd` |
| BPMN-06 | Flujo operativo de consulta externa | `bpmn/BPMN-06-FLUJO-CONSULTA.mmd` |
| BPMN-07 | Administración, auditoría e indicadores | `bpmn/BPMN-07-ADMINISTRACION.mmd` |
| IDEF0 A-0 | Contexto e ICOM | `idef0/IDEF0-A-0.md` |
| IDEF0 A0 | Descomposición de seis funciones | `idef0/IDEF0-A0.md` |
| AS-IS | Límites de modelado, sin figura de secuencia | `as-is/AS-IS-MODELING-LIMITS.md` |

La división conserva el macroproceso legible y separa transacciones distintas. BPMN-07 agrupa preparación administrativa y controles transversales, pero no afirma que auditoría o accesibilidad sean etapas de una cita. El modelo tiene un pool conceptual «HospitalPlatform / Consulta externa programada» con responsabilidades de Paciente, HospitalPlatform, Recepción, Profesional y Administración/revisor. Mermaid muestra los responsables en etiquetas y documentación; no impone lanes BPMN. Portal público, reservas web, web operativa y app móvil comparten dominio/backend.

## Trazabilidad de tareas BPMN

Cada fila identifica una tarea o actividad modelada. Resultado, precondición, entidades y RNF específicos se detallan en `docs/auditoria/TO-BE.md` §3; esta tabla complementa la figura sin saturarla. `T27` y `T29` son transversales, no pasos obligatorios de cada instancia.

| Elemento BPMN | Txx | RF | RB | Actor | Observación |
|---|---|---|---|---|---|
| BPMN-07 contenido | T01 | RF-009 | RB-002, RB-022 | Admin/editor | Publicar o rechazar fuente |
| BPMN-02 portal | T02 | RF-010 | RB-003, RB-022–023 | Visitante | Información pública y entrada |
| BPMN-01 registro/perfil | T03 | RF-001, RF-005 | RB-001–002 | Paciente | Cuenta y perfil sin duplicado |
| BPMN-01 acceso; BPMN-02 referencia | T04 | RF-002–004 | RB-002 | Usuario/sistema | Sesión o rechazo por rol y propiedad |
| BPMN-07 catálogo | T05 | RF-007–008 | RB-002–003 | Admin | Profesionales/especialidades vigentes |
| BPMN-07 agenda | T06 | RF-011 | RB-004–005 | Admin | Slots discretos válidos |
| BPMN-02 especialidades | T07 | RF-008, RF-010 | RB-003, RB-022 | Visitante/paciente | Selección activa |
| BPMN-02 disponibilidad | T08 | RF-012 | RB-004 | Paciente/recepción | Cupos o sin disponibilidad |
| BPMN-05 solicitud | T09 | RF-021 | RB-015 | Paciente | Solicitud, sin prioridad automática |
| BPMN-05 revisión | T10 | RF-022, RF-032 | RB-002, RB-015 | Revisor autorizado | Decisión manual motivada |
| BPMN-02 reserva | T11 | RF-013–014 | RB-005–006 | Paciente/recepción/sistema | Una cita SCHEDULED o conflicto |
| BPMN-02 constancia | T12 | RF-015 | RB-006–007 | Sistema | Constancia sin CONFIRMED |
| BPMN-03 aviso; BPMN-04 referencia | T13 | RF-023 | RB-008, RB-021 | Sistema | Fallo no revierte cita; envío no prueba lectura |
| BPMN-03 consulta propia | T14 | RF-006 | RB-002, RB-016–017 | Paciente | Citas propias |
| BPMN-03 confirmación | T15 | RF-016 | RB-007–008, RB-016 | Paciente/autorizado | CONFIRMED si elegible |
| BPMN-03 cancelación | T16 | RF-017 | RB-002, RB-009, RB-016 | Paciente/recepción | CANCELLED con historial |
| BPMN-03 reprogramación | T17 | RF-014, RF-019 | RB-005, RB-010–011 | Paciente/recepción | Sucesora SCHEDULED; fallo conserva original |
| BPMN-03 liberación | T18 | RF-018 | RB-009, RB-011 | Sistema | Cupo anterior liberado si elegible |
| BPMN-04 espera | T19 | RF-020 | RB-012 | Paciente | WaitlistEntry sin cita |
| BPMN-04 oferta | T20 | RF-024 | RB-011–013 | Sistema | SlotOffer PENDING, sin adjudicación |
| BPMN-04 respuesta | T21 | RF-014, RF-025 | RB-005, RB-013–014 | Paciente/sistema | Aceptación transaccional o rechazo/expiración |
| BPMN-06 agenda | T22 | RF-030 | RB-002 | Recepción/profesional | Vista filtrada |
| BPMN-06 llegada | T23 | RF-026 | RB-016–018, RB-020 | Recepción | CHECK_IN |
| BPMN-06 espera | T24 | RF-027 | RB-017–018, RB-020 | Recepción | WAITING |
| BPMN-06 inicio | T25 | RF-028 | RB-017–018, RB-020 | Profesional titular | IN_ATTENTION |
| BPMN-06 fin | T26 | RF-029 | RB-011, RB-017–020 | Profesional titular | FINISHED y COMPLETED; slot RESERVED |
| BPMN-07 transversal | T27 | RF-032 | RB-020 | Sistema | AuditLog en misma transacción crítica |
| BPMN-07 indicadores | T28 | RF-031 | RB-002, RB-024 | Admin/auditor | Métrica trazable o no medible |
| BPMN-07 transversal | T29 | RF-033 | RB-023 | Usuario interfaz | Preferencia; estado legible sin color |

## Trazabilidad de gateways

| Decisión | Figura detallada | RF/RB |
|---|---|---|
| D01 | BPMN-07 | RF-009 / RB-022 |
| D02 | BPMN-01 y BPMN-02 | RF-002–004 / RB-002 |
| D03 | BPMN-02 | RF-008, RF-012 / RB-003–004 |
| D04 | BPMN-00 y BPMN-02 | RF-012–013, RF-020 / RB-004, RB-012 |
| D05 | BPMN-05 | RF-021–022 / RB-015 |
| D06 | BPMN-02, BPMN-03 y BPMN-04 | RF-013–014 / RB-005 |
| D07 | BPMN-03 | RF-016–019 / RB-007–009, RB-016 |
| D08 | BPMN-03 | RF-017, RF-019 / RB-009–010 |
| D09 | BPMN-03 | RF-018 / RB-009, RB-011 |
| D10 | BPMN-04 | RF-020, RF-024 / RB-012–013 |
| D11 | BPMN-04 | RF-014, RF-025 / RB-013–014 |
| D12 | BPMN-06 | RF-026 / RB-016–017 |
| D13 | BPMN-06 | RF-026–029 / RB-017–020 |

## Límites comunes

Las políticas de plazo de confirmación, cancelación, reprogramación, selección/expiración de oferta y prioridad requieren validación. No se fijó temporizador T-7. `AppointmentStatus` tiene SCHEDULED, CONFIRMED, CANCELLED, RESCHEDULED y COMPLETED; `FlowStage` tiene `null → CHECK_IN → WAITING → IN_ATTENTION → FINISHED`. `NO_SHOW` no es estado aprobado. Las metas y los indicadores son de ensayo con datos sintéticos, no métricas observadas del hospital. Ninguna figura AS-IS inventa secuencia.
