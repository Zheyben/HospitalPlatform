import { NextRequest, NextResponse } from "next/server";
import { backendFetch, backendUnavailable, forward, sameOrigin, setSession } from "@/lib/backend";

export async function POST(request: NextRequest) {
  if (!sameOrigin(request)) return NextResponse.json({ errorCode: "FORBIDDEN" }, { status: 403 });
  try {
    const result = await backendFetch("/auth/login", { method: "POST", body: await request.text() });
    if (!result.ok) return forward(result);
    const tokens = await result.json();
    const identity = await backendFetch("/users/me", { headers: { Authorization: `Bearer ${tokens.accessToken}` } });
    if (!identity.ok) return backendUnavailable();
    const user = await identity.json() as { roles: string[] };
    const destination = user.roles.includes("ADMIN") ? "/admin/professionals"
      : user.roles.includes("RECEPTIONIST") ? "/reception/appointments"
      : user.roles.includes("PROFESSIONAL") ? "/doctor" : "/patient/availability";
    const response = NextResponse.json({ authenticated: true, destination });
    setSession(response, tokens);
    return response;
  } catch {
    return backendUnavailable();
  }
}
