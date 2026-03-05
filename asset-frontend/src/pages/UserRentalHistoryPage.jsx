import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import axios from "../api/axios";
import { getErrorMessage } from "../utils/getErrorMessage";

export default function UserRentalHistoryPage() {
    const { userId } = useParams();
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(20);
    const [data, setData] = useState(null);

    const fetchData = async (p = page, s = size) => {
        setLoading(true);
        try {
            const res = await axios.get(`/api/users/${userId}/rentals`, { params: { page: p, size: s } });
            setData(res?.data?.data ?? res?.data);
        } catch (e) {
            toast.error(getErrorMessage(e));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        setPage(0);
        fetchData(0, size);
    }, [userId]);

    return (
        <div style={{ padding: 16 }}>
            <h2 style={{ marginBottom: 12 }}>사용자 대여/반납 기록 (userId: {userId})</h2>

            <div style={{ display: "flex", gap: 8, alignItems: "center", marginBottom: 12 }}>
                <button onClick={() => fetchData(page, size)} disabled={loading}>새로고침</button>

                <span>size</span>
                <select
                    value={size}
                    onChange={(e) => {
                        const s = Number(e.target.value);
                        setSize(s);
                        setPage(0);
                        fetchData(0, s);
                    }}
                >
                    <option value={10}>10</option>
                    <option value={20}>20</option>
                    <option value={50}>50</option>
                </select>

                {loading && <span>Loading...</span>}
            </div>

            <table style={{ width: "100%", borderCollapse: "collapse", background: "white" }}>
                <thead>
                <tr>
                    <th style={th}>id</th>
                    <th style={th}>status</th>
                    <th style={th}>start</th>
                    <th style={th}>end</th>
                    <th style={th}>issuedAt</th>
                    <th style={th}>returnedAt</th>
                    <th style={th}>items</th>
                    <th style={th}>상세</th>
                </tr>
                </thead>
                <tbody>
                {(data?.content ?? []).length === 0 && (
                    <tr><td style={td} colSpan={8}>기록이 없습니다.</td></tr>
                )}

                {(data?.content ?? []).map((r) => (
                    <tr key={r.id}>
                        <td style={td}>{r.id}</td>
                        <td style={td}>{r.status}</td>
                        <td style={td}>{r.startDate}</td>
                        <td style={td}>{r.endDate}</td>
                        <td style={td}>{r.issuedAt}</td>
                        <td style={td}>{r.returnedAt ?? "-"}</td>
                        <td style={td}>{r.itemCount}</td>
                        <td style={td}>
                            <Link to={`/rentals/${r.id}`}>보기</Link>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

const th = { textAlign: "left", borderBottom: "1px solid #ddd", padding: "10px 8px", fontSize: 13 };
const td = { borderBottom: "1px solid #eee", padding: "10px 8px", fontSize: 13 };