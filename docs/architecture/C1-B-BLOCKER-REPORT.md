# C1-B — Bloqueos tras el cierre de decisiones

> **Actualización C1-C, 2026-10-01:** la base local principal ya está en V5. Los bloqueos de corte descritos abajo son históricos. Véase [C1-C-V5-CUTOVER-REPORT.md](C1-C-V5-CUTOVER-REPORT.md).

## Actualización de implementación — 2026-10-01

La sección histórica de abajo corresponde al gate de fase 0 del 2026-09-30. **C1A2-20 ya está cerrado como `CLOSED — APPROVED FOR IMPLEMENTATION`** en el registro de decisiones. El diseño PostgreSQL 16 superó la validación aislada y se integró en V5, el gateway y los servicios. El estado verificable del código y de las pruebas se registra en [C1-B-IMPLEMENTATION-REPORT.md](C1-B-IMPLEMENTATION-REPORT.md).

**No queda un bloqueo de diseño para C1-B.** Sigue pendiente el corte de la base principal: se leyó en modo `READ ONLY`, conserva V1–V4 y sus datos incompatibles, y V5 no se ejecutó allí. La validación V4→V5 utilizó una base sintética aislada con la correspondencia de [C1-B-DATA-PREFLIGHT.md](C1-B-DATA-PREFLIGHT.md). La base principal también conserva 0 ADMIN activos y la cuenta profesional de demo deshabilitada; ambas condiciones requieren una decisión operativa antes de usarla como entorno de demostración. **GO para la implementación validada en aislamiento; NO GO para afirmar que la base principal ya está migrada.**

---

**Fecha:** 2026-09-30 (America/Bogota)  
**Estado:** fase 0 repetida; **NO GO**. No se modificaron datos, código ni migraciones.

## Decisiones cerradas

| ID | Cierre aprobado | Consecuencia para C1-B |
|---|---|---|
| C1A2-03 | Identidad por `UNIQUE(document_type, document_number)`; tipos `DNI`, `CE`, `PASSPORT`; minoría de DNI derivada de nacimiento. | El índice global de V1 deberá sustituirse solo en futura migración validada. La misma cifra en DNI y CE podrá coexistir. |
| C1A2-19 | Los 14 DNI y `DEMO-CMP-0001` se corrigen mediante correspondencia documentada PK → valor sintético válido y único en migración trazable. | Se conservan UUID, relaciones, citas e historia; no hay limpieza manual. |
| DEC-010 / C1A2-21 | `CONFIRM`, `CANCEL` y `RESCHEDULE` ordinarios se rechazan en citas pasadas. | No se agregan ventanas temporales ni excepciones ADMIN en C1-B. Otras políticas de DEC-010 quedan fuera del alcance, sin bloquear esta regla acotada. |
| C1A2-22 | Cuenta profesional deshabilitada no opera, recibe nuevas reservas ni inicia atención; perfil e historial permanecen. | El seed de desarrollo tendrá que habilitar la cuenta profesional ofrecida en disponibilidad. |
| C1A2-23 | No se desactiva/revoca profesional con atención en `CHECK_IN`, `WAITING` o `IN_ATTENTION`; especialidad inactiva no admite asociaciones, horarios operativos ni reservas nuevas, pero citas existentes pueden finalizar. | Se preservan citas e historia y se añaden guardias cuando se autorice implementación. |

## Bloqueo restante

| ID | Evidencia actual | Para cerrar el gate |
|---|---|---|
| **C1A2-20 — OPEN** | V1 no evita solapamiento entre schedules; V3 protege solo ocupación por `slot_id`. La exclusión limitada a `active` permite que un schedule desactivado con reserva futura sea solapado por otro schedule activo. | Revisar y aprobar el diseño de [capacidad PostgreSQL 16](C1-A2-20-POSTGRESQL-CAPACITY-DESIGN.md), después probar DDL, backfill, SQL directo, privilegios y carreras en copia aislada. Solo entonces reconsiderar C1-B fase 1. |

El diseño propone exclusión GiST por profesional/día/intervalo, protección persistente del horario
al desactivar, liberación guardada cuando cesa ocupación futura, exclusión de slots, unicidad de
ocupación incluyendo `COMPLETED`, triggers y locks para DML directo. **Es una propuesta:** no se
aplicó ni se demostró todavía. El rol de runtime separado del propietario de esquema y la fuente
de zona IANA en PostgreSQL forman parte de la validación pendiente.

## Preflight repetido

PostgreSQL 16.15, V1–V4, 15 usuarios, 14 pacientes, 1 profesional, 1 especialidad, 7 horarios,
84 slots y 18 citas. Se confirmaron 14 DNI incompatibles, una colegiatura incompatible,
0 ADMIN activos, 1 cuenta profesional vinculada deshabilitada, 0 solapes entre horarios activos
y 0 horarios inactivos con `RESERVED` futuro al instante de consulta. `btree_gist` no está
instalado. Todas las consultas se hicieron en transacciones `READ ONLY` terminadas en `ROLLBACK`.
El preflight no sustituye pruebas de concurrencia ni hace válidos los datos incompatibles.

## Gate

**NO GO para C1-B fase 1.** Ya no queda una decisión de identidad/cuenta/ciclo de cita abierta
para el alcance indicado. Solo C1A2-20 mantiene bloqueada la implementación. La elaboración de
la correspondencia por PK, los cambios de seed/código y V5 son trabajo posterior y tampoco están
autorizados por el encargo actual. No hubo commit, push ni merge.
