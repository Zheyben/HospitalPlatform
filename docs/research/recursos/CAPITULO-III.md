# CAPÍTULO III: DISEÑO DEL PROYECTOS

## 1. Análisis de Requerimientos (Funcionales y/o No Funcionales)

El análisis define las funciones y condiciones de calidad de HospitalPlatform para gestionar citas médicas y dar seguimiento a la consulta externa del Hospital de Huaycán como caso de estudio académico. Se toma como base el avance del equipo, los requisitos del repositorio y el esquema inicial de PostgreSQL.

El proceso descrito corresponde a la solución propuesta (TO-BE). Las reglas institucionales definitivas y la línea base deberán validarse mediante el levantamiento autorizado. Los requisitos expresan comportamiento esperado; su inclusión no significa que ya esté implementado o probado.

### 1.1. Requerimientos del Sistema

#### 1.1.1. Requerimientos Funcionales

Se conservan los identificadores RF-01 a RF-20 y se precisan sus condiciones. Se incorporan RF-21 a RF-24 para cubrir reasignación de cupos, auditoría, contenido público y gestión de sesiones previstos en el proyecto.

| ID | Módulo | Descripción del requerimiento |
| --- | --- | --- |
| RF-01 | Autenticación | Permitir registro del paciente e inicio de sesión con credenciales válidas; rechazar cuentas duplicadas y credenciales incorrectas. |
| RF-02 | Usuarios y permisos | Gestionar usuarios y roles PATIENT, ADMIN, PROFESSIONAL, RECEPTIONIST y TRIAGE. Validar permisos y propiedad del recurso; SYSTEM representa procesos internos. |
| RF-03 | Pacientes | Registrar y actualizar los datos mínimos del paciente, validar campos obligatorios y evitar duplicidad de documento de identidad. |
| RF-04 | Pacientes | Permitir al paciente consultar únicamente sus citas y estados; el personal autorizado consultará los registros necesarios para su función. |
| RF-05 | Profesionales | Gestionar profesionales, colegiatura y asociación con una o varias especialidades activas; impedir colegiaturas duplicadas. |
| RF-06 | Especialidades | Mostrar especialidades activas y permitir su administración. Una especialidad desactivada no se ofrecerá para nuevas reservas. |
| RF-07 | Agenda médica | Configurar horarios por profesional y especialidad, generar cupos por fecha y controlar horarios inválidos o incompatibles. |
| RF-08 | Disponibilidad | Consultar cupos por especialidad, profesional y fecha; excluir los reservados y bloqueados. |
| RF-09 | Citas | Registrar una reserva vinculada al paciente, profesional y cupo seleccionado, con identificador y estado administrativo inicial. |
| RF-10 | Consistencia | Revalidar y asignar el cupo en una transacción. Si dos solicitudes compiten por el mismo cupo, aceptar una sola reserva activa e informar el conflicto a la otra. |
| RF-11 | Confirmación | Mostrar la constancia de reserva y permitir confirmar asistencia, diferenciando la notificación de reserva del cambio a CONFIRMED. |
| RF-12 | Reprogramación | Reprogramar a un cupo disponible manteniendo trazabilidad entre cita anterior y nueva. Si falla la asignación, conservar la cita original. |
| RF-13 | Cancelación | Cancelar según permisos y reglas validadas, registrar usuario y fecha, conservar historial y habilitar la recuperación del cupo cuando corresponda. |
| RF-14 | Lista de espera | Registrar solicitudes sin disponibilidad por especialidad y, opcionalmente, profesional; permitir consultarlas y cancelarlas. |
| RF-15 | Priorización | Registrar solicitudes de prioridad ambulatoria y permitir a personal autorizado aprobarlas o rechazarlas con motivo y trazabilidad. No realizar diagnóstico ni clasificación automática de urgencias. |
| RF-16 | Notificaciones | Generar avisos de reserva, cambios y recordatorios por canales configurados; registrar fallos sin duplicar reservas. |
| RF-17 | Check-in | Permitir al personal autorizado validar la cita y registrar llegada; una cita cancelada no iniciará atención. |
| RF-18 | Flujo asistencial | Registrar CHECK_IN, WAITING, IN_ATTENTION y FINISHED con actor y fecha, aplicando transiciones válidas y separándolas del estado administrativo. |
| RF-19 | Administración | Consultar agendas, citas, disponibilidad y pacientes en espera según rol; el profesional consultará su agenda asignada. |
| RF-20 | Indicadores | Consultar indicadores de citas y agenda por periodo y, cuando corresponda, especialidad o profesional, con definiciones y datos trazables. |
| RF-21 | Reasignación | Identificar solicitudes elegibles de lista de espera al liberarse un cupo y permitir asignación validada, evitando adjudicarlo simultáneamente a dos pacientes. |
| RF-22 | Auditoría | Registrar cambios críticos con actor, acción, entidad y fecha, sin exponer contraseñas ni tokens. |
| RF-23 | Portal público | Presentar información institucional y especialidades con acceso claro a reservas; permitir al administrador mantener el contenido autorizado. |
| RF-24 | Sesiones | Renovar y cerrar sesiones de forma controlada mediante tokens de acceso y renovación, con expiración y revocación según la estrategia documentada. |


