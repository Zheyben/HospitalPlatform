# 04 - UI/UX DESIGN BRIEF v1.2.2 FINAL

## Estado del documento

**Versión:** 1.2.2 FINAL (FASE 5.11.1)

**Control de vigencia E.3.2 (30/09/2026):** este brief reúne pantallas objetivo y antecedentes, no interfaces implementadas. `apps/frontend` y `apps/mobile` contienen solo carpetas sin archivos de aplicación. La pantalla de triaje/evaluación clínica de este documento queda **fuera del MVP actual** según el baseline y ADR-007. Catálogo, asociación profesional–especialidad, disponibilidad sanitizada y dashboard no tienen UI/API funcional actual; DEC-006/007 aprueban solo diseño conceptual. Los roles de negocio actuales son ADMIN, PATIENT, RECEPTIONIST y PROFESSIONAL (DEC-002); la operación de Agenda HTTP permanece en ADMIN. Las pantallas futuras no confieren permisos.

**Producto:** Plataforma web y móvil modular para la gestión de atención
ambulatoria, citas médicas y trazabilidad del flujo asistencial.

**Documentos relacionados:**

-   PRD v1.4 FINAL
-   TRD v1.2 FINAL ajustado
-   BACKEND SCHEMA v1.3.1 FINAL
-   API SPECIFICATION v1.3 FINAL

------------------------------------------------------------------------

# 1. Objetivo

Este documento define los lineamientos UX/UI para construir interfaces
coherentes para:

-   visitantes;
-   pacientes;
-   administradores;
-   recepcionistas;
-   personal de triaje;
-   profesionales de salud.

Servirá como base para:

-   prototipos;
-   componentes frontend;
-   validaciones UX;
-   pruebas de usabilidad.

------------------------------------------------------------------------

# 2. Principios UX

## Simplicidad

Reducir pasos innecesarios.

Flujo reserva:

``` text
Especialidad
↓
Profesional
↓
Horario
↓
Confirmación
```

## Transparencia del estado

El usuario debe conocer:

-   acción realizada;
-   resultado;
-   siguiente paso.

## Prevención de errores

Las acciones críticas deberán solicitar confirmación.

## Accesibilidad

Considerar:

-   textos legibles;
-   contraste adecuado;
-   lenguaje claro;
-   reducción de carga cognitiva.

------------------------------------------------------------------------

# 3. Usuarios y perfiles

## Visitante

Consulta información institucional y accede a reservas.

------------------------------------------------------------------------

## Paciente

Funciones:

-   reservar citas;
-   cancelar;
-   reprogramar;
-   consultar historial;
-   visualizar seguimiento asistencial;
-   recibir notificaciones.

Navegación móvil:

``` text
Inicio

Citas

Notificaciones

Perfil
```

------------------------------------------------------------------------

## Administrador

Funciones:

-   gestionar usuarios;
-   administrar roles y permisos;
-   gestionar especialidades;
-   editar contenido público;
-   visualizar dashboards;
-   administrar operación general.

Navegación principal:

``` text
Sidebar

Dashboard

Usuarios

Especialidades

Profesionales

Agendas

Citas

Reportes

Configuración
```

------------------------------------------------------------------------

## Recepcionista

Objetivo:

Gestionar llegada y admisión del paciente.

Necesidades:

-   ver citas del día;
-   buscar pacientes;
-   registrar check-in;
-   derivar flujo asistencial.

Vista principal:

``` text
Dashboard recepción

Citas próximas

Pacientes esperando

Check-in pendientes
```

------------------------------------------------------------------------

## Triaje — antecedente fuera del MVP actual

Objetivo:

Registrar evaluación inicial del paciente.

Necesidades:

-   visualizar pacientes pendientes;
-   registrar prioridad;
-   agregar observaciones;
-   derivar a atención profesional.

Flujo:

``` text
Paciente pendiente

↓

Evaluación triaje

↓

Atención profesional
```

------------------------------------------------------------------------

## Profesional

Objetivo:

En el diseño histórico se proponía gestionar agenda y atención básica. En el backend actual PROFESSIONAL solo ejecuta transiciones de atención de citas asignadas; no administra Agenda ni registra diagnóstico clínico.

Necesidades:

-   agenda diaria;
-   pacientes programados;
-   actualizar estados;
-   registrar atención.

------------------------------------------------------------------------

# 4. Arquitectura de información

## Portal público

``` text
Inicio

├── Información institucional
├── Especialidades
├── Servicios
├── Profesionales
└── Reservar cita
```

