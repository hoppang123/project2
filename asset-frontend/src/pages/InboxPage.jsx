import React, { useEffect, useState } from "react";
import { approveRequest, rejectRequest, inboxV2 } from "../api/rentals";
import { Link } from "react-router-dom";
import { getErrorMessage } from "../utils/error";

export default function InboxPage() {
    const [pageData, setPageData] = useState(null);
    const [noteMap, setNoteMap] = useState({});
    const [err, setErr] = useState("");
    const [msg, setMsg] = useState("");

    const load = async (page = 0) => {
        setErr("");
        setMsg("");
        try {
            const data = await inboxV2(page, 10);
            setPageData(data);
        } catch (e) {
            console.error(e);
            setErr(getErrorMessage(e, "승인함 로드 실패"));
        }
    };

    useEffect(() => {
        load(0);
    }, []);

    const setNote = (requestId, val) =>
        setNoteMap((prev) => ({ ...prev, [requestId]: val }));

    const onApprove = async (requestId) => {
        setErr("");
        setMsg("");
        try {
            await approveRequest(requestId, noteMap[requestId] || "");
            setMsg(`Approved requestId=${requestId}`);
            await load(pageData?.number ?? 0);
        } catch (e) {
            console.error(e);
            setErr(getErrorMessage(e, "승인 실패"));
        }
    };

    const onReject = async (requestId) => {
        setErr("");
        setMsg("");
        try {
            const reason = noteMap[requestId] || "반려";
            await rejectRequest(requestId, reason);
            setMsg(`Rejected requestId=${requestId}`);
            await load(pageData?.number ?? 0);
        } catch (e) {
            console.error(e);
            setErr(getErrorMessage(e, "반려 실패"));
        }
    };

    return (
        <div style={{ padding: 16 }}>
            <h2>Inbox (Approvals)</h2>

            {err ? <div style={{ color: "crimson", marginBottom: 10 }}>{err}</div> : null}
            {msg ? <div style={{ color: "green", marginBottom: 10 }}>{msg}</div> : null}

            {!pageData ? (
                <div>Loading...</div>
            ) : (
                <>
                    <table border="1" cellPadding="8" style={{ borderCollapse: "collapse", width: "100%" }}>
                        <thead>
                        <tr>
                            <th>Request</th>
                            <th>My Step</th>
                            <th>Status</th>
                            <th>Purpose</th>
                            <th>Period</th>
                            <th>Items</th>
                            <th>Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        {pageData.content.map((r) => (
                            <tr key={r.requestId}>
                                <td>
                                    <Link to={`/rentals/requests/${r.requestId}`}>{r.requestId}</Link>
                                </td>
                                <td>
                                    <b>#{r.stepNo}</b> ({r.approverRole})
                                </td>
                                <td>{r.status}</td>
                                <td>{r.purpose}</td>
                                <td>
                                    {r.startDate} ~ {r.endDate}
                                </td>
                                <td>{r.itemCount}</td>
                                <td style={{ minWidth: 300 }}>
                                    <input
                                        value={noteMap[r.requestId] || ""}
                                        onChange={(e) => setNote(r.requestId, e.target.value)}
                                        placeholder="note/reason"
                                        style={{ width: "100%", marginBottom: 6 }}
                                    />
                                    <div style={{ display: "flex", gap: 8 }}>
                                        <button onClick={() => onApprove(r.requestId)}>Approve</button>
                                        <button onClick={() => onReject(r.requestId)}>Reject</button>
                                    </div>
                                </td>
                            </tr>
                        ))}

                        {pageData.content.length === 0 ? (
                            <tr>
                                <td colSpan="7" style={{ textAlign: "center", color: "#666" }}>
                                    처리할 승인건이 없습니다.
                                </td>
                            </tr>
                        ) : null}
                        </tbody>
                    </table>

                    <div style={{ marginTop: 10, display: "flex", gap: 8 }}>
                        <button disabled={pageData.first} onClick={() => load(pageData.number - 1)}>
                            Prev
                        </button>
                        <div>
                            Page {pageData.number + 1} / {pageData.totalPages}
                        </div>
                        <button disabled={pageData.last} onClick={() => load(pageData.number + 1)}>
                            Next
                        </button>
                    </div>
                </>
            )}
        </div>
    );
}