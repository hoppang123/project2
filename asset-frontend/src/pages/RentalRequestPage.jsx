import React, { useEffect, useMemo, useState } from "react";
import { searchAssets } from "../api/assets";
import { createRentalRequest } from "../api/rentals";

export default function RentalRequestPage() {
    const [assets, setAssets] = useState([]);
    const [checked, setChecked] = useState({}); // {assetId: true}
    const [keyword, setKeyword] = useState("");

    const [purpose, setPurpose] = useState("업무용 대여");
    const [startDate, setStartDate] = useState("2026-02-23");
    const [endDate, setEndDate] = useState("2026-02-24");

    const [msg, setMsg] = useState("");
    const [err, setErr] = useState("");

    const selectedIds = useMemo(() => {
        return Object.entries(checked)
            .filter(([, v]) => v)
            .map(([k]) => Number(k));
    }, [checked]);

    const load = async () => {
        setErr("");
        try {
            const page = await searchAssets({
                status: "AVAILABLE",
                keyword: keyword || undefined,
                page: 0,
                size: 50,
            });
            setAssets(page.content || []);
        } catch (e) {
            console.error(e);
            setErr("AVAILABLE 자산 목록 로드 실패");
        }
    };

    useEffect(() => {
        load();
    }, []);

    const toggle = (id) => setChecked((prev) => ({ ...prev, [id]: !prev[id] }));

    const clearSelected = () => setChecked({});

    const onSubmit = async (e) => {
        e.preventDefault();
        setMsg("");
        setErr("");

        if (selectedIds.length === 0) {
            setErr("대여할 자산을 최소 1개 선택해줘");
            return;
        }

        try {
            const requestId = await createRentalRequest({
                assetIds: selectedIds,
                purpose,
                startDate,
                endDate,
            });

            setMsg(`신청 완료! requestId=${requestId}`);
            clearSelected();
            await load(); // 신청 후 AVAILABLE에서 빠졌는지 확인
        } catch (e2) {
            console.error(e2);
            setErr("신청 실패(기간/상태/서버 로그 확인)");
        }
    };

    return (
        <div style={{ padding: 16 }}>
            <h2>Rent Request</h2>

            <div style={{ display: "flex", gap: 8, marginBottom: 12, alignItems: "center" }}>
                <input
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    placeholder="검색(코드/이름/시리얼)"
                    style={{ width: 280 }}
                />
                <button onClick={load}>Search</button>

                <div style={{ marginLeft: "auto" }}>
                    Selected: <b>{selectedIds.length}</b>{" "}
                    <button onClick={clearSelected} style={{ marginLeft: 8 }}>
                        Clear
                    </button>
                </div>
            </div>

            <form onSubmit={onSubmit} style={{ display: "grid", gap: 10, maxWidth: 520, marginBottom: 16 }}>
                <label>
                    Purpose
                    <input value={purpose} onChange={(e) => setPurpose(e.target.value)} />
                </label>

                <label>
                    Start Date
                    <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
                </label>

                <label>
                    End Date
                    <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
                </label>

                <button type="submit">Submit Request</button>
            </form>

            {err ? <div style={{ color: "crimson", marginBottom: 10 }}>{err}</div> : null}
            {msg ? <div style={{ color: "green", marginBottom: 10 }}>{msg}</div> : null}

            <h3>Available Assets</h3>
            <table border="1" cellPadding="8" style={{ borderCollapse: "collapse", width: "100%" }}>
                <thead>
                <tr>
                    <th>Select</th>
                    <th>ID</th>
                    <th>Code</th>
                    <th>Name</th>
                    <th>Location</th>
                </tr>
                </thead>
                <tbody>
                {assets.map((a) => (
                    <tr key={a.id}>
                        <td>
                            <input
                                type="checkbox"
                                checked={!!checked[a.id]}
                                onChange={() => toggle(a.id)}
                            />
                        </td>
                        <td>{a.id}</td>
                        <td>{a.assetCode}</td>
                        <td>{a.name}</td>
                        <td>{a.location ?? "-"}</td>
                    </tr>
                ))}
                {assets.length === 0 ? (
                    <tr>
                        <td colSpan="5" style={{ textAlign: "center", color: "#666" }}>
                            AVAILABLE 자산이 없거나 검색 결과가 없음
                        </td>
                    </tr>
                ) : null}
                </tbody>
            </table>
        </div>
    );
}