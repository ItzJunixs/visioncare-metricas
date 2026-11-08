import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth";
import { ThemeToggle } from "../theme";

export function Login() {
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const location = useLocation() as { state?: { from?: string } };
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await signIn(username.trim(), password);
      navigate(location.state?.from ?? "/", { replace: true });
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login">
      <div className="login-hero">
        <div className="brand">
          <span className="dot" />
          <strong>VisionCare</strong>
        </div>
        <h2>La operación de la óptica en una sola plataforma.</h2>
        <p>Pacientes, fórmulas ópticas, inventario de monturas, órdenes de trabajo y facturación — MVP.</p>
        <div className="login-hint">
          Usuarios de demostración:
          <br />
          <code>admin / Admin2026*</code> · <code>seller / Seller2026*</code> · <code>optometrist / Optom2026*</code>
        </div>
      </div>

      <div className="login-form-wrap">
        <form className="login-card" onSubmit={submit}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <h3>Iniciar sesión</h3>
            <ThemeToggle />
          </div>
          <p>Ingresa con tu usuario para continuar.</p>

          {error && (
            <div className="alert err" style={{ marginBottom: 16 }}>
              <span>{error}</span>
            </div>
          )}

          <div className="field">
            <label htmlFor="username">Usuario</label>
            <input id="username" value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" />
          </div>
          <div className="field">
            <label htmlFor="password">Contraseña</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
          </div>

          <button className="btn primary" style={{ width: "100%", marginTop: 8 }} disabled={loading}>
            {loading ? "Ingresando…" : "Ingresar"}
          </button>
        </form>
      </div>
    </div>
  );
}
