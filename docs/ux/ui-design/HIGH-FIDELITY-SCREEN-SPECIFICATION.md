# ETAPA F.4.2 — Especificación de pantallas de alta fidelidad

**Proyecto:** HOSPITALPLATFORM, caso de estudio académico del Hospital de Huaycán.

**Estado del entregable:** `PROPUESTA UI` para preparar Figma. Ninguna pantalla de este documento está implementada como frontend.

## 1. Objetivo

Convertir las referencias `WF-01`–`WF-15` ya documentadas en instrucciones visuales revisables para 14 composiciones UI. El [Design System F.4.1](HOSPITALPLATFORM-DESIGN-SYSTEM.md) gobierna color, tipografía, espaciado y componentes. La [consolidación F.3.3](../wireframes/WIREFRAME-CONSOLIDATION-REPORT.md) y la [resolución F.3.4](../wireframes/WIREFRAME-GAP-RESOLUTION-REPORT.md) gobiernan la cobertura y los cortes de navegación. También se contrastaron los wireframes [públicos](../wireframes/PUBLIC-PORTAL-LOW-FIDELITY-WIREFRAMES.md), [ADMIN](../wireframes/ADMIN-PORTAL-LOW-FIDELITY-WIREFRAMES.md), [RECEPTIONIST](../wireframes/RECEPTIONIST-PORTAL-LOW-FIDELITY-WIREFRAMES.md), el [alcance PROFESSIONAL](../wireframes/PROFESSIONAL-SCOPE-WIREFRAME.md), los [flujos consolidados](../user-flows/USER-FLOW-CONSOLIDATION.md) y [D.4](../../agenda/consolidation/AGENDA-UML-CONSOLIDATION-REPORT.md).

**Convención de estado:** `IMPLEMENTADO` = operación backend en su alcance actual; `PARCIAL` = subconjunto o experiencia incompleta; `CONCEPTUAL` = objetivo sin API/permiso operativo; `FUTURO` = fuera del funcionamiento actual. `PROPUESTA UI` indica composición visual y nunca eleva uno de esos estados. Los rótulos de alcance van escritos, no codificados solo con color. La palabra «confirmación» en UI-05 nombra la **lectura visual del resultado**; la creación de una cita sigue devolviendo `SCHEDULED`, distinta de `CONFIRMED`.

**Lienzos de trabajo propuestos para Figma:** escritorio 1440 × 900 px y móvil 390 × 844 px; una variante de tableta 768 × 1024 px cuando una tabla o formulario lo requiera. Son tamaños de diseño, no requisitos del producto. Aplicar grid de 12/8/4 columnas, contenido máximo 1200 px, márgenes 24/16 px y escala 4–48 px de F.4.1. Heading 1 32/40, Heading 2 24/32, body 16/24 y label 14/20; reducir visualmente en móvil sin romper el orden semántico. Fondo `color.surface.canvas`, superficie `color.surface.card`, texto `color.text.primary`, énfasis `color.brand.primary` y bordes necesarios `color.border.strong`. Revalidar contraste en cada composición final.

## 2. Arquitectura de pantallas

| Área | Wireframe → composición UI | Corte de navegación |
|---|---|---|
| PUBLIC PATIENT | WF-01 → UI-01; WF-02 → UI-02; WF-03 → UI-03; WF-04 + WF-05 → UI-04; WF-06 → UI-05; WF-07 → UI-06. | Registro `FUTURO`; descubrimiento CU-D6 `CONCEPTUAL`. UI-04 solo puede enviar reserva con `slotId` ya conocido. |
| ADMIN | WF-02 → UI-07; WF-08 → UI-08; WF-09 → UI-09; WF-10 → UI-10; WF-11 → UI-11. | UI-08 es navegación propuesta, sin dashboard/API. |
| RECEPTIONIST | WF-02 → UI-12; WF-12 + WF-13 + WF-14 → UI-13. | WF-12 solo organiza la vista; CU-D6 del rol sigue sin pantalla navegable. |
| PROFESSIONAL | WF-15 → UI-14 como **referencia conceptual no navegable**. | No existe portal ni GET de citas para el rol. |

