# ETAPA F.3.4 — WIREFRAME GAP RESOLUTION

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico del Hospital de Huaycán.

**Rama:** `feat/hospitalplatform-wireframe-gaps`.

**Resultado:** 🟡 **WIREFRAME GAP RESOLUTION COMPLETE WITH OBSERVATIONS**.

## 1. Objetivo

Completar la definición documental de `WF-07 — Mis citas`, que [F.3.3](WIREFRAME-CONSOLIDATION-REPORT.md) identificó sin lámina propia, y entregar un mapa de navegación paciente y un inventario de preparación para UI. Esta etapa no crea interfaz, contrato, permiso ni funcionalidad.

Fuentes contrastadas: [inventario F.3.1](WIREFRAME-INVENTORY.md) y [post-audit](WIREFRAME-INVENTORY-POST-AUDIT.md); wireframes [público](PUBLIC-PORTAL-LOW-FIDELITY-WIREFRAMES.md), [ADMIN](ADMIN-PORTAL-LOW-FIDELITY-WIREFRAMES.md), [RECEPTIONIST](RECEPTIONIST-PORTAL-LOW-FIDELITY-WIREFRAMES.md) y [alcance PROFESSIONAL](PROFESSIONAL-SCOPE-WIREFRAME.md); [flujos consolidados](../user-flows/USER-FLOW-CONSOLIDATION.md); [D.2](../../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), escenarios [D.3.1](../../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md), [D.3.2](../../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md), [D.3.3](../../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md) y [D.4](../../agenda/consolidation/AGENDA-USE-CASE-CATALOG.md). La evidencia actual de lectura se contrastó además con `AppointmentController`, `AppointmentService` y `AppointmentResponseDTO`; solo se leyeron.

`IMPLEMENTADO` se refiere a capacidad backend verificada, **no a pantalla frontend construida**. Las prioridades de la sección 5 son de **trabajo de diseño UI**, no prioridad médica, regla de negocio, permiso o compromiso de implementación.

## 2. GAP identificados

| Brecha de F.3.3 | Evidencia | Resolución documental de esta etapa |
|---|---|---|
| WF-07 estaba en el inventario y en F.2.3, pero sin lámina F.3.2 propia. | RF-006 y los GET de citas propias existen para PATIENT; la [lámina pública 05](PUBLIC-PORTAL-LOW-FIDELITY-WIREFRAMES.md) lo cita como continuación. | Se especifica abajo una lámina de baja fidelidad para el mismo `WF-07`, sin ID o CU nuevo. |
| La secuencia del paciente podía leerse como recorrido continuo. | RF-001/DEC-017 siguen abiertos; CU-D6 no tiene consulta PATIENT; CU-D7 reserva con `slotId` conocido. | Los mapas de la sección 4 señalan el corte de registro y el corte entre descubrimiento y reserva. |
| Faltaba una matriz final para preparar UI. | F.3.1 contiene `WF-01`–`WF-15`; F.3.3 ya auditó actores y estados. | Se conservan los 15 ID y sus estados en la sección 5. |

**Límite de la resolución:** se cierra la falta de *definición UX* de WF-07. No se cierra la ausencia de frontend, autorregistro, disponibilidad sanitizada, obtención de identificadores ni portal profesional.

## 3. WF-07 — Mis citas / detalle

**Actor:** PATIENT autenticado con perfil propio activo para la consulta de citas propias.

**Estado:** backend de lista y detalle `IMPLEMENTADO` (RF-006); wireframe y frontend **no implementados**. El mismo ID WF-07 ya estaba aprobado en el inventario; esta sección no añade una pantalla distinta.

**Objetivo:** permitir reconocer las citas propias registradas, abrir una cita y ver su estado y datos básicos realmente devueltos. La consulta usa `GET /api/v1/appointments` y `GET /api/v1/appointments/{id}` con filtrado/ownership actual del paciente.

