# HOSPITALPLATFORM — guía de demo PATIENT (D2)

Esta guía usa la base PostgreSQL **local de desarrollo**. El registro y la reserva crean datos reales; el seed de desarrollo crea una especialidad, un profesional habilitado, horarios y slots futuros. No usar estos comandos con una base de producción.

## 1. Preparación

Requisitos: Docker Desktop, Node.js/npm, Chrome y PowerShell. Desde la raíz del repositorio, mantener un `.env` local con `DATABASE_PASSWORD` y `JWT_SECRET` (al menos 32 bytes). Para los puertos de esta guía, definir también `DATABASE_USERNAME=hospital_app` y `POSTGRES_HOST_PORT=55432`. No mostrar ni versionar los valores de los secretos.

En la primera terminal:

```powershell
# Ejecutar desde la raíz de este repositorio.
docker compose -p hospital-platform up -d postgres
docker compose -p hospital-platform ps postgres
```

Esperar a que PostgreSQL figure como `healthy`. Puerto local `127.0.0.1:55432`, puerto interno `5432`, base `hospital_platform`.

## 2. Backend y seed de desarrollo

Si otro contenedor ya usa el puerto local `18080`, detenerlo primero. En el entorno de validación C1-C el nombre fue `hospital-platform-c1c-api`:

```powershell
docker ps --filter "publish=18080" --format "{{.Names}}"
docker stop hospital-platform-c1c-api
```

Omitir `docker stop` cuando no exista ese contenedor. Luego, desde la raíz y en una terminal que permanecerá abierta:

```powershell
docker build -f apps/backend/Dockerfile -t hospital-platform-backend:d2-demo .
docker run --rm --name hospital-platform-d2-api --network hospital-platform_default `
  --env-file .env `
  -e DATABASE_URL=jdbc:postgresql://postgres:5432/hospital_platform `
  -e SPRING_PROFILE=dev -e SERVER_PORT=8080 -e DEMO_SEED_ENABLED=true `
  -p 127.0.0.1:18080:8080 hospital-platform-backend:d2-demo
```

La aplicación aplica las migraciones existentes y activa el seed idempotente solo con `SPRING_PROFILE=dev` y `DEMO_SEED_ENABLED=true`. Verificar el arranque en otra terminal:

```powershell
docker logs --tail 40 hospital-platform-d2-api
```

La salida debe incluir `Started HospitalPlatformApplication` y `Tomcat started on port 8080`. El endpoint `/api/v1/actuator/health` exige autenticación en la configuración actual y no sirve como comprobación pública. Puerto backend `127.0.0.1:18080`. Para presentaciones posteriores, volver a iniciar con el seed activado repone el horizonte de slots futuros sin duplicar los existentes.

## 3. Frontend

En una tercera terminal:

```powershell
Set-Location .\apps\frontend
npm ci
Copy-Item .env.example .env.local -Force
npm run build
npm run start -- -p 3000
```

El valor de `BACKEND_URL` en `.env.local` debe ser `http://127.0.0.1:18080/api/v1`. Es una variable del servidor Next.js. Puerto frontend `127.0.0.1:3000`. URL inicial: **http://127.0.0.1:3000/register**. Si hay un servidor frontend anterior en el puerto `3000`, detenerlo antes de `npm run start`.

## 4. Guion de presentación

1. Abrir `/register` a 1440 px. Crear un correo y DNI de 8 dígitos nuevos. Completar contraseña, nombres, apellidos, fecha de nacimiento pasada, teléfono y aseguradora. Mostrar el mensaje de cuenta creada. El registro no inicia sesión.
2. Abrir `/login` e ingresar con ese correo y contraseña. La redirección lleva a `/patient/availability`.
3. Seleccionar especialidad, profesional y fecha; elegir una hora disponible real. Elegir `Seleccionar`; en el resumen se muestran esos mismos datos recibidos de la API. Escribir un motivo breve.
4. Seleccionar `Confirmar reserva`. Mostrar “Cita registrada correctamente” y el estado **`SCHEDULED`**. Conservar la referencia de cita visible.
5. Abrir `Ver mis citas`. Mostrar la misma referencia, el motivo, la fecha y hora del turno, el profesional, la especialidad y la etapa legible obtenidos de `GET /appointments/me/summary`.
6. En pgAdmin, ejecutar las consultas de la sección siguiente para mostrar la relación `users → patients → appointments → availability_slots`, el horario real y el estado `RESERVED` del slot.

Para repetir la demo, registrar otro correo y documento nuevos y elegir otro slot disponible. El E2E también crea pacientes y reservas reales en esta base local.

## 5. pgAdmin: comprobación de persistencia

Crear una conexión con host `127.0.0.1`, puerto `55432`, base `hospital_platform`, usuario `hospital_app` y la contraseña de `DATABASE_PASSWORD` del `.env` local. Sustituir el correo de ejemplo por el correo que se usó en la presentación.

```sql
SELECT u.id AS user_id, u.email, u.enabled,
       p.id AS patient_id, p.document_type, p.document_number, p.insurance
FROM users u
JOIN patients p ON p.user_id = u.id
WHERE u.email = 'correo.usado.en.la.demo@example.test';
```

```sql
SELECT a.id AS appointment_id, a.appointment_status, a.reason,
       a.patient_id, a.professional_id, a.slot_id,
       sl.slot_date, sl.start_time, sl.end_time, sl.status AS slot_status,
       sp.name AS specialty,
       concat_ws(' ', pu.first_name, pu.last_name) AS professional
FROM appointments a
JOIN patients p ON p.id = a.patient_id
JOIN users u ON u.id = p.user_id
JOIN availability_slots sl ON sl.id = a.slot_id
JOIN schedules sc ON sc.id = sl.schedule_id
JOIN specialties sp ON sp.id = sc.specialty_id
JOIN professionals pr ON pr.id = a.professional_id
LEFT JOIN users pu ON pu.id = pr.user_id
WHERE u.email = 'correo.usado.en.la.demo@example.test'
ORDER BY a.created_at DESC
LIMIT 5;
```

La segunda consulta debe mostrar `appointment_status = SCHEDULED`, `slot_status = RESERVED` y el `slot_id` que enlaza cita y slot. Las tablas del árbol de pgAdmin son `users`, `patients`, `appointments`, `availability_slots`, `schedules`, `professionals` y `specialties`.

## 6. Comprobación automatizada

Con PostgreSQL y backend reales levantados, desde `apps/frontend`:

```powershell
npm run build
npm run test:e2e
```

Playwright usa Chrome instalado en la ruta habitual de Windows o `PLAYWRIGHT_CHROME_PATH`. El flujo principal del E2E usa backend y PostgreSQL reales. Solo los estados vacíos y de error de red se simulan dentro de la prueba para comprobar su presentación.

## Alcance de esta guía

Esta guía D2 cubre el recorrido del paciente. Los portales de Administración, Recepción y Médico requieren cuentas sintéticas de esos roles; la demostración clínica final requiere además catálogos clínicos aprobados y cargados. Todavía no existe una guía de arranque en frío verificada para el recorrido integral.
