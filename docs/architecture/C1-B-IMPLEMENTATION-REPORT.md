# C1-B — Informe de implementación de integridad de dominio

> **Actualización C1-C, 2026-10-01:** el corte de la base local principal a V5 ya se realizó y validó. La indicación de corte pendiente en este informe describe el estado anterior. Véase [C1-C-V5-CUTOVER-REPORT.md](C1-C-V5-CUTOVER-REPORT.md).

**Fecha:** 2026-10-01 (America/Bogota)  
**Estado:** implementación validada en bases PostgreSQL 16 aisladas; corte de la base principal pendiente.

## Decisiones y alcance

C1A2-20 figura en el [Domain Decision Register](../DOMAIN-DECISION-REGISTER.md) como **CLOSED — APPROVED FOR IMPLEMENTATION**. La aprobación del diseño y la integración del código son hitos distintos. La implementación usa el [diseño de capacidad](C1-A2-20-POSTGRESQL-CAPACITY-DESIGN.md) y su [validación](C1-A2-20-POSTGRESQL-VALIDATION-REPORT.md), sin alterar V1–V4. No se modificó Figma ni se añadieron módulos clínicos.

## Datos y migración

El preflight de la base principal fue de solo lectura: V1–V4, 15 usuarios, 14 pacientes, 1 profesional, 1 especialidad, 7 horarios, 84 slots y 18 citas. Encontró 14 DNI y una colegiatura incompatibles, sin solapes activos, reservas huérfanas ni asociaciones inválidas. La [correspondencia por UUID](C1-B-DATA-PREFLIGHT.md) asigna DNI sintéticos `90000001`–`90000014` y colegiatura sintética `900001`; conserva UUID, FK, citas e historial y falla si los valores fuente difieren. **No se aplicó a la base principal.**

La nueva [V5](../../database/migrations/V5__domain_integrity_hardening.sql) incorpora la correspondencia trazable, unicidad documental compuesta, validaciones de documento y colegiatura, normalización, restricciones de asociación profesional–especialidad, `btree_gist`, exclusión de intervalos `[inicio, fin)`, `capacity_protected`, protección de slots/citas históricas, funciones y triggers de capacidad, y revocación de ejecución pública de funciones sensibles. Los nuevos slots siguen durando 30 minutos. Se probaron instalación limpia V1→V5 y upgrade sintético V4→V5 con Flyway y `Hibernate validate`; se comprobaron UUID, citas y slots preservados.

## Backend y zona civil

Los DTO, servicios y entidad de paciente validan `DNI` (8 dígitos), `CE` (8–12 alfanuméricos) y `PASSPORT` (6–12 alfanuméricos); el número se normaliza por tipo. Se usa identidad `(document_type, document_number)`, y la minoría de edad se deriva de DNI y nacimiento. La colegiatura sigue siendo texto de 4–6 dígitos, con ceros iniciales. Email y username se canonizan de acuerdo con el dominio aprobado.

`AgendaService` verifica la asociación profesional–especialidad y su vigencia; el seed de desarrollo crea la cuenta profesional habilitada con colegiatura compatible. `UserService` protege al último ADMIN activo, perfiles vinculados y cuentas profesionales con atención en curso. Las operaciones ordinarias de confirmación, cancelación y reprogramación rechazan citas pasadas; cancelación/reprogramación rechazan `CHECK_IN`, `WAITING` e `IN_ATTENTION`. Una cita nueva sigue en `SCHEDULED`; una `COMPLETED` conserva la referencia histórica a su slot `RESERVED` sin proteger capacidad futura indefinidamente.

