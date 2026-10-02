# ETAPA F.3.2.3 — Wireframes de baja fidelidad del portal RECEPTIONIST

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico del Hospital de Huaycán.

**Alcance:** diseño documental de operaciones de citas. `IMPLEMENTADO` califica el backend, **no una pantalla construida**; `PARCIAL` identifica una brecha de requisito o navegación; `CONCEPTUAL` carece de API/permiso funcional; `FUTURO` indica implementación posterior sin fecha comprometida. Las láminas no crean rutas, DTO, permisos ni reglas de negocio.

## 1. Objetivo y mapa de navegación

Bosquejar el acceso, la consulta y las operaciones de citas que corresponden al rol RECEPTIONIST. Las láminas 03 y 04 desdoblan la referencia inventariada `WF-14` para distinguir ciclo de cita y alta; no constituyen dos casos de uso nuevos. La lámina 05 **no es una pantalla navegable del inventario**: registra visualmente la brecha CU-D6 para evitar que se reutilice el GET ADMIN.

```text
01 Acceso [WF-02; login backend IMPLEMENTADO]
    ↓ cuenta RECEPTIONIST válida
02 Entrada a citas [WF-12; navegación UX CONCEPTUAL]
    ├─ 03 Lista/detalle [WF-13; GET backend IMPLEMENTADO]
    │     └─ acciones elegibles de cita [WF-14; backend IMPLEMENTADO por estado]
    └─ 04 Alta de cita [WF-14; backend IMPLEMENTADO con patientId/slotId conocidos]
          ↓ 201 → SCHEDULED; 409 → sin reserva

05 Disponibilidad sanitizada [CU-D6 CONCEPTUAL; referencia NO NAVEGABLE]
    ╳ sin GET ni permiso RECEPTIONIST; no provee slotId hoy
```

Las flechas indican organización UX propuesta. El backend permite reservar un `slotId` conocido sin consulta previa CU-D6; obtener ese identificador y el `patientId` por una experiencia autorizada sigue pendiente. `SCHEDULED` es el estado inicial de la cita creada; `CONFIRMED` exige una operación separada.

## 2. Wireframe 01 — Receptionist Access

**Referencia de inventario:** `WF-02`, login compartido.<br>
**Actor:** RECEPTIONIST con cuenta existente.<br>
**Estado:** login backend `IMPLEMENTADO`; portal UI no construido.<br>
**Objetivo:** autenticar la cuenta para acceder a las operaciones de citas autorizadas.

**Estructura:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] HOSPITALPLATFORM · Acceso                            │
├──────────────────────────────────────────────────────────────┤
│ [CONTENIDO]                                                  │
│ Correo electrónico [________________________]                  │
│ Contraseña         [________________________]                  │
│ [Área de validación / error según respuesta]                  │
│                                                              │
│ [ACCIONES] [Ingresar]                                         │
├──────────────────────────────────────────────────────────────┤
│ [FOOTER] Sin selección de portal ni funciones clínicas       │
└──────────────────────────────────────────────────────────────┘
```

**Datos visibles:** entradas `email`, `password` y resultado de autenticación o rechazo; sin datos clínicos o de perfil inventados.<br>
**Acciones usuario:** enviar `POST /api/v1/auth/login`, compartido por los roles vigentes. El destino UX propuesto depende del rol de la sesión; cada recurso posterior conserva su autorización.<br>
**Validaciones:** campos obligatorios y formato de email; credenciales y cuenta habilitada se verifican en backend. No se atribuye una causa específica a un rechazo sin respuesta que la identifique.<br>
**Estados UX:** vacío, validando/cargando, `400` de entrada, `401` de autenticación o acceso correcto. Son estados de presentación, no estados nuevos de cuenta.<br>
**Dependencias:** RF-002 y rol RECEPTIONIST existente; no hay endpoint de login exclusivo ni selector de portal.<br>
**Trazabilidad:** Receptionist Access → CU `—` (Auth fuera de CU-D1–D8) → RF-002 → backend `IMPLEMENTADO`, UI propuesta.

## 3. Wireframe 02 — Receptionist Home / Navigation

**Referencia de inventario:** `WF-12`.<br>
**Actor:** RECEPTIONIST autenticado.<br>
**Estado:** navegación UX `CONCEPTUAL / NO IMPLEMENTADA`; sin API de dashboard.<br>
**Objetivo:** orientar a la consulta y gestión de citas ya documentadas.

**Estructura:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] Área de citas · RECEPTIONIST                         │
├──────────────────────────────────────────────────────────────┤
│ [CONTENIDO]                                                  │
│ [Citas: lista / detalle]    [Nueva cita con IDs conocidos]   │
│ Sin indicadores, reportes, pacientes ni módulos clínicos.    │
│                                                              │
│ [ACCIONES] Abrir consulta o alta de cita candidata.           │
├──────────────────────────────────────────────────────────────┤
│ [FOOTER] Organización UX, no módulo/API propio                │
└──────────────────────────────────────────────────────────────┘
```

