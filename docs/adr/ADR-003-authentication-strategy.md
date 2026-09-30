# ADR-003 --- Estrategia de Autenticación Web

## Estado

Aceptado

## Decisión

Implementar:

-   JWT para acceso.
-   Refresh Token para renovación.

## Reglas

Access Token:

-   expiración corta.

Refresh Token:

-   renovación controlada;
-   revocación.

## Seguridad

Debe contemplar:

-   auditoría de accesos;
-   protección de credenciales;
-   cierre de sesión seguro.
