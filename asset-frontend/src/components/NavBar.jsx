import React, { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import client from "../api/client";
import { APP_EVENTS, onAppEvent } from "../common/events";

export default function NavBar() {
  const { user, logout, loading } = useAuth();
  const role = user?.role;

  const isManager = role === "MANAGER";
  const isAdmin = role === "ASSET_ADMIN" || role === "SUPER_ADMIN";

  const { pathname } = useLocation();

  const isActive = (to) => {
    if (to === "/") return pathname === "/";
    return pathname === to || pathname.startsWith(to + "/");
  };

  const [unread, setUnread] = useState(0);

  const fetchUnread = async () => {
    if (!user) return;
    try {
      const res = await client.get("/api/notifications/unread-count");
      const cnt = res?.data?.data ?? res?.data;
      setUnread(Number(cnt) || 0);
    } catch (e) {
      console.log("unread-count error", e);
    }
  };

  useEffect(() => {
    if (!user) {
      setUnread(0);
      return;
    }
    fetchUnread();
    const t = setInterval(fetchUnread, 15_000);
    return () => clearInterval(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [!!user, user?.email]);

  useEffect(() => {
    const off = onAppEvent(APP_EVENTS.NOTIFICATIONS_CHANGED, () => fetchUnread());
    return off;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [!!user, user?.email]);

  const pill = (to) => ({
    display: "inline-flex",
    alignItems: "center",
    gap: 8,
    padding: "8px 12px",
    borderRadius: 999,
    border: "1px solid var(--border)",
    background: isActive(to) ? "rgba(37,99,235,0.10)" : "var(--surface)",
    color: isActive(to) ? "var(--primary-2)" : "var(--text)",
    fontWeight: 800,
    textDecoration: "none",
    transition: "0.12s",
  });

  return (
    <div style={{ padding: "12px 0" }}>
      <div className="container">
        <div
          className="card"
          style={{
            padding: 12,
            display: "flex",
            gap: 12,
            alignItems: "center",
            flexWrap: "wrap",
          }}
        >
          <div style={{ fontWeight: 900, marginRight: 8 }}>Asset Platform</div>

          <div className="row" style={{ gap: 8 }}>
            <Link to="/" style={pill("/")}>Dashboard</Link>
            <Link to="/assets" style={pill("/assets")}>Assets</Link>
            {isAdmin && <Link to="/assets/new" style={pill("/assets/new")}>Create Asset</Link>}

            <Link to="/rentals/request" style={pill("/rentals/request")}>Rent Request</Link>
            <Link to="/rentals/requests" style={pill("/rentals/requests")}>My Requests</Link>
            <Link to="/rentals" style={pill("/rentals")}>My Rentals</Link>

            <Link to="/reservations" style={pill("/reservations")}>My Reservations</Link>

            <Link to="/extensions" style={pill("/extensions")}>Extensions</Link>

            {(isManager || isAdmin) && (
              <Link to="/rentals/inbox" style={pill("/rentals/inbox")}>Inbox</Link>
            )}
            {isAdmin && (
              <Link to="/extensions/admin" style={pill("/extensions/admin")}>Ext Admin</Link>
            )}

            <Link to="/me/rentals" style={pill("/me/rentals")}>My History</Link>
            <Link to="/me/sanction" style={pill("/me/sanction")}>My Sanction</Link>

            {isAdmin && (
              <Link to="/admin/risk/low-return" style={pill("/admin/risk/low-return")}>Low Return</Link>
            )}
            {isAdmin && (
              <Link to="/admin/stats" style={pill("/admin/stats")}>Stats</Link>
            )}

            <Link to="/notifications" style={{ ...pill("/notifications"), position: "relative" }}>
              🔔
              {unread > 0 && (
                <span
                  style={{
                    position: "absolute",
                    top: -6,
                    right: -6,
                    background: "var(--danger)",
                    color: "#fff",
                    borderRadius: 999,
                    padding: "2px 7px",
                    fontSize: 11,
                    fontWeight: 900,
                    lineHeight: 1.2,
                  }}
                >
                  {unread}
                </span>
              )}
            </Link>
          </div>

          <div className="spacer" />

          <div className="row" style={{ gap: 10 }}>
            {loading ? (
              <span className="muted">Loading...</span>
            ) : user ? (
              <>
                <span className="badge">{user.email} ({user.role})</span>
                <button className="btn" onClick={logout}>Logout</button>
              </>
            ) : null}
          </div>
        </div>
      </div>
    </div>
  );
}