#### 1.1.2. Requerimientos No Funcionales

Las cifras siguientes son metas técnicas propuestas para evaluar el prototipo con datos sintéticos. No son mediciones del hospital ni compromisos de producción. Se registrarán equipo, red, volumen de datos, duración y versión evaluada.

| ID | Categoría | Requerimiento | Criterio de verificación |
| --- | --- | --- | --- |
| RNF-01 | Seguridad | Proteger credenciales mediante hash, usar HTTPS en el entorno publicado y validar rol y propiedad en el servidor. | Rechazar el 100 % de casos de acceso no autorizado definidos; no registrar contraseñas ni tokens. |
| RNF-02 | Privacidad | Utilizar datos sintéticos y mostrar solo los datos necesarios por rol. | Revisión de datos, respuestas y logs sin información personal real ni secretos. |
| RNF-03 | Rendimiento | Responder oportunamente en disponibilidad y reserva. | Meta propuesta: p95 ≤ 3 s por operación con 50 usuarios virtuales durante 10 min; registrar errores y configuración. |
| RNF-04 | Disponibilidad | Mantener accesible el servicio durante la evaluación y registrar interrupciones. | Meta propuesta: ≥ 99 % de sondeos de salud satisfactorios, uno por minuto durante 8 h de prueba. |
| RNF-05 | Usabilidad | Facilitar reserva sin asistencia y errores comprensibles. | Meta propuesta: ≥ 80 % de participantes completa una reserva sin ayuda; informar muestra y procedimiento. |
| RNF-06 | Accesibilidad | Permitir teclado, foco visible, etiquetas y mensajes que no dependan solo del color. | Completar el flujo principal por teclado y revisar formularios con lector de pantalla, sin bloqueos críticos. |
| RNF-07 | Compatibilidad | Conservar funciones principales en web y en el cliente móvil previsto con React Native y Expo. | Probar reserva y consulta en Chrome, Edge, Firefox y un entorno móvil; documentar versiones y dispositivo. |
| RNF-08 | Mantenibilidad | Organizar el backend por funcionalidad, separando controladores, servicios, repositorios y DTO. | Revisar dependencias, contratos y migraciones versionadas; ejecutar pruebas del componente modificado. |
| RNF-09 | Escalabilidad | Permitir crecimiento modular y evaluar carga sobre el núcleo compartido. | Comparar 10, 25 y 50 usuarios virtuales; registrar latencia, errores y recursos sin afirmar capacidad productiva. |
| RNF-10 | Trazabilidad | Conservar eventos críticos con actor, acción, entidad y fecha. | Reconstruir el 100 % de transiciones críticas ensayadas desde historial o auditoría. |
| RNF-11 | Integridad | Prevenir reservas activas duplicadas y cambios parciales mediante restricciones y transacciones. | 20 solicitudes simultáneas para un cupo generan una sola reserva activa; reprogramación fallida conserva cita original. |
| RNF-12 | Recuperación | Mantener respaldo y procedimiento de restauración del entorno académico. | Restaurar en base aislada y verificar conteos, relaciones y registros de muestra; documentar tiempos. |


#### 1.1.3. Correspondencia con el repositorio y estado del avance

La revisión es documental y estática: no se ejecutó la aplicación ni se validaron sus pruebas. Las dependencias o tablas existentes no acreditan por sí solas una funcionalidad terminada.

