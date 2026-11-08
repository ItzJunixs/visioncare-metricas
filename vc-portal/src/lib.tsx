/* ---------- formato ---------- */
export function cop(n: number | string | null | undefined): string {
  const v = typeof n === "string" ? Number(n) : n ?? 0;
  return "$" + (Math.round(v as number) || 0).toLocaleString("es-CO");
}

export function initials(name: string): string {
  return name
    .split(/\s+/)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase() ?? "")
    .join("");
}

/* ---------- iconos (stroke, 24x24) ---------- */
const PATHS: Record<string, string> = {
  home: "M3 10.5 12 4l9 6.5V20a1 1 0 0 1-1 1h-5v-6H10v6H4a1 1 0 0 1-1-1z",
  users:
    "M16 20v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 10a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm13 10v-2a4 4 0 0 0-3-3.87M16 2.13A4 4 0 0 1 16 10",
  box: "m21 8-9-5-9 5 9 5 9-5Zm0 0v8l-9 5-9-5V8m9 5v8",
  clip: "M9 3h6a1 1 0 0 1 1 1v1H8V4a1 1 0 0 1 1-1Zm-2 2H6a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-1",
  cash: "M3 7h18v10H3V7Zm9 2.5a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5ZM5 9v6M19 9v6",
  search: "m21 21-4.3-4.3M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16Z",
  check: "m20 6-11 11-5-5",
  alert: "M12 9v4m0 4h.01M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0Z",
  plus: "M12 5v14M5 12h14",
  arrow: "M5 12h14M13 6l6 6-6 6",
  back: "M19 12H5M11 6l-6 6 6 6",
  sun: "M12 17a5 5 0 1 0 0-10 5 5 0 0 0 0 10Zm0-15v2m0 16v2M4.9 4.9l1.4 1.4m11.4 11.4 1.4 1.4M2 12h2m16 0h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4",
  moon: "M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8Z",
  logout: "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4m7 14 5-5-5-5m5 5H9",
};

export function Icon({ name, size = 16 }: { name: string; size?: number }) {
  return (
    <svg
      viewBox="0 0 24 24"
      width={size}
      height={size}
      fill="none"
      stroke="currentColor"
      strokeWidth={1.7}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d={PATHS[name] ?? ""} />
    </svg>
  );
}

/* ---------- chips de estado (copy en español) ---------- */
const CHIP_TONE: Record<string, string> = {
  QUOTE: "info",
  APPROVED: "info",
  IN_LAB: "warn",
  READY: "ok",
  DELIVERED: "neutral",
  CANCELLED: "danger",
  ACTIVE: "ok",
  INACTIVE: "neutral",
  PENDING: "danger",
  PARTIAL: "warn",
  PAID: "ok",
  OVERDUE: "danger",
};
const STATUS_LABEL: Record<string, string> = {
  QUOTE: "Cotización",
  APPROVED: "Aprobada",
  IN_LAB: "En laboratorio",
  READY: "Lista",
  DELIVERED: "Entregada",
  CANCELLED: "Cancelada",
  ACTIVE: "Activo",
  INACTIVE: "Inactivo",
  PENDING: "Pendiente",
  PARTIAL: "Pago parcial",
  PAID: "Pagada",
  OVERDUE: "Control vencido",
};

export function StatusChip({ value }: { value: string }) {
  return (
    <span className={"chip " + (CHIP_TONE[value] ?? "neutral")}>
      <span className="d" />
      {STATUS_LABEL[value] ?? value}
    </span>
  );
}

/** BR-08: secuencia de estados que una orden puede recorrer (CANCELLED es la excepción, BR-10). */
export const WORK_ORDER_STATUSES = ["QUOTE", "APPROVED", "IN_LAB", "READY", "DELIVERED"] as const;
export function statusLabel(v: string): string {
  return STATUS_LABEL[v] ?? v;
}

export function roleLabel(r: string): string {
  return { ADMIN: "Administrador", SELLER: "Vendedor", OPTOMETRIST: "Optómetra" }[r] ?? r;
}

export function methodLabel(m: string): string {
  return { CASH: "Efectivo", CARD: "Tarjeta", TRANSFER: "Transferencia" }[m] ?? m;
}