`WF-02` es un único login backend compartido, presentado en tres contextos visuales; UI-02/07/12 no son tres contratos de autenticación. Las referencias CU-D2/CU-D3 y la invariante CU-D8 no reciben pantalla propia. El mapa entre áreas es una propuesta de organización de prototipo, no un selector de portal implementado.

## 3. Especificación por pantalla

### UI-01 — Landing / Acceso público

- **Actor:** visitante orientado a PATIENT. **Estado funcional:** portal `FUTURO` (RF-010); composición `PROPUESTA UI`, no landing implementada.
- **Objetivo UX:** orientar hacia el login y señalar que el registro público todavía no está disponible.
- **Resolución visual:** escritorio y móvil definidos en §1. Usar Heading 1, body y una sola acción primaria; acento `color.brand.primary` sobre superficie clara. Sin fotografía, escudo o marca institucional oficial.
- **Layout:** `[HEADER]` rótulo provisional HOSPITALPLATFORM; `[MAIN CONTENT]` título y explicación breve del acceso a citas; `[SIDEBAR/NAVIGATION]` ninguna; `[FOOTER]` sin información hospitalaria no aprobada.
- **Componentes UI:** bloque informativo, botón primary «Ingresar» como enlace de prototipo a UI-02; etiqueta `FUTURO` y referencia textual de registro UI-03 sin CTA activo de alta.
- **Datos visibles:** nombre del sistema y propósito del prototipo, sin oferta médica, horarios, disponibilidad o métricas.
- **Acciones:** avanzar a UI-02 dentro del prototipo; la referencia de registro no crea cuenta.
- **Estados:** loading, empty, error y success de negocio no aplican. La página solo muestra estado de presentación inicial.
- **Validaciones:** ninguna de datos; no se simula acceso ni registro desde esta vista.
- **Dependencias:** RF-010 sin frontend; RF-001 y DEC-017 `OPEN`.
- **Trazabilidad:** UI-01 → WF-01 → CU `—` → RF-010 → `FUTURO`.

### UI-02 — Login PATIENT

- **Actor:** PATIENT con cuenta existente. **Estado funcional:** login backend `IMPLEMENTADO` (RF-002); pantalla `PROPUESTA UI`.
- **Objetivo UX:** autenticar la cuenta existente sin sugerir autorregistro activo.
- **Resolución visual:** formulario de una columna centrado, ancho moderado; Heading 1, labels persistentes y foco visible. Primary `color.brand.primary`, fondo blanco.
- **Layout:** `[HEADER]` acceso; `[MAIN CONTENT]` tarjeta de credenciales y área de respuesta; `[SIDEBAR/NAVIGATION]` ninguna; `[FOOTER]` sin recuperación de cuenta inventada.
- **Componentes UI:** inputs texto `email` y contraseña, botón primary «Ingresar», alerta Error y texto de ayuda.
- **Datos visibles:** `email`, `password` introducidos y resultado de autenticación; sin perfil clínico o datos de paciente inferidos.
- **Acciones:** enviar el login compartido. El destino PATIENT es organización UX posterior, no selector de portal.
- **Estados:** vacío inicial; loading/validando durante petición; error de entrada `400` o rechazo `401` según respuesta; success solo tras autenticación real. Empty de listado no aplica.
- **Validaciones:** ambos campos obligatorios y formato de email; no revelar un motivo de rechazo que la respuesta no proporcione.
- **Dependencias:** RF-002, cuenta habilitada; reserva posterior requiere perfil PATIENT activo vinculado y `slotId` conocido.
- **Trazabilidad:** UI-02 → WF-02 → CU `—` (Auth) → RF-002 → backend `IMPLEMENTADO`, UI no construida.

### UI-03 — Registro paciente

