# ETAPA F.3.3 — WIREFRAME CONSOLIDATION AUDIT

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico del Hospital de Huaycán.

**Rama:** `feat/hospitalplatform-wireframes`.

**Resultado:** 🟡 **WIREFRAME CONSOLIDATION COMPLETE WITH OBSERVATIONS**.

## 1. Objetivo

Consolidar las referencias `WF-01`–`WF-15` de [F.3.1](WIREFRAME-INVENTORY.md) y contrastar las láminas F.3.2 con los [requisitos UX](../UX-REQUIREMENTS-ANALYSIS.md), su [post-audit](../UX-REQUIREMENTS-POST-AUDIT.md), los [flujos público](../user-flows/PUBLIC-PORTAL-USER-FLOWS.md) y [privados](../user-flows/PRIVATE-PORTALS-USER-FLOWS.md), la [consolidación F.2.3](../user-flows/USER-FLOW-CONSOLIDATION.md) y su [post-audit](../user-flows/USER-FLOW-CONSOLIDATION-POST-AUDIT.md). Se comprobaron también [D.2](../../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), los escenarios [D.3.1](../../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md), [D.3.2](../../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md) y [D.3.3](../../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md), y el [catálogo D.4](../../agenda/consolidation/AGENDA-USE-CASE-CATALOG.md) con su [matriz de actores](../../agenda/consolidation/AGENDA-ACTOR-USECASE-MATRIX.md) y [trazabilidad](../../agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md).

Las [láminas públicas](PUBLIC-PORTAL-LOW-FIDELITY-WIREFRAMES.md), [ADMIN](ADMIN-PORTAL-LOW-FIDELITY-WIREFRAMES.md), [RECEPTIONIST](RECEPTIONIST-PORTAL-LOW-FIDELITY-WIREFRAMES.md) y la [referencia PROFESSIONAL](PROFESSIONAL-SCOPE-WIREFRAME.md) son propuestas documentales. `IMPLEMENTADO` describe soporte de backend cuando así se indica; no acredita frontend construido. Los [post-audits del inventario](WIREFRAME-INVENTORY-POST-AUDIT.md), [público](PUBLIC-PORTAL-WIREFRAMES-POST-AUDIT.md), [ADMIN](ADMIN-PORTAL-WIREFRAMES-POST-AUDIT.md), [RECEPTIONIST](RECEPTIONIST-WIREFRAMES-POST-AUDIT.md) y [PROFESSIONAL](PROFESSIONAL-SCOPE-WIREFRAME-POST-AUDIT.md) se usaron para contrastar brechas ya reconocidas.

## 2. Arquitectura UX consolidada

El login `WF-02` es una **capacidad backend compartida**. Portal público y portales privados son ramas documentales según el rol autenticado, no un paso secuencial de público a privado ni un selector de portal implementado.

```text
Entrada documental
├─ PATIENT: WF-01 [FUTURO]
│  ├─ WF-03 [autorregistro FUTURO; sin alta ejecutable]
│  └─ WF-02 [login backend de cuenta existente]
│     ├─ WF-04 [descubrimiento CONCEPTUAL] ╳ sin selección conectada a reserva
│     └─ con slotId conocido: WF-05 [reserva backend] → WF-06 [resultado]
│        → WF-07 [consulta propia respaldada por GET; sin lámina F.3.2]
├─ ADMIN: WF-02 → WF-08 [navegación CONCEPTUAL]
│  ├─ WF-09 [profesionales PARCIAL]
│  ├─ WF-10 [horarios PARCIAL]
│  └─ WF-11 [disponibilidad ADMIN IMPLEMENTADA]
├─ RECEPTIONIST: WF-02 → WF-12 [navegación CONCEPTUAL]
│  ├─ WF-13 [lista/detalle de citas: GET vigente]
│  └─ WF-14 [alta/ciclo vigente con patientId y slotId conocidos]
│     Referencia CU-D6 [CONCEPTUAL; sin vista navegable]
└─ PROFESSIONAL: WF-02 ↛ WF-15 [referencia de brecha; sin portal]
```

Las flechas de navegación son propuestas UX. `WF-04` no produce hoy un `slotId` para PATIENT; la consulta sanitizada de RECEPTIONIST tampoco existe. `WF-08` y `WF-12` no son dashboards ni APIs. `WF-15` no es destino navegable. No se detectaron cruces de permisos entre los cuatro actores.

