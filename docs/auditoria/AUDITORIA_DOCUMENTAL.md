# Auditoría documental de HospitalPlatform

**Corte:** 23/09/2026. **Rama:** `docs/research/documentacion`. **Estado:** borrador de auditoría; no equivale a validación docente ni institucional.

## Método y jerarquía

Se verificaron rama, estado e inventario Git. `docs/research/recursos/` contiene cuatro archivos sin seguimiento previo a esta auditoría; se conservaron intactos. Se leyeron en orden los documentos `docs/00` a `docs/12`; después, los cuatro Markdown de `recursos`. La inspección de código fue estática y ligera. No se ejecutaron pruebas ni se verificó un despliegue.

**Jerarquía aplicada:** `docs/00-PROJECT-ROADMAP.md` gobierna alcance y decisiones generales; las decisiones aceptadas de `docs/adr/` y los ajustes explícitos de FASE 5.11 gobiernan el incremento técnico; `docs/01` a `docs/12` detallan áreas, pero sus afirmaciones incompatibles se registran aquí. Los archivos de `recursos` son evidencia académica secundaria. “FINAL” en el título de un documento indica su rótulo editorial, no que exista aprobación institucional o implementación.

Etiquetas: [CONFIRMADO] evidencia documental vigente o inspección estática; [PROPUESTO] diseño revisable; [PENDIENTE_VALIDACION] requiere docente o institución; [IMPLEMENTADO] código suficiente observado; [PARCIAL] capacidad incompleta; [NO_IMPLEMENTADO] sin implementación funcional observada; [CONTRADICCION] fuentes relevantes difieren; [FUTURO] después del MVP; [FUERA_MVP] excluido. Los estados de implementación son estimaciones estáticas, no certificación de funcionamiento.

## Inventario documental principal

| Archivo | Propósito | Autoridad | Estado | Información importante | Conflictos detectados |
|---|---|---|---|---|---|
| `docs/00-PROJECT-ROADMAP.md` | Dirección, alcance, objetivos, reglas y fases | Principal | [CONFIRMADO] guía viva; validaciones pendientes | OG, OE1–OE6, cuatro superficies MVP, estados, restricciones | Su TO-BE narrativo y fases mencionan triaje, admisión, llamado y NO_SHOW que sus §§6.2 y 11 restringen |
| `docs/01-PRD.md` | Producto, historias, RF y RNF iniciales | Principal subordinada | [CONTRADICCION] requiere armonización | 20 RF y 13 RNF con IDs de tres dígitos | Reduce confirmación/lista a Should; incluye diagnóstico y triaje; omite RF explícitos de prioridad y ofertas |
| `docs/02-TRD.md` | Arquitectura y tecnologías | Principal subordinada | [CONTRADICCION] desactualizado | Monolito modular y API REST | Flutter/PostgreSQL 16 frente a React Native/Expo/PostgreSQL 17 del roadmap; registro clínico |
| `docs/03-APP-FLOW.md` | Flujo de aplicación y FASE 5.11 | Principal especializada | [PARCIAL] | Estados canónicos y transiciones operativas | Su lista de espera termina en “asignación” sin consentimiento/oferta del roadmap |
| `docs/04-UI-UX-DESIGN-BRIEF.md` | Vistas y principios UI | Principal especializada | [PARCIAL] | Seguimiento canónico y roles | Pantallas de triaje clínico y registro de atención; omite flujos detallados de prioridad y ofertas |
| `docs/05-BACKEND-SCHEMA.md` | Modelo de datos propuesto | Principal especializada | [CONTRADICCION] | Entidades y restricciones | PK numéricas y tablas de estado vs UUID/enums de diseño/SQL; `NO_ASISTIO`, triaje y atención clínica |
| `docs/06-API-SPECIFICATION.md` | Contratos REST en prosa | Principal especializada | [PARCIAL] | Endpoints de citas y operaciones | No sustituye `contracts/openapi.yaml`; contrato amplio excede API implementada |
| `docs/07-IMPLEMENTATION-PLAN.md` | Fases de construcción | Principal especializada | [PARCIAL] | Entregables y roles del equipo | Cierre MVP sigue admitiendo atención profesional clínica; metodología incremental, no RUP definido |
| `docs/08-SECURITY-THREAT-MODEL.md` | Riesgos y controles | Principal especializada | [PARCIAL] | RBAC, auditoría, privacidad | Incluye triaje y datos asistenciales fuera del alcance clínico aprobado |
| `docs/09-TEST-STRATEGY.md` | Estrategia QA | Principal especializada | [PARCIAL] | Niveles y tipos de prueba | Casos de triaje requieren recorte o etiqueta futura |
| `docs/10-TEST-PLAN.md` | Casos de prueba | Principal especializada | [PARCIAL] | Matriz requisito–prueba | Triaje y atención básica no concuerdan con FASE 5.11 ni con exclusión de datos clínicos |
| `docs/11-DEPLOYMENT-PLAN.md` | Entornos y liberación | Principal especializada | [PROPUESTO] | Staging y producción académica | Despliegue operativo aún no comprobado |
| `docs/12-MONITORING-MAINTENANCE.md` | Métricas, respaldo y operación | Principal especializada | [PROPUESTO] | Dashboards, alertas, RTO/RPO propuestos | Monitoreo de triaje excede la máquina actual |

