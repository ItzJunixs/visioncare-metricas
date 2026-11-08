import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ApiError, api } from "../api";
import { useAuth } from "../auth";
import { useAsync } from "../useApi";
import { useToast } from "../toast";
import { Page, Topbar } from "../components/Layout";
import { Field } from "../components/Field";
import { Icon, StatusChip } from "../lib";
import type { OpticalFormula } from "../api";

const todayIso = () => new Date().toISOString().slice(0, 10);

export function PatientDetail() {
  const { id = "" } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const toast = useToast();
  const { data: patient, loading, error, reload } = useAsync(() => api.patient(id), [id]);
  const { data: formulas, reload: reloadFormulas } = useAsync(() => api.formulas(id), [id]);
  const [showFormula, setShowFormula] = useState(false);
  const [editingFormula, setEditingFormula] = useState<string | null>(null);
  const [form, setForm] = useState({
    odSph: "0", odCyl: "0", odAxis: "0", oiSph: "0", oiCyl: "0", oiAxis: "0",
    add: "", pupillaryDistance: "", formulaDate: todayIso(),
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);

  const bind = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [k]: e.target.value });

  function validate(): boolean {
    const e: Record<string, string> = {};
    const odAxis = Number(form.odAxis);
    const oiAxis = Number(form.oiAxis);
    if (isNaN(odAxis) || odAxis < 0 || odAxis > 180) e.odAxis = "El eje debe estar entre 0 y 180 (BR-02).";
    if (isNaN(oiAxis) || oiAxis < 0 || oiAxis > 180) e.oiAxis = "El eje debe estar entre 0 y 180 (BR-02).";
    if (!form.formulaDate) e.formulaDate = "Selecciona la fecha de la fórmula.";
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  function startEdit(f: OpticalFormula) {
    setEditingFormula(f.id);
    setForm({
      odSph: String(f.odSph), odCyl: String(f.odCyl), odAxis: String(f.odAxis),
      oiSph: String(f.oiSph), oiCyl: String(f.oiCyl), oiAxis: String(f.oiAxis),
      add: f.add == null ? "" : String(f.add),
      pupillaryDistance: f.pupillaryDistance == null ? "" : String(f.pupillaryDistance),
      formulaDate: f.formulaDate,
    });
    setShowFormula(true);
  }

  async function removeFormula(f: OpticalFormula) {
    if (!window.confirm("¿Eliminar la fórmula del " + f.formulaDate + "?")) return;
    try {
      await api.deleteFormula(id, f.id);
      toast("Fórmula eliminada.");
      reload();
      reloadFormulas();
    } catch (e) {
      toast(e instanceof ApiError ? e.message : "Error al eliminar la fórmula", "err");
    }
  }

  async function deactivate() {
    if (!window.confirm("¿Desactivar a este paciente? No se borra su historial.")) return;
    try {
      await api.deactivatePatient(id);
      toast("Paciente desactivado.");
      reload();
    } catch (e) {
      toast(e instanceof ApiError ? e.message : "Error al desactivar", "err");
    }
  }

  async function submitFormula(ev: React.FormEvent) {
    ev.preventDefault();
    if (!validate()) {
      toast("Revisa los campos de la fórmula.", "err");
      return;
    }
    setSubmitting(true);
    try {
      const payload = {
        odSph: Number(form.odSph), odCyl: Number(form.odCyl), odAxis: Number(form.odAxis),
        oiSph: Number(form.oiSph), oiCyl: Number(form.oiCyl), oiAxis: Number(form.oiAxis),
        add: form.add ? Number(form.add) : null,
        pupillaryDistance: form.pupillaryDistance ? Number(form.pupillaryDistance) : null,
        isCurrent: true,
        formulaDate: form.formulaDate,
      };
      if (editingFormula) {
        await api.updateFormula(id, editingFormula, payload);
        toast("Fórmula actualizada.");
      } else {
        await api.registerFormula(id, payload);
        toast("Fórmula óptica registrada como vigente (BR-03).");
      }
      setShowFormula(false);
      setEditingFormula(null);
      reload();
      reloadFormulas();
    } catch (e) {
      toast(e instanceof ApiError ? e.message : "Error al guardar la fórmula", "err");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <Topbar
        title="Ficha del paciente"
        subtitle="HU-02, HU-04 · Datos clínicos y control periódico"
        right={
          patient && user?.role !== "OPTOMETRIST" ? (
            <button
              className="btn primary sm"
              onClick={() => navigate("/work-orders/new", { state: { patientId: patient.id } })}
            >
              <Icon name="plus" />
              Nueva orden
            </button>
          ) : undefined
        }
      />
      <Page>
        <button className="btn subtle sm" onClick={() => navigate("/patients")} style={{ marginBottom: 16 }}>
          <Icon name="back" />
          Volver a pacientes
        </button>

        {loading && <p className="muted">Cargando…</p>}
        {error && <div className="alert err">{error}</div>}

        {patient && (
          <div className="stack">
            <div className="card card-pad">
              <div className="row-between">
                <div>
                  <h2 style={{ fontSize: 20 }}>{patient.fullName}</h2>
                  <div className="card-sub mono">
                    {patient.documentType} {patient.documentNumber} · {patient.eps}
                  </div>
                </div>
                <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
                  <StatusChip value={patient.status} />
                  <button className="btn ghost sm" onClick={() => navigate("/patients/" + patient.id + "/edit")}>
                    Editar
                  </button>
                  {patient.status === "ACTIVE" && (
                    <button className="btn ghost sm" onClick={deactivate}>Desactivar</button>
                  )}
                </div>
              </div>
            </div>

            <div className="split">
              <div className="card card-pad">
                <h2 style={{ marginBottom: 12 }}>Datos de contacto</h2>
                <dl className="kv">
                  <dt>Documento</dt>
                  <dd className="mono">{patient.documentType} {patient.documentNumber}</dd>
                  <dt>Teléfono</dt>
                  <dd className="mono">{patient.phone}</dd>
                  <dt>Correo</dt>
                  <dd>{patient.email ?? "—"}</dd>
                  <dt>EPS</dt>
                  <dd>{patient.eps}</dd>
                </dl>
              </div>

              <div className="card card-pad">
                <div className="row-between" style={{ marginBottom: 12 }}>
                  <h2>Fórmula óptica vigente</h2>
                  {patient.periodicControl && <StatusChip value={patient.periodicControl.status} />}
                </div>
                {patient.currentFormula ? (
                  <dl className="kv">
                    <dt>OD (esf/cil/eje)</dt>
                    <dd className="mono">
                      {patient.currentFormula.odSph} / {patient.currentFormula.odCyl} / {patient.currentFormula.odAxis}°
                    </dd>
                    <dt>OI (esf/cil/eje)</dt>
                    <dd className="mono">
                      {patient.currentFormula.oiSph} / {patient.currentFormula.oiCyl} / {patient.currentFormula.oiAxis}°
                    </dd>
                    <dt>Adición</dt>
                    <dd className="mono">{patient.currentFormula.add ?? "—"}</dd>
                    <dt>DIP</dt>
                    <dd className="mono">{patient.currentFormula.pupillaryDistance ?? "—"}</dd>
                    <dt>Fecha</dt>
                    <dd className="mono">{patient.currentFormula.formulaDate}</dd>
                    <dt>Próximo control</dt>
                    <dd className="mono">{patient.periodicControl?.nextControlDate ?? "—"}</dd>
                  </dl>
                ) : (
                  <p className="muted">Este paciente aún no tiene una fórmula óptica registrada.</p>
                )}
                {user?.role !== "SELLER" && (
                  <button className="btn ghost sm" style={{ marginTop: 14 }} onClick={() => { setEditingFormula(null); setShowFormula((s) => !s); }}>
                    <Icon name="plus" />
                    {showFormula ? "Cancelar" : "Registrar fórmula"}
                  </button>
                )}
              </div>
            </div>

            {formulas && formulas.length > 0 && (
              <div className="card card-pad">
                <h2 style={{ marginBottom: 12 }}>Historial de fórmulas</h2>
                <div className="table-wrap">
                  <table className="tbl">
                    <thead>
                      <tr><th>Fecha</th><th>OD</th><th>OI</th><th>Vigente</th>{user?.role !== "SELLER" && <th></th>}</tr>
                    </thead>
                    <tbody>
                      {formulas.map((f) => (
                        <tr key={f.id}>
                          <td className="mono">{f.formulaDate}</td>
                          <td className="mono">{f.odSph} / {f.odCyl} / {f.odAxis}°</td>
                          <td className="mono">{f.oiSph} / {f.oiCyl} / {f.oiAxis}°</td>
                          <td>{f.isCurrent ? "Sí" : "No"}</td>
                          {user?.role !== "SELLER" && (
                            <td>
                              <div style={{ display: "flex", gap: 6 }}>
                                <button className="btn ghost sm" onClick={() => startEdit(f)}>Editar</button>
                                <button className="btn ghost sm" onClick={() => removeFormula(f)}>Eliminar</button>
                              </div>
                            </td>
                          )}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {showFormula && (
              <form className="card card-pad" onSubmit={submitFormula} noValidate>
                <div className="section-title">{editingFormula ? "Editar fórmula óptica" : "Nueva fórmula óptica"}</div>
                <div className="section-note">
                  HU-02 · Esta fórmula queda como vigente y reemplaza a la anterior (BR-03). El eje debe estar entre 0°
                  y 180° (BR-02).
                </div>
                <div className="form-grid">
                  <Field label="OD esfera" required><input className="mono" value={form.odSph} onChange={bind("odSph")} /></Field>
                  <Field label="OD cilindro" required><input className="mono" value={form.odCyl} onChange={bind("odCyl")} /></Field>
                  <Field label="OD eje" required error={errors.odAxis}><input className="mono" value={form.odAxis} onChange={bind("odAxis")} /></Field>
                  <Field label="OI esfera" required><input className="mono" value={form.oiSph} onChange={bind("oiSph")} /></Field>
                  <Field label="OI cilindro" required><input className="mono" value={form.oiCyl} onChange={bind("oiCyl")} /></Field>
                  <Field label="OI eje" required error={errors.oiAxis}><input className="mono" value={form.oiAxis} onChange={bind("oiAxis")} /></Field>
                  <Field label="Adición"><input className="mono" value={form.add} onChange={bind("add")} placeholder="opcional" /></Field>
                  <Field label="Distancia pupilar (DIP)"><input className="mono" value={form.pupillaryDistance} onChange={bind("pupillaryDistance")} placeholder="opcional" /></Field>
                  <Field label="Fecha de la fórmula" required error={errors.formulaDate}>
                    <input type="date" value={form.formulaDate} onChange={bind("formulaDate")} />
                  </Field>
                </div>
                <div className="form-actions">
                  <button type="button" className="btn ghost" onClick={() => setShowFormula(false)}>Cancelar</button>
                  <button className="btn primary" disabled={submitting}>
                    <Icon name="check" />
                    {submitting ? "Guardando…" : "Guardar fórmula"}
                  </button>
                </div>
              </form>
            )}
          </div>
        )}
      </Page>
    </>
  );
}
