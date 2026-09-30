# ADR-009 --- Estrategia de Identificadores de Base de Datos

## Estado

Aceptado - Versión 1.1

## Decisión

Utilizar UUID como identificador principal.

## Generación

La generación de UUID será responsabilidad del backend.

## Contexto

La plataforma expone información mediante APIs y aplicaciones cliente.

## Motivos

UUID permite:

-   evitar exposición de secuencias internas;
-   mejorar compatibilidad con APIs;
-   facilitar evolución futura.

## Aplicación

Se utilizará en:

-   users;
-   patients;
-   professionals;
-   appointments;
-   availability_slots;
-   audit_logs.
