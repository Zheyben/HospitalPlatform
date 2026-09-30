# ETAPA E.3.2.1 — POST-AUDIT DOCUMENTATION ALIGNMENT

**Proyecto:** HOSPITALPLATFORM — caso de estudio académico del Hospital de Huaycán

**Fecha de corte:** 2026-09-30

**Alcance:** postauditoría de los 18 Markdown modificados en E.3.2. El [reporte E.3.2](DOCUMENTATION-ALIGNMENT-REPORT.md) se usó para identificar cambios y alcance; no se reabrieron documentos históricos ni se revisó código con intención de modificarlo.

## 1. Estado Git

- Rama comprobada: `chore/documentation-alignment-remediation`.
- Los 18 documentos de E.3.2 siguen modificados sin commit. Permanecen sin seguimiento el reporte E.3.2 y este reporte E.3.2.1.
- Se revisó el diff de E.3.2; las correcciones de esta postauditoría afectan solo cinco de esos 18 Markdown.
- No se hizo commit, push ni merge.

## 2. Archivos auditados y fuentes

| Área | Archivos auditados (todos modificados por E.3.2) | Contraste principal |
|---|---|---|
| Requisitos, diseño y API | [PRD](../01-PRD.md), [TRD](../02-TRD.md), [UI/UX](../04-UI-UX-DESIGN-BRIEF.md), [Backend Schema](../05-BACKEND-SCHEMA.md), [API Specification](../06-API-SPECIFICATION.md) | Baseline, DEC-002/006/007/019/020, C.1–C.4, D.1–D.4, controllers/DTOs |
| Seguridad y pruebas | [Threat Model](../08-SECURITY-THREAT-MODEL.md), [Test Strategy](../09-TEST-STRATEGY.md), [Test Plan](../10-TEST-PLAN.md), [ADR-005](../adr/ADR-005-role-permission-model.md), [Security Architecture](../security/SECURITY-ARCHITECTURE.md) | DEC-002/020/023, anotaciones de seguridad, `AppointmentModuleIT`, SRS RF-014 |
| Arquitectura modular | [Backend Architecture](../architecture/BACKEND-ARCHITECTURE.md), [Module Contracts](../contracts/MODULE-CONTRACTS-ARCHITECTURE.md), [Patients](../patients/PATIENT-MANAGEMENT-ARCHITECTURE.md), [Professionals](../professionals/PROFESSIONAL-MANAGEMENT-ARCHITECTURE.md), [Users](../users/USER-MANAGEMENT-ARCHITECTURE.md) | Baseline, Decision Register, C.3, D.4, servicios y contratos Java |
| Persistencia | [Database Design](../database/DATABASE-DESIGN.md), [ERD pendiente](../database/ENTITY-RELATIONSHIP-DIAGRAM.md), [Migration Strategy pendiente](../database/MIGRATION-STRATEGY.md) | V1, V2, V3, baseline, DEC-005/006/008/010 |

