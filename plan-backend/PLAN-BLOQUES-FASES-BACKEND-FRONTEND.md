# Plan de bloques y fases pendientes: backend, cinco formularios y frontend

Fecha de corte: 2026-10-06 (`America/Lima`). Este documento es una **hoja de trabajo futura**, no una implementacion ni una aprobacion de cierre. Base local comprobada: `main` limpio en `e6fe2ac`, merge del PR #75 (seguro normalizado). No se creo rama ni commit para este informe. Antes de ejecutar cualquier bloque, comprobar Git, actualizar `main` con Fetch/Pull y volver a contrastar este plan con el codigo y los controles remotos.

## Auditoria de vigencia y continuacion (2026-10-06)

- La base citada arriba es historica. Al iniciar esta continuacion, `main` local estaba limpio en `505683f` (plan fusionado), sin commits locales adicionales frente a `origin/main` almacenado. El intento de `git fetch origin` no respondio y fue detenido; por tanto, la punta remota actual no esta verificada.
- B1 seguia pendiente en el codigo. Se abrio la rama local `codex/backend-patient-demographics` y se implemento V18, contratos de registro/lectura/edicion ADMIN, contexto medico y snapshots. `mvnw.cmd clean test` paso con 183 unitarias; las integraciones PostgreSQL no pudieron arrancar porque este equipo no ofrece un daemon Docker. El trabajo queda en un commit local para revision. **B1 queda pendiente de `mvn clean verify`, revision y CI; no se declara cerrado ni fusionado.**
- El plan omitia el efecto de `PUT /patients/{id}` sobre campos nuevos. La implementacion usa `PATCH /patients/{id}/demographics` para reemplazarlos sin que clientes ADMIN antiguos los borren accidentalmente.
- B2 no tiene fuentes clinicas oficiales. El usuario autorizo explicitamente datos sinteticos para la demo; `database/demo/` contiene un paquete ficticio separado de Flyway que puede instalarse para probar el recorrido. No debe presentarse como CIE-10, medicamentos o procedimientos clinicos reales. La carga, la repeticion y el rechazo de conflictos requieren verificacion PostgreSQL antes de considerarla cerrada.
- `ESTADO_BACKEND.md` conserva el estado previo al merge de PR #75 y debe leerse como historial; se actualiza en esta rama. El orden restante B2-B6, F0-F5, M1-M3 y V1-V2 sigue siendo plan, sujeto a verificar contratos y pruebas en cada bloque.

## 0. Como leer y usar el plan

- **Rama propuesta** es el nombre exacto que se usaria *cuando el usuario encargue ese bloque*. Todas naceran de `main` actualizado, en orden de dependencias; **ninguna rama de esta tabla existe por el hecho de figurar aqui**. Si otro colaborador ya creo una, inspeccionarla antes de reutilizarla.
- **Commit asociado**: los bloques futuros no tienen SHA ni commit creado. Se da el texto propuesto para **Summary** de GitHub Desktop, que pasara a ser el mensaje del commit principal cuando el bloque se implemente. No se inventan hashes.
- **Auditoria requerida** distingue una revision tecnica especifica de la verificacion ordinaria de un PR. `No` significa que bastan revision de codigo y pruebas del bloque, no que se pueda omitir la seguridad, la integracion ni CI.
- La aprobacion academica de usar catalogos clinicos no equivale a disponer de archivos, valores, version y condiciones de redistribucion. Las tablas y busquedas existen. Por decision posterior del usuario, el recorrido de demo puede usar el paquete sintetico rotulado de `database/demo/`; no equivale a un catalogo clinico oficial y no incluye dosis medicas.
- Usar pacientes y usuarios sinteticos. El resultado sera una demo academica, no un sistema habilitado para atencion clinica real.

**Regla comun para cada bloque de codigo:** una rama pequena desde `main` actualizado, PR propio, pruebas unitarias focalizadas y pruebas de integracion PostgreSQL/Testcontainers en todo cambio backend, regresion del recorrido afectado, actualizacion de `ESTADO_BACKEND.md` y documentacion pertinente. Antes de fusionar, ejecutar la suite completa aplicable y esperar **Backend CI** y **Repository Validation** verdes; para frontend, `typecheck`, `build`, E2E con backend/PostgreSQL reales y los controles CI que existan o se creen. No fusionar PR con controles fallidos, omitidos sin justificacion o pendientes. Un PR solo documental puede no disparar Backend CI: verificar su alcance de otra forma y dejarlo registrado.

