# Matriz base de objetivos, requisitos y validación

> **Actualización 23/09/2026 — SRS 0.1:** la sección «Matriz canónica SRS» al final de este archivo es la referencia activa. Las tablas anteriores se conservan como evidencia de la auditoría previa y usan los IDs históricos de Capítulo III (`RF-01`–`RF-24`, `RNF-01`–`RNF-12`), que **no** deben confundirse con los nuevos `RF-001`–`RF-033` y `RNF-001`–`RNF-013`. Fuente canónica propuesta: `docs/requisitos/SRS-HOSPITALPLATFORM.md`; equivalencias: `docs/requisitos/EQUIVALENCIAS_REQUISITOS.md`.

**Corte:** 23/09/2026. **Estado:** [PROPUESTO], pendiente de conciliación formal. Los objetivos provienen de `docs/00-PROJECT-ROADMAP.md` §5.2. La especificación más detallada hallada usa `RF-01`–`RF-24` y `RNF-01`–`RNF-12` en `docs/research/recursos/CAPITULO-III.md` §1.1.1–1.1.2; su autoridad es secundaria. `docs/01-PRD.md` mantiene **otra** serie `RF-001`–`RF-020` y `RNF-001`–`RNF-013`; no se renumera ni se presume correspondencia por dígitos. “CU” y prueba son derivados candidatos, no artefactos ya aprobados o ejecutados.

## Objetivos

| Objetivo | Evidencia | Procesos relacionados | RF relacionados (serie capítulo III) | RNF relacionados | Estado | Observaciones |
|---|---|---|---|---|---|---|
| OG: diseñar, desarrollar, implementar y evaluar plataforma web/móvil de citas y trazabilidad | `docs/00` §5.1 | TO-BE completo | 01–24, con huecos señalados abajo | 01–12 | [PROPUESTO] | Alcance aprobado documentalmente; resultado y adopción no acreditados |
| OE1: levantar/modelar AS-IS | `docs/00` §5.2 | Discovery institucional | Ninguno demuestra proceso actual | 02, 10 como disciplina | [PENDIENTE_VALIDACION] | Requiere MAPRO/entrevistas/observación; RF no sustituye evidencia de campo |
| OE2: definir TO-BE, RF/RNF, UX, datos, API | `docs/00` §5.2 | Diseño digital | 01–24 | 01–12 | [PARCIAL] | PRD y capítulo III usan ID y alcance distintos |
| OE3: construir núcleo y cuatro superficies | `docs/00` §5.2 | Portal, reserva, espera, flujo | 01–21, 23–24 | 03, 06–09, 11 | [PARCIAL] | Backend parcial; web/móvil no observados; ofertas y preferencias requieren RF refinados |
| OE4: seguridad/privacidad | `docs/00` §5.2 | Identidad, permisos, auditoría | 01, 02, 04, 22, 24 | 01, 02, 10, 11 | [PARCIAL] | Debe cubrir todas las superficies y controles por objeto |
| OE5: pruebas y aprovechamiento de agenda | `docs/00` §5.2 | Pruebas, métricas y recuperación | 08–13, 16–22 | 03–05, 09–12 | [PARCIAL] | Sin línea base hospitalaria; metas del capítulo III son académicas |
| OE6: información pública y acceso digital | `docs/00` §5.2 | Portal público → reservas | 06, 23 | 02, 05–07 | [NO_IMPLEMENTADO] web | Contenido institucional necesita fuente/autorización |

## Matriz de RF (serie RF-01–24)

Abreviaturas de proceso: PUB portal; ID identidad; CAT catálogo; AGE agenda; CIT citas; PRI prioridad; ESP espera/ofertas; FLU flujo; AUD auditoría/métricas. Estado técnico basado en inspección estática de `apps/backend/src/main/java` y `database/migrations/`; no se ejecutaron pruebas.

