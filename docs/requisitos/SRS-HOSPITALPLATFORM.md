# SRS — HospitalPlatform

**Versión:** 0.1, propuesta canónica documental, 23/09/2026. **Caso de estudio:** Hospital de Huaycán. **Estado:** pendiente de revisión del equipo/docente y de validación institucional de reglas operativas. Esta SRS sustituye como **propuesta de referencia para requisitos** a las dos series incompatibles, sin modificar sus archivos. No certifica implementación. Equivalencias y decisiones: `EQUIVALENCIAS_REQUISITOS.md`; reglas: `REGLAS_NEGOCIO.md`; auditoría: `docs/auditoria/AUDITORIA_DOCUMENTAL.md`.

## 1. Propósito, autoridad y vocabulario

El producto es un prototipo académico con datos sintéticos para consulta externa programada. La jerarquía utilizada es `docs/00-PROJECT-ROADMAP.md` → ADR aceptadas → `docs/03-APP-FLOW.md` → arquitectura/modelo de datos vigente → `docs/01-PRD.md` → `docs/research/recursos/CAPITULO-III.md` → otros recursos. La evidencia de código solo informa el estado de implementación, no la definición de necesidades. [CONFIRMADO] significa decisión documental vigente; [PROPUESTO] diseño aún revisable; [PENDIENTE_VALIDACION] política/dato por confirmar; [PARCIAL]/[NO_IMPLEMENTADO] describen inspección estática, no resultado de pruebas.

**Actores propuestos:** visitante, paciente, administrador, recepcionista, profesional, revisor clínico autorizado, auditor y sistema automático. `RECEPTIONIST` y `PROFESSIONAL` son los roles de transición ya aprobados por ADR-007; `ADMISSION`, `FLOW_OPERATOR`, `PRACTITIONER`, `CLINICAL_PRIORITY_REVIEWER` y `AUDITOR` del roadmap siguen siendo candidatos y requieren mapeo institucional (`docs/00` §19.3; ADR-005/007). “Sistema” no es un rol humano.

**Objetivos:** OE1 levantamiento del AS-IS; OE2 diseño del TO-BE/requisitos/UX/datos/API; OE3 núcleo y cuatro superficies; OE4 seguridad y privacidad; OE5 validación y aprovechamiento de agenda; OE6 información pública (`docs/00` §5.2). OE1 depende de evidencia de campo; una función del software no prueba el proceso actual.

## 2. Resolución de alcance

### Dentro del MVP

Portal público institucional, portal web de reservas, web operativa y app móvil del paciente **obligatoria**; autenticación y permisos; pacientes, profesionales, catálogo, agenda y slots; reserva, constancia, confirmación, cancelación, reprogramación y liberación; lista de espera, oferta, consentimiento y reasignación única; solicitud y revisión **manual** de prioridad ambulatoria con política validada; avisos in-app/push propuestos; preferencias accesibles; cuatro etapas operativas aprobadas; auditoría e indicadores académicos. Fuentes: `docs/00` §§1, 5–6, 11, 14, 18–19; ADR-006–008. La inclusión en MVP total no implica que esté en el incremento backend FASE 5.11.

### Fuera del MVP

Historia clínica completa, diagnóstico, signos vitales/síntomas/antecedentes como datos de triaje, notas clínicas, prescripción, farmacia/laboratorio como sistemas integrales, emergencia, hospitalización, cirugía, facturación completa e integraciones oficiales MINSA/SIS/SIHCE/DIRIS/PRONIS. Fuentes: `docs/00` §§6.1–6.3, 16; ADR-007. Los antiguos `RF-014` y `RF-020` del PRD quedan solo como antecedentes incompatibles.

### Futuro

Espacio clínico del profesional con documentación estructurada y PDFs derivados, firma y cobertura/integraciones autorizadas; WhatsApp y otras integraciones, Redis/colas solo con detonante medido. Fuentes: `docs/00` §§6.3, 16, 18.4, 20, 25 fases 17–18.

### Pendiente de validación

Proceso AS-IS y pasos institucionales de admisión/triaje/llamado; rol de prioridad, criterios y selección de lista; plazos de confirmación/cancelación/reprogramación y T-7; modalidad de check-in; contenido público; métricas y línea base. **`NO_SHOW`** se contempla en el roadmap como capacidad MVP, pero está fuera de los estados/operaciones aprobados para FASE 5.11: requiere una decisión posterior sobre evento o estado, plazo, autoridad y efecto en slot antes de ser requisito ejecutable. Fuentes: `docs/00` §§4.2, 6.1, 7.1, 11.1, 18, 29; ADR-007. No se agrega a `AppointmentStatus`.

**Estados aprobados:** `AppointmentStatus = SCHEDULED | CONFIRMED | CANCELLED | RESCHEDULED | COMPLETED`. `FlowStage = null | CHECK_IN | WAITING | IN_ATTENTION | FINISHED`; `CHECK_IN` inicia solo con cita `CONFIRMED`, y `FINISHED` pone la cita `COMPLETED`. Admisión, triaje y llamado no son valores adicionales de `FlowStage` (`docs/00` §§11.1, 11.4; ADR-007; `docs/03` §3).

## 3. Requisitos funcionales canónicos propuestos

**Convención:** prioridad “Alta” significa compromiso de alcance MVP; la entrega depende de las reglas marcadas pendientes. Los RF con origen en funciones del roadmap ausentes en las dos series se señalan [PROPUESTA]. Los criterios describen evidencia futura, no pruebas ejecutadas.

### RF-001 — Registrar cuenta de paciente

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE4. Actor principal: paciente. Secundarios: sistema.

**Descripción:** crear una cuenta con datos mínimos validados sin duplicar identidad. **Precondiciones:** paciente sin cuenta activa equivalente. **Flujo principal:** introducir campos requeridos → validar formato y unicidad → guardar usuario/paciente → informar resultado. **Alternos/excepciones:** datos inválidos o duplicados se rechazan sin crear registro parcial. **Postcondiciones:** cuenta persistida o estado sin cambios. **Reglas:** RB-001/002. **Datos:** User, Patient, identificador/documento mínimo y credencial protegida. **Aceptación:** un registro válido crea una sola cuenta; repetir identificador/documento devuelve error controlado; la respuesta no expone hash. **Dependencias:** política de campos y consentimiento por validar. **Fuente:** PRD RF-001; Cap. III RF-01/03; roadmap §§6.1, 19.

### RF-002 — Autenticar usuario

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE4. Actor principal: paciente o personal autorizado. Secundario: sistema.

