# Gantt — Hospital Platform

**Proyecto:** `hospital-platform`  
**Curso:** Curso Integrador I: Sistemas Software  
**Caso de estudio propuesto:** Hospital de Huaycán — MINSA / DIRIS Lima Este  
**Documento:** Cronograma Gantt académico  
**Horizonte:** 18 semanas  
**Base de planificación:** sílabo del curso + `docs/00-PROJECT-ROADMAP.md` + `docs/planning/wbs.md`  
**Estado:** versión inicial, sujeta a ajustes por indicaciones del docente y resultados del levantamiento

> Este cronograma organiza el trabajo por **semanas académicas** y no fija todavía fechas calendario.
> Las fechas reales podrán agregarse cuando el calendario oficial del curso quede confirmado.
>
> El porcentaje de avance indicado en este documento representa **avance interno del proyecto**, no ponderación de nota ni porcentaje oficial del curso.

---

# 1. Objetivo del cronograma

Convertir la WBS del proyecto en una secuencia temporal que permita:

- ordenar los trabajos de las cuatro unidades del sílabo;
- identificar dependencias;
- establecer hitos;
- evitar iniciar desarrollo antes de cerrar análisis y diseño;
- distribuir progresivamente backend, web y móvil;
- reservar tiempo suficiente para pruebas, despliegue y cierre;
- controlar el avance del equipo durante las 18 semanas.

---

# 2. Reglas de planificación

1. El roadmap y la WBS son la base del cronograma.
2. No se implementan reglas de negocio no validadas.
3. La documentación puede adelantarse, pero debe revisarse después del levantamiento.
4. Backend, web y móvil avanzan mediante vertical slices, no como proyectos completamente separados.
5. Seguridad y pruebas acompañan el desarrollo; no se dejan únicamente para el final.
6. La app móvil forma parte del MVP.
7. Las integraciones futuras no se incorporan al cronograma obligatorio del MVP.
8. Las semanas pueden reajustarse si el docente modifica entregables o si discovery revela cambios relevantes.

---

# 3. Distribución por unidades

| Unidad | Semanas | Enfoque principal |
| --- | --- | --- |
| **Unidad 1 — Planificación y análisis** | 1–4 | Investigación, Lean Canvas, PMBOK, Project Charter, WBS, Gantt, levantamiento y requerimientos |
| **Unidad 2 — Diseño** | 5–8 | BPMN, App Flow, UI/UX, arquitectura, modelo de datos, TRD y contrato API |
| **Unidad 3 — Desarrollo** | 9–12 | Foundation, backend, web, móvil y vertical slices funcionales |
| **Unidad 4 — Pruebas, despliegue y mantenimiento** | 13–18 | Integración, seguridad, E2E, performance, staging, monitoreo, documentación y cierre |

---

# 4. Cronograma maestro

