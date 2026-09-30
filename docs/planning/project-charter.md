# Project Charter — Hospital de Huaycán

**Proyecto:** `hospital-platform`  
**Curso:** Curso Integrador I: Sistemas Software  
**Unidad de aprendizaje:** Unidad 1 — Planificación y análisis  
**Documento:** Project Charter / Acta de Constitución del Proyecto  
**Caso de estudio propuesto:** Hospital de Huaycán — MINSA / DIRIS Lima Este  
**Estado:** borrador académico inicial, sujeto a validación docente y levantamiento institucional

> Este documento adapta el Project Charter de PMBOK al contexto académico del curso.
> No representa autorización institucional del Hospital de Huaycán, MINSA, DIRIS Lima Este o PRONIS.
> La aprobación del docente corresponde al ámbito académico del proyecto.

---

# 1. Información general del proyecto

| Campo | Detalle |
| --- | --- |
| Nombre del proyecto | Plataforma web y móvil para la gestión de citas médicas y trazabilidad del flujo de consulta externa |
| Repositorio | `hospital-platform` |
| Curso | Curso Integrador I: Sistemas Software |
| Equipo | 7 integrantes |
| Caso de estudio | Hospital de Huaycán — MINSA / DIRIS Lima Este |
| Tipo de proyecto | Académico — desarrollo de software |
| Enfoque | Planificación, análisis, diseño, desarrollo, pruebas, despliegue y mantenimiento |
| Arquitectura propuesta | Monolito Modular + Package by Feature |
| Backend | Java 21 + Spring Boot |
| Base de datos | PostgreSQL |
| Web | Next.js + React + TypeScript |
| Mobile | React Native + Expo + TypeScript |
| Estado actual | Investigación, planificación y análisis |

---

# 2. Propósito del proyecto

El propósito del proyecto es diseñar, desarrollar, implementar y evaluar una plataforma web y móvil que apoye la gestión de citas médicas y la trazabilidad del flujo de consulta externa del Hospital de Huaycán como caso de estudio académico.

La solución busca integrar en una misma plataforma:

- información institucional pública;
- consulta de especialidades y disponibilidad;
- reserva de citas;
- confirmación anticipada;
- cancelación y reprogramación;
- recuperación de cupos liberados;
- lista de espera;
- priorización ambulatoria validada por personal autorizado;
- seguimiento del paciente dentro del flujo de consulta externa;
- notificaciones;
- auditoría;
- métricas operativas;
- accesibilidad web y móvil.

El proyecto no se limita a crear una interfaz para reservar citas. Su enfoque es representar de manera trazable el ciclo completo de la atención ambulatoria desde la programación hasta el cierre administrativo de la consulta.

---

# 3. Justificación

La literatura académica revisada muestra que los sistemas digitales de citas pueden contribuir a mejorar tiempos de gestión, acceso a información, eficiencia administrativa, satisfacción y utilización de agendas.

También existen antecedentes internacionales que evidencian la importancia de factores como:

- no-show;
- cancelaciones;
- programación dinámica;
- tiempos de espera;
- facilidad de uso;
- privacidad;
- soporte;
- integración;
- gestión del cambio.

En el Hospital de Huaycán existen fuentes institucionales relacionadas con consulta externa, selección, admisión, archivo y Plan Cero Colas que justifican estudiar el flujo ambulatorio como un problema de gestión relevante.

Sin embargo, el proyecto no presupone la existencia ni la magnitud de problemas específicos. Las causas, tiempos, porcentajes y reglas institucionales deberán validarse mediante documentación oficial y levantamiento autorizado.

---

# 4. Problema de alto nivel

El problema de trabajo se formula de manera preliminar como:

> **¿De qué manera la implementación de una plataforma web y móvil puede apoyar la gestión de citas médicas, reducir el desaprovechamiento de cupos por inasistencias evitables y mejorar la trazabilidad del flujo de consulta externa del Hospital de Huaycán — MINSA, considerando sus procesos actuales y el escenario de su futura infraestructura hospitalaria?**

Estado:

**PENDIENTE DE VALIDACIÓN DOCENTE Y LEVANTAMIENTO EN CAMPO.**

---

# 5. Objetivo general

