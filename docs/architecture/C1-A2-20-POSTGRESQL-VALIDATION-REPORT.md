# C1A2-20.2 — PostgreSQL capacity design validation report

**Fecha:** 2026-09-30 (America/Bogota)  
**Ámbito:** PostgreSQL 16.15 en contenedor desechable; copia sintética V4 y worktree Java aislado. La base principal no se conectó ni se modificó. No se creó V5 ni se hizo commit, push o merge.

## Resultado del candidato

🟡 **C1A2-20 DESIGN VALIDATED WITH NON-BLOCKING OBSERVATIONS.** Los tres bloqueadores de C1A2-20.1 se resolvieron **en el candidato aislado**: cero `40P01` en 15 carreras de dos sesiones; ninguna desactivación profesional dejó `CHECK_IN`, `WAITING` o `IN_ATTENTION` en curso; Java de prueba y SQL usaron `America/Lima` en el borde de día. Este resultado aprueba el diseño técnico del candidato, **no** autoriza desplegarlo ni usar el rol restringido con el backend principal actual. C1-B continúa **NO GO para FASE 1** hasta aprobar la integración planificada.

## Orden de locks y gateway

El orden global es gate transaccional `pg_advisory_xact_lock(12020,2)` → User → Professional → Schedule(s) por UUID → Slot(s) por UUID → Appointment(s) por UUID. El gate se adquiere antes de locks de fila, incluso en reprogramación entre horarios. [C1-A2-20-GATEWAY.sql](validation/C1-A2-20-GATEWAY.sql) contiene las operaciones sensibles, incluida finalización y generación idempotente. Reserva bloquea Schedule antes de Slot antes de insertar Appointment. Cancelación y reprogramación bloquean Schedule, Slot y Appointment en el mismo orden; las dos agendas de una reprogramación cruzada se ordenan por UUID. El gate global reduce paralelismo; no hubo retry automático de deadlocks.

Se eligió gateway PostgreSQL porque el backend actual bloquea `Appointment` antes de `Slot` en varias rutas y el `UPDATE` directo de slots podía invertir `Slot → Schedule`. La alternativa de solo locks Java no protege DML SQL directo si el rol conserva `UPDATE` sobre tablas críticas. [C1-A2-20-RUNTIME-ROLE.sql](validation/C1-A2-20-RUNTIME-ROLE.sql) niega ese DML, la alteración de tablas/triggers/funciones y la configuración de zona; solo concede lectura y `EXECUTE` de las operaciones controladas. El runtime tampoco pudo invocar el helper interno ni leer la tabla de zona (`42501`). La primera ejecución con privilegios mínimos detectó que el trigger diferido invocaba una función auxiliar como runtime y obtuvo `42501`; se corrigieron ambas funciones de coherencia con `SECURITY DEFINER` y `search_path` fijo. La matriz completa posterior pasó.

## Guardia Professional y User

El candidato rechaza por SQL directo la desactivación lógica de Professional o de su User vinculado cuando una cita `CONFIRMED` está en `CHECK_IN`, `WAITING` o `IN_ATTENTION`. También impide entrar a esas etapas si Professional está inactivo o User deshabilitado. Las seis carreras etapa/desactivación en ambos órdenes dejaron cero profesionales inactivos con atención en curso. Una cuenta profesional deshabilitada hizo fallar una nueva reserva; la consulta PATIENT del Java aislado dejó de mostrar esos slots.

## Fecha civil y disponibilidad

El trigger de `hospital_business_config` fija la única zona a `America/Lima`; incluso el propietario no pudo cambiarla a `UTC` (`22023`), y runtime no tiene permiso de escritura (`42501`). `hospital_business_now()` usa esa configuración. El Java del worktree de prueba cambió su `Clock` de UTC a Lima, la consulta nativa de reserva compara contra Lima, y la disponibilidad filtra usuario profesional habilitado y citas `COMPLETED`. `SlotGenerationService` y `AgendaService` ya leen del `Clock` inyectado, por lo que ambos pasan a la misma fecha civil.

Se comprobaron `2026-10-01T04:59Z → 2026-09-30 23:59 Lima` y `2026-10-01T05:00Z → 2026-10-01 00:00 Lima` en Java y PostgreSQL. La integración Testcontainers de disponibilidad PATIENT validó registro, login, consulta, reserva, doble reserva y el caso de profesional deshabilitado: al deshabilitarlo, el slot desapareció y el POST obtuvo `409 SLOT_UNAVAILABLE`. Ese test corre sobre V1–V4 con el Java de prueba; el candidato SQL y el rol se probaron por separado en PostgreSQL 16. No se ha afirmado un E2E HTTP del gateway integrado.

## Carreras PostgreSQL 16

