import React, { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { searchAssets, changeAssetStatus } from "../api/assets";
import { useAuth } from "../auth/AuthContext";
import { getErrorMessage } from "../utils/error";
import Toast from "../components/Toast";
import LoadingOverlay from "../components/LoadingOverlay";

const STATUS_OPTIONS = [
  "AVAILABLE",
  "RESERVED",
  "RENTED",
  "MAINTENANCE",
  "LOST",
  "DISPOSED",
];

function statusTone(status) {
  if (status === "AVAILABLE") return "success";
  if (status === "MAINTENANCE" || status === "LOST" || status === "DISPOSED") return "danger";
  if (status === "RESERVED" || status === "RENTED") return "primary";
  return "default";
}

function StatusBadge({ status }) {
  const tone = statusTone(status);
  const cls =
    tone === "success"
      ? "badge badge-success"
      : tone === "danger"
        ? "badge badge-danger"
        : tone === "primary"
          ? "badge badge-primary"
          : "badge";
  return <span className={cls}>{status}</span>;
}

export default function AssetsPage() {
  const { user } = useAuth();
  const role = user?.role;
  const isAdmin = role === "ASSET_ADMIN" || role === "SUPER_ADMIN";

  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState("");

  const [pageData, setPageData] = useState(null);

  // row UI state
  const [rowStatus, setRowStatus] = useState({});
  const [rowNote, setRowNote] = useState({});
  const [busyRow, setBusyRow] = useState({});

  // global UI state
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState({ open: false, type: "success", message: "" });

  const showSuccess = (message) => setToast({ open: true, type: "success", message });
  const showError = (message) => setToast({ open: true, type: "error", message });

  const setBusy = (assetId, val) => setBusyRow((prev) => ({ ...prev, [assetId]: val }));
  const setStatusForRow = (assetId, val) => setRowStatus((prev) => ({ ...prev, [assetId]: val }));
  const setNoteForRow = (assetId, val) => setRowNote((prev) => ({ ...prev, [assetId]: val }));

  const initRowStatus = (data) => {
    const map = {};
    (data.content || []).forEach((a) => {
      map[a.id] = a.status;
    });
    setRowStatus(map);
  };

  const load = async (page = 0) => {
    setLoading(true);
    try {
      const params = {
        keyword: keyword || undefined,
        status: status || undefined,
        page,
        size: 10,
      };
      const data = await searchAssets(params);
      setPageData(data);
      initRowStatus(data);
    } catch (e) {
      console.error(e);
      showError(getErrorMessage(e, "자산 목록 로드 실패"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const applyRowStatus = async (assetId, currentStatus) => {
    const next = rowStatus[assetId];

    if (!next || next === currentStatus) {
      showSuccess("변경사항 없음");
      return;
    }

    try {
      setBusy(assetId, true);
      await changeAssetStatus(assetId, {
        status: next,
        note: rowNote[assetId] || null,
      });

      showSuccess(`자산 #${assetId} 상태 변경: ${currentStatus} → ${next}`);
      await load(pageData?.number ?? 0);
    } catch (e) {
      console.error(e);
      showError(getErrorMessage(e, "상태 변경 실패"));
    } finally {
      setBusy(assetId, false);
    }
  };

  const pageInfo = useMemo(() => {
    if (!pageData) return "";
    const cur = (pageData.number ?? 0) + 1;
    const total = pageData.totalPages ?? 0;
    const totalEl = total === 0 ? "?" : total;
    return `Page ${cur} / ${totalEl}`;
  }, [pageData]);

  return (
    <div className="container page">
      <div className="row" style={{ marginBottom: 12 }}>
        <div className="h1" style={{ margin: 0 }}>Assets</div>
        <div className="spacer" />
        <span className="badge">{isAdmin ? "관리자 모드" : "일반 모드"}</span>
      </div>

      {/* Filters */}
      <div className="card card-pad" style={{ marginBottom: 12 }}>
        <div className="row" style={{ gap: 10 }}>
          <input
            className="input"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="keyword (name/code/serial/location)"
            style={{ width: 320 }}
          />

          <select
            className="select"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            style={{ minWidth: 180 }}
          >
            <option value="">ALL</option>
            {STATUS_OPTIONS.map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>

          <button className="btn btn-primary" onClick={() => load(0)} disabled={loading}>
            Search
          </button>

          <button className="btn" onClick={() => load(pageData?.number ?? 0)} disabled={loading}>
            Refresh
          </button>

          <div className="spacer" />

          <div className="muted" style={{ fontSize: 13, fontWeight: 800 }}>
            {isAdmin
              ? "목록에서 상태 변경 가능"
              : "상태 변경은 관리자만 가능"}
          </div>
        </div>
      </div>

      {/* Table */}
      {!pageData ? (
        <div className="card card-pad">
          <div className="muted">{loading ? "Loading..." : "데이터가 없습니다."}</div>
        </div>
      ) : (
        <>
          <table className="table">
            <thead>
              <tr>
                <th style={{ width: 80 }}>ID</th>
                <th style={{ width: 150 }}>Code</th>
                <th>Name</th>
                <th style={{ width: 170 }}>Status</th>
                <th style={{ width: 180 }}>Category</th>
                <th style={{ width: 180 }}>Location</th>
                {isAdmin ? <th style={{ width: 420 }}>Admin Action</th> : null}
              </tr>
            </thead>

            <tbody>
              {pageData.content.map((a) => (
                <tr key={a.id}>
                  <td>{a.id}</td>
                  <td className="muted" style={{ fontWeight: 800 }}>{a.assetCode}</td>
                  <td>
                    <Link to={`/assets/${a.id}`} style={{ fontWeight: 900, textDecoration: "none" }}>
                      {a.name}
                    </Link>
                  </td>
                  <td>
                    <StatusBadge status={a.status} />
                  </td>
                  <td className="muted">{a.categoryName ?? "-"}</td>
                  <td className="muted">{a.location ?? "-"}</td>

                  {isAdmin ? (
                    <td>
                      <div className="row" style={{ gap: 8 }}>
                        <select
                          className="select"
                          value={rowStatus[a.id] ?? a.status}
                          onChange={(e) => setStatusForRow(a.id, e.target.value)}
                          disabled={!!busyRow[a.id]}
                          style={{ minWidth: 180 }}
                        >
                          {STATUS_OPTIONS.map((s) => (
                            <option key={s} value={s}>{s}</option>
                          ))}
                        </select>

                        <input
                          className="input"
                          value={rowNote[a.id] || ""}
                          onChange={(e) => setNoteForRow(a.id, e.target.value)}
                          placeholder="note(optional)"
                          disabled={!!busyRow[a.id]}
                          style={{ width: 220 }}
                        />

                        <button
                          className="btn btn-primary"
                          onClick={() => applyRowStatus(a.id, a.status)}
                          disabled={!!busyRow[a.id]}
                        >
                          Apply
                        </button>

                        {busyRow[a.id] ? (
                          <span className="muted" style={{ fontWeight: 800 }}>...</span>
                        ) : null}
                      </div>

                      <div className="muted" style={{ fontSize: 12, marginTop: 6 }}>
                        * 실무 권장: RENTED/RESERVED는 워크플로우로만 변경하도록 제한
                      </div>
                    </td>
                  ) : null}
                </tr>
              ))}

              {pageData.content.length === 0 ? (
                <tr>
                  <td colSpan={isAdmin ? 7 : 6} style={{ textAlign: "center" }}>
                    <span className="muted">자산이 없습니다.</span>
                  </td>
                </tr>
              ) : null}
            </tbody>
          </table>

          {/* Pagination */}
          <div className="row" style={{ marginTop: 12 }}>
            <button
              className="btn"
              disabled={pageData.first || loading}
              onClick={() => load(pageData.number - 1)}
            >
              Prev
            </button>

            <span className="badge">{pageInfo}</span>

            <button
              className="btn"
              disabled={pageData.last || loading}
              onClick={() => load(pageData.number + 1)}
            >
              Next
            </button>

            <div className="spacer" />

            <span className="muted" style={{ fontSize: 13, fontWeight: 800 }}>
              Total: {pageData.totalElements ?? "-"}
            </span>
          </div>
        </>
      )}

      <LoadingOverlay open={loading} text="Loading Assets..." />

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