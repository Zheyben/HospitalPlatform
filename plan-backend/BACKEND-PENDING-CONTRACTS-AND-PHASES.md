# Contratos pendientes y plan de cierre del backend

Fecha: 2026-10-06. Base comprobada: `main` en `2113282`; rama documental `codex/backend-completion-audit`. Este documento convierte la [auditoria de cierre](BACKEND-COMPLETION-AUDIT.md) en trabajo verificable. Alcance actual: solo backend academico. No implementar frontend ni usar datos de pacientes reales.

## Decisiones vigentes

- El equipo y el docente aprobaron el uso de catalogos clinicos, segun confirmacion del usuario. **Aprobacion de uso no equivale a datos cargados**: no hay en el repositorio archivos de CIE-10, medicamentos/presentaciones o procedimientos con valores, version y referencia de uso. Este documento no inventa codigos, farmacos, dosis ni procedimientos. La primera carga solo puede usar los artefactos concretos aprobados y trazables.
- F01 conserva compatibilidad con sus nueve campos originales. Los nuevos datos demograficos son opcionales. Un medico tiene una especialidad. La fecha civil de CHECK_IN e inicio medico se interpreta en `America/Lima`; los 30 minutos del turno son informativos.
- Cancelar y reprogramar **desde Recepcion** quedan aplazados, incluso si las rutas legadas existen. No tocar historial ni reglas de capacidad. Los borradores se conservan hasta cierre o hasta una eliminacion administrativa que tenga politica definida; no hay purga automatica.

## 1. Catalogos clinicos aprobados

**Estado real.** V8 ya define `medical_catalog_sources`, `icd10_codes`, `medications`, `medication_presentations` y `procedures`, con procedencia, relaciones y estados activos. Las busquedas medicas son literales y acotadas; la finalizacion exige un CIE-10 activo, y una presentacion activa si hay receta. Las migraciones V1-V16 no cargan registros de esos cuatro catalogos; las pruebas usan fixtures sinteticas.

| Catalogo | Campos minimos ya modelados | Artefacto de carga verificable pendiente |
| --- | --- | --- |
| CIE-10 | Codigo, descripcion, activo, fuente | Archivo aprobado con codigo/descripcion, emisor, version, fecha y referencia de licencia o permiso |
| Medicamentos | Nombre generico, comercial opcional, activo, fuente | Archivo aprobado con identificador estable y nombres; sin dosis ni indicaciones inventadas |
| Presentaciones | Medicamento padre, nombre, concentracion, forma farmaceutica, activo | Archivo aprobado relacionado por clave estable al medicamento; no presentar opciones huerfanas |
| Procedimientos/examenes | Codigo, nombre, activo, fuente | Archivo aprobado con codigo/nombre, emisor, version y referencia de uso |

**Contrato de importacion propuesto.** Preparar un manifiesto versionado por fuente (`catalog_type`, `source_name`, `source_version`, `license_reference`, aprobador/fecha y checksum del archivo), validar esquema, duplicados, referencias y campos vacios antes de una transaccion de carga. `approved_by_user_id` exige un usuario existente: el procedimiento de instalacion debe crear/identificar al aprobador antes de importar; no poner una UUID ficticia en una migracion. Repetir el mismo paquete debe ser idempotente. Una version nueva no debe borrar ni alterar snapshots historicos; desactivar solo con semantica documentada. El rol runtime de la aplicacion no debe recibir escritura libre de catalogos.

**Criterio de salida.** Instalacion PostgreSQL limpia + importacion reproducible; busqueda devuelve opciones activas reales; presentaciones se limitan al medicamento padre; IDs invalidas/inactivas se rechazan al guardar y al cerrar; se puede cerrar una atencion con un diagnostico del paquete aprobado. Conservar un manifiesto de procedencia en el repositorio si la licencia permite distribuir los datos; si no, documentar ubicacion y carga externa autorizada sin publicar el dataset.

## 2. Seguro normalizado

**Fuente funcional aprobada.** El Word enumera exactamente `SIS`, `EsSalud` y `Particular` para el registro, y exige valores de catalogo con integridad referencial. Hoy `RegisterPatientRequestDTO.insurance` acepta cualquier texto y `patients.insurance` es `VARCHAR(150)`.

