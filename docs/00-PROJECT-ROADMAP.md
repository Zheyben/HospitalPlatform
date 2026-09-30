# Roadmap técnico y funcional maestro --- Hospital de Huaycán

**Proyecto:** `hospital-platform`\
**Curso:** Curso Integrador I: Sistemas Software\
**Caso de estudio propuesto:** Hospital de Huaycán --- MINSA / DIRIS
Lima Este\
**Estado del documento:** guía maestra viva de investigación, producto y
arquitectura\
**Fecha de actualización:** Versión de revisión documental\
**Información institucional y técnica verificada al:** 18 de agosto de
2026

> **Principio de trabajo:** INVESTIGAR → VALIDAR → DISEÑAR → IMPLEMENTAR
> → PROBAR → MEDIR → ESCALAR.
>
> **Decisión de alcance:** la solución deberá incluir **portal público
> institucional + portal web de reservas + web
> administrativa/operativa + aplicación móvil para pacientes** dentro
> del MVP académico. La app móvil deja de considerarse una extensión
> opcional futura.
>
> Este proyecto es académico. No representa una solución oficial del
> Hospital de Huaycán, MINSA, DIRIS Lima Este o PRONIS y no presupone
> acceso a sistemas internos, historias clínicas ni datos personales
> reales.

------------------------------------------------------------------------

## 1. Propósito del roadmap

Este documento redefine la dirección del proyecto después de descartar
el Hospital de Vitarte como caso inicial y reorientar el estudio hacia
el **Hospital de Huaycán**, un establecimiento del MINSA en Lima Este
con una cartera de servicios de categoría II-1 y con un proyecto de
nueva infraestructura cuyo expediente técnico fue aprobado por PRONIS en
febrero de 2026 \[I1\]\[I2\].

La reorientación no consiste en sustituir únicamente el nombre de una
institución por otra. Cambia el contexto operativo, el ecosistema
institucional y el valor que debe aportar el software. El proyecto deja
de plantearse como una simple "web para sacar citas" y pasa a enfocarse
en una **plataforma web y móvil de gestión de citas y trazabilidad del
flujo de consulta externa**, capaz de modelar el recorrido operativo del
paciente desde la programación hasta la finalización de la atención
ambulatoria. La app móvil será el canal principal de autoservicio del
paciente; la web se dividirá en un portal público institucional, un
portal específico de reservas y una superficie operativa interna para el
personal hospitalario.

El roadmap sirve para:

-   orientar al equipo de 7 integrantes;
-   separar hechos verificados de hipótesis de diseño;
-   definir el alcance del MVP;
-   ordenar investigación, PRD, BPMN, UI/UX, TRD, modelo de datos y
    programación;
-   impedir overengineering;
-   mantener seguridad y privacidad desde el diseño;
-   definir cuándo incorporar tecnologías futuras;
-   establecer pruebas y gates antes de avanzar de fase;
-   ofrecer una referencia técnica coherente con la futura
    infraestructura del Hospital de Huaycán sin afirmar que el software
    será adoptado oficialmente.

------------------------------------------------------------------------

## 2. Lenguaje de decisión

  ---------------------------------------------------------------------
  Etiqueta                           Significado
  ---------------------------------- ----------------------------------
  **CONFIRMADO**                     Existe en el repositorio o está
                                     sustentado por una fuente
                                     institucional/técnica verificable.

  **PROPUESTO**                      Decisión de diseño recomendada
                                     para el proyecto, todavía
                                     revisable.

  **PENDIENTE DE VALIDACIÓN          Requiere confirmación académica
  DOCENTE**                          tras la reorientación a Huaycán.

  **PENDIENTE DE LEVANTAMIENTO EN    Debe verificarse con el hospital,
  CAMPO**                            su documentación o personal
                                     autorizado.

  **FUTURO**                         No pertenece al MVP y solo se
                                     incorporará si existe necesidad
                                     real.

  **FUERA DEL MVP**                  Capacidad expresamente excluida de
                                     la primera versión.

  **NO AUTORIZADO**                  No debe realizarse por razones
                                     académicas, de privacidad o de
                                     alcance.
  ---------------------------------------------------------------------

------------------------------------------------------------------------

## 3. Reorientación institucional

### 3.1 Caso de estudio

**Hospital de Huaycán --- MINSA / DIRIS Lima Este.**

Estado en el proyecto: **PROPUESTO PARA VALIDACIÓN FINAL CON EL
DOCENTE**.

La DIRIS Lima Este actualizó en septiembre de 2025 la **Cartera de
Servicios de Salud del Hospital de Huaycán, categoría II-1** \[I3\]. El
Hospital de Huaycán también publica un Manual de Procedimientos del
Servicio de Consulta Externa y Hospitalización \[I4\] y mantiene
formalmente equipos vinculados a Selección, Admisión y Archivo \[I5\].

### 3.2 Nueva infraestructura hospitalaria

El 11 de febrero de 2026 PRONIS informó la aprobación del expediente
técnico del proyecto de mejoramiento y ampliación del Hospital de
Huaycán, con inversión superior a S/ 460 millones \[I2\]. La información
más reciente disponible describe, entre otros componentes:

-   infraestructura hospitalaria superior a 31 000 m²;
-   22 consultorios médicos;
-   7 tópicos de urgencias y emergencias;
-   3 salas de parto multifuncionales;
-   3 salas de operaciones;
-   equipamiento diagnóstico de mayor capacidad;
-   un plan de contingencia asociado a la ejecución del proyecto.

**Regla documental:** para especificaciones de diseño se priorizan las
cifras del expediente técnico 2026. Las cifras de la viabilidad
publicada en 2021 \[I1\] se conservan únicamente como antecedente
histórico porque el proyecto evolucionó entre preinversión y expediente
técnico.

### 3.3 Plan Cero Colas

El Hospital de Huaycán cuenta con un **Plan Cero Colas 2026** y una
versión V2 aprobada en mayo de 2026 \[I6\]\[I7\]. Además, el Plan Cero
Colas 2024 del propio hospital documentó evaluación del proceso de
atención en consulta externa \[I8\].

Esto convierte los tiempos de espera, la coordinación de citas y el
flujo ambulatorio en un contexto institucional real que puede orientar
el proyecto. No obstante, el equipo **no debe inventar valores, causas o
cuellos de botella específicos de 2026** hasta realizar el levantamiento
correspondiente.

### 3.4 Ecosistema MINSA y diferenciación

El MINSA ya dispone de mecanismos de programación de citas en línea o
por canales digitales en distintos establecimientos, y DIRIS Lima Este
ha desplegado citas en línea para centros de salud de su jurisdicción
\[I9\]\[I10\].

Por ello, el valor diferencial del proyecto **no será replicar un portal
nacional de reserva**. La propuesta se concentrará en:

1.  gestión hospitalaria de agendas y disponibilidad;
2.  reserva, confirmación, cancelación y reprogramación;
3.  check-in o registro de llegada;
4.  trazabilidad del flujo de consulta externa;
5.  cola/sala de espera digital;
6.  estados operativos de triaje cuando corresponda;
7.  llamado e ingreso a consulta;
8.  finalización y seguimiento administrativo;
9.  métricas de tiempos y utilización;
10. integración futura con sistemas nacionales mediante adaptadores,
    nunca sustituyéndolos por suposición.

------------------------------------------------------------------------

## 4. Problema y oportunidad de investigación

### 4.1 Formulación de trabajo

**Problema general propuesto:**

> ¿De qué manera una plataforma web y móvil puede apoyar la gestión de
> citas, reducir el desaprovechamiento de cupos por inasistencias
> evitables y mejorar la trazabilidad del flujo de consulta externa del
> Hospital de Huaycán --- MINSA, considerando sus procesos actuales y el
> escenario de su futura infraestructura hospitalaria?

Estado: **PENDIENTE DE VALIDACIÓN DOCENTE Y LEVANTAMIENTO EN CAMPO**.

### 4.2 Qué no se afirma todavía

No se afirma como hecho que actualmente existan:

-   colas de una duración específica;
-   determinado porcentaje de no-show;
-   pérdida de citas o fichas;
-   fallos de un sistema informático particular;
-   una secuencia exacta de admisión/triaje/archivo;
-   carencia de citas digitales;
-   determinado número de pacientes diarios;
-   un patrón específico de ausentismo médico;
-   problemas de privacidad o seguridad concretos.

Estos elementos deben medirse o verificarse.

### 4.3 Oportunidad del proyecto

El contexto del nuevo hospital permite construir un **modelo académico
de operación digital** que pueda simular y evaluar cómo funcionaría la
gestión ambulatoria en una infraestructura con mayor capacidad, sin
afirmar que será la solución que PRONIS o MINSA implementarán.

La plataforma podrá funcionar como:

-   prototipo funcional;
-   simulador de agendas y consultorios;
-   sistema de demostración para pruebas con datos sintéticos;
-   herramienta académica de medición de flujo;
-   referencia de arquitectura para una futura solución institucional.

------------------------------------------------------------------------

## 5. Objetivos

### 5.1 Objetivo general

**Diseñar, desarrollar, implementar y evaluar una plataforma web y móvil
orientada a apoyar la gestión de citas médicas y la trazabilidad del
flujo de consulta externa del Hospital de Huaycán --- MINSA,
incorporando un portal público institucional, un portal especializado de
reservas, confirmación anticipada de asistencia, cancelación,
reprogramación, recuperación de cupos liberados, gestión de lista de
espera y priorización de citas validada por personal autorizado, además
de criterios de seguridad, privacidad, accesibilidad, usabilidad,
consistencia transaccional, observabilidad e interoperabilidad.**

### 5.2 Objetivos específicos

**OE1 --- Levantamiento y modelado.** Analizar y modelar el proceso
actual de consulta externa y gestión de citas del Hospital de Huaycán
mediante fuentes institucionales y levantamiento autorizado,
identificando actores, actividades, canales, reglas, especialidades,
tiempos, incidencias, restricciones e indicadores de línea base.

**OE2 --- Diseño del proceso digital.** Definir el proceso TO-BE, los
requerimientos funcionales y no funcionales, los flujos de experiencia
de usuario accesibles, el modelo de datos y el contrato API necesarios
para gestionar citas y trazabilidad ambulatoria de manera segura y
medible.

**OE3 --- Implementación del núcleo web y móvil.** Implementar
progresivamente los módulos de identidad, contenido institucional,
catálogo de servicios, pacientes, profesionales, agendas,
disponibilidad, citas, priorización validada, confirmaciones, lista de
espera, notificaciones y flujo ambulatorio, proporcionando un portal
público del hospital, un portal específico de reservas, una web
operativa para el personal y una aplicación móvil para el paciente, con
consistencia transaccional y prevención de conflictos como la doble
reserva o la aceptación simultánea de un mismo cupo liberado.

**OE4 --- Seguridad y privacidad.** Aplicar mecanismos de autenticación,
autorización por rol y por objeto, gestión segura de sesiones,
validación, minimización de datos, auditoría, protección frente a abuso
y controles alineados con OWASP para aplicaciones web y APIs.

**OE5 --- Validación y aprovechamiento de agenda.** Evaluar la solución
mediante pruebas unitarias, integración, API, concurrencia, seguridad,
end-to-end, usabilidad y rendimiento, complementadas con métricas como
tasa de confirmación, no-show, cancelación anticipada, recuperación y
reasignación de cupos, aceptación de ofertas de lista de espera, uso de
prioridades validadas, ocupación efectiva de agenda y tiempos del flujo
ambulatorio definidos a partir del levantamiento institucional.

**OE6 --- Información institucional y acceso digital.** Diseñar una
experiencia pública web que concentre información verificable del
hospital ---ubicación, servicios, especialidades, políticas, canales de
contacto y contenido multimedia autorizado--- y conduzca de forma clara
al portal de reservas, manteniendo separados el contenido institucional,
la gestión de citas y las funciones internas del personal.

------------------------------------------------------------------------

## 6. Alcance del MVP

### 6.1 Capacidades incluidas

