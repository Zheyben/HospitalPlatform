# MODULE CONTRACTS ARCHITECTURE

## Hospital Platform

## Estado

Versión 1.4 - Ownership profesional implementado para Appointment Operations.

**Control de vigencia E.3.2 (30/09/2026):** los contratos de lookup de paciente/profesional y reserva/liberación de slot que existen en Java mantienen su alcance actual. DEC-006 asigna conceptualmente a Catalogs las definiciones de Specialty y a Professionals la asociación N:M; no existe aún contrato Java, API o servicio funcional de esa gestión. DEC-007 aprueba solo la futura exposición sanitizada de disponibilidad para PATIENT/RECEPTIONIST; el GET operativo actual de Agenda es ADMIN. Usar C.3 para distinguir contratos existentes de propuestas, sin añadir dependencias nuevas por interpretación.

------------------------------------------------------------------------

# 1. Objetivo

Definir la arquitectura de contratos públicos utilizados para la
comunicación entre módulos internos de Hospital Platform.

El objetivo principal es mantener:

-   bajo acoplamiento;
-   separación de responsabilidades;
-   independencia entre módulos;
-   protección de entidades internas.

Los contratos permiten que un módulo consulte capacidades de otro módulo
sin acceder directamente a sus entidades, repositories o detalles
internos de implementación.

------------------------------------------------------------------------

# 2. Principios Arquitectónicos

## No acceder a entidades externas

Ejemplo incorrecto:

``` text
appointments
      |
      ↓
patients.entity.Patient
```

Un módulo consumidor nunca debe depender de entidades JPA pertenecientes
a otro módulo.

------------------------------------------------------------------------

## No acceder a repositories externos

Ejemplo incorrecto:

``` text
appointments
      |
      ↓
PatientRepository
```

Los repositories pertenecen exclusivamente al módulo propietario.

------------------------------------------------------------------------

## Comunicación mediante contratos públicos

Ejemplo correcto:

``` text
appointments

      ↓

PatientLookupService
```

Los contratos representan capacidades públicas del módulo.

------------------------------------------------------------------------

# 3. Motivación

A medida que el sistema crece, los módulos requieren información de
otros dominios.

Ejemplos:

Appointments necesita:

-   validar pacientes;
-   validar profesionales;
-   validar disponibilidad.

Pero no debe conocer:

-   entidades internas;
-   repositories;
-   estructura de base de datos.

La solución es definir contratos públicos mínimos.

------------------------------------------------------------------------

# 4. Alcance

## Incluye

-   contratos de consulta entre módulos;
-   referencias mínimas;
-   validaciones externas;
-   interfaces públicas.

## No incluye

-   CRUD completo;
-   lógica completa del dominio;
-   acceso directo a base de datos;
-   eventos distribuidos;
-   comunicación externa.

------------------------------------------------------------------------

# 5. Estado de implementación de contratos

Los contratos no implican que todos los módulos tengan implementación
completa.

Estado actual esperado:

  -----------------------------------------------------------------------
  Contrato                            Estado
  ----------------------------------- -----------------------------------
  PatientLookupService                Contrato + capacidad real en módulo
                                      patients

  ProfessionalLookupService           Contrato + validación de existencia y
                                      ownership implementadas

  AvailabilitySlotService             Contrato + capacidad real de consulta
                                      en módulo agenda

  AvailabilitySlotReservationService  Contrato + capacidad real de reserva
                                      atómica en módulo agenda

  AvailabilitySlotReleaseService      Contrato + capacidad real de liberación
                                      atómica en módulo agenda

  AuditLogService                     Contrato + capacidad real de auditoría
                                      transaccional en módulo audit
  -----------------------------------------------------------------------

Los contratos Professional y Agenda se definen para establecer la
arquitectura futura, pero no deben tener implementaciones falsas o
temporales en producción.

------------------------------------------------------------------------

# 6. Estructura esperada

Cada módulo expone sus contratos dentro de un paquete público.

Ejemplo:

``` text
patients

└── contract

    └── PatientLookupService
```

Los contratos pertenecen al módulo dueño de la información.

------------------------------------------------------------------------

# 7. PatientLookupService

## Ubicación

``` text
patients.contract
```

## Responsabilidad

Permitir consultar información mínima sobre pacientes.

Casos de uso:

-   validar existencia;
-   validar estado activo;
-   obtener referencia mínima.

------------------------------------------------------------------------

## Métodos conceptuales esperados

Ejemplo:

``` java
existsActivePatient(UUID patientId)

findPatientReference(UUID patientId)

findActivePatientReferenceByUserId(UUID userId)
```

