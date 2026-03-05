import { api } from "./client";

export async function dashboardSummary() {
    const res = await api.get("/api/dashboard/summary");
    return res.data.data;
}