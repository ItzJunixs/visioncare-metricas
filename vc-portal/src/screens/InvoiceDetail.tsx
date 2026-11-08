import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ApiError, api } from "../api";
import { useAsync } from "../useApi";
import { useToast } from "../toast";
import { Page, Topbar } from "../components/Layout";
import { Field } from "../components/Field";
import { cop, Icon, methodLabel, StatusChip } from "../lib";

const METHODS = ["CASH", "CARD", "TRANSFER"];

export function InvoiceDetail() {
  const { id = "" } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const { data: invoice, loading, error, reload } = useAsync(() => api.invoice(id), [id]);
  const [amount, setAmount] = useState("");
  const [method, setMethod] = useState("CASH");
  const [submitting, setSubmitting] = useState(false);

  async function submit(ev: React.FormEvent) {
    ev.preventDefault();
    const value = Number(amount);
    if (!value || value <= 0) {
      toast("Ingresa un monto válido.", "err");
      return;
    }
    setSubmitting(true);
    try {
      await api.registerPayment(id, value, method);
      toast("Pago registrado.");
      setAmount("");
      reload();
    } catch (e) {
      if (e instanceof ApiError && e.status === 409) {
        toast("Este pago excede el saldo pendiente de la factura (BR-13).", "err");
      } else {
        toast(e instanceof Error ? e.message : "Error al registrar el pago", "err");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <Topbar title="Detalle de la factura" subtitle="HU-10 · Pagos y saldo (BR-13, BR-14, BR-15)" />
      <Page>
        <button className="btn subtle sm" onClick={() => navigate("/invoices")} style={{ marginBottom: 16 }}>
          <Icon name="back" />
          Volver a facturación
        </button>

        {loading && <p className="muted">Cargando…</p>}
        {error && <div className="alert err">{error}</div>}

        {invoice && (
          <div className="split">
            <div className="stack">
              <div className="card card-pad">
                <div className="row-between" style={{ marginBottom: 14 }}>
                  <div>
                    <h2 className="mono" style={{ fontSize: 18 }}>{invoice.invoiceNumber}</h2>
                    <div className="card-sub">Orden asociada: <span className="mono">{invoice.workOrderId.slice(0, 8)}…</span></div>
                  </div>
                  <StatusChip value={invoice.status} />
                </div>
                <div className="sum-row"><span>Subtotal</span><span className="mono">{cop(invoice.subtotal)}</span></div>
                <div className="sum-row"><span>Descuento</span><span className="mono">-{cop(invoice.discount)}</span></div>
                <div className="sum-row"><span>Impuesto</span><span className="mono">{cop(invoice.tax)}</span></div>
                <div className="sum-row total"><span>Total</span><span className="mono">{cop(invoice.total)}</span></div>
                <div className="sum-row" style={{ fontWeight: 700 }}>
                  <span>Saldo pendiente</span>
                  <span className="mono">{cop(invoice.balance)}</span>
                </div>
              </div>

              <div className="card card-pad">
                <h2 style={{ marginBottom: 12 }}>Pagos registrados</h2>
                {invoice.payments.length === 0 && <p className="muted">Sin pagos registrados todavía.</p>}
                {invoice.payments.map((p) => (
                  <div className="sum-row" key={p.id}>
                    <span>{methodLabel(p.method)} · {p.paidAt.slice(0, 16).replace("T", " ")}</span>
                    <span className="mono">{cop(p.amount)}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="card card-pad">
              <h2 style={{ marginBottom: 12 }}>Registrar pago</h2>
              {invoice.balance <= 0 ? (
                <div className="alert ok" style={{ marginBottom: 0 }}>
                  <Icon name="check" />
                  <div>Esta factura ya está pagada en su totalidad.</div>
                </div>
              ) : (
                <form onSubmit={submit}>
                  <Field label="Monto" required>
                    <input className="mono" inputMode="decimal" value={amount} onChange={(e) => setAmount(e.target.value)} placeholder={String(invoice.balance)} />
                  </Field>
                  <Field label="Método de pago" required>
                    <select value={method} onChange={(e) => setMethod(e.target.value)}>
                      {METHODS.map((m) => <option key={m} value={m}>{methodLabel(m)}</option>)}
                    </select>
                  </Field>
                  <button className="btn primary" style={{ width: "100%", marginTop: 8 }} disabled={submitting}>
                    <Icon name="check" />
                    {submitting ? "Registrando…" : "Registrar pago"}
                  </button>
                </form>
              )}
            </div>
          </div>
        )}
      </Page>
    </>
  );
}