Los nombres finales pueden adaptarse a la implementación manteniendo la
responsabilidad definida.

------------------------------------------------------------------------

## PatientReference

La referencia mínima debe ser un DTO/record de contrato.

Ejemplo:

``` text
PatientReference

id

active
```

Debe cumplir:

-   no ser una entidad JPA;
-   no exponer información sensible;
-   contener únicamente datos necesarios para consumidores externos.

El campo `active` es un valor derivado del estado del paciente.

Ejemplo conceptual:

``` text
active = deleted_at == null
```

No representa una nueva columna ni una segunda fuente de verdad.

------------------------------------------------------------------------

# 8. ProfessionalLookupService

## Ubicación

``` text
professionals.contract
```

## Estado

Contrato con capacidad real en el módulo professionals.

El módulo professionals contiene implementación foundation. No incluye
gestión avanzada de especialidades, agenda ni disponibilidad.

------------------------------------------------------------------------

## Responsabilidad

Permitir validar profesionales desde otros módulos.

Casos de uso:

-   verificar existencia;
-   verificar estado activo.
-   validar que un profesional activo esté vinculado al usuario autenticado.

------------------------------------------------------------------------

## Método conceptual esperado

Ejemplo:

``` java
existsActiveProfessional(UUID professionalId)

isActiveProfessionalLinkedToUser(UUID professionalId, UUID userId)
```

`existsActiveProfessional` e `isActiveProfessionalLinkedToUser` están
implementados en el módulo professionals.

El nuevo método devuelve `true` únicamente cuando el profesional existe, está
activo y está vinculado al usuario indicado. No retorna la entidad
`Professional`, información sensible ni detalles de persistencia.

------------------------------------------------------------------------

## No incluye

-   gestión de profesionales;
-   especialidades;
-   horarios;
-   disponibilidad.
-   autenticación o autorización de la operación consumidora.

------------------------------------------------------------------------

# 9. AvailabilitySlotService

## Ubicación

``` text
agenda.contract
```

## Estado

Contrato con capacidad real en el módulo agenda.

El módulo agenda contiene implementación foundation para schedules,
availability_slots y las transiciones atómicas `AVAILABLE` a `RESERVED` y
`RESERVED` a `AVAILABLE`. No incluye lifecycle de citas ni generación
automática de slots.

La implementación real es:

``` text
DatabaseAvailabilitySlotService
```

El contrato expone únicamente operaciones de consulta: `existsSlot`,
`isAvailable` e `isUsable`.

------------------------------------------------------------------------

## Responsabilidad

Permitir validar disponibilidad de slots.

Casos de uso:

-   verificar existencia de slot;
-   verificar disponibilidad;
-   validar si el slot es utilizable.

------------------------------------------------------------------------

## Métodos conceptuales esperados

Ejemplo:

``` java
existsSlot(UUID slotId)

isAvailable(UUID slotId)

isUsable(UUID slotId)
```

`isAvailable` comprueba únicamente que el estado persistido del slot sea
`AVAILABLE`.

`isUsable` devuelve verdadero únicamente cuando el estado persistido del
slot es `AVAILABLE` y el schedule asociado tiene `active = true`.

En esta fase `isUsable` no aplica reglas de fecha pasada, anticipación,
reserva ni reglas propias del módulo appointments.

------------------------------------------------------------------------

## AvailabilitySlotReservationService

Las consultas permanecen en `AvailabilitySlotService`. Los comandos de
reserva se exponen mediante un contrato público separado:

``` java
AvailabilitySlotReference reserveUsableSlot(UUID slotId)
```

Agenda es propietaria de la transición `AVAILABLE` a `RESERVED` y la realiza
mediante un UPDATE condicional. Appointments orquesta la transacción de negocio
sin acceder a repositories ni entidades JPA de Agenda.

`AvailabilitySlotReference` expone solamente `slotId`, `professionalId`,
`specialtyId`, `slotDate`, `startTime` y `endTime`.

La operación requiere una transacción existente y rechaza la reserva cuando el
slot no está disponible o el schedule asociado está inactivo.

------------------------------------------------------------------------

## AvailabilitySlotReleaseService

Los comandos de liberación se exponen mediante un contrato público separado:

``` java
void releaseReservedSlot(UUID slotId)
```

Agenda es propietaria de la transición `RESERVED` a `AVAILABLE` y la realiza
mediante un UPDATE condicional. La operación requiere una transacción existente
y rechaza slots inexistentes o cuyo estado no sea `RESERVED`.