## 1. Estado comprobado y limites

| Area | Ya existe en `main` | Aun falta para cierre integral |
| --- | --- | --- |
| F01 Registro | Backend transaccional con cabecera de Historia Clinica; nueve campos originales compatibles; direccion y sexo opcionales; seguro normalizado SIS/EsSalud/Particular en PR #75. UI de registro y prueba E2E paciente. | Demografia opcional adicional, adaptacion de UI al catalogo de seguros/contrato final y regresion F01 antigua/nueva. |
| F02 Reserva paciente | Disponibilidad real, reserva con capacidad PostgreSQL y UI Next.js; resumen de citas propio en backend. | Alinear UI de Mis citas al resumen legible y verificar recorrido completo, estados y bordes civiles. |
| F03 Profesional ADMIN | API de profesional, una sola especialidad y gestion de estado; PR #48. | Portal Next.js administrativo F03 y pruebas de rol. |
| F04 Horario ADMIN | API de agendas y publicacion de turnos; PR #49. | Portal Next.js F04; comprobar seleccion de dias, publicacion y disponibilidad derivada. |
| F05 Cita presencial RECEPCION | Busqueda documental, disponibilidad, consulta de citas filtrada, confirmacion, CHECK_IN en fecha Lima, WAITING y sala de espera; PR #51, #53-#55. | Retirar permiso HTTP legado de cancelar/reprogramar para Recepcion conforme al acuerdo; portal Next.js F05 y cola. |
| Medico y lectura del paciente | Contexto, inicio, borradores, cierre transaccional/idempotente, lectura final propia, resumen de antecedentes y prueba integrada PostgreSQL; PR #56-#72. | Carga de catalogos, demografia en contexto/snapshot, detalle longitudinal, portal medico y vistas clinicas del paciente. |
| Operacion | Fixture de rol SQL restringido y guia parcial de demo paciente. | Provisionamiento reproducible, auditoria integral, guia de arranque desde PC apagada y pruebas E2E de todos los roles. |

La auditoria G.1 del 2026-10-02 y `BACKEND-COMPLETION-AUDIT.md` describen bases anteriores. Sus brechas ya resueltas no se reabren por inercia. `ESTADO_BACKEND.md` aun llama pendiente al PR #75, aunque `main` en `e6fe2ac` demuestra que esta fusionado: corregirlo en la primera rama de implementacion, sin modificarlo durante este encargo documental. No declarar cerrados los cinco formularios solo porque sus API existen: F03, F04 y F05 no tienen portal Next.js funcional.

## 2. Bloque B: cerrar contratos y backend

### B1. Contrato demografico opcional del paciente

- **Rama propuesta:** `codex/backend-patient-demographics` (desde `main` actualizado).
- **Commit asociado:** pendiente; Summary propuesto: `Add optional patient demographics without breaking F01`.
- **Auditoria requerida:** **Si, previa y focalizada** sobre DTO, esquema, datos existentes, contexto medico y snapshots; despues, revision del PR. No requiere nueva auditoria global por si solo.
- **Trabajo:** agregar estado civil, ocupacion, distrito, grado de instruccion, numero de afiliacion y contacto de emergencia (nombre, parentesco, telefono) como campos opcionales, con limites y validaciones; `NULL` expresa no declarado. Conservar los nueve campos F01 antiguos, el registro atomico de usuario/paciente/cabecera y el seguro normalizado. La afiliacion debe corresponder a un seguro que la admita; para Particular, `NULL`. Definir exposicion estricta en lectura propia, edicion ADMIN autorizada, contexto medico y snapshot clinico cuando corresponda, sin reescribir cierres antiguos.
- **Pruebas/salida:** unitarias de formatos y compatibilidad; PostgreSQL de migracion de legados, F01 viejo/nuevo, rollback, permisos, contexto y snapshot inmutable. Un cliente de nueve campos sigue registrando al paciente.

### B2. Paquete e importacion de catalogos clinicos aprobados

