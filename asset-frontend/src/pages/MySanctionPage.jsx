import React, { useEffect, useState } from "react";
import client from "../api/client";
import { getErrorMessage } from "../utils/error";

export default function MySanctionPage() {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await client.get("/api/sanctions/me");
      setData(res?.data?.data ?? res?.data);
    } catch (e) {
      alert(getErrorMessage(e));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  return (
    <div style={{ padding: 16 }}>
      <h2 style={{ marginBottom: 12 }}>My Sanction</h2>

      <button onClick={fetchData} disabled={loading}>
        {loading ? "Loading..." : "Refresh"}
      </button>

      <div style={{ marginTop: 12 }}>
        {!data ? (
          <div>Loading...</div>
        ) : data.sanctioned ? (
          <div style={{ border: "1px solid #ffb3b3", background: "#ffe5e5", padding: 12, borderRadius: 12 }}>
            <div><b>상태:</b> 제재 중</div>
            <div><b>사유:</b> {data.reason}</div>
            <div><b>기간:</b> {String(data.startsAt).replace("T"," ")} ~ {String(data.endsAt).replace("T"," ")}</div>
            <div><b>포인트:</b> {data.points}</div>
            <div><b>메모:</b> {data.memo ?? "-"}</div>
          </div>
        ) : (
          <div style={{ border: "1px solid #a6f3b6", background: "#e6ffed", padding: 12, borderRadius: 12 }}>
            <div><b>상태:</b> 정상 (제재 없음)</div>
          </div>
        )}
      </div>
    </div>
  );
}