## Fuentes secundarias y técnicas examinadas

| Archivo o grupo | Propósito | Autoridad | Estado | Información importante | Conflictos detectados |
|---|---|---|---|---|---|
| `docs/research/recursos/Acta-de-constitucion.md` | Acta académica | Secundaria | [CONFIRMADO] texto; aprobación institucional no acreditada | Alcance, presupuesto, seis hitos | Su “decisión móvil no resuelta” contradice roadmap |
| `docs/research/recursos/CAPITULO-I.md` | Problema y objetivos académicos | Secundaria | [PARCIAL] | OG/OE1–OE6 y límites | OG abreviado y móvil “complementario” frente a roadmap obligatorio |
| `docs/research/recursos/CAPITULO-III.md` | RF/RNF detallados, EDT y TO-BE | Secundaria | [PARCIAL] | RF-01–24, RNF-01–12, metas académicas | Su §1.1.3 describe código antiguo (Spring Boot 4.1.0 y módulos vacíos); diagramas `assets/` referidos no están en la carpeta |
| `docs/research/recursos/Diagrama-de-gantt_G1.md` | Cronograma convertido | Secundaria | [CONFIRMADO] plan, no ejecución | 55 filas, seis hitos | Fechas previstas no acreditan aprobación ni avance real |
| `docs/planning/wbs.md`, `docs/planning/requirements-elicitation.md` | EDT e instrumentos | Apoyo | [PARCIAL] | Paquetes, preguntas, gate de discovery | Levantamiento no consta completado |
| `docs/planning/gantt.md`, `docs/planning/project-charter.md`, `docs/planning/PROJECT-CHARTER-HOSPITAL-HUAYCAN.md` | Planificación | Apoyo | [PROPUESTO] | Hitos y alcance | Versiones paralelas requieren control de línea base |
| `docs/adr/ADR-001`–`ADR-011` | Decisiones de arquitectura | Decisiones aceptadas por tema | [CONFIRMADO] documental | Monolito, PostgreSQL, roles, slots, estados, prioridad, UUID, auditoría | ADR-007 limita FASE 5.11; no resuelve todo el MVP |
| `docs/database/DATABASE-DESIGN.md`, `docs/database/ENTITY-RELATIONSHIP-DIAGRAM.md`, `docs/database/MIGRATION-STRATEGY.md` | Persistencia | Diseño técnico | [PARCIAL] | UUID, tablas y migraciones | Requiere sincronizar con `docs/05` y migración V3 |
| `README.md`, `apps/backend/README.md`, `apps/backend/pom.xml`, `database/migrations/V1`–`V3`, enums y controladores backend | Comprobación estática | Evidencia de implementación | [PARCIAL] | Java 21, Spring Boot 3.5.14, PostgreSQL, citas y flujo | No equivalen a pruebas ejecutadas; `recursos/CAPITULO-III.md` informa estado antiguo |

