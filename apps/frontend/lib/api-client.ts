import type { ApiError } from "./types";

export class ApiRequestError extends Error {
  constructor(public status: number, public code: string, message: string) {
    super(message);
  }
}

export async function apiRequest<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(path, { ...init, cache: "no-store" });
  } catch {
    throw new ApiRequestError(0, "NETWORK_ERROR", "No se pudo conectar con el servidor. Inténtalo de nuevo.");
  }

  const body = await response.json().catch(() => ({})) as T & ApiError;
  if (!response.ok) {
    if (response.status === 401 && (path.startsWith("/api/patient/") || path.startsWith("/api/portal/"))) {
      window.location.assign("/login?expired=1");
    }
    throw new ApiRequestError(response.status, body.errorCode ?? "API_ERROR", body.message ?? "Ocurrió un error. Inténtalo de nuevo.");
  }
  return body;
}
