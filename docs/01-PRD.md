# 01 - PRD v1.4 FINAL (Product Requirements Document)

## Estado del documento

**Versión:** 1.4 FINAL

**Producto:** Plataforma web y móvil modular para la gestión de atención
ambulatoria, citas médicas y trazabilidad del flujo asistencial.

**Caso de estudio:** Hospital de Huaycán - MINSA.

**Tipo de proyecto:** Caso de estudio académico de Ingeniería de
Software.

**Control de vigencia E.3.2 (30/09/2026):** los IDs `RF-001`–`RF-020` de este PRD son la **serie histórica**, distinta de los IDs canónicos de `requisitos/SRS-HOSPITALPLATFORM.md`. Usar `requisitos/EQUIVALENCIAS_REQUISITOS.md` antes de trasladar un RF a una matriz actual. En particular, los antiguos RF-014 (registro de atención clínica) y RF-020 (evaluación de triaje) están **fuera del MVP actual**; `RF-014` canónico significa exclusión de doble reserva. Este PRD describe intención de producto, no implementación ni aprobación institucional.

------------------------------------------------------------------------

# 1. Información general del producto

## Propósito

Definir las necesidades, alcance, funcionalidades y restricciones de una
plataforma digital orientada a mejorar la gestión de atención
ambulatoria mediante una solución web y móvil.

La plataforma permitirá organizar:

-   disponibilidad médica;
-   reservas;
-   cancelaciones;
-   reprogramaciones;
-   seguimiento del flujo asistencial;
-   administración operativa;
-   visualización de métricas;
-   gestión de información institucional.

El proyecto corresponde a un caso de estudio académico.

No representa una implementación oficial del Hospital de Huaycán ni
contempla integración real con sistemas institucionales del MINSA.

------------------------------------------------------------------------

# 2. Contexto del producto

Los establecimientos de salud requieren mecanismos organizados para
administrar la atención ambulatoria, especialmente en:

-   programación de citas;
-   disponibilidad médica;
-   comunicación con pacientes;
-   seguimiento del recorrido asistencial;
-   administración de información pública;
-   toma de decisiones basada en métricas.

La propuesta busca centralizar estos procesos mediante módulos digitales
orientados a pacientes y personal autorizado.

El producto contempla:

-   Portal público institucional.
-   Portal de reservas para pacientes.
-   Plataforma web operativa para personal autorizado.
-   Panel administrativo.
-   Aplicación móvil orientada al paciente.

------------------------------------------------------------------------

# 3. Actores del sistema

  -----------------------------------------------------------------------
  Actor                               Descripción
  ----------------------------------- -----------------------------------
  Visitante                           Consulta información pública
                                      institucional y especialidades

  Paciente                            Gestiona sus citas y consulta
                                      información relacionada con su
                                      atención

  Recepcionista                       Gestiona llegada de pacientes,
                                      reservas y procesos administrativos
                                      iniciales

  Triaje                              Registra evaluación inicial y flujo
                                      previo a consulta

  Profesional de salud                Consulta agenda y registra
                                      información básica de atención

  Farmacia                            Responsable de inventario y entrega
                                      de productos (futuro)

  Cajero/Facturación                  Responsable de cobros y
                                      comprobantes (futuro)

  Administrador                       Gestiona usuarios, roles,
                                      configuración, contenido y
                                      dashboards

  Sistema                             Ejecuta procesos automáticos como
                                      notificaciones y auditoría
  -----------------------------------------------------------------------

------------------------------------------------------------------------

# 4. Historias de usuario

## HU-001 Registro de paciente

Como paciente, quiero registrarme en la plataforma para gestionar mis
citas médicas digitalmente.

Prioridad: Alta.

Criterios:

-   Registrar datos obligatorios.
-   Evitar usuarios duplicados.
-   Proteger credenciales.

------------------------------------------------------------------------

## HU-002 Consulta de especialidades

Como paciente, quiero visualizar especialidades disponibles para conocer
los servicios antes de solicitar una cita.

Prioridad: Alta.

Criterios:

