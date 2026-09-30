# ETAPA E.3.1 — INVENTARIO DOCUMENTAL GLOBAL

**Proyecto:** HOSPITALPLATFORM — caso de estudio académico del Hospital de Huaycán

**Fecha de corte:** 2026-09-30

**Naturaleza:** inventario y clasificación; no se corrigieron las fuentes.

## 1. Estado Git

- Rama verificada: `chore/documentation-global-alignment`.
- Árbol inicial limpio.
- Este informe es el único archivo nuevo de E.3.1. No se hizo commit, push ni merge.

## 2. Alcance revisado

Se enumeraron los **125 archivos físicos preexistentes** bajo `docs/`: 120 versionados y cinco referencias externas bajo `docs/references/` ignoradas por Git. Se incluyen 87 Markdown, nueve PlantUML, diez Mermaid, 12 DOCX, cuatro PDF, dos PPTX y un XLSX. El presente informe es el archivo 126 y se excluye de su propio inventario. Los documentos 00–12 y los dos archivos de dominio están en la raíz de `docs/`; no se hallaron esos nombres en la raíz del repositorio. Se abrieron los formatos binarios para comprobar título o estructura y se leyó de forma focal su contenido relevante. La revisión de contradicciones fue estática; no implica validación institucional, renderizado de todos los diagramas ni ejecución de pruebas.

