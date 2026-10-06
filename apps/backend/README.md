# Hospital Platform Backend

Backend Spring Boot para Hospital Platform.

## Estado de integración

La ruta `GET /api/v1/medical/appointments/{appointmentId}/history/{encounterId}`
permite al profesional asignado abrir una atención previa finalizada del mismo
paciente, mientras la cita actual esté en espera o atención en la fecha civil de
Lima. Devuelve snapshots de diagnóstico y tratamiento, historia registrada,
orden solicitada y receta si existe. La orden no representa un resultado de
laboratorio. Un ID ajeno, actual, futuro o no finalizado devuelve 404.

Recepción puede crear, confirmar, registrar llegada y pasar a espera. Cancelar
y reprogramar requieren PATIENT propietario o ADMIN; el rol RECEPTIONIST recibe
403 en esas rutas. Los borradores se conservan hasta cierre o hasta que se
defina una eliminación administrativa auditada; no hay purga automática.

## Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL 16+
- Docker, para pruebas de integracion con Testcontainers

## Instalacion

Desde `apps/backend`:

```bash
./mvnw dependency:resolve
```

En Windows:

```powershell
.\mvnw.cmd dependency:resolve
```

## Variables de entorno

Crear las variables tomando como referencia `.env.example`:

```text
DATABASE_URL=
DATABASE_USERNAME=
DATABASE_PASSWORD=
FLYWAY_USERNAME=
FLYWAY_PASSWORD=
JWT_SECRET=
JWT_EXPIRATION=
REFRESH_TOKEN_EXPIRATION=
SPRING_PROFILE=
SERVER_PORT=
```

No se deben versionar secretos reales.

### Rol PostgreSQL restringido

En una base nueva, el propietario de esquema ejecuta las migraciones Flyway
antes de iniciar la aplicación con el rol `hospital_app_runtime`. Después fija
`hp.runtime_password` solo en la sesión SQL y ejecuta
[`infrastructure/sql/provision-runtime.sql`](../../infrastructure/sql/provision-runtime.sql).
La contraseña se proporciona fuera de Git; el script no la contiene. El rol
se puede reprovisionar con otra contraseña y solo recibe lectura de las tablas,
escrituras concretas y funciones de negocio necesarias. No recibe `CREATE`
en `public` ni escritura de catálogos.

Para iniciar con `SPRING_PROFILE=prod`, definir `DATABASE_USERNAME` y
`DATABASE_PASSWORD` para `hospital_app_runtime`, y `FLYWAY_USERNAME` y
`FLYWAY_PASSWORD` para el propietario de migraciones. El perfil prod usa las
credenciales de Flyway separadas del datasource de la aplicación. Repetir
el aprovisionamiento después de cada nueva migración que agregue tablas o
funciones necesarias para runtime.

En la base local vacía configurada el 2026-10-06 se aplicaron V1–V18; repetir
Flyway informó cero migraciones pendientes. Una conexión de prueba con
`hospital_app_runtime` confirmó SELECT permitido, UPDATE de pacientes permitido,
y UPDATE de registro final, INSERT de seguros y CREATE en `public` denegados.
La contraseña aleatoria usada para esa prueba no se guardó: antes de usar ese
rol en la aplicación hay que reprovisionarlo con una contraseña propia.

## Ejecucion local

Perfil por defecto:

```powershell
.\mvnw.cmd spring-boot:run
```

Perfil explicito:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

La API sirve los endpoints funcionales bajo `/api/v1`.

## Comandos Maven

```powershell
.\mvnw.cmd clean compile
.\mvnw.cmd test
.\mvnw.cmd verify
.\mvnw.cmd package
```

`verify` ejecuta las pruebas de integracion `*IT` con PostgreSQL mediante Testcontainers.

## Administracion de profesionales (F03)

Un usuario con rol `ADMIN` puede consultar `GET /api/v1/specialties` para obtener
las especialidades activas y `GET /api/v1/professionals` para ver profesionales
activos e inactivos. `GET /api/v1/professionals/{id}` devuelve el detalle.

`POST /api/v1/professionals` recibe `firstName`, `lastName`, `email`, `password`,
`licenseNumber` (4 a 6 digitos) y `specialtyId`. Crea la cuenta con rol
`PROFESSIONAL` y una sola especialidad en la misma transaccion. El correo y
la contrasena elegidos sirven para iniciar sesion; la respuesta no devuelve
la contrasena. `PUT /api/v1/professionals/{id}` recibe nombres, licencia y
`specialtyId`. No permite cambiar la especialidad si el profesional ya tiene
horarios historicos, para preservar la relacion con esos horarios.

