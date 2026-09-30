# ADR-005 --- Modelo de Roles y Permisos

## Estado

Aceptado

**Nota de vigencia E.3.2:** se conserva la decisión RBAC original. Para el caso académico actual, DEC-002 (`CLOSED`) delimita los roles aprobados a `ADMIN`, `PATIENT`, `RECEPTIONIST` y `PROFESSIONAL`. La lista de roles MVP que sigue es antecedente de este ADR: `TRIAGE` y `SYSTEM` no reciben operaciones de negocio ni permisos nuevos. `SYSTEM` en CU-D8 designa ejecución técnica interna. Esta nota no cambia el estado histórico del ADR ni cierra DEC-020.

## Decisión

Implementar RBAC (Role Based Access Control).

## Roles MVP

**Lista histórica; aplicar la delimitación posterior de DEC-002 indicada arriba.**

  Rol            Responsabilidad
  -------------- -------------------------------------
  PATIENT        Gestionar sus propias citas
  ADMIN          Administración completa del sistema
  PROFESSIONAL   Gestión de atención asignada
  RECEPTIONIST   Gestión de citas y check-in
  TRIAGE         Gestión del flujo operativo
  SYSTEM         Procesos internos del sistema

## Roles futuros

-   PHARMACY
-   BILLING
-   EMERGENCY_STAFF

## Modelo

User → UserRole → Role → Permission
