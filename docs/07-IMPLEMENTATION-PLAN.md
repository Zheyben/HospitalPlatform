# 07 - IMPLEMENTATION PLAN v1.3.2 FINAL

## Estado del documento

**Versión:** 1.3.2 FINAL (FASE 5.11.1)

**Producto:** Plataforma web y móvil modular para la gestión de atención
ambulatoria, citas médicas y trazabilidad del flujo asistencial.

**Documentos relacionados:**

-   PRD v1.4 FINAL
-   TRD v1.2 FINAL ajustado
-   APP FLOW v1.3 FINAL
-   UI/UX DESIGN BRIEF v1.2.2 FINAL
-   BACKEND SCHEMA v1.4.1 FINAL
-   API SPECIFICATION v1.3 FINAL
-   SECURITY THREAT MODEL
-   TEST STRATEGY
-   TEST PLAN
-   DEPLOYMENT PLAN
-   MONITORING & MAINTENANCE

------------------------------------------------------------------------

# Ajustes aplicados en esta versión

-   Incorporación explícita del módulo Administración del sistema dentro
    del MVP.
-   Inclusión de gestión de contenido público dentro del panel
    administrador.
-   Ampliación del plan de seguridad técnica.
-   Integración de tipos de pruebas esperadas.
-   Inclusión de migraciones versionadas de base de datos.
-   Nuevo riesgo relacionado al crecimiento del alcance MVP.

------------------------------------------------------------------------

# 1. Información general

Este documento define el plan de implementación de la solución
tecnológica, estableciendo:

-   estrategia de construcción;
-   fases de desarrollo;
-   prioridades técnicas;
-   entregables;
-   orden recomendado de implementación.

La implementación considera:

-   Backend.
-   Frontend web.
-   Aplicación móvil.
-   Base de datos PostgreSQL.
-   API REST.
-   Seguridad.
-   Validación progresiva.

------------------------------------------------------------------------

# 2. Objetivo del plan

Definir una estrategia ordenada para construir la plataforma reduciendo
riesgos técnicos y asegurando entregas funcionales verificables.

------------------------------------------------------------------------

# 3. Estrategia de implementación

La construcción seguirá un enfoque:

``` text
Desarrollo incremental

+

Validación progresiva

+

Entrega funcional por etapas
```

Cada etapa deberá generar una funcionalidad verificable antes de
avanzar.

------------------------------------------------------------------------

# 4. Metodología de desarrollo

Se utilizará un enfoque basado en principios ágiles mediante iteraciones
cortas.

Cada iteración contempla:

-   planificación;
-   desarrollo;
-   revisión;
-   pruebas;
-   ajustes.

------------------------------------------------------------------------

# 5. Fases del proyecto

## Fase 0 - Preparación técnica

Actividades:

-   configuración repositorios;
-   estructura backend;
-   estructura frontend;
-   configuración PostgreSQL;
-   configuración ambientes;
-   estándares desarrollo;
-   arquitectura base;
-   seguridad inicial.

Resultado:

Base técnica preparada para desarrollo.

------------------------------------------------------------------------

## Fase 1 - Fundamentos de plataforma

Incluye:

-   autenticación;
-   JWT;
-   usuarios;
-   roles;
-   permisos;
-   autorización RBAC;
-   auditoría inicial.

Resultado:

Usuarios acceden según permisos.

------------------------------------------------------------------------

## Fase 2 - Catálogos y administración

Incluye:

-   especialidades;
-   contenido público;
-   profesionales;
-   agendas;
-   configuración administrativa;
-   administración del sistema.

Resultado:

Sistema preparado para gestionar configuración operativa y pública.

------------------------------------------------------------------------

## Fase 3 - Gestión de citas MVP

Incluye:

-   disponibilidad;
-   reserva;
-   confirmación;
-   cancelación;
-   reprogramación.

------------------------------------------------------------------------

## Fase 4 - Flujo asistencial

Para el incremento backend FASE 5.11 incluye únicamente:

-   recepción;
-   check-in;
-   espera;
-   atención profesional;
-   trazabilidad.

Workflow:

``` text
CHECK_IN

↓

WAITING

↓

IN_ATTENTION

↓

FINISHED
```

