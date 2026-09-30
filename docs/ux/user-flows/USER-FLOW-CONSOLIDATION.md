# ETAPA F.2.3 — Consolidación de flujos UX

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico del Hospital de Huaycán.

**Fecha:** 2026-09-30.

**Rama:** `feat/hospitalplatform-user-flows`.

## 1. Objetivo

Reunir los recorridos F.2.1 y F.2.2 en un mapa de navegación **documental** para PATIENT, ADMIN, RECEPTIONIST y PROFESSIONAL. Se reutilizan únicamente las vistas candidatas ya inventariadas. `IMPLEMENTADO` se refiere al backend comprobado, **no a una pantalla construida**; `PARCIAL` delimita el subconjunto operativo; `CONCEPTUAL` no equivale a API o permiso; `FUTURO` queda fuera del funcionamiento actual. El portal público RF-010 carece de frontend funcional, y las áreas privadas son propuestas de organización UX.

El mapa conserva la discontinuidad principal: una reserva CU-D7 puede crearse con `slotId` conocido, pero PATIENT y RECEPTIONIST aún no tienen una consulta autorizada de disponibilidad para descubrirlo. La creación de cita devuelve `SCHEDULED`; `CONFIRMED` requiere una operación posterior. Ninguna decisión `CLOSED` de dominio promueve por sí misma un caso a implementación.

## 2. Arquitectura UX general

La **entrada al sistema** y el **destino por rol** son nodos de navegación propuestos. No existe una pantalla de «selección de portal», un endpoint de selección ni un dashboard común. El mismo login backend `POST /api/v1/auth/login` autentica cuentas existentes; las rutas funcionales posteriores aplican sus permisos actuales. El registro de PATIENT es una alternativa futura **anterior** al login, no una capacidad que este habilite.

```text
ENTRADA AL SISTEMA [navegación UX; frontend no implementado]
├─ PATIENT sin cuenta → Registro de paciente [FUTURO; RF-001/DEC-017 OPEN]
│                    → login posterior solo si una cuenta válida llegara a existir
└─ Cuenta existente → Acceso / login [IMPLEMENTADO en backend: RF-002]
                     → destino UX según rol [organización propuesta; sin pantalla/API propia]
                        ├─ PATIENT → Solicitud y descubrimiento de cita [CONCEPTUAL: CU-D6]
                        │          → Envío de reserva con slotId conocido [IMPLEMENTADO backend: CU-D7]
                        │          → Resultado SCHEDULED [respuesta API; RF-015 PARCIAL]
                        │          → Mis citas / detalle [IMPLEMENTADO backend: RF-006]
                        ├─ ADMIN → Profesionales [PARCIAL: CU-D1]
                        │         → Horarios [PARCIAL: CU-D4]
                        │         → Disponibilidad operativa [IMPLEMENTADO ADMIN: CU-D5]
                        ├─ RECEPTIONIST → Citas: lista/detalle [GET implementado; RF-030 global PARCIAL]
                        │                → Cita: alta/gestión [operaciones backend implementadas]
                        │                ↳ Disponibilidad sanitizada [CONCEPTUAL: CU-D6; sin API]
                        └─ PROFESSIONAL → Portal profesional [UX CONCEPTUAL / NO IMPLEMENTADO]
                                           ↳ Inicio/fin operativos de cita asignada
                                             [POST existentes; sin GET de navegación]
```

Las flechas de ADMIN expresan un orden de lectura del prototipo, no una dependencia del backend: consultar slots existentes no exige crear un horario durante la sesión. La rama conceptual de disponibilidad **no** es precondición técnica de CU-D7. La rama de PROFESSIONAL no es un flujo navegable; sus POST actuales se registran para evitar negar una capacidad real mientras se reconoce la falta de consulta de citas asignadas.

## 3. Mapa de actores

