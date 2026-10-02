const labels: Record<string, string> = {
  SCHEDULED: "SCHEDULED",
  CONFIRMED: "CONFIRMED",
  CANCELLED: "CANCELLED",
  RESCHEDULED: "RESCHEDULED",
  COMPLETED: "COMPLETED",
};

export function StatusBadge({ status }: { status: string }) {
  const known = Object.hasOwn(labels, status);
  return <span className={`status-badge ${known ? `status-${status.toLowerCase()}` : "status-unknown"}`}>{known ? labels[status] : status}</span>;
}
