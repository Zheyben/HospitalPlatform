# Frontend de los portales

Next.js App Router, React y TypeScript. Consume la API real mediante rutas internas de Next.js. Los tokens JWT se guardan en cookies `HttpOnly`, `SameSite=Lax` y `Secure` en producción; el navegador no recibe los tokens en JSON. El inicio de sesión consulta `/users/me` en el servidor y envía al portal de Paciente, Administración, Recepción o Médico según el rol.

## Demo local

Seguir la [guía D2 de demo](DEMO-GUIDE.md). Incluye el arranque de PostgreSQL, backend con seed de desarrollo y frontend, más el recorrido PATIENT y las consultas de pgAdmin.

## Validación

Con PostgreSQL y backend reales activos, desde `apps/frontend`:

```powershell
npm run build
npm run test:e2e
```

El E2E usa Chrome y crea pacientes y citas reales en la base de desarrollo. Solo simula las respuestas vacías y los fallos de red para comprobar esos estados de la interfaz.

“Mis citas” utiliza `GET /api/v1/appointments/me/summary` para mostrar la fecha y hora civil de Lima, especialidad, profesional, estado y etapa de cada reserva propia.

Los portales de Administración, Recepción y Médico usan `/api/portal/...` como BFF con cookies HttpOnly. El backend decide los permisos y las transiciones; el BFF comprueba origen en mutaciones. Las páginas de cada rol consultan `/users/me` antes de mostrar datos.

Recepción usa búsqueda documental exacta, disponibilidad autorizada, consulta filtrada de citas y sala de espera. La consulta mínima no contiene motivo ni antecedentes. El catálogo clínico operativo aprobado todavía no está disponible: las búsquedas médicas ofrecen solo opciones activas del backend, sin datos inventados en el cliente.
