# TO-BE — HospitalPlatform, consulta externa programada

**Versión:** 0.2, 23/09/2026. **Estado:** [PROPUESTO] como proceso digital del MVP académico; políticas institucionales marcadas [PENDIENTE_VALIDACION]. Referencias activas: `docs/requisitos/SRS-HOSPITALPLATFORM.md` (RF-001–033, RNF-001–013), `docs/requisitos/REGLAS_NEGOCIO.md` (RB-001–024), `docs/00-PROJECT-ROADMAP.md`, ADR aceptadas y `docs/03-APP-FLOW.md`. La SRS es propuesta documental, no aprobación institucional ni evidencia de implementación.

## 1. Alcance y superficies

**A. Portal público:** ciudadanía/visitante consulta contenido verificable y llega al portal de reservas. **B. Portal web de reservas:** paciente consulta cupos y gestiona citas, prioridad y espera. **C. Web operativa:** administrador, recepción, profesional y revisor autorizado realizan las acciones permitidas. **D. App móvil del paciente:** comparte los flujos de autoservicio, ofertas y avisos. Las cuatro superficies pertenecen a **un solo HospitalPlatform**, con el mismo backend modular, reglas y API; no son cuatro sistemas independientes (`docs/00` §§1, 6.1, 9, 14; ADR-001). Web y app son parte del MVP documental aunque no se observaron implementadas en el repositorio al corte de la auditoría.

Los datos son sintéticos; el portal no se presenta como sitio oficial. Se excluyen historia clínica, diagnóstico, prescripción, triaje clínico digital, farmacia/laboratorio integrales, emergencia e integraciones oficiales (`docs/00` §§6.2–6.3, 16; SRS §2).

## 2. Macroflujo con ramas

1. **Preparación administrativa, no ejecutada por cada paciente:** administración mantiene contenido, especialidades, profesionales, agendas y slots (T01, T05–T06). Contenido y cupos se publican solo con validaciones pertinentes.
2. **Entrada:** visitante consulta portal público (T02). Para gestionar cita, paciente se registra si hace falta, inicia/renueva sesión y mantiene perfil (T03–T04), desde reservas web o móvil. Puede consultar sus citas (T14).
3. **Selección:** el paciente consulta catálogo y disponibilidad (T07–T08). Si solicita prioridad ambulatoria, abre revisión manual (T09–T10); esta rama no concede urgencia ni bloquea necesariamente una reserva estándar. Criterio/rol final [PENDIENTE_VALIDACION].
4. **Con cupo disponible:** se revalida y reserva transaccionalmente (T11), se emite constancia de `SCHEDULED` (T12) y, por regla configurada, avisos/recordatorios (T13). Constancia no equivale a `CONFIRMED`. El paciente confirma asistencia (T15) o, cuando la política lo permita, cancela (T16) o reprograma (T17). Cancelación/reprogramación elegible libera el cupo antiguo (T18). Estas son **alternativas**; no se recorren todas en una misma cita.
5. **Sin cupo o ante liberación:** el paciente puede ingresar en lista de espera (T19). Si se libera un slot, el sistema busca entradas elegibles y crea `SlotOffer=PENDING` (T20), sin crear cita. La persona acepta/rechaza, o la oferta expira (T21). Solo la aceptación válida revalida el slot y crea una cita; una carrera concede el cupo a una persona. Sin entrada elegible, el cupo permanece disponible según política. Rechazo/expiración sigue la política aún por validar.
6. **Con cita `CONFIRMED`:** recepción consulta su operación (T22) y registra `CHECK_IN` (T23), después `WAITING` (T24). Profesional vinculado registra `IN_ATTENTION` (T25) y `FINISHED` (T26); esa última transición pone `AppointmentStatus=COMPLETED` y el slot sigue `RESERVED` como capacidad consumida. El paciente solo observa el avance, no cambia `FlowStage`.
7. **Transversal:** autorización (T04), auditoría (T27), indicadores (T28) y preferencias visuales (T29) acompañan los pasos aplicables. Auditoría y métricas no son pasos clínicos; los indicadores de ensayo no son resultados del hospital.

`AppointmentStatus = SCHEDULED | CONFIRMED | CANCELLED | RESCHEDULED | COMPLETED`. `FlowStage = null → CHECK_IN → WAITING → IN_ATTENTION → FINISHED` (ADR-007; SRS §2; RB-016–020). `NO_SHOW` no es estado actual; admisión, triaje y llamado tampoco son valores de `FlowStage`. Su lugar eventual exige levantamiento o decisión posterior. La cita sucesora de una reprogramación comienza `SCHEDULED`/`null`; una completada no libera su slot.

