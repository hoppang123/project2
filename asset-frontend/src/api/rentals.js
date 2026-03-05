import { api } from "./client";

export async function createRentalRequest(payload) {
    const res = await api.post("/api/rentals/requests", payload);
    return res.data.data; // requestId
}

export async function myRequests(page = 0, size = 10) {
    const res = await api.get("/api/rentals/requests/mine", { params: { page, size } });
    return res.data.data;
}

export async function requestDetail(id) {
    const res = await api.get(`/api/rentals/requests/${id}`);
    return res.data.data;
}

export async function inbox(page = 0, size = 10) {
    const res = await api.get("/api/rentals/requests/inbox", { params: { page, size } });
    return res.data.data;
}

export async function approveRequest(id, note) {
    const res = await api.post(`/api/rentals/requests/${id}/approve`, { note });
    return res.data;
}

export async function rejectRequest(id, reason) {
    const res = await api.post(`/api/rentals/requests/${id}/reject`, { reason });
    return res.data;
}

export async function cancelRequest(id) {
    const res = await api.post(`/api/rentals/requests/${id}/cancel`);
    return res.data;
}

export async function myRentals(page = 0, size = 10) {
    const res = await api.get("/api/rentals/mine", { params: { page, size } });
    return res.data.data;
}

export async function rentalDetail(rentalId) {
    const res = await api.get(`/api/rentals/${rentalId}`);
    return res.data.data;
}

export async function returnRequest(rentalId) {
    const res = await api.post(`/api/rentals/${rentalId}/return-request`);
    return res.data;
}

export async function returnConfirm(rentalId) {
    const res = await api.post(`/api/rentals/${rentalId}/return-confirm`);
    return res.data;
}

export async function inboxV2(page = 0, size = 10) {
    const res = await api.get("/api/rentals/requests/inbox-v2", { params: { page, size }});
    return res.data.data;
}