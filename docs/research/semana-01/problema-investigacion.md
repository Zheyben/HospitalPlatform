# Problema de investigación — Semana 01

## Estado de formulación

Esta formulación corresponde a la Semana 01 del curso **Curso Integrador I: Sistemas Software** y ha sido actualizada para reflejar la reorientación del proyecto hacia el **Hospital de Huaycán — MINSA / DIRIS Lima Este**.

El dominio hospitalario y la línea de trabajo centrada en la gestión de citas médicas se mantienen. El Hospital de Huaycán se considera actualmente el **caso de estudio propuesto**, pendiente de validación final con el docente.

La formulación continúa siendo académicamente preliminar porque el diagnóstico institucional detallado permanece **PENDIENTE DE LEVANTAMIENTO EN CAMPO**. Todavía deben validarse los procesos reales, actores, canales de atención, especialidades, disponibilidad, tiempos, volúmenes, incidencias, cancelaciones, reprogramaciones, inasistencias, mecanismos de recordatorio, reutilización de cupos, criterios de priorización ambulatoria, restricciones tecnológicas e indicadores de línea base.

No se asumirán como hechos problemas, tiempos, porcentajes o flujos que no hayan sido respaldados por fuentes institucionales o por levantamiento autorizado.

---

## Título tentativo

**Propuesta de plataforma web y móvil para la gestión de citas médicas y la trazabilidad del flujo de consulta externa del Hospital de Huaycán — MINSA.**

El título podrá ajustarse después del levantamiento institucional y de la validación definitiva del alcance con el docente.

---

## Distinción académica obligatoria

### Caso de estudio propuesto

El **Hospital de Huaycán — MINSA / DIRIS Lima Este** es el nuevo caso de estudio propuesto para el proyecto académico.

La reorientación considera como contexto la situación actual del hospital, sus procesos de consulta externa y el proyecto de nueva infraestructura hospitalaria. Esto no implica que la plataforma propuesta vaya a ser adoptada oficialmente por el Hospital de Huaycán, MINSA, DIRIS Lima Este o PRONIS.

### Diagnóstico institucional pendiente

La problemática institucional específica, sus indicadores, estadísticas, procesos, herramientas y magnitud todavía deben investigarse mediante fuentes oficiales y levantamiento de información autorizado.

No se inventarán:

- tiempos de espera;
- volumen diario o mensual de pacientes;
- porcentaje de inasistencias;
- cantidad de cancelaciones o reprogramaciones;
- número de cupos desaprovechados;
- causas específicas de colas;
- herramientas informáticas utilizadas;
- secuencia exacta de admisión, archivo, triaje y consulta;
- criterios actuales de priorización de citas;
- funcionamiento de una lista de espera;
- métricas de satisfacción;
- restricciones internas no documentadas.

---

## Idea de investigación

Diseñar, desarrollar, implementar y evaluar una **plataforma web y móvil** orientada a apoyar la gestión de citas médicas y la trazabilidad del flujo de consulta externa del Hospital de Huaycán.

La propuesta plantea una solución compuesta por:

- portal público institucional;
- portal web especializado de reservas;
- web operativa para el personal;
- aplicación móvil para el paciente.

La solución estudiará capacidades como:

- consulta de especialidades y disponibilidad;
- programación de citas;
- confirmación anticipada de asistencia;
- cancelación;
- reprogramación;
- recordatorios;
- recuperación de cupos liberados;
- lista de espera;
- ofertas de cupos disponibles;
- priorización ambulatoria validada por personal autorizado;
- check-in;
- seguimiento de etapas operativas de consulta externa;
- gestión de cola/sala de espera;
- auditoría;
- medición de tiempos e indicadores.

La aplicación móvil forma parte del **MVP académico** y no se considera una extensión opcional futura.

---

## Situación problemática

La literatura revisada durante la Semana 01 muestra que la gestión de citas médicas puede presentar dificultades relacionadas con tiempos de espera, acceso a información, organización de agendas, carga administrativa, inasistencias, trazabilidad, usabilidad, privacidad y satisfacción de los usuarios.