| WBS | Actividad / paquete | Inicio | Fin | Dependencias | Entregable / resultado |
| --- | --- | ---: | ---: | --- | --- |
| 1.2.1 | Revisión bibliográfica y fuentes | S1 | S1 | — | Fuentes y marco teórico |
| 1.2.2 | Problema de investigación | S1 | S1 | 1.2.1 | Problema y objetivos |
| 1.1.2 | Lean Canvas | S1 | S2 | 1.2.1–1.2.2 | `lean-canvas.md` |
| 1.1.3 | Project Charter | S1 | S2 | 1.1.2 | `project-charter.md` |
| 1.1.4 | WBS | S2 | S2 | 1.1.3 | `wbs.md` |
| 1.1.5 | Gantt | S2 | S2 | 1.1.4 | `gantt.md` |
| 1.2.3 | Revisión institucional | S1 | S3 | 1.2.1 | Evidencia institucional |
| 1.2.4 | Preparar levantamiento | S2 | S3 | 1.2.3 | Instrumentos |
| 1.2.5 | Ejecutar levantamiento | S3 | S4 | 1.2.4 | Hallazgos AS-IS |
| 1.2.6 | Consolidar resultados | S4 | S4 | 1.2.5 | Línea base / hallazgos |
| 1.3.1 | Identificar stakeholders | S2 | S3 | 1.2.3 | Matriz de actores |
| 1.3.2 | Requisitos funcionales | S3 | S4 | 1.2.5 | RF preliminares |
| 1.3.3 | Requisitos no funcionales | S3 | S4 | 1.2.5 | RNF preliminares |
| 1.3.4 | Reglas de negocio | S3 | S4 | 1.2.5 | Reglas iniciales |
| 1.3.5 | Priorización MVP | S4 | S4 | 1.3.2–1.3.4 | Alcance priorizado |
| 1.3.6 | PRD | S4 | S5 | 1.2.6, 1.3.5 | `01-PRD.md` |
| 1.4.1 | BPMN AS-IS | S5 | S5 | 1.2.6 | Proceso actual validado |
| 1.4.2 | BPMN TO-BE | S5 | S6 | 1.4.1, 1.3.6 | Proceso objetivo |
| 1.4.3 | App Flow | S5 | S6 | 1.3.6, 1.4.2 | `03-APP-FLOW.md` |
| 1.4.4 | Diseño UI/UX | S6 | S7 | 1.4.3 | Flujos y componentes |
| 1.4.5 | Accesibilidad | S6 | S7 | 1.4.4 | Criterios WCAG y temas |
| 1.4.6 | Prototipos | S7 | S8 | 1.4.4–1.4.5 | Prototipos web/mobile |
| 1.5.1 | Arquitectura lógica | S5 | S6 | 1.3.6 | Módulos y límites |
| 1.5.3 | TRD | S6 | S7 | 1.5.1 | `02-TRD.md` |
| 1.5.4 | ADR iniciales | S6 | S8 | 1.5.1 | ADR críticos |
| 1.5.5 | Modelo de datos | S6 | S7 | 1.3.6, 1.5.1 | `05-BACKEND-SCHEMA.md` |
| 1.5.6 | Contrato OpenAPI | S7 | S8 | 1.5.5, 1.3.6 | `contracts/openapi.yaml` |
| 1.6.1–1.6.4 | Hardening de foundation | S8 | S9 | 1.5.3 | Foundation reproducible |
| 1.6.5 | CI inicial | S8 | S9 | 1.6.1–1.6.4 | Pipeline verde |
| 1.7.1 | Backend Identity | S9 | S9 | 1.6.5 | Auth/RBAC inicial |
| 1.7.2–1.7.4 | Institutional + Catalog + Practitioners | S9 | S10 | 1.7.1 | Catálogo y agenda |
| 1.7.5 | Appointments | S9 | S11 | 1.7.3–1.7.4 | Citas y concurrencia |
| 1.7.6 | Prioritization | S10 | S11 | 1.7.5 | Revisión auditable |
| 1.7.7 | Waitlist | S10 | S11 | 1.7.5 | Recuperación de cupos |
| 1.7.8 | Notifications | S10 | S12 | 1.7.5, 1.7.7 | In-app / push |
| 1.7.9 | Outpatient Flow | S11 | S12 | 1.7.5 | Flujo ambulatorio |
| 1.7.10 | Audit & Metrics | S11 | S12 | módulos backend | Auditoría/métricas |
| 1.8.1 | Foundation web | S9 | S9 | 1.5.6 | Proyecto web base |
| 1.8.2 | Portal público | S9 | S10 | 1.8.1, 1.7.2 | Web institucional |
| 1.8.3 | Portal de reservas | S10 | S12 | 1.7.5 | Reserva web |
| 1.8.4 | Web operativa | S10 | S12 | backend progresivo | Backoffice |
| 1.8.5 | Accesibilidad web | S9 | S12 | 1.8.1 | Accesibilidad integrada |
| 1.9.1 | Foundation móvil | S9 | S9 | 1.5.6 | Proyecto móvil base |
| 1.9.2 | Autenticación móvil | S9 | S10 | 1.7.1 | Login seguro |
| 1.9.3 | Gestión de citas móvil | S10 | S12 | 1.7.5 | Reserva y mis citas |
| 1.9.4 | Waitlist móvil | S11 | S12 | 1.7.7 | Ofertas de cupos |
| 1.9.5 | Notificaciones móvil | S11 | S12 | 1.7.8 | Push |
| 1.9.6 | Seguimiento móvil | S11 | S12 | 1.7.9 | Estado/check-in |
| 1.9.7 | Accesibilidad móvil | S9 | S12 | 1.9.1 | Accesibilidad integrada |
| 1.10.1–1.10.3 | Unit + integration + API tests | S9 | S13 | desarrollo progresivo | Suite automatizada |
| 1.10.4 | Concurrency tests | S11 | S13 | 1.7.5, 1.7.7 | Double booking / slot offers |
| 1.10.5 | Security tests | S12 | S14 | módulos críticos | Evidencia de seguridad |
| 1.10.6 | Web E2E | S12 | S14 | 1.8 | Flujos web |
| 1.10.7 | Mobile tests | S12 | S14 | 1.9 | Flujos mobile |
| 1.10.8 | Performance | S14 | S15 | MVP integrado | Línea base de rendimiento |
| 1.10.9 | Usabilidad y accesibilidad | S13 | S15 | web/mobile integrados | Evidencia UX/a11y |
| 1.11.1 | Staging | S14 | S15 | build estable | Entorno académico |
| 1.11.2 | CI/CD completo | S13 | S15 | 1.6.5 | Pipeline final |
| 1.11.3 | Observabilidad | S14 | S16 | staging | Logs/métricas/health |
| 1.11.4 | Hardening deployment | S15 | S16 | staging | HTTPS/secrets/backups |
| 1.12 | Documentación y evidencias | S1 | S18 | transversal | Docs y evidencias |
| 1.13.1 | Validación final | S16 | S17 | MVP + pruebas | Revisión de alcance |
| 1.13.2 | Entrega 100 % | S17 | S18 | 1.13.1 | Versión final |
| 1.13.3 | Plan de mantenimiento | S16 | S17 | 1.11 | Mantenimiento |
| 1.13.4 | Lecciones aprendidas | S18 | S18 | cierre | Retrospectiva |

