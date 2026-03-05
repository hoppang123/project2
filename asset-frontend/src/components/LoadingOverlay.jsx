import React from "react";

export default function LoadingOverlay({ open, text = "Loading..." }) {
    if (!open) return null;

    return (
        <div
            style={{
                position: "fixed",
                inset: 0,
                zIndex: 9998,
                background: "rgba(0,0,0,0.25)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
            }}
        >
            <div
                style={{
                    background: "white",
                    borderRadius: 14,
                    padding: "14px 16px",
                    minWidth: 240,
                    boxShadow: "0 10px 30px rgba(0,0,0,0.2)",
                }}
            >
                <div style={{ fontWeight: 700, marginBottom: 6 }}>{text}</div>
                <div style={{ color: "#666", fontSize: 13 }}>잠시만 기다려주세요.</div>
            </div>
        </div>
    );
}