| RF | Descripción canónica de trabajo | Actor | Proceso | Entrada → salida | Entidad afectada | Objetivo | Evidencia | Estado / observación |
|---|---|---|---|---|---|---|---|---|
| RF-01 | Registro e inicio de sesión | Paciente | ID | Credenciales/datos → sesión o error | User, RefreshToken | OE3/4 | Cap. III §1.1.1; `docs/00` §19 | [PARCIAL] registro público por verificar; RF agrupa dos operaciones |
| RF-02 | Usuarios, roles, permisos y propiedad | Admin; sistema | ID | Asignación/solicitud → acceso o rechazo | User, Role, Permission | OE3/4 | Id.; ADR-005 | [PARCIAL] roles difieren del roadmap; RF amplio |
| RF-03 | Alta/actualización de paciente | Paciente; personal autorizado | ID/CIT | Datos mínimos → perfil | Patient | OE3/4 | Id.; `docs/01` HU-001 | [PARCIAL] actor de alta asistida por fijar |
| RF-04 | Consulta de citas/estados propios | Paciente; personal autorizado | CIT/FLU | Identidad/filtro → citas | Appointment | OE3/4 | Id.; `docs/03` §5 | [PARCIAL] criterios por rol incompletos |
| RF-05 | Gestionar profesionales y especialidades | Admin | CAT/AGE | Datos/licencia → profesional | Professional, Specialty | OE3 | Id.; `docs/00` §6 | [PARCIAL] validación especialidad por verificar |
| RF-06 | Mostrar/administrar especialidades activas | Visitante; admin | PUB/CAT | Catálogo/edición → vista/estado | Specialty | OE3/6 | Id.; `docs/01` RN-010 | [NO_IMPLEMENTADO] catálogo API pública observado; RF mezcla lectura/escritura |
| RF-07 | Configurar agenda y generar slots | Admin | AGE | Horario → slots | Schedule, AvailabilitySlot | OE3 | Id.; ADR-006 | [PARCIAL] reglas de consultorio/solape por definir |
| RF-08 | Consultar cupos disponibles | Paciente/personal | AGE/CIT | Filtros → cupos usables | AvailabilitySlot | OE3/5 | Id.; `docs/06` §11 | [PARCIAL] criterios institucionales por validar |
| RF-09 | Crear reserva | Paciente/recepción | CIT | Slot/motivo → cita SCHEDULED | Appointment | OE3 | Id.; `docs/06` §12 | [PARCIAL] API/backend observados |
| RF-10 | Evitar doble reserva | Sistema | CIT | Solicitudes concurrentes → una reserva/conflicto | Appointment, AvailabilitySlot | OE3/5 | Id.; V3 SQL | [PARCIAL] código y pruebas fuente; ejecución no verificada |
| RF-11 | Constancia y confirmar asistencia | Paciente/sistema | CIT | Cita/confirmación → constancia/CONFIRMED | Appointment, Notification | OE3/5 | Id.; `docs/00` §18.1 | [PARCIAL] constancia vs notificación requiere separar aceptación |
| RF-12 | Reprogramar atómicamente | Paciente/actor autorizado | CIT | Cita/slot nuevo → sucesora o sin cambio | Appointment, Slot | OE3/5 | Id.; ADR-007 | [PARCIAL] política temporal pendiente |
| RF-13 | Cancelar y liberar cupo | Paciente/actor autorizado | CIT/ESP | Cita/motivo → CANCELLED/slot elegible | Appointment, Slot, AuditLog | OE3/5 | Id.; `docs/00` §18 | [PARCIAL] criterios de elegibilidad pendientes |
| RF-14 | Crear/consultar/cancelar espera | Paciente | ESP | Especialidad/preferencia → WaitlistEntry | WaitlistEntry | OE3/5 | Id.; `docs/00` §11.2 | [NO_IMPLEMENTADO] RF agrupa tres operaciones |
| RF-15 | Solicitar y revisar prioridad manual | Paciente/revisor autorizado | PRI | Solicitud/criterio → decisión | PriorityRequest, AuditLog | OE3/4 | Id.; ADR-008 | [NO_IMPLEMENTADO] política y rol por validar |
| RF-16 | Avisos y recordatorios | Sistema | CIT/ESP | Evento/regla → aviso o fallo | Notification | OE3/5 | Id.; `docs/00` §18.4 | [NO_IMPLEMENTADO] canales/tiempo por definir |
| RF-17 | Check-in de cita confirmada | Recepción | FLU | Cita → CHECK_IN | Appointment, AuditLog | OE3/5 | Id.; ADR-007 | [PARCIAL] backend observado |
| RF-18 | Registrar cuatro etapas | Recepción/profesional | FLU | Etapa actual/comando → siguiente etapa | Appointment, AuditLog | OE3/5 | Id.; ADR-007 | [PARCIAL] falta verificación ejecutada |
| RF-19 | Consultas operativas por rol | Personal/profesional | AGE/FLU | Rol/filtro → agenda/citas | Schedule, Appointment | OE3/4 | Id.; `docs/03` §§7–10 | [PARCIAL] paneles no observados |
| RF-20 | Indicadores por periodo | Admin/auditor autorizado | AUD | Periodo → indicadores | Appointment/AuditLog derivados | OE5 | Id.; `docs/00` §7.3 | [NO_IMPLEMENTADO] definiciones y denominadores por cerrar |
| RF-21 | Recuperar cupo desde espera | Sistema/paciente/personal | ESP | Cupo/entrada/oferta → aceptación y reserva | WaitlistEntry, SlotOffer, Appointment | OE3/5 | Id.; `docs/00` §§11.2, 18.2 | [NO_IMPLEMENTADO] texto del RF no explicita consentimiento/oferta/expiración; corregir |
| RF-22 | Auditar cambios críticos | Sistema | AUD | Evento → registro | AuditLog | OE3/4/5 | Id.; ADR-011 | [PARCIAL] cobertura total por verificar |
| RF-23 | Mostrar/mantener portal público | Visitante/admin | PUB | Contenido autorizado → página | Specialty, contenido | OE6 | Id.; `docs/00` §14.2.1 | [NO_IMPLEMENTADO] divide consulta y edición |
| RF-24 | Renovar/cerrar sesión | Paciente/personal | ID | Token/acción → sesión renovada/revocada | RefreshToken | OE3/4 | Id.; ADR-003/004 | [PARCIAL] proveedor móvil no definido |

