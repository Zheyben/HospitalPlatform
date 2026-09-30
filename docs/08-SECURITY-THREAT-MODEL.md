# 08 - SECURITY THREAT MODEL v1.3 FINAL

## Estado del documento

**Versión:** 1.3 FINAL (Actualizada)

**Control de vigencia E.3.2:** este modelo enumera amenazas y controles candidatos. La autorización actual se rige por DEC-002 y los `@PreAuthorize` de los controllers: roles de negocio `ADMIN`, `PATIENT`, `RECEPTIONIST` y `PROFESSIONAL`. `TRIAGE` y `SYSTEM` no tienen operación de negocio aprobada; los permisos de ejemplo de triaje, especialidades y dashboard no prueban endpoints ni permisos configurados. El triaje clínico se conserva como antecedente fuera del MVP actual, conforme al baseline y ADR-007.

**Producto:** Plataforma web y móvil modular para la gestión de atención
ambulatoria, citas médicas y trazabilidad del flujo asistencial.

**Documentos relacionados:**

-   PRD v1.4 FINAL
-   TRD v1.2 FINAL ajustado
-   APP FLOW v1.2.1 FINAL
-   UI/UX DESIGN BRIEF v1.2.1 FINAL
-   BACKEND SCHEMA v1.4.1 FINAL
-   API SPECIFICATION v1.2.2 FINAL
-   IMPLEMENTATION PLAN v1.3.1 FINAL
-   TEST STRATEGY
-   TEST PLAN
-   DEPLOYMENT PLAN
-   MONITORING & MAINTENANCE

------------------------------------------------------------------------

# 1. Información general

Este documento define el modelo de amenazas y controles de seguridad
para la plataforma web y móvil orientada a la gestión de atención
ambulatoria, citas médicas y trazabilidad del flujo asistencial.

El objetivo es identificar:

-   activos críticos;
-   amenazas;
-   vulnerabilidades;
-   controles preventivos;
-   controles correctivos.

------------------------------------------------------------------------

# 2. Objetivos de seguridad

## Confidencialidad

Proteger:

-   datos personales;
-   información asistencial;
-   credenciales;
-   información operativa;
-   tokens de autenticación.

## Integridad

Garantizar que:

-   citas;
-   agendas;
-   estados asistenciales;
-   permisos;
-   configuraciones;

no sean modificados sin autorización.

## Disponibilidad

Mantener:

-   acceso al sistema;
-   disponibilidad de reservas;
-   continuidad operativa.

## Trazabilidad

Registrar:

-   usuario responsable;
-   acción realizada;
-   fecha y hora;
-   recurso afectado;
-   resultado de la operación.

------------------------------------------------------------------------

# 3. Alcance

Incluye:

-   aplicación web;
-   aplicación móvil;
-   API REST;
-   backend;
-   PostgreSQL;
-   autenticación;
-   autorización;
-   auditoría.

No incluye:

-   infraestructura física hospitalaria;
-   integraciones externas no definidas;
-   dispositivos médicos.

------------------------------------------------------------------------

# 4. Activos críticos

  Activo                            Nivel
  --------------------------------- ---------
  Datos personales del paciente     Crítico
  Información asistencial           Crítico
  Credenciales de acceso            Crítico
  Información de citas              Crítico
  Tokens autenticación              Alto
  Agendas médicas                   Alto
  Roles y permisos                  Alto
  Registros auditoría               Alto
  Configuración administrativa      Alto
  Contenido público institucional   Medio

------------------------------------------------------------------------

# 5. Actores y permisos

## Paciente

Puede:

-   gestionar sus propias citas;
-   consultar disponibilidad;
-   recibir notificaciones.

No puede:

-   acceder a información de otros pacientes;
-   modificar agendas;
-   administrar configuraciones.

------------------------------------------------------------------------

## Profesional

Puede:

-   consultar agenda asignada;
-   consultar pacientes autorizados;
-   registrar atención;
-   actualizar estados asistenciales permitidos.

No puede:

