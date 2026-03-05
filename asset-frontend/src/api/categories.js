import { api } from "./client";

export async function listCategories() {
    const res = await api.get("/api/asset-categories");
    return res.data.data; // [{id,name}]
}