## 3. Matriz de actividades TO-BE

Los IDs T01–T29 son estables para el siguiente modelado. “Entidades” indica conceptos de diseño; `SlotOffer`, contenido y preferencias no deben interpretarse como tablas implementadas. Cada fila dispone de RF y RB activos. Un RNF se consigna cuando aporta un criterio específico, además de los transversales RNF-001/002/006/010.

| ID proceso | Actividad | Actor | Superficie | Precondición | RF | RNF | RB | Entidades | Resultado |
|---|---|---|---|---|---|---|---|---|---|
| T01 | Mantener contenido institucional autorizado | Admin/editor autorizado | Web operativa | Fuente y permiso aprobados | RF-009 | RNF-002, RNF-010 | RB-002, RB-022 | Contenido, Specialty | Versión publicable o rechazo |
| T02 | Consultar portal público y acceder a reservas | Visitante | Portal público | Contenido publicado | RF-010 | RNF-005–007 | RB-003, RB-022–023 | Contenido, Specialty | Información y acceso a reservas |
| T03 | Registrar paciente y mantener perfil | Paciente | Reservas web/app móvil | Datos válidos; identidad única | RF-001, RF-005 | RNF-001–002 | RB-001–002 | User, Patient | Cuenta/perfil o error sin duplicado |
| T04 | Autenticar, renovar/cerrar sesión y aplicar permisos | Paciente/personal; sistema | Reservas web/app/web operativa | Cuenta habilitada, token válido cuando aplique | RF-002–004 | RNF-001–002 | RB-002 | User, Role, RefreshToken | Sesión/permiso o rechazo |
| T05 | Mantener profesionales y especialidades | Admin | Web operativa | Permiso; datos verificables | RF-007–008 | RNF-008 | RB-002–003 | Professional, Specialty | Catálogo vigente |
| T06 | Configurar agenda y slots discretos | Admin | Web operativa | Profesional y especialidad habilitados | RF-011 | RNF-011 | RB-004–005 | Schedule, AvailabilitySlot | Cupos válidos, sin duplicidad |
| T07 | Consultar especialidades disponibles | Visitante/paciente | Portal público/reservas web/app | Catálogo publicado | RF-008, RF-010 | RNF-006–007 | RB-003, RB-022 | Specialty | Selección activa; no ofrece inactivas |
| T08 | Consultar disponibilidad reservable | Paciente/recepción | Reservas web/app/web operativa | Agenda habilitada y filtros permitidos | RF-012 | RNF-003, RNF-007 | RB-004 | Schedule, AvailabilitySlot | Cupos o “sin disponibilidad” |
| T09 | Solicitar revisión de prioridad, si corresponde | Paciente | Reservas web/app | Política institucional aprobada | RF-021 | RNF-001–002 | RB-015 | PriorityRequest | Solicitud pendiente, sin autoaprobación |
| T10 | Revisar y aprobar/rechazar prioridad manualmente | Revisor de salud autorizado | Web operativa | Solicitud pendiente y permiso | RF-022, RF-032 | RNF-001, RNF-010 | RB-002, RB-015 | PriorityRequest, AuditLog | Decisión motivada y auditable |
| T11 | Revalidar slot y reservar atómicamente | Paciente/recepción; sistema | Reservas web/app/web operativa | Identidad, permiso y slot elegible | RF-013–014 | RNF-011 | RB-005–006 | Patient, Slot, Appointment | Una cita `SCHEDULED` o conflicto |
| T12 | Emitir constancia | Sistema | Reservas web/app | Reserva persistida | RF-015 | RNF-006 | RB-006–007 | Appointment | ID/horario y `SCHEDULED`; no confirma |
| T13 | Generar avisos y recordatorios | Sistema | App móvil; reservas web según canal | Evento/canal habilitado | RF-023 | RNF-002, RNF-013 | RB-008, RB-021 | Appointment, Notification | Aviso o fallo registrado; cita intacta |
| T14 | Consultar citas y estados propios | Paciente | Reservas web/app | Sesión vinculada a Patient | RF-006 | RNF-001–002 | RB-002, RB-016–017 | Appointment | Citas propias, sin datos ajenos |
| T15 | Confirmar asistencia | Paciente/actor autorizado | Reservas web/app/web operativa | Cita `SCHEDULED` elegible; plazo por validar | RF-016 | RNF-010 | RB-007–008, RB-016 | Appointment, AuditLog | `CONFIRMED` o rechazo |
| T16 | Cancelar cita elegible | Paciente/recepción autorizada | Reservas web/app/web operativa | Cita propia/vigente; plazo por validar | RF-017 | RNF-010–011 | RB-002, RB-009, RB-016 | Appointment, AuditLog | `CANCELLED` con historial |
| T17 | Reprogramar atómicamente | Paciente/recepción autorizada | Reservas web/app/web operativa | Cita elegible y nuevo slot libre | RF-019, RF-014 | RNF-011 | RB-005, RB-010–011 | Appointment anterior/sucesora, Slot | Anterior `RESCHEDULED`, sucesora `SCHEDULED`; fallo conserva original |
| T18 | Liberar cupo anterior si procede | Sistema | Backend compartido | Cancelación/reprogramación y regla de elegibilidad | RF-018 | RNF-011 | RB-009, RB-011 | Appointment, AvailabilitySlot | Slot liberado u ocupado sin adjudicar |
| T19 | Ingresar, consultar o cancelar espera | Paciente | Reservas web/app | Sin cupo o condición admitida | RF-020 | RNF-001–002 | RB-012 | WaitlistEntry | Entrada separada de cita |
| T20 | Buscar elegible y emitir oferta | Sistema; personal si política exige | Backend compartido/web operativa | Slot liberado y entrada elegible | RF-024 | RNF-010–011 | RB-011–013 | Slot, WaitlistEntry, SlotOffer | `SlotOffer=PENDING`; ninguna cita nueva |
| T21 | Aceptar/rechazar oferta o registrar expiración | Paciente; sistema para expiración | App móvil/reservas web/backend | Oferta `PENDING`; titular, vigencia y slot se revalidan | RF-025, RF-014 | RNF-001, RNF-011 | RB-005, RB-013–014 | SlotOffer, Appointment, Slot | `ACCEPTED` + una cita, `REJECTED` o `EXPIRED` |
| T22 | Consultar agenda y pacientes autorizados | Recepción/profesional | Web operativa | Rol/propiedad válida | RF-030 | RNF-001–002 | RB-002 | Schedule, Appointment, Patient | Vista mínima filtrada |
| T23 | Registrar llegada `CHECK_IN` | Recepción | Web operativa | Cita `CONFIRMED`, `flowStage=null` | RF-026 | RNF-010 | RB-016–018, RB-020 | Appointment, AuditLog | `CHECK_IN` o conflicto |
| T24 | Registrar `WAITING` | Recepción | Web operativa | Cita `CONFIRMED`, etapa `CHECK_IN` | RF-027 | RNF-010 | RB-017–018, RB-020 | Appointment, AuditLog | `WAITING` o conflicto |
| T25 | Registrar `IN_ATTENTION` | Profesional vinculado | Web operativa | Cita propia en `WAITING` | RF-028 | RNF-001, RNF-010 | RB-017–018, RB-020 | Appointment, Professional, AuditLog | Inicio operativo, sin dato clínico |
| T26 | Registrar `FINISHED` y `COMPLETED` | Profesional vinculado | Web operativa | Cita propia en `IN_ATTENTION` | RF-029 | RNF-010–011 | RB-011, RB-017–020 | Appointment, Slot, AuditLog | Flujo/cita finalizados; slot consumido |
| T27 | Auditar acciones críticas | Sistema | Backend compartido | Evento crítico real | RF-032 | RNF-010 | RB-020 | AuditLog, entidad afectada | Registro reconstruible o reversión cuando es transaccional |
| T28 | Consultar indicadores de ensayo | Admin/auditor autorizado | Web operativa | Cohorte/denominador definidos | RF-031 | RNF-002, RNF-013 | RB-002, RB-024 | Appointment, Slot, AuditLog | Métrica trazable o “no medible” |
| T29 | Elegir preferencias visuales accesibles | Usuario de interfaz | Las cuatro superficies según usuario | Interfaz disponible | RF-033 | RNF-006 | RB-023 | Preferencia visual | Tema/modo aplicados; estados siguen legibles sin color |