-   administrar usuarios;
-   modificar configuraciones globales.

------------------------------------------------------------------------

## Recepcionista

Puede:

-   consultar citas del día;
-   validar pacientes;
-   realizar check-in;
-   registrar admisión.

No puede:

-   modificar atención profesional;
-   administrar permisos.

------------------------------------------------------------------------

## Triaje

Puede:

-   consultar pacientes admitidos;
-   registrar evaluación inicial;
-   actualizar flujo asistencial permitido.

No puede:

-   modificar agendas;
-   administrar usuarios.

------------------------------------------------------------------------

## Administrador

Puede:

-   administrar usuarios;
-   gestionar roles y permisos;
-   gestionar catálogos;
-   configurar agendas;
-   administrar contenido público;
-   consultar dashboards autorizados.

------------------------------------------------------------------------

## Sistema

Actor técnico interno.

Puede:

-   generar notificaciones;
-   ejecutar procesos automáticos;
-   registrar eventos.

No representa un rol humano dentro del modelo RBAC.

------------------------------------------------------------------------

# 6. Superficie de ataque

## Frontend web

Riesgos:

-   XSS;
-   manipulación solicitudes;
-   robo de sesión;
-   acceso a rutas no autorizadas.

## Aplicación móvil

Riesgos:

-   almacenamiento inseguro de tokens;
-   exposición sesión;
-   almacenamiento local inseguro.

## API REST

Riesgos:

-   acceso no autorizado;
-   abuso endpoints;
-   inyección;
-   manipulación parámetros;
-   escalamiento privilegios.

## Autenticación

Riesgos:

-   fuerza bruta;
-   robo tokens;
-   secuestro sesión;
-   uso indebido credenciales.

## Base de datos

Riesgos:

-   acceso indebido;
-   exposición información;
-   modificación no autorizada.

## Panel administrativo

Riesgos:

-   abuso privilegios;
-   cambios configuración indebidos;
-   modificación contenido público.

------------------------------------------------------------------------

# 7. Modelo STRIDE

  Amenaza                  Descripción
  ------------------------ ------------------------------
  Spoofing                 Suplantación identidad
  Tampering                Alteración datos
  Repudiation              Negación acciones realizadas
  Information Disclosure   Exposición información
  Denial of Service        Interrupción servicio
  Elevation of Privilege   Escalamiento permisos

------------------------------------------------------------------------

# 8. Amenazas identificadas

## T01 - Compromiso de credenciales

Impacto:

Crítico.

Mitigación:

-   JWT;
-   refresh tokens;
-   hash seguro contraseñas;
-   políticas contraseña;
-   bloqueo intentos fallidos.

------------------------------------------------------------------------

## T02 - Acceso a datos de otro paciente

Impacto:

Crítico.

Mitigación:

-   validación propietario recurso;
-   autorización usuario autenticado;
-   permisos backend.

------------------------------------------------------------------------

## T03 - Doble reserva de cupo

Impacto:

Alto.

Mitigación:

-   transacciones;
-   constraints PostgreSQL;
-   validación backend.

------------------------------------------------------------------------

## T04 - Modificación no autorizada de agenda

Impacto:

Alto.

Mitigación:

-   RBAC;
-   permisos;
-   validación operaciones.

------------------------------------------------------------------------

## T05 - Exposición de información sensible

Impacto:

Crítico.

Mitigación:

-   HTTPS;
-   mínimo privilegio;
-   protección datos.

------------------------------------------------------------------------

## T06 - Escalamiento indebido de privilegios

Impacto:

Alto.

Mitigación:

-   RBAC;
-   autorización endpoints;
-   separación responsabilidades.

------------------------------------------------------------------------

## T07 - Manipulación de estados asistenciales

Impacto:

Crítico.

Descripción:

Un usuario intenta modificar estados del flujo asistencial sin
autorización.

Ejemplo:

``` text
EN_ESPERA

↓

FINALIZADO
```

Mitigación:

-   validación workflow;
-   permisos por rol;
-   auditoría eventos.

