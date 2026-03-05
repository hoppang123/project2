import React, { useEffect, useState } from "react";
import { myRequests } from "../api/rentals";
import { Link } from "react-router-dom";

export default function MyRequestsPage() {
    const [pageData, setPageData] = useState(null);

    const load = async (page = 0) => {
        const data = await myRequests(page, 10);
        setPageData(data);
    };

    useEffect(() => { load(0); }, []);

    return (
        <div style={{ padding: 16 }}>
            <h2>My Requests</h2>

            {!pageData ? (
                <div>Loading...</div>
            ) : (
                <>
                    <table border="1" cellPadding="8" style={{ borderCollapse: "collapse", width: "100%" }}>
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Status</th>
                            <th>Purpose</th>
                            <th>Period</th>
                            <th>Items</th>
                            <th>Created</th>
                        </tr>
                        </thead>
                        <tbody>
                        {pageData.content.map((r) => (
                            <tr key={r.id}>
                                <td><Link to={`/rentals/requests/${r.id}`}>{r.id}</Link></td>
                                <td>{r.status}</td>
                                <td>{r.purpose}</td>
                                <td>{r.startDate} ~ {r.endDate}</td>
                                <td>{r.itemCount}</td>
                                <td>{String(r.createdAt).replace("T", " ")}</td>
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
