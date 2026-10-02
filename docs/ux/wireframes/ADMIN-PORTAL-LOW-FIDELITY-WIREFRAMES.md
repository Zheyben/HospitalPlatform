# ETAPA F.3.2.2 — Wireframes de baja fidelidad del portal ADMIN

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico del Hospital de Huaycán.

**Alcance:** propuesta de interfaz documental. `IMPLEMENTADO` y `PARCIAL` indican soporte del backend, **no pantallas construidas**. `CONCEPTUAL` indica organización UX sin contrato funcional propio. Las cajas no son rutas de frontend, permisos nuevos ni diseño visual definitivo.

## 1. Objetivo y mapa de navegación

Bosquejar el acceso ADMIN y las tres áreas documentadas en F.2.2: profesionales, horarios y disponibilidad operativa. Cada área es un destino independiente. Consultar slots existentes no exige crear un profesional u horario durante la sesión, y crear un Schedule no genera slots.

```text
01 Acceso ADMIN [WF-02; login backend IMPLEMENTADO]
    ↓ cuenta autenticada con rol ADMIN
02 Inicio / navegación [WF-08; UX CONCEPTUAL, sin dashboard/API]
    ├─ 03 Profesionales [WF-09; CU-D1 PARCIAL]
    ├─ 04 Horarios [WF-10; CU-D4 PARCIAL]
    └─ 05 Disponibilidad operativa [WF-11; CU-D5 IMPLEMENTADO para ADMIN]
```

La flecha después del login representa un destino de diseño por rol. No existe login ADMIN distinto ni pantalla de selección de portal. `WF-08` no autoriza un módulo de reportes, métricas o configuración hospitalaria.

## 2. Wireframe 01 — Admin Access

**Referencia del inventario:** `WF-02` (login compartido).<br>
**Actor:** ADMIN con cuenta existente.<br>
**Estado:** autenticación de backend `IMPLEMENTADO`; wireframe de pantalla, no frontend existente.<br>
**Objetivo:** ingresar con la cuenta ADMIN habilitada.

**Estructura:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] HOSPITALPLATFORM · Acceso                            │
├──────────────────────────────────────────────────────────────┤
│ [CONTENIDO]                                                  │
│ Correo electrónico [________________________]                  │
│ Contraseña         [________________________]                  │
│ [Área de error según respuesta real]                          │
│                                                              │
│ [ACCIONES] [Ingresar]                                         │
└──────────────────────────────────────────────────────────────┘
```

**Datos visibles:** entradas `email` y `password`; resultado de autenticación o rechazo, sin datos de perfil inventados.<br>
**Acciones:** enviar `POST /api/v1/auth/login`, operación compartida por los roles actuales. Continuar a la navegación candidata solo si la sesión corresponde a ADMIN; cada recurso conserva su autorización.<br>
**Validaciones:** ambos campos obligatorios y formato de email; credenciales y habilitación de cuenta evaluadas por el backend. No se atribuye un motivo más preciso al rechazo si la respuesta no lo aporta.<br>
**Estados UX:** vacío, validando/cargando, error de entrada `400`, rechazo `401` o acceso correcto. Son estados de presentación, no estados persistidos nuevos.<br>
**Dependencias:** RF-002 y rol ADMIN vigente; no hay endpoint de «login administrativo» ni selección de portal.<br>
**Trazabilidad:** Admin Access → CU `—` (Auth fuera de CU-D1–D8) → RF-002 → backend `IMPLEMENTADO`, UI propuesta.

## 3. Wireframe 02 — Admin Home / Navigation

**Referencia del inventario:** `WF-08`.<br>
**Actor:** ADMIN autenticado.<br>
**Estado:** `CONCEPTUAL` como navegación UX; no existe dashboard ni API propia.<br>
**Objetivo:** orientar a las tres vistas ADMIN delimitadas por el incremento.

**Estructura:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] HOSPITALPLATFORM · Área operativa ADMIN              │
├──────────────────────────────────────────────────────────────┤
│ [CONTENIDO]                                                  │
│ [Profesionales]   [Horarios]   [Disponibilidad]               │
│ Sin indicadores, cifras ni actividad agregada.               │
│                                                              │
│ [ACCIONES] Abrir una de las tres vistas candidatas.           │
└──────────────────────────────────────────────────────────────┘
```