**Descripción:** comprobar credenciales y estado de cuenta para conceder sesión con permisos aplicables. **Precondiciones:** cuenta habilitada. **Flujo principal:** recibir credenciales → verificar → emitir sesión/tokens según canal. **Alternos:** credenciales erróneas o cuenta inhabilitada producen rechazo sin revelar cuál dato falló. **Postcondiciones:** sesión válida o ninguna. **Reglas:** RB-002. **Datos:** User, Role, credencial, token. **Aceptación:** credencial correcta permite acceder a recurso permitido; incorrecta no genera sesión y no aparece en logs. **Dependencias:** ADR-003/004. **Fuente:** PRD RF-001; Cap. III RF-01; ADR-003/004; `docs/00` §19.

### RF-003 — Renovar y revocar sesión

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE4. Actor principal: usuario autenticado. Secundario: sistema.

**Descripción:** renovar sesión vigente y cerrarla mediante revocación del mecanismo de renovación. **Precondiciones:** token de renovación válido o sesión identificable. **Flujo:** validar token → rotar/renovar; al salir, revocar → impedir nueva renovación. **Alternos:** expirado/revocado se rechaza. **Postcondiciones:** token actualizado o sesión terminada. **Reglas:** RB-002. **Datos:** RefreshToken, User. **Aceptación:** un token revocado no permite renovar; un token válido sí bajo el contrato aprobado. **Dependencias:** estrategia móvil/canal ADR. **Fuente:** Cap. III RF-24; `docs/06` §6; ADR-003/004.

### RF-004 — Administrar roles y autorizar recursos

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE4. Actor principal: administrador autorizado. Secundario: sistema.

**Descripción:** asignar roles/permisos y aplicar autorización por rol, permiso y propiedad en cada operación protegida. **Precondiciones:** actor autenticado con permiso de administración para cambios. **Flujo:** validar privilegio → modificar asignación → auditar; al acceder, evaluar permiso/propiedad. **Alternos:** rol no válido o falta de privilegio/propiedad se rechaza. **Postcondiciones:** permisos coherentes o sin cambio. **Reglas:** RB-002/020. **Datos:** User, Role, Permission, AuditLog. **Aceptación:** un paciente no lee cita ajena; solo administrador autorizado cambia roles; la vista oculta no reemplaza el rechazo del servidor. **Dependencias:** mapa final de roles. **Fuente:** PRD RF-002; Cap. III RF-02; ADR-005; roadmap §19.

### RF-005 — Mantener perfil de paciente

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE4. Actor principal: paciente. Secundario: personal autorizado, según permisos.

**Descripción:** consultar y actualizar solo datos de perfil permitidos, preservando unicidad de documento. **Precondiciones:** paciente identificado. **Flujo:** consultar perfil propio → modificar campos editables → validar → guardar. **Alternos:** documento duplicado, campo prohibido o perfil ajeno se rechazan. **Postcondiciones:** perfil actualizado o intacto. **Reglas:** RB-001/002. **Datos:** Patient, User. **Aceptación:** modificación válida persiste; intento de alterar perfil ajeno o duplicar documento no persiste. **Dependencias:** lista de campos y permisos. **Fuente:** Cap. III RF-03; PRD HU-001; `docs/02` §8.

### RF-006 — Consultar citas propias

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE4. Actor principal: paciente. Secundario: sistema.

**Descripción:** mostrar citas y estados administrativos/operativos solo del paciente autenticado. **Precondiciones:** sesión válida vinculada a paciente. **Flujo:** solicitar lista/detalle → filtrar por propiedad → devolver estados. **Alternos:** ID ajeno o inexistente no revela datos. **Postcondiciones:** sin modificación. **Reglas:** RB-002/016/017. **Datos:** Patient, Appointment. **Aceptación:** el paciente ve sus citas y no obtiene las de otro mediante ID directo. **Dependencias:** RF-002/004. **Fuente:** Cap. III RF-04; `docs/03` §5; `docs/06` §12.

### RF-007 — Administrar profesionales

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3. Actor principal: administrador autorizado. Secundario: sistema.

**Descripción:** registrar/actualizar profesional y asociarlo a especialidades habilitadas. **Precondiciones:** permiso y catálogo aplicable. **Flujo:** validar datos/colegiatura → asociar especialidades → guardar. **Alternos:** licencia duplicada o asociación inválida se rechaza. **Postcondiciones:** profesional disponible para agenda o sin cambio. **Reglas:** RB-002/003. **Datos:** Professional, Specialty. **Aceptación:** licencia repetida no crea segundo profesional; asociación válida se consulta. **Dependencias:** fuente institucional de profesionales/especialidades. **Fuente:** PRD RF-011; Cap. III RF-05; `docs/00` §6.1.

### RF-008 — Administrar catálogo de especialidades

Estado: [PROPUESTO], [NO_IMPLEMENTADO] API observada. Prioridad: Alta. Objetivo: OE3/OE6. Actor principal: administrador autorizado. Secundario: sistema.

**Descripción:** crear/editar/activar/desactivar especialidades para reserva. **Precondiciones:** permiso; datos validados. **Flujo:** guardar cambio → actualizar estado consultable. **Alternos:** código duplicado o edición no autorizada se rechaza. **Postcondiciones:** catálogo modificado o intacto. **Reglas:** RB-003/020. **Datos:** Specialty. **Aceptación:** especialidad desactivada deja de ofrecerse para nuevas reservas, sin borrar citas históricas. **Dependencias:** catálogo oficial/verificado por validar. **Fuente:** PRD RF-004; Cap. III RF-06; PRD RN-010; roadmap §§6.1, 8.

### RF-009 — Mantener contenido público autorizado

Estado: [PROPUESTO], [NO_IMPLEMENTADO]. Prioridad: Alta. Objetivo: OE3/OE6. Actor principal: administrador autorizado. Secundario: revisor de contenido por definir.

**Descripción:** mantener información institucional, servicios, políticas, ubicación y medios autorizados, con origen verificable. **Precondiciones:** permiso y material aprobado. **Flujo:** ingresar contenido/fuente → validar autorización → publicar o actualizar. **Alternos:** sin fuente/autorización no se publica. **Postcondiciones:** contenido público versionado o sin cambio. **Reglas:** RB-022. **Datos:** contenido institucional y Specialty. **Aceptación:** contenido sin fuente aprobada permanece no publicado; cambio autorizado se refleja en portal. **Dependencias:** responsable editorial y materiales. **Fuente:** PRD RF-003/005; Cap. III RF-23; roadmap §14.2.1.

