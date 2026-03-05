import React, { createContext, useContext, useEffect, useMemo, useState } from "react";
import { setAuthToken } from "../api/client";
import { loginApi, meApi } from "../api/auth";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null); // {id,email,name,role}
    const [loading, setLoading] = useState(true);

    // 앱 시작 시 토큰 있으면 적용 + me 호출
    useEffect(() => {
        const token = localStorage.getItem("accessToken");
        if (!token) {
            setLoading(false);
            return;
        }
        setAuthToken(token);
        meApi()
            .then((u) => setUser(u))
            .catch(() => {
                localStorage.removeItem("accessToken");
                localStorage.removeItem("refreshToken");
                setAuthToken(null);
            })
            .finally(() => setLoading(false));
    }, []);

    const value = useMemo(() => ({
        user,
        loading,
        async login(email, password) {
            const tokens = await loginApi(email, password);
            localStorage.setItem("accessToken", tokens.accessToken);
            localStorage.setItem("refreshToken", tokens.refreshToken);
            setAuthToken(tokens.accessToken);
            const u = await meApi();
            setUser(u);
            return u;
        },
        logout() {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            setAuthToken(null);
            setUser(null);
            window.location.href = "/login";
        }
    }), [user, loading]);

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    return useContext(AuthContext);
}