Diseñar, desarrollar, implementar y evaluar una plataforma web y móvil orientada a apoyar la gestión de citas médicas y la trazabilidad del flujo de consulta externa del Hospital de Huaycán — MINSA, incorporando un portal público institucional, un portal especializado de reservas, confirmación anticipada de asistencia, cancelación, reprogramación, recuperación de cupos liberados, gestión de lista de espera y priorización de citas validada por personal autorizado, además de criterios de seguridad, privacidad, accesibilidad, usabilidad, consistencia transaccional, observabilidad e interoperabilidad.

---

# 6. Objetivos específicos

## OE1 — Levantamiento y modelado

Analizar y modelar el proceso actual de consulta externa y gestión de citas del Hospital de Huaycán mediante fuentes institucionales y levantamiento autorizado.

## OE2 — Diseño del proceso digital

Definir el proceso TO-BE, requerimientos funcionales y no funcionales, flujos de usuario, modelo de datos y contrato API.

## OE3 — Implementación del núcleo web y móvil

Implementar progresivamente los módulos de identidad, contenido institucional, catálogo, pacientes, profesionales, agendas, citas, priorización, lista de espera, notificaciones y flujo ambulatorio.

## OE4 — Seguridad y privacidad

Aplicar autenticación, autorización, auditoría, validación, minimización de datos, protección frente a abuso y controles alineados con buenas prácticas OWASP.

## OE5 — Validación y aprovechamiento de agenda

Evaluar la solución mediante pruebas funcionales, integración, concurrencia, seguridad, end-to-end, usabilidad, accesibilidad y rendimiento.

## OE6 — Información institucional y acceso digital

Diseñar una experiencia pública web que permita consultar información institucional verificable y dirigir al usuario hacia el portal de reservas.

---

# 7. Alcance de alto nivel

## 7.1 Incluido en el MVP

### Portal público institucional

- información del hospital;
- ubicación;
- contacto;
- servicios;
- especialidades;
- políticas;
- preguntas frecuentes;
- contenido multimedia autorizado;
- acceso al portal de reservas.

### Portal web de reservas

- especialidades;
- disponibilidad;
- reserva;
- confirmación;
- cancelación;
- reprogramación;
- lista de espera;
- ofertas de cupos;
- consulta de citas.

### Web operativa interna

- agenda;
- disponibilidad;
- admisión;
- priorización;
- seguimiento de flujo;
- cola de pacientes;
- administración;
- auditoría;
- métricas.

### Aplicación móvil

- autenticación;
- disponibilidad;
- reserva;
- confirmación;
- cancelación;
- reprogramación;
- lista de espera;
- ofertas;
- notificaciones;
- check-in cuando corresponda;
- seguimiento administrativo.

### Capacidades transversales

- seguridad;
- privacidad;
- accesibilidad;
- auditoría;
- notificaciones;
- consistencia transaccional;
- prevención de doble reserva;
- prevención de doble aceptación de cupos;
- métricas.

---

# 8. Fuera del alcance inicial

No forman parte del MVP:

- historia clínica electrónica completa;
- diagnóstico médico;
- prescripción electrónica;
- farmacia completa;
- laboratorio clínico completo;
- imágenes diagnósticas completas;
- hospitalización;
- emergencia;
- cirugía;
- gestión de equipos biomédicos;
- integración real con sistemas MINSA sin autorización;
- autorización automática de cobertura SIS;
- uso de datos clínicos reales.

Estas capacidades podrán analizarse posteriormente como expansión futura.

---

# 9. Entregables principales

## 9.1 Documentación de planificación y análisis

- Lean Canvas;
- Project Charter;
- WBS;
- cronograma/Gantt;
- plan de levantamiento de requerimientos;
- instrumentos de toma de requerimientos;
- análisis AS-IS;
- análisis TO-BE;
- PRD;
- App Flow.

## 9.2 Documentación técnica

- TRD;
- UI/UX Design Brief;
- Backend Schema;
- OpenAPI;
- Security Threat Model;
- Test Strategy;
- Deployment Plan;
- Monitoring & Maintenance Plan;
- ADRs cuando correspondan.

## 9.3 Producto software

- backend Spring Boot;
- portal público;
- portal de reservas;
- web operativa;
- aplicación móvil;
- base de datos PostgreSQL;
- migraciones;
- pruebas automatizadas;
- CI/CD;
- staging académico si el curso lo requiere.

---

# 10. Requisitos de alto nivel

## Funcionales

