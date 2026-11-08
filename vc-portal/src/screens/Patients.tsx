import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { api, type Patient } from "../api";
import { Page, Topbar } from "../components/Layout";
import { Icon, StatusChip } from "../lib";

export function Patients() {
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [rows, setRows] = useState<Patient[]>([]);
  const [error, setError] = useState<string | null>(null);
  const newId = (useLocation().state as { newId?: string } | null)?.newId;

  useEffect(() => {
    const timer = setTimeout(() => {
      api
        .patients(query)
        .then(setRows)
        .catch((e: Error) => setError(e.message));
    }, 180);
    return () => clearTimeout(timer);
  }, [query]);

  return (
    <>
      <Topbar
        title="Pacientes"
        subtitle="HU-03 · Directorio de la sede"
        right={
          <button className="btn primary sm" onClick={() => navigate("/patients/new")}>
            <Icon name="plus" />
            Nuevo paciente
          </button>
        }
      />
      <Page>
        <div className="stack">
          <div className="search" style={{ maxWidth: 340 }}>
            <Icon name="search" />
            <input
              placeholder="Buscar por nombre o documento…"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
          </div>

          {error && <div className="alert err">{error}</div>}

          <div className="table-wrap">
            <table className="tbl">
              <thead>
                <tr>
                  <th>Paciente</th>
                  <th>Documento</th>
                  <th>Teléfono</th>
                  <th>EPS</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((p) => (
                  <tr
                    key={p.id}
                    className={"clickable" + (p.id === newId ? " row-new" : "")}
                    onClick={() => navigate("/patients/" + p.id)}
                  >
                    <td>
                      <b>{p.fullName}</b>
                      {p.id === newId && <span className="tag-new">NUEVO</span>}
                    </td>
                    <td className="mono">
                      {p.documentType} {p.documentNumber}
                    </td>
                    <td className="mono">{p.phone}</td>
                    <td>{p.eps}</td>
                    <td>
                      <StatusChip value={p.status} />
                    </td>
                  </tr>
                ))}
                {rows.length === 0 && (
                  <tr>
                    <td colSpan={5} className="empty">
                      {query ? `Sin resultados para “${query}”.` : "Aún no hay pacientes."}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
          <p className="muted" style={{ fontSize: 12 }}>
            {rows.length} paciente(s) · toca una fila para ver la ficha
          </p>
        </div>
      </Page>
    </>
  );
}
