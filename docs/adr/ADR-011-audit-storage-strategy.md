# ADR-011 --- Estrategia de Almacenamiento de Auditoría

## Estado

Aceptado - Versión 1.1

## Decisión

Utilizar audit_logs con almacenamiento JSONB.

## Modelo conceptual

audit_logs:

-   id;
-   user_id;
-   action;
-   entity_name;
-   entity_id;
-   old_values JSONB;
-   new_values JSONB;
-   ip_address;
-   user_agent;
-   created_at.

## Motivo

Permite registrar cambios sin modificar el esquema ante nuevas entidades
o atributos.

## Beneficios

-   trazabilidad completa;
-   flexibilidad;
-   soporte para auditoría futura.
