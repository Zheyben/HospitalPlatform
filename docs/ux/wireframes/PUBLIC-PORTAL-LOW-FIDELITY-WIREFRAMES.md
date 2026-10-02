# ETAPA F.3.2.1 — Wireframes de baja fidelidad del portal público

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico del Hospital de Huaycán.<br>
**Rama:** `feat/hospitalplatform-wireframes`.<br>
**Alcance:** diseño documental; ninguna lámina acredita una interfaz construida. `IMPLEMENTADO` califica el backend, `PARCIAL` delimita una capacidad incompleta y `CONCEPTUAL` no dispone de contrato ejecutable. El portal RF-010 y el autorregistro RF-001 permanecen `FUTURO`.

## 1. Objetivo

Bosquejar el acceso del paciente y el resultado de una reserva propia sin transformar el recorrido deseado en funcionalidad actual. Las láminas numeradas 01–05 de este documento agrupan las referencias oficiales `WF-01`–`WF-06` del [inventario](WIREFRAME-INVENTORY.md): la lámina 04 distingue `WF-04` (descubrimiento conceptual) de `WF-05` (envío de reserva implementado en backend), y la lámina 05 corresponde a `WF-06`. `WF-07` («Mis citas / detalle») solo se cita como posible continuación respaldada por GET; no se diseña otra pantalla en esta entrega.

Las cajas muestran **jerarquía y contenido provisional**. No fijan marca institucional, microcopy, rutas de frontend, calendario, filtros ni permisos. Un contorno de diseño no significa que el control esté disponible para el usuario.

## 2. Mapa de navegación

```text
USUARIO EXISTENTE
01 Acceso al portal [FUTURO como frontend; WF-01]
    ↓ acceso a login
02 Login [backend IMPLEMENTADO; WF-02]
    ↓ cuenta PATIENT válida con perfil activo vinculado
04-A Descubrimiento de cita [CONCEPTUAL; WF-04; sin API PATIENT]
    ╳ no existe transición ejecutable de selección a reserva

Con slotId usable conocido por una vía autorizada aún no resuelta en UX:
04-B Envío de reserva [backend IMPLEMENTADO; WF-05]
    ↓ respuesta real del POST
05 Resultado [201 → SCHEDULED; RF-015 PARCIAL; WF-06]
    ↓ consulta propia opcional
WF-07 Mis citas / detalle [backend GET IMPLEMENTADO; fuera de estas láminas]

USUARIO NUEVO
01 Acceso al portal → 03 Registro [CONCEPTUAL en el dibujo / FUTURO en producto]
                              ╳ DEC-017 OPEN; no existe autorregistro público
Una cuenta creada y habilitada por un proceso vigente independiente puede usar 02.
No hay transición automática desde 03 a 02 ni desde 02 a una búsqueda operativa.
```

Las flechas son **intención de navegación**. `WF-04` no es precondición técnica de `POST /api/v1/appointments`; `WF-05` solo funciona con `slotId` conocido. CU-D8 protege el envío internamente y no produce una pantalla propia.

## 3. Wireframe 01 — Landing / Acceso

**Referencia de inventario:** `WF-01`.<br>
**Actor:** visitante que pretende acceder como PATIENT.<br>
**Estado:** `FUTURO` como portal RF-010; el acceso a login es intención de diseño, no pantalla construida.<br>
**Objetivo:** orientar hacia la autenticación y señalar el registro pendiente.

