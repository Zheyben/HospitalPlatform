# 12 - MONITORING MAINTENANCE v1.2 FINAL

## Estado del documento

**Versión:** 1.2 FINAL (Actualizada)

**Producto:** Plataforma web y móvil modular para la gestión de atención
ambulatoria, citas médicas y trazabilidad del flujo asistencial.

------------------------------------------------------------------------

# Mejoras aplicadas

-   Actualización del contexto del sistema.
-   Actualización de referencias documentales.
-   Incorporación de monitoreo del flujo asistencial.
-   Incorporación de monitoreo de contenedores e infraestructura.
-   Ampliación de métricas técnicas y de negocio.
-   Mejora de estrategia de logs y retención.
-   Incorporación de continuidad operacional.
-   Inclusión de mantenimiento de dependencias e infraestructura.

------------------------------------------------------------------------

# 1. Objetivo del documento

Definir las actividades necesarias para mantener la plataforma estable,
segura y disponible después de su puesta en producción.

Incluye:

-   monitoreo;
-   mantenimiento;
-   soporte;
-   continuidad;
-   seguridad operacional;
-   evolución del sistema.

------------------------------------------------------------------------

# 2. Alcance operativo

## Aplicación

Incluye:

-   frontend web;
-   aplicación móvil;
-   API;
-   backend.

## Datos

Incluye:

-   PostgreSQL;
-   backups;
-   integridad datos;
-   restauración.

## Seguridad

Incluye:

-   accesos;
-   vulnerabilidades;
-   auditoría;
-   permisos.

## Flujo asistencial

Monitorear:

-   reservas;
-   check-in;
-   triaje;
-   atención profesional;
-   estados asistenciales.

## Infraestructura

Incluye:

-   servidores;
-   contenedores;
-   almacenamiento;
-   servicios.

------------------------------------------------------------------------

# 3. Estrategia de monitoreo

El monitoreo se divide en:

``` text
Aplicación

↓

Infraestructura

↓

Seguridad

↓

Datos

↓

Experiencia usuario

↓

Flujo asistencial
```

------------------------------------------------------------------------

# 4. Monitoreo de aplicación

Validar:

## Disponibilidad

-   aplicación accesible;
-   API disponible;
-   servicios principales activos.

## Rendimiento

-   tiempos respuesta;
-   errores;
-   carga.

## Funcionalidad

Monitorear:

-   autenticación;
-   reservas;
-   cancelaciones;
-   reprogramaciones.

## Workflow asistencial

Monitorear:

-   pacientes pendientes;
-   estados detenidos;
-   errores de transición.

Ejemplo:

``` text
EN_ESPERA > tiempo máximo permitido

↓

Generar alerta
```

------------------------------------------------------------------------

# 5. Monitoreo de infraestructura

Supervisar:

-   CPU;
-   memoria;
-   almacenamiento;
-   conectividad;
-   estado contenedores;
-   reinicios servicios;
-   salud PostgreSQL.

Objetivo:

Detectar problemas antes de afectar usuarios.

------------------------------------------------------------------------

# 6. Gestión de logs

Registrar:

## Aplicación

-   errores;
-   excepciones;
-   eventos importantes.

## Seguridad

-   login;
-   accesos rechazados;
-   cambios permisos;
-   modificaciones críticas.

## Negocio

-   creación cita;
-   cancelación;
-   reprogramación;
-   cambios workflow.

------------------------------------------------------------------------

# Política de retención

Definir:

-   tiempo conservación;
-   usuarios autorizados;
-   protección información.

Ejemplo:

``` text
Logs técnicos:
30 días

Auditoría:
12 meses
```

------------------------------------------------------------------------

# 7. Métricas del sistema

## Técnicas

Medir:

-   disponibilidad;
-   tiempo respuesta;
-   errores API;
-   consumo recursos;
-   estado BD.

## Negocio

Medir:

-   citas creadas;
-   citas completadas;
-   cancelaciones;
-   demanda especialidad;
-   reservas web.

## Asistenciales

Medir:

-   tiempo promedio espera;
-   pacientes atendidos;
-   pacientes pendientes;
-   duración atención.

## MVP

Agregar:

-   tiempo promedio reserva;
-   tasa cancelación;
-   utilización cupos.

------------------------------------------------------------------------

# 8. Dashboards operativos

Los dashboards deben permitir visualizar:

## Técnicos

-   disponibilidad servicios;
-   errores;
-   rendimiento.

## Negocio

-   reservas;
-   atención;
-   demanda.

## Operativos

-   pacientes pendientes;
-   tiempos espera;
-   estados asistenciales.

------------------------------------------------------------------------

# 9. Alertas e incidentes

## Crítico

Ejemplos:

-   sistema caído;
-   pérdida conexión BD;
-   pérdida información.

## Alto

Ejemplos:

-   aumento errores;
-   degradación rendimiento;
-   fallos servicio.

## Medio

Ejemplos:

-   advertencias operativas.

## Bajo

Ejemplos:

-   mantenimiento programado;
-   recomendaciones preventivas.

------------------------------------------------------------------------

# 10. Gestión de respaldos

Estrategia:

## Base de datos

Realizar:

-   backups periódicos;
-   validación restauración.

## Configuración

Respaldar:

-   variables;
-   configuraciones;
-   archivos necesarios.

## Restauración

Validar periódicamente:

-   integridad backup;
-   recuperación correcta.

------------------------------------------------------------------------

# 11. Mantenimiento preventivo

Actividades:

-   revisión logs;
-   limpieza datos temporales;
-   revisión rendimiento;
-   actualización componentes;
-   revisión permisos;
-   actualización imágenes Docker;
-   revisión dependencias seguridad.

------------------------------------------------------------------------

# 12. Actualización de dependencias

Proceso:

``` text
Identificar actualización

↓

Evaluar impacto

↓

Probar

↓

Aplicar cambio

↓

Validar
```

Incluye:

-   backend;
-   frontend;
-   móvil;
-   herramientas;
-   contenedores.

------------------------------------------------------------------------

# 13. Gestión de incidentes

Proceso:

``` text
Detección

↓

Clasificación

↓

Respuesta

↓

Corrección

↓

Análisis causa raíz

↓

Documentación
```

Registrar:

-   causa;
-   impacto;
-   solución;
-   acciones preventivas.

------------------------------------------------------------------------

# 14. Seguridad operacional

Mantener:

-   revisión roles;
-   actualización seguridad;
-   auditoría accesos;
-   protección datos;
-   eliminación accesos innecesarios.

Relacionado con:

``` text
08-SECURITY-THREAT-MODEL
```

------------------------------------------------------------------------

# 15. Continuidad del servicio

Considerar:

-   recuperación ante fallos;
-   restauración backups;
-   disponibilidad.

Definir:

## RTO

Tiempo máximo para recuperar servicio.

## RPO

Pérdida máxima de datos aceptable.

Objetivo:

Reducir interrupciones.

------------------------------------------------------------------------

# 16. Mejora continua

Usar:

-   métricas;
-   incidentes;
-   feedback usuarios.

Para mejorar:

-   rendimiento;
-   experiencia;
-   funcionalidades.

------------------------------------------------------------------------

# 17. Criterios de operación

El sistema se considera operativo cuando:

-   está disponible;
-   servicios principales funcionan;
-   respaldos activos;
-   logs permiten seguimiento;
-   incidentes tienen proceso definido;
-   accesos revisados;
-   monitoreo activo.

------------------------------------------------------------------------

**Documento actualizado como 12-MONITORING-MAINTENANCE v1.2 FINAL.**
