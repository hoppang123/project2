import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "./AuthContext";

export default function RoleProtectedRoute({ allowRoles, children }) {
    const { user, loading } = useAuth();

    if (loading) return <div style={{ padding: 16 }}>Loading...</div>;
    if (!user) return <Navigate to="/login" replace />;

    const role = user.role;
    const allowed = allowRoles.includes(role);

    if (!allowed) {
        return (
            <div style={{ padding: 16 }}>
                <h3>403 Forbidden</h3>
                <div>권한이 없어서 접근할 수 없습니다. (role: {role})</div>
            </div>
        );
    }

    return children;
}