# Requirements Elicitation Plan — Hospital de Huaycán

**Proyecto:** `hospital-platform`  
**Curso:** Curso Integrador I: Sistemas Software  
**Unidad de aprendizaje:** Unidad 1 — Planificación y análisis  
**Documento:** Plan de toma y levantamiento de requerimientos  
**Caso de estudio propuesto:** Hospital de Huaycán — MINSA / DIRIS Lima Este  
**Estado:** versión inicial académica, pendiente de validación docente y ejecución del levantamiento

> Este documento define **cómo se obtendrán, registrarán, validarán y priorizarán los requerimientos** del proyecto.
>
> No contiene todavía requerimientos definitivos del Hospital de Huaycán. Las preguntas, actores, procesos y reglas descritas aquí constituyen un **instrumento de levantamiento** y deben contrastarse con fuentes institucionales y personal autorizado.
>
> El proyecto utilizará únicamente información necesaria para fines académicos, priorizando datos agregados y evitando historias clínicas o datos personales identificables.

---

# 1. Objetivo del documento

Definir una estrategia estructurada para identificar los requerimientos funcionales y no funcionales de la plataforma web y móvil propuesta para la gestión de citas médicas y la trazabilidad del flujo de consulta externa del Hospital de Huaycán.

El levantamiento debe permitir conocer:

- cómo funciona actualmente el proceso de citas;
- qué actores participan;
- qué canales utilizan;
- qué información necesitan;
- qué reglas de negocio existen;
- qué problemas están realmente presentes;
- qué restricciones institucionales y tecnológicas deben respetarse;
- qué capacidades debe tener la solución;
- qué requisitos pertenecen al MVP;
- qué elementos deben mantenerse fuera del alcance.

---

# 2. Objetivos específicos del levantamiento

## OE-L1 — Comprender el proceso actual

Reconstruir el proceso **AS-IS** de gestión de citas y consulta externa, identificando actividades, responsables, decisiones, documentos, sistemas y excepciones.

## OE-L2 — Identificar necesidades

Determinar las necesidades reales de pacientes y personal involucrado en:

- programación;
- confirmación;
- cancelación;
- reprogramación;
- disponibilidad;
- admisión;
- espera;
- llamado;
- seguimiento.

## OE-L3 — Identificar reglas de negocio

Recopilar reglas relacionadas con:

- creación de agendas;
- asignación de cupos;
- requisitos administrativos;
- cancelaciones;
- reprogramaciones;
- no-show;
- lista de espera;
- prioridad ambulatoria;
- check-in;
- cierre de atención.

## OE-L4 — Identificar restricciones

Determinar restricciones:

- institucionales;
- normativas;
- operativas;
- tecnológicas;
- de seguridad;
- de privacidad;
- de conectividad;
- de accesibilidad.

## OE-L5 — Obtener indicadores de línea base

Identificar qué métricas existen o pueden obtenerse de forma agregada y autorizada para evaluar posteriormente la propuesta.

---

# 3. Alcance del levantamiento

El levantamiento se concentrará en **consulta externa programada**.

## Incluye

- información pública institucional;
- especialidades y servicios;
- mecanismos de solicitud de cita;
- agendas y disponibilidad;
- reserva;
- confirmación;
- cancelación;
- reprogramación;
- inasistencia/no-show;
- liberación y reutilización de cupos;
- lista de espera, si existe;
- priorización ambulatoria, si existe;
- llegada/check-in;
- admisión;
- archivo o vinculación administrativa;
- triaje cuando corresponda;
- espera;
- llamado;
- consulta como estado operativo;
- finalización administrativa;
- notificaciones;
- actores;
- sistemas utilizados;
- métricas operativas.

## No incluye como objeto de levantamiento detallado

- diagnóstico;
- tratamiento médico;
- historia clínica completa;
- prescripción;
- farmacia clínica;
- laboratorio clínico;
- imágenes diagnósticas;
- hospitalización;
- emergencia;
- cirugía.