- **Actor:** visitante orientado a PATIENT. **Estado funcional:** autorregistro `FUTURO`; lámina `CONCEPTUAL` y no navegable como alta.
- **Objetivo UX:** hacer visible la intención de registro sin definir un formulario ejecutable.
- **Resolución visual:** marco claro con etiqueta `FUTURO` siempre visible; Heading 1 y bloques neutros. No aplicar estilo de formulario listo para envío.
- **Layout:** `[HEADER]` «Registro paciente — FUTURO»; `[MAIN CONTENT]` zonas documentales «identidad por definir» y «credenciales por definir»; `[SIDEBAR/NAVIGATION]` ninguna; `[FOOTER]` DEC-017 `OPEN`.
- **Componentes UI:** texto informativo y enlace secondary de retorno a UI-02; ningún input definitivo ni botón primary «Crear cuenta» accionable.
- **Datos visibles:** categorías pendientes, sin campos, DTO, consentimiento, regla de duplicado o estado de aprobación fijados.
- **Acciones:** volver a login en el prototipo. No hay envío de alta ni salto automático a una cuenta creada.
- **Estados:** loading, empty, error y success de autorregistro **no se diseñan**, porque no existe contrato; solo se ve el rótulo `FUTURO`.
- **Validaciones:** ninguna regla de alta se define aquí.
- **Dependencias:** RF-001, DEC-017 `OPEN`; cuenta válida tendría que existir por proceso separado para usar UI-02.
- **Trazabilidad:** UI-03 → WF-03 → CU `—` → RF-001 → `FUTURO` / representación `CONCEPTUAL`.

### UI-04 — Solicitud de cita y envío de reserva

- **Actor:** PATIENT autenticado; envío solo con perfil propio activo. **Estado funcional:** zona A `CONCEPTUAL` CU-D6, zona B backend `IMPLEMENTADO` CU-D7 con `slotId` conocido. La pantalla integral no existe.
- **Objetivo UX:** expresar la intención de descubrir una cita y la operación vigente de enviar una reserva sin conectar falsamente ambas.
- **Resolución visual:** dos paneles claramente separados por una franja textual «Consulta PATIENT no implementada». Zona A con etiqueta `CONCEPTUAL` y sin controles activos; zona B en tarjeta blanca con énfasis primary. En móvil se apilan manteniendo el corte.
- **Layout:** `[HEADER]` «Solicitar cita»; `[MAIN CONTENT]` panel A de necesidad conceptual y panel B de envío con dato conocido; `[SIDEBAR/NAVIGATION]` ninguna; `[FOOTER]` la oferta hipotética no garantiza reserva.
- **Componentes UI:** etiqueta `CONCEPTUAL`, alerta Info, input texto `slotId` obligatorio, input `reason` opcional y botón primary «Enviar reserva» condicionado a dato conocido. No usar calendario ni selector funcional de especialidad/profesional.
- **Datos visibles:** en A, solo anotaciones «especialidad/profesional/fecha por definir», sin resultados; en B, `slotId` y `reason`. PATIENT no introduce `patientId`.
- **Acciones:** ninguna búsqueda o selección ejecutable en A. En B, enviar una única petición de reserva vigente con `slotId` conocido y esperar resultado; CU-D8 actúa internamente, sin control propio.
- **Estados:** A no tiene loading, empty, error o success de resultados. B puede mostrar formulario vacío, loading «Enviando», error real `400`/`403`/`404`/`409 SLOT_UNAVAILABLE` o success `201` que conduce a UI-05. Ante `409` no mostrar cita creada.
- **Validaciones:** `slotId` obligatorio, `reason` opcional; el backend revalida slot usable y ownership. No se fijan filtros, fechas, ventanas o reglas de elegibilidad nuevas.
- **Dependencias:** DEC-007 aprueba solo alcance conceptual de CU-D6; no hay API/permiso PATIENT para descubrir slots. CU-D7/RF-013 y CU-D8/RF-014 permiten reserva con ID conocido; DEC-008/010 siguen abiertas.
- **Trazabilidad:** UI-04 A → WF-04 → CU-D6 → RF-012 → `CONCEPTUAL`; UI-04 B → WF-05 → CU-D7 (+ CU-D8 interno) → RF-013/014 → backend `IMPLEMENTADO`, selección UX pendiente.

### UI-05 — Resultado de reserva registrada

