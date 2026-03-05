import React, { useEffect, useMemo, useState } from "react";
import axios from "../api/axios";          // src/api/axios.js
import { getErrorMessage } from "../utils/error";
import Toast from "../components/Toast";
import LoadingOverlay from "../components/LoadingOverlay";

export default function AdminStatsPage() {
  const [days, setDays] = useState(14);
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState(null);

  const [toast, setToast] = useState({ open: false, type: "success", message: "" });
  const showError = (m) => setToast({ open: true, type: "error", message: m });
  const showSuccess = (m) => setToast({ open: true, type: "success", message: m });

  const fetchData = async (d = days) => {
    setLoading(true);
    try {
      const res = await axios.get("/api/admin/stats", { params: { days: d } });
      setData(res?.data?.data ?? res?.data ?? null);
      showSuccess("통계 로드 완료");
    } catch (e) {
      showError(getErrorMessage(e, "통계 로드 실패"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData(14);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const series = data?.series ?? [];

  const maxVal = useMemo(() => {
    if (!series.length) return 1;
    return Math.max(
      1,
      ...series.map((x) =>
        Math.max(
          x.rentalsStarted ?? 0,
          x.returnsConfirmed ?? 0,
          x.reservationsCreated ?? 0,
          x.maintenancesCreated ?? 0
        )
      )
    );
  }, [series]);

  const Bar = ({ v }) => {
    const width = Math.round(((v ?? 0) / maxVal) * 120);
    return (
      <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
        <div
          style={{
            width: 120,
            height: 10,
            border: "1px solid #ddd",
            borderRadius: 999,
            overflow: "hidden",
            background: "#fafafa",
          }}
        >
          <div style={{ width, height: "100%" }} />
        </div>
        <span style={{ fontSize: 12, color: "#555" }}>{v ?? 0}</span>
      </div>
    );
  };

  return (
    <div style={{ padding: 16 }}>
      <div style={{ display: "flex", alignItems: "baseline", justifyContent: "space-between", gap: 12, flexWrap: "wrap" }}>
        <h2 style={{ marginBottom: 12 }}>Admin Stats</h2>

        <div style={{ display: "flex", gap: 10, flexWrap: "wrap", alignItems: "center" }}>
          <span style={{ color: "#666" }}>기간(days)</span>
          <select
            value={days}
            disabled={loading}
            onChange={(e) => {
              const d = Number(e.target.value);
              setDays(d);
              fetchData(d);
            }}
          >
            <option value={7}>7</option>
            <option value={14}>14</option>
            <option value={30}>30</option>
            <option value={60}>60</option>
            <option value={90}>90</option>
          </select>

          <button onClick={() => fetchData(days)} disabled={loading}>
            새로고침
          </button>
        </div>
      </div>

      {!data ? (
        <div style={{ color: "#666" }}>데이터 없음</div>
      ) : (
        <>
          <div style={{ display: "flex", gap: 10, flexWrap: "wrap", marginBottom: 16 }}>
            <Card title="ACTIVE" value={data.activeRentals} />
            <Card title="OVERDUE" value={data.overdueRentals} />
            <Card title="RETURN_REQUESTED" value={data.returnRequestedRentals} />
            <Card title="Today Rentals" value={data.todayRentalsStarted} />
            <Card title="Today Returns" value={data.todayReturnsConfirmed} />
            <Card title="Today Reservations" value={data.todayReservationsCreated} />
            <Card title="Today Maintenances" value={data.todayMaintenancesCreated} />
          </div>

          <h3 style={{ marginBottom: 8 }}>Daily Series</h3>

          <table style={{ width: "100%", borderCollapse: "collapse", background: "white" }}>
            <thead>
              <tr>
                <th style={th}>date</th>
                <th style={th}>rentals</th>
                <th style={th}>returns</th>
                <th style={th}>reservations</th>
                <th style={th}>maintenances</th>
              </tr>
            </thead>
            <tbody>
              {series.length === 0 && (
                <tr>
                  <td style={td} colSpan={5}>데이터가 없습니다.</td>
                </tr>
              )}

              {series.map((r) => (
                <tr key={r.date}>
                  <td style={td}>{r.date}</td>
                  <td style={td}><Bar v={r.rentalsStarted} /></td>
                  <td style={td}><Bar v={r.returnsConfirmed} /></td>
                  <td style={td}><Bar v={r.reservationsCreated} /></td>
                  <td style={td}><Bar v={r.maintenancesCreated} /></td>
                </tr>
              ))}
            </tbody>
          </table>

          <div style={{ marginTop: 10, color: "#666", fontSize: 12 }}>
            * 막대는 기간 내 최대값 기준으로 상대 표시
          </div>
        </>
      )}

      <LoadingOverlay open={loading} text="Loading stats..." />
      <Toast
        open={toast.open}
        type={toast.type}
        message={toast.message}
        onClose={() => setToast((t) => ({ ...t, open: false }))}
        durationMs={2500}
      />
    </div>
  );
}

function Card({ title, value }) {
  return (
    <div style={{ border: "1px solid #ddd", borderRadius: 12, padding: 12, minWidth: 160 }}>
      <div style={{ color: "#666", fontSize: 12 }}>{title}</div>
      <div style={{ fontSize: 22, fontWeight: 800 }}>{value ?? 0}</div>
    </div>
  );
}

const th = { textAlign: "left", borderBottom: "1px solid #ddd", padding: "10px 8px", fontSize: 13 };
const td = { borderBottom: "1px solid #eee", padding: "10px 8px", fontSize: 13, verticalAlign: "top" };