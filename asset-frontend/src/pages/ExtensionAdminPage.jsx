import React, { useEffect, useState } from "react";
import { pendingExtensions, approveExtension, rejectExtension } from "../api/extensions";

export default function ExtensionAdminPage() {
    const [pageData, setPageData] = useState(null);
    const [noteMap, setNoteMap] = useState({});
    const [err, setErr] = useState("");

    const load = async (page = 0) => {
        const data = await pendingExtensions(page, 10);
        setPageData(data);
    };

    useEffect(() => { load(0); }, []);

    const setNote = (id, val) => setNoteMap((prev) => ({ ...prev, [id]: val }));

    const onApprove = async (id) => {
        setErr("");
        try {
            await approveExtension(id, noteMap[id] || "");
            await load(pageData?.number ?? 0);
        } catch (e2) {
            setErr("승인 실패(권한 확인)");
            console.error(e2);
        }
    };

    const onReject = async (id) => {
        setErr("");
        try {
            await rejectExtension(id, noteMap[id] || "반려");
            await load(pageData?.number ?? 0);
        } catch (e2) {
            setErr("반려 실패(권한 확인)");
            console.error(e2);
        }
    };

    return (
        <div style={{ padding: 16 }}>
            <h2>Extensions Admin (Pending)</h2>
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
                            <th>Requester</th>
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
                                <td>{r.requesterEmail}</td>
                                <td>{r.requestedEndDate}</td>
                                <td>{r.reason}</td>
                                <td style={{ minWidth: 280 }}>
                                    <input
                                        value={noteMap[r.id] || ""}
                                        onChange={(e) => setNote(r.id, e.target.value)}
                                        placeholder="adminNote"
                                        style={{ width: "100%", marginBottom: 6 }}
                                    />
                                    <div style={{ display: "flex", gap: 8 }}>
                                        <button onClick={() => onApprove(r.id)}>Approve</button>
                                        <button onClick={() => onReject(r.id)}>Reject</button>
                                    </div>
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
