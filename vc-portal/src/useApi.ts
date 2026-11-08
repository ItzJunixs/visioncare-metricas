import { useCallback, useEffect, useState } from "react";

interface State<T> {
  data: T | null;
  error: string | null;
  loading: boolean;
}

/** Ejecuta una promesa y expone data/error/loading, con recarga manual. */
export function useAsync<T>(fn: () => Promise<T>, deps: unknown[] = []): State<T> & { reload: () => void } {
  const [state, setState] = useState<State<T>>({ data: null, error: null, loading: true });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  const run = useCallback(fn, deps);

  const load = useCallback(() => {
    let vivo = true;
    setState((s) => ({ ...s, loading: true, error: null }));
    run()
      .then((data) => vivo && setState({ data, error: null, loading: false }))
      .catch((e: Error) => vivo && setState({ data: null, error: e.message, loading: false }));
    return () => {
      vivo = false;
    };
  }, [run]);

  useEffect(load, [load]);

  return { ...state, reload: load };
}