------------------------------------------------------------------------

## No incluye

-   calendario completo;
-   generación automática de slots;
-   reglas avanzadas de agenda.

------------------------------------------------------------------------

# 10. Dependencias permitidas

Ejemplo:

``` text
appointments

    ↓

PatientLookupService

    ↓

patients
```

Ejemplo:

``` text
appointments

    ↓

AvailabilitySlotService

    ↓

agenda
```

------------------------------------------------------------------------

# 11. Dependencias prohibidas

No permitido:

``` text
appointments
      ↓
PatientRepository
```

No permitido:

``` text
appointments
      ↓
Patient Entity
```

No permitido:

``` text
appointments
      ↓
Database tables externas
```

------------------------------------------------------------------------

# 12. Seguridad y exposición de información

Los contratos deben exponer únicamente información necesaria.

No deben retornar:

-   passwords;
-   tokens;
-   permisos internos;
-   información sensible no requerida.

Cada módulo mantiene propiedad sobre sus datos internos.

------------------------------------------------------------------------

# 13. Testing

Los contratos deben validar:

-   comportamiento esperado;
-   respuesta ante recursos inexistentes;
-   manejo de errores controlados.

Los módulos consumidores deben probar sus integraciones mediante:

-   mocks;
-   implementaciones de prueba;
-   pruebas de integración cuando corresponda.

------------------------------------------------------------------------

# 14. AuditLogService

## Ubicación

``` text
audit.contract
```

## Responsabilidad

Registrar eventos de dominio mediante la persistencia propiedad del módulo
audit, sin exponer entidades JPA, repositories ni detalles PostgreSQL.

El contrato recibe el tipo de evento, nombre e identificador de entidad y los
valores anteriores y nuevos admitidos por `audit_logs`. La implementación
resuelve el actor autenticado mediante `CurrentUserService`.

La operación utiliza `Propagation.MANDATORY`: el registro participa en la misma
transacción del caso de uso consumidor y desaparece si esa transacción revierte.

------------------------------------------------------------------------

# 15. Evolución futura

Nuevos contratos pueden agregarse según necesidad real.

Ejemplos:

``` text
NotificationService

BillingService

MedicalRecordService

InventoryService
```

No crear contratos sin un caso de uso concreto.

------------------------------------------------------------------------

# 16. Relación con Appointment Module

El módulo appointments consume los siguientes contratos públicos:

``` text
appointments

    ├── PatientLookupService
    |
    ├── ProfessionalLookupService
    |
    ├── AvailabilitySlotReservationService
    |
    ├── AvailabilitySlotReleaseService
    |
    ├── AvailabilitySlotReference
    |
    ├── AuditLogService
    |
    └── CurrentUserService
```

Esto permite implementar appointments sin romper independencia modular.

`AppointmentService` orquesta `AvailabilitySlotReservationService`,
`AvailabilitySlotReleaseService` y `AuditLogService` dentro de la misma
transacción para crear, cancelar y reprogramar citas sin acceder a repositories
ni entidades de los módulos proveedores. `CurrentUserService` resuelve el actor
autenticado necesario para ownership, cancelación y auditoría sin exponer la
entidad de usuario.

Para Appointment Operations, `AppointmentService` utiliza
`ProfessionalLookupService.isActiveProfessionalLinkedToUser` antes de iniciar o
completar la atención. Appointments seguirá sin acceder a
`ProfessionalRepository`, `Professional` ni tablas internas de professionals.

La existencia del contrato no garantiza que el módulo proveedor esté
implementado completamente.

Antes de implementar consumidores, deben existir las capacidades
proveedoras necesarias.

------------------------------------------------------------------------

# Historial de cambios

## v1.2

Cambios aplicados después de auditoría:

-   agregado estado de implementación de contratos;
-   aclarada diferencia entre contrato e implementación real;
-   aclarado significado de PatientReference.active;
-   agregado método conceptual para ProfessionalLookupService;
-   agregado métodos conceptuales para AvailabilitySlotService;
-   reforzada regla de no crear implementaciones falsas.

## v1.3

-   aprobado `isActiveProfessionalLinkedToUser(UUID, UUID)` como contrato
    público mínimo de ownership profesional;
-   aclarado que el método está aprobado pero todavía no implementado;
-   documentada su utilización futura por Appointment Operations sin romper
    límites modulares.

## v1.4

-   implementado `isActiveProfessionalLinkedToUser(UUID, UUID)` mediante el
    repository interno de professionals;
-   integrado el contrato en Appointment Operations para validar ownership sin
    exponer entidades ni persistencia.
