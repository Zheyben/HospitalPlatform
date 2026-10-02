# HOSPITALPLATFORM — Design System base del prototipo UI

**Etapa:** F.4.1. **Estado:** `PROPUESTA UI` para un caso de estudio académico; sin aprobación de identidad institucional del Hospital de Huaycán.

## 1. Objetivo del Design System

Establecer reglas visuales reutilizables para el futuro prototipo de HOSPITALPLATFORM. La consistencia de texto, color, espaciado y componentes debe ayudar a distinguir actores, acciones y resultados sin ampliar el alcance funcional. Este documento es una guía de diseño, no una interfaz construida ni una especificación de backend.

Las fuentes de alcance son [requisitos UX F.1](../UX-REQUIREMENTS-ANALYSIS.md), [flujos consolidados F.2.3](../user-flows/USER-FLOW-CONSOLIDATION.md), [auditoría de wireframes F.3.3](../wireframes/WIREFRAME-CONSOLIDATION-REPORT.md), [resolución de brechas F.3.4](../wireframes/WIREFRAME-GAP-RESOLUTION-REPORT.md) y [documento maestro](../../documentation-export/HOSPITALPLATFORM-COMPLETE-DOCUMENTATION.docx). Estas fuentes distinguen `IMPLEMENTADO`, `PARCIAL`, `CONCEPTUAL` y `FUTURO`. Aquí `IMPLEMENTADO` significa capacidad actual del backend, **no pantalla de frontend existente**.

## 2. Identidad visual

**Concepto `PROPUESTA UI`:** interfaz sobria de servicio, con jerarquía clara, superficies luminosas y énfasis moderado en azul petróleo. Debe facilitar tareas de cita y lectura de estados en contextos administrativos y de atención al público. La personalidad propuesta es clara, confiable y profesional; el lenguaje visual no debe sugerir diagnóstico, seguimiento clínico ni aprobación oficial del hospital.

El nombre «HOSPITALPLATFORM» puede aparecer como rótulo de prototipo. No se define escudo, sello, logotipo institucional, fotografía del hospital ni una promesa de marca oficial. Los iconos, si se usan después, acompañarán una etiqueta textual y no servirán como único medio para entender una acción o estado.

## 3. Paleta visual

Todos los valores son **tokens de `PROPUESTA UI`**, sujetos a verificación en las composiciones finales. El estado funcional del diseño se escribe siempre con palabras; ningún color por sí solo acredita que una capacidad exista.

| Token | Valor | Uso propuesto |
|---|---|---|
| `color.brand.primary` | `#1E5A78` | Acción primaria y encabezado de secciones. |
| `color.brand.deep` | `#123B52` | Énfasis de texto o navegación privada. |
| `color.brand.soft` | `#E9F3F7` | Fondo de información de navegación. |
| `color.accent.secondary` | `#2E6B63` | Acento secundario, sin competir con la acción primaria. |
| `color.surface.canvas` | `#F7FAFC` | Fondo general. |
| `color.surface.card` | `#FFFFFF` | Tarjetas, formularios y tablas. |
| `color.text.primary` | `#1B2B34` | Texto principal. |
| `color.text.secondary` | `#4B5B66` | Texto auxiliar con contraste suficiente. |
| `color.border.strong` | `#6B7D87` | Bordes necesarios para identificar campos o controles. |

| Estado visual | Texto | Fondo | Aplicación |
|---|---|---|---|
| Éxito de operación | `#176143` | `#E9F6EE` | Petición completada según respuesta real. Una reserva exitosa sigue siendo `SCHEDULED`. |
| Error | `#A3292E` | `#FDEBEC` | Validación, rechazo o error real. |
| Advertencia | `#7A4B00` | `#FFF4D6` | Límite, dependencia o información que requiere atención; no crea estado de cita. |
| Información | `#15547A` | `#E8F3FA` | Aclaración de proceso o dato contextual. |

