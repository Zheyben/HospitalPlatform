# Análisis técnico del backend Médico

Fecha: 2026-10-05. Base revisada: `main` en `a151a2b` (PR #55 de sala de espera fusionado). Este documento diseña trabajo futuro; no implementa API, migración ni atención clínica. Alcance exclusivo de backend.

## Autoridad y límite

- La especificación funcional vigente es `HospitalPlatform_Integrado.docx`, secciones 2, 3.5-3.6, 6-11, junto con los acuerdos del equipo: una especialidad por médico y cinco etapas médicas posteriores a este análisis.
- La auditoría G.1 de cinco formularios excluyó clínica y receta. Su conclusión de no requerir una migración adicional no aplica al módulo Médico.
- `docs/DOMAIN-BASELINE.md` describe el alcance operativo anterior y considera clínica futura. No usar sus afirmaciones de ausencia clínica para descartar el Word posterior. Antes de construir las etapas, registrar la ampliación académica de alcance y sus decisiones de seguridad; no presentarla como política oficial de un hospital.
- No se diseñan triaje independiente, facturación, farmacia/inventario, firma digital válida, PDF clínico ni integración externa. No usar datos reales de pacientes en desarrollo o pruebas.

## Estado real del código

| Requisito | Evidencia actual | Brecha |
| --- | --- | --- |
| Médico autenticado y especialidad única | `users`, `professionals`, `professional_specialties`; V6 limita la asociación a una. `ProfessionalLookupService` comprueba vínculo activo. | Falta contexto propio, turno clínico y lectura autorizada de paciente. Los profesionales legados pueden carecer de usuario o asociación. |
| Cita y flujo | `AppointmentService` y V5 implementan `WAITING -> IN_ATTENTION -> FINISHED/COMPLETED` con locks, capacidad y auditoría. | `POST /appointments/{id}/start-attention` no crea atención; `POST /appointments/{id}/complete` puede cerrar sin nota, diagnóstico ni receta. No son cierre clínico. |
| Paciente e historia | `patients` contiene documento, nacimiento, teléfono, seguro y dirección; `users` contiene nombres. `PatientRegistrationService.register` ya es transaccional. | No hay número/cabecera de Historia Clínica, sexo, estado civil, ocupación, distrito, grado de instrucción ni contacto de emergencia. F01 actual acepta nueve campos y no crea historia. |
| Contexto y catálogos | `catalogs` solo contiene paquetes marcador; V1-V6 no crean tablas CIE-10, medicamentos, presentaciones ni procedimientos. | No hay búsqueda válida para Typeahead ni contenido histórico clínico. Una lista vacía debe permanecer vacía, no convertirse en texto libre. |
| Persistencia clínica | No hay entidades/tablas de atención, borrador, diagnóstico, receta, ítems ni órdenes. | No se puede conservar navegación clínica ni cerrar de forma atómica. |
| Lectura posterior | Paciente solo consulta citas y su resumen. | Faltan atenciones y recetas finalizadas, filtradas por la identidad autenticada. |
| Auditoría y permisos SQL | `AuditLogService.record` exige una transacción; V5 revoca ejecución pública de funciones y el rol runtime documentado solo tiene grants de módulos anteriores. | Hay que añadir eventos de atención sin registrar texto clínico en `audit_logs` y probar grants mínimos para nuevas tablas/funciones. |

## Contratos propuestos

Todos los contratos siguientes son propuestas, no endpoints existentes. Mantener `ROLE_PROFESSIONAL` y resolver el `professionalId` desde `CurrentUserService`; nunca confiar en un ID de médico enviado por el cliente. Verificar en servicio, además del controller, el profesional activo asignado a la cita. `PATIENT` se resuelve desde la sesión para lecturas propias. `ADMIN` y `RECEPTIONIST` no reciben información clínica por su rol.

| Etapa | Contrato propuesto | Datos y límite |
| --- | --- | --- |
| 1 | `GET /medical/me/context` | Nombre, CMP, especialidad única, identificador RNE **simulado** y turno propio del día; solo cita asignada en WAITING/IN_ATTENTION. No lista global. |
| 1 | `GET /medical/appointments/{id}/context` | Identidad e historia del paciente, cita, antecedentes, alergias, medicación habitual, última atención finalizada y listas históricas disponibles. Solo médico asignado con flujo permitido; ausencias como `null`/`[]`. |
| 1 | `GET /medical/catalogs/icd10`, `/medications`, `/procedures` | Búsqueda por código/descripción o nombre, `q` obligatorio y límite acotado; IDs y etiquetas activos. Presentaciones se obtienen por medicamento seleccionado, no como lista universal. |
| 2 | `POST /medical/appointments/{id}/start` | Crea o devuelve la misma atención abierta para la cita WAITING, registra inicio y cambia a IN_ATTENTION en una transacción. |
| 2 | `GET/PUT /medical/encounters/{id}/draft` | Borrador íntegro de historia actual, evaluación, diagnóstico, tratamiento y receta temporal; `version` para detectar edición concurrente. No hay receta definitiva al cambiar de paso. |
| 3 | `POST /medical/encounters/{id}/finalize` | Recibe versión y contenido clínico completo, valida catálogos/obligatorios, persiste final, marca FINISHED/COMPLETED y devuelve la misma atención si se reintenta el mismo cierre. |
| 4 | `GET /medical/me/encounters` y `GET /medical/me/prescriptions` | Solo PATIENT autenticado, solo registros finalizados propios, lectura mínima y paginada. No borradores ni citas ajenas. |

No inferir una ventana de inicio tardío o una finalización automática a los 30 minutos: el Word define cronómetro informativo pero no cierre automático. El filtro exacto del "turno actual" necesita acuerdo antes de usarlo como regla que bloquee atención.

## Modelo y migraciones incrementales

1. **Identidad e historia:** migración nueva, sin editar V1-V6, para `clinical_records` (`patient_id` único, número de historia único/inmutable, `opened_at`). Generar número mediante secuencia de PostgreSQL y retrocrear cabeceras para pacientes legados sin inventar datos demográficos. Extender `patients` solo con campos faltantes aceptados por el contrato de registro; conservar `NULL` en legados. Coordinar la obligatoriedad nueva de sexo/dirección con la compatibilidad de F01 antes de imponerla a clientes existentes. RNE simulado debe llevar marcador explícito, persistirse y nunca parecer credencial real.
2. **Catálogos:** `icd10_codes`, `medications`, `medication_presentations` y `procedures` con claves estables, vigencia y FKs. No borrar físicamente códigos o presentaciones referenciados. Registrar procedencia, versión y autorización de carga de datos; no inventar códigos, medicamentos o concentraciones para que una búsqueda parezca completa. Los valores fijos del Word pueden vivir como restricciones/enums validados por backend.
3. **Atención temporal:** `clinical_encounters` con `appointment_id UNIQUE`, paciente, profesional, inicio, fin y estado; `encounter_drafts` con contenido temporal, versión y fecha de actualización. Una atención por cita. El borrador puede persistir para sobrevivir navegación/recarga, pero no se publica como registro clínico definitivo ni como receta. Definir retención/borrado de borradores abandonados.
4. **Atención final:** tablas normalizadas para antecedentes de la consulta, evaluación/signos vitales, diagnósticos con FK CIE-10, indicaciones/tratamiento, órdenes si existen, receta e ítems con FK a presentación. Mantener snapshots legibles de etiquetas relevantes para que cambios de catálogo/nombre no alteren el significado histórico. Campos clínicos no medidos quedan `NULL`, nunca cero; receta vacía no se crea. Timestamps de eventos como instantes (`timestamptz`), fecha/hora del turno interpretadas en `America/Lima`.
5. **Integridad:** `UNIQUE(appointment_id)` y `UNIQUE(encounter_id)` donde corresponda; checks positivos para dosis, duración, cantidad y mediciones; FK de presentación al medicamento y vigencia comprobada en el servicio al finalizar. La migración de completitud solo debe proteger nuevas transiciones a COMPLETED: no fabricar registros clínicos para citas históricas ya completadas.

El número exacto de versión Flyway se elige al implementar, después de actualizar `main`. Cada etapa agrega únicamente las tablas/grants que usa; no hacer una migración monolítica por anticipado.

## Transacciones, idempotencia y privacidad

- Inicio: lock de cita con el gateway actual, comprobar `CONFIRMED/WAITING`, profesional asignado/activo y ausencia de otra atención abierta; crear `clinical_encounters` y ejecutar la transición con auditoría en la misma transacción. Repetir sobre la misma cita/atención abierta devuelve el mismo ID. El índice único resuelve carreras además del lock.
- Cierre: lock de cita/atención en orden consistente, validar versión del borrador o payload final, campos obligatorios y referencias activas; escribir los datos finales y marcar la atención finalizada, cambiar estado vía `CapacityGateway.complete` y auditar dentro de **una** transacción. Así el guard de DB puede comprobar que ya existe atención final al pasar a COMPLETED, sin exponerla antes del commit. Fallo en cualquier escritura, capacidad o auditoría revierte todo. Guardar un hash canónico del contenido final para que el mismo reintento devuelva el resultado previo; contenido diferente tras cierre devuelve 409.
- Al activar cierre clínico, retirar o delegar `POST /appointments/{id}/complete`: el endpoint actual permite COMPLETED sin clínica. Proteger también la transición nueva en DB para que `capacity_appointment_complete` no pueda saltarse el registro final; aplicar el guard solo a transiciones nuevas. Revisar igualmente `/start-attention` para que no cree IN_ATTENTION sin atención abierta.
- No introducir texto clínico, recetas ni datos sensibles completos en logs HTTP o `audit_logs.old_values/new_values`. Registrar actor, ID de atención, acción y timestamps. Consulta posterior del paciente exige propiedad en la consulta SQL, no filtrado después de serializar. Profesional no puede leer pacientes/citas ajenos aun con UUID conocido.
- Los datos simulados RNE, tipo de atención y servicio deben almacenarse y devolverse con bandera/etiqueta explícita. No utilizar datos de prueba para sustituir información clínica ausente.

## Cinco etapas y criterios de salida

| Etapa | Entrega backend en rama pequeña | Criterio verificable |
| --- | --- | --- |
| 1. Contexto, catálogos y autorización | Cabecera de historia/retrocreación, datos de contexto propios y búsqueda acotada de catálogos con procedencia; pruebas de permisos. Separar migración de contrato si el diff crece. | Médico propio ve contexto; otro médico, Recepción y Admin no ven clínica; catálogo inválido/inactivo no se ofrece. F01 sigue funcionando. |
| 2. Captura clínica y receta temporal | Inicio idempotente que crea atención, borrador versionado y validaciones de captura; no cierre definitivo. | Navegación/recarga conserva borrador; filas de receta incompletas no se aceptan; solicitud repetida no duplica atención. |
| 3. Cierre transaccional e idempotente | Tablas finales, validación fuerte, cierre único y hardening del endpoint operativo antiguo/DB. | Una sola atención final por cita; payload distinto tras cierre da 409; fallo parcial revierte datos, FINISHED, COMPLETED y auditoría. |
| 4. Lectura posterior del paciente | Resúmenes/detalles propios de atención y receta finalizadas, limitados y paginados. | Paciente A no ve B, borradores ni receta vacía; historial conserva significado tras desactivar catálogo/profesional. |
| 5. Integración PostgreSQL | Flujo ADMIN -> PATIENT/RECEPTIONIST -> PROFESSIONAL -> PATIENT, con Testcontainers y rol runtime restringido. | Flyway desde cero y upgrade, grants mínimos, concurrencia/reintentos, rollback y regresión de capacidad/historial aprobados en Backend CI y Repository Validation. |

## Decisiones a cerrar antes de la etapa 1

1. **Registro F01:** acordar despliegue compatible de sexo/dirección y demás campos ahora ausentes, y quién completa registros legados. No imponer campos obligatorios por sorpresa en el contrato de nueve campos actual.
2. **Catálogos:** aprobar fuente/licencia, versión, carga y responsable de CIE-10, medicamentos, presentaciones y procedimientos. Sin datos verificables no se puede declarar la búsqueda clínica lista.
3. **Turno actual:** definir si la ventana de 30 minutos bloquea inicio tardío, cómo se atiende un retraso y si se muestra WAITING fuera de su intervalo. CHECK_IN solo valida fecha, no esa ventana.
4. **RNE simulado y borradores:** aprobar formato claramente ficticio, retención de borradores y política de acceso/depuración de datos clínicos de prueba.
5. **Gobernanza:** reflejar explícitamente que el Word y acuerdo actuales amplían el baseline académico anterior; confirmar que la entrega es simulación, no sistema clínico apto para producción.

Siguiente rama sugerida tras fusionar este análisis y cerrar las decisiones bloqueantes: `codex/medical-context-catalogs-auth` desde `main` actualizado. No fusionar ninguna etapa con Backend CI o Repository Validation fallidos.
