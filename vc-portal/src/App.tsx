import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./auth";
import { Layout, RequireAuth } from "./components/Layout";
import type { Role } from "./api";
import { Login } from "./screens/Login";
import { Dashboard } from "./screens/Dashboard";
import { Patients } from "./screens/Patients";
import { PatientNew } from "./screens/PatientNew";
import { PatientDetail } from "./screens/PatientDetail";
import { Inventory } from "./screens/Inventory";
import { FrameNew } from "./screens/FrameNew";
import { WorkOrders } from "./screens/WorkOrders";
import { WorkOrderNew } from "./screens/WorkOrderNew";
import { WorkOrderDetail } from "./screens/WorkOrderDetail";
import { Invoices } from "./screens/Invoices";
import { InvoiceDetail } from "./screens/InvoiceDetail";

function RequireRole({ roles, children }: { roles: Role[]; children: React.ReactNode }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (!roles.includes(user.role)) return <Navigate to="/" replace />;
  return <>{children}</>;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        element={
          <RequireAuth>
            <Layout />
          </RequireAuth>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="patients" element={<Patients />} />
        <Route
          path="patients/new"
          element={
            <RequireRole roles={["ADMIN", "SELLER", "OPTOMETRIST"]}>
              <PatientNew />
            </RequireRole>
          }
        />
        <Route
          path="patients/:id/edit"
          element={
            <RequireRole roles={["ADMIN", "SELLER", "OPTOMETRIST"]}>
              <PatientNew />
            </RequireRole>
          }
        />
        <Route path="patients/:id" element={<PatientDetail />} />

        <Route
          path="inventory"
          element={
            <RequireRole roles={["ADMIN", "SELLER"]}>
              <Inventory />
            </RequireRole>
          }
        />
        <Route
          path="inventory/new"
          element={
            <RequireRole roles={["ADMIN"]}>
              <FrameNew />
            </RequireRole>
          }
        />

        <Route
          path="inventory/:id/edit"
          element={
            <RequireRole roles={["ADMIN"]}>
              <FrameNew />
            </RequireRole>
          }
        />

        <Route
          path="work-orders"
          element={
            <RequireRole roles={["ADMIN", "SELLER"]}>
              <WorkOrders />
            </RequireRole>
          }
        />
        <Route
          path="work-orders/new"
          element={
            <RequireRole roles={["ADMIN", "SELLER"]}>
              <WorkOrderNew />
            </RequireRole>
          }
        />
        <Route
          path="work-orders/:id"
          element={
            <RequireRole roles={["ADMIN", "SELLER"]}>
              <WorkOrderDetail />
            </RequireRole>
          }
        />

        <Route
          path="invoices"
          element={
            <RequireRole roles={["ADMIN", "SELLER"]}>
              <Invoices />
            </RequireRole>
          }
        />
        <Route
          path="invoices/:id"
          element={
            <RequireRole roles={["ADMIN", "SELLER"]}>
              <InvoiceDetail />
            </RequireRole>
          }
        />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