| Actor | Entrada y destino documentado | Límite actual |
|---|---|---|
| **PATIENT** | Entrada del portal, Acceso, Solicitud y descubrimiento de cita, Envío de reserva, Resultado de solicitud, Mis citas / detalle. Registro de paciente como alternativa futura. | Login, reserva con perfil propio activo y `slotId` conocido, y consulta de citas propias existen en backend; autorregistro RF-001 está `OPEN` y CU-D6 sigue `CONCEPTUAL`. |
| **ADMIN** | Acceso, Área operativa ADMIN, Profesionales, Horarios, Disponibilidad operativa. | Profesionales CU-D1 y horarios CU-D4 `PARCIAL`; lectura de slots CU-D5 `IMPLEMENTADO` solo ADMIN. El área es navegación propuesta, no dashboard. |
| **RECEPTIONIST** | Acceso, Entrada a citas, Citas: lista/detalle, Cita: alta/gestión. | Gestión de citas según estado `IMPLEMENTADO` en backend; sin listado de pacientes para el rol ni consulta autorizada de slots. CU-D6 `CONCEPTUAL`. |
| **PROFESSIONAL** | Acceso y referencia documental «Portal profesional». | Rol y POST de inicio/fin operativos reales; no GET de citas para el rol ni recorrido UX completo. No participa en CU-D1–D8. |

`SYSTEM` en CU-D8 es ejecución técnica interna, no persona ni portal. La «selección de portal» solicitada se interpreta como **destino de navegación condicionado por rol**, nunca como un selector disponible al usuario o una nueva asignación de permisos.

## 4. Portal público — PATIENT

**Entrada / registro / login → solicitud → reserva.** La [descripción F.2.1](PUBLIC-PORTAL-USER-FLOWS.md) y su [auditoría](PUBLIC-PORTAL-USER-FLOW-POST-AUDIT.md) separan dos recorridos:

- **Cuenta existente:** Acceso con `email` y `password` (`IMPLEMENTADO` en backend). Con paciente activo vinculado y `slotId` usable conocido, `POST /api/v1/appointments` reserva de manera transaccional y devuelve `201`, cita `SCHEDULED` y `Location` (`IMPLEMENTADO` CU-D7, invariante CU-D8). RF-015 permanece `PARCIAL` porque esa respuesta no constituye una constancia UX completa.
- **Cuenta nueva:** Entrada del portal y Registro de paciente son `FUTURO` como experiencia. RF-010 no tiene frontend y RF-001/DEC-017 siguen `OPEN`; no hay API de autorregistro ni campos aprobados. El login posterior solo funciona si ya existe una cuenta habilitada y un perfil válido, sin que esto cierre la decisión de onboarding.
- **Descubrimiento y selección:** Solicitud y descubrimiento de cita son `CONCEPTUAL` CU-D6 para PATIENT autenticado por DEC-007. No hay URI, DTO, filtros ni permiso instalados para esa vista. Los GET operativos `/api/v1/availability` son ADMIN y no son reutilizables en este portal. Por ello el recorrido registro → búsqueda → reserva no es ejecutable de extremo a extremo.

`SCHEDULED` tras la reserva no se presenta como `CONFIRMED`. Las opciones de registro, criterios de búsqueda y selección visual quedan sin formulario o calendario definitivo.

## 5. Portales privados

### ADMIN

**Login → Profesionales → Horarios → Disponibilidad.** Se reutilizan «Área operativa ADMIN», «Profesionales», «Horarios» y «Disponibilidad operativa» del [flujo F.2.2](PRIVATE-PORTALS-USER-FLOWS.md), con los límites confirmados por su [post-audit](PRIVATE-PORTALS-USER-FLOW-POST-AUDIT.md). Profesionales permite alta, consulta y actualización básica de licencia (`PARCIAL` CU-D1); la asociación N:M y el ciclo User–Professional DEC-005 permanecen pendientes. Horarios permite alta, consulta, actualización y cambio `active` (`PARCIAL` CU-D4), sin generar slots ni validar specialty activa/asociada. Disponibilidad muestra slots existentes, sus estados y `usable` por los dos GET ADMIN (`IMPLEMENTADO` CU-D5). RF-012 global sigue parcial por CU-D6.

El identificador de especialidad requerido al configurar un horario no implica un catálogo funcional ni un selector soportado. «Área operativa ADMIN» solo organiza las vistas: no contiene estadísticas, reportes ni configuración hospitalaria.

### RECEPTIONIST

**Login → Entrada a citas → Citas: lista/detalle → Cita: alta/gestión.** Los GET de citas, la reserva para paciente activo con `patientId` y `slotId` conocidos, confirmación/cancelación/reprogramación y check-in/espera tienen backend actual. Check-in requiere `CONFIRMED/null`; espera requiere `CONFIRMED/CHECK_IN`. RF-030 es `PARCIAL` en conjunto porque falta la consulta operativa de PROFESSIONAL. RECEPTIONIST no tiene GET de pacientes ni acceso a disponibilidad operativa ADMIN; su futura consulta sanitizada CU-D6 sigue `CONCEPTUAL`. No se añade gestión médica o de horarios.