`PATCH /api/v1/professionals/{id}/status` recibe `{"active": false}` para
desactivar o `{"active": true}` para reactivar. La desactivacion usa las reglas
de capacidad existentes; la reactivacion exige cuenta habilitada y especialidad
activa. La migracion V6 limita a una la asociacion de especialidad por profesional.

## Demo de disponibilidad para PATIENT (B2)

`Schedule.dayOfWeek` usa la convencion SQL: `0=domingo` hasta `6=sabado`.
`SlotGenerationService.generate(scheduleId)` es una operacion explicita reutilizable.
Produce intervalos de 30 minutos desde `startTime`, sin exceder `endTime`, para
los siguientes 14 dias civiles en `America/Lima` (desde manana). La insercion usa la
clave unica `(schedule_id, slot_date, start_time)` y no modifica slots existentes.

Un `ADMIN` publica la disponibilidad de un horario activo con
`POST /api/v1/agendas/{id}/publish` (sin cuerpo). La respuesta incluye
`scheduleId`, `createdSlots` y `horizonDays`. Repetir la solicitud conserva los
slots existentes y devuelve `createdSlots: 0` si no hay nuevos turnos en el
horizonte. Un horario inactivo devuelve 409 `AGENDA_INACTIVE`. Crear un horario
no publica automaticamente turnos; la publicacion es un paso explicito.

Para datos locales reproducibles, iniciar el backend con perfil `dev` y
`DEMO_SEED_ENABLED=true`. Por defecto el seed esta desactivado y nunca se ejecuta en
`test` o `prod`. Crea una especialidad, un profesional con cuenta deshabilitada,
la relacion profesional-especialidad, siete horarios semanales (09:00-12:00 UTC)
y slots futuros. Repetir el inicio agrega solo fechas nuevas y conserva los slots
ya reservados. No crea una contrasena conocida para el profesional de demo.

Con el PostgreSQL de `docker-compose.yml` iniciado desde la raiz del repositorio:

```powershell
docker build -f apps/backend/Dockerfile -t hospital-platform-backend:b2 .
docker run --rm --name hospital-platform-b2-api --network hospital-platform_default `
  --env-file .env -e DATABASE_URL=jdbc:postgresql://postgres:5432/hospital_platform `
  -e SPRING_PROFILE=dev -e SERVER_PORT=8080 -e DEMO_SEED_ENABLED=true `
  -p 127.0.0.1:18080:8080 hospital-platform-backend:b2
```

Un PATIENT autenticado puede consultar `GET /api/v1/availability`. El servidor
solo devuelve slots futuros `AVAILABLE` de horarios, profesionales y especialidades
activos; el filtro `status` del cliente no amplia esos resultados. Cada fila incluye
`slotId` (alias de `id` por compatibilidad), fecha, horas, profesional y especialidad.
La reserva usa `POST /api/v1/appointments` con `slotId` y `reason`; el paciente
se obtiene del token. Un segundo intento sobre el mismo slot devuelve 409.

## Consulta de citas para Recepcion

`GET /api/v1/appointments/reception?patientId={id}&limit={n}` requiere el rol
`RECEPTIONIST`. El `patientId` se obtiene mediante la busqueda documental exacta
de `GET /api/v1/patients/search`; no se admite una consulta sin paciente. El limite
predeterminado es 50 y el maximo es 100. La respuesta incluye identidad minima
del paciente, profesional, especialidad, fecha, horario, estado y etapa; conserva
citas historicas y no devuelve el motivo de consulta. `GET /api/v1/appointments`
y `GET /api/v1/appointments/{id}` quedan reservados a ADMIN y PATIENT (este
ultimo solo para sus propias citas).

## Llegada en Recepcion

`POST /api/v1/appointments/{id}/check-in` solo registra una llegada nueva si la
cita esta `CONFIRMED` y la fecha civil del turno coincide con la fecha actual
del servidor en `America/Lima`. Un intento anticipado o tardio devuelve 409
`INVALID_APPOINTMENT_TRANSITION` sin cambiar la etapa ni registrar auditoria.
Repetir un CHECK_IN ya registrado conserva la respuesta idempotente sin crear
otro evento. `WAITING` sigue requiriendo CHECK_IN previo.