**Dependencias de política:** T09–T10 solo opera con protocolo y rol institucional validados; T15–T21 depende de plazos, elegibilidad, selección y expiración por definir. La oferta no es cita y una notificación enviada no prueba que fue leída. T23–T26 son el flujo aprobado para FASE 5.11; el resto del MVP se especifica sin afirmar que ya esté implementado.

## 4. Decisiones derivadas

Los “No” significan ruta alternativa o rechazo; no se interpretan como nueva función. Los IDs D01–D13 son referencias para diagramas futuros.

| ID | Decisión | Sí | No | RF/RB |
|---|---|---|---|---|
| D01 | ¿Contenido institucional autorizado para publicar? | T01 publica | Mantener sin publicar | RF-009 / RB-022 |
| D02 | ¿Usuario autenticado y autorizado para la operación? | Continuar | Rechazar o conducir a autenticación | RF-002–004 / RB-002 |
| D03 | ¿Especialidad activa y cupo visible? | T08 consulta slots | Mostrar no reservable o sin disponibilidad | RF-008, RF-012 / RB-003–004 |
| D04 | ¿Existe cupo elegible para reservar? | T11 | Ofrecer alternativa/espera T19 | RF-012–013, RF-020 / RB-004, RB-012 |
| D05 | ¿Se solicita prioridad bajo política aprobada? | T09–T10 | Seguir reserva estándar | RF-021–022 / RB-015 |
| D06 | ¿Slot sigue disponible al confirmar transacción? | Crear una cita | Conflicto sin reserva parcial | RF-013–014 / RB-005 |
| D07 | ¿Paciente confirma asistencia cuando corresponde? | T15 deja `CONFIRMED` | Conservar estado o elegir T16/T17 según regla; sin inventar no-show | RF-016–019 / RB-007–009, RB-016 |
| D08 | ¿Se solicita y permite cancelar o reprogramar? | T16 o T17 | Mantener cita original | RF-017, RF-019 / RB-009–010 |
| D09 | ¿Cupo anterior es elegible para liberar? | T18 | Mantenerlo no disponible; `COMPLETED` nunca libera | RF-018 / RB-009, RB-011 |
| D10 | ¿Existe entrada elegible en espera? | T20 crea oferta | Slot sigue disponible según política | RF-020, RF-024 / RB-012–013 |
| D11 | ¿Oferta propia sigue vigente y se acepta? | T21 revalida y crea una cita | Rechazo/expiración sin cita; siguiente candidato según política | RF-025, RF-014 / RB-013–014 |
| D12 | ¿Cita está `CONFIRMED` antes de check-in? | T23 | Rechazar inicio de flujo | RF-026 / RB-016–017 |
| D13 | ¿Actor/propiedad y transición de flujo son válidos? | T23–T26 según etapa | Conflicto sin retroceso; repetición actual idempotente | RF-026–029 / RB-017–020 |

