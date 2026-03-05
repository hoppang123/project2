import { api } from "./client";

export async function loginApi(email, password) {
    const res = await api.post("/api/auth/login", { email, password });
    return res.data.data; // ApiResponse<T> 구조 가정
}

export async function meApi() {
    const res = await api.get("/api/auth/me");
    return res.data.data;
}