El MVP se concentrará en **consulta externa programada**, con **web
operativa para el personal + aplicación móvil para pacientes**.

  ---------------------------------------------------------------------
  Capacidad                          MVP
  ---------------------------------- ----------------------------------
  Autenticación y roles              Sí

  Portal público institucional del   Sí
  hospital                           

  Portal web específico de reservas  Sí

  Web administrativa/operativa       Sí

  Aplicación móvil del paciente      Sí, obligatoria

  Catálogo de                        Sí
  especialidades/servicios           

  Profesionales                      Sí

  Consultorios físicos configurables Sí

  Agendas y horarios                 Sí

  Disponibilidad/slots               Sí

  Reserva de cita                    Sí

  Solicitud de prioridad para cita,  Sí, con reglas institucionales
  sujeta a validación                pendientes

  Revisión/validación de prioridad   Sí, sin sustituir triaje de
  por rol autorizado                 emergencia

  Confirmación anticipada de         Sí
  asistencia                         

  Recordatorio T-7 días              Sí como regla inicial configurable

  Recordatorios adicionales          Configurables según reglas
                                     aprobadas

  Reprogramación                     Sí

  Cancelación                        Sí

  Liberación inmediata del cupo      Sí
  cancelado                          

  Lista de espera                    Sí

  Oferta de cupos liberados          Sí

  Aceptación/rechazo de oferta desde Sí
  móvil                              

  Gestión de no-show                 Sí

  Push notifications                 Sí, con proveedor a definir

  Preferencias de visualización      Sí
  (claro/oscuro/sistema)             

  Alto contraste                     Sí

  Modos de apoyo para protanopia,    Sí, como preferencia de
  deuteranopia y tritanopia          accesibilidad

  Check-in/llegada                   Sí

  Estado de admisión                 Sí

  Estado de triaje no clínico        Sí, si el proceso real lo requiere

  Sala de espera/cola virtual        Sí

  Llamado a consultorio              Sí

  Estado de consulta                 Sí

  Finalización del flujo             Sí

  Auditoría                          Sí

  Información pública: ubicación,    Sí
  contacto, políticas y servicios    

  Galería de imágenes/videos         Sí, contenido verificado o propio
  autorizados                        

  Especialidades disponibles para    Sí
  reserva en tiempo real             

  Métricas operativas                Sí

  Historia clínica completa          No

  Diagnóstico médico                 No

  Prescripción electrónica           No

  Farmacia completa                  No

  Laboratorio clínico completo       No

  Imágenes diagnósticas completas    No

  Hospitalización                    No

  Emergencia                         No

  Cirugía                            No

  Equipos biomédicos                 No
  ---------------------------------------------------------------------

**Regla:** la notificación de una semana antes (`T-7 días`) se toma como
criterio inicial recomendado por el docente, pero deberá ser
configurable y validarse durante el levantamiento. El sistema no
prometerá eliminar el no-show al 100 %; buscará **reducir inasistencias
evitables y recuperar capacidad de agenda**.

### 6.2 Datos clínicos de triaje

El MVP **no requiere almacenar signos vitales, síntomas, diagnósticos ni
antecedentes clínicos**. Triaje no forma parte de la máquina aprobada para el
incremento backend FASE 5.11 y no se representa mediante valores adicionales de
`FlowStage`. Si el proceso real lo requiere, se diseñará como una capacidad
futura mediante una decisión de dominio separada.

La incorporación de información clínica requeriría un alcance, threat
model, modelo de consentimiento, retención y controles adicionales; por
ello queda **FUERA DEL MVP** salvo decisión posterior documentada.

### 6.3 Expansión futura: espacio clínico del profesional y documentos firmados

Estado: **FUTURO / FUERA DEL MVP de citas y flujo ambulatorio.**

La evolución del sistema podrá incorporar, para el profesional de salud,
un **espacio clínico dentro de la web operativa** que permita documentar
la atención según la especialidad. Esta capacidad no debe confundirse
con una nota libre sin controles ni con un simple generador de PDF. Si
se implementa, deberá tratarse como un módulo clínico formal, sujeto a
normativa, seguridad reforzada, auditoría y definición institucional.

Capacidades candidatas futuras:

-   plantillas configurables por especialidad y tipo de atención;
-   nota clínica estructurada y observaciones profesionales;
-   recomendaciones e indicaciones al paciente;
-   prescripción de medicamentos cuando el profesional y el flujo estén
    autorizados;
-   órdenes de exámenes, procedimientos o dispositivos médicos cuando
    corresponda;
-   generación de una **representación PDF** para visualización o
    impresión;
-   firma electrónica o firma digital conforme al marco institucional
    aplicable;
-   historial de versiones, autor, fecha/hora, trazabilidad y auditoría;
-   disponibilidad posterior del documento firmado para el paciente, si
    las reglas institucionales lo permiten.

**Regla de fuente de verdad:** el PDF no será la fuente primaria del
dato clínico. La información debe almacenarse de forma estructurada y
versionada; el PDF será una representación derivada para consulta,
impresión o intercambio.

**Cobertura SIS:** una receta, orden o PDF generado por el sistema **no
debe considerarse por sí mismo autorización de cobertura o
financiamiento del SIS**. Cualquier flujo de elegibilidad, cobertura,
autorización o reconocimiento de prestaciones/medicamentos deberá
modelarse solo después de validar el proceso institucional y, si existe,
integrar los sistemas o servicios oficiales correspondientes.

Arquitectura candidata futura:

``` text
WEB PROFESIONAL
  -> nota / indicación / prescripción / orden
  -> validación clínica y de permisos
  -> firma electrónica/digital cuando corresponda
  -> documento estructurado + PDF derivado
  -> integración futura SIHCE / receta electrónica / SIS, si existe autorización

MOBILE PACIENTE (futuro)
  -> visualizar documentos firmados autorizados
  -> descargar copia PDF
  -> nunca editar documentos clínicos
```

El proyecto no intentará reemplazar el SIHCE del MINSA ni afirmar
interoperabilidad con SIS, DIGEMID u otros sistemas sin documentación
oficial, autorización y pruebas de integración.

------------------------------------------------------------------------

## 7. Flujo ambulatorio de referencia

### 7.1 Hipótesis AS-IS para validar

El Hospital de Huaycán publica un MAPRO de Consulta Externa y
Hospitalización y mantiene funciones formales de Selección, Admisión y
Archivo \[I4\]\[I5\]. Con base en ese marco, se utilizará la siguiente
secuencia **solo como hipótesis de discovery**:

``` text
Solicitud / referencia / cita existente
        |
        v
Orientación / identificación
        |
        v
Selección - Admisión
        |
        v
Archivo / vinculación del registro
        |
        v
Check-in o confirmación de llegada
        |
        v
Triaje (solo cuando corresponda)
        |
        v
Sala de espera
        |
        v
Llamado
        |
        v
Consultorio
        |
        v
Finalización administrativa
        |
        +--> control / nueva cita
        +--> apoyo diagnóstico (futuro/integración)
        +--> farmacia (futuro/integración)
        +--> referencia (futuro/integración)
```

**Gate:** esta secuencia no se convertirá en BPMN definitivo hasta
contrastarla con el MAPRO y el levantamiento presencial.

### 7.2 TO-BE digital objetivo

``` text
Paciente (app móvil) / personal (web)
      |
      v
Catálogo + disponibilidad
      |
      v
Reserva de cita
      |
      v
Recordatorios configurables
(T-7 días como regla inicial)
      |
      v
Confirmar / cancelar / reprogramar
      |
      +------------------------------+
      |                              |
      | confirma                     | cancela/no puede asistir
      v                              v
Cita confirmada               Slot liberado
      |                              |
      |                              v
      |                        Lista de espera
      |                              |
      |                              v
      |                        Oferta de cupo
      |                              |
      |                         acepta/rechaza
      |                              |
      +------------------------------+
                     |
                     v
              Cita vigente
                     |
                     v
              Check-in de llegada
                     |
                     v
          Flujo ambulatorio por estados
                     |
          +----------+-----------+
          |          |           |
       admisión   triaje      espera/llamado
                  operativo       |
                  (si aplica)     v
                              consulta -> cierre
                     |
                     v
        Métricas + auditoría + seguimiento
```

### 7.3 Métricas candidatas

No se fijan metas numéricas sin línea base. El software debe poder
medir, cuando la institución o el escenario de prueba lo permitan:

-   tiempo solicitud → cita confirmada;
-   tiempo llegada → admisión;
-   tiempo admisión → triaje;
-   tiempo triaje → llamado;
-   tiempo llegada → consulta;
-   duración del estado de espera;
-   duración de consulta si se autoriza como métrica operativa;
-   tiempo total de permanencia;
-   porcentaje de confirmación de asistencia;
-   porcentaje de no-show;
-   porcentaje de cancelaciones anticipadas y tardías;
-   porcentaje de reprogramaciones;
-   cantidad de solicitudes de prioridad;
-   porcentaje de prioridades aprobadas/rechazadas;
-   tiempo de revisión de una solicitud de prioridad;
-   ocupación de slots;
-   cantidad de slots liberados;
-   tasa de recuperación de cupos liberados;
-   tasa de reasignación efectiva de cupos;
-   tiempo medio desde liberación hasta reasignación;
-   tasa de aceptación/rechazo/expiración de ofertas de lista de espera;
-   citas recuperadas gracias a confirmación/cancelación anticipada;
-   utilización de consultorios;
-   citas completadas por período;
-   errores/conflictos de reserva;
-   satisfacción/usabilidad en pruebas académicas.

------------------------------------------------------------------------

## 8. Cartera de servicios y especialidades

### 8.1 Fuente de verdad

La DIRIS Lima Este actualizó la Cartera de Servicios de Salud del
Hospital de Huaycán en 2025 \[I3\]. Esa cartera y sus anexos deben
considerarse la **fuente institucional prioritaria** para construir el
catálogo real del sistema.

El ASIS/ASISHO 2022 del Hospital de Huaycán documenta históricamente
especialidades como cardiología, neumología, nefrología, neurología,
gastroenterología, geriatría, urología, otorrinolaringología y
pediatría, entre otras \[I11\]. Comunicaciones institucionales de 2025
también evidencian atención en medicina general, pediatría, odontología,
obstetricia y ginecología \[I12\], y en 2026 el hospital informó
fortalecimiento de cardiología con equipo Holter \[I13\].

**Importante:** esta evidencia sirve para discovery, pero no debe
sustituir el anexo oficial 2025. La lista exacta y vigente de
especialidades, procedimientos y servicios debe verificarse antes de
cargar datos definitivos.

### 8.2 Regla de diseño

Las especialidades **no se codificarán como `enum` Java**.

Se modelarán como catálogo de base de datos para permitir cambios sin
recompilar:

``` text
Specialty
- id
- code
- name
- active

ServiceOffering
- id
- specialty_id
- code
- name
- service_type
- requires_triage
- default_duration_minutes
- active
```

También se propondrá:

``` text
ConsultingRoom
- id
- code
- name
- floor_or_zone
- active
```

La cifra de 22 consultorios del expediente técnico 2026 se usará para
**escenarios de simulación**, no como una constante del código \[I2\].

------------------------------------------------------------------------

## 9. Arquitectura objetivo

### 9.1 Decisión

**PROPUESTO: Monolito modular.**

No microservicios inicialmente.

### 9.2 Justificación

-   equipo académico de 7 integrantes;
-   menor costo operacional;
-   una sola unidad desplegable;
-   transacciones locales fuertes;
-   mejor soporte para consistencia de citas;
-   pruebas de integración más simples;
-   despliegue y observabilidad iniciales más sencillos;
-   posibilidad de extraer módulos solo cuando las métricas lo
    justifiquen.

### 9.2.1 Superficies de la solución

La plataforma seguirá siendo una sola solución lógica, aunque tenga
varias experiencias de usuario:

``` text
PORTAL PÚBLICO WEB -----------+
PORTAL DE RESERVAS WEB -------+
WEB OPERATIVA INTERNA --------+--> REST API /api/v1 --> Spring Boot modular --> PostgreSQL
APP MÓVIL PACIENTE -----------+
```

Reglas:

-   no crear un backend independiente por cada cliente;
-   separar endpoints públicos, autenticados y administrativos por
    autorización y contrato;
-   el portal público puede consumir información institucional/cacheable
    sin exponer datos internos;
-   el portal de reservas y la app móvil consumen la misma lógica de
    disponibilidad, prioridades, citas y lista de espera;
-   la web interna exige permisos de personal;
-   PostgreSQL nunca se expone directamente a los clientes.

### 9.3 Package by Feature

Package raíz confirmado:

``` text
com.integrador.salud.api
```

Módulos candidatos para el MVP:

``` text
com.integrador.salud.api
├── shared
├── identity
├── institutional
├── catalog
├── patients
├── practitioners
├── appointments
├── prioritization
├── waitlist
├── outpatientflow
├── notifications
└── audit
```

Módulos futuros:

``` text
integrations
reports
```

Fuera del MVP:

``` text
pharmacy
laboratory
imaging
hospitalization
emergency
clinical
biomedical
```

Dentro de cada feature, cuando la complejidad lo requiera:

``` text
domain
application
infrastructure
web
```

No se crearán paquetes vacíos únicamente para imitar una arquitectura.

### 9.4 Spring Modulith

Estado: **FUTURO**.

Se evaluará cuando existan módulos reales y dependencias significativas.
Podrá ayudar a:

-   verificar límites modulares;
-   detectar dependencias indebidas;
-   probar módulos;
-   documentar estructura;
-   gestionar eventos internos.

No se agrega antes de tener un problema que resolver.

------------------------------------------------------------------------

## 10. Inventario de clases candidatas

Las clases no se crean por cuota. La siguiente lista es de
planificación.

### 10.1 Dominio y catálogos

  ---------------------------------------------------------------------
  Módulo                             Clases candidatas
  ---------------------------------- ----------------------------------
  identity                           `UserAccount`, `Role`,
                                     `UserStatus`, `MobileSession` o
                                     equivalente según ADR

  institutional                      `HospitalProfile`, `PublicPolicy`,
                                     `MediaAsset`, `ContactChannel`

  catalog                            `Specialty`, `ServiceOffering`,
                                     `ConsultingRoom`

  patients                           `Patient`,
                                     `PatientContactPreference`

  practitioners                      `Practitioner`,
                                     `PractitionerSchedule`

  appointments                       `Appointment`, `AvailabilitySlot`,
                                     `AppointmentStatus`,
                                     `AppointmentConfirmation`

  prioritization                     `PriorityRequest`,
                                     `PriorityReview`,
                                     `PriorityReviewStatus`,
                                     `PriorityDecision`

  waitlist                           `WaitlistEntry`, `SlotOffer`,
                                     `WaitlistStatus`,
                                     `SlotOfferStatus`

  outpatientflow                     `OutpatientVisit`, `FlowStage`,
                                     `QueueTicket`

  notifications                      `Notification`,
                                     `NotificationChannel`,
                                     `DeviceRegistration`

  audit                              `AuditEntry`
  ---------------------------------------------------------------------

### 10.2 Servicios/casos de uso candidatos

-   `AuthenticationService`
-   `UserManagementService`
-   `InstitutionalContentService`
-   `CatalogService`
-   `PatientService`
-   `PractitionerService`
-   `AvailabilityService`
-   `AppointmentService`
-   `AppointmentConfirmationService`
-   `PriorityReviewService`
-   `WaitlistService`
-   `SlotReallocationService`
-   `CheckInService`
-   `OutpatientFlowService`
-   `QueueService`
-   `NotificationService`
-   `PushNotificationService` o adapter equivalente
-   `AuditService`
-   `AppointmentMetricsService`

**Candidatas FUTURAS para expansión clínica/documental --- no forman
parte del MVP:**

-   `ClinicalNote`
-   `ClinicalTemplate`
-   `ClinicalDocument`
-   `Prescription`
-   `PrescriptionItem`
-   `MedicalOrder`
-   `MedicalOrderItem`
-   `DigitalSignatureRecord`
-   `CoverageRequest` / `CoverageDecision` únicamente si el proceso
    institucional lo justifica
-   `SihceAdapter`, `ElectronicPrescriptionAdapter` y
    `SisCoverageAdapter` solo con integración autorizada

Estas clases no se crean hasta que el alcance clínico futuro sea
aprobado y exista un modelo normativo/institucional claro.

### 10.3 Controllers candidatos

-   `AuthController`
-   `PublicHospitalController`
-   `CatalogController`
-   `PatientController`
-   `PractitionerController`
-   `AvailabilityController`
-   `AppointmentController`
-   `PriorityRequestController`
-   `PriorityReviewController`
-   `WaitlistController`
-   `SlotOfferController`
-   `DeviceRegistrationController`
-   `CheckInController`
-   `OutpatientFlowController`
-   `AdminController`
-   `AuditController`
-   `MetricsController`

### 10.4 Evolución aproximada

  ---------------------------------------------------------------------
  Etapa                              Orden de magnitud
  ---------------------------------- ----------------------------------
  Foundation existente               Clases mínimas de
                                     arranque/configuración/prueba

  Vertical slice Identity            10--20 clases productivas

  Portal institucional + Catálogo +  12--24 adicionales
  Agenda                             

  Appointment + Confirmation +       18--35 adicionales
  Prioritization                     

  Waitlist + Notifications           10--20 adicionales

  Outpatient Flow                    10--20 adicionales

  MVP backend académico              Aproximadamente 60--105 clases
                                     productivas, según PRD
  ---------------------------------------------------------------------

Las cifras son orientativas. **No son objetivos de cantidad.** La app
móvil y la web tendrán además su propia estructura de componentes,
pantallas, hooks/services y pruebas; no se contabilizan aquí como
"clases backend".

------------------------------------------------------------------------

## 11. Máquina de estados

### 11.1 Estado de cita

Modelo canónico aprobado:

``` text
SCHEDULED
CONFIRMED
CANCELLED
RESCHEDULED
COMPLETED
```

El flujo operativo no introduce estados administrativos adicionales. Una cita
confirmada pasa a `COMPLETED` únicamente al finalizar el flujo en `FINISHED`.
`NO_SHOW`, confirmación pendiente y cualquier regla temporal quedan fuera de
FASE 5.11 y requieren una decisión posterior.

`SLOT_RELEASED` no será necesariamente un estado de `Appointment`; puede
modelarse como evento de dominio derivado de una
cancelación/reprogramación para no mezclar el ciclo de la cita con el
ciclo del cupo.

### 11.2 Lista de espera y ofertas de cupo

La lista de espera se modelará separadamente de la cita para evitar
reasignaciones automáticas no consentidas.

``` text
WAITLIST_ENTRY
  ACTIVE
    -> OFFERED
        ├── ACCEPTED
        ├── REJECTED
        └── EXPIRED
    -> CANCELLED
    -> FULFILLED
```

``` text
SLOT_OFFER
  PENDING
    ├── ACCEPTED
    ├── REJECTED
    └── EXPIRED
```

Reglas clave:

-   un cupo liberado puede generar una o varias ofertas según la
    política aprobada;
-   la aceptación debe confirmarse transaccionalmente;
-   solo una persona puede obtener el mismo slot;
-   una oferta expirada no debe bloquear indefinidamente el cupo;
-   el sistema no reasignará una cita a otro paciente sin
    consentimiento;
-   la prioridad no será "clínica" salvo que exista una regla
    institucional explícita y autorizada.

### 11.3 Priorización de citas ambulatorias

La plataforma podrá incorporar una **priorización de citas ambulatorias
validada por personal autorizado**, siempre que el Hospital de Huaycán
confirme durante discovery que existe una regla institucional aplicable.
Esta funcionalidad **no sustituye el triaje de emergencia ni diagnostica
urgencias**.

Principios:

-   el paciente puede solicitar revisión de prioridad o esta puede
    originarse desde una referencia/indicación válida;
-   una autodeclaración del paciente no convierte automáticamente una
    cita en prioritaria;
-   la decisión debe ser revisada por un rol clínico autorizado por la
    institución;
-   el sistema registra quién validó, cuándo, bajo qué criterio y con
    qué resultado;
-   la prioridad nunca debe desplazar una cita ya confirmada de forma
    arbitraria;
-   la política debe definir si una prioridad influye en lista de
    espera, cupos reservados, fecha máxima o búsqueda de disponibilidad;
-   las categorías de emergencia **Prioridad I--IV no se reutilizarán
    automáticamente para consulta externa**, porque la NTS de Servicios
    de Emergencia del MINSA regula la priorización en ese contexto
    \[I19\];
-   si durante el uso se identifica una posible emergencia, la interfaz
    debe indicar que la persona debe acudir al servicio de emergencia o
    seguir el canal institucional correspondiente, no esperar una cita
    ambulatoria.

Flujo candidato:

``` text
Solicitud de cita
    |
    +--> cita estándar ------------------------------> agenda normal
    |
    +--> solicitud de revisión de prioridad
              |
              v
       PRIORITY_REVIEW_PENDING
              |
       +------+-------+
       |              |
    APPROVED       REJECTED
       |              |
       v              v
política institucional  agenda normal
(lista/cupo/fecha)
```

Estados candidatos de la revisión:

``` text
REQUESTED -> UNDER_REVIEW -> APPROVED / REJECTED / CANCELLED
```

El nivel o escala concreta de prioridad **no se codificará hasta validar
la política institucional**. Para el MVP se limitará a una solicitud de
revisión manual con resultado aprobado/rechazado por un rol autorizado.
No se implementarán algoritmos automáticos de priorización. La decisión
debe ser auditable y no debe inventar niveles clínicos inexistentes.

### 11.4 Etapas del flujo ambulatorio

Separar `AppointmentStatus` de `FlowStage` evita mezclar el estado
administrativo de una cita con el lugar operacional del paciente.

``` text
CHECK_IN
  -> WAITING
  -> IN_ATTENTION
  -> FINISHED
```

Antes de `CHECK_IN`, `flowStage` es `null` y la cita debe estar `CONFIRMED`.
Al alcanzar `FINISHED`, `AppointmentStatus` cambia a `COMPLETED`. Conceptos como
admisión, triaje, llamado, queue ticket o medición detallada de tiempos
permanecen como posibles expansiones futuras y no son valores de `FlowStage` en
el modelo actual.

------------------------------------------------------------------------

## 12. Base de datos

### 12.1 Tecnología

-   PostgreSQL.
-   Desarrollo e integración: PostgreSQL 17 fijado a versión
    reproducible.
-   Migraciones: Flyway.
-   ORM: Spring Data JPA / Hibernate.
-   `ddl-auto=validate`.
-   Testcontainers para integración.

### 12.2 Supabase

Estado: **FUTURO para staging/producción académica**.

Supabase se entiende como una **plataforma administrada basada en
PostgreSQL**, no como un reemplazo conceptual de PostgreSQL.

Arquitectura:

``` text
Next.js / React Native
      |
      v
Spring Boot API
      |
      v
PostgreSQL local / Testcontainers / Supabase PostgreSQL
```

No se permitirá que el frontend ejecute lógica sensible directamente
contra la base de datos.

### 12.3 Consistencia y double booking

La prevención de doble reserva y de **doble aceptación de un cupo
liberado** debe quedar garantizada en PostgreSQL y en la transacción de
negocio.

Primera estrategia a evaluar:

-   slots discretos;
-   constraint único o equivalente por agenda/slot;
-   transacción;
-   actualización atómica;
-   locking cuando sea necesario;
-   idempotency key para requests repetidos;
-   prueba de concurrencia;
-   compare-and-set/update atómico o locking para aceptar una oferta de
    waitlist;
-   constraint que asegure que un mismo slot no pueda terminar asignado
    a dos citas activas.

Caso obligatorio de prueba: si múltiples pacientes intentan aceptar
simultáneamente una oferta o un cupo equivalente, **solo uno debe
obtenerlo** y el resto debe recibir un conflicto de negocio controlado.

Si el modelo final utiliza rangos temporales solapables, se evaluará una
restricción de exclusión PostgreSQL. **No se adopta GiST anticipadamente
sin decidir primero el modelo de agenda.**

### 12.4 Datos sintéticos

Todas las demos, seeds, pruebas y capturas usarán datos sintéticos. No
se copiarán historias clínicas, DNI reales ni datos personales de
pacientes.

------------------------------------------------------------------------

## 13. Backend actual y Spring Initializr

Stack confirmado del backend foundation:

-   Java 21;
-   Spring Boot 4.1.0;
-   Maven Wrapper;
-   Spring Web MVC;
-   Spring Security;
-   Validation;
-   Spring Data JPA;
-   PostgreSQL Driver;
-   Flyway;
-   Actuator;
-   Lombok;
-   DevTools;
-   Testcontainers PostgreSQL.

Configuración:

-   `application.yaml`;
-   `application-local.yaml`;
-   `application-staging.yaml`;
-   `application-prod.yaml`;
-   `application-test.yaml`.

Package raíz:

``` text
com.integrador.salud.api
```