La comparación empleó [DOMAIN-BASELINE](../DOMAIN-BASELINE.md), [DOMAIN-DECISION-REGISTER](../DOMAIN-DECISION-REGISTER.md), [C.1](../agenda/AGENDA-AVAILABILITY-RF-UC-MATRIX.md), [C.2](../agenda/AGENDA-AVAILABILITY-USE-CASES.md), [C.3](../agenda/AGENDA-AVAILABILITY-CONTRACT-DESIGN.md), [C.4](../agenda/AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md), [D.1](../agenda/uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.md), [D.2](../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [D.3.1](../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md), [D.3.2](../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md), [D.3.3](../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md) y [D.4](../agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md). Se inspeccionaron estáticamente controllers, DTOs, servicios, seguridad y tests de `apps/backend`, además de las tres migraciones SQL. No se ejecutó Maven ni pruebas.

## 3. Hallazgos

| Severidad | Hallazgo detectado en E.3.2 | Evidencia | Resultado de postauditoría |
|---|---|---|---|
| **CRÍTICO — corregido** | La matriz de `06-API-SPECIFICATION.md` todavía asignaba `ADMINISTRADOR` y permisos a `POST /specialties` y `GET /dashboard/metrics`, pese a que esas rutas son históricas. | No hay controllers funcionales de Catalogs/Dashboard; DEC-002/006/019/020. | Filas identificadas como `NO IMPLEMENTADO`, sin rol operativo; los endpoints vigentes se expresan por rol y ownership. |
| **CRÍTICO — corregido** | `05-BACKEND-SCHEMA.md` afirmaba sin reserva que una Specialty inactiva no aparece en nuevas reservas. El backend actual no valida esa política. | `AppointmentService`, contratos de Agenda y CU-D2/D3/D7; DEC-006. | Regla etiquetada `PROPUESTA, NO IMPLEMENTADA`. También se precisó que completar una cita no libera el slot por efecto del índice V3. |
| **MEDIO — corregido** | `DATABASE-DESIGN.md` rotulaba `availability_slots` como lista de migraciones, citaba `waitlist`/`priority` como tablas y conservaba `status` como índice de appointments. | V1 crea `availability_slots`, `waitlist_entries`, `priority_requests` e índice sobre `appointment_status`; V3 crea índice parcial por `slot_id`. | Nombres y rótulos corregidos. Se distingue módulo contemplado de módulo funcional. |
| **MEDIO — corregido** | `PROFESSIONAL-MANAGEMENT-ARCHITECTURE.md` seguía llamando conceptual al método de lookup ya implementado y omitía `isActiveProfessionalLinkedToUser`. | `ProfessionalLookupService` y `DatabaseProfessionalLookupService`; DEC-005 abierto. | Se documentaron ambos métodos reales; la asociación N:M continúa conceptual y no aparece como API. |
| **MEDIO — corregido** | El TRD todavía mostraba web como componente sin etiqueta objetivo y `ROLE_TRIAGE` bajo “Roles principales”, con validación granular como regla actual. | Carpetas web/móvil sin archivos de aplicación, DEC-002/020 y controllers. | Web rotulada como objetivo; lista de roles y permisos marcada histórica. |
| **BAJO — observación** | Los diseños anteriores conservan ejemplos de tablas, pantallas y controles no ejecutables, pero sus notas de vigencia y secciones corregidas ya los delimitan. Los dos marcadores de base de datos siguen sin contenido técnico. | Baseline, inventario E.3.1 y los propios marcadores. | Conservar procedencia; elaborar ERD/estrategia solo en una etapa documental aprobada. |

**API.** `AuthController`, `UserController`, `PatientController`, `ProfessionalController`, `AgendaController` y `AppointmentController` respaldan las rutas marcadas vigentes; el contexto `/api/v1` consta en `application.yml`. `auth/register`, `auth/me`, gestión HTTP de Specialty/Dashboard/permisos y `PUT /patients/me` quedan diferenciadas como históricas o no implementadas. `DashboardMetricResponse` no existe como DTO Java; los DTOs reales prevalecen sobre ejemplos. La asignación actual de roles es `POST /users/{id}/roles` para ADMIN. No hay envelope ni paginación universal (DEC-019).

**Seguridad.** Los actores de negocio actuales son exactamente `PATIENT`, `RECEPTIONIST`, `PROFESSIONAL` y `ADMIN`. Las menciones a `TRIAGE`, `SYSTEM` y roles futuros en ADR/diseño están identificadas como antecedentes, enum o actor técnico; no son permisos operativos nuevos. No se halló actor auditor habilitado. DEC-020 sigue `PROPOSED`.

**Arquitectura y Professionals.** Las dependencias Agenda/Appointments → `ProfessionalLookupService` y Appointments → contratos de reserva/liberación de Agenda coinciden con el código. Catalogs posee conceptualmente definiciones de Specialty y Professionals la asignación N:M (DEC-006); V1 conserva `professional_specialties`, sin servicio/endpoint de gestión. CU-D1 sigue `PARCIAL`; CU-D2/D3 `CONCEPTUAL`; DEC-005 sigue `OPEN`. No se promovieron carpetas marcador a módulos funcionales ni se identificó dependencia nueva prohibida por las correcciones.

**Persistencia y tests.** V1 contiene `specialties`, `professional_specialties`, `schedules`, `availability_slots` y las tablas de citas/espera/prioridad; V2 crea refresh tokens; V3 sustituye la unicidad absoluta de slot de cita por `uq_appointments_active_slot` para `SCHEDULED`/`CONFIRMED`. Las tablas históricas en castellano del Backend Schema siguen marcadas como modelo previo. `AppointmentModuleIT.allowsOnlyOneOfTwoConcurrentReservations` acredita **2 solicitudes**; el ensayo SRS RF-014 de **20 solicitudes** permanece pendiente. Las filas FT del plan son diseño de pruebas, no resultados ejecutados.

## 4. Correcciones realizadas

Se realizaron correcciones factuales solamente en [TRD](../02-TRD.md), [Backend Schema](../05-BACKEND-SCHEMA.md), [API Specification](../06-API-SPECIFICATION.md), [Database Design](../database/DATABASE-DESIGN.md) y [Professional Architecture](../professionals/PROFESSIONAL-MANAGEMENT-ARCHITECTURE.md). No se cambió el registro de decisiones, la SRS, C/D, código ni SQL. Los demás 13 documentos E.3.2 se validaron sin nueva edición.

## 5. Archivos modificados

- **E.3.2.1:** cinco Markdown existentes indicados arriba y este archivo nuevo.
- **Acumulado en la rama desde E.3.2:** 18 Markdown existentes bajo `docs/`, [reporte E.3.2](DOCUMENTATION-ALIGNMENT-REPORT.md) y este reporte. Ningún archivo de aplicación ni migración cambió.

## 6. Validación

- `git status` confirmó la rama y el alcance solo documental.
- `git diff --check` terminó sin errores de whitespace/conflicto para los archivos versionados; el archivo nuevo se comprobó aparte en cuanto a espacios finales. Los avisos LF/CRLF de Git son informativos.
- Java: **0 cambios**. SQL: **0 cambios**. Migraciones: **0 cambios**. Tests: **0 cambios**. Seguridad de aplicación: **0 cambios**. Configuración: **0 cambios**.
- No se ejecutó Maven, no se agregaron endpoints, DTOs, roles, permisos ni contratos funcionales.

## 7. Gate siguiente

**🟡 ALIGNMENT APPROVED WITH OBSERVATIONS.** Las contradicciones funcionales detectadas durante la postauditoría quedaron corregidas con evidencia. No quedan hallazgos críticos abiertos en el diff auditado. Persisten antecedentes expresamente históricos, dos marcadores documentales sin diseño validado y decisiones `OPEN`/`PROPOSED`; la siguiente limpieza debe conservar esa distinción.
