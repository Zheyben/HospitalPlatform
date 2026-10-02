# ETAPA F.3.1 — Inventario y especificación de pantallas para wireframes

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico del Hospital de Huaycán.

**Fecha:** 2026-09-30.

**Rama:** `feat/hospitalplatform-wireframes`.

## 1. Objetivo

Fijar el inventario de **vistas candidatas ya documentadas** en F.1–F.2 como base para wireframes. Los ID `WF-01`–`WF-15` son referencias de diseño de este inventario, **no rutas, permisos, pantallas implementadas ni casos de uso nuevos**. `IMPLEMENTADO` y `PARCIAL` describen el soporte funcional del backend; `CONCEPTUAL` describe una intención sin contrato operativo; `FUTURO` señala trabajo fuera del alcance actual. El frontend y estos wireframes aún no están implementados.

Un wireframe de una función existente debe poder distinguir el dato recibido del backend de los datos que no puede obtener. En particular, PATIENT y RECEPTIONIST no disponen hoy de un GET de disponibilidad autorizado; PROFESSIONAL no tiene GET de citas; el registro público de paciente depende de DEC-017 `OPEN`. El inventario no convierte ninguna de esas brechas en una interacción ejecutable.

## 2. Principios UX de alcance

1. **Actor y permiso visibles:** cada vista conserva el rol de su flujo; el login compartido no añade autorización para recursos de otro actor.
2. **Soporte ≠ UI:** una API `IMPLEMENTADO` permite diseñar una vista candidata, pero no prueba que esa pantalla exista. «Área operativa» y «Entrada a citas» son navegación documental sin dashboard funcional.
3. **Estado exacto:** una reserva `201` crea cita `SCHEDULED`; `CONFIRMED` exige confirmación de asistencia por operación separada. RF-015 sigue `PARCIAL` como constancia UX.
4. **Datos mínimos demostrados:** usar campos de DTO y validaciones actuales cuando existan; para registro y disponibilidad sanitizada no fijar campos, filtros, mensajes literales ni estados de aprobación.
5. **Dependencias explícitas:** CU-D7 acepta `slotId` conocido sin exigir CU-D6. Un Schedule puede crearse con `specialtyId` existente, pero no hay selector de catálogo funcional ni generación automática de slots.
6. **Estados de presentación:** cargando, vacío, error, éxito y no autorizado son estados transitorios de UI, no nuevos estados del dominio.

## 3. Inventario de pantallas

`—` en CU indica que la vista se basa en Auth/Appointments o en navegación UX fuera de los ocho CU-D; no se crea un caso nuevo. **Alcance de wireframe** separa lo que puede bosquejarse con contrato actual de lo que debe quedar señalado como referencia pendiente.