`GET /api/v1/appointments/reception/waiting-room?limit={n}&offset={n}` requiere
`RECEPTIONIST` y devuelve solo citas `CONFIRMED` del dia actual en
`America/Lima` con etapa `CHECK_IN` o `WAITING`. La respuesta contiene el ID de
la cita, paciente, hora, profesional, especialidad y etapa; no incluye motivo ni
datos clinicos. Si el paciente no tiene nombre registrado, se muestra su tipo y
numero de documento. La cola se ordena por hora e ID de cita. `limit` vale 50
por defecto (maximo 100) y `offset` vale 0 (no admite negativos).

## Cabecera de Historia Clinica

La migracion V7 crea una cabecera `clinical_records` por paciente, con numero
unico `HC-{secuencia}` y fecha de apertura. Retrocrea las cabeceras de pacientes
existentes sin inventar datos demograficos. Las altas nuevas, incluido F01,
crean la cabecera en la misma transaccion mediante un trigger de PostgreSQL.
El registro F01 conserva sus nueve campos originales y acepta `address` y `sex`
como campos opcionales (maximos 500 y 50 caracteres, respectivamente). Los
espacios exteriores se eliminan y los valores vacios se guardan como NULL.
`sex` es texto declarado por el paciente: esta version no impone un catalogo ni
altera los registros previos. La cabecera no requiere estos datos para crear la
cuenta.

V18 agrega a F01 ocho campos opcionales: `maritalStatus` (80), `occupation`
(120), `district` (120), `educationLevel` (120), `affiliationNumber` (60),
`emergencyContactName` (150), `emergencyContactRelationship` (80) y
`emergencyContactPhone` (50). Los valores vacios se guardan como `NULL` y los
pacientes anteriores conservan `NULL`. El telefono de emergencia, si se envia,
debe tener 7-15 digitos con `+` inicial opcional. La afiliacion solo corresponde
a SIS o EsSalud; Particular no admite numero. `GET /api/v1/patients/me` devuelve
estos datos al paciente autenticado. ADMIN puede reemplazar el conjunto completo
con `PATCH /api/v1/patients/{id}/demographics`; los campos omitidos quedan `NULL`.
El `PUT /api/v1/patients/{id}` anterior no modifica estos nuevos datos.

V17 normaliza el seguro en `insurance_providers` con las opciones activas
`SIS`, `EsSalud` y `Particular`. `GET /api/v1/insurance-providers` es publico
para cargar el selector de F01 y devuelve `id`, `code` y `name`. El registro
acepta `insuranceId`; para compatibilidad, tambien acepta el campo antiguo
`insurance` si coincide con una opcion aprobada. Si se envian ambos deben
referirse al mismo seguro. La respuesta conserva `insurance` y agrega
`insuranceId`. Valores libres, IDs desconocidas e inconsistencias devuelven
400. V17 retrocrea la FK para valores antiguos reconocidos, pero **detiene la
migracion** si encuentra un seguro no mapeable: revisar esos datos antes de
reintentar; nunca se convierten automaticamente a `Particular`.

## Contexto medico inicial

`GET /api/v1/medical/me/context` requiere `PROFESSIONAL` vinculado a un
perfil activo con especialidad activa. Devuelve nombre, CMP, RNE simulado, especialidad y
una cola de hasta 50 citas propias `CONFIRMED` en `WAITING` o `IN_ATTENTION`
para la fecha civil de `America/Lima`. La cola no incluye motivo ni datos del
paciente. `GET /api/v1/medical/appointments/{id}/context` devuelve identidad
basica del paciente (incluidos sexo y demografia opcional declarados), cabecera de Historia
Clinica, horario, etapa y motivo
solo al profesional asignado a esa cita en el mismo dia y esas etapas. Una
cita ajena o fuera del flujo permitido responde 404. Administracion,
Recepcion y Paciente no pueden usar estas rutas.

