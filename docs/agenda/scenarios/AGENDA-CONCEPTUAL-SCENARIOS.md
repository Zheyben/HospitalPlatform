# ETAPA D.3.3 — CONCEPTUAL SCENARIOS REPORT

## Propósito y alcance

Documentar CU-D2, CU-D3 y CU-D6 como **escenarios UML conceptuales del caso académico HOSPITALPLATFORM**. Los diagramas muestran objetivos e interacciones posibles dentro del límite del sistema, no trazas de ejecución. La aprobación del proyecto académico no se presenta como política institucional del Hospital de Huaycán.

| Categoría | Alcance en este incremento |
|---|---|
| **IMPLEMENTADO** | CU-D5: consulta operativa de disponibilidad para ADMIN; CU-D7: reserva actual; CU-D8: exclusión técnica de doble reserva. Están documentados en [D.3.1](AGENDA-IMPLEMENTED-SCENARIOS.md). |
| **PARCIAL IMPLEMENTADO** | CU-D1: perfil profesional básico; CU-D4: configuración de horarios. Están documentados en [D.3.2](AGENDA-PARTIAL-SCENARIOS.md). |
| **APROBADO CONCEPTUALMENTE** | CU-D2: cardinalidad/propiedad N:M por DEC-006; CU-D6: actores y frontera de exposición por DEC-007. **NO IMPLEMENTADOS** como casos completos. |
| **FUTURO / CONCEPTUAL** | CU-D3: la gestión del catálogo responde a RF-008; DEC-006 aprueba su propietario, no CRUD. La operación funcional sigue **NO IMPLEMENTADA**. Sus reglas de estado y efectos son **PENDIENTES**. |

Los identificadores CU-D2/D3/D6 son locales de la descomposición documental C.4 y D.1/D.2. No se elevan a operaciones disponibles por aparecer en UML.

## Fuentes revisadas

