import type { ReactNode } from "react";
import { NavLink, Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth";
import { Icon, initials, roleLabel } from "../lib";
import { ThemeToggle } from "../theme";
import type { Role } from "../api";

interface NavEntry {
  to: string;
  label: string;
  icon: string;
  roles: Role[];
}

const NAV: NavEntry[] = [
  { to: "/", label: "Resumen", icon: "home", roles: ["ADMIN", "SELLER", "OPTOMETRIST"] },
  { to: "/patients", label: "Pacientes", icon: "users", roles: ["ADMIN", "SELLER", "OPTOMETRIST"] },
  { to: "/inventory", label: "Inventario", icon: "box", roles: ["ADMIN", "SELLER"] },
  { to: "/work-orders", label: "Órdenes", icon: "clip", roles: ["ADMIN", "SELLER"] },
  { to: "/invoices", label: "Facturación", icon: "cash", roles: ["ADMIN", "SELLER"] },
];

export function RequireAuth({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const location = useLocation();
  if (!user) return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  return <>{children}</>;
}

export function Layout() {
  const { user, signOut } = useAuth();
  if (!user) return null;
  const visible = NAV.filter((n) => n.roles.includes(user.role));

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand">
          <span className="dot" />
          <strong>VisionCare</strong>
        </div>
        <div className="nav-label">Gestión</div>
        {visible.map((n) => (
          <NavLink key={n.to} to={n.to} end={n.to === "/"} className="nav-item">
            <Icon name={n.icon} size={17} />
            <span>{n.label}</span>
          </NavLink>
        ))}
        <div className="spacer" />
        <div className="sidebar-foot">
          <div className="user-card">
            <span className="av">{initials(user.name)}</span>
            <div>
              <b>{user.name}</b>
              <small>{roleLabel(user.role)}</small>
            </div>
          </div>
          <button className="btn subtle sm" onClick={signOut}>
            <Icon name="logout" />
            Cerrar sesión
          </button>
        </div>
      </aside>
      <div className="main">
        <Outlet />
      </div>
    </div>
  );
}

export function Topbar({ title, subtitle, right }: { title: string; subtitle?: string; right?: ReactNode }) {
  return (
    <header className="topbar">
      <div>
        <h1>{title}</h1>
        {subtitle && <div className="sub">{subtitle}</div>}
      </div>
      <div className="push">
        {right}
        <ThemeToggle />
      </div>
    </header>
  );
}

export function Page({ children }: { children: ReactNode }) {
  return (
    <div className="content">
      <div className="wrap">{children}</div>
    </div>
  );
}
