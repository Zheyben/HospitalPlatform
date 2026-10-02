import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "HOSPITALPLATFORM | Pacientes",
  description: "Registro y reserva de citas para pacientes",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="es"><body>{children}</body></html>;
}
