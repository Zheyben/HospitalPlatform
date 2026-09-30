# ADR-001 --- Arquitectura Monolito Modular

## Estado

Aceptado

## Decisión

Se utilizará una arquitectura Monolito Modular con separación por
dominios funcionales.

## Módulos principales

-   auth
-   users
-   patients
-   professionals
-   catalogs
-   agenda
-   appointments
-   waitlist
-   priority
-   notifications
-   dashboard
-   audit

## Regla de modularidad

Los módulos no deben acceder directamente a tablas internas de otros
módulos.

La comunicación entre módulos debe realizarse mediante servicios o
contratos definidos.

## Alternativas descartadas

### Microservicios

Descartado para MVP por complejidad operacional e infraestructura
adicional.

### Monolito sin módulos

Descartado por alto acoplamiento y menor mantenibilidad.

## Consecuencias

Positivas:

-   desarrollo más rápido;
-   límites claros;
-   evolución futura.

Negativas:

-   requiere disciplina arquitectónica.