------------------------------------------------------------------------

## Portal paciente

``` text
Dashboard

├── Próxima cita
├── Mis citas
├── Nueva cita
├── Historial
├── Seguimiento asistencial
├── Notificaciones
└── Perfil
```

------------------------------------------------------------------------

## Panel administrador

``` text
Dashboard

├── Métricas
├── Usuarios
├── Roles y permisos
├── Especialidades
├── Contenido público
├── Profesionales
├── Agendas
├── Disponibilidad
├── Citas
├── Lista de espera
├── Auditoría
└── Configuración
```

------------------------------------------------------------------------

# 5. Dashboard administrador

Debe permitir visualizar:

## Cards

-   Citas del día.
-   Reservas del mes.
-   Cancelaciones.
-   Especialidad más solicitada.
-   Profesionales activos.

## Gráficos

-   Reservas por mes.
-   Demanda por especialidad.
-   Estados de citas.
-   Canal de reserva.

## Filtros

-   Fecha inicio.
-   Fecha fin.
-   Especialidad.
-   Profesional.

------------------------------------------------------------------------

# 6. Componentes UI principales

## Appointment Card

Componente para mostrar una cita.

Información:

``` text
Especialidad

Profesional

Fecha

Hora

Estado
```

------------------------------------------------------------------------

## Status Badge

Representa estados:

``` text
PROGRAMADA

CHECK-IN

EN ESPERA

EN ATENCIÓN

FINALIZADO

CANCELADA
```

------------------------------------------------------------------------

## Patient Summary Card

Uso en recepción y profesionales.

Información:

``` text
Nombre

Documento

Hora cita

Estado actual
```

------------------------------------------------------------------------

## Cards

Uso:

-   citas;
-   indicadores;
-   estados.

------------------------------------------------------------------------

## Tablas

Uso:

-   administración;
-   agendas;
-   usuarios;
-   citas.

------------------------------------------------------------------------

## Calendarios

Uso:

-   disponibilidad;
-   selección horarios.

------------------------------------------------------------------------

## Timeline

Representa flujo asistencial.

``` text
✔ Confirmada

✔ Check-in

● En espera

○ Atención
```

------------------------------------------------------------------------

## Modales

Uso:

-   cancelación;
-   reprogramación;
-   acciones críticas.

------------------------------------------------------------------------

# 7. Flujo paciente

## Nueva reserva

``` text
Especialidad

↓

Profesional

↓

Horario

↓

Validación disponibilidad

↓

Confirmación
```

------------------------------------------------------------------------

## Seguimiento asistencial

La cita debe mostrar:

``` text
CONFIRMED + flowStage null

↓

CHECK_IN

↓

WAITING

↓

IN_ATTENTION

↓

FINISHED + AppointmentStatus COMPLETED
```

Los nombres anteriores son los valores canónicos de dominio. La interfaz puede
localizar sus etiquetas, pero no debe introducir etapas adicionales. El
paciente visualiza el progreso y no modifica `FlowStage`.

------------------------------------------------------------------------

# 8. Sistema basado en permisos

La interfaz deberá adaptarse según permisos.

Administrador:

``` text
Editar especialidades

Gestionar usuarios

Ver dashboards
```

Recepcionista:

``` text
Registrar llegada

Consultar citas del día
```

Paciente:

``` text
Gestionar sus propias citas
```

Profesional:

``` text
Registrar atención

Actualizar flujo
```

------------------------------------------------------------------------

# 9. Estados visuales de interfaz

Toda pantalla debe contemplar:

## Loading

"Cargando disponibilidad..."

## Empty State

"No tienes citas programadas."

## Error

"No fue posible completar la operación."

## Confirmación

"Su cita fue registrada correctamente."

## Sin permisos

"No tiene autorización para realizar esta acción."

------------------------------------------------------------------------

# 10. Responsividad

## Desktop

Uso:

-   administración;
-   profesionales.

## Tablet

Uso:

-   operación interna.

## Mobile

Uso:

-   pacientes;
-   consultas rápidas.

------------------------------------------------------------------------

# 11. Validación UX

La experiencia será evaluada mediante:

-   facilidad navegación;
-   cantidad de pasos;
-   claridad;
-   prevención de errores;
-   consistencia;
-   accesibilidad.

------------------------------------------------------------------------

**Documento actualizado como 04-UI/UX-DESIGN-BRIEF v1.2.2 FINAL.**
