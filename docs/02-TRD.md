# 02 - TRD v1.2 FINAL (Technical Requirements Document)

## Estado del documento

**Versión:** 1.2 FINAL (Ajustada)

**Producto:** Plataforma web y móvil modular para la gestión de atención
ambulatoria, citas médicas y trazabilidad del flujo asistencial.

**Documento relacionado:** PRD v1.4 FINAL.

**Tipo de proyecto:** Caso de estudio académico de Ingeniería de
Software.

**Control de vigencia E.3.2 (30/09/2026):** el stack móvil objetivo del roadmap es React Native/Expo y PostgreSQL 17 es un objetivo documental, sin despliegue acreditado aquí. El backend presente usa Java 21/Spring Boot 3.5.14 y migraciones V1–V3. `catalogs`, `priority`, `waitlist`, `notifications` y `dashboard` no tienen módulo funcional Java; sus paquetes vacíos no prueban entrega. Los apartados de triaje clínico y registro de diagnóstico quedan como diseño histórico **fuera del MVP actual** según el baseline y ADR-007. Los únicos roles de negocio aprobados para el caso actual son ADMIN, PATIENT, RECEPTIONIST y PROFESSIONAL (DEC-002). Esta nota no crea módulos ni cambia decisiones abiertas.

------------------------------------------------------------------------

# 1. Información técnica general

## Propósito

El presente documento define los requerimientos técnicos, arquitectura,
componentes, tecnologías y decisiones de diseño necesarias para
implementar una plataforma digital orientada a la gestión de atención
ambulatoria.

La solución permitirá administrar:

-   citas médicas;
-   agendas;
-   disponibilidad;
-   flujo asistencial;
-   gestión operativa;
-   contenido institucional;
-   métricas administrativas;
-   auditoría.

El TRD transforma los requerimientos del PRD en una arquitectura
modular, segura, mantenible y preparada para evolución futura.

------------------------------------------------------------------------

# 2. Objetivo técnico

Diseñar una arquitectura tecnológica compuesta por:

-   Portal público institucional.
-   Portal web paciente.
-   Aplicación móvil.
-   Plataforma operativa para personal autorizado.
-   Panel administrativo.
-   API REST centralizada.
-   Base de datos relacional.

La solución deberá soportar:

-   Gestión de usuarios.
-   Gestión de roles y permisos.
-   Gestión de pacientes.
-   Gestión de profesionales.
-   Gestión de especialidades.
-   Gestión de contenido institucional.
-   Gestión de agendas.
-   Gestión de citas.
-   Recepción.
-   Triaje básico.
-   Registro profesional básico.
-   Dashboards administrativos.
-   Flujo asistencial.
-   Auditoría.
-   Notificaciones.

------------------------------------------------------------------------

# 3. Principios de diseño

## 3.1 Arquitectura modular

El sistema será desarrollado mediante módulos independientes organizados
por dominio de negocio.

Beneficios:

-   separación de responsabilidades;
-   mantenibilidad;
-   facilidad de evolución;
-   reducción de acoplamiento.

------------------------------------------------------------------------

## 3.2 Monolito modular

La solución utilizará arquitectura de monolito modular.

Permite:

-   reducir complejidad inicial;
-   mantener separación lógica;
-   facilitar desarrollo académico;
-   permitir evolución futura.

No se implementará arquitectura de microservicios en esta fase.

------------------------------------------------------------------------

## 3.3 API First

La comunicación entre aplicaciones se realizará mediante API REST
centralizada.

Permite:

-   reutilización entre web y móvil;
-   separación frontend/backend;
-   pruebas independientes;
-   documentación mediante OpenAPI.

------------------------------------------------------------------------

## 3.4 Seguridad desde el diseño

La seguridad será considerada mediante:

-   autenticación;
-   autorización;
-   control de permisos;
-   protección de datos;
-   auditoría.

------------------------------------------------------------------------

## 3.5 Diseño orientado al dominio

Los módulos serán organizados según responsabilidades del negocio.

Cada módulo deberá contener:

-   reglas propias;
-   servicios asociados;
-   entidades relacionadas;
-   validaciones correspondientes.

------------------------------------------------------------------------

# 4. Arquitectura general del sistema

