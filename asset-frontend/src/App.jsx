import React from "react";
import { BrowserRouter, Routes, Route, Outlet, Navigate } from "react-router-dom";

import { AuthProvider } from "./auth/AuthContext";
import ProtectedRoute from "./auth/ProtectedRoute";
import RoleProtectedRoute from "./auth/RoleProtectedRoute";

import NavBar from "./components/NavBar";

import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";

import AssetsPage from "./pages/AssetsPage";
import AssetCreatePage from "./pages/AssetCreatePage";
import AssetDetailPage from "./pages/AssetDetailPage";

import RentalRequestPage from "./pages/RentalRequestPage";
import MyRequestsPage from "./pages/MyRequestsPage";
import RequestDetailPage from "./pages/RequestDetailPage";
import InboxPage from "./pages/InboxPage";

import MyRentalsPage from "./pages/MyRentalsPage";
import RentalDetailPage from "./pages/RentalDetailPage";

import ExtensionsPage from "./pages/ExtensionsPage";
import ExtensionAdminPage from "./pages/ExtensionAdminPage";

import MyReservationsPage from "./pages/MyReservationsPage";
import NotificationsPage from "./pages/NotificationsPage";

import MyRentalHistoryPage from "./pages/MyRentalHistoryPage";
import UserRentalHistoryPage from "./pages/UserRentalHistoryPage";
import LowReturnUsersPage from "./pages/LowReturnUsersPage";

import AdminStatsPage from "./pages/AdminStatsPage";
import MySanctionPage from "./pages/MySanctionPage";

// ✅ Protected 영역 레이아웃
function ProtectedLayout() {
  return (
    <ProtectedRoute>
      <NavBar />
      <Outlet />
    </ProtectedRoute>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public */}
          <Route path="/login" element={<LoginPage />} />

          {/* Protected */}
          <Route element={<ProtectedLayout />}>
            <Route path="/" element={<DashboardPage />} />

            {/* Assets */}
            <Route path="/assets" element={<AssetsPage />} />
            <Route
              path="/assets/new"
              element={
                <RoleProtectedRoute allowRoles={["ASSET_ADMIN", "SUPER_ADMIN"]}>
                  <AssetCreatePage />
                </RoleProtectedRoute>
              }
            />
            <Route path="/assets/:id" element={<AssetDetailPage />} />

            {/* Rentals */}
            <Route path="/rentals/request" element={<RentalRequestPage />} />
            <Route path="/rentals/requests" element={<MyRequestsPage />} />
            <Route path="/rentals/requests/:id" element={<RequestDetailPage />} />
            <Route
              path="/rentals/inbox"
              element={
                <RoleProtectedRoute allowRoles={["MANAGER", "ASSET_ADMIN", "SUPER_ADMIN"]}>
                  <InboxPage />
                </RoleProtectedRoute>
              }
            />
            <Route path="/rentals" element={<MyRentalsPage />} />
            <Route path="/rentals/:id" element={<RentalDetailPage />} />

            {/* Extensions */}
            <Route path="/extensions" element={<ExtensionsPage />} />
            <Route
              path="/extensions/admin"
              element={
                <RoleProtectedRoute allowRoles={["ASSET_ADMIN", "SUPER_ADMIN"]}>
                  <ExtensionAdminPage />
                </RoleProtectedRoute>
              }
            />

            {/* Reservations */}
            <Route path="/reservations" element={<MyReservationsPage />} />

            {/* Notifications */}
            <Route path="/notifications" element={<NotificationsPage />} />

            {/* My pages */}
            <Route path="/me/rentals" element={<MyRentalHistoryPage />} />
            <Route path="/me/sanction" element={<MySanctionPage />} />

            {/* Admin: risk */}
            <Route
              path="/admin/risk/low-return"
              element={
                <RoleProtectedRoute allowRoles={["ASSET_ADMIN", "SUPER_ADMIN"]}>
                  <LowReturnUsersPage />
                </RoleProtectedRoute>
              }
            />

            {/* Admin: user rentals */}
            <Route
              path="/admin/users/:userId/rentals"
              element={
                <RoleProtectedRoute allowRoles={["ASSET_ADMIN", "SUPER_ADMIN"]}>
                  <UserRentalHistoryPage />
                </RoleProtectedRoute>
              }
            />

            {/* Admin: stats */}
            <Route
              path="/admin/stats"
              element={
                <RoleProtectedRoute allowRoles={["ASSET_ADMIN", "SUPER_ADMIN"]}>
                  <AdminStatsPage />
                </RoleProtectedRoute>
              }
            />
          </Route>

          {/* 없는 경로는 대시보드로 */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}