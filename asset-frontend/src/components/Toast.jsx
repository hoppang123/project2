import React, { useEffect } from "react";

export default function Toast({ open, message, type = "success", onClose, durationMs = 2500 }) {
  useEffect(() => {
    if (!open) return;
    const t = setTimeout(() => onClose?.(), durationMs);
    return () => clearTimeout(t);
  }, [open, durationMs, onClose]);

  if (!open) return null;

  const bg = type === "error" ? "#ffe5e5" : "#e6ffed";
  const bd = type === "error" ? "#ffb3b3" : "#a6f3b6";
  const fg = type === "error" ? "#a40000" : "#0b6b2b";

  return (
    <div
      style={{
        position: "fixed",
        right: 16,
        bottom: 16,
        zIndex: 9999,
        background: bg,
        border: `1px solid ${bd}`,
        color: fg,
        padding: "10px 12px",
        borderRadius: 12,
        minWidth: 260,
        boxShadow: "0 6px 20px rgba(0,0,0,0.12)",
      }}
      role="alert"
    >
      <div style={{ display: "flex", gap: 10, alignItems: "center" }}>
        <div style={{ fontWeight: 700 }}>{type === "error" ? "Error" : "Success"}</div>
        <div style={{ flex: 1 }}>{message}</div>
        <button onClick={onClose} style={{ border: "none", background: "transparent", cursor: "pointer" }}>
          ✕
        </button>
      </div>
    </div>
  );
}