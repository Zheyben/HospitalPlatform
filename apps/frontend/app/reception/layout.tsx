import { RoleLayout } from "@/components/role-layout";

export default function ReceptionLayout({ children }: { children: React.ReactNode }) {
  return <RoleLayout role="RECEPTIONIST" title="Recepción" navigation={[
    { href: "/reception/appointments", label: "Gestión de citas" },
  ]}>{children}</RoleLayout>;
}
