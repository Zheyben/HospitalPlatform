# CAPÍTULO I: ASPECTOS GENERALES

## 1. Definición del problema

La gestión de la atención ambulatoria en los establecimientos de salud requiere procesos organizados para administrar actividades relacionadas con la programación de citas, disponibilidad médica, confirmación de usuarios y seguimiento del recorrido del paciente durante las diferentes etapas del proceso asistencial.

En el Hospital de Huaycán - MINSA, considerado como caso de estudio académico, se plantea analizar cómo una plataforma web y móvil puede contribuir a optimizar la gestión de citas médicas y mejorar la trazabilidad del flujo de consulta externa.

La problemática no se limita únicamente al registro de una cita médica, sino a la necesidad de disponer de información organizada sobre las diferentes etapas del proceso asistencial, permitiendo identificar estados, registros e indicadores que faciliten el análisis y la mejora continua del flujo ambulatorio.

### 1.1 Descripción del problema

La presente investigación se orienta al Hospital de Huaycán - MINSA, recategorizado oficialmente como establecimiento de salud nivel II-1 (Dirección de Redes Integradas de Salud Lima Este 2026, párr. 1), como caso de estudio del proyecto académico. El trabajo se centra en la gestión de citas médicas y en la trazabilidad del flujo de consulta externa.

La documentación pública del establecimiento permite contextualizar el análisis del proceso ambulatorio y la necesidad de optimización de la atención; además, el Hospital de Huaycán se encuentra en una etapa de modernización respaldada por la adopción de su Plan “Cero Colas” 2026 (Hospital de Huaycán 2026, párr. 1) y un escenario de ampliación de infraestructura hospitalaria con expediente técnico aprobado (Programa Nacional de Inversiones en Salud 2026, párr. 1).

Por esta razón, el problema no se formula afirmando cifras, tasas de inasistencia, tiempos de espera, pérdidas de cupos o fallos concretos que no hayan sido medidos. En cambio, se plantea una necesidad de investigación y diseño alrededor de tres dimensiones que forman parte del alcance del proyecto: la gestión consistente de agendas y disponibilidad, el aprovechamiento de cupos ante cancelaciones o inasistencias susceptibles de gestión y la trazabilidad del recorrido operativo del paciente en consulta externa.

**Gestión de citas y disponibilidad:** Necesidad de representar de forma segura y consistente especialidades, profesionales, agendas, disponibilidad y reservas, evitando conflictos como la doble asignación de un mismo cupo.

**Desaprovechamiento de cupos por inasistencias susceptibles de gestión:** Necesidad de estudiar mecanismos de confirmación, recordatorios, cancelaciones y reprogramaciones oportunas, recuperación de cupos liberados y lista de espera. La magnitud real del comportamiento de inasistencias y cupos desaprovechados en Huaycán deberá determinarse mediante el levantamiento correspondiente.

**Trazabilidad del flujo de consulta externa:** Necesidad de disponer de registros e indicadores que permitan analizar las diferentes etapas del recorrido ambulatorio desde la cita hasta el cierre administrativo, considerando admisión, check-in, triaje cuando corresponda, espera, llamado y consulta según el proceso institucional que sea validado.

La propuesta tecnológica considera un portal público institucional, un portal web de reservas, una web operativa para el personal y una aplicación móvil como componente complementario para pacientes. Estas superficies serán diseñadas considerando reglas de negocio validadas, controles de seguridad y privacidad, auditoría y mecanismos orientados a prevenir conflictos de concurrencia como la doble reserva o la aceptación simultánea de un mismo cupo liberado.

### 1.2 Formulación del problema general

**Problema general:**

> ¿De qué manera una plataforma web y móvil puede contribuir a mejorar la gestión de citas médicas y la trazabilidad del flujo de consulta externa del Hospital de Huaycán - MINSA?

## 2. Definición de objetivos

### 2.1 Objetivo general

Implementar una plataforma web y móvil para optimizar la gestión de citas médicas y la trazabilidad del flujo de consulta externa del Hospital de Huaycán - MINSA.

### 2.2 Objetivos específicos

- **OE1 - Levantamiento y modelado.** Analizar y modelar el proceso actual de consulta externa y gestión de citas del Hospital de Huaycán mediante fuentes institucionales y levantamiento autorizado, identificando actores, actividades, canales, reglas, especialidades, tiempos, incidencias, restricciones e indicadores de línea base.
- **OE2 - Diseño del proceso digital.** Definir el proceso TO-BE, los requerimientos funcionales y no funcionales, los flujos de experiencia de usuario accesibles, el modelo de datos y el contrato API necesarios para gestionar citas y trazabilidad ambulatoria de manera segura y medible.
- **OE3 - Implementación del núcleo web y móvil.** Desarrollar e integrar los módulos de gestión de agendas, disponibilidad, reserva, confirmación, cancelación y reprogramación de citas, recuperación de cupos mediante lista de espera y registro de estados del flujo de consulta externa, mediante interfaces web para pacientes y personal autorizado y una aplicación móvil complementaria para pacientes.
- **OE4 - Seguridad y privacidad.** Aplicar autenticación, autorización por rol y por recurso, gestión segura de sesiones, validación, minimización de datos, auditoría, protección frente a abuso y controles de seguridad para aplicaciones web, móviles y APIs.
- **OE5 - Validación y aprovechamiento de agenda.** Evaluar la solución mediante pruebas unitarias, integración, API, concurrencia, seguridad, end-to-end, usabilidad y rendimiento, complementadas con métricas de confirmación, no-show, cancelación anticipada, recuperación y reasignación de cupos, ocupación efectiva de agenda y tiempos del flujo ambulatorio definidos a partir del levantamiento institucional.
- **OE6 - Información institucional y acceso digital.** Diseñar una experiencia pública web que concentre información verificable del hospital y conduzca de forma clara al portal de reservas, manteniendo separados el contenido institucional, la gestión de citas y las funciones internas del personal.