`GET /api/v1/medical/appointments/{id}/history?limit={n}&offset={n}` devuelve
solo atenciones previas finalizadas y citas `COMPLETED/FINISHED` del mismo
paciente. Exige una cita actual `CONFIRMED` en `WAITING` o `IN_ATTENTION` del dia
Lima asignada al medico autenticado; la consulta repite ese filtro en SQL.
Incluye fecha, servicio simulado, medico, especialidad, motivo y diagnostico;
no incluye borradores. `limit` vale 20 por defecto (1-50) y `offset` vale 0
(0-10000). Una cita ajena devuelve 404 y una pagina invalida 400.

Esta API no selecciona aun un unico turno actual de 30 minutos. El resumen
longitudinal de antecedentes, alergias y medicacion habitual sigue pendiente.
Los 30 minutos del turno son
informativos: no bloquean el inicio fuera de ese intervalo ni cierran la
atencion automaticamente.

## Catalogos medicos y paquete sintetico de demostracion

V8 crea `medical_catalog_sources`, `icd10_codes`, `medications`, la tabla
dependiente `medication_presentations` y `procedures`. Flyway no carga filas.
Existe un [paquete sintetico para la demo](../../database/demo/README.md),
que se instala por separado con un ADMIN sintetico y el propietario del esquema.
Sus codigos, medicamentos, presentaciones y procedimientos son ficticios y no
habilitan uso clinico. Toda carga de catalogos reales requerira fuente, version,
licencia y usuario aprobador;
no existe una API de escritura de catalogos. Las lecturas requieren
`PROFESSIONAL`:

- `GET /api/v1/medical/catalogs/icd10?q={texto}&limit={n}` busca codigo o descripcion.
- `GET /api/v1/medical/catalogs/medications?q={texto}&limit={n}` busca nombre generico o comercial.
- `GET /api/v1/medical/catalogs/medications/{id}/presentations?limit={n}` devuelve solo presentaciones activas de un medicamento activo.
- `GET /api/v1/medical/catalogs/procedures?q={texto}&limit={n}` busca codigo o nombre.

`q` es obligatorio, se recorta y exige entre 2 y 100 caracteres. `limit`
admite 1 a 50; por defecto es 20 en busquedas y 50 en presentaciones. La
busqueda es literal, no interpreta `%` ni `_` como comodines. Una lista vacia
no habilita texto libre. El paquete sintetico permite probar el flujo academico;
no sustituye una fuente clinica aprobada para atencion real.

## Inicio de atencion medica

`POST /api/v1/medical/appointments/{id}/start` requiere el profesional activo
asignado a una cita `CONFIRMED` en `WAITING` o `IN_ATTENTION` del dia civil actual
en `America/Lima`. Devuelve `encounterId`, `appointmentId`, estado `OPEN`,
`startedAt`, `legacyStart`, RNE, tipo de atencion y servicio simulados. Repetir
la llamada conserva el mismo encuentro y
no duplica la auditoria. La ruta anterior
`POST /api/v1/appointments/{id}/start-attention` usa el mismo inicio atomico y
mantiene su respuesta operativa. V9 retrocrea encuentros para citas que ya
estaban en `IN_ATTENTION`, con `legacyStart=true` y sin inventar `startedAt`.

Este inicio por si solo no guarda notas, diagnosticos ni recetas. V11 conserva
`simulatedRne` por profesional como `SIM-RNE-` seguido de los ultimos 12
caracteres hexadecimales de su UUID, y conserva `simulatedCareType` y
`simulatedService` por encuentro. Sus valores incluyen la marca "dato simulado"
y no representan credenciales ni un servicio clinico verificado. V11 cubre
tambien profesionales y encuentros existentes sin inventar fecha de inicio. Los
borradores se conservan hasta el cierre o una futura eliminacion administrativa
definida y auditada; no existe purga automatica ni endpoint de borrado. El servicio operativo de
citas y el endpoint de cierre previo siguen requiriendo el endurecimiento del
cierre clinico en la etapa 3.

## Borrador de historial medico

V10 agrega `encounter_drafts` para conservar contenido temporal de una atencion
abierta. `GET /api/v1/medical/encounters/{id}/draft/history` devuelve el historial
editable y su `version`; si todavia no se guardo, devuelve `version: 0`,
`history: null` y `updatedAt: null`. `PUT` a la misma ruta recibe `version` y
`history` con secciones `personal`, `family`, `gynecologic` y `otherAlerts`.
El profesional activo asignado puede guardar mientras la cita siga `CONFIRMED`
en `IN_ATTENTION` y el encuentro este `OPEN`. Una version obsoleta devuelve
409 `MEDICAL_DRAFT_VERSION_CONFLICT`; otro medico obtiene 404 y los roles no
medicos no acceden. Los enteros ginecoobstetricos no admiten negativos; los
campos opcionales ausentes permanecen nulos. El guardado no crea una historia
definitiva ni una receta y la auditoria no contiene texto clinico.