También se revisó la estructura de `docs/appointments/`, `docs/architecture/`, `docs/contracts/`, `docs/patients/`, `docs/professionals/`, `docs/security/` y `docs/users/` para ubicar especificaciones; no se usa documentación semanal de `docs/research/semana-*` como evidencia. Los documentos de planificación y ADR se consultaron por secciones pertinentes, no constituyen un levantamiento institucional.

## Definiciones canónicas detectadas

| Tema | Definición y estado | Fuente |
|---|---|---|
| Problema | Gestión consistente de citas/cupos y trazabilidad ambulatoria como oportunidad de investigación; no hay magnitud local medida [PENDIENTE_VALIDACION] | `docs/00` §§4.1–4.3; `recursos/CAPITULO-I.md` §1.1 |
| Problema general | Pregunta del roadmap sobre apoyo web/móvil, recuperación de cupos y trazabilidad [PROPUESTO] | `docs/00` §4.1; versión abreviada en `recursos/CAPITULO-I.md` §1.2 |
| Objetivo general | Diseñar, desarrollar, implementar y evaluar plataforma web/móvil con cuatro superficies, citas, waitlist, prioridad validada, seguridad y medición [PROPUESTO] | `docs/00` §5.1 |
| Objetivos específicos | OE1–OE6, levantamiento, diseño, implementación, seguridad, evaluación y portal público [PROPUESTO] | `docs/00` §5.2; repetidos de forma abreviada en `recursos/CAPITULO-I.md` §2.2 |
| Alcance | Consulta externa programada y portal público, reservas, web operativa y app móvil obligatoria; prioridad manual condicionada a política [PROPUESTO] | `docs/00` §§6.1, 14.2, 25 |
| Limitaciones | Caso académico, datos sintéticos, sin patrocinio, acceso interno ni integración oficial; proceso y línea base sin levantar [CONFIRMADO]/[PENDIENTE_VALIDACION] | `docs/00` §§1, 4, 16; `recursos/CAPITULO-I.md` §2.3 |
| Actores y roles | Visitante/paciente/personal; roles candidatos `PATIENT`, `ADMISSION`, `FLOW_OPERATOR`, `PRACTITIONER`, `CLINICAL_PRIORITY_REVIEWER`, `ADMIN`, `AUDITOR`; actuales backend `PATIENT`, `ADMIN`, `RECEPTIONIST`, `PROFESSIONAL`, `TRIAGE` [CONTRADICCION] | `docs/00` §19.3; `docs/01` §3; `docs/adr/ADR-005`; `RoleName.java` |
| Módulos | Identidad, institucional/catálogos, pacientes, profesionales, agenda, citas, prioridad, espera, notificaciones, flujo, auditoría, dashboard [PROPUESTO] | `docs/00` §§9, 25; `docs/02` §8 |
| Arquitectura | Monolito modular `package by feature`, API REST compartida [CONFIRMADO] diseño | `docs/00` §9; `docs/adr/ADR-001`; `docs/02` §3 |
| Modelo de datos | PostgreSQL con UUID, schedules, slots discretos, appointments, waitlist, priority, audit; migraciones Flyway [PARCIAL] | `docs/00` §12; `docs/adr/ADR-006`, `ADR-009`; `docs/database/DATABASE-DESIGN.md`; `database/migrations/` |
| Procesos | AS-IS institucional por levantar; TO-BE digital de catálogo a cita, espera, llegada, atención operativa y métricas [PROPUESTO] | `docs/00` §§7.1–7.2; `docs/03` §§4–13 |
| Estados cita | `SCHEDULED`, `CONFIRMED`, `CANCELLED`, `RESCHEDULED`, `COMPLETED`; `NO_SHOW` sin decisión de dominio aprobada [CONFIRMADO] | `docs/00` §11.1; `docs/adr/ADR-007`; `AppointmentStatus.java` |
| Estados flujo | `null` → `CHECK_IN` → `WAITING` → `IN_ATTENTION` → `FINISHED`; solo desde cita confirmada; `FINISHED` completa cita [CONFIRMADO] | `docs/00` §11.4; `docs/adr/ADR-007`; `FlowStage.java` |
| Tecnologías | Java 21/Spring Boot 3.5.14 observado; PostgreSQL/Flyway; web Next.js/React/TypeScript y móvil React Native/Expo propuestos [PARCIAL] | `apps/backend/pom.xml`; `docs/00` §§12, 14; `docs/02` §5 |
| Restricciones, seguridad y privacidad | Datos sintéticos, RBAC y control por objeto, auditoría, no exponer secretos ni datos clínicos; no integrar oficialmente [PROPUESTO]/[PARCIAL] | `docs/00` §§16, 19; `docs/08` §§9–12 |
| Accesibilidad | WCAG 2.2 AA de referencia, teclado/foco, alto contraste y modos de visión de color [PROPUESTO] | `docs/00` §14.3.1; `docs/04` §2 |
| Métricas | Candidatas de confirmación, cupos, espera, ocupación y calidad; sin línea base hospitalaria [PROPUESTO] | `docs/00` §7.3; `recursos/CAPITULO-III.md` §1.4.3 |
| Entregables e hitos | Documentos 00–12; seis hitos de planificación a cierre previstos 25/08–15/12/2026, cumplimiento no comprobado [PROPUESTO] | `docs/07` §19; `recursos/Diagrama-de-gantt_G1.md`; `recursos/Acta-de-constitucion.md` |

