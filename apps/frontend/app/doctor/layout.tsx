import { RoleLayout } from "@/components/role-layout";

export default function DoctorLayout({ children }: { children: React.ReactNode }) {
  return <RoleLayout role="PROFESSIONAL" title="Portal médico" navigation={[
    { href: "/doctor", label: "Mis atenciones" },
  ]}>{children}</RoleLayout>;
}
