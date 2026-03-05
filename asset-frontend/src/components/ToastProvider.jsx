import React, { createContext, useCallback, useContext, useMemo, useState } from "react";
import Toast from "./Toast";

const ToastContext = createContext(null);

export function ToastProvider({ children }) {
  const [open, setOpen] = useState(false);
  const [message, setMessage] = useState("");
  const [type, setType] = useState("success");

  const close = useCallback(() => setOpen(false), []);

  const show = useCallback((msg, t = "success") => {
    setMessage(msg);
    setType(t);
    setOpen(true);
  }, []);

  const api = useMemo(
    () => ({
      success: (msg) => show(msg, "success"),
      error: (msg) => show(msg, "error"),
      show,
    }),
    [show]
  );

  return (
    <ToastContext.Provider value={api}>
      {children}
      <Toast open={open} message={message} type={type} onClose={close} />
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) {
    // Provider 없이 써도 빌드 깨지지 않도록 안전 처리
    return {
      success: (msg) => console.log("[toast:success]", msg),
      error: (msg) => console.log("[toast:error]", msg),
      show: (msg) => console.log("[toast]", msg),
    };
  }
  return ctx;
}