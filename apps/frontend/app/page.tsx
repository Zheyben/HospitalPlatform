import { cookies } from "next/headers";
import { redirect } from "next/navigation";

export default async function Home() {
  const cookieStore = await cookies();
  redirect(cookieStore.has("hp_access") || cookieStore.has("hp_refresh") ? "/patient/availability" : "/login");
}