| Caso | Resultado observado |
|---|---|
| reserve vs reserve | Una cita; segundo intento `SLOT_UNAVAILABLE`; 0 deadlocks. |
| reserve vs deactivate Schedule, ambos órdenes | Reserva previa deja horario protegido; desactivación previa rechaza reserva; 0 deadlocks. |
| reserve vs edit Schedule | Edición estructural con slots rechazada; 0 deadlocks. |
| cancel vs deactivate Schedule | Slot liberado, horario desactivado; 0 deadlocks. |
| cancel vs reserve, ambos órdenes | Una cita ocupante o reserva rechazada y slot libre; 0 deadlocks. |
| reschedule A→B vs B→A | Ambas reprogramaciones confirmadas; 0 deadlocks. |
| generación vs edición | El slot queda dentro del horario; edición incompatible rechazada; 0 deadlocks. |
| CHECK_IN / WAITING / IN_ATTENTION vs deactivate Professional, ambos órdenes | Desactivación o transición rechazada según orden; 0 profesionales inactivos en atención; 0 deadlocks. |

**Total:** nueve tipos obligatorios más cancelación/reserva, 15 ejecuciones con dos conexiones PostgreSQL independientes; **0 SQLSTATE `40P01`**, sin retry. [C1-A2-20-REMEDIATION-VALIDATE.py](validation/C1-A2-20-REMEDIATION-VALIDATE.py) reproduce las pruebas sobre clones del contenedor desechable. El gate serializa estas rutas; futuras rutas de escritura sensible deberán pasar por él antes de asignar el rol runtime.

## Upgrade, clean install y `COMPLETED`

- Sobre V4 sintético válido, el candidato conservó UUID y cita; zona `America/Lima` presente.
- Un horario histórico solapado provocó `23P01` y rollback completo. Un slot histórico incoherente provocó `23514` en preflight y rollback completo. No se corrigieron ni borraron datos reales.
- `COMPLETED` antes del inicio del slot fue rechazado por el gateway. Una cita histórica completada a través del gateway conservó `RESERVED`; después de pasar su slot, se liberó la protección de su horario desactivado y pudo crearse capacidad semanal nueva. Generar dos veces el mismo slot devolvió el mismo UUID y dejó una sola fila.
- Una instalación vacía V1→V4→candidato→gateway→grants completó sin errores. El jar del worktree arrancó en modo no web con `Hibernate ddl-auto: validate` contra esa base vacía y también contra la copia con fixture; ambos arranques terminaron con código 0. Flyway quedó deshabilitado para ese arranque porque el DDL experimental no es V5. El primer arranque web alcanzó Hibernate validate, pero Tomcat no pudo abrir un socket loopback en el entorno de ejecución; se repitió en modo no web para cerrar la verificación de esquema.

## Tests Java aislados

- `SecurityConfigurationTimeZoneTest`, `AgendaServiceTest`, `SlotGenerationServiceTest`: **14/14** aprobados.
- `PatientAvailabilityFlowIT` con PostgreSQL Testcontainers: **1/1** aprobado. La primera ejecución expuso una expectativa heredada de error (`PROFESSIONAL_NOT_AVAILABLE`) que cambió a `SLOT_UNAVAILABLE` al filtrar elegibilidad en la actualización condicional; se ajustó el test y se añadió comprobación de cuenta profesional deshabilitada.
- `mvn package -DskipTests`: correcto. El backend principal no recibió cambios Java.

## Riesgos y trabajo antes de C1-B

1. Integrar **todas** las escrituras sensibles del backend con el gateway, manteniendo autorización de negocio en los servicios y traducción de SQLSTATE/errores HTTP. El Java principal aún usa UTC y DML directo. No asignar todavía el rol restringido a ese backend.
2. Definir permisos completos del rol de aplicación para registro, autenticación y otros módulos. El script de rol ensayado cubre solo capacidad. Mantener el propietario de migraciones separado y `public` sin `CREATE` para runtime; revisar privilegios por defecto de funciones futuras.
3. Convertir el candidato en una migración nueva únicamente en una fase autorizada, con correspondencia trazable para datos reales incompatibles y preflight sobre copia representativa. No se probó V5 ni la cardinalidad real de la base principal.
4. El gate global limita throughput y representa una decisión conservadora. Si se sustituye, repetir toda la matriz sin retries.

Archivos de validación: [candidato SQL](validation/C1-A2-20-CANDIDATE.sql), [gateway](validation/C1-A2-20-GATEWAY.sql), [grants](validation/C1-A2-20-RUNTIME-ROLE.sql), [fixture](validation/C1-A2-20-FIXTURE.sql), [harness](validation/C1-A2-20-REMEDIATION-VALIDATE.py). Ninguno está aplicado a la base principal.
