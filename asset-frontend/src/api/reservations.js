// src/api/reservations.js
import client from "./client";

export const reservationsApi = {
    // 예약 생성
    create: ({ assetId, startDate, endDate }) =>
        client.post("/api/reservations", { assetId, startDate, endDate }),

    // 내 예약 목록
    myReservations: (page = 0, size = 20) =>
        client.get("/api/reservations/me", { params: { page, size } }),

    // 예약 취소
    cancel: (id) => client.post(`/api/reservations/${id}/cancel`),

    // 예약 → 대여 전환(checkout)
    checkout: (id) => client.post(`/api/reservations/${id}/checkout`),
};