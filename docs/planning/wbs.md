# WBS — Work Breakdown Structure — Hospital de Huaycán

**Proyecto:** `hospital-platform`  
**Curso:** Curso Integrador I: Sistemas Software  
**Unidad de aprendizaje actual:** Unidad 1 — Planificación y análisis  
**Documento:** WBS / EDT — Estructura de Desglose del Trabajo  
**Caso de estudio propuesto:** Hospital de Huaycán — MINSA / DIRIS Lima Este  
**Estado:** versión inicial académica, alineada con el sílabo y el roadmap maestro

> La WBS descompone el proyecto en entregables y paquetes de trabajo manejables.
> No representa todavía un cronograma; las fechas, duraciones y dependencias temporales se definirán en `gantt.md`.
>
> La numeración WBS se utilizará como referencia para planificación, asignación, seguimiento y trazabilidad documental.

---

# 1. Objetivo de la WBS

Descomponer el proyecto `hospital-platform` en paquetes de trabajo controlables que permitan:

- planificar el proyecto;
- distribuir responsabilidades entre los 7 integrantes;
- relacionar actividades con el sílabo;
- identificar entregables;
- facilitar seguimiento de avance;
- construir posteriormente el cronograma Gantt;
- mantener trazabilidad entre investigación, requisitos, diseño, implementación y pruebas.

---

# 2. Principios de estructuración

La WBS sigue cinco principios:

1. **Orientación a entregables:** cada bloque debe producir un resultado verificable.
2. **Alineación con el sílabo:** las cuatro unidades académicas se reflejan en la estructura.
3. **Alineación con el roadmap:** no se programa negocio antes de investigar, validar y diseñar.
4. **Descomposición progresiva:** los paquetes pueden dividirse más adelante si aparece complejidad real.
5. **No sobreplanificación:** tecnologías o integraciones futuras no se convierten en trabajo obligatorio del MVP sin aprobación.

---

# 3. Estructura general

```text
1.0 Hospital Platform
│
├── 1.1 Gestión y planificación del proyecto
├── 1.2 Investigación y levantamiento
├── 1.3 Requerimientos y definición del producto
├── 1.4 Diseño de procesos y experiencia
├── 1.5 Diseño técnico y arquitectura
├── 1.6 Foundation e infraestructura
├── 1.7 Desarrollo backend
├── 1.8 Desarrollo web
├── 1.9 Desarrollo móvil
├── 1.10 Pruebas y seguridad
├── 1.11 Despliegue y observabilidad
├── 1.12 Documentación y entrega académica
└── 1.13 Cierre y mantenimiento
```

---

# 4. WBS detallada

## 1.0 Hospital Platform

Proyecto académico para diseñar, desarrollar, implementar y evaluar una plataforma web y móvil orientada a la gestión de citas médicas y la trazabilidad del flujo de consulta externa del Hospital de Huaycán.

---

## 1.1 Gestión y planificación del proyecto

### 1.1.1 Definición inicial del proyecto

- validar dominio hospitalario;
- validar Hospital de Huaycán como caso de estudio;
- confirmar alcance web + móvil;
- establecer repositorio y flujo Git;
- definir fuente documental maestra.

**Entregables:**

- `docs/00-PROJECT-ROADMAP.md`;
- README raíz;
- estructura base del repositorio.

### 1.1.2 Lean Canvas

- identificar problemas de trabajo;
- identificar usuarios y actores;
- formular propuesta de valor;
- definir solución preliminar;
- identificar canales;
- definir métricas;
- analizar costos y sostenibilidad;
- registrar hipótesis pendientes.

**Entregable:**

- `docs/planning/lean-canvas.md`.

### 1.1.3 Project Charter

- propósito;
- justificación;
- objetivos;
- alcance;
- entregables;
- stakeholders;
- supuestos;
- restricciones;
- riesgos;
- criterios de éxito.

**Entregable:**

- `docs/planning/project-charter.md`.

### 1.1.4 WBS

- descomponer alcance;
- codificar paquetes de trabajo;
- relacionar entregables;
- preparar base para cronograma.

**Entregable:**

- `docs/planning/wbs.md`.

### 1.1.5 Cronograma

- definir actividades;
- establecer dependencias;
- asignar semanas;
- identificar hitos;
- relacionar avance porcentual solicitado por el sílabo.

**Entregable:**

- `docs/planning/gantt.md`.

### 1.1.6 Gestión de riesgos

- identificar riesgos;
- evaluar probabilidad e impacto;
- definir mitigaciones;
- actualizar riesgos durante el proyecto.

**Entregables:**