## 3. Mapa de actores

| Actor | Alcance documentado | Frontera que no debe cruzarse |
|---|---|---|
| PATIENT | Login de cuenta existente, citas propias y reserva con `slotId` conocido. | Sin autorregistro público ni GET de disponibilidad PATIENT actual. |
| ADMIN | Perfil profesional básico, Schedule y consulta operativa de slots. | Sin CRUD de Specialty, asociación N:M, generación de slots o dashboard de métricas. |
| RECEPTIONIST | Lista/detalle y operaciones de cita según rol y estado; reserva con `patientId` y `slotId` conocidos. | Sin consulta de disponibilidad del rol, GET de pacientes, gestión clínica, profesional u horarios. |
| PROFESSIONAL | Rol real y dos POST operativos de cita asignada en backend. | Sin GET de citas autorizado, portal completo, agenda personal o atención clínica. |

## 4. Matriz de pantallas

`—` en CU indica identidad, navegación o Appointments fuera de CU-D1–CU-D8; no crea un caso nuevo. Los 15 ID son únicos en el inventario. La columna «Cobertura F.3.2» indica qué se dibujó, sin elevar una referencia documental a interfaz funcional.

| ID | Pantalla o referencia | Actor | Estado | Caso de uso | RF | Cobertura F.3.2 |
|---|---|---|---|---|---|---|
| WF-01 | Entrada / Landing | Visitante orientado a PATIENT | `FUTURO` frontend | — | RF-010 | Público, lámina 01 |
| WF-02 | Login compartido | PATIENT, ADMIN, RECEPTIONIST, PROFESSIONAL | Backend `IMPLEMENTADO`; UI propuesta | — | RF-002 | Público 02; ADMIN 01; RECEPTIONIST 01; PROFESSIONAL solo referencia de entrada |
| WF-03 | Registro paciente | Visitante orientado a PATIENT | `FUTURO`; boceto `CONCEPTUAL` | — | RF-001 | Público 03, acción inactiva |
| WF-04 | Descubrimiento de cita | PATIENT | `CONCEPTUAL` | CU-D6 | RF-012 | Público 04, zona A sin API |
| WF-05 | Envío de reserva | PATIENT con perfil activo | Backend `IMPLEMENTADO`; llegada UX pendiente | CU-D7; CU-D8 interno | RF-013/014 | Público 04, zona B con `slotId` conocido |
| WF-06 | Resultado de reserva | PATIENT | Respuesta `IMPLEMENTADA`; constancia UX `PARCIAL` | CU-D7 | RF-015 | Público 05 |
| WF-07 | Mis citas / detalle | PATIENT | GET propio `IMPLEMENTADO`; UI no construida | — | RF-006; RF-016/017/019 para acciones | Solo inventario y referencia en Público 05; **sin lámina propia** |
| WF-08 | Área operativa | ADMIN | Navegación `CONCEPTUAL`; sin dashboard | — | — | ADMIN 02 |
| WF-09 | Profesionales | ADMIN | `PARCIAL` | CU-D1 | RF-007 | ADMIN 03 |
| WF-10 | Horarios | ADMIN | `PARCIAL` | CU-D4 | RF-011 | ADMIN 04 |
| WF-11 | Disponibilidad operativa | ADMIN | `IMPLEMENTADO` para ADMIN; UI propuesta | CU-D5 | RF-012, porción ADMIN | ADMIN 05 |
| WF-12 | Entrada a citas | RECEPTIONIST | Navegación `CONCEPTUAL`; sin dashboard | — | — | RECEPTIONIST 02 |
| WF-13 | Citas: lista/detalle | RECEPTIONIST | GET `IMPLEMENTADO`; RF global `PARCIAL` | — | RF-030, porción recepción | RECEPTIONIST 03 |
| WF-14 | Cita: alta/gestión | RECEPTIONIST | Backend `IMPLEMENTADO`; selección UX pendiente | CU-D7/CU-D8 para alta; otras acciones fuera de CU-D | RF-013/014/016/017/019/026/027 | RECEPTIONIST 03, ciclo; 04, alta |
| WF-15 | Portal profesional, referencia de brecha | PROFESSIONAL | UX `CONCEPTUAL / NO IMPLEMENTADO`; RF-030 `PARCIAL` | — | RF-028/029/030 | Documento de alcance, sin wireframe funcional |