**Jerarquía de contraste.** Para implementación actual: [DOMAIN-BASELINE](../DOMAIN-BASELINE.md), [Decision Register](../DOMAIN-DECISION-REGISTER.md), código Java, migraciones V1–V3 y pruebas fuente. Para Agenda: [C.1](../agenda/AGENDA-AVAILABILITY-RF-UC-MATRIX.md), [C.2](../agenda/AGENDA-AVAILABILITY-USE-CASES.md), [C.3](../agenda/AGENDA-AVAILABILITY-CONTRACT-DESIGN.md), [C.4](../agenda/AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md), [D.1](../agenda/uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.md), [D.2](../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [D.3](../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md) y [D.4](../agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md). Para la edición Word: [maestro](../documentation-export/HOSPITALPLATFORM-AGENDA-AVAILABILITY-DOCUMENTATION.docx) y anexos A–E.

**Criterio de clasificación.** `VIGENTE` significa fiel al corte en su alcance declarado, aunque describa una propuesta o una decisión `OPEN`; no equivale a función implementada. `PARCIALMENTE VIGENTE` conserva información útil pero mezcla horizontes o requiere el documento acompañante para interpretar límites. `HISTÓRICO` conserva una versión, investigación o plan anterior como procedencia, sin autoridad funcional actual. `REQUIERE ALINEACIÓN` contiene una contradicción verificable o, en el caso excepcional de dos archivos vacíos, carece de contenido utilizable. Estos dos vacíos se identifican expresamente como ausencia, no como contradicción. **Última responsabilidad** indica el frente documental que parece custodiar el artefacto, no una persona ni una asignación formal nueva. Las prioridades son de revisión editorial para E.3.2, no de implementación.

## 3. Inventario completo

| Documento | Estado | Área | Última responsabilidad | Relación con dominio actual | Contradicciones detectadas | Prioridad de revisión |
|---|---|---|---|---|---|---|
| [00-PROJECT-ROADMAP.md](../00-PROJECT-ROADMAP.md) | PARCIALMENTE VIGENTE | Producto | Producto | Roadmap vigente como intención; horizontes MVP/FASE mezclados | C-02 | Media |
| [01-PRD.md](../01-PRD.md) | REQUIERE ALINEACIÓN | Requisitos | Producto | RF antiguos y capacidades clínicas fuera del MVP | C-01 | Alta |
| [02-TRD.md](../02-TRD.md) | REQUIERE ALINEACIÓN | Arquitectura | Arquitectura | Stack y módulos objetivo anteriores | C-02/C-06 | Alta |
| [03-APP-FLOW.md](../03-APP-FLOW.md) | PARCIALMENTE VIGENTE | Procesos | Análisis funcional | Estados actuales y flujo TO-BE más amplio | — | Media |
| [04-UI-UX-DESIGN-BRIEF.md](../04-UI-UX-DESIGN-BRIEF.md) | REQUIERE ALINEACIÓN | UX/UI | UX | Pantallas propuestas incluyen triaje y catálogo no implementados | C-06 | Alta |
| [05-BACKEND-SCHEMA.md](../05-BACKEND-SCHEMA.md) | REQUIERE ALINEACIÓN | Persistencia | Arquitectura de datos | Modelo previo distinto de V1–V3 | C-03 | Alta |
| [06-API-SPECIFICATION.md](../06-API-SPECIFICATION.md) | REQUIERE ALINEACIÓN | API | API/Backend | Mezcla rutas actuales con rutas y permisos inexistentes | C-04 | Alta |
| [07-IMPLEMENTATION-PLAN.md](../07-IMPLEMENTATION-PLAN.md) | PARCIALMENTE VIGENTE | Plan técnico | Gestión técnica | Fases y entregables planeados; avance no acreditado | — | Media |
| [08-SECURITY-THREAT-MODEL.md](../08-SECURITY-THREAT-MODEL.md) | REQUIERE ALINEACIÓN | Seguridad | Seguridad | Amenazas útiles; roles/acciones de triaje fuera del alcance | C-05/C-06 | Alta |
| [09-TEST-STRATEGY.md](../09-TEST-STRATEGY.md) | PARCIALMENTE VIGENTE | Testing | QA | Estrategia útil; casos futuros no son evidencia ejecutada | — | Media |
| [10-TEST-PLAN.md](../10-TEST-PLAN.md) | REQUIERE ALINEACIÓN | Testing | QA | Incluye triaje clínico como caso de prueba del plan | C-06 | Alta |
| [11-DEPLOYMENT-PLAN.md](../11-DEPLOYMENT-PLAN.md) | PARCIALMENTE VIGENTE | Despliegue | DevOps | Plan de entornos; despliegue productivo no demostrado | — | Media |
| [12-MONITORING-MAINTENANCE.md](../12-MONITORING-MAINTENANCE.md) | PARCIALMENTE VIGENTE | Operación | Operación | Métricas/RTO/RPO propuestos; DEC-023 abierto | — | Media |
| [DOMAIN-BASELINE.md](../DOMAIN-BASELINE.md) | VIGENTE | Dominio | Dominio B | Clasificación transversal actual de código y alcance | — | Baja |
| [DOMAIN-DECISION-REGISTER.md](../DOMAIN-DECISION-REGISTER.md) | VIGENTE | Decisiones | Dominio B | 23 decisiones con estados CLOSED/OPEN/PROPOSED/FUTURE | — | Baja |
| [adr/ADR-001-architecture-monolith-modular.md](../adr/ADR-001-architecture-monolith-modular.md) | VIGENTE | ADR | Arquitectura/decisiones | Monolito modular actual | — | Baja |
| [adr/ADR-002-database-postgresql.md](../adr/ADR-002-database-postgresql.md) | VIGENTE | ADR | Arquitectura/decisiones | PostgreSQL actual | — | Baja |
| [adr/ADR-003-authentication-strategy.md](../adr/ADR-003-authentication-strategy.md) | VIGENTE | ADR | Arquitectura/decisiones | Estrategia JWT web; consultar implementación actual | — | Baja |
| [adr/ADR-004-mobile-authentication.md](../adr/ADR-004-mobile-authentication.md) | VIGENTE | ADR | Arquitectura/decisiones | OAuth2/PKCE aprobado como objetivo móvil, no entregado | — | Baja |
| [adr/ADR-005-role-permission-model.md](../adr/ADR-005-role-permission-model.md) | REQUIERE ALINEACIÓN | ADR | Arquitectura/decisiones | Lista TRIAGE/SYSTEM contradice DEC-002 actual | C-05 | Alta |
| [adr/ADR-006-agenda-slot-model.md](../adr/ADR-006-agenda-slot-model.md) | VIGENTE | ADR | Arquitectura/decisiones | Slots discretos; duración/generación pendientes | — | Baja |
| [adr/ADR-007-appointment-state-machine.md](../adr/ADR-007-appointment-state-machine.md) | VIGENTE | ADR | Arquitectura/decisiones | Estados y transiciones actuales; no NO_SHOW | — | Baja |
| [adr/ADR-008-priority-management.md](../adr/ADR-008-priority-management.md) | PARCIALMENTE VIGENTE | ADR | Arquitectura/decisiones | Prioridad como diseño; revisor/política DEC-013 futuros | — | Media |
| [adr/ADR-009-database-identifier-strategy.md](../adr/ADR-009-database-identifier-strategy.md) | VIGENTE | ADR | Arquitectura/decisiones | UUID alineado con V1–V3 | — | Baja |
| [adr/ADR-010-soft-delete-strategy.md](../adr/ADR-010-soft-delete-strategy.md) | VIGENTE | ADR | Arquitectura/decisiones | Soft delete vigente según módulos | — | Baja |
| [adr/ADR-011-audit-storage-strategy.md](../adr/ADR-011-audit-storage-strategy.md) | PARCIALMENTE VIGENTE | ADR | Arquitectura/decisiones | Almacenamiento vigente; retención/acceso DEC-022 abiertos | — | Media |
| [agenda/AGENDA-AVAILABILITY-CONTRACT-DESIGN.md](../agenda/AGENDA-AVAILABILITY-CONTRACT-DESIGN.md) | VIGENTE | Agenda | Arquitectura C | C.3 separa contrato actual/propuesto | — | Baja |
| [agenda/AGENDA-AVAILABILITY-RF-UC-MATRIX.md](../agenda/AGENDA-AVAILABILITY-RF-UC-MATRIX.md) | VIGENTE | Agenda | Requisitos C | Trazabilidad C.1 RF–UC y evidencia actual | — | Baja |
| [agenda/AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md](../agenda/AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md) | VIGENTE | Agenda | Análisis C | C.4 especifica CU-D1–D8 sin promover concepto | — | Baja |
| [agenda/AGENDA-AVAILABILITY-USE-CASES.md](../agenda/AGENDA-AVAILABILITY-USE-CASES.md) | VIGENTE | Agenda | Análisis C | Casos descriptivos C.2 y brechas | — | Baja |
| [agenda/consolidation/AGENDA-ACTOR-USECASE-MATRIX.md](../agenda/consolidation/AGENDA-ACTOR-USECASE-MATRIX.md) | VIGENTE | Agenda | UML D | D.4 actor–caso; SYSTEM solo técnico | — | Baja |
| [agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md](../agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md) | VIGENTE | Agenda | UML D | D.4 RF–CU–escenario–contrato–persistencia–test | — | Baja |
| [agenda/consolidation/AGENDA-UML-CONSOLIDATION-REPORT.md](../agenda/consolidation/AGENDA-UML-CONSOLIDATION-REPORT.md) | VIGENTE | Agenda | UML D | Resumen D.1–D.3 con límites | — | Baja |
| [agenda/consolidation/AGENDA-USE-CASE-CATALOG.md](../agenda/consolidation/AGENDA-USE-CASE-CATALOG.md) | VIGENTE | Agenda | UML D | D.4 clasifica ocho casos | — | Baja |
| [agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md](../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md) | VIGENTE | Agenda | UML D | D.3.3 CU-D2/D3/D6 conceptuales | — | Baja |
| [agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md](../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md) | VIGENTE | Agenda | UML D | D.3.1 CU-D5/D7/D8 implementados | — | Baja |
| [agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md](../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md) | VIGENTE | Agenda | UML D | D.3.2 CU-D1/D4 parciales; Git de corte histórico | — | Baja |
| [agenda/scenarios/CU-D1-PROFESSIONAL-MANAGEMENT-PARTIAL-SCENARIO.puml](../agenda/scenarios/CU-D1-PROFESSIONAL-MANAGEMENT-PARTIAL-SCENARIO.puml) | VIGENTE | Agenda | UML D | D.3 CU-D1 Gestión profesional; flujo PARCIAL | — | Baja |
| [agenda/scenarios/CU-D2-PROFESSIONAL-SPECIALTY-CONCEPTUAL-SCENARIO.puml](../agenda/scenarios/CU-D2-PROFESSIONAL-SPECIALTY-CONCEPTUAL-SCENARIO.puml) | VIGENTE | Agenda | UML D | D.3 CU-D2 Asociación N:M; flujo CONCEPTUAL | — | Baja |
| [agenda/scenarios/CU-D3-SPECIALTY-CATALOG-CONCEPTUAL-SCENARIO.puml](../agenda/scenarios/CU-D3-SPECIALTY-CATALOG-CONCEPTUAL-SCENARIO.puml) | VIGENTE | Agenda | UML D | D.3 CU-D3 Catálogo; flujo CONCEPTUAL | — | Baja |
| [agenda/scenarios/CU-D4-SCHEDULE-CONFIGURATION-PARTIAL-SCENARIO.puml](../agenda/scenarios/CU-D4-SCHEDULE-CONFIGURATION-PARTIAL-SCENARIO.puml) | VIGENTE | Agenda | UML D | D.3 CU-D4 Schedule; flujo PARCIAL | — | Baja |
| [agenda/scenarios/CU-D5-ADMIN-AVAILABILITY-SCENARIO.puml](../agenda/scenarios/CU-D5-ADMIN-AVAILABILITY-SCENARIO.puml) | VIGENTE | Agenda | UML D | D.3 CU-D5 Consulta ADMIN; flujo IMPLEMENTADO | — | Baja |
| [agenda/scenarios/CU-D6-SANITIZED-AVAILABILITY-CONCEPTUAL-SCENARIO.puml](../agenda/scenarios/CU-D6-SANITIZED-AVAILABILITY-CONCEPTUAL-SCENARIO.puml) | VIGENTE | Agenda | UML D | D.3 CU-D6 Vista sanitizada; flujo CONCEPTUAL | — | Baja |
| [agenda/scenarios/CU-D7-APPOINTMENT-RESERVATION-SCENARIO.puml](../agenda/scenarios/CU-D7-APPOINTMENT-RESERVATION-SCENARIO.puml) | VIGENTE | Agenda | UML D | D.3 CU-D7 Reserva; flujo IMPLEMENTADO | — | Baja |
| [agenda/scenarios/CU-D8-CONCURRENT-RESERVATION-SCENARIO.puml](../agenda/scenarios/CU-D8-CONCURRENT-RESERVATION-SCENARIO.puml) | VIGENTE | Agenda | UML D | D.3 CU-D8 Exclusión concurrente; flujo IMPLEMENTADO | — | Baja |
| [agenda/uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.md](../agenda/uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.md) | VIGENTE | Agenda | UML D | D.1 explica actores, relaciones y estados | — | Baja |
| [agenda/uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.puml](../agenda/uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.puml) | VIGENTE | Agenda | UML D | D.1 diagrama fuente general | — | Baja |
| [agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md](../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md) | VIGENTE | Agenda | UML D | D.2 detalla CU-D1–D8 | — | Baja |
| [appointments/APPOINTMENT-MANAGEMENT-ARCHITECTURE.md](../appointments/APPOINTMENT-MANAGEMENT-ARCHITECTURE.md) | VIGENTE | Citas | Appointments | Operaciones de cita actuales y exclusiones temporales | — | Baja |
| [architecture/BACKEND-ARCHITECTURE.md](../architecture/BACKEND-ARCHITECTURE.md) | PARCIALMENTE VIGENTE | Backend | Arquitectura | Monolito válido; pasos de creación ya ejecutados | — | Media |
| [auditoria/AS-IS.md](../auditoria/AS-IS.md) | VIGENTE | AS-IS | Auditoría A | Límites y preguntas; pendiente validación institucional explícita | — | Baja |
| [auditoria/AUDITORIA_DOCUMENTAL.md](../auditoria/AUDITORIA_DOCUMENTAL.md) | HISTÓRICO | Auditoría | Auditoría A | Corte 23/09 anterior al baseline B y C–E | C-09 | Baja |
| [auditoria/MATRIZ_TRAZABILIDAD_BASE.md](../auditoria/MATRIZ_TRAZABILIDAD_BASE.md) | PARCIALMENTE VIGENTE | Trazabilidad | Auditoría A/Requisitos | Tablas viejas + matriz SRS canónica al final | — | Media |
| [auditoria/PLAN_ARTEFACTOS_PENDIENTES.md](../auditoria/PLAN_ARTEFACTOS_PENDIENTES.md) | HISTÓRICO | Plan documental | Auditoría A | Inventario de pendientes al 23/09, previo a D/E | — | Baja |
| [auditoria/TO-BE.md](../auditoria/TO-BE.md) | VIGENTE | TO-BE | Auditoría A | Proceso propuesto explícito, no implementación | — | Baja |
| [contracts/MODULE-CONTRACTS-ARCHITECTURE.md](../contracts/MODULE-CONTRACTS-ARCHITECTURE.md) | PARCIALMENTE VIGENTE | Contratos | Arquitectura modular | Puertos actuales; falta frontera conceptual Catalogs DEC-006 | — | Media |
| [database/DATABASE-DESIGN.md](../database/DATABASE-DESIGN.md) | REQUIERE ALINEACIÓN | Persistencia | Arquitectura de datos | Diseño útil; referencia a V2 inexistente | C-07 | Alta |
| [database/ENTITY-RELATIONSHIP-DIAGRAM.md](../database/ENTITY-RELATIONSHIP-DIAGRAM.md) | REQUIERE ALINEACIÓN | Persistencia | Arquitectura de datos | Archivo vacío; no hay diagrama utilizable | A-01 | Alta |
| [database/MIGRATION-STRATEGY.md](../database/MIGRATION-STRATEGY.md) | REQUIERE ALINEACIÓN | Persistencia | Arquitectura de datos | Archivo vacío; no hay estrategia utilizable | A-01 | Alta |
| [documentation-export/DOCUMENTATION-EXPORT-PLAN.md](../documentation-export/DOCUMENTATION-EXPORT-PLAN.md) | HISTÓRICO | Exportación | Documentación E.1/E.2 | Plan E.1; letras A–E provisionales reemplazadas | — | Baja |
| [documentation-export/DOCUMENTATION-PACKAGE-FINAL-AUDIT-REPORT.md](../documentation-export/DOCUMENTATION-PACKAGE-FINAL-AUDIT-REPORT.md) | VIGENTE | Exportación | Documentación E.1/E.2 | Registro de auditoría E.2.3 | — | Baja |
| [documentation-export/DOCUMENTATION-PACKAGE-FINAL-POST-AUDIT.md](../documentation-export/DOCUMENTATION-PACKAGE-FINAL-POST-AUDIT.md) | VIGENTE | Exportación | Documentación E.1/E.2 | Postauditoría E.2.3 | — | Baja |
| [documentation-export/HOSPITALPLATFORM-AGENDA-AVAILABILITY-DOCUMENTATION.docx](../documentation-export/HOSPITALPLATFORM-AGENDA-AVAILABILITY-DOCUMENTATION.docx) | VIGENTE | Exportación | Documentación E.1/E.2 | Maestro Agenda E.2.1 corregido en E.2.3 | — | Baja |
| [documentation-export/annexes/ANNEX-A-UML-DIAGRAMS.docx](../documentation-export/annexes/ANNEX-A-UML-DIAGRAMS.docx) | VIGENTE | Exportación | Documentación E.1/E.2 | Anexo UML D.1–D.3 | — | Baja |
| [documentation-export/annexes/ANNEX-B-TRACEABILITY-MATRICES.docx](../documentation-export/annexes/ANNEX-B-TRACEABILITY-MATRICES.docx) | VIGENTE | Exportación | Documentación E.1/E.2 | Anexo matrices C.1/D.4 | — | Baja |
| [documentation-export/annexes/ANNEX-C-DOMAIN-DECISIONS.docx](../documentation-export/annexes/ANNEX-C-DOMAIN-DECISIONS.docx) | VIGENTE | Exportación | Documentación E.1/E.2 | Anexo baseline y decisiones | — | Baja |
| [documentation-export/annexes/ANNEX-D-CONTRACT-DESIGN.docx](../documentation-export/annexes/ANNEX-D-CONTRACT-DESIGN.docx) | VIGENTE | Exportación | Documentación E.1/E.2 | Anexo contratos C.3 | — | Baja |
| [documentation-export/annexes/ANNEX-E-IMPLEMENTATION-SCENARIOS.docx](../documentation-export/annexes/ANNEX-E-IMPLEMENTATION-SCENARIOS.docx) | VIGENTE | Exportación | Documentación E.1/E.2 | Anexo D.3.1–D.3.3 | — | Baja |
| [modelado/README.md](../modelado/README.md) | VIGENTE | Modelado | Análisis de procesos | Índice y advertencia TO-BE/convenciones Mermaid | — | Baja |
| [modelado/as-is/AS-IS-MODELING-LIMITS.md](../modelado/as-is/AS-IS-MODELING-LIMITS.md) | VIGENTE | AS-IS | Modelado | Límite de evidencia institucional explícito | — | Baja |
| [modelado/bpmn/BPMN-00-MACROPROCESO.md](../modelado/bpmn/BPMN-00-MACROPROCESO.md) | VIGENTE | Modelado BPMN | Análisis de procesos | Ficha TO-BE: macroproceso TO-BE; pendientes señalados | — | Baja |
| [modelado/bpmn/BPMN-00-MACROPROCESO.mmd](../modelado/bpmn/BPMN-00-MACROPROCESO.mmd) | PARCIALMENTE VIGENTE | Modelado BPMN | Análisis de procesos | Figura: macroproceso TO-BE; requiere ficha TO-BE | — | Media |
| [modelado/bpmn/BPMN-01-IDENTIDAD.md](../modelado/bpmn/BPMN-01-IDENTIDAD.md) | VIGENTE | Modelado BPMN | Análisis de procesos | Ficha TO-BE: identidad y acceso; pendientes señalados | — | Baja |
| [modelado/bpmn/BPMN-01-IDENTIDAD.mmd](../modelado/bpmn/BPMN-01-IDENTIDAD.mmd) | PARCIALMENTE VIGENTE | Modelado BPMN | Análisis de procesos | Figura: identidad y acceso; requiere ficha TO-BE | — | Media |
| [modelado/bpmn/BPMN-02-RESERVA.md](../modelado/bpmn/BPMN-02-RESERVA.md) | VIGENTE | Modelado BPMN | Análisis de procesos | Ficha TO-BE: consulta y reserva; pendientes señalados | — | Baja |
| [modelado/bpmn/BPMN-02-RESERVA.mmd](../modelado/bpmn/BPMN-02-RESERVA.mmd) | PARCIALMENTE VIGENTE | Modelado BPMN | Análisis de procesos | Figura: consulta y reserva; requiere ficha TO-BE | — | Media |
| [modelado/bpmn/BPMN-03-GESTION-CITA.md](../modelado/bpmn/BPMN-03-GESTION-CITA.md) | VIGENTE | Modelado BPMN | Análisis de procesos | Ficha TO-BE: ciclo de cita; pendientes señalados | — | Baja |
| [modelado/bpmn/BPMN-03-GESTION-CITA.mmd](../modelado/bpmn/BPMN-03-GESTION-CITA.mmd) | PARCIALMENTE VIGENTE | Modelado BPMN | Análisis de procesos | Figura: ciclo de cita; requiere ficha TO-BE | — | Media |
| [modelado/bpmn/BPMN-04-ESPERA.md](../modelado/bpmn/BPMN-04-ESPERA.md) | VIGENTE | Modelado BPMN | Análisis de procesos | Ficha TO-BE: waitlist/ofertas futuras; pendientes señalados | — | Baja |
| [modelado/bpmn/BPMN-04-ESPERA.mmd](../modelado/bpmn/BPMN-04-ESPERA.mmd) | PARCIALMENTE VIGENTE | Modelado BPMN | Análisis de procesos | Figura: waitlist/ofertas futuras; requiere ficha TO-BE | — | Media |
| [modelado/bpmn/BPMN-05-PRIORIDAD.md](../modelado/bpmn/BPMN-05-PRIORIDAD.md) | VIGENTE | Modelado BPMN | Análisis de procesos | Ficha TO-BE: prioridad futura; pendientes señalados | — | Baja |
| [modelado/bpmn/BPMN-05-PRIORIDAD.mmd](../modelado/bpmn/BPMN-05-PRIORIDAD.mmd) | PARCIALMENTE VIGENTE | Modelado BPMN | Análisis de procesos | Figura: prioridad futura; requiere ficha TO-BE | — | Media |
| [modelado/bpmn/BPMN-06-FLUJO-CONSULTA.md](../modelado/bpmn/BPMN-06-FLUJO-CONSULTA.md) | VIGENTE | Modelado BPMN | Análisis de procesos | Ficha TO-BE: flujo actual limitado; pendientes señalados | — | Baja |
| [modelado/bpmn/BPMN-06-FLUJO-CONSULTA.mmd](../modelado/bpmn/BPMN-06-FLUJO-CONSULTA.mmd) | PARCIALMENTE VIGENTE | Modelado BPMN | Análisis de procesos | Figura: flujo actual limitado; requiere ficha TO-BE | — | Media |
| [modelado/bpmn/BPMN-07-ADMINISTRACION.md](../modelado/bpmn/BPMN-07-ADMINISTRACION.md) | VIGENTE | Modelado BPMN | Análisis de procesos | Ficha TO-BE: administración e indicadores propuestos; pendientes señalados | — | Baja |
| [modelado/bpmn/BPMN-07-ADMINISTRACION.mmd](../modelado/bpmn/BPMN-07-ADMINISTRACION.mmd) | PARCIALMENTE VIGENTE | Modelado BPMN | Análisis de procesos | Figura: administración e indicadores propuestos; requiere ficha TO-BE | — | Media |
| [modelado/idef0/IDEF0-A-0.md](../modelado/idef0/IDEF0-A-0.md) | VIGENTE | Modelado IDEF0 | Análisis de procesos | Ficha TO-BE: contexto ICOM | — | Baja |
| [modelado/idef0/IDEF0-A-0.mmd](../modelado/idef0/IDEF0-A-0.mmd) | PARCIALMENTE VIGENTE | Modelado IDEF0 | Análisis de procesos | Figura: contexto ICOM; requiere ficha TO-BE | — | Media |
| [modelado/idef0/IDEF0-A0.md](../modelado/idef0/IDEF0-A0.md) | VIGENTE | Modelado IDEF0 | Análisis de procesos | Ficha TO-BE: descomposición ICOM | — | Baja |
| [modelado/idef0/IDEF0-A0.mmd](../modelado/idef0/IDEF0-A0.mmd) | PARCIALMENTE VIGENTE | Modelado IDEF0 | Análisis de procesos | Figura: descomposición ICOM; requiere ficha TO-BE | — | Media |
| [modelado/idef0/IDEF0-TRAZABILIDAD.md](../modelado/idef0/IDEF0-TRAZABILIDAD.md) | VIGENTE | Modelado IDEF0 | Análisis de procesos | Balance de flechas A-0/A0 propuesto | — | Baja |
| [patients/PATIENT-MANAGEMENT-ARCHITECTURE.md](../patients/PATIENT-MANAGEMENT-ARCHITECTURE.md) | REQUIERE ALINEACIÓN | Pacientes | Arquitectura Patients | Gestión básica actual; incluye SYSTEM como rol | C-05 | Alta |
| [planning/00-PROJECT-ROADMAP.docx](../planning/00-PROJECT-ROADMAP.docx) | HISTÓRICO | Planificación | Gestión de proyecto | Copia Word anterior del roadmap | — | Baja |
| [planning/PROJECT-CHARTER-HOSPITAL-HUAYCAN.docx](../planning/PROJECT-CHARTER-HOSPITAL-HUAYCAN.docx) | HISTÓRICO | Planificación | Gestión de proyecto | Versión Word de charter preliminar | — | Baja |
| [planning/PROJECT-CHARTER-HOSPITAL-HUAYCAN.md](../planning/PROJECT-CHARTER-HOSPITAL-HUAYCAN.md) | HISTÓRICO | Planificación | Gestión de proyecto | Charter preliminar sin aval institucional | — | Baja |
| [planning/gantt.md](../planning/gantt.md) | HISTÓRICO | Planificación | Gestión de proyecto | Cronograma inicial; no prueba avance | — | Baja |
| [planning/gantt.xlsx](../planning/gantt.xlsx) | HISTÓRICO | Planificación | Gestión de proyecto | Hoja Gantt/Hitos de 18 semanas; plan, no ejecución | — | Baja |
| [planning/lean-canvas.md](../planning/lean-canvas.md) | HISTÓRICO | Planificación | Gestión de proyecto | Hipótesis de negocio iniciales | — | Baja |
| [planning/project-charter.md](../planning/project-charter.md) | HISTÓRICO | Planificación | Gestión de proyecto | Borrador académico inicial del charter | — | Baja |
| [planning/requirements-elicitation.md](../planning/requirements-elicitation.md) | PARCIALMENTE VIGENTE | Planificación | Gestión de proyecto | Instrumento útil; levantamiento sin ejecutar | — | Media |
| [planning/wbs.md](../planning/wbs.md) | PARCIALMENTE VIGENTE | Planificación | Gestión de proyecto | EDT propuesta; progreso no verificado | — | Media |
| [professionals/PROFESSIONAL-MANAGEMENT-ARCHITECTURE.md](../professionals/PROFESSIONAL-MANAGEMENT-ARCHITECTURE.md) | PARCIALMENTE VIGENTE | Profesionales | Arquitectura Professionals | Perfil actual + integración llamada futura y specialty conceptual | C-08 | Media |
| [references/metodologia/Hernandez-Sampieri-2014/Metodologia de la Investigacion_ Hernández, S_ Fernandez, C_ Baptista, L_ 6ta Ed 2014.pdf](<../references/metodologia/Hernandez-Sampieri-2014/Metodologia de la Investigacion_ Hernández, S_ Fernandez, C_ Baptista, L_ 6ta Ed 2014.pdf>) | HISTÓRICO | Referencia externa | Fuente externa/docente | Texto metodológico externo; no especifica backend | — | Baja |
| [references/profesor/guias/Ficha de Trabajo de Investigación_TKCTFW.docx](<../references/profesor/guias/Ficha de Trabajo de Investigación_TKCTFW.docx>) | HISTÓRICO | Referencia externa | Fuente externa/docente | Guía docente; no especifica backend | — | Baja |
| [references/profesor/guias/TAREA SEMANA 01.pptx](<../references/profesor/guias/TAREA SEMANA 01.pptx>) | HISTÓRICO | Referencia externa | Fuente externa/docente | Guía docente; no especifica backend | — | Baja |
| [references/profesor/ppt-referencia/REFERENCIA PPT DEL PROFESOR. CONTENIDO ACORTADO.pptx](<../references/profesor/ppt-referencia/REFERENCIA PPT DEL PROFESOR. CONTENIDO ACORTADO.pptx>) | HISTÓRICO | Referencia externa | Fuente externa/docente | Ejemplo docente/tesis; no especifica backend | — | Baja |
| [references/profesor/tesis-referencia/TESIS SAAVEDRA FREDDY_SAAVEDRA ULISES.pdf](<../references/profesor/tesis-referencia/TESIS SAAVEDRA FREDDY_SAAVEDRA ULISES.pdf>) | HISTÓRICO | Referencia externa | Fuente externa/docente | Ejemplo docente/tesis; no especifica backend | — | Baja |
| [requisitos/EQUIVALENCIAS_REQUISITOS.md](../requisitos/EQUIVALENCIAS_REQUISITOS.md) | PARCIALMENTE VIGENTE | Requisitos | Análisis de requisitos | Concilia IDs viejos/canónicos; aprobación aún pendiente | C-01 | Media |
| [requisitos/REGLAS_NEGOCIO.md](../requisitos/REGLAS_NEGOCIO.md) | PARCIALMENTE VIGENTE | Reglas | Análisis de requisitos | RB propuestas y decisiones pendientes explícitas | — | Media |
| [requisitos/SRS-HOSPITALPLATFORM.md](../requisitos/SRS-HOSPITALPLATFORM.md) | PARCIALMENTE VIGENTE | SRS | Análisis de requisitos | RF-001–033 canónicos propuestos; validación pendiente | — | Media |
| [research/recursos/Acta-de-constitucion.md](../research/recursos/Acta-de-constitucion.md) | HISTÓRICO | Investigación | Equipo académico | Acta académica inicial; aprobación no acreditada | — | Baja |
| [research/recursos/CAPITULO-I.md](../research/recursos/CAPITULO-I.md) | HISTÓRICO | Investigación | Equipo académico | Planteamiento académico inicial | — | Baja |
| [research/recursos/CAPITULO-III.md](../research/recursos/CAPITULO-III.md) | HISTÓRICO | Investigación | Equipo académico | Capítulo académico; estado técnico y RF antiguos | C-09 | Alta |
| [research/recursos/Diagrama-de-gantt_G1.md](../research/recursos/Diagrama-de-gantt_G1.md) | HISTÓRICO | Investigación | Equipo académico | Cronograma académico inicial | — | Baja |
| [research/semana-01/README.md](../research/semana-01/README.md) | HISTÓRICO | Investigación | Equipo académico | Entrega/fuente de semana 01; antecedente | — | Baja |
| [research/semana-01/entregables/Marco_Teorico_Semana_01.docx](../research/semana-01/entregables/Marco_Teorico_Semana_01.docx) | HISTÓRICO | Investigación | Equipo académico | Entrega/fuente de semana 01; antecedente | — | Baja |
| [research/semana-01/entregables/PPT Sistemas Citas EsSalud.pptx.pdf](<../research/semana-01/entregables/PPT Sistemas Citas EsSalud.pptx.pdf>) | HISTÓRICO | Investigación | Equipo académico | Entrega/fuente de semana 01; antecedente | — | Baja |
| [research/semana-01/fuentes.md](../research/semana-01/fuentes.md) | HISTÓRICO | Investigación | Equipo académico | Entrega/fuente de semana 01; antecedente | — | Baja |
| [research/semana-01/marco-teorico.md](../research/semana-01/marco-teorico.md) | HISTÓRICO | Investigación | Equipo académico | Entrega/fuente de semana 01; antecedente | — | Baja |
| [research/semana-01/problema-investigacion.md](../research/semana-01/problema-investigacion.md) | HISTÓRICO | Investigación | Equipo académico | Entrega/fuente de semana 01; antecedente | — | Baja |
| [research/semana-02/Entregables/GRUPO 1 CURSO INTEGRADOR I SISTEMAS SOFTWARE.docx](<../research/semana-02/Entregables/GRUPO 1 CURSO INTEGRADOR I SISTEMAS SOFTWARE.docx>) | HISTÓRICO | Investigación | Equipo académico | Entrega de semana 02; antecedente | — | Baja |
| [research/semana-02/Entregables/PPT.pdf](../research/semana-02/Entregables/PPT.pdf) | HISTÓRICO | Investigación | Equipo académico | Entrega de semana 02; antecedente | — | Baja |
| [research/semana-03/GRUPO 1 CURSO INTEGRADOR I v5.0.docx](<../research/semana-03/GRUPO 1 CURSO INTEGRADOR I v5.0.docx>) | HISTÓRICO | Investigación | Equipo académico | Entrega de semana 03; antecedente | — | Baja |
| [security/SECURITY-ARCHITECTURE.md](../security/SECURITY-ARCHITECTURE.md) | REQUIERE ALINEACIÓN | Seguridad | Arquitectura Security | JWT actual; TRIAGE/SYSTEM listados como iniciales | C-05 | Alta |
| [users/USER-MANAGEMENT-ARCHITECTURE.md](../users/USER-MANAGEMENT-ARCHITECTURE.md) | REQUIERE ALINEACIÓN | Usuarios | Arquitectura Users | Operaciones actuales; lista TRIAGE/SYSTEM heredada | C-05 | Alta |

## 4. Clasificación documental

| Estado | Archivos | Lectura |
|---|---:|---|
| VIGENTE | 56 | Referencia fiel a su alcance declarado |
| PARCIALMENTE VIGENTE | 27 | Con contenido válido y horizonte/validación pendiente |
| HISTÓRICO | 28 | Conservar como corte o referencia externa |
| REQUIERE ALINEACIÓN | 14 | Contradicción o marcador vacío identificado |

Total: **125 archivos**. Estado documental y estado de implementación son ejes distintos; esta tabla no aprueba requisitos ni cierra decisiones.

Un informe de un escenario `PARCIAL` puede ser documentalmente `VIGENTE` cuando separa bien lo implementado de lo pendiente. Las versiones `FINAL` de 01–12 son rótulos editoriales y no acreditan aprobación docente, institucional ni producto terminado. Los materiales externos de `references/` y los entregables semanales quedan inventariados como fuentes archivadas, no como especificaciones funcionales del backend.

## 5. Documentos vigentes

La línea base de dominio y el registro de decisiones establecen las etiquetas actuales. C.1–C.4, D.1–D.4 y el paquete Word E.2 conservan la clasificación CU-D1/D4 `PARCIAL`, CU-D2/D3/D6 `CONCEPTUAL` y CU-D5/D7/D8 `IMPLEMENTADO`. El paquete Word sirve al incremento Agenda, no sustituye el resto del repositorio. Los ADR que describen decisiones aún válidas se leen en su alcance: `CLOSED` o “Aceptado” no prueba código. Las fichas AS-IS/TO-BE explícitamente pendientes o propuestas son vigentes **como hipótesis documentales**, no como proceso hospitalario certificado.

## 6. Documentos parcialmente vigentes

El roadmap, flujos, planes de implementación, despliegue y monitoreo mezclan backend existente con alcance MVP total y trabajo futuro. La SRS 0.1, las equivalencias y las reglas de negocio mantienen IDs canónicos y advertencias de validación, pero todavía requieren decisiones y evidencia para convertirse en línea base aprobada. Algunas arquitecturas de módulos conservan contratos actuales mientras usan frases de “integración futura” ya superadas por el código. Los `.mmd` de modelado deben leerse junto a su ficha Markdown y `modelado/README.md`: aislados no muestran de forma suficiente su carácter TO-BE propuesto.

## 7. Documentos históricos

La auditoría A y el plan de artefactos pendientes tienen corte 23/09/2026 y fueron sucedidos por la línea base B y las etapas C–E. El plan E.1 guarda su asignación editorial original de anexos A–E; la correspondencia vigente es la tabla corregida del maestro E.2. Los planes iniciales, actas, cronogramas y entregables semanales conservan procedencia académica y no acreditan avance, aprobación ni AS-IS observado. Las cinco referencias externas ignoradas por Git se inventariaron para completitud; no son fuentes de verdad funcional del proyecto.

## 8. Documentos con necesidad de alineación

La primera revisión editorial debe concentrarse en el PRD, TRD, UI/UX, Backend Schema, API Specification, seguridad, test plan, ADR-005 y documentos de usuarios/pacientes que aún enumeran `TRIAGE`/`SYSTEM` como roles actuales. `DATABASE-DESIGN.md` cita una V2 distinta a la migración existente. Los dos archivos vacíos de `database/` necesitan una decisión documental: contenido verificable o señalización inequívoca de marcador, sin inventar diseño. Esta etapa solo registra la necesidad.

## 9. Contradicciones encontradas

| ID | Evidencia documental | Contraste actual | Consecuencia de uso |
|---|---|---|---|
| C-01 | [PRD](../01-PRD.md) RF-014/RF-020 describen atención clínica y triaje; su numeración RF se superpone con la serie canónica. | [SRS](../requisitos/SRS-HOSPITALPLATFORM.md) y [equivalencias](../requisitos/EQUIVALENCIAS_REQUISITOS.md) excluyen esos RF clínicos del MVP; `RF-014` canónico es concurrencia de reserva. | Evitar citar un ID sin fuente y versión. |
| C-02 | [TRD](../02-TRD.md) prescribe Flutter, PostgreSQL 16 y módulo de triaje clínico. | [Roadmap](../00-PROJECT-ROADMAP.md) describe React Native/Expo y PostgreSQL 17 como objetivos; [baseline](../DOMAIN-BASELINE.md) excluye triaje clínico del estado actual. | Alinear objetivo técnico y separar futuro de actual. |
| C-03 | [Backend Schema](../05-BACKEND-SCHEMA.md) usa `NO_ASISTIO`, tablas de estado y esquema en castellano. | ADR-007, [baseline](../DOMAIN-BASELINE.md) y V1–V3 conservan `AppointmentStatus` sin `NO_SHOW` y otro esquema físico. | No usar el ejemplo SQL como migración vigente. |
| C-04 | [API Specification](../06-API-SPECIFICATION.md) enumera `POST /api/v1/auth/register`, `GET /api/v1/auth/me`, CRUD de `/specialties`, `GET /api/v1/dashboard/metrics`, un `DashboardMetricResponse` y permisos granulares como si fueran contrato API. | Los controllers actuales no exponen esas rutas; no hay DTO de dashboard en Java. DEC-019 da primacía al comportamiento actual. CU-D3/D6 son conceptuales. | Alto riesgo de contratos cliente falsos. |
| C-05 | [ADR-005](../adr/ADR-005-role-permission-model.md), [seguridad](../security/SECURITY-ARCHITECTURE.md), [usuarios](../users/USER-MANAGEMENT-ARCHITECTURE.md) y [pacientes](../patients/PATIENT-MANAGEMENT-ARCHITECTURE.md) incluyen `TRIAGE` o `SYSTEM` entre roles iniciales. | DEC-002 `CLOSED` limita el caso actual a `ADMIN`, `PATIENT`, `RECEPTIONIST`, `PROFESSIONAL`; `SYSTEM` en CU-D8 es técnico interno. | No inferir permiso vigente por un rol enumerado históricamente. |
| C-06 | [UI/UX](../04-UI-UX-DESIGN-BRIEF.md), [TRD](../02-TRD.md) y [Test Plan](../10-TEST-PLAN.md) describen evaluación o pantalla de triaje clínico. | [Baseline](../DOMAIN-BASELINE.md) y ADR-007 excluyen datos clínicos y etapas adicionales del flujo actual. | Prototipos y casos de prueba no deben presentarse como entrega implementada. |
| C-07 | [Database Design](../database/DATABASE-DESIGN.md) cita `V2__appointment_constraints.sql`. | La V2 real es `V2__create_refresh_tokens.sql`; V3 soporta lifecycle/unicidad activa. | No citar la migración inexistente como evidencia. |
| C-08 | [Professional Architecture](../professionals/PROFESSIONAL-MANAGEMENT-ARCHITECTURE.md) aún llama “futura” la integración con Agenda/Appointments y mezcla información de especialidad con perfil profesional. | Contratos actuales ya integran esos módulos; DEC-006 asigna definiciones a Catalogs y N:M a Professionals solo conceptualmente. | Separar integración actual de ownership conceptual. |
| C-09 | [Capítulo III](../research/recursos/CAPITULO-III.md) afirma Spring Boot 4.1.0, módulos funcionales vacíos y unicidad antigua de citas. | POM y backend actuales usan Spring Boot 3.5.14 y módulos operativos; V3 cambia la restricción de cita activa. | Conservar como corte histórico; no citarlo como estado técnico actual. |
| A-01 | [Entity Relationship Diagram](../database/ENTITY-RELATIONSHIP-DIAGRAM.md) y [Migration Strategy](../database/MIGRATION-STRATEGY.md) tienen 0 bytes. | No existe contenido que contrastar con V1–V3. | Ausencia de artefacto utilizable, no contradicción de dominio. |

**Gaps sin promoción de estado:** `catalogs`, `waitlist`, `priority`, `notifications` y `dashboard` contienen solo `package-info.java` en sus paquetes Java; su mención en TRD o diseño de datos expresa objetivo, no módulo funcional. La tabla SQL `specialties` y la unión N:M no prueban CRUD de Catalogs; CU-D6 carece de endpoint/permiso; DEC-008/010 siguen `OPEN`; RF-014 conserva ensayo SRS de 20 solicitudes pendiente frente a una prueba de dos. Son brechas conocidas, no defectos nuevos de las fuentes C/D/E.

## 10. Priorización de revisión

1. **Alta — contrato y seguridad:** 06 API Specification, ADR-005, arquitectura de seguridad/usuarios/pacientes. Verificar cada ruta y rol contra controllers, DEC-002 y DEC-019 antes de publicarlos como referencia de integración.
2. **Alta — dominio y persistencia:** 01 PRD, 02 TRD, 05 Backend Schema, Database Design y archivos vacíos. Mantener trazabilidad de IDs viejos a SRS; no modificar migraciones en E.3.2.
3. **Media — UX y pruebas:** 04 UI/UX, 10 Test Plan, 09 Test Strategy y SRS. Separar triaje clínico excluido, flujos futuros y evidencias realmente ejecutadas; RF-014 20 solicitudes sigue pendiente.
4. **Media — arquitectura y planes:** Professional Architecture, Backend Architecture, contratos y planes 00/03/07/11/12. Anotar integración actual frente a diseño futuro.
5. **Baja — archivo y navegación:** planes y entregables académicos anteriores, `.mmd` aislados y plan E.1. Preservar procedencia y enlazar a la fuente vigente, sin reescribir historia.

## 11. Recomendaciones para E.3.2

- Definir una tabla de precedencia por área: código/API vigente, dominio B, requisitos SRS propuestos, C/D para Agenda y exportación Word como copia controlada. Mantener las etiquetas de implementación separadas de las documentales.
- Revisar primero las contradicciones C-01–C-08 con diffs pequeños y trazables. Cada corrección futura debe citar fuente vigente, responsable de aprobación y efecto en documentos dependientes; E.3.1 no decide políticas.
- Mantener el Capítulo III, los entregables semanales, el plan E.1 y las auditorías anteriores como cortes históricos. Anotar procedencia si se enlazan desde documentación actual.
- Resolver editorialmente los dos marcadores vacíos solo cuando haya una fuente aprobada; no generar un ERD ni una estrategia de migración por inferencia.
- No cerrar DEC-003/005/008/010/017/022/023 ni convertir propuestas de specialty, disponibilidad sanitizada, waitlist, prioridad o notificaciones en contratos actuales.

## 12. Validación final

- Se comprobó la rama, el estado inicial y el inventario físico con inclusión de archivos ignorados; cada uno aparece una vez en la tabla.
- Los hallazgos se sustentan en contraste documental y lectura estática de controllers, DTOs, tests y migraciones; no se ejecutaron pruebas ni se alteró el sistema.
- **Java modificado: 0. SQL modificado: 0. Migraciones: 0. Tests: 0. Seguridad: 0. Documentación existente: 0.**
- Se verifican al cierre `git status` y `git diff --check`. No se hizo commit, push ni merge.