**Estructura:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] HOSPITALPLATFORM · identidad visual provisional   │
├──────────────────────────────────────────────────────────────┤
│ [CONTENIDO]                                                 │
│ Acceso a los servicios de citas                              │
│ El contenido público definitivo queda por definir.          │
│                                                              │
│ [ACCIONES]                                                   │
│ [Ir a login]  [Registro de paciente · FUTURO / no activo]     │
├──────────────────────────────────────────────────────────────┤
│ [FOOTER] Información institucional: contenido no definido   │
└──────────────────────────────────────────────────────────────┘
```

**Datos visibles:** nombre del sistema como rótulo de prototipo; sin oferta médica, horarios, métricas ni aval institucional.<br>
**Acciones usuario:** elegir acceso a la lámina 02; la referencia a registro identifica la lámina 03, pero no habilita un alta.<br>
**Estados UX:** presentación inicial; no hay consulta, carga, éxito o error de negocio propios.<br>
**Dependencias:** RF-010 `DEFINED_NOT_IMPLEMENTED`; registro RF-001 y DEC-017 `OPEN`.<br>
**Trazabilidad:** Landing / Acceso → CU `—` (navegación, sin CU-D) → RF-010 → `FUTURO`.

## 4. Wireframe 02 — Login

**Referencia de inventario:** `WF-02`.<br>
**Actor:** PATIENT con cuenta existente; la operación de autenticación es compartida con los roles actuales, sin añadir permisos.<br>
**Estado:** backend `IMPLEMENTADO` para login; pantalla de portal no implementada.<br>
**Objetivo:** autenticar una cuenta existente habilitada.

**Estructura:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] HOSPITALPLATFORM · Acceso                           │
├──────────────────────────────────────────────────────────────┤
│ [CONTENIDO]                                                 │
│ Correo electrónico [____________________________]             │
│ Contraseña         [____________________________]             │
│ [Área de validación o rechazo según respuesta real]           │
│                                                              │
│ [ACCIONES] [Ingresar]                                         │
├──────────────────────────────────────────────────────────────┤
│ [FOOTER] Sin recuperación de cuenta ni registro activo       │
└──────────────────────────────────────────────────────────────┘
```

**Datos visibles:** `email`, `password` como entradas; respuesta de autenticación sin inventar datos de perfil.<br>
**Acciones usuario:** completar ambos campos y enviar el login vigente `POST /api/v1/auth/login`. El destino del paciente depende de su rol y de las capacidades posteriores, sin selector de portal.<br>
**Estados UX:** vacío; validando/cargando; error de validación `400` o rechazo `401` según respuesta; acceso correcto. Estos son estados de presentación, no estados nuevos de cuenta.<br>
**Dependencias:** RF-002; cuenta habilitada. La reserva exige además perfil PATIENT activo vinculado y `slotId` conocido.<br>
**Trazabilidad:** Login → CU `—` (Auth fuera de CU-D1–D8) → RF-002 → backend `IMPLEMENTADO`; UI pendiente.

## 5. Wireframe 03 — Registro paciente

**Referencia de inventario:** `WF-03`.<br>
**Actor:** visitante que pretende ser PATIENT.<br>
**Estado:** `CONCEPTUAL` **solo como lámina**; autorregistro `FUTURO`, RF-001 y DEC-017 `OPEN`.<br>
**Objetivo:** hacer visible la intención de alta sin presentarla como acción ejecutable.

