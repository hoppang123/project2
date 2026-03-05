export function getErrorMessage(err, fallback = "요청 실패") {
    // axios error 형태 대응
    const res = err?.response;
    const data = res?.data;

    // 백엔드가 ApiResponse 형태라면 message를 우선 사용
    const msg =
        data?.message ||
        data?.error?.message ||
        data?.errorMessage ||
        data?.detail;

    if (msg) return msg;

    // 상태코드 기반 기본 메시지
    const status = res?.status;
    if (status === 401) return "인증이 필요합니다(로그인 만료)";
    if (status === 403) return "권한이 없습니다";
    if (status === 404) return "대상을 찾을 수 없습니다";
    if (status >= 500) return "서버 오류가 발생했습니다";

    return fallback;
}