**Contrato objetivo.** Crear catalogo estable `insurance_providers` (clave, etiqueta, activo) y asociar al paciente mediante FK. Publicar lectura de opciones activas para el formulario. El contrato nuevo enviara `insuranceId` y devolvera identificador y etiqueta. Durante la transicion, aceptar el campo legado `insurance` de los nueve campos solo cuando coincida, tras normalizacion controlada, con una de las tres opciones; si llegan ambos, deben identificar la misma. Rechazar valores libres y no convertir silenciosamente desconocidos a `Particular`. La migracion de datos existentes debe informar valores no mapeables y detenerse de forma segura, no perderlos. Para `Particular`, el numero de afiliacion queda `NULL`.

**Pruebas de salida.** Unitarias de normalizacion, dualidad/contradiccion de campos y rechazo de texto libre; integracion PostgreSQL de tres opciones, FK, backfill con datos heredados, rollback con valor desconocido, registro F01 de nueve campos y registro nuevo por ID. Verificar que el contexto medico lea la etiqueta correcta y que el snapshot final no cambie al renombrar un catalogo.

## 3. Datos demograficos opcionales

**Estado real.** F01 ya permite `address` y `sex` opcionales. Faltan estado civil, ocupacion, distrito, grado de instruccion, numero de seguro/afiliacion, nombre de contacto de emergencia, parentesco y telefono de emergencia. El Word los muestra cuando existan; el acuerdo posterior preserva F01 sin volverlos obligatorios.

**Contrato objetivo.** Agregar columnas nullable con limites y validacion de longitud/formato; `NULL` significa no declarado y no se sustituye por cadenas inventadas. El telefono de emergencia, cuando exista, usa validacion estructurada consistente con el telefono principal. El numero de afiliacion solo se acepta para seguro que lo admita; `Particular` lo conserva `NULL`. Incorporar campos a registro, lectura propia, edicion administrativa autorizada y contexto medico; congelar en el registro final solo los datos cuya lectura historica requiere el Word, sin reescribir cierres anteriores. Mantener la identidad y cabecera de Historia Clinica en la misma transaccion del registro.

**Pruebas de salida.** Unitarias de opcionalidad, formatos, normalizacion y reglas de afiliacion; PostgreSQL de alta F01 antigua/nueva, atomicidad, migracion de legados con `NULL`, actualizacion autorizada, lectura medica y snapshots inmutables. Probar que un registro antiguo sigue funcionando sin enviar ningun campo nuevo.

## 4. Lectura medica longitudinal

**Estado real.** `GET /medical/appointments/{id}/history` devuelve una pagina de `MedicalPriorEncounterDTO` con fecha, medico, especialidad y diagnostico principal. Las tablas finales guardan historia, evaluacion, diagnostico, tratamiento, orden y receta. No existe una lectura de detalle historico para el medico equivalente a todo lo que la consulta actual necesita.

**Contrato objetivo.** Mantener el resumen paginado y agregar `GET /medical/appointments/{appointmentId}/history/{encounterId}` para el detalle de una atencion previa finalizada del **mismo paciente**. La autorizacion debe partir de la cita actual asignada al medico activo, en fecha Lima y etapa permitida, igual que el resumen; nunca confiar en un `patientId` enviado por el cliente. Filtrar en SQL por paciente, `FINALIZED` y `COMPLETED/FINISHED`, excluir la atencion actual y devolver 404 uniforme para ID ajena/inexistente. Exponer identificacion del episodio, historia personal/familiar/ginecologica registrada, alergias/alertas declaradas, diagnosticos CIE-10 y snapshots, plan, ordenes/procedimientos con estado realmente persistido y receta/medicamentos si existen. Listas ausentes son vacias; valores no capturados son `NULL`. No exponer borradores, no duplicar receta vacia y no convertir texto de hospitalizaciones en un evento estructurado ficticio.

**Limite honesto del contrato.** El Word menciona resultados de examenes e internaciones estructuradas, pero el backend actual no posee un flujo que registre resultados o ingresos/altas independientes. El detalle puede mostrar ordenes y el texto de hospitalizaciones historicas, no resultados o episodios hospitalarios inventados. Si el equipo exige gestion completa de resultados/internacion para el cierre academico, se abre un bloque funcional separado con modelo, permisos y pruebas; no se disfraza como una proyeccion.

**Pruebas de salida.** Unitarias de validacion de pagina/proyeccion y autorizacion; PostgreSQL de mismo paciente/otro paciente, otro medico, fecha/etapa, finalizado/borrador, receta presente/ausente, catalogo o profesional luego inactivo, orden sin resultado, paginacion estable e historial inalterado. Las consultas deben ser acotadas y evitar lectura masiva.

