import React, { useState, useEffect } from 'react'
import { X, CheckCircle, AlertCircle, Info } from 'lucide-react'
import '../../Styles/Toaster.css'

// Evento global para disparar toasts de qualquer lugar
export const toastEvent = new EventTarget()

export function toast(message, type = 'info') {
    const event = new CustomEvent('add-toast', { detail: { message, type } })
    toastEvent.dispatchEvent(event)
}

export default function Toaster() {
    const [toasts, setToasts] = useState([])

    useEffect(() => {
        const handleAddToast = (event) => {
            const { message, type } = event.detail
            const id = Date.now()
            setToasts((prev) => [...prev, { id, message, type }])

            // Auto remover após 3 segundos
            setTimeout(() => {
                removeToast(id)
            }, 3000)
        }

        toastEvent.addEventListener('add-toast', handleAddToast)
        return () => toastEvent.removeEventListener('add-toast', handleAddToast)
    }, [])

    const removeToast = (id) => {
        setToasts((prev) => prev.filter((t) => t.id !== id))
    }

    const getIcon = (type) => {
        switch (type) {
            case 'success': return <CheckCircle size={18} />
            case 'error': return <AlertCircle size={18} />
            default: return <Info size={18} />
        }
    }

    return (
        <div className="toaster-container">
            {toasts.map((t) => (
                <div key={t.id} className={`toast toast-${t.type}`}>
                    <div className="toast-icon">{getIcon(t.type)}</div>
                    <span className="toast-message">{t.message}</span>
                    <button onClick={() => removeToast(t.id)} className="toast-close">
                        <X size={14} />
                    </button>
                </div>
            ))}
        </div>
    )
}