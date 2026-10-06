# Auditoria de cierre del backend

Fecha: 2026-10-06. Base: `main` en `2113282` (PR #72 fusionado). Rama documental: `codex/backend-completion-audit`. Alcance: backend academico; no se audita ni implementa frontend en esta etapa.

Contratos detallados y orden de implementacion: [BACKEND-PENDING-CONTRACTS-AND-PHASES.md](BACKEND-PENDING-CONTRACTS-AND-PHASES.md).

## Metodo y criterio de cierre

Se contrastaron `HospitalPlatform_Integrado.docx`, la auditoria G.1, `ESTADO_BACKEND.md`, contratos HTTP, migraciones V1-V16, servicios, autorizacion, pruebas y guia de ejecucion. La auditoria G.1 es historica: sus brechas F03/F04/G.6 y lectura global de Recepcion ya fueron corregidas en PR posteriores. Se considera cerrado el backend academico solo cuando el recorrido Administrador -> Paciente/Recepcion -> Medico -> Paciente funcione con datos de catalogo instalables y trazables, permisos y capacidad preservados, documentacion ejecutable y pruebas PostgreSQL verdes. Esto no equivale a certificacion para uso clinico real.

Verificacion local: `mvnw.cmd -q clean verify` completo con Docker/Testcontainers y PostgreSQL 16; 180 pruebas unitarias y 106 de integracion, sin fallos, errores ni omisiones. El primer intento dentro del sandbox no pudo acceder a Docker; la repeticion con acceso al motor local termino con codigo 0.

## Implementado y comprobado en codigo

- F01 crea cuenta, paciente y cabecera de historia en una transaccion; F02 reserva contra capacidad real. F03 administra profesional con una especialidad; F04 publica turnos.
- Recepcion tiene busqueda documental exacta, disponibilidad, cita presencial, consulta acotada por paciente sin motivo, confirmacion, CHECK_IN con fecha civil `America/Lima`, WAITING y sala de espera del dia. `GET /appointments` ya excluye RECEPTIONIST.
- Medico tiene contexto propio, inicio de atencion, borradores versionados de historia/evaluacion/receta, cierre transaccional e idempotente, y lectura posterior propia del paciente. PR #72 agrega resumen paginado de atenciones previas para el medico asignado. `MedicalIntegratedRuntimeIT` recorre el flujo con PostgreSQL y un rol SQL restringido de prueba.
- Existen pruebas de concurrencia/capacidad, autorizacion negativa, fecha local, rollback y cierre sin receta vacia. Esta auditoria no encontro motivo para alterar esas reglas.

## Hallazgos priorizados

### P0 - cierre academico bloqueado

1. **Catalogos clinicos sin carga reproducible.** V8 crea `catalog_sources`, `icd10_codes`, `medications`, `medication_presentations` y `procedures`, pero las migraciones oficiales no insertan filas. Los registros observados estan en fixtures de prueba. `MedicalFinalizationService.readAndValidate` exige CIE-10 activo para cerrar; una instalacion nueva queda sin diagnosticos seleccionables y no puede completar el recorrido. El usuario confirma aprobacion del equipo y docente; eso no aporta por si solo los archivos, version, referencia de licencia y valores concretos. Crear una carga academica reproducible a partir de la fuente efectivamente aprobada, con metadatos y validacion de importacion, sin inventar codigos ni medicamentos. Evidencia: `database/migrations/V8__medical_catalog_structure.sql`, `MedicalFinalizationService.java`, `MedicalIntegratedRuntimeIT.java`.

### P1 - diferencias con la especificacion funcional

2. **Seguro libre y sin catalogo normalizado.** PAT-F01 y reglas 10.4/11 del Word piden SIS, EsSalud y Particular como opciones de catalogo con integridad referencial. El request acepta cualquier `String insurance`, `patients.insurance` es texto y no existe catalogo de seguros. Preservar compatibilidad de los nueve campos antiguos al migrar el contrato. Evidencia: `RegisterPatientRequestDTO.java`, `Patient.java`, V1.
3. **Datos demograficos incompletos.** F01 ya acepta direccion y sexo opcionales por acuerdo posterior, pero no captura estado civil, ocupacion, distrito, grado de instruccion, numero de afiliacion ni contacto de emergencia/parentesco/telefono. La cabecera/contexto/snapshot medico tampoco los expone. El acuerdo de compatibilidad permite que sean opcionales; no autoriza omitirlos del alcance final. Evidencia: `RegisterPatientRequestDTO.java`, `MedicalAppointmentContextDTO.java`, `Patient.java`.
4. **Historia longitudinal medica solo parcial.** PR #72 ofrece un resumen de atenciones previas (`MedicalPriorEncounterDTO`) con diagnostico principal, no detalle navegable de antecedentes, recetas, ordenes/resultados, hospitalizaciones/procedimientos y alertas historicas que el Word describe para el medico. Los datos finalizados y algunas secciones de historia existen, pero no una lectura longitudinal integral. Definir proyeccion minima segura y distinguir datos nunca registrados (p. ej. resultado de examen o internacion) de datos historicos existentes; no fabricar historial. Evidencia: `MedicalContextController.java`, `MedicalPriorEncounterDTO.java`, `PatientClinicalReadController.java`.
5. **Cancelar/reprogramar siguen autorizados a Recepcion.** El acuerdo aplaza estas acciones *desde el flujo de Recepcion* y el estado previo dice que no exponerlas en el portal; la API aun permite `RECEPTIONIST` en ambas rutas. Antes de cerrar, resolver expresamente si aplazamiento significa solo UI o tambien permiso HTTP. Si son acciones futuras de ese rol, retirar el permiso con pruebas negativas en una rama pequena; preservar PATIENT/ADMIN y la cadena historica. Evidencia: `AppointmentController.java`.

### P2 - operacion y criterios pendientes

6. **Rol SQL de despliegue no reproducible.** La prueba integrada usa `src/test/resources/medical-runtime-role.sql`; el README reconoce que es fixture y no politica de produccion. Falta instruccion/script de provisionamiento para propietario Flyway y rol de aplicacion con privilegios minimos, mas verificacion en una base nueva. No copiar a produccion credenciales o grants de prueba. Evidencia: `MedicalIntegratedRuntimeIT.java`, `apps/backend/README.md`.
7. **Politica de borradores incompleta.** El equipo acordo conservar hasta cierre o eliminacion administrativa definida. No hay contrato de eliminacion ni politica documentada de autorizacion/auditoria para ese caso. Para cerrar el backend academico, documentar retencion sin purga automatica; implementar eliminacion solo tras definir su semantica y pruebas de integridad. Evidencia: `MedicalHistoryDraftController.java`, `MedicalDraftService.java`, `ESTADO_BACKEND.md`.
8. **Documentacion de ejecucion desactualizada.** README enumera migraciones hasta V14 aunque existen V15/V16 y no describe la futura carga de catalogos. Actualizarlo como parte del cierre, con entorno de prueba y sin secretos. `Backend CI` se dispara solo por cambios en `apps/backend/**`, `database/**` o su workflow: un PR solo documental no ejecutara esa suite, aunque `Repository Validation` si correra.

## Secuencia propuesta en ramas pequenas

1. `codex/backend-catalog-seed` desde `main` actualizado: integrar fuente clinica aprobada con procedencia/version/licencia, importacion idempotente y prueba de instalacion nueva/cierre real. Si los archivos aprobados no estan disponibles, no marcar P0 como resuelto.
2. `codex/backend-insurance-catalog` desde `main` actualizado: catalogo institucional, integridad y adaptacion compatible de F01; pruebas de valores invalidos y legado.
3. `codex/backend-patient-demographics` desde `main` actualizado: campos opcionales restantes, validacion, contexto y snapshots pertinentes sin exigirlos a clientes F01 existentes.
4. `codex/backend-medical-longitudinal-history` desde `main` actualizado: lectura paginada/detalle solo para medico asignado, sin borradores ni datos de otros pacientes.
5. Rama breve de permisos de Recepcion, si se confirma que el aplazamiento tambien aplica al API; rama de provisionamiento/guia de ejecucion y retencion; luego verificacion integrada de instalacion limpia.

## Puertas de salida

- Cada rama parte de `main` actualizado, actualiza `ESTADO_BACKEND.md`, pasa pruebas locales relevantes y espera `Backend CI` y `Repository Validation` verdes antes del merge. En PR solo documental, ejecutar la suite backend localmente o activar una verificacion equivalente, ya que el workflow no se dispara por esos paths.
- No usar datos reales de pacientes ni catalogos inventados. La aprobacion humana de catalogos queda asentada; faltan sus artefactos concretos para una carga verificable.
- Al terminar las ramas, repetir `mvn clean verify` con Docker/Testcontainers, un recorrido HTTP con base nueva y rol runtime, revisar permisos negativos y publicar la guia de ejecucion. Solo entonces declarar cerrado el backend y abrir la etapa de diseno frontend.
