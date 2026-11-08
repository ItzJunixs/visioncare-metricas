import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { ApiError, api, type Frame, type Patient } from "../api";
import { useToast } from "../toast";
import { Page, Topbar } from "../components/Layout";
import { cop, Icon, StatusChip } from "../lib";

const STEPS = ["Paciente", "Montura", "Resumen"];

export function WorkOrderNew() {
  const navigate = useNavigate();
  const toast = useToast();
  const prefill = (useLocation().state as { patientId?: string } | null)?.patientId;

  const [step, setStep] = useState(prefill ? 2 : 1);
  const [patients, setPatients] = useState<Patient[]>([]);
  const [frames, setFrames] = useState<Frame[]>([]);
  const [query, setQuery] = useState("");
  const [patientId, setPatientId] = useState<string | null>(prefill ?? null);
  const [frameId, setFrameId] = useState<string | null>(null);
  const [stockError, setStockError] = useState<string | null>(null);
  const [formulaError, setFormulaError] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    api.frames().then(setFrames).catch((e: Error) => toast(e.message, "err"));
  }, [toast]);

  useEffect(() => {
    const timer = setTimeout(() => {
      api.patients(query).then(setPatients).catch(() => setPatients([]));
    }, 180);
    return () => clearTimeout(timer);
  }, [query]);

  const patient = useMemo(() => patients.find((p) => p.id === patientId) ?? null, [patients, patientId]);
  const frame = useMemo(() => frames.find((f) => f.id === frameId) ?? null, [frames, frameId]);

  function pickFrame(f: Frame) {
    if (f.stockQuantity === 0) {
      setFrameId(null);
      setStockError(`${f.brand} ${f.model}`);
      toast("Montura sin stock — no se puede usar en la orden (BR-06).", "err");
      return;
    }
    setStockError(null);
    setFrameId(f.id);
  }

  async function create() {
    if (!patientId || !frameId) return;
    setSubmitting(true);
    setFormulaError(false);
    try {
      const order = await api.createWorkOrder(patientId, frameId);
      toast(`Orden ${order.orderCode} creada en estado Cotización.`);
      navigate("/work-orders/" + order.id, { state: { newId: order.id } });
    } catch (e) {
      if (e instanceof ApiError && e.status === 404 && /formula/i.test(e.message)) {
        setFormulaError(true);
      } else if (e instanceof ApiError && /stock/i.test(e.message)) {
        setStep(2);
        setFrameId(null);
        setStockError(frame ? `${frame.brand} ${frame.model}` : "la montura elegida");
      }
      toast(e instanceof Error ? e.message : "Error al crear la orden", "err");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <Topbar title="Nueva orden de trabajo" subtitle="HU-08 · Vincula un paciente y una montura con stock" />
      <Page>
        <div className="stepper">
          {STEPS.map((label, i) => {
            const n = i + 1;
            const cls = n < step ? "done" : n === step ? "current" : "";
            return (
              <span key={label} style={{ display: "contents" }}>
                {i > 0 && <span className="step-sep" />}
                <button
                  className={"step " + cls}
                  disabled={n >= step}
                  onClick={() => n < step && setStep(n)}
                  style={{ cursor: n < step ? "pointer" : "default" }}
                >
                  <span className="n">{n < step ? "✓" : n}</span>
                  {label}
                </button>
              </span>
            );
          })}
        </div>

        {step === 1 && (
          <div className="card card-pad">
            <h2 style={{ marginBottom: 4 }}>Paso 1 · Elegir paciente</h2>
            <div className="card-sub" style={{ marginBottom: 16 }}>
              La orden se vincula a un paciente existente, con fórmula óptica vigente (HU-08).
            </div>
            <div className="search" style={{ marginBottom: 14 }}>
              <Icon name="search" />
              <input placeholder="Buscar paciente…" value={query} onChange={(e) => setQuery(e.target.value)} />
            </div>
            <div className="pick-list">
              {patients.map((p) => (
                <button
                  key={p.id}
                  className={"pick" + (patientId === p.id ? " sel" : "")}
                  onClick={() => setPatientId(p.id)}
                >
                  <span className="radio" />
                  <span className="pk-main">
                    <b>{p.fullName}</b>
                    <small className="mono">{p.documentType} {p.documentNumber} · {p.eps}</small>
                  </span>
                </button>
              ))}
              {patients.length === 0 && (
                <div className="empty">
                  Sin coincidencias.{" "}
                  <button className="btn ghost sm" onClick={() => navigate("/patients/new")}>Registrar paciente</button>
                </div>
              )}
            </div>
            <div className="form-actions">
              <button className="btn ghost" onClick={() => navigate("/work-orders")}>Cancelar</button>
              <button className="btn primary" disabled={!patientId} onClick={() => setStep(2)}>
                Continuar <Icon name="arrow" />
              </button>
            </div>
          </div>
        )}

        {step === 2 && (
          <div className="card card-pad">
            <h2 style={{ marginBottom: 4 }}>Paso 2 · Elegir montura</h2>
            <div className="card-sub" style={{ marginBottom: 16 }}>
              Solo puedes continuar con una montura que tenga stock disponible (BR-06).
            </div>
            {stockError && (
              <div className="alert err">
                <Icon name="alert" />
                <div>
                  <b>Stock insuficiente.</b> “{stockError}” no tiene unidades disponibles. Elige otra montura o
                  registra stock en Inventario.
                </div>
              </div>
            )}
            <div className="frame-grid">
              {frames.map((f) => (
                <button
                  key={f.id}
                  className={"frame-card" + (frameId === f.id ? " sel" : "") + (f.stockQuantity === 0 ? " oos" : "")}
                  onClick={() => pickFrame(f)}
                >
                  <div className="fc-top">
                    <b>{f.brand}</b>
                    {f.stockQuantity === 0 ? (
                      <span className="chip danger"><span className="d" />Sin stock</span>
                    ) : (
                      <span className="chip ok"><span className="d" />{f.stockQuantity} ud</span>
                    )}
                  </div>
                  <span className="fc-sku mono">{f.sku} · {f.model}</span>
                  <span className="fc-price mono">{cop(f.sellPrice)}</span>
                </button>
              ))}
            </div>
            <div className="form-actions">
              <button className="btn ghost" onClick={() => setStep(1)}><Icon name="back" />Atrás</button>
              <button className="btn primary" disabled={!frameId} onClick={() => setStep(3)}>
                Continuar <Icon name="arrow" />
              </button>
            </div>
          </div>
        )}

        {step === 3 && patient && frame && (
          <div className="split">
            <div className="card card-pad">
              <h2 style={{ marginBottom: 12 }}>Paso 3 · Resumen</h2>
              {formulaError && (
                <div className="alert err">
                  <Icon name="alert" />
                  <div>
                    Este paciente no tiene una fórmula óptica vigente. Regístrala en su ficha antes de crear la orden.
                  </div>
                </div>
              )}
              <dl className="kv">
                <dt>Paciente</dt>
                <dd>{patient.fullName}</dd>
                <dt>Documento</dt>
                <dd className="mono">{patient.documentType} {patient.documentNumber}</dd>
                <dt>Montura</dt>
                <dd>{frame.brand} {frame.model}</dd>
                <dt>SKU</dt>
                <dd className="mono">{frame.sku}</dd>
                <dt>Estado inicial</dt>
                <dd><StatusChip value="QUOTE" /></dd>
              </dl>
              <div className="alert info" style={{ marginTop: 16, marginBottom: 0 }}>
                <Icon name="alert" />
                <div>La orden queda en Cotización; el stock de la montura solo se descuenta al aprobarla (BR-09).</div>
              </div>
            </div>
            <div className="card card-pad">
              <h2 style={{ marginBottom: 12 }}>Total estimado</h2>
              <div className="sum-row">
                <span>Montura (1 ud)</span>
                <span className="mono">{cop(frame.sellPrice)}</span>
              </div>
              <div className="sum-row total">
                <span>Total</span>
                <span className="mono">{cop(frame.sellPrice)}</span>
              </div>
              <div style={{ display: "flex", flexDirection: "column", gap: 10, marginTop: 20 }}>
                <button className="btn primary" onClick={create} disabled={submitting}>
                  <Icon name="check" />
                  {submitting ? "Creando…" : "Crear orden"}
                </button>
                <button className="btn ghost" onClick={() => setStep(2)}>
                  <Icon name="back" />
                  Atrás
                </button>
              </div>
            </div>
          </div>
        )}
      </Page>
    </>
  );
}
