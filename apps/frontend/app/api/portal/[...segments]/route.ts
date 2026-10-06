import { NextRequest, NextResponse } from "next/server";
import { authorizedRequest, sameOrigin } from "@/lib/backend";

type Context = { params: Promise<{ segments: string[] }> };
const roots = new Set(["users", "specialties", "professionals", "agendas", "availability", "patients", "reception", "appointments", "medical", "catalogs"]);

async function relay(request: NextRequest, context: Context, method: "GET" | "POST" | "PUT" | "PATCH") {
  const { segments } = await context.params;
  if (!segments.length || !roots.has(segments[0]) || segments.some(segment => !/^[A-Za-z0-9-]+$/.test(segment))) {
    return NextResponse.json({ errorCode: "INVALID_PATH" }, { status: 404 });
  }
  if (method !== "GET" && !sameOrigin(request)) {
    return NextResponse.json({ errorCode: "FORBIDDEN" }, { status: 403 });
  }
  const path = `/${segments.join("/")}${request.nextUrl.search}`;
  return authorizedRequest(request, path, { method, body: method === "GET" ? undefined : await request.text() });
}

export const GET = (request: NextRequest, context: Context) => relay(request, context, "GET");
export const POST = (request: NextRequest, context: Context) => relay(request, context, "POST");
export const PUT = (request: NextRequest, context: Context) => relay(request, context, "PUT");
export const PATCH = (request: NextRequest, context: Context) => relay(request, context, "PATCH");