**Estructura de baja fidelidad:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] Mis citas · referencia WF-07 · UI no implementada   │
├──────────────────────────────────────────────────────────────┤
│ [PRÓXIMA CITA — espacio de diseño, NO dato actual]            │
│ No identificable con fecha/hora mediante el DTO de citas.    │
│ No se destaca ninguna cita como próxima.                     │
├──────────────────────────────────────────────────────────────┤
│ [LISTA DE CITAS PROPIAS — respuesta GET]                      │
│ ID de cita | appointmentStatus | flowStage si se devuelve    │
│ [Abrir detalle de una cita propia]                            │
├──────────────────────────────────────────────────────────────┤
│ [DETALLE DE LA CITA SELECCIONADA — respuesta GET/{id}]        │
│ id | professionalId | slotId | appointmentStatus             │
│ flowStage | reason | fechas devueltas por el DTO             │
│ [ACCIONES ELEGIBLES: confirmar / cancelar / reprogramar]     │
│ [sin selector de nuevo slot; necesita slotId conocido]       │
├──────────────────────────────────────────────────────────────┤
│ [FOOTER] Reserva creada = SCHEDULED; CONFIRMED es posterior  │
└──────────────────────────────────────────────────────────────┘
```

**Información visible:** en lista, identificador y `appointmentStatus`; `flowStage` solo como dato retornado, incluso si es `null`. En detalle, únicamente valores de `AppointmentResponseDTO` que realmente lleguen: `id`, `patientId`, `professionalId`, `slotId`, `appointmentStatus`, `flowStage`, `reason`, `cancelledAt`, `cancelledBy`, `createdAt` y `updatedAt`, según corresponda. Los identificadores pueden presentarse sin enriquecimiento; no se transforma `professionalId` en nombre, especialidad o ficha clínica. El DTO no incluye fecha/hora del slot. `createdAt` indica creación del registro, **no fecha de la cita**.

**«Próxima cita»:** se incluye como espacio de jerarquía UX solicitado, **sin contenido calculado ni promesa funcional**. El GET actual ordena por `createdAt` descendente, que tampoco determina la siguiente cita cronológica. Poblar ese bloque con fecha/hora o elegir una «próxima» requiere datos/contrato autorizados posteriores; no se consulta disponibilidad ADMIN ni se infiere desde `slotId`.

**Acciones:** cargar lista, abrir detalle propio y, si rol y estado lo permiten, confirmar desde cita `SCHEDULED`, cancelar o reprogramar según los contratos vigentes de Appointments. Reprogramar requiere un `slotId` nuevo conocido; aquí no hay búsqueda de oferta. No se ejecuta ninguna acción al abrir la pantalla. La acción «confirmar» es transición de asistencia posterior, distinta del resultado de reserva WF-06.

**Estados UX:** cargando lista/detalle; lista vacía; detalle encontrado; cita ausente `404`; sesión ausente `401` o acceso ajeno denegado `403`; error de transición según respuesta; resultado de una acción solo tras respuesta real. Estos son estados de presentación. Los valores `SCHEDULED`, `CONFIRMED`, `CANCELLED`, `RESCHEDULED`, `COMPLETED` y `flowStage` son datos de dominio, no etiquetas nuevas. Una cita recién creada en WF-05 aparece `SCHEDULED` tras `201`, nunca `CONFIRMED` por el mero alta.

**Dependencias y restricciones:** cuenta PATIENT válida, ownership de la cita y RF-006. RF-016/017/019 respaldan acciones elegibles, sin ampliar sus reglas. La lectura de cita propia no exige CU-D6. RF-015 permanece `PARCIAL` como constancia UX del alta; esta vista no lo convierte en `IMPLEMENTADO` completo. No contiene historia clínica, resultados, diagnóstico ni atención médica.

**Trazabilidad:** WF-07 → flujo público «Mis citas / detalle» → CU `—` (lectura de Appointments fuera de CU-D1–D8) → RF-006 → GET propios backend `IMPLEMENTADO`, UI no construida. Acciones de detalle → CU `—` → RF-016/017/019 → operaciones existentes condicionadas por rol y estado.

## 4. Navegación paciente actualizada

**Usuario nuevo — intención UX, no alta ejecutable:**

```text
WF-01 Landing [FUTURO como frontend]
  ↓ opción de registro documental
WF-03 Registro [FUTURO; DEC-017 OPEN; acción Crear cuenta inactiva]
  ╳ no se crea ni habilita una cuenta aquí
WF-02 Login [backend IMPLEMENTADO] solo si una cuenta válida existe
```

La secuencia «Landing → Registro → Login» describe el objetivo futuro. No implica envío de formulario, transición automática, aprobación de cuenta ni autorregistro disponible.

**Usuario existente — navegación con dos cortes explícitos:**

```text
WF-01 Landing [FUTURO como frontend]
  ↓
WF-02 Login [backend IMPLEMENTADO para cuenta existente]
  ↓