### 2.3 Alcances y limitaciones

#### Alcances

- **Consulta externa programada como dominio principal del MVP académico.** El proyecto se enfoca en el proceso de consulta externa programada, considerando la gestión de citas médicas y la trazabilidad del flujo ambulatorio como eje principal de la solución propuesta.
- **Portal público institucional.** La plataforma incluirá un portal público con información verificable del Hospital de Huaycán, servicios disponibles, información institucional y un acceso claro hacia el proceso de reserva de citas.
- **Portal web de reservas y aplicación móvil del paciente.** Los usuarios podrán consultar disponibilidad, revisar opciones de atención, realizar reservas, confirmar citas, cancelar o reprogramar según las reglas definidas para el MVP académico.
- **Web operativa para personal autorizado.** El sistema contemplará funcionalidades internas orientadas a la administración de especialidades, profesionales, agendas, disponibilidad, citas, lista de espera, priorización validada y estados del flujo ambulatorio.
- **Recuperación de cupos liberados mediante lista de espera.** La solución considerará mecanismos para gestionar cupos disponibles producto de cancelaciones o liberaciones, aplicando reglas de control para evitar asignaciones duplicadas o inconsistentes.
- **Trazabilidad del flujo de consulta externa.** El sistema permitirá representar y registrar etapas operativas del proceso ambulatorio como check-in, admisión, triaje cuando corresponda, espera, llamado, consulta y cierre administrativo, siempre sujeto a validación del proceso institucional real.
- **Seguridad, privacidad y calidad como requisitos transversales.** La plataforma incorporará controles de autenticación, autorización, auditoría, protección de información, accesibilidad, observabilidad y pruebas automatizadas como parte del diseño de la solución.
- **Uso de datos sintéticos.** El desarrollo, pruebas y demostraciones utilizarán datos sintéticos debido a que no se trabajará con información clínica o personal real.

#### Limitaciones

- **Levantamiento institucional pendiente.** El diagnóstico detallado del proceso actual, población, muestra, indicadores de línea base y reglas definitivas del flujo dependerán del levantamiento y validación metodológica correspondiente.
- **No se asumirán métricas sin evidencia institucional.** No se afirmarán tiempos de espera, porcentajes de inasistencia, cantidad de pacientes, pérdida de cupos o comportamiento operativo del hospital sin información validada.
- **Caso de estudio académico.** El proyecto no representa una solución oficial implementada por el Hospital de Huaycán, MINSA, DIRIS Lima Este o PRONIS, ni implica adopción institucional.
- **Exclusión de módulos clínicos integrales.** El MVP no contempla historia clínica electrónica completa, emergencia, hospitalización, cirugía, farmacia o laboratorio como sistemas clínicos integrales.
- **Protección de datos personales.** No se utilizarán historias clínicas reales ni datos personales sensibles sin autorización formal, evaluación ética y cumplimiento de condiciones regulatorias.
- **Interoperabilidad futura.** La integración con sistemas nacionales o institucionales será considerada como una capacidad futura mediante adaptadores, pero no se afirmará interoperabilidad real sin documentación, permisos y pruebas correspondientes.

### 2.4 Justificación

La justificación teórica de la presente investigación se fundamenta en el análisis de la relación existente entre la transformación digital y la optimización de los procesos sanitarios, considerando el aporte de los sistemas de información aplicados al sector salud. Para Turkosqui (2022, p. 1), los sistemas web orientados a la gestión de citas médicas permiten automatizar actividades administrativas y mejorar la disponibilidad de información para la atención de los usuarios. Asimismo, Mwanswila, Mollel y Mushi (2024, p. 16) señalan que la mejora del flujo de pacientes en consulta externa requiere considerar herramientas tecnológicas junto con estrategias de reorganización de los procesos asistenciales. En ese sentido, la investigación permitirá analizar cómo una plataforma web y móvil puede contribuir a mejorar la gestión de citas médicas y la trazabilidad del flujo de consulta externa del Hospital de Huaycán - MINSA.

La justificación tecnológica se presenta mediante la propuesta e implementación de una plataforma web y móvil orientada a gestionar información relacionada con citas médicas, disponibilidad, estados del proceso ambulatorio y mecanismos de seguridad. Esta solución considera buenas prácticas de desarrollo de software, control de acceso y protección de información. Según Revilla (2023, pp. 6-7), las soluciones digitales aplicadas a la gestión sanitaria deben considerar aspectos como funcionalidad, eficiencia y portabilidad para garantizar una adecuada experiencia de uso. Asimismo, Wang et al. (2024, p. 3) identifican que la adopción de sistemas electrónicos sanitarios depende de factores como la facilidad de uso, confianza del usuario y condiciones tecnológicas facilitadoras, elementos que serán considerados dentro del diseño de la plataforma propuesta.

La justificación social radica en la posibilidad de mejorar la experiencia de los pacientes mediante una solución tecnológica que facilite el acceso a la información de sus citas médicas, reduzca la incertidumbre durante el proceso de atención y permita una mayor organización del flujo de consulta externa. En relación con ello, Ostadmohammadi et al. (2025, p. 2) resaltan que la percepción y satisfacción de los usuarios, tanto pacientes como personal sanitario, influyen directamente en la aceptación y efectividad de los sistemas electrónicos de citas. Por ello, la plataforma propuesta busca aportar una alternativa digital que favorezca una atención más organizada y accesible para los usuarios del Hospital de Huaycán como caso de estudio académico.