-   Mostrar especialidades activas.
-   Mostrar imagen y descripción.
-   Mostrar información actualizada.

------------------------------------------------------------------------

## HU-003 Gestión de contenido institucional

Como administrador, quiero gestionar las especialidades visibles al
público para mantener actualizada la información institucional.

Prioridad: Media.

Criterios:

-   Crear especialidad.
-   Editar nombre, imagen y descripción.
-   Activar o desactivar especialidades.

------------------------------------------------------------------------

## HU-004 Consulta de disponibilidad

Como paciente, quiero consultar horarios disponibles para seleccionar
una cita adecuada.

Prioridad: Alta.

Criterios:

-   Mostrar únicamente horarios disponibles.
-   Evitar mostrar cupos ocupados.

------------------------------------------------------------------------

## HU-005 Reserva de cita

Como paciente, quiero reservar una cita médica para obtener atención
programada.

Prioridad: Alta.

Criterios:

-   Debe existir disponibilidad.
-   No permite doble reserva.
-   Genera confirmación.

------------------------------------------------------------------------

## HU-006 Cancelación y reprogramación

Como paciente, quiero modificar una cita para liberar o reutilizar un
cupo disponible.

Prioridad: Alta.

Criterios:

-   Liberar disponibilidad.
-   Registrar modificación.
-   Mantener historial.

------------------------------------------------------------------------

## HU-007 Gestión de agenda

Como personal administrativo, quiero administrar agendas médicas para
controlar disponibilidad.

Prioridad: Alta.

------------------------------------------------------------------------

## HU-008 Registro de llegada

Como recepcionista, quiero registrar la llegada del paciente para
iniciar el flujo de atención.

Prioridad: Alta.

------------------------------------------------------------------------

## HU-009 Evaluación inicial

Como personal de triaje, quiero registrar información inicial del
paciente para continuar el flujo asistencial.

Prioridad: Media.

------------------------------------------------------------------------

## HU-010 Atención profesional básica

Como profesional de salud, quiero registrar información básica de
atención para documentar la consulta.

Incluye:

-   observaciones;
-   diagnóstico básico;
-   indicaciones.

No incluye historia clínica electrónica completa.

Prioridad: Media.

------------------------------------------------------------------------

## HU-011 Dashboard administrativo

Como administrador, quiero visualizar métricas operativas para conocer
el comportamiento del servicio.

Criterios:

-   Filtros por rango de fechas.
-   Gráficos actualizados.
-   Métricas operativas.

------------------------------------------------------------------------

# 5. Requisitos funcionales

-   RF-001: Permitir registro e inicio de sesión.
-   RF-002: Gestionar roles y permisos.
-   RF-003: Mostrar información institucional.
-   RF-004: Gestionar especialidades públicas.
-   RF-005: Gestionar contenido de especialidades.
-   RF-006: Consultar disponibilidad médica.
-   RF-007: Registrar reservas de citas.
-   RF-008: Cancelar citas.
-   RF-009: Reprogramar citas.
-   RF-010: Evitar doble asignación de cupos.
-   RF-011: Gestionar profesionales.
-   RF-012: Administrar agendas.
-   RF-013: Registrar estados del flujo ambulatorio.
-   RF-014: Registrar información básica de atención. **Histórico; fuera del MVP actual.**
-   RF-015: Registrar acciones mediante auditoría.
-   RF-016: Gestionar notificaciones y recordatorios.
-   RF-017: Gestionar lista de espera.
-   RF-018: Mostrar dashboards administrativos configurables.
-   RF-019: Registrar llegada y admisión del paciente.
-   RF-020: Registrar evaluación inicial de triaje. **Histórico; fuera del MVP actual.**

------------------------------------------------------------------------

# 6. Requisitos no funcionales

-   RNF-001: Autenticación segura.
-   RNF-002: Autorización basada en roles.
-   RNF-003: Protección de información sensible.
-   RNF-004: Registro de eventos de seguridad.
-   RNF-005: Rendimiento adecuado.
-   RNF-006: Interfaz usable.
-   RNF-007: Accesibilidad básica.
-   RNF-008: Código mantenible.
-   RNF-009: Capacidad de evolución modular.
-   RNF-010: Compatibilidad con navegadores modernos y dispositivos
    móviles.
