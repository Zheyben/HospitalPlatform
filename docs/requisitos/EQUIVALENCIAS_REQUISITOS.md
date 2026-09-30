# Conciliación y equivalencias históricas de requisitos

**Estado:** propuesta documental de consolidación, 23/09/2026. Las dos series antiguas permanecen intactas. `RF-0XX` y `RNF-0XX` de esta carpeta son identificadores **nuevos**; el mismo número en el PRD no implica la misma semántica. Fuentes: `docs/01-PRD.md` §§5–7; `docs/research/recursos/CAPITULO-III.md` §1.1; jerarquía y contradicciones en `docs/auditoria/AUDITORIA_DOCUMENTAL.md`.

Relaciones: **EQUIVALENTE** coincide en comportamiento esencial; **PARCIALMENTE_EQUIVALENTE** coincide en tema, difiere en actor, condición, dato, error o alcance; **AMPLIACIÓN** la segunda fuente agrega comportamiento relevante; **DIVISIÓN** una redacción agrupa varias operaciones; **SIN_EQUIVALENTE** carece de par; **CONTRADICCIÓN** vulnera decisión vigente. Esta clasificación se aplica por fila del PRD, no al número de vínculos del esquema canónico.

## Comparación RF anterior a la asignación canónica

| PRD | Capítulo III | Relación | Diferencias examinadas | Decisión |
|---|---|---|---|---|
| RF-001 | RF-01, RF-24 | DIVISIÓN | Paciente, registro/login y sesiones tienen datos, errores y resultados diferentes | Separar registro, autenticación y ciclo de sesión |
| RF-002 | RF-02 | PARCIALMENTE_EQUIVALENTE | Cap. III añade propiedad del recurso y roles concretos; roles propuestos del roadmap difieren | Un RF de administración/autorización; roles sujetos a mapeo |
| RF-003 | RF-23 | PARCIALMENTE_EQUIVALENTE | PRD solo muestra información; cap. III agrega administración y autorización | Separar publicación y consulta pública |
| RF-004 | RF-06, RF-23 | AMPLIACIÓN | Gestión y visualización de especialidad se mezclan; inactiva no reservable | Separar mantenimiento de consulta; contenido en RF público |
| RF-005 | RF-23 | PARCIALMENTE_EQUIVALENTE | Contenido de especialidad no define publicación/verificación ni error | Absorber en mantenimiento de contenido público |
| RF-006 | RF-08 | EQUIVALENTE | Mismo actor lector, filtros y cupos disponibles; Cap. III precisa reservados/bloqueados | Un requisito de disponibilidad con aceptación precisa |
| RF-007 | RF-09 | PARCIALMENTE_EQUIVALENTE | Cap. III exige paciente/profesional/slot y estado inicial; concurrencia en RF-10 | Un RF de reserva y otro de integridad concurrente |
| RF-008 | RF-13 | PARCIALMENTE_EQUIVALENTE | Cap. III añade permisos, historial y liberación; política temporal no validada | Cancelación y liberación como acciones diferenciadas |
| RF-009 | RF-12 | PARCIALMENTE_EQUIVALENTE | Cap. III agrega sucesora y conservación de original ante fallo | Reprogramación atómica |
| RF-010 | RF-10 | EQUIVALENTE | Ambos previenen doble cupo; Cap. III define resultado en carrera | Un RF de asignación exclusiva |
| RF-011 | RF-05 | PARCIALMENTE_EQUIVALENTE | Cap. III incorpora colegiatura y asociación a especialidades | Gestión de profesional, campos según validación |
| RF-012 | RF-07 | PARCIALMENTE_EQUIVALENTE | Cap. III precisa horarios, slots y rechazo de incompatibilidad | Configuración de agenda/slots |
| RF-013 | RF-18 | PARCIALMENTE_EQUIVALENTE | PRD no enumera estados; ADR-007 limita actor, orden y valores | Dividir por las cuatro transiciones aprobadas |
| RF-014 | Ninguno compatible | CONTRADICCIÓN | Registros de diagnóstico/indicaciones chocan con roadmap §6.2 y ADR-007 | No entra a SRS MVP; conservar ID histórico como fuera de alcance |
| RF-015 | RF-22 | EQUIVALENTE | Ambos exigen registrar acciones; Cap. III precisa actor/entidad/fecha y secretos excluidos | Un RF de auditoría |
| RF-016 | RF-16 | PARCIALMENTE_EQUIVALENTE | Canales, fallos y recordatorios configurables se precisan después | Un RF de notificación; calendario por validar |
| RF-017 | RF-14, RF-21 | AMPLIACIÓN | Cap. III agrega recuperación; roadmap exige oferta, aceptación y expiración | Separar entrada, oferta y respuesta; no asignación sin consentimiento |
| RF-018 | RF-20 | PARCIALMENTE_EQUIVALENTE | PRD pide configuración; Cap. III define consulta por periodo, métricas aún sin denominador | Indicadores con definiciones y filtros autorizados |
| RF-019 | RF-17, RF-18 | PARCIALMENTE_EQUIVALENTE | `CHECK_IN` aprobado; admisión adicional sin estado aprobado | RF de check-in; admisión institucional pendiente |
| RF-020 | Ninguno compatible | CONTRADICCIÓN | Evaluación clínica de triaje frente a roadmap §6.2 y ADR-007 | No entra a SRS MVP; no crear etapa ni dato clínico |

