import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ApiError, api } from "../api";
import { useToast } from "../toast";
import { Page, Topbar } from "../components/Layout";
import { Field } from "../components/Field";
import { cop, Icon } from "../lib";

export function FrameNew() {
  const navigate = useNavigate();
  const toast = useToast();
  const { id } = useParams();
  const editing = Boolean(id);
  const [form, setForm] = useState({
    sku: "", brand: "", model: "", buyPrice: "", sellPrice: "", stockQuantity: "0", minStock: "2",
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [banner, setBanner] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!id) return;
    api.frame(id)
      .then((f) =>
        setForm({
          sku: f.sku, brand: f.brand, model: f.model, buyPrice: String(f.buyPrice),
          sellPrice: String(f.sellPrice), stockQuantity: String(f.stockQuantity), minStock: String(f.minStock),
        }),
      )
      .catch((e: Error) => toast(e.message, "err"));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const bind = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [k]: e.target.value });

  function validate(): boolean {
    const e: Record<string, string> = {};
    const buy = Number(form.buyPrice);
    const sell = Number(form.sellPrice);
    const stock = Number(form.stockQuantity);
    if (!form.sku.trim()) e.sku = "Ingresa el SKU (BR-05: debe ser único).";
    if (!form.brand.trim()) e.brand = "Ingresa la marca.";
    if (!form.model.trim()) e.model = "Ingresa el modelo.";
    if (!form.buyPrice || isNaN(buy) || buy < 0) e.buyPrice = "Precio de compra inválido.";
    if (!form.sellPrice || isNaN(sell) || sell < 0) e.sellPrice = "Precio de venta inválido.";
    if (isNaN(stock) || stock < 0) e.stockQuantity = "El stock no puede ser negativo (BR-06).";
    if (!e.buyPrice && !e.sellPrice && sell < buy) e.sellPrice = "El precio de venta no puede ser menor al de compra (BR-04).";
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function submit(ev: React.FormEvent) {
    ev.preventDefault();
    setBanner(null);
    if (!validate()) {
      toast("Revisa los campos del formulario.", "err");
      return;
    }
    setSubmitting(true);
    try {
      const body = {
        sku: form.sku.trim(), brand: form.brand.trim(), model: form.model.trim(),
        buyPrice: Number(form.buyPrice), sellPrice: Number(form.sellPrice),
        stockQuantity: Number(form.stockQuantity), minStock: Number(form.minStock || 0),
      };
      const saved = id ? await api.updateFrame(id, body) : await api.createFrame(body);
      toast(id ? "Montura actualizada." : "Montura registrada en inventario.");
      navigate("/inventory", { state: { newId: saved.id } });
    } catch (e) {
      if (e instanceof ApiError && (e.status === 422 || e.status === 409)) {
        setBanner(e.message);
        if (/sku/i.test(e.message)) setErrors({ sku: "SKU duplicado (BR-05)." });
        else if (/price|precio/i.test(e.message)) setErrors({ sellPrice: "Precio de venta menor al de compra (BR-04)." });
      }
      toast(e instanceof Error ? e.message : "Error al guardar", "err");
    } finally {
      setSubmitting(false);
    }
  }

  const buy = Number(form.buyPrice);
  const sell = Number(form.sellPrice);

  return (
    <>
      <Topbar title={editing ? "Editar montura" : "Registrar nueva montura"} subtitle="HU-05 · SKU único (BR-05) · stock ≥ 0 (BR-06) · precio venta ≥ compra (BR-04)" />
      <Page>
        <form className="card card-pad" onSubmit={submit} noValidate>
          <div className="section-title">Información del producto</div>
          <div className="section-note">Agrega una referencia al inventario de la sede.</div>

          {banner && (
            <div className="alert err">
              <Icon name="alert" />
              <div>{banner}</div>
            </div>
          )}

          <div className="form-grid">
            <Field label="Marca" required error={errors.brand}>
              <input value={form.brand} onChange={bind("brand")} />
            </Field>
            <Field label="SKU / Referencia" required error={errors.sku}>
              <input className="mono" value={form.sku} onChange={bind("sku")} />
            </Field>
            <Field label="Modelo" required error={errors.model}>
              <input value={form.model} onChange={bind("model")} />
            </Field>
            <Field label="Precio de compra" required error={errors.buyPrice}>
              <input className="mono" inputMode="numeric" value={form.buyPrice} onChange={bind("buyPrice")} />
            </Field>
            <Field label="Precio de venta" required error={errors.sellPrice}>
              <input className="mono" inputMode="numeric" value={form.sellPrice} onChange={bind("sellPrice")} />
            </Field>
            <Field label={editing ? "Stock" : "Stock inicial"} required error={errors.stockQuantity}>
              <input className="mono" inputMode="numeric" value={form.stockQuantity} onChange={bind("stockQuantity")} />
            </Field>
            <Field label="Stock mínimo">
              <input className="mono" inputMode="numeric" value={form.minStock} onChange={bind("minStock")} />
            </Field>
          </div>

          {form.buyPrice && form.sellPrice && !isNaN(buy) && !isNaN(sell) && (
            <p className="muted" style={{ fontSize: 12, marginTop: 12 }}>
              Margen: <span className="mono">{cop(sell - buy)}</span>
            </p>
          )}

          <div className="form-actions">
            <button type="button" className="btn ghost" onClick={() => navigate("/inventory")}>Cancelar</button>
            <button className="btn primary" disabled={submitting}>
              <Icon name="check" />
              {submitting ? "Guardando…" : editing ? "Guardar cambios" : "Guardar montura"}
            </button>
          </div>
        </form>
      </Page>
    </>
  );
}