**Etiquetas de alcance documental:** `IMPLEMENTADO` puede usar la combinación de éxito; `PARCIAL`, advertencia; `CONCEPTUAL`, texto `#5A3D86` sobre `#F3EEFA`; `FUTURO`, texto `#4B5B66` sobre `#E8EDF0`. La palabra se imprime dentro de cada etiqueta, preferiblemente con aclaración «backend» o «UX», y nunca se sustituye por un punto de color. Estas etiquetas **no son** `appointmentStatus`, `flowStage` ni estados de slot. Para citas se muestra el valor real del dominio, por ejemplo `SCHEDULED` o `CONFIRMED`, con texto explícito y sin inferir transiciones.

## 4. Tipografía

**Familia `PROPUESTA UI`:** pila del sistema `system-ui, "Segoe UI", Arial, sans-serif` para lectura estable sin depender de una fuente externa. Peso regular para contenido y semibold para títulos, rótulos y acciones. No usar solo mayúsculas en párrafos extensos.

| Nivel | Tamaño / interlineado propuesto | Uso |
|---|---|---|
| Heading 1 | 32 / 40 px, semibold | Título de la vista. |
| Heading 2 | 24 / 32 px, semibold | Sección principal. |
| Heading 3 | 20 / 28 px, semibold | Grupo de formulario o tabla. |
| Body | 16 / 24 px | Instrucciones, datos y resultados. |
| Label | 14 / 20 px, semibold | Nombre persistente de campo, columna o control. |
| Caption | 14 / 20 px | Ayuda, fecha disponible y aclaración secundaria. |

La jerarquía combina tamaño, peso y posición; no depende solo del color. En pantallas pequeñas los títulos pueden reducirse un nivel visual, manteniendo el orden semántico de encabezados.

## 5. Espaciado y layout

**Escala `PROPUESTA UI`:** unidad de 4 px; separaciones de 4, 8, 12, 16, 24, 32 y 48 px. Los controles relacionados se agrupan con 8–12 px; las secciones se separan con 24–32 px. El contenido principal puede limitarse a 1200 px de ancho y usar márgenes de 24 px en escritorio y 16 px en móvil.

**Grid propuesto:** 12 columnas en escritorio, 8 en tableta y 4 en móvil, con separación de 16 px. Formularios breves como login usan una sola columna; tablas administrativas ocupan el ancho disponible sin comprimir etiquetas importantes. En móvil, las tablas pueden presentar cada registro en filas apiladas con nombres de campo visibles; no se omiten estados ni identificadores necesarios. El orden de lectura sigue título → contexto → datos → acción → respuesta.

El layout es una regla de composición del prototipo, no una arquitectura de portales construida. La navegación por rol solo puede mostrar destinos respaldados por los documentos UX y debe conservar las barreras de las vistas conceptuales.

## 6. Componentes UI base

Los componentes son **patrones visuales**. Sus variantes no crean operaciones nuevas: una acción se ofrece únicamente cuando la fuente del wireframe y el contrato vigente la respaldan para el actor y estado correspondientes.

### Buttons

| Variante | Aspecto propuesto | Uso y límite |
|---|---|---|
| Primary | Fondo `color.brand.primary`, texto blanco. | Una acción principal disponible, como enviar login o reserva con `slotId` conocido. |
| Secondary | Fondo blanco, texto `color.brand.deep`, borde fuerte. | Navegar o abrir detalle existente. |
| Danger | Fondo rojo oscuro, texto blanco. | Cancelar una cita solo cuando el rol y estado actuales lo permiten; no equivale a DELETE de Professional. |
| Disabled | Aspecto atenuado y texto explicativo cercano. | Control temporalmente no disponible por datos incompletos o petición en curso. No hace parecer operativa una función `CONCEPTUAL`/`FUTURO`. |

El botón mantiene verbo específico («Ingresar», «Enviar reserva», «Abrir detalle») y muestra carga sin duplicar envíos. Una referencia conceptual se presenta como anotación de diseño, no como botón accionable disimulado.

### Inputs

| Tipo | Uso aprobado para el prototipo | Límite |
|---|---|---|
| Texto | `email`, `licenseNumber`, identificadores conocidos o `reason` según actor/contrato. | Etiqueta persistente, ayuda y error junto al campo. PATIENT no introduce `patientId` en su reserva. |
| Contraseña | Login compartido de cuenta existente. | Sin flujo de recuperación de cuenta inventado. |
| Selección | Valores de filtro o estado que ya existen, por ejemplo `status` en consulta ADMIN. | No crear selector funcional de Specialty Catalog ni oferta PATIENT/RECEPTIONIST. |
| Fecha | Filtro `slotDate` de disponibilidad ADMIN cuando se use esa vista. | No crear calendario de reserva PATIENT ni ventanas temporales DEC-010. |