### PROFESSIONAL

El actor existe y puede invocar `POST /api/v1/appointments/{id}/start-attention` y `/complete` para la cita asignada y en estados exactos (`IMPLEMENTADO` RF-028/029 como mutación de backend). Los GET de Appointments no autorizan PROFESSIONAL. Por ello «Portal profesional» permanece **CONCEPTUAL / NO IMPLEMENTADO como UX**: no hay lista de trabajo, agenda personal, dashboard ni pantalla de atención médica. DEC-005 sigue `OPEN`. La falta de navegación no cambia el estado implementado de las dos mutaciones.

## 6. Matriz Actor → Flujo

La columna «Pantallas» nombra **solo las vistas candidatas ya inventariadas en F.2.1/F.2.2**; no acredita que existan en la aplicación.

| Actor | Flujo | Pantallas ya documentadas | Estado consolidado |
|---|---|---|---|
| PATIENT | Acceso, solicitud, reserva propia y consulta posterior; registro como alternativa. | Entrada del portal; Registro de paciente; Acceso; Solicitud y descubrimiento de cita; Envío de reserva; Resultado de solicitud; Mis citas / detalle. | Login/reserva/consulta backend `IMPLEMENTADO`; constancia `PARCIAL`; disponibilidad `CONCEPTUAL`; portal/registro `FUTURO` con RF-001/DEC-017 `OPEN`. |
| ADMIN | Login, gestión básica de profesional, Schedule y lectura de slots. | Acceso; Área operativa ADMIN; Profesionales; Horarios; Disponibilidad operativa. | CU-D1/CU-D4 `PARCIAL`; CU-D5 `IMPLEMENTADO` para ADMIN; navegación UI no implementada. |
| RECEPTIONIST | Login, consulta y gestión de citas; check-in y espera. | Acceso; Entrada a citas; Citas: lista/detalle; Cita: alta/gestión. | Operaciones de citas `IMPLEMENTADO` en backend; RF-030 global `PARCIAL`; CU-D6 `CONCEPTUAL`; UI no implementada. |
| PROFESSIONAL | Solo alcance del rol y mutaciones operativas conocidas. | Acceso; Portal profesional (referencia de brecha, sin pantalla funcional). | POST de inicio/fin `IMPLEMENTADO`; flujo de portal `CONCEPTUAL / NO IMPLEMENTADO`; RF-030 `PARCIAL`. |

## 7. Trazabilidad pantalla → caso de uso → RF → estado

`—` en CU indica que la capacidad procede de Auth/Appointments o es navegación propuesta fuera de CU-D1–D8; no crea un identificador nuevo. Los CU-D son etiquetas locales del incremento Agenda.

| Pantalla / acción ya documentada | Caso de uso | RF | Estado y límite |
|---|---|---|---|
| Entrada del portal | — | RF-010 | `FUTURO` UX; RF `DEFINED_NOT_IMPLEMENTED`, sin frontend. |
| Registro de paciente | — | RF-001 | `FUTURO` / `OPEN` por DEC-017; sin API pública. |
| Acceso para cuenta existente | — | RF-002 | Login backend `IMPLEMENTADO`; pantalla no construida. |
| Solicitud y descubrimiento de cita PATIENT | CU-D6 | RF-012 | `CONCEPTUAL`; sin endpoint/permiso PATIENT. |
| Envío de reserva PATIENT | CU-D7; CU-D8 interno | RF-013/014 | Reserva `IMPLEMENTADO` con `slotId` conocido; consulta previa no obligatoria. |
| Resultado de solicitud PATIENT | CU-D7 | RF-015 | Respuesta `201` existente; constancia UX `PARCIAL`. |
| Mis citas / detalle PATIENT | — | RF-006 | GET propios backend `IMPLEMENTADO`. |
| Área operativa ADMIN | — | — | Navegación UX propuesta; sin dashboard/API propia. |
| Profesionales ADMIN | CU-D1 | RF-007 | `PARCIAL`: alta, consulta, actualización básica. |
| Horarios ADMIN | CU-D4 | RF-011 | `PARCIAL`: gestión Schedule sin generación de slots. |
| Disponibilidad operativa ADMIN | CU-D5 | RF-012 | `IMPLEMENTADO` para ADMIN; RF-012 global `PARCIAL`. |
| Entrada a citas RECEPTIONIST | — | — | Navegación UX propuesta; sin recurso propio. |
| Citas: lista/detalle RECEPTIONIST | — | RF-030 | GET actual `IMPLEMENTADO` para el rol; RF global `PARCIAL`. |
| Cita: alta/gestión RECEPTIONIST | CU-D7; CU-D8 interno para alta | RF-013/014; RF-016/017/019/026/027 para transiciones | Operaciones backend `IMPLEMENTADO` según rol/estado; `slotId`/`patientId` conocidos, búsqueda UX pendiente. |
| Disponibilidad sanitizada RECEPTIONIST (sin vista funcional) | CU-D6 | RF-012 | `CONCEPTUAL`; sin API ni permiso. |
| Portal profesional (referencia de brecha) | — | RF-028/029/030 | Mutaciones backend `IMPLEMENTADO`; consulta RF-030 `PARCIAL`; portal UX `NO IMPLEMENTADO`. |

