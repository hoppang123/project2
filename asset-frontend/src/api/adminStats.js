import { api } from "./client";

export async function getAdminStats(days = 14) {
  const res = await api.get("/api/admin/stats", { params: { days } });
  return res?.data?.data ?? res?.data;
}