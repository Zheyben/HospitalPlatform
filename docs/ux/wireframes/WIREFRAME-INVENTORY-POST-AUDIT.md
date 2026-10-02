# ETAPA F.3.1 — Auditoría posterior del inventario de wireframes

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico.

**Fecha:** 2026-09-30.

**Rama:** `feat/hospitalplatform-wireframes`.

**Documento auditado:** [WIREFRAME-INVENTORY.md](WIREFRAME-INVENTORY.md).

## 1. Estado Git

Al iniciar, `git status --short --branch` confirmó la rama indicada y mostró únicamente `?? docs/ux/wireframes/`, sin archivos rastreados modificados. Esa carpeta contenía el inventario F.3.1 aún sin seguimiento. Esta auditoría crea solo el presente informe; no cambia el inventario base.

## 2. Fuentes revisadas

- [F.1 requisitos UX](../UX-REQUIREMENTS-ANALYSIS.md) y [F.1.1 post-audit](../UX-REQUIREMENTS-POST-AUDIT.md): actores, capacidades, estados y corrección de RF-015/RF-030.
- [F.2.1 portal público](../user-flows/PUBLIC-PORTAL-USER-FLOWS.md), [F.2.2 portales privados](../user-flows/PRIVATE-PORTALS-USER-FLOWS.md), [F.2.3 consolidación](../user-flows/USER-FLOW-CONSOLIDATION.md) y [su post-audit](../user-flows/USER-FLOW-CONSOLIDATION-POST-AUDIT.md): nombres de vistas candidatas, dependencias y cortes de navegación.
- [D.2 detalles](../../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [D.3.1 implementados](../../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md), [D.3.2 parciales](../../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md), [D.3.3 conceptuales](../../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md), [D.4 catálogo](../../agenda/consolidation/AGENDA-USE-CASE-CATALOG.md), [actores](../../agenda/consolidation/AGENDA-ACTOR-USECASE-MATRIX.md) y [trazabilidad](../../agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md).
- [Documento maestro Word](../../documentation-export/HOSPITALPLATFORM-COMPLETE-DOCUMENTATION.docx), abierto en lectura: 182 párrafos, 19 tablas y referencias a RF-010, CU-D1/D4/D5/D6/D7/D8 y DEC-017. Como autoridad de estados se contrastaron además [Domain Baseline](../../DOMAIN-BASELINE.md) y [Decision Register](../../DOMAIN-DECISION-REGISTER.md).

## 3. Integridad del inventario

**Conforme.** La tabla contiene 15 identificadores `WF-01`–`WF-15`, cada uno con actor, objetivo, caso de uso o `—` justificado, RF y estado funcional. Cada ID tiene una ficha homónima con información visible, acciones, validaciones/estados UX y dependencias. Los ID son referencias locales de wireframe, no nuevos CU, rutas o permisos.

WF-01/WF-03/WF-04/WF-08/WF-12/WF-15 están declaradas como entrada futura, intención conceptual o referencia de navegación/brecha, **sin afirmar UI funcional**. WF-08 no añade dashboard de métricas; WF-15 prohíbe expresamente un wireframe profesional funcional. Las demás vistas reutilizan las denominaciones de F.2.1/F.2.2. No se detectó módulo o pantalla operativa inventada.

## 4. Validación por portal

| Alcance | Resultado |
|---|---|
| **PATIENT — público** | WF-02 usa login actual. WF-03 conserva autorregistro RF-001/DEC-017 `OPEN` como `FUTURO`. WF-04 marca disponibilidad CU-D6 `CONCEPTUAL`, sin endpoint/permiso ni campos definitivos. WF-05 reserva con `slotId` conocido y backend CU-D7; WF-06 muestra resultado `SCHEDULED` con RF-015 `PARCIAL`; WF-07 consulta citas propias y ubica la confirmación de asistencia como acción distinta. RF-010 mantiene el portal/frontend no implementado. |
| **ADMIN — privado** | WF-09 profesional CU-D1/RF-007 `PARCIAL`; WF-10 Schedule CU-D4/RF-011 `PARCIAL`; WF-11 disponibilidad operativa CU-D5 `IMPLEMENTADO` solo ADMIN. WF-08 es navegación sin estadísticas, reportes o administración hospitalaria añadida. |
| **RECEPTIONIST — privado** | WF-13 GET de citas y WF-14 operaciones de alta/ciclo coinciden con el backend actual. Se declara la dependencia de `patientId`/`slotId` conocidos y la ausencia de búsqueda autorizada para el rol. No hay gestión clínica, horarios ni configuración. |
| **PROFESSIONAL** | WF-15 es referencia de brecha, no portal ni dashboard. Los dos POST de estado operacional existen, pero falta GET de citas para navegar. No se propone atención clínica. DEC-005 permanece `OPEN`. |

## 5. Estados UX y trazabilidad

El inventario distingue **vacío**, **loading/cargando**, **error**, **éxito** y **no autorizado** como presentación, nunca como estados persistidos nuevos. Conserva `SCHEDULED` diferente de `CONFIRMED`; los estados de cita/slot citados proceden del dominio. No aparecen `NO_SHOW`, registro aprobado, calendario generado ni ventanas temporales.

La matriz **pantalla → CU → RF → estado** coincide con D.4 para CU-D1/D4/D5/D6/D7/D8. CU-D8 permanece invariante sin pantalla propia. Las operaciones de Auth/Appointments fuera de CU-D1–D8 usan `—` sin fabricar un caso local. RF-015 y RF-030 permanecen `PARCIAL` en su alcance global, aunque existan respuesta `201` y GET para los actores autorizados. CU-D2/CU-D3 se mantienen conceptuales y fuera de navegación funcional.

## 6. Hallazgos y correcciones

**Críticos: 0. Medios: 0. Bajos: 0. Correcciones al inventario: 0.**

**Observación 1:** WF-01/WF-03/WF-04 describen partes necesarias del recorrido deseado, pero RF-010 no tiene frontend, DEC-017 sigue `OPEN` y CU-D6 carece de API PATIENT. El inventario ya impide tratarlas como pantallas activas.

**Observación 2:** WF-14 necesita identificadores de paciente/slot sin búsquedas del rol; WF-15 no puede convertirse en portal profesional con los GET actuales. Ambas dependencias estaban documentadas en F.2.

No se modificó el inventario: ninguna de estas brechas es un error factual del archivo.

## 7. Validación final y estado

`git status --short --branch` confirmó la rama `feat/hospitalplatform-wireframes` y únicamente `?? docs/ux/wireframes/` (el inventario previo y este informe aún sin seguimiento). `git diff --check` terminó sin errores; dado que no incluye archivos sin seguimiento, también se revisaron ambos Markdown por espacios finales y enlaces locales relativos, sin incidencias. Archivo creado: `WIREFRAME-INVENTORY-POST-AUDIT.md`. Archivos existentes modificados en esta auditoría: **0**.

**Java: 0; SQL: 0; tests: 0; migraciones: 0; seguridad: 0.** No se ejecutó Maven ni se hizo commit, push o merge.

**🟡 WIREFRAME INVENTORY APPROVED WITH OBSERVATIONS.** La base de wireframes representa fielmente los flujos y el estado actual, con las vistas futuras/conceptuales etiquetadas para impedir una simulación engañosa de funciones existentes.