| ID | Pantalla candidata documentada | Actor | Objetivo | Caso de uso | RF | Estado funcional / alcance de wireframe |
|---|---|---|---|---|---|---|
| WF-01 | Entrada del portal (Landing / Acceso) | Visitante que pretende ser PATIENT | Orientar al acceso y señalar el registro pendiente. | — | RF-010 | `FUTURO`: portal sin frontend; solo estructura de entrada, sin contenido definitivo. |
| WF-02 | Acceso (Login) | PATIENT, ADMIN, RECEPTIONIST, PROFESSIONAL | Autenticar cuenta existente. | — | RF-002 | Backend `IMPLEMENTADO`; vista candidata sin UI construida. |
| WF-03 | Registro de paciente | Visitante que pretende ser PATIENT | Documentar intención de autorregistro. | — | RF-001 | `FUTURO` / DEC-017 `OPEN`; referencia sin formulario ejecutable ni campos finales. |
| WF-04 | Solicitud y descubrimiento de cita | PATIENT autenticado | Descubrir oferta sanitizada para elegir. | CU-D6 | RF-012 | `CONCEPTUAL`; no producir una búsqueda conectable a API actual. |
| WF-05 | Envío de reserva | PATIENT con perfil activo vinculado | Solicitar una cita propia con `slotId` conocido. | CU-D7; CU-D8 interno | RF-013/014 | Backend `IMPLEMENTADO`; acceso al slot desde WF-04 pendiente. |
| WF-06 | Resultado de solicitud (reserva registrada) | PATIENT | Distinguir `SCHEDULED` de rechazo y de `CONFIRMED`. | CU-D7 | RF-015 | Respuesta backend existente; constancia UX `PARCIAL`. |
| WF-07 | Mis citas / detalle | PATIENT autenticado | Consultar cita propia y acciones elegibles. | — | RF-006; RF-016/017/019 para acciones | GET propios `IMPLEMENTADO`; acciones de ciclo implementadas según estado. |
| WF-08 | Área operativa ADMIN (referencia de navegación, no dashboard de métricas) | ADMIN | Orientar a las tres vistas autorizadas del incremento. | — | — | `CONCEPTUAL` como organización UX; sin recurso/dashboard implementado. |
| WF-09 | Profesionales | ADMIN | Crear, consultar y editar perfil básico. | CU-D1 | RF-007 | `PARCIAL`; sin N:M ni desactivación HTTP. |
| WF-10 | Horarios | ADMIN | Crear, consultar, editar y cambiar `active` de Schedule. | CU-D4 | RF-011 | `PARCIAL`; no genera slots. |
| WF-11 | Disponibilidad operativa | ADMIN | Consultar slots existentes y `usable`. | CU-D5 | RF-012, porción ADMIN | `IMPLEMENTADO` para ADMIN; RF-012 global `PARCIAL`. |
| WF-12 | Entrada a citas (referencia de navegación) | RECEPTIONIST | Orientar a consulta y gestión de citas. | — | — | `CONCEPTUAL` como organización UX; sin dashboard/API propia. |
| WF-13 | Citas: lista/detalle | RECEPTIONIST | Consultar citas accesibles y estado actual. | — | RF-030, porción recepción | GET `IMPLEMENTADO`; RF-030 global `PARCIAL`. |
| WF-14 | Cita: alta/gestión | RECEPTIONIST | Reservar y operar cita elegible. | CU-D7; CU-D8 interno para alta; demás acciones fuera de CU-D | RF-013/014/016/017/019/026/027 | Backend `IMPLEMENTADO` en su límite; obtención UX de `patientId`/`slotId` pendiente. |
| WF-15 | Portal profesional (referencia de brecha, **sin wireframe funcional**) | PROFESSIONAL | Hacer visible la ausencia de navegación de citas asignadas. | — | RF-028/029/030 | UX `CONCEPTUAL / NO IMPLEMENTADO`; dos POST operativos existentes, RF-030 `PARCIAL`. |

WF-08 y WF-12 reutilizan las áreas de navegación **candidatas** de F.2.2. WF-15 conserva la referencia de brecha de ese documento y **no autoriza diseñar un dashboard profesional**. No se añaden pantallas navegables de Specialty Catalog o asociación N:M: CU-D2/CU-D3 permanecen conceptuales.

La «Solicitud de cita» solicitada se descompone en las dos vistas ya documentadas: WF-04 (descubrimiento conceptual) y WF-05 (envío con `slotId` conocido). La «Confirmación de cita» solicitada corresponde a WF-06 como **resultado de reserva registrada**; la confirmación de asistencia `CONFIRMED` es una acción posterior del detalle WF-07, sin pantalla independiente inventada. El «Dashboard administrativo» se representa únicamente por WF-08, área de navegación sin métricas.

## 4. Especificación por pantalla

En las fichas, «validaciones» nombra solo las reglas verificables por contrato actual. «Estados UX» se refiere a presentación o al resultado recibido, sin crear estados persistidos nuevos.

### WF-01 — Entrada del portal (Landing / Acceso) — `FUTURO`

- **Objetivo y actor:** orientar a quien pretende acceder como PATIENT; RF-010 no tiene frontend funcional.
- **Información visible:** punto de entrada a login; contenido público, identidad visual y texto definitivo sin especificar. La opción de registro, si se dibuja, lleva etiqueta de futura/pendiente.
- **Acciones:** ir a WF-02; referencia no operativa a WF-03.
- **Validaciones y estados UX:** no hay formulario ni respuesta backend propia; no mostrar «cuenta creada» o contenido institucional aprobado.
- **Dependencias:** RF-010 `DEFINED_NOT_IMPLEMENTED`; WF-03 depende de DEC-017 `OPEN`.

