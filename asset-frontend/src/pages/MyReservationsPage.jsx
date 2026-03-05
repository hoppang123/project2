import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import client from "../api/client";
import { getErrorMessage } from "../utils/getErrorMessage";
import { useToast } from "../components/ToastProvider";

export default function MyReservationsPage() {
    const navigate = useNavigate();
    const toast = useToast();

    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(20);
    const [data, setData] = useState(null); // Page

    const fetchData = async (p = page, s = size) => {
        setLoading(true);
        try {
            const res = await client.get("/api/reservations/me", { params: { page: p, size: s } });
            setData(res?.data?.data ?? res?.data);
        } catch (e) {
            toast.error(getErrorMessage(e));
        } finally {
            setLoading(false);
        }
    };

    const cancel = async (reservationId) => {
        if (!window.confirm("예약을 취소할까요?")) return;
        setLoading(true);
        try {
            await client.post(`/api/reservations/${reservationId}/cancel`);
            toast.success("취소 완료");
            await fetchData(page, size);
        } catch (e) {
            toast.error(getErrorMessage(e));
        } finally {
            setLoading(false);
        }
    };

    const checkout = async (reservationId) => {
        if (!window.confirm("예약을 대여로 전환(체크아웃)할까요?")) return;
        setLoading(true);
        try {
            const res = await client.post(`/api/reservations/${reservationId}/checkout`);
            const rentalId = res?.data?.data ?? res?.data; // ApiResponse<Long>
            toast.success("대여로 전환 완료");
            navigate(`/rentals/${rentalId}`);
        } catch (e) {
            toast.error(getErrorMessage(e));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData(0, size);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const content = data?.content ?? [];
    const curPage = data?.number ?? page;
    const totalPages = data?.totalPages ?? 0;

    return (
        <div style={{ padding: 16 }}>
            <h2 style={{ marginBottom: 12 }}>내 예약</h2>

            <div style={{ display: "flex", gap: 10, alignItems: "center", marginBottom: 12, flexWrap: "wrap" }}>
                <button onClick={() => fetchData(curPage, size)} disabled={loading}>
                    새로고침
                </button>

                <span>size</span>
                <select
                    value={size}
                    onChange={(e) => {
                        const s = Number(e.target.value);
                        setSize(s);
                        setPage(0);
                        fetchData(0, s);
                    }}
                    disabled={loading}
                >
                    <option value={10}>10</option>
                    <option value={20}>20</option>
                    <option value={50}>50</option>
                </select>

                {loading && <span style={{ color: "#666" }}>Loading...</span>}
            </div>

            <table style={{ width: "100%", borderCollapse: "collapse", background: "white" }}>
                <thead>
                <tr>
                    <th style={th}>id</th>
                    <th style={th}>asset</th>
                    <th style={th}>start</th>
                    <th style={th}>end</th>
                    <th style={th}>status</th>
                    <th style={th}>createdAt</th>
                    <th style={th}>actions</th>
                </tr>
                </thead>
                <tbody>
                {content.length === 0 && (
                    <tr>
                        <td style={td} colSpan={7}>
                            예약이 없습니다.
                        </td>
                    </tr>
                )}

                {content.map((r) => (
                    <tr key={r.id}>
                        <td style={td}>{r.id}</td>
                        <td style={td}>
                            {r.assetName ? `${r.assetName} (${r.assetCode ?? ""})` : (r.assetId ?? "-")}
                        </td>
                        <td style={td}>{r.startDate}</td>
                        <td style={td}>{r.endDate}</td>
                        <td style={td}>{r.status}</td>
                        <td style={td}>{r.createdAt ?? "-"}</td>
                        <td style={td}>
                            {r.status === "RESERVED" ? (
                                <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                                    <button onClick={() => checkout(r.id)} disabled={loading}>
                                        Checkout
                                    </button>
                                    <button onClick={() => cancel(r.id)} disabled={loading}>
                                        Cancel
                                    </button>
                                </div>
                            ) : (
                                <span style={{ color: "#999" }}>-</span>
                            )}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>

            <div style={{ marginTop: 12, display: "flex", gap: 8, alignItems: "center" }}>
                <button
                    onClick={() => {
                        const next = Math.max(0, curPage - 1);
                        setPage(next);
                        fetchData(next, size);
                    }}
                    disabled={loading || curPage === 0}
                >
                    이전
                </button>

                <span>
                    page: {curPage} / {totalPages === 0 ? "?" : totalPages - 1}
                </span>

                <button
                    onClick={() => {
                        const next = curPage + 1;
                        setPage(next);
                        fetchData(next, size);
                    }}
                    disabled={loading || (totalPages !== 0 && curPage + 1 >= totalPages)}
                >
                    다음
                </button>
            </div>
        </div>
    );
}

const th = { textAlign: "left", borderBottom: "1px solid #ddd", padding: "10px 8px", fontSize: 13 };
const td = { borderBottom: "1px solid #eee", padding: "10px 8px", fontSize: 13 };