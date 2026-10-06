import { NextRequest, NextResponse } from "next/server";
import { authorizedRequest, sameOrigin } from "@/lib/backend";

export async function GET(request: NextRequest) {
  return authorizedRequest(request, "/appointments/me/summary");
}

export async function POST(request: NextRequest) {
  if (!sameOrigin(request)) return NextResponse.json({ errorCode: "FORBIDDEN" }, { status: 403 });
  return authorizedRequest(request, "/appointments", { method: "POST", body: await request.text() });
}