``` text
Usuarios

        |
        +----------------------+
        |                      |
    React Web (objetivo)  React Native/Expo App (objetivo)
        |                      |
        +----------------------+

                  |

            API REST HTTPS

                  |

        Spring Boot Backend

          Monolito Modular

                  |

             PostgreSQL
```

------------------------------------------------------------------------

# 5. Stack tecnológico

## Backend

Tecnologías:

-   Java 21 LTS.
-   Spring Boot 3.x.
-   Spring Security.
-   Spring Data JPA.
-   Hibernate.

Responsabilidades:

-   lógica negocio;
-   API;
-   seguridad;
-   validaciones;
-   persistencia.

------------------------------------------------------------------------

## Frontend Web

Tecnologías:

-   React.
-   TypeScript.

Responsabilidades:

-   interfaces web;
-   paneles operativos;
-   consumo API.

------------------------------------------------------------------------

## Aplicación móvil

Tecnología:

-   React Native/Expo (objetivo documental; app no implementada).

Responsabilidades:

-   experiencia móvil;
-   gestión citas;
-   consulta información;
-   notificaciones.

------------------------------------------------------------------------

## Base de datos

Tecnología:

-   PostgreSQL 17 (objetivo del roadmap; despliegue no acreditado).

Responsabilidades:

-   persistencia;
-   integridad;
-   transacciones.

------------------------------------------------------------------------

# 6. Comunicación entre componentes

La comunicación utilizará:

-   HTTPS.
-   REST.
-   JSON.
-   JWT.

Las operaciones deberán validar:

-   identidad;
-   permisos;
-   reglas negocio.

------------------------------------------------------------------------

# 7. Componentes del sistema

## Portal público institucional

Funciones:

-   información institucional;
-   especialidades;
-   servicios;
-   acceso reservas.

Usuario:

-   visitante.

------------------------------------------------------------------------

## Portal paciente

Funciones:

-   registro;
-   autenticación;
-   perfil;
-   disponibilidad;
-   reserva;
-   cancelación;
-   reprogramación.

Usuario:

-   paciente.

------------------------------------------------------------------------

## Aplicación móvil

Funciones:

-   gestión citas;
-   consulta estados;
-   recordatorios.

Usuario:

-   paciente.

------------------------------------------------------------------------

## Plataforma operativa

Componentes:

``` text
Panel administrador

Panel recepción

Panel triaje

Panel profesional

Dashboard operativo
```

Usuarios:

-   administrador;
-   recepcionista;
-   triaje;
-   profesional.

------------------------------------------------------------------------

# 8. Diseño lógico de módulos backend

## Módulo identidad y autorización

Responsabilidades:

-   usuarios;
-   autenticación;
-   roles;
-   permisos;
-   sesiones.

------------------------------------------------------------------------

## Módulo catálogo y contenido institucional

Responsabilidades:

-   especialidades;
-   servicios;
-   información pública;
-   imágenes;
-   descripciones;
-   estado activo/inactivo.

------------------------------------------------------------------------

## Módulo pacientes

Responsabilidades:

-   datos paciente;
-   perfil;
-   relación citas.

------------------------------------------------------------------------

## Módulo profesionales

Responsabilidades:

-   información profesional;
-   especialidades asociadas;
-   agenda.

------------------------------------------------------------------------

## Módulo agenda

Responsabilidades:

-   horarios;
-   disponibilidad;
-   cupos.

------------------------------------------------------------------------

## Módulo citas

Responsabilidades:

-   creación;
-   confirmación;
-   cancelación;
-   reprogramación;
-   control doble reserva.

------------------------------------------------------------------------

## Módulo recepción

Responsabilidades:

-   búsqueda paciente;
-   validación cita;
-   registro llegada;
-   admisión.

------------------------------------------------------------------------

## Módulo triaje — diseño histórico fuera del MVP actual

Responsabilidades:

-   evaluación inicial;
-   transición flujo asistencial.

------------------------------------------------------------------------

## Módulo atención profesional — separar flujo operativo vigente de registro clínico histórico

Responsabilidades:

-   observaciones;
-   diagnóstico básico;
-   indicaciones.

No incluye historia clínica electrónica completa.

------------------------------------------------------------------------

## Módulo flujo asistencial

Responsabilidades:

-   estados de atención;
-   transición entre etapas;
-   seguimiento del recorrido del paciente.

Ejemplo:

``` text
CHECK-IN

↓

TRIAJE

↓

EN_ESPERA

↓

EN_ATENCION

↓

FINALIZADO
```

------------------------------------------------------------------------

## Módulo dashboard

Responsabilidades:

-   métricas;
-   filtros de fechas;
-   indicadores operativos;
-   configuración de visualización.

------------------------------------------------------------------------

## Módulo notificaciones

Responsabilidades:

-   recordatorios;
-   avisos;
-   confirmaciones.

------------------------------------------------------------------------

## Módulo auditoría

Responsabilidades:

-   registro acciones;
-   historial cambios;
-   eventos relevantes.

------------------------------------------------------------------------

# 9. Seguridad técnica

## Autenticación

Tecnología:

-   Spring Security.
-   JWT.

Credenciales:

-   almacenamiento seguro;
-   hashing BCrypt.

------------------------------------------------------------------------

## Autorización

Control basado en roles y permisos.

Roles del diseño histórico; `ROLE_TRIAGE` no tiene operación de negocio actual y DEC-002 delimita los cuatro roles vigentes:

``` text
ROLE_PATIENT

ROLE_ADMIN

ROLE_RECEPTIONIST

ROLE_TRIAGE

ROLE_PROFESSIONAL
```

La validación granular de permisos prevista por este diseño no es un control
universal actual. Los controllers usan controles por rol y ownership; DEC-020
mantiene la granularidad de permisos como `PROPOSED`.

------------------------------------------------------------------------

## Auditoría

Registrar:

-   usuario;
-   acción;
-   fecha;
-   recurso afectado;
-   resultado operación.

------------------------------------------------------------------------

# 10. Modelo inicial de datos

Entidades principales:

``` text
Usuario

Rol

Permiso

Paciente

Profesional

Especialidad

EspecialidadContenido

Agenda

Disponibilidad

Cita

EstadoCita

EstadoAtencion

HistorialCita

RegistroRecepcion

EvaluacionTriaje

AtencionProfesional

DashboardMetric

DashboardConfig

Notificacion

Auditoria
```

Relaciones:

``` text
Usuario
 |
 +-- Paciente

Usuario
 |
 +-- Rol
 |
 +-- Permisos


Profesional
 |
 +-- Especialidad


Paciente
 |
 +-- Cita


Cita
 |
 +-- Historial


Cita
 |
 +-- RegistroRecepcion


Cita
 |
 +-- AtencionProfesional
```

------------------------------------------------------------------------

# 11. Requerimientos técnicos

## Rendimiento

Debe soportar:

-   autenticación;
-   consultas;
-   reservas;
-   dashboards básicos.

------------------------------------------------------------------------

## Mantenibilidad

Mantener:

-   separación por capas;
-   modularidad;
-   documentación.

------------------------------------------------------------------------

## Observabilidad

Registrar:

-   logs técnicos;
-   errores;
-   eventos importantes;
-   métricas básicas.

------------------------------------------------------------------------

# 12. Restricciones técnicas

El proyecto considera:

-   caso académico;
-   datos sintéticos;
-   sin integración real MINSA;
-   sin historia clínica electrónica completa;
-   sin infraestructura hospitalaria productiva.

------------------------------------------------------------------------

# 13. Escalabilidad y evolución futura

La arquitectura permitirá incorporar:

-   farmacia;
-   facturación;
-   laboratorio;
-   integraciones externas;
-   reportes avanzados;
-   interoperabilidad sanitaria.

------------------------------------------------------------------------

# 14. Decisiones técnicas iniciales

  Decisión              Justificación
  --------------------- --------------------------------------------
  Monolito modular      Reduce complejidad y mantiene organización
  Spring Boot           Framework empresarial robusto
  React + TypeScript    Frontend objetivo, no implementado
  React Native/Expo     Aplicación móvil objetivo, no implementada
  PostgreSQL            Integridad relacional
  JWT                   Seguridad API
  OpenAPI               Documentación y pruebas
  Auditoría             Trazabilidad operacional
  Módulos por dominio   Evolución futura

------------------------------------------------------------------------

**Documento actualizado como TRD v1.2 FINAL ajustado.**
