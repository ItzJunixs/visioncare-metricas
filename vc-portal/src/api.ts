/* Cliente HTTP y tipos de la API para VisionCare (vc-api). */

export type Role = "ADMIN" | "SELLER" | "OPTOMETRIST";

export interface Session {
  token: string;
  username: string;
  name: string;
  role: Role;
}

export interface OpticalFormula {
  id: string;
  odSph: number;
  odCyl: number;
  odAxis: number;
  oiSph: number;
  oiCyl: number;
  oiAxis: number;
  add: number | null;
  pupillaryDistance: number | null;
  isCurrent: boolean;
  formulaDate: string;
}

export interface PeriodicControl {
  nextControlDate: string;
  status: "PENDING" | "OVERDUE";
}

export interface Patient {
  id: string;
  documentType: string;
  documentNumber: string;
  firstName: string;
  lastName: string;
  fullName: string;
  phone: string;
  email: string | null;
  eps: string;
  status: string;
}

export interface PatientDetail extends Patient {
  currentFormula: OpticalFormula | null;
  periodicControl: PeriodicControl | null;
}

export interface Frame {
  id: string;
  sku: string;
  brand: string;
  model: string;
  buyPrice: number;
  sellPrice: number;
  stockQuantity: number;
  minStock: number;
  belowMinStock: boolean;
}

export interface WorkOrder {
  id: string;
  orderCode: string;
  patientId: string;
  frameId: string;
  status: string;
  createdAt: string;
  actualDelivery: string | null;
}

export interface StatusHistoryEntry {
  previousStatus: string;
  newStatus: string;
  changedBy: string;
  reason: string | null;
  changedAt: string;
}

export interface Payment {
  id: string;
  amount: number;
  method: string;
  paidAt: string;
}

export interface Invoice {
  id: string;
  invoiceNumber: string;
  workOrderId: string;
  subtotal: number;
  discount: number;
  tax: number;
  total: number;
  status: "PENDING" | "PARTIAL" | "PAID";
  balance: number;
  payments: Payment[];
}

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

const SESSION_KEY = "visioncare.session";

export function saveSession(s: Session | null) {
  if (s) localStorage.setItem(SESSION_KEY, JSON.stringify(s));
  else localStorage.removeItem(SESSION_KEY);
}

export function readSession(): Session | null {
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? (JSON.parse(raw) as Session) : null;
  } catch {
    return null;
  }
}

/** Turns an API error body ({ error, message, details? }) into text the user can read. */
export function errorMessage(status: number, body: { message?: string; details?: { field: string; message: string }[] }): string {
  if (status === 403) return "No tienes permiso para realizar esta acción.";
  if (body.details && body.details.length > 0) {
    return body.details.map((d) => d.field + ": " + d.message).join(" · ");
  }
  return body.message ?? "Error " + status;
}

export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const session = readSession();
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(init.headers as Record<string, string>),
  };
  if (session) headers.Authorization = "Bearer " + session.token;

  const res = await fetch("/api" + path, { ...init, headers });

  if (res.status === 401 && !path.startsWith("/auth/")) {
    saveSession(null);
    window.location.assign("/login");
    throw new ApiError(401, "Sesión expirada");
  }
  if (res.status === 204) return undefined as T;

  const body = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new ApiError(res.status, errorMessage(res.status, body));
  }
  return body as T;
}

export const api = {
  login: (username: string, password: string) =>
    request<Session & { expiresInMinutes: number }>("/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    }),

  patients: (q?: string) => request<Patient[]>("/patients" + (q ? "?q=" + encodeURIComponent(q) : "")),
  patient: (id: string) => request<PatientDetail>("/patients/" + id),
  createPatient: (body: unknown) => request<Patient>("/patients", { method: "POST", body: JSON.stringify(body) }),
  updatePatient: (id: string, body: unknown) =>
    request<Patient>("/patients/" + id, { method: "PUT", body: JSON.stringify(body) }),
  deactivatePatient: (id: string) => request<void>("/patients/" + id, { method: "DELETE" }),
  formulas: (patientId: string) => request<OpticalFormula[]>("/patients/" + patientId + "/optical-formulas"),
  updateFormula: (patientId: string, formulaId: string, body: unknown) =>
    request<OpticalFormula>("/patients/" + patientId + "/optical-formulas/" + formulaId, {
      method: "PUT",
      body: JSON.stringify(body),
    }),
  deleteFormula: (patientId: string, formulaId: string) =>
    request<void>("/patients/" + patientId + "/optical-formulas/" + formulaId, { method: "DELETE" }),
  registerFormula: (patientId: string, body: unknown) =>
    request<OpticalFormula>("/patients/" + patientId + "/optical-formulas", {
      method: "POST",
      body: JSON.stringify(body),
    }),

  frames: (lowStock?: boolean) => request<Frame[]>("/frames" + (lowStock ? "?lowStock=true" : "")),
  createFrame: (body: unknown) => request<Frame>("/frames", { method: "POST", body: JSON.stringify(body) }),
  frame: (id: string) => request<Frame>("/frames/" + id),
  updateFrame: (id: string, body: unknown) =>
    request<Frame>("/frames/" + id, { method: "PUT", body: JSON.stringify(body) }),
  deleteFrame: (id: string) => request<void>("/frames/" + id, { method: "DELETE" }),
  setMinStock: (id: string, minStock: number) =>
    request<Frame>("/frames/" + id + "/min-stock", { method: "PATCH", body: JSON.stringify({ minStock }) }),

  workOrders: (status?: string) => request<WorkOrder[]>("/work-orders" + (status ? "?status=" + status : "")),
  workOrder: (id: string) => request<WorkOrder>("/work-orders/" + id),
  workOrderHistory: (id: string) => request<StatusHistoryEntry[]>("/work-orders/" + id + "/status-history"),
  createWorkOrder: (patientId: string, frameId: string) =>
    request<WorkOrder>("/work-orders", { method: "POST", body: JSON.stringify({ patientId, frameId }) }),
  approveWorkOrder: (id: string) => request<WorkOrder>("/work-orders/" + id + "/approve", { method: "POST" }),
  advanceWorkOrder: (id: string, status: string, reason?: string) =>
    request<WorkOrder>("/work-orders/" + id + "/status", { method: "PATCH", body: JSON.stringify({ status, reason }) }),

  invoices: (status?: string) => request<Invoice[]>("/invoices" + (status ? "?status=" + status : "")),
  invoice: (id: string) => request<Invoice>("/invoices/" + id),
  registerPayment: (invoiceId: string, amount: number, method: string) =>
    request<Invoice>("/invoices/" + invoiceId + "/payments", {
      method: "POST",
      body: JSON.stringify({ amount, method }),
    }),
};
