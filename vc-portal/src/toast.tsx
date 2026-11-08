import { createContext, useCallback, useContext, useState, type ReactNode } from "react";
import { createPortal } from "react-dom";
import { Icon } from "./lib";

type Kind = "ok" | "err";
interface Item {
  id: number;
  message: string;
  kind: Kind;
}

const ToastContext = createContext<(message: string, kind?: Kind) => void>(() => {});

export function ToastProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<Item[]>([]);

  const push = useCallback((message: string, kind: Kind = "ok") => {
    const id = Date.now() + Math.random();
    setItems((xs) => [...xs, { id, message, kind }]);
    window.setTimeout(() => setItems((xs) => xs.filter((x) => x.id !== id)), 3400);
  }, []);

  return (
    <ToastContext.Provider value={push}>
      {children}
      {createPortal(
        <div id="toasts">
          {items.map((t) => (
            <div key={t.id} className={"toast " + t.kind}>
              <Icon name={t.kind === "err" ? "alert" : "check"} />
              <span>{t.message}</span>
            </div>
          ))}
        </div>,
        document.body,
      )}
    </ToastContext.Provider>
  );
}

export function useToast() {
  return useContext(ToastContext);
}