No hay duplicación funcional por reutilizar `WF-02` entre actores, dividir `WF-14` entre dos láminas o agrupar `WF-04`/`WF-05` en una sola lámina. La quinta lámina RECEPTIONIST documenta CU-D6 **sin ID WF, controles activos ni navegación**; no es una pantalla adicional del inventario. `WF-07` es la única referencia inventariada sin lámina F.3.2 propia; su consulta está respaldada por backend y la omisión se declara expresamente en el documento público. No se incorporó una pantalla nueva para cubrirla.

## 5. Trazabilidad UX

| Pantalla / referencia | Flujo UX | CU | RF | Estado y corte |
|---|---|---|---|---|
| WF-01/02/03 | Público: entrada, login o intención de registro | — | RF-010/002/001 | Portal `FUTURO`; login backend `IMPLEMENTADO`; autorregistro `FUTURO`, DEC-017 `OPEN`. |
| WF-04 → WF-05 → WF-06 | Público: descubrir → reservar → ver resultado | CU-D6 → CU-D7; CU-D8 interno | RF-012 → RF-013/014 → RF-015 | Descubrimiento `CONCEPTUAL`, **sin transición ejecutable**; reserva con ID conocido `IMPLEMENTADO`, cita `SCHEDULED`; constancia `PARCIAL`. |
| WF-07 | Público: consulta posterior de cita propia | — | RF-006; RF-016/017/019 | GET y acciones elegibles respaldados por backend; falta lámina F.3.2 propia. |
| WF-08 → WF-09/10/11 | ADMIN: navegación a tres áreas | — → CU-D1/CU-D4/CU-D5 | — → RF-007/011/012 | Inicio `CONCEPTUAL`; profesionales y horarios `PARCIAL`; disponibilidad operativa ADMIN `IMPLEMENTADO`. |
| WF-12 → WF-13/14 | RECEPTIONIST: navegar, consultar y operar citas | —; CU-D7/CU-D8 para alta | RF-030; RF-013/014/016/017/019/026/027 | Navegación `CONCEPTUAL`; GET/mutaciones backend vigentes; obtención de IDs pendiente. |
| Referencia RECEPTIONIST sin ID WF | Disponibilidad sanitizada futura | CU-D6 | RF-012 | `CONCEPTUAL`, sin endpoint, permiso o navegación. |
| WF-15 | PROFESSIONAL: límite de portal | — | RF-028/029/030 | Dos POST operativos existentes; RF-030 `PARCIAL`; UX `CONCEPTUAL / NO IMPLEMENTADO`. |

La clasificación de D.4 permanece: **CU-D5/D7/D8 IMPLEMENTADO; CU-D1/D4 PARCIAL; CU-D2/D3/D6 CONCEPTUAL**. CU-D8 es una invariante interna de reserva, sin pantalla. CU-D2/D3 tampoco tienen pantallas funcionales. RF-012 global es `PARCIAL` aunque CU-D5 sea vigente; RF-030 global es `PARCIAL` aunque RECEPTIONIST pueda consultar citas.

## 6. Estados del prototipo y consistencia de reserva

| Área | Comprobación |
|---|---|
| PATIENT | Login backend `IMPLEMENTADO`; registro `FUTURO`; disponibilidad `CONCEPTUAL`; reserva solo con `slotId` conocido. El `201` crea `SCHEDULED`, `flowStage=null`; `CONFIRMED` exige una acción posterior. |
| ADMIN | Profesionales CU-D1 y horarios CU-D4 `PARCIAL`; disponibilidad CU-D5 `IMPLEMENTADO` solo para lectura ADMIN. Crear Schedule no crea slots. |
| RECEPTIONIST | GET y transiciones de citas vigentes según rol/estado; alta `SCHEDULED` con `patientId`/`slotId` conocidos. Disponibilidad del rol CU-D6 `CONCEPTUAL`. |
| PROFESSIONAL | Login compartido y dos mutaciones operativas backend; sin consulta de citas del rol ni portal completo. WF-15 `CONCEPTUAL / NO IMPLEMENTADO`. |

