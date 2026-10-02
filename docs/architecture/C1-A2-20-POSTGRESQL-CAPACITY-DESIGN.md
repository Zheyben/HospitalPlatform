# C1A2-20 — Diseño de capacidad profesional en PostgreSQL 16

**Estado al 2026-09-30:** candidato C1A2-20.2 validado en PostgreSQL 16 aislado. No es una migración ni está integrado al backend principal. C1-B permanece sin autorización de implementación hasta aplicar y revisar la integración.

## Invariante de capacidad

Para un profesional y día semanal, dos horarios activos o protegidos no se solapan, aunque correspondan a especialidades distintas. Los intervalos son `[inicio, fin)`; horarios contiguos sí pueden existir. Un horario desactivado con reservas futuras conserva `capacity_protected=true` y bloquea una capacidad semanal equivalente hasta una liberación explícita y comprobada. Se preservan citas y slots históricos.

Cada slot dura exactamente 30 minutos, pertenece al día semanal y cae por completo dentro del horario. Un slot `RESERVED` tiene exactamente una cita ocupante (`SCHEDULED`, `CONFIRMED` o `COMPLETED`) al commit. `COMPLETED` conserva su slot histórico; una cita terminada y pasada no impide liberar la protección semanal de un horario desactivado.

El DDL completo y ejecutable del ensayo está en [C1-A2-20-CANDIDATE.sql](validation/C1-A2-20-CANDIDATE.sql). Añade `capacity_protected`, exclusión GiST de horarios y slots, duración de slot, índice único parcial de citas ocupantes, preflight de datos V4, triggers de dominio y comprobación diferida de coherencia cita/slot. Usa `btree_gist`; no crea tablas clínicas nuevas. `hospital_business_config` es una configuración singleton bajo el propietario del esquema, no una entidad de agenda. Las modificaciones futuras de este esquema requieren una nueva migración; V1–V4 no se alteran.

## Orden único de locks y gateway

El orden obligatorio para **todas** las escrituras sensibles es:

1. `pg_advisory_xact_lock(12020, 2)`, adquirido antes de cualquier lock de fila;
2. User vinculado, después Professional, cada grupo por UUID ascendente;
3. Schedule por UUID ascendente;
4. AvailabilitySlot por UUID ascendente;
5. Appointment por UUID ascendente.

El gateway de ensayo [C1-A2-20-GATEWAY.sql](validation/C1-A2-20-GATEWAY.sql) aplica ese orden a reserva, cancelación, reprogramación entre horarios, generación idempotente de slot, edición y desactivación de horario, desactivación profesional, deshabilitación de cuenta, confirmación, finalización y transiciones `CHECK_IN`/`WAITING`/`IN_ATTENTION`. Primero adquiere el gate transaccional; después lee identificadores, toma locks de User/Professional/Schedule/Slot/Appointment en el orden indicado y efectúa DML. La transacción externa conserva el gate hasta commit o rollback. Las funciones `SECURITY DEFINER` fijan `search_path=pg_catalog,public,pg_temp`; el schema `public` no admite `CREATE` del runtime.

La serialización global evita ciclos entre las rutas controladas incluso cuando un trigger consulta otra fila. Es intencionalmente conservadora: limita concurrencia entre profesionales independientes. Puede sustituirse más adelante por locks por profesional o capacidad, pero solo con una nueva prueba de la misma matriz y una demostración de orden global. **No se usa retry para ocultar `40P01`.**

El rol runtime de capacidad no tiene DML directo sobre `users`, `professionals`, `schedules`, `availability_slots` ni `appointments`; tampoco puede cambiar triggers, funciones, tablas ni zona. Se revoca `EXECUTE` público de funciones y se conceden únicamente las llamadas del gateway y lectura necesaria. El ensayo está en [C1-A2-20-RUNTIME-ROLE.sql](validation/C1-A2-20-RUNTIME-ROLE.sql). Es un perfil de privilegios **solo de capacidad**; los permisos de autorregistro, autenticación y demás módulos necesitan inventario separado antes de producción. El propietario de migraciones debe ser distinto del rol runtime. SQL directo con el rol runtime no puede invertir el orden, porque su DML sensible obtiene `42501`. Un propietario o superusuario queda fuera de esta barrera y requiere control operativo.

