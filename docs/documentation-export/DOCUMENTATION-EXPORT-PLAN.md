# ETAPA E.1 — Plan de exportación documental de HOSPITALPLATFORM

## Propósito y límite

Inventariar y ordenar la documentación existente del incremento **Agenda Availability + Specialty Policy** para una futura edición en Word. Esta etapa no genera `.docx`, no modifica las fuentes ni reinterpreta decisiones. HOSPITALPLATFORM es un caso académico; la documentación no acredita aprobación institucional del Hospital de Huaycán.

**Prioridad editorial:** **Alta** = necesaria para entender el entregable y sus estados; **Media** = respaldo técnico o histórico que debe acompañarlo. La prioridad no cambia la prioridad de RF, DEC ni el estado de implementación. **Destino Word** designa una ubicación propuesta para una exportación posterior, no un archivo ya creado.

## 1. Inventario documental

| Documento | Propósito comprobado | Categoría | Prioridad | Destino Word propuesto |
|---|---|---|---|---|
| [DOMAIN-BASELINE.md](../DOMAIN-BASELINE.md) | Fuente transversal de alcance, actores, módulos, RF, API, persistencia, pruebas y contradicciones. | **REFERENCIA INTERNA** | Alta | Citas verificables en capítulos 2–3; conservar fuente íntegra en el repositorio. |
| [DOMAIN-DECISION-REGISTER.md](../DOMAIN-DECISION-REGISTER.md) | Inventario y estado de decisiones DEC; distingue aprobaciones de diseño y asuntos abiertos. | **REFERENCIA INTERNA** | Alta | Citas verificables en capítulo 9; conservar fuente íntegra en el repositorio. |
| [AGENDA-AVAILABILITY-RF-UC-MATRIX.md](../agenda/AGENDA-AVAILABILITY-RF-UC-MATRIX.md) | C.1: vínculo inicial RF–caso, inventario de endpoints/contratos reales y brechas. | **ANEXO TÉCNICO** | Alta | Anexo A, íntegro; tablas anchas en página horizontal si hace falta. |
| [AGENDA-AVAILABILITY-USE-CASES.md](../agenda/AGENDA-AVAILABILITY-USE-CASES.md) | C.2: descomposición descriptiva de ocho casos y límites entre actual, aprobado y pendiente. | **ANEXO TÉCNICO** | Media | Anexo B, íntegro; preservar el aviso de que los IDs UC no son oficiales. |
| [AGENDA-AVAILABILITY-CONTRACT-DESIGN.md](../agenda/AGENDA-AVAILABILITY-CONTRACT-DESIGN.md) | C.3: contratos existentes frente a propuestas y límites modulares. | **ANEXO TÉCNICO** | Media | Anexo C, íntegro; conservar etiquetas **EXISTENTE**/**PROPUESTO**. |
| [AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md](../agenda/AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md) | C.4: especificación CU-D1–CU-D8, reglas, dependencias y brechas. | **ANEXO TÉCNICO** | Alta | Anexo D, íntegro; referencia de detalle para capítulos 5–8. |
| [AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.md](../agenda/uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.md) | D.1: explicación del diagrama general, actores, relaciones y límites. | **DOCUMENTO PRINCIPAL** | Alta | Capítulo 6; figura derivada del [PlantUML D.1](../agenda/uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.puml) y texto explicativo. |
| [AGENDA-AVAILABILITY-USE-CASE-DETAILS.md](../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md) | D.2: fichas de ocho casos con flujo, estado, seguridad, persistencia y pruebas. | **DOCUMENTO PRINCIPAL** | Alta | Capítulo 5; conservar fichas completas y sus estados. |
| [AGENDA-IMPLEMENTED-SCENARIOS.md](../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md) | D.3.1: evidencia y límites de CU-D5, CU-D7 y CU-D8 implementados. | **DOCUMENTO PRINCIPAL** | Alta | Capítulo 7, sección IMPLEMENTADO; figuras de los tres escenarios `.puml`. |
| [AGENDA-PARTIAL-SCENARIOS.md](../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md) | D.3.2: auditoría detallada de CU-D1 y CU-D4, incluidos hallazgos y estado Git de aquel corte. | **ANEXO TÉCNICO** | Media | Anexo E, íntegro y rotulado como registro histórico; capítulo 7 enlaza sus escenarios `.puml`. |
| [AGENDA-CONCEPTUAL-SCENARIOS.md](../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md) | D.3.3: CU-D2, CU-D3 y CU-D6 sin atribuirles API ni permisos vigentes. | **DOCUMENTO PRINCIPAL** | Alta | Capítulo 7, sección CONCEPTUAL; figuras de los tres escenarios `.puml`. |
| [AGENDA-USE-CASE-CATALOG.md](../agenda/consolidation/AGENDA-USE-CASE-CATALOG.md) | D.4: catálogo de CU-D1–CU-D8, RF, actores, módulos, evidencia y dependencias. | **DOCUMENTO PRINCIPAL** | Alta | Capítulo 5, antes de las fichas D.2. |
| [AGENDA-ACTOR-USECASE-MATRIX.md](../agenda/consolidation/AGENDA-ACTOR-USECASE-MATRIX.md) | D.4: participación actor–caso sin otorgar permisos; SYSTEM solo técnico. | **DOCUMENTO PRINCIPAL** | Alta | Capítulo 5, después del catálogo; mantener leyenda y notas. |
| [AGENDA-FULL-TRACEABILITY-MATRIX.md](../agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md) | D.4: cadena RF → CU → escenario → contrato → persistencia → test → estado. | **DOCUMENTO PRINCIPAL** | Alta | Capítulo 8; tabla ancha en página horizontal si hace falta. |
| [AGENDA-UML-CONSOLIDATION-REPORT.md](../agenda/consolidation/AGENDA-UML-CONSOLIDATION-REPORT.md) | D.4: síntesis de arquitectura documental, estados, decisiones y contradicciones. | **DOCUMENTO PRINCIPAL** | Alta | Capítulos 1–2 y 9 como fuente de síntesis, sin suprimir el documento original. |

## 2. Clasificación y tratamiento

- **DOCUMENTO PRINCIPAL (8):** D.1, D.2, D.3.1, D.3.3 y los cuatro documentos D.4. Sustentan el cuerpo académico. La clasificación de los casos permanece CU-D1/D4 **PARCIAL**, CU-D2/D3/D6 **CONCEPTUAL**, CU-D5/D7/D8 **IMPLEMENTADO** en su alcance documentado.
- **ANEXO TÉCNICO (5):** C.1–C.4 y el informe D.3.2. Se propone conservarlos íntegros como respaldo, incluidas tablas, exclusiones y advertencias de alcance. El estado Git registrado en D.3.2 describe aquel corte, no el estado de la futura exportación.
- **REFERENCIA INTERNA (2):** baseline y registro de decisiones. Son fuentes transversales de mayor alcance que Agenda. Se citan sin sustituir ni recortar sus archivos. Si el requisito de entrega posterior exige los 15 textos íntegros en Word, incorporarlos como anexos complementarios identificados; esta decisión editorial queda pendiente de la etapa de exportación.

La clasificación indica **ubicación editorial**, no autoridad funcional. Un contrato propuesto, una tabla SQL y una decisión cerrada solo en diseño no se convierten en funcionalidad implementada por pasar a Word.

## 3. Estructura Word propuesta

1. **Portada:** proyecto académico, módulo, título del entregable, etapa y fecha de edición futura; autoría y versión únicamente cuando existan datos confirmados.
2. **Índice automático:** niveles de títulos consistentes, lista de figuras y lista de tablas generadas durante la exportación.
3. **Cuerpo por capítulos:** capítulos 1–9 del orden siguiente; cada capítulo conserva referencias a su fuente Markdown y distingue evidencia actual de contenido conceptual o pendiente.
4. **Tablas:** conservar encabezados, notas, leyendas, RF/CU/DEC y estados. Usar ancho de página horizontal o partición editorial con encabezado repetido cuando una matriz no quepa; ninguna columna de trazabilidad se elimina.
5. **Diagramas:** reservar una figura para el [diagrama D.1](../agenda/uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.puml) y ocho figuras para los escenarios CU-D1–CU-D8 en `docs/agenda/scenarios/`. Renderizar los `.puml` solo en una etapa posterior; conservar fuente, título, estado y pie explicativo. Los diagramas conceptuales deben mantener **CONCEPTUAL — NO IMPLEMENTADO**.
6. **Anexos:** A–E según inventario. Las referencias internas conservarán su ruta y versión verificable; si se adjuntan íntegramente más adelante, hacerlo sin alterar el texto fuente.

Las rutas Markdown relativas deberán convertirse en referencias internas, pies de figura o referencias bibliográficas comprobables durante la exportación. Los vínculos a `.puml` requieren figura renderizada y referencia al archivo fuente; en E.1 todavía no hay imágenes ni paginación Word verificadas.

## 4. Orden recomendado del entregable

| Orden | Sección futura | Fuente principal y función |
|---|---|---|
| 1 | Introducción | Informe D.4: objetivo y carácter académico. |
| 2 | Alcance y estado actual | Informe D.4 y catálogo; delimitar los ocho casos. |
| 3 | Dominio | Baseline como autoridad citada, sin convertirlo en capítulo íntegro específico de Agenda. |
| 4 | Requisitos | C.1 y C.4 como respaldo; exponer RF-007/008/011/012/013/014 y su alcance sin cambiar estados. |
| 5 | Casos de uso y actores | Catálogo D.4, matriz actor–caso y fichas D.2. |
| 6 | UML general | D.1 explicativo y su figura PlantUML. |
| 7 | Escenarios | D.3.1 implementados, D.3.2 parciales y D.3.3 conceptuales; mostrar las ocho figuras por estado. |
| 8 | Trazabilidad | Matriz completa D.4; mantener la cadena RF → CU → escenario → contrato → persistencia → test → estado. |
| 9 | Decisiones y contradicciones | Informe D.4 y registro DEC citado; separar `CLOSED` de `OPEN` y aprobación conceptual de implementación. |
| 10 | Anexos | C.1–C.4 y D.3.2 íntegros; referencias internas al baseline y registro DEC. |

La repetición entre síntesis D.4 y fuentes C/D se resuelve mediante referencias cruzadas y ubicación en anexos, **sin borrar ni reescribir** material fuente. El orden es editorial; no modifica el orden lógico ni la semántica de los flujos.

## 5. Reglas de integridad para la futura exportación

- No eliminar información de los Markdown originales ni cambiar su significado. Conservar los originales como fuente y marcar cualquier selección o síntesis del cuerpo como tal.
- No corregir decisiones desde el maquetado. DEC-006/007 están cerradas para diseño; DEC-005/008/010 permanecen abiertas en el corte documental revisado. Revalidar el registro al momento de exportar.
- No inventar contenido, actores, permisos, endpoints, DTO, reglas, estados, diagramas ni pruebas. Mantener visibles **IMPLEMENTADO**, **PARCIAL**, **CONCEPTUAL** y **PENDIENTE** donde corresponda.
- No presentar rutas de la API Specification histórica, propuestas C.3 ni estructuras SQL como contratos funcionales actuales. La consulta ADMIN CU-D5 y la vista sanitizada CU-D6 deben permanecer separadas.
- Registrar la versión de cada fuente y comprobar enlaces, tablas, figuras y referencias cruzadas antes de producir Word. Las notas de Git de informes anteriores son históricas y no sustituyen una comprobación nueva.
- E.1 solo prepara el inventario y la composición. No se ha creado `.docx`, PDF ni imagen de los diagramas.
