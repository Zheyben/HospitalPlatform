# ETAPA D.1 - Diagrama general de casos de uso

## Objetivo y autoridad

El archivo `AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.puml` modela unicamente el
incremento Agenda Availability + Specialty Policy de HOSPITALPLATFORM, caso
de estudio academico. La autoridad de las decisiones es el proyecto/docente;
no se atribuye aprobacion institucional al Hospital de Huaycan. El diagrama
se basa en C.1-C.4, SRS, DOMAIN-BASELINE, DOMAIN-DECISION-REGISTER,
API Specification, MODULE-CONTRACTS-ARCHITECTURE, ADR-001/005/006/007/009,
controllers, services, DTOs, contratos, entidades, seguridad, tests y V1-V3.
Una tabla o decision aprobada no equivale a una operacion Java implementada.

## Limite del sistema y actores

El rectangulo `HospitalPlatform` contiene solo los casos del incremento.
ADMIN participa en gestion profesional, horarios, disponibilidad operativa y
solicitud de cita. Su asociacion con las dos capacidades de especialidades es
**conceptual**, no un permiso efectivo. PATIENT y RECEPTIONIST participan en
la solicitud de cita actual y, solo conceptualmente segun DEC-007, en consulta
sanitizada. PATIENT reserva para su perfil propio; ADMIN/RECEPTIONIST pueden
indicar un paciente activo segun las reglas existentes.

PROFESSIONAL no tiene una asociacion con estos casos y por eso no se dibuja:
su rol existe, pero DEC-007 no le concede consulta de disponibilidad. Tampoco
se dibuja `Sistema`: la exclusion de doble reserva es comportamiento tecnico
interno, no un actor/rol que inicie una operacion. No se agrega ningun rol.

## Casos y estados

| Casos del diagrama | Estado mostrado | Fuente y limite |
|---|---|---|
| Crear, consultar y actualizar profesional | `<<PARCIAL>>` dentro de gestion profesional RF-007 | `ProfessionalController` ADMIN implementa las operaciones basicas; C.4 CU-D1 es parcialmente implementado. No existe asignacion N:M ni desactivacion HTTP como parte del alcance completo. |
| Administrar definicion de especialidad | `<<CONCEPTUAL>>` | RF-008, DEC-006, C.4 CU-D3. Catalogs no tiene CRUD Java ni endpoint vigente. |
| Asociar profesional con especialidad | `<<CONCEPTUAL>>` | RF-007, DEC-006, C.4 CU-D2. V1 tiene tabla N:M, no flujo Java de gestion. |
| Gestionar horario profesional | `<<PARCIAL>>` | `AgendaController`/`AgendaService` administran Schedule; C.4 CU-D4. No genera slots ni valida specialty activa/asignada. |
| Consultar disponibilidad operativa | `<<IMPLEMENTADO>>` | Dos GET de Agenda ADMIN; C.4 CU-D5. Filtros actuales: `scheduleId`, `professionalId`, `slotDate`, `status`; `usable = AVAILABLE && schedule.active`. |
| Consultar disponibilidad para reserva | `<<CONCEPTUAL>>` | PATIENT/RECEPTIONIST autenticados aprobados por DEC-007; C.4 CU-D6. No existe URI, DTO, filtro definitivo, permiso efectivo ni vista sanitizada Java. |
| Solicitar cita medica; Reservar slot disponible | `<<IMPLEMENTADO>>` | POST actual de Appointments; C.4 CU-D7. El service deriva contexto del slot y reserva dentro de la transaccion; no exige consulta previa. |
| Validar disponibilidad atomica | `<<IMPLEMENTADO>>`, interno | UPDATE condicional de Agenda e indice parcial V3; C.4 CU-D8. No es un caso de uso invocable por un actor. |

Las tres etiquetas `CU-D*` son identificadores locales descriptivos de C.4,
no IDs UC oficiales. La etiqueta `<<PARCIAL>>` de horario representa el
alcance incompleto de RF-011, no una promesa de generacion de slots. La
gestion profesional se marca `<<PARCIAL>>` por el alcance de RF-007/CU-D1,
aunque sus tres operaciones basicas representadas si existen. La solicitud de cita representa solo RF-013 actual,
no funcionalidades posteriores de Appointments.

## Relaciones UML

`Solicitar cita medica` **incluye obligatoriamente** `Reservar slot disponible`:
la creacion actual no persiste una cita sin la reserva del slot. `Reservar slot
disponible` **incluye obligatoriamente** `Validar disponibilidad atomica`:
la reserva usa UPDATE condicional sobre slot AVAILABLE y schedule activo,
con unicidad parcial de cita activa en V3. No hay `extend`: ninguna extension
opcional real esta aprobada para este diagrama. La consulta conceptual no se
conecta mediante `include` a la cita porque el POST actual no requiere un
endpoint previo de descubrimiento.

RF-014 tiene evidencia PostgreSQL de **2 solicitudes concurrentes**; la SRS
exige un escenario de **20 solicitudes simultaneas**, aun pendiente de
validacion. El diagrama no lo representa como caso independiente ni como
criterio ya probado.

## Fuera del alcance y gate

No se representan generacion automatica de slots, calendario/duracion/
solapamiento, ventanas temporales, waitlist, notificaciones, prioridad,
frontend/UI, nuevas operaciones de Appointments ni roles adicionales.
DEC-008 y DEC-010 permanecen abiertas. Este dibujo no crea endpoints,
contratos Java, DTOs, tablas, migraciones ni permisos. D.2 puede detallar
los casos manteniendo separados implementado, parcial y conceptual; no puede
convertir las relaciones de diseno en capacidades ejecutables por inferencia.