---

# 5. Vista Gantt por semanas

Leyenda:

- `██` = trabajo planificado;
- `◆` = hito;
- `·` = sin actividad principal.

```text
ACTIVIDAD                                   01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18
-------------------------------------------------------------------------------------------------
SEMANA 01 / investigación                  ██ ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ··
Lean Canvas                                ██ ██ ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ··
Project Charter                            ██ ██ ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ··
WBS / Gantt                                ·· ██ ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ··
Revisión institucional                     ██ ██ ██ ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ··
Preparación levantamiento                  ·· ██ ██ ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ··
Levantamiento de campo                     ·· ·· ██ ██ ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ··
Requisitos / reglas                        ·· ·· ██ ██ ██ ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ··
PRD                                        ·· ·· ·· ██ ██ ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ··
BPMN AS-IS / TO-BE                         ·· ·· ·· ·· ██ ██ ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ··
App Flow                                   ·· ·· ·· ·· ██ ██ ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ··
UI/UX + accesibilidad                      ·· ·· ·· ·· ·· ██ ██ ██ ·· ·· ·· ·· ·· ·· ·· ·· ·· ··
Arquitectura / TRD                         ·· ·· ·· ·· ██ ██ ██ ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ··
Modelo datos + OpenAPI                     ·· ·· ·· ·· ·· ██ ██ ██ ·· ·· ·· ·· ·· ·· ·· ·· ·· ··
Foundation / CI                            ·· ·· ·· ·· ·· ·· ·· ██ ██ ·· ·· ·· ·· ·· ·· ·· ·· ··
Backend MVP                                ·· ·· ·· ·· ·· ·· ·· ·· ██ ██ ██ ██ ·· ·· ·· ·· ·· ··
Web MVP                                    ·· ·· ·· ·· ·· ·· ·· ·· ██ ██ ██ ██ ·· ·· ·· ·· ·· ··
Mobile MVP                                 ·· ·· ·· ·· ·· ·· ·· ·· ██ ██ ██ ██ ·· ·· ·· ·· ·· ··
Unit / Integration / API tests             ·· ·· ·· ·· ·· ·· ·· ·· ██ ██ ██ ██ ██ ·· ·· ·· ·· ··
Concurrency / Security / E2E               ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ██ ██ ██ ██ ·· ·· ·· ··
Performance / UX / Accessibility           ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ██ ██ ██ ·· ·· ··
Staging / CI-CD                            ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ██ ██ ██ ██ ·· ··
Observabilidad / hardening                 ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ██ ██ ██ ·· ··
Validación / mantenimiento                 ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ██ ██ ··
Entrega y cierre                           ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ·· ██ ██
Documentación / evidencia                  ██ ██ ██ ██ ██ ██ ██ ██ ██ ██ ██ ██ ██ ██ ██ ██ ██ ██
-------------------------------------------------------------------------------------------------
HITOS                                      ◆  ·· ·· ◆  ◆  ·· ·· ◆  ·· ·· ·· ◆  ·· ·· ◆  ·· ◆  ◆
```