**Datos visibles:** solo rótulos de navegación hacia `WF-09`, `WF-10` y `WF-11`; ningún agregado del hospital.<br>
**Acciones:** abrir profesionales, horarios o disponibilidad. No hay acción de negocio propia de este inicio.<br>
**Validaciones:** acceso a cada recurso sujeto a sesión/rol ADMIN; el nodo de navegación no otorga permisos.<br>
**Estados UX:** referencia de navegación; no se diseñan vacío, éxito o error de un dashboard inexistente. Un rechazo de acceso corresponde al recurso real que se intente abrir.<br>
**Dependencias:** organización documentada en F.2.2/F.2.3; las capacidades subyacentes se delimitan en las láminas 03–05.<br>
**Trazabilidad:** Admin Home → CU `—` → RF `—` → navegación `CONCEPTUAL / NO IMPLEMENTADA`.

## 4. Wireframe 03 — Professional Management

**Referencia del inventario:** `WF-09`.<br>
**Actor:** ADMIN autenticado.<br>
**Estado:** CU-D1 `PARCIAL`; backend de alta, lista, detalle y actualización básica existente; UI no construida.<br>
**Objetivo:** crear, consultar y modificar la licencia de un perfil profesional vigente.

**Estructura:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] Profesionales · PARCIAL                              │
├──────────────────────────────────────────────────────────────┤
│ [CONTENIDO] Lista                                             │
│ id             userId        licenseNumber        active      │
│ [valor API]    [valor API]   [valor API]           [valor API] │
│ [Abrir detalle]                                               │
│                                                              │
│ Alta (vista candidata): licenseNumber* [__________]           │
│                         userId (opcional) [__________]        │
│ Detalle/edición: id [solo lectura] active [solo lectura]      │
│                  licenseNumber* [__________]                  │
│                                                              │
│ [ACCIONES] [Crear] [Consultar] [Guardar licencia]             │
└──────────────────────────────────────────────────────────────┘
```

**Datos visibles:** `id`, `userId`, `licenseNumber`, `active` devueltos por `ProfessionalResponseDTO`. `active` deriva de soft delete y se muestra **solo lectura**; el `userId` es opcional en alta y no editable por el PUT actual.<br>
**Acciones:** `GET /api/v1/professionals`, `GET /api/v1/professionals/{id}`, `POST /api/v1/professionals` y `PUT /api/v1/professionals/{id}`. Alta, consulta y edición son alternativas independientes. El PUT cambia únicamente `licenseNumber`; no se ofrece DELETE, desactivación HTTP ni reasignación de cuenta.<br>
**Validaciones:** licencia obligatoria, no vacía y de máximo 100 caracteres; unicidad gestionada por el backend. `userId` opcional sujeto a referencia válida al crear. Mostrar rechazo real de validación, `404 PROFESSIONAL_NOT_FOUND` o `409 DUPLICATE_PROFESSIONAL` según la operación, sin inventar mensajes literales.<br>
**Estados UX:** lista cargando o vacía, detalle cargando/no encontrado, formulario inválido, conflicto de licencia y resultado de alta/edición recibido. No se crea un nuevo estado de Professional.<br>
**Dependencias:** CU-D1/RF-007. DEC-005 sigue `OPEN` para el ciclo User–Professional; CU-D2 asociación N:M y CU-D3 catálogo son `CONCEPTUAL`, sin selector de especialidades ni API funcional.<br>
**Trazabilidad:** Professional Management → CU-D1 → RF-007 → `PARCIAL`.

## 5. Wireframe 04 — Schedule Management

**Referencia del inventario:** `WF-10`.<br>
**Actor:** ADMIN autenticado.<br>
**Estado:** CU-D4 `PARCIAL`; alta, consulta, actualización y cambio de `active` de Schedule en backend, sin generación de slots ni DELETE.<br>
**Objetivo:** consultar, crear y actualizar la configuración actual de horarios.

**Estructura:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] Horarios · PARCIAL                                   │
├──────────────────────────────────────────────────────────────┤
│ [CONTENIDO] Lista / filtros GET                               │
│ professionalId [________]  specialtyId [________]            │
│ id  professionalId  specialtyId  dayOfWeek  horas  active    │
│ [valores devueltos por API]  [Abrir detalle]                   │
│                                                              │
│ Alta / edición de Schedule                                    │
│ professionalId* [________]  specialtyId* [________]           │
│ dayOfWeek* (0–6) [__]                                         │
│ startTime* [______]       endTime* [______]                   │
│ active [valor de respuesta / cambio por PATCH separado]      │
│                                                              │
│ [ACCIONES] [Consultar] [Crear] [Guardar] [Cambiar active]      │
└──────────────────────────────────────────────────────────────┘
```

