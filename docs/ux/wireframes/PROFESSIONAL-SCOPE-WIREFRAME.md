# ETAPA F.3.2.4 — Alcance UX y referencia de wireframe PROFESSIONAL

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico del Hospital de Huaycán.

**Estado del portal profesional:** `CONCEPTUAL / NO IMPLEMENTADO`. La referencia visual es `FUTURO`; no constituye una pantalla, navegación o permiso existente. Los estados `IMPLEMENTADO` citados más abajo se aplican únicamente a operaciones actuales del backend.

## 1. Objetivo

Delimitar la presencia del actor PROFESSIONAL y registrar qué faltaría para una experiencia navegable, sin diseñar un portal completo. La referencia local [WF-15](WIREFRAME-INVENTORY.md) es una **brecha documentada, sin wireframe funcional**. Este archivo no crea un nuevo caso de uso, endpoint, DTO, contrato ejecutable ni flujo clínico.

## 2. Presencia del actor y límite actual

PROFESSIONAL pertenece a los cuatro roles vigentes del caso de estudio. Una cuenta existente puede usar el **login compartido del backend**, como cualquier rol actual; esa capacidad no acredita una entrada visual profesional implementada. En Appointments, el profesional activo vinculado a la cuenta y asignado a una cita puede iniciar el estado operacional desde `CONFIRMED/WAITING` y completarlo desde `CONFIRMED/IN_ATTENTION`. Son las mutaciones actuales RF-028 y RF-029, no registro de atención médica. Al completar, la cita pasa a `COMPLETED/FINISHED` según el dominio.

Los GET actuales de citas **no** autorizan PROFESSIONAL. RF-030 permanece `PARCIAL`: recepción consulta citas, mientras falta una superficie de consulta de citas asignadas para el profesional. Sin esa obtención del identificador de cita y una navegación autorizada, los dos POST no forman por sí solos un recorrido de portal. PROFESSIONAL no inicia CU-D1–CU-D8 del incremento Agenda; CU-D1 es gestión ADMIN y no implica autoservicio profesional.

| Elemento | Evidencia y estado | Límite UX |
|---|---|---|
| Actor PROFESSIONAL | Rol actual según el dominio. | No equivale a dashboard o vista propia. |
| Autenticación | Login backend compartido `IMPLEMENTADO` para cuenta existente. | Entrada profesional visual `CONCEPTUAL / NO IMPLEMENTADA`; no se define endpoint de login nuevo. |
| Inicio/fin operacional de cita asignada | RF-028/029 `IMPLEMENTADO` como POST de Appointments sujetos a ownership y estado exacto. | No se dibujan botones ejecutables ni una pantalla de atención. |
| Consulta de citas asignadas | RF-030 `PARCIAL`: no hay GET autorizado para PROFESSIONAL. | Lista, detalle, agenda personal y navegación siguen `NO IMPLEMENTADOS`. |
| Vínculo User–Professional | `professionals.user_id` opcional y comprobación actual de ownership; DEC-005 `OPEN`. | No se inventa alta, vinculación, aprobación, sincronización o autoservicio. |

## 3. Punto de entrada conceptual

La secuencia de diseño termina antes de cualquier portal profesional. `WF-02` representa el acceso compartido, cuya operación de backend existe; el destino específico profesional es solo una posibilidad futura. No se afirma un formulario adicional, permiso de consulta, ruta frontend o selector de portal.

```text
Cuenta PROFESSIONAL existente
    ↓ login compartido [backend IMPLEMENTADO; WF-02]
    ╳ sin destino profesional navegable acreditado
WF-15 [referencia CONCEPTUAL / NO IMPLEMENTADA]
```

**Objetivo futuro del punto de entrada:** dar acceso a una vista mínima autorizada para el rol si más adelante se aprueban y construyen consulta, contrato y navegación. No se decide aquí su forma, datos o comportamiento.

## 4. Vista conceptual futura — referencia de brecha WF-15

**Actor:** PROFESSIONAL.<br>
**Estado:** `CONCEPTUAL / FUTURO / NO IMPLEMENTADO` como UX. No es wireframe funcional ni portal existente.<br>
**Objetivo:** señalar el lugar donde podría explicarse una futura consulta de citas asignadas, sin mostrar una lista, un paciente o una tarea ejecutable.

**Estructura:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] Alcance profesional · REFERENCIA NO NAVEGABLE       │
├──────────────────────────────────────────────────────────────┤
│ [CONTENIDO]                                                  │
│ Actor PROFESSIONAL reconocido en el dominio.                 │
│ Consulta de citas asignadas: PENDIENTE / sin GET del rol.    │
│ Identificación profesional básica: por definir en UX/API.   │
│ No se muestran citas, pacientes ni agenda personal.          │
│                                                              │
│ [ACCIONES] Ninguna acción de pantalla.                       │
├──────────────────────────────────────────────────────────────┤
│ [FOOTER] DEC-005 OPEN · RF-030 PARCIAL                        │
└──────────────────────────────────────────────────────────────┘
```

**Datos visibles:** solo rótulos documentales de alcance y brecha. «Citas asignadas» e «identificación profesional» son posibles necesidades futuras, **no datos consultados**, campos de DTO, filtros ni resultados visibles actuales. No se muestran identificadores de paciente, historias clínicas, diagnósticos o tratamientos.<br>
**Acciones posibles:** ninguna en esta referencia. RF-028/029 acreditan mutaciones backend sobre una cita conocida y asignada, pero el dibujo no ofrece iniciar/finalizar atención ni supone un medio para obtener la cita.<br>
**Dependencias:** definir una consulta de citas asignadas con autorización y datos mínimos, aclarar la navegación y resolver el alcance del ciclo User–Professional bajo DEC-005 `OPEN`. Cualquier contrato o permiso nuevo requiere decisión e implementación posterior; no se elige aquí una solución.<br>
**Limitaciones:** no hay GET de citas para PROFESSIONAL, pantalla de listado/detalle, agenda personal ni portal completo. La existencia de tablas `professionals`/`appointments`, una FK o los dos POST operativos no construye una UI clínica.<br>
**Trazabilidad:** referencia WF-15 → CU `—` (PROFESSIONAL no participa en CU-D1–D8) → RF-030 `PARCIAL` para consulta y RF-028/029 `IMPLEMENTADO` solo como mutaciones backend → portal UX `CONCEPTUAL / NO IMPLEMENTADO`.

## 5. Exclusiones y dependencias futuras

**NO EXISTE actualmente como UX:** dashboard profesional, agenda personal, lista/detalle autorizado de citas asignadas, gestión de pacientes, atención médica, historia clínica, seguimiento clínico, diagnósticos, tratamientos o prescripciones. El actor no obtiene por DEC-007 acceso a la disponibilidad sanitizada PATIENT/RECEPTIONIST, ni administra profesionales u horarios por CU-D1/CU-D4.

Antes de plantear una navegación futura se necesitarían decisiones sobre alcance de consulta y minimización de datos, autorización del rol, contrato de lectura y experiencia de destino. DEC-005 sigue `OPEN` para el ciclo de vinculación User–Professional; el vínculo opcional de V1 y las comprobaciones actuales de ownership no cierran esa decisión. Estos puntos son **dependencias**, no requisitos nuevos ni aprobación de un diseño.

## 6. Fuentes y validación de alcance

Fuentes: [inventario F.3.1](WIREFRAME-INVENTORY.md), [post-audit F.3.1](WIREFRAME-INVENTORY-POST-AUDIT.md), [flujos privados F.2.2](../user-flows/PRIVATE-PORTALS-USER-FLOWS.md), [post-audit F.2.2](../user-flows/PRIVATE-PORTALS-USER-FLOW-POST-AUDIT.md), [consolidación F.2.3](../user-flows/USER-FLOW-CONSOLIDATION.md), [D.2](../../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [D.3.1](../../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md), [D.3.2](../../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md), [D.3.3](../../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md), [D.4 catálogo](../../agenda/consolidation/AGENDA-USE-CASE-CATALOG.md), [D.4 actores](../../agenda/consolidation/AGENDA-ACTOR-USECASE-MATRIX.md), [D.4 trazabilidad](../../agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md), [Domain Baseline](../../DOMAIN-BASELINE.md) y [Decision Register](../../DOMAIN-DECISION-REGISTER.md).

Se creó únicamente este Markdown de alcance UX. `git status --short --branch` confirmó `feat/hospitalplatform-wireframes` y mostró solo `?? docs/ux/wireframes/`, carpeta con entregables previos sin seguimiento. `git diff --check` no reportó errores en archivos rastreados; la revisión directa del archivo nuevo encontró quince enlaces locales válidos y ningún espacio final. Archivos existentes modificados en F.3.2.4: **0**. Java: **0**; SQL: **0**; tests: **0**; migraciones: **0**; seguridad: **0**. No se ejecutó Maven ni se hizo commit, push o merge.

**🟡 PROFESSIONAL SCOPE COMPLETE WITH OBSERVATIONS.** La presencia del actor y las dos mutaciones backend están documentadas sin crear un portal. RF-030 sigue `PARCIAL`, no hay GET de citas para PROFESSIONAL y DEC-005 permanece `OPEN`; por eso la referencia WF-15 no es navegable.
