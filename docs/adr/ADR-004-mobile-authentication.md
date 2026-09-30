# ADR-004 --- Autenticación Móvil

## Estado

Aceptado

## Decisión

Usar OAuth2 + PKCE para autenticación móvil.

## Contexto

La aplicación móvil requiere:

-   protección de tokens;
-   renovación segura;
-   cierre de sesión;
-   revocación.

## Reglas

La aplicación móvil no almacenará:

-   contraseñas;
-   secretos sensibles.

## Consecuencia

Permite una autenticación segura para clientes móviles.