Riesgo técnico pendiente: sustituir `postgres:latest` por una versión
fijada y reproducible cuando se autorice modificar la foundation.

------------------------------------------------------------------------

## 14. Frontend, aplicación móvil y UI/UX

### 14.1 Stack propuesto

**Web pública + portal de reservas + web operativa:**

-   Next.js;
-   React;
-   TypeScript;
-   App Router;
-   Tailwind CSS;
-   Playwright para E2E.

**Aplicación móvil del paciente --- MVP obligatorio:**

-   React Native;
-   Expo;
-   TypeScript;
-   mecanismo de push notifications compatible con el stack móvil,
    proveedor final pendiente de ADR.

La app móvil no se implementará como un segundo backend. Web y móvil
consumirán el mismo contrato REST/OpenAPI y las mismas reglas de negocio
de Spring Boot.

### 14.2 Distribución de responsabilidades

``` text
WEB PÚBLICA INSTITUCIONAL
Ciudadanía / paciente / visitante
- presentación del hospital
- ubicación y cómo llegar
- categoría y servicios
- especialidades
- horarios y canales de contacto
- políticas y orientaciones públicas
- preguntas frecuentes
- imágenes y videos autorizados
- acceso destacado a "Reservar cita"

PORTAL WEB DE RESERVAS
Paciente
- especialidades habilitadas para cita
- disponibilidad y próximo cupo
- reserva
- prioridad sujeta a validación
- confirmación/cancelación/reprogramación
- lista de espera y ofertas
- mis citas

WEB OPERATIVA INTERNA
Personal hospitalario / administración
- agenda
- admisión
- revisión de prioridades según permiso
- cola y flujo
- profesionales
- catálogo
- auditoría
- métricas

MOBILE
Paciente
- disponibilidad
- reserva
- solicitud/estado de revisión de prioridad
- confirmación
- cancelación
- reprogramación
- lista de espera
- ofertas de cupo liberado
- recordatorios
- estado de cita/check-in
```

La **web pública institucional y el portal web de reservas forman parte
del MVP** junto con la web operativa y la app móvil. No se requieren
backends distintos: las superficies consumen el mismo backend Spring
Boot, con endpoints públicos separados de los autenticados.

### 14.2.1 Portal público institucional

La solución web tendrá una **capa pública amplia del hospital** que
sirva como punto de información y entrada al ecosistema digital del
proyecto. Debe distinguirse visual y técnicamente del portal de reservas
y del backoffice del personal.

Contenido mínimo candidato:

-   nombre y descripción del Hospital de Huaycán;
-   ubicación, referencia geográfica y datos de contacto verificados;
-   información institucional pública;
-   cartera de servicios y especialidades;
-   horarios públicos cuando existan fuentes autorizadas;
-   políticas y orientaciones para pacientes;
-   requisitos generales para atención/citas cuando estén validados;
-   preguntas frecuentes;
-   galería de imágenes y videos **propios, autorizados o con uso
    permitido**;
-   avisos operativos públicos;
-   botón/CTA principal **"Reservar cita"**;
-   enlace a la aplicación móvil.

Rutas candidatas:

``` text
/
/hospital
/servicios
/especialidades
/ubicacion
/politicas
/multimedia
/preguntas-frecuentes
/citas
```

El portal no debe presentarse como sitio oficial del Hospital de Huaycán
mientras no exista autorización institucional. Toda información
institucional debe provenir de fuentes verificadas o de contenido
aprobado durante el proyecto. El contenido mostrado será considerado
material académico basado en fuentes públicas o autorizadas.

### 14.2.2 Portal web de reservas

`/citas` será una experiencia específica orientada a la gestión de citas
y no una simple sección de texto del portal público.

Menú/flujo candidato:

``` text
CITAS
- Buscar especialidad
- Especialidades disponibles
- Próximos cupos
- Reservar
- Mis citas
- Confirmar asistencia
- Cancelar / reprogramar
- Lista de espera
- Prioridad: solicitar revisión / ver estado
```

La vista **"Especialidades disponibles"** debe construirse a partir del
catálogo + agendas + slots reales del sistema:

-   mostrar especialidad/servicio;
-   indicar si existen cupos;
-   mostrar próximo cupo disponible cuando sea permitido;
-   permitir filtrar por fecha/turno/profesional si la regla
    institucional lo autoriza;
-   si no hay cupos, mostrar estado "Sin disponibilidad" y ofrecer lista
    de espera cuando aplique;
-   no confundir "especialidad existente en la cartera" con
    "especialidad actualmente disponible para reserva".

La disponibilidad debe provenir del backend y nunca quedar hardcodeada
en el frontend.

### 14.3 Sistema de diseño

Identidad visual propia y académica. No usar branding de MINSA, DIRIS o
Hospital de Huaycán como si existiera patrocinio oficial.

Principios:

-   accesibilidad;
-   bajo esfuerzo cognitivo;
-   mobile-first para paciente;
-   desktop-first responsive para personal;
-   foco visible;
-   contraste;
-   navegación por teclado en web;
-   tamaños táctiles adecuados en móvil;
-   prevención de errores;
-   feedback inmediato;
-   estados loading/empty/success/warning/error;
-   formularios con labels y mensajes claros;
-   confirmación de acciones destructivas;
-   notificaciones con texto mínimo y sin información clínica sensible.

### 14.3.1 Accesibilidad cromática, temas y daltonismo

La accesibilidad visual será un requisito transversal del **MVP web y
móvil**, no un tema puramente estético. Se tomará como referencia **WCAG
2.2 nivel AA** para contraste, uso del color, foco y componentes
interactivos.

Selector de visualización candidato:

``` text
Tema
- Sistema
- Claro
- Oscuro
- Alto contraste

Modo de visión de color
- Estándar
- Protanopia
- Deuteranopia
- Tritanopia
```

Los tres modos de visión de color se incorporan como **preferencias de
apoyo**, no como sustituto de un diseño accesible. El sistema debe
seguir siendo comprensible aunque el usuario no active ningún modo
especial.

Reglas obligatorias:

-   **nunca comunicar un estado únicamente por color**; combinar color +
    texto + icono y, cuando ayude, forma/patrón;
-   validar contraste de texto, controles, gráficos e indicadores de
    foco;
-   evitar pares de color problemáticos como única distinción;
-   mantener etiquetas textuales para estados de cita, cola, alerta,
    éxito y error;
-   conservar la preferencia por usuario o dispositivo sin exponer datos
    sensibles;
-   permitir volver siempre a la configuración estándar;
-   probar web y móvil con simuladores de deficiencia cromática y
    pruebas manuales;
-   no depender de filtros CSS globales que deterioren imágenes,
    fotografías, documentos o legibilidad.

Ejemplo de estado accesible:

``` text
[✓] CONFIRMADA
[!] REQUIERE ACCIÓN
[×] CANCELADA
[⏱] EN ESPERA
```

El color refuerza el significado, pero el icono y el texto lo comunican
por sí mismos.

### 14.4 Pantallas móviles del paciente

MVP candidato:

-   onboarding/login;
-   perfil y preferencias de contacto;
-   ajustes de visualización y accesibilidad (tema, alto contraste y
    modos de visión de color);
-   catálogo de servicios/especialidades;
-   disponibilidad;
-   reservar cita;
-   solicitar revisión de prioridad cuando corresponda;
-   consultar estado de revisión de prioridad;
-   mis citas;
-   confirmar asistencia;
-   cancelar;
-   reprogramar;
-   solicitar ingreso a lista de espera;
-   ver/aceptar/rechazar oferta de cupo;
-   notificaciones y recordatorios;
-   check-in cuando la regla institucional lo permita;
-   estado administrativo del flujo;
-   indicaciones no clínicas.

### 14.5 Interfaces web operativas

**Admisión/recepción:**

-   búsqueda de cita;
-   check-in;
-   registro asistido;
-   agenda del día;
-   cambios permitidos;
-   lista de espera y cupos liberados según permisos;
-   gestión de incidencias.

**Revisor clínico de prioridad:**

-   cola de solicitudes de prioridad;
-   evidencia/referencia mínima autorizada;
-   aprobar o rechazar según protocolo institucional;
-   registrar criterio/código de motivo;
-   auditoría obligatoria de cada decisión;
-   sin facultad para reemplazar el triaje de emergencia.

**Personal de flujo/triaje operativo:**

-   lista de pacientes pendientes;
-   cambio de etapa;
-   sin datos clínicos en MVP.

**Profesional:**

-   agenda;
-   cola de pacientes;
-   llamado;
-   inicio/fin de consulta como evento operativo.

**Expansión clínica futura del profesional --- NO MVP:**

-   registrar nota clínica estructurada según especialidad;
-   emitir recomendaciones e indicaciones;
-   generar prescripciones u órdenes solo cuando exista alcance y
    autorización;
-   firmar electrónicamente/digitalmente cuando corresponda;
-   generar PDF derivado para impresión/consulta;
-   consultar trazabilidad de documentos emitidos.

**Administrador:**

-   usuarios;
-   roles;
-   catálogo;
-   profesionales;
-   consultorios;
-   horarios;
-   reglas de recordatorio;
-   configuración de lista de espera;
-   auditoría;
-   métricas.

------------------------------------------------------------------------

## 15. API y contrato

Base:

``` text
/api/v1
```

Recursos candidatos:

``` text
/api/v1/public/hospital
/api/v1/public/services
/api/v1/public/specialties
/api/v1/public/media
/api/v1/auth
/api/v1/catalog
/api/v1/specialties
/api/v1/services
/api/v1/patients
/api/v1/practitioners
/api/v1/rooms
/api/v1/availability
/api/v1/appointments
/api/v1/appointment-priority-requests
/api/v1/priority-reviews
/api/v1/waitlist
/api/v1/slot-offers
/api/v1/notifications/devices
/api/v1/check-ins
/api/v1/outpatient-flow
/api/v1/admin
/api/v1/audit
/api/v1/metrics
```

Recursos **FUTUROS y fuera del MVP**, únicamente si se aprueba la
expansión clínica:

``` text
/api/v1/clinical-notes
/api/v1/clinical-documents
/api/v1/prescriptions
/api/v1/medical-orders
/api/v1/coverage-requests
```

Estos recursos no deben incorporarse al contrato inicial hasta validar
normativa, roles, firma, datos clínicos e integración institucional.

OpenAPI:

``` text
contracts/openapi.yaml
```

Se completará únicamente después de aprobar casos de uso y modelo de
datos.

Reglas:

-   JSON;
-   DTOs, no entidades JPA expuestas;
-   validación de entrada;
-   paginación;
-   límites de tamaño;
-   errores consistentes;
-   idempotencia para operaciones sensibles, incluida aceptación de
    ofertas;
-   autorización por endpoint/método/objeto;
-   versionamiento explícito.

------------------------------------------------------------------------

## 16. Interoperabilidad y Minsa Digital

El proyecto no asume acceso a APIs internas del MINSA. Si en una etapa
futura existe autorización o documentación suficiente, las integraciones
se harán mediante **Adapter Pattern**.

### 16.1 FHIR

Estado: **FUTURO**.

FHIR no será el modelo interno del sistema. Recursos candidatos para
mapeo futuro:

-   `Patient`;
-   `Practitioner`;
-   `PractitionerRole`;
-   `Appointment`;
-   `Schedule`;
-   `Slot`.

### 16.2 Sistemas de citas MINSA

Debido a que existen servicios de citas digitales en el ecosistema
MINSA/DIRIS \[I9\]\[I10\], se documentará durante discovery:

-   si el Hospital de Huaycán utiliza un sistema nacional o local;
-   qué parte de la agenda se gestiona externamente;
-   qué integraciones serían necesarias;
-   si el prototipo debe importar, simular o interoperar con citas
    existentes.

No se diseñará una integración real sin información oficial.

### 16.3 SIHCE, receta electrónica, firma digital y cobertura SIS

Estado: **FUTURO / sujeto a autorización y normativa aplicable.**

MINSA cuenta con lineamientos y sistemas para Historia Clínica
Electrónica, receta electrónica y uso de firma electrónica/digital en
actos médicos y actos de salud. Por ello, una futura expansión clínica
del proyecto debe priorizar **interoperar o alinearse** con esos
mecanismos antes que crear formatos incompatibles o procesos paralelos.

Principios de diseño:

-   no reemplazar el SIHCE como sistema institucional de historia
    clínica;
