import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { PortalShell } from "@/components/portal-shell";

export async function RoleLayout({ role, title, navigation, children }: {
  role: string; title: string; navigation: { href: string; label: string }[]; children: React.ReactNode;
}) {
  const store = await cookies();
  if (!store.has("hp_access") && !store.has("hp_refresh")) redirect("/login");
  return <PortalShell role={role} title={title} navigation={navigation}>{children}</PortalShell>;
}