- gestionar usuarios y roles;
- gestionar especialidades y servicios;
- gestionar profesionales;
- configurar agendas y disponibilidad;
- reservar citas;
- confirmar asistencia;
- cancelar y reprogramar;
- liberar cupos;
- gestionar lista de espera;
- ofrecer cupos;
- validar prioridad ambulatoria;
- registrar llegada;
- gestionar estados del flujo ambulatorio;
- enviar notificaciones;
- auditar operaciones;
- consultar métricas.

## No funcionales

- seguridad;
- privacidad;
- accesibilidad;
- usabilidad;
- disponibilidad;
- rendimiento;
- trazabilidad;
- mantenibilidad;
- escalabilidad progresiva;
- consistencia transaccional;
- observabilidad.

---

# 11. Stakeholders de alto nivel

| Stakeholder | Rol / interés |
| --- | --- |
| Docente | Valida académicamente alcance, metodología y entregables |
| Equipo de 7 estudiantes | Análisis, diseño, desarrollo, pruebas y documentación |
| Pacientes | Usuarios principales del portal de reservas y app móvil |
| Personal de admisión | Gestión operativa de citas y llegada |
| Profesionales de salud | Consulta de agenda y flujo de pacientes |
| Revisor de prioridad | Validación de prioridad ambulatoria, si la institución la confirma |
| Administrador | Configuración y control del sistema |
| Auditor | Consulta de trazabilidad y eventos |
| Personal TI | Interés futuro en soporte, seguridad y operación |
| Hospital de Huaycán | Caso de estudio; no se presume participación oficial |
| DIRIS Lima Este / MINSA | Contexto institucional y posibles fuentes oficiales |

---

# 12. Supuestos

Se trabaja inicialmente bajo los siguientes supuestos:

- el docente validará o ajustará el caso Hospital de Huaycán;
- se podrá acceder a suficiente información institucional pública;
- el equipo podrá realizar o simular un levantamiento metodológicamente válido;
- se utilizarán datos sintéticos;
- la solución podrá evaluarse académicamente sin conectarse a sistemas reales;
- web y móvil consumirán el mismo backend;
- PostgreSQL será suficiente para el MVP;
- no se requerirán microservicios para cumplir el alcance;
- las integraciones externas serán opcionales hasta contar con evidencia y autorización.

Los supuestos deberán revisarse conforme avance el proyecto.

---

# 13. Restricciones

## Académicas

- seguir el sílabo del curso;
- cumplir entregables por unidad;
- respetar fechas definidas por el docente;
- documentar evidencia de avance;
- evitar afirmaciones no verificadas.

## Técnicas

- Java 21;
- Spring Boot;
- PostgreSQL;
- arquitectura inicial de monolito modular;
- Package by Feature;
- YAML para configuración;
- datos sintéticos;
- no introducir tecnologías innecesarias sin justificación.

## Seguridad y privacidad

- no usar historias clínicas reales;
- no utilizar DNI reales en demos;
- no exponer datos sensibles;
- secretos fuera del repositorio;
- autorización por rol y objeto;
- uso académico claramente identificado.

---

# 14. Riesgos de alto nivel

| Riesgo | Impacto | Respuesta inicial |
| --- | --- | --- |
| Caso Huaycán no validado por el docente | Alto | validar antes de cerrar PRD |
| Información institucional insuficiente | Alto | combinar fuentes oficiales, entrevistas y escenarios académicos |
| Inventar procesos o métricas | Alto | documentar hipótesis y separar hechos de propuestas |
| Scope creep | Alto | mantener MVP y fuera de alcance explícitos |
| Doble reserva | Alto | constraints, transacciones y pruebas concurrentes |
| Doble aceptación de un cupo | Alto | operación atómica e idempotencia |
| Datos personales expuestos | Alto | datos sintéticos, mínimo privilegio y pruebas de seguridad |
| App móvil retrasa el proyecto | Medio/Alto | planificar vertical slices y reutilizar API |
| Complejidad excesiva | Medio | evitar microservicios y dependencias sin trigger |
| Baja usabilidad | Medio | prototipos y pruebas tempranas |
| Accesibilidad insuficiente | Medio | WCAG, contraste y pruebas específicas |
| Dependencia de proveedores externos | Medio | adapters, fallback y funcionalidades desacopladas |

---

# 15. Hitos de alto nivel

Los hitos se alinean con el sílabo y el roadmap del proyecto.

## Unidad 1 — Planificación y análisis

- Lean Canvas;
- Project Charter;
- WBS;
- Gantt;
- toma de requerimientos;
- planificación del proyecto.

