# C1-C — Corte local PostgreSQL V4→V5

**Fecha:** 2026-10-01 (America/Bogota)  
**Resultado:** 🟡 LOCAL V5 CUTOVER COMPLETE WITH OBSERVATIONS

## Respaldo previo

Se creó antes de cualquier escritura, con `pg_dump -Fc`, el archivo local [hospital_platform_pre_v5_20261001_005312.dump](../../.codex_tmp/backups/hospital_platform_pre_v5_20261001_005312.dump). Está fuera del contenedor PostgreSQL y `.codex_tmp/` está ignorado por Git. Fecha local: **2026-10-01 00:53:13 -05:00**. Tamaño: **51.554 bytes**. `pg_restore -l` leyó su índice sin error y SHA-256 coincidió entre el archivo del contenedor y el del host: `7018F9A8D56764E0B5652744142298174314F20EAB320A37C54CED16A26A2B3A`. No se ejecutó restauración porque el corte y las validaciones terminaron correctamente.

## Estado inicial y preflight

Consultas `READ ONLY` confirmaron Flyway **V1–V4 success**, sin V5, y PostgreSQL 16.15. Conteos antes del corte: **15 users, 14 patients, 1 professional, 1 specialty, 7 schedules, 84 availability_slots y 18 appointments**. Los 14 documentos DNI y la única colegiatura eran incompatibles con las reglas aprobadas. Había **0 ADMIN activos** y la cuenta vinculada al profesional del seed estaba deshabilitada. El preflight no encontró identidades documentales ni email/username normalizados duplicados, solapes de horarios activos, asociaciones inválidas, slots fuera de horario, `RESERVED` huérfanos ni citas con profesional distinto del horario. Los 18 appointments eran `SCHEDULED`; 18 slots estaban `RESERVED` y 66 `AVAILABLE`.

## Corrección controlada antes de V5

Con autorización explícita del usuario se creó una identidad **exclusivamente LOCAL/DEMO** `local-demo-admin` y su rol `ADMIN`. La contraseña aleatoria se guardó cifrada con DPAPI en `.codex_tmp/secrets/local-demo-admin.dpapi`, ignorado por Git; su hash en PostgreSQL es BCrypt. Se verificó login `200`, rol ADMIN y rechazo `409` al intentar deshabilitar al último ADMIN. El bootstrap local **no cierra DEC-003** ni define un procedimiento de producción. El archivo DPAPI se descifra solamente bajo el mismo contexto elevado de Windows usado para crearlo; no se publica la contraseña.

Se habilitó exclusivamente el usuario sintético `demo-professional` vinculado al professional `88743663-5b1b-3868-bf1a-aa0371adfac3`. Se comprobaron el nombre y email de demo antes del `UPDATE`; UUID y relaciones permanecieron. El backend anterior conectado a la base se detuvo antes de estos cambios.

Para recuperar la contraseña del ADMIN local en el mismo equipo, ejecutar PowerShell **como Administrator** desde la raíz del repositorio: `$s = Get-Content .codex_tmp/secrets/local-demo-admin.dpapi -Raw | ConvertTo-SecureString; [System.Net.NetworkCredential]::new('', $s).Password`. La salida se muestra solo en esa terminal local; no debe pegarse en el repositorio ni en el informe.

## Datos demo y Flyway

La correspondencia aprobada [PK → valor sintético válido](C1-B-DATA-PREFLIGHT.md) se aplicó **dentro de V5**: los 14 DNI demo incompatibles pasaron a `90000001`–`90000014` y `DEMO-CMP-0001` pasó a `900001`. El archivo enlazado documenta cada `BEFORE → AFTER`; V5 comprueba UUID y valor fuente y aborta si difieren. No hubo borrado de pacientes, citas ni relaciones y no se cambió ningún UUID. No se ejecutaron fragmentos manuales de la migración.

El backend, iniciado con el JAR que aprobó Maven `verify`, ejecutó Flyway normalmente sobre `hospital_platform`: detectó versión 4, aplicó **V5__domain_integrity_hardening.sql** y registró `success`. Hibernate arrancó con `ddl-auto: validate`. La verificación SQL confirmó **V1–V5 success**, `btree_gist`, unicidad `(document_type, document_number)`, `CHECK` de documento/colegiatura/slot, FK profesional–especialidad, exclusiones GiST de horarios y slots, nueve triggers `trg_*`, veinte funciones `capacity_*` y zona civil `America/Lima`. `hospital_business_now()` coincidió con el reloj civil de Lima. No quedaron documentos inválidos ni slots reservados huérfanos.

