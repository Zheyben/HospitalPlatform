import Link from "next/link";

export function Brand({ href = "/" }: { href?: string }) {
  return <Link className="brand" href={href} aria-label="HOSPITALPLATFORM, ir al inicio">
    <span className="brand-mark" aria-hidden="true"><span /></span>
    <span className="brand-name">HOSPITAL<span>PLATFORM</span></span>
  </Link>;
}