«Vacío», «cargando», «error», «éxito» y «no autorizado» son estados de presentación para solicitudes respaldadas. `SCHEDULED` y `CONFIRMED` son estados distintos del dominio; ninguna lámina crea una cita directamente en `CONFIRMED`. La respuesta `201` no equivale a una constancia UX completa: RF-015 sigue `PARCIAL`.

## 7. Dependencias pendientes y hallazgos

| Clasificación | Hallazgo o dependencia | Consecuencia para el prototipo |
|---|---|---|
| **Medio** | `WF-07` está en el inventario y en el flujo público, pero no tiene lámina propia en F.3.2. | Mantenerlo como referencia especificada y no declarar cobertura visual completa de los 15 ID. Su GET vigente no se cuestiona. |
| **Observación** | DEC-017 `OPEN`: autorregistro público sin contrato ni formulario definitivo. | WF-03 debe seguir inactivo / `FUTURO`. |
| **Observación** | DEC-007 aprueba la frontera conceptual de disponibilidad para PATIENT y RECEPTIONIST, sin API/permiso; DEC-008/010 siguen abiertas para generación y temporalidad. | WF-04 y la referencia RECEPTIONIST no pueden producir una selección de slot navegable. |
| **Observación** | La reserva de PATIENT requiere `slotId` conocido; RECEPTIONIST requiere además `patientId` conocido. | La navegación desde descubrimiento o búsqueda de identificadores sigue interrumpida, aunque CU-D7 funcione. |
| **Observación** | RF-030 `PARCIAL` y DEC-005 `OPEN`: PROFESSIONAL no tiene GET de citas autorizado ni ciclo de vínculo completo. | WF-15 permanece no navegable; los POST de RF-028/029 no constituyen portal. |
| **Observación** | CU-D4 necesita `specialtyId` existente, sin catálogo CU-D3 ni asociación CU-D2 funcionales. | WF-10 no debe simular selector de catálogo o elegibilidad profesional-especialidad. |

**Contradicciones críticas detectadas: 0. Duplicados de ID: 0. Pantallas operativas inventadas detectadas: 0.** La cobertura visual pendiente de WF-07 y las dependencias ya señaladas justifican el gate con observaciones, sin alterar estados ni crear nuevas pantallas en esta consolidación.

## 8. Exclusiones

No se incluyen historia clínica, interfaz de atención médica, diagnóstico, recetas, gestión clínica de pacientes, reportes, métricas, notificaciones, waitlist, prioridad, generación automática de slots, calendario/duración/ventanas temporales, dashboard profesional ni módulos futuros. Las láminas no agregan endpoints, DTO, permisos, reglas de negocio o estados persistidos.

## 9. Recomendaciones para prototipado UI

1. Usar un único acceso compartido como referencia de autenticación y separar destinos por rol solo como navegación propuesta; no representar portales frontend como existentes.
2. Mantener deshabilitadas o rotuladas como conceptuales las zonas de registro y descubrimiento; no enlazarlas a una reserva simulando un `slotId` obtenido de una API inexistente.
3. Mostrar la reserva creada únicamente tras respuesta real `201` con `SCHEDULED`; reservar `CONFIRMED` para la transición posterior autorizada.
4. Si una fase posterior requiere cobertura visual de todo el inventario, diseñar `WF-07` entonces y auditarlo contra RF-006 y el GET propio; esta etapa no añade la lámina.
5. Preservar WF-15 y la quinta lámina RECEPTIONIST como referencias no navegables hasta que existan las decisiones y contratos necesarios.

## 10. Validación final

`git status --short --branch` y `git diff --check` se ejecutaron. Como `docs/ux/wireframes/` figura sin seguimiento, también se revisaron directamente enlaces locales y espacios finales de este informe; `git diff --check` por sí solo no inspecciona archivos no rastreados. No se ejecutó Maven ni se hizo commit, push o merge.

**Archivo creado:** `docs/ux/wireframes/WIREFRAME-CONSOLIDATION-REPORT.md`. **Archivos existentes modificados:** 0. **Java:** 0; **SQL:** 0; **tests:** 0; **migraciones:** 0; **seguridad:** 0.

**🟡 WIREFRAME CONSOLIDATION COMPLETE WITH OBSERVATIONS.** La clasificación funcional y las fronteras por actor son coherentes. El prototipo completo todavía tiene cortes explícitos de navegación y `WF-07` carece de lámina propia F.3.2.
