import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { rentalDetail, returnConfirm } from "../api/rentals";
import { requestExtension } from "../api/extensions";
import { useAuth } from "../auth/AuthContext";

export default function RentalDetailPage() {
    const { id } = useParams();
    const { user } = useAuth();
    const role = user?.role;

    const [data, setData] = useState(null);
    const [msg, setMsg] = useState("");
    const [err, setErr] = useState("");

    // ✅ 연장 요청 폼(대여 상세에서 바로)
    const [requestedEndDate, setRequestedEndDate] = useState("");
    const [reason, setReason] = useState("업무 일정 연장");

    const canConfirm = role === "ASSET_ADMIN" || role === "SUPER_ADMIN";

    const load = async () => {
        setErr("");
        const d = await rentalDetail(id);
        setData(d);

        // 기본값: 현재 endDate + 1일 (표시만)
        if (d?.endDate) {
            // YYYY-MM-DD
            const base = new Date(d.endDate + "T00:00:00");
            base.setDate(base.getDate() + 1);
            const y = base.getFullYear();
            const m = String(base.getMonth() + 1).padStart(2, "0");
            const day = String(base.getDate()).padStart(2, "0");
            setRequestedEndDate(`${y}-${m}-${day}`);
        }
    };

    useEffect(() => {
        load();
    }, [id]);

    const onConfirm = async () => {
        setErr(""); setMsg("");
        try {
            await returnConfirm(id);
            setMsg("반납 확인 완료");
            await load();
        } catch (e2) {
            setErr("반납 확인 실패(권한 확인)");
            console.error(e2);
        }
    };

    const onRequestExtension = async () => {
        setErr(""); setMsg("");
        try {
            const extId = await requestExtension({
                rentalId: Number(id),
                requestedEndDate,
                reason
            });
            setMsg(`연장 요청 완료! extensionRequestId=${extId}`);
        } catch (e2) {
            setErr("연장 요청 실패(정책/상태/중복요청/서버 로그 확인)");
            console.error(e2);
        }
    };

    return (
        <div style={{ padding: 16 }}>
            <h2>Rental Detail #{id}</h2>

            {msg ? <div style={{ marginBottom: 10, color: "green" }}>{msg}</div> : null}
            {err ? <div style={{ marginBottom: 10, color: "crimson" }}>{err}</div> : null}

            {!data ? (
                <div>Loading...</div>
            ) : (
                <>
                    <div style={{ display: "grid", gap: 6, marginBottom: 12 }}>
                        <div><b>Status:</b> {data.status}</div>
                        <div><b>Renter:</b> {data.renterEmail}</div>
                        <div><b>Period:</b> {data.startDate} ~ {data.endDate}</div>
                        <div><b>IssuedAt:</b> {String(data.issuedAt).replace("T", " ")}</div>
                        <div><b>ReturnedAt:</b> {data.returnedAt ? String(data.returnedAt).replace("T"," ") : "-"}</div>
                    </div>

                    <h3>Items</h3>
                    <ul>
                        {data.items.map((it) => (
                            <li key={it.assetId}>
                                #{it.assetId} / {it.assetCode} / {it.name} / <b>{it.status}</b>
                            </li>
                        ))}
                    </ul>

                    {/* ✅ 연장 요청 UI */}
                    <div style={{ border: "1px solid #ddd", borderRadius: 12, padding: 12, marginTop: 16 }}>
                        <h3 style={{ marginTop: 0 }}>Request Extension</h3>
                        <div style={{ display: "flex", gap: 8, flexWrap: "wrap", alignItems: "center" }}>
                            <label>
                                New End Date{" "}
                                <input
                                    type="date"
                                    value={requestedEndDate}
                                    onChange={(e) => setRequestedEndDate(e.target.value)}
                                />
                            </label>
                            <input
                                value={reason}
                                onChange={(e) => setReason(e.target.value)}
                                placeholder="reason"
                                style={{ width: 320 }}
                            />
                            <button onClick={onRequestExtension}>Request</button>
                        </div>
                        <div style={{ color: "#666", fontSize: 12, marginTop: 6 }}>
                            * 정책(maxExtensions, maxRentalDays) 및 상태(RETURNED 불가)에 따라 실패할 수 있음.
                        </div>
                    </div>

                    {/* ✅ 관리자 반납 확인 */}
                    {canConfirm ? (
                        <button onClick={onConfirm} style={{ marginTop: 16 }}>
                            Return Confirm (Admin)
                        </button>
                    ) : null}
                </>
            )}
        </div>
    );
}