El [gateway PostgreSQL](../../apps/backend/src/main/java/com/hospital/platform/agenda/contract/PostgresCapacityGateway.java) coordina reservas, cancelaciones, reprogramaciones, cambios de horario y estado profesional mediante las funciones V5. Los servicios conservan autorización y ownership. El orden de adquisición comienza por el advisory transaction gate y continúa por filas de usuario/profesional, horario, slot y cita. Se retiraron los servicios antiguos de DML directo de reserva/liberación. La zona civil de negocio es `America/Lima` en el reloj Java, la configuración JDBC/Hibernate y las funciones SQL; JWT y timestamps técnicos conservan su semántica propia.

## Frontend

El registro usa `select` para DNI, Carné de Extranjería y Pasaporte y valida formato y longitud según el tipo. El backend y PostgreSQL siguen siendo la autoridad. El flujo de paciente existente conserva registro, login, disponibilidad, reserva y Mis citas sin datos simulados.

## Pruebas y evidencia

| Gate | Evidencia | Resultado |
|---|---|---|
| Migración | Testcontainers PostgreSQL 16, instalación limpia y fixture V4→V5; Flyway y `Hibernate validate` | Aprobado |
| Backend | `mvn verify` con zona JVM UTC para detectar dependencia de timezone de host | 148 unitarios + 69 integración = **217/217**, 0 fallos, 0 errores |
| Capacidad | [Harness](validation/C1-B-CAPACITY-VALIDATE.py) con 15 carreras de dos sesiones, incluyendo reserva simultánea, cambios de horario, cancelación/reprogramación y atención versus desactivación | 15/15, 0 `40P01` |
| Frontend | `npm run build` y `npm run test:e2e -- --reporter=line` con backend y PostgreSQL reales aislados | Build y 2/2 E2E aprobados |
| E2E PostgreSQL | Registro, login, disponibilidad, reserva, Mis citas y segundo intento; comprobación SQL de FK y estados | 201 `SCHEDULED`, slot `RESERVED`; segundo intento 409 `SLOT_UNAVAILABLE` |
| Rol runtime | Backend con Flyway/seed desactivados y [grants restringidos](validation/C1-B-APP-RUNTIME-ROLE.sql), después de la suite/E2E | Arranque, registro, login, disponibilidad, reserva, reprogramación y cancelación aprobados |

En el E2E aislado quedaron 2 citas `SCHEDULED`, 2 slots `RESERVED` y 2 joins coherentes entre paciente, profesional, horario y slot. La cuenta profesional ofrecida estaba habilitada. Las pruebas cubren documentos inválidos, documento/email duplicados, slot ocupado y autorización. En el rol runtime se comprobó ausencia de `INSERT` directo en `appointments` y de `UPDATE` directo en `availability_slots`/`schedules`; la aplicación operó a través de funciones `EXECUTE` concedidas explícitamente. Estos grants se validaron para el flujo PATIENT y el ciclo de cita probado, no para todos los endpoints administrativos futuros.

## Riesgos y corte pendiente

La base PostgreSQL principal continúa en V4 y conserva sus 14 DNI incompatibles, la colegiatura incompatible, 0 ADMIN activos y una cuenta profesional deshabilitada. V5 **no** se ejecutó allí. La migración sobre la base principal requiere un respaldo verificable y una ventana de corte; no debe confundirse el éxito del fixture sintético con haber migrado los datos principales. La copia íntegra de esa base hacia un contenedor temporal fue rechazada por la revisión automática por tratarse de datos sensibles sin autorización específica de destino. Se continuó la validación con fixtures sintéticos y consultas de solo lectura. El corte real permanece pendiente de un destino de respaldo autorizado y de la resolución operativa de ADMIN/seed.

## Comandos de verificación

Desde `apps/backend`, con Docker disponible para Testcontainers, ejecutar `./mvnw.cmd verify` en Windows. Desde `apps/frontend`, ejecutar `npm run build` y `npm run test:e2e`. El E2E requiere el PostgreSQL y backend reales configurados según el entorno de desarrollo. Los scripts SQL y el harness de validación se encuentran en `docs/architecture/validation/` y fueron ejecutados exclusivamente contra bases aisladas. No hubo commit, push ni merge.
