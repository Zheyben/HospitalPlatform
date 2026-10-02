import { NextRequest, NextResponse } from "next/server";
import { backendFetch, backendUnavailable, forward, sameOrigin, setSession } from "@/lib/backend";

export async function POST(request: NextRequest) {
  if (!sameOrigin(request)) return NextResponse.json({ errorCode: "FORBIDDEN" }, { status: 403 });
  try {
    const result = await backendFetch("/auth/login", { method: "POST", body: await request.text() });
    if (!result.ok) return forward(result);
    const tokens = await result.json();
    const response = NextResponse.json({ authenticated: true });
    setSession(response, tokens);
    return response;
  } catch {
    return backendUnavailable();
  }
}