## 5. Diferencias AS-IS / TO-BE

El AS-IS no tiene secuencia validada (`AS-IS.md` §§6–11). Cada comparación declara el cambio **propuesto** sin afirmar mejora medida.

| Aspecto | AS-IS conocido | TO-BE HospitalPlatform | Requisito que introduce el cambio | Estado |
|---|---|---|---|---|
| Información pública | PENDIENTE_VALIDACION | Portal público con contenido autorizado y CTA | RF-009–010; RB-022 | [PROPUESTO] |
| Solicitud de cita/canales | PENDIENTE_VALIDACION | Portal de reservas y app móvil | RF-001–004, RF-010 | [PROPUESTO] |
| Catálogo/disponibilidad | PENDIENTE_VALIDACION | Especialidades activas y slots consultables | RF-008, RF-011–012 | [PROPUESTO] |
| Reserva/conflicto | PENDIENTE_VALIDACION | Reserva atómica; solo una cita activa por slot | RF-013–014; RB-005 | [PROPUESTO] |
| Constancia/confirmación | PENDIENTE_VALIDACION | Constancia `SCHEDULED`; confirmación separada | RF-015–016; RB-007–008 | [PROPUESTO] |
| Cancelación/reprogramación | PENDIENTE_VALIDACION | Cambios con historial y liberación condicional | RF-017–019 | [PROPUESTO] |
| Lista/recuperación de cupos | PENDIENTE_VALIDACION | Entrada, oferta y aceptación transaccional consentida | RF-020, RF-024–025; RB-012–014 | [PROPUESTO] |
| Priorización | PENDIENTE_VALIDACION | Solicitud y revisión manual auditable | RF-021–022; RB-015 | [PROPUESTO], política [PENDIENTE_VALIDACION] |
| Avisos | PENDIENTE_VALIDACION | Recordatorios configurables y fallos aislados | RF-023; RB-021 | [PROPUESTO] |
| Llegada/espera/atención | PENDIENTE_VALIDACION | `CHECK_IN → WAITING → IN_ATTENTION → FINISHED`, sin dato clínico | RF-026–029; RB-017–020 | [PROPUESTO] modelo; proceso institucional [PENDIENTE_VALIDACION] |
| Auditoría/indicadores | PENDIENTE_VALIDACION | Eventos y métricas sintéticas definidas | RF-031–032; RB-024 | [PROPUESTO] |
| Accesibilidad visual | PENDIENTE_VALIDACION | Preferencias y estados comprensibles sin color | RF-033; RNF-006 | [PROPUESTO] |

