import axios from "axios";

export const api = axios.create({
    baseURL: "http://localhost:8080",
    withCredentials: false,
});

export function setAuthToken(token) {
    if (token) api.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    else delete api.defaults.headers.common["Authorization"];
}

// 인터셉터: 401이면 토큰 제거(로그아웃 유도)
api.interceptors.response.use(
    (res) => res,
    (err) => {
        const status = err?.response?.status;
        if (status === 401) {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            // 강제 이동(간단 처리)
            if (window.location.pathname !== "/login") window.location.href = "/login";
        }
        return Promise.reject(err);
    }
);

export default api;