### RF-010 — Consultar portal público y acceder a reservas

Estado: [PROPUESTO], [NO_IMPLEMENTADO] web. Prioridad: Alta. Objetivo: OE3/OE6. Actor principal: visitante. Secundario: sistema.

**Descripción:** mostrar contenido público aprobado y especialidades, con acceso claro al portal de reservas diferenciado. **Precondiciones:** contenido publicado. **Flujo:** navegar → consultar información → elegir CTA de reservas. **Alternos:** contenido no disponible muestra estado claro sin presentar datos no verificados. **Postcondiciones:** sin modificación. **Reglas:** RB-003/022/023. **Datos:** contenido, Specialty. **Aceptación:** una especialidad inactiva no aparece como reservable; el CTA conduce al flujo específico de citas. **Dependencias:** RF-008/009, web. **Fuente:** PRD RF-003/004; Cap. III RF-06/23; roadmap §14.2.

### RF-011 — Configurar agenda y slots

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3. Actor principal: administrador autorizado. Secundario: sistema.

**Descripción:** configurar horario por profesional/especialidad y generar slots discretos sin duplicidad o intervalos inválidos. **Precondiciones:** profesional y especialidad habilitados. **Flujo:** registrar horario → validar límites/incompatibilidades → crear agenda/slots. **Alternos:** horario inválido o duplicado se rechaza sin slots parciales. **Postcondiciones:** disponibilidad creada o intacta. **Reglas:** RB-002/004/005. **Datos:** Professional, Specialty, Schedule, AvailabilitySlot. **Aceptación:** dos slots con misma agenda/fecha/inicio no se duplican; horario invertido se rechaza. **Dependencias:** duración y calendario institucional por validar. **Fuente:** PRD RF-012; Cap. III RF-07; ADR-006; `docs/06` §11.

### RF-012 — Consultar disponibilidad reservable

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE5. Actor principal: paciente. Secundarios: recepción, sistema.

**Descripción:** consultar cupos por filtros autorizados, distinguiendo especialidad existente de oferta reservable. **Precondiciones:** agenda activa. **Flujo:** seleccionar especialidad/fecha/profesional permitido → devolver slots usables. **Alternos:** sin cupos muestra ausencia y posible lista de espera; slots ocupados/bloqueados no se ofrecen. **Postcondiciones:** sin reserva. **Reglas:** RB-003/004. **Datos:** Specialty, Schedule, AvailabilitySlot. **Aceptación:** un slot reservado o bloqueado no aparece como reservable; la lista se revalida al reservar. **Dependencias:** RF-011, filtros institucionales. **Fuente:** PRD RF-006; Cap. III RF-08; roadmap §14.2.2.

### RF-013 — Reservar cita

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE5. Actor principal: paciente. Secundarios: recepción autorizada, sistema.

**Descripción:** crear cita para paciente activo y slot reservable, con profesional/especialidad derivados por servidor. **Precondiciones:** identidad/permisos, slot válido y disponible. **Flujo:** seleccionar slot → revalidar → crear Appointment `SCHEDULED`, `flowStage=null` → devolver ID. **Alternos:** slot no reservable, paciente/profesional inválido o falta de permiso devuelve error sin cita parcial. **Postcondiciones:** cita activa y slot reservado. **Reglas:** RB-002/004–006. **Datos:** Patient, Professional, AvailabilitySlot, Appointment. **Aceptación:** un slot libre crea una cita con IDs coherentes; paciente no fuerza `professionalId` ni estados. **Dependencias:** RF-005/011/012/014. **Fuente:** PRD RF-007; Cap. III RF-09; `docs/06` §12.

### RF-014 — Impedir doble asignación concurrente

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE5. Actor principal: sistema. Secundarios: pacientes competidores.

**Descripción:** asignar un cupo en transacción con restricción de unicidad activa. **Precondiciones:** solicitudes concurrentes sobre slot válido. **Flujo:** bloquear/actualizar de forma atómica → persistir una cita activa. **Alternos:** solicitud perdedora recibe conflicto controlado, sin estado intermedio. **Postcondiciones:** a lo sumo una cita activa por slot. **Reglas:** RB-005/014. **Datos:** AvailabilitySlot, Appointment. **Aceptación:** en 20 solicitudes simultáneas de ensayo, solo una reserva activa queda persistida; las demás no obtienen ese cupo. **Dependencias:** RF-013, índice de migración V3 y RNF-011. **Fuente:** PRD RF-010; Cap. III RF-10; roadmap §12.3; `database/migrations/V3__support_appointment_lifecycle.sql`.

### RF-015 — Emitir constancia de reserva

Estado: [PROPUESTO], [PARCIAL] respuesta backend; interfaz no implementada. Prioridad: Alta. Objetivo: OE3. Actor principal: sistema. Secundario: paciente.

**Descripción:** devolver identificador, fecha/horario, profesional/especialidad y `SCHEDULED` tras reserva válida, sin confundir constancia con confirmación de asistencia. **Precondiciones:** RF-013 completado. **Flujo:** generar respuesta y presentarla en canal. **Alternos:** si falla aviso externo, la reserva se conserva. **Postcondiciones:** cita sigue `SCHEDULED`. **Reglas:** RB-006/007/021. **Datos:** Appointment, Slot. **Aceptación:** la constancia muestra ID y estado `SCHEDULED`; no cambia a `CONFIRMED` sin RF-016. **Dependencias:** RF-013, interfaz web/móvil. **Fuente:** Cap. III RF-11; `docs/03` §§5, 11; roadmap §18.4.

### RF-016 — Confirmar asistencia

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE5. Actor principal: paciente. Secundarios: recepción autorizada, sistema.

**Descripción:** convertir una cita elegible de `SCHEDULED` a `CONFIRMED` por acción autorizada. **Precondiciones:** cita vigente y política temporal aplicable validada. **Flujo:** autenticar/autorizar → validar transición → registrar `CONFIRMED` y auditoría. **Alternos:** cita ajena, cancelada, completada o fuera de plazo se rechaza; plazo exacto [PENDIENTE_VALIDACION]. **Postcondiciones:** cita confirmada o intacta. **Reglas:** RB-002/007/008/016. **Datos:** Appointment, AuditLog. **Aceptación:** cita elegible cambia a `CONFIRMED`; constancia por sí sola no la cambia. **Dependencias:** RF-013/015, política de plazos. **Fuente:** Cap. III RF-11; roadmap §§6.1, 18.1; ADR-007.

