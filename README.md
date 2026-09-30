# Hospital Platform

## Plataforma Web y Móvil para Gestión de Citas Médicas y Flujo de Consulta Externa

Proyecto académico basado en el caso de estudio del Hospital de
Huaycán - MINSA / DIRIS Lima Este.

> Esta solución no representa un software oficial del Hospital de
> Huaycán, MINSA, DIRIS Lima Este ni PRONIS.

------------------------------------------------------------------------

# Información del proyecto

**Tipo:** Proyecto académico de ingeniería de software.

**Objetivo:** Diseñar e implementar una plataforma web y móvil orientada
a optimizar la gestión de citas médicas y la trazabilidad del flujo de
consulta externa.

------------------------------------------------------------------------

# Estado actual

## Fase actual

Preparación de desarrollo.

El proyecto completó:

-   definición del producto;
-   arquitectura técnica;
-   diseño funcional;
-   modelo de seguridad;
-   estrategia de pruebas;
-   despliegue;
-   monitoreo;
-   decisiones arquitectónicas (ADR).

La implementación inicia después del cierre documental.

------------------------------------------------------------------------

# Alcance MVP

## Backend API

Responsable del núcleo del sistema.

Incluye:

-   autenticación;
-   autorización por roles;
-   usuarios;
-   pacientes;
-   profesionales;
-   especialidades;
-   agenda;
-   disponibilidad;
-   citas;
-   lista de espera;
-   prioridad ambulatoria;
-   notificaciones;
-   dashboards operativos;
-   auditoría.

Tecnologías:

-   Java 21
-   Spring Boot
-   Spring Security
-   Spring Data JPA
-   PostgreSQL
-   Flyway
-   Testcontainers

------------------------------------------------------------------------

## Plataforma Web

Cliente operativo principal.

Incluye:

-   portal público institucional;
-   reservas;
-   gestión operativa;
-   administración;
-   dashboards.

Tecnologías:

-   Next.js
-   React
-   TypeScript
-   Tailwind CSS
-   Playwright

------------------------------------------------------------------------

## Aplicación móvil

Cliente paciente.

Incluye:

-   consulta disponibilidad;
-   reserva;
-   seguimiento de citas;
-   confirmaciones;
-   cancelaciones;
-   reprogramaciones;
-   notificaciones.

Tecnología definida:

-   React Native
-   Expo
-   TypeScript

------------------------------------------------------------------------

# Roles del sistema

## MVP

  Rol            Responsabilidad
  -------------- -------------------------------------------
  PATIENT        Gestionar sus propias citas
  ADMIN          Administración completa
  PROFESSIONAL   Gestión de atención asignada
  RECEPTIONIST   Gestión de citas y check-in
  TRIAGE         Flujo operativo
  SYSTEM         Procesos internos automáticos del sistema

> SYSTEM no representa un usuario humano.

## Futuro

-   PHARMACY
-   BILLING
-   EMERGENCY_STAFF

------------------------------------------------------------------------

# Arquitectura

La solución utiliza:

## Monolito Modular

Organización:

``` text
Package by Feature
```

Módulos principales:

``` text
auth
users
patients
professionals
catalogs
agenda
appointments
waitlist
priority
notifications
dashboard
audit
```

------------------------------------------------------------------------

# Estructura del repositorio

``` text
hospital-platform/

├── apps/
│   ├── backend/
│   ├── frontend/
│   └── mobile/
│
├── database/
│
├── docs/
│   ├── adr/
│   ├── api/
│   ├── architecture/
│   ├── database/
│   ├── deployment/
│   ├── security/
│   └── testing/
│
├── infrastructure/
│
├── scripts/
│
├── .github/
│   └── workflows/
│
├── docker-compose.yml
│
├── README.md
└── CONTRIBUTING.md
```

------------------------------------------------------------------------

# Documentación

Ubicación:

``` text
docs/
```

Incluye:

-   PRD;
-   TRD;
-   APP FLOW;
-   UI/UX;
-   Backend Schema;
-   Database Design & Constraints;
-   API Specification;
-   Implementation Plan;
-   Security Model;
-   Testing;
-   Deployment;
-   Monitoring;
-   ADR.

------------------------------------------------------------------------

# Base de datos

Motor:

``` text
PostgreSQL
```

Herramientas:

-   JPA/Hibernate
-   Flyway

Características:

-   integridad relacional;
-   constraints;
-   transacciones;
-   auditoría.

------------------------------------------------------------------------

# Seguridad

Principios:

-   RBAC;
-   autenticación segura;
-   autorización por recurso;
-   OWASP Top 10;
-   OWASP API Security;
-   protección de datos;
-   auditoría;
-   secretos fuera del repositorio.

Se utilizan datos sintéticos para desarrollo y pruebas.

------------------------------------------------------------------------

# Pruebas

Incluye:

-   unitarias;
-   integración;
-   API;
-   seguridad;
-   end-to-end;
-   accesibilidad;
-   rendimiento.

------------------------------------------------------------------------

# Fuera del MVP

No se implementa inicialmente:

-   historia clínica electrónica completa;
-   diagnóstico médico;
-   receta electrónica;
-   farmacia;
-   laboratorio;
-   hospitalización;
-   emergencia;
-   cirugía;
-   integraciones oficiales MINSA/SIS/SIHCE.

Estos módulos quedan como evolución futura.

------------------------------------------------------------------------

# Flujo de trabajo

``` text
INVESTIGAR
    ↓
VALIDAR
    ↓
DISEÑAR
    ↓
IMPLEMENTAR
    ↓
PROBAR
    ↓
MEDIR
    ↓
ESCALAR
```

------------------------------------------------------------------------

# Principios del desarrollo

-   No implementar funcionalidades no validadas.
-   Mantener separación por módulos.
-   Documentar decisiones importantes mediante ADR.
-   Priorizar MVP antes de expansión.
-   Mantener seguridad desde diseño.

------------------------------------------------------------------------

# Inicio del proyecto

Próximos pasos:

1.  Crear estructura del repositorio.
2.  Configurar entorno desarrollo.
3.  Crear modelo PostgreSQL inicial.
4.  Implementar backend base.
5.  Implementar frontend base.
6.  Configurar CI/CD.

------------------------------------------------------------------------

Documento maestro:

``` text
docs/00-PROJECT-ROADMAP.md
```