-   usar documentos estructurados como fuente primaria y PDF como
    representación derivada;
-   aplicar firma electrónica/digital cuando el documento y la normativa
    lo requieran;
-   mantener identidad del profesional, sello de tiempo, versión y
    auditoría;
-   separar prescripción de dispensación;
-   no asumir que una receta/orden emitida implica automáticamente
    cobertura del SIS;
-   modelar cobertura/autorización del SIS solo con reglas verificadas
    y, de existir, integración oficial;
-   evitar que el paciente pueda alterar un documento firmado;
-   aplicar autorización por objeto y acceso mínimo necesario a
    información clínica.

Flujo conceptual futuro:

``` text
Atención profesional
  -> nota clínica estructurada
  -> prescripción / orden (si aplica)
  -> firma electrónica/digital
  -> PDF derivado / copia para paciente
  -> dispensación / prestación
  -> validación de cobertura SIS por proceso oficial, si corresponde
```

Fuentes normativas/técnicas a revisar antes de implementar esta
expansión: SIHCE MINSA, estándar de transacción de receta electrónica
DIGEMID, directiva de firma electrónica/digital en actos médicos y
reglas vigentes de cobertura SIS.

------------------------------------------------------------------------

## 17. Patrones de diseño

  -----------------------------------------------------------------------
  Patrón                          Uso
  ------------------------------- ---------------------------------------
  Modular Monolith                Unidad desplegable con límites por
                                  feature

  Package by Feature              Organización por capacidad del negocio

  Application Service / Use Case  Orquestación de casos de uso

  Repository                      Persistencia por agregado/caso real

  DTO                             Contrato HTTP y datos externos

  Mapper                          Cuando reduzca duplicación real

  Adapter                         MINSA/FHIR/notificaciones/proveedores

  Strategy                        Variantes reales, por ejemplo canal de
                                  notificación

  Domain/Application Events       Cambios internos desacoplados

  Idempotency                     Citas, cancelaciones, reprogramaciones,
                                  aceptación de ofertas, webhooks

  Transactional Outbox            Futuro si se requiere entrega externa
                                  garantizada
  -----------------------------------------------------------------------

Regla: evitar pattern overengineering.

------------------------------------------------------------------------

## 18. Confirmaciones, lista de espera, eventos y notificaciones

Esta capacidad forma parte del **MVP obligatorio**, no de una mejora
futura. Su propósito es reducir inasistencias evitables, liberar con
anticipación cupos que no serán usados y facilitar que otra persona
pueda aceptarlos. No se promete eliminar el no-show o las colas al 100
%.

### 18.1 Confirmación anticipada

Regla inicial candidata:

``` text
T-7 días antes de la cita
        |
        v
"¿Podrás asistir?"
        |
   +----+----+
   |         |
  SÍ         NO
   |         |
CONFIRMED  CANCELLED / RESCHEDULED
             |
             v
        SLOT_RELEASED
```

`T-7 días` se documenta por recomendación docente, pero deberá
configurarse y validarse con el proceso real. Podrán existir
recordatorios adicionales (por ejemplo T-48 h/T-24 h) solo si las reglas
de negocio lo justifican.

### 18.2 Recuperación de cupos y lista de espera

Flujo propuesto:

``` text
Slot liberado
    |
    v
Buscar candidatos elegibles
    |
    v
Crear oferta de cupo
    |
    v
Notificar en app/push
    |
    +--> REJECTED / EXPIRED -> siguiente candidato según política
    |
    v
ACCEPTED
    |
    v
Reserva transaccional
    |
    v
Cupo reasignado
```

La reasignación **no será automática sin consentimiento**. La política
de prioridad debe validarse durante discovery. Candidatos posibles de la
política, sin convertirlos todavía en regla definitiva:

-   mismo servicio/especialidad;
-   requisitos administrativos compatibles;
-   disponibilidad declarada por el paciente;
-   antigüedad en lista de espera;
-   prioridad ambulatoria previamente validada por personal autorizado,
    solo si la política institucional la incorpora;
-   reglas institucionales autorizadas.

La lista de espera no convertirá una autodeclaración en prioridad
clínica. Cualquier criterio de prioridad usado para ordenar ofertas debe
provenir de una revisión aprobada y auditable.

### 18.3 Eventos internos candidatos

-   `AppointmentScheduled`;
-   `AppointmentConfirmationRequested`;
-   `AppointmentConfirmed`;
-   `AppointmentCancelled`;
-   `AppointmentRescheduled`;
-   `AppointmentNoShow`;
-   `PriorityReviewRequested`;
-   `PriorityReviewApproved`;
-   `PriorityReviewRejected`;
-   `SlotReleased`;
-   `WaitlistEntryCreated`;
-   `SlotOfferCreated`;
-   `SlotOfferAccepted`;
-   `SlotOfferRejected`;
-   `SlotOfferExpired`;
-   `PatientCheckedIn`;
-   `PatientCalled`;
-   `OutpatientVisitCompleted`.

`notifications` y `waitlist` podrán reaccionar a eventos sin acoplarse
directamente a la implementación interna de `appointments`.

### 18.4 Canales de notificación

Prioridad de MVP:

1.  notificaciones dentro de la app;
2.  push notification móvil;
3.  email si se aprueba;
4.  WhatsApp como integración futura, si existe proveedor, autorización
    y presupuesto.

No enviar diagnósticos, resultados clínicos ni información sensible en
previews de notificación.

El fallo de una notificación no debe revertir una cita correctamente
confirmada/cancelada. La entrega de una notificación tampoco se
considerará prueba automática de que el usuario la leyó.

Webhooks: solo para integraciones externas, con firma, replay
protection, idempotencia, validación y rate limiting.

------------------------------------------------------------------------

## 19. Seguridad y privacidad

Baseline:

-   OWASP Top 10:2025;
-   OWASP API Security Top 10:2023;
-   Spring Security.

### 19.1 Autenticación web

Propuesta inicial:

``` text
Spring Security
+ server-side session
+ cookie HttpOnly
+ Secure fuera de local
+ SameSite apropiado
+ CSRF habilitado
```

Password hashing:

-   `DelegatingPasswordEncoder`;
-   bcrypt como candidato inicial;
-   factor de trabajo medido antes de producción.

### 19.2 Autenticación móvil

La autenticación móvil es una decisión obligatoria antes de implementar
la app. Debe resolverse mediante ADR y no mediante un esquema casero de
tokens. Opciones a evaluar:

-   OAuth2/OIDC con Authorization Code + PKCE mediante un proveedor
    apropiado;
-   otro mecanismo soportado formalmente por Spring Security que permita
    revocación y renovación segura.

Requisitos mínimos:

-   access tokens de vida corta si se adopta bearer token;
-   refresh/reautenticación protegida;
-   almacenamiento seguro en el dispositivo;
-   revocación al cerrar sesión o comprometer dispositivo cuando sea
    viable;
-   no guardar tokens en almacenamiento inseguro;
-   autorización por objeto en backend independientemente del cliente
    móvil.

No se implementará un OAuth Authorization Server propio salvo necesidad
y ADR explícito.

### 19.3 Roles candidatos

-   `PATIENT`;
-   `ADMISSION`;
-   `FLOW_OPERATOR`;
-   `PRACTITIONER`;
-   `CLINICAL_PRIORITY_REVIEWER`;
-   `ADMIN`;
-   `AUDITOR`.

Los roles definitivos se validarán con los actores reales.
`CLINICAL_PRIORITY_REVIEWER` es un rol/permiso **PROPUESTO** que solo
debe asignarse a personal de salud autorizado por el hospital para
revisar criterios de priorización ambulatoria; no debe recaer por
defecto en recepción ni administración.

### 19.4 Controles mínimos

-   RBAC;
-   object-level authorization;
-   prevención IDOR/BOLA;
-   CSRF;
-   CORS allowlist;
-   CSP en frontend;
-   security headers;
-   rate limiting;
-   protección brute force;
-   progressive backoff;
-   validación;
-   mass assignment protection;
-   límites de payload/paginación;
-   secretos fuera del repo;
-   logs sin datos sensibles;
-   auditoría;
-   errores seguros;
-   HTTPS;
-   datos sintéticos;
-   minimización de datos;
-   protección de device tokens/push tokens;
-   previews de notificación sin datos sensibles;
-   revocación y control de sesiones/dispositivos cuando se defina el
    mecanismo móvil.

------------------------------------------------------------------------

### 19.5 Datos clínicos y documentos firmados --- expansión futura

Si se incorpora el espacio clínico profesional, se elevará el nivel de
protección porque el sistema pasará de manejar principalmente
información administrativa de citas a procesar información clínica.
Antes de implementarlo serán obligatorios:

-   threat model específico del módulo clínico;
-   matriz de permisos por especialidad/rol y autorización por objeto;
-   cifrado en tránsito y controles de cifrado/gestión de claves en
    almacenamiento según arquitectura aprobada;
-   auditoría de lectura, creación, modificación, firma y descarga;
-   versionado e inmutabilidad de documentos firmados;
-   reglas de corrección/adenda en lugar de sobrescritura silenciosa;
-   retención, respaldo y recuperación documentados;
-   protección de PDFs y URLs de descarga;
-   sesiones reforzadas para actos de firma;
-   pruebas de acceso indebido y fuga de información;
-   prohibición de incluir información clínica sensible en push
    notifications.

------------------------------------------------------------------------

## 20. Tolerancia a fallos y escalamiento

### 20.1 Primero resolver lo local

-   transacciones;
-   constraints;
-   timeouts;
-   idempotencia;
-   health checks;
-   manejo de errores;
-   rollback;
-   expiración determinística de ofertas de lista de espera;
-   no duplicar confirmaciones/reasignaciones ante reintentos móviles.

### 20.2 Integraciones externas

Cuando existan:

-   timeout;
-   retry solo si es seguro/idempotente;
-   exponential backoff;
-   circuit breaker;
-   fallback cuando tenga sentido.

Resilience4j: **FUTURO**, solo con una integración real.

### 20.3 Redis

**NO AHORA.**

Triggers posibles:

-   múltiples instancias;
-   sesiones distribuidas;
-   rate limiting distribuido;
-   cache demostrada por métricas.

### 20.4 RabbitMQ/Kafka

**NO AHORA.**

RabbitMQ se evaluará para trabajos asíncronos o reintentos cuando
eventos in-process no sean suficientes. Kafka solo tendría sentido con
una necesidad real de streaming/eventos a escala.

### 20.5 Evolución de escala

``` text
1 instancia
  -> PostgreSQL administrado
  -> pooling
  -> optimización de queries/índices
  -> cache medida
  -> backend horizontal
  -> sesión/rate limit distribuido
  -> workers async
  -> replicas de lectura si se justifican
  -> extracción de módulo solo con evidencia
```

------------------------------------------------------------------------

## 21. Estrategia de pruebas

Las pruebas acompañan cada feature en backend, web y móvil.

  -----------------------------------------------------------------------
  Nivel                   Herramientas/enfoque    Qué valida
  ----------------------- ----------------------- -----------------------
  Unit backend            JUnit + Mockito         reglas, estados,
                                                  confirmaciones,
                                                  waitlist, casos límite

  Integration             Spring Boot +           contexto,
                          Testcontainers +        transacciones, Flyway,
                          PostgreSQL              JPA

  Repository              PostgreSQL real de test queries, constraints,
                                                  locking

  API                     Spring MVC/Security     HTTP, DTO,
                          Test                    autorización, errores

  Concurrency             pruebas paralelas       doble reserva,
                                                  aceptación simultánea
                                                  de cupo, idempotencia

  Security                pruebas negativas       BOLA/IDOR, CSRF, roles,
                                                  abuso, tokens/sesiones,
                                                  acceso indebido a
                                                  revisión de prioridades

  Web E2E                 Playwright              flujos operativos
                                                  críticos

  Mobile unit/component   tooling compatible con  confirmación,
                          React Native/Expo a     cancelación, waitlist,
                          definir                 manejo de estados

  Mobile E2E              herramienta a decidir   flujo reservar →
                          en ADR/tooling          recordar →
                                                  confirmar/cancelar →
                                                  oferta → aceptar

  Notification            fake provider +         contenido,
                          integration tests       preferencias,
                                                  reintentos, no
                                                  reversión de cita

  Performance             k6                      línea base, carga,
                                                  stress, p95/p99 cuando
                                                  se definan

  Usability               pruebas con escenarios  comprensión y fricción
                          autorizados             web/móvil
  -----------------------------------------------------------------------