### WF-02 — Acceso (Login) — backend `IMPLEMENTADO`

- **Objetivo y actor:** autenticar cuentas existentes de los cuatro roles. Es la misma operación, no cuatro endpoints distintos.
- **Información visible:** entradas `email` y `password`; respuesta de sesión o rechazo, sin inventar campos de perfil.
- **Acciones:** enviar `POST /api/v1/auth/login`; al éxito, continuar al destino UX que corresponda al rol, sin pantalla/API de «selección de portal».
- **Validaciones y estados UX:** ambos campos obligatorios; formato email validado. Vacío, cargando, éxito o error `400`/`401` conforme a respuesta. Cuenta no habilitada se rechaza.
- **Dependencias:** RF-002; el acceso a cada recurso posterior conserva sus permisos.

### WF-03 — Registro de paciente — `FUTURO` / DEC-017 `OPEN`

- **Objetivo y actor:** representar la intención de alta de un visitante, sin prometer autorregistro funcional.
- **Información visible:** solo estructura conceptual; campos de identidad, credenciales, consentimiento y resultado final no aprobados.
- **Acciones:** ninguna creación ejecutable; no vincular a `/auth/register` como endpoint existente. La vuelta a WF-02 no supone cuenta creada.
- **Validaciones y estados UX:** no se definen obligatorios, duplicados ni «pendiente de aprobación» como estado del sistema.
- **Dependencias:** RF-001 `OPEN`, DEC-017 `OPEN`; la creación/vinculación actual de User/Patient es administrativa.

### WF-04 — Solicitud y descubrimiento de cita — `CONCEPTUAL`

- **Objetivo y actor:** PATIENT autenticado pretende consultar oferta sanitizada y seleccionar una opción.
- **Información visible:** estructura de intención sin campos ni filtros finales; no reutilizar `AvailabilitySlotResponseDTO` operativo de ADMIN.
- **Acciones:** ninguna búsqueda ejecutable ni selección conectada al backend actual.
- **Validaciones y estados UX:** no definir «sin slots», calendario, especialidad, duración o ventanas como comportamiento actual; un vacío real solo podrá especificarse tras existir contrato de consulta.
- **Dependencias:** CU-D6/RF-012 y DEC-007 `CLOSED` solo para actores/exposición conceptual; sin URI/DTO/permiso actual. WF-05 no exige técnicamente WF-04.

### WF-05 — Envío de reserva — backend `IMPLEMENTADO`

- **Objetivo y actor:** PATIENT con perfil activo vinculado solicita cita propia en un slot usable conocido.
- **Información visible:** `slotId` requerido y `reason` opcional del request actual; no pedir `patientId` al PATIENT ni campos de profesional/especialidad como elección cliente.
- **Acciones:** enviar `POST /api/v1/appointments`; no suponer de dónde obtuvo la UI el `slotId`. CU-D8 actúa internamente sin botón o pantalla propia.
- **Validaciones y estados UX:** `slotId` obligatorio; carga, `400` de solicitud inválida, `403` de ownership indebido, `404` de paciente no disponible o `409 SLOT_UNAVAILABLE` según respuesta. No mostrar éxito si el slot no fue reservado.
- **Dependencias:** CU-D7/RF-013 implementados, CU-D8/RF-014 invariante; la selección PATIENT WF-04 es conceptual y DEC-010 no establece ventanas.

### WF-06 — Resultado de solicitud (reserva registrada) — RF-015 `PARCIAL`

- **Objetivo y actor:** mostrar al PATIENT si se creó la cita o fue rechazada.
- **Información visible:** de un `201` pueden mostrarse `id`, `appointmentStatus=SCHEDULED` y datos presentes en `AppointmentResponseDTO`; `Location` existe en respuesta. El DTO no aporta una constancia visual final ni horario resuelto del slot.
- **Acciones:** pasar a WF-07 si existe cita propia; no disparar confirmación de asistencia automáticamente.
- **Validaciones y estados UX:** éxito de reserva `SCHEDULED` o rechazo real; **«Reserva registrada» no equivale a cita `CONFIRMED`**.
- **Dependencias:** RF-015 `PARCIAL`. La confirmación de asistencia es una acción independiente desde la cita elegible en WF-07 (RF-016).

### WF-07 — Mis citas / detalle — backend `IMPLEMENTADO`

- **Objetivo y actor:** PATIENT autenticado consulta solo citas propias y, desde el detalle, actúa si el estado lo permite.
- **Información visible:** lista/detalle de `AppointmentResponseDTO`, incluidos `id`, `patientId`, `professionalId`, `slotId`, `appointmentStatus`, `flowStage`, `reason` y fechas devueltas; no inventar datos clínicos.
- **Acciones:** `GET /api/v1/appointments`, `GET /api/v1/appointments/{id}`; confirmar asistencia desde `SCHEDULED`, cancelar cita elegible o reprogramar con nuevo `slotId` conocido mediante operaciones actuales.
- **Validaciones y estados UX:** cargando, vacío, error/no autorizado, estado real `SCHEDULED`/`CONFIRMED`/`CANCELLED`/`RESCHEDULED`/`COMPLETED` según respuesta. No inventar ventanas temporales o búsqueda de nuevo slot.
- **Dependencias:** RF-006; RF-016/017/019 para acciones. Una pantalla independiente de «confirmación de asistencia» no está inventariada: la acción pertenece al detalle.

### WF-08 — Área operativa ADMIN — `CONCEPTUAL` como navegación

- **Objetivo y actor:** orientar a ADMIN hacia WF-09, WF-10 y WF-11.
- **Información visible:** referencias a esas vistas; sin métricas, informes ni datos hospitalarios agregados.
- **Acciones:** abrir las vistas candidatas autorizadas; no ofrece operaciones propias.
- **Validaciones y estados UX:** solo control de acceso general según sesión/rol; sin estado de dashboard o API propia.
- **Dependencias:** organización F.2.2 y F.2.3; no equivale a módulo funcional nuevo.

### WF-09 — Profesionales — CU-D1 `PARCIAL`

- **Objetivo y actor:** ADMIN crea, lista, consulta y actualiza perfil profesional básico.
- **Información visible:** `id`, `userId`, `licenseNumber`, `active` del DTO actual. `active` es visible; no hay acción HTTP de desactivación.
- **Acciones:** `POST/GET /api/v1/professionals`, `GET/PUT /api/v1/professionals/{id}`. En alta, `userId` opcional; PUT cambia solo licencia.
- **Validaciones y estados UX:** licencia obligatoria, máximo 100 caracteres; vacío/cargando, error `400`, `404` o `409 DUPLICATE_PROFESSIONAL` según operación, y resultado devuelto.
- **Dependencias:** RF-007/CU-D1; asociación N:M CU-D2 `CONCEPTUAL`; DEC-005 `OPEN`. Sin selector de especialidades funcional.

### WF-10 — Horarios — CU-D4 `PARCIAL`

- **Objetivo y actor:** ADMIN mantiene la configuración básica de Schedule.
- **Información visible:** `id`, `professionalId`, `specialtyId`, `dayOfWeek`, `startTime`, `endTime`, `active`; lista filtrable por profesional/especialidad.
- **Acciones:** `POST/GET /api/v1/agendas`, `GET/PUT /api/v1/agendas/{id}`, `PATCH /api/v1/agendas/{id}/status`.
- **Validaciones y estados UX:** identificadores, día 0–6 y horas obligatorios; `endTime > startTime`; profesional activo y specialty existente por FK. Vacío/cargando, `400` o `404` según respuesta; no afirmar validación de specialty activa/asociada.
- **Dependencias:** RF-011/CU-D4; no hay API de catálogo para elegir `specialtyId`, generación de slots ni ventanas DEC-008/010.

### WF-11 — Disponibilidad operativa — CU-D5 `IMPLEMENTADO` ADMIN

