# Plan de artefactos pendientes

**Corte:** 23/09/2026. Los estados se refieren a archivos localizados, no a aprobación o ejecución. Las fuentes y discrepancias se detallan en `AUDITORIA_DOCUMENTAL.md`. No se generan aquí diagramas ni prototipos definitivos.

## Inventario y prioridad

| Artefacto | ¿Existe? | Archivo actual | Calidad/estado | Qué falta | Dependencias | Prioridad |
|---|---|---|---|---|---|---|
| Acta, capítulo I, Gantt y EDT | Sí | `docs/research/recursos/` y `docs/planning/` | [PARCIAL] versiones paralelas | Control de versión, coherencia de móvil/alcance y evidencia de aprobaciones | Definir línea base | Alta |
| Especificación de Requerimientos | Parcial | `docs/01-PRD.md`; `recursos/CAPITULO-III.md` §1.1 | [CONTRADICCION] dos series de IDs | SRS única con actor, entradas, salidas, reglas, aceptación y estado | Decidir equivalencias y límites MVP | Crítica |
| Matriz de requerimientos | Parcial | Cap. III §1.1; `MATRIZ_TRAZABILIDAD_BASE.md` | [PARCIAL] base de auditoría | Aprobar IDs y descomponer requisitos amplios | SRS | Alta |
| Matriz maestra de trazabilidad | Base nueva | `MATRIZ_TRAZABILIDAD_BASE.md` | [PROPUESTO] | Ligar CU, pantallas, API, entidad y prueba con IDs aprobados | SRS/TO-BE | Alta |
| AS-IS | Base nueva | `AS-IS.md`; `docs/00` §7.1 | [PENDIENTE_VALIDACION] hipótesis separadas | MAPRO detallado, levantamiento autorizado, línea base | Acceso/evidencia institucional | Crítica |
| TO-BE | Base nueva | `TO-BE.md`; `docs/00` §7.2; `docs/03` | [PROPUESTO] | Aprobar políticas de espera, prioridad, no-show, plazos y roles | SRS + AS-IS | Alta |
| IDEF0 AS-IS / TO-BE | No | — | Faltan | Contexto, entradas/controles/salidas/mecanismos y validación de fronteras | AS-IS validado / TO-BE aprobado | Media |
| BPMN AS-IS | No localizable como archivo legible | Cap. III apunta a `assets/diagrama-general-procesos-bpmn.png` ausente | [PARCIAL] referencia rota; posible figura externa no verificable | Pools/carriles, eventos, excepciones y validación | AS-IS | Alta |
| BPMN TO-BE / diagrama de procesos | No localizable como archivo legible | Cap. III §1.4 y referencias `assets/` ausentes | [PARCIAL] texto de proceso | Oferta/aceptación, cancelación, flujo y excepciones | TO-BE + SRS | Alta |
| UML casos de uso | No | — | Falta | Actores y límites del sistema validados | Matriz + SRS | Alta |
| UML actividad / diagrama de flujo | Referencia no verificable | Cap. III §1.4, imágenes `assets/` no encontradas | [PARCIAL] descripción textual | Flujos críticos y excepciones, distinguir AS-IS/TO-BE | TO-BE | Media |
| UML secuencia | No | — | Falta | Reservar, reprogramar, aceptar oferta, transición de flujo, autorización | CU + contratos API | Media |
| UML clases | No como UML verificable | `docs/00` §10; `docs/05` dominio textual | [PARCIAL] candidatos | Clases por escenario, cardinalidades y separación dominio/DTO | SRS + secuencias + modelo datos | Media |
| Modelo conceptual BD | Parcial | `docs/database/ENTITY-RELATIONSHIP-DIAGRAM.md`, `docs/05` | [PARCIAL] | Entidades y relaciones del MVP, sin diseño físico mezclado | SRS/TO-BE | Alta |
| Modelo lógico BD | Parcial | `docs/database/DATABASE-DESIGN.md`, `docs/05` | [CONTRADICCION] UUID vs PK numéricas/tablas estado | Normalizar cardinalidades, claves, estados, espera/ofertas | Conceptual aprobado | Alta |
| Modelo físico BD | Parcial | `database/migrations/V1`–`V3`; `docs/database/MIGRATION-STRATEGY.md` | [PARCIAL] esquema implementado de núcleo | Diagrama con tablas, constraints, índices y divergencias del TO-BE | Lógico + SQL vigente | Alta |
| API/OpenAPI | Prosa sí; contrato máquina no observado | `docs/06-API-SPECIFICATION.md` | [PARCIAL] | `contracts/openapi.yaml` y separación vigente/planeado | SRS + arquitectura | Alta |
| RUP / Proceso Unificado | No definido | `docs/07-IMPLEMENTATION-PLAN.md` §4; EDT/Gantt | [PROPUESTO] adaptación posible | Decisión metodológica formal, iteraciones y artefactos por fase | Aprobación docente | Media |
| UX/UI y documentación de interfaz | Brief sí | `docs/04-UI-UX-DESIGN-BRIEF.md` | [PARCIAL] | Inventario aprobado, estados y especificación pantalla↔RF | SRS/TO-BE | Alta |
| Prototipo | No localizado | — | Falta | Wireframes y prototipo web/móvil; Figma en tarea posterior | Inventario UX, CU | Media |
| Reportes / dashboard | Planeado | `docs/00` §7.3; `docs/12` | [PARCIAL] diseño | Definiciones de indicadores, denominadores y vistas | RF-20 + datos disponibles | Media |
| Presentación del prototipo | No | — | Falta | Preparar después del prototipo validado | Prototipo y pruebas UX | Baja |
| Diagrama lógico y físico de BD para entrega | Bases parciales | `docs/database/`, migraciones | [PARCIAL] | Diagramas finales armonizados | Modelos conceptual/lógico | Media |

