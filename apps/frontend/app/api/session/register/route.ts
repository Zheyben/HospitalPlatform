import { NextRequest, NextResponse } from "next/server";
import { backendFetch, backendUnavailable, forward, sameOrigin } from "@/lib/backend";

export async function POST(request: NextRequest) {
  if (!sameOrigin(request)) return NextResponse.json({ errorCode: "FORBIDDEN" }, { status: 403 });
  try {
    const body = await request.text();
    return forward(await backendFetch("/auth/register", { method: "POST", body }));
  } catch {
    return backendUnavailable();
  }
}
