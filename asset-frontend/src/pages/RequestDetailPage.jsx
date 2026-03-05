import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { requestDetail, cancelRequest } from "../api/rentals";

export default function RequestDetailPage() {
    const { id } = useParams();
    const [data, setData] = useState(null);
    const [msg, setMsg] = useState("");
    const [err, setErr] = useState("");

    const load = async () => {
        const d = await requestDetail(id);
        setData(d);
    };

    useEffect(() => { load(); }, [id]);

    const onCancel = async () => {
        setErr(""); setMsg("");
        try {
            await cancelRequest(id);
            setMsg("취소 완료");
            await load();
        } catch (e2) {
            setErr("취소 실패");
            console.error(e2);
        }
    };

    return (
        <div style={{ padding: 16 }}>
            <h2>Request Detail #{id}</h2>

            {!data ? (
                <div>Loading...</div>
            ) : (
                <>
                    <div style={{ display: "grid", gap: 6, marginBottom: 12 }}>
                        <div><b>Status:</b> {data.status}</div>
                        <div><b>Requester:</b> {data.requesterEmail}</div>
                        <div><b>Purpose:</b> {data.purpose}</div>
                        <div><b>Period:</b> {data.startDate} ~ {data.endDate}</div>
                        <div><b>RentalId:</b> {data.rentalId ?? "-"}</div>
                    </div>

                    <h3>Items</h3>
                    <ul>
                        {data.items.map((it) => (
                            <li key={it.assetId}>
                                #{it.assetId} / {it.assetCode} / {it.name} / <b>{it.assetStatus}</b>
                            </li>
                        ))}
                    </ul>

                    <h3>Approval Steps</h3>
                    <ol>
                        {data.steps.map((s) => (
                            <li key={s.stepNo}>
                                step {s.stepNo} / {s.approverRole} / <b>{s.status}</b>
                                {s.reason ? ` / note: ${s.reason}` : ""}
                            </li>
                        ))}
                    </ol>

                    <div style={{ marginTop: 12, display: "flex", gap: 8 }}>
                        <button onClick={onCancel}>Cancel Request</button>
                        <button onClick={load}>Refresh</button>
                    </div>

                    {msg ? <div style={{ marginTop: 10, color: "green" }}>{msg}</div> : null}
                    {err ? <div style={{ marginTop: 10, color: "crimson" }}>{err}</div> : null}
                </>
            )}
        </div>
    );
}
