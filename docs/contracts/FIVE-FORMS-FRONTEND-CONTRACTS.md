# Contratos de los cinco formularios y portales clínicos

Revisión local: 2026-10-06, desde `main` en `26235a6` (PR #76). Este documento registra el contrato que consume el frontend; no declara cerrado el recorrido integral.

| Formulario / portal | API consumida | Rol y criterio principal |
| --- | --- | --- |
| F01 Registro | `GET /insurance-providers`, `POST /auth/register`, `POST /auth/login`, `GET /users/me` | Público; nueve campos antiguos compatibles, demografía adicional opcional; seguro activo por ID. |
| F02 Reserva y Mis citas | `GET /availability`, `POST /appointments`, `GET /appointments/me/summary` | PATIENT; selección dependiente y capacidad decidida por PostgreSQL. |
| F03 Profesionales | `GET /specialties`, `GET/POST/PUT/PATCH /professionals` | ADMIN; una especialidad activa por profesional. |
| F04 Horarios | `GET/POST/PUT/PATCH /agendas`, `POST /agendas/{id}/publish`, `GET /availability` | ADMIN; 0=domingo a 6=sábado, publicación explícita. |
| F05 Cita presencial | `GET /patients/search`, `GET /reception/availability`, `POST /appointments`, `GET /appointments/reception`, `POST /appointments/{id}/confirm/check-in/waiting`, `GET /appointments/reception/waiting-room` | RECEPTIONIST; búsqueda exacta; consulta mínima sin motivo ni clínica; sin cancelar ni reprogramar. |
| Médico | `GET /medical/me/context`, `GET /medical/appointments/{id}/context/history`, `GET /medical/appointments/{id}/history/{priorId}`, `POST /medical/appointments/{id}/start`, `GET/PUT /medical/encounters/{id}/draft/{section}`, `GET /medical/catalogs/*`, `POST /medical/encounters/{id}/finalize` | PROFESSIONAL asignado; fecha civil Lima, etapa, borradores versionados y cierre único en servidor. |
| Paciente clínico | `GET /medical/me/encounters`, `GET /medical/me/encounters/{id}`, `GET /medical/me/prescriptions`, `GET /medical/me/prescriptions/{id}` | PATIENT propietario; solo atenciones finalizadas y recetas emitidas. |

El BFF guarda JWT en cookies HttpOnly y reenvía respuestas y errores del backend. Las mutaciones comprueban el origen; el backend conserva el control de roles, propiedad, fechas, capacidad y transición. La UI distingue carga, vacío y error; 401 redirige al inicio de sesión y 409 conserva cambios locales en el borrador clínico para resolver el conflicto.

## Pendiente para validación de cierre

- Archivos clínicos aprobados con emisor, versión, licencia y checksum. El usuario indicó que aún no dispone de ellos; no se sustituyen por valores inventados.
- Pruebas PostgreSQL/Testcontainers y Playwright de ADMIN, RECEPTIONIST, PROFESSIONAL y lectura clínica PATIENT, más `mvn clean verify` y CI. El `typecheck` frontend y la compilación webpack son controles locales parciales.
- Revisión de accesibilidad y recorrido desde una base limpia con el rol runtime restringido; guía de arranque en frío.