### RF-017 — Cancelar cita

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE5. Actor principal: paciente. Secundarios: recepción autorizada, sistema.

**Descripción:** cancelar una cita elegible, preservando actor, fecha e historial. **Precondiciones:** cita activa propia o permiso apropiado; plazo según política validada. **Flujo:** validar → cambiar a `CANCELLED` → registrar auditoría → solicitar liberación de slot. **Alternos:** falta de permiso/estado/plazo rechaza sin cambio. **Postcondiciones:** cita cancelada, no eliminada. **Reglas:** RB-002/009/011/016. **Datos:** Appointment, AuditLog. **Aceptación:** cancelación elegible conserva historia y dispara evaluación de liberación; no permite avanzar en flujo. **Dependencias:** RF-018, regla de plazos. **Fuente:** PRD RF-008; Cap. III RF-13; `docs/03` §6; roadmap §18.

### RF-018 — Liberar cupo elegible

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE5. Actor principal: sistema. Secundario: paciente en lista de espera.

**Descripción:** hacer reservable el slot liberado por cancelación/reprogramación elegible sin borrar la cita anterior; no liberar al completar atención. **Precondiciones:** operación válida y política de reutilización aplicable. **Flujo:** determinar elegibilidad → liberar slot de forma transaccional → emitir evento para espera. **Alternos:** slot no elegible permanece no reservable. **Postcondiciones:** slot disponible/ofertable o intacto. **Reglas:** RB-009/011/012. **Datos:** Appointment, AvailabilitySlot, AuditLog. **Aceptación:** cancelar libera el slot cuando la regla lo permite; `COMPLETED` conserva `RESERVED`; no se crea cita ajena automáticamente. **Dependencias:** RF-017/019, política de plazo. **Fuente:** PRD RN-004; Cap. III RF-13/21; roadmap §§11.1, 18.2; ADR-007.

### RF-019 — Reprogramar cita atómicamente

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE5. Actor principal: paciente. Secundarios: recepción autorizada, sistema.

**Descripción:** cambiar una cita elegible a otro slot, enlazando anterior y sucesora. **Precondiciones:** cita propia/vigente; nuevo slot reservable; política de plazos. **Flujo:** validar y reservar nuevo slot → marcar anterior `RESCHEDULED` → crear sucesora `SCHEDULED` → evaluar liberación anterior en una transacción. **Alternos:** si nuevo slot falla, conservar cita y slot originales. **Postcondiciones:** pareja anterior/sucesora coherente o ningún cambio. **Reglas:** RB-005/010/011/016. **Datos:** Appointment, AvailabilitySlot, AuditLog. **Aceptación:** error de nuevo slot no cambia original; éxito conserva vínculo y no genera dos citas activas en el mismo slot. **Dependencias:** RF-014/018, política temporal. **Fuente:** PRD RF-009; Cap. III RF-12; ADR-007; `docs/06` §12.

### RF-020 — Gestionar entrada en lista de espera

Estado: [PROPUESTO], [NO_IMPLEMENTADO] módulo funcional. Prioridad: Alta. Objetivo: OE3/OE5. Actor principal: paciente. Secundario: sistema.

**Descripción:** solicitar, consultar y cancelar participación en espera para servicio/especialidad elegible, sin crear cita. **Precondiciones:** identidad y criterios de ingreso aprobados; disponibilidad insuficiente o preferencia permitida. **Flujo:** solicitar → validar → crear entrada activa → consultar/cancelar por propietario. **Alternos:** duplicado, falta de elegibilidad o propiedad ajena se rechazan. **Postcondiciones:** entrada registrada/cancelada; cita sin cambio. **Reglas:** RB-002/012. **Datos:** WaitlistEntry, Patient, Specialty. **Aceptación:** entrada válida es consultable por su titular; entrar en espera no adjudica slot ni cita. **Dependencias:** política de elegibilidad institucional [PENDIENTE_VALIDACION]. **Fuente:** PRD RF-017; Cap. III RF-14; roadmap §11.2.

### RF-021 — Solicitar revisión de prioridad ambulatoria

Estado: [PROPUESTO], [NO_IMPLEMENTADO]. Prioridad: Alta, condicionado. Objetivo: OE3/OE4. Actor principal: paciente. Secundario: sistema.

**Descripción:** registrar petición de revisión sin conceder prioridad clínica ni alterar citas existentes. **Precondiciones:** identidad, información mínima y política institucional aprobada. **Flujo:** presentar solicitud → validar → guardar `REQUESTED` → informar estado. **Alternos:** solicitud incompleta o duplicada según política se rechaza. **Postcondiciones:** solicitud pendiente; agenda no cambia. **Reglas:** RB-015. **Datos:** PriorityRequest, Patient, AuditLog. **Aceptación:** una autodeclaración nunca pasa a `APPROVED` automáticamente; solicitud válida queda auditable. **Dependencias:** criterio institucional y rol revisor [PENDIENTE_VALIDACION]. **Fuente:** Cap. III RF-15; roadmap §11.3; ADR-008.

### RF-022 — Revisar prioridad ambulatoria

Estado: [PROPUESTO], [NO_IMPLEMENTADO]. Prioridad: Alta, condicionado. Objetivo: OE3/OE4. Actor principal: revisor clínico autorizado. Secundario: sistema.

**Descripción:** aprobar o rechazar petición con motivo y actor bajo política validada, sin clasificación automática de urgencia. **Precondiciones:** solicitud pendiente y permiso específico. **Flujo:** consultar evidencias mínimas → decidir → guardar resultado/motivo/auditoría. **Alternos:** usuario no autorizado o decisión sin criterio obligatorio se rechaza. **Postcondiciones:** resultado auditable; cita existente no se desplaza arbitrariamente. **Reglas:** RB-002/015/020. **Datos:** PriorityRequest, AuditLog. **Aceptación:** solo revisor permitido decide; quedan actor, fecha y resultado; paciente no puede aprobarse. **Dependencias:** aprobación institucional de protocolo/permiso. **Fuente:** Cap. III RF-15; roadmap §11.3; ADR-008.

### RF-023 — Generar avisos y recordatorios

Estado: [PROPUESTO], [NO_IMPLEMENTADO] módulo funcional. Prioridad: Alta. Objetivo: OE3/OE5. Actor principal: sistema. Secundario: paciente.

