import { NextRequest, NextResponse } from "next/server";

const apiBase = (process.env.BACKEND_URL ?? "http://127.0.0.1:18080/api/v1").replace(/\/$/, "");
const accessCookie = "hp_access";
const refreshCookie = "hp_refresh";

type TokenPair = { accessToken: string; refreshToken: string; expiresIn: number };

export function sameOrigin(request: NextRequest): boolean {
  const origin = request.headers.get("origin");
  if (!origin) return true;
  try {
    return new URL(origin).host === request.headers.get("host");
  } catch {
    return false;
  }
}

export function setSession(response: NextResponse, tokens: TokenPair) {
  const base = { httpOnly: true, sameSite: "lax" as const, secure: process.env.NODE_ENV === "production", path: "/" };
  response.cookies.set(accessCookie, tokens.accessToken, { ...base, maxAge: Math.max(1, tokens.expiresIn) });
  response.cookies.set(refreshCookie, tokens.refreshToken, base);
}

export function clearSession(response: NextResponse) {
  response.cookies.delete(accessCookie);
  response.cookies.delete(refreshCookie);
}

export async function backendFetch(path: string, init?: RequestInit): Promise<Response> {
  return fetch(`${apiBase}${path}`, { ...init, cache: "no-store", headers: { "Content-Type": "application/json", ...init?.headers } });
}

export function backendUnavailable(): NextResponse {
  return NextResponse.json({ errorCode: "API_UNAVAILABLE", message: "El servicio no está disponible. Inténtalo de nuevo." }, { status: 503 });
}

export async function forward(response: Response): Promise<NextResponse> {
  const text = await response.text();
  return new NextResponse(text || null, { status: response.status, headers: text ? { "Content-Type": response.headers.get("content-type") ?? "application/json" } : undefined });
}

export async function authorizedRequest(request: NextRequest, path: string, init?: RequestInit): Promise<NextResponse> {
  let access = request.cookies.get(accessCookie)?.value;
  const refresh = request.cookies.get(refreshCookie)?.value;
  if (!access && !refresh) {
    return NextResponse.json({ errorCode: "UNAUTHENTICATED", message: "Inicia sesión para continuar." }, { status: 401 });
  }

  const send = (token: string) => backendFetch(path, { ...init, headers: { ...init?.headers, Authorization: `Bearer ${token}` } });
  try {
    let result = access ? await send(access) : null;
    let tokens: TokenPair | null = null;
    if ((!result || result.status === 401) && refresh) {
      const renewal = await backendFetch("/auth/refresh", { method: "POST", body: JSON.stringify({ refreshToken: refresh }) });
      if (renewal.ok) {
        tokens = await renewal.json() as TokenPair;
        access = tokens.accessToken;
        result = await send(access);
      }
    }
    if (!result || result.status === 401) {
      const expired = NextResponse.json({ errorCode: "SESSION_EXPIRED", message: "Tu sesión ha terminado. Inicia sesión de nuevo." }, { status: 401 });
      clearSession(expired);
      return expired;
    }
    const forwarded = await forward(result);
    if (tokens) setSession(forwarded, tokens);
    return forwarded;
  } catch {
    return backendUnavailable();
  }
}