## Estado técnico observado

El repositorio contiene backend Java con controladores y servicios de autenticación, usuarios, pacientes, profesionales, agenda, citas y transiciones del flujo; migraciones V1–V3 y pruebas fuente. [PARCIAL] No se ejecutaron dichas pruebas. No se encontraron aplicaciones web ni móvil dentro de `apps/`; se clasifican [NO_IMPLEMENTADO] en este corte. `waitlist`, `notifications`, `priority`, `dashboard` y `catalogs` tienen en gran medida `package-info.java`, sin servicio/controlador funcional observado. La tabla o módulo vacío no acredita entrega. `V3__support_appointment_lifecycle.sql` sustituye la unicidad absoluta de `slot_id` de V1 por un índice parcial de citas activas; el juicio de `recursos/CAPITULO-III.md` §1.1.3 sobre esa restricción ya no describe todo el estado actual. Fuente: `apps/backend/src/main/java`, `database/migrations/`, `apps/backend/pom.xml`.

## Contradicciones reales y decisiones pendientes

| Tema | Fuente A | Fuente B | Diferencia | Fuente que parece vigente | Acción necesaria |
|---|---|---|---|---|---|
| Móvil MVP | `docs/00` §§1, 6.1 | `recursos/CAPITULO-I.md` §1.1; acta, riesgos | Obligatoria vs complementaria/decisión abierta | Roadmap | Rectificar entregables secundarios |
| Confirmación/lista de espera | `docs/00` §§6.1, 18 | `docs/01` §9 | MVP obligatorio vs Should | Roadmap | Actualizar priorización y criterios PRD |
| Datos clínicos y triaje | `docs/00` §§6.2, 11.4 y ADR-007 | `docs/01` HU-009/010, RF-014/020; `docs/02` §§7–10; `docs/04` §3 | Sin diagnóstico/etapa triaje actual vs evaluación, diagnóstico y pantallas clínicas | Roadmap/ADR-007 para fase actual | Retirar del MVP o formalizar expansión posterior |
| NO_SHOW | `docs/00` §§6.1, 11.1 | ADR-007, `AppointmentStatus.java` | Capacidad MVP vs sin estado/regla implementada en FASE 5.11 | Ambas hablan de horizontes distintos | Definir decisión posterior, política temporal y prueba; no añadir estado por intuición |
| Lista de espera | `docs/00` §§11.2, 18.2 | `docs/03` §12; `recursos/CAPITULO-III.md` RF-21 | Oferta y consentimiento vs asignación validada por personal sin oferta explícita | Roadmap | Precisar RF-21, oferta y aceptación |
| Stack móvil/BD | `docs/00` §§12, 14.1 | `docs/02` §5 | React Native/Expo y PostgreSQL 17 vs Flutter y PostgreSQL 16 | Roadmap; POM confirma Spring 3.5.14 | Armonizar TRD y planes |
| Identificadores y estados BD | ADR-009, `docs/database/DATABASE-DESIGN.md`, migraciones | `docs/05` §§8.1, 8.6 | UUID/enums/checks vs PK numéricas/tablas de estado | ADR/SQL | Rehacer modelo lógico/físico sin alterar DB ahora |
| Requisitos | `docs/01` §§5–6 | `recursos/CAPITULO-III.md` §1.1 | RF-001–020/RNF-001–013 vs RF-01–24/RNF-01–12: mismas cifras no son misma semántica | Ninguna numeración consolidada | Aprobar tabla de equivalencias antes de modificar IDs |
| Roles | `docs/00` §19.3 | `docs/02` §9; `RoleName.java` | Candidatos ADMISSION/FLOW_OPERATOR/REVIEWER/AUDITOR vs roles actuales RECEPTIONIST/TRIAGE | Por horizonte: roadmap objetivo; backend actual | Validar con institución y registrar mapeo explícito |
| Estado del código | `recursos/CAPITULO-III.md` §1.1.3 | `apps/backend/pom.xml`, controladores, V3 | Dice Spring 4.1.0 y sin módulos; POM actual 3.5.14 y sí hay módulos | Código actual para implementación | Actualizar capítulo al siguiente corte |
| Representación del flujo | `docs/00` §7.2 y §25 fase 12 | `docs/00` §§6.2, 11.4; ADR-007 | Narrativa amplia admisión/triaje/llamado vs cuatro estados aprobados | Estados explícitos posteriores | Separar tareas posibles de estados aprobados |