- **Rama propuesta:** `codex/backend-clinical-catalog-import` (desde `main` actualizado).
- **Commit asociado:** pendiente; Summary propuesto: `Load approved clinical catalogs with provenance`.
- **Auditoria requerida:** **Si, obligatoria antes de codificar y posterior a la carga**: procedencia, version, licencia/permiso, esquema, duplicados, codigos, relaciones medicamento-presentacion y posibilidad de redistribucion. Si los archivos concretos no estan disponibles, este bloque queda **bloqueado**, aunque el equipo y el docente hayan aprobado el uso de catalogos.
- **Trabajo:** recibir o ubicar los archivos efectivamente aprobados de CIE-10, medicamentos/presentaciones y procedimientos; documentar emisor, version, fecha, responsable y checksum. Preparar importacion reproducible, transaccional e idempotente para una base PostgreSQL nueva; mantener metadatos de fuente, integridad de relaciones y solo opciones activas. No incluir dataset en Git si su licencia no permite distribuirlo: documentar carga externa autorizada. Preparar el usuario aprobador/propietario sin UUID ficticios y evitar privilegios amplios al rol runtime. No confundir fixtures sinteticas de tests con catalogos operativos.
- **Pruebas/salida:** validadores unitarios; PostgreSQL limpio con importacion repetida, duplicados/huérfanos/errores rechazados sin carga parcial, busquedas acotadas y cierre medico con diagnostico de la fuente aprobada. Cambios de version no alteran snapshots historicos.

### B3. Lectura longitudinal detallada para el medico

- **Rama propuesta:** `codex/backend-medical-longitudinal-detail` (desde `main` actualizado).
- **Commit asociado:** pendiente; Summary propuesto: `Expose authorized longitudinal encounter detail`.
- **Auditoria requerida:** **Si, focalizada** en privacidad, SQL acotado, snapshots y alcance real de los datos. Revalidar el contrato antes de implementarlo.
- **Trabajo:** conservar el resumen paginado actual y permitir abrir el detalle de una atencion previa finalizada del **mismo paciente**, partiendo de la cita actual asignada al medico autenticado. Mostrar historia, alergias/alertas registradas, diagnosticos con snapshots CIE-10, plan, ordenes y receta existente; ausencias como `NULL` o listas vacias. Excluir borradores, otro paciente, otro medico y la atencion actual; 404 uniforme para ID inaccesible. No presentar ordenes como resultados de laboratorio ni texto de hospitalizacion como ingreso/alta estructurado inexistente.
- **Pruebas/salida:** unitarias de proyeccion/paginacion; PostgreSQL para mismo/otro paciente, medico asignado/ajeno, fecha y etapa Lima, finalizado/borrador, receta presente/ausente, snapshots de catalogo inactivo y consultas limitadas. Si el docente exige resultados o internaciones estructuradas, abrir alcance y auditoria separados antes de prometerlos.

### B4. Limite HTTP de permisos de Recepcion y retencion de borradores

- **Rama propuesta:** `codex/backend-reception-permissions-draft-policy` (desde `main` actualizado).
- **Commit asociado:** pendiente; Summary propuesto: `Restrict reception cancellation and document draft retention`.
- **Auditoria requerida:** **Si, focalizada** en matriz de roles, endpoints legados e historial/capacidad; la politica de eliminacion administrativa necesita acuerdo/auditoria propios antes de cualquier `DELETE`.
- **Trabajo:** quitar `RECEPTIONIST` de cancelar y reprogramar citas en el API, coherente con su aplazamiento, conservando los permisos PATIENT/ADMIN y sus transiciones existentes. No mostrar esas acciones en F05. Documentar que borradores clinicos se conservan hasta cierre o hasta una futura eliminacion administrativa definida; no agregar purga automatica ni borrar historia cerrada. Precisar que Recepcion no accede a motivo/historia/receta fuera del formulario autorizado.
- **Pruebas/salida:** HTTP 403 para Recepcion en rutas aplazadas, regresion PATIENT/ADMIN, capacidad e historial intactos, rutas permitidas de Recepcion y no exposicion de datos clinicos. Registro de politica de retencion sin simular una eliminacion aun no especificada.

### B5. Provisionamiento PostgreSQL y documentacion backend