| Tabla | Antes | Inmediatamente después de V5 | Tras smoke y E2E |
|---|---:|---:|---:|
| users | 15 | 16 (ADMIN local) | 20 |
| patients | 14 | 14 | 18 |
| professionals | 1 | 1 | 1 |
| specialties | 1 | 1 | 1 |
| schedules | 7 | 7 | 7 |
| availability_slots | 84 | 84 | 84 |
| appointments | 18 | 18 | 23 |

Los incrementos finales provienen de las cuentas/citas de validación. Los 14 patient UUID del preflight siguieron presentes; los 18 appointments previos conservaron su conteo y vínculos. La comprobación de constraints y relaciones no encontró pérdida ni inconsistencia.

## Backend, frontend y PostgreSQL E2E

El backend principal está en `127.0.0.1:18080`, conectado a la base migrada como propietario de esquema para el corte. El frontend Next se inició en `127.0.0.1:3000` con `BACKEND_URL=http://127.0.0.1:18080/api/v1`. En el smoke HTTP: registro `201`, login `200`, disponibilidad `200` con 66 slots, cita `201 SCHEDULED`, Mis citas `200` y segunda reserva del mismo slot `409 SLOT_UNAVAILABLE`. La consulta directa a PostgreSQL confirmó la cita `e9518103-ed1a-4594-928b-8ec155eae7d7`, patient `357f31e2-95f0-46e3-a8a6-27a66f4b4a5d`, professional `88743663-5b1b-3868-bf1a-aa0371adfac3` y slot `6b3a3c19-8a42-4568-8f71-25fdb891a702` en `RESERVED`. Son identificadores de datos de prueba, no credenciales.

Pruebas negativas: DNI, CE y PASSPORT inválidos → `400`; documento DNI ya existente → `409 DUPLICATE_DOCUMENT`; disponibilidad privada sin sesión → `401`. En transacciones terminadas en `ROLLBACK`, colegiatura inválida produjo SQLSTATE `23514` y horario solapado `23P01`. Al deshabilitar temporalmente al usuario profesional demo mediante API ADMIN, PATIENT vio **0 slots**; el usuario quedó rehabilitado y verificado.

## Regresión y rol runtime

Con la JVM forzada a UTC, Maven `verify` aprobó **148 unitarias + 69 integración = 217/217**, 0 fallos y 0 errores. `npm run build` aprobó y `npm run test:e2e -- --reporter=line` aprobó **2/2** contra frontend, backend y PostgreSQL locales. El incremento de citas en la base principal confirmó que Playwright no usó la base aislada de C1-B.

Después de la regresión se creó `c1c_app_runtime`, separado del propietario de esquema, con credencial aleatoria DPAPI fuera de Git. Se aplicaron los grants mínimos de [C1-B-APP-RUNTIME-ROLE.sql](validation/C1-B-APP-RUNTIME-ROLE.sql) adaptando solo el nombre del rol. Carece de `INSERT` directo en `appointments` y de `UPDATE` directo en `availability_slots` y `schedules`; conserva `INSERT` necesario para registro y `EXECUTE` del gateway. Un backend separado en `127.0.0.1:18085`, con Flyway y seed desactivados, arrancó y pasó Hibernate validate. Con ese rol funcionaron registro `201`, login `200`, disponibilidad `200`, reserva `201`, reprogramación `201` y cancelación `200`. PostgreSQL confirmó original `RESCHEDULED` y sucesora `CANCELLED`, ambos slots liberados; los privilegios sensibles seguían denegados.

## Observaciones y control de cambios

`DEC-003` sigue abierta para el bootstrap de producción; el ADMIN creado aquí es solo local/demo. El rol runtime se verificó para el flujo PATIENT y el ciclo de cita probado, pero sus grants actuales no cubren todos los CRUD administrativos. Por ello el backend principal de demostración permanece con el propietario de esquema; el backend restringido corre separado en `18085`. Esto no afecta el corte V5 y sí delimita el siguiente trabajo de permisos operativos.

En C1-C no se cambiaron Java, frontend, tests de código ni V1–V5. Se añadió el ignore de `.codex_tmp/` a `.gitignore`, este informe y las notas de actualización a informes históricos C1-B. Las escrituras SQL fueron el bootstrap local autorizado, la habilitación del profesional demo, Flyway V5, los datos de smoke/E2E y la creación/grants del rol runtime. **No hubo commit, push ni merge.**