Los antecedentes nacionales revisados reportan mejoras en indicadores de gestión después de incorporar sistemas web para citas médicas, mientras que los antecedentes internacionales muestran que los sistemas electrónicos de reserva pueden contribuir a reducir tiempos de espera y mejorar la organización del servicio, aunque su efectividad depende también de factores como usabilidad, privacidad, integración, soporte técnico y gestión del cambio.

En el caso del Hospital de Huaycán, existen fuentes institucionales que justifican estudiar la gestión de consulta externa y los tiempos de atención como un contexto relevante. El hospital cuenta con documentación formal relacionada con Consulta Externa, Selección, Admisión y Archivo, así como con iniciativas institucionales orientadas a mejorar la atención y reducir colas. Además, el proyecto de nueva infraestructura hospitalaria constituye un escenario de referencia para analizar cómo podría organizarse digitalmente una operación ambulatoria con mayor capacidad.

Sin embargo, estas fuentes **no permiten afirmar automáticamente** que el Hospital de Huaycán tenga una determinada tasa de no-show, un problema específico de doble reserva, una lista de espera inexistente, un flujo exacto de triaje o una pérdida concreta de cupos.

Por ello, el proyecto debe investigar el proceso **AS-IS** y establecer una línea base antes de cerrar los requerimientos funcionales, reglas de negocio, indicadores y métricas de evaluación.

Dentro de este contexto, una oportunidad relevante de diseño es estudiar mecanismos que permitan:

- recordar una cita con anticipación;
- solicitar confirmación de asistencia;
- permitir cancelación o reprogramación oportuna;
- liberar el cupo cuando el paciente indique que no podrá asistir;
- ofrecer el cupo liberado a otro paciente elegible mediante una lista de espera;
- evitar que un mismo cupo sea asignado simultáneamente a más de una persona;
- registrar y medir inasistencias reales;
- mejorar la visibilidad del estado de la cita y del flujo ambulatorio;
- permitir una revisión auditable de solicitudes de prioridad, sin sustituir el triaje de emergencia.

La propuesta no plantea eliminar las colas o las inasistencias al 100 %. Su propósito es **contribuir a una mejor utilización de la agenda, reducir inasistencias evitables y mejorar la trazabilidad del proceso**, siempre que los indicadores obtenidos durante el levantamiento permitan demostrarlo.

---

## Vacío de investigación

Los antecedentes revisados demuestran que los sistemas web y electrónicos pueden mejorar determinados indicadores de gestión de citas en contextos específicos. Sin embargo, no prueban automáticamente cuál sería el efecto de una solución de estas características en el Hospital de Huaycán ni determinan qué funcionalidades se ajustan a sus procesos reales.

El vacío de investigación se ubica en determinar **de qué manera una plataforma web y móvil, diseñada a partir del proceso real del Hospital de Huaycán y complementada con mecanismos de confirmación anticipada, cancelación, reprogramación, recuperación de cupos, lista de espera, priorización validada y trazabilidad del flujo de consulta externa, puede contribuir a mejorar la gestión de citas médicas**.

La investigación deberá relacionar la propuesta tecnológica con indicadores obtenidos o definidos a partir del levantamiento institucional, evitando trasladar sin validación los resultados de otros hospitales o establecimientos de salud.

---

## Variables de investigación

### Variable independiente

**Plataforma web y móvil para la gestión de citas médicas y la trazabilidad del flujo de consulta externa.**

Dimensiones candidatas, sujetas a validación metodológica:

- gestión de disponibilidad y reserva;
- confirmación, cancelación y reprogramación;
- notificaciones y recordatorios;
- recuperación de cupos y lista de espera;
- priorización ambulatoria validada;
- trazabilidad del flujo;
- accesibilidad y usabilidad;
- seguridad y privacidad.

### Variable dependiente

**Gestión de citas médicas y flujo de consulta externa.**

Dimensiones candidatas, sujetas al levantamiento:

- tiempo de gestión de la cita;
- utilización de cupos;
- confirmación de asistencia;
- cancelación anticipada;
- reprogramación;
- no-show;
- recuperación y reasignación de cupos;
- tiempos de espera por etapa;
- trazabilidad del recorrido ambulatorio;
- satisfacción/usabilidad;
- incidencias operativas.

Las dimensiones e indicadores definitivos se cerrarán después de revisar la metodología del curso y contar con una línea base institucional o un escenario de evaluación académica válido.

---

## Problema general

**¿De qué manera la implementación de una plataforma web y móvil puede apoyar la gestión de citas médicas, reducir el desaprovechamiento de cupos por inasistencias evitables y mejorar la trazabilidad del flujo de consulta externa del Hospital de Huaycán — MINSA, considerando sus procesos actuales y el escenario de su futura infraestructura hospitalaria?**

Estado: **PENDIENTE DE VALIDACIÓN DOCENTE Y LEVANTAMIENTO EN CAMPO**.

---

## Problemas específicos propuestos

### PE1 — Proceso actual

¿Cómo se desarrolla actualmente la gestión de citas y el flujo de consulta externa del Hospital de Huaycán, y qué actores, actividades, reglas, restricciones, tiempos e indicadores deben considerarse para establecer una línea base?

### PE2 — Diseño digital

¿Qué requerimientos funcionales, no funcionales, de accesibilidad, seguridad y experiencia de usuario debe cumplir una plataforma web y móvil para representar de manera adecuada el proceso de citas y consulta externa?

### PE3 — Gestión de citas y disponibilidad

¿Cómo implementar la gestión de especialidades, profesionales, agendas, disponibilidad y citas evitando conflictos como la doble reserva?

### PE4 — Confirmación y recuperación de cupos

¿Cómo pueden los recordatorios, la confirmación anticipada, la cancelación, la reprogramación y una lista de espera contribuir a recuperar y reasignar cupos que de otro modo podrían quedar desaprovechados?

### PE5 — Priorización ambulatoria

¿Cómo incorporar un proceso de solicitud y validación de prioridad para citas ambulatorias que sea auditable, dependa de personal autorizado y no sustituya el triaje de emergencia?

### PE6 — Evaluación

¿En qué medida la plataforma puede mejorar indicadores de gestión, trazabilidad, aprovechamiento de agenda, accesibilidad, usabilidad y tiempos del flujo ambulatorio definidos a partir del levantamiento institucional?

---

## Objetivo general

**Diseñar, desarrollar, implementar y evaluar una plataforma web y móvil orientada a apoyar la gestión de citas médicas y la trazabilidad del flujo de consulta externa del Hospital de Huaycán — MINSA, incorporando un portal público institucional, un portal especializado de reservas, confirmación anticipada de asistencia, cancelación, reprogramación, recuperación de cupos liberados, gestión de lista de espera y priorización de citas validada por personal autorizado, además de criterios de seguridad, privacidad, accesibilidad, usabilidad, consistencia transaccional, observabilidad e interoperabilidad.**

---

## Objetivos específicos

### OE1 — Levantamiento y modelado

Analizar y modelar el proceso actual de consulta externa y gestión de citas del Hospital de Huaycán mediante fuentes institucionales y levantamiento autorizado, identificando actores, actividades, canales, reglas, especialidades, tiempos, incidencias, restricciones e indicadores de línea base.

### OE2 — Diseño del proceso digital

Definir el proceso TO-BE, los requerimientos funcionales y no funcionales, los flujos de experiencia de usuario accesibles, el modelo de datos y el contrato API necesarios para gestionar citas y trazabilidad ambulatoria de manera segura y medible.

### OE3 — Implementación del núcleo web y móvil

Implementar progresivamente los módulos de identidad, contenido institucional, catálogo de servicios, pacientes, profesionales, agendas, disponibilidad, citas, priorización validada, confirmaciones, lista de espera, notificaciones y flujo ambulatorio, proporcionando un portal público del hospital, un portal específico de reservas, una web operativa para el personal y una aplicación móvil para el paciente, con consistencia transaccional y prevención de conflictos como la doble reserva o la aceptación simultánea de un mismo cupo liberado.

