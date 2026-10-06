import { expect, test } from "@playwright/test";

test("registration uses the approved document types and dynamic validation", async ({ page }) => {
  await page.goto("/register");
  const type = page.getByLabel("Tipo de documento");
  const number = page.getByLabel("Número de documento");
  await expect(type.locator("option")).toHaveCount(3);
  await number.fill("1234567");
  expect(await number.evaluate((input: HTMLInputElement) => input.checkValidity())).toBe(false);
  await number.fill("12345678");
  expect(await number.evaluate((input: HTMLInputElement) => input.checkValidity())).toBe(true);
  await type.selectOption("CE");
  await number.fill("ABC123");
  expect(await number.evaluate((input: HTMLInputElement) => input.checkValidity())).toBe(false);
  await number.fill("AB123456");
  expect(await number.evaluate((input: HTMLInputElement) => input.checkValidity())).toBe(true);
  await type.selectOption("PASSPORT");
  await number.fill("ABC12");
  expect(await number.evaluate((input: HTMLInputElement) => input.checkValidity())).toBe(false);
  await number.fill("AB1234");
  expect(await number.evaluate((input: HTMLInputElement) => input.checkValidity())).toBe(true);
});

test("patient registration, login, availability, booking, conflict and appointments", async ({ page, context }) => {
  const browserPost = (slotId: string, reason: string) => page.evaluate(async ({ slotId, reason }) => {
    const response = await fetch("/api/patient/appointments", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ slotId, reason }) });
    return { status: response.status, body: await response.json() };
  }, { slotId, reason });
  const unique = `${Date.now()}${Math.floor(Math.random() * 1000)}`;
  const email = `b3patient${unique}@example.test`;
  const documentNumber = String(10000000 + Math.floor(Math.random() * 89999990));
  const password = "B3PatientPass123!";

  await page.goto("/patient/availability");
  await expect(page).toHaveURL(/\/login$/);

  async function fillRegistration(accountEmail: string, document: string) {
    await page.goto("/register");
    await page.getByLabel("Correo electrónico").fill(accountEmail);
    await page.getByLabel("Contraseña").fill(password);
    await page.getByLabel("Número de documento").fill(document);
    await page.getByLabel("Nombre", { exact: true }).fill("Valeria");
    await page.getByLabel("Apellido").fill("Prueba");
    await page.getByLabel("Fecha de nacimiento").fill("1993-06-15");
    await page.getByLabel("Teléfono").fill("+573001234567");
    await page.getByLabel("Seguro", { exact: true }).selectOption({ label: "SIS" });
  }

  async function chooseFirstAvailable() {
    await page.getByLabel("Especialidad").selectOption({ index: 1 });
    await page.getByLabel("Profesional").selectOption({ index: 1 });
    await page.getByLabel("Fecha", { exact: true }).selectOption({ index: 1 });
  }

  await fillRegistration(email, documentNumber);
  const registered = page.waitForResponse(response => response.url().endsWith("/api/session/register"));
  await page.getByRole("button", { name: "Crear cuenta" }).click();
  expect((await registered).status()).toBe(201);
  await expect(page.getByText("Cuenta creada correctamente. Inicia sesión para continuar.")).toBeVisible();

  await fillRegistration(email, String(Number(documentNumber) + 1));
  await page.getByRole("button", { name: "Crear cuenta" }).click();
  await expect(page.locator(".alert.error")).toContainText("Este correo ya está registrado.");

  await fillRegistration(`other${unique}@example.test`, documentNumber);
  await page.getByRole("button", { name: "Crear cuenta" }).click();
  await expect(page.locator(".alert.error")).toContainText("Este documento ya está registrado.");

  await fillRegistration(`invalid${unique}@example.test`, String(Number(documentNumber) + 2));
  await page.getByLabel("Teléfono").evaluate(element => element.removeAttribute("pattern"));
  await page.getByLabel("Teléfono").fill("invalid");
  const invalidResponse = page.waitForResponse(response => response.url().endsWith("/api/session/register"));
  await page.getByRole("button", { name: "Crear cuenta" }).click();
  expect((await invalidResponse).status()).toBe(400);
  await expect(page.locator(".alert.error")).toContainText("Revisa los datos del formulario");

  await page.goto("/login");
  await page.getByLabel("Correo electrónico").fill(email);
  await page.getByLabel("Contraseña").fill("wrongpassword");
  await page.getByRole("button", { name: "Iniciar sesión" }).click();
  await expect(page.locator(".alert.error")).toContainText("Correo o contraseña incorrectos.");

  await page.getByLabel("Contraseña").fill(password);
  const loginResponse = page.waitForResponse(response => response.url().endsWith("/api/session/login"));
  await page.getByRole("button", { name: "Iniciar sesión" }).click();
  const loggedIn = await loginResponse;
  expect(loggedIn.status()).toBe(200);
  expect(await loggedIn.json()).toEqual({ authenticated: true, destination: "/patient/availability" });
  await expect(page).toHaveURL(/\/patient\/availability$/);
  const sessionCookies = await context.cookies();
  expect(sessionCookies.find(cookie => cookie.name === "hp_access")?.httpOnly).toBe(true);
  expect(sessionCookies.find(cookie => cookie.name === "hp_refresh")?.httpOnly).toBe(true);

  const forgedPatient = await page.evaluate(async () => {
    const response = await fetch("/api/patient/appointments", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ slotId: "00000000-0000-0000-0000-000000000001", patientId: "00000000-0000-0000-0000-000000000002", reason: "No permitido" }) });
    return response.status;
  });
  expect(forgedPatient).toBe(403);

  await chooseFirstAvailable();
  await expect(page.getByRole("button", { name: "Seleccionar" }).first()).toBeVisible();
  await page.getByRole("button", { name: "Seleccionar" }).first().click();
  await expect(page.getByRole("dialog")).toBeVisible();
  await expect(page.getByRole("button", { name: "Cerrar", exact: true })).toBeFocused();
  await page.keyboard.press("Shift+Tab");
  await expect(page.getByRole("button", { name: "Volver" })).toBeFocused();
  await page.getByLabel("Motivo de consulta").fill("Consulta de demostración B3");
  const bookingResponse = page.waitForResponse(response => response.url().endsWith("/api/patient/appointments") && response.request().method() === "POST");
  await page.getByRole("button", { name: "Confirmar reserva" }).click();
  const booked = await bookingResponse;
  expect(booked.status()).toBe(201);
  const appointment = await booked.json();
  expect(appointment.appointmentStatus).toBe("SCHEDULED");
  await expect(page.getByText("Cita registrada correctamente")).toBeVisible();
  await expect(page.getByText("Estado:")).toContainText("Programada");

  await page.goto("/patient/appointments");
  await expect(page.getByText(appointment.id)).toBeVisible();
  await expect(page.getByText("Consulta de demostración B3")).toBeVisible();
  await expect(page.getByText("Programada")).toBeVisible();
  await expect(page.getByText("Profesional", { exact: true })).toBeVisible();
  await expect(page.getByText("Fecha y hora (Lima)")).toBeVisible();
  await page.screenshot({ path: "test-results/b31-my-appointments.png", fullPage: true });

  const secondAttempt = await browserPost(appointment.slotId, "Segundo intento");
  expect(secondAttempt.status).toBe(409);
  expect(secondAttempt.body.errorCode).toBe("SLOT_UNAVAILABLE");

  await page.goto("/patient/availability");
  await chooseFirstAvailable();
  await expect(page.getByRole("button", { name: "Seleccionar" }).first()).toBeVisible();
  const raceSlotId = await page.getByRole("button", { name: "Seleccionar" }).first().getAttribute("data-slot-id");
  expect(raceSlotId).toBeTruthy();
  await page.getByRole("button", { name: "Seleccionar" }).first().click();
  const reservedMeanwhile = await browserPost(raceSlotId!, "Reserva concurrente B3");
  expect(reservedMeanwhile.status).toBe(201);
  await page.getByLabel("Motivo de consulta").fill("Intento concurrente B3");
  await page.getByRole("button", { name: "Confirmar reserva" }).click();
  await expect(page.locator(".alert.error")).toContainText("Este horario ya no está disponible. Selecciona otro horario.");
  await expect(page.getByRole("dialog")).toHaveCount(0);
  await expect(page.getByRole("button", { name: "Seleccionar" }).first()).toBeVisible();

  await page.route("**/api/patient/availability", async route => {
    await new Promise(resolve => setTimeout(resolve, 700));
    await route.continue();
  });
  await page.reload();
  await expect(page.getByText("Cargando disponibilidad…")).toBeVisible();
  await chooseFirstAvailable();
  await expect(page.getByRole("button", { name: "Seleccionar" }).first()).toBeVisible();
  await page.unrouteAll();

  await page.route("**/api/patient/availability", route => route.fulfill({ status: 200, contentType: "application/json", body: "[]" }));
  await page.reload();
  await expect(page.getByText("No hay horarios disponibles")).toBeVisible();
  await page.unrouteAll();

  await page.route("**/api/patient/availability", route => route.abort());
  await page.reload();
  await expect(page.locator(".state-card.error-state")).toContainText("No se pudo conectar con el servidor.");
  await page.unrouteAll();

  await page.getByRole("button", { name: "Cerrar sesión" }).click();
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByLabel("Contraseña")).toHaveValue("");
  await page.goto("/patient/appointments");
  await expect(page).toHaveURL(/\/login$/);
});
