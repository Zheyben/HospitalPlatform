import { NextRequest } from "next/server";
import { authorizedRequest } from "@/lib/backend";

export async function GET(request: NextRequest) {
  return authorizedRequest(request, "/availability");
}