Casos concurrentes obligatorios:

1.  dos usuarios intentan reservar el mismo slot; solo uno gana;
2.  varios usuarios intentan aceptar el mismo cupo liberado; solo uno
    gana;
3.  un mismo request móvil se reintenta por mala conectividad; no crea
    citas/ofertas duplicadas;
4.  una oferta expira al mismo tiempo que el paciente intenta aceptarla;
    el resultado debe ser consistente y auditable.

No se fijan SLO definitivos sin línea base.

------------------------------------------------------------------------

## 22. CI/CD

GitHub Actions.

Pipeline progresivo:

1.  checkout;
2.  Java 21;
3.  build/compile;
4.  unit tests;
5.  integration tests con Docker/Testcontainers;
6.  package;
7.  dependency/security scan;
8.  web lint/typecheck/test cuando exista;
9.  mobile lint/typecheck/unit tests cuando exista;
10. Playwright web;
11. mobile E2E cuando el flujo esté estable;
12. performance smoke cuando exista staging;
13. artifacts web/backend/mobile cuando corresponda.

Merge bloqueado si falla un quality gate crítico.

------------------------------------------------------------------------

## 23. Observabilidad

Actualmente: Actuator `health,info`.

Evolución:

-   readiness/liveness;
-   métricas;
-   logs estructurados;
-   correlation ID;
-   trazas;
-   OpenTelemetry;
-   dashboards;
-   alertas;
-   métricas de negocio no sensibles.

Métricas de negocio candidatas:

-   citas por estado;
-   ocupación de slots;
-   confirmaciones;
-   no-show;
-   cancelaciones anticipadas/tardías;
-   reprogramaciones;
-   cupos liberados;
-   cupos reasignados;
-   tasa de recuperación de cupos;
-   ofertas de waitlist aceptadas/rechazadas/expiradas;
-   tiempos entre etapas;
-   utilización de consultorios;
-   errores de reserva;
-   tasa de conflictos concurrentes.

No registrar datos clínicos ni identificadores innecesarios.

------------------------------------------------------------------------

## 24. Deployment

### 24.1 Local

-   Maven Wrapper;
-   Spring Boot;
-   PostgreSQL local o Testcontainers;
-   variables de entorno.

### 24.2 Staging académico

Propuesta:

-   backend desplegado en plataforma a decidir;
-   PostgreSQL administrado, potencialmente Supabase;
-   frontend Next.js;
-   build móvil de prueba/distribución interna;
-   proveedor/configuración de push separado por entorno;
-   HTTPS;
-   secrets gestionados fuera del código;
-   migraciones Flyway;
-   datos sintéticos;
-   staging protegido.

### 24.3 Producción académica

Solo si el curso lo requiere. No se denominará producción hospitalaria
real.

------------------------------------------------------------------------

## 25. Roadmap por fases

### FASE 0 --- Reorientación a Huaycán

**Estado:** EN CURSO.

Objetivos:

-   sustituir correctamente el caso Vitarte;
-   validar Huaycán con el docente;
-   incorporar la decisión obligatoria de aplicación móvil;
-   depurar fuentes no verificadas;
-   actualizar marco teórico, problema, roadmap y entregables
    académicos.

**Gate:** caso de estudio, título y alcance web+móvil aprobados.

### FASE 1 --- Discovery institucional

Actividades:

-   analizar MAPRO;
-   obtener cartera 2025/anexo;
-   entrevistas autorizadas;
-   observación del flujo;
-   actores;
-   canales de cita;
-   AS-IS;
-   tiempos;
-   reglas;
-   sistema(s) actual(es);
-   no-show/cancelaciones/reprogramaciones;
-   mecanismos actuales de recordatorio;
-   existencia o no de lista de espera;
-   reglas de reutilización de cupos;
-   línea base.

**Gate:** diagnóstico validado y matriz de evidencia.

### FASE 2 --- PRD, portal público y reglas de negocio

Entregables:

-   `docs/01-PRD.md`;
-   alcance MVP web+móvil;
-   actores;
-   historias/casos de uso;
-   RF/RNF;
-   reglas de confirmación;
-   política de cancelación/reprogramación;
-   política de waitlist y ofertas;
-   política de priorización ambulatoria y rol validador;
-   alcance del portal institucional público y portal de reservas;
-   criterios de aceptación;
-   fuera de alcance.

**Gate:** MVP aprobado.

### FASE 3 --- BPMN AS-IS / TO-BE y App Flow

Entregable:

-   `docs/03-APP-FLOW.md`;
-   BPMN;
-   estados de cita;
-   estados de lista de espera/oferta;
-   etapas ambulatorias;
-   escenarios alternos y expiraciones.

**Gate:** flujos validados.

### FASE 4 --- UI/UX Portal público + Reservas + Web operativa + Mobile

Entregable:

-   `docs/04-UI-UX-DESIGN-BRIEF.md`;
-   design system compartido;
-   wireframes web;
-   wireframes mobile;
-   prototipo de paciente móvil;
-   prototipo de operación web;
-   flujo de recordatorio/confirmación;
-   flujo de lista de espera/oferta;
-   accesibilidad WCAG 2.2 AA como referencia;
-   temas Sistema/Claro/Oscuro/Alto contraste;
-   modos de apoyo para protanopia, deuteranopia y tritanopia;
-   componentes que no dependan únicamente del color.

**Gate:** flujos críticos web y móvil entendibles, accesibles y
probados; estados críticos comprensibles sin depender solo del color.

### FASE 5 --- TRD, arquitectura y ADRs

Entregables:

-   `docs/02-TRD.md`;
-   C4;
-   módulos;
-   ADRs;
-   threat model;
-   observabilidad;
-   estrategia de autenticación web;
-   estrategia de autenticación mobile;
-   proveedor/estrategia de push;
-   política técnica de waitlist;
-   modelo de permisos y auditoría para revisión de prioridades;
-   arquitectura de portal público / reservas / backoffice y estrategia
    de contenido institucional.

**Gate:** arquitectura aprobada.

### FASE 6 --- Modelo de datos y OpenAPI

Entregables:

-   `docs/05-BACKEND-SCHEMA.md`;
-   ERD;
-   diccionario de datos;
-   constraints;
-   entidades de confirmation/waitlist/slot offer;
-   entidades de solicitud/revisión de prioridad;
-   endpoints públicos institucionales y de disponibilidad;
-   `contracts/openapi.yaml`.

**Gate:** contrato y modelo aprobados.

### FASE 7 --- Foundation hardening / CI

Tareas:

-   fijar PostgreSQL Testcontainers;
-   CI backend;
-   quality gates;
-   estructura modular mínima;
-   error handling base;
-   auditoría de configuración.

**Gate:** build y test reproducibles en CI.

### FASE 8 --- Identity & Security

Implementar:

-   usuarios;
-   roles;
-   login/logout web;
-   mecanismo de autenticación mobile según ADR;
-   hashing;
-   RBAC;
-   autorización por objeto;
-   auditoría de acceso;
-   rate limit inicial.

**Gate:** security tests web/mobile verdes.

### FASE 9 --- Catalog, practitioners y agenda

Implementar:

-   especialidades;
-   servicios;
-   consultorios;
-   profesionales;
-   schedules;
-   slots.

**Gate:** disponibilidad consistente y testeada.

### FASE 10 --- Appointment, Priority Review & Confirmation Core

Implementar:

-   reservar;
-   solicitud de revisión de prioridad;
-   cola de revisión para rol autorizado;
-   aprobación/rechazo auditable de prioridad;
-   aplicación de prioridad solo según política institucional;
-   confirmación anticipada;
-   reprogramar;
-   cancelar;
-   no-show;
-   liberación de slot;
-   idempotencia;
-   double-booking protection.

**Gate:** reserva/confirmación/cancelación y revisión de prioridad
funcionan con autorización, auditoría y tests concurrentes verdes; una
prioridad no puede ser autoaprobada por el paciente.

> **Aclaración de numeración:** el incremento técnico backend **FASE 5.11 -
> Appointment Operations Foundation** no corresponde a la **FASE 11** de esta
> EDT macro. La EDT conserva su orden y alcance; waitlist y notificaciones no
> forman parte del incremento backend 5.11.

### FASE 11 --- Waitlist & Notification Core

Implementar:

-   lista de espera;
-   selección de candidatos según política aprobada;
-   ofertas de cupo;
-   aceptación/rechazo/expiración;
-   notificación in-app;
-   push móvil;
-   reglas T-7/configurables;
-   auditoría y métricas de recuperación.

**Gate:** un cupo solo puede ser reasignado a una persona y el fallo de
push no rompe la transacción de negocio.

### FASE 12 --- Outpatient Flow

Implementar:

-   llegada/check-in;
-   admisión;
-   etapa de triaje condicional;
-   espera;
-   llamado;
-   consulta como estado operativo;
-   cierre;
-   queue ticket;
-   medición de tiempos.

**Gate:** flujo E2E backend completo.

### FASE 13 --- Portal público + Portal de reservas + Web operativa

Implementar:

**Portal público:** - página principal del hospital; -
ubicación/contacto; - servicios y especialidades; -
políticas/orientaciones; - multimedia autorizada; - CTA hacia reservas y
app móvil.

**Portal de reservas:** - catálogo de especialidades; - especialidades
con disponibilidad real; - próximo cupo y filtros permitidos; -
reserva/mis citas; - prioridad, confirmación, cancelación,
reprogramación y waitlist.

**Web operativa:** - panel de admisión; - panel de revisión de
prioridades; - panel profesional; - administración; - agenda; - lista de
espera/cupos; - dashboards iniciales.

**Gate:** Playwright verde para navegación pública, flujo web de reserva
y flujos operativos internos críticos.

### FASE 14 --- App Móvil Paciente

Implementar:

-   autenticación móvil;
-   disponibilidad y reserva;
-   solicitud/consulta de revisión de prioridad;
-   mis citas;
-   confirmación;
-   cancelación/reprogramación;
-   lista de espera;
-   ofertas de cupo;
-   push notifications;
-   check-in/estado cuando aplique.

**Gate:** flujo móvil reservar → solicitar/consultar prioridad cuando
corresponda → recordar → confirmar/cancelar → recibir/aceptar oferta →
seguimiento funciona con datos sintéticos y controles de seguridad.

### FASE 15 --- Hardening, Analytics y UX

-   auditoría OWASP;
-   negative tests;
-   performance;
-   accesibilidad web/móvil, contraste y navegación;
-   pruebas de protanopia, deuteranopia y tritanopia;
-   verificación de que estados/alertas no dependen únicamente del
    color;
-   concurrencia;
-   revisión de logs;
-   dependencias;
-   usabilidad web/móvil;
-   métricas de no-show y recuperación de cupos.

**Gate:** riesgos críticos resueltos o aceptados.

### FASE 16 --- Staging y validación

-   despliegue;
-   migraciones;
-   web staging;
-   build móvil de prueba;
-   push por entorno;
-   smoke tests;
-   escenarios sintéticos;
-   medición de línea base vs propuesta cuando sea metodológicamente
    válido.

**Gate:** evidencia documentada.

### FASE 17 --- Integrations / FHIR / Scaling

Después del MVP web+móvil estable:

-   FHIR/adapters si existe acceso;
-   integración con sistemas MINSA si se autoriza;
-   WhatsApp/email según proveedor;
-   Redis/RabbitMQ solo con trigger;
-   escalamiento basado en métricas.

------------------------------------------------------------------------

### FASE 18 --- Expansión clínica, documentos firmados y cobertura --- FUTURO

Esta fase **no pertenece al MVP** y solo se inicia después de validar el
núcleo de citas/flujo y contar con aprobación académica/institucional
para ampliar el tratamiento de datos clínicos.

Explorar e implementar progresivamente, si corresponde:

-   workspace clínico web del profesional;
-   plantillas por especialidad;
-   notas clínicas estructuradas;
-   recomendaciones e indicaciones;
-   receta electrónica y órdenes de medicamentos/dispositivos/exámenes
    según normativa;
-   firma electrónica/digital de actos médicos;
-   generación de PDF como representación derivada;
-   acceso del paciente a documentos firmados autorizados;
-   adapters hacia SIHCE/receta electrónica;
-   flujo de cobertura/autorización SIS únicamente con reglas
    verificadas e integración aprobada.