**Hallazgos RF:** ningún RF de esta serie carece por completo de objetivo, pero OE1 no tiene RF demostrativo porque es actividad de investigación. Los RF-01, 02, 05, 06, 11, 14, 15, 21, 23 y 24 son compuestos o amplios; requieren casos y aceptación por acción sin renumerar ahora. RF-21 necesita incorporar oferta/aceptación/expiración. Falta RF explícito para preferencias de accesibilidad, `NO_SHOW` decidido, política de confirmación, contenido autorizado y quizá consultorios físicos. Los RF-014 y RF-020 de **la serie PRD** chocan con exclusión clínica; deben resolverse antes de una SRS única (`docs/01` §§5, 10; `docs/00` §§6.2, 11.4).

### Correspondencia temática con la serie RF-001–020 del PRD

Esta tabla **no** aprueba equivalencias de identificadores. Solo localiza temas duplicados o divergentes (`docs/01-PRD.md` §5 frente a `recursos/CAPITULO-III.md` §1.1.1).

| RF del PRD | Tema | RF capítulo III relacionado | Evaluación |
|---|---|---|---|
| RF-001 | Registro/login | RF-01, 24 | Coincidencia parcial; sesiones se separan en capítulo III |
| RF-002 | Roles/permisos | RF-02 | Misma familia, actores/roles difieren |
| RF-003 | Información institucional | RF-23 | Capítulo III añade autorización de contenido |
| RF-004 | Especialidades públicas | RF-06, 23 | Solapamiento con RF-005; lectura/edición mezcladas |
| RF-005 | Contenido especialidades | RF-23 | Solapamiento con RF-003/004; falta entidad/contenido autorizado |
| RF-006 | Disponibilidad | RF-08 | Coincidencia temática; filtros precisados después |
| RF-007 | Reserva | RF-09 | Coincidencia; concurrencia separada en RF-010 |
| RF-008 | Cancelación | RF-13 | Capítulo III incluye historial y liberación |
| RF-009 | Reprogramación | RF-12 | Capítulo III añade atomicidad |
| RF-010 | Doble asignación | RF-10 | Coincidencia temática; validación concurrente precisa |
| RF-011 | Profesionales | RF-05 | Coincidencia parcial; colegiatura/especialidad ampliadas |
| RF-012 | Agendas | RF-07 | Coincidencia; slots detallados |
| RF-013 | Estados de flujo | RF-18 | Capítulo III restringe a cuatro estados |
| RF-014 | Información básica de atención | Ninguno aprobado para MVP | [CONTRADICCION] con exclusión clínica del roadmap/ADR-007 |
| RF-015 | Auditoría | RF-22 | Coincidencia parcial; eventos críticos por definir |
| RF-016 | Notificaciones | RF-16 | Coincidencia; canal y regla pendientes |
| RF-017 | Lista de espera | RF-14, 21 | PRD no explicita oferta y consentimiento |
| RF-018 | Dashboard | RF-20 | Configurabilidad y métricas no especificadas a nivel de dato |
| RF-019 | Llegada/admisión | RF-17, 18 | `CHECK_IN` sí; admisión adicional requiere validación |
| RF-020 | Evaluación inicial triaje | Ninguno aprobado para FASE 5.11 | [CONTRADICCION] dato clínico fuera de MVP; posible proceso posterior |

