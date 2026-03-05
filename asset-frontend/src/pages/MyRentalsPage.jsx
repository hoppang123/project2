import React, { useEffect, useState } from "react";
import { myRentals, returnRequest } from "../api/rentals";
import { Link } from "react-router-dom";

export default function MyRentalsPage() {
    const [pageData, setPageData] = useState(null);
    const [err, setErr] = useState("");

    const load = async (page = 0) => {
        const data = await myRentals(page, 10);
        setPageData(data);
    };

    useEffect(() => { load(0); }, []);

    const onReturnRequest = async (id) => {
        setErr("");
        try {
            await returnRequest(id);
            await load(pageData?.number ?? 0);
        } catch (e2) {
            setErr("반납 요청 실패");
            console.error(e2);
        }
    };

    return (
        <div style={{ padding: 16 }}>
            <h2>My Rentals</h2>
            {err ? <div style={{ color: "crimson", marginBottom: 10 }}>{err}</div> : null}

            {!pageData ? (
                <div>Loading...</div>
            ) : (
                <>
                    <table border="1" cellPadding="8" style={{ borderCollapse: "collapse", width: "100%" }}>
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Status</th>
                            <th>Period</th>
                            <th>Items</th>
                            <th>Issued</th>
                            <th>Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        {pageData.content.map((r) => (
                            <tr key={r.id}>
                                <td><Link to={`/rentals/${r.id}`}>{r.id}</Link></td>
                                <td>{r.status}</td>
                                <td>{r.startDate} ~ {r.endDate}</td>
                                <td>{r.itemCount}</td>
                                <td>{String(r.issuedAt).replace("T"," ")}</td>
                                <td>
                                    <button onClick={() => onReturnRequest(r.id)}>
                                        Return Request
                                    </button>
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