Si durante el levantamiento aparecen estos procesos, se documentarán únicamente como **puntos de integración o dependencias externas**, sin expandir el MVP automáticamente.

---

# 4. Principios del levantamiento

1. **No confirmar hipótesis previamente.**  
   Las entrevistas deben descubrir el proceso, no forzar respuestas que validen el diseño actual.

2. **Separar hechos de opiniones.**  
   Una percepción del usuario se registra como percepción; una regla institucional requiere evidencia o validación responsable.

3. **No inventar cifras.**  
   No se registrarán tiempos, porcentajes o volúmenes que no puedan sustentarse.

4. **Triangulación.**  
   Cuando sea posible, contrastar:
   - entrevista;
   - observación;
   - documento institucional.

5. **Minimización de datos.**  
   Recopilar solo información necesaria para el objetivo académico.

6. **Trazabilidad.**  
   Cada requisito debe poder relacionarse con una fuente, actor o evidencia.

7. **Validación posterior.**  
   Los hallazgos iniciales deben ser revisados antes de convertirse en requisitos definitivos.

---

# 5. Técnicas de levantamiento

Se utilizará una combinación de técnicas.

## 5.1 Entrevistas semiestructuradas

Permiten conocer:

- actividades;
- problemas;
- excepciones;
- reglas;
- necesidades;
- restricciones;
- expectativas.

Ventaja:

- permiten profundizar según las respuestas.

Riesgo:

- la respuesta puede representar una percepción individual y no una regla institucional.

Mitigación:

- contrastar con otros actores y documentación.

---

## 5.2 Observación del proceso

Permite registrar:

- secuencia real;
- puntos de espera;
- interacciones;
- documentos;
- cambios de responsable;
- canales;
- interrupciones;
- excepciones.

La observación deberá evitar registrar:

- nombres;
- DNI;
- diagnósticos;
- fotografías de información clínica;
- pantallas con datos personales.

---

## 5.3 Revisión documental

Fuentes prioritarias:

- MAPRO de Consulta Externa y Hospitalización;
- Cartera de Servicios;
- Plan Cero Colas;
- resoluciones institucionales;
- información pública del Hospital de Huaycán;
- documentación DIRIS Lima Este;
- documentación MINSA;
- PRONIS como contexto de infraestructura.

Objetivo:

- identificar procesos formalmente definidos;
- actores;
- responsabilidades;
- cartera vigente;
- políticas;
- restricciones.

---

## 5.4 Análisis de sistemas existentes

Si existe autorización:

- identificar aplicaciones utilizadas;
- qué parte del proceso cubren;
- qué información registran;
- qué tareas siguen siendo manuales;
- qué integraciones existen;
- limitaciones percibidas.

No se solicitarán credenciales ni acceso no autorizado.

---

## 5.5 Taller de requerimientos

Después de entrevistas y observación, el equipo podrá realizar un taller interno o con actores autorizados para:

- validar hallazgos;
- resolver contradicciones;
- ordenar necesidades;
- priorizar;
- validar prototipos iniciales.

---

## 5.6 Prototipado

Los primeros wireframes se utilizarán como herramienta de validación, no como evidencia de que un requisito ya está aprobado.

Permitirán verificar:

- comprensión del flujo;
- orden de pantallas;
- información necesaria;
- acciones principales;
- mensajes;
- accesibilidad.

---

# 6. Stakeholders a consultar

