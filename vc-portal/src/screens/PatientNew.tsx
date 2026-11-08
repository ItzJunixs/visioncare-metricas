import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ApiError, api } from "../api";
import { useToast } from "../toast";
import { Page, Topbar } from "../components/Layout";
import { Field } from "../components/Field";
import { Icon } from "../lib";

const EPS_OPTIONS = ["Nueva EPS", "Sura EPS", "Sanitas", "Compensar", "Famisanar", "Coosalud", "Salud Total"];
const DOCUMENT_TYPES = ["CC", "TI", "CE", "PASSPORT"];

export function PatientNew() {
  const navigate = useNavigate();
  const toast = useToast();
  const { id } = useParams();
  const editing = Boolean(id);
  const [form, setForm] = useState({
    documentType: "CC",
    documentNumber: "",
    firstName: "",
    lastName: "",
    phone: "",
    email: "",
    eps: "",
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [banner, setBanner] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!id) return;
    api.patient(id)
      .then((p) =>
        setForm({
          documentType: p.documentType, documentNumber: p.documentNumber, firstName: p.firstName,
          lastName: p.lastName, phone: p.phone, email: p.email ?? "", eps: p.eps,
        }),
      )
      .catch((e: Error) => toast(e.message, "err"));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const bind = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
    setForm({ ...form, [k]: e.target.value });

  function validate(): boolean {
    const e: Record<string, string> = {};
    if (!form.documentNumber.trim()) e.documentNumber = "Ingresa el número de documento.";
    if (!form.firstName.trim()) e.firstName = "Ingresa los nombres.";
    if (!form.lastName.trim()) e.lastName = "Ingresa los apellidos.";
    if (!form.phone.trim()) e.phone = "Ingresa un teléfono de contacto.";
    if (!form.eps) e.eps = "Selecciona la EPS.";
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function submit(ev: React.FormEvent) {
    ev.preventDefault();
    setBanner(null);
    if (!validate()) {
      toast("Revisa los campos obligatorios.", "err");
      return;
    }
    setSubmitting(true);
    try {
      const body = { ...form, email: form.email || null };
      if (id) {
        await api.updatePatient(id, body);
        toast("Datos del paciente actualizados.");
        navigate("/patients/" + id);
      } else {
        const created = await api.createPatient(body);
        toast("Paciente registrado y disponible para búsqueda.");
        navigate("/patients", { state: { newId: created.id } });
      }
    } catch (e) {
      if (e instanceof ApiError && (e.status === 422 || e.status === 409)) {
        setErrors({ documentNumber: "Documento ya registrado (BR-01)." });
        setBanner(e.message);
      }
      toast(e instanceof Error ? e.message : "Error al guardar", "err");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <Topbar
        title={editing ? "Editar paciente" : "Nuevo paciente"}
        subtitle={editing ? "Actualizar los datos del paciente" : "HU-01 · Registrar un paciente"}
      />
      <Page>
        <form className="card card-pad" onSubmit={submit} noValidate>
          <div className="section-title">Información personal</div>
          <div className="section-note">
            Los campos con <span style={{ color: "var(--danger)" }}>*</span> son obligatorios. El documento debe ser
            único (BR-01).
          </div>

          {banner && (
            <div className="alert err">
              <Icon name="alert" />
              <div>
                <b>Documento duplicado.</b> {banner}
              </div>
            </div>
          )}

          <div className="form-grid">
            <Field label="Tipo de documento" required>
              <select value={form.documentType} onChange={bind("documentType")}>
                {DOCUMENT_TYPES.map((t) => (
                  <option key={t}>{t}</option>
                ))}
              </select>
            </Field>
            <Field label="Número de documento" required error={errors.documentNumber}>
              <input className="mono" inputMode="numeric" value={form.documentNumber} onChange={bind("documentNumber")} />
            </Field>
            <Field label="Nombres" required error={errors.firstName}>
              <input value={form.firstName} onChange={bind("firstName")} />
            </Field>
            <Field label="Apellidos" required error={errors.lastName}>
              <input value={form.lastName} onChange={bind("lastName")} />
            </Field>
            <Field label="Teléfono" required error={errors.phone}>
              <input className="mono" inputMode="tel" value={form.phone} onChange={bind("phone")} />
            </Field>
            <Field label="Correo electrónico">
              <input type="email" value={form.email} onChange={bind("email")} placeholder="opcional" />
            </Field>
            <Field label="EPS / aseguradora" required error={errors.eps}>
              <select value={form.eps} onChange={bind("eps")}>
                <option value="">Seleccionar…</option>
                {EPS_OPTIONS.map((x) => (
                  <option key={x}>{x}</option>
                ))}
              </select>
            </Field>
          </div>

          <div className="form-actions">
            <button type="button" className="btn ghost" onClick={() => navigate("/patients")}>
              Cancelar
            </button>
            <button className="btn primary" disabled={submitting}>
              <Icon name="check" />
              {submitting ? "Guardando…" : editing ? "Guardar cambios" : "Guardar paciente"}
            </button>
          </div>
        </form>
      </Page>
    </>
  );
}
