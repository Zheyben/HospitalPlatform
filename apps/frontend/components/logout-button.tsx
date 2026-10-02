"use client";

import { useState } from "react";

export function LogoutButton() {
  const [pending, setPending] = useState(false);
  return <button className="nav-button" type="button" disabled={pending} onClick={async () => {
    setPending(true);
    await fetch("/api/session/logout", { method: "POST" }).catch(() => null);
    window.location.replace("/login");
  }}><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M9 4H6a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h3M16 16l4-4-4-4m4 4H9" /></svg><span>{pending ? "Saliendo…" : "Cerrar sesión"}</span></button>;
}