El flujo inicia con `AppointmentStatus = CONFIRMED` y `flowStage = null`.
Al llegar a `FINISHED`, la cita cambia a `AppointmentStatus = COMPLETED`.
Admisión independiente, triaje, prioridad clínica, queue tickets y reglas
temporales permanecen fuera de este incremento.

------------------------------------------------------------------------

## Fase 5 - Operación complementaria

Incluye:

-   lista de espera;
-   recuperación de cupos;
-   notificaciones;
-   dashboards;
-   auditoría avanzada.

------------------------------------------------------------------------

## Fase 6 - Validación y cierre MVP

Incluye:

-   pruebas funcionales;
-   pruebas seguridad;
-   validación aceptación;
-   despliegue;
-   documentación final.

------------------------------------------------------------------------

# 6. Roadmap de implementación

Las iteraciones 6 y 7 describen la planificación macro del producto. En el
incremento backend FASE 5.11 solo se aprueban `CHECK_IN`, `WAITING`,
`IN_ATTENTION` y `FINISHED`; triaje continúa como capacidad futura.

  Iteración     Objetivo
  ------------- -------------------------------------------
  Iteración 1   Preparación técnica y arquitectura base
  Iteración 2   Usuarios, roles y permisos
  Iteración 3   Catálogos, especialidades y profesionales
  Iteración 4   Agendas y disponibilidad
  Iteración 5   Reserva y gestión de citas
  Iteración 6   Recepción y flujo operativo
  Iteración 7   Atención profesional y trazabilidad
  Iteración 8   Dashboard, notificaciones y auditoría
  Iteración 9   Seguridad, pruebas y cierre MVP

------------------------------------------------------------------------

# 7. Priorización de módulos

## Prioridad alta - MVP

-   autenticación;
-   usuarios;
-   roles;
-   permisos;
-   administración del sistema;
-   pacientes;
-   profesionales;
-   especialidades;
-   agendas;
-   disponibilidad;
-   citas;
-   recepción;
-   atención profesional.

Triaje permanece como prioridad futura sujeta a una decisión de dominio
independiente.

------------------------------------------------------------------------

## Prioridad media

-   trazabilidad avanzada;
-   lista de espera;
-   notificaciones;
-   dashboards.

------------------------------------------------------------------------

## Prioridad futura

-   reportes avanzados;
-   analítica avanzada;
-   integraciones externas;
-   funcionalidades inteligentes.

------------------------------------------------------------------------

# 8. Plan backend

Orden:

``` text
Configuración proyecto

↓

Seguridad

↓

Modelo datos

↓

Servicios negocio

↓

API REST

↓

Auditoría
```

Módulos:

-   auth;
-   users;
-   patients;
-   professionals;
-   catalog;
-   content-management;
-   schedules;
-   appointments;
-   reception;
-   triage;
-   attention;
-   waiting-list;
-   dashboard;
-   notifications;
-   audit;
-   reports.

------------------------------------------------------------------------

# 9. Plan frontend web

## Portal público

Funciones:

-   información institucional;
-   especialidades;
-   servicios;
-   contenido público;
-   reserva.

------------------------------------------------------------------------

## Panel administrador

Funciones:

-   usuarios;
-   roles;
-   permisos;
-   especialidades;
-   gestión contenido página pública;
-   agendas;
-   dashboards;
-   reportes.

------------------------------------------------------------------------

## Panel profesional

Funciones:

-   agenda diaria;
-   pacientes;
-   atención;
-   actualización estados.

------------------------------------------------------------------------

## Panel recepción

Funciones:

-   citas del día;
-   búsqueda pacientes;
-   check-in;
-   paso a espera.

------------------------------------------------------------------------

## Panel triaje

Estado: futuro, fuera de FASE 5.11.

Funciones:

-   pacientes pendientes;
-   evaluación inicial;
-   derivación.

------------------------------------------------------------------------

# 10. Plan aplicación móvil

Funciones MVP:

-   autenticación;
-   consulta citas;
-   reserva;
-   cancelación;
-   reprogramación;
-   notificaciones;
-   seguimiento asistencial.