**Descripción:** generar mensajes de reserva/cambio/confirmación y recordatorios configurables por canales aprobados, sin información clínica sensible. **Precondiciones:** evento relevante y canal habilitado. **Flujo:** obtener plantilla/regla → crear aviso → intentar entrega → registrar resultado. **Alternos:** fallo de proveedor registra error/reintento sin revertir transacción de cita. **Postcondiciones:** aviso entregado o fallo rastreable. **Reglas:** RB-008/021. **Datos:** Appointment, Notification, preferencias de canal. **Aceptación:** fallo de push no cancela cita válida; T-7 es valor configurable, no dato institucional confirmado. **Dependencias:** proveedor push, política de aviso y consentimiento [PENDIENTE_VALIDACION]. **Fuente:** PRD RF-016; Cap. III RF-16; roadmap §§6.1, 18.1, 18.4.

### RF-024 — Crear oferta de cupo liberado

Estado: [PROPUESTO], [NO_IMPLEMENTADO]. Prioridad: Alta. Objetivo: OE3/OE5. Actor principal: sistema. Secundario: personal autorizado según política.

**Descripción:** identificar candidatos elegibles de espera y emitir oferta de un slot liberado, sin asignar cita. **Precondiciones:** slot libre y entrada elegible bajo política aprobada. **Flujo:** seleccionar candidato → registrar oferta `PENDING` y caducidad → notificar. **Alternos:** sin candidato, mantener slot disponible; fallo de aviso no crea cita. **Postcondiciones:** oferta pendiente o ninguna. **Reglas:** RB-011–014/021. **Datos:** AvailabilitySlot, WaitlistEntry, SlotOffer, Notification. **Aceptación:** emitir oferta no cambia propietario de slot ni crea cita; selección queda auditada. **Dependencias:** RF-018/020/023, algoritmo de selección y plazo [PENDIENTE_VALIDACION]. **Fuente:** PRD RF-017; Cap. III RF-21; roadmap §§11.2, 18.2.

### RF-025 — Responder oferta y reasignar cupo

Estado: [PROPUESTO], [NO_IMPLEMENTADO]. Prioridad: Alta. Objetivo: OE3/OE5. Actor principal: paciente ofertado. Secundario: sistema.

**Descripción:** aceptar/rechazar oferta vigente; al aceptar, adjudicar el slot en transacción exclusiva, con consentimiento. **Precondiciones:** titular autenticado y oferta `PENDING` no expirada. **Flujo:** responder → validar titular/vigencia/cupo → en aceptación, crear cita y marcar oferta `ACCEPTED`; en rechazo, `REJECTED`. **Alternos:** expirada marca `EXPIRED`; carrera perdida devuelve conflicto y no crea segunda cita. **Postcondiciones:** como máximo una cita activa por slot; otras ofertas se cierran según política. **Reglas:** RB-005/012–014. **Datos:** SlotOffer, WaitlistEntry, AvailabilitySlot, Appointment. **Aceptación:** aceptar oferta ajena o expirada falla; dos aceptaciones concurrentes dan una sola cita; rechazo no asigna. **Dependencias:** RF-014/024, plazo y política de ofertas. **Fuente:** Cap. III RF-21; roadmap §§11.2, 18.2.

### RF-026 — Registrar check-in

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE5. Actor principal: recepcionista (`RECEPTIONIST`). Secundario: sistema.

**Descripción:** avanzar una cita `CONFIRMED` de `flowStage=null` a `CHECK_IN`. **Precondiciones:** cita válida/confirmada y permiso. **Flujo:** validar rol/cita → registrar etapa y auditoría en transacción. **Alternos:** cita cancelada, usuario sin rol o salto se rechaza; repetición actual es idempotente. **Postcondiciones:** `CHECK_IN` sin cambiar `AppointmentStatus`. **Reglas:** RB-002/016–018/020. **Datos:** Appointment, AuditLog. **Aceptación:** recepción puede marcar `CHECK_IN`; paciente/admin no; repetir no duplica evento. **Dependencias:** RF-016 y vinculación de cita. **Fuente:** PRD RF-019; Cap. III RF-17/18; ADR-007; `docs/03` §7.

### RF-027 — Pasar paciente a espera

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE5. Actor principal: recepcionista (`RECEPTIONIST`). Secundario: sistema.

**Descripción:** avanzar de `CHECK_IN` a `WAITING` en cita confirmada. **Precondiciones:** etapa actual `CHECK_IN`. **Flujo:** validar → cambiar etapa → auditar. **Alternos:** salto desde `null`, retroceso o actor no autorizado se rechaza; repetición es idempotente. **Postcondiciones:** `WAITING`. **Reglas:** RB-017/018/020. **Datos:** Appointment, AuditLog. **Aceptación:** orden exacto y un evento por transición efectiva. **Dependencias:** RF-026. **Fuente:** PRD RF-013; Cap. III RF-18; ADR-007.

### RF-028 — Iniciar atención operativa

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE5. Actor principal: profesional vinculado (`PROFESSIONAL`). Secundario: sistema.

**Descripción:** avanzar su cita de `WAITING` a `IN_ATTENTION`, sin almacenar nota clínica. **Precondiciones:** cita confirmada en espera y profesional asociado al usuario. **Flujo:** validar propiedad profesional → transicionar → auditar. **Alternos:** otro profesional, recepción o etapa errónea se rechazan. **Postcondiciones:** `IN_ATTENTION`. **Reglas:** RB-017/018/020. **Datos:** Professional, Appointment, AuditLog. **Aceptación:** profesional titular puede iniciar; otro no; no se crea diagnóstico. **Dependencias:** RF-027, vínculo profesional/usuario. **Fuente:** PRD RF-013; Cap. III RF-18/19; ADR-007.

### RF-029 — Finalizar atención operativa

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE5. Actor principal: profesional vinculado. Secundario: sistema.

**Descripción:** avanzar de `IN_ATTENTION` a `FINISHED`, cambiando cita de `CONFIRMED` a `COMPLETED` en la misma operación. **Precondiciones:** cita del profesional en etapa correcta. **Flujo:** validar → actualizar ambos estados → auditar. **Alternos:** actor/etapa incorrectos se rechazan; repetición no duplica evento. **Postcondiciones:** slot sigue `RESERVED` como capacidad consumida. **Reglas:** RB-011/016–020. **Datos:** Appointment, AvailabilitySlot, AuditLog. **Aceptación:** fin deja `FINISHED` + `COMPLETED`; no libera slot ni crea nota clínica. **Dependencias:** RF-028. **Fuente:** PRD RF-013; Cap. III RF-18; roadmap §11.4; ADR-007.

### RF-030 — Consultar operación por rol

