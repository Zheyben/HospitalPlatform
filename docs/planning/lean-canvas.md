# Lean Canvas — Hospital de Huaycán

**Proyecto:** `hospital-platform`  
**Curso:** Curso Integrador I: Sistemas Software  
**Unidad de aprendizaje:** Unidad 1 — Planificación y análisis  
**Caso de estudio propuesto:** Hospital de Huaycán — MINSA / DIRIS Lima Este  
**Estado:** propuesta académica inicial, sujeta a validación docente y levantamiento institucional

> Este Lean Canvas adapta el enfoque tradicional de emprendimiento a un proyecto académico de software para una institución pública de salud.  
> Los bloques de ingresos, costos y ventaja diferencial se interpretan en términos de **sostenibilidad, valor institucional y viabilidad operativa**, no como un modelo comercial de venta del sistema al hospital.
>
> No se consideran como hechos problemas específicos del Hospital de Huaycán que todavía no hayan sido validados mediante fuentes institucionales o levantamiento de campo.

---

## 1. Problema

### Problemas de trabajo a validar

1. **Gestión fragmentada del ciclo de la cita médica**
   - La reserva por sí sola no resuelve todo el proceso.
   - Es necesario estudiar cómo se gestionan disponibilidad, confirmación, cancelación, reprogramación, llegada y finalización de la atención.
   - El proceso real del Hospital de Huaycán debe validarse antes de convertir esta hipótesis en requisito definitivo.

2. **Posible desaprovechamiento de cupos por inasistencias o cancelaciones**
   - Las investigaciones revisadas muestran que el no-show y las cancelaciones pueden generar capacidad no utilizada en servicios ambulatorios.
   - En Huaycán todavía debe determinarse la magnitud real del problema.
   - Se propone estudiar mecanismos de confirmación anticipada, liberación de cupos y lista de espera.

3. **Falta de trazabilidad integral del flujo de consulta externa**
   - El proyecto busca analizar el recorrido desde la programación hasta la atención.
   - Se plantea registrar estados como llegada, admisión, triaje cuando corresponda, espera, llamado, consulta y cierre.
   - La secuencia exacta debe contrastarse con el MAPRO y con el levantamiento en campo.

### Problemas secundarios a estudiar

- dificultad para conocer disponibilidad real de especialidades y horarios;
- necesidad de una experiencia web y móvil accesible;
- necesidad de información institucional pública clara;
- necesidad de proteger datos personales y controlar accesos;
- posibles conflictos de doble reserva;
- falta de indicadores integrados sobre confirmaciones, cancelaciones, no-show, cupos recuperados y tiempos del flujo.

---

## 2. Segmentos de usuarios y actores

### Usuarios principales

#### Paciente

Necesita:

- consultar servicios y especialidades;
- revisar disponibilidad;
- reservar una cita;
- confirmar asistencia;
- cancelar o reprogramar;
- ingresar a lista de espera cuando corresponda;
- recibir una oferta de cupo liberado;
- consultar el estado de su cita;
- recibir recordatorios;
- acceder desde web y aplicación móvil.

#### Personal de admisión / recepción

Necesita:

- consultar citas programadas;
- registrar llegada;
- apoyar al paciente en la gestión de la cita;
- visualizar agenda y disponibilidad según permisos;
- registrar incidencias administrativas;
- mantener trazabilidad de cambios.

#### Profesional de salud

Necesita, dentro del alcance administrativo del MVP:

- visualizar su agenda;
- conocer la cola de pacientes;
- realizar llamado;
- registrar inicio y finalización operativa de la atención.

El MVP no incluye historia clínica electrónica completa ni prescripción.

#### Revisor autorizado de prioridad

Rol propuesto, pendiente de validación institucional.

Necesita:

- revisar solicitudes de prioridad ambulatoria;
- aprobar, rechazar o solicitar información según reglas autorizadas;
- dejar evidencia auditable de la decisión.

No sustituye al triaje de emergencia.

#### Administrador

Necesita:

- gestionar usuarios y roles;
- configurar catálogo de servicios;
- gestionar profesionales;
- configurar consultorios, horarios y agendas;
- revisar métricas;
- consultar auditoría;
- gestionar reglas operativas autorizadas.

### Actores secundarios

- personal de flujo;
- auditor;
- responsables de contenido institucional;
- personal de TI;
- dirección o gestión hospitalaria;
- MINSA / DIRIS Lima Este como contexto institucional;
- servicios externos de notificaciones, si son incorporados posteriormente.

---

## 3. Propuesta única de valor

> **Una plataforma web y móvil que integra la gestión de citas con la trazabilidad del flujo de consulta externa, permitiendo al paciente y al personal conocer y gestionar el ciclo completo de la atención ambulatoria de forma segura, accesible y medible.**

### Valor para el paciente

- mayor claridad sobre sus citas;
- posibilidad de gestionar confirmaciones, cancelaciones y reprogramaciones;
- acceso web y móvil;
- recordatorios;
- posibilidad de aceptar cupos liberados;
- menor dependencia de información dispersa;
- experiencia accesible.

### Valor para el personal

- agenda centralizada;
- mejor visibilidad de estados;
- reducción de conflictos de reserva;
- trazabilidad de cambios;
- información operativa para seguimiento;
- métricas para analizar el proceso.

### Valor académico e institucional

- modelar un proceso ambulatorio digital;
- evaluar indicadores con datos sintéticos o agregados autorizados;
- demostrar consistencia, seguridad, accesibilidad y concurrencia;
- construir una arquitectura escalable sin sobreingeniería.

---

## 4. Solución

La propuesta académica se divide en cuatro superficies.

### 4.1 Portal público institucional

Funciones candidatas:

- información del Hospital de Huaycán;
- ubicación y contacto;
- servicios y especialidades verificadas;
- políticas y orientaciones;
- preguntas frecuentes;
- contenido multimedia autorizado;
- acceso visible al portal de reservas.

### 4.2 Portal web de reservas

Funciones candidatas:

- consultar especialidades;
- consultar disponibilidad;
- reservar cita;
- confirmar asistencia;
- cancelar;
- reprogramar;
- solicitar ingreso a lista de espera;
- aceptar o rechazar ofertas de cupos;
- consultar estado de prioridad cuando aplique.

### 4.3 Web operativa interna

Funciones candidatas:

- agendas;
- disponibilidad;
- admisión;
- revisión de prioridad;
- seguimiento de flujo;
- cola de pacientes;
- administración;
- auditoría;
- métricas.

### 4.4 Aplicación móvil del paciente

Funciones candidatas:

- autenticación;
- disponibilidad;
- reserva;
- mis citas;
- confirmación;
- cancelación;
- reprogramación;
- lista de espera;
- ofertas de cupos;
- notificaciones;
- check-in cuando corresponda;
- seguimiento del estado administrativo.

### Funciones transversales

- autenticación y autorización;
- auditoría;
- accesibilidad;
- notificaciones;
- seguridad y privacidad;
- consistencia transaccional;
- prevención de doble reserva;
- prevención de doble aceptación de un cupo liberado.

---

## 5. Canales

### Canales digitales propuestos

- portal web público;
- portal web de reservas;
- aplicación móvil;
- notificaciones dentro de la aplicación;
- push notifications;
- correo electrónico si se aprueba.

### Canales futuros o de integración

- WhatsApp;
- SMS;
- sistemas digitales del MINSA;
- servicios institucionales externos.

Estos canales no se incorporarán sin validar proveedor, autorización, costos y reglas institucionales.

### Canales de levantamiento y validación

Durante la fase de investigación:

- entrevistas autorizadas;
- observación del proceso;
- revisión documental;
- MAPRO;
- Cartera de Servicios;
- Plan Cero Colas;
- fuentes oficiales del Hospital de Huaycán, DIRIS Lima Este, MINSA y PRONIS.

---

## 6. Métricas clave

No se establecen metas numéricas definitivas sin una línea base válida.

### Gestión de citas

