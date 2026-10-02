# C1-B — Auditoría previa a la implementación

## Actualización — 2026-10-01

Este documento conserva la fotografía previa a C1-B. El `NO GO` que aparece más abajo era correcto el 2026-09-30, pero C1A2-20 fue después validado y aprobado para implementación. Se creó V5 y se integraron backend, frontend y pruebas según [C1-B-IMPLEMENTATION-REPORT.md](C1-B-IMPLEMENTATION-REPORT.md). Los datos reales del PostgreSQL principal **no** se cambiaron; V5 se probó con instalación limpia y upgrade sintético V4→V5. La correspondencia de valores sintéticos está en [C1-B-DATA-PREFLIGHT.md](C1-B-DATA-PREFLIGHT.md).

---

**Fecha:** 2026-09-30 (America/Bogota)  
**Estado:** fase 0 repetida después del cierre de decisiones del 2026-09-30; **NO GO** por C1A2-20.  
**Alcance:** solo lectura de repositorio y PostgreSQL; actualización documental. No se modificaron Java, SQL, datos, frontend ni migraciones.

## Fuentes y autoridad

Se cotejaron el cierre C1-A.2 comunicado por el usuario, el addendum de `docs/DOMAIN-DECISION-REGISTER.md`, las migraciones V1–V4, entidades, servicios, repositories, tests, seed y configuración existentes. Se repitió el preflight de PostgreSQL con transacciones `READ ONLY` y `ROLLBACK`. C1A2-03, 19, 21, 22 y 23 ya tienen política aprobada. C1A2-20 sigue `OPEN` hasta que su diseño se apruebe y se valide en PostgreSQL 16. El encargo actual permite únicamente repetir fase 0.

El inventario B.1.1 del registro conserva descripciones históricas anteriores a B1/B2: DEC-017 y DEC-008 no describen el código actual de registro y generación de slots. El addendum C1-A.2 es la autoridad para C1-B; código, V1–V4 y PostgreSQL representan la implementación actual.

## Decisiones C1-A.2 aplicables

| IDs | Regla cerrada | Brecha actual |
|---|---|---|
| C1A2-01/02/03/04 | `DNI`, `CE`, `PASSPORT` persistidos; minoría de DNI derivada de edad menor de 18; identidad única por `(document_type, document_number)`; corrección documental solo ADMIN | V1 impone `UNIQUE(document_number)` global y columnas libres; DTO/service aceptan otros formatos; frontend ofrece `CC` y etiqueta CE incorrecta. La sustitución del índice es futura, no se ejecutó. |
| C1A2-05/06 | Seguro libre obligatorio en autorregistro; colegiatura textual de 4–6 dígitos | Seguro ya se exige en registro. Licencia admite hasta 100 caracteres y el seed usa `DEMO-CMP-0001`. |
| C1A2-07 | Schedule debe referir una pareja profesional–especialidad existente y activa | V1 tiene dos FK separadas; `AgendaService` solo comprueba profesional activo. |
| C1A2-09/10/11/20 | Horarios activos no solapados por profesional, intervalo `[start,end)`; desactivación conserva reservas y protege capacidad futura; edición estructural no destruye historia. C1A2-20 sigue abierto en diseño técnico. | No hay constraint temporal, protección de horarios inactivos ni reconciliación de slots en `updateAgenda`. El diseño candidato está en `C1-A2-20-POSTGRESQL-CAPACITY-DESIGN.md` y no está aprobado/probado. |
| C1A2-12/13/14/21 | Slots de 30 minutos; COMPLETED consume capacidad; no cancelar/reprogramar desde CHECK_IN/WAITING/IN_ATTENTION; no confirmar/cancelar/reprogramar citas pasadas. Sin ventanas adicionales ni excepciones ADMIN en C1-B. | V3 excluye COMPLETED del índice único; `AppointmentService` no valida etapa para cancelar/reprogramar ni pasado en las tres transiciones. |
| C1A2-15/16/17/18/22/23 | Hora civil configurable `America/Lima`; último ADMIN, perfiles y citas en curso protegidos; cuenta profesional deshabilitada sin nuevas operaciones/reservas/atenciones; especialidad inactiva sin nuevas asociaciones/horarios/reservas, pero citas existentes pueden terminar; IDs derivados/verificados por servidor. | Comparaciones UTC, `UserService.assignRoles/updateStatus` sin todas las guardias, desactivación incompleta y seed con cuenta profesional deshabilitada. La reserva PATIENT sí deriva paciente y profesional. |
| C1A2-19 | Sustitución trazable en futura migración por correspondencia PK → valor sintético válido y único, conservando UUID, relaciones, citas e historia. | La política está aprobada; aún no existen la correspondencia por fila ni la migración. Prepararlas es fase posterior, prohibida ahora. |

## Archivos y tablas afectados si se autoriza C1-B

| Área | Archivos principales | Tablas |
|---|---|---|
| Identidad/registro | `auth/dto/RegisterPatientRequestDTO.java`, `auth/service/PatientRegistrationService.java`, `patients/{dto,service,repository,entity}`, `users/{service,repository,entity}`, `apps/frontend/app/register/page.tsx` | `users`, `patients`, `user_roles` |
| Profesionales/catálogo | `professionals/{dto,service,repository,entity}`, `agenda/demo/DevDemoDataSeeder.java` | `professionals`, `specialties`, `professional_specialties` |
| Agenda/slots | `agenda/{dto,service,repository,entity,controller}`, en especial `AgendaService`, `SlotGenerationService`, `ScheduleRepository`, `AvailabilitySlotRepository` | `schedules`, `availability_slots`, `professional_specialties` |
| Citas/seguridad | `appointments/{dto,service,repository,entity,controller}`, `users/service/UserService.java`, `security/filter/JwtAuthenticationFilter.java` | `appointments`, `availability_slots`, `users`, `user_roles`, `patients`, `professionals` |
| Esquema | Nueva `database/migrations/V5__*.sql` solo tras cerrar bloqueos | Tablas anteriores e índices/constraints nuevos; V1–V4 intocables |