**Gate:** threat model clínico, marco normativo, modelo de datos, firma,
interoperabilidad y política de acceso aprobados antes de almacenar
datos clínicos reales o emitir documentos con efectos asistenciales.

------------------------------------------------------------------------

## 26. Dependencias actuales y backlog

### 26.1 Dependencias Spring actuales

-   Spring Boot Actuator;
-   Spring Data JPA;
-   Spring Flyway;
-   Spring Security;
-   Validation;
-   Spring Web MVC;
-   PostgreSQL Driver;
-   Flyway PostgreSQL;
-   DevTools;
-   Lombok;
-   Testcontainers;
-   JUnit/Spring test starters.

### 26.2 Backlog de dependencias futuras

  ---------------------------------------------------------------------
  Dependencia                        Trigger
  ---------------------------------- ----------------------------------
  Spring Modulith                    límites modulares reales difíciles
                                     de controlar

  Spring Session                     múltiples instancias con sesión
                                     server-side

  Redis                              cache/sesión/rate limit
                                     distribuido justificado

  Resilience4j                       primera API externa real

  RabbitMQ                           jobs async/reintentos fuera del
                                     request

  OpenAPI tooling                    contrato estable

  Librería/proveedor push mobile     ADR de notificaciones y app móvil
                                     lista para integración

  Prometheus/OpenTelemetry exporters staging con observabilidad
                                     centralizada
  ---------------------------------------------------------------------

------------------------------------------------------------------------

## 27. ADR pendientes

1.  ADR-001 --- Validación del nuevo caso Hospital de Huaycán.
2.  ADR-002 --- Límites finales de módulos (`waitlist` independiente o
    subcapacidad de `appointments`).
3.  ADR-003 --- Estrategia de autenticación web.
4.  ADR-004 --- Estrategia de autenticación mobile.
5.  ADR-005 --- Roles y matriz de permisos.
6.  ADR-006 --- Modelo de agenda: slot discreto vs rango.
7.  ADR-007 --- Prevención de double booking y aceptación concurrente de
    ofertas.
8.  ADR-008 --- Workflow OpenAPI compartido web/mobile.
9.  ADR-009 --- Política de confirmación y recordatorios (T-7 y ventanas
    adicionales).
10. ADR-010 --- Política de lista de espera, prioridad, expiración y
    fairness.
11. ADR-011 --- Proveedor y estrategia de push notifications.
12. ADR-012 --- Supabase para staging.
13. ADR-013 --- Política de auditoría y retención.
14. ADR-014 --- FHIR/interoperabilidad MINSA.
15. ADR-015 --- Plataforma de deployment.
16. ADR-016 --- Introducción de Redis.
17. ADR-017 --- Introducción de RabbitMQ.
18. ADR-018 --- Estrategia de accesibilidad visual, temas y modos de
    visión de color.
19. ADR-019 --- Expansión clínica: notas, plantillas, prescripción,
    órdenes y documentos firmados.
20. ADR-020 --- Interoperabilidad SIHCE/receta electrónica y flujo de
    cobertura SIS.
21. ADR-021 --- Política de priorización ambulatoria: criterios, rol
    autorizado, auditoría y relación con waitlist.
22. ADR-022 --- Arquitectura del portal público, portal de reservas y
    gestión de contenido institucional.
23. ADR-023 --- Política de medios públicos: imágenes/videos
    autorizados, almacenamiento y derechos de uso.

------------------------------------------------------------------------

## 28. Riesgos

  -----------------------------------------------------------------------
  Riesgo                      Severidad             Mitigación
  --------------------------- --------------------- ---------------------
  Reorientación institucional Alta académica        validar título/caso
  aún no validada formalmente                       con docente antes de
                                                    cerrar PRD

  Inventar el flujo real      Alta académica        MAPRO + trabajo de
                                                    campo + evidencia

  Confundir sistema académico Alta                  disclaimer y lenguaje
  con solución oficial MINSA                        prudente

  Duplicar capacidades de     Media                 enfocar flujo
  Minsa Digital sin                                 hospitalario y
  diferenciación                                    métricas operativas

  Lista de especialidades     Media                 usar cartera 2025
  incompleta/desactualizada                         como source of truth
                                                    y catálogo dinámico

  Double booking              Alta técnica          constraints +
                                                    transacciones + tests
                                                    concurrentes

  Doble aceptación de cupo    Alta técnica          locking/update
  liberado                                          atómico +
                                                    idempotencia + tests
                                                    concurrentes

  Reasignación automática     Alta funcional/ética  consentimiento
  injusta                                           explícito + política
                                                    de prioridad
                                                    validada + auditoría

  Prioridad ambulatoria mal   Alta                  solo personal
  clasificada                 clínica/seguridad     autorizado +
                                                    criterios
                                                    institucionales +
                                                    trazabilidad + nunca
                                                    sustituir emergencia

  Usar niveles de emergencia  Alta clínica          mantener emergencia
  I--IV como prioridad de                           separada; la NTS de
  cita                                              Emergencia del MINSA
                                                    regula ese contexto y
                                                    no debe trasladarse
                                                    automáticamente a
                                                    consulta externa
                                                    \[I19\]

  Portal público con          Media reputacional    source of truth,
  información desactualizada                        fecha de vigencia y
                                                    revisión de contenido

  Uso no autorizado de        Media                 contenido propio,
  imágenes/videos             legal/reputacional    autorizado o con
                                                    licencia/permiso
                                                    verificable

  Push no entregado           Media                 no asumir lectura;
                                                    reintentos/control de
                                                    estado; canal alterno
                                                    si se aprueba

  Notificación con datos      Alta privacidad       contenido mínimo,
  sensibles                                         previews seguras y
                                                    preferencias

  Sobre-notificación/fatiga   Media UX              ventanas
  del usuario                                       configurables y
                                                    reglas aprobadas

  Prometer eliminar           Alta académica        medir impacto real y
  no-show/colas al 100 %                            redactar objetivos
                                                    como contribución, no
                                                    garantía

  Introducir datos clínicos   Alta privacidad       estado operativo sin
  por triaje                                        signos vitales en MVP

  Frontend directo a DB       Alta seguridad        Frontend → Spring
                                                    Boot → PostgreSQL

  Overengineering             Media                 dependencias por
                                                    trigger, no por moda

  `postgres:latest`           Media                 fijar versión
                                                    reproducible

  Fuentes académicas no       Alta académica        no incorporar
  verificables del informe                          datos/resultados sin
  interno                                           PDF/DOI/repo
                                                    verificable

  Exposición de datos         Alta                  datos sintéticos,
  personales                                        minimización,
                                                    RBAC/BOLA controls

  Interfaces que dependan     Alta accesibilidad    texto + iconos +
  solo del color                                    contraste + pruebas
                                                    WCAG y modos de
                                                    visión de color

  Usar "tema para daltónicos" Media UX              diseño base
  como única medida de                              accesible; los modos
  accesibilidad                                     cromáticos son apoyo
                                                    adicional

  Scope creep hacia HCE       Alta                  mantener módulo
  clínica antes de cerrar el  técnica/académica     clínico como FASE 18
  MVP                                               FUTURO

  Confundir un PDF con receta Alta legal/funcional  documento
  oficial o autorización SIS                        estructurado + firma
                                                    cuando aplique +
                                                    proceso SIS oficial
                                                    validado

  Acceso indebido a           Alta privacidad       threat model clínico,
  notas/prescripciones                              object authorization,
  futuras                                           auditoría y mínimo
                                                    privilegio
  -----------------------------------------------------------------------

------------------------------------------------------------------------

## 29. Guía para levantamiento de campo

La visita debe reconstruir el proceso, no buscar confirmar una hipótesis
preconcebida.

### 29.1 Citas

-   ¿Qué canales existen hoy para solicitar una cita?
-   ¿Paciente nuevo y continuador siguen el mismo proceso?
-   ¿Cómo intervienen SIS, referencias o condición de pago?
-   ¿Quién configura cupos y horarios?
-   ¿Cómo se gestionan cancelaciones y reprogramaciones?
-   ¿Qué ocurre con un no-show?
-   ¿Cuándo se considera oficialmente que una cita es no-show?
-   ¿Cuántas inasistencias se registran de forma agregada?
-   ¿Actualmente se envían recordatorios? ¿Por qué canal y con cuánta
    anticipación?
-   ¿Puede el paciente confirmar asistencia?
-   ¿Puede cancelar o reprogramar sin acudir físicamente?
-   ¿Qué ocurre con el cupo después de una cancelación?
-   ¿Existe lista de espera o mecanismo para cubrir cupos liberados?
-   ¿Existe actualmente priorización de citas ambulatorias?
-   ¿Quién está autorizado para revisar/validar una prioridad?
-   ¿Qué criterios o documentos justifican una prioridad?
-   ¿La prioridad modifica fecha, lista de espera, cupos reservados o
    únicamente seguimiento?
-   ¿Qué debe hacer el sistema si la situación corresponde a emergencia
    y no a una cita?
-   ¿Qué reglas de prioridad serían válidas para ofrecer un cupo
    liberado?
-   ¿Con cuánto tiempo mínimo debería cancelarse para que el cupo pueda
    reutilizarse?
-   ¿Qué datos de contacto están autorizados para
    recordatorios/notificaciones?
-   ¿Qué sistema informático se utiliza?
-   ¿Minsa Digital u otra plataforma participa?

### 29.2 Llegada y admisión

-   ¿Dónde llega primero el paciente?
-   ¿Cómo se verifica identidad/cita?
-   ¿Existe ticket o número de cola?
-   ¿Cómo se vincula con archivo/historia?
-   ¿Cuánto demora esta etapa?

### 29.3 Triaje

-   ¿Qué especialidades pasan por triaje?
-   ¿Triaje ocurre antes o después de admisión?
-   ¿Qué información es administrativa y cuál clínica?
-   ¿Qué estado del flujo podría digitalizarse sin manejar HCE?

### 29.4 Sala de espera y consulta

-   ¿Cómo se ordena la cola?
-   ¿Cómo se llama al paciente?
-   ¿Cómo se gestiona ausencia temporal?
-   ¿Cómo sabe el médico qué paciente sigue?
-   ¿Cómo se registra fin de atención?

### 29.5 Salida

-   ¿Cómo se programa control?
-   ¿Cómo se deriva a laboratorio, imágenes o farmacia?
-   ¿Qué puede digitalizarse solo como referencia/estado sin implementar
    dichos sistemas?

### 29.6 Capacidad y especialidades

-   lista vigente de especialidades;
-   procedimientos ambulatorios;
-   número de consultorios activos actuales;
-   turnos;
-   duración típica por servicio;
-   restricciones de asignación de consultorio;
-   información disponible sobre transición a contingencia/nueva
    infraestructura.

### 29.7 Métricas

Solicitar únicamente información **agregada y autorizada**, por ejemplo:

-   citas por día/mes;
-   cancelaciones;
-   no-show;
-   confirmaciones;
-   cancelaciones anticipadas/tardías;
-   cupos liberados y reasignados;
-   aceptación/rechazo de ofertas de lista de espera;
-   tiempos promedio por etapa;
-   demanda por especialidad;
-   capacidad de agenda;
-   incidencias operativas.

No solicitar historias clínicas ni bases de datos identificables.

------------------------------------------------------------------------

### 29.8 Portal público e información institucional

-   ¿Qué información pública del hospital debe mostrarse
    obligatoriamente?
-   ¿Cuál es la ubicación/contacto oficial que debe mantenerse vigente?
-   ¿Qué especialidades y servicios pueden publicarse?
-   ¿Qué horarios, requisitos y políticas pueden exponerse al público?
-   ¿Quién valida cambios de contenido institucional?
-   ¿Existen imágenes/videos autorizados para difusión?
-   ¿Qué avisos operativos requieren publicación rápida?
-   ¿La reserva web debe ser accesible sin cuenta hasta seleccionar
    disponibilidad o exige identificación previa?
-   ¿Qué especialidades deben aparecer como "disponibles para cita" y
    cuáles solo como parte de la cartera general?

### 29.9 Accesibilidad y preferencias visuales

Preguntar/observar, sin recopilar datos médicos innecesarios:

-   barreras visuales frecuentes en pacientes y personal;
-   uso de teléfonos con modo oscuro, aumento de texto o alto contraste;
-   colores actualmente utilizados para estados/turnos y si generan
    confusión;