El borrador se conserva hasta el cierre o una eliminacion administrativa
definida; no hay caducidad ni endpoint de eliminacion en este bloque. Los
catalogos siguen vacios hasta aprobar sus fuentes.

## Borrador de evaluacion medica

`GET /api/v1/medical/encounters/{id}/draft/assessment` devuelve la evaluacion
editable y la misma `version` global del borrador de historial. Si no se ha
guardado evaluacion, devuelve `assessment: null`. `PUT` a esa ruta recibe
`version` y `assessment` con secciones opcionales `presentation`, `vitalSigns`,
`examination`, `diagnosis` y `treatmentPlan`. Cada guardado preserva la otra
seccion y avanza la version compartida; un cliente con version anterior recibe
409 `MEDICAL_DRAFT_VERSION_CONFLICT`.

Solo el profesional activo asignado puede leer o guardar durante una atencion
abierta en `IN_ATTENTION`. Las referencias seleccionadas de CIE-10,
procedimiento y especialidad de interconsulta deben estar activas; las
referencias desconocidas o inactivas devuelven 400
`INVALID_MEDICAL_DRAFT_REFERENCE`. La fuente de los catalogos sigue pendiente,
por lo que no se cargan datos clinicos de produccion en este bloque. La
auditoria registra seccion y version, sin texto clinico. Esto no finaliza
diagnosticos ni tratamientos; faltan receta temporal, cierre clinico y lectura
posterior por el paciente.

## Borrador de receta medica

`GET /api/v1/medical/encounters/{id}/draft/prescription` devuelve la receta
temporal y la misma `version` de historial y evaluacion. Antes del primer
guardado devuelve `prescription: null`. `PUT` recibe `version` y `prescription`
con `items` (lista requerida, que puede estar vacia) y textos opcionales de
precauciones, recomendaciones no farmacologicas, signos de alarma, cuidados y
seguimiento. Cada `PUT` reemplaza la lista temporal completa, por lo que un
cliente puede agregar, editar o quitar filas mientras la atencion siga abierta.

Cada fila requiere `medicationId`, `presentationId`, dosis positiva (hasta tres
decimales), unidad de dosis, frecuencia, via, duracion positiva (hasta tres
decimales), unidad de duracion, cantidad entera positiva e indicaciones de uso.
Las unidades admitidas son `MG`, `G`, `ML`, `DROPS`, `TABLET`, `CAPSULE`, `IU`;
las frecuencias son `EVERY_6_HOURS`, `EVERY_8_HOURS`, `EVERY_12_HOURS`,
`EVERY_24_HOURS`, `ONCE`; las duraciones usan `DAYS` o `WEEKS`. La forma
farmaceutica y concentracion se obtienen de la presentacion seleccionada.
El backend comprueba que medicamento y presentacion esten activos y relacionados.
Una seleccion invalida devuelve 400 `INVALID_MEDICAL_DRAFT_REFERENCE`.

Solo el profesional activo asignado puede consultar o guardar este borrador;
una version obsoleta devuelve 409 `MEDICAL_DRAFT_VERSION_CONFLICT`. El guardado
conserva las otras secciones, no crea una receta definitiva ni genera numero
de receta. Una lista vacia tampoco crea una receta final. La auditoria registra
solo seccion y version, sin texto clinico. Los catalogos de produccion siguen
vacios hasta aprobar fuente, licencia y version.

## Estructura clinica final

V12 crea `clinical_final_records` (un registro por encuentro),
`clinical_history_entries`, `clinical_assessments`, `clinical_diagnoses`,
`clinical_treatment_plans`, `clinical_orders`, `clinical_prescriptions` y
`clinical_prescription_items`. Los diagnosticos y procedimientos referencian
sus catalogos; cada item de receta exige una presentacion del medicamento
seleccionado. Los campos de snapshot conservan nombres y etiquetas para
lecturas historicas aunque cambie el catalogo. Los checks impiden dosis,
duraciones, cantidades y mediciones invalidas. El numero de receta se genera
con una secuencia cuando se crea una receta con medicamentos.

