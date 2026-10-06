import { RoleLayout } from "@/components/role-layout";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return <RoleLayout role="ADMIN" title="Administración" navigation={[
    { href: "/admin/professionals", label: "Profesionales" },
    { href: "/admin/schedules", label: "Horarios" },
  ]}>{children}</RoleLayout>;
}
