import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { Brand } from "@/components/brand";
import { PatientNavigation } from "@/components/patient-navigation";

export default async function PatientLayout({ children }: { children: React.ReactNode }) {
  const cookieStore = await cookies();
  if (!cookieStore.has("hp_access") && !cookieStore.has("hp_refresh")) redirect("/login");
  return <div className="app-shell">
    <aside className="app-sidebar">
      <div className="sidebar-brand"><Brand href="/patient/availability" /><span className="sidebar-caption">Portal de pacientes</span></div>
      <PatientNavigation />
      <div className="sidebar-foot"><span className="sidebar-foot-mark" aria-hidden="true">+</span><p>Tu espacio para consultar disponibilidad y reservar una cita médica.</p></div>
    </aside>
    <div className="app-content">
      <header className="app-header"><div><span className="header-kicker">PORTAL DE PACIENTES</span><strong>Gestiona tus citas con claridad</strong></div><span className="header-chip"><span aria-hidden="true" /> Atención ambulatoria</span></header>
      <main className="app-main" id="contenido">{children}</main>
    </div>
  </div>;
}