No se encontró archivo de imagen en `docs/research/recursos/assets/` al inventariar el repositorio; las referencias Markdown de Capítulo III no demuestran que el BPMN/actividad estén disponibles. Para calidad de UX y reportes, el brief y el plan son intención, no prototipo implementado.

## RUP: encaje posible, sin forzar decisión

`docs/07-IMPLEMENTATION-PLAN.md` §4 habla de implementación incremental; no establece RUP. `docs/research/recursos/CAPITULO-III.md` §1.2 y `docs/planning/wbs.md` organizan paquetes por entregable, y `recursos/Diagrama-de-gantt_G1.md` contiene hitos. Se puede **mapear** el trabajo a RUP si el docente lo exige, pero el estado actual es [NO DOCUMENTADO] como metodología formal.

| Fase RUP candidata | Actividades existentes (Gantt/EDT) | Artefactos | Estado | Pendientes |
|---|---|---|---|---|
| Inicio | Gantt 3–9; EDT 1.1–1.3: alcance, interesados, acta, riesgo | Roadmap, acta, Lean Canvas, EDT, cronograma | [PARCIAL] | Validación de caso/alcance y línea base real |
| Elaboración | Gantt 11–28; EDT 2.1–3.5: levantamiento, RF/RNF, AS-IS/TO-BE, UX, arquitectura, datos, API | SRS, procesos, modelos, prototipo, TRD, ADR | [PARCIAL] | AS-IS validado, SRS conciliada, diagramas y diseño aprobado |
| Construcción | Gantt 30–40 y pruebas 42–44; EDT 4.1–5.3 | Backend/web/móvil, migraciones, pruebas | [PARCIAL] | Web/móvil y módulos faltantes; evidencia de pruebas |
| Transición | Gantt 45–55; EDT 5.4–6.4 | E2E, staging, manuales, presentación, cierre | [PROPUESTO] | Despliegue académico verificado y aceptación del prototipo |

Las fechas del Gantt son programadas, no evidencia de que el hito se cumplió. La línea base de requisitos aparece prevista para 08/09/2026 y el diseño para 06/10/2026 (`recursos/Diagrama-de-gantt_G1.md`); sus aprobaciones no se localizaron.

## Plan de diagramas

Los diagramas se hacen por escenario y con referencias a RF/RNF de la matriz, no como láminas gigantes. “RF” alude provisionalmente a la serie Capítulo III.