Los campos de Schedule ADMIN conservan `professionalId`, `specialtyId`, `dayOfWeek`, `startTime` y `endTime` conforme a su DTO actual; que `specialtyId` exista por FK no acredita catálogo o asociación activa. Un campo de fecha genérico no define formato, zona horaria aplicada o reglas futuras.

### Cards

Las tarjetas de cita agrupan `id`, `appointmentStatus` y, si se devuelve, `flowStage`; el detalle puede mostrar otros datos presentes en `AppointmentResponseDTO`. Una tarjeta informativa puede resumir la respuesta real de reserva `201` como «Reserva registrada» y `SCHEDULED`. La tarjeta **no** afirma «cita confirmada» ni muestra fecha/hora resuelta o «próxima cita» calculada cuando el contrato actual no entrega esos datos. [WF-07](../wireframes/WIREFRAME-GAP-RESOLUTION-REPORT.md) conserva ese bloque como espacio de diseño sin dato actual.

### Tables

Cabecera textual, columnas alineadas, estados escritos y acciones por fila solo cuando procedan. Uso propuesto: profesionales y horarios `PARCIAL` para ADMIN, disponibilidad operativa `IMPLEMENTADO` solo ADMIN y citas accesibles a RECEPTIONIST. Una tabla vacía indica ausencia de resultados, no ausencia de permiso. La vista ADMIN de slots puede mostrar `AVAILABLE`, `RESERVED`, `BLOCKED` y `usable` recibidos; no se convierte en editor de slots.

### Alerts

`Success`, `Error` y `Warning` tienen icono opcional, título breve y mensaje textual. Se muestran tras una respuesta real o para aclarar un límite ya documentado. `Success` tras crear cita nombra el estado `SCHEDULED`; `Error` no crea un comprobante. `Warning` puede explicar que un `slotId` debe conocerse o que una zona del prototipo es conceptual, sin prometer una búsqueda disponible.

### Navigation

**Navbar público `PROPUESTA UI`:** nombre provisional del sistema, acceso al login y rutas documentales del paciente. Registro RF-001 aparece solo como referencia `FUTURO`; no se presenta como alta activa. La consulta WF-04 permanece `CONCEPTUAL` y no conduce por sí sola a un `slotId`.

**Navegación privada `PROPUESTA UI`:** después del login compartido, ADMIN ve únicamente destinos de profesionales, horarios y disponibilidad operativa; RECEPTIONIST, consulta y gestión de citas. Las áreas de entrada WF-08/WF-12 son organización UX `CONCEPTUAL`, sin dashboard ni API propia. PROFESSIONAL no recibe menú, dashboard o portal completo. Ninguna navegación añade permisos al backend.

## 7. Estados UX

| Estado de presentación | Tratamiento visual | Alcance |
|---|---|---|
| Loading | Indicador con texto «Cargando» o «Enviando»; preservar contexto de la vista. | Solo durante petición existente; evitar segundo envío de reserva. |
| Empty | Mensaje claro y sin datos simulados. | Lista propia de citas, tablas ADMIN o lista de RECEPTIONIST cuando la consulta autorizada devuelve vacío. |
| Error | Alerta textual con respuesta o causa identificable y acción de recuperación cuando exista. | Diferenciar validación, rechazo de autorización, no encontrado y conflicto; no fingir reserva tras `409`. |
| Success | Alerta o resultado con dato devuelto. | Tras operación confirmada por backend. `201` de reserva = `SCHEDULED`; `CONFIRMED` solo tras transición independiente. |

Los estados `CONCEPTUAL` y `FUTURO` son **clasificaciones de alcance**, no estados de carga o negocio. No se fabrican estados de cuenta, no-show, prioridad o aprobación. En vistas sin API, como disponibilidad sanitizada, no se muestran resultados, spinner de consulta ni «sin turnos» como si la búsqueda hubiera ocurrido.

