import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ApiError, api } from "../api";
import { useAsync } from "../useApi";
import { useToast } from "../toast";
import { Page, Topbar } from "../components/Layout";
import { Icon, StatusChip, statusLabel, WORK_ORDER_STATUSES } from "../lib";

export function WorkOrderDetail() {
  const { id = "" } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const { data: order, loading, error, reload } = useAsync(() => api.workOrder(id), [id]);
  const { data: history } = useAsync(() => api.workOrderHistory(id), [id]);
  const [cancelReason, setCancelReason] = useState("");
  const [showCancel, setShowCancel] = useState(false);
  const [busy, setBusy] = useState(false);

  const currentIndex = order
    ? WORK_ORDER_STATUSES.indexOf(order.status as (typeof WORK_ORDER_STATUSES)[number])
    : -1;
  const nextStatus = currentIndex >= 0 && currentIndex < WORK_ORDER_STATUSES.length - 1
    ? WORK_ORDER_STATUSES[currentIndex + 1]
    : null;

  async function approve() {
    setBusy(true);
    try {
      await api.approveWorkOrder(id);
      toast("Orden aprobada — stock descontado (BR-09) y factura generada automáticamente.");
      reload();
    } catch (e) {
      toast(e instanceof ApiError ? e.message : "No se pudo aprobar la orden", "err");
    } finally {
      setBusy(false);
    }
  }

  async function advance() {
    if (!nextStatus) return;
    setBusy(true);
    try {
      await api.advanceWorkOrder(id, nextStatus);
      toast(`Orden avanzada a ${statusLabel(nextStatus)}.`);
      reload();
    } catch (e) {
      toast(e instanceof ApiError ? e.message : "No se pudo avanzar la orden", "err");
    } finally {
      setBusy(false);
    }
  }

  async function cancel() {
    if (!cancelReason.trim()) {
      toast("Indica un motivo para cancelar (BR-10).", "err");
      return;
    }
    setBusy(true);
    try {
      await api.advanceWorkOrder(id, "CANCELLED", cancelReason.trim());
      toast("Orden cancelada.");
      setShowCancel(false);
      reload();
    } catch (e) {
      toast(e instanceof ApiError ? e.message : "No se pudo cancelar la orden", "err");
    } finally {
      setBusy(false);
    }
  }

  const canCancel = order && order.status !== "DELIVERED" && order.status !== "CANCELLED";

  return (
    <>
      <Topbar title="Detalle de la orden" subtitle="HU-07, HU-09 · Estado, historial y facturación" />
      <Page>
        <button className="btn subtle sm" onClick={() => navigate("/work-orders")} style={{ marginBottom: 16 }}>
          <Icon name="back" />
          Volver a órdenes
        </button>

        {loading && <p className="muted">Cargando…</p>}
        {error && <div className="alert err">{error}</div>}

        {order && (
          <div className="split">
            <div className="stack">
              <div className="card card-pad">
                <div className="row-between" style={{ marginBottom: 14 }}>
                  <div>
                    <h2 className="mono" style={{ fontSize: 18 }}>{order.orderCode}</h2>
                    <div className="card-sub">Creada el {order.createdAt.slice(0, 10)}</div>
                  </div>
                  <StatusChip value={order.status} />
                </div>

                <div className="row-between" style={{ gap: 10, flexWrap: "wrap" }}>
                  {order.status === "QUOTE" && (
                    <button className="btn primary sm" onClick={approve} disabled={busy}>
                      <Icon name="check" />
                      Aprobar orden
                    </button>
                  )}
                  {nextStatus && order.status !== "QUOTE" && (
                    <button className="btn primary sm" onClick={advance} disabled={busy}>
                      <Icon name="arrow" />
                      Avanzar a {statusLabel(nextStatus)}
                    </button>
                  )}
                  {order.status === "APPROVED" && (
                    <button className="btn subtle sm" onClick={() => navigate("/invoices")}>
                      <Icon name="arrow" />
                      Ver factura
                    </button>
                  )}
                  {canCancel && (
                    <button className="btn ghost sm" onClick={() => setShowCancel((s) => !s)} disabled={busy}>
                      Cancelar orden
                    </button>
                  )}
                </div>

                {showCancel && (
                  <div style={{ marginTop: 14, display: "flex", gap: 8 }}>
                    <input
                      placeholder="Motivo de cancelación (obligatorio — BR-10)"
                      value={cancelReason}
                      onChange={(e) => setCancelReason(e.target.value)}
                      style={{ flex: 1 }}
                    />
                    <button className="btn primary sm" onClick={cancel} disabled={busy}>Confirmar</button>
                  </div>
                )}
              </div>

              <div className="card card-pad">
                <h2 style={{ marginBottom: 12 }}>Historial de estados (BR-11)</h2>
                <div className="timeline">
                  {(history ?? []).map((h, i) => (
                    <div key={i} className="tl-item done">
                      <div className="tl-mark"><span className="tl-dot" /><span className="tl-line" /></div>
                      <div className="tl-body">
                        <b>{statusLabel(h.previousStatus)} → {statusLabel(h.newStatus)}</b>
                        <small>{h.changedBy} · {h.changedAt.slice(0, 16).replace("T", " ")}{h.reason ? ` · ${h.reason}` : ""}</small>
                      </div>
                    </div>
                  ))}
                  {(!history || history.length === 0) && (
                    <p className="muted" style={{ fontSize: 12.5 }}>Sin cambios de estado registrados todavía.</p>
                  )}
                </div>
              </div>
            </div>

            <div className="card card-pad">
              <h2 style={{ marginBottom: 14 }}>Secuencia de la orden (BR-08)</h2>
              <div className="timeline">
                {WORK_ORDER_STATUSES.map((s, i) => (
                  <div key={s} className={"tl-item " + (i < currentIndex ? "done" : i === currentIndex ? "current" : "")}>
                    <div className="tl-mark"><span className="tl-dot" /><span className="tl-line" /></div>
                    <div className="tl-body">
                      <b>{statusLabel(s)}</b>
                      <small>{i < currentIndex ? "Completado" : i === currentIndex ? "Estado actual" : "Pendiente"}</small>
                    </div>
                  </div>
                ))}
              </div>
              {order.status === "CANCELLED" && (
                <div className="alert err" style={{ marginTop: 10, marginBottom: 0 }}>
                  <Icon name="alert" />
                  <div>Esta orden fue cancelada.</div>
                </div>
              )}
            </div>
          </div>
        )}
      </Page>
    </>
  );
}
