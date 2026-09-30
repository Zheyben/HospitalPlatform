# ETAPA F.2.1 — Flujos UX del portal público

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico del Hospital de Huaycán.

**Fecha:** 2026-09-30.

**Criterio:** `IMPLEMENTADO` describe soporte de backend, no una pantalla existente; `PARCIAL` describe un resultado incompleto; `CONCEPTUAL` expresa un objetivo de diseño aprobado sin operación actual; `FUTURO` queda fuera del recorrido vigente. El portal público como interfaz corresponde a RF-010 `DEFINED_NOT_IMPLEMENTED`. Este documento diseña flujos para prototipo y no afirma que haya frontend desplegado o aprobación institucional.

## 1. Objetivo del portal público

Mostrar cómo una persona llegaría desde el acceso hasta una solicitud de cita, sin ocultar las discontinuidades actuales. Un paciente **ya registrado y vinculado** puede autenticarse y reservar mediante la API si conoce un `slotId` usable. La experiencia de descubrir y seleccionar disponibilidad para PATIENT es CU-D6 `CONCEPTUAL`, aunque DEC-007 esté `CLOSED` para actores y minimización de datos. El autorregistro de paciente es RF-001 `OPEN`, bloqueado por DEC-017 `OPEN`; no existe flujo de creación de cuenta pública. La reserva CU-D7 sí existe y no exige técnicamente una consulta CU-D6 previa.

Este documento conserva dos nociones distintas: **enviar solicitud de reserva** y **confirmar asistencia**. La creación responde `201` con cita `SCHEDULED`; el estado `CONFIRMED` requiere otra operación de cita. RF-015 es `PARCIAL`: existe `AppointmentResponseDTO` y `Location`, pero no una constancia UX completa ni datos resueltos de horario en el DTO.

## 2. Actores y límites del sistema

| Participante | Papel en estos flujos | Límite |
|---|---|---|
| **PATIENT** | Persona principal con cuenta activa y perfil de paciente activo vinculado para reservar por sí misma. Antes de autenticarse es visitante del recorrido, no un rol nuevo. | No administra datos ni indica otro `patientId` en su reserva. |
| **Autenticación de HospitalPlatform** | Componente que valida credenciales y emite sesión en el login existente. | No es una persona, rol adicional ni un servicio externo supuesto. |
| **HospitalPlatform** | Límite del sistema que procesa la reserva, consulta de citas propias y respuestas. | No ofrece hoy portal público, autorregistro ni consulta sanitizada de disponibilidad para PATIENT. |

Los actores secundarios son componentes del sistema para describir respuestas, no permisos nuevos ni roles operativos. La ruta pública vigente relevante es `POST /api/v1/auth/login`; el resto de operaciones de cita exige autenticación.

## 3. User journey de usuario existente

El recorrido solicitado se conserva completo como **objetivo de experiencia**, con el punto de corte indicado. En la tabla, «pantalla» significa vista candidata de prototipo, nunca UI ya implementada.