WF-04 Solicitud / descubrimiento [CONCEPTUAL; CU-D6]
  ╳ no existe consulta ni selección de slot PATIENT autorizada

Con perfil PATIENT activo y slotId usable conocido por una vía aún no resuelta en UX:
WF-05 Envío de reserva [backend IMPLEMENTADO; CU-D7/CU-D8 interno]
  ↓ respuesta real 201
WF-06 Resultado [SCHEDULED; RF-015 PARCIAL como constancia]
  ↓ consulta propia opcional
WF-07 Mis citas / detalle [GET backend IMPLEMENTADO; UI no construida]
```

El camino solicitado «Landing → Login → Solicitud → Reserva → Mis citas» queda documentado, pero **no es navegable de extremo a extremo hoy**: portal frontend RF-010 no implementado y CU-D6 sin API/permiso PATIENT. CU-D7 sí puede ejecutarse con `slotId` conocido sin CU-D6 previo. Ningún mapa convierte una consulta conceptual en fuente real del identificador.

## 5. Inventario final para UI Prototype

**Prioridad UI documental:** `P1` = especificar primero vistas apoyadas por operaciones existentes; `P2` = organizar navegación o resultado con límites declarados; `P3` = mantener como referencia bloqueada/conceptual. La prioridad no cambia el estado funcional ni autoriza hacer la vista navegable. `WF-02` se reutiliza entre actores y se cuenta **una sola vez**.

| ID | Pantalla o referencia | Actor | Estado | Prioridad UI | Caso de uso | RF |
|---|---|---|---|---|---|---|
| WF-01 | PUBLIC — Landing / Acceso | Visitante orientado a PATIENT | `FUTURO` frontend | P2 | — | RF-010 |
| WF-02 | PUBLIC/privados — Login compartido | PATIENT, ADMIN, RECEPTIONIST, PROFESSIONAL | Backend `IMPLEMENTADO`; UI no construida | P1 | — | RF-002 |
| WF-03 | PUBLIC — Registro paciente | Visitante orientado a PATIENT | `FUTURO`; representación `CONCEPTUAL` | P3 | — | RF-001 |
| WF-04 | PUBLIC — Solicitud / descubrimiento de cita | PATIENT | `CONCEPTUAL`; sin API PATIENT | P3 | CU-D6 | RF-012 |
| WF-05 | PUBLIC — Envío de reserva | PATIENT con perfil propio activo | Backend `IMPLEMENTADO`; `slotId` UX pendiente | P1, condicionado | CU-D7; CU-D8 interno | RF-013/014 |
| WF-06 | PUBLIC — Resultado / reserva registrada | PATIENT | Respuesta backend `IMPLEMENTADA`; constancia UX `PARCIAL` | P2 | CU-D7 | RF-015 |
| WF-07 | PUBLIC — Mis citas / detalle | PATIENT | GET propio `IMPLEMENTADO`; UI no construida | P1 | — | RF-006; RF-016/017/019 para acciones |
| WF-08 | ADMIN — Home / navegación | ADMIN | Organización UX `CONCEPTUAL`; sin dashboard | P2 | — | — |
| WF-09 | ADMIN — Profesionales | ADMIN | `PARCIAL` | P1 | CU-D1 | RF-007 |
| WF-10 | ADMIN — Horarios | ADMIN | `PARCIAL` | P1, condicionado | CU-D4 | RF-011 |
| WF-11 | ADMIN — Disponibilidad operativa | ADMIN | `IMPLEMENTADO` solo ADMIN; UI no construida | P1 | CU-D5 | RF-012, porción ADMIN |
| WF-12 | RECEPTIONIST — Home / entrada a citas | RECEPTIONIST | Organización UX `CONCEPTUAL`; sin dashboard | P2 | — | — |
| WF-13 | RECEPTIONIST — Gestión de citas: lista/detalle | RECEPTIONIST | GET `IMPLEMENTADO`; RF global `PARCIAL` | P1 | — | RF-030, porción recepción |
| WF-14 | RECEPTIONIST — Gestión de citas: alta/ciclo | RECEPTIONIST | Backend `IMPLEMENTADO`; IDs UX pendientes | P1, condicionado | CU-D7/CU-D8 para alta; demás acciones fuera de CU-D | RF-013/014/016/017/019/026/027 |
| WF-15 | PROFESSIONAL — referencia de alcance | PROFESSIONAL | UX `CONCEPTUAL / NO IMPLEMENTADO`; RF-030 `PARCIAL` | P3, solo referencia | — | RF-028/029/030 |

La matriz conserva exactamente los 15 ID de F.3.1. La quinta lámina RECEPTIONIST sobre disponibilidad es **referencia conceptual sin ID WF ni navegación**, no una pantalla adicional. El inventario no incorpora dashboard profesional, catálogo Specialty funcional o un segundo login por rol. La clasificación D.4 permanece: CU-D5/D7/D8 `IMPLEMENTADO`, CU-D1/D4 `PARCIAL`, CU-D2/D3/D6 `CONCEPTUAL`.

## 6. Estados funcionales

- **PATIENT:** RF-002 login backend y RF-006 lectura propia `IMPLEMENTADO`; RF-001/registro `FUTURO`; CU-D6/disponibilidad `CONCEPTUAL`; CU-D7 reserva `IMPLEMENTADO` con `slotId` conocido; RF-015 constancia UX `PARCIAL`. La creación responde `SCHEDULED`; `CONFIRMED` exige transición posterior.
- **ADMIN:** CU-D1 profesionales y CU-D4 horarios `PARCIAL`; CU-D5 disponibilidad operativa `IMPLEMENTADO` solo para ADMIN. CU-D2/CU-D3 no pasan a CRUD por aparecer en SQL.
- **RECEPTIONIST:** consulta y operaciones de citas vigentes dentro de su autorización; CU-D6 disponibilidad del rol `CONCEPTUAL`; el alta requiere `patientId` y `slotId` conocidos.
- **PROFESSIONAL:** RF-028/029 son mutaciones operativas backend; RF-030 `PARCIAL` sin GET de citas del rol. Portal `CONCEPTUAL / NO IMPLEMENTADO`; DEC-005 permanece `OPEN`.

Los estados UX de carga, vacío, error, éxito y acceso denegado se limitan a presentación. No se incorporan estados persistidos nuevos.

## 7. Dependencias pendientes

| Dependencia | Límite vigente |
|---|---|
| RF-001 y DEC-017 `OPEN` | Sin autorregistro público, campos finales o transición automática desde WF-03. |
| CU-D6 / DEC-007 | Disponibilidad sanitizada PATIENT/RECEPTIONIST aprobada conceptualmente, sin endpoint, DTO, filtros definitivos ni permiso actual. |
| `slotId` de PATIENT; `patientId` y `slotId` de RECEPTIONIST | No hay recorrido UX autorizado de descubrimiento para esos roles; CU-D7 admite IDs conocidos. |
| Próxima cita en WF-07 | `AppointmentResponseDTO` no trae fecha/hora del slot; `createdAt` no permite calcular próxima atención. El bloque queda sin dato hasta contar con fuente autorizada. |
| RF-015 | Respuesta `201` y DTO existentes; constancia UX completa aún `PARCIAL`. |
| CU-D2/CU-D3, DEC-005/008/010 | Asociación/catálogo sin API funcional; ciclo User–Professional, generación de slots y reglas temporales abiertos según su alcance. |
| RF-030 / portal profesional | Sin GET de citas para PROFESSIONAL; WF-15 no navegable. |

## 8. Exclusiones

No se diseñan historia clínica, resultados médicos, diagnóstico, atención médica, recetas, agenda profesional, dashboards de métricas, reportes, notificaciones, waitlist, prioridad, generación automática de slots, calendario, ventanas temporales, filtros de disponibilidad sanitizada definitivos, selector de Specialty Catalog ni módulos futuros. No se añade endpoint, DTO, permiso, estado de dominio o regla de negocio.

## 9. Validación y gate

Se ejecutaron `git status --short --branch` y `git diff --check`. Se revisaron enlaces locales, espacios finales y unicidad de `WF-01`–`WF-15` en este archivo. No se ejecutó Maven. No se hizo commit, push o merge.

**Archivo creado:** `docs/ux/wireframes/WIREFRAME-GAP-RESOLUTION-REPORT.md`. **Archivos existentes modificados:** 0. **Java:** 0; **SQL:** 0; **tests:** 0; **migraciones:** 0; **seguridad:** 0.

**🟡 WIREFRAME GAP RESOLUTION COMPLETE WITH OBSERVATIONS.** WF-07 ya tiene definición de baja fidelidad y el inventario UI está completo como documento. La «próxima cita» no puede poblarse con fecha/hora mediante el DTO vigente; registro, disponibilidad y navegación de identificadores siguen siendo brechas funcionales explícitas, fuera de esta resolución documental.