**Cobertura del proceso:** RF-001–033 y RB-001–024 tienen al menos una actividad T01–T29 o decisión D01–D13. RNF-004 (disponibilidad del prototipo), RNF-009 (ensayo de escalabilidad) y RNF-012 (restauración) son verificaciones de operación/QA y no pasos del recorrido del paciente; se conservan en la SRS sin forzar una actividad de negocio. `NO_SHOW` sigue pendiente de decisión y por ello no genera actividad actual.

## Elementos autorizados para modelado

### Actores

Visitante, paciente, administrador, recepcionista (`RECEPTIONIST`), profesional vinculado (`PROFESSIONAL`), revisor de prioridad autorizado [PENDIENTE_VALIDACION], auditor autorizado y sistema automático. `SYSTEM` no es persona. Mapeo de roles finales y responsabilidades institucionales aún pendiente (SRS §1; ADR-005/007).

### Procesos

Preparar catálogo/agenda; consulta pública; identidad/sesión; disponibilidad/reserva; constancia/confirmación; cancelación/reprogramación/liberación; lista de espera/oferta/respuesta; revisión manual de prioridad; cuatro transiciones operativas; notificaciones; auditoría/indicadores; accesibilidad transversal. T01–T29 delimitan las actividades permitidas (SRS RF-001–033; RB-001–024).

### Decisiones

D01–D13 de §4. No introducir gateways para tasas, tiempos ni diagnósticos sin fuente y requisito aprobado.

### Entidades

User, Role, RefreshToken, Patient, Professional, Specialty, contenido institucional, Schedule, AvailabilitySlot, Appointment, WaitlistEntry, SlotOffer, PriorityRequest, Notification, AuditLog y preferencia visual. Las entidades de espera/oferta/notificación/contenido/preferencia son **candidatas de diseño**, no prueba de tabla o módulo implementado (`docs/00` §§9–12, 18; SRS §§3–4; auditoría técnica).

### Estados

`AppointmentStatus`: `SCHEDULED`, `CONFIRMED`, `CANCELLED`, `RESCHEDULED`, `COMPLETED`. `FlowStage`: `null`, `CHECK_IN`, `WAITING`, `IN_ATTENTION`, `FINISHED`. Para `SlotOffer`, la propuesta del roadmap usa `PENDING → ACCEPTED | REJECTED | EXPIRED`; no es `AppointmentStatus`. Estados de prioridad propuestos por ADR-008: `REQUESTED`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`; criterio/rol final pendiente. No modelar `NO_SHOW`, `ADMISSION`, `TRIAGE` ni `CALLED` como `FlowStage` actual (roadmap §11; ADR-007/008; RB-015–019).

### Eventos

Eventos de flujo aprobados para FASE 5.11: `APPOINTMENT_CHECKED_IN`, `APPOINTMENT_WAITING`, `APPOINTMENT_ATTENTION_STARTED`, `APPOINTMENT_COMPLETED` (ADR-007). `SlotReleased`, `SlotOfferCreated/Accepted/Rejected/Expired`, cambios de prioridad y avisos son **eventos candidatos** del MVP, no confirmación de implementación (`docs/00` §18.3; SRS RF-018/021–025/032). La entrega de aviso no equivale a lectura (RB-021).

### Límites del sistema

Un backend y dominio compartidos para cuatro superficies; datos sintéticos; sin patrocinio ni integración oficial. El AS-IS institucional permanece sin secuencia validada. Admisión, triaje y llamado pueden existir como contexto por validar, pero no se añaden como estados/software actual. `NO_SHOW` requiere decisión de dominio; no se inventa transición, evento persistido ni métrica disponible. Sin historia clínica, diagnóstico, prescripción ni módulos clínicos integrales en el MVP (`docs/00` §§1, 6, 11, 16; ADR-007; SRS §2).