Estado: [PROPUESTO], [PARCIAL] backend; web no implementada. Prioridad: Alta. Objetivo: OE3/OE4. Actor principal: recepcionista o profesional. Secundarios: administrador y revisor autorizados.

**Descripción:** consultar agendas, citas del día, disponibilidad y pacientes en espera según rol/propiedad; profesional solo su agenda. **Precondiciones:** sesión y permiso. **Flujo:** aplicar filtros por servidor → devolver mínimo necesario. **Alternos:** intento de agenda/cita ajena sin permiso se rechaza. **Postcondiciones:** sin modificación. **Reglas:** RB-001/002. **Datos:** Schedule, Appointment, Patient, Professional. **Aceptación:** profesional no consulta agenda ajena; recepción ve solo datos necesarios para check-in. **Dependencias:** RF-004/011/026–029. **Fuente:** Cap. III RF-19; PRD HU-007/008; `docs/03` §§7–10.

### RF-031 — Consultar indicadores operativos

Estado: [PROPUESTO], [NO_IMPLEMENTADO] dashboard funcional. Prioridad: Alta. Objetivo: OE3/OE5. Actor principal: administrador o auditor autorizado. Secundario: sistema.

**Descripción:** calcular indicadores de citas, cupos y flujo por periodo/filtros definidos sobre eventos trazables y datos sintéticos de ensayo. **Precondiciones:** rol autorizado; definición de numerador/denominador. **Flujo:** seleccionar rango → calcular → mostrar valor y población; si denominador cero, indicar “no aplica”. **Alternos:** indicador sin eventos suficientes se marca no medible; acceso no autorizado se rechaza. **Postcondiciones:** sin cambio. **Reglas:** RB-002/024. **Datos:** Appointment, AvailabilitySlot, AuditLog y derivados. **Aceptación:** filtro de fechas modifica cohorte reproduciblemente; no se publica tasa de no-show sin registro/política definida. **Dependencias:** RF-032, definiciones de KPI y línea base [PENDIENTE_VALIDACION]. **Fuente:** PRD RF-018; Cap. III RF-20 y §1.4.3; roadmap §7.3; `docs/12` §7.

### RF-032 — Auditar acciones críticas

Estado: [PROPUESTO], [PARCIAL] backend. Prioridad: Alta. Objetivo: OE3/OE4/OE5. Actor principal: sistema. Secundario: auditor autorizado.

**Descripción:** registrar actor, acción, entidad, fecha, resultado y cambios permitidos de cada operación crítica, sin contraseñas/tokens. **Precondiciones:** operación auditada. **Flujo:** crear evento en transacción correspondiente → restringir consulta por permiso. **Alternos:** fallo de auditoría en transición de flujo revierte transición; repetición idempotente no duplica. **Postcondiciones:** evento reconstructible o operación no aplicada cuando es crítico. **Reglas:** RB-002/020. **Datos:** AuditLog, entidad afectada. **Aceptación:** cada transición efectiva de RF-026–029 tiene un evento con actor/estados; evento no contiene credencial/token. **Dependencias:** ADR-011, política de retención [PENDIENTE_VALIDACION]. **Fuente:** PRD RF-015/RN-005; Cap. III RF-22; ADR-007/011; roadmap §19.

### RF-033 — Guardar preferencias de accesibilidad visual

Estado: [PROPUESTA], [NO_IMPLEMENTADO]. Prioridad: Alta. Objetivo: OE2/OE3/OE6. Actor principal: paciente o usuario de interfaz. Secundario: sistema.

**Descripción:** permitir seleccionar tema sistema/claro/oscuro/alto contraste y modo estándar/protanopia/deuteranopia/tritanopia en web y móvil, con retorno a estándar. **Precondiciones:** interfaz disponible; sesión si se guarda por cuenta. **Flujo:** elegir preferencia → aplicarla → persistir por usuario/dispositivo según diseño aprobado. **Alternos:** opción no soportada vuelve a estándar sin ocultar estados. **Postcondiciones:** preferencia activa; significado sigue disponible en texto/icono. **Reglas:** RB-023. **Datos:** preferencia visual no clínica, usuario/dispositivo. **Aceptación:** estado de cita se entiende sin color; preferencia se conserva según mecanismo definido y puede restablecerse. **Dependencias:** diseño UX y persistencia por canal [PENDIENTE_VALIDACION]. **Fuente:** `docs/00` §§6.1, 14.3.1; `docs/04` §2; no tiene ID explícito en las dos series antiguas.

## 4. Requisitos no funcionales canónicos propuestos

**Clasificación de cifras:** toda cifra en este apartado es `META_ACADÉMICA` para un prototipo sintético, nunca `LINEA_BASE_REAL`. Los umbrales propuestos de Capítulo III requieren aprobación del protocolo y reporte de hardware/red/dataset antes de usarse como criterio de aceptación. Cuando no hay cifra, el criterio verificable es una revisión o prueba definida, y la cuantificación institucional es `PENDIENTE_VALIDACION`.

### RNF-001 — Seguridad de identidad y autorización

Categoría: seguridad. Objetivos: OE3/OE4. **Requisito:** hash de credenciales, HTTPS en entorno publicado, permisos y propiedad comprobados en servidor. **Métrica:** 100 % de casos no autorizados definidos en suite rechazados; cero contraseñas/tokens en logs (`META_ACADÉMICA`). **Método:** pruebas de credenciales, roles, BOLA/IDOR y revisión de logs. **Entorno:** backend y clientes de ensayo, datos sintéticos. **Fuente:** PRD RNF-001/002; Cap. III RNF-01; roadmap §19; ADR-003/005. **Observaciones:** la suite/casos deben inventariarse antes de afirmar 100 %; rol institucional final [PENDIENTE_VALIDACION].

### RNF-002 — Privacidad y minimización

Categoría: privacidad. Objetivos: OE3/OE4/OE6. **Requisito:** usar datos sintéticos y exponer por canal/rol solo los campos necesarios; no guardar datos clínicos fuera del alcance. **Métrica:** cero datos personales reales o secretos en dataset, respuestas de ensayo y logs revisados (`META_ACADÉMICA`); matriz campo↔rol pendiente. **Método:** revisión de datos, API, UI, logs y control por objeto. **Entorno:** desarrollo, pruebas y demostración académica. **Fuente:** PRD RNF-003; Cap. III RNF-02; roadmap §§1, 6.2, 19. **Observaciones:** retención, consentimiento y política institucional [PENDIENTE_VALIDACION].

### RNF-003 — Rendimiento de disponibilidad y reserva