**Datos visibles:** solo destinos `WF-13` y `WF-14`, sin métricas, panel de pacientes o agenda.<br>
**Acciones usuario:** abrir las vistas candidatas de citas; la entrada no realiza una operación de negocio propia.<br>
**Validaciones:** sesión/rol aplicados en cada operación real de Appointments; esta referencia de navegación no concede permisos nuevos.<br>
**Estados UX:** no se define vacío, éxito o error de un dashboard inexistente; los estados aparecen al consultar o mutar citas.<br>
**Dependencias:** F.2.2/F.2.3; la búsqueda de pacientes y disponibilidad para el rol no está implementada.<br>
**Trazabilidad:** Receptionist Home → CU `—` → RF `—` → UX `CONCEPTUAL / NO IMPLEMENTADA`.

## 4. Wireframe 03 — Appointment Management

**Referencias de inventario:** `WF-13` lista/detalle y porción de gestión de `WF-14`.<br>
**Actor:** RECEPTIONIST autenticado.<br>
**Estado:** GET de citas y transiciones elegibles `IMPLEMENTADO` en backend; RF-030 global `PARCIAL` por ausencia de consulta para PROFESSIONAL. UI no construida.<br>
**Objetivo:** consultar citas accesibles y ejecutar solo transiciones operativas permitidas.

**Estructura:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] Citas · consulta y gestión                           │
├──────────────────────────────────────────────────────────────┤
│ [CONTENIDO] Lista GET                                        │
│ id  patientId  professionalId  slotId  appointmentStatus     │
│ [valores devueltos por API]                  [Abrir detalle]   │
│                                                              │
│ Detalle: id, patientId, professionalId, slotId, reason,       │
│          appointmentStatus, flowStage, fechas devueltas      │
│ [Área de respuesta / error de transición]                    │
│                                                              │
│ [ACCIONES según rol y estado de la cita]                      │
│ [Confirmar] [Cancelar] [Reprogramar con nuevo slotId]         │
│ [Registrar check-in] [Pasar a espera]                         │
├──────────────────────────────────────────────────────────────┤
│ [FOOTER] Sin historia clínica ni registro de atención        │
└──────────────────────────────────────────────────────────────┘
```

**Datos visibles:** campos recibidos en `AppointmentResponseDTO`: `id`, `patientId`, `professionalId`, `slotId`, `appointmentStatus`, `flowStage`, `reason` y fechas presentes. No se infiere identidad completa del paciente, diagnóstico o detalle clínico.<br>
**Acciones usuario:** `GET /api/v1/appointments` y `GET /api/v1/appointments/{id}`; `POST /api/v1/appointments/{id}/confirm`, `/cancel` o `/reschedule` según elegibilidad. Reprogramar requiere **nuevo `slotId` conocido**. `POST /api/v1/appointments/{id}/check-in` aplica solo desde `CONFIRMED`/`flowStage=null`; `/waiting` desde `CONFIRMED`/`CHECK_IN`. Los controles del dibujo se muestran habilitados únicamente cuando el estado y rol lo permiten; no se dibuja inicio/fin de atención.<br>
**Validaciones:** consulta sujeta al rol. Transiciones y elegibilidad verificadas por el backend; un rechazo no cambia la cita. No se inventan ventanas temporales ni selección de slot para reprogramación.<br>
**Estados UX:** lista cargando/vacía, detalle no encontrado `404`, sin sesión `401`, acceso denegado `403`, error de transición `409` cuando corresponda, resultado real tras una acción. `SCHEDULED`, `CONFIRMED`, `CANCELLED`, `RESCHEDULED`, `COMPLETED` y `flowStage` son datos persistidos del dominio, no estados UX nuevos.<br>
**Dependencias:** GET de Appointments para RECEPTIONIST; RF-030 global `PARCIAL`. Confirmar/cancelar/reprogramar: RF-016/017/019; check-in/espera: RF-026/027. El rol no tiene GET de pacientes ni vista sanitizada de disponibilidad para obtener un nuevo `slotId`.<br>
**Trazabilidad:** lista/detalle → CU `—` (Appointments fuera de CU-D1–D8) → RF-030, porción RECEPTIONIST → GET `IMPLEMENTADO`, RF global `PARCIAL`; transiciones → CU `—` → RF-016/017/019/026/027 → backend `IMPLEMENTADO` según estado.

## 5. Wireframe 04 — Appointment Reservation Support

**Referencia de inventario:** `WF-14`, porción de alta.<br>
**Actor:** RECEPTIONIST autenticado, para un paciente activo identificado explícitamente.<br>
**Estado:** reserva CU-D7/RF-013 `IMPLEMENTADO` en backend; acceso UX a `patientId` y `slotId` `PARCIAL / PENDIENTE`. CU-D8/RF-014 es invariante interna, sin pantalla.<br>
**Objetivo:** registrar una cita con identificadores ya conocidos y mostrar el resultado real.

**Estructura:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] Nueva cita · RECEPTIONIST                            │
├──────────────────────────────────────────────────────────────┤
│ [CONTENIDO]                                                  │
│ patientId* [________________]  (conocido, paciente activo)     │
│ slotId*    [________________]  (conocido, slot usable)         │
│ reason     [________________]  (opcional)                     │
│ Sin búsqueda de pacientes ni consulta de slots del rol.      │
│                                                              │
│ Resultado 201: id recibido · SCHEDULED · flowStage=null      │
│ Resultado de error: respuesta real, sin reserva provisional   │
│                                                              │
│ [ACCIONES] [Enviar reserva] [Abrir detalle si se creó]        │
├──────────────────────────────────────────────────────────────┤
│ [FOOTER] SCHEDULED ≠ CONFIRMED                                │
└──────────────────────────────────────────────────────────────┘
```