**Conteo RF por las 20 filas del PRD:** EQUIVALENTE 3; PARCIALMENTE_EQUIVALENTE 12; AMPLIACIÓN 2; DIVISIÓN 1; CONTRADICCIÓN 2; SIN_EQUIVALENTE 0. RF de Capítulo III sin par directo: RF-03/04 (perfil/consulta), RF-15 (prioridad), RF-24 (sesiones, aunque amplía PRD RF-001); RF-21 amplía RF-017. No se perdió ninguna función válida por carecer de par.

## Comparación RNF anterior a la asignación canónica

| RNF PRD | RNF Capítulo III | Categoría | Relación | Diferencias | Decisión |
|---|---|---|---|---|---|
| RNF-001 | RNF-01 | Seguridad/autenticación | PARCIALMENTE_EQUIVALENTE | Cap. III agrega hash y HTTPS | Conservar control medible |
| RNF-002 | RNF-01 | Autorización | AMPLIACIÓN | Roles solos son insuficientes; propiedad de objeto y permisos servidor | Unificar con RNF de seguridad, probar BOLA |
| RNF-003 | RNF-02 | Privacidad | PARCIALMENTE_EQUIVALENTE | Cap. III agrega datos sintéticos y minimización | Conservar en RNF privacidad |
| RNF-004 | RNF-10, RNF-01 | Seguridad/auditoría | PARCIALMENTE_EQUIVALENTE | Eventos de seguridad no equivalen a todas las transiciones | Conservar eventos seguridad explícitos |
| RNF-005 | RNF-03 | Rendimiento | AMPLIACIÓN | PRD no tiene métrica; Cap. III propone p95 y carga | Meta académica condicionada al entorno |
| RNF-006 | RNF-05 | Usabilidad | AMPLIACIÓN | PRD no fija muestra ni umbral | Ensayo académico reproducible |
| RNF-007 | RNF-06 | Accesibilidad | AMPLIACIÓN | “Básica” no alcanza referencia WCAG 2.2 AA del roadmap | Criterios web/móvil, color y navegación |
| RNF-008 | RNF-08 | Mantenibilidad | PARCIALMENTE_EQUIVALENTE | Cap. III concreta estructura y pruebas | Verificación por revisión y migraciones |
| RNF-009 | RNF-08/09 | Evolución modular | PARCIALMENTE_EQUIVALENTE | Solapa RNF-013 PRD; carga no equivale a modularidad | Mantener arquitectura modular y prueba de carga separadas |
| RNF-010 | RNF-07 | Compatibilidad | PARCIALMENTE_EQUIVALENTE | Cap. III concreta navegadores y stack móvil; versiones sin fijar | Matriz de entorno |
| RNF-011 | RNF-10 | Trazabilidad | EQUIVALENTE | Cap. III agrega reconstrucción del 100 % de eventos ensayados | Un requisito de trazabilidad |
| RNF-012 | Ninguno directo | Observabilidad técnica | SIN_EQUIVALENTE | Cap. III RNF-10 se centra en negocio; logs técnicos son distintos | Conservar RNF de observabilidad con método verificable |
| RNF-013 | RNF-08/09 | Evolución futura | PARCIALMENTE_EQUIVALENTE | Aspiración sin aceptación; solapa RNF-009 | Absorber como criterio arquitectónico de mantenibilidad, sin prometer módulo futuro |

**Conteo RNF por las 13 filas PRD:** EQUIVALENTE 1; PARCIALMENTE_EQUIVALENTE 7; AMPLIACIÓN 4; SIN_EQUIVALENTE 1; CONTRADICCIÓN 0. Capítulo III aporta además disponibilidad de ensayo (RNF-04), integridad (RNF-11) y recuperación (RNF-12), todos retenidos.

## Tabla de equivalencias RF canónicos

“—” significa que la obligación procede de roadmap/ADR o de la división de una operación compuesta, sin ID literal equivalente. Ningún ID viejo se borra.