Categoría: rendimiento. Objetivos: OE3/OE5. **Requisito:** responder sin degradación crítica en consultas y reservas bajo carga de ensayo reproducible. **Métrica:** p95 ≤ 3 s por operación, 50 usuarios virtuales, 10 min (`META_ACADÉMICA`). **Método:** prueba de carga con latencia, tasa de error y configuración. **Entorno:** staging académico, hardware/red/dataset registrados. **Fuente:** PRD RNF-005; Cap. III RNF-03. **Observaciones:** no es tiempo real ni capacidad del hospital; umbral sujeto a aprobación.

### RNF-004 — Disponibilidad del prototipo

Categoría: disponibilidad. Objetivo: OE5. **Requisito:** mantener el servicio de ensayo accesible y registrar interrupciones. **Métrica:** ≥99 % de sondeos satisfactorios, uno por minuto durante 8 h (`META_ACADÉMICA`). **Método:** health checks con timestamps y cálculo explícito. **Entorno:** staging académico con dependencias declaradas. **Fuente:** Cap. III RNF-04; `docs/11` §9; `docs/12` §4. **Observaciones:** no es SLA institucional/productivo.

### RNF-005 — Usabilidad de reserva

Categoría: usabilidad. Objetivos: OE3/OE5/OE6. **Requisito:** permitir que pacientes del ensayo completen reserva con mensajes de error comprensibles. **Métrica:** ≥80 % completa sin ayuda (`META_ACADÉMICA`). **Método:** tareas observadas, protocolo, muestra y criterio de éxito publicados. **Entorno:** prototipo web/móvil y participantes de ensayo autorizados. **Fuente:** PRD RNF-006; Cap. III RNF-05; `docs/04` §11. **Observaciones:** muestra y reclutamiento [PENDIENTE_VALIDACION]; no representa satisfacción hospitalaria.

### RNF-006 — Accesibilidad

Categoría: accesibilidad. Objetivos: OE2/OE3/OE6. **Requisito:** seguir WCAG 2.2 AA como referencia en web y criterios equivalentes aplicables en móvil; formularios etiquetados, foco visible, teclado, lector de pantalla, texto/icono además de color y preferencias. **Métrica:** flujo crítico completado por teclado en web; cero bloqueos críticos encontrados en revisión de lector de pantalla y contraste (`META_ACADÉMICA`); matriz de criterios WCAG pendiente. **Método:** revisión manual y automatizada por vista crítica, simulaciones de visión de color. **Entorno:** navegadores/dispositivos declarados. **Fuente:** PRD RNF-007; Cap. III RNF-06; roadmap §14.3.1. **Observaciones:** “accesibilidad básica” del PRD queda superada por el roadmap; no afirmar conformidad total sin auditoría.

### RNF-007 — Compatibilidad de canales

Categoría: compatibilidad. Objetivos: OE3/OE6. **Requisito:** mantener reserva y consulta en web y app móvil React Native/Expo previstas. **Métrica:** flujos críticos pasan en Chrome, Edge, Firefox y al menos un entorno móvil registrado (`META_ACADÉMICA`). **Método:** matriz de versión de navegador/OS/dispositivo y resultados. **Entorno:** versiones fijadas al ensayo. **Fuente:** PRD RNF-010; Cap. III RNF-07; roadmap §14.1. **Observaciones:** versiones y dispositivos [PENDIENTE_VALIDACION]; Flutter del TRD no gobierna.

### RNF-008 — Mantenibilidad modular

Categoría: mantenibilidad. Objetivos: OE2/OE3. **Requisito:** organizar backend por funcionalidad con límites entre módulos, contratos y migraciones versionadas. **Métrica:** revisión de dependencias/contratos/migraciones completada y pruebas pertinentes del componente modificado ejecutadas (`META_ACADÉMICA` de proceso). **Método:** revisión de arquitectura y evidencia CI por cambio. **Entorno:** repositorio y pipeline académico. **Fuente:** PRD RNF-008/009/013; Cap. III RNF-08; roadmap §9; ADR-001. **Observaciones:** “permitir futuros módulos” es criterio de diseño, no promesa de implementarlos.

### RNF-009 — Escalabilidad evaluable

Categoría: escalabilidad. Objetivos: OE3/OE5. **Requisito:** medir comportamiento del núcleo compartido antes de proponer escala adicional. **Métrica:** comparar 10, 25 y 50 usuarios virtuales con latencia, errores y recursos (`META_ACADÉMICA`). **Método:** perfiles de carga reproducibles. **Entorno:** staging académico fijo. **Fuente:** PRD RNF-009; Cap. III RNF-09; roadmap §20. **Observaciones:** umbral de degradación aceptable [PENDIENTE_VALIDACION]; no certifica producción.

### RNF-010 — Trazabilidad de acciones críticas

Categoría: trazabilidad y seguridad. Objetivos: OE3/OE4/OE5. **Requisito:** reconstruir cambios críticos con actor, acción, entidad y fecha, protegiendo el registro. **Métrica:** 100 % de transiciones críticas ensayadas reconstruibles y sin duplicados por repetición idempotente (`META_ACADÉMICA`). **Método:** comparar operaciones de suite con AuditLog e historial. **Entorno:** base de ensayo con datos sintéticos. **Fuente:** PRD RNF-004/011; Cap. III RNF-10; ADR-007/011. **Observaciones:** inventario total de eventos y retención [PENDIENTE_VALIDACION].

### RNF-011 — Integridad transaccional

Categoría: integridad/concurrencia. Objetivos: OE3/OE5. **Requisito:** impedir reservas activas duplicadas y cambios parciales de reprogramación u oferta. **Métrica:** 20 solicitudes concurrentes por un cupo → una reserva; reprogramación fallida conserva original (`META_ACADÉMICA`). **Método:** pruebas de concurrencia e inspección de restricciones/transacciones. **Entorno:** PostgreSQL de integración con migraciones vigentes. **Fuente:** Cap. III RNF-11; PRD RF-010/RN-001; roadmap §12.3. **Observaciones:** verificar además aceptación simultánea de ofertas.

### RNF-012 — Respaldo y recuperación

Categoría: recuperación. Objetivo: OE5. **Requisito:** disponer de respaldo y restauración verificable del entorno académico. **Métrica:** restauración en base aislada y verificación de conteos/relaciones/muestras (`META_ACADÉMICA`); RTO/RPO `PENDIENTE_VALIDACION`. **Método:** ensayo documentado de restauración. **Entorno:** PostgreSQL académico aislado. **Fuente:** Cap. III RNF-12; `docs/11` §13; `docs/12` §§10, 15. **Observaciones:** no asumir continuidad hospitalaria real.