### OE4 — Seguridad y privacidad

Aplicar mecanismos de autenticación, autorización por rol y por objeto, gestión segura de sesiones, validación, minimización de datos, auditoría, protección frente a abuso y controles alineados con buenas prácticas de seguridad para aplicaciones web y APIs.

### OE5 — Validación y aprovechamiento de agenda

Evaluar la solución mediante pruebas unitarias, integración, API, concurrencia, seguridad, end-to-end, usabilidad y rendimiento, complementadas con métricas como tasa de confirmación, no-show, cancelación anticipada, recuperación y reasignación de cupos, aceptación de ofertas de lista de espera, uso de prioridades validadas, ocupación efectiva de agenda y tiempos del flujo ambulatorio definidos a partir del levantamiento institucional.

### OE6 — Información institucional y acceso digital

Diseñar una experiencia pública web que concentre información verificable del hospital —ubicación, servicios, especialidades, políticas, canales de contacto y contenido multimedia autorizado— y conduzca de forma clara al portal de reservas, manteniendo separados el contenido institucional, la gestión de citas y las funciones internas del personal.

---

## Alcance inicial del análisis

El análisis de Semana 01 y de las siguientes fases considerará:

- gestión de citas de consulta externa programada;
- consulta de especialidades y servicios;
- disponibilidad de profesionales y horarios;
- reserva;
- confirmación anticipada;
- cancelación;
- reprogramación;
- recordatorios configurables;
- no-show;
- liberación de cupos;
- lista de espera;
- ofertas de cupos liberados;
- solicitud y validación de prioridad ambulatoria;
- check-in;
- admisión;
- triaje como estado operativo cuando corresponda;
- espera y llamado;
- finalización administrativa;
- accesibilidad web y móvil;
- seguridad y privacidad;
- auditoría;
- métricas de operación.

### Fuera del alcance inicial

No se considera dentro del MVP inicial:

- historia clínica electrónica completa;
- diagnóstico médico;
- prescripción electrónica;
- farmacia completa;
- laboratorio clínico completo;
- imágenes diagnósticas completas;
- hospitalización;
- emergencia;
- cirugía;
- gestión de equipos biomédicos.

El proyecto podrá estudiar estas capacidades como expansión futura únicamente después de cerrar el MVP y validar requisitos normativos, institucionales y de seguridad.

---

## Accesibilidad y experiencia de usuario

La solución debe considerar accesibilidad desde el diseño.

Como requisitos candidatos del MVP se contemplan:

- tema del sistema;
- tema claro;
- tema oscuro;
- alto contraste;
- modo de apoyo para protanopia;
- modo de apoyo para deuteranopia;
- modo de apoyo para tritanopia;
- estados comunicados mediante texto e iconos, no únicamente mediante color;
- navegación comprensible;
- controles y formularios accesibles;
- experiencia mobile-first para pacientes.

Estas capacidades forman parte de los requerimientos no funcionales y deberán detallarse posteriormente en el UI/UX Design Brief.

---

## Priorización ambulatoria

La plataforma podrá incluir una funcionalidad de solicitud de prioridad para una cita únicamente si durante el levantamiento se valida que existe una política institucional aplicable.

Principios:

- el paciente puede solicitar una revisión;
- el paciente no puede autoaprobar una prioridad;
- la revisión debe corresponder a personal autorizado;
- la decisión debe quedar auditada;
- no debe utilizarse una escala clínica inventada;
- no se reutilizarán automáticamente las prioridades de emergencia para consulta externa;
- una posible emergencia debe dirigirse al canal institucional de emergencia y no permanecer esperando una cita;
- la prioridad no puede desplazar arbitrariamente citas ya confirmadas.

---

## Confirmación, cancelación y lista de espera

Se estudiará como regla inicial un recordatorio **T-7 días** antes de la cita, configurable según la política institucional.

Flujo conceptual:

```text
Cita programada
      ↓
Recordatorio anticipado
      ↓
¿Podrá asistir?
   ┌──┴──┐
   │     │
  Sí     No
   │     │
Confirmar Cancelar / Reprogramar
         ↓
   Liberar cupo
         ↓
   Lista de espera
         ↓
   Ofrecer cupo
         ↓
   Aceptación explícita
         ↓
   Reasignación transaccional
```