| Actor | Información que puede aportar | Prioridad |
| --- | --- | --- |
| Paciente | experiencia, canales, dificultades, necesidades, accesibilidad | Alta |
| Admisión / recepción | proceso de registro, citas, excepciones, documentos | Alta |
| Responsable de agenda/citas | cupos, horarios, reglas, cancelaciones, disponibilidad | Alta |
| Profesional de salud | agenda, llamado, secuencia operativa de atención | Alta |
| Personal de archivo | vinculación de registro/documentación | Media/Alta |
| Personal de triaje | cuándo aplica y cómo se integra al flujo | Media/Alta |
| Responsable de consulta externa | proceso integral, políticas y métricas | Alta |
| Personal TI | sistemas actuales, restricciones, integración, infraestructura | Alta |
| Administrador / dirección | objetivos, reglas, indicadores, restricciones | Alta |
| Responsable de calidad | tiempos, incidencias, indicadores, Plan Cero Colas | Alta |
| Seguridad/privacidad, si existe | permisos, protección de datos, auditoría | Media |
| Auditor | trazabilidad requerida | Media |

La disponibilidad real de estos actores debe confirmarse.

---

# 7. Guion general de entrevista

## 7.1 Preguntas de apertura

1. ¿Cuál es su función dentro del proceso de consulta externa?
2. ¿En qué momento participa dentro del proceso de una cita?
3. ¿Qué actividades realiza normalmente?
4. ¿Con qué otras áreas o personas se coordina?
5. ¿Qué herramientas utiliza?

---

# 8. Preguntas sobre gestión de citas

## 8.1 Solicitud y programación

1. ¿Qué canales existen actualmente para solicitar una cita?
2. ¿Todos los pacientes utilizan el mismo procedimiento?
3. ¿Existen diferencias entre paciente nuevo y continuador?
4. ¿Qué requisitos debe cumplir una persona antes de obtener una cita?
5. ¿Cómo se valida su identidad?
6. ¿Cómo intervienen SIS, referencias u otras condiciones administrativas?
7. ¿Quién puede crear o modificar una cita?
8. ¿Quién configura los horarios de atención?
9. ¿Quién determina cuántos cupos existen?
10. ¿Cómo se asignan los cupos por especialidad o profesional?
11. ¿Existen cupos reservados para determinados casos?
12. ¿Cómo se conoce actualmente la disponibilidad?
13. ¿Qué ocurre cuando no existe disponibilidad?

---

## 8.2 Cancelación y reprogramación

1. ¿Puede un paciente cancelar una cita?
2. ¿Por qué canales?
3. ¿Con cuánta anticipación?
4. ¿Qué ocurre con el cupo cancelado?
5. ¿Puede reutilizarse?
6. ¿Quién puede reprogramar una cita?
7. ¿Existen límites de reprogramación?
8. ¿Qué sucede si el médico o servicio cancela la atención?
9. ¿Cómo se informa al paciente?
10. ¿Existe un procedimiento especial para cambios de última hora?

---

## 8.3 Confirmación y recordatorios

1. ¿Actualmente se envían recordatorios?
2. ¿Por qué canal?
3. ¿Con cuánta anticipación?
4. ¿El paciente puede confirmar asistencia?
5. ¿Qué ocurre si no responde?
6. ¿Qué datos de contacto se consideran válidos?
7. ¿Existe autorización o consentimiento para enviar mensajes?
8. ¿Sería útil confirmar con varios días de anticipación?
9. ¿Qué ventana de tiempo sería operativamente adecuada?

La regla `T-7 días` se presenta como **propuesta académica inicial** y no como una regla institucional.

---

## 8.4 No-show / inasistencia

1. ¿Cómo se define actualmente una inasistencia?
2. ¿Cuándo una cita pasa a considerarse no-show?
3. ¿Quién registra la inasistencia?
4. ¿Se mide actualmente?
5. ¿Existen estadísticas agregadas?
6. ¿Qué ocurre con el paciente después de una inasistencia?
7. ¿Se aplican restricciones o requiere nueva programación?
8. ¿Cuáles son las causas más frecuentes según la experiencia del personal?
9. ¿Qué capacidad queda sin utilizar cuando ocurre una inasistencia?

No solicitar nombres ni historiales individuales.

---

# 9. Preguntas sobre lista de espera y recuperación de cupos

