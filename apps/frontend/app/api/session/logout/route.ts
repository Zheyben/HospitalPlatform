import { NextRequest, NextResponse } from "next/server";
import { authorizedRequest, clearSession, sameOrigin } from "@/lib/backend";

export async function POST(request: NextRequest) {
  if (!sameOrigin(request)) return NextResponse.json({ errorCode: "FORBIDDEN" }, { status: 403 });
  await authorizedRequest(request, "/auth/logout", { method: "POST" });
  const response = NextResponse.json({ authenticated: false });
  clearSession(response);
  return response;
}