---

# 6. Plan semanal

## Semana 1 — Investigación y reorientación

Objetivos:

- cerrar reorientación Vitarte → Huaycán;
- actualizar roadmap;
- actualizar README;
- actualizar problema de investigación;
- ampliar marco teórico y fuentes;
- iniciar Lean Canvas y Project Charter.

**Salida:** base documental coherente.

---

## Semana 2 — PMBOK y planificación

Objetivos:

- cerrar Lean Canvas;
- cerrar Project Charter;
- cerrar WBS;
- cerrar Gantt;
- identificar stakeholders;
- preparar instrumentos de levantamiento.

**Salida:** planificación formal de la Unidad 1.

---

## Semana 3 — Toma de requerimientos

Objetivos:

- continuar revisión de MAPRO y Cartera;
- ejecutar entrevistas/observación si se autoriza;
- identificar proceso actual;
- obtener reglas de negocio;
- identificar actores;
- levantar problemas e indicadores.

**Salida:** evidencia de discovery.

---

## Semana 4 — Consolidación y requisitos

Objetivos:

- consolidar AS-IS;
- definir línea base cuando exista evidencia;
- elaborar RF;
- elaborar RNF;
- definir reglas;
- priorizar MVP;
- iniciar PRD.

**Salida:** análisis de Unidad 1 cerrado.

**Hito H1:** Unidad 1 completada.

---

## Semana 5 — BPMN y PRD

Objetivos:

- cerrar PRD;
- BPMN AS-IS;
- iniciar BPMN TO-BE;
- iniciar App Flow;
- iniciar definición de arquitectura.

**Salida:** requisitos validados y proceso actual modelado.

---

## Semana 6 — Diseño funcional y técnico

Objetivos:

- cerrar TO-BE;
- cerrar App Flow;
- arquitectura lógica;
- iniciar TRD;
- iniciar modelo de datos;
- iniciar UI/UX.

---

## Semana 7 — UI/UX, datos y arquitectura

Objetivos:

- completar TRD;
- avanzar prototipos;
- cerrar modelo de datos;
- diseñar accesibilidad;
- definir ADR críticos;
- iniciar OpenAPI.

---

## Semana 8 — Cierre de diseño

Objetivos:

- validar prototipos;
- completar OpenAPI;
- revisar arquitectura;
- completar decisiones técnicas necesarias;
- preparar foundation para programación.

**Hito H2:** Unidad 2 completada.

---

## Semana 9 — Inicio de desarrollo

Objetivos:

- hardening de foundation;
- CI;
- Identity;
- Catalog;
- Institutional;
- iniciar Practitioners;
- foundation web;
- foundation móvil.

**Salida:** primera vertical slice técnica.

---

## Semana 10 — Agenda y citas

Objetivos:

- profesionales;
- schedules;
- availability;
- appointment core;
- portal público;
- autenticación móvil;
- inicio del portal de reservas.

---

## Semana 11 — Waitlist, prioridad y concurrencia

Objetivos:

- confirmation;
- cancellation;
- rescheduling;
- prioritization;
- waitlist;
- slot offers;
- concurrency tests iniciales;
- integración web/mobile.

---

## Semana 12 — Flujo ambulatorio y MVP integrado

Objetivos:

- notifications;
- outpatient flow;
- audit;
- metrics;
- web operativa;
- móvil completo del MVP;
- pruebas E2E iniciales.

**Hito H3:** Unidad 3 completada / MVP funcional integrado.

---

## Semana 13 — Integración y calidad

Objetivos:

- cerrar integration/API tests;
- concurrency;
- web E2E;
- mobile tests;
- security testing;
- preparación CI/CD final.

---

## Semana 14 — Seguridad y staging

Objetivos:

- pruebas negativas;
- accesibilidad;
- usabilidad;
- performance inicial;
- despliegue staging;
- observabilidad inicial.

---

## Semana 15 — Performance y hardening

Objetivos:

- carga/stress;
- optimización;
- revisión de dependencias;
- revisión de logs;
- completar UX/accessibility tests;
- fortalecer staging.

---

## Semana 16 — Validación integral

Objetivos:

- smoke tests;
- revisar criterios de aceptación;
- corregir defectos críticos;
- documentación de deployment;
- monitoreo;
- plan de mantenimiento.

---

## Semana 17 — Preentrega

Objetivos:

- validar alcance final;
- evidencia;
- documentación final;
- presentación;
- demo;
- revisión cruzada del equipo.

---

## Semana 18 — Cierre

Objetivos:

- entrega final;
- presentación/demostración;
- repositorio consolidado;
- lecciones aprendidas;
- cierre académico.

**Hito H4:** Proyecto académico cerrado.

---

# 7. Hitos

| Hito | Semana | Criterio |
| --- | ---: | --- |
| **M1 — Reorientación documental** | S1 | Roadmap, investigación y fuentes coherentes con Huaycán |
| **M2 — Planificación completa** | S2 | Lean Canvas + Charter + WBS + Gantt |
| **M3 — Discovery completado** | S4 | levantamiento y requisitos preliminares consolidados |
| **M4 — PRD aprobado** | S5 | alcance y requerimientos aceptados |
| **M5 — Diseño funcional aprobado** | S6 | BPMN + App Flow |
| **M6 — Diseño técnico aprobado** | S8 | UI/UX + TRD + modelo + OpenAPI |
| **M7 — Foundation lista** | S9 | backend/web/mobile + CI reproducibles |
| **M8 — MVP integrado** | S12 | backend + web + mobile conectados |
| **M9 — Hardening completado** | S15 | seguridad, performance y accesibilidad |
| **M10 — Staging validado** | S16 | despliegue y smoke tests |
| **M11 — Preentrega** | S17 | documentación y demo listas |
| **M12 — Cierre** | S18 | entrega final |

---

# 8. Dependencias críticas

```text
Investigación
    ↓
Levantamiento
    ↓
Requisitos
    ↓
PRD
    ↓
BPMN / App Flow
    ↓
UI/UX + Arquitectura
    ↓
Modelo de datos + OpenAPI
    ↓
Foundation
    ↓
Appointments
    ↓
Waitlist / Prioritization / Notifications
    ↓
Web + Mobile integrados
    ↓
Pruebas
    ↓
Staging
    ↓
Cierre
```

### Dependencias que no deben romperse

- no cerrar PRD antes de consolidar el levantamiento;
- no definir BPMN definitivo con hipótesis no verificadas;
- no cerrar OpenAPI antes de requerimientos/modelo;
- no implementar `appointments` sin modelo de agenda;
- no implementar `waitlist` sin política definida;
- no implementar prioridad sin criterios y rol autorizados;
- no cerrar MVP sin app móvil;
- no considerar staging listo sin pruebas críticas.

