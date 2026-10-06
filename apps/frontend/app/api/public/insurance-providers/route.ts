import { backendFetch, backendUnavailable, forward } from "@/lib/backend";

export async function GET() {
  try {
    return forward(await backendFetch("/insurance-providers"));
  } catch {
    return backendUnavailable();
  }
}