| Componente | Evidencia | Alcance actual |
| --- | --- | --- |
| Backend inicial | `apps/backend/pom.xml`; `HospitalApiApplication.java`; `application*.yaml` | Java 21 y Spring Boot 4.1.0; arranque y perfiles. No se encontraron controladores, servicios ni entidades de los módulos funcionales. |
| Persistencia | `database/migrations/V1__initial_schema.sql` | Script de tablas y restricciones para seguridad, personas, agendas, citas, espera, prioridad y auditoría. No se verificó ejecución ni descubrimiento automático por Flyway. |
| Pruebas iniciales | `src/test/java/com/integrador/salud/api/` | Pruebas de contexto y configuración PostgreSQL con Testcontainers; no acreditan flujos de negocio aprobados. |
| Web y móvil | `README.md` y `docs/02-TRD.md` | Next.js, React y TypeScript para web; React Native y Expo para móvil están previstos. Las aplicaciones no están implementadas. |
| Despliegue | `docker-compose.yml`; `infrastructure/docker/`; `.github/workflows/` | Los archivos examinados están vacíos; despliegue y automatización pendientes. |


Aspectos por armonizar al implementar: la arquitectura documental menciona Spring Boot 3.x, mientras el POM declara 4.1.0. El SQL define `slot_id` como único en todas las citas; reutilizar un cupo cancelado requiere resolver esa restricción preservando historial. El SQL y ADR-007 no incluyen `NO_SHOW` ni estados separados de admisión o triaje. Estas observaciones no implican cambios al código en este entregable.

#### 1.1.4. Matriz breve de trazabilidad

La matriz relaciona necesidades del proyecto, requisitos y verificaciones previstas. No representa pruebas ya ejecutadas.

| Necesidad | Requisito | Cómo se verificará |
| --- | --- | --- |
| Evitar doble reserva | RF-10 | Enviar dos solicitudes simultáneas al mismo cupo: solo una obtiene la reserva y la otra recibe un conflicto. |
| Recuperar cupos liberados | RF-21 | Liberar un cupo y reasignarlo a un paciente elegible de la lista de espera, sin doble asignación. |
| Seguir el recorrido del paciente | RF-18 | Recorrer las etapas permitidas y comprobar que cada transición conserva fecha y responsable; rechazar cambios inválidos. |
| Proteger la información | RF-02 | Intentar consultar o modificar la cita de otro paciente: el sistema rechaza el acceso sin exponer sus datos. |


### 1.2. EDT – Esquema de Desglose de Tareas

La EDT organiza el trabajo por entregables desde el inicio hasta el cierre académico. Se mantiene la estructura del avance y se completa la responsabilidad por perfil; cada responsable coordina el paquete con apoyo del equipo.

**PM:** Project Manager. **ADB:** Architecture DB. **WM:** Web Master. **DEV:** Programming.

Esta asignación corresponde a la planificación y no certifica tareas ya realizadas por cada integrante.

