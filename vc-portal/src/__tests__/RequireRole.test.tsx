import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { AuthProvider } from "../auth";
import { RequireAuth } from "../components/Layout";
import { saveSession } from "../api";

function renderAt(path: string) {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/login" element={<p>login screen</p>} />
          <Route
            path="/secret"
            element={
              <RequireAuth>
                <p>private screen</p>
              </RequireAuth>
            }
          />
        </Routes>
      </MemoryRouter>
    </AuthProvider>,
  );
}

describe("route guard", () => {
  it("redirects to /login without a session", () => {
    localStorage.clear();
    renderAt("/secret");
    expect(screen.getByText("login screen")).toBeInTheDocument();
  });

  it("lets a signed-in user through", () => {
    saveSession({ token: "t", username: "admin", name: "Admin", role: "ADMIN" });
    renderAt("/secret");
    expect(screen.getByText("private screen")).toBeInTheDocument();
  });
});
