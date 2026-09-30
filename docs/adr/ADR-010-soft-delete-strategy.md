# ADR-010 --- Estrategia Soft Delete

## Estado

Aceptado - Versión 1.1

## Decisión

Aplicar eliminación lógica donde sea necesario conservar historial.

## Implementación

Las entidades con soft delete utilizarán:

``` text
deleted_at
```

## Aplicación

Usar soft delete en:

-   users;
-   professionals;
-   specialties.

## No aplicar

No se eliminan físicamente:

-   audit_logs;
-   citas históricas.

## Motivo

Mantener trazabilidad y consistencia histórica.