| Paso / pantalla candidata | Objetivo | Acción PATIENT y datos ingresados | Respuesta de HospitalPlatform | Estado funcional |
|---|---|---|---|---|
| 1. Entrada al portal | Encontrar acceso a citas. | Abrir punto de entrada; sin datos. | No hay portal/frontend funcional ni contenido público contratado. | `FUTURO` — RF-010 no implementado |
| 2. Acceso / login | Iniciar sesión con cuenta existente. | Introducir `email` y `password`; enviar. | `POST /api/v1/auth/login` valida credenciales y cuenta habilitada; devuelve tokens o rechazo. | `IMPLEMENTADO` en backend — RF-002 |
| 3. Acceso al área de citas | Continuar con identidad autenticada. | Usar sesión válida; no ingresar `patientId`. | La reserva posterior exige paciente activo vinculado a la cuenta; el perfil propio puede leerse por `/api/v1/patients/me`. | `IMPLEMENTADO` en backend; vista de portal no existe |
| 4. Solicitar cita | Iniciar elección de una opción reservable. | Expresar intención de reservar; criterios concretos aún no definidos. | No hay búsqueda de oferta sanitizada para PATIENT ni filtros/DTO aprobados. | `CONCEPTUAL` — CU-D6, RF-012 |
| 5. Seleccionar información disponible | Elegir una opción sin exponer datos internos. | Selección conceptual; no se fija calendario, filtro ni campo de presentación. | No existe interacción actual que entregue `slotId` a PATIENT desde una vista autorizada. | `CONCEPTUAL` — depende de CU-D6 |
| 6. Confirmar envío de reserva | Solicitar la asignación del slot elegido. | Enviar `slotId` conocido; `reason` opcional; omitir `patientId`. «Confirmar» aquí significa **enviar solicitud**, no cambiar estado a `CONFIRMED`. | `POST /api/v1/appointments` revalida y reserva atómicamente; puede rechazar si el slot dejó de ser usable. | `IMPLEMENTADO` en backend — CU-D7/CU-D8; acceso UX al `slotId` pendiente |
| 7. Cita registrada / resultado | Comprender el resultado de la solicitud. | Leer respuesta; ninguna acción automática de confirmación de asistencia. | `201`, `Location` y `AppointmentResponseDTO` con `appointmentStatus=SCHEDULED` y `flowStage=null`. No hay constancia visual final implementada. | Reserva `IMPLEMENTADO`; constancia RF-015 `PARCIAL` |

**Lectura de extremo a extremo:** la cadena «entrada → login → solicitud → selección → reserva → cita registrada» **no es ejecutable hoy como portal de paciente**. El tramo CU-D6 es conceptual y el portal RF-010 no existe. Una prueba de CU-D7 con un `slotId` conocido demuestra el backend de reserva, no la experiencia de descubrimiento.

**Desvíos respaldados:** credenciales inválidas o cuenta no habilitada detienen login; ausencia de paciente activo vinculado impide reserva; `slotId` ausente o inválido produce rechazo de solicitud; conflicto de slot (`409 SLOT_UNAVAILABLE`) no crea cita parcial. El resultado debe reflejar la respuesta real, sin prometer otro slot ni una espera automática.

## 4. User journey de usuario nuevo

RF-001 describe el objetivo de registrar una cuenta de paciente, pero su estado es `OPEN`; DEC-017 mantiene sin decidir prueba de identidad, duplicados, datos permitidos, consentimiento y creación/vinculación atómica. Los endpoints actuales de creación de usuario y paciente son ADMIN y **no** forman un autorregistro. Este flujo es **condicional** y no se conecta a una acción real del portal.

| Paso / pantalla candidata | Objetivo | Acción del visitante y datos | Respuesta posible documentable | Estado funcional |
|---|---|---|---|---|
| 1. Entrada al portal | Llegar a la opción de cuenta. | Abrir entrada; sin datos. | Portal RF-010 aún sin interfaz funcional. | `FUTURO` |
| 2. Seleccionar registro | Declarar intención de crear cuenta de paciente. | Elegir registro; sin datos todavía. | No hay `POST /api/v1/auth/register` ni ruta pública equivalente. | `FUTURO` / bloqueado por RF-001 y DEC-017 `OPEN` |
| 3. Formulario de paciente | Entender qué información sería necesaria antes de continuar. | Solo estructura conceptual; no se determinan campos obligatorios, identificadores ni consentimientos. | No hay DTO ni validación de autorregistro aprobados. | `FUTURO` / pendiente de decisión |
| 4. Validación de datos | Prevenir envío incorrecto y duplicados. | Revisar los datos que una futura decisión autorice. | Prueba de identidad, duplicados y validación no están especificados para el flujo público. | `FUTURO` / pendiente de decisión |
| 5. Creación de cuenta | Obtener cuenta y vínculo de paciente. | Solicitar alta solo si existe un contrato futuro aprobado. | No hay creación pública atómica de usuario + paciente; no mostrar éxito automático. | `FUTURO` / no implementado |
| 6. Login posterior | Acceder con una cuenta efectivamente creada y habilitada. | Ingresar `email` y `password`. | Login backend actual existe, pero no prueba ni completa el registro previo. | `IMPLEMENTADO` solo como operación independiente |
| 7. Solicitud de cita | Continuar al recorrido del usuario existente. | Reservar solo con perfil activo vinculado y `slotId` usable conocido. | Consulta CU-D6 conceptual; reserva CU-D7 implementada. | Recorrido completo pendiente |

