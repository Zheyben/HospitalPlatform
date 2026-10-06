const labels: Record<string, string> = {
  SCHEDULED: "Programada",
  CONFIRMED: "Confirmada",
  CANCELLED: "Cancelada",
  RESCHEDULED: "Reprogramada",
  COMPLETED: "Finalizada",
};

export function StatusBadge({ status }: { status: string }) {
  const known = Object.hasOwn(labels, status);
  return <span className={`status-badge ${known ? `status-${status.toLowerCase()}` : "status-unknown"}`}>{known ? labels[status] : status}</span>;
}