- **Actor:** PATIENT que envió una reserva propia. **Estado funcional:** respuesta de CU-D7 `IMPLEMENTADO`; constancia UX RF-015 `PARCIAL`; pantalla no construida.
- **Objetivo UX:** leer el resultado del envío sin confundirlo con confirmación de asistencia.
- **Resolución visual:** tarjeta de resultado con Heading 1 «Reserva registrada» solo tras `201`, alerta Success `#176143/#E9F6EE` y valor `SCHEDULED` en texto. Variante de error separada, sin comprobante.
- **Layout:** `[HEADER]` resultado de solicitud; `[MAIN CONTENT]` respuesta real y resumen de campos devueltos; `[SIDEBAR/NAVIGATION]` ninguna; `[FOOTER]` aclaración `SCHEDULED ≠ CONFIRMED`.
- **Componentes UI:** card, alerta Success o Error, etiqueta de estado de cita textual, enlace secondary a UI-06 si se obtuvo una cita propia.
- **Datos visibles:** `id`, `appointmentStatus=SCHEDULED` y solo otros campos de `AppointmentResponseDTO` efectivamente devueltos. No añadir fecha/hora del slot, nombre de especialidad o constancia visual final inferida.
- **Acciones:** abrir «Mis citas»; no confirmar asistencia automáticamente.
- **Estados:** loading mientras se espera respuesta; empty no aplica; error tras rechazo sin cita/comprobante; success únicamente tras `201` con `SCHEDULED`.
- **Validaciones:** no presentar «confirmada» como estado inicial. `CONFIRMED` requiere transición posterior desde detalle elegible.
- **Dependencias:** CU-D7/RF-013, RF-015 `PARCIAL`, RF-006 para consulta posterior.
- **Trazabilidad:** UI-05 → WF-06 → CU-D7 → RF-015 (origen RF-013) → respuesta backend existente / constancia UX `PARCIAL`.

### UI-06 — Mis citas / detalle

- **Actor:** PATIENT autenticado. **Estado funcional:** GET de citas propias RF-006 `IMPLEMENTADO`; UI no construida.
- **Objetivo UX:** consultar citas registradas propias, abrir detalle y reconocer el estado real.
- **Resolución visual:** título y lista de cards en móvil; tabla o lista de cards en escritorio. Texto primario y badge de `appointmentStatus` legible. Bloque «Próxima cita» solo como anotación de diseño sin dato calculado.
- **Layout:** `[HEADER]` «Mis citas»; `[MAIN CONTENT]` lista y detalle seleccionado; `[SIDEBAR/NAVIGATION]` ninguna necesaria; `[FOOTER]` aclaración de estado y ausencia de fecha/hora resuelta.
- **Componentes UI:** card/table, botón secondary «Abrir detalle» y botones de confirmar/cancelar/reprogramar solo si la cita y rol actuales son elegibles; Alert para respuesta real.
- **Datos visibles:** campos de `AppointmentResponseDTO` que lleguen: `id`, `patientId`, `professionalId`, `slotId`, `appointmentStatus`, `flowStage`, `reason` y fechas del registro devueltas. `createdAt` **no** es fecha de la cita. No calcular «próxima cita» porque el DTO no incluye fecha/hora del slot.
- **Acciones:** consultar lista/detalle propios; confirmar asistencia desde `SCHEDULED` si procede, cancelar o reprogramar según reglas vigentes. Reprogramar exige nuevo `slotId` conocido, sin buscador aquí.
- **Estados:** loading de GET; empty si la lista propia está vacía; error `401`/`403`/`404` o transición rechazada según respuesta; success tras operación real. Los estados de negocio visibles son los recibidos, no etiquetas creadas por UI.
- **Validaciones:** ownership propio y elegibilidad de cada transición; no mostrar controles activos cuando la operación no corresponde. Una cita creada en UI-04 aparece `SCHEDULED` tras `201`.
- **Dependencias:** RF-006 y RF-016/017/019; definición faltante de WF-07 completada en F.3.4. Sin detalle clínico ni datos de agenda derivados.
- **Trazabilidad:** UI-06 → WF-07 → CU `—` (Appointments fuera de CU-D1–D8) → RF-006 y acciones RF-016/017/019 → GET/operaciones backend `IMPLEMENTADO` en su límite.

### UI-07 — Login ADMIN

