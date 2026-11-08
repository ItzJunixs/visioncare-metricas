import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react";
import { api, readSession, saveSession, type Role, type Session } from "./api";

interface AuthContextValue {
  user: Session | null;
  signIn: (username: string, password: string) => Promise<void>;
  signOut: () => void;
  hasRole: (...roles: Role[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<Session | null>(() => readSession());

  const signIn = useCallback(async (username: string, password: string) => {
    const session = await api.login(username, password);
    saveSession(session);
    setUser(session);
  }, []);

  const signOut = useCallback(() => {
    saveSession(null);
    setUser(null);
  }, []);

  const hasRole = useCallback((...roles: Role[]) => !!user && roles.includes(user.role), [user]);

  const value = useMemo(() => ({ user, signIn, signOut, hasRole }), [user, signIn, signOut, hasRole]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth used outside AuthProvider");
  return ctx;
}