1. ¿Existe actualmente una lista de espera?
2. Si existe, ¿cómo funciona?
3. ¿Quién ingresa a una persona?
4. ¿Qué información se registra?
5. ¿Cómo se decide quién recibe un cupo liberado?
6. ¿Se contacta a una sola persona o a varias?
7. ¿Cuánto tiempo puede esperar una persona para aceptar?
8. ¿Qué ocurre si rechaza?
9. ¿Qué ocurre si no responde?
10. ¿Puede el paciente indicar días o turnos disponibles?
11. ¿Qué requisitos deben coincidir para poder ofrecerle el cupo?
12. ¿Existen criterios de prioridad?
13. ¿Cómo debería documentarse la aceptación?

Si no existe lista de espera, registrar el hallazgo y evaluar esta capacidad únicamente como propuesta TO-BE.

---

# 10. Preguntas sobre priorización ambulatoria

1. ¿Existe alguna priorización para obtener citas de consulta externa?
2. ¿Quién puede solicitarla?
3. ¿Quién puede aprobarla?
4. ¿Qué criterios se utilizan?
5. ¿Se requiere referencia o documento?
6. ¿Existen categorías o niveles?
7. ¿Cómo afecta la prioridad a la agenda?
8. ¿Existen cupos específicos?
9. ¿Influye en una lista de espera?
10. ¿Cómo se registra la decisión?
11. ¿Qué ocurre si el caso debería dirigirse a emergencia?

Reglas:

- no asumir que prioridades de emergencia son reutilizables;
- no diseñar categorías clínicas sin validación;
- el paciente no puede autoaprobar una prioridad.

---

# 11. Preguntas sobre llegada y admisión

1. ¿Dónde debe presentarse primero el paciente?
2. ¿Cómo se verifica que tiene una cita?
3. ¿Qué datos se comprueban?
4. ¿Se entrega número, ticket o turno?
5. ¿Cómo se registra la llegada?
6. ¿Qué ocurre si llega temprano?
7. ¿Qué ocurre si llega tarde?
8. ¿Qué ocurre si llega sin cita?
9. ¿Qué documentos necesita?
10. ¿Cómo se relaciona admisión con archivo?
11. ¿Qué sucede si existe un problema administrativo?
12. ¿Cómo se comunica al siguiente punto del proceso que el paciente ya llegó?

---

# 12. Preguntas sobre archivo

1. ¿Qué función cumple archivo dentro de consulta externa?
2. ¿En qué momento interviene?
3. ¿Cómo recibe información de la cita?
4. ¿Qué información necesita?
5. ¿Cómo comunica que el registro está disponible?
6. ¿Qué ocurre si existe una incidencia?
7. ¿Qué parte podría representarse solamente como estado operativo en el sistema?

El proyecto no pretende sustituir una historia clínica institucional.

---

# 13. Preguntas sobre triaje

1. ¿Todas las especialidades pasan por triaje?
2. ¿Cuáles sí?
3. ¿En qué momento del proceso ocurre?
4. ¿Quién determina que debe realizarse?
5. ¿Cómo se comunica que el paciente terminó esta etapa?
6. ¿Existe una cola específica?
7. ¿Qué información es estrictamente clínica?
8. ¿Sería suficiente que el sistema académico registre solo el estado `TRIAGE_COMPLETED`?

No recopilar signos vitales ni datos clínicos para el MVP sin autorización y ampliación formal del alcance.

---

# 14. Preguntas sobre sala de espera y llamado

1. ¿Cómo se determina el orden de atención?
2. ¿Cómo sabe el paciente cuándo será llamado?
3. ¿Cómo realiza el profesional el llamado?
4. ¿Qué sucede si el paciente no responde?
5. ¿Puede volver a ser llamado?
6. ¿Existen prioridades que alteren el orden?
7. ¿Cómo se registran retrasos?
8. ¿Cómo sabe el personal cuántos pacientes están esperando?
9. ¿Se registra el inicio de la consulta?
10. ¿Se registra el fin?

---

