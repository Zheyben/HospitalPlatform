# IDEF0 — Balance y trazabilidad de flechas

## Objetivo

Verificar que A0 conserve las flechas ICOM de A-0 sin introducir entradas o salidas externas nuevas.

## Alcance

Frontera funcional del TO-BE académico. La descomposición usa los mismos IDs de flecha; la flecha interna «slot derivado de agenda» no cruza la frontera.

## Fuente

`IDEF0-A-0.md`, `IDEF0-A0.md`; `docs/auditoria/TO-BE.md`; SRS 0.1; RB; ADR-001/002/005–008.

## Actores

M1 paciente/visitante/personal y M2 plataforma son mecanismos en ambos niveles.

## Actividades

A1–A6 y T01–T29 asignadas en `IDEF0-A0.md`.

## Decisiones

D01–D13 asignadas en `IDEF0-A0.md`; no se modelan como flechas de frontera.

## Requisitos relacionados

RF-001–033; RNF-001–013 como criterios y verificaciones.

## Reglas de negocio relacionadas

RB-001–024.

## Exclusiones

No se añaden datos hospitalarios reales, actores como inputs ni métricas medidas.

## Pendientes de validación

C3 queda condicional hasta aprobación institucional de políticas; no se fija plazo ni elegibilidad concreta.

## Archivo fuente del diagrama

`IDEF0-A-0.mmd` y `IDEF0-A0.mmd`; la tabla siguiente gobierna su lectura ICOM.

| Flecha | Tipo I/C/O/M | A-0 | A0 | Fuente |
|---|---|---|---|---|
| I1 | I | Solicitudes/datos mínimos | A1 y A3 | RF-001/005/013; RB-001/006 |
| I2 | I | Selección y acciones sobre cita | A2/A3 | RF-012–019; RB-004–011 |
| I3 | I | Respuestas de espera/prioridad/consulta | A3/A4/A5/A6 según consulta | RF-006/020–025/030–031; RB-012–015 |
| C1 | C | SRS/RB | Control de A1–A6 | SRS 0.1; RB-001–024 |
| C2 | C | Permisos y estados | Control de A1/A3/A4/A5/A6 | ADR-005/007; RB-002/016–020 |
| C3 | C | Políticas validadas cuando existan | Control de A2/A3/A4/A5 | RB-008–009/012–015/022/024 |
| C4 | C | Seguridad/privacidad/contenido/accesibilidad | Control de A1–A6 | RNF-001/002/006; RB-001/021–023 |
| O1 | O | Cuenta/sesión/perfil/vistas | A1 y A5/A6 | RF-001–006/030/033 |
| O2 | O | Cita/constancia/estados | A3 y A5 | RF-013–019/026–029; RB-006–011/019 |
| O3 | O | Espera/oferta/prioridad | A4 | RF-020–022/024–025; RB-012–015 |
| O4 | O | Flujo/AuditLog/indicadores | A5 y A6 | RF-026–032; RB-020/024 |
| O5 | O | Contenido/catálogo/disponibilidad | A2 | RF-007–012; RB-003–004/022 |
| M1 | M | Paciente/visitante/personal autorizado | Mecanismo de A1–A6 según tarea | TO-BE §3; RB-002 |
| M2 | M | HospitalPlatform/backend/PostgreSQL/web/app | Mecanismo de A1–A6 | ADR-001/002; roadmap §9/§14 |

**Resultado del balance:** 3 entradas, 4 controles, 5 salidas y 2 mecanismos en ambos niveles. A0 distribuye cada flecha de A-0 entre A1–A6; ningún intercambio interno cambia el tipo ICOM de su flecha de frontera. Las salidas auxiliares `Notification` y preferencia visual son especializaciones de O2 y O1. El slot es estado interno de A2/A3/A4, no una entrada externa adicional.
