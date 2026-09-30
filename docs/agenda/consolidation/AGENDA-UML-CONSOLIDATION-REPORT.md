# ETAPA D.4 — Informe de consolidación UML

## Objetivo del módulo y del documento

Agenda Availability + Specialty Policy reúne gestión profesional básica, horarios, lectura de slots existentes y reserva de citas con exclusión de doble asignación. La parte de catálogo, asociación N:M y consulta sanitizada está documentada a nivel conceptual. Este informe organiza el trabajo D.1–D.3.3 para el caso académico HOSPITALPLATFORM; no crea funcionalidad ni presume una política institucional del Hospital de Huaycán.

## Arquitectura documental

| Etapa | Artefacto | Función en la trazabilidad |
|---|---|---|
| Base | [DOMAIN-BASELINE](../../DOMAIN-BASELINE.md) y [DOMAIN-DECISION-REGISTER](../../DOMAIN-DECISION-REGISTER.md) | Separan comportamiento actual, decisiones aprobadas y decisiones abiertas. |
| C.1–C.4 | [Matriz RF–CU](../AGENDA-AVAILABILITY-RF-UC-MATRIX.md), [casos](../AGENDA-AVAILABILITY-USE-CASES.md), [diseño de contratos](../AGENDA-AVAILABILITY-CONTRACT-DESIGN.md), [especificaciones](../AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md) | Definen IDs locales, límites funcionales y contratos actuales frente a propuestas. |
| D.1–D.2 | [UML general](../uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.puml), [explicación](../uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.md), [fichas detalladas](../use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md) | Representan actores, ocho casos y alcance de cada uno. |
| D.3.1 | [Escenarios implementados](../scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md) | CU-D5, CU-D7 y CU-D8; secuencias con evidencia de código. |
| D.3.2 | [Escenarios parciales](../scenarios/AGENDA-PARTIAL-SCENARIOS.md) | CU-D1 y CU-D4; distingue pasos vigentes de brechas. |
| D.3.3 | [Escenarios conceptuales](../scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md) | CU-D2, CU-D3 y CU-D6; no son secuencias API actuales. |
| D.4 | [Catálogo](AGENDA-USE-CASE-CATALOG.md), [matriz actor–caso](AGENDA-ACTOR-USECASE-MATRIX.md), [trazabilidad completa](AGENDA-FULL-TRACEABILITY-MATRIX.md) | Índices consolidados; no reemplazan fuentes primarias. |

## Actores y casos

Los actores humanos de estos ocho casos son `ADMIN`, `PATIENT` y `RECEPTIONIST`. `SYSTEM` técnico se registra en la [matriz de actores](AGENDA-ACTOR-USECASE-MATRIX.md) solo como ejecutor interno de la invariante CU-D8; D.1 no lo dibuja como actor UML externo. `PROFESSIONAL` aparece en la matriz para hacer explícita su **ausencia de participación** en este incremento; sus operaciones de atención asignada existen en Appointments fuera del alcance CU-D1–CU-D8.

| Estado D.4 | Casos | Alcance correcto |
|---|---|---|
| **IMPLEMENTADO** | CU-D5, CU-D7, CU-D8 | Consulta operativa ADMIN; reserva actual; exclusión mutua interna. CU-D5 no cubre todo RF-012 y CU-D8 no acredita el ensayo SRS de 20 solicitudes. |
| **PARCIAL** | CU-D1, CU-D4 | Gestión básica de perfil limitada a alta/consulta/cambio de licencia; configuración de `Schedule` sin generación de slots ni política de specialty. |
| **CONCEPTUAL** | CU-D2, CU-D3, CU-D6 | Asociación N:M, gestión de catálogo y descubrimiento sanitizado sin flujo Java/API vigente. |

El detalle actor → caso → evidencia está en el [catálogo](AGENDA-USE-CASE-CATALOG.md); la cadena RF → caso → escenario → contrato → persistencia → test → estado está en la [matriz completa](AGENDA-FULL-TRACEABILITY-MATRIX.md).

## Estado actual y decisiones