---

# 9. Ruta crítica candidata

La ruta crítica académica inicial es:

```text
Levantamiento
   ↓
Requisitos
   ↓
PRD
   ↓
BPMN / App Flow
   ↓
Modelo de datos / OpenAPI
   ↓
Appointments
   ↓
Waitlist + Notifications + Outpatient Flow
   ↓
Integración web/mobile
   ↓
Security + E2E + Concurrency
   ↓
Staging
   ↓
Entrega
```

Un retraso importante en cualquiera de estos bloques puede afectar directamente la fecha de cierre.

---

# 10. Avance interno sugerido

Este porcentaje es únicamente una métrica interna para seguimiento del equipo.

| Bloque | Peso sugerido |
| --- | ---: |
| Investigación y planificación | 15 % |
| Requerimientos y discovery | 15 % |
| Diseño funcional y UI/UX | 15 % |
| Arquitectura y diseño técnico | 10 % |
| Backend | 15 % |
| Web | 8 % |
| Mobile | 8 % |
| Pruebas y seguridad | 8 % |
| Deployment/observabilidad | 3 % |
| Documentación y cierre | 3 % |
| **Total** | **100 %** |

Regla:

> El porcentaje no debe incrementarse por cantidad de archivos creados, sino por entregables realmente revisados y aceptados.

---

# 11. Seguimiento semanal

Cada semana el equipo debería registrar:

```text
Semana:
Objetivos comprometidos:
Objetivos completados:
Bloqueos:
Riesgos nuevos:
Cambios de alcance:
PRs cerrados:
Pruebas ejecutadas:
Avance estimado:
Acciones para la siguiente semana:
```

Esto puede documentarse posteriormente en:

```text
docs/progress/
```

si el volumen de evidencias lo justifica.

---

# 12. Trabajo paralelo del equipo

El cronograma permite paralelizar trabajo sin romper dependencias.

Ejemplo durante desarrollo:

```text
                    ┌── Backend
Arquitectura lista ─┼── Web
                    ├── Mobile
                    └── Tests
```

Pero todos consumen:

```text
PRD
+ App Flow
+ UI/UX
+ OpenAPI
+ reglas de negocio
```

Por tanto, paralelizar no significa trabajar con contratos distintos o inventar comportamiento por separado.

---

# 13. Gestión de cambios del cronograma

Un cambio importante deberá revisarse cuando ocurra alguno de estos eventos:

- el docente modifica un entregable;
- Huaycán no es aprobado como caso;
- el levantamiento cambia significativamente el alcance;
- una capacidad deja de pertenecer al MVP;
- una dependencia técnica bloquea una fase;
- la app móvil requiere cambios importantes de estrategia;
- una integración externa pasa a ser obligatoria;
- un riesgo crítico afecta el calendario.

Procedimiento:

```text
Cambio detectado
      ↓
Evaluar impacto
      ↓
Actualizar roadmap / PRD si corresponde
      ↓
Actualizar WBS
      ↓
Actualizar Gantt
      ↓
Commit + PR
```

---

# 14. Documentos relacionados

```text
docs/00-PROJECT-ROADMAP.md
docs/planning/lean-canvas.md
docs/planning/project-charter.md
docs/planning/wbs.md
docs/planning/gantt.md
```

Próximo documento de planificación:

```text
docs/planning/requirements-elicitation.md
```

---

# 15. Estado

- [x] Horizonte de 18 semanas definido.
- [x] Unidades del sílabo incorporadas.
- [x] WBS convertida en actividades temporales.
- [x] Dependencias definidas.
- [x] Hitos definidos.
- [x] Ruta crítica candidata definida.
- [x] Desarrollo web y móvil incorporado al MVP.
- [x] Pruebas y seguridad distribuidas durante el desarrollo.
- [ ] Fechas calendario confirmadas.
- [ ] Responsables nominales asignados.
- [ ] Validación del equipo.
- [ ] Validación docente.
- [ ] Actualización posterior al levantamiento.