| Fase / Entregable | Tarea / Paquete de Trabajo | Responsable |
| --- | --- | --- |
| 1. INICIO | 1.1 Definición del alcance. Identificación del problema, objetivos, límites y beneficios esperados del sistema. | PM |
| 1. INICIO | 1.2 Identificación de interesados. Identificación de pacientes, personal administrativo, profesionales de salud y responsables institucionales. | PM / equipo |
| 1. INICIO | 1.3 Acta del proyecto. Elaboración del documento inicial con alcance, objetivos y planificación general. | PM |
| 2. PLANIFICACIÓN | 2.1 Levantamiento de requerimientos. Identificación de requerimientos funcionales y no funcionales del sistema. | PM / ADB |
| 2. PLANIFICACIÓN | 2.2 Análisis del proceso actual. Modelar el proceso AS-IS con información verificada; el levantamiento institucional permanece pendiente. | PM / ADB |
| 2. PLANIFICACIÓN | 2.3 Definición del proceso propuesto. Diseño del flujo optimizado mediante la plataforma digital. | ADB / WM |
| 2. PLANIFICACIÓN | 2.4 Elaboración de planificación. Organización de actividades, tiempos, recursos y responsables del proyecto. | PM |
| 3. DISEÑO | 3.1 Diseño UX/UI. Creación de interfaces orientadas a pacientes, profesionales y administración. | WM |
| 3. DISEÑO | 3.2 Diseño de arquitectura software. Definir monolito modular, límites funcionales y comunicación mediante servicios y API. | ADB / DEV |
| 3. DISEÑO | 3.3 Diseño de base de datos. Modelado de entidades, relaciones y estructura de almacenamiento. | ADB |
| 3. DISEÑO | 3.4 Diseño de APIs. Definición de servicios de comunicación entre frontend y backend. | ADB / DEV |
| 3. DISEÑO | 3.5 Modelamiento del sistema. Elaboración de casos de uso, diagramas y modelos necesarios. | ADB |
| 4. IMPLEMENTACIÓN | 4.1 Módulo de autenticación y seguridad. Implementación de usuarios, roles y control de acceso. | DEV |
| 4. IMPLEMENTACIÓN | 4.2 Módulo de pacientes. Desarrollo de gestión de información del paciente. | DEV |
| 4. IMPLEMENTACIÓN | 4.3 Módulo de profesionales. Desarrollo de gestión de médicos y personal asistencial. | DEV |
| 4. IMPLEMENTACIÓN | 4.4 Módulo de agenda médica. Implementación de horarios y disponibilidad profesional. | DEV / ADB |
| 4. IMPLEMENTACIÓN | 4.5 Módulo de citas médicas. Desarrollo del proceso de reserva, modificación y cancelación de citas. | DEV / ADB |
| 4. IMPLEMENTACIÓN | 4.6 Módulo de lista de espera. Gestión de pacientes sin disponibilidad inmediata. | DEV |
| 4. IMPLEMENTACIÓN | 4.7 Módulo de notificaciones. Implementación de avisos y recordatorios relacionados con citas. | DEV |
| 4. IMPLEMENTACIÓN | 4.8 Flujo asistencial. Implementar etapas operativas, transiciones válidas e historial de eventos. | DEV / WM |
| 4. IMPLEMENTACIÓN | 4.9 Portal público y reservas. Implementar interfaz Next.js, consulta de especialidades, reserva y seguimiento. | WM / DEV |
| 4. IMPLEMENTACIÓN | 4.10 Aplicación móvil. Construir e integrar el cliente React Native y Expo con la API compartida. | DEV / WM |
| 4. IMPLEMENTACIÓN | 4.11 Indicadores y panel. Integrar consultas por periodo, agenda y flujo según permisos. | WM / DEV |
| 4. IMPLEMENTACIÓN | 4.12 Auditoría y prioridad. Registrar eventos y solicitudes de prioridad con validación autorizada. | DEV / ADB |
| 5. PRUEBAS | 5.1 Pruebas funcionales. Validación de funcionalidades principales del sistema. | DEV / WM |
| 5. PRUEBAS | 5.2 Pruebas de integración. Verificación de comunicación entre módulos. | DEV / ADB |
| 5. PRUEBAS | 5.3 Pruebas de seguridad. Validación de autenticación, permisos y protección de información. | DEV / ADB |
| 5. PRUEBAS | 5.4 Validación del usuario. Evaluación del sistema respecto a las necesidades identificadas. | PM / WM |
| 5. PRUEBAS | 5.5 Concurrencia y rendimiento. Probar doble reserva, reprogramación atómica y metas de carga. | DEV / ADB |
| 5. PRUEBAS | 5.6 Extremo a extremo y accesibilidad. Validar recorrido paciente-personal, teclado y compatibilidad. | WM / DEV |
| 6. CIERRE | 6.1 Despliegue del sistema. Preparar un entorno académico reproducible con configuración, secretos y respaldo; no implica producción hospitalaria. | DEV |
| 6. CIERRE | 6.2 Documentación final. Elaboración de manuales técnicos y de usuario. | WM / DEV |
| 6. CIERRE | 6.3 Capacitación y entrega. Preparación de usuarios finales y presentación del proyecto. | PM / WM |
| 6. CIERRE | 6.4 Cierre del proyecto. Evaluación final de resultados y lecciones aprendidas. | PM / equipo |


El diseño precede al desarrollo de cada componente. La entrega requiere validar flujos críticos, resolver defectos bloqueantes y documentar limitaciones. El cronograma detallado está en `docs/planning/gantt.md` y la descomposición ampliada en `docs/planning/wbs.md`.

![EDT detallada - Nivel 3](assets/edt-detallada-nivel-3.png)


#### 1. Inicio del Proyecto


##### 1.1. Definición del alcance del proyecto

Se establece el propósito del sistema, los límites del proyecto, objetivos principales y entregables esperados para la plataforma de gestión de citas médicas.


##### 1.2. Identificación de interesados

Se identifican los usuarios involucrados como pacientes, profesionales de salud, personal administrativo y responsables del hospital.


##### 1.3. Acta del proyecto

Se formaliza el inicio del proyecto mediante la definición de objetivos, recursos iniciales y aprobación del desarrollo de HospitalPlatform.


#### 2. Análisis y Planificación


##### 2.1. Levantamiento de requerimientos

Se recopila información sobre las necesidades del hospital y se determinan los requerimientos funcionales y no funcionales del sistema.