- **Actor:** ADMIN con cuenta existente. **Estado funcional:** login compartido backend `IMPLEMENTADO`; UI propuesta.
- **Objetivo UX:** autenticar antes de acceder a las áreas operativas permitidas.
- **Resolución visual:** misma plantilla, escala y colores de UI-02; cambiar solo el contexto textual «Acceso administrativo». No crear una marca ni ruta de login distinta.
- **Layout:** `[HEADER]` acceso; `[MAIN CONTENT]` tarjeta de credenciales; `[SIDEBAR/NAVIGATION]` ninguna; `[FOOTER]` sin funciones de cuenta adicionales.
- **Componentes UI:** inputs `email` y contraseña, primary «Ingresar», alerta Error y foco visible.
- **Datos visibles:** credenciales ingresadas y respuesta real de autenticación.
- **Acciones:** enviar login compartido; continuar a UI-08 solo como navegación candidata para rol ADMIN.
- **Estados:** vacío inicial, loading, error `400`/`401` y success de autenticación. Empty de datos no aplica.
- **Validaciones:** ambos campos obligatorios y email válido; recursos posteriores conservan autorización ADMIN.
- **Dependencias:** RF-002; no hay endpoint «login ADMIN» ni selector de portal.
- **Trazabilidad:** UI-07 → WF-02 → CU `—` → RF-002 → backend `IMPLEMENTADO`, UI no construida.

### UI-08 — Admin Home / navegación

- **Actor:** ADMIN autenticado. **Estado funcional:** organización UX `CONCEPTUAL / NO IMPLEMENTADA`; no dashboard/API.
- **Objetivo UX:** orientar hacia profesionales, horarios y disponibilidad operativa, como destinos independientes.
- **Resolución visual:** tres tarjetas de navegación de igual jerarquía sobre `color.surface.card`, Heading 1 y espacios 24–32 px. Sin KPI, gráfico o cifra.
- **Layout:** `[HEADER]` «Área operativa»; `[MAIN CONTENT]` tres destinos; `[SIDEBAR/NAVIGATION]` solo esos destinos si se usa una barra lateral; `[FOOTER]` sin datos institucionales.
- **Componentes UI:** Navigation, card y enlace secondary. Etiqueta «Navegación propuesta» visible en la lámina de revisión.
- **Datos visibles:** únicamente rótulos «Profesionales», «Horarios» y «Disponibilidad».
- **Acciones:** abrir UI-09, UI-10 o UI-11 en el prototipo; la propia home no ejecuta negocio.
- **Estados:** loading/empty/error/success de dashboard no aplican; rechazo de acceso se maneja al consultar cada recurso real.
- **Validaciones:** sesión/rol al entrar a cada área; esta composición no concede permisos.
- **Dependencias:** WF-08 y los tres destinos existentes en F.2.3. No se presupone secuencia profesionales → horarios → slots.
- **Trazabilidad:** UI-08 → WF-08 → CU `—` → RF `—` → navegación `CONCEPTUAL`.

### UI-09 — Professional Management

- **Actor:** ADMIN. **Estado funcional:** CU-D1 `PARCIAL`; alta, consulta y actualización básica backend existentes.
- **Objetivo UX:** encontrar profesionales y ejecutar las tres operaciones básicas sin sugerir gestión completa.
- **Resolución visual:** tabla en escritorio y registros apilados con labels en móvil; formulario de licencia en panel separado. Heading 1, primary para alta/guardar y secondary para detalle.
- **Layout:** `[HEADER]` «Profesionales»; `[MAIN CONTENT]` lista/detalle y panel de alta o edición; `[SIDEBAR/NAVIGATION]` ADMIN propuesta hacia UI-08/10/11; `[FOOTER]` ninguno de negocio.
- **Componentes UI:** table, inputs `licenseNumber` y `userId` **solo en alta**, card de detalle, alerts Error/Success. `active` como dato de solo lectura.
- **Datos visibles:** `id`, `userId`, `licenseNumber`, `active` devueltos. No mostrar asociación N:M ni especialidades.
- **Acciones:** crear profesional, consultar lista/detalle, actualizar **solo** `licenseNumber`. Sin DELETE, desactivación HTTP ni reasignación de cuenta.
- **Estados:** loading de lista/detalle; empty de lista; error de validación, `404 PROFESSIONAL_NOT_FOUND` o `409 DUPLICATE_PROFESSIONAL`; success tras DTO devuelto.
- **Validaciones:** licencia obligatoria, no vacía y máximo 100 caracteres; `userId` opcional en alta, sujeto a referencia válida. No inventar ciclo de vínculo User–Professional.
- **Dependencias:** CU-D1/RF-007; DEC-005 `OPEN`; CU-D2/CU-D3 `CONCEPTUAL`.
- **Trazabilidad:** UI-09 → WF-09 → CU-D1 → RF-007 → `PARCIAL`.

