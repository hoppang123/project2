import axios from "axios";

const instance = axios.create({
  baseURL: "http://localhost:8080",
  withCredentials: false,
});

// token 세팅 함수(기존 파일들이 필요하면 사용 가능)
export function setAuthToken(token) {
  if (token) instance.defaults.headers.common["Authorization"] = `Bearer ${token}`;
  else delete instance.defaults.headers.common["Authorization"];
}

// 401 처리(너 client.js와 동일)
instance.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err?.response?.status;
    if (status === 401) {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      if (window.location.pathname !== "/login") window.location.href = "/login";
    }
    return Promise.reject(err);
  }
);

export default instance;