##### 2.2. Análisis del proceso actual

Se estudia el proceso actual de solicitud y atención de citas médicas, identificando problemas, limitaciones y oportunidades de mejora.


##### 2.3. Definición del proceso propuesto

Se establece el nuevo flujo de atención médica digital, considerando la interacción entre pacientes, administrativos y profesionales de salud.


#### 3. Diseño de la Solución


##### 3.1. Diseño UX/UI

Se diseñan las interfaces del sistema, estructura de navegación y experiencia del usuario para facilitar el uso de la plataforma.


##### 3.2. Diseño de arquitectura del sistema

Se define la estructura general de la solución tecnológica, organización de componentes e integración de los módulos principales.


##### 3.3. Diseño del modelo de datos

Se establece la organización de la información del sistema mediante la identificación de datos principales, relaciones y estructuras necesarias.


#### 4. Desarrollo e Implementación


##### 4.1. Autenticación y Seguridad

Se desarrolla el módulo encargado del acceso seguro al sistema, administración de usuarios y control de permisos.


###### 4.1.1. Gestión de usuarios

Permite registrar y administrar las cuentas de pacientes, profesionales y personal administrativo.


###### 4.1.2. Control de acceso

Gestiona el ingreso al sistema mediante validación de usuarios y permisos correspondientes.


###### 4.1.3. Gestión de perfiles

Define los diferentes roles y funciones disponibles dentro de HospitalPlatform.


##### 4.2. Gestión de Pacientes

Se implementan funcionalidades para administrar la información de los pacientes registrados.


###### 4.2.1. Registro de pacientes

Permite crear nuevos registros con información personal y datos necesarios para la atención.


###### 4.2.2. Actualización de información

Facilita la modificación y mantenimiento de los datos del paciente.


###### 4.2.3. Consulta de datos

Permite visualizar información registrada del paciente dentro del sistema.


##### 4.3. Gestión de Profesionales de Salud

Se desarrolla el módulo para administrar médicos, especialidades y datos profesionales.


###### 4.3.1. Registro de profesionales

Permite registrar información del personal médico disponible.


###### 4.3.2. Gestión de especialidades

Organiza las áreas médicas disponibles para solicitud de citas.


###### 4.3.3. Administración profesional

Gestiona información y disponibilidad de los profesionales de salud.


##### 4.4. Gestión de Agenda Médica

Se implementa la administración de horarios y disponibilidad de atención médica.


###### 4.4.1. Configuración de horarios

Permite establecer los horarios disponibles de cada profesional.


###### 4.4.2. Gestión de disponibilidad

Controla los espacios disponibles para nuevas citas médicas.


###### 4.4.3. Administración de agenda

Organiza las agendas médicas según especialistas y horarios.


##### 4.5. Gestión de Citas Médicas

Se desarrolla el módulo principal para registrar y administrar las citas médicas.


###### 4.5.1. Solicitud de cita

Permite al paciente solicitar una atención médica según especialidad y disponibilidad.


###### 4.5.2. Validación de disponibilidad

Verifica los horarios disponibles antes de confirmar una cita.


###### 4.5.3. Confirmación de cita

Registra y confirma la reserva realizada por el paciente.


###### 4.5.4. Cancelación de cita

Permite cancelar citas previamente registradas.


###### 4.5.5. Reprogramación de cita

Permite modificar fecha u horario de una cita existente.


##### 4.6. Gestión de Lista de Espera

Administra solicitudes pendientes cuando no existen horarios disponibles.


###### 4.6.1. Registro de solicitudes pendientes

Almacena pacientes que requieren una nueva disponibilidad.


###### 4.6.2. Seguimiento de disponibilidad

Controla nuevos espacios generados en la agenda médica.


###### 4.6.3. Asignación posterior

Permite asignar citas según disponibilidad encontrada.


##### 4.7. Sistema de Notificaciones

Gestiona avisos automáticos relacionados con las citas médicas.


###### 4.7.1. Confirmaciones

Envía mensajes confirmando la reserva de una cita.


###### 4.7.2. Recordatorios

Genera avisos previos a la fecha de atención.


###### 4.7.3. Avisos al paciente

Comunica cambios, cancelaciones o actualizaciones importantes.


##### 4.8. Flujo Asistencial

Gestiona el proceso de atención del paciente dentro del hospital.


###### 4.8.1. Recepción del paciente

Registra la llegada del paciente al establecimiento.


###### 4.8.2. Registro de llegada

Actualiza el estado de asistencia relacionado con la cita.


