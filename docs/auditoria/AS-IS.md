# AS-IS — Consulta externa Hospital de Huaycán

**Versión:** 0.2, 23/09/2026. **Estado:** base documental [PENDIENTE_VALIDACION]; no es una representación institucional aprobada. Este archivo describe el proceso actual **solo hasta donde lo permiten las fuentes**. HospitalPlatform es la propuesta TO-BE y no se utiliza aquí como evidencia de operación presente.

Se emplean cuatro etiquetas: [CONFIRMADO] existencia documentada en las fuentes revisadas; [HIPOTESIS_DISCOVERY] posibilidad planteada para investigar; [PENDIENTE_VALIDACION] dato, secuencia o regla sin respaldo suficiente; [NO_SUSTENTADO] afirmación que no puede hacerse con la evidencia disponible. [CONFIRMADO] sobre una función no implica que esté probado su lugar en una secuencia.

## 1. Alcance del proceso

Consulta externa del Hospital de Huaycán como caso de estudio académico. El interés de investigación abarca solicitud/programación de cita y recorrido del paciente hasta el cierre, **sin** afirmar todavía canales, pasos, responsables, sistemas, tiempos ni resultados concretos (`docs/00-PROJECT-ROADMAP.md` §§4, 7.1). La unidad de análisis y los límites exactos deberán acordarse durante el levantamiento [PENDIENTE_VALIDACION].

## 2. Fuentes utilizadas

| Fuente | Aporte comprobable | Límite |
|---|---|---|
| `docs/00-PROJECT-ROADMAP.md` §§3–4, 7.1, 29 | Cita la existencia de un MAPRO de Consulta Externa y Hospitalización y funciones de Selección, Admisión y Archivo; propone preguntas de campo | Su secuencia §7.1 está rotulada como hipótesis; no sustituye lectura directa del MAPRO ni observación |
| `docs/research/recursos/CAPITULO-I.md` §§1.1–2.3 | Formula problema y reconoce falta de línea base | Es elaboración académica, no acta de proceso institucional |
| `docs/planning/requirements-elicitation.md` | Guía de preguntas, observación y criterio de cierre | Instrumento propuesto; el levantamiento no consta ejecutado |
| `docs/auditoria/AUDITORIA_DOCUMENTAL.md` | Inventario y límite de evidencia | Auditoría secundaria; no agrega hechos institucionales |

El MAPRO citado **no fue incorporado ni leído directamente** en este repositorio durante esta consolidación. Tampoco se hallaron entrevistas u observaciones autorizadas con pasos atribuibles al hospital. La existencia de un documento institucional citado no valida por sí sola una ruta específica.

## 3. Actores identificados

| ID | Actor | Responsabilidad conocida | Evidencia | Estado |
|---|---|---|---|---|
| A-01 | Paciente/usuario de consulta externa | Participante de la consulta externa; canal de solicitud y acciones concretas desconocidos | `docs/00` §§4, 7.1 | [PENDIENTE_VALIDACION] participación detallada |
| A-02 | Función de Selección | Existencia de función formal citada; ejecutor y actividad concreta no extraídos del MAPRO | `docs/00` §7.1 | [CONFIRMADO] función citada; [PENDIENTE_VALIDACION] operación |
| A-03 | Función de Admisión | Existencia de función formal citada; límites respecto de Selección desconocidos | `docs/00` §7.1 | [CONFIRMADO] función citada; [PENDIENTE_VALIDACION] operación |
| A-04 | Función de Archivo | Existencia de función formal citada; intervención por caso desconocida | `docs/00` §7.1 | [CONFIRMADO] función citada; [PENDIENTE_VALIDACION] operación |
| A-05 | Profesional de consulta externa | Atención en consultorio es elemento a verificar, sin asignación de tareas actual documentada | `docs/00` §7.1 | [HIPOTESIS_DISCOVERY] detalle del rol |
| A-06 | Personal de orientación, espera, llamado o triaje | Existencia, responsabilidades y condiciones por verificar | `docs/00` §§7.1, 29 | [HIPOTESIS_DISCOVERY] |
| A-07 | Encargado de citas/agenda | Rol, nombre institucional, canal y permisos no documentados | `docs/planning/requirements-elicitation.md` §§7–17 | [PENDIENTE_VALIDACION] |

Los nombres anteriores no equivalen a los roles técnicos de HospitalPlatform. La delimitación de unidades, cargos y responsables debe provenir del MAPRO vigente o de levantamiento autorizado.

## 4. Entradas del proceso

No hay inventario institucional validado de entradas [PENDIENTE_VALIDACION]. Deben comprobarse: solicitud o referencia, identificación, requisitos administrativos, especialidad solicitada y cita previa **si cada elemento aplica** (`docs/00` §7.1; `docs/planning/requirements-elicitation.md`). No se afirma que se usen documentos, formatos o sistemas específicos.

## 5. Salidas del proceso

No hay inventario institucional validado de salidas [PENDIENTE_VALIDACION]. Deben verificarse: constancia o registro de cita, constancia de llegada/admisión, llamado, atención y cierre o derivación **si existen** en la práctica documentada (`docs/00` §§7.1, 29). No se atribuyen al hospital registros digitales, tasas ni documentos concretos.

## 6. Flujo conocido

**No existe evidencia suficiente para establecer una secuencia de pasos institucionales.** La siguiente fila registra el límite de conocimiento; no es una actividad ni una flecha de proceso. Las funciones citadas se presentan por separado en §7.