Quedan necesidades del roadmap sin RF inequívoco en el PRD: solicitud/revisión manual de prioridad, ofertas de lista de espera con aceptación/rechazo/expiración, preferencias de accesibilidad, `NO_SHOW` como regla futura por definir, y separación formal de portal público/portal de reservas (`docs/00` §§6, 11, 14, 18). No se crea un nuevo número en esta auditoría.

## Matriz RNF (serie RNF-01–12)

Las métricas numéricas proceden de `recursos/CAPITULO-III.md` §1.1.2 y son metas de ensayo propuestas. No son estadísticas ni compromisos del Hospital de Huaycán.

| RNF | Categoría | Descripción y métrica/criterio | Evidencia esperada | Objetivos | Estado de verificabilidad |
|---|---|---|---|---|---|
| RNF-01 | Seguridad | Hash, HTTPS publicado, rol/propiedad servidor; rechazar todos los casos no autorizados definidos | Pruebas de acceso y revisión de logs | OE3/4 | [PROPUESTO] medible sobre suite definida; cobertura por fijar |
| RNF-02 | Privacidad | Datos sintéticos y mínima exposición por rol | Revisión de dataset, API, UI y logs | OE3/4/6 | [PROPUESTO] verificable con checklist; minimización por campo pendiente |
| RNF-03 | Rendimiento | p95 ≤ 3 s con 50 usuarios virtuales, 10 min | Informe de carga con ambiente/datos | OE3/5 | [PROPUESTO] medible en piloto, no hospital |
| RNF-04 | Disponibilidad | ≥99 % sondeos por minuto durante 8 h | Registro de health checks | OE5 | [PROPUESTO] medible en prueba, no SLA productivo |
| RNF-05 | Usabilidad | ≥80 % completa reserva sin ayuda | Protocolo, muestra y resultados | OE3/5/6 | [PROPUESTO] medible; muestra aún indefinida |
| RNF-06 | Accesibilidad | Flujo teclado, foco, etiquetas, lector de pantalla, sin bloqueos críticos; roadmap refiere WCAG 2.2 AA | Revisión manual/automatizada web y móvil | OE2/3/6 | [PARCIAL] criterio cualitativo; falta matriz WCAG, temas y visión de color |
| RNF-07 | Compatibilidad | Reserva/consulta en Chrome, Edge, Firefox y entorno móvil React Native/Expo | Matriz de versiones/dispositivos | OE3/6 | [PROPUESTO] verificable; dispositivos/OS por fijar |
| RNF-08 | Mantenibilidad | Módulos por funcionalidad y migraciones versionadas | Revisión de dependencias, contratos y pruebas | OE2/3 | [PROPUESTO] verificable; umbral de acoplamiento no definido |
| RNF-09 | Escalabilidad | Comparar 10/25/50 usuarios virtuales y recursos | Perfil de carga y comparación | OE3/5 | [PROPUESTO] medible; aceptación de degradación no definida |
| RNF-10 | Trazabilidad | 100 % de transiciones críticas ensayadas reconstruibles | Eventos e historial por caso | OE3/4/5 | [PROPUESTO] medible; universo de eventos pendiente |
| RNF-11 | Integridad | 20 solicitudes simultáneas → una reserva; fallo de reprogramación no altera original | Prueba de concurrencia/transacción | OE3/5 | [PROPUESTO] medible; pruebas no ejecutadas aquí |
| RNF-12 | Recuperación | Restaurar backup en BD aislada y comprobar conteos/relaciones | Acta de restauración y tiempos | OE5 | [PROPUESTO] verificable; RTO/RPO requieren acuerdo |