### UI-10 — Schedule Management

- **Actor:** ADMIN. **Estado funcional:** CU-D4 `PARCIAL`; gestión actual de Schedule sin generación de slots.
- **Objetivo UX:** leer y configurar horarios mediante campos y cambio `active` existentes.
- **Resolución visual:** tabla/lista y formulario lateral o apilado; labels persistentes y controles de hora separados. Usar primary para guardar y secondary para detalle; `active` muestra resultado real.
- **Layout:** `[HEADER]` «Horarios»; `[MAIN CONTENT]` lista/detalle, filtros por ID y formulario; `[SIDEBAR/NAVIGATION]` ADMIN propuesta; `[FOOTER]` aclaración «Guardar horario no genera slots».
- **Componentes UI:** table, inputs para `professionalId`, `specialtyId`, `dayOfWeek`, `startTime`, `endTime`, control de estado `active` sujeto al PATCH vigente, alerts. Sin selector funcional de Specialty Catalog.
- **Datos visibles:** `id`, `professionalId`, `specialtyId`, `dayOfWeek`, `startTime`, `endTime`, `active` del DTO.
- **Acciones:** crear, listar, abrir detalle, actualizar configuración y cambiar `active`; operaciones independientes. Sin DELETE o creación automática de disponibilidad.
- **Estados:** loading, empty de lista, error de validación/`404` y success según DTO; no estado visual de «slots generados».
- **Validaciones:** cinco datos de POST/PUT obligatorios, `dayOfWeek` 0–6 y `endTime > startTime`; profesional activo. FK de `specialtyId` prueba existencia, no actividad ni asociación con profesional.
- **Dependencias:** CU-D4/RF-011; IDs conocidos, CU-D2/D3 conceptuales; DEC-008/010 `OPEN`.
- **Trazabilidad:** UI-10 → WF-10 → CU-D4 → RF-011 → `PARCIAL`.

### UI-11 — Availability View ADMIN

- **Actor:** ADMIN. **Estado funcional:** consulta CU-D5 `IMPLEMENTADO` solo ADMIN; RF-012 global `PARCIAL` por CU-D6.
- **Objetivo UX:** leer slots existentes y su `usable` sin editarlos.
- **Resolución visual:** fila de filtros sobre tabla en escritorio; filtros apilados y registros etiquetados en móvil. Colores de estado solo como apoyo al texto real `status`/`usable`.
- **Layout:** `[HEADER]` «Disponibilidad operativa»; `[MAIN CONTENT]` filtros, lista y detalle; `[SIDEBAR/NAVIGATION]` ADMIN propuesta; `[FOOTER]` «usable no garantiza reserva posterior».
- **Componentes UI:** input ID `scheduleId`/`professionalId`, date input para filtro `slotDate`, selección de `status` existente, table, card de detalle y alertas. No botón de gestión de slots.
- **Datos visibles:** `id`, `scheduleId`, `slotDate`, `startTime`, `endTime`, `status`, `usable`; pueden aparecer `AVAILABLE`, `RESERVED` y `BLOCKED`.
- **Acciones:** consultar lista con filtros opcionales y detalle por ID. Sin filtro `specialtyId`, creación, bloqueo, liberación o reserva desde esta vista.
- **Estados:** loading, empty de coincidencias, error `401`/`403` o detalle ausente `404`, success de lectura. La lista vacía no es un estado de negocio nuevo.
- **Validaciones:** formato de parámetros y autorización ADMIN; `usable` es derivado y no control editable.
- **Dependencias:** CU-D5/RF-012 porción ADMIN; CU-D6 sigue `CONCEPTUAL` para PATIENT/RECEPTIONIST.
- **Trazabilidad:** UI-11 → WF-11 → CU-D5 → RF-012 porción ADMIN → `IMPLEMENTADO` solo ADMIN.

### UI-12 — Login RECEPTIONIST

