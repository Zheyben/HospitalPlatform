# ADR-002 --- Base de Datos PostgreSQL

## Estado

Aceptado

## Decisión

PostgreSQL será la base de datos principal.

## Stack relacionado

-   PostgreSQL
-   JPA/Hibernate
-   Flyway para migraciones

## Motivo

El dominio requiere:

-   relaciones complejas;
-   integridad de datos;
-   transacciones;
-   auditoría;
-   control de concurrencia.

## Datos principales

-   usuarios;
-   roles;
-   pacientes;
-   profesionales;
-   especialidades;
-   agenda;
-   slots;
-   citas;
-   auditoría.
