# SECURITY ARCHITECTURE

## Hospital Platform

## Estado

Versión 1.1 - Diseño actualizado después de auditoría FASE 5.2
Security + Auth Foundation.

**Control de vigencia E.3.2:** la autenticación JWT/refresh y las verificaciones de rol existentes se contrastan con `AuthController`, `SecurityConfiguration` y los controllers de negocio. DEC-002 aprueba para el caso actual únicamente `ADMIN`, `PATIENT`, `RECEPTIONIST` y `PROFESSIONAL`; `TRIAGE` y `SYSTEM` permanecen como menciones históricas/enum, sin permiso de negocio actual. Los pasos de implementación al final del documento describen el plan de la fase original, no tareas aún sin ejecutar. DEC-020 mantiene pendiente la política de permisos granulares.

------------------------------------------------------------------------

# 1. Objetivo

Definir la arquitectura de seguridad y autenticación de Hospital
Platform antes de iniciar la implementación.

Este documento establece:

-   modelo de autenticación;
-   autorización;
-   JWT;
-   refresh tokens;
-   roles y permisos;
-   responsabilidades de módulos;
-   reglas de seguridad.

------------------------------------------------------------------------

# 2. Alcance

Esta fase implementa la base de seguridad del sistema.

Incluye:

-   Spring Security;
-   autenticación mediante JWT;
-   refresh tokens;
-   RBAC;
-   usuarios;
-   roles;
-   permisos.

No incluye:

-   recuperación de contraseña;
-   MFA;
-   OAuth externo;
-   LDAP;
-   proveedores externos.

------------------------------------------------------------------------

# 3. Arquitectura de seguridad

El sistema utiliza:

``` text
Spring Security

+

JWT

+

RBAC
```

Modelo:

``` text
Usuario

↓

Autenticación

↓

JWT Access Token

↓

Autorización mediante roles y permisos
```

------------------------------------------------------------------------

# 4. Módulos involucrados

## security

Responsable de:

-   configuración Spring Security;
-   filtros JWT;
-   validación de tokens;
-   contexto autenticado;
-   excepciones de seguridad.

## auth

Responsable de:

-   login;
-   generación de tokens;
-   renovación de sesión;
-   logout.

## users

Responsable de:

-   usuarios;
-   credenciales;
-   roles asociados.

------------------------------------------------------------------------

# 5. Flujo de autenticación

``` text
Cliente

↓

POST /api/v1/auth/login

↓

AuthService

↓

AuthenticationManager

↓

Validación usuario/password

↓

Generación JWT

↓

Respuesta con tokens
```

------------------------------------------------------------------------

# 6. Access Token

Características:

-   corta duración;
-   enviado mediante Bearer Token;
-   recomendado: 15 minutos.

## Algoritmo de firma

Para la primera versión:

``` text
HS256
```

Motivo:

-   menor complejidad operacional;
-   adecuado para arquitectura monolítica actual.

Una migración futura a RS256 podrá evaluarse si el sistema evoluciona
hacia múltiples servicios.

------------------------------------------------------------------------

# 7. Refresh Token

Características:

-   mayor duración;
-   permite renovar sesiones;
-   recomendado: 7 días.

## Almacenamiento

Los refresh tokens NO se almacenarán en texto plano.

Se almacenará un hash del token.

Modelo:

``` text
refresh_tokens

id UUID

user_id

token_hash

expires_at

revoked_at

created_at
```

Reglas:

-   cada refresh token pertenece a un usuario;
-   puede ser revocado;
-   tokens expirados no pueden renovarse;
-   no guardar secretos completos en base de datos.

------------------------------------------------------------------------

# 8. Modelo JWT

No debe contener información sensible.

Permitido:

``` json
{
  "sub": "user-id",
  "roles": [
    "PATIENT"
  ]
}
```

No incluir:

-   contraseñas;
-   datos personales;
-   información clínica.

------------------------------------------------------------------------

# 9. Usuarios

Basado en tabla users:

``` text
id UUID

username

email

password_hash

enabled
```

Reglas:

-   nunca guardar passwords planos;
-   utilizar BCrypt;
-   usuarios deshabilitados no pueden autenticarse.

Futura evolución:

-   bloqueo de cuenta;
-   políticas de intentos fallidos.

------------------------------------------------------------------------

# 10. Roles y permisos

Modelo:

``` text
User

N:M

Role

N:M

Permission
```

Roles iniciales del diseño anterior (delimitados después por DEC-002):

``` text
ADMIN

PROFESSIONAL

PATIENT

RECEPTIONIST

TRIAGE

SYSTEM
```

**Roles aprobados para operaciones actuales:** `ADMIN`, `PATIENT`, `RECEPTIONIST`, `PROFESSIONAL`. La lista anterior no amplía los accesos de los controllers.

------------------------------------------------------------------------

# 11. Autorización

Dos niveles:

## Rol

Control general de acceso.

## Propiedad del recurso

Validación de que el usuario puede actuar sobre ese recurso.

Ejemplo:

``` text
PATIENT

solo gestiona

sus propias citas
```

------------------------------------------------------------------------

# 12. Endpoints iniciales

## Login

``` http
POST /api/v1/auth/login
```

## Refresh Token

``` http
POST /api/v1/auth/refresh
```

## Logout

``` http
POST /api/v1/auth/logout
```

El logout permitirá revocar refresh tokens activos.

------------------------------------------------------------------------

# 13. Reglas de implementación

-   No exponer entidades directamente.
-   Usar DTOs.
-   Separar seguridad de lógica de negocio.
-   Usar variables de entorno.
-   Auditar acciones críticas.
-   No almacenar tokens sensibles sin protección.

------------------------------------------------------------------------

# 14. Testing

Validar:

-   login correcto;
-   credenciales inválidas;
-   expiración de tokens;
-   refresh token;
-   revocación de tokens;
-   autorización por roles;
-   acceso indebido a recursos.

------------------------------------------------------------------------

# 15. Próximos pasos

1.  Implementar User, Role y Permission.
2.  Implementar RefreshToken.
3.  Crear repositorios.
4.  Implementar JWT.
5.  Crear AuthService.
6.  Crear endpoints de autenticación.
7.  Auditar implementación.