La reasignación no será automática sin consentimiento.

---

## Elementos pendientes de levantamiento en campo

### Gestión de citas

- canales actuales para solicitar citas;
- diferencias entre paciente nuevo y continuador;
- reglas para SIS, referencia u otras condiciones administrativas;
- responsables de configurar agendas y cupos;
- especialidades disponibles;
- duración de citas;
- horarios;
- cancelaciones;
- reprogramaciones;
- inasistencias/no-show;
- reglas para considerar una cita como no-show;
- mecanismos de confirmación;
- mecanismos de recordatorio;
- posibilidad actual de liberar/reutilizar cupos;
- existencia de lista de espera;
- criterios para ofrecer un cupo liberado;
- volumen agregado de citas.

### Priorización

- existencia o no de priorización ambulatoria;
- personal autorizado para validar;
- criterios institucionales;
- documentos o evidencias requeridos;
- influencia de la prioridad en agenda/lista de espera;
- procedimiento cuando el caso corresponde a emergencia.

### Flujo de consulta externa

- orientación inicial;
- identificación;
- selección/admisión;
- archivo o vinculación del registro;
- check-in;
- triaje y especialidades en las que aplica;
- sala de espera;
- sistema de llamado;
- ingreso a consulta;
- finalización;
- programación de controles;
- derivación hacia otros servicios.

### Información institucional

- cartera vigente de especialidades y servicios;
- información pública autorizada;
- ubicación/contacto oficial;
- políticas;
- horarios públicos;
- material multimedia autorizado;
- responsable de validar cambios de contenido.

### Tecnología

- sistemas informáticos actuales;
- participación de Minsa Digital u otros sistemas;
- restricciones de interoperabilidad;
- disponibilidad de APIs;
- conectividad;
- dispositivos utilizados por personal y pacientes.

### Métricas

Solicitar únicamente información agregada y autorizada:

- citas por período;
- cancelaciones;
- reprogramaciones;
- no-show;
- tiempos promedio por etapa;
- demanda por especialidad;
- cupos disponibles/utilizados;
- cupos liberados/reasignados;
- incidencias operativas;
- indicadores utilizados por la institución.

---

## Criterios éticos y de privacidad

Durante la investigación y el desarrollo académico:

- se utilizarán datos sintéticos;
- no se recopilarán historias clínicas reales;
- no se utilizarán datos identificables de pacientes sin autorización;
- no se solicitarán diagnósticos, prescripciones o resultados clínicos para demostrar el MVP;
- se priorizará información institucional pública y estadísticas agregadas;
- se aplicará minimización de datos;
- cualquier futura expansión hacia información clínica requerirá una nueva evaluación de alcance, seguridad, normativa y permisos.

---

## Relación con el roadmap maestro

La formulación de este documento debe mantenerse alineada con:

```text
docs/00-PROJECT-ROADMAP.md
```

El roadmap es la **fuente maestra viva del proyecto**.

Cualquier cambio posterior en:

- caso de estudio;
- objetivo general;
- objetivos específicos;
- alcance web/móvil;
- lista de espera;
- priorización;
- flujo ambulatorio;
- seguridad;
- accesibilidad;
- integración;

deberá propagarse de manera controlada a este documento y a los entregables académicos relacionados.

---

## Estado para continuar

Antes de cerrar definitivamente el problema de investigación deben completarse, como mínimo:

- [ ] Hospital de Huaycán validado formalmente con el docente.
- [ ] MAPRO de Consulta Externa revisado.
- [ ] Cartera de Servicios vigente revisada.
- [ ] Levantamiento de campo o evidencia institucional equivalente completada.
- [ ] AS-IS validado.
- [ ] Indicadores de línea base definidos.
- [ ] Variables y dimensiones revisadas metodológicamente.
- [ ] Problema general validado.
- [ ] Objetivo general validado.
- [ ] Objetivos específicos validados.