- Autoridad de dominio: [DOMAIN-BASELINE](../../DOMAIN-BASELINE.md), [DOMAIN-DECISION-REGISTER](../../DOMAIN-DECISION-REGISTER.md), en especial DEC-006 y DEC-007; DEC-003, DEC-008 y DEC-010 permanecen relevantes para decisiones posteriores.
- Diseño y trazabilidad: [matriz RF–CU](../AGENDA-AVAILABILITY-RF-UC-MATRIX.md), [casos de uso](../AGENDA-AVAILABILITY-USE-CASES.md), [diseño de contratos](../AGENDA-AVAILABILITY-CONTRACT-DESIGN.md), [especificaciones C.4](../AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md), [D.1 PlantUML](../uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.puml) y su [explicación](../uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.md), [D.2](../use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [D.3.1](AGENDA-IMPLEMENTED-SCENARIOS.md) con sus tres `.puml` y [D.3.2](AGENDA-PARTIAL-SCENARIOS.md) con sus dos `.puml`.
- Requisitos y arquitectura: [SRS](../../requisitos/SRS-HOSPITALPLATFORM.md) RF-007/008/012, [API Specification](../../06-API-SPECIFICATION.md), [ADR-005 RBAC](../../adr/ADR-005-role-permission-model.md), [ADR-006 slots discretos](../../adr/ADR-006-agenda-slot-model.md), [ADR-007 estados de cita](../../adr/ADR-007-appointment-state-machine.md), [ADR-009 UUID](../../adr/ADR-009-database-identifier-strategy.md) y [ADR-010 soft delete](../../adr/ADR-010-soft-delete-strategy.md). Los identificadores DEC del registro y los ADR homónimos son documentos distintos.
- Estado ejecutable contrastado: [AgendaController](../../../apps/backend/src/main/java/com/hospital/platform/agenda/controller/AgendaController.java), [AgendaService](../../../apps/backend/src/main/java/com/hospital/platform/agenda/service/AgendaService.java), [ProfessionalController](../../../apps/backend/src/main/java/com/hospital/platform/professionals/controller/ProfessionalController.java), [ProfessionalService](../../../apps/backend/src/main/java/com/hospital/platform/professionals/service/ProfessionalService.java), [AppointmentController](../../../apps/backend/src/main/java/com/hospital/platform/appointments/controller/AppointmentController.java), [AppointmentService](../../../apps/backend/src/main/java/com/hospital/platform/appointments/service/AppointmentService.java), [SecurityConfiguration](../../../apps/backend/src/main/java/com/hospital/platform/security/config/SecurityConfiguration.java), estructura vacía de Catalogs, tests de Agenda/Professionals/Appointments y migraciones [V1](../../../database/migrations/V1__initial_schema.sql), [V2](../../../database/migrations/V2__create_refresh_tokens.sql), [V3](../../../database/migrations/V3__support_appointment_lifecycle.sql).

## Casos incluidos

### CU-D2 — Asociación Professional ↔ Specialty

**Diagrama:** [CU-D2-PROFESSIONAL-SPECIALTY-CONCEPTUAL-SCENARIO.puml](CU-D2-PROFESSIONAL-SPECIALTY-CONCEPTUAL-SCENARIO.puml). **Estado:** **APROBADO CONCEPTUALMENTE / NO IMPLEMENTADO**. **Actor:** ADMIN como actor de gestión previsto por RF-007; no posee una operación N:M autorizada hoy.

**Objetivo y flujo conceptual:** seleccionar profesional, consultar opciones de especialidad, indicar una asociación y considerar su validación. El diagrama no prescribe interfaz, orden transaccional, resultado HTTP ni persistencia. El criterio de “disponibles”, la identidad aplicable, la semántica de especialidad habilitada, duplicados, desasociación e idempotencia siguen **PENDIENTES**.

**Decisión y dependencia:** DEC-006 **CLOSED solo en diseño** aprueba N:M y reparte ownership: Catalogs posee definiciones y Professionals la relación. V1 tiene `professional_specialties` con PK compuesta y FK a ambas tablas; eso acredita capacidad física, no gestión funcional. Se requieren decisiones de política y un contrato futuro entre módulos antes de implementar. `ProfessionalLookupService` actual comprueba existencia/ownership de profesional, no asociaciones de especialidad. No hay prueba funcional N:M.

### CU-D3 — Gestión Specialty Catalog

**Diagrama:** [CU-D3-SPECIALTY-CATALOG-CONCEPTUAL-SCENARIO.puml](CU-D3-SPECIALTY-CATALOG-CONCEPTUAL-SCENARIO.puml). **Estado:** **CONCEPTUAL / FUTURO / NO IMPLEMENTADO**. **Actor:** ADMIN esperado por RF-008, sin controller actual de Catalogs.

**Objetivo y flujo conceptual:** consulta, creación, actualización y cambio activo/inactivo de definiciones, como alternativas. DEC-006 asigna propiedad de definiciones a Catalogs, pero no cierra operaciones, validaciones, errores ni efectos sobre horarios, slots o citas. V1 contiene `specialties.name` único, `active` y `deleted_at`; la semántica conjunta y la política de desactivación están **PENDIENTES**. La SRS menciona un `code` y rechazo de código duplicado; V1 no posee esa columna. La estructura Java de Catalogs contiene solo `package-info.java`; no hay entidad, repositorio, servicio, controller, DTO, permiso efectivo ni tests funcionales del catálogo. DEC-003 conserva abierto el bootstrap de datos.

### CU-D6 — Consulta de disponibilidad sanitizada

**Diagrama:** [CU-D6-SANITIZED-AVAILABILITY-CONCEPTUAL-SCENARIO.puml](CU-D6-SANITIZED-AVAILABILITY-CONCEPTUAL-SCENARIO.puml). **Estado:** **APROBADO CONCEPTUALMENTE / NO IMPLEMENTADO**. **Actores:** PATIENT y RECEPTIONIST autenticados, exclusivamente según DEC-007 para esta vista futura.

**Objetivo y flujo conceptual:** solicitar disponibilidad, aplicar filtros permitidos aún por definir y devolver una representación minimizada sin detalle operativo innecesario. La autenticación existe como capacidad general; **la autorización de esta consulta para esos roles no está configurada**. DEC-007 aprueba los actores y la separación frente a la vista ADMIN, pero no campos, filtros, URI, DTO ni regla de propiedad. La consulta actual de Agenda sigue reservada a ADMIN y expone información operativa; no se recicla como vista sanitizada. La reserva vigente de CU-D7 revalida el slot y no exige consulta previa. El resultado conceptual no reserva ni garantiza disponibilidad posterior. DEC-010 deja abiertas las ventanas temporales; la política de specialty activa/asociada también está pendiente. No hay tests de la vista sanitizada.

## Diferencias entre dominio aprobado y código actual

| Dominio o documento | Código actual | Clasificación para D.3.3 |
|---|---|---|
| DEC-006 aprueba Catalogs como dueño de definiciones y Professionals como dueño de la relación N:M. | Catalogs no tiene implementación funcional; Professionals solo gestiona perfil básico; V1 contiene tablas. | **APROBADO CONCEPTUALMENTE** en ownership/cardinalidad; CRUD y asociación **NO IMPLEMENTADOS**. |
| RF-008 y la API Specification describen gestión de especialidades. | No hay controller ni contrato Java de Catalogs. | **FUTURO / CONCEPTUAL**; las rutas documentales no son API vigente. |
| DEC-007 aprueba descubrimiento minimizado para PATIENT y RECEPTIONIST autenticados. | Los métodos actuales de consulta de disponibilidad en Agenda exigen ADMIN; Appointments permite reserva por actores propios pero no aporta esa consulta. | **APROBADO CONCEPTUALMENTE**, vista y permisos **NO IMPLEMENTADOS**. |
| RF-012 propone filtros y oferta reservable; C.3 da candidatos. | El listado ADMIN actual no aporta una vista por rol ni filtro de especialidad en disponibilidad. | Filtros/campos definitivos **PENDIENTES**; no se trasladan los del ADMIN. |

## Dependencias, exclusiones y contradicciones conocidas

- **Dependencias:** definición/estado de specialty bajo Catalogs, asociación bajo Professionals, futura frontera pública entre módulos, autorización específica de consulta y minimización de datos bajo Agenda. Ninguna se representa como clase o contrato ejecutable. DEC-008 mantiene abierta la generación/calendario de slots; DEC-010 la temporalidad. Las vistas conceptuales no alteran la reserva actual.
- **Exclusiones:** Java, controllers, endpoints, DTO definitivos, migraciones, permisos reales, contratos ejecutables, pruebas, UI, generación de slots, reglas de ventana, sincronización automática, estados nuevos y flujos institucionales no aprobados. Los diagramas solo contienen actor(es), límite HospitalPlatform, interacción de dominio y notas **“CONCEPTUAL — NO IMPLEMENTADO”**.
- **Contradicciones:** SRS RF-007 incorpora asociación dentro de gestión profesional, pero CU-D1 real solo crea/consulta/actualiza licencia; CU-D2 queda separado. SRS RF-008 y API Specification describen gestión de catálogo sin Java correspondiente. SRS RF-012 plantea acceso de paciente/recepción, mientras la consulta actual de disponibilidad es ADMIN. La SRS menciona `code` de especialidad y V1 solo tiene `name UNIQUE`. Estas diferencias no se resuelven inventando API.
- **Corte histórico:** D.3.2 dice que D.3.1 no estaba presente en *aquel checkout*. En la rama actual ambos entregables existen y se revisaron; no se modifica el informe histórico de D.3.2.

## Validación final

- Revisión de trazabilidad documental y de ausencia de implementación completada para los tres casos. Los diagramas no nombran endpoints, DTO, clases Java ni permisos configurados.
- Pendiente de una herramienta PlantUML local: renderizado visual; se comprueba estructura textual y balance de bloques antes de entrega.
- Git: verificar rama, `status`, archivos creados y `git diff --check` al cierre. El alcance de cambios debe ser exclusivamente estos cuatro archivos documentales.
- Java modificado: **0**. SQL modificado: **0**. Tests modificados: **0**. Seguridad modificada: **0**. Migraciones modificadas: **0**. Sin commit, push ni merge.
