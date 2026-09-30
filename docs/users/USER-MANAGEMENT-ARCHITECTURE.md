# USER MANAGEMENT ARCHITECTURE

## Hospital Platform

## Estado

Versión 1.3 - Diseño actualizado después de auditoría de implementación
de la FASE 5.3 User Management Foundation.

**Control de vigencia E.3.2:** la creación, consulta, actualización, cambio de estado y asignación de roles se contrastan con `UserController` y sus DTOs actuales. DEC-002 delimita los roles de negocio aprobados a `ADMIN`, `PATIENT`, `RECEPTIONIST` y `PROFESSIONAL`. Los valores `TRIAGE`/`SYSTEM` mencionados en la lista original no habilitan operaciones actuales; no se infiere permiso por su presencia en un enum o tabla. DEC-003 sobre bootstrap y DEC-020 sobre granularidad permanecen `OPEN`/`PROPOSED` respectivamente.

------------------------------------------------------------------------

# 1. Objetivo

Definir la arquitectura funcional y técnica del módulo de gestión de
usuarios de Hospital Platform.

Este documento establece:

-   responsabilidades del módulo users;
-   casos de uso;
-   modelo de datos;
-   endpoints;
-   DTOs;
-   reglas de seguridad;
-   estrategia de testing.

------------------------------------------------------------------------

# 2. Contexto

Esta fase se construye sobre:

-   Backend Foundation.
-   Security + Auth Foundation.

La autenticación ya existe.

El objetivo actual es administrar los usuarios que utilizan la
plataforma.

La autenticación permanece separada en los módulos auth/security.

------------------------------------------------------------------------

# 3. Alcance

Incluye:

-   creación de usuarios;
-   consulta de usuarios;
-   actualización de usuarios;
-   activación/desactivación;
-   gestión de roles.

No incluye:

-   pacientes;
-   profesionales;
-   citas;
-   agenda;
-   recuperación de contraseña;
-   MFA;
-   OAuth externo.

------------------------------------------------------------------------

# 4. Arquitectura del módulo

Mantener:

Package by Feature.

Estructura:

``` text
users/

├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
├── exception
└── specification
```

------------------------------------------------------------------------

# 5. Responsabilidad del módulo

El módulo users administra:

-   identidad de usuarios;
-   estado de cuenta;
-   datos básicos del usuario;
-   credenciales;
-   roles asociados.

La autenticación permanece en el módulo auth/security.

------------------------------------------------------------------------

# 6. Casos de uso

## Crear usuario

Actor:

ADMIN

Validaciones:

-   email único;
-   username único;
-   password cifrado;
-   rol válido.

## Consultar usuarios

Actor:

ADMIN.

Permite:

-   listar usuarios;
-   buscar usuarios;
-   consultar estado.

## Consultar perfil propio

Actor:

Usuario autenticado.

Permite:

-   consultar datos propios;
-   consultar roles asignados.

## Actualizar usuario

Actor:

ADMIN.

Permite:

-   actualizar información básica;
-   modificar estado.

No incluye cambio de contraseña.

El cambio de contraseña será tratado como un flujo separado.

## Activar/desactivar usuario

Actor:

ADMIN.

Regla:

Usuario deshabilitado no puede autenticarse.

## Gestión de roles

Actor:

ADMIN.

Permite:

-   asignar roles;
-   retirar roles.

------------------------------------------------------------------------

# 7. Modelo de usuario

Basado en tabla:

users

Campos:

``` text
id UUID

username

email

first_name

last_name

password_hash

enabled

created_at

updated_at
```

Los campos first_name y last_name representan la información básica de
identificación del usuario.

------------------------------------------------------------------------

# 8. Relaciones

Modelo:

``` text
User

N:M

Role

N:M

Permission
```

Las tablas existentes de roles y permisos no deben modificarse.

------------------------------------------------------------------------

# 9. Roles iniciales

La siguiente lista documenta el diseño anterior; para el alcance vigente aplicar DEC-002 y las autorizaciones reales de controllers.

``` text
ADMIN

PROFESSIONAL

PATIENT

RECEPTIONIST

TRIAGE

SYSTEM
```

------------------------------------------------------------------------

# 10. Seguridad