------------------------------------------------------------------------

# 11. Plan base de datos

Proceso:

``` text
Modelo conceptual

↓

Modelo físico

↓

Constraints PostgreSQL

↓

Migraciones versionadas

↓

Validación
```

Considera:

-   PostgreSQL;
-   índices;
-   claves únicas;
-   auditoría;
-   integridad referencial.

Ejemplo de versionado:

``` text
V1_create_users.sql

V2_create_roles.sql

V3_create_appointments.sql
```

------------------------------------------------------------------------

# 12. Plan seguridad

La implementación debe considerar:

-   JWT;
-   RBAC;
-   protección de endpoints;
-   validación de permisos;
-   validación de entradas;
-   auditoría de operaciones críticas.

La definición detallada se desarrolla en:

``` text
08-SECURITY-THREAT-MODEL
```

------------------------------------------------------------------------

# 13. Plan testing

La validación contemplará:

## Pruebas unitarias

Validación de lógica interna.

## Pruebas integración API

Validación de:

-   endpoints;
-   permisos;
-   respuestas;
-   errores.

## Pruebas frontend

Validación de:

-   navegación;
-   formularios;
-   estados visuales.

## Pruebas aceptación

Validación de requisitos funcionales.

Documentos relacionados:

``` text
09-TEST-STRATEGY

10-TEST-PLAN
```

------------------------------------------------------------------------

# 14. Plan integración API

Proceso:

``` text
Implementación backend

↓

Validación endpoints

↓

Integración frontend

↓

Pruebas funcionales
```

Validar:

-   contratos API;
-   permisos;
-   respuestas;
-   errores.

------------------------------------------------------------------------

# 15. Control de versiones

Herramienta:

Git.

Estrategia:

``` text
main

develop

feature/*
```

------------------------------------------------------------------------

# 16. Ambientes de trabajo

## Desarrollo

Construcción diaria.

## Testing

Validación funcional y técnica.

## Staging

Validación previa a producción.

## Producción

Operación final.

------------------------------------------------------------------------

# 17. Roles del equipo

## Product Owner

-   requisitos;
-   prioridades;
-   validación funcional.

## Backend Developer

-   lógica negocio;
-   API;
-   base datos.

## Frontend Developer

-   interfaces;
-   integración API.

## Mobile Developer

-   aplicación móvil.

## QA

-   pruebas;
-   calidad.

## DevOps

-   ambientes;
-   despliegue;
-   mantenimiento técnico.

------------------------------------------------------------------------

# 18. Gestión de riesgos

  Riesgo                                 Mitigación
  -------------------------------------- --------------------------
  Cambios frecuentes requisitos          Control versiones PRD
  Errores integración                    Pruebas API progresivas
  Problemas seguridad                    Security Threat Model
  Problemas rendimiento                  Pruebas técnicas
  Complejidad flujo hospitalario         Implementación modular
  Dependencia validación institucional   Validación progresiva
  Crecimiento del alcance MVP            Priorización por módulos

------------------------------------------------------------------------

# 19. Entregables

## Documentales

-   PRD.
-   TRD.
-   APP FLOW.
-   UI/UX Design Brief.
-   Backend Schema.
-   API Specification.
-   Implementation Plan.
-   Security Threat Model.
-   Test Strategy.
-   Test Plan.
-   Deployment Plan.
-   Monitoring & Maintenance.

------------------------------------------------------------------------

## Técnicos

-   Backend.
-   Frontend web.
-   Aplicación móvil.
-   Base datos.
-   API REST.
-   Documentación técnica.

------------------------------------------------------------------------

# 20. Criterios de finalización MVP

El MVP será considerado completado cuando:

-   permita gestionar usuarios y permisos;
-   permita administrar configuración principal;
-   permita reservar citas;
-   controle disponibilidad;
-   gestione el flujo operativo aprobado de recepción y atención;
-   permita atención profesional;
-   registre trazabilidad;
-   aplique controles seguridad;
-   apruebe pruebas críticas;
-   pueda desplegarse en ambiente definido.

------------------------------------------------------------------------

**Documento actualizado como 07-IMPLEMENTATION-PLAN v1.3.2 FINAL.**