**Estructura:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] Registro de paciente · CONCEPTUAL / FUTURO          │
├──────────────────────────────────────────────────────────────┤
│ [CONTENIDO]                                                 │
│ [Zona reservada para datos de identidad · por decidir]       │
│ [Zona reservada para credenciales · por decidir]             │
│ [Consentimiento y verificación · por decidir]                │
│ No hay campos obligatorios ni formulario aprobado.           │
│                                                              │
│ [ACCIONES] [Volver a login]  [Crear cuenta · no activo]       │
├──────────────────────────────────────────────────────────────┤
│ [FOOTER] DEC-017 OPEN · sin resultado de alta definido        │
└──────────────────────────────────────────────────────────────┘
```

**Datos visibles:** categorías de información pendientes para estudiar UX; **ningún campo concreto, DTO, tabla, regla de duplicado o consentimiento se fija aquí**. El `email`/`password` de login no define por sí mismo el formulario público de alta.<br>
**Acciones usuario:** volver a la lámina 02. «Crear cuenta» es rótulo inactivo de intención, sin envío ni promesa de cuenta creada.<br>
**Estados UX:** no se dibujan validación, aprobación, rechazo ni éxito de alta; siguen sin contrato.<br>
**Dependencias:** RF-001, DEC-017 `OPEN`; creación/vinculación vigente de User y Patient es administrativa, sin autorregistro público.<br>
**Trazabilidad:** Registro paciente → CU `—` (fuera de CU-D1–D8) → RF-001 → `FUTURO` / representación `CONCEPTUAL`, no implementado.

## 6. Wireframe 04 — Solicitud de cita

**Referencias de inventario:** `WF-04` descubrimiento y `WF-05` envío.<br>
**Actor:** PATIENT autenticado; para el envío debe tener perfil activo vinculado.<br>
**Estado:** zona A `CONCEPTUAL` CU-D6; zona B backend `IMPLEMENTADO` CU-D7 con `slotId` conocido. La pantalla integral no existe.<br>
**Objetivo:** separar la intención de buscar una opción de la solicitud de reserva que acepta la API vigente.

**Estructura:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] Solicitud de cita · prototipo no implementado       │
├──────────────────────────────────────────────────────────────┤
│ [CONTENIDO A — CONCEPTUAL; sin consulta PATIENT]              │
│ Especialidad: referencia de necesidad, sin selector real     │
│ Profesional: si correspondiera, por definir                  │
│ Fecha/hora disponible: por definir; sin calendario/slots      │
│ [Consultar / seleccionar · no activo]                        │
│ ───── CORTE: no existe API de descubrimiento del rol ─────── │
│ [CONTENIDO B — solicitud con dato ya conocido]               │
│ slotId* [________________] (identificador conocido)           │
│ reason  [________________] (opcional)                         │
│ Datos adicionales: ninguno exigido al PATIENT en el request  │
│                                                              │
│ [ACCIONES] [Enviar reserva con slotId conocido]              │
├──────────────────────────────────────────────────────────────┤
│ [FOOTER] La oferta visible no garantiza la reserva           │
└──────────────────────────────────────────────────────────────┘
```

**Datos visibles:** especialidad, profesional y fecha/hora son **anotaciones de necesidad conceptual**, no campos activos, filtros aprobados ni valores del DTO de disponibilidad ADMIN. En la zona respaldada por API solo `slotId` obligatorio y `reason` opcional; no se solicita `patientId` al PATIENT. El origen UX del `slotId` permanece sin resolver.<br>
**Acciones usuario:** ninguna búsqueda ni selección ejecutable en la zona A. Con un `slotId` conocido, enviar una vez `POST /api/v1/appointments` desde la zona B y esperar su respuesta; el botón representa el contrato backend, no una UI existente.<br>
**Estados UX:** zona A sin vacío/cargando/resultados reales. Zona B: formulario sin `slotId`, enviando, éxito `201` o error real (`400`, `403`, `404`, `409 SLOT_UNAVAILABLE`, según respuesta). No mostrar reserva provisional ante `409`.<br>
**Dependencias:** DEC-007 `CLOSED` aprueba actores y exposición sanitizada conceptual, sin endpoint, DTO o permiso PATIENT; CU-D6/RF-012 sigue `CONCEPTUAL`. CU-D7/RF-013 exige slot usable conocido. CU-D8/RF-014 es protección interna contra doble reserva. DEC-008/010 no fijan generación, duración, calendario ni ventanas.<br>
**Trazabilidad:** Solicitud A → CU-D6 → RF-012 → `CONCEPTUAL`; solicitud B → CU-D7 (+ CU-D8 interno) → RF-013/014 → backend `IMPLEMENTADO`, acceso UX al slot pendiente.

## 7. Wireframe 05 — Confirmación de reserva registrada

**Referencia de inventario:** `WF-06`. El título de la lámina nombra la **confirmación visual del resultado del envío**, no el estado de dominio `CONFIRMED`.<br>
**Actor:** PATIENT que envió una reserva propia.<br>
**Estado:** respuesta backend `IMPLEMENTADO` de CU-D7; constancia UX RF-015 `PARCIAL`; pantalla no construida.<br>
**Objetivo:** mostrar el resultado real y evitar confundir reserva creada con asistencia confirmada.

