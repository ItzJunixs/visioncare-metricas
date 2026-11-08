import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { Icon } from "./lib";

type Mode = "light" | "dark" | "system";
const KEY = "visioncare.theme";

function readMode(): Mode {
  const v = localStorage.getItem(KEY);
  return v === "light" || v === "dark" ? v : "system";
}

function apply(mode: Mode) {
  const root = document.documentElement;
  if (mode === "system") root.removeAttribute("data-theme");
  else root.setAttribute("data-theme", mode);
}

interface ThemeContextValue {
  mode: Mode;
  effective: "light" | "dark";
  toggle: () => void;
}
const ThemeContext = createContext<ThemeContextValue | null>(null);

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [mode, setMode] = useState<Mode>(() => readMode());

  useEffect(() => {
    apply(mode);
  }, [mode]);

  const effective: "light" | "dark" =
    mode === "system"
      ? window.matchMedia("(prefers-color-scheme: dark)").matches
        ? "dark"
        : "light"
      : mode;

  const toggle = useCallback(() => {
    setMode((m) => {
      const next: Mode = (m === "system" ? effective : m) === "dark" ? "light" : "dark";
      localStorage.setItem(KEY, next);
      return next;
    });
  }, [effective]);

  const value = useMemo(() => ({ mode, effective, toggle }), [mode, effective, toggle]);
  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}

export function useTheme(): ThemeContextValue {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error("useTheme used outside ThemeProvider");
  return ctx;
}

export function ThemeToggle() {
  const { effective, toggle } = useTheme();
  return (
    <button
      className="icon-btn"
      onClick={toggle}
      title={effective === "dark" ? "Cambiar a modo claro" : "Cambiar a modo oscuro"}
      aria-label="Cambiar tema"
    >
      <Icon name={effective === "dark" ? "sun" : "moon"} />
    </button>
  );
}
