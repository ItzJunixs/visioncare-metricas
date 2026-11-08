import { useNavigate } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../auth";
import { useAsync } from "../useApi";
import { Page, Topbar } from "../components/Layout";
import { Icon, roleLabel } from "../lib";

/** HU-12 · Tablero de staff: contadores de órdenes por estado y monturas bajo mínimo. */
export function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const { data, loading, error } = useAsync(
    () =>
      Promise.all([api.workOrders(), api.frames(true), api.patients()]).then(([workOrders, lowStockFrames, patients]) => ({
        workOrders,
        lowStockFrames,
        patients,
      })),
    [],
  );

  const activeStatuses = ["QUOTE", "APPROVED", "IN_LAB", "READY"];
  const active = data?.workOrders.filter((o) => activeStatuses.includes(o.status)).length ?? 0;
  const inQuote = data?.workOrders.filter((o) => o.status === "QUOTE").length ?? 0;

  return (
    <>
      <Topbar
        title={`Hola, ${user?.name.split(" ")[0]}`}
        subtitle={`${roleLabel(user!.role)} · VisionCare`}
        right={
          user?.role !== "OPTOMETRIST" ? (
            <button className="btn primary sm" onClick={() => navigate("/work-orders/new")}>
              <Icon name="plus" />
              Nueva orden
            </button>
          ) : undefined
        }
      />
      <Page>
        {error && <div className="alert err">{error}</div>}
        {loading && <p className="muted">Cargando…</p>}
        {data && (
          <div className="stack">
            <div className="grid-kpi">
              <Kpi label="Pacientes registrados" value={data.patients.length} tone="ok" foot="disponibles" />
              <Kpi label="Órdenes activas" value={active} tone="flat" foot={`${inQuote} en cotización`} />
              <Kpi
                label="Monturas bajo mínimo"
                value={data.lowStockFrames.length}
                tone={data.lowStockFrames.length > 0 ? "warn" : "ok"}
                foot={data.lowStockFrames.length > 0 ? "revisar stock" : "todo en orden"}
              />
              <Kpi
                label="Órdenes entregadas"
                value={data.workOrders.filter((o) => o.status === "DELIVERED").length}
                tone="flat"
                foot="histórico"
              />
            </div>

            <div className="card card-pad">
              <h2>Accesos rápidos</h2>
              <div className="card-sub" style={{ marginBottom: 14 }}>
                Las operaciones principales del MVP
              </div>
              <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 12 }}>
                <Quick label="Registrar paciente" onClick={() => navigate("/patients/new")} />
                {user?.role === "ADMIN" && (
                  <Quick label="Registrar montura" onClick={() => navigate("/inventory/new")} />
                )}
                {user?.role !== "OPTOMETRIST" && (
                  <Quick label="Crear orden de trabajo" onClick={() => navigate("/work-orders/new")} />
                )}
              </div>
            </div>
          </div>
        )}
      </Page>
    </>
  );
}

function Kpi({ label, value, tone, foot }: { label: string; value: number; tone: string; foot: string }) {
  return (
    <div className="kpi">
      <div className="k-label">{label}</div>
      <div className="k-val mono">{value}</div>
      <div className={"k-foot " + tone}>{foot}</div>
    </div>
  );
}

function Quick({ label, onClick }: { label: string; onClick: () => void }) {
  return (
    <button className="frame-card" onClick={onClick}>
      <b>{label}</b>
      <span
        className="muted"
        style={{ fontSize: 12, marginTop: 4, display: "inline-flex", gap: 4, alignItems: "center" }}
      >
        Abrir <Icon name="arrow" size={12} />
      </span>
    </button>
  );
}