# 15. Preguntas sobre finalización

1. ¿Qué ocurre cuando termina la consulta?
2. ¿Se debe registrar cierre administrativo?
3. ¿Cómo se programa una cita de control?
4. ¿Qué pasa si el paciente debe ir a otro servicio?
5. ¿Se dirige a farmacia, laboratorio o imágenes?
6. ¿Qué información necesita conservar la gestión de citas?
7. ¿Qué eventos deberían marcar el final del flujo digital?

---

# 16. Preguntas sobre especialidades, servicios y agenda

1. ¿Cuál es la cartera de especialidades vigente?
2. ¿Qué servicios pueden reservarse?
3. ¿Todos los servicios tienen cita?
4. ¿Qué profesionales atienden cada servicio?
5. ¿Cuántos consultorios están activos?
6. ¿Cómo se asigna un consultorio?
7. ¿Qué duración tiene normalmente una cita?
8. ¿La duración cambia por especialidad?
9. ¿Existen turnos?
10. ¿Existen bloqueos de agenda?
11. ¿Cómo se manejan feriados, vacaciones o ausencias?
12. ¿Quién puede modificar una agenda?

Los valores definitivos deben provenir de evidencia institucional.

---

# 17. Preguntas sobre sistemas actuales

1. ¿Qué sistemas informáticos se utilizan actualmente?
2. ¿Existe un sistema para citas?
3. ¿Existe integración con Minsa Digital?
4. ¿Se utilizan hojas de cálculo?
5. ¿Se utilizan documentos físicos?
6. ¿Existen módulos independientes?
7. ¿Qué tareas requieren duplicar información?
8. ¿Qué sistemas no se comunican entre sí?
9. ¿Qué problemas técnicos son frecuentes?
10. ¿Existen restricciones para integrar sistemas externos?
11. ¿Existe conectividad estable?
12. ¿Qué dispositivos utiliza el personal?

No solicitar acceso técnico no autorizado.

---

# 18. Preguntas de seguridad y privacidad

1. ¿Qué roles pueden consultar citas?
2. ¿Quién puede crear o modificar citas?
3. ¿Quién puede ver información de otro paciente?
4. ¿Qué acciones necesitan auditoría?
5. ¿Existen políticas sobre contraseñas?
6. ¿Cómo se gestionan cuentas del personal?
7. ¿Qué información puede mostrarse en notificaciones?
8. ¿Qué información no debe aparecer?
9. ¿Existen reglas de retención?
10. ¿Quién puede consultar reportes?
11. ¿Cómo se gestionan bajas o cambios de personal?
12. ¿Existen políticas institucionales de protección de datos aplicables?

---

# 19. Preguntas de accesibilidad y usabilidad

No es necesario identificar condiciones médicas individuales para ofrecer características accesibles.

Preguntar:

1. ¿Qué dispositivos utilizan con mayor frecuencia los pacientes?
2. ¿Existen pacientes que requieren ayuda para usar servicios digitales?
3. ¿Qué dificultades presentan los formularios actuales?
4. ¿Qué información suele generar confusión?
5. ¿Qué colores se utilizan para representar estados?
6. ¿Es necesario texto grande o alto contraste?
7. ¿Debe existir soporte para navegación por teclado?
8. ¿Qué información debe ser especialmente clara para adultos mayores?
9. ¿Qué acciones deberían poder completarse con pocos pasos?
10. ¿Qué errores son más críticos y deberían prevenirse?

---

# 20. Preguntas sobre información pública del hospital

1. ¿Qué información institucional puede publicarse?
2. ¿Quién es responsable de validarla?
3. ¿Qué especialidades pueden mostrarse?
4. ¿Qué horarios son públicos?
5. ¿Qué requisitos para citas pueden publicarse?
6. ¿Qué canales de contacto son oficiales?
7. ¿Qué políticas deben estar visibles?
8. ¿Existen imágenes o videos autorizados?
9. ¿Qué avisos requieren actualización frecuente?
10. ¿Quién debería tener permisos para modificar contenido?

