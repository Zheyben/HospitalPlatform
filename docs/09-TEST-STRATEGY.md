# 09 - TEST STRATEGY v1.3 FINAL

## Estado del documento

**Versión:** 1.3 FINAL (Actualizada)

**Control de vigencia E.3.2 (30/09/2026):** esta estrategia mezcla pruebas existentes y objetivos de cobertura. El triaje clínico y dashboard carecen de flujo/módulo funcional actual; sus casos son antecedentes o propuestas, no tests ejecutados. Para RF-014 canónico, `AppointmentModuleIT` demuestra exclusión con dos solicitudes; el ensayo SRS de veinte permanece pendiente (DEC-023). La evidencia ejecutable se verifica en `apps/backend/src/test`, no en la enumeración de un plan. No se afirma despliegue ni cumplimiento de metas operativas.

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
-   SECURITY THREAT MODEL v1.3 FINAL
-   TEST PLAN
-   DEPLOYMENT PLAN
-   MONITORING & MAINTENANCE

------------------------------------------------------------------------

# Ajustes aplicados en esta versión

-   Actualización del contexto del sistema.
-   Actualización de documentos relacionados.
-   Inclusión de recepción, triaje y dashboards en alcance.
-   Incorporación de pruebas RBAC y workflow asistencial.
-   Inclusión de pruebas end-to-end.
-   Incorporación de pruebas basadas en riesgo.
-   Validación de migraciones y restricciones de base de datos.
-   Mejora de métricas de calidad.

------------------------------------------------------------------------

# 1. Información general

Este documento define la estrategia general de pruebas para validar la
calidad de la plataforma web y móvil orientada a la gestión de atención
ambulatoria, citas médicas y trazabilidad del flujo asistencial.

La estrategia establece:

-   enfoque de calidad;
-   niveles de prueba;
-   tipos de validación;
-   responsabilidades;
-   criterios generales de aceptación.

------------------------------------------------------------------------

# 2. Objetivo de la estrategia

Garantizar que la solución cumpla:

-   requisitos funcionales;
-   requisitos no funcionales;
-   reglas de negocio;
-   controles de seguridad;
-   experiencia esperada del usuario.

------------------------------------------------------------------------

# 3. Alcance de pruebas

## Aplicación web

Incluye:

-   portal público;
-   panel administrativo;
-   panel profesional;
-   panel recepción;
-   panel triaje.

Validar:

-   navegación;
-   permisos;
-   formularios;
-   estados visuales;
-   flujos principales.

------------------------------------------------------------------------

## Aplicación móvil

Incluye:

-   autenticación;
-   gestión de citas;
-   seguimiento del paciente;
-   notificaciones.

------------------------------------------------------------------------

## Backend

Incluye:

-   servicios;
-   reglas de negocio;
-   API REST;
-   autenticación;
-   autorización;
-   auditoría;
-   workflow asistencial.

------------------------------------------------------------------------

## Base de datos

Validar:

-   integridad;
-   relaciones;
-   restricciones;
-   índices;
-   migraciones;
-   transacciones.

------------------------------------------------------------------------

# 4. Principios de calidad

## Prevención

Identificar errores durante desarrollo antes de llegar a producción.

## Automatización

Automatizar pruebas repetitivas cuando sea viable.

## Trazabilidad

Relacionar:

``` text
Requisito

↓

Validación

↓

Resultado
```

## Seguridad desde diseño

Validar:

-   autenticación;
-   autorización;
-   protección información.

## Mejora continua

Utilizar resultados para mejorar calidad.

## Pruebas basadas en riesgo

Priorizar escenarios críticos:

-   autenticación;
-   permisos;
-   citas;
-   flujo asistencial;
-   información sensible.

------------------------------------------------------------------------

# 5. Enfoque de testing

Se aplicará una estrategia combinada:

``` text
Pruebas manuales

+

Pruebas automatizadas

+

Validación funcional

+

Pruebas basadas en riesgo
```

------------------------------------------------------------------------

# 6. Niveles de prueba

## Pruebas unitarias

Validan:

-   funciones;
-   servicios;
-   reglas negocio.

Responsable:

Equipo desarrollo.

------------------------------------------------------------------------

## Pruebas de integración

Validan:

``` text
Frontend

↓

API

↓

Backend

↓

Base de datos
```

------------------------------------------------------------------------

## Pruebas de sistema

Validan comportamiento completo.

------------------------------------------------------------------------

## Pruebas end-to-end

Validan flujos completos:

``` text
Paciente reserva cita

↓

Recepción realiza check-in

↓

Triaje registra evaluación

↓

Profesional realiza atención
```

------------------------------------------------------------------------

## Pruebas de aceptación

Validan objetivos del usuario.

------------------------------------------------------------------------

## Pruebas de regresión

Aplicadas después de cambios en:

-   citas;
-   disponibilidad;
-   agenda;
-   estados asistenciales;
-   permisos.

------------------------------------------------------------------------

# 7. Tipos de pruebas

## Pruebas funcionales

Validan:

-   autenticación;
-   usuarios;
-   roles;
-   citas;
-   agendas;
-   recepción;
-   triaje;
-   atención.

------------------------------------------------------------------------

## Pruebas RBAC

Validan:

-   acceso según rol;
-   restricciones;
-   permisos específicos.

Ejemplo:

``` text
Recepcionista

NO puede modificar atención profesional
```

------------------------------------------------------------------------

## Pruebas workflow

Validan:

-   estados asistenciales;
-   transiciones permitidas;
-   restricciones de flujo.

------------------------------------------------------------------------

## Pruebas de seguridad

Basadas en Security Threat Model.

Validan:

-   autenticación;
-   autorización;
-   protección datos;
-   roles;
-   auditoría.

------------------------------------------------------------------------

## Pruebas de rendimiento

Evalúan:

-   tiempos respuesta;
-   carga;
-   concurrencia.

------------------------------------------------------------------------

## Pruebas de usabilidad

Evalúan:

-   navegación;
-   comprensión flujo;
-   facilidad uso.

------------------------------------------------------------------------

## Pruebas accesibilidad

Evalúan:

-   legibilidad;
-   navegación;
-   formularios.

------------------------------------------------------------------------

## Pruebas compatibilidad

Validan:

-   navegadores;
-   dispositivos móviles.

------------------------------------------------------------------------

# 8. Estrategia por componente

## Backend y API

Validar:

-   endpoints;
-   respuestas;
-   reglas negocio;
-   permisos;
-   autenticación;
-   auditoría.

------------------------------------------------------------------------

## Frontend web

Validar:

-   componentes;
-   formularios;
-   navegación;
-   rutas protegidas;
-   manejo errores.

------------------------------------------------------------------------

## Aplicación móvil

Validar:

-   sesión;
-   interacción;
-   almacenamiento seguro;
-   funcionalidades.

------------------------------------------------------------------------

## Base de datos

Validar:

-   integridad;
-   relaciones;
-   constraints;
-   migraciones;
-   índices;
-   transacciones.

------------------------------------------------------------------------

# 9. Ambientes de prueba

``` text
Desarrollo

↓

Testing

↓

Staging

↓

Producción
```

## Desarrollo

Construcción y validaciones iniciales.

## Testing

Ejecución QA.

## Staging

Validación previa despliegue.

## Producción

Operación final.

------------------------------------------------------------------------

# 10. Datos de prueba

Los datos deben:

-   estar controlados;
-   evitar información real sensible;
-   representar escenarios reales.

Escenarios:

-   usuarios diferentes roles;
-   disponibilidad;
-   reservas;
-   cancelaciones;
-   reprogramaciones;
-   pacientes admitidos;
-   triaje;
-   estados asistenciales.

------------------------------------------------------------------------

# 11. Herramientas propuestas

Según necesidad:

-   gestión de pruebas;
-   pruebas API;
-   automatización;
-   rendimiento;
-   seguridad.

------------------------------------------------------------------------

# 12. Roles y responsabilidades

## QA

-   planificación;
-   ejecución;
-   reporte defectos.

## Desarrollo

-   pruebas unitarias;
-   corrección errores;
-   soporte técnico.

## Product Owner

-   validación funcional;
-   aceptación producto.

------------------------------------------------------------------------

# 13. Gestión de defectos

Clasificación:

  Nivel     Descripción
  --------- --------------------------------
  Crítico   Bloquea operación
  Alto      Afecta funcionalidad principal
  Medio     Impacto parcial
  Bajo      Problema menor

Flujo:

``` text
Detectado

↓

Analizado

↓

Asignado

↓

Corregido

↓

Validado
```

------------------------------------------------------------------------

# 14. Métricas de calidad

Se considerarán:

-   porcentaje pruebas aprobadas;
-   cantidad defectos encontrados;
-   defectos críticos abiertos;
-   defectos reabiertos;
-   cobertura pruebas;
-   tiempo resolución;
-   cobertura por módulo.

Ejemplo:

``` text
Auth: 90%

Citas: 95%

Dashboard: 70%
```

------------------------------------------------------------------------

# 15. Criterios de entrada

Las pruebas pueden iniciar cuando:

-   requisitos definidos;
-   versión funcional disponible;
-   ambiente disponible;
-   datos prueba preparados.

------------------------------------------------------------------------

# 16. Criterios de salida

La etapa finaliza cuando:

-   pruebas críticas aprobadas;
-   defectos críticos solucionados;
-   validación funcional completada;
-   seguridad validada;
-   versión preparada para despliegue.

------------------------------------------------------------------------

**Documento actualizado como 09-TEST-STRATEGY v1.3 FINAL.**
