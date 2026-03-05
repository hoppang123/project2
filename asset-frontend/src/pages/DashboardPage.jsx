import React, { useEffect, useMemo, useState } from "react";
import { useAuth } from "../auth/AuthContext";
import { dashboardSummary } from "../api/dashboard";

function StatCard({ title, value, hint, tone = "default" }) {
  const badgeClass =
    tone === "primary"
      ? "badge badge-primary"
      : tone === "danger"
        ? "badge badge-danger"
        : tone === "success"
          ? "badge badge-success"
          : "badge";

  return (
    <div className="card card-pad" style={{ minWidth: 210 }}>
      <div className="row" style={{ justifyContent: "space-between", marginBottom: 6 }}>
        <div className="muted" style={{ fontSize: 13, fontWeight: 800 }}>
          {title}
        </div>
        {hint ? <span className={badgeClass}>{hint}</span> : null}
      </div>

      <div style={{ fontSize: 30, fontWeight: 900, letterSpacing: -0.5 }}>
        {value ?? "-"}
      </div>
    </div>
  );
}

function Section({ title, right, children }) {
  return (
    <div style={{ marginBottom: 16 }}>
      <div className="row" style={{ marginBottom: 10 }}>
        <div className="h2" style={{ margin: 0 }}>
          {title}
        </div>
        <div className="spacer" />
        {right}
      </div>
      {children}
    </div>
  );
}

export default function DashboardPage() {
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(false);

  const load = async () => {
    setErr("");
    setLoading(true);
    try {
      const d = await dashboardSummary();
      setData(d);
    } catch (e) {
      console.error(e);
      setErr("대시보드 통계 로드 실패");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const welcome = useMemo(() => {
    if (!user) return "";
    return `${user.email} (${user.role})`;
  }, [user]);

  return (
    <div className="container page">
      <div className="row" style={{ marginBottom: 12 }}>
        <div>
          <div className="h1" style={{ marginBottom: 6 }}>
            Dashboard
          </div>
          <div className="muted">
            Welcome: <span className="badge">{welcome || "-"}</span>
          </div>
        </div>

        <div className="spacer" />

        <button className="btn" onClick={load} disabled={loading}>
          {loading ? "Refreshing..." : "Refresh"}
        </button>
      </div>

      {err ? (
        <div className="notice error" style={{ marginBottom: 12 }}>
          {err}
        </div>
      ) : null}

      {!data ? (
        <div className="card card-pad">
          <div className="muted">{loading ? "Loading..." : "데이터가 없습니다."}</div>
        </div>
      ) : (
        <>
          <Section
            title="Assets"
            right={<span className="badge">Inventory</span>}
          >
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "repeat(auto-fit, minmax(210px, 1fr))",
                gap: 12,
              }}
            >
              <StatCard title="AVAILABLE" value={data.assetAvailable} tone="success" hint="OK" />
              <StatCard title="RESERVED" value={data.assetReserved} tone="primary" hint="Hold" />
              <StatCard title="RENTED" value={data.assetRented} tone="primary" hint="Out" />
              <StatCard title="MAINTENANCE" value={data.assetMaintenance} tone="danger" hint="Fix" />
            </div>
          </Section>

          <Section title="Requests" right={<span className="badge">Workflow</span>}>
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "repeat(auto-fit, minmax(210px, 1fr))",
                gap: 12,
              }}
            >
              <StatCard title="APPROVING" value={data.requestApproving} tone="primary" hint="In Progress" />
              <StatCard title="APPROVED" value={data.requestApproved} tone="success" hint="Done" />
              <StatCard title="REJECTED" value={data.requestRejected} tone="danger" hint="No" />
              <StatCard title="CANCELED" value={data.requestCanceled} hint="Stop" />
            </div>
          </Section>

          <Section title="Rentals" right={<span className="badge">Operations</span>}>
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "repeat(auto-fit, minmax(210px, 1fr))",
                gap: 12,
              }}
            >
              <StatCard title="ACTIVE" value={data.rentalActive} tone="primary" hint="Running" />
              <StatCard title="RETURN_REQUESTED" value={data.rentalReturnRequested} hint="Waiting" />
              <StatCard title="RETURNED" value={data.rentalReturned} tone="success" hint="Closed" />
              <StatCard title="EXT PENDING" value={data.extensionPending} hint="Queue" />
            </div>
          </Section>

          <Section title="Today" right={<span className="badge">Daily</span>}>
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "repeat(auto-fit, minmax(210px, 1fr))",
                gap: 12,
              }}
            >
              <StatCard title="Today Requests" value={data.todayRequestCount} />
              <StatCard title="Today Extension Requests" value={data.todayExtensionRequestCount} />
            </div>

            <div className="card card-pad" style={{ marginTop: 12 }}>
              <div className="muted" style={{ fontSize: 13 }}>
                * 숫자는 API(`/api/dashboard`) 집계 기준입니다. (권한/필터 정책에 따라 값이 달라질 수 있어요)
              </div>
            </div>
          </Section>
        </>
      )}
    </div>
  );
}