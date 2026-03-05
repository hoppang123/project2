import React, { useState } from "react";
import { useAuth } from "../auth/AuthContext";

export default function LoginPage() {
    const { login } = useAuth();
    const [email, setEmail] = useState("admin@company.com");
    const [password, setPassword] = useState("1234");
    const [err, setErr] = useState("");

    const onSubmit = async (e) => {
        e.preventDefault();
        setErr("");
        try {
            await login(email, password);
            window.location.href = "/";
        } catch (e2) {
            setErr("Login failed");
        }
    };

    return (
        <div style={{ maxWidth: 360, margin: "60px auto", padding: 16, border: "1px solid #ddd" }}>
            <h3>Login</h3>
            <form onSubmit={onSubmit} style={{ display: "grid", gap: 10 }}>
                <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="email" />
                <input value={password} onChange={(e) => setPassword(e.target.value)} placeholder="password" type="password" />
                <button type="submit">Login</button>
                {err ? <div style={{ color: "crimson" }}>{err}</div> : null}
            </form>

            <div style={{ marginTop: 12, fontSize: 12, color: "#666" }}>
                테스트 계정: admin/asset/manager/user @company.com / 1234
            </div>
        </div>
    );
}