- citas programadas;
- citas confirmadas;
- tasa de confirmación;
- cancelaciones;
- reprogramaciones;
- no-show;
- ocupación efectiva de agenda.

### Recuperación de capacidad

- cupos liberados;
- cupos ofrecidos;
- ofertas aceptadas;
- ofertas rechazadas;
- ofertas expiradas;
- cupos reasignados;
- tiempo desde liberación hasta reasignación;
- tasa de recuperación de cupos.

### Flujo ambulatorio

- tiempo llegada → admisión;
- tiempo admisión → triaje, cuando aplique;
- tiempo triaje → llamado;
- tiempo llegada → consulta;
- tiempo total de permanencia;
- pacientes por etapa del flujo.

### Experiencia y calidad

- satisfacción en pruebas académicas;
- tasa de éxito en tareas;
- errores de interacción;
- accesibilidad;
- comprensión de estados;
- uso de web y móvil.

### Calidad técnica

- conflictos de doble reserva;
- intentos concurrentes controlados;
- errores de API;
- disponibilidad del sistema;
- tiempos de respuesta;
- incidencias de seguridad;
- fallos de notificación.

---

## 7. Ventaja diferencial

La ventaja diferencial propuesta no consiste únicamente en permitir reservar una cita.

El proyecto busca integrar:

```text
CITA
  +
CONFIRMACIÓN
  +
CANCELACIÓN / REPROGRAMACIÓN
  +
RECUPERACIÓN DE CUPOS
  +
LISTA DE ESPERA
  +
PRIORIZACIÓN VALIDADA
  +
TRAZABILIDAD DEL FLUJO AMBULATORIO
  +
WEB + MOBILE
  +
ACCESIBILIDAD
  +
SEGURIDAD
  +
MÉTRICAS
```

### Diferenciadores académicos

- enfoque en el ciclo completo de la cita;
- recuperación de cupos con aceptación explícita;
- protección contra concurrencia y doble reserva;
- separación entre estado administrativo de la cita y flujo ambulatorio;
- priorización ambulatoria sujeta a validación autorizada;
- accesibilidad desde el diseño;
- datos sintéticos;
- arquitectura de monolito modular;
- posibilidad de evolución hacia interoperabilidad sin asumir integraciones inexistentes.

### Restricción

Esta ventaja es **propuesta**. No debe presentarse como una superioridad demostrada frente a los sistemas actuales del Hospital de Huaycán hasta realizar el levantamiento correspondiente.

---

## 8. Estructura de costos

El proyecto no plantea todavía un modelo comercial. Este bloque identifica costos y recursos necesarios para construir, probar y eventualmente operar el prototipo académico.

### Desarrollo

- tiempo del equipo;
- diseño UX/UI;
- desarrollo backend;
- desarrollo web;
- desarrollo móvil;
- pruebas;
- documentación;
- mantenimiento del repositorio.

### Infraestructura

- hosting del backend;
- hosting web;
- PostgreSQL administrado para staging, si se aprueba;
- almacenamiento;
- dominio académico, si se requiere;
- certificados HTTPS;
- servicios de observabilidad.

### Servicios externos

- proveedor de push notifications;
- correo;
- WhatsApp/SMS en una fase futura;
- herramientas de monitoreo si se requieren.

### Seguridad y calidad

- pruebas de seguridad;
- análisis de dependencias;
- backups;
- monitoreo;
- control de logs;
- gestión de secretos.

### Costos no monetarios

- capacitación;
- adaptación organizacional;
- levantamiento de procesos;
- validación con usuarios;
- mantenimiento de contenidos;
- soporte.

---

## 9. Fuentes de valor y sostenibilidad

En un proyecto académico para una institución pública, este bloque sustituye el enfoque tradicional de «fuentes de ingresos».

### Valor institucional potencial

- mejor utilización de la capacidad disponible;
- mayor trazabilidad;
- reducción de tareas repetitivas;
- recuperación de cupos;
- información para toma de decisiones;
- mejora de experiencia del paciente;
- evidencia mediante métricas.

### Sostenibilidad técnica