## Unidad 2 — Diseño

- BPMN;
- diseño de base de datos;
- prototipos UI/UX;
- documentación y arquitectura detallada.

## Unidad 3 — Desarrollo

- arquitectura inicial;
- implementación progresiva;
- backend;
- web;
- mobile;
- control de versiones;
- avances de desarrollo.

## Unidad 4 — Pruebas, despliegue y mantenimiento

- pruebas de software;
- pruebas de seguridad;
- despliegue;
- monitoreo;
- mantenimiento;
- cierre del proyecto.

Las fechas concretas se definirán en `gantt.md`.

---

# 16. Criterios de éxito

El proyecto será considerado exitoso académicamente si:

- cumple los entregables definidos por el curso;
- presenta un problema y alcance coherentes y validados;
- mantiene trazabilidad entre investigación, requerimientos, diseño y código;
- implementa el MVP acordado;
- web y móvil utilizan el mismo backend y reglas de negocio;
- se evita double booking;
- se evita doble aceptación de cupos;
- los flujos críticos funcionan end-to-end;
- las pruebas automatizadas críticas están verdes;
- se aplican controles de seguridad;
- se utilizan datos sintéticos;
- existe evidencia de accesibilidad y usabilidad;
- el despliegue académico puede demostrarse;
- la documentación permanece alineada con el roadmap.

No se considerará criterio de éxito demostrar adopción real por el Hospital de Huaycán.

---

# 17. Presupuesto y recursos

## Presupuesto monetario

**No definido en esta etapa.**

El proyecto se desarrollará principalmente con herramientas gratuitas, académicas u open source.

Posibles costos futuros:

- hosting;
- dominio;
- base de datos administrada;
- servicios de notificación;
- almacenamiento;
- monitoreo.

Cualquier gasto deberá ser aprobado por el equipo antes de incorporarse.

## Recursos humanos

- 7 integrantes;
- docente como autoridad académica;
- actores entrevistados o consultados, si se obtiene autorización.

---

# 18. Gobernanza del proyecto

## Fuente maestra

```text
docs/00-PROJECT-ROADMAP.md
```

## Flujo documental

```text
Roadmap
   ↓
Lean Canvas
   ↓
Project Charter
   ↓
WBS
   ↓
Gantt
   ↓
Requirements Elicitation
   ↓
PRD
   ↓
App Flow / BPMN
   ↓
UI/UX
   ↓
TRD
   ↓
Backend Schema / OpenAPI
   ↓
Implementación
```

## Control de cambios

Los cambios importantes de:

- institución;
- alcance;
- objetivos;
- arquitectura;
- seguridad;
- cronograma;
- MVP;

deberán quedar documentados mediante commit, Pull Request y, cuando corresponda, ADR.

---

# 19. Autoridad y aprobación académica

La aprobación de este Project Charter significa únicamente que el equipo acepta trabajar bajo el alcance y las reglas definidas para el proyecto académico.

No constituye:

- contrato;
- convenio;
- autorización del Hospital de Huaycán;
- aprobación de MINSA;
- aprobación de DIRIS Lima Este;
- compromiso de implementación institucional.

## Aprobaciones pendientes

- [ ] Revisión del equipo.
- [ ] Validación del docente.
- [ ] Confirmación del caso Hospital de Huaycán.
- [ ] Ajuste posterior al levantamiento de campo.

---

# 20. Relación con otros documentos

Este Project Charter debe mantenerse alineado con:

```text
docs/00-PROJECT-ROADMAP.md
docs/planning/lean-canvas.md
docs/research/semana-01/problema-investigacion.md
docs/research/semana-01/marco-teorico.md
docs/research/semana-01/fuentes.md
```

Próximos documentos:

```text
docs/planning/wbs.md
docs/planning/gantt.md
docs/planning/requirements-elicitation.md
```

---

# 21. Estado del documento

- [x] Propósito definido.
- [x] Justificación definida.
- [x] Problema preliminar definido.
- [x] Objetivo general definido.
- [x] Objetivos específicos definidos.
- [x] Alcance inicial definido.
- [x] Entregables principales definidos.
- [x] Stakeholders identificados.
- [x] Supuestos identificados.
- [x] Restricciones identificadas.
- [x] Riesgos iniciales identificados.
- [x] Criterios de éxito definidos.
- [ ] Revisión final del equipo.
- [ ] Validación docente.
- [ ] Actualización posterior al levantamiento de campo.