------------------------------------------------------------------------

## T08 - Acceso indebido a dashboards

Impacto:

Alto.

Mitigación:

-   permisos dashboard;
-   filtros por rol;
-   autorización backend.

------------------------------------------------------------------------

## T09 - Modificación no autorizada de contenido público

Impacto:

Medio.

Mitigación:

-   permisos administrador;
-   auditoría cambios;
-   validación operaciones.

------------------------------------------------------------------------

# 9. Controles de seguridad

## Autenticación

Aplicar:

-   JWT;
-   refresh tokens;
-   expiración sesión;
-   almacenamiento seguro credenciales;
-   hash contraseñas.

------------------------------------------------------------------------

## Autorización

Modelo:

``` text
RBAC
(Role Based Access Control)
```

Permisos ejemplo:

**Ejemplos históricos/objetivo; `TRIAGE_REGISTER`, `SPECIALTY_MANAGE` y `DASHBOARD_VIEW` no son permisos operativos de endpoints actuales.**

``` text
DASHBOARD_VIEW

SPECIALTY_MANAGE

APPOINTMENT_CANCEL

TRIAGE_REGISTER
```

------------------------------------------------------------------------

## Seguridad API

Aplicar:

-   validación entrada;
-   control permisos;
-   rate limiting;
-   respuestas seguras;
-   manejo errores.

------------------------------------------------------------------------

## Seguridad base de datos

Aplicar:

-   consultas parametrizadas;
-   privilegios mínimos;
-   respaldos;
-   control acceso;
-   separación usuarios BD.

------------------------------------------------------------------------

# 10. Protección de datos

Principios:

-   mínimo acceso necesario;
-   protección información personal;
-   almacenamiento seguro.

Datos sensibles:

-   identificación paciente;
-   información asistencial;
-   historial operativo.

------------------------------------------------------------------------

# 11. Seguridad frontend y móvil

## Frontend web

Controles:

-   sanitización entradas;
-   protección XSS;
-   protección rutas;
-   manejo seguro sesión.

## Aplicación móvil

Controles:

-   almacenamiento seguro tokens;
-   HTTPS;
-   validación sesión;
-   protección datos locales.

------------------------------------------------------------------------

# 12. Auditoría y trazabilidad

Eventos críticos:

-   inicio sesión;
-   creación cita;
-   cancelación;
-   reprogramación;
-   cambios agenda;
-   actualización estados;
-   check-in;
-   triaje;
-   atención profesional;
-   cambios roles y permisos.

Registrar:

``` json
{
"user":"123",
"action":"CANCEL_APPOINTMENT",
"resource":"appointment",
"date":"2026-01-01",
"result":"SUCCESS"
}
```

------------------------------------------------------------------------

# 13. Gestión de incidentes

Proceso:

``` text
Detección

↓

Análisis

↓

Contención

↓

Corrección

↓

Registro
```

------------------------------------------------------------------------

# 14. Riesgos residuales

  -----------------------------------------------------------------------
  Riesgo                              Tratamiento
  ----------------------------------- -----------------------------------
  Errores humanos                     Capacitación y controles

  Ataques avanzados                   Monitoreo posterior

  Configuración incorrecta            Revisiones periódicas

  Dependencia terceros                Evaluación integraciones

  Error configuración permisos        Revisión RBAC

  Exposición accidental información   Control acceso por rol
  asistencial                         
  -----------------------------------------------------------------------

------------------------------------------------------------------------

# 15. Criterios de aceptación de seguridad

El sistema será aceptado cuando:

-   usuarios solo accedan a recursos autorizados;
-   roles funcionen correctamente;
-   las citas no puedan duplicarse;
-   operaciones críticas sean auditables;
-   comunicación sea segura;
-   datos personales tengan protección;
-   tokens y credenciales tengan controles adecuados;
-   estados asistenciales respeten permisos definidos.

------------------------------------------------------------------------

**Documento actualizado como 08-SECURITY-THREAT-MODEL v1.3 FINAL.**
