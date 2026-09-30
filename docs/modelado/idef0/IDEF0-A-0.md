# IDEF0 A-0 — HospitalPlatform

## Objetivo

**Función A-0:** Gestionar citas médicas y trazabilidad de consulta externa programada.

## Alcance

Contexto del TO-BE académico con datos sintéticos, cuatro superficies y un dominio/backend. Esta es una especificación ICOM textual replicable. El `.mmd` ayuda a visualizar relaciones, pero Mermaid no garantiza la posición normalizada de flechas IDEF0 (entrada izquierda, control arriba, salida derecha, mecanismo abajo); la **tabla ICOM es normativa**.

## Fuente

`docs/auditoria/TO-BE.md`; SRS 0.1; RB-001–024; roadmap §§6, 9, 11–14, 18; ADR-001–008; APP-FLOW. La disponibilidad/slot es **estado interno derivado de agenda**, no dato externo aportado por paciente (RB-004). PostgreSQL es mecanismo técnico del diseño (ADR-002), sin atribución al hospital.

## Actores

Paciente, visitante, recepción, profesional titular, administración/revisor autorizado y sistema; como mecanismos humanos o técnicos, no como inputs.

## Actividades

A-0 agrega A1–A6 de `IDEF0-A0.md`, que abarcan T01–T29.

## Decisiones

D01–D13 se realizan en A1–A6; no son flechas ICOM independientes.

## Requisitos relacionados

RF-001–033; RNF-001–013 como restricciones o criterios de ensayo según su naturaleza.

## Reglas de negocio relacionadas

RB-001–024.

## Exclusiones

No representa atención clínica, integración institucional ni secuencia AS-IS. No se presume política pendiente como control ya aprobado.

## Pendientes de validación

Políticas de plazo, espera/ofertas, prioridad, contenido y roles finales. Mientras no se validen, C3 restringe el diseño a principios documentados y no impone valores.

## Archivo fuente del diagrama

`IDEF0-A-0.mmd`. El balance exacto con A0 está en `IDEF0-TRAZABILIDAD.md`.

## ICOM normativo de A-0

| ID | Tipo | Flecha | Base |
|---|---|---|---|
| I1 | Input | Solicitudes de cuenta/cita, datos mínimos declarados | RF-001/005/013; RB-001/006 |
| I2 | Input | Selección de especialidad/slot y acciones sobre cita | RF-012–019; RB-004–011 |
| I3 | Input | Solicitud/respuesta de espera, prioridad y consulta | RF-006/020–025/030–031; RB-012–015 |
| C1 | Control | SRS 0.1 y reglas de negocio canónicas | RF-001–033; RB-001–024 |
| C2 | Control | Permisos, propiedad y máquinas de estados aprobadas | RB-002/016–020; ADR-005/007 |
| C3 | Control | Políticas institucionales validadas cuando existan | RB-008–009/012–015/022/024 |
| C4 | Control | Seguridad, privacidad, contenido autorizado y accesibilidad | RNF-001/002/006; RB-001/021–023 |
| O1 | Output | Cuenta, sesión, perfil y vistas autorizadas | RF-001–006/030 |
| O2 | Output | Cita, constancia y estados actualizados | RF-013–019/026–029 |
| O3 | Output | WaitlistEntry, SlotOffer y decisión de prioridad | RF-020–022/024–025 |
| O4 | Output | Avance operativo, AuditLog e indicadores reproducibles | RF-026–032; RB-020/024 |
| O5 | Output | Contenido autorizado, catálogo y disponibilidad | RF-007–012; RB-003–004/022 |
| M1 | Mechanism | Paciente/visitante y personal autorizado | TO-BE §1/§3; RB-002 |
| M2 | Mechanism | HospitalPlatform: backend modular, PostgreSQL, web y app móvil | ADR-001/002; roadmap §9/§14 |

`Notification` y preferencias visuales son salidas auxiliares de O2/O1 respectivamente; no se interpretan como canales institucionales actuales.