- sección de riesgos del Project Charter;
- actualizaciones posteriores del roadmap/planes.

---

## 1.2 Investigación y levantamiento

### 1.2.1 Revisión bibliográfica

- antecedentes nacionales principales;
- antecedentes internacionales principales;
- fuentes complementarias;
- fuente metodológica;
- fuentes institucionales.

**Entregables:**

- `docs/research/semana-01/fuentes.md`;
- `docs/research/semana-01/marco-teorico.md`.

### 1.2.2 Formulación del problema

- situación problemática;
- vacío de investigación;
- problema general;
- problemas específicos;
- variables candidatas;
- objetivo general;
- objetivos específicos.

**Entregable:**

- `docs/research/semana-01/problema-investigacion.md`.

### 1.2.3 Revisión institucional

- revisar MAPRO;
- revisar Cartera de Servicios;
- revisar Plan Cero Colas;
- revisar información PRONIS;
- identificar estructura de Consulta Externa;
- registrar hechos versus hipótesis.

**Salida verificable:**

- matriz de evidencia institucional;
- actualización de documentación de discovery.

### 1.2.4 Preparación del levantamiento

- definir actores a consultar;
- preparar preguntas;
- preparar guía de observación;
- definir información agregada requerida;
- definir restricciones éticas;
- validar instrumento con docente.

**Entregable:**

- `docs/planning/requirements-elicitation.md`.

### 1.2.5 Ejecución del levantamiento

- entrevistas autorizadas;
- observación;
- revisión documental;
- identificación de reglas;
- identificación de tiempos;
- identificación de canales;
- identificación de problemas y restricciones.

**Condición:**

- no recopilar historias clínicas reales;
- no registrar datos personales identificables sin autorización.

### 1.2.6 Consolidación de resultados

- documentar hallazgos;
- separar evidencia y supuestos;
- construir línea base;
- registrar necesidades;
- actualizar Lean Canvas;
- actualizar Project Charter si fuera necesario.

---

## 1.3 Requerimientos y definición del producto

### 1.3.1 Identificación de stakeholders

- paciente;
- admisión;
- profesional;
- revisor de prioridad;
- administrador;
- auditor;
- personal TI;
- actores institucionales.

### 1.3.2 Requerimientos funcionales

Definir requisitos para:

- portal institucional;
- portal de reservas;
- aplicación móvil;
- web operativa;
- identidad;
- catálogo;
- agenda;
- citas;
- confirmación;
- cancelación;
- reprogramación;
- lista de espera;
- priorización;
- notificaciones;
- flujo ambulatorio;
- auditoría;
- métricas.

### 1.3.3 Requerimientos no funcionales

Definir requisitos de:

- seguridad;
- privacidad;
- accesibilidad;
- usabilidad;
- rendimiento;
- disponibilidad;
- mantenibilidad;
- trazabilidad;
- observabilidad;
- consistencia;
- escalabilidad progresiva.

### 1.3.4 Reglas de negocio

- reserva;
- disponibilidad;
- double booking;
- confirmación;
- cancelación;
- reprogramación;
- no-show;
- liberación de cupos;
- lista de espera;
- expiración de ofertas;
- prioridad validada;
- check-in;
- transición de estados.

### 1.3.5 Priorización del MVP

- clasificar Must / Should / Could / Won't;
- validar capacidades obligatorias;
- mantener alcance clínico fuera del MVP;
- validar app móvil como obligatoria.

### 1.3.6 PRD

**Entregable:**

- `docs/01-PRD.md`.

**Gate:**

- MVP aprobado antes de continuar al diseño detallado.

---

## 1.4 Diseño de procesos y experiencia

## 1.4.1 BPMN AS-IS

- proceso real de citas;
- admisión;
- archivo;
- triaje cuando corresponda;
- espera;
- llamado;
- consulta;
- cierre.

**Gate:**

- modelo basado en evidencia, no en hipótesis no verificadas.

### 1.4.2 BPMN TO-BE

- reserva digital;
- confirmación;
- cancelación;
- reprogramación;
- lista de espera;
- llegada;
- flujo ambulatorio;
- métricas.

### 1.4.3 App Flow

- portal público;
- reserva web;
- paciente móvil;
- admisión;
- prioridad;
- lista de espera;
- flujo interno.

**Entregable:**

- `docs/03-APP-FLOW.md`.

### 1.4.4 Diseño UI/UX

- arquitectura de información;
- navegación;
- wireframes;
- componentes;
- formularios;
- estados;
- errores;
- feedback.

### 1.4.5 Accesibilidad

