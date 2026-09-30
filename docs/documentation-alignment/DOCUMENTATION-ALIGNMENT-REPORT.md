# ETAPA E.3.2 — DOCUMENTATION ALIGNMENT REPORT

**Proyecto:** HOSPITALPLATFORM — caso de estudio académico del Hospital de Huaycán

**Corte:** 2026-09-30

**Alcance:** remediación documental controlada de hallazgos prioritarios del inventario E.3.1. Este reporte no aprueba requisitos ni decisiones, ni acredita despliegue hospitalario.

## 1. Estado Git

- Rama de trabajo: `chore/documentation-alignment-remediation`.
- El árbol estaba limpio antes de E.3.2. Al cierre quedan solo cambios Markdown bajo `docs/`: 18 documentos existentes corregidos y este reporte nuevo.
- No se hizo commit, push ni merge.

## 2. Autoridad y método

Se tomó como punto de partida el [inventario E.3.1](DOCUMENTATION-INVENTORY-REPORT.md), en especial C-01–C-08 y A-01. Se contrastó con [Domain Baseline](../DOMAIN-BASELINE.md), [Decision Register](../DOMAIN-DECISION-REGISTER.md), [C.1](../agenda/AGENDA-AVAILABILITY-RF-UC-MATRIX.md), [C.2](../agenda/AGENDA-AVAILABILITY-USE-CASES.md), [C.3](../agenda/AGENDA-AVAILABILITY-CONTRACT-DESIGN.md), [C.4](../agenda/AGENDA-AVAILABILITY-USE-CASE-SPECIFICATIONS.md), [D.1](../agenda/uml/AGENDA-AVAILABILITY-USE-CASE-DIAGRAM.md), [D.2](../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [D.3.1](../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md), [D.3.2](../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md), [D.3.3](../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md) y [D.4](../agenda/consolidation/AGENDA-FULL-TRACEABILITY-MATRIX.md). Para hechos ejecutables se leyeron controllers, DTOs, servicios, configuración de seguridad y tests fuente en `apps/backend`, junto con `database/migrations/V1__initial_schema.sql`, `V2__create_refresh_tokens.sql` y `V3__support_appointment_lifecycle.sql`.

El inventario E.3.1 conserva su corte original: **no se alteró su clasificación retroactivamente**. Esta etapa separa explícitamente antecedentes, diseño propuesto, decisiones de dominio y comportamiento Java/API. Se preservaron las secciones históricas útiles; se añadieron notas de vigencia y se rectificaron referencias factuales concretas.

## 3. Inventario de documentos revisados y corrección

`Estado E.3.1` refleja la clasificación previa, no una reclasificación automática. `Fuente` identifica el contraste que justificó el cambio. Todos los archivos de esta tabla fueron modificados solo en Markdown.

| Documento | Estado E.3.1 | Problema encontrado y corrección documental | Fuente | Prioridad |
|---|---|---|---|---|
| [01-PRD.md](../01-PRD.md) | REQUIERE ALINEACIÓN | RF-014/RF-020 históricos se confundían con IDs SRS; se distinguió la serie antigua y se marcó el contenido clínico fuera del MVP. | SRS, equivalencias, C.1, baseline | Alta |
| [02-TRD.md](../02-TRD.md) | REQUIERE ALINEACIÓN | Flutter/PG16 y triaje se leían como arquitectura actual; se marcó React Native/Expo y PG17 como objetivos del roadmap, y la parte clínica como histórica. | Roadmap, POM backend, baseline, ADR-007 | Alta |
| [04-UI-UX-DESIGN-BRIEF.md](../04-UI-UX-DESIGN-BRIEF.md) | REQUIERE ALINEACIÓN | Pantallas de triaje, specialty y dashboard parecían entregadas; se identificaron como diseño y se delimitó la actuación actual de PROFESSIONAL. | Baseline, DEC-002/006/007, controllers; `apps/frontend` y `apps/mobile` solo contienen carpetas | Alta |
| [05-BACKEND-SCHEMA.md](../05-BACKEND-SCHEMA.md) | REQUIERE ALINEACIÓN | Modelo previo y `NO_ASISTIO` podían pasar por esquema vigente; se separó del físico y se sustituyó el ejemplo de índice por el índice activo real de V3. | V1–V3, ADR-007, DEC-002/006 | Alta |
| [06-API-SPECIFICATION.md](../06-API-SPECIFICATION.md) | REQUIERE ALINEACIÓN | Registro, `auth/me`, specialty, dashboard, roles/permisos y `patients/me` de escritura mezclaban propuesta con API real; se marcaron como históricos/no implementados y se explicitó la autoridad de controllers/DTOs. | Auth/User/Patient/Agenda/Appointment controllers, DTOs, DEC-019, C.3 | Crítica |
| [08-SECURITY-THREAT-MODEL.md](../08-SECURITY-THREAT-MODEL.md) | REQUIERE ALINEACIÓN | Roles y permisos de triaje/dashboard podían interpretarse como concedidos; se etiquetaron como ejemplos históricos/objetivo. | DEC-002/020, `@PreAuthorize`, baseline | Alta |
| [09-TEST-STRATEGY.md](../09-TEST-STRATEGY.md) | PARCIALMENTE VIGENTE | Plan y pruebas existentes se mezclaban; se indicó que RF-014 demuestra dos solicitudes y que el criterio SRS de veinte sigue pendiente. | `AppointmentModuleIT`, SRS, DEC-023 | Media |
| [10-TEST-PLAN.md](../10-TEST-PLAN.md) | REQUIERE ALINEACIÓN | FT de triaje y dashboard podían leerse como cobertura ejecutada; se etiquetaron como históricos/propuestos. | Tests fuente, baseline, ADR-007 | Alta |
| [adr/ADR-005-role-permission-model.md](../adr/ADR-005-role-permission-model.md) | REQUIERE ALINEACIÓN | Lista original TRIAGE/SYSTEM parecía alcance aprobado actual; se anotó la delimitación posterior de DEC-002 sin cambiar el estado del ADR. | DEC-002/020, controllers | Alta |
| [architecture/BACKEND-ARCHITECTURE.md](../architecture/BACKEND-ARCHITECTURE.md) | PARCIALMENTE VIGENTE | Checklist y paquetes marcador se podían interpretar como módulos funcionales; se indicó el backend presente y el carácter histórico del checklist. | Árbol Java, controllers, baseline | Media |
| [contracts/MODULE-CONTRACTS-ARCHITECTURE.md](../contracts/MODULE-CONTRACTS-ARCHITECTURE.md) | PARCIALMENTE VIGENTE | Faltaba distinguir contratos Java de ownership conceptual Specialty y vista sanitizada; se agregó la frontera actual/propuesta. | Contratos Java, C.3, DEC-006/007 | Media |
| [database/DATABASE-DESIGN.md](../database/DATABASE-DESIGN.md) | REQUIERE ALINEACIÓN | Citaba una V2 inexistente; se corrigió a V2 refresh tokens y V3 lifecycle y se recordó que tablas no prueban servicios. | Migraciones V1–V3, DEC-005/008/010 | Alta |
| [database/ENTITY-RELATIONSHIP-DIAGRAM.md](../database/ENTITY-RELATIONSHIP-DIAGRAM.md) | REQUIERE ALINEACIÓN | Archivo vacío; se dejó marcador inequívoco `PENDIENTE DE DOCUMENTACIÓN`, sin crear ERD. | V1–V3, E.3.1 A-01 | Alta |
| [database/MIGRATION-STRATEGY.md](../database/MIGRATION-STRATEGY.md) | REQUIERE ALINEACIÓN | Archivo vacío; se dejó marcador inequívoco, sin definir una estrategia nueva. | V1–V3, DEC-003/023, E.3.1 A-01 | Alta |
| [patients/PATIENT-MANAGEMENT-ARCHITECTURE.md](../patients/PATIENT-MANAGEMENT-ARCHITECTURE.md) | REQUIERE ALINEACIÓN | SYSTEM y autogestión podían leerse como vigentes; se delimitó lectura propia, gestión ADMIN y DEC-017 abierta. | `PatientController`, DEC-002/017 | Alta |
| [professionals/PROFESSIONAL-MANAGEMENT-ARCHITECTURE.md](../professionals/PROFESSIONAL-MANAGEMENT-ARCHITECTURE.md) | PARCIALMENTE VIGENTE | Llamaba futura a integración Agenda/Appointments ya existente; se corrigió estado Java/tests y se dejó N:M como conceptual. | `ProfessionalController`/service/lookup, tests, C.3, DEC-005/006 | Media |
| [security/SECURITY-ARCHITECTURE.md](../security/SECURITY-ARCHITECTURE.md) | REQUIERE ALINEACIÓN | TRIAGE/SYSTEM en lista inicial parecían roles de negocio actuales; se distinguió lista histórica de los cuatro roles aprobados. | DEC-002/020, seguridad Java | Alta |
| [users/USER-MANAGEMENT-ARCHITECTURE.md](../users/USER-MANAGEMENT-ARCHITECTURE.md) | REQUIERE ALINEACIÓN | Enum/lista heredada TRIAGE/SYSTEM podía expandir permisos por inferencia; se señaló alcance real y decisiones pendientes. | `UserController`, DEC-002/003/020 | Alta |

También se revisaron sin cambio el roadmap, flujos, SRS, equivalencias RF, reglas de negocio, ADR-007/008/011, D.4 y los planes de implementación/despliegue/monitoreo. Su condición propuesta o pendiente ya está declarada en su alcance, o no había una rectificación factual inequívoca que justificara editarlos ahora. Los diagramas `.mmd` parciales se mantienen ligados a sus fichas TO-BE; no se reinterpretaron como sistema en producción. El Capítulo III de investigación (C-09) y demás fuentes `HISTÓRICO` se conservaron intactos.

## 4. Clasificación de hallazgos

| Severidad | Hallazgo | Resultado E.3.2 |
|---|---|---|
| **CRÍTICO** | C-04: la especificación API mezclaba rutas, DTOs y permisos inexistentes con contratos actuales. | Rutas y payloads históricos señalados; controllers/DTOs y DEC-019 identificados como fuente funcional. No se crearon contratos. |
| **CRÍTICO** | C-05: TRIAGE/SYSTEM podían parecer roles autorizados por documentación de seguridad/usuarios. | Delimitación DEC-002 añadida en ADR y documentos afectados. No se concedieron permisos. |
| **MEDIO** | C-01/C-02/C-03/C-06/C-07/C-08: numeración RF anterior, stack objetivo, modelo SQL antiguo, triaje, migración errónea e integración Professionals desactualizada. | Notas de vigencia y correcciones puntuales; antecedentes retenidos con etiqueta histórica o conceptual. |
| **BAJO** | A-01: dos archivos vacíos carecían de señal de estado. | Marcadores `PENDIENTE DE DOCUMENTACIÓN`; siguen sin diagrama ni estrategia validada. |

## 5. Límites y observaciones

- DEC-006 y DEC-007 están `CLOSED` para ownership/actores conceptuales; no hay CRUD de catálogo, asociación N:M ni consulta sanitizada Java/API. CU-D1/D4 continúan `PARCIAL`; CU-D2/D3/D6 `CONCEPTUAL`; CU-D5/D7/D8 `IMPLEMENTADO` en el alcance D.4.
- DEC-003/005/008/010/017/022/023 permanecen `OPEN`; DEC-020 permanece `PROPOSED`. Ni los ejemplos del PRD/TRD/API ni las tablas V1–V3 cierran esas decisiones.
- Los documentos de diseño anteriores siguen incluyendo escenarios objetivo. Las notas de vigencia impiden citarlos como evidencia ejecutable, pero una limpieza editorial integral de ejemplos antiguos corresponde a una etapa distinta y debe respetar su procedencia.
- Los dos marcadores de base de datos siguen sin contenido técnico validado. Elaborar ERD o estrategia requeriría contrastar V1–V3 y aprobar el artefacto en una etapa posterior.
- No se ejecutó Maven ni pruebas: el alcance fue documental. La validación de implementación se hizo por lectura estática de código y tests existentes.

## 6. Validación final y control de alcance

- `git status` confirmó la rama y que los cambios se limitan a `docs/*.md` y este reporte.
- `git diff --check` se ejecutó sin errores de espacios o marcadores de conflicto. Los avisos de conversión LF/CRLF de Git no son errores del diff.
- Java modificado: **0**. SQL modificado: **0**. Migraciones modificadas: **0**. Tests modificados: **0**. Seguridad de aplicación modificada: **0**. Configuración modificada: **0**.
- No hubo cambios de API funcional, DTO, controller, servicio, permiso, decisión de dominio ni arquitectura ejecutable.

## 7. Gate final

**🟡 READY WITH OBSERVATIONS.** Las contradicciones prioritarias con evidencia clara quedaron delimitadas o corregidas documentalmente. Persisten material histórico y propuestas que deben leerse con las notas de vigencia, dos marcadores sin diseño validado y decisiones abiertas que no pueden resolverse por edición documental. La limpieza posterior puede avanzar usando el baseline, el Decision Register y C/D como referencias, conservando esas limitaciones.