## 5. Otros pendientes de backend

- **Permisos de Recepcion:** retirar `RECEPTIONIST` de `POST /appointments/{id}/cancel` y `/reschedule` para que el API refleje el aplazamiento; conservar PATIENT/ADMIN y las transiciones de capacidad existentes. Probar HTTP 403 y regresion de operaciones permitidas.
- **Borradores:** documentar que no hay purga automatica y que el cierre congela el registro final. No habilitar borrado administrativo hasta definir actor, condiciones, auditoria y efecto sobre encuentros cerrados; no es un atajo para borrar historia clinica.
- **Provisionamiento:** separar propietario de migraciones Flyway y rol runtime con grants minimos; la fixture `medical-runtime-role.sql` es prueba, no procedimiento de despliegue. Crear pasos reproducibles para base nueva y probarlos con PostgreSQL.
- **Documentacion:** actualizar README/migraciones, configuracion sin secretos, catalogos, bootstrap y errores conocidos. La guia de demostracion completa desde PC recien encendida se escribe y verifica despues de cerrar backend y construir frontend; incluira prerequisitos, terminales numerados, comandos exactos, URLs, cuentas sinteticas, recorrido por roles, comprobaciones y apagado.

## Plan de bloques y fases

Cada fila representa una rama pequena desde `main` actualizado, PR propio, actualizacion de `ESTADO_BACKEND.md`, pruebas unitarias + integracion PostgreSQL aplicables y espera de **Backend CI** y **Repository Validation** verdes antes de fusionar. Si cambia el orden por disponibilidad del dataset clinico, no se cambia el criterio de salida.

| Fase | Rama propuesta | Entregable y puerta de salida |
| --- | --- | --- |
| 0. Contratos y plan | `codex/backend-completion-audit` | Este documento y auditoria listos para revision/publicacion; sin cambio de codigo ni afirmacion de CI no ejecutado |
| 1. Seguro | `codex/backend-insurance-catalog` | Tres opciones normalizadas, backfill seguro, F01 antiguo/nuevo compatibles; tests unitarios y PostgreSQL |
| 2. Demografia | `codex/backend-patient-demographics` | Campos opcionales, contexto y snapshots coherentes; tests de compatibilidad, atomicidad y PostgreSQL |
| 3. Catalogos clinicos | `codex/backend-catalog-seed` | Paquete de datos aprobados, manifiesto, carga idempotente y cierre real en base nueva; tests unitarios y PostgreSQL. Requiere artefactos concretos de fuente antes de marcarse completa |
| 4. Historia longitudinal | `codex/backend-medical-longitudinal-history` | Resumen y detalle autorizados, snapshots y ausencias honestas; tests unitarios, RBAC y PostgreSQL |
| 5. Permisos y retencion | `codex/backend-reception-permissions` | Recepcion sin cancelar/reprogramar, regresion de capacidad y politica de borradores documentada; tests unitarios/HTTP/PostgreSQL |
| 6. Operacion | `codex/backend-runtime-provisioning` | Roles SQL, bootstrap e instrucciones backend reproducibles; prueba PostgreSQL con usuario runtime restringido |
| 7. Auditoria final | `codex/backend-final-audit` | Base limpia, `mvn clean verify`, recorrido integrado, permisos negativos, matriz Word/codigo/tests y README vigentes; cero hallazgos bloqueantes antes de declarar backend cerrado |
| 8. Posterior, fuera de alcance actual | Nueva planificacion frontend | Diseno e implementacion frontend; luego guia demo integral desde arranque de PC, terminal por terminal, y verificacion de punta a punta |

**Regla de pruebas por fase.** Ejecutar pruebas unitarias focalizadas y PostgreSQL/Testcontainers focalizadas durante el cambio, despues `mvn clean verify` completo antes de entregar la rama. Registrar numeros de pruebas, fallos y migracion aplicada en `ESTADO_BACKEND.md`. Si Docker no esta disponible, el bloque no se declara verificado. No fusionar con controles fallidos o pendientes.

**Regla de avance.** La fase 3 es el bloqueo funcional principal para un cierre clinico en instalacion limpia. Las fases 1 y 2 pueden avanzar mientras se incorporan los archivos concretos de catalogos; ninguna fixture sintetica sustituye la carga aprobada. Tras la fase 7 se decide explicitamente si el backend academico esta cerrado. Solo entonces comienza frontend y, al final, la guia demo integral.