| Paso | Actor | Actividad | Entrada | Resultado | Evidencia | Estado |
|---|---|---|---|---|---|---|
| — | Por identificar | Secuencia de consulta externa por levantar | Por determinar | Por determinar | `docs/00` §7.1 rotula el flujo como hipótesis; `docs/planning/requirements-elicitation.md` no documenta ejecución | [PENDIENTE_VALIDACION] |

## 7. Información conocida pero sin secuencia validada

| Elemento | Qué se sabe | Qué no se sabe | Estado |
|---|---|---|---|
| Selección | El roadmap cita función formal de Selección | Entrada, salida, responsable, orden e interacción con citas | [CONFIRMADO] existencia citada; [PENDIENTE_VALIDACION] detalle |
| Admisión | El roadmap cita función formal de Admisión | Si ocurre siempre, cuándo y bajo qué requisitos | [CONFIRMADO] existencia citada; [PENDIENTE_VALIDACION] detalle |
| Archivo | El roadmap cita función formal de Archivo | Si interviene en cada caso y con qué medio | [CONFIRMADO] existencia citada; [PENDIENTE_VALIDACION] detalle |
| Solicitud/referencia, orientación, llegada, espera, llamado, consulta y cierre | Aparecen en la **hipótesis** de `docs/00` §7.1 | Existencia real, orden, actor, excepción y medio | [HIPOTESIS_DISCOVERY] |
| Triaje cuando corresponda | Aparece de forma condicional en la hipótesis de `docs/00` §7.1 | Si aplica a consulta externa, a cuáles casos y con qué alcance | [HIPOTESIS_DISCOVERY] |
| Plan “Cero Colas” e infraestructura futura | Son contexto institucional citado en `docs/00` §3 y `recursos/CAPITULO-I.md` §1.1 | Su efecto concreto en el proceso observado | [CONFIRMADO] contexto citado; [PENDIENTE_VALIDACION] efecto |

## 8. Problemas confirmados

**No existe evidencia suficiente para afirmar un problema operacional específico.** La falta de línea base y de secuencia institucional validada es una **brecha de esta investigación**, no una falla demostrada del Hospital de Huaycán (`docs/00` §4.2; `docs/auditoria/AUDITORIA_DOCUMENTAL.md`). Afirmaciones como “largas colas”, “pérdida de citas”, “doble reserva actual” o “tasa alta de inasistencia” serían [NO_SUSTENTADO].

## 9. Variables pendientes de levantamiento

| Variable | Estado | Evidencia necesaria |
|---|---|---|
| Canales de solicitud, referencia, confirmación, cancelación y reprogramación | [PENDIENTE_VALIDACION] | MAPRO vigente y entrevista/observación autorizada |
| Secuencia exacta, excepciones, responsables y puntos de decisión | [PENDIENTE_VALIDACION] | Página/actividad de MAPRO y triangulación con personal |
| Herramientas, sistemas o medios actuales | [PENDIENTE_VALIDACION] | Demostración/documentación autorizada, sin acceso no autorizado |
| Criterios de agenda, cupos, lista de espera, prioridad o inasistencia si existen | [PENDIENTE_VALIDACION] | Política institucional y responsable del proceso |
| Tiempos de espera/atención, volúmenes, ocupación e inasistencias | [PENDIENTE_VALIDACION] | Datos agregados con periodo, definición de cohorte y autorización |
| Aplicación y responsabilidad de Selección, Admisión, Archivo y eventual triaje | [PENDIENTE_VALIDACION] | MAPRO y verificación de campo |

## 10. Preguntas para levantamiento institucional

1. ¿Qué versión y páginas del MAPRO describen consulta externa programada, y quién valida que sigan vigentes?
2. ¿Dónde empieza y termina el proceso de una cita ambulatoria y cuáles son sus salidas formales?
3. ¿Qué canales se usan hoy para solicitar, confirmar, cancelar o reprogramar una cita y quién ejecuta cada acción?
4. ¿Qué funciones realizan Selección, Admisión y Archivo, en qué casos intervienen y en qué orden?
5. ¿El triaje se aplica a consulta externa? Si sí, ¿a qué casos y quién lo realiza? No solicitar datos clínicos individuales.
6. ¿Cómo se organiza la espera, el llamado al consultorio y el cierre administrativo, si se registran?
7. ¿Qué herramientas o sistemas utiliza cada actor y qué evidencia autorizada permite describirlos?
8. ¿Existen reglas actuales de disponibilidad, liberación de cupos, lista de espera, prioridad y declaración de inasistencia? ¿Dónde están aprobadas?
9. ¿Qué datos agregados autorizados existen para medir tiempos, demanda, cancelaciones e inasistencias, con qué definiciones y periodo?
10. ¿Qué excepciones o rutas alternativas reconoce el personal y qué documento/observación permite verificarlas?

## 11. Límite del AS-IS

No puede modelarse como hecho una secuencia institucional, un cuello de botella, una métrica, un sistema actualmente usado o una regla operativa sin evidencia adicional. En particular, app móvil, portal de reservas, lista de espera digital, `SlotOffer`, `FlowStage`, auditoría digital, dashboard, notificaciones y control de concurrencia son conceptos del TO-BE de HospitalPlatform; **no** se atribuyen a la operación actual del hospital. Solo tras MAPRO directo y levantamiento autorizado podrá cerrarse el AS-IS para diagramas definitivos.