| Diagrama | Objetivo | Fuente | Actores/entidades | Requisitos | Dependencias / información faltante | Formato recomendado |
|---|---|---|---|---|---|---|
| IDEF0 AS-IS | Delimitar función institucional real | `AS-IS.md`, MAPRO por obtener | Áreas de citas, admisión, archivo | OE1; sin RF del software | Secuencia/controles y responsables validados | IDEF0 vector editable |
| IDEF0 TO-BE | Entradas, controles y mecanismos del servicio digital | `TO-BE.md`, roadmap | Paciente, personal, API, slots | RF-01–24 | Política de ofertas/prioridad y SRS única | IDEF0 vector editable |
| BPMN AS-IS | Recorrido real y excepciones | MAPRO y levantamiento | Paciente/áreas reales | OE1 | Eventos, canales y excepciones institucionales | BPMN 2.0 editable |
| BPMN TO-BE | Orquestar reserva, cambios, espera y atención | `TO-BE.md` | Paciente, sistema, recepción, profesional | RF-08–22 | Aprobación de reglas temporales y ofertas | BPMN 2.0 editable, subprocesos |
| UML Casos de Uso | Responsabilidades por actor | Matriz base + SRS | Visitante, paciente, admin, recepción, profesional, revisor | RF-01–24 | Roles finales y separación de superficies | PlantUML/Mermaid por dominio |
| UML Actividad / flujo | Decisiones de reserva y flujo operativo | `docs/03`, `TO-BE.md` | Paciente/sistema/personal | RF-08–18/21 | Política de cancelación/no-show | PlantUML/Mermaid separado por escenario |
| UML Clases | Estructura de dominio relevante | ADR, SRS, datos | User, Slot, Appointment, WaitlistEntry, SlotOffer | RF-01–24 | Modelo lógico, cardinalidades, roles | PlantUML por bounded context |
| UML Secuencia | Probar interacciones/transacciones | API, ADR-007, TO-BE | Clientes/API/servicios/repositorios | RF-09/10/12/18/21 | Contrato API de espera y prioridad | PlantUML por caso crítico |
| BD conceptual | Relaciones de negocio | SRS/TO-BE | Paciente, profesional, agenda, cita, espera, oferta | RF-03–22 | Decidir entidades vs eventos derivados | ER conceptual |
| BD lógico | Claves/cardinalidades/normalización | Conceptual, ADR-009 | Tablas candidatas | RF-01–24 | Conciliar `docs/05` con SQL y oferta | Crow's foot editable |
| BD físico | Esquema realmente desplegable | SQL V1–V3, lógico aprobado | Tablas, índices, checks | RF-01–24; RNF-11 | Nuevas migraciones futuras; verificar versión exacta | Diagrama desde PostgreSQL/DDL |

## Inventario preliminar UX/UI, sin diseñar Figma

“Estados” incluye estados visuales necesarios y valores de dominio cuando aplican. Todo es vista candidata [PROPUESTO] salvo la implementación web/móvil, que no se observó. Fuente: `docs/00` §§14.2–14.5 y `docs/04` §§3–9; RF de Capítulo III.