CU-D2/RF-007 (asociación Professional–Specialty) y CU-D3/RF-008 (catálogo) siguen `CONCEPTUAL` y **no se añaden como pantallas navegables**. CU-D8 no tiene pantalla ni actor humano independiente. Ninguna fila concede acceso nuevo.

## 8. Exclusiones y fuentes

Quedan fuera historia clínica, atención médica, diagnósticos, prescripciones, dashboards no existentes, reportes avanzados, notificaciones, waitlist, prioridad ambulatoria, generación automática de slots, duración/calendario, ventanas temporales, registro público ejecutable y demás módulos futuros. No se infieren permisos nuevos ni campos definitivos de disponibilidad sanitizada. DEC-005/008/010/017 permanecen `OPEN`; DEC-006/007 `CLOSED` solo resuelven el alcance de dominio indicado, sin implementar CU-D2/D3/D6.

Fuentes consolidadas: [F.1 requisitos UX](../UX-REQUIREMENTS-ANALYSIS.md), [F.1.1 post-audit](../UX-REQUIREMENTS-POST-AUDIT.md), [F.2.1 portal público](PUBLIC-PORTAL-USER-FLOWS.md), [F.2.1 post-audit](PUBLIC-PORTAL-USER-FLOW-POST-AUDIT.md), [F.2.2 privados](PRIVATE-PORTALS-USER-FLOWS.md), [F.2.2 post-audit](PRIVATE-PORTALS-USER-FLOW-POST-AUDIT.md), [D.2 casos](../../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [D.3.1 implementados](../../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md), [D.3.2 parciales](../../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md), [D.3.3 conceptuales](../../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md), [D.4 catálogo](../../agenda/consolidation/AGENDA-USE-CASE-CATALOG.md), [D.4 matriz de actores](../../agenda/consolidation/AGENDA-ACTOR-USECASE-MATRIX.md) y [D.4 trazabilidad](../../agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md). Estas fuentes ya contrastan el [baseline](../../DOMAIN-BASELINE.md) y el [registro de decisiones](../../DOMAIN-DECISION-REGISTER.md); el presente documento no sustituye sus decisiones.

**Gate F.2.3: 🟡 USER FLOW CONSOLIDATION COMPLETE WITH OBSERVATIONS.** El mapa reúne las rutas y los estados documentados sin crear navegación funcional ficticia. El frontend, el autorregistro, la disponibilidad sanitizada y el recorrido profesional completo siguen siendo dependencias para etapas posteriores.

## Validación de alcance

Archivo creado: `docs/ux/user-flows/USER-FLOW-CONSOLIDATION.md`. Archivos existentes modificados en F.2.3: **0**. `git status` confirmó la rama `feat/hospitalplatform-user-flows` y mostró únicamente `?? docs/ux/user-flows/`, que contiene también los entregables previos sin seguimiento. `git diff --check` no reportó errores en archivos rastreados; la revisión directa del Markdown nuevo no encontró espacios finales ni enlaces locales rotos. **Java: 0; SQL: 0; tests: 0; migraciones: 0; seguridad: 0**. No se ejecutó Maven ni se hizo commit, push o merge.
