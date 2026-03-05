import { api } from "./client";

export async function searchAssets(params) {
    const res = await api.get("/api/assets", { params });
    return res.data.data; // Page<AssetResponse>
}

export async function getAsset(id) {
    const res = await api.get(`/api/assets/${id}`);
    return res.data.data;
}

export async function createAsset(payload) {
    const res = await api.post("/api/assets", payload);
    return res.data.data; // assetId
}

export async function assetHistories(assetId, page = 0, size = 10) {
    const res = await api.get(`/api/assets/${assetId}/histories`, { params: { page, size } });
    return res.data.data;
}

export async function changeAssetStatus(assetId, payload) {
    const res = await api.patch(`/api/assets/${assetId}/status`, payload);
    return res.data;
}

export async function updateAsset(assetId, payload) {
    const res = await api.put(`/api/assets/${assetId}`, payload);
    return res.data;
}