## Guardia de profesional y cuenta

Un trigger rechaza `professional.deleted_at` cuando existe una cita `CONFIRMED` de ese profesional en `CHECK_IN`, `WAITING` o `IN_ATTENTION`. Otro rechaza `user.enabled=false` o `user.deleted_at` para su cuenta vinculada en las mismas etapas. La transición hacia cada etapa toma y comprueba Professional y User; el gateway toma sus locks antes de la cita. Si la transición confirma primero, desactivar falla; si desactivar confirma primero, la transición falla. Se conservan los registros históricos.

Para reservar, el trigger verifica horario, profesional, especialidad, asociación y usuario profesional habilitados. La consulta PATIENT del prototipo Java aislado aplica los mismos filtros conocidos, además de `AVAILABLE`, futuro local y ausencia de cita ocupante, incluido `COMPLETED`. La autoridad final sigue siendo la transacción DB: una reserva concurrente puede invalidar un slot después de listarlo y debe convertirse en `409 SLOT_UNAVAILABLE`.

## Fecha civil de negocio

La única zona civil de negocio es `America/Lima`. `hospital_business_now()` devuelve `clock_timestamp() AT TIME ZONE 'America/Lima'`; la configuración solo acepta ese valor y el runtime no puede editarla. El prototipo Java del worktree de prueba usa `Clock.system(ZoneId.of("America/Lima"))` para `SlotGenerationService` y `AgendaService`. La consulta nativa de reserva compara la fecha/hora del slot con Lima, y la disponibilidad PATIENT recibe fecha/hora del mismo `Clock`. UTC queda reservado a instantes técnicos como emisión de JWT o marcas de auditoría; `hibernate.jdbc.time_zone: UTC` no redefine el día civil de un slot.

El borde comprobado: `2026-10-01T04:59Z` es `2026-09-30 23:59` en Lima; `2026-10-01T05:00Z` es `2026-10-01 00:00`. Java y PostgreSQL dieron el mismo resultado en el ensayo.

## Validación y límites de integración

La matriz obligatoria de nueve carreras, más cancelación frente a reserva en ambos órdenes, produjo **15 ejecuciones con dos sesiones y cero `40P01`**. Reserva doble dejó una sola cita ocupante. Las transiciones y la desactivación profesional nunca dejaron un profesional inactivo con atención en curso. El upgrade sintético V4, conflictos históricos, huérfano, finalización histórica y clean install se documentan en [el informe de validación](C1-A2-20-POSTGRESQL-VALIDATION-REPORT.md). El harness reproducible es [C1-A2-20-REMEDIATION-VALIDATE.py](validation/C1-A2-20-REMEDIATION-VALIDATE.py).

La aplicación principal todavía usa DML directo y `Clock.systemUTC()`; no puede recibir el rol restringido sin cambiar sus servicios. La implementación de C1-B debe integrar todas las rutas del gateway, conservar autorización de pacientes/administradores en Application Service, mapear los SQLSTATE al contrato HTTP y alinear Java en la rama principal. La prueba Java de esta etapa vive solo en un worktree aislado. No hay V5 y no se ha aplicado nada a la base principal.

### Referencias

- [PostgreSQL 16: constraints y exclusión](https://www.postgresql.org/docs/16/ddl-constraints.html)
- [PostgreSQL 16: bloqueo explícito](https://www.postgresql.org/docs/16/explicit-locking.html)
- [PostgreSQL 16: funciones `SECURITY DEFINER`](https://www.postgresql.org/docs/16/sql-createfunction.html)