La serie RNF-001–013 del PRD es más general y tiene **13** elementos; incluye “eventos técnicos” y “módulos futuros” como requisitos propios, sin el mismo mapa semántico. Debe aprobarse equivalencia y decidir si observabilidad e interoperabilidad son RNF separados o criterios de otros RNF.

### Correspondencia temática con RNF-001–013 del PRD

| RNF PRD | Tema | RNF capítulo III relacionado | Hallazgo |
|---|---|---|---|
| RNF-001 | Autenticación segura | RNF-01 | PRD sin métrica |
| RNF-002 | Autorización por roles | RNF-01 | Falta propiedad de recurso en redacción PRD |
| RNF-003 | Información sensible | RNF-02 | Falta matriz de minimización por campo |
| RNF-004 | Eventos de seguridad | RNF-10 y 01 | Falta universo y retención |
| RNF-005 | Rendimiento adecuado | RNF-03 | PRD ambiguo; capítulo III propone carga académica |
| RNF-006 | Usabilidad | RNF-05 | PRD ambiguo; muestra de piloto pendiente |
| RNF-007 | Accesibilidad básica | RNF-06 | “Básica” insuficiente frente a WCAG 2.2 AA del roadmap |
| RNF-008 | Código mantenible | RNF-08 | Criterio PRD incompleto |
| RNF-009 | Evolución modular | RNF-08/09 | Solapa RNF-013 del PRD |
| RNF-010 | Compatibilidad | RNF-07 | Versiones/dispositivos por fijar |
| RNF-011 | Trazabilidad de cambios | RNF-10 | Definir eventos críticos |
| RNF-012 | Eventos técnicos | RNF-10 parcialmente | Observabilidad técnica puede requerir criterio propio |
| RNF-013 | Módulos futuros | RNF-08/09 | Redacción como aspiración; aceptación no verificable |

La serie PRD no formula de modo separado disponibilidad, integridad concurrente ni recuperación probada; el capítulo III los añade como RNF-04, 11 y 12. Las metas propuestas deben aprobarse como objetivos de ensayo académico, nunca como rendimiento observado del hospital.

## Matriz maestra base

| Objetivo | RF/RNF | Actor | Proceso | Caso de uso derivado | Entidades | Artefacto que lo representará | Validación prevista | Estado |
|---|---|---|---|---|---|---|---|---|
| OE1 | Evidencia de campo | Paciente/personal institucional | AS-IS | Describir proceso actual | No aplica | BPMN/IDEF0 AS-IS | MAPRO + entrevistas autorizadas | [PENDIENTE_VALIDACION] |
| OE2/OE6 | RF-23, RNF-06/07 | Visitante/admin | PUB | Consultar y mantener información | Specialty/contenido | CU portal + UX + modelo datos | Revisión contenido/autorización y navegación | [PROPUESTO] |
| OE3/OE4 | RF-01/02/24, RNF-01/02 | Paciente/admin | ID | Registrarse, autenticar, renovar | User/Role/RefreshToken | CU, secuencia login, clases | Acceso permitido/denegado y revocación | [PARCIAL] |
| OE3 | RF-05–08 | Admin/paciente | CAT/AGE | Configurar y consultar agenda | Professional/Schedule/Slot | CU, BPMN TO-BE, ERD | Slots válidos y disponibilidad | [PARCIAL] |
| OE3/OE5 | RF-09/10, RNF-11 | Paciente/sistema | CIT | Reservar cita | Appointment/Slot | CU, secuencia reserva, ERD físico | Concurrencia: una reserva activa | [PARCIAL] |
| OE3/OE5 | RF-11–13/16 | Paciente/sistema | CIT | Confirmar, cancelar, reprogramar | Appointment/Slot/Notification | BPMN, secuencias, estados | Transiciones e historial; fallo no revierte cita | [PARCIAL] |
| OE3/OE5 | RF-14/21, RNF-11 | Paciente/sistema | ESP | Entrar en lista, aceptar oferta | WaitlistEntry/SlotOffer/Slot | BPMN, secuencia oferta, ERD | Consentimiento y única adjudicación | [NO_IMPLEMENTADO] |
| OE3/OE4 | RF-15/22 | Paciente/revisor | PRI | Solicitar/revisar prioridad | PriorityRequest/AuditLog | CU, BPMN, secuencia | Decisión autorizada, trazable | [NO_IMPLEMENTADO] |
| OE3/OE5 | RF-17–19, RNF-10 | Recepción/profesional | FLU | Check-in y completar flujo | Appointment/AuditLog | BPMN, actividad, estados, secuencia | Orden, ownership, auditoría | [PARCIAL] |
| OE4/OE5 | RF-20/22, RNF-02/10 | Admin/auditor | AUD | Consultar indicadores/auditoría | AuditLog/Appointment | CU, dashboard, datos | Reconstrucción y denominadores | [PARCIAL] |
| OE5 | RNF-03–05/09/12 | QA/equipo | Validación | Ensayar carga, UX y recuperación | Datos sintéticos | Plan QA y evidencias | Informes de piloto | [PENDIENTE_VALIDACION] |

