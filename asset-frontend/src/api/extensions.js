import { api } from "./client";

export async function requestExtension(payload) {
    const res = await api.post("/api/extensions", payload);
    return res.data.data; // extensionRequestId
}

export async function myExtensionRequests(page = 0, size = 10) {
    const res = await api.get("/api/extensions/mine", { params: { page, size }});
    return res.data.data;
}

export async function pendingExtensions(page = 0, size = 10) {
    const res = await api.get("/api/extensions/pending", { params: { page, size }});
    return res.data.data;
}

export async function approveExtension(id, adminNote) {
    const res = await api.post(`/api/extensions/${id}/approve`, { adminNote });
    return res.data;
}

export async function rejectExtension(id, adminNote) {
    const res = await api.post(`/api/extensions/${id}/reject`, { adminNote });
    return res.data;
}

export async function cancelExtension(id) {
    const res = await api.post(`/api/extensions/${id}/cancel`);
    return res.data;
}