- **Rama propuesta:** `codex/backend-runtime-provisioning` (desde `main` actualizado).
- **Commit asociado:** pendiente; Summary propuesto: `Document and verify restricted backend runtime setup`.
- **Auditoria requerida:** **Si, tecnica** de privilegios SQL, secretos, migraciones y reproducibilidad en base nueva.
- **Trabajo:** separar propietario de Flyway y rol de aplicacion con privilegios minimos; convertir la fixture actual de tests en un procedimiento de despliegue academico revisado, sin publicar credenciales reales. Documentar PostgreSQL, migraciones vigentes, carga de catalogos, variables de entorno y bootstrap de usuarios sinteticos ADMIN/RECEPTIONIST/PROFESSIONAL. Actualizar README backend y referencias obsoletas. No alterar reglas de reserva o cierre para facilitar la instalacion.
- **Pruebas/salida:** base limpia con migraciones y datos aprobados instalados, aplicacion usando el rol runtime restringido y recorrido integrado PostgreSQL. Comprobar acceso permitido y operaciones SQL denegadas. Registrar comandos reales y resultados, no solo un script sin ejecutar.

### B6. Auditoria completa y puerta de cierre del backend

- **Rama propuesta:** `codex/backend-final-audit` (desde `main` actualizado).
- **Commit asociado:** pendiente; Summary propuesto: `Audit integrated backend against final contracts`.
- **Auditoria requerida:** **Si, integral y obligatoria**. Es una fase de evidencia; cualquier defecto encontrado se corrige en PR pequeno separado antes de afirmar cierre.
- **Trabajo:** volver a contrastar Word, decisiones posteriores, migraciones, rutas, permisos, DTO, documentación y pruebas. Matriz de F01-F05 mas Modulo Medico y lectura propia del paciente; autorizacion negativa; capacidad/concurrencia; fecha Lima; cierre idempotente; historia/snapshots; catalogos y seguro; instalacion limpia; retencion y secretos. Verificar que las funciones futuras (cancelar/reprogramar por Recepcion, consultorio real, resultados e internaciones no modelados) queden explicitamente fuera del alcance actual o se abran como nuevos bloques aprobados.
- **Pruebas/salida:** `mvn clean verify` con Docker/PostgreSQL, recorrido HTTP integrado y rol restringido, numeros y resultado de suites, Backend CI/Repository Validation verdes si hay PR, cero hallazgos bloqueantes. Solo entonces actualizar `ESTADO_BACKEND.md` a **backend academico cerrado**. Si no hay fuentes clinicas verificables, el cierre sigue pendiente.

## 3. Bloque F: contratos y frontend de los cinco formularios

Iniciar despues de B6, salvo que el usuario autorice explicitamente un solapamiento controlado. Reutilizar Next.js App Router, BFF y cookies HttpOnly ya presentes; los HTML estaticos de `app/publico` no cuentan como portales integrados. Cada fase debe validar estados vacio, carga, error, 401/403, conflicto y doble envio donde aplique. El frontend no decide permisos ni capacidad: siempre usa la API real.

### F0. Auditoria de contratos y estructura de portales

- **Rama propuesta:** `codex/frontend-contract-audit` (desde `main` actualizado tras B6).
- **Commit asociado:** pendiente; Summary propuesto: `Audit frontend contracts for five forms and clinical portals`.
- **Auditoria requerida:** **Si, previa y obligatoria**. Sin implementacion visual en esta fase.
- **Trabajo:** inventariar rutas Next.js/BFF, tokens, tipos, endpoints reales, forma de errores y roles; mapear cada campo del Word contra el contrato backend final. Fijar navegacion por rol, estados UI, accesibilidad, sesiones/refresh y estrategia de pruebas unitarias/componentes (hoy el frontend tiene Playwright, `typecheck` y `build`, pero no una suite unitaria declarada). Detectar diferencias como README de Mis citas desactualizado. Producir matriz verificable antes de abrir F01-F05.
- **Salida:** documento de contratos y criterios E2E por rol, sin duplicar logica clinica ni catálogos en el cliente. Si falta una API necesaria, abrir un bloque backend especifico antes del formulario consumidor.

### F1. F01 Registro de paciente y sesion

- **Rama propuesta:** `codex/frontend-f01-registration` (desde `main` actualizado).
- **Commit asociado:** pendiente; Summary propuesto: `Complete patient registration with catalog and optional data`.
- **Auditoria requerida:** **No nueva auditoria global**; usar F0 y revisar privacidad/compatibilidad del formulario.
- **Trabajo:** conservar el registro de nueve campos, integrar opciones activas SIS/EsSalud/Particular desde la API, añadir demografia opcional sin volverla obligatoria, errores de validacion claros y flujo de inicio de sesion. Mostrar consentimiento/terminos existentes segun el contrato vigente, sin almacenar tokens en JavaScript del navegador. Validar bordes de fecha civil y no mandar valores vacios ficticios.
- **Pruebas/salida:** unitarias/componentes de campos dependientes y normalizacion visual; Playwright con PostgreSQL de registro antiguo/nuevo, duplicado documental, seguro invalido, sesion y red fallida. Mantener F01 utilizable en movil/escritorio.