###### 4.8.3. Seguimiento de atención

Permite controlar el proceso hasta la finalización de la consulta.


##### 4.9. Portal Público y Reservas

Desarrolla el acceso externo para consulta y solicitud de citas.


###### 4.9.1. Consulta de información

Permite visualizar información general del servicio médico.


###### 4.9.2. Solicitud pública de cita

Facilita el registro de solicitudes desde el portal institucional.


###### 4.9.3. Seguimiento de reservas

Permite consultar el estado de una cita registrada.


##### 4.10. Aplicación móvil del paciente

Implementa una aplicación para facilitar la gestión de citas desde dispositivos móviles.


###### 4.10.1. Consulta de citas

Permite visualizar próximas citas médicas.


###### 4.10.2. Gestión personal

Facilita la actualización de información del usuario.


###### 4.10.3. Notificaciones móviles

Envía alertas relacionadas con la atención médica.


##### 4.11. Indicadores y Panel de Gestión

Permite analizar información del funcionamiento del sistema.


###### 4.11.1. Definición de métricas

Establece indicadores para evaluar la gestión de citas.


###### 4.11.2. Generación de reportes

Produce informes sobre atención y uso del sistema.


###### 4.11.3. Visualización de indicadores

Presenta datos mediante paneles de control.


##### 4.12. Auditoría y Priorización

Controla registros importantes y seguimiento de actividades.


###### 4.12.1. Seguimiento de acciones

Registra actividades realizadas dentro del sistema.


###### 4.12.2. Registro histórico

Mantiene historial de operaciones realizadas.


###### 4.12.3. Gestión prioritaria

Permite administrar casos que requieren atención especial.


#### 5. Pruebas y Validación


##### 5.1. Pruebas funcionales

Se verifican las funcionalidades principales del sistema mediante casos de prueba.


##### 5.2. Pruebas de integración

Se valida la correcta comunicación entre los módulos desarrollados.


##### 5.3. Pruebas de seguridad

Se comprueba la protección de accesos e información del sistema.


##### 5.4. Validación con usuarios

Se realizan pruebas con usuarios finales para obtener observaciones y mejoras.


#### 6. Cierre del Proyecto


##### 6.1. Documentación final

Se elaboran manuales, documentos técnicos y registros finales del proyecto.


##### 6.2. Capacitación

Se capacita al personal administrativo y profesionales de salud en el uso del sistema.


##### 6.3. Entrega del proyecto

Se realiza la presentación final, entrega de resultados y cierre formal del proyecto.


### 1.3. Perfiles del equipo de desarrolladores

El equipo está integrado por siete estudiantes. Se conserva la distribución funcional del avance, utilizando nombres de la portada y retirando comentarios personales ajenos al informe. Un integrante puede participar en más de un perfil.


#### 1.3.1. Project Manager

| Campo | Descripción |
| --- | --- |
| Integrantes | Puente Arce, Leonardo Joaquin |
| Rol | Project Manager |
| Objetivo | Coordinar alcance, prioridades, cronograma, riesgos y entregas académicas. |
| Responsabilidades | Mantener acta, EDT y cronograma; organizar reuniones; controlar cambios; consolidar evidencias y coordinar la validación de requisitos. |
| Habilidades clave | Planificación, comunicación, gestión de riesgos y coordinación. |
| Herramientas | Documentación del repositorio, Git/GitHub y cronograma Gantt. |


#### 1.3.2. Architecture DB

| Campo | Descripción |
| --- | --- |
| Integrantes | Puente Arce, Leonardo Joaquin. / Povis Zavala, Rodrigo Raul. |
| Rol | Architecture DB |
| Objetivo | Diseñar la arquitectura de datos y restricciones de integridad del núcleo ambulatorio. |
| Responsabilidades | Mantener modelos conceptual, lógico y físico; revisar SQL y migraciones; definir relaciones, índices y consistencia transaccional; documentar decisiones y apoyar contratos API. |
| Habilidades clave | Modelado relacional, SQL, PostgreSQL, normalización y transacciones. |
| Herramientas | PostgreSQL, Flyway, documentación ADR y herramientas de diagramación. |


#### 1.3.3. Web Master

