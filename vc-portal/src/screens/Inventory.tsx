import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { ApiError, api } from "../api";
import { useAuth } from "../auth";
import { useAsync } from "../useApi";
import { useToast } from "../toast";
import { Page, Topbar } from "../components/Layout";
import { cop, Icon } from "../lib";

export function Inventory() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const toast = useToast();
  const newId = (useLocation().state as { newId?: string } | null)?.newId;
  const [onlyLowStock, setOnlyLowStock] = useState(false);
  const { data, loading, error, reload } = useAsync(() => api.frames(onlyLowStock), [onlyLowStock]);
  const [editing, setEditing] = useState<string | null>(null);
  const [minStockValue, setMinStockValue] = useState("0");

  async function saveMinStock(id: string) {
    try {
      await api.setMinStock(id, Number(minStockValue));
      toast("Stock mínimo actualizado.");
      setEditing(null);
      reload();
    } catch (e) {
      toast(e instanceof ApiError ? e.message : "Error al actualizar", "err");
    }
  }

  async function removeFrame(id: string, sku: string) {
    if (!window.confirm("¿Eliminar la montura " + sku + "? Esta acción no se puede deshacer.")) return;
    try {
      await api.deleteFrame(id);
      toast("Montura eliminada.");
      reload();
    } catch (e) {
      toast(e instanceof ApiError ? e.message : "Error al eliminar", "err");
    }
  }

  return (
    <>
      <Topbar
        title="Inventario de monturas"
        subtitle="HU-05, HU-06 · Existencias y precios de la sede"
        right={
          user?.role === "ADMIN" ? (
            <button className="btn primary sm" onClick={() => navigate("/inventory/new")}>
              <Icon name="plus" />
              Registrar montura
            </button>
          ) : undefined
        }
      />
      <Page>
        <div className="stack">
          <label style={{ display: "inline-flex", alignItems: "center", gap: 8, fontSize: 13 }}>
            <input type="checkbox" checked={onlyLowStock} onChange={(e) => setOnlyLowStock(e.target.checked)} />
            Mostrar solo monturas bajo el mínimo
          </label>

          {loading && <p className="muted">Cargando…</p>}
          {error && <div className="alert err">{error}</div>}
          {data && (
            <div className="table-wrap">
              <table className="tbl">
                <thead>
                  <tr>
                    <th>SKU</th>
                    <th>Marca / modelo</th>
                    <th className="num">Costo</th>
                    <th className="num">Precio venta</th>
                    <th>Stock</th>
                    <th>Mínimo</th>
                    {user?.role === "ADMIN" && <th></th>}
                  </tr>
                </thead>
                <tbody>
                  {data.map((f) => (
                    <tr key={f.id} className={f.id === newId ? "row-new" : ""}>
                      <td className="mono">
                        {f.sku}
                        {f.id === newId && <span className="tag-new">NUEVA</span>}
                      </td>
                      <td>
                        <b>{f.brand}</b>
                        <br />
                        <span className="muted" style={{ fontSize: 12 }}>{f.model}</span>
                      </td>
                      <td className="num mono">{cop(f.buyPrice)}</td>
                      <td className="num mono">{cop(f.sellPrice)}</td>
                      <td>
                        {f.stockQuantity === 0 ? (
                          <span className="chip danger"><span className="d" />Sin stock</span>
                        ) : f.belowMinStock ? (
                          <span className="chip warn"><span className="d" />{f.stockQuantity} ud</span>
                        ) : (
                          <span className="chip ok"><span className="d" />{f.stockQuantity} ud</span>
                        )}
                      </td>
                      <td>
                        {editing === f.id ? (
                          <div style={{ display: "flex", gap: 6 }}>
                            <input
                              className="mono"
                              style={{ width: 64, padding: "4px 8px" }}
                              value={minStockValue}
                              onChange={(e) => setMinStockValue(e.target.value)}
                            />
                            <button className="btn primary sm" onClick={() => saveMinStock(f.id)}>OK</button>
                          </div>
                        ) : (
                          <span className="mono">{f.minStock}</span>
                        )}
                      </td>
                      {user?.role === "ADMIN" && (
                        <td>
                          {editing !== f.id && (
                            <div style={{ display: "flex", gap: 6 }}>
                              <button
                                className="btn subtle sm"
                                onClick={() => { setEditing(f.id); setMinStockValue(String(f.minStock)); }}
                              >
                                Editar mínimo
                              </button>
                              <button className="btn ghost sm" onClick={() => navigate("/inventory/" + f.id + "/edit")}>
                                Editar
                              </button>
                              <button className="btn ghost sm" onClick={() => removeFrame(f.id, f.sku)}>
                                Eliminar
                              </button>
                            </div>
                          )}
                        </td>
                      )}
                    </tr>
                  ))}
                  {data.length === 0 && (
                    <tr><td colSpan={7} className="empty">Sin monturas que coincidan con el filtro.</td></tr>
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
