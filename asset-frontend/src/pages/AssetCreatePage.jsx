import React, { useEffect, useState } from "react";
import { listCategories } from "../api/categories";
import { createAsset } from "../api/assets";
import { useAuth } from "../auth/AuthContext";
import { getErrorMessage } from "../utils/error";
import Toast from "../components/Toast";
import LoadingOverlay from "../components/LoadingOverlay";

export default function AssetCreatePage() {
    const { user } = useAuth();
    const role = user?.role;
    const isAdmin = role === "ASSET_ADMIN" || role === "SUPER_ADMIN";

    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(false);
    const [toast, setToast] = useState({ open: false, type: "success", message: "" });

    const showSuccess = (message) => setToast({ open: true, type: "success", message });
    const showError = (message) => setToast({ open: true, type: "error", message });

    const [categoryId, setCategoryId] = useState("");
    const [assetCode, setAssetCode] = useState("");
    const [serialNo, setSerialNo] = useState("");
    const [name, setName] = useState("");
    const [location, setLocation] = useState("");
    const [purchaseDate, setPurchaseDate] = useState("");
    const [price, setPrice] = useState("");

    useEffect(() => {
        setLoading(true);
        listCategories()
            .then((data) => setCategories(data))
            .catch((e) => {
                console.error(e);
                showError(getErrorMessage(e, "카테고리 로드 실패"));
            })
            .finally(() => setLoading(false));
    }, []);

    const onSubmit = async (e) => {
        e.preventDefault();

        if (!isAdmin) {
            showError("자산 등록은 ASSET_ADMIN 이상만 가능합니다.");
            return;
        }
        if (!categoryId || !assetCode.trim() || !name.trim()) {
            showError("Category / Asset Code / Name은 필수야");
            return;
        }

        setLoading(true);
        try {
            const payload = {
                categoryId: Number(categoryId),
                assetCode: assetCode.trim(),
                serialNo: serialNo.trim() ? serialNo.trim() : null,
                name: name.trim(),
                location: location.trim() ? location.trim() : null,
                purchaseDate: purchaseDate || null,
                price: price ? Number(price) : null,
                ownerDeptId: null,
                managerUserId: null,
            };

            const id = await createAsset(payload);
            showSuccess(`등록 완료! assetId=${id}`);

            setAssetCode("");
            setSerialNo("");
            setName("");
            setLocation("");
            setPurchaseDate("");
            setPrice("");
        } catch (e2) {
            console.error(e2);
            showError(getErrorMessage(e2, "등록 실패"));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ padding: 16 }}>
            <h2>Create Asset</h2>

            {!isAdmin ? (
                <div style={{ color: "crimson", marginBottom: 10 }}>
                    현재 권한({role})으로는 등록할 수 없음
                </div>
            ) : null}

            <form onSubmit={onSubmit} style={{ display: "grid", gap: 10, maxWidth: 520, marginTop: 12 }}>
                <label>
                    Category
                    <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
                        <option value="">선택</option>
                        {categories.map((c) => (
                            <option key={c.id} value={c.id}>
                                {c.name} (id={c.id})
                            </option>
                        ))}
                    </select>
                </label>

                <label>
                    Asset Code (unique) *
                    <input value={assetCode} onChange={(e) => setAssetCode(e.target.value)} placeholder="예: LAP-003" />
                </label>

                <label>
                    Serial No (optional, unique)
                    <input value={serialNo} onChange={(e) => setSerialNo(e.target.value)} placeholder="예: SN-LAP-003" />
                </label>

                <label>
                    Name *
                    <input value={name} onChange={(e) => setName(e.target.value)} placeholder="예: MacBook Air" />
                </label>

                <label>
                    Location
                    <input value={location} onChange={(e) => setLocation(e.target.value)} placeholder="예: HQ 3F" />
                </label>

                <label>
                    Purchase Date
                    <input type="date" value={purchaseDate} onChange={(e) => setPurchaseDate(e.target.value)} />
                </label>

                <label>
                    Price
                    <input value={price} onChange={(e) => setPrice(e.target.value)} placeholder="예: 1500000" />
                </label>

                <button type="submit" disabled={!isAdmin || loading}>
                    Create
                </button>
            </form>

            <LoadingOverlay open={loading} text="Saving..." />
            <Toast
                open={toast.open}
                type={toast.type}
                message={toast.message}
                onClose={() => setToast((t) => ({ ...t, open: false }))}
                durationMs={2500}
            />
        </div>
    );
}