**Estructura:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] Resultado de solicitud                              │
├──────────────────────────────────────────────────────────────┤
│ [CONTENIDO — solo después de respuesta real]                 │
│ Si 201: Reserva registrada                                   │
│          Cita ID: [id recibido]                               │
│          Estado: SCHEDULED                                    │
│          [resumen solo de datos devueltos por el DTO]         │
│ Si rechazo: [error devuelto] · sin cita creada                │
│                                                              │
│ [ACCIONES] [Ir a Mis citas] (si existe cita propia)           │
├──────────────────────────────────────────────────────────────┤
│ [FOOTER] SCHEDULED ≠ CONFIRMED                                │
└──────────────────────────────────────────────────────────────┘
```

**Datos visibles:** `id`, `appointmentStatus=SCHEDULED` y otros datos efectivamente presentes en `AppointmentResponseDTO`; no completar fecha/hora resuelta, especialidad o profesional por inferencia. La respuesta `201` incluye `Location`, pero no acredita una constancia visual final.<br>
**Acciones usuario:** consultar la cita propia mediante la vista inventariada `WF-07`, si existe; no ejecutar confirmación de asistencia automáticamente. La acción posterior de confirmar pertenece al detalle de cita elegible, no a esta lámina.<br>
**Estados UX:** esperando respuesta; reserva registrada solo tras `201`; error/rechazo sin comprobante si falla. La creación devuelve `SCHEDULED`; `CONFIRMED` exige operación independiente.<br>
**Dependencias:** CU-D7/RF-013 para alta, RF-015 `PARCIAL` para constancia, RF-006 para consulta posterior. La disponibilidad del slot se vuelve a comprobar durante la reserva; CU-D8 no tiene UI propia.<br>
**Trazabilidad:** Resultado de reserva → CU-D7 → RF-015 (y RF-013 como origen del `201`) → respuesta backend existente / constancia UX `PARCIAL`.

## 8. Exclusiones y fuentes

Quedan fuera historia clínica, atención médica, agenda profesional, notificaciones, waitlist, prioridad, generación automática de slots, calendario, duración, ventanas temporales, catálogo funcional de especialidades, datos institucionales definitivos y cualquier módulo futuro. No se diseña una consulta de disponibilidad PATIENT conectada, un registro público ejecutable ni una confirmación de asistencia automática. Ningún wireframe añade endpoint, DTO, permiso, regla de negocio o estado persistido.

Fuentes: [inventario F.3.1](WIREFRAME-INVENTORY.md), [post-audit F.3.1](WIREFRAME-INVENTORY-POST-AUDIT.md), [flujo público F.2.1](../user-flows/PUBLIC-PORTAL-USER-FLOWS.md), [post-audit F.2.1](../user-flows/PUBLIC-PORTAL-USER-FLOW-POST-AUDIT.md), [consolidación F.2.3](../user-flows/USER-FLOW-CONSOLIDATION.md), [requisitos UX F.1](../UX-REQUIREMENTS-ANALYSIS.md) y [documento maestro Word](../../documentation-export/HOSPITALPLATFORM-COMPLETE-DOCUMENTATION.docx). La clasificación conserva el alcance de esas fuentes; las láminas no reemplazan el contrato funcional.

## 9. Validación de alcance

Se creó únicamente este Markdown de baja fidelidad. `git status --short --branch` confirmó `feat/hospitalplatform-wireframes` y mostró solo `?? docs/ux/wireframes/`, carpeta que ya contenía los entregables F.3.1 sin seguimiento. `git diff --check` no informó errores en archivos rastreados; este archivo nuevo se revisó directamente: cinco láminas, ocho enlaces locales válidos y sin espacios finales. Archivos existentes modificados en F.3.2.1: **0**. Java: **0**; SQL: **0**; tests: **0**; migraciones: **0**; seguridad: **0**. No se ejecutó Maven ni se hizo commit, push o merge.

**🟡 PUBLIC PORTAL WIREFRAMES COMPLETE WITH OBSERVATIONS.** Los bosquejos son utilizables como base documental de prototipo, pero el autorregistro, la consulta de disponibilidad PATIENT y la llegada UX a un `slotId` siguen sin implementación para un recorrido público continuo.
