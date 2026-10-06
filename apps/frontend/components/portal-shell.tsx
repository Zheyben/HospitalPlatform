"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { apiRequest } from "@/lib/api-client";
import { Brand } from "@/components/brand";
import { LogoutButton } from "@/components/logout-button";

type Identity = { roles: string[] };
type Navigation = { href: string; label: string }[];

export function PortalShell({ role, title, navigation, children }: {
  role: string; title: string; navigation: Navigation; children: React.ReactNode;
}) {
  const [allowed, setAllowed] = useState<boolean | null>(null);
  useEffect(() => {
    let live = true;
    apiRequest<Identity>("/api/portal/users/me")
      .then(user => { if (live) setAllowed(user.roles.includes(role)); })
      .catch(() => { if (live) setAllowed(false); });
    return () => { live = false; };
  }, [role]);

  if (allowed === null) return <main className="portal-message" role="status">Comprobando acceso…</main>;
  if (!allowed) return <main className="portal-message" role="alert"><h1>Acceso no autorizado</h1><p>Tu cuenta no tiene permiso para este portal.</p><Link href="/login">Volver al inicio de sesión</Link></main>;
  return <div className="app-shell">
    <aside className="app-sidebar"><div className="sidebar-brand"><Brand href={navigation[0].href} /><span className="sidebar-caption">{title}</span></div>
      <nav className="portal-nav" aria-label="Navegación del portal">{navigation.map(item => <Link key={item.href} href={item.href}>{item.label}</Link>)}</nav>
      <LogoutButton />
    </aside>
    <div className="app-content"><header className="app-header"><strong>{title}</strong><span className="header-chip">Demo académica</span></header><main className="app-main" id="contenido">{children}</main></div>
  </div>;
}
