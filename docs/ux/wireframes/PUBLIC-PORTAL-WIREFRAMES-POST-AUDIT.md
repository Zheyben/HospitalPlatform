# ETAPA F.3.2.1 — Post-audit de wireframes del portal público

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico del Hospital de Huaycán.

**Documento auditado:** [PUBLIC-PORTAL-LOW-FIDELITY-WIREFRAMES.md](PUBLIC-PORTAL-LOW-FIDELITY-WIREFRAMES.md).

## 1. Estado Git y alcance

Al inicio, `git status --short --branch` confirmó `feat/hospitalplatform-wireframes` y mostró únicamente `?? docs/ux/wireframes/`. El directorio contiene el inventario F.3.1, su auditoría y los wireframes públicos sin seguimiento. Esta auditoría no altera esos archivos; añade solo este informe. El estado `IMPLEMENTADO` se interpreta como soporte del **backend**, no como una pantalla terminada.

## 2. Fuentes contrastadas

- [Inventario F.3.1](WIREFRAME-INVENTORY.md) y [post-audit F.3.1](WIREFRAME-INVENTORY-POST-AUDIT.md): WF-01–WF-06, estados y corte entre descubrimiento y envío.
- [Flujo público F.2.1](../user-flows/PUBLIC-PORTAL-USER-FLOWS.md), [su post-audit](../user-flows/PUBLIC-PORTAL-USER-FLOW-POST-AUDIT.md) y [consolidación F.2.3](../user-flows/USER-FLOW-CONSOLIDATION.md): recorridos de cuenta existente/nueva, `slotId` conocido y reserva `SCHEDULED`.
- [D.2 detalles de CU-D1–CU-D8](../../agenda/use-cases/AGENDA-AVAILABILITY-USE-CASE-DETAILS.md), [D.3.1 implementados](../../agenda/scenarios/AGENDA-IMPLEMENTED-SCENARIOS.md) y [D.3.3 conceptuales](../../agenda/scenarios/AGENDA-CONCEPTUAL-SCENARIOS.md): CU-D6 conceptual, CU-D7 reserva vigente y CU-D8 invariante interna. [D.3.2 parcial](../../agenda/scenarios/AGENDA-PARTIAL-SCENARIOS.md) se revisó para no atribuir a profesionales u horarios un selector público funcional.
- [Documento maestro Word final](../../documentation-export/HOSPITALPLATFORM-COMPLETE-DOCUMENTATION.docx), abierto en lectura: confirma RF-010 sin frontend, `auth/register` inexistente, CU-D6 sin URI/DTO/autorización y DEC-017 abierta.

## 3. Validación por lámina

| Lámina | Inventario / trazabilidad | Resultado |
|---|---|---|
| 01 Landing / Acceso | WF-01 → CU `—` → RF-010 → `FUTURO` | Conforme. La identidad es provisional; el registro está rotulado no activo y no se declara portal frontend construido. |
| 02 Login | WF-02 → CU `—` → RF-002 → backend `IMPLEMENTADO` | Conforme. Usa `email` y `password` para cuenta existente, con vacío, validando, `400`/`401` y acceso correcto como presentación. No asocia el login a un autorregistro activo. |
| 03 Registro paciente | WF-03 → CU `—` → RF-001 → `FUTURO`, boceto `CONCEPTUAL`; DEC-017 `OPEN` | Conforme. No fija campos ni validaciones, tabla, DTO, endpoint o estado de aprobación. «Crear cuenta» está inactivo y no conecta automáticamente con login. |
| 04 Solicitud de cita | Zona A: WF-04 → CU-D6 → RF-012 → `CONCEPTUAL`. Zona B: WF-05 → CU-D7/CU-D8 interno → RF-013/014 → backend `IMPLEMENTADO` | Conforme. La zona A señala especialidad/profesional/fecha como necesidades aún sin selector, filtros ni API PATIENT. La zona B envía solo `slotId` requerido y `reason` opcional cuando el identificador ya es conocido; PATIENT no aporta `patientId`. El corte gráfico impide inferir una selección conectada. |
| 05 Resultado de reserva | WF-06 → CU-D7 → RF-015 → constancia UX `PARCIAL` | Conforme. El éxito exige respuesta `201` y muestra `SCHEDULED`; un rechazo no produce comprobante. `CONFIRMED` queda para una operación posterior independiente. |

Los identificadores de las **cinco láminas** son ordinales del entregable: la lámina 04 agrupa dos referencias WF del inventario. Esa agrupación se declara al inicio y no cambia los CU ni sus estados. `WF-07` se menciona solo como consulta propia posterior respaldada por GET; no se inventa una sexta lámina funcional.

## 4. Datos, acciones y permisos

El login refleja los campos actuales y no promete recuperación de cuenta. El registro muestra zonas por decidir, no un formulario aprobable. En solicitud, `especialidad`, `profesional` y `fecha/hora` son anotaciones conceptuales: **no** se toman del DTO operativo ADMIN ni se convierten en filtros PATIENT. El request vigente de reserva admite `slotId` y `reason` opcional para PATIENT; profesional y especialidad se derivan del slot/schedule. La vista no expone `patientId` como entrada de PATIENT.

La disponibilidad sanitizada de DEC-007 sigue sin API, DTO o permiso PATIENT; `CLOSED` aprueba la dirección de dominio, no la implementación. CU-D8 aparece como protección interna sin botón propio. La reserva se revalida en el backend; un `409 SLOT_UNAVAILABLE` no deja cita parcial. Las láminas no añaden roles, endpoints, tablas, estados de cita ni reglas temporales. El nombre «Confirmación de reserva registrada» se acota expresamente como resultado visual, sin confundirlo con `appointmentStatus=CONFIRMED`.

## 5. Hallazgos y correcciones

**Errores factuales críticos: 0; medios: 0; bajos: 0. Correcciones al wireframe base: 0.**

**Observación 1:** la lámina 04 coloca el descubrimiento conceptual y el envío respaldado por API en un mismo bosquejo. La separación A/B y el corte «no existe API de descubrimiento» son correctos; al convertir el bosquejo en prototipo navegable deberá conservarse esa barrera para no sugerir una selección PATIENT operativa.

**Observación 2:** la lámina 03 es útil para representar la intención de registro, pero DEC-017 continúa `OPEN` y RF-001 no dispone de autorregistro público. El documento ya impide ejecutar «Crear cuenta» o prometer login como resultado del boceto.

**Observación 3:** la lámina 05 utiliza datos de la respuesta `201`; RF-015 continúa `PARCIAL` para constancia UX. No se debe interpretar el bosquejo como comprobante final implementado.

Estas observaciones describen dependencias existentes, no contradicciones del archivo. No se cambió el wireframe auditado.

## 6. Validación final y gate

Se ejecutaron `git status --short --branch` y `git diff --check`. El segundo comando no informó errores en archivos rastreados. Como el directorio sigue sin seguimiento, se revisaron directamente el Markdown auditado y este informe por espacios finales y enlaces locales. Archivos creados en esta post-auditoría: `PUBLIC-PORTAL-WIREFRAMES-POST-AUDIT.md`. Archivos existentes modificados: **0**.

**Java: 0; SQL: 0; tests: 0; migraciones: 0; seguridad: 0.** No se ejecutó Maven ni se hizo commit, push o merge.

**🟡 PUBLIC PORTAL WIREFRAMES APPROVED WITH OBSERVATIONS.** Las láminas reflejan los flujos y estados aprobados para diseño documental. El recorrido completo registro → descubrimiento → reserva permanece interrumpido por capacidades aún no implementadas.