| Campo | Descripción |
| --- | --- |
| Integrantes | Espinoza Caso, Emanuel Anthony / Nuñez Vasquez, Yosselyn Aracelly |
| Rol | Web Master |
| Objetivo | Diseñar e implementar interfaces web para pacientes y personal autorizado. |
| Responsabilidades | Preparar prototipos; construir portal público, reservas y paneles; integrar API; revisar diseño adaptable, accesibilidad y errores; coordinar coherencia visual con móvil. |
| Habilidades clave | HTML, CSS, TypeScript, React, Next.js, UX/UI y pruebas de interfaz. |
| Herramientas | Next.js, React, Tailwind CSS y Playwright previstos; herramienta de prototipado a elección del equipo. |


#### 1.3.4. Programming

| Campo | Descripción |
| --- | --- |
| Integrantes | Apaza Huaman, Joan Ricardo / Ramirez Calixto, Dominick Joshiro / Valqui Cabello, Eduar Alonzo |
| Rol | Programming |
| Objetivo | Implementar API, reglas de negocio, persistencia y cliente móvil. |
| Responsabilidades | Desarrollar autenticación, permisos, agenda, citas, espera y auditoría; aplicar transacciones; integrar notificaciones; construir móvil con apoyo de WM; ejecutar pruebas y preparar despliegue académico. |
| Habilidades clave | Java, orientación a objetos, Spring Boot, REST, SQL, pruebas y TypeScript. |
| Herramientas | Java 21, Spring Boot, Maven, PostgreSQL, JPA, Flyway y Testcontainers; React Native, Expo y Docker previstos. |


### 1.4. Modelo del Negocio – Diagrama de Actividades

El modelo TO-BE representa la gestión digital de citas y su continuidad en consulta externa. Los carriles distinguen al paciente, HospitalPlatform, recepción o personal autorizado y profesional de salud. Admisión y triaje se incorporarán según el proceso institucional validado; no se confunden con estados ya presentes en el SQL.

![Diagrama de actividades - Gestión de citas médicas](assets/diagrama-actividades-gestion-citas.png)


#### 1.4.1. Descripción del Flujo de Actividades

| N.º | Actor | Actividad | Descripción |
| --- | --- | --- | --- |
| 1 | Paciente | Acceder y autenticarse | Ingresar desde web o móvil; registrarse si no dispone de cuenta. |
| 2 | Sistema | Validar acceso | Comprobar credenciales, estado de cuenta y permisos; rechazar acceso inválido. |
| 3 | Paciente / sistema | Consultar disponibilidad | Elegir especialidad, profesional y fecha; mostrar solo cupos disponibles. |
| 4 | Paciente / personal | Gestionar falta de cupos | Elegir otra fecha o solicitar lista de espera; la asignación exige elegibilidad y cupo libre. |
| 5 | Paciente / sistema | Reservar | Seleccionar cupo y revalidarlo en la transacción; ante conflicto, volver a consultar. |
| 6 | Sistema | Registrar y notificar | Crear cita, actualizar disponibilidad y generar constancia. Un fallo de aviso no duplica la reserva. |
| 7 | Paciente / sistema | Confirmar o modificar | Confirmar, cancelar o reprogramar según reglas. Conservar cita original si falla la reprogramación. |
| 8 | Personal / sistema | Reasignar cupo | Evaluar lista de espera, validar elegibilidad y asignar una sola reserva activa. |
| 9 | Recepción / sistema | Registrar llegada | Validar cita y registrar CHECK_IN. Si no hay llegada, aplicar política validada; NO_SHOW exige desarrollo adicional. |
| 10 | Personal / sistema | Preparar atención | Registrar admisión y triaje si corresponden al proceso validado; situar al paciente en WAITING. |
| 11 | Profesional / sistema | Iniciar atención | Consultar agenda asignada, llamar al paciente y registrar IN_ATTENTION. |
| 12 | Profesional / sistema | Finalizar atención | Registrar FINISHED y estado administrativo correspondiente, sin historia clínica electrónica completa. |
| 13 | Sistema / personal | Consultar trazabilidad | Conservar actor y fecha de cambios y consultar indicadores por periodo. |


Las figuras siguientes forman un mismo flujo. El conector A enlaza reserva y atención; falta de cupo y conflicto tienen salidas explícitas. Cancelación, reprogramación, inasistencia y prioridad se detallan en las decisiones de control.

![Continuación del flujo de actividades](assets/flujo-actividades-continuacion.png)


#### 1.4.2. Decisiones Clave del Flujo

