# Frontend del paciente

Next.js App Router, React y TypeScript. Consume la API real mediante rutas internas de Next.js. Los tokens JWT se guardan en cookies `HttpOnly`, `SameSite=Lax` y `Secure` en producción; el navegador no recibe los tokens en JSON.

## Demo local

Seguir la [guía D2 de demo](DEMO-GUIDE.md). Incluye el arranque de PostgreSQL, backend con seed de desarrollo y frontend, más el recorrido PATIENT y las consultas de pgAdmin.

## Validación

Con PostgreSQL y backend reales activos, desde `apps/frontend`:

```powershell
npm run build
npm run test:e2e
```

El E2E usa Chrome y crea pacientes y citas reales en la base de desarrollo. Solo simula las respuestas vacías y los fallos de red para comprobar esos estados de la interfaz.

`GET /api/v1/appointments` no devuelve fecha/hora del slot ni nombres de profesional o especialidad. “Mis citas” muestra solo los datos recibidos: estado, motivo, fecha de registro del servidor e identificadores.