### F2. F02 Reserva y Mis citas del paciente

- **Rama propuesta:** `codex/frontend-f02-booking-summary` (desde `main` actualizado).
- **Commit asociado:** pendiente; Summary propuesto: `Align patient booking and appointment summary`.
- **Auditoria requerida:** **No nueva auditoria global**; revision focalizada de contrato y capacidad.
- **Trabajo:** mantener la seleccion dependiente Especialidad -> Medico -> Fecha -> Hora -> Motivo; limpiar selecciones posteriores cuando cambie una anterior; obtener catalogos/disponibilidad del backend y revisar la reserva antes de confirmar. Integrar `GET /appointments/me/summary` para mostrar fecha, hora, profesional, especialidad y etapa legible; actualizar README que aun describe una limitacion superada. No exponer el termino tecnico `slot` al usuario ni reconstruir disponibilidad solo en cliente.
- **Pruebas/salida:** componentes de dependencia y estados; Playwright con PostgreSQL para reserva real, dos usuarios intentando la misma hora, 409 por cupo agotado, historial propio, zona Lima y permiso cruzado. Mantener historial y capacidad.

### F3. F03 Administracion de profesionales

- **Rama propuesta:** `codex/frontend-f03-professionals` (desde `main` actualizado).
- **Commit asociado:** pendiente; Summary propuesto: `Build admin professional management form`.
- **Auditoria requerida:** **No nueva auditoria global**; revision de RBAC y una sola especialidad.
- **Trabajo:** portal ADMIN con listado/busqueda, alta y edicion de profesional, cuenta asociada, CMP numerico, especialidad unica activa y estado; confirmaciones para cambios de estado y errores de integridad. Usar `/professionals` y `/specialties`, sin selector multiple. Verificar previamente si el catalogo maestro de especialidades requiere administracion adicional: hoy la API observada publica lectura ADMIN, no CRUD; abrir contrato/backend aparte solo si la especificacion de cierre lo exige.
- **Pruebas/salida:** unitarias/componentes de validacion y estado; Playwright con PostgreSQL de alta/edicion/desactivacion, error de CMP, especialidad inactiva y acceso denegado a no ADMIN.

### F4. F04 Administracion y publicacion de horarios

- **Rama propuesta:** `codex/frontend-f04-schedules` (desde `main` actualizado).
- **Commit asociado:** pendiente; Summary propuesto: `Build admin schedules and slot publication`.
- **Auditoria requerida:** **No nueva auditoria global**; revision focalizada de solapamiento y disponibilidad derivada.
- **Trabajo:** crear/editar jornada habitual del medico con varios dias seleccionables y hora de inicio/fin, especialidad de solo lectura derivada del profesional, estado y publicacion para fechas concretas. Mostrar cantidad publicada y conflictos sin permitir que ADMIN marque manualmente un turno reservado como disponible. La vista debe usar la misma fuente de disponibilidad que Paciente/Recepcion.
- **Pruebas/salida:** unitarias/componentes de dias/rangos; Playwright con PostgreSQL de crear-publicar-consultar disponibilidad, repeticion idempotente o conflicto segun contrato, horario inactivo y acceso no ADMIN. Preservar slots ya reservados.

### F5. F05 Recepcion: cita presencial y cola

- **Rama propuesta:** `codex/frontend-f05-reception` (desde `main` actualizado tras B4).
- **Commit asociado:** pendiente; Summary propuesto: `Build reception booking and waiting-room flow`.
- **Auditoria requerida:** **Si, focalizada antes de exponer datos**: matriz RECEPTIONIST, proyecciones minimas, filtros obligatorios y etapas.
- **Trabajo:** abrir en Gestion de citas; buscar paciente por tipo/numero exactos, sin listado masivo; si no existe, indicar registro F01. Elegir Especialidad -> Medico -> Fecha -> Hora, recoger motivo y revisar antes de crear cita presencial. Localizar citas con consulta filtrada/acotada y mostrar confirmacion, llegada, CHECK_IN y WAITING en el orden permitido, mas sala de espera del dia. No mostrar diagnostico, recetas ni antecedentes; no ofrecer cancelar/reprogramar ni iniciar/finalizar atencion.
- **Pruebas/salida:** componentes de selecciones y botones por etapa; Playwright/PostgreSQL para busqueda exacta, cita asistida, confirmacion/llegada, rechazo de CHECK_IN fuera del dia Lima, WAITING, cola paginada, 403 y no filtracion de motivo en consulta minima.