### Datos necesarios para UX y confirmación posterior en backend

| Necesidad para bosquejar el flujo | Evidencia actual | Pendiente antes de definir un formulario funcional |
|---|---|---|
| Identificar a la persona y evitar duplicados | DEC-017 reconoce prueba de identidad y resolución de duplicados como problema abierto. | Campos exactos, fuente de verificación, reglas de coincidencia y mensajes de rechazo. |
| Establecer credenciales de acceso | El login actual usa `email` y `password` para cuentas ya existentes. | Si esos campos y qué restricciones corresponden al alta pública; el login no define el contrato de registro. |
| Crear/vincular User y Patient | Hoy ADMIN crea y vincula por operaciones separadas. | Política de creación atómica o revisión asistida, estado intermedio, consentimiento, habilitación y ownership; DEC-017 `OPEN`. |
| Explicar el siguiente paso tras registro | El backend solo acredita login y reserva para cuenta/paciente activos. | Resultado permitido del autorregistro futuro y cuándo podrá iniciar sesión/reservar. |

Esta separación permite estudiar la secuencia sin convertir datos supuestos en campos de un DTO. Tampoco se adopta ninguna de las opciones de DEC-017 ni se inventa un estado «pendiente de aprobación».

## 5. Flujo de consulta y reserva de cita

| Etapa | Soporte y decisión de UX |
|---|---|
| Usuario autenticado | Login RF-002 `IMPLEMENTADO`; la reserva exige PATIENT con perfil activo vinculado. |
| Solicitar cita | Punto de entrada del prototipo; portal RF-010 no implementado. |
| Ingresar criterios disponibles | `CONCEPTUAL` CU-D6. DEC-007 `CLOSED` aprueba PATIENT autenticado y respuesta sanitizada, **no** campos, filtros, URI, DTO ni permiso instalado. No se prescribe fecha, especialidad, profesional ni calendario como controles actuales. |
| Seleccionar opción | `CONCEPTUAL` como interacción de descubrimiento. El GET operativo `/api/v1/availability` es ADMIN y no puede reutilizarse en el portal de paciente. |
| Confirmar envío de reserva | CU-D7 `IMPLEMENTADO` solo al contar con `slotId` conocido. Solicitud PATIENT: `slotId` obligatorio, `reason` opcional, sin `patientId`. CU-D8 protege contra doble asignación; un conflicto devuelve rechazo sin cita parcial. |
| Mostrar resultado | `201` con `SCHEDULED` si se crea; `409` si el slot no es usable. RF-015 continúa `PARCIAL` como constancia al usuario. No adelantar a `CONFIRMED`. |

La selección conceptual no es precondición técnica de `POST /api/v1/appointments`. El servicio vuelve a comprobar la posibilidad de reserva en la transacción; ver una opción no garantizaría su disponibilidad posterior. No se define generación de slots, duración ni reglas de ventanas.

## 6. Inventario de pantallas candidatas

**Estado funcional** significa soporte del flujo o API subyacente. Ninguna fila afirma UI construida.