### RNF-013 — Observabilidad técnica

Categoría: observabilidad. Objetivos: OE3/OE5. **Requisito:** registrar fallos técnicos y salud del servicio sin secretos ni datos sensibles, distinguibles de auditoría de negocio. **Métrica:** cada fallo inyectado de la suite produce evento diagnóstico rastreable y cada health check responde conforme a su estado (`META_ACADÉMICA`, suite por definir). **Método:** inyección controlada de fallos y revisión de logs/health checks. **Entorno:** staging académico. **Fuente:** PRD RNF-012; roadmap §23; `docs/12` §§4, 6. **Observaciones:** se conserva aunque no tenga par directo en Cap. III; umbrales de alerta [PENDIENTE_VALIDACION].

## 5. Cobertura de objetivos

“Cobertura” indica que existe requisito **documental** suficiente para planear una verificación; no significa que el objetivo se haya cumplido.

| Objetivo | RF | RNF | Cobertura | Motivo |
|---|---|---|---|---|
| OE1 | Ninguno de software | RNF-002/010 solo como restricciones | PARCIAL | Requiere evidencia AS-IS externa a la SRS; no se inventa RF |
| OE2 | RF-009–014, RF-033 como insumos de diseño | RNF-001–013 | PARCIAL | TO-BE, datos, UX/API y aprobación de políticas aún no cerrados |
| OE3 | RF-001–033 | RNF-001–011, 013 | PARCIAL | Alcance cubierto; implementación y reglas condicionadas |
| OE4 | RF-002–004, 021–022, 030, 032 | RNF-001/002/010/011 | PARCIAL | Falta matriz de minimización por campo/rol y validación de controles |
| OE5 | RF-012–020, 023–025, 031–032 | RNF-003–005/009–013 | PARCIAL | Sin línea base ni protocolo aprobado; NO_SHOW no medible |
| OE6 | RF-008–010, RF-033 | RNF-002/005–007 | PARCIAL | Contenido público y prototipo no aprobados |

**Propuestas estrictamente necesarias:** RF-033 explicita preferencias exigidas por `docs/00` §14.3.1 sin ID previo. El futuro requisito de `NO_SHOW` se deja como [PROPUESTA PENDIENTE_VALIDACION] sin ID porque falta decidir si será evento, estado u otra clasificación; formularlo ahora como transición concreta inventaría dominio. OE1 necesita un **artefacto de levantamiento y validación**, no un RF de software (`docs/auditoria/AS-IS.md`).

## 6. Matriz de trazabilidad resumida

La matriz detallada y sus equivalencias están en `docs/auditoria/MATRIZ_TRAZABILIDAD_BASE.md` y `EQUIVALENCIAS_REQUISITOS.md`. Los nombres de caso de uso siguientes son **derivados textuales** de los RF, no diagramas creados.

| Objetivo | Requisito | Actor | Caso de uso derivado | Proceso | Entidades | Validación prevista |
|---|---|---|---|---|---|---|
| OE3/OE4 | RF-001–004; RNF-001/002 | Paciente/admin | Registrar, autenticar y autorizar | Identidad | User/Role/RefreshToken | Cuenta única; rechazo acceso ajeno |
| OE3/OE6 | RF-008–010/033; RNF-006/007 | Visitante/admin | Consultar portal y preferencias | Público | Specialty/contenido/preferencia | Contenido autorizado; navegación accesible |
| OE3/OE5 | RF-011–015; RNF-003/011 | Admin/paciente | Consultar y reservar | Agenda/citas | Schedule/Slot/Appointment | Una cita activa por slot |
| OE3/OE5 | RF-016–019/023 | Paciente/sistema | Confirmar, cancelar, reprogramar | Ciclo cita | Appointment/Slot/Notification | Transiciones, conservación ante fallo |
| OE3/OE5 | RF-020/024/025; RNF-011 | Paciente/sistema | Entrar en espera y aceptar oferta | Recuperación | WaitlistEntry/SlotOffer/Appointment | Consentimiento y adjudicación única |
| OE3/OE4 | RF-021/022/032; RNF-010 | Paciente/revisor | Solicitar y resolver prioridad | Prioridad | PriorityRequest/AuditLog | Decisión manual auditable |
| OE3/OE5 | RF-026–030/032; RNF-010 | Recepción/profesional | Registrar flujo operativo | Consulta externa propuesta | Appointment/AuditLog | Orden/ownership/idempotencia |
| OE4/OE5 | RF-031/032; RNF-010/013 | Admin/auditor | Consultar indicadores y eventos | Auditoría | Appointment/AuditLog | Cohortes y eventos reconstructibles |
| OE1/OE2 | Evidencia de discovery | Personal institucional autorizado | Validar AS-IS | Investigación | No aplica | MAPRO, observación/entrevista autorizada |

## 7. Control de cambios y aceptación de la SRS

Ningún RF afirma funcionamiento oficial del Hospital de Huaycán. Los cambios de política institucional, nuevo estado de cita/flujo o dato clínico requieren decisión documentada, actualización de RF/RNF/RB, trazabilidad y criterio de prueba antes de diseño/implementación. Esta versión se considera **consolidación propuesta**; su aprobación académica/institucional no se presume. Los documentos originales quedan como antecedentes con IDs preservados.

## 8. Comprobación editorial de esta versión

| Control | Resultado |
|---|---|
| Identificadores únicos | 33 encabezados RF-001–033 y 13 RNF-001–013, sin duplicados; 24 RB-001–024 en archivo separado |
| Actor, objetivo, aceptación y fuente de RF | Presentes en los 33 bloques RF; verificación estructural local |
| RNF verificable | Los 13 indican métrica/criterio, método y entorno; umbrales académicos aún requieren protocolo aprobado |
| Repetición funcional | Registro/login/sesión, constancia/confirmación, cancelación/liberación, oferta/respuesta y etapas de flujo se distinguen por resultado y regla |
| Alcance exclusivo | Capacidades clínicas antiguas se registran solo fuera del MVP; `NO_SHOW` está solo pendiente de decisión de dominio |
| Estados | Se conservan cinco `AppointmentStatus` y cuatro `FlowStage`, sin valor añadido |
| Evidencia institucional | No se incorpora tiempo, volumen, problema operativo ni línea base real no documentada |
| Validación de funcionamiento | No ejecutada; esta comprobación es documental y estructural |
