import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getAsset, assetHistories, changeAssetStatus, updateAsset } from "../api/assets";
import { listCategories } from "../api/categories";
import { useAuth } from "../auth/AuthContext";
import { reservationsApi } from "../api/reservations";
import { getErrorMessage } from "../utils/getErrorMessage";
import { useToast } from "../components/ToastProvider";

export default function AssetDetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const toast = useToast();

    const { user } = useAuth();
    const role = user?.role;
    const isAdmin = role === "ASSET_ADMIN" || role === "SUPER_ADMIN";

    const [asset, setAsset] = useState(null);
    const [histPage, setHistPage] = useState(null);
    const [histPageNo, setHistPageNo] = useState(0);

    const [categories, setCategories] = useState([]);

    // edit form
    const [editMode, setEditMode] = useState(false);
    const [categoryId, setCategoryId] = useState("");
    const [serialNo, setSerialNo] = useState("");
    const [name, setName] = useState("");
    const [location, setLocation] = useState("");
    const [purchaseDate, setPurchaseDate] = useState("");
    const [price, setPrice] = useState("");

    // status change
    const [newStatus, setNewStatus] = useState("MAINTENANCE");
    const [note, setNote] = useState("");

    const [msg, setMsg] = useState("");
    const [err, setErr] = useState("");

    // ✅ 예약 생성 폼 상태
    const [resStart, setResStart] = useState("");
    const [resEnd, setResEnd] = useState("");
    const [resLoading, setResLoading] = useState(false);

    const loadAsset = async () => {
        const data = await getAsset(id);
        setAsset(data);

        // edit form 초기값
        setCategoryId(String(data.categoryId ?? ""));
        setSerialNo(data.serialNo ?? "");
        setName(data.name ?? "");
        setLocation(data.location ?? "");
        setPurchaseDate(data.purchaseDate ?? "");
        setPrice(data.price != null ? String(data.price) : "");
        setNewStatus(data.status ?? "MAINTENANCE");
    };

    const loadHistories = async (page = 0) => {
        const data = await assetHistories(id, page, 10);
        setHistPage(data);
        setHistPageNo(page);
    };

    const loadCategories = async () => {
        const data = await listCategories();
        setCategories(data);
    };

    const refreshAll = async () => {
        setErr("");
        setMsg("");
        try {
            await Promise.all([loadAsset(), loadHistories(0), loadCategories()]);
        } catch (e) {
            console.error(e);
            const m = getErrorMessage(e);
            setErr(m);
            toast.error(m);
        }
    };

    useEffect(() => {
        refreshAll();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id]);

    const onSaveEdit = async () => {
        setErr("");
        setMsg("");

        if (!isAdmin) {
            const m = "관리자만 수정 가능";
            setErr(m);
            toast.error(m);
            return;
        }
        if (!categoryId || !name.trim()) {
            const m = "Category / Name은 필수";
            setErr(m);
            toast.error(m);
            return;
        }

        try {
            await updateAsset(id, {
                categoryId: Number(categoryId),
                serialNo: serialNo.trim() ? serialNo.trim() : null,
                name: name.trim(),
                location: location.trim() ? location.trim() : null,
                purchaseDate: purchaseDate || null,
                price: price ? Number(price) : null,
                ownerDeptId: null,
                managerUserId: null,
            });

            const m = "수정 완료";
            setMsg(m);
            toast.success(m);

            setEditMode(false);
            await loadAsset();
            await loadHistories(0);
        } catch (e) {
            console.error(e);
            const m = getErrorMessage(e) || "수정 실패(중복 serial/권한/서버 로그 확인)";
            setErr(m);
            toast.error(m);
        }
    };

    const onChangeStatus = async () => {
        setErr("");
        setMsg("");

        if (!isAdmin) {
            const m = "관리자만 상태 변경 가능";
            setErr(m);
            toast.error(m);
            return;
        }

        try {
            await changeAssetStatus(id, { status: newStatus, note: note || null });

            const m = "상태 변경 완료";
            setMsg(m);
            toast.success(m);

            setNote("");
            await loadAsset();
            await loadHistories(0);
        } catch (e) {
            console.error(e);
            const m = getErrorMessage(e) || "상태 변경 실패";
            setErr(m);
            toast.error(m);
        }
    };

    const submitReservation = async () => {
        setErr("");
        setMsg("");

        if (!asset?.id) {
            const m = "자산 정보를 먼저 불러와주세요.";
            setErr(m);
            toast.error(m);
            return;
        }

        // 날짜 입력 검증
        if (!resStart || !resEnd) {
            const m = "예약 시작/종료 날짜를 입력해주세요.";
            setErr(m);
            toast.error(m);
            return;
        }
        if (resEnd < resStart) {
            const m = "종료일은 시작일보다 빠를 수 없습니다.";
            setErr(m);
            toast.error(m);
            return;
        }

        setResLoading(true);
        try {
            const res = await reservationsApi.create({
                assetId: asset.id,
                startDate: resStart,
                endDate: resEnd,
            });

            const reservationId = res?.data?.data ?? res?.data;
            const m = `예약 완료 (id: ${reservationId})`;
            setMsg(m);
            toast.success(m);

            // 입력값 초기화
            setResStart("");
            setResEnd("");

            // 예약 목록으로 이동
            navigate("/reservations");
        } catch (e) {
            console.error(e);
            const m = getErrorMessage(e);
            setErr(m);
            toast.error(m);
        } finally {
            setResLoading(false);
        }
    };

    return (
        <div style={{ padding: 16 }}>
            <h2>Asset Detail #{id}</h2>

            {err ? <div style={{ color: "crimson", marginBottom: 10 }}>{err}</div> : null}
            {msg ? <div style={{ color: "green", marginBottom: 10 }}>{msg}</div> : null}

            {!asset ? (
                <div>Loading...</div>
            ) : (
                <>
                    {/* ===== summary ===== */}
                    <div style={{ display: "grid", gap: 6, marginBottom: 16 }}>
                        <div><b>Code:</b> {asset.assetCode}</div>
                        <div><b>Name:</b> {asset.name}</div>
                        <div><b>Status:</b> <b>{asset.status}</b></div>
                        <div><b>Category:</b> {asset.categoryName ?? "-"}</div>
                        <div><b>Serial:</b> {asset.serialNo ?? "-"}</div>
                        <div><b>Location:</b> {asset.location ?? "-"}</div>
                        <div><b>Price:</b> {asset.price ?? "-"}</div>
                        <div><b>PurchaseDate:</b> {asset.purchaseDate ?? "-"}</div>

                        <div style={{ marginTop: 8, display: "flex", gap: 8, flexWrap: "wrap" }}>
                            <button onClick={refreshAll}>Refresh</button>
                            <button onClick={() => navigate(-1)}>Back</button>
                            {isAdmin && (
                                <button onClick={() => setEditMode((v) => !v)}>
                                    {editMode ? "Close Edit" : "Edit"}
                                </button>
                            )}
                        </div>
                    </div>

                    {/* ✅ 예약 생성 섹션 */}
                    <div style={{ border: "1px solid #ddd", borderRadius: 12, padding: 12, marginBottom: 16 }}>
                        <h3 style={{ marginTop: 0 }}>예약 만들기</h3>

                        <div style={{ display: "flex", gap: 10, flexWrap: "wrap", alignItems: "center" }}>
                            <div style={{ display: "flex", gap: 6, alignItems: "center" }}>
                                <span>Start</span>
                                <input
                                    type="date"
                                    value={resStart}
                                    onChange={(e) => setResStart(e.target.value)}
                                />
                            </div>

                            <div style={{ display: "flex", gap: 6, alignItems: "center" }}>
                                <span>End</span>
                                <input
                                    type="date"
                                    value={resEnd}
                                    onChange={(e) => setResEnd(e.target.value)}
                                />
                            </div>

                            <button onClick={submitReservation} disabled={resLoading}>
                                {resLoading ? "예약 중..." : "예약하기"}
                            </button>
                        </div>

                        <div style={{ marginTop: 8, color: "#666", fontSize: 12 }}>
                            ※ 예약 기간이 기존 예약/대여/정비와 겹치면 실패합니다.
                        </div>
                    </div>

                    {/* ===== edit ===== */}
                    {isAdmin && editMode ? (
                        <div style={{ border: "1px solid #ddd", borderRadius: 12, padding: 12, marginBottom: 16 }}>
                            <h3 style={{ marginTop: 0 }}>Edit Asset</h3>

                            <div style={{ display: "grid", gap: 10, maxWidth: 520 }}>
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
                                    Serial No (unique)
                                    <input value={serialNo} onChange={(e) => setSerialNo(e.target.value)} />
                                </label>

                                <label>
                                    Name *
                                    <input value={name} onChange={(e) => setName(e.target.value)} />
                                </label>

                                <label>
                                    Location
                                    <input value={location} onChange={(e) => setLocation(e.target.value)} />
                                </label>

                                <label>
                                    Purchase Date
                                    <input
                                        type="date"
                                        value={purchaseDate}
                                        onChange={(e) => setPurchaseDate(e.target.value)}
                                    />
                                </label>

                                <label>
                                    Price
                                    <input
                                        value={price}
                                        onChange={(e) => setPrice(e.target.value)}
                                        placeholder="예: 1500000"
                                    />
                                </label>

                                <div style={{ display: "flex", gap: 8 }}>
                                    <button onClick={onSaveEdit}>Save</button>
                                    <button onClick={() => setEditMode(false)}>Cancel</button>
                                </div>
                            </div>
                        </div>
                    ) : null}

                    {/* ===== admin status change ===== */}
                    {isAdmin ? (
                        <div style={{ border: "1px solid #ddd", borderRadius: 12, padding: 12, marginBottom: 16 }}>
                            <h3 style={{ marginTop: 0 }}>Admin: Change Status</h3>
                            <div style={{ display: "flex", gap: 8, flexWrap: "wrap", alignItems: "center" }}>
                                <select value={newStatus} onChange={(e) => setNewStatus(e.target.value)}>
                                    <option value="AVAILABLE">AVAILABLE</option>
                                    <option value="MAINTENANCE">MAINTENANCE</option>
                                    <option value="LOST">LOST</option>
                                    <option value="DISPOSED">DISPOSED</option>
                                    <option value="RESERVED">RESERVED</option>
                                    <option value="RENTED">RENTED</option>
                                </select>

                                <input
                                    value={note}
                                    onChange={(e) => setNote(e.target.value)}
                                    placeholder="note(optional)"
                                    style={{ width: 320 }}
                                />
                                <button onClick={onChangeStatus}>Apply</button>
                            </div>
                            <div style={{ color: "#666", fontSize: 12, marginTop: 6 }}>
                                * 실무에서는 RENTED/RESERVED는 워크플로우로만 바꾸도록 제한하는 게 일반적.
                            </div>
                        </div>
                    ) : null}

                    {/* ===== histories ===== */}
                    <h3>Histories</h3>

                    {!histPage ? (
                        <div>Loading histories...</div>
                    ) : (
                        <>
                            <table border="1" cellPadding="8" style={{ borderCollapse: "collapse", width: "100%" }}>
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>At</th>
                                    <th>Action</th>
                                    <th>Before</th>
                                    <th>After</th>
                                    <th>Actor</th>
                                    <th>Note</th>
                                </tr>
                                </thead>
                                <tbody>
                                {histPage.content.map((h) => (
                                    <tr key={h.id}>
                                        <td>{h.id}</td>
                                        <td>{String(h.createdAt).replace("T", " ")}</td>
                                        <td>{h.action}</td>
                                        <td>{h.beforeStatus ?? "-"}</td>
                                        <td><b>{h.afterStatus ?? "-"}</b></td>
                                        <td>{h.actorEmail ?? "-"}</td>
                                        <td>{h.note ?? "-"}</td>
                                    </tr>
                                ))}
                                {histPage.content.length === 0 ? (
                                    <tr>
                                        <td colSpan="7" style={{ textAlign: "center", color: "#666" }}>
                                            이력이 없습니다.
                                        </td>
                                    </tr>
                                ) : null}
                                </tbody>
                            </table>

                            <div style={{ marginTop: 10, display: "flex", gap: 8, alignItems: "center" }}>
                                <button
                                    disabled={histPage.first}
                                    onClick={() => loadHistories(histPage.number - 1)}
                                >
                                    Prev
                                </button>
                                <div>
                                    Page {histPage.number + 1} / {histPage.totalPages}
                                </div>
                                <button
                                    disabled={histPage.last}
                                    onClick={() => loadHistories(histPage.number + 1)}
                                >
                                    Next
                                </button>
                            </div>

                            {/* (선택) 현재 페이지 표시용 */}
                            {histPageNo != null ? (
                                <div style={{ marginTop: 6, color: "#777", fontSize: 12 }}>
                                    histPageNo: {histPageNo}
                                </div>
                            ) : null}
                        </>
                    )}
                </>
            )}
        </div>
    );
}