| Pantalla | Actor | Objetivo | Datos y acciones | Estado funcional |
|---|---|---|---|---|
| Entrada del portal | Visitante que pretende ser PATIENT | Orientar hacia acceso y futura reserva. | Contenido y navegación definitivos no especificados. | `FUTURO` — RF-010 sin frontend |
| Acceso | PATIENT con cuenta existente | Autenticarse. | `email`, `password`; enviar; mostrar respuesta de éxito o rechazo. | Backend `IMPLEMENTADO`; vista no construida |
| Registro de paciente | Visitante que pretende ser PATIENT | Representar la intención de abrir cuenta. | Sin campos, validaciones ni envío definitivos; no asociar con un endpoint. | `FUTURO`, RF-001/DEC-017 `OPEN` |
| Solicitud y descubrimiento de cita | PATIENT autenticado | Explorar opciones reservables minimizadas. | Criterios, datos visibles y selección **por definir**; no presentar slots operativos ADMIN. | `CONCEPTUAL` — CU-D6 |
| Envío de reserva | PATIENT con perfil activo y `slotId` conocido | Registrar una cita propia. | `slotId` obligatorio, `reason` opcional; enviar una vez y esperar resultado. No ingresar `patientId`. | Backend `IMPLEMENTADO` — CU-D7/CU-D8; llegada desde selección no resuelta |
| Resultado de solicitud | PATIENT | Distinguir reserva creada de rechazo. | Mostrar `id`/estado `SCHEDULED` recibidos o error real; consulta posterior de cita propia disponible. Sin constancia UX completa. | Respuesta backend `IMPLEMENTADO`; RF-015 `PARCIAL` |
| Mis citas / detalle | PATIENT autenticado | Consultar una cita propia después del alta. | Lista/detalle de `AppointmentResponseDTO`; consulta propia. | Backend `IMPLEMENTADO` — RF-006 |

No se agrega pantalla de dashboard, catálogo de especialidades, gestión clínica o calendario generado.

## 7. Estados UX

Los estados de formulario y espera son **presentación del prototipo**, no nuevos estados de negocio ni de cuenta.

| Estado UX | Dónde aplica | Límite respaldado |
|---|---|---|
| Formulario vacío | Login; reserva cuando exista un `slotId` conocido. | Campos obligatorios actuales: `email`/`password` en login y `slotId` en reserva. Registro permanece sin campos definidos. |
| Validando / cargando | Durante login, lectura de citas o envío de reserva actual. | Solo espera de respuesta; no estado persistente ni promesa de éxito. |
| Error | Validación/autenticación o reserva rechazada. | Reflejar `400`, `401`, `403`, `404` o `409` según contrato/respuesta; no inventar textos literales ni solución automática. |
| Reserva registrada | Tras `201` de CU-D7. | Cita `SCHEDULED`, `flowStage=null`; RF-015 parcial. No equivale a `CONFIRMED`. |
| Pendiente de definición | Registro RF-001 y descubrimiento CU-D6. | Etiqueta **documental de diseño**, no estado de cuenta, cita, slot ni aprobación. No mostrarla como estado retornado por la API. |

No se introducen `NO_SHOW`, «registro aprobado», «slot retenido» ni notificaciones de confirmación.

## 8. Trazabilidad

`—` indica que identidad/portal quedan fuera de CU-D1–CU-D8 y no crea un caso de uso adicional. Los CU-D son identificadores locales del incremento Agenda.