- **Objetivo y actor:** ADMIN consulta slots existentes; no reserva ni genera desde esta vista.
- **Información visible:** `id`, `scheduleId`, `slotDate`, `startTime`, `endTime`, `status` y `usable`; lista puede incluir `AVAILABLE`, `RESERVED` y `BLOCKED`.
- **Acciones:** `GET /api/v1/availability` con filtros opcionales `scheduleId`, `professionalId`, `slotDate`, `status`; `GET /api/v1/availability/{id}`.
- **Validaciones y estados UX:** cargando, lista vacía, detalle `404` o acceso `401`/`403`; `usable` es derivado (`AVAILABLE` y Schedule activo), no promesa de reserva futura.
- **Dependencias:** RF-012 porción ADMIN; RF global `PARCIAL` por CU-D6. No habilitar esta consulta a PATIENT/RECEPTIONIST.

### WF-12 — Entrada a citas — `CONCEPTUAL` como navegación

- **Objetivo y actor:** orientar a RECEPTIONIST hacia WF-13/WF-14.
- **Información visible:** referencias a lista/detalle y gestión de citas; sin dashboard, métricas o gestión de pacientes.
- **Acciones:** abrir vistas candidatas de Appointments; no tiene contrato/API propios.
- **Validaciones y estados UX:** control de sesión/rol; no se define estado de módulo persistente.
- **Dependencias:** organización F.2.2/F.2.3; no implica disponibilidad autorizada para el rol.

### WF-13 — Citas: lista/detalle — GET existente

- **Objetivo y actor:** RECEPTIONIST consulta citas accesibles y su etapa operativa.
- **Información visible:** `AppointmentResponseDTO` con identificadores, estado, `flowStage`, `reason` y fechas que devuelve la API.
- **Acciones:** `GET /api/v1/appointments`, `GET /api/v1/appointments/{id}`; pasar a acciones elegibles en WF-14.
- **Validaciones y estados UX:** cargando, vacío, `404` de detalle, `401`/`403`; no mostrar detalle clínico ni disponibilidad ADMIN.
- **Dependencias:** RF-030 porción RECEPTIONIST tiene GET `IMPLEMENTADO`; RF-030 global sigue `PARCIAL` por ausencia de consulta PROFESSIONAL.

### WF-14 — Cita: alta/gestión — backend actual con brecha de selección

- **Objetivo y actor:** RECEPTIONIST crea y gestiona una cita de paciente activo según estado permitido.
- **Información visible:** para alta `patientId` explícito, `slotId` obligatorio, `reason` opcional; para gestión, `appointmentStatus` y `flowStage` recibidos.
- **Acciones:** crear, confirmar, cancelar o reprogramar con nuevo `slotId`; registrar check-in desde `CONFIRMED/null` y espera desde `CONFIRMED/CHECK_IN`. Son operaciones actuales de Appointments.
- **Validaciones y estados UX:** campos de alta, rol y transiciones verificadas por backend; éxito `SCHEDULED` de alta, `CONFIRMED` tras confirmación, etapas `CHECK_IN`/`WAITING`; `400`/`403`/`404`/`409` según respuesta. No inventar un buscador de pacientes o slots.
- **Dependencias:** CU-D7/RF-013 y CU-D8/RF-014 interno para alta; RF-016/017/019/026/027 para ciclo. El rol carece de GET de pacientes y disponibilidad sanitizada; la obtención UX de ambos identificadores sigue pendiente.

### WF-15 — Portal profesional — `CONCEPTUAL / NO IMPLEMENTADO` como UX

- **Objetivo y actor:** dejar constancia de la brecha para PROFESSIONAL; **no producir wireframe funcional** en esta etapa.
- **Información visible y acciones:** no se definen lista, dashboard, agenda personal ni datos de pacientes; los dos POST operativos de inicio/fin de una cita asignada existen, pero no proporcionan navegación para hallar la cita.
- **Validaciones y estados UX:** no se especifica un flujo de pantalla; los estados operativos reales de backend no autorizan una UI clínica.
- **Dependencias:** RF-028/029 `IMPLEMENTADO` como mutaciones; RF-030 `PARCIAL` sin GET para PROFESSIONAL; DEC-005 `OPEN`.

## 5. Mapa de navegación entre vistas inventariadas

Las flechas indican **proximidad de diseño**, no pantallas construidas, precondiciones técnicas o rutas nuevas. Cuando la siguiente vista depende de una capacidad ausente, se señala el corte.