## 4. Bloque M: experiencia clinica completa, fuera del conteo F01-F05

Estos pasos son necesarios para el recorrido academico integrado del Word. Que el backend medico exista no significa que su portal web este terminado. Los 30 minutos del turno son informativos; el servidor controla fecha Lima y etapa, no un cierre automatico por reloj. Cada medico conserva una especialidad. El RNE `SIM-RNE` y el tipo/servicio simulados deben identificarse como tales.

### M1. Portal Medico: contexto, cola e historial

- **Rama propuesta:** `codex/frontend-medical-context-history` (desde `main` actualizado tras B3).
- **Commit asociado:** pendiente; Summary propuesto: `Build doctor context and longitudinal history views`.
- **Auditoria requerida:** **Si, focalizada** en autorizacion, privacidad y proyecciones longitudinales.
- **Trabajo:** pantalla inicial de atencion del profesional autenticado, cola propia del dia y apertura solo de cita asignada en WAITING. Mostrar identidad, cabecera de Historia Clinica, demografia disponible y antecedentes finales; resumen paginado y detalle de atenciones previas. Diferenciar ausencia de dato de resultado normal; no enseñar borradores ajenos ni fingir examenes/internaciones no registrados.
- **Pruebas/salida:** componentes de estados vacios/privacidad; Playwright/PostgreSQL con medico propio/ajeno, cita fuera de dia/etapa, historial del mismo paciente y 404 uniforme para atencion ajena.

### M2. Portal Medico: captura, receta y cierre

- **Rama propuesta:** `codex/frontend-medical-attention` (desde `main` actualizado tras M1 y B2).
- **Commit asociado:** pendiente; Summary propuesto: `Build medical drafts prescription and finalization`.
- **Auditoria requerida:** **Si, focalizada** en persistencia temporal, versiones optimistas, catalogos y cierre irreversible.
- **Trabajo:** iniciar consulta; navegar Historia Clinica, Diagnostico/tratamiento y Receta sin perder cambios; guardar borradores con version/409 y recuperacion tras recarga; buscar CIE-10, medicamentos/presentaciones y procedimientos aprobados; CRUD temporal de medicamentos completos; previsualizar y confirmar cierre unico. La receta es opcional, nunca vacia; el frontend no altera estado COMPLETED de forma independiente ni permite cerrar sin diagnostico valido. Mostrar con claridad datos expresamente simulados.
- **Pruebas/salida:** unitarias/componentes de version, validacion y navegacion; Playwright/PostgreSQL de borrador persistido, conflicto de edicion, catalogo inactivo, cierre transaccional e idempotente, rollback, receta presente/ausente y permisos. Sin catalogos cargados B2 este bloque no se considera completo.

### M3. Paciente: atenciones finalizadas y Mis recetas

- **Rama propuesta:** `codex/frontend-patient-clinical-records` (desde `main` actualizado tras M2).
- **Commit asociado:** pendiente; Summary propuesto: `Build patient finalized encounters and prescriptions`.
- **Auditoria requerida:** **Si, focalizada** en datos sensibles, propiedad de registros y snapshots.
- **Trabajo:** listar y abrir solo atenciones finalizadas propias, diagnosticos/tratamiento y receta emitida si existe. Fecha, medico, especialidad e identificadores historicos salen del snapshot/backend, no de catalogos actuales. No mostrar receta vacia ni borrador; paginacion, estados vacios, error y acceso movil.
- **Pruebas/salida:** componentes de ausencia/presencia de receta; Playwright/PostgreSQL de lectura propia/ajena, registro final, snapshot tras cambio de catalogo, ninguna receta cuando no se prescribio y sesion expirada.

## 5. Bloque V: verificacion integral y guia de demostracion

### V1. Auditoria completa de los cinco formularios y recorrido clinico

- **Rama propuesta:** `codex/integrated-five-forms-audit` (desde `main` actualizado tras F5 y M3).
- **Commit asociado:** pendiente; Summary propuesto: `Audit five forms and full clinical journey`.
- **Auditoria requerida:** **Si, integral y obligatoria**; bloque de evidencia, no maquillaje de hallazgos.
- **Trabajo:** matriz campo a campo del Word y acuerdos posteriores contra API, UI, permisos, migraciones y pruebas. Recorrido ADMIN configura profesional/horario -> PATIENT se registra y reserva o RECEPCION registra cita -> RECEPCION confirma y pasa a WAITING -> MEDICO consulta, captura, receta y cierra -> PATIENT lee final. Revisar accesibilidad, responsividad, seguridad de cookies/BFF, errores 401/403/409, concurrencia/capacidad, fecha Lima, datos sensibles y trazabilidad de catalogos. Repetir en base PostgreSQL nueva con todos los roles sinteticos.
- **Pruebas/salida:** unitarias, integracion PostgreSQL, `typecheck`, `build` y Playwright de todos los roles sin fallos; CI verde. Documentar hallazgos y abrir PR correctivos pequenos por defecto. Solo despues se declaran **cinco formularios y portal clinico terminados**.

### V2. Guia demo desde una PC recien encendida

- **Rama propuesta:** `codex/integrated-demo-runbook` (desde `main` actualizado tras V1).
- **Commit asociado:** pendiente; Summary propuesto: `Document and verify cold-start full demo`.
- **Auditoria requerida:** **Si, de reproducibilidad**: otra persona debe ejecutar la guia de principio a fin en entorno limpio; no necesita nueva auditoria funcional si V1 paso sin cambios de codigo.
- **Trabajo:** guia paso a paso para Windows/PowerShell con prerequisitos y versiones verificadas (Git, Java, Maven wrapper, Node/npm, Docker Desktop/PostgreSQL), clonacion/Fetch, archivos `.env.example` sin secretos, terminal numerada para DB, backend y frontend, comandos exactos de arranque, migraciones/carga de catalogos, credenciales sinteticas, URLs, health checks, recorrido F01-F05 y Medico/Paciente, consultas de verificacion, fallos frecuentes y apagado. Separar comandos de instalacion unica de los de cada demostracion. Prohibir datos reales.
- **Pruebas/salida:** ejecucion literal de la guia desde estado limpio, con tiempos/prerequisitos observados y resultado de cada comando. Actualizar README enlazando la guia; Repository Validation verde antes de fusionar.

## 6. Dependencias, decisiones y alcance diferido

Orden recomendado: **B1 -> B2 -> B3 -> B4 -> B5 -> B6 -> F0 -> F1/F2 -> F3 -> F4 -> F5 -> M1 -> M2 -> M3 -> V1 -> V2**. B3/B4/B5 pueden reordenarse tras B1/B2 si `main` se actualiza entre ramas; F1/F2 pueden avanzar en ramas separadas tras F0. Nunca mezclar dos entregables grandes en un solo PR. Cada fragmento de este documento puede enviarse literalmente como encargo posterior; al iniciar, volver a verificar el estado real.

**Decisiones/insumos que pueden abrir un bloque nuevo, no se presuponen resueltos:** archivos de catalogos clinicos con procedencia/licencia/version; si el docente exige CRUD de especialidades, resultados de examenes o ingresos/altas hospitalarios estructurados; politica exacta de eliminacion administrativa de borradores; cuentas/demo y mecanismos de seed compatibles con rol restringido. Si no se aprueban como alcance del cierre academico, registrarlos explicitamente como limitaciones/futuro, sin inventar datos o funcionalidades.

**Fuera de este cierre salvo nuevo pedido:** cancelar/reprogramar desde Recepcion; consultorio real/asignacion fisica; fotografias de medicos; integraciones con aseguradoras; operacion clinica de produccion. No permitir que estas funciones futuras bloqueen artificialmente el recorrido academico acordado, pero tampoco presentarlas como implementadas.

**Referencias base:** `HospitalPlatform_Integrado.docx`, `G1-FIVE-FORMS-ADMIN-RECEPTIONIST-SCOPE-AUDIT.md`, `BACKEND-COMPLETION-AUDIT.md`, `BACKEND-PENDING-CONTRACTS-AND-PHASES.md`, `ESTADO_BACKEND.md`, codigo de `apps/backend`, `apps/frontend` y migraciones de `database`. La evidencia viva de Git y codigo prevalece sobre estados historicos de esos informes.