| Superficie / vista | Usuario | Objetivo | RF relacionados | Datos utilizados | Estados principales |
|---|---|---|---|---|---|
| Portal público: inicio/hospital/ubicación | Visitante | Información verificada y CTA reservas | 23 | Contenido institucional autorizado | carga, disponible, error |
| Portal público: servicios/especialidades/políticas/FAQ | Visitante | Conocer oferta pública | 06, 23 | Catálogo + contenido | activo, sin contenido, error |
| Portal de reservas: acceso/perfil | Paciente | Identificarse | 01, 03, 24 | Usuario/paciente | válido, inválido, expirado |
| Portal de reservas: especialidades y disponibilidad | Paciente | Elegir cupo | 06–08 | Specialty/Schedule/Slot | disponible, sin cupos, ocupado |
| Portal de reservas: nueva reserva y constancia | Paciente | Crear cita | 09–11 | Slot/Appointment | progreso, éxito, conflicto |
| Portal de reservas: mis citas/detalle | Paciente | Consultar y gestionar | 04, 11–13 | Appointment | SCHEDULED, CONFIRMED, CANCELLED, RESCHEDULED, COMPLETED |
| Portal de reservas: espera y ofertas | Paciente | Solicitar/aceptar cupo | 14, 21 | WaitlistEntry/SlotOffer | activa, ofrecida, aceptada, rechazada, expirada |
| Portal de reservas: prioridad | Paciente | Solicitar revisión y ver respuesta | 15 | PriorityRequest | solicitada, en revisión, aprobada, rechazada |
| Web operativa: usuarios/roles | Admin | Autorizar acceso | 02, 24 | User/Role/Permission | activo, inactivo, error |
| Web operativa: catálogo/profesionales/agenda | Admin | Mantener oferta | 05–07, 23 | Specialty/Professional/Schedule/Slot | activo, bloqueado, sin datos |
| Web operativa: recepción/citas del día | Recepción | Buscar y registrar llegada/espera | 04, 17, 18 | Appointment/Patient | CONFIRMED, CHECK_IN, WAITING |
| Web operativa: prioridad | Revisor autorizado | Resolver solicitud | 15, 22 | PriorityRequest/AuditLog | pendiente, aprobada, rechazada |
| Web operativa: profesional/cola | Profesional | Iniciar/finalizar atención operativa | 18, 19 | Agenda/Appointment | WAITING, IN_ATTENTION, FINISHED |
| Web operativa: auditoría/indicadores | Admin/auditor | Revisar eventos y métricas | 20, 22 | AuditLog/Appointment | filtros, vacío, error |
| Aplicación móvil: acceso/perfil/preferencias | Paciente | Identidad y accesibilidad | 01, 03, 24; **RF de preferencias faltante** | User/preferencias | autenticado, expirado, tema/modo |
| Aplicación móvil: catálogo/disponibilidad/reserva | Paciente | Gestionar nueva cita | 06, 08–11 | Specialty/Slot/Appointment | sin cupos, conflicto, éxito |
| Aplicación móvil: mis citas/confirmar/cancelar/reprogramar | Paciente | Gestionar asistencia | 04, 11–13 | Appointment | cinco estados cita |
| Aplicación móvil: espera/ofertas/notificaciones | Paciente | Recuperar cupo | 14, 16, 21 | WaitlistEntry/SlotOffer/Notification | ofrecida, aceptada, rechazada, expirada |
| Aplicación móvil: prioridad/seguimiento | Paciente | Ver decisión y progreso | 15, 17, 18 | PriorityRequest/Appointment | revisión y cuatro etapas de flujo |

No se propone vista de diagnóstico, triaje clínico, receta, farmacia ni historia clínica para este MVP (`docs/00` §§6.2–6.3). La pantalla de triaje de `docs/04` queda pendiente de reclasificación, no se convierte aquí en prototipo.

## Orden recomendado desde este corte

1. Registrar/validar evidencia institucional y cerrar el AS-IS o documentar su límite metodológico (`docs/00` §25 fases 0–1; `AS-IS.md`).
2. Conciliar alcance e identificadores del PRD y Capítulo III; aprobar SRS y criterios de aceptación, incluidos oferta, accesibilidad, prioridad y NO_SHOW como decisión pendiente.
3. Aprobar TO-BE y matriz maestra con actores, políticas, datos y pruebas; mantener explícitos los puntos no validados.
4. Crear IDEF0/BPMN AS-IS y TO-BE según el grado de evidencia; después casos de uso y actividades por escenario.
5. Cerrar modelos conceptual → lógico → físico de datos, corrigiendo divergencias de UUID/estados y cotejando SQL V1–V3.
6. Especificar secuencias y clases por casos críticos, y alinear API/OpenAPI con los modelos aprobados.
7. Completar documentación UX/UI y prototipos de las cuatro superficies; preparar luego presentación del prototipo y reportes.
8. Documentar la adaptación RUP **solo si se decide metodológicamente**; adjuntar pruebas, despliegue y cierre académicos conforme se ejecuten.

El orden permite trabajo paralelo en UX e ingeniería una vez acordada la SRS, pero ningún diagrama AS-IS puede presentarse como proceso institucional validado antes del levantamiento.
