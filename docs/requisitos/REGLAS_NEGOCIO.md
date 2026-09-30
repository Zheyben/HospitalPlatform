# Reglas de negocio consolidadas de HospitalPlatform

**Estado:** propuesta documental, 23/09/2026. `RB-XXX` son identificadores nuevos; `RN-XXX` del PRD permanece como antecedente. [CONFIRMADO] significa decisión documental vigente, no proceso institucional observado. Las políticas dependientes del Hospital de Huaycán se marcan [PENDIENTE_VALIDACION]. Fuentes principales: `docs/00-PROJECT-ROADMAP.md`, ADR-003 a ADR-008, `docs/03-APP-FLOW.md`, `docs/01-PRD.md` §7 y `docs/research/recursos/CAPITULO-III.md` §1.1.

| Regla | Descripción | RF canónicos relacionados | Fuente | Estado |
|---|---|---|---|---|
| RB-001 | Solo datos sintéticos en desarrollo/demostración; ningún flujo supone acceso a historias reales o integración institucional oficial. | Todos | Roadmap §§1, 6.2, 16; PRD §1 | [CONFIRMADO] alcance académico |
| RB-002 | El usuario actúa según rol, permiso y propiedad del recurso; `SYSTEM` es actor técnico, no personal humano. | 002–006, 013, 016–017, 026–032 | Roadmap §19; ADR-005; APP-FLOW §15; PRD RN-011 | [CONFIRMADO] diseño; roles finales [PENDIENTE_VALIDACION] |
| RB-003 | Una especialidad inactiva no se ofrece para nueva reserva. | 008, 010–013 | PRD RN-010; Cap. III RF-06 | [CONFIRMADO] diseño |
| RB-004 | La disponibilidad se deriva de agenda y slots discretos, no de valores fijos en frontend. | 011–014 | Roadmap §§12.3, 14.2.2; ADR-006 | [CONFIRMADO] diseño |
| RB-005 | Un slot admite como máximo una cita activa; si compiten solicitudes, una obtiene la reserva y las otras reciben conflicto controlado. | 013–014, 025 | Roadmap §12.3; PRD RN-001/002; Cap. III RF-10; V3 SQL | [CONFIRMADO] regla; prueba de ejecución pendiente |
| RB-006 | Toda cita se vincula a paciente, profesional, especialidad derivada y slot válido; comienza `SCHEDULED` con `flowStage=null`. | 013–015 | PRD RN-007; APP-FLOW §§5, 11; API §12 | [CONFIRMADO] diseño |
| RB-007 | La constancia de reserva no cambia por sí sola `AppointmentStatus` a `CONFIRMED`. | 015–016 | Cap. III RF-11; APP-FLOW §5 | [CONFIRMADO] diseño |
| RB-008 | Confirmación de asistencia cambia una cita elegible a `CONFIRMED`; reglas temporales y T-7 son configurables y requieren validación. | 016, 023 | Roadmap §§6.1, 18.1; ADR-007 | [CONFIRMADO] transición; [PENDIENTE_VALIDACION] calendario |
| RB-009 | Cancelación autorizada conserva actor, fecha e historial; la política de plazos y elegibilidad de liberación requiere validación. | 017–018, 032 | PRD RN-004/009; roadmap §18; Cap. III RF-13 | [CONFIRMADO] diseño; [PENDIENTE_VALIDACION] plazo |
| RB-010 | Reprogramación crea vínculo anterior/sucesora y conserva la cita original si falla el nuevo cupo. | 019, 032 | ADR-007; Cap. III RF-12; APP-FLOW §6 | [CONFIRMADO] diseño |
| RB-011 | La liberación de un slot por cancelación/reprogramación es distinta de adjudicarlo a otra persona; una cita completada no lo libera. | 018–019, 024–025, 029 | Roadmap §§11.1, 18.2; ADR-007 | [CONFIRMADO] diseño |
| RB-012 | La lista de espera es separada de la cita; ingreso, cancelación y elegibilidad dependen de reglas aprobadas. | 020, 024 | Roadmap §11.2; PRD RN-006 | [CONFIRMADO] separación; [PENDIENTE_VALIDACION] elegibilidad |
| RB-013 | Un cupo liberado genera oferta según política aprobada; no se reasigna sin aceptación del paciente; rechazo o expiración permite continuar conforme a política. | 024–025 | Roadmap §§11.2, 18.2; Cap. III RF-21 | [CONFIRMADO] consentimiento; [PENDIENTE_VALIDACION] selección/expiración |
| RB-014 | Aceptar una oferta exige transacción que impida doble adjudicación del mismo slot. | 014, 025 | Roadmap §§11.2, 12.3, 18.2 | [CONFIRMADO] diseño |
| RB-015 | Solicitar prioridad no la concede; solo personal de salud institucionalmente autorizado puede aprobar/rechazar y debe auditar motivo/actor. No hay diagnóstico ni prioridad de emergencia automática. | 021–022, 032 | Roadmap §11.3; ADR-008 | [CONFIRMADO] principio; [PENDIENTE_VALIDACION] criterio/rol final |
| RB-016 | `AppointmentStatus` solo admite `SCHEDULED`, `CONFIRMED`, `CANCELLED`, `RESCHEDULED`, `COMPLETED` en el modelo aprobado. `NO_SHOW` no es estado actual. | 013, 016–019, 026–029 | Roadmap §11.1; ADR-007; APP-FLOW §3 | [CONFIRMADO] |
| RB-017 | `FlowStage` solo admite `null → CHECK_IN → WAITING → IN_ATTENTION → FINISHED`; inicia únicamente con cita `CONFIRMED`. | 026–029 | Roadmap §11.4; ADR-007; PRD RN-008 | [CONFIRMADO] |
| RB-018 | `RECEPTIONIST` ejecuta `CHECK_IN` y `WAITING`; `PROFESSIONAL` ejecuta inicio/fin en su cita; paciente/admin no alteran flujo. Repetición de etapa actual es idempotente; salto/retroceso se rechaza. | 026–029 | ADR-007; APP-FLOW §15 | [CONFIRMADO] incremento 5.11 |
| RB-019 | Al registrar `FINISHED`, se registra `COMPLETED` en la cita y el slot queda `RESERVED` por capacidad consumida. | 029 | Roadmap §11.4; ADR-007 | [CONFIRMADO] |
| RB-020 | Transiciones críticas y auditoría comparten transacción; un fallo de auditoría revierte la transición y repetirla no duplica evento. | 026–029, 032 | ADR-007; APP-FLOW §13; PRD RN-005 | [CONFIRMADO] diseño |
| RB-021 | Fallo de notificación no revierte una cita correctamente cambiada; entrega no prueba lectura; avisos no exponen datos clínicos sensibles. | 023 | Roadmap §18.4 | [CONFIRMADO] diseño |
| RB-022 | El contenido institucional público requiere fuente verificable/autorización y no presenta el proyecto académico como portal oficial. | 009–010 | Roadmap §14.2.1; PRD RF-003/005 | [CONFIRMADO] límite; [PENDIENTE_VALIDACION] contenido específico |
| RB-023 | Estados y alertas no se comunican solo con color; preferencias de tema y visión de color son apoyo, sin sustituir contraste/semántica accesible. | 033 | Roadmap §14.3.1 | [CONFIRMADO] criterio de diseño |
| RB-024 | Métricas de inasistencia, recuperación y espera solo se calculan cuando sus eventos, periodos y denominadores están definidos; ninguna cifra académica es línea base hospitalaria. | 031 | Roadmap §§4.2, 7.3; Cap. III §1.4.3 | [CONFIRMADO] método; datos [PENDIENTE_VALIDACION] |

## Reglas deliberadamente sin valor cerrado

- `NO_SHOW`: el roadmap lo contempla como capacidad del MVP, pero ADR-007 lo excluye de FASE 5.11 y no existe valor `AppointmentStatus` aprobado. Se requiere decisión posterior de evento/estado, plazo, autoridad y efecto sobre slot antes de convertirlo en RF ejecutable (roadmap §§6.1, 11.1).
- Política de cancelación, reprogramación, T-7, selección y expiración de ofertas, prioridad ambulatoria y modalidad de check-in: [PENDIENTE_VALIDACION] institucional y, cuando proceda, docente (`docs/00` §§6.1, 11, 18, 29).
- Triaje institucional puede pertenecer al AS-IS, pero no se añade a `FlowStage` ni se registran signos vitales/diagnósticos en el MVP aprobado (`docs/00` §§6.2, 7.1, 11.4; ADR-007).
