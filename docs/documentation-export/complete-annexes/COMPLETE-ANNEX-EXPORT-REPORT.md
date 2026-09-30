# ETAPA E.3.3.2 — COMPLETE TECHNICAL ANNEX EXPORT REPORT

**Proyecto:** HOSPITALPLATFORM — caso de estudio académico del Hospital de Huaycán

**Fecha de corte:** 30 de septiembre de 2026

**Rama:** `feat/documentation-word-complete-export`

## 1. Archivos creados

| Anexo | Contenido | Páginas | Tablas | Diagramas UML |
|---|---|---:|---:|---:|
| [A — Arquitectura](ANNEX-A-ARCHITECTURE.docx) | Backend, módulos, ownership, dependencias y ADR relacionados | 5 | 4 | 0 |
| [B — API y contratos](ANNEX-B-API-CONTRACTS.docx) | Rutas actuales, contratos modulares y diseños propuestos separados | 5 | 4 | 0 |
| [C — Modelo de datos](ANNEX-C-DATABASE-DESIGN.docx) | Persistencia, relaciones y migraciones V1/V2/V3 | 4 | 4 | 0 |
| [D — Seguridad](ANNEX-D-SECURITY.docx) | Roles, autorización, controles actuales y amenazas documentadas | 4 | 4 | 0 |
| [E — Testing](ANNEX-E-TESTING.docx) | Estrategia, plan, evidencia al corte y brechas | 3 | 4 | 0 |
| [F — Dominio y Agenda](ANNEX-F-DOMAIN-AGENDA.docx) | Baseline, 23 decisiones, CU-D1 a CU-D8 y UML | 15 | 4 | 9 modelos en 10 imágenes |
| **Total** | **Seis DOCX** | **36** | **24** | **9 modelos en 10 imágenes** |

Los anexos A–E no incorporan diagramas nuevos: las fuentes alineadas de esas áreas no aportan un UML aprobado para insertar. El anexo F contiene el diagrama general D.1 y los ocho escenarios vigentes; CU-D4 utiliza dos imágenes por su extensión.

## 2. Fuentes y criterio de autoridad

Se utilizaron [DOMAIN-BASELINE](../../DOMAIN-BASELINE.md), [DOMAIN-DECISION-REGISTER](../../DOMAIN-DECISION-REGISTER.md), los Markdown alineados de [arquitectura](../../architecture/BACKEND-ARCHITECTURE.md), [contratos](../../contracts/MODULE-CONTRACTS-ARCHITECTURE.md), [API](../../06-API-SPECIFICATION.md), [base de datos](../../database/DATABASE-DESIGN.md), [seguridad](../../security/SECURITY-ARCHITECTURE.md), [amenazas](../../08-SECURITY-THREAT-MODEL.md), [estrategia](../../09-TEST-STRATEGY.md) y [plan de pruebas](../../10-TEST-PLAN.md); además C.1–C.4, D.1–D.4, los reportes [E.3.2](../../documentation-alignment/DOCUMENTATION-ALIGNMENT-REPORT.md) y [E.3.2.1](../../documentation-alignment/DOCUMENTATION-ALIGNMENT-POST-AUDIT.md), y el [maestro E.3.3.1](../HOSPITALPLATFORM-COMPLETE-DOCUMENTATION.docx). Cada anexo incluye su tabla de fuentes con rutas concretas.

Para la API funcional se dio prioridad al baseline alineado y a la delimitación DEC-019; los ejemplos históricos de la especificación API se presentan como no implementados. Los ADR relevantes se leen junto con el registro de decisiones y no se interpretan como prueba de código. La rasterización UML proviene del maestro actualizado y se cotejó con D.1 y D.3.1–D.3.3.

## 3. Validación funcional y documental

- Los seis DOCX abren correctamente como paquetes Office y Microsoft Word los exportó a PDF sin error.
- Se inspeccionaron visualmente los 36 folios rasterizados. Portadas, títulos, tablas, encabezados, pies y diagramas aparecen sin recortes observados.
- Se preserva la clasificación: CU-D5/D7/D8 **IMPLEMENTADO**, CU-D1/D4 **PARCIAL**, CU-D2/D3/D6 **CONCEPTUAL**. La generación de slots, reglas temporales, waitlist, prioridad y notificaciones no se presentan como funcionalidad actual.
- DEC-005/008/010 y las demás decisiones **OPEN** permanecen abiertas; **PROPOSED** y **FUTURE** no se convierten en contratos ejecutables.
- El anexo C distingue tablas físicas V1 de módulos Java; el anexo B distingue endpoints actuales de rutas históricas y contratos propuestos; el anexo D mantiene solo PATIENT, RECEPTIONIST, PROFESSIONAL y ADMIN como actores operativos actuales.
- El anexo E conserva RF-014: **2 solicitudes concurrentes probadas** y **20 solicitudes SRS pendientes**. Las cifras de suites pertenecen al corte del baseline; no se presentan como una nueva ejecución.

El renderizador empaquetado requería `soffice.exe`, ausente en este entorno. La validación visual se realizó mediante exportación local de Microsoft Word y rasterización de los PDF de control. Los PDF e imágenes fueron temporales y no forman parte del entregable.

## 4. Control de alcance

| Tipo de archivo | Cambios en E.3.3.2 |
|---|---:|
| DOCX nuevos | 6 |
| Reporte Markdown nuevo | 1 |
| Java | 0 |
| SQL | 0 |
| Tests | 0 |
| Migraciones | 0 |
| Seguridad de aplicación | 0 |
| Configuración | 0 |

Se comprobó `git status` y `git diff --check`. No se ejecutó Maven ni se hizo commit, push o merge. El maestro y su reporte, creados en E.3.3.1 y aún sin seguimiento en esta rama, no se editaron en esta etapa.

## 5. Resultado

**🟢 TECHNICAL ANNEXES READY.** Los seis anexos están generados, abren correctamente y mantienen la separación entre comportamiento vigente, alcance parcial, diseño conceptual y trabajo futuro.