**Datos visibles:** `id`, `professionalId`, `specialtyId`, `dayOfWeek`, `startTime`, `endTime`, `active` del `AgendaResponseDTO`. La lista puede filtrarse por `professionalId` y `specialtyId`; esos identificadores no implican un buscador/catálogo funcional.<br>
**Acciones:** `GET /api/v1/agendas` y `GET /api/v1/agendas/{id}` para lectura; `POST /api/v1/agendas` para alta; `PUT /api/v1/agendas/{id}` para configuración; `PATCH /api/v1/agendas/{id}/status` para cambiar solo `active`. Son operaciones independientes, no pasos obligatorios de un asistente.<br>
**Validaciones:** los cinco datos de alta/PUT son obligatorios; `dayOfWeek` admite 0–6 y `endTime > startTime`. El backend exige profesional activo y referencia de specialty existente por FK, **sin comprobar specialty activa ni asociación profesional–especialidad**. Mostrar `400` o `404` según respuesta, sin definir duración, solapamiento ni reglas temporales nuevas.<br>
**Estados UX:** cargando, lista vacía, dato inválido, detalle no encontrado y éxito según DTO devuelto. `active` es un campo real de Schedule; cambiarlo no promete crear/cancelar slots ni modificar citas automáticamente.<br>
**Dependencias:** CU-D4/RF-011; `specialtyId` debe ser conocido porque CU-D3 no tiene catálogo Java/API. CU-D2 N:M sigue conceptual. DEC-008 y DEC-010 continúan `OPEN`; no hay generación, horizonte, calendario, ventanas o zona horaria aplicada en esta vista.<br>
**Trazabilidad:** Schedule Management → CU-D4 → RF-011 → `PARCIAL`.

## 6. Wireframe 05 — Availability View

**Referencia del inventario:** `WF-11`.<br>
**Actor:** ADMIN autenticado.<br>
**Estado:** CU-D5 `IMPLEMENTADO` para consulta operativa ADMIN; RF-012 completo sigue `PARCIAL` por CU-D6. UI no construida.<br>
**Objetivo:** leer slots existentes y su usabilidad derivada, sin mutarlos.

**Estructura:**

```text
┌──────────────────────────────────────────────────────────────┐
│ [HEADER] Disponibilidad operativa · solo lectura             │
├──────────────────────────────────────────────────────────────┤
│ [CONTENIDO] Filtros opcionales del GET                        │
│ scheduleId [________]  professionalId [________]             │
│ slotDate   [________]  status         [________]             │
│                                                              │
│ id  scheduleId  slotDate  startTime  endTime  status  usable │
│ [valores devueltos por AvailabilitySlotResponseDTO]          │
│ [Abrir detalle por id]                                        │
│                                                              │
│ [ACCIONES] [Consultar] [Ver detalle]                          │
└──────────────────────────────────────────────────────────────┘
```