-   RNF-011: Mantener trazabilidad de cambios.
-   RNF-012: Registrar eventos técnicos.
-   RNF-013: Permitir incorporación futura de nuevos módulos.

------------------------------------------------------------------------

# 7. Reglas de negocio

-   RN-001: Una cita no puede asignarse a dos pacientes.
-   RN-002: Un profesional no puede tener citas simultáneas.
-   RN-003: Solo usuarios autorizados pueden modificar agendas.
-   RN-004: Una cita cancelada libera disponibilidad.
-   RN-005: Los cambios relevantes deben registrarse.
-   RN-006: La lista de espera requiere validación.
-   RN-007: Toda cita pertenece a una especialidad y profesional.
-   RN-008: El flujo ambulatorio debe respetar estados definidos.
-   RN-009: Una cita conserva historial de modificaciones.
-   RN-010: Una especialidad inactiva no aparece para nuevas reservas.
-   RN-011: Cada usuario ejecuta únicamente acciones permitidas por su
    rol.
-   RN-012: Los registros de atención solo pueden ser modificados por
    personal autorizado.

------------------------------------------------------------------------

# 8. Criterios de aceptación generales

## Reserva de cita

Dado un paciente activo, cuando selecciona una disponibilidad válida,
entonces el sistema registra la cita y confirma la operación.

## Gestión especialidades

Dado un administrador autorizado, cuando modifica una especialidad,
entonces la información pública se actualiza.

## Dashboard

Dado un administrador autorizado, cuando selecciona un rango de fechas,
entonces el sistema muestra métricas correspondientes.

## Recepción

Dado un paciente con cita válida, cuando llega al establecimiento,
entonces puede registrarse su ingreso.

## Control permisos

Dado un usuario sin permisos suficientes, cuando intenta acceder a una
función restringida, entonces el sistema rechaza la operación.

------------------------------------------------------------------------

# 9. Priorización MoSCoW

## Must Have

-   Registro e inicio de sesión.
-   Consulta disponibilidad.
-   Reserva de citas.
-   Cancelación y reprogramación.
-   Gestión agendas.
-   Gestión profesionales.
-   Roles y permisos.
-   Auditoría básica.
-   Gestión especialidades.
-   Recepción básica.
-   Dashboard administrativo básico.

## Should Have

-   Lista de espera.
-   Confirmación de citas.
-   Notificaciones.
-   Triaje básico.

## Could Have

-   Mejoras avanzadas móviles.
-   Automatizaciones adicionales.
-   Farmacia.
-   Facturación.

## Won't Have inicialmente

-   Historia clínica electrónica completa.
-   Integración oficial MINSA.
-   Emergencia.
-   Hospitalización.
-   Cirugía.
-   Farmacia completa.
-   Laboratorio.
-   Pagos completos.
-   Inteligencia artificial predictiva.

------------------------------------------------------------------------

# 10. MVP real

## Incluido

### Portal público

-   Información institucional.
-   Especialidades.
-   Servicios.
-   Acceso a reservas.

### Portal paciente

-   Registro.
-   Inicio de sesión.
-   Perfil básico.
-   Disponibilidad.
-   Reserva.
-   Cancelación.
-   Reprogramación.

### Plataforma operativa

-   Usuarios internos.
-   Roles.
-   Especialidades.
-   Profesionales.
-   Agendas.
-   Citas.
-   Estados de atención.
-   Recepción básica.
-   Registro profesional básico.

### Administrador

-   Gestión usuarios.
-   Gestión roles.
-   Gestión especialidades.
-   Dashboard configurable.
-   Auditoría.

## Fuera del MVP

-   Historia clínica electrónica completa.
-   Integración oficial MINSA.
-   Farmacia completa.
-   Facturación completa.
-   Laboratorio.
-   Emergencia.
-   Hospitalización.

------------------------------------------------------------------------

**Documento actualizado como PRD v1.4 FINAL.**