- tema sistema;
- claro;
- oscuro;
- alto contraste;
- protanopia;
- deuteranopia;
- tritanopia;
- texto + iconos + color;
- navegación por teclado;
- controles táctiles adecuados.

### 1.4.6 Prototipo

- portal público;
- portal de reservas;
- backoffice;
- aplicación móvil.

**Entregable:**

- `docs/04-UI-UX-DESIGN-BRIEF.md`;
- prototipos asociados.

---

## 1.5 Diseño técnico y arquitectura

### 1.5.1 Arquitectura lógica

- Monolito Modular;
- Package by Feature;
- límites entre módulos;
- relaciones permitidas;
- responsabilidades.

### 1.5.2 Módulos candidatos

- `shared`;
- `identity`;
- `institutional`;
- `catalog`;
- `patients`;
- `practitioners`;
- `appointments`;
- `prioritization`;
- `waitlist`;
- `outpatientflow`;
- `notifications`;
- `audit`.

### 1.5.3 TRD

- arquitectura;
- stack;
- decisiones técnicas;
- seguridad;
- integración;
- observabilidad;
- restricciones.

**Entregable:**

- `docs/02-TRD.md`.

### 1.5.4 ADR

Preparar decisiones cuando sean necesarias para:

- autenticación web;
- autenticación móvil;
- agenda;
- double booking;
- waitlist;
- push notifications;
- accesibilidad;
- deployment.

### 1.5.5 Modelo de datos

- entidades;
- relaciones;
- claves;
- índices;
- constraints;
- estados;
- auditoría;
- modelo de agenda.

**Entregable:**

- `docs/05-BACKEND-SCHEMA.md`.

### 1.5.6 Contrato API

- recursos;
- endpoints;
- DTOs;
- errores;
- paginación;
- autorización;
- idempotencia;
- versionamiento.

**Entregable futuro:**

- `contracts/openapi.yaml`.

---

## 1.6 Foundation e infraestructura

### 1.6.1 Backend foundation

- Java 21;
- Spring Boot;
- Maven Wrapper;
- Spring Web;
- Spring Security;
- Validation;
- JPA;
- Flyway;
- Actuator.

### 1.6.2 PostgreSQL

- entorno local;
- versión reproducible;
- migraciones;
- Testcontainers;
- datos sintéticos.

### 1.6.3 Configuración

- `application.yaml`;
- `application-local.yaml`;
- `application-test.yaml`;
- `application-staging.yaml`;
- `application-prod.yaml`.

### 1.6.4 Infra local

- Docker;
- PostgreSQL;
- variables de entorno;
- documentación de arranque.

### 1.6.5 CI inicial

- build;
- unit tests;
- integration tests;
- quality gates.

---

## 1.7 Desarrollo backend

### 1.7.1 Identity

- usuarios;
- roles;
- autenticación;
- autorización;
- auditoría de acceso.

### 1.7.2 Institutional

- información pública;
- contacto;
- políticas;
- medios autorizados.

### 1.7.3 Catalog

- especialidades;
- servicios;
- consultorios.

### 1.7.4 Practitioners

- profesionales;
- especialidades;
- agendas;
- horarios.

### 1.7.5 Appointments

- disponibilidad;
- slots;
- reserva;
- confirmación;
- cancelación;
- reprogramación;
- no-show;
- idempotencia;
- double booking protection.

### 1.7.6 Prioritization

- solicitud;
- revisión;
- aprobación/rechazo;
- auditoría.

### 1.7.7 Waitlist

- ingreso a lista;
- selección de candidatos;
- oferta de cupo;
- aceptación;
- rechazo;
- expiración;
- reasignación segura.

### 1.7.8 Notifications

- in-app;
- push;
- reglas de recordatorio;
- preferencias;
- device registration.

### 1.7.9 Outpatient Flow

- check-in;
- admisión;
- triaje operativo condicional;
- espera;
- llamado;
- consulta;
- cierre;
- tiempos.

### 1.7.10 Audit & Metrics

- auditoría;
- métricas de citas;
- métricas de waitlist;
- tiempos de flujo.

---

## 1.8 Desarrollo web

### 1.8.1 Foundation web

- Next.js;
- React;
- TypeScript;
- Tailwind;
- estructura del proyecto.

### 1.8.2 Portal público

- inicio;
- hospital;
- servicios;
- especialidades;
- ubicación;
- políticas;
- preguntas frecuentes;
- multimedia.

### 1.8.3 Portal de reservas

- disponibilidad;
- especialidades;
- reserva;
- mis citas;
- confirmación;
- cancelación;
- reprogramación;
- lista de espera.

### 1.8.4 Web operativa

