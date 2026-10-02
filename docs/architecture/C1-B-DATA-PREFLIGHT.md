# C1-B — Precheck y correspondencia sintética V4→V5

**Fecha:** 2026-09-30 (America/Bogota). Lecturas `READ ONLY` con `ROLLBACK` sobre `hospital-platform-postgres-1`, PostgreSQL 16.15. Este archivo no ejecuta cambios.

## Resultado previo a V5

V1–V4; 15 usuarios, 14 pacientes, 1 profesional, 7 horarios, 84 slots y 18 citas. Catorce pacientes tienen `DNI` con número incompatible con ocho dígitos; una colegiatura tiene texto incompatible con 4–6 dígitos. No se encontraron duplicados normalizados de email/username, documentos compuestos ni colegiaturas; tampoco asociaciones horario-profesional-especialidad inválidas, slots fuera de horario o de duración incorrecta, citas con profesional distinto al horario, slots `RESERVED` huérfanos ni horarios activos solapados. Hay 0 ADMIN activos y la única cuenta profesional está deshabilitada; estas condiciones requieren tratamiento separado y no justifican borrar pacientes o citas.

## Correspondencia aprobada para datos demo

Los nuevos números `90000001`–`90000014` son identificadores **sintéticos de prueba**, no documentos reales. Cada sustitución se realiza solo si coinciden UUID, tipo y valor antiguo; la migración falla si la fuente difiere. No se cambia ningún UUID ni FK.

| patient_id existente | DNI antiguo | DNI sintético nuevo |
|---|---|---|
| `093e273b-43d9-4bb1-b275-c9bed2ccb358` | `1790812486462683` | `90000001` |
| `1ad64dbe-35eb-4b93-a411-39fd309ee39e` | `1790812366391234` | `90000002` |
| `2295852d-0f98-4cbd-8f77-f3539c59fc3e` | `B2-56a05b4757fa43e0` | `90000003` |
| `33db4d03-1a03-4165-a91b-2ba09149d1a8` | `B2-26fd4ea41c8a40e2` | `90000004` |
| `43d10d5d-6e03-4d80-8f9a-4018039d78f0` | `1790813053314` | `90000005` |
| `63ac1e83-d3e2-402a-a47f-30fae309e177` | `B2F-b810c705cef24f7b` | `90000006` |
| `6b8bde3d-5014-434f-8c74-f45de837ab23` | `1790812176982980` | `90000007` |
| `80c2e11a-2910-458f-af27-77e2bd9f7e93` | `9020260930180602` | `90000008` |
| `85bb8787-8ef8-42a5-81de-cbdd60ec789d` | `1790813533229783` | `90000009` |
| `8dbac740-af9c-4c97-ab95-39d122b0fbb1` | `1790813322872346` | `90000010` |
| `aaba452c-4a0b-4fb3-a600-8ac74a45881c` | `1790812870450706` | `90000011` |
| `af3e4cb6-b6b8-4fd3-bbad-a739585708d8` | `1790812195194439` | `90000012` |
| `dc0a1aeb-5077-4002-bd2e-baaadcbeb5b7` | `1790812286082548` | `90000013` |
| `f7e7e408-03b7-4f14-8ee7-b85e3851ffff` | `1790812462256890` | `90000014` |

| professional_id existente | Colegiatura antigua | Colegiatura sintética nueva |
|---|---|---|
| `88743663-5b1b-3868-bf1a-aa0371adfac3` | `DEMO-CMP-0001` | `900001` |

La migración V5 valida que los destinos no estén ocupados. Si la base no contiene estos registros (instalación limpia), la correspondencia no hace cambios. Si contiene uno de los UUID con un valor distinto del esperado, aborta antes de crear constraints; no adivina identidad. Los demás datos incompatibles detectados por el preflight también provocan fallo explícito, no una limpieza automática.