- **Actor:** RECEPTIONIST con cuenta existente. **Estado funcional:** login compartido backend `IMPLEMENTADO`; UI propuesta.
- **Objetivo UX:** autenticar antes de acceder a citas permitidas.
- **Resolución visual:** reutilizar composición, colores, tipografía y espaciado de UI-02; contexto textual «Acceso recepción», sin nuevo branding.
- **Layout:** `[HEADER]` acceso; `[MAIN CONTENT]` tarjeta `email`/`password`; `[SIDEBAR/NAVIGATION]` ninguna; `[FOOTER]` sin módulos adicionales.
- **Componentes UI:** dos inputs, botón primary «Ingresar», alerta Error, foco visible.
- **Datos visibles:** credenciales ingresadas y resultado de autenticación, sin ficha de paciente.
- **Acciones:** enviar login compartido; continuar a UI-13 como navegación propuesta si la sesión corresponde al rol.
- **Estados:** vacío inicial, loading, error `400`/`401`, success de autenticación. Empty de citas no aplica en login.
- **Validaciones:** campos requeridos y email válido; autorización real se aplica a cada operación de Appointments.
- **Dependencias:** RF-002; no hay endpoint «login RECEPTIONIST» ni selector de portal.
- **Trazabilidad:** UI-12 → WF-02 → CU `—` → RF-002 → backend `IMPLEMENTADO`, UI no construida.

### UI-13 — Appointment Management RECEPTIONIST

- **Actor:** RECEPTIONIST. **Estado funcional:** lista/detalle y mutaciones de citas backend `IMPLEMENTADO` en sus límites; RF-030 global `PARCIAL`; navegación WF-12 solo `CONCEPTUAL`.
- **Objetivo UX:** consultar citas accesibles y gestionar alta/ciclo sin incorporar funciones clínicas.
- **Resolución visual:** título y navegación mínima «Citas», tabla de lista, detalle y panel de acciones; en móvil apilar lista/detalle/formulario. No dashboard, métricas o menú administrativo.
- **Layout:** `[HEADER]` «Gestión de citas»; `[MAIN CONTENT]` lista/detalle WF-13 y alta/ciclo WF-14 en secciones distinguibles; `[SIDEBAR/NAVIGATION]` WF-12 solo como organización candidata; `[FOOTER]` sin información clínica.
- **Componentes UI:** table/card de cita, inputs `patientId` y `slotId` conocidos para alta, `reason` opcional, botones de confirmar/cancelar/reprogramar/check-in/espera solo según estado y rol, alertas de respuesta. CU-D6 no aparece como buscador activo.
- **Datos visibles:** `AppointmentResponseDTO`: IDs, `appointmentStatus`, `flowStage`, `reason` y fechas devueltas. No nombre completo de paciente inferido ni datos clínicos. Tras alta `201`, cita `SCHEDULED`, `flowStage=null`.
- **Acciones:** GET lista/detalle; alta con `patientId` explícito y `slotId` conocido; confirmar/cancelar/reprogramar cuando proceda; check-in desde `CONFIRMED/null` y espera desde `CONFIRMED/CHECK_IN`. Reprogramación exige otro `slotId` conocido. Sin inicio/fin de atención profesional.
- **Estados:** loading, empty de lista, error `401`/`403`/`404` o conflicto/ transición rechazada, success solo tras respuesta real. Rechazo de reserva no muestra comprobante; creación no produce `CONFIRMED`.
- **Validaciones:** IDs requeridos en alta según rol; paciente activo, slot usable y transiciones revalidados por backend. No se simula búsqueda de pacientes o disponibilidad, ni se incorporan ventanas temporales.
- **Dependencias:** RF-030 porción recepción, CU-D7/RF-013 y CU-D8/RF-014 para alta; RF-016/017/019/026/027 para acciones. `patientId`/`slotId` deben conocerse por vía no resuelta en UX; CU-D6 del rol `CONCEPTUAL`.
- **Trazabilidad:** UI-13 navegación → WF-12 → CU/RF `—` → `CONCEPTUAL`; consulta → WF-13 → CU `—` → RF-030 → GET del rol `IMPLEMENTADO`, RF global `PARCIAL`; alta/ciclo → WF-14 → CU-D7/CU-D8 interno para alta y `—` para transiciones → RF-013/014/016/017/019/026/027 → backend `IMPLEMENTADO` en su límite.

