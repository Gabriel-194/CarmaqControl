import React, { useState, useEffect } from 'react'
import { Upload, Trash2, Camera, Lock } from 'lucide-react'
import axios from 'axios'
import { toast } from './ui/Toaster'
import '../Styles/ServicePhotos.css'

const API_URL = 'http://localhost:8080/api/service-orders'

// Componente de upload e visualização de fotos associadas a uma OS
export default function ServicePhotos({ serviceOrderId, orderStatus }) {
    const [photos, setPhotos] = useState([])
    const [uploading, setUploading] = useState(false)

    // Regra de bloqueio: permite em qualquer status exceto se faturado (PAGO)
    const isEditable = orderStatus !== 'PAGO'

    const fetchPhotos = async () => {
        try {
            const res = await axios.get(`${API_URL}/${serviceOrderId}/photos`, { withCredentials: true })
            setPhotos(res.data)
        } catch (error) {
            console.error('Erro ao carregar fotos', error)
        }
    }

    useEffect(() => {
        if (serviceOrderId) fetchPhotos()
    }, [serviceOrderId])

    const handleUpload = async (e) => {
        if (!isEditable) return
        const file = e.target.files[0]
        if (!file) return

        const formData = new FormData()
        formData.append('file', file)

        setUploading(true)
        try {
            await axios.post(`${API_URL}/${serviceOrderId}/photos`, formData, {
                withCredentials: true,
                headers: { 'Content-Type': 'multipart/form-data' }
            })
            toast('Foto enviada com sucesso!', 'success')
            fetchPhotos()
        } catch (error) {
            toast('Erro ao enviar foto.', 'error')
        } finally {
            setUploading(false)
        }
    }

    const handleDelete = async (photoId) => {
        if (!isEditable) return
        if (!confirm('Remover esta foto?')) return
        try {
            await axios.delete(`${API_URL}/${serviceOrderId}/photos/${photoId}`, { withCredentials: true })
            toast('Foto removida.', 'success')
            fetchPhotos()
        } catch (error) {
            toast('Erro ao remover foto.', 'error')
        }
    }

    return (
        <div className="photos-container">
            {isEditable ? (
                <div className="photo-upload-area">
                    <label className="photo-upload-label">
                        <Upload size={24} />
                        <span>{uploading ? 'Enviando...' : 'Clique para enviar uma foto'}</span>
                        <input
                            type="file"
                            accept="image/*"
                            onChange={handleUpload}
                            disabled={uploading}
                        />
                    </label>
                </div>
            ) : (
                <div className="status-warning" style={{ 
                    display: 'flex', 
                    alignItems: 'center', 
                    justifyContent: 'center', // Centralizado igual ao ListaPecas
                    gap: '0.5rem', 
                    padding: '1rem', // Aumentado para 1rem
                    backgroundColor: '#fef3c7', 
                    color: '#92400e', 
                    borderRadius: '6px', 
                    marginBottom: '1rem', 
                    fontSize: '0.9rem',
                    fontWeight: '500' // Fonte ligeiramente mais forte
                }}>
                    <Lock size={18} />
                    <span>{orderStatus === 'PAGO' ? 'OS Paga - Upload de fotos bloqueado' : 'Upload de fotos permitido'}</span>
                </div>
            )}

            {photos.length === 0 ? (
                <div className="photo-empty">
                    <Camera size={24} style={{ marginBottom: '0.5rem', opacity: 0.5 }} />
                    <p>Nenhuma foto registada para esta OS.</p>
                </div>
            ) : (
                <div className="photo-grid">
                    {photos.map(photo => (
                        <div key={photo.id} className="photo-item">
                            <img
                                src={`${API_URL}/${serviceOrderId}/photos/${photo.id}/view`}
                                alt={photo.fileName}
                                loading="lazy"
                            />
                            {isEditable && (
                                <div className="photo-overlay">
                                    <button
                                        className="photo-delete-btn"
                                        onClick={() => handleDelete(photo.id)}
                                    >
                                        <Trash2 size={16} />
                                    </button>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    )
}