```text
PATIENT:
WF-01 Entrada del portal [FUTURO] → WF-02 Acceso [login backend]
WF-01 → WF-03 Registro [FUTURO/DEC-017 OPEN] ── sin alta ejecutable
WF-02 → WF-04 Descubrimiento [CONCEPTUAL/CU-D6] ── sin selección API actual
WF-05 Envío de reserva [solo con slotId conocido] → WF-06 Resultado → WF-07 Mis citas/detalle
WF-07 detalle: confirmar asistencia elegible [RF-016], distinto del resultado WF-06

ADMIN:
WF-02 Acceso → WF-08 Área operativa [navegación candidata]
WF-08 → WF-09 Profesionales | WF-10 Horarios | WF-11 Disponibilidad operativa
Estas tres vistas son destinos independientes; un horario no genera slots.

RECEPTIONIST:
WF-02 Acceso → WF-12 Entrada a citas [navegación candidata]
WF-12 → WF-13 Citas: lista/detalle ↔ WF-14 Cita: alta/gestión
WF-14 requiere patientId/slotId conocidos; CU-D6 sigue conceptual.

PROFESSIONAL:
WF-02 Acceso ↛ WF-15 Portal profesional [sin recorrido UX funcional]
```

No se agrega una pantalla de selección de portal. El destino UX por rol descrito en F.2.3 no tiene contrato ni interfaz acreditada.

## 6. Exclusiones, fuentes y gate

Se excluyen historia clínica, interfaz de atención médica, diagnósticos, prescripciones, notificaciones, generación automática de slots, waitlist, prioridad, reportes y dashboards con métricas, catálogo Specialty operativo, asociación N:M funcional, ventanas temporales y módulos futuros. DEC-005/008/010/017 siguen `OPEN`; DEC-006/007 `CLOSED` no instalan API o permisos. CU-D8 es una invariante, no un wireframe independiente.

Fuentes: [F.1 requisitos](../UX-REQUIREMENTS-ANALYSIS.md), [F.1.1 auditoría](../UX-REQUIREMENTS-POST-AUDIT.md), [F.2.1 público](../user-flows/PUBLIC-PORTAL-USER-FLOWS.md), [F.2.2 privados](../user-flows/PRIVATE-PORTALS-USER-FLOWS.md), [F.2.3 consolidación](../user-flows/USER-FLOW-CONSOLIDATION.md), [F.2.3 post-audit](../user-flows/USER-FLOW-CONSOLIDATION-POST-AUDIT.md), [Word maestro](../../documentation-export/HOSPITALPLATFORM-COMPLETE-DOCUMENTATION.docx), [D.2 fichas](../../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [D.4 catálogo](../../agenda/consolidation/AGENDA-USE-CASE-CATALOG.md), [D.4 actores](../../agenda/consolidation/AGENDA-ACTOR-USECASE-MATRIX.md) y [D.4 trazabilidad](../../agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md). Los estados de dominio del [baseline](../../DOMAIN-BASELINE.md) y [Decision Register](../../DOMAIN-DECISION-REGISTER.md) se conservan mediante esas fuentes.

**Gate F.3.1: 🟡 WIREFRAME INVENTORY COMPLETE WITH OBSERVATIONS.** El inventario sirve para bosquejar funciones backend existentes y marcar referencias conceptuales/futuras sin fingir un frontend actual. Un recorrido ejecutable de registro → búsqueda → reserva o un portal profesional requieren decisiones y contratos posteriores.

## Validación de alcance

Archivo creado: `docs/ux/wireframes/WIREFRAME-INVENTORY.md`. Archivos existentes modificados por F.3.1: **0**. `git status` confirmó la rama `feat/hospitalplatform-wireframes` y mostró únicamente `?? docs/ux/wireframes/`. `git diff --check` no reportó errores en archivos rastreados; la revisión directa del Markdown nuevo no encontró espacios finales ni enlaces locales rotos. **Java: 0; SQL: 0; tests: 0; migraciones: 0; seguridad: 0**. No se ejecutó Maven ni se hizo commit, push o merge.