## PostgreSQL y datos incompatibles

Lectura repetida el 2026-09-30 dentro de `BEGIN TRANSACTION READ ONLY` y `ROLLBACK` al contenedor `hospital-platform-postgres-1`: PostgreSQL **16.15**, timezone técnico UTC, V1–V4 aplicadas, **15 users, 14 patients, 1 professional, 1 specialty, 7 schedules, 84 slots (66 AVAILABLE, 18 RESERVED) y 18 appointments SCHEDULED**. Los **14 DNI** incumplen ocho dígitos; la única colegiatura `DEMO-CMP-0001` incumple 4–6 dígitos. Hay **0 ADMIN activos**, **1 profesional vinculado a cuenta deshabilitada**, 0 pares `(document_type, document_number)` duplicados, **0 horarios activos solapados** y **0 horarios inactivos con slots RESERVED futuros** al instante de la lectura. `btree_gist` no está instalado y `capacity_protected` no existe. La lectura previa también encontró 0 asociaciones schedule inválidas y 0 slots de duración distinta de 30 minutos. Esto es un preflight, no una prueba de carrera ni autorización para UPDATE/DDL.

La correspondencia documentada PK → valor sintético único de los 14 pacientes y el profesional se preparará en fase posterior como parte de una migración trazable. No se crea `C1-B-DATA-PREFLIGHT.md` ahora: el encargo prohíbe fase 1 y cambios de datos.

## Tests y fixtures afectados

`apps/backend/src/test/java/com/hospital/platform/auth/PatientRegistrationIT.java`, `agenda/PatientAvailabilityFlowIT.java`, `appointments/AppointmentPersistenceIT.java`, `appointments/AppointmentLifecycleIT.java`, tests de `PatientService`, `ProfessionalService`, `AgendaService`, `SlotGenerationService`, `AppointmentService`, autorización de controllers y `apps/frontend/tests/patient-core.spec.ts`. Los fixtures contienen documentos `B2-*` y colegiaturas `CMP-*`; el seed usa `DEMO-CMP-0001`. Una V5 restrictiva sobre instalación limpia rompería pruebas/seed si no se actualizan antes. No se ejecutaron tests: no hubo implementación.

## Riesgos y migración necesaria

1. **Único bloqueo de decisión: C1A2-20.** Una exclusión `WHERE active` no protege reservas futuras de schedules desactivados. El candidato de protección por horario debe aprobarse y demostrarse en PostgreSQL 16, con DML directo y carreras.
2. **Trabajo de implementación ya decidido, no bloqueo de política:** sustituir global `UNIQUE(document_number)` por unicidad compuesta y ajustar DTO/service/frontend; mapear por PK los 14 DNI y la licencia antes de CHECK restrictivos; preservar citas/slots históricos; alinear reloj de negocio y seed profesional habilitado.
3. **Gate técnico previo a V5:** demostrar con copia aislada que las constraints, triggers, locks, privilegios y backfill propuestos cubren la carrera desactivación–reserva y todos los caminos directos. No se ha hecho esta prueba.

## Orden de implementación condicionado

1. Obtener aprobación y prueba PostgreSQL 16 del diseño C1A2-20; mantener C1-B detenido hasta entonces.
2. Tras una autorización posterior para fase 1, publicar `C1-B-DATA-PREFLIGHT.md` con PK, problema, regla y sustitución sintética única por fila.
3. Preparar código, fixtures y seed compatibles sin aplicar constraints destructivos.
4. Aplicar cleanup reproducible y trazable, sin cambiar IDs ni relaciones.
5. Validar datos y diseñar/probar DDL PostgreSQL 16 de V5 en una copia aislada.
6. Crear V5 solo con decisiones cerradas; probar constraints, concurrencia y servicios.
7. Probar upgrade V4→V5 con copia representativa y, por separado, instalación limpia V1→V5 con seed.
8. Ejecutar `mvnw.cmd verify` y E2E PATIENT real, documentar resultados en `C1-B-IMPLEMENTATION-REPORT.md`.

**Upgrade V4→V5:** primero inventario, limpieza aprobada y verificación de duplicados/relaciones; luego V5 con ventana de DDL y rollback operativo preparado. **Instalación limpia V1→V5:** migraciones en orden, Hibernate `ddl-auto: validate`, seed compatible y tests de registro/reserva. Flyway empaqueta `database/migrations` desde `apps/backend/pom.xml`. `docker-compose.yml` usa PostgreSQL 16, healthcheck `pg_isready` y volumen persistente; secretos provienen de variables de entorno, sin registrarlos aquí.

## Gate

**NO GO.** Fase 0 repetida y C1-B detenido exclusivamente por C1A2-20, pendiente de aprobación y validación técnica. C1A2-03/19/21/22/23 quedaron cerradas en su alcance. Este encargo tampoco autoriza fase 1 aunque cambie el gate posteriormente. No se inició cleanup, V5, Java, frontend, pruebas de regresión ni E2E.
