import React, { useEffect, useState } from "react";
import client from "../api/client";
import { getErrorMessage } from "../utils/getErrorMessage";
import { useToast } from "../components/ToastProvider";
import { APP_EVENTS, emitAppEvent } from "../common/events";

export default function NotificationsPage() {
    const toast = useToast();

    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(20);
    const [data, setData] = useState(null); // Spring Page

    const fetchData = async (p = page, s = size) => {
        setLoading(true);
        try {
            const res = await client.get("/api/notifications", { params: { page: p, size: s } });
            setData(res?.data?.data ?? res?.data);
        } catch (e) {
            toast.error(getErrorMessage(e));
        } finally {
            setLoading(false);
        }
    };

    const markRead = async (id) => {
        setLoading(true);
        try {
            await client.post(`/api/notifications/${id}/read`);
            toast.success("읽음 처리 완료");

            // ✅ 목록 갱신
            await fetchData(page, size);

            // ✅ NavBar unread 즉시 갱신
            emitAppEvent(APP_EVENTS.NOTIFICATIONS_CHANGED);
        } catch (e) {
            toast.error(getErrorMessage(e));
        } finally {
            setLoading(false);
        }
    };

    const markAllRead = async () => {
        if (!window.confirm("전체 알림을 읽음 처리할까요?")) return;
        setLoading(true);
        try {
            await client.post("/api/notifications/read-all");
            toast.success("전체 읽음 처리 완료");

            await fetchData(0, size);
            setPage(0);

            emitAppEvent(APP_EVENTS.NOTIFICATIONS_CHANGED);
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
            <h2 style={{ marginBottom: 12 }}>알림</h2>

            <div style={{ display: "flex", gap: 10, alignItems: "center", marginBottom: 12, flexWrap: "wrap" }}>
                <button onClick={() => fetchData(curPage, size)} disabled={loading}>
                    새로고침
                </button>

                <button onClick={markAllRead} disabled={loading}>
                    전체 읽음
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
                    <th style={th}>status</th>
                    <th style={th}>type</th>
                    <th style={th}>message</th>
                    <th style={th}>createdAt</th>
                    <th style={th}>action</th>
                </tr>
                </thead>
                <tbody>
                {content.length === 0 && (
                    <tr>
                        <td style={td} colSpan={5}>
                            알림이 없습니다.
                        </td>
                    </tr>
                )}

                {content.map((n) => (
                    <tr key={n.id}>
                        <td style={td}>
                            <b style={{ color: n.status === "UNREAD" ? "#a40000" : "#666" }}>{n.status}</b>
                        </td>
                        <td style={td}>{n.type}</td>
                        <td style={td}>{n.message}</td>
                        <td style={td}>{n.createdAt ? String(n.createdAt).replace("T", " ") : "-"}</td>
                        <td style={td}>
                            {n.status === "UNREAD" ? (
                                <button onClick={() => markRead(n.id)} disabled={loading}>
                                    읽음
                                </button>
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