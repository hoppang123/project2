import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "../api/axios"; // ✅ 네 구조: src/api
import { getErrorMessage } from "../utils/getErrorMessage"; // ✅ 네 구조: src/utils

export default function LowReturnUsersPage() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [items, setItems] = useState([]);
    const [threshold, setThreshold] = useState(0.7);
    const [minCount, setMinCount] = useState(3);

    const fetchList = async () => {
        setLoading(true);
        try {
            const res = await axios.get("/api/admin/risk/low-return");
            const data = res?.data?.data ?? res?.data;
            setItems(Array.isArray(data) ? data : []);
        } catch (e) {
            toast.error(getErrorMessage(e));
        } finally {
            setLoading(false);
        }
    };

    const recalc = async () => {
        setLoading(true);
        try {
            await axios.post("/api/admin/risk/recalc", null, {
                params: { threshold, minCount },
            });
            await fetchList();
            toast.success("재계산 완료");
        } catch (e) {
            toast.error(getErrorMessage(e));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchList();
    }, []);

    return (
        <div style={{ padding: 16 }}>
            <h2 style={{ marginBottom: 12 }}>저반납률 사용자 목록</h2>

            <div style={{ display: "flex", gap: 8, alignItems: "center", marginBottom: 12, flexWrap: "wrap" }}>
                <button onClick={fetchList} disabled={loading}>새로고침</button>

                <div style={{ display: "flex", gap: 6, alignItems: "center" }}>
                    <span>threshold</span>
                    <input
                        type="number"
                        step="0.05"
                        min="0"
                        max="1"
                        value={threshold}
                        onChange={(e) => setThreshold(Number(e.target.value))}
                        style={{ width: 90 }}
                    />
                </div>

                <div style={{ display: "flex", gap: 6, alignItems: "center" }}>
                    <span>minCount</span>
                    <input
                        type="number"
                        min="1"
                        value={minCount}
                        onChange={(e) => setMinCount(Number(e.target.value))}
                        style={{ width: 80 }}
                    />
                </div>

                <button onClick={recalc} disabled={loading}>강제 재계산</button>
                {loading && <span>Loading...</span>}
            </div>

            <table style={{ width: "100%", borderCollapse: "collapse", background: "white" }}>
                <thead>
                <tr>
                    <th style={th}>userId</th>
                    <th style={th}>level</th>
                    <th style={th}>total</th>
                    <th style={th}>returned</th>
                    <th style={th}>returnRate</th>
                    <th style={th}>calculatedAt</th>
                    <th style={th}>Action</th>
                </tr>
                </thead>
                <tbody>
                {items.length === 0 && (
                    <tr><td style={td} colSpan={7}>데이터 없음</td></tr>
                )}

                {items.map((r) => (
                    <tr key={r.userId}>
                        <td style={td}>{r.userId}</td>
                        <td style={td}>{r.level}</td>
                        <td style={td}>{r.totalRentals}</td>
                        <td style={td}>{r.returnedRentals}</td>
                        <td style={td}>
                            {typeof r.returnRate === "number"
                                ? (r.returnRate * 100).toFixed(1) + "%"
                                : r.returnRate}
                        </td>
                        <td style={td}>{r.calculatedAt}</td>
                        <td style={td}>
                            <button onClick={() => navigate(`/admin/users/${r.userId}/rentals`)}>
                                대여기록 보기
                            </button>
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