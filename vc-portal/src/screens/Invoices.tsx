import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api";
import { useAsync } from "../useApi";
import { Page, Topbar } from "../components/Layout";
import { cop, StatusChip } from "../lib";

const FILTERS = ["ALL", "PENDING", "PARTIAL", "PAID"] as const;
const FILTER_LABEL: Record<string, string> = { ALL: "Todas", PENDING: "Pendientes", PARTIAL: "Pago parcial", PAID: "Pagadas" };

export function Invoices() {
  const navigate = useNavigate();
  const [filter, setFilter] = useState<(typeof FILTERS)[number]>("ALL");
  const { data, loading, error } = useAsync(() => api.invoices(filter === "ALL" ? undefined : filter), [filter]);

  return (
    <>
      <Topbar title="Facturación" subtitle="HU-10 · Facturas generadas al aprobar una orden" />
      <Page>
        <div className="stack">
          <div className="filter-chips">
            {FILTERS.map((f) => (
              <button key={f} className={f === filter ? "on" : ""} onClick={() => setFilter(f)}>{FILTER_LABEL[f]}</button>
            ))}
          </div>

          {loading && <p className="muted">Cargando…</p>}
          {error && <div className="alert err">{error}</div>}
          {data && (
            <div className="table-wrap">
              <table className="tbl">
                <thead>
                  <tr>
                    <th>N° factura</th>
                    <th className="num">Total</th>
                    <th className="num">Saldo</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {data.map((inv) => (
                    <tr key={inv.id} className="clickable" onClick={() => navigate("/invoices/" + inv.id)}>
                      <td className="mono">{inv.invoiceNumber}</td>
                      <td className="num mono">{cop(inv.total)}</td>
                      <td className="num mono">{inv.balance == null ? "—" : cop(inv.balance)}</td>
                      <td><StatusChip value={inv.status} /></td>
                    </tr>
                  ))}
                  {data.length === 0 && <tr><td colSpan={4} className="empty">Aún no hay facturas con este filtro.</td></tr>}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </Page>
    </>
  );
}