---

# 21. Información cuantitativa a solicitar

Únicamente de forma **agregada y autorizada**.

## Citas

- número de citas por día/semana/mes;
- citas por especialidad;
- citas confirmadas;
- cancelaciones;
- reprogramaciones;
- inasistencias;
- cupos utilizados;
- cupos no utilizados.

## Flujo

- tiempo promedio de admisión;
- tiempo promedio de espera;
- tiempo hasta llamado;
- tiempo total de permanencia, si existe;
- número de pacientes atendidos por período.

## Agenda

- capacidad por especialidad;
- turnos;
- duración típica;
- porcentaje de ocupación, si existe.

## Calidad

- incidencias;
- reclamos agregados;
- satisfacción agregada, si existe.

---

# 22. Checklist de observación

Durante una observación autorizada registrar:

```text
[ ] Punto de inicio del proceso
[ ] Actor que atiende primero
[ ] Documento o dato solicitado
[ ] Herramienta utilizada
[ ] Tiempo de inicio
[ ] Tiempo de término
[ ] Cambio de área/responsable
[ ] Esperas
[ ] Repetición de información
[ ] Documento físico generado
[ ] Sistema informático utilizado
[ ] Excepción observada
[ ] Forma de llamado
[ ] Forma de cierre
```

No registrar:

```text
[ ] Nombre del paciente
[ ] DNI
[ ] Historia clínica
[ ] Diagnóstico
[ ] Medicación
[ ] Fotografías de pantallas sensibles
[ ] Conversaciones clínicas
```

---

# 23. Plantilla para registrar una entrevista

```text
Código de entrevista:
Fecha:
Actor/rol:
Área:
Duración aproximada:
Entrevistador(es):
Autorización/consentimiento según indicación docente:

Objetivo:
-

Hallazgos:
1.
2.
3.

Reglas mencionadas:
-
-

Problemas percibidos:
-
-

Necesidades:
-
-

Excepciones:
-
-

Métricas mencionadas:
-
-

Sistemas/herramientas:
-
-

Información que requiere validación adicional:
-
-

Fuente/documento relacionado:
-
```

No es obligatorio registrar nombre personal del entrevistado si no es necesario.

---

# 24. Registro de hallazgos

Cada hallazgo debe clasificarse.

| Tipo | Ejemplo |
| --- | --- |
| `FACT` | regla confirmada por documento o responsable |
| `OBSERVATION` | comportamiento observado |
| `USER_NEED` | necesidad expresada por usuario |
| `PAIN_POINT` | problema percibido |
| `BUSINESS_RULE` | regla del proceso |
| `CONSTRAINT` | restricción |
| `METRIC` | indicador existente |
| `HYPOTHESIS` | supuesto pendiente |
| `OPPORTUNITY` | oportunidad de mejora |

Ejemplo:

```text
ID: F-001
Tipo: USER_NEED
Actor: Admisión
Hallazgo: ...
Fuente: entrevista E-02
Estado: pendiente de validación
```

---

# 25. Conversión de hallazgos a requerimientos

Flujo:

```text
Evidencia / entrevista / observación
              ↓
           Hallazgo
              ↓
          Necesidad
              ↓
        Regla de negocio
              ↓
         Requerimiento
              ↓
    Criterio de aceptación
              ↓
          Caso de prueba
```

Ejemplo conceptual:

```text
Hallazgo:
Los pacientes pueden cancelar una cita.

        ↓

Regla:
Solo una cita vigente puede cancelarse.

        ↓

RF:
El sistema debe permitir al paciente cancelar una cita elegible.

        ↓

Criterio:
Al cancelar, la cita cambia a CANCELLED y el slot queda disponible
según la política institucional.

        ↓

Test:
Verificar cancelación + liberación del slot.
```

La regla del ejemplo sigue siendo propuesta hasta ser validada.

---

# 26. Formato de requerimientos funcionales

Los RF posteriores utilizarán identificadores estables:

```text
RF-001
RF-002
RF-003
...
```

Plantilla:

```text
ID:
Nombre:
Descripción:
Actor:
Precondiciones:
Flujo principal:
Flujos alternos:
Reglas relacionadas:
Prioridad:
Fuente:
Criterios de aceptación:
Estado:
```

---

# 27. Formato de requerimientos no funcionales

```text
RNF-001
RNF-002
...
```

Plantilla:

```text
ID:
Categoría:
Descripción:
Métrica / criterio verificable:
Fuente:
Prioridad:
Método de validación:
Estado:
```

Categorías candidatas:

- seguridad;
- privacidad;
- rendimiento;
- disponibilidad;
- accesibilidad;
- usabilidad;
- mantenibilidad;
- observabilidad;
- compatibilidad;
- portabilidad;
- confiabilidad.

---

# 28. Priorización

Se utilizará **MoSCoW** como técnica inicial.

| Categoría | Significado |
| --- | --- |
| **Must** | obligatorio para que el MVP cumpla su propósito |
| **Should** | importante, pero puede existir alternativa temporal |
| **Could** | aporta valor, pero puede diferirse |
| **Won't now** | explícitamente fuera de esta versión |

La clasificación debe realizarse después del levantamiento, no únicamente por preferencia técnica del equipo.

---

# 29. Criterios para aceptar un requerimiento

Un requerimiento podrá considerarse aceptado cuando:

- tiene fuente identificada;
- responde a una necesidad real;
- está redactado de forma clara;
- no contradice otra regla aprobada;
- pertenece al alcance;
- puede probarse;
- respeta privacidad y seguridad;
- tiene prioridad;
- posee criterios de aceptación;
- fue revisado por el equipo y, cuando corresponda, por el actor responsable o docente.

---

# 30. Manejo de contradicciones

Si dos actores describen reglas distintas:

```text
Contradicción detectada
        ↓
Registrar ambas versiones
        ↓
Buscar documento institucional
        ↓
Consultar responsable del proceso
        ↓
Resolver / marcar pendiente
        ↓
No implementar hasta definir regla
```

No se seleccionará una versión únicamente porque sea técnicamente más conveniente.

---

# 31. Matriz de trazabilidad inicial

El levantamiento debe permitir construir posteriormente una matriz como:

| Requerimiento | Fuente | Actor | Regla | Prototipo | API | Caso de prueba |
| --- | --- | --- | --- | --- | --- | --- |
| RF-001 | E-01 | Paciente | RN-01 | P-01 | API-01 | TC-001 |
| RF-002 | MAPRO | Admisión | RN-03 | P-04 | API-07 | TC-010 |

Esto permitirá demostrar la relación:

```text
INVESTIGACIÓN
      ↓
REQUERIMIENTO
      ↓
DISEÑO
      ↓
IMPLEMENTACIÓN
      ↓
PRUEBA
```

---

# 32. Evidencia a conservar

El repositorio podrá conservar, cuando no contenga información sensible:

- cuestionarios vacíos;
- guías de entrevista;
- matrices anonimizadas;
- notas consolidadas;
- BPMN;
- requerimientos;
- decisiones;
- fotografías únicamente de espacios/procesos autorizados sin datos personales;
- referencias a documentos institucionales.

No subir al repositorio:

- bases reales de pacientes;
- DNI;
- historias clínicas;
- credenciales;
- información clínica;
- documentos internos no autorizados.

---

# 33. Riesgos del levantamiento

| Riesgo | Impacto | Mitigación |
| --- | --- | --- |
| No acceder al personal necesario | Alto | usar fuentes institucionales y declarar limitación |
| Respuestas contradictorias | Medio/Alto | triangulación |
| Confundir percepción con regla | Alto | clasificar fuente y validar |
| Recopilar datos sensibles | Alto | minimización y anonimización |
| Preguntas demasiado técnicas | Medio | adaptar lenguaje al actor |
| Sesgo de confirmación | Alto | preguntas abiertas y neutrales |
| Sobreextender el alcance | Alto | contrastar con MVP |
| No obtener métricas | Medio | utilizar evaluación académica sintética y declarar limitación |
| Cambios posteriores de proceso | Medio | versionar y fechar hallazgos |

---

# 34. Salidas esperadas

Al finalizar el levantamiento deberán existir:

1. mapa de stakeholders validado;
2. evidencia institucional consolidada;
3. proceso AS-IS;
4. necesidades por actor;
5. pain points;
6. reglas de negocio;
7. restricciones;
8. indicadores de línea base disponibles;
9. RF;
10. RNF;
11. priorización MoSCoW;
12. alcance MVP ajustado;
13. lista de elementos fuera de alcance;
14. insumos para BPMN TO-BE;
15. insumos para prototipos;
16. insumos para PRD.

---

# 35. Gate para cerrar el levantamiento

Antes de considerar finalizada esta etapa:

```text
[ ] Stakeholders principales identificados.
[ ] MAPRO revisado.
[ ] Cartera de Servicios revisada.
[ ] Canales de citas identificados.
[ ] Proceso AS-IS documentado.
[ ] Agenda/disponibilidad comprendida.
[ ] Cancelación/reprogramación comprendidas.
[ ] No-show investigado.
[ ] Lista de espera validada como existente o inexistente.
[ ] Priorización validada como existente o inexistente.
[ ] Flujo de llegada/admisión identificado.
[ ] Triaje identificado cuando aplique.
[ ] Restricciones técnicas identificadas.
[ ] Requisitos iniciales trazables.
[ ] RNF iniciales trazables.
[ ] Datos sensibles excluidos.
[ ] Hallazgos contradictorios resueltos o marcados pendientes.
[ ] Resultados revisados con el equipo.
[ ] Validación docente realizada cuando corresponda.
```

---

# 36. Relación con documentos del proyecto

Entradas:

```text
docs/00-PROJECT-ROADMAP.md
docs/planning/lean-canvas.md
docs/planning/project-charter.md
docs/planning/wbs.md
docs/planning/gantt.md
docs/research/semana-01/fuentes.md
docs/research/semana-01/marco-teorico.md
docs/research/semana-01/problema-investigacion.md
```

Salidas principales:

```text
docs/01-PRD.md
docs/03-APP-FLOW.md
docs/04-UI-UX-DESIGN-BRIEF.md
```

El levantamiento también alimentará:

```text
docs/02-TRD.md
docs/05-BACKEND-SCHEMA.md
docs/07-SECURITY-THREAT-MODEL.md
docs/08-TEST-STRATEGY.md
```

---

# 37. Flujo de trabajo

```text
Preparar instrumentos
        ↓
Revisión docente
        ↓
Entrevistas / observación / documentos
        ↓
Registrar hallazgos
        ↓
Triangular
        ↓
Validar
        ↓
RF + RNF + reglas
        ↓
Priorizar MoSCoW
        ↓
Actualizar alcance
        ↓
PRD
        ↓
BPMN / App Flow
        ↓
Prototipos
```

---

# 38. Estado del documento

- [x] Técnicas de levantamiento definidas.
- [x] Stakeholders candidatos identificados.
- [x] Guías de entrevista definidas.
- [x] Checklist de observación definido.
- [x] Formato de registro de hallazgos definido.
- [x] Plantilla RF definida.
- [x] Plantilla RNF definida.
- [x] Estrategia MoSCoW definida.
- [x] Reglas de privacidad definidas.
- [x] Gate de cierre definido.
- [ ] Instrumento revisado por el equipo.
- [ ] Instrumento validado por el docente.
- [ ] Levantamiento ejecutado.
- [ ] Hallazgos consolidados.
- [ ] RF/RNF finales elaborados.