V12 por si sola no copia borradores ni cambia estados. V13 y el endpoint
`POST /api/v1/medical/encounters/{id}/finalize` completan ese flujo. El cuerpo
es `{ "version": 3 }`, usando la version actual del borrador guardado. Solo
el profesional activo asignado puede cerrar una atencion `OPEN` que este en
`CONFIRMED/IN_ATTENTION`. Son obligatorios el historial guardado, el motivo,
los sintomas, el diagnostico principal con CIE-10 activo y tipo, el plan y las
indicaciones. Los medicamentos son opcionales; sin items no se crea receta.

La operacion valida referencias activas, congela snapshots, marca la atencion
`FINALIZED` y la cita `FINISHED/COMPLETED` en una sola transaccion, con auditoria
sin texto clinico. Repetir la misma version y contenido devuelve el mismo
resultado; otra version o contenido despues del cierre devuelve 409. Datos
incompletos o referencias inactivas devuelven 400 y una atencion ajena 404.
`POST /appointments/{id}/complete` ya no permite cierres nuevos sin clinica;
V13 exige registro final para cada nueva transicion a `COMPLETED`, incluso si
se invoca directamente el gateway SQL. No se fabrican registros para citas
historicas ya completadas. Los catalogos de produccion siguen vacios hasta
aprobar fuente, licencia y version.

## Lectura clinica del paciente

Solo `PATIENT` con perfil activo puede consultar datos clinicos finalizados
propios. Los listados `GET /api/v1/medical/me/encounters` y
`GET /api/v1/medical/me/prescriptions` aceptan `limit` (1-50, por defecto 20)
y `offset` (0-10000, por defecto 0), y devuelven `items`, `limit`, `offset`
y `hasMore`. Los detalles se leen en
`GET /api/v1/medical/me/encounters/{id}` y
`GET /api/v1/medical/me/prescriptions/{id}`. Una ID ajena o inexistente
devuelve 404; paginacion invalida devuelve 400.

Las consultas filtran por paciente, encuentro `FINALIZED` y cita
`COMPLETED/FINISHED` en SQL. No leen borradores ni muestran recetas sin
medicamentos. Nombres, diagnostico, presentaciones y `patientSex` del detalle
provienen de snapshots del cierre, no del perfil o catalogo actual. Los
registros finalizados antes de V16 conservan `patientSex` en NULL y los
finalizados antes de V18 conservan los nuevos campos demograficos en NULL, sin
retrocrear un valor. Admin, Recepcion y Medico
no tienen acceso a estas rutas por sus roles.

La regresion `MedicalIntegratedRuntimeIT` migra V1-V18 desde cero con el
propietario de esquema y ejecuta el recorrido ADMIN, PACIENTE, RECEPCION,
MEDICO y PACIENTE con un rol SQL distinto. Su fixture de grants esta en
`src/test/resources/medical-runtime-role.sql`: no permite `UPDATE` directo
sobre capacidad ni catalogos. V14 encapsula los bloqueos de referencias
activas en funciones `SECURITY DEFINER` con `EXECUTE` explicito, sin ampliar
privilegios de escritura sobre los catalogos. La fixture usa datos sinteticos
y no es una politica de despliegue de produccion.

## Migraciones

Las migraciones oficiales viven en:

```text
database/migrations/V1__initial_schema.sql ... V18__patient_optional_demographics.sql
```

El `pom.xml` las incluye como recurso de build en `classpath:db/migration` para
que Flyway las ejecute en orden sin duplicarlas ni modificarlas.

## Docker

Construir desde la raiz del repositorio para incluir la migracion oficial:

```powershell
docker build -f apps/backend/Dockerfile -t hospital-platform-backend .
```

Ejecutar:

```powershell
docker run --rm -p 8080:8080 --env-file apps/backend/.env hospital-platform-backend
```

## Estructura

```text
src/main/java/com/hospital/platform
|-- common
|-- security
|-- auth
|-- users
|-- patients
|-- professionals
|-- medical
|-- catalogs
|-- agenda
|-- appointments
|-- waitlist
|-- priority
|-- notifications
|-- dashboard
`-- audit
```

Cada modulo funcional se organiza internamente por feature con `controller`, `service`, `repository`, `entity`, `dto`, `mapper` y `exception`.
