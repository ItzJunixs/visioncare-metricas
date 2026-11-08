import type { ReactNode } from "react";

/** Control de formulario con etiqueta y error en línea. El copy de la UI está en español. */
export function Field({
  label,
  required,
  error,
  children,
}: {
  label: string;
  required?: boolean;
  error?: string;
  children: ReactNode;
}) {
  return (
    <div className={"field" + (error ? " invalid" : "")}>
      <label>
        {label} {required && <span className="req">*</span>}
      </label>
      {children}
      {error && <span className="err">{error}</span>}
    </div>
  );
}
