# Matriz Actor → Caso de Uso — Agenda Availability + Specialty Policy

## Propósito

Relacionar los actores ya documentados en [D.1](../uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.md), [D.2](../use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md) y los informes [D.3.1](../scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md), [D.3.2](../scenarios/AGENDA-PARTIAL-SCENARIOS.md) y [D.3.3](../scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md) con CU-D1–CU-D8. Esta matriz **describe participación, no concede permisos**. `CONCEPTUAL` no es acceso HTTP.

**Leyenda:** `P` = actor de caso **PARCIAL** con operación actual delimitada; `I` = actor de caso **IMPLEMENTADO**; `C` = actor **CONCEPTUAL** sin operación vigente; `T` = ejecutor técnico interno; `—` = sin asociación en estos ocho casos.

| Actor | CU-D1 | CU-D2 | CU-D3 | CU-D4 | CU-D5 | CU-D6 | CU-D7 | CU-D8 |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **ADMIN** | P | C | C | P | I | — | I | — |
| **PATIENT** | — | — | — | — | — | C | I¹ | — |
| **RECEPTIONIST** | — | — | — | — | — | C | I² | — |
| **PROFESSIONAL** | — | — | — | — | — | — | — | — |
| **SYSTEM técnico** | — | — | — | — | — | — | — | T³ |

¹ PATIENT reserva para su perfil de paciente activo vinculado. ² ADMIN y RECEPTIONIST indican un paciente activo en la reserva actual. ³ CU-D8 se activa dentro de las solicitudes de CU-D7; no tiene disparador humano, permiso ni endpoint propio. Los solicitantes heredan su autorización de CU-D7, sin una segunda asociación actor–CU-D8. `SYSTEM técnico` es una referencia analítica interna: D.1 no lo dibuja como actor UML externo.

## Límites por actor

- **ADMIN:** gestión básica de profesionales/horarios y consulta operativa actuales; asociación N:M y catálogo solo como actor documental. CU-D6 se diseñó como vista distinta para PATIENT/RECEPTIONIST; ADMIN conserva CU-D5.
- **PATIENT y RECEPTIONIST:** CU-D6 está aprobado conceptualmente por DEC-007, sin autorización configurada. CU-D7 sí existe conforme a sus reglas actuales.
- **PROFESSIONAL:** tiene rol y operaciones de atención asignada en Appointments, fuera de CU-D1–CU-D8. No se le atribuye gestión de profesionales, agenda ni descubrimiento de disponibilidad.
- **SYSTEM técnico:** representa la actualización condicional y la barrera de unicidad de CU-D8, no el rol `SYSTEM` como usuario de una API. No hay actor humano adicional.

Evidencia de seguridad: [`ProfessionalController`](../../../apps/backend/src/main/java/com/hospital/platform/professionals/controller/ProfessionalController.java) y [`AgendaController`](../../../apps/backend/src/main/java/com/hospital/platform/agenda/controller/AgendaController.java) exigen `ADMIN` en las operaciones aquí pertinentes; [`AppointmentController`](../../../apps/backend/src/main/java/com/hospital/platform/appointments/controller/AppointmentController.java) autoriza la reserva a `PATIENT`, `ADMIN` y `RECEPTIONIST`. [`SecurityConfiguration`](../../../apps/backend/src/main/java/com/hospital/platform/security/config/SecurityConfiguration.java) exige autenticación general. DEC-006/007 en el [registro](../../DOMAIN-DECISION-REGISTER.md) no instalan permisos.