| ID | Punto de control | Acción y resultado |
| --- | --- | --- |
| D-01 | ¿Sesión y permisos válidos? | Sí: continuar. No: solicitar autenticación o rechazar la acción; no mostrar datos restringidos. |
| D-02 | ¿Existen cupos disponibles? | Sí: permitir selección. No: ofrecer otra fecha, profesional o lista de espera; no crear cita sin cupo. |
| D-03 | ¿El cupo sigue libre? | Sí: confirmar transacción. No: informar conflicto y actualizar disponibilidad; no persistir reserva parcial. |
| D-04 | ¿Confirma o cancela? | Confirmación: registrar CONFIRMED. Cancelación permitida: registrar CANCELLED, actor y fecha; conservar historial y gestionar liberación. |
| D-05 | ¿Nuevo cupo válido para reprogramar? | Sí: registrar cambio atómico conservando relación con la cita anterior. No: mantener cita original. |
| D-06 | ¿Hay paciente elegible en espera? | Sí: personal autorizado valida asignación y el sistema revalida cupo. No: mantenerlo disponible. Prioridad exige criterios aprobados. |
| D-07 | ¿Se registró llegada? | Sí: iniciar CHECK_IN. No: esperar el plazo validado y gestionar incidencia. No contabilizar NO_SHOW sin regla y registro implementados. |
| D-08 | ¿Transición permitida? | Sí: registrar etapa, actor y fecha. No: rechazar cambio; una cita cancelada no continúa a atención. |
| D-09 | ¿Se entregó la notificación? | Sí: conservar resultado. No: registrar fallo y gestionar reintentos; la reserva ya confirmada permanece registrada. |


#### 1.4.3. Indicadores del Modelo de Negocio (KPIs)

**Diagrama de procesos**

Se conservan los ocho indicadores del avance y se añaden inasistencia y cobertura de trazabilidad. Los porcentajes se calculan para un mismo periodo y población definida; ante denominador cero se informa «no aplica», no 0 %.

Las metas son propuestas académicas sujetas a validación del equipo e instrumentos disponibles. No representan una línea base, mejora observada ni compromiso del Hospital de Huaycán.

| Indicador | Definición y fuente | Meta / condición |
| --- | --- | --- |
| Tiempo de gestión de cita | Promedio de minutos entre inicio de tarea y emisión de constancia. Fuente: tareas del piloto. | Propuesta: ≤ 5 min por reserva completada; informar mediana y número de tareas. |
| Disponibilidad publicada | Cupos mostrados disponibles que coinciden con estado persistido / cupos revisados × 100. Fuente: interfaz y base. | 100 % de coincidencia en verificaciones sin cambios concurrentes; revalidación al reservar. |
| Tasa de confirmación | Citas confirmadas antes de hora programada / citas vigentes que requieren confirmación × 100. Fuente: historial. | Medir en piloto; fijar meta operativa después de conocer línea base y plazos. |
| Tasa de cancelaciones | Citas canceladas / citas de cohorte con atención prevista en el periodo × 100; excluir sustituidas por reprogramación. | Registrar 100 % de cancelaciones del ensayo; definir tasa deseable con línea base, sin penalizar cancelaciones oportunas. |
| Recuperación de cupos | Cupos liberados elegibles reasignados antes de su inicio / cupos liberados elegibles × 100. Fuente: historial y espera. | Propuesta: ≥ 80 % en escenario sintético con candidatos elegibles y tiempo suficiente; no extrapolar a operación real. |
| Tiempo de espera | Promedio de minutos entre CHECK_IN e IN_ATTENTION para atenciones con ambas marcas válidas. | Medir 100 % de casos trazables del ensayo; definir reducción esperada después de levantar línea base. |
| Ocupación efectiva de agenda | Cupos con atención finalizada / cupos habilitados del periodo × 100, excluyendo bloqueos. | Medir piloto; fijar meta institucional después de validar demanda y capacidad. |
| Satisfacción del usuario | Respuestas de 4 o 5 en escala de 1 a 5 / respuestas válidas × 100. Fuente: encuesta posterior a tareas. | Propuesta: ≥ 80 %; documentar cuestionario, muestra y limitaciones. |
| Tasa de inasistencia | Citas clasificadas como inasistencia / citas vigentes cuya hora de atención terminó × 100; excluir cancelaciones y reprogramaciones. | Previsto: exige plazo validado y registro adicional al SQL actual. Sin ellos, informar «no medible». |
| Cobertura de trazabilidad | Transiciones críticas con actor, fecha y entidad / transiciones críticas ejecutadas × 100. Fuente: historial y auditoría. | 100 % de transiciones críticas ensayadas registradas y reconstruibles. |


#### 1.4.4. Diagrama general de procesos

![Diagrama general de procesos (BPMN)](assets/diagrama-general-procesos-bpmn.png)
