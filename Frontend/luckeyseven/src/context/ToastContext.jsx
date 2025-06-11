"use client"

import { createContext, useContext, useState } from "react"

const ToastContext = createContext(null)

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])

  const addToast = ({ title, description, variant = "default" }) => {
    const id = Date.now().toString()
    setToasts((prev) => [...prev, { id, title, description, variant }])

    // 3초 후 자동으로 토스트 제거
    setTimeout(() => {
      setToasts((prev) => prev.filter((toast) => toast.id !== id))
    }, 3000)
  }

  const removeToast = (id) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id))
  }

  return (
    <ToastContext.Provider value={{ toasts, addToast, removeToast }}>
      {children}
      <div className="fixed bottom-0 right-0 p-4 space-y-2 z-50">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            className={`p-4 rounded-md shadow-md ${
              toast.variant === "destructive" ? "bg-red-100 text-red-800" : "bg-white"
            }`}
          >
            <div className="font-semibold">{toast.title}</div>
            {toast.description && <div className="text-sm">{toast.description}</div>}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export const useToast = () => {
  const context = useContext(ToastContext)
  if (!context) {
    throw new Error("useToast must be used within a ToastProvider")
  }
  return context
}