| Pantalla / paso | CU | RF | Estado y contrato actual |
|---|---|---|---|
| Entrada del portal | — | RF-010 | `FUTURO` para UX; RF `DEFINED_NOT_IMPLEMENTED`, sin frontend. |
| Registro de paciente | — | RF-001 | `FUTURO` / `OPEN` por DEC-017; sin API pública ni campos aprobados. |
| Acceso de usuario existente | — | RF-002 | `IMPLEMENTADO` backend: `POST /api/v1/auth/login`. |
| Consulta de perfil propio para condición de reserva | — | RF-005 | Lectura PATIENT existente `GET /api/v1/patients/me`; RF-005 global `PARCIAL` por falta de autoservicio de alta/edición. |
| Consulta de disponibilidad y selección | CU-D6 | RF-012 | `CONCEPTUAL`; DEC-007 `CLOSED` en actores/exposición, sin endpoint PATIENT. RF-012 global `PARCIAL`. |
| Envío de reserva | CU-D7 | RF-013 | `IMPLEMENTADO` backend: `POST /api/v1/appointments` con `slotId` conocido. |
| Exclusión de doble reserva | CU-D8 interno | RF-014 | `IMPLEMENTADO` dentro de CU-D7, sin pantalla ni endpoint propio; prueba de 2 solicitudes, ensayo SRS de 20 pendiente. |
| Resultado de reserva | CU-D7 | RF-015 | Respuesta `201` + DTO + `Location` existente; RF-015 `PARCIAL` como constancia UX. |
| Mis citas | — | RF-006 | `IMPLEMENTADO` backend: `GET /api/v1/appointments` y `GET /api/v1/appointments/{id}` para PATIENT propio. |

## 9. Exclusiones y gate

- Notificaciones, waitlist, prioridad ambulatoria y módulos clínicos futuros.
- Generación automática de slots, calendario/duración/solapamiento y ventanas temporales: DEC-008 y DEC-010 siguen `OPEN`.
- Registro público ejecutable, verificación, consentimiento, reglas de duplicado y estados de aprobación: DEC-017 sigue `OPEN`.
- Consulta de disponibilidad PATIENT implementada, reutilización de la vista ADMIN, filtros definitivos o garantía de slot tras verlo: CU-D6 sigue `CONCEPTUAL`.
- Endpoints, DTO, permisos, contenido público definitivo y frontend no acreditados por las fuentes.

**Gate F.2.1: 🟡 PUBLIC PORTAL FLOW BASELINE WITH OBSERVATIONS.** Los recorridos están listos como diseño documental de prototipo con fronteras explícitas. Un portal ejecutable de registro → descubrimiento → reserva depende de decisiones y contratos posteriores; el login y la reserva actuales solo acreditan sus operaciones de backend.

## Fuentes y validación

Fuentes: [F.1 análisis UX](../UX-REQUIREMENTS-ANALYSIS.md), [F.1.1 post-audit](../UX-REQUIREMENTS-POST-AUDIT.md), [Word maestro](../../documentation-export/HOSPITALPLATFORM-COMPLETE-DOCUMENTATION.docx), [Domain Baseline](../../DOMAIN-BASELINE.md), [Decision Register](../../DOMAIN-DECISION-REGISTER.md), [C.1](../../agenda/AGENDA-AVAILABILITY-RF-UC-MATRIX.md), [C.2](../../agenda/AGENDA-AVAILABILITY-USE-CASES.md), [C.3](../../agenda/AGENDA-AVAILABILITY-CONTRACT-DESIGN.md), [C.4](../../agenda/AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md), [D.1](../../agenda/uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.md), [D.2](../../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [D.3.1](../../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md), [D.3.2](../../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md), [D.3.3](../../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md) y [D.4](../../agenda/consolidation/AGENDA-UML-CONSOLIDATION-REPORT.md). No se toma contenido histórico como capacidad actual.

Control final: rama `feat/hospitalplatform-user-flows`; `git status` muestra únicamente `?? docs/ux/user-flows/`. Archivo creado: `PUBLIC-PORTAL-USER-FLOWS.md`; archivos existentes modificados: **0**. `git diff --check` no reportó errores en archivos rastreados; la revisión directa del Markdown nuevo no detectó espacios finales ni enlaces locales rotos. **Java: 0; SQL: 0; tests: 0; migraciones: 0; seguridad: 0**. No se ejecutó Maven ni se hizo commit, push o merge.