**Datos visibles:** `patientId` explícito y `slotId` obligatorio del request actual; `reason` opcional. Profesional y especialidad se derivan del slot/schedule en el backend, no se fijan como entradas del rol. Mostrar `id`, `appointmentStatus=SCHEDULED` y `flowStage=null` únicamente tras la respuesta `201`.<br>
**Acciones usuario:** enviar `POST /api/v1/appointments` con identificadores conocidos; esperar resultado antes de abrir detalle. No existe botón de confirmación automática ni reserva de slot desde la pantalla de disponibilidad conceptual.<br>
**Validaciones:** `patientId` requerido para RECEPTIONIST aunque el DTO lo tipa opcional; `slotId` obligatorio; paciente activo y slot usable comprobados dentro de la operación. Entrada inválida `400`, paciente no disponible `404`, slot no usable o contención `409 SLOT_UNAVAILABLE` según respuesta. El fallo revierte la operación, sin cita o slot parcialmente reservado.<br>
**Estados UX:** formulario vacío, enviando, alta `201` con cita `SCHEDULED`, rechazo real sin comprobante. `CONFIRMED` solo puede surgir de la confirmación posterior de una cita elegible; no es estado inicial.<br>
**Dependencias:** CU-D7/RF-013, CU-D8/RF-014 interno. No hay GET de pacientes para RECEPTIONIST ni consulta autorizada de disponibilidad; obtener `patientId` y `slotId` por UX sigue pendiente. RF-014 tiene prueba concurrente de dos solicitudes; el ensayo SRS de veinte permanece pendiente, sin afectar la representación de la regla actual.<br>
**Trazabilidad:** Appointment Reservation Support → CU-D7 (+ CU-D8 interno) → RF-013/014 → backend `IMPLEMENTADO`; selección de identificadores UX `PARCIAL / PENDIENTE`.

## 6. Wireframe 05 — Availability Consultation (referencia no navegable)

**Referencia de inventario:** **ninguna vista RECEPTIONIST funcional**; solo objetivo CU-D6 documentado en D.2–D.4. Este número ordena el análisis, no añade un ID `WF` ni una ruta de navegación.<br>
**Actor:** RECEPTIONIST autenticado como actor **conceptual** de DEC-007.<br>
**Estado:** CU-D6/RF-012 `CONCEPTUAL`; posible implementación `FUTURO`, sin endpoint, DTO, filtro ni permiso configurado para el rol.<br>
**Objetivo:** registrar el límite de una futura consulta sanitizada sin simular una pantalla operativa.