### UI-14 — Professional Scope, referencia conceptual

- **Actor:** PROFESSIONAL. **Estado funcional:** referencia UX `CONCEPTUAL / FUTURO / NO IMPLEMENTADO`; **no es pantalla de portal**.
- **Objetivo UX:** preservar el límite del actor para revisión en Figma, sin diseñar trabajo clínico o navegación ejecutable.
- **Resolución visual:** una lámina de documentación con etiqueta `CONCEPTUAL` visible, tipografía body y superficie neutra. No usar el marco de una página operativa ni affordances de aplicación.
- **Layout:** `[HEADER]` «Alcance PROFESSIONAL»; `[MAIN CONTENT]` rótulos «rol existente», «consulta de citas asignadas pendiente», «sin portal»; `[SIDEBAR/NAVIGATION]` ninguna; `[FOOTER]` DEC-005 `OPEN`, RF-030 `PARCIAL`.
- **Componentes UI:** etiquetas de alcance e información; **sin** botones, cards de pacientes, tabla de citas o menú.
- **Datos visibles:** solo anotaciones documentales, no citas, pacientes, horarios, identificadores o resultados reales.
- **Acciones:** ninguna. El login backend compartido y los POST operativos de inicio/fin de cita asignada no producen un recorrido UX de portal.
- **Estados:** loading, empty, error y success de pantalla no aplican; no hay GET de citas autorizado para el rol.
- **Validaciones:** no se simula permiso, contrato, autorización de lectura o ciclo de vinculación User–Professional.
- **Dependencias:** RF-030 `PARCIAL`, DEC-005 `OPEN`; RF-028/029 `IMPLEMENTADO` solo como mutaciones backend sobre cita conocida/asignada. PROFESSIONAL no inicia CU-D1–CU-D8.
- **Trazabilidad:** UI-14 → WF-15 → CU `—` → RF-030 (`PARCIAL`) y RF-028/029 (backend `IMPLEMENTADO`) → referencia UX `CONCEPTUAL / NO IMPLEMENTADO`.

## 4. Reglas de composición y exclusiones para Figma

Cada composición debe conservar la etiqueta de estado funcional junto al nombre de la pantalla y registrar en el archivo de diseño el ID UI y WF. `IMPLEMENTADO` se anota «backend», `PARCIAL` delimita qué parte funciona, `CONCEPTUAL` no ofrece interacción ejecutable y `FUTURO` no simula resultados. Los estados visuales Loading, Empty, Error y Success solo se dibujan donde existe una operación que pueda producirlos. Todos usan componentes y tokens de F.4.1, etiquetas persistentes, foco visible y estados expresados también en texto.

No incluir backend, APIs nuevas, base de datos, lógica clínica, historia médica, diagnóstico, atención médica, reportes, métricas, notificaciones, generación automática de slots, calendario/ventanas temporales, CRUD de Specialty, asociación N:M operativa, autorregistro ejecutable, disponibilidad sanitizada ejecutable o portal profesional. Ningún prototipo visual aprueba permisos o decisiones de dominio.

## 5. Validación y gate

Se revisó la cobertura UI-01–UI-14 contra WF-01–WF-15 y D.4. Se ejecutaron `git status --short --branch` y `git diff --check`; al tratarse de un archivo nuevo, se revisaron también sus enlaces locales y espacios finales. No se ejecutó Maven ni se hizo commit, push o merge.

**Archivo creado:** `docs/ux/ui-design/HIGH-FIDELITY-SCREEN-SPECIFICATION.md`. **Archivos existentes modificados:** 0. **Java:** 0; **SQL:** 0; **tests:** 0; **migraciones:** 0; **seguridad:** 0.

**🟡 HIGH FIDELITY SPECIFICATION COMPLETE WITH OBSERVATIONS.** Las 14 composiciones tienen guía visual y trazabilidad para Figma. UI-01/03 y UI-14 siguen sin producto funcional; UI-04 no conecta descubrimiento conceptual con reserva; UI-06 no calcula «próxima cita» sin datos autorizados.