**Cadena modelo:** RF-09/10 → CU “Reservar cita” → T08 → `Appointment`/`AvailabilitySlot` → secuencia de reserva → relación/índice de BD → prueba concurrente RNF-11. Cada nuevo actor, clase o paso deberá señalar un RF, RNF, regla o ADR concreto.

## Matriz canónica SRS 0.1 — referencia activa

Los casos de uso son nombres textuales derivados del requisito, no diagramas definitivos. Las validaciones son **planeadas**, no resultados ejecutados. `OE1` sigue ligado al levantamiento, no a una funcionalidad inventada.

| Objetivo | Requisito canónico | Actor | Caso de uso derivado | Proceso | Entidades/datos | Validación prevista |
|---|---|---|---|---|---|---|
| OE3/OE4 | RF-001 | Paciente | Registrar cuenta | Identidad | User, Patient | Alta válida única y duplicado rechazado |
| OE3/OE4 | RF-002 | Paciente/personal | Iniciar sesión | Identidad | User, Role | Credencial correcta/incorrecta, sin fuga |
| OE3/OE4 | RF-003 | Usuario autenticado | Renovar/cerrar sesión | Identidad | RefreshToken | Token revocado no renueva |
| OE3/OE4 | RF-004 | Admin/sistema | Gestionar roles y autorizar | Identidad | User, Role, Permission | Acceso ajeno y cambio de rol no autorizado rechazados |
| OE3/OE4 | RF-005 | Paciente | Mantener perfil | Pacientes | Patient | Documento único y campos permitidos |
| OE3/OE4 | RF-006 | Paciente | Consultar mis citas | Citas | Appointment | No leer cita ajena |
| OE3 | RF-007 | Admin | Mantener profesional | Catálogo | Professional, Specialty | Licencia única y asociación válida |
| OE3/OE6 | RF-008 | Admin | Mantener especialidades | Catálogo | Specialty | Inactiva no reservable |
| OE3/OE6 | RF-009 | Admin | Publicar contenido | Portal público | Contenido institucional | Fuente/autorización antes de publicar |
| OE3/OE6 | RF-010 | Visitante | Consultar portal | Portal público | Specialty, contenido | CTA y contenido permitido |
| OE3 | RF-011 | Admin | Configurar agenda | Agenda | Schedule, AvailabilitySlot | Horario válido y slot no duplicado |
| OE3/OE5 | RF-012 | Paciente | Consultar disponibilidad | Agenda | Specialty, Slot | Ocupados/bloqueados no reservables |
| OE3/OE5 | RF-013 | Paciente/recepción | Reservar cita | Citas | Patient, Slot, Appointment | SCHEDULED con IDs derivados por servidor |
| OE3/OE5 | RF-014; RNF-011 | Sistema | Impedir doble reserva | Citas | Slot, Appointment | 20 solicitudes → una cita activa |
| OE3 | RF-015 | Sistema | Emitir constancia | Citas | Appointment | Constancia no equivale a CONFIRMED |
| OE3/OE5 | RF-016 | Paciente | Confirmar asistencia | Citas | Appointment, AuditLog | Transición autorizada; plazo pendiente |
| OE3/OE5 | RF-017 | Paciente/recepción | Cancelar cita | Citas | Appointment, AuditLog | Historia y permiso conservados |
| OE3/OE5 | RF-018 | Sistema | Liberar cupo | Recuperación | Slot, Appointment | Solo elegible; completada no libera |
| OE3/OE5 | RF-019 | Paciente/recepción | Reprogramar cita | Citas | Appointment, Slot | Fallo conserva original |
| OE3/OE5 | RF-020 | Paciente | Entrar en lista de espera | Espera | WaitlistEntry | Entrada no crea cita |
| OE3/OE4 | RF-021 | Paciente | Solicitar revisión prioridad | Prioridad | PriorityRequest | No autoaprobación |
| OE3/OE4 | RF-022 | Revisor autorizado | Resolver prioridad | Prioridad | PriorityRequest, AuditLog | Decisión/motivo/actor; sin diagnóstico |
| OE3/OE5 | RF-023 | Sistema | Notificar/recordar | Comunicación | Notification | Fallo de push no revierte cita |
| OE3/OE5 | RF-024 | Sistema | Ofertar cupo | Recuperación | SlotOffer, WaitlistEntry | Oferta no asigna cita |
| OE3/OE5 | RF-025; RNF-011 | Paciente | Responder oferta | Recuperación | SlotOffer, Appointment, Slot | Consentimiento; una adjudicación |
| OE3/OE5 | RF-026 | Recepción | Hacer check-in | Flujo | Appointment, AuditLog | Solo CONFIRMED; repetición idempotente |
| OE3/OE5 | RF-027 | Recepción | Pasar a espera | Flujo | Appointment, AuditLog | CHECK_IN → WAITING exacto |
| OE3/OE5 | RF-028 | Profesional titular | Iniciar atención | Flujo | Appointment, Professional | WAITING → IN_ATTENTION y ownership |
| OE3/OE5 | RF-029 | Profesional titular | Finalizar atención | Flujo | Appointment, AuditLog | FINISHED + COMPLETED; slot consumido |
| OE3/OE4 | RF-030 | Personal autorizado | Consultar operación | Web operativa | Schedule, Appointment | Filtrado por rol/propiedad |
| OE3/OE5 | RF-031 | Admin/auditor | Consultar indicadores | Métricas | Appointment, AuditLog | Cohorte/denominador reproducible |
| OE3/OE4/OE5 | RF-032; RNF-010 | Sistema | Auditar evento | Auditoría | AuditLog | Evento por transición, sin secreto |
| OE2/OE3/OE6 | RF-033; RNF-006 | Usuario interfaz | Elegir preferencia visual | Accesibilidad | Preferencia | Estado legible sin color y restaurable |
| OE3/OE4 | RNF-001/002 | QA | Verificar seguridad/privacidad | Transversal | User, datos sintéticos | Suite de accesos y revisión datos/logs |
| OE3/OE5 | RNF-003/004/009 | QA | Medir carga y salud | Validación | Telemetría | p95, sondeos y perfiles documentados |
| OE3/OE5/OE6 | RNF-005/006/007 | QA/participantes | Validar experiencia | UX | Vistas web/móvil | Tareas, teclado, compatibilidad |
| OE2/OE3 | RNF-008 | Equipo técnico | Revisar modularidad | Arquitectura | Módulos/migraciones | Revisión y pruebas por cambio |
| OE3/OE5 | RNF-011/012/013 | QA/operación | Validar integridad/recuperación/observabilidad | Operación | BD/logs | Carrera, restauración y fallos inyectados |
| OE1 | Evidencia de levantamiento, sin RF de software | Personal institucional autorizado | Validar proceso actual | AS-IS | No aplica | MAPRO y entrevistas/observación autorizadas |

### Cobertura documental tras consolidación

| Objetivo | RF | RNF | Cobertura |
|---|---|---|---|
| OE1 | Ninguno de software; artefacto AS-IS | 002/010 como restricciones | PARCIAL |
| OE2 | 009–014, 033 como insumos | 001–013 | PARCIAL |
| OE3 | 001–033 | 001–011, 013 | PARCIAL |
| OE4 | 002–004, 021–022, 030, 032 | 001/002/010/011 | PARCIAL |
| OE5 | 012–020, 023–025, 031–032 | 003–005, 009–013 | PARCIAL |
| OE6 | 008–010, 033 | 002, 005–007 | PARCIAL |

**Pendiente:** la matriz se convertirá en línea base aprobada solo cuando se validen políticas de `NO_SHOW`, espera/ofertas, prioridad, plazos, contenido público y el AS-IS. No se han creado diagramas ni pruebas nuevas en esta consolidación.