ADMIN puede:

-   crear usuarios;
-   modificar usuarios;
-   asignar roles;
-   consultar cualquier usuario.

Usuario autenticado:

-   puede consultar su propio perfil mediante `/api/v1/users/me`.

No puede:

-   modificar usuarios;
-   cambiar roles;
-   consultar información de otros usuarios mediante endpoints
    administrativos.

Consulta individual:

``` http
GET /api/v1/users/{id}
```

Reglas:

-   ADMIN puede consultar cualquier usuario.
-   Usuario autenticado consulta su propio perfil mediante `/me`.

------------------------------------------------------------------------

# 11. API REST

Base:

``` text
/api/v1/users
```

Endpoints:

``` text
POST   /api/v1/users

GET    /api/v1/users

GET    /api/v1/users/{id}

GET    /api/v1/users/me

PUT    /api/v1/users/{id}

PATCH  /api/v1/users/{id}/status

POST   /api/v1/users/{id}/roles
```

Respuestas principales:

``` text
200 OK
201 Created
403 Forbidden
404 Not Found
409 Conflict
```

------------------------------------------------------------------------

# 12. DTOs

No exponer entidades JPA directamente.

DTOs:

``` text
UserResponseDTO

CreateUserRequestDTO

UpdateUserRequestDTO

UpdateUserStatusRequestDTO

AssignRoleRequestDTO
```

------------------------------------------------------------------------

# 12.1 DTO Details

Esta sección define DTOs críticos con reglas funcionales importantes.

## CreateUserRequestDTO

Responsable de crear usuarios.

Campos principales:

``` json
{
  "username": "usuario",
  "email": "correo@example.com",
  "password": "password",
  "roles": [
    "ADMIN"
  ]
}
```

Reglas:

-   password nunca se almacena directamente;
-   debe cifrarse mediante BCrypt;
-   los roles deben existir previamente.

------------------------------------------------------------------------

## UpdateUserRequestDTO

Responsable de actualizar información básica.

Campos permitidos:

``` json
{
  "username": "nuevo_usuario",
  "email": "nuevo@email.com"
}
```

No permite:

-   password;
-   roles;
-   estado.

------------------------------------------------------------------------

## UpdateUserStatusRequestDTO

Responsable únicamente de cambiar el estado de cuenta.

Ejemplo:

``` json
{
  "enabled": false
}
```

Reglas:

-   solo ADMIN puede ejecutar esta operación;
-   un usuario con enabled=false no puede autenticarse;
-   no elimina el usuario de la base de datos.

------------------------------------------------------------------------

## AssignRoleRequestDTO

Responsable de asignación de roles.

Ejemplo:

``` json
{
  "role": "PROFESSIONAL"
}
```

Reglas:

-   el rol debe existir;
-   no permite crear roles nuevos;
-   solo ADMIN puede ejecutar esta operación.

------------------------------------------------------------------------

# 13. Reglas técnicas

-   Passwords siempre cifrados con BCrypt.
-   Emails únicos.
-   Validación mediante DTOs.
-   Mapper obligatorio.
-   Services contienen lógica de negocio.
-   Controllers manejan únicamente HTTP.

------------------------------------------------------------------------

# 14. Auditoría futura

Eventos preparados:

``` text
USER_CREATED

USER_UPDATED

USER_DISABLED

ROLE_ASSIGNED
```

------------------------------------------------------------------------

# 15. Testing

Debe cubrir:

Usuarios:

-   creación correcta;
-   email duplicado;
-   username duplicado;
-   actualización;
-   usuario deshabilitado.

Roles:

-   asignación correcta;
-   permisos insuficientes.

Seguridad:

-   ADMIN permitido;
-   usuario normal rechazado.

------------------------------------------------------------------------

# 16. Próximos pasos

1.  Implementar módulo users.
2.  Ejecutar pruebas.
3.  Auditar implementación.

------------------------------------------------------------------------

# Historial de cambios

## v1.3

Ajustes aplicados:

-   Mantiene todo el contenido arquitectónico de v1.2.
-   Agregada sección 12.1 DTO Details.
-   Documentadas reglas funcionales de DTOs críticos.
-   Documentados ejemplos de payload.