- agenda;
- admisión;
- prioridad;
- cola;
- profesional;
- administración;
- auditoría;
- métricas.

### 1.8.5 Accesibilidad web

- WCAG 2.2 AA como referencia;
- teclado;
- foco;
- contraste;
- temas;
- modos de visión de color.

---

## 1.9 Desarrollo móvil

### 1.9.1 Foundation móvil

- React Native;
- Expo;
- TypeScript;
- navegación;
- gestión de configuración.

### 1.9.2 Autenticación móvil

- estrategia definida por ADR;
- almacenamiento seguro;
- logout;
- renovación/reautenticación.

### 1.9.3 Gestión de citas

- disponibilidad;
- reserva;
- mis citas;
- confirmación;
- cancelación;
- reprogramación.

### 1.9.4 Lista de espera

- ingreso;
- ofertas;
- aceptación;
- rechazo;
- expiración.

### 1.9.5 Notificaciones

- registro de dispositivo;
- push;
- in-app;
- deep linking si se aprueba.

### 1.9.6 Seguimiento

- check-in;
- estado de cita;
- estado administrativo del flujo.

### 1.9.7 Accesibilidad móvil

- temas;
- alto contraste;
- modos de visión de color;
- tamaños táctiles;
- etiquetas accesibles.

---

## 1.10 Pruebas y seguridad

### 1.10.1 Unit tests

- reglas de dominio;
- estados;
- validaciones;
- servicios.

### 1.10.2 Integration tests

- PostgreSQL;
- Flyway;
- JPA;
- transacciones.

### 1.10.3 API tests

- endpoints;
- DTOs;
- autorización;
- errores.

### 1.10.4 Concurrency tests

- dos reservas sobre el mismo slot;
- múltiples aceptaciones del mismo cupo;
- reintentos;
- expiración simultánea.

### 1.10.5 Security tests

- roles;
- BOLA/IDOR;
- CSRF;
- CORS;
- sesiones/tokens;
- validación;
- rate limiting;
- exposición de datos.

### 1.10.6 Web E2E

- navegación;
- reserva;
- operaciones internas críticas.

### 1.10.7 Mobile tests

- reserva;
- confirmación;
- cancelación;
- lista de espera;
- notificaciones.

### 1.10.8 Performance

- carga;
- stress;
- tiempos de respuesta;
- baseline.

### 1.10.9 Usabilidad y accesibilidad

- tareas críticas;
- contraste;
- teclado;
- modos de visión de color;
- comprensión de estados.

**Entregables:**

- `docs/07-SECURITY-THREAT-MODEL.md`;
- `docs/08-TEST-STRATEGY.md`.

---

## 1.11 Despliegue y observabilidad

### 1.11.1 Staging

- backend;
- PostgreSQL;
- web;
- build móvil;
- variables de entorno;
- datos sintéticos.

### 1.11.2 CI/CD

- build;
- tests;
- artifacts;
- despliegue controlado.

### 1.11.3 Observabilidad

- Actuator;
- logs;
- health;
- métricas;
- correlation ID;
- monitoreo.

### 1.11.4 Seguridad de despliegue

- HTTPS;
- secretos;
- configuración segura;
- backups;
- rollback.

**Entregables:**

- `docs/09-DEPLOYMENT-PLAN.md`;
- `docs/10-MONITORING-MAINTENANCE.md`.

---

## 1.12 Documentación y entrega académica

### 1.12.1 Documentación académica

- investigación;
- planificación;
- BPMN;
- requerimientos;
- arquitectura;
- pruebas.

### 1.12.2 Documentación técnica

- README;
- API;
- arquitectura;
- setup;
- decisiones;
- manuales necesarios.

### 1.12.3 Evidencias

- screenshots;
- resultados de pruebas;
- CI;
- métricas;
- staging;
- commits y PR.

### 1.12.4 Entregables de curso

- documentos Word cuando se soliciten;
- presentaciones;
- demostración;
- exposición.

---

## 1.13 Cierre y mantenimiento

### 1.13.1 Validación final

- comprobar alcance;
- revisar requisitos;
- cerrar defectos críticos;
- validar documentación.

### 1.13.2 Entrega 100 %

- versión final;
- evidencia;
- presentación;
- repositorio actualizado.

### 1.13.3 Plan de mantenimiento

- backups;
- actualizaciones;
- logs;
- dependencias;
- tareas programadas;
- soporte.

### 1.13.4 Lecciones aprendidas

- decisiones acertadas;
- problemas;
- deuda técnica;
- mejoras;
- trabajo futuro.

---

# 5. Relación de la WBS con el sílabo