**Estructura:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] Disponibilidad · CONCEPTUAL / NO NAVEGABLE           │
├──────────────────────────────────────────────────────────────┤
│ [CONTENIDO]                                                  │
│ Objetivo de dominio: oferta minimizada para reserva.         │
│ Campos, filtros, resultados y estados: por definir.          │
│ No se muestran slots internos de la consulta ADMIN.          │
│                                                              │
│ [ACCIONES] Ninguna acción ejecutable o enlace desde citas.    │
├──────────────────────────────────────────────────────────────┤
│ [FOOTER] DEC-007: actores/exposición; API no implementada    │
└──────────────────────────────────────────────────────────────┘
```

**Datos visibles:** solo anotaciones de alcance conceptual. No se presentan campos, disponibilidad real, filtros, calendario, horas ni `AvailabilitySlotResponseDTO` de ADMIN.<br>
**Acciones usuario:** ninguna; no hay consulta ni selección conectable. La reserva CU-D7 puede funcionar con un `slotId` conocido sin esta consulta.<br>
**Validaciones:** no se definen reglas ni códigos HTTP de una API inexistente; autenticar al rol no concede todavía acceso a la futura vista.<br>
**Estados UX:** no se modelan cargando, vacío, éxito o error de resultados porque no existe contrato de consulta RECEPTIONIST. `CONCEPTUAL` y `FUTURO` son etiquetas documentales, no estados de slot o cita.<br>
**Dependencias:** DEC-007 `CLOSED` solo para actores y frontera de exposición; RF-012/CU-D6 sin implementación. CU-D5 ofrece GET operativo exclusivamente ADMIN y no se reutiliza aquí. DEC-010 mantiene abiertas las ventanas temporales.<br>
**Trazabilidad:** Availability Consultation → CU-D6 → RF-012 (porción RECEPTIONIST) → `CONCEPTUAL / NO IMPLEMENTADO`.

## 7. Exclusiones y fuentes

Quedan fuera historia clínica, atención médica, diagnóstico, recetas/prescripciones, agenda profesional, configuración de horarios, gestión profesional, administración de pacientes, reportes, métricas, dashboard, generación automática de slots, notificaciones, waitlist y reglas temporales futuras. Los POST de inicio/fin de estado operacional corresponden al rol PROFESSIONAL asignado y no se muestran como acciones RECEPTIONIST. Ninguna tabla SQL o decisión `CLOSED` se convierte en permiso funcional.

Fuentes: [inventario F.3.1](WIREFRAME-INVENTORY.md), [post-audit F.3.1](WIREFRAME-INVENTORY-POST-AUDIT.md), [flujos privados F.2.2](../user-flows/PRIVATE-PORTALS-USER-FLOWS.md), [post-audit F.2.2](../user-flows/PRIVATE-PORTALS-USER-FLOW-POST-AUDIT.md), [consolidación F.2.3](../user-flows/USER-FLOW-CONSOLIDATION.md), [D.2](../../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [D.3.1](../../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md), [D.3.2](../../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md), [D.3.3](../../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md), [D.4 catálogo](../../agenda/consolidation/AGENDA-USE-CASE-CATALOG.md), [D.4 actores](../../agenda/consolidation/AGENDA-ACTOR-USECASE-MATRIX.md), [D.4 trazabilidad](../../agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md) y [documento maestro Word](../../documentation-export/HOSPITALPLATFORM-COMPLETE-DOCUMENTATION.docx).

## 8. Validación de alcance

Se creó únicamente este Markdown de baja fidelidad. `git status --short --branch` confirmó `feat/hospitalplatform-wireframes` y mostró solo `?? docs/ux/wireframes/`, carpeta con entregables previos sin seguimiento. `git diff --check` no reportó errores en archivos rastreados; la revisión directa del archivo nuevo encontró cinco láminas, trece enlaces locales válidos y ningún espacio final. Archivos existentes modificados en F.3.2.3: **0**. Java: **0**; SQL: **0**; tests: **0**; migraciones: **0**; seguridad: **0**. No se ejecutó Maven ni se hizo commit, push o merge.

**🟡 RECEPTIONIST WIREFRAMES COMPLETE WITH OBSERVATIONS.** La consulta y las operaciones de cita se dibujan con respaldo de backend; la navegación sigue siendo propuesta, la obtención de `patientId`/`slotId` está pendiente y la disponibilidad para RECEPTIONIST permanece conceptual y no navegable.
