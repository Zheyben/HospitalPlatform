"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LogoutButton } from "@/components/logout-button";

const destinations = [
  { href: "/patient/availability", label: "Inicio / Reservar cita", icon: "calendar" },
  { href: "/patient/appointments", label: "Mis citas", icon: "list" },
  { href: "/patient/records", label: "Mis atenciones", icon: "list" },
  { href: "/patient/prescriptions", label: "Mis recetas", icon: "list" },
] as const;

function NavIcon({ name }: { name: "calendar" | "list" }) {
  return name === "calendar"
    ? <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><rect x="3" y="5" width="18" height="16" rx="2" /><path d="M7 3v4m10-4v4M3 10h18m-13 5h3" /></svg>
    : <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><rect x="4" y="4" width="16" height="16" rx="2" /><path d="M8 9h8M8 13h8M8 17h5" /></svg>;
}

export function PatientNavigation() {
  const pathname = usePathname();
  return <nav aria-label="Navegación de pacientes" className="patient-nav">
    <div className="nav-group-label">MI ESPACIO</div>
    {destinations.map(item => <Link key={item.href} href={item.href} className={`nav-link${pathname === item.href ? " active" : ""}`} aria-current={pathname === item.href ? "page" : undefined}>
      <NavIcon name={item.icon} /><span>{item.label}</span>
    </Link>)}
    <div className="nav-logout"><LogoutButton /></div>
  </nav>;
}