## 8. Componentes por módulo

| Área | Composición permitida en el prototipo | Frontera |
|---|---|---|
| PUBLIC PATIENT | Login de cuenta existente; formulario de reserva con `slotId` conocido; tarjeta de resultado `SCHEDULED`; lista/detalle de citas propias. | Landing `FUTURO`, registro `FUTURO`, descubrimiento `CONCEPTUAL`, constancia RF-015 `PARCIAL`. No se calcula «próxima cita» sin fecha/hora de slot. |
| ADMIN | Tabla y formulario de Professional básico `PARCIAL`; tabla y formulario de Schedule `PARCIAL`; filtros y tabla de disponibilidad operativa `IMPLEMENTADO` solo ADMIN. | Sin CRUD Specialty, asociación N:M, desactivación HTTP de Professional, generación de slots o informes. |
| RECEPTIONIST | Lista/detalle de citas y acciones vigentes; reserva con `patientId` y `slotId` conocidos. | Sin buscador de pacientes o disponibilidad autorizada; CU-D6 permanece `CONCEPTUAL`. |
| PROFESSIONAL | Etiqueta documental de alcance del actor. | Sin vista de trabajo, agenda personal, dashboard, listado de pacientes o interfaz clínica; WF-15 `CONCEPTUAL / NO IMPLEMENTADO`. |

Los componentes de `PARCIAL` muestran solo el subconjunto respaldado. Los de `CONCEPTUAL`/`FUTURO` pueden figurar en una lámina anotada para revisión, sin controles activos ni estados simulados.

## 9. Accesibilidad

El objetivo del prototipo es que texto, controles y estados puedan leerse y operarse sin depender del color. Como referencia de diseño, [WCAG 2.2 explica un contraste mínimo de 4.5:1 para texto normal y 3:1 para texto grande](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum), y [3:1 para información visual necesaria de controles](https://www.w3.org/WAI/WCAG22/Understanding/non-text-contrast.html). Esto es una **meta de verificación**, no una declaración de conformidad del producto.

En los pares propuestos, blanco sobre `#1E5A78` da aproximadamente 7.53:1, texto principal sobre blanco 14.57:1, texto secundario sobre blanco 7.03:1 y borde fuerte sobre blanco 4.28:1; los pares de texto/fondo de éxito, error, advertencia e información superan 6:1. Las composiciones finales deberán volver a medir contraste, especialmente sobre imágenes, overlays, hover y foco.

Cada input tiene etiqueta visible, mensaje de error asociado y ayuda que no desaparece al escribir. La navegación por teclado sigue el orden visual, muestra foco visible y permite llegar a acciones y tablas sin depender de puntero. Estados y enlaces combinan texto, forma y color; los mensajes no usan solo un icono. El diseño responsive preserva tamaño legible, separación entre controles y lectura de encabezados de tabla.

## 10. Exclusiones

Este Design System no incluye backend, API, arquitectura, seguridad, persistencia, lógica de negocio, reglas temporales, permisos ni implementación frontend. Tampoco aprueba identidad visual institucional. No define formularios definitivos para autorregistro, filtros de disponibilidad sanitizada, catálogo Specialty, generación automática de slots, notificaciones, historia clínica, diagnóstico, atención médica, reportes o portal profesional.

## Validación documental

Se revisaron las cinco fuentes de alcance y el documento maestro; la paleta, tipografía y medidas están marcadas como `PROPUESTA UI`. Se ejecutaron `git status --short --branch` y `git diff --check`, además de una comprobación directa de enlaces y espacios finales de este archivo nuevo. No se ejecutó Maven ni se hizo commit, push o merge.

**Archivo creado:** `docs/ux/ui-design/HOSPITALPLATFORM-DESIGN-SYSTEM.md`. **Archivos existentes modificados:** 0. **Java:** 0; **SQL:** 0; **tests:** 0; **migraciones:** 0; **seguridad:** 0.

**🟡 DESIGN SYSTEM COMPLETE WITH OBSERVATIONS.** La base visual queda definida para diseñar pantallas de alta fidelidad como prototipo documental. Registro público, descubrimiento de disponibilidad y navegación profesional siguen fuera de la experiencia funcional actual.