-   necesidad de señalización redundante mediante texto/iconos;
-   condiciones de iluminación en ventanillas, pasillos y salas de
    espera;
-   dispositivos y tamaños de pantalla más comunes.

La presencia de daltonismo en una persona concreta no necesita
registrarse como dato médico para ofrecer la preferencia visual; el
usuario puede activar el modo de forma voluntaria.

### 29.10 Documentación clínica y cobertura --- exploración FUTURA

Sin capturar historias clínicas reales, levantar únicamente el proceso y
reglas institucionales:

-   dónde documenta hoy el médico la consulta;
-   si utiliza SIHCE u otro sistema;
-   cómo se generan y firman recetas/órdenes;
-   qué documentos se imprimen y por qué;
-   qué diferencia existe entre receta, orden, referencia, indicación y
    documento de cobertura;
-   cómo se valida actualmente la condición SIS y qué sistema/persona
    autoriza cada proceso;
-   qué información requiere farmacia, laboratorio, imágenes u otra
    UPSS;
-   qué documentos puede recibir digitalmente el paciente;
-   qué integraciones oficiales existen y cuáles están prohibidas o no
    disponibles.

Este levantamiento no autoriza implementar HCE, receta electrónica o
cobertura SIS dentro del MVP.

------------------------------------------------------------------------

## 30. Próximos pasos concretos

1.  Validar con el docente el **Hospital de Huaycán**, el nuevo título
    centrado en citas + flujo ambulatorio y dejar registrada la app
    móvil como alcance obligatorio.
2.  Sustituir en la documentación académica las referencias a Vitarte,
    sin hacer reemplazo mecánico: reescribir problema y contexto.
3.  Revisar el informe interno del compañero y eliminar/aislar
    investigaciones adicionales que no puedan verificarse con fuente
    original.
4.  Obtener y revisar completamente el **MAPRO de Consulta Externa y
    Hospitalización** y el anexo de la **Cartera de Servicios 2025**.
5.  Preparar instrumento de levantamiento de campo incluyendo no-show,
    confirmación, cancelación, reprogramación, reutilización de cupos,
    lista de espera y canales de notificación.
6.  Realizar discovery y BPMN AS-IS.
7.  Cerrar PRD, reglas de negocio y alcance MVP web+móvil.
8.  Definir política de confirmación (`T-7` configurable), waitlist,
    priorización ambulatoria, rol validador, expiración y aceptación de
    cupos.
9.  Diseñar TO-BE y UI/UX coordinados para portal público + portal de
    reservas + web operativa + app móvil, incluyendo claro/oscuro, alto
    contraste y modos de apoyo para protanopia, deuteranopia y
    tritanopia.
10. Completar TRD, ADRs, modelo de datos y OpenAPI compartido por
    web/móvil.
11. Recién entonces iniciar las primeras clases de negocio y scaffolds
    de frontend/mobile.

------------------------------------------------------------------------

## 31. Fuentes institucionales verificadas

**\[I1\] PRONIS --- Viabilidad del proyecto del Hospital de Huaycán
(2021).**\
https://www.gob.pe/institucion/pronis/noticias/509319-pronis-viabiliza-proyecto-para-construir-nuevo-hospital-de-huaycan-en-ate

**\[I2\] PRONIS --- Expediente técnico del Hospital de Huaycán,
inversión \> S/ 460 millones (11/02/2026).**\
https://www.gob.pe/institucion/pronis/noticias/1351591-pronis-aprueba-expediente-tecnico-del-hospital-de-huaycan-con-inversion-superior-a-s-460-millones

**\[I3\] DIRIS Lima Este --- Actualización de la Cartera de Servicios
del Hospital de Huaycán, categoría II-1 (09/09/2025).**\
https://www.gob.pe/institucion/dirislimaeste/normas-legales/7165172-000687-2025-dg-diris-le

**\[I4\] Hospital de Huaycán --- MAPRO del Servicio de Consulta Externa
y Hospitalización (05/06/2025).**\
https://www.gob.pe/institucion/hospitalhuaycan/informes-publicaciones/6839371-mapro-servicio-de-consulta-externa-y-hospitalizacion-hh

**\[I5\] Hospital de Huaycán --- Selección, Admisión y Archivo del
Servicio de Consulta Externa y Hospitalización (RD 033-2026).**\
https://www.gob.pe/institucion/hospitalhuaycan/normas-legales/7732597-033-2026-d-hh-minsa

**\[I6\] Hospital de Huaycán --- Plan Cero Colas 2026 (RD 216-2026).**\
https://www.gob.pe/institucion/hospitalhuaycan/normas-legales/7990532-216-2026-d-hh-minsa

**\[I7\] Hospital de Huaycán --- Plan Cero Colas 2026 V2 (RD
254-2026).**\
https://www.gob.pe/institucion/hospitalhuaycan/normas-legales/8114750-254-2026-d-hh-minsa

**\[I8\] Hospital de Huaycán --- Plan Cero Colas 2024.**\
https://cdn.www.gob.pe/uploads/document/file/6162407/5436766-067-2024-plan-cero-colas-2024.pdf

**\[I9\] MINSA --- Obtener cita médica en un establecimiento del
Minsa.**\
https://www.gob.pe/32985-obtener-cita-medica-en-un-establecimiento-del-minsa

**\[I10\] DIRIS Lima Este --- Apertura de citas en línea para atención
en centros de salud.**\
https://www.gob.pe/institucion/dirislimaeste/noticias/921110-diris-lima-esta-apertura-citas-en-linea-para-atencion-en-centros-de-salud

**\[I11\] Hospital de Huaycán --- ASISHO 2022 (cartera de servicios
histórica, usar solo como referencia y contrastar con 2025).**\
https://cdn.www.gob.pe/uploads/document/file/5219397/ASISHO%202022.pdf

**\[I12\] Hospital de Huaycán --- Campaña de salud con
servicios/especialidades (26/07/2025).**\
https://www.gob.pe/institucion/hospitalhuaycan/campa%C3%B1as/115784-pobladores-de-huaycan-se-benefiaron-con-campana-de-salud-gratuita

**\[I13\] Hospital de Huaycán --- Fortalecimiento de cardiología con
equipo Holter (2026).**\
https://www.gob.pe/institucion/hospitalhuaycan/noticias/1363225-hospital-de-huaycan-recibe-donacion-de-equipo-holter-para-fortalecer-cardiologia

**\[I14\] MINSA --- Implementación del Sistema de Información de
Historias Clínicas Electrónicas (SIHCE).**\
https://www.gob.pe/institucion/minsa/noticias/584767-minsa-aprueba-documento-tecnico-para-la-implementacion-del-sistema-de-informacion-de-historias-clinicas-electronicas

**\[I15\] MINSA --- Directiva sobre firma electrónica y firma digital en
actos médicos y actos de salud (RM 462-2023-MINSA).**\
https://www.gob.pe/institucion/minsa/normas-legales/4234106-462-2023-minsa

**\[I16\] DIGEMID --- Estándar de transacción de receta electrónica (RM
079-2022/MINSA).**\
https://www.digemid.minsa.gob.pe/webDigemid/normas-legales/2022/resolucion-ministerial-n-079-2022-minsa/

**\[I17\] Seguro Integral de Salud --- Preguntas frecuentes sobre
cobertura.**\
https://www.gob.pe/institucion/sis/informes-publicaciones/7564875-preguntas-frecuentes

**\[I18\] Hospital de Huaycán --- Portal institucional en Gob.pe.**\
https://www.gob.pe/hospitalhuaycan

**\[I19\] MINSA --- Norma Técnica de Salud de los Servicios de
Emergencia N.° 042-MINSA/DGSP-V.01.**\
https://www.gob.pe/institucion/minsa/informes-publicaciones/353462-norma-tecnica-de-salud-de-los-servicios-de-emergencia-nt-n-042-minsa-dgsp-v-01

------------------------------------------------------------------------

## 32. Fuentes técnicas primarias de referencia

-   Spring Boot: https://docs.spring.io/spring-boot/
-   Spring Security: https://docs.spring.io/spring-security/reference/
-   Spring Modulith: https://docs.spring.io/spring-modulith/reference/
-   PostgreSQL 17: https://www.postgresql.org/docs/17/
-   Supabase Database: https://supabase.com/docs/guides/database
-   Next.js App Router: https://nextjs.org/docs/app
-   React Native: https://reactnative.dev/docs/getting-started
-   Expo: https://docs.expo.dev/
-   Tailwind CSS: https://tailwindcss.com/docs
-   OpenAPI: https://spec.openapis.org/oas/
-   HL7 FHIR: https://hl7.org/fhir/
-   OWASP Top 10: https://owasp.org/Top10/
-   OWASP API Security: https://owasp.org/www-project-api-security/
-   GitHub Actions Java/Maven:
    https://docs.github.com/actions/tutorials/build-and-test-code/java-with-maven
-   Playwright: https://playwright.dev/docs/intro
-   Grafana k6: https://grafana.com/docs/k6/latest/
-   W3C WCAG 2.2: https://www.w3.org/TR/WCAG22/
-   W3C Understanding Use of Color 1.4.1:
    https://www.w3.org/WAI/WCAG22/Understanding/use-of-color.html
-   W3C Understanding Contrast Minimum 1.4.3:
    https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum

------------------------------------------------------------------------

## 33. Checklist de control antes de programar negocio

-   [ ] Huaycán validado con el docente.
-   [ ] Título/problema revisados.
-   [ ] MAPRO leído completamente.
-   [ ] Cartera 2025 y especialidades validadas.
-   [ ] Trabajo de campo realizado o sustituido por evidencia
    institucional suficiente.
-   [ ] BPMN AS-IS validado.
-   [ ] TO-BE aprobado.
-   [ ] PRD completado.
-   [ ] RF/RNF y reglas aprobados.
-   [ ] UI/UX de flujos críticos web y móvil aprobado.
-   [ ] Accesibilidad base validada: claro/oscuro/alto contraste,
    protanopia, deuteranopia y tritanopia.
-   [ ] Estados críticos comprensibles sin depender exclusivamente del
    color.
-   [ ] Política de confirmación/recordatorios aprobada.
-   [ ] Política de waitlist/prioridad/expiración aprobada.
-   [ ] Rol/permiso de revisión de prioridad validado con el hospital.
-   [ ] Prioridad ambulatoria diferenciada explícitamente del triaje de
    emergencia.
-   [ ] Arquitectura del portal público + portal de reservas aprobada.
-   [ ] Fuente de verdad de contenido institucional, especialidades,
    ubicación, políticas y multimedia definida.
-   [ ] Menú de especialidades disponibles obtiene disponibilidad del
    backend, no de valores hardcodeados.
-   [ ] Portal público incluye disclaimer de prototipo académico
    mientras no exista autorización institucional.
-   [ ] Estrategia de autenticación móvil aprobada.
-   [ ] Estrategia/proveedor de push notifications definida para MVP.
-   [ ] TRD y módulos aprobados.
-   [ ] Threat model inicial completado.
-   [ ] Modelo de datos aprobado.
-   [ ] Estrategia de double booking definida.
-   [ ] OpenAPI candidato aprobado para web y móvil.
-   [ ] PostgreSQL Testcontainers fijado a versión reproducible.
-   [ ] CI backend verde.
-   [ ] Expansión clínica/documental explícitamente fuera del MVP
    inicial salvo cambio de alcance aprobado.

Solo después de cumplir estos gates se crearán las primeras clases de
negocio y los scaffolds funcionales de web/móvil. La aplicación móvil
forma parte del MVP y no debe postergarse como "fase opcional
posterior".

## Relación con unidades de aprendizaje del curso

La planificación del proyecto se alinea con las etapas académicas:

### Unidad 1 --- Planificación y análisis

-   Lean Canvas.
-   Project Charter.
-   Requerimientos del sistema.
-   Identificación del problema.
-   Alcance inicial del MVP.

### Unidad 2 --- Diseño

-   BPMN AS-IS / TO-BE.
-   Diseño de base de datos.
-   UX/UI.
-   Documentación técnica.

### Unidad 3 --- Desarrollo

-   Arquitectura MVC/modular.
-   Java y Spring Boot.
-   Git/GitHub.
-   Implementación backend y frontend.

### Unidad 4 --- Pruebas, despliegue y mantenimiento

-   Testing.
-   Seguridad.
-   Despliegue.
-   Monitoreo.
-   Mantenimiento.
