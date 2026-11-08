import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { api } from "../api";
import { useAsync } from "../useApi";
import { Page, Topbar } from "../components/Layout";
import { Icon, StatusChip } from "../lib";

const FILTERS = ["ALL", "QUOTE", "APPROVED", "IN_LAB", "READY", "DELIVERED", "CANCELLED"] as const;
const FILTER_LABEL: Record<string, string> = {
  ALL: "Todas", QUOTE: "Cotización", APPROVED: "Aprobadas", IN_LAB: "En laboratorio",
  READY: "Listas", DELIVERED: "Entregadas", CANCELLED: "Canceladas",
};

export function WorkOrders() {
  const navigate = useNavigate();
  const newId = (useLocation().state as { newId?: string } | null)?.newId;
  const [filter, setFilter] = useState<(typeof FILTERS)[number]>("ALL");
  const { data, loading, error } = useAsync(() => api.workOrders(filter === "ALL" ? undefined : filter), [filter]);

  return (
    <>
      <Topbar
        title="Órdenes de trabajo"
        subtitle="HU-07, HU-08, HU-09 · Órdenes de la sede y su estado"
        right={
          <button className="btn primary sm" onClick={() => navigate("/work-orders/new")}>
            <Icon name="plus" />
            Nueva orden
          </button>
        }
      />
      <Page>
        <div className="stack">
          <div className="filter-chips">
            {FILTERS.map((f) => (
              <button key={f} className={f === filter ? "on" : ""} onClick={() => setFilter(f)}>
                {FILTER_LABEL[f]}
              </button>
            ))}
          </div>

          {loading && <p className="muted">Cargando…</p>}
          {error && <div className="alert err">{error}</div>}
          {data && (
            <div className="table-wrap">
              <table className="tbl">
                <thead>
                  <tr>
                    <th>N° orden</th>
                    <th>Estado</th>
                    <th>Creada</th>
                    <th>Entrega real</th>
                  </tr>
                </thead>
                <tbody>
                  {data.map((o) => (
                    <tr
                      key={o.id}
                      className={"clickable" + (o.id === newId ? " row-new" : "")}
                      onClick={() => navigate("/work-orders/" + o.id)}
                    >
                      <td className="mono">
                        {o.orderCode}
                        {o.id === newId && <span className="tag-new">NUEVA</span>}
                      </td>
                      <td><StatusChip value={o.status} /></td>
                      <td className="mono muted">{o.createdAt.slice(0, 10)}</td>
                      <td className="mono muted">{o.actualDelivery ? o.actualDelivery.slice(0, 10) : "—"}</td>
                    </tr>
                  ))}
                  {data.length === 0 && (
                    <tr><td colSpan={4} className="empty">Aún no hay órdenes con este filtro.</td></tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </Page>
    </>
  );
}