`ProfessionalController` permite a ADMIN crear/listar/detallar/cambiar licencia. `AgendaController` permite a ADMIN gestionar schedules y leer disponibilidad operativa. `AppointmentController` permite la reserva actual a PATIENT para sí, o ADMIN/RECEPTIONIST para un paciente activo. Agenda reserva el slot mediante UPDATE condicional y V3 respalda la unicidad de cita activa. [V1](../../../database/migrations/V1__initial_schema.sql) tiene `specialties` y `professional_specialties`, pero Catalogs carece de implementación funcional; V2 atiende refresh tokens y [V3](../../../database/migrations/V3__support_appointment_lifecycle.sql) refuerza el lifecycle de citas.

| Decisión | Estado vigente | Efecto documental en D.4 |
|---|---|---|
| DEC-006 | `CLOSED` para diseño | Catalogs posee definiciones, Professionals asociaciones N:M; no crea CRUD, lookup ni asignación Java. |
| DEC-007 | `CLOSED` para diseño | PATIENT/RECEPTIONIST autenticados son actores de una futura vista sanitizada; ADMIN conserva consulta operativa. No instala permiso. |
| DEC-009 / DEC-019 | `CLOSED` en su alcance | Zona IANA de negocio elegida para reglas futuras; controller vigente prima sobre rutas históricas. No se aplican ventanas ni envelope universal. |
| DEC-003 / DEC-005 | `OPEN` | Bootstrap de datos/cuentas y ciclo User–Professional pendientes. |
| DEC-008 / DEC-010 | `OPEN` | Generación, duración, calendario, solapamiento y reglas temporales pendientes. |
| DEC-023 | `OPEN` | Metas operativas/ensayo SRS de 20 solicitudes requieren evidencia posterior; no invalida la invariante probada con 2. |

## Contradicciones conocidas y tratamiento

1. **SRS RF-007 vs Java:** la SRS incluye asociación de especialidades; el código de Professionals solo implementa perfil básico. CU-D1 es `PARCIAL` y CU-D2 `CONCEPTUAL`.
2. **SRS RF-008/API Specification vs Java:** la especificación histórica enumera gestión de specialties y un permiso, pero Catalogs solo contiene estructura vacía. CU-D3 es `CONCEPTUAL`; no se trasladan esas rutas a la API actual. La SRS menciona `code`; V1 no lo contiene.
3. **SRS RF-011 vs Agenda:** la SRS describe generación de slots y especialidad habilitada; el código guarda schedules y valida `specialtyId` solo por FK. CU-D4 es `PARCIAL`; DEC-008/010 y la política de specialty siguen pendientes.
4. **SRS RF-012 vs seguridad actual:** PATIENT/RECEPTIONIST son actores conceptuales de CU-D6 por DEC-007; los GET de disponibilidad actuales exigen ADMIN y corresponden a CU-D5.
5. **SRS RF-014 vs test:** PostgreSQL demuestra exclusión mutua con dos solicitudes y V3 aporta un índice parcial; el escenario de veinte solicitudes no está probado.
6. **Cortes históricos:** D.3.2 registra que D.3.1 no estaba en *aquel checkout*. Ambos están presentes y se usan en esta consolidación. D.3.3 se recuperó del commit documental `e749634` en esta rama sin merge; sus archivos conservan los hashes del commit fuente.

## Exclusiones y control de alcance

No se incorporan nuevos casos, actores, endpoints, DTOs, permisos, estados, reglas, generación de slots, validación de specialty activa/asignada, ventana temporal, UI, waitlist, notificaciones ni cambios de producción. Las relaciones conceptuales no son promesas de acceso. [`SecurityConfiguration`](../../../apps/backend/src/main/java/com/hospital/platform/security/config/SecurityConfiguration.java) mantiene autenticación general y los controllers conservan sus anotaciones actuales. Los tests existentes se usaron como evidencia documental; no se agregan ni ejecutan suites por esta etapa.

Los cuatro archivos D.4 son solo documentación. Como prerrequisito de navegación se recuperaron los cuatro artefactos D.3.3 idénticos al commit `e749634`, también solo documentales. Java, SQL, migraciones, tests, seguridad, controllers, services, DTOs y contratos existentes permanecen sin cambios. No se hizo commit, push ni merge.