**Datos visibles:** `id`, `scheduleId`, `slotDate`, `startTime`, `endTime`, `status`, `usable`. La respuesta puede contener `AVAILABLE`, `RESERVED` y `BLOCKED`; no se filtran automáticamente los no usables. `usable` equivale a `AVAILABLE` con Schedule activo y no garantiza que una reserva posterior vaya a prosperar.<br>
**Acciones:** `GET /api/v1/availability` con filtros opcionales `scheduleId`, `professionalId`, `slotDate`, `status`; `GET /api/v1/availability/{id}` para detalle. No se representa crear, bloquear, liberar, reservar o generar slots desde esta vista. No existe filtro `specialtyId` en este GET.<br>
**Validaciones:** formato de los parámetros según contrato actual; acceso exclusivo ADMIN. Lista sin coincidencias vacía, detalle inexistente `404 AVAILABILITY_SLOT_NOT_FOUND`, acceso anónimo/no autorizado `401`/`403` según respuesta.<br>
**Estados UX:** cargando, lista vacía, detalle encontrado/no encontrado y error de acceso. Son estados de lectura; `status` y `usable` son datos de dominio recibidos, no controles de edición.<br>
**Dependencias:** CU-D5/RF-012 porción ADMIN. CU-D6, consulta sanitizada PATIENT/RECEPTIONIST, continúa `CONCEPTUAL` sin URI, DTO ni permiso; esta vista operativa no se reutiliza para esos actores.<br>
**Trazabilidad:** Availability View → CU-D5 → RF-012 (porción ADMIN) → `IMPLEMENTADO` para ADMIN; RF-012 global `PARCIAL`.

## 7. Exclusiones y fuentes

No se diseñan reportes, estadísticas, indicadores, gestión hospitalaria, administración completa de usuarios, historia clínica, atención médica, configuración avanzada, catálogo funcional Specialty, asociación Professional–Specialty, generación de slots, calendario, duración, ventanas temporales ni reglas automáticas de agenda. Ninguna tabla SQL se interpreta como módulo ADMIN operativo. Los estados y controles de esta propuesta no agregan roles, permisos, endpoints ni DTO.

Fuentes: [inventario F.3.1](WIREFRAME-INVENTORY.md), [post-audit F.3.1](WIREFRAME-INVENTORY-POST-AUDIT.md), [flujos privados F.2.2](../user-flows/PRIVATE-PORTALS-USER-FLOWS.md), [post-audit F.2.2](../user-flows/PRIVATE-PORTALS-USER-FLOW-POST-AUDIT.md), [consolidación F.2.3](../user-flows/USER-FLOW-CONSOLIDATION.md), [D.2](../../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [D.3.1](../../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md), [D.3.2](../../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md), [D.3.3](../../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md), [D.4 catálogo](../../agenda/consolidation/AGENDA-USE-CASE-CATALOG.md), [D.4 actores](../../agenda/consolidation/AGENDA-ACTOR-USECASE-MATRIX.md) y [D.4 trazabilidad](../../agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md).

## 8. Validación de alcance

Se creó únicamente este Markdown de baja fidelidad. `git status --short --branch` confirmó `feat/hospitalplatform-wireframes` y mostró solo `?? docs/ux/wireframes/`, carpeta que contenía entregables previos sin seguimiento. `git diff --check` no reportó errores en archivos rastreados; la inspección directa del archivo nuevo encontró cinco láminas, doce enlaces locales válidos y ningún espacio final. Archivos existentes modificados en F.3.2.2: **0**. Java: **0**; SQL: **0**; tests: **0**; migraciones: **0**; seguridad: **0**. No se ejecutó Maven ni se hizo commit, push o merge.

**🟡 ADMIN WIREFRAMES COMPLETE WITH OBSERVATIONS.** Las vistas documentan operaciones actuales con sus límites. El inicio ADMIN sigue siendo navegación propuesta; la edición de horarios depende de un `specialtyId` existente sin catálogo funcional, y CU-D1/CU-D4 permanecen `PARCIAL`.
