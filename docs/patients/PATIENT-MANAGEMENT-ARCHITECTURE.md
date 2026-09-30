# PATIENT MANAGEMENT ARCHITECTURE

## Hospital Platform

## Estado

Versión 1.1 - Ajustes posteriores a auditoría del módulo Patient
Management Foundation (FASE 5.4).

**Control de vigencia E.3.2:** `PatientController` permite gestión administrativa y lectura propia `GET /patients/me` para PATIENT. La autogestión de alta/actualización continúa pendiente por DEC-017. DEC-002 no aprueba `SYSTEM` como rol de negocio actual; su mención posterior en este documento no concede acceso. La autorización efectiva depende de las anotaciones del controller y de la propiedad del perfil.

------------------------------------------------------------------------

# 1. Objetivo

Definir la arquitectura funcional y técnica del módulo de gestión de
pacientes de Hospital Platform.

Este documento establece:

-   responsabilidades del módulo patients;
-   relación con users;
-   casos de uso;
-   modelo de datos;
-   endpoints;
-   DTOs;
-   reglas de seguridad;
-   estrategia de testing.

------------------------------------------------------------------------

# 2. Contexto

El módulo patients forma parte de la arquitectura de Monolito Modular
definida para Hospital Platform.

Módulos relacionados:

-   auth;
-   users;
-   patients;
-   professionals;
-   catalogs;
-   agenda;
-   appointments;
-   waitlist;
-   priority;
-   notifications;
-   dashboard;
-   audit.

El módulo users administra:

-   identidad del usuario;
-   autenticación;
-   roles;
-   permisos.

El módulo patients administra:

-   información administrativa del paciente;
-   datos propios del registro paciente;
-   relación paciente-usuario.

La autenticación permanece separada en auth/security.

------------------------------------------------------------------------

# 3. Alcance

## Incluye

-   creación de pacientes;
-   consulta de pacientes;
-   consulta individual;
-   actualización de información administrativa;
-   vinculación con usuario existente;
-   gestión del ciclo de vida del paciente mediante Soft Delete.

## No incluye

-   historia clínica;
-   diagnósticos;
-   tratamientos;
-   recetas;
-   laboratorio;
-   imágenes médicas;
-   citas;
-   agenda médica.

El registro público de pacientes queda fuera del alcance de esta fase.

------------------------------------------------------------------------

# 4. Arquitectura del módulo

Mantener:

Package by Feature.

``` text
patients/

├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
├── exception
└── specification
```

Regla:

El módulo patients no debe acceder directamente a tablas internas de
otros módulos.

La comunicación entre módulos debe realizarse mediante servicios o
contratos definidos.

------------------------------------------------------------------------

# 5. Responsabilidad del módulo

El módulo patients administra:

-   datos administrativos del paciente;
-   identificación documental;
-   información de contacto;
-   fecha de nacimiento;
-   ciclo de vida del paciente mediante eliminación lógica;
-   vínculo con usuario del sistema.

No administra:

-   credenciales;
-   autenticación;
-   roles;
-   permisos.

------------------------------------------------------------------------

# 6. Relación User - Patient

Modelo:

``` text
User

1

|

0..1

|

Patient
```

Un usuario puede tener un perfil paciente asociado.

La relación utiliza:

``` text
patients.user_id
```

como referencia hacia:

``` text
users.id
```

Un paciente puede existir sin usuario asociado.

Un paciente sin usuario asociado no puede autenticarse en la plataforma
hasta que sea vinculado con un usuario del sistema.

------------------------------------------------------------------------

# 7. Modelo de datos

Tabla:

``` text
patients
```

Campos:

``` text
id UUID

user_id UUID

document_type

document_number

birth_date

phone

address

deleted_at

created_at

updated_at
```

Los identificadores utilizan UUID según la estrategia definida.

------------------------------------------------------------------------

# 8. Identificadores

El módulo utiliza UUID como identificador principal.

Motivos:

-   evitar exposición de secuencias internas;
-   compatibilidad con APIs;
-   evolución futura del sistema.

------------------------------------------------------------------------

# 9. Soft Delete

Patients utiliza eliminación lógica mediante:

``` text
deleted_at
```

No se realiza eliminación física.

Motivo:

-   conservar historial;
-   mantener trazabilidad;
-   permitir auditoría futura.

------------------------------------------------------------------------

# 10. Casos de uso

## Crear paciente

Actor:

ADMIN.

Regla:

En esta fase únicamente ADMIN puede crear pacientes.

El registro público de pacientes queda fuera del alcance.

Validaciones:

-   documento válido;
-   datos obligatorios presentes;
-   evitar duplicidad documental.

------------------------------------------------------------------------

## Consultar pacientes

Actor:

ADMIN.

Permite:

-   listar pacientes;
-   consultar información administrativa.

------------------------------------------------------------------------

## Consultar perfil propio

Actor:

Usuario con rol PATIENT.

Permite consultar su información mediante el usuario autenticado.

------------------------------------------------------------------------

## Actualizar paciente

Actor:

ADMIN.

Permite actualizar:

-   teléfono;
-   dirección;
-   información administrativa permitida.

------------------------------------------------------------------------

## Vincular usuario

Actor:

ADMIN.

Permite asociar un paciente existente con un usuario del sistema.

------------------------------------------------------------------------

## Desactivar paciente

Actor:

ADMIN.

Regla:

La desactivación aplica Soft Delete mediante deleted_at.

No elimina físicamente información.

------------------------------------------------------------------------

# 11. Seguridad

El módulo utiliza la seguridad existente:

-   JWT;
-   RBAC;
-   SecurityContext.

Roles relacionados:

La lista siguiente es antecedente de diseño; `SYSTEM` no es un rol autorizado para una operación actual de Patients.

``` text
PATIENT

ADMIN

SYSTEM
```

Reglas:

ADMIN:

-   gestionar pacientes;
-   consultar pacientes.

PATIENT:

-   consultar únicamente su propio perfil.

El módulo patients no administra permisos.

------------------------------------------------------------------------

# 12. API REST

Base:

``` text
/api/v1/patients
```

Endpoints previstos:

``` http
POST   /api/v1/patients

GET    /api/v1/patients

GET    /api/v1/patients/{id}

GET    /api/v1/patients/me

PUT    /api/v1/patients/{id}

PATCH  /api/v1/patients/{id}/status

POST   /api/v1/patients/{id}/user
```

La actualización de estado debe respetar la estrategia Soft Delete
mediante deleted_at.

------------------------------------------------------------------------

# 13. DTOs

No exponer entidades JPA directamente.

DTOs:

``` text
PatientResponseDTO

CreatePatientRequestDTO

UpdatePatientRequestDTO

UpdatePatientStatusRequestDTO

LinkUserRequestDTO
```

------------------------------------------------------------------------

# 13.1 DTO Details

## CreatePatientRequestDTO

Responsable de crear pacientes.

Campos principales:

``` json
{
  "documentType": "DNI",
  "documentNumber": "12345678",
  "birthDate": "1990-01-01",
  "phone": "999999999",
  "address": "Direccion"
}
```

Reglas:

-   validar documento;
-   evitar duplicidad.

------------------------------------------------------------------------

## UpdatePatientRequestDTO

Permite actualizar información administrativa.

No permite:

-   modificar identificador;
-   modificar relaciones internas sin autorización.

------------------------------------------------------------------------

## UpdatePatientStatusRequestDTO

Responsable de cambiar estado del paciente.

Ejemplo:

``` json
{
  "status": "INACTIVE"
}
```

La operación debe aplicar la estrategia Soft Delete mediante deleted_at.

------------------------------------------------------------------------

## LinkUserRequestDTO

Asocia paciente con usuario existente.

Reglas:

-   usuario debe existir;
-   no crear usuarios desde patients.

------------------------------------------------------------------------

# 14. Auditoría

Eventos preparados:

``` text
PATIENT_CREATED

PATIENT_UPDATED

PATIENT_DISABLED

PATIENT_LINKED_TO_USER
```

La auditoría utilizará el módulo audit.

------------------------------------------------------------------------

# 15. Testing

Debe cubrir:

Pacientes:

-   creación correcta;
-   documento duplicado;
-   consulta;
-   actualización;
-   desactivación mediante Soft Delete.

Relación:

-   vinculación con usuario existente;
-   usuario inexistente rechazado.

Seguridad:

-   ADMIN permitido;
-   PATIENT solo accede a su información;
-   usuario sin permisos rechazado.

------------------------------------------------------------------------

# 16. Próximos pasos

1.  Auditar documento.
2.  Implementar módulo patients.
3.  Ejecutar pruebas.
4.  Auditar implementación.

------------------------------------------------------------------------

# Historial de cambios

## v1.1

Ajustes aplicados:

-   Se mantuvo todo el contenido de la versión v1.0.
-   Se aclaró la relación User-Patient.
-   Se agregó la restricción de creación de pacientes por ADMIN en MVP.
-   Se eliminó la ambigüedad entre enabled y deleted_at.
-   Se alineó el estado del paciente con la estrategia Soft Delete.