## Unidad 1 — Semanas 1 a 4 — Planificación y análisis

| WBS | Tema del sílabo |
| --- | --- |
| 1.1.2 | Lean Canvas |
| 1.1.3 | Project Charter |
| 1.1.4 | WBS |
| 1.1.5 | Gantt |
| 1.2 | Investigación / levantamiento |
| 1.3 | Toma de requerimientos |
| 1.4.4–1.4.6 | Prototipos basados en requerimientos |
| 1.1 | Planificación del proyecto |

## Unidad 2 — Semanas 5 a 8 — Diseño

| WBS | Tema del sílabo |
| --- | --- |
| 1.4.1–1.4.2 | BPM / BPMN |
| 1.5.5 | Diseño de base de datos |
| 1.4.4–1.4.6 | Diseño UX/UI |
| 1.5 | Diseño técnico / arquitectura |
| 1.12 | Documentación |

## Unidad 3 — Semanas 9 a 12 — Desarrollo

| WBS | Tema del sílabo |
| --- | --- |
| 1.5 | Arquitectura |
| 1.6 | Foundation |
| 1.7 | Backend |
| 1.8 | Web |
| 1.9 | Mobile |
| 1.6.5 | Git / CI |
| 1.12.3 | Evidencia de avances |

## Unidad 4 — Semanas 13 a 18 — Pruebas, despliegue y mantenimiento

| WBS | Tema del sílabo |
| --- | --- |
| 1.10 | Pruebas |
| 1.10.5 | Seguridad |
| 1.11 | Despliegue |
| 1.11.3 | Monitoreo |
| 1.13.3 | Mantenimiento |
| 1.13 | Cierre |

---

# 6. Hitos académicos asociados

| Hito | Resultado esperado |
| --- | --- |
| H1 | Semana 01 y reorientación documental completadas |
| H2 | Lean Canvas + Project Charter + WBS aprobados |
| H3 | Gantt y plan de requerimientos completados |
| H4 | Levantamiento y PRD validados |
| H5 | BPMN, base de datos y UI/UX diseñados |
| H6 | Arquitectura y contrato técnico definidos |
| H7 | Foundation y vertical slices iniciales implementados |
| H8 | Desarrollo funcional progresivo demostrado |
| H9 | Pruebas funcionales y de seguridad completadas |
| H10 | Staging, monitoreo y cierre académico completados |

Las semanas exactas se establecerán en `gantt.md`.

---

# 7. Paquetes fuera del MVP

Los siguientes elementos no se convierten en paquetes obligatorios de desarrollo:

- historia clínica completa;
- diagnóstico;
- receta electrónica;
- laboratorio completo;
- farmacia completa;
- imágenes diagnósticas completas;
- hospitalización;
- emergencia;
- cirugía;
- equipos biomédicos;
- interoperabilidad real MINSA/SIS sin autorización;
- microservicios;
- Redis;
- RabbitMQ;
- Kafka.

Podrán incorporarse únicamente mediante cambio de alcance aprobado.

---

# 8. Criterios de finalización de un paquete de trabajo

Un paquete WBS se considera terminado cuando:

- produce el entregable definido;
- cumple sus criterios de aceptación;
- ha sido revisado por al menos otro integrante cuando corresponda;
- no contiene supuestos presentados como hechos;
- mantiene trazabilidad con el roadmap y requisitos;
- está versionado en Git;
- pasa las pruebas requeridas si incluye software;
- queda documentado.

---

# 9. Uso para asignación del equipo

La WBS **no asigna todavía personas específicas**.

La distribución deberá realizarse considerando:

- experiencia;
- carga de trabajo;
- dependencia entre paquetes;
- necesidad de revisión cruzada;
- equilibrio entre documentación, backend, frontend/mobile y pruebas.

La asignación nominal podrá añadirse al Gantt o a una matriz RACI si el docente la solicita.

---

# 10. Próximo paso

La WBS sirve como entrada directa para:

```text
docs/planning/gantt.md
```

El Gantt deberá convertir los paquetes de trabajo relevantes en:

- actividades;
- duración;
- semana de inicio;
- semana de fin;
- dependencias;
- hitos;
- avance esperado.

---

# 11. Estado

- [x] Estructura general definida.
- [x] WBS alineada con el roadmap.
- [x] WBS alineada con las cuatro unidades del sílabo.
- [x] Entregables principales identificados.
- [x] MVP y fuera de alcance diferenciados.
- [x] Hitos académicos identificados.
- [ ] Validación del equipo.
- [ ] Validación docente.
- [ ] Conversión a cronograma Gantt.
- [ ] Asignación de responsables.
