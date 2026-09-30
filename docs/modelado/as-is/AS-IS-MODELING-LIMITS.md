# AS-IS — límites para modelado

**Estado:** PENDIENTE DE VALIDACIÓN INSTITUCIONAL. Este documento **no** es un BPMN ni IDEF0 AS-IS definitivo y no propone una secuencia. Se deriva únicamente de `docs/auditoria/AS-IS.md` al 23/09/2026. El TO-BE de HospitalPlatform y sus RF no prueban la operación actual del Hospital de Huaycán.

## Objetivo

Precisar qué evidencia habilitaría legítimamente futuros modelos AS-IS, sin convertir hipótesis de discovery en hechos.

## Alcance

Consulta externa programada como caso de estudio; no se atribuyen canales, sistemas, reglas, tiempos ni resultados institucionales no verificados.

## Fuente

`docs/auditoria/AS-IS.md` §§1–11; `docs/00-PROJECT-ROADMAP.md` §§3–4, 7.1, 29; `docs/planning/requirements-elicitation.md`. El MAPRO está citado de segunda mano: no fue leído directamente en esta consolidación.

## Actores

| Elemento | Etiqueta | Evidencia y límite |
|---|---|---|
| Paciente de consulta externa | [PENDIENTE_VALIDACION] detalle | Participación general citada; acciones/canales no verificados |
| Funciones Selección, Admisión y Archivo | [CONFIRMADO] existencia citada; [PENDIENTE_VALIDACION] intervención | Roadmap §7.1 menciona funciones formales, no prueba orden ni ejecutor |
| Profesional, orientación, triaje, encargado de citas | [HIPOTESIS_DISCOVERY] o [PENDIENTE_VALIDACION] | Presencia y responsabilidad por caso requieren fuente directa |

## Actividades

Ninguna secuencia institucional está confirmada. Solicitud/referencia, llegada, espera, llamado, consulta y cierre son [HIPOTESIS_DISCOVERY] de `docs/00` §7.1, sin flechas entre sí. No se crea una figura provisional para evitar confundir una lista de conceptos con proceso ejecutado.

## Decisiones

No hay gateways AS-IS verificados. La eventual aplicación de triaje, asignación de cupo, admisión, cancelación, inasistencia o cierre requiere fuente institucional y casos de excepción.

## Requisitos relacionados

Ningún RF de HospitalPlatform demuestra un paso AS-IS. OE1 del roadmap exige levantamiento; la SRS canónica solo especifica el TO-BE propuesto.

## Reglas de negocio relacionadas

RB-001–024 rigen el diseño propuesto, no describen reglas vigentes del hospital. Deben obtenerse políticas institucionales actuales por separado.

## Exclusiones

Sin BPMN AS-IS definitivo, IDEF0 AS-IS, hipótesis dibujada como proceso confirmado, métricas atribuidas al hospital, ni estados/software HospitalPlatform trasladados al AS-IS.

## Pendientes de validación

| Vacío | Impide | Fuente/levantamiento que lo resolvería |
|---|---|---|
| Inicio, fin, pasos, orden y rutas alternativas | BPMN AS-IS | Lectura directa de versión/páginas del MAPRO vigente y observación/entrevista autorizada |
| Actor ejecutor de cada paso y traspasos | Pools/lanes BPMN AS-IS | MAPRO con responsables y triangulación con personal de Selección, Admisión, Archivo y consulta |
| Eventos, reglas de decisión y excepciones | Gateways/eventos BPMN AS-IS | Procedimiento institucional y casos observados/documentados |
| Entradas y salidas formales por función | ICOM IDEF0 AS-IS | Formularios/registros permitidos, MAPRO y validación de responsables |
| Controles y políticas aplicadas | Controls IDEF0 AS-IS | Normativa/protocolos institucionales vigentes con versión/aprobación |
| Recursos, sistemas y roles reales | Mechanisms IDEF0 AS-IS | Inventario y demostración autorizados; entrevista por rol |
| Tiempos, volúmenes, inasistencia y cuellos de botella | Análisis cuantitativo o problemas confirmados | Datos agregados autorizados con periodo, cohorte y definición de indicador |

El AS-IS podrá modelarse después de registrar para cada paso/flecha fuente, versión, página o acta, responsable de validación y estado de evidencia. Hasta entonces el límite de `AS-IS.md` §11 permanece vigente.

## Archivo fuente del diagrama

No aplica: deliberadamente no se creó archivo de figura AS-IS ni se trazó una secuencia institucional.