| ID canónico | PRD anterior | Capítulo III anterior | Decisión/fuente complementaria |
|---|---|---|---|
| RF-001 | RF-001 | RF-01 | Registro |
| RF-002 | RF-001 | RF-01 | Login |
| RF-003 | RF-001 | RF-24 | Renovación/revocación de sesión; ADR-003/004 |
| RF-004 | RF-002 | RF-02 | Roles y propiedad; ADR-005 |
| RF-005 | — | RF-03 | Perfil paciente |
| RF-006 | — | RF-04 | Citas propias |
| RF-007 | RF-011 | RF-05 | Profesionales |
| RF-008 | RF-004 | RF-06 | Administración especialidades |
| RF-009 | RF-003, RF-005 | RF-23 | Contenido público autorizado |
| RF-010 | RF-003, RF-004 | RF-06, RF-23 | Consulta pública/CTA |
| RF-011 | RF-012 | RF-07 | Agenda y slots; ADR-006 |
| RF-012 | RF-006 | RF-08 | Disponibilidad |
| RF-013 | RF-007 | RF-09 | Crear cita |
| RF-014 | RF-010 | RF-10 | Exclusividad concurrente |
| RF-015 | RF-007 | RF-11 | Constancia separada de confirmación |
| RF-016 | — | RF-11 | Confirmar asistencia; roadmap §18 |
| RF-017 | RF-008 | RF-13 | Cancelar |
| RF-018 | RF-008 | RF-13, RF-21 | Liberar cupo; roadmap §18 |
| RF-019 | RF-009 | RF-12 | Reprogramar |
| RF-020 | RF-017 | RF-14 | Espera |
| RF-021 | — | RF-15 | Solicitar prioridad; ADR-008 |
| RF-022 | — | RF-15 | Revisar prioridad; ADR-008 |
| RF-023 | RF-016 | RF-16 | Notificaciones |
| RF-024 | RF-017 | RF-21 | Crear oferta, consentimiento roadmap §§11/18 |
| RF-025 | RF-017 | RF-21 | Responder oferta y asignar transaccionalmente |
| RF-026 | RF-019 | RF-17/18 | `CHECK_IN`; ADR-007 |
| RF-027 | RF-013 | RF-18 | `WAITING`; ADR-007 |
| RF-028 | RF-013 | RF-18 | `IN_ATTENTION`; ADR-007 |
| RF-029 | RF-013 | RF-18 | `FINISHED`/`COMPLETED`; ADR-007 |
| RF-030 | — (HU-007/008) | RF-19 | Consulta operativa por rol; RF-018 PRD corresponde a indicadores |
| RF-031 | RF-018 | RF-20 | Indicadores |
| RF-032 | RF-015 | RF-22 | Auditoría |
| RF-033 | — | — | Preferencias de accesibilidad del roadmap §14.3.1 [PROPUESTA] |

**RF-014 y RF-020 del PRD** se retienen únicamente en la comparación como antecedentes incompatibles con el MVP; no equivalen a RF-014/RF-020 canónicos. No hay requisitos canónicos “eliminados”: las operaciones solapadas se expresan una vez, y los dos RF clínicos antiguos se excluyen del MVP por decisión superior.

## Tabla de equivalencias RNF canónicos

| ID canónico | PRD anterior | Capítulo III anterior | Decisión |
|---|---|---|---|
| RNF-001 | RNF-001/002 | RNF-01 | Autenticación/autorización segura |
| RNF-002 | RNF-003 | RNF-02 | Privacidad y datos sintéticos |
| RNF-003 | RNF-005 | RNF-03 | Rendimiento de ensayo |
| RNF-004 | — | RNF-04 | Disponibilidad de ensayo |
| RNF-005 | RNF-006 | RNF-05 | Usabilidad |
| RNF-006 | RNF-007 | RNF-06 | Accesibilidad |
| RNF-007 | RNF-010 | RNF-07 | Compatibilidad |
| RNF-008 | RNF-008/009/013 | RNF-08 | Mantenibilidad modular |
| RNF-009 | RNF-009 parcialmente | RNF-09 | Escalabilidad medida |
| RNF-010 | RNF-004/011 | RNF-10 | Auditoría/trazabilidad |
| RNF-011 | — | RNF-11 | Integridad transaccional |
| RNF-012 | — | RNF-12 | Respaldo/restauración |
| RNF-013 | RNF-012 | — | Observabilidad técnica conservada |

No hay cifra `LINEA_BASE_REAL` en estas dos series. Las cifras de Capítulo III son `META_ACADÉMICA` y siguen sujetas a aprobación y reproducibilidad del ensayo.