No se considera contradicción que waitlist quede fuera de **FASE 5.11** y dentro del **MVP total**: son horizontes distintos (`docs/00` §25, `ADR-007`).

## Hallazgos de trazabilidad

- [CONFIRMADO] OE1 depende de levantamiento institucional; ningún RF implementable demuestra por sí mismo el AS-IS (`docs/00` §5.2; `docs/planning/requirements-elicitation.md` §35).
- [PARCIAL] OE2–OE6 cuentan con cobertura temática en `recursos/CAPITULO-III.md` RF-01–24/RNF-01–12, pero la matriz completa y los criterios por actor/dato todavía faltan.
- [CONTRADICCION] `docs/01` RF-014 y RF-020 introducen registro clínico fuera del MVP aprobado. `docs/01` tampoco identifica RF separados para solicitud y revisión de prioridad, aceptación/rechazo/expiración de oferta, preferencias de accesibilidad ni portal de reservas como superficie distinguible.
- [PARCIAL] Varios RF agrupan dos operaciones (`RF-01`, `RF-02`, `RF-11`, `RF-21`, `RF-23`, `RF-24` del capítulo III); faltan precondiciones, entradas/salidas y aceptación por operación. `RNF-03`–`RNF-12` del capítulo III proponen pruebas medibles, pero no son indicadores del hospital.
- [PENDIENTE_VALIDACION] No se halló línea base de tiempos, no-show, volumen, canales actuales o responsables del AS-IS con evidencia de campo. El MAPRO se cita en `docs/00` §7.1, pero no se encontró copia ni extracción de pasos institucionales dentro de las fuentes revisadas.

La matriz de objetivos, RF/RNF y cadena a diagramas y validación está en `MATRIZ_TRAZABILIDAD_BASE.md`. Los procesos separados están en `AS-IS.md` y `TO-BE.md`; las dependencias y faltantes en `PLAN_ARTEFACTOS_PENDIENTES.md`.