- arquitectura modular;
- una sola API para web y móvil;
- PostgreSQL;
- migraciones versionadas;
- automatización de pruebas;
- CI/CD;
- documentación técnica;
- monitoreo;
- dependencia limitada de componentes externos.

### Sostenibilidad operativa

Requeriría validar:

- responsable institucional del sistema;
- responsables de configuración de agendas;
- soporte;
- administración de usuarios;
- actualización de contenido;
- gestión de dispositivos;
- continuidad del servicio;
- respaldo y recuperación.

### Modelo económico

**No definido en esta etapa.**

El proyecto no presupone:

- venta de licencias al Hospital de Huaycán;
- suscripción;
- pago de pacientes;
- contrato con MINSA;
- adopción institucional.

Cualquier análisis económico futuro deberá desarrollarse como escenario académico y no como acuerdo real.

---

# 10. Lean Canvas resumido

| Bloque | Síntesis |
| --- | --- |
| **Problema** | Gestión del ciclo de citas, posible desaprovechamiento de cupos y trazabilidad del flujo ambulatorio; todo sujeto a validación en Huaycán. |
| **Segmentos** | Paciente, admisión, profesional, revisor de prioridad, administrador, auditor y personal de flujo. |
| **Propuesta de valor** | Gestión integrada de cita + flujo ambulatorio mediante web y móvil, con seguridad, accesibilidad y métricas. |
| **Solución** | Portal público, reservas web, web operativa y app móvil conectados al mismo backend. |
| **Canales** | Web, móvil, in-app, push, email opcional; integraciones futuras. |
| **Métricas** | Confirmaciones, no-show, cancelaciones, reprogramaciones, cupos recuperados, tiempos del flujo, usabilidad y calidad técnica. |
| **Ventaja diferencial** | Integración del ciclo completo de la cita con recuperación de cupos, trazabilidad, concurrencia segura y accesibilidad. |
| **Costos** | Desarrollo, infraestructura, servicios externos, seguridad, soporte y mantenimiento. |
| **Valor/sostenibilidad** | Mejor uso de capacidad, trazabilidad, información operativa y sostenibilidad técnica; sin modelo comercial definido. |

---

# 11. Hipótesis que deben validarse

El Lean Canvas no convierte estas afirmaciones en hechos. Deben investigarse:

- existencia y magnitud real del no-show;
- causas de inasistencia;
- mecanismos actuales de recordatorio;
- manejo actual de cancelaciones;
- reutilización actual de cupos;
- existencia de lista de espera;
- sistemas de citas utilizados actualmente;
- procesos exactos de admisión y archivo;
- uso real de triaje en consulta externa;
- criterios de prioridad ambulatoria;
- actores autorizados;
- especialidades vigentes;
- tiempos de espera;
- capacidad de agenda;
- necesidades reales de pacientes y personal;
- restricciones de conectividad;
- nivel de adopción digital;
- necesidades de accesibilidad.

---

# 12. Relación con los documentos del proyecto

Este Lean Canvas toma como referencia:

```text
docs/00-PROJECT-ROADMAP.md
docs/research/semana-01/problema-investigacion.md
docs/research/semana-01/marco-teorico.md
docs/research/semana-01/fuentes.md
```

Los resultados posteriores del levantamiento pueden modificar este documento.

Orden de trabajo propuesto para la Unidad 1:

```text
Lean Canvas
    ↓
Project Charter
    ↓
WBS
    ↓
Gantt
    ↓
Toma de requerimientos
    ↓
Instrumentos de levantamiento
    ↓
Prototipos iniciales
    ↓
PRD
```

---

# 13. Estado

- [x] Problema inicial identificado.
- [x] Segmentos de usuarios propuestos.
- [x] Propuesta de valor inicial definida.
- [x] Solución conceptual definida.
- [x] Canales candidatos definidos.
- [x] Métricas candidatas definidas.
- [x] Costos y sostenibilidad identificados.
- [ ] Validación docente.
- [ ] Validación mediante levantamiento de campo.
- [ ] Actualización del Canvas con resultados reales.
