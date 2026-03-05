import React, { useEffect, useState } from "react";
import { myExtensionRequests, requestExtension, cancelExtension } from "../api/extensions";

export default function ExtensionsPage() {
    const [rentalId, setRentalId] = useState("");
    const [requestedEndDate, setRequestedEndDate] = useState("2026-02-25");
    const [reason, setReason] = useState("업무 일정 연장");
    const [pageData, setPageData] = useState(null);
    const [msg, setMsg] = useState("");
    const [err, setErr] = useState("");

    const load = async (page = 0) => {
        const data = await myExtensionRequests(page, 10);
        setPageData(data);
    };

    useEffect(() => { load(0); }, []);

    const onSubmit = async (e) => {
        e.preventDefault();
        setErr(""); setMsg("");
        try {
            const idNum = Number(rentalId);
            if (!idNum) { setErr("rentalId를 숫자로 입력"); return; }

            const extId = await requestExtension({
                rentalId: idNum,
                requestedEndDate,
                reason
            });

            setMsg(`연장 요청 완료! id=${extId}`);
            setRentalId("");
            await load(0);
        } catch (e2) {
            setErr("연장 요청 실패");
            console.error(e2);
        }
    };

    const onCancel = async (id) => {
        setErr(""); setMsg("");
        try {
            await cancelExtension(id);
            setMsg("취소 완료");
            await load(pageData?.number ?? 0);
        } catch (e2) {
            setErr("취소 실패");
            console.error(e2);
        }
    };

    return (
        <div style={{ padding: 16 }}>
            <h2>Extensions (My)</h2>

            <form onSubmit={onSubmit} style={{ display: "grid", gap: 10, maxWidth: 420, marginBottom: 16 }}>
                <label>
                    Rental ID
                    <input value={rentalId} onChange={(e) => setRentalId(e.target.value)} placeholder="예: 1" />
                </label>
                <label>
                    Requested End Date
                    <input type="date" value={requestedEndDate} onChange={(e) => setRequestedEndDate(e.target.value)} />
                </label>
                <label>
                    Reason
                    <input value={reason} onChange={(e) => setReason(e.target.value)} />
                </label>
                <button type="submit">Request Extension</button>
            </form>

            {msg ? <div style={{ color: "green", marginBottom: 10 }}>{msg}</div> : null}
            {err ? <div style={{ color: "crimson", marginBottom: 10 }}>{err}</div> : null}

            {!pageData ? (
                <div>Loading...</div>
            ) : (
                <>
                    <table border="1" cellPadding="8" style={{ borderCollapse: "collapse", width: "100%" }}>
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Rental</th>
                            <th>Status</th>
                            <th>RequestedEnd</th>
                            <th>Reason</th>
                            <th>Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        {pageData.content.map((r) => (
                            <tr key={r.id}>
                                <td>{r.id}</td>
                                <td>{r.rentalId}</td>
                                <td>{r.status}</td>
                                <td>{r.requestedEndDate}</td>
                                <td>{r.reason}</td>
                                <td>
                                    <button onClick={() => onCancel(r.id)}>Cancel</button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>

                    <div style={{ marginTop: 10, display: "flex", gap: 8 }}>
                        <button disabled={pageData.first} onClick={() => load(pageData.number - 1)}>Prev</button>
                        <div>Page {pageData.number + 1} / {pageData.totalPages}</div>
                        <button disabled={pageData.last} onClick={() => load(pageData.number + 1)}>Next</button>
                    </div>
                </>
            )}
        </div>
    );
}
