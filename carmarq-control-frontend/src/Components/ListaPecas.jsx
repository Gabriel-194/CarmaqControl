import React, { useState, useEffect } from 'react'
import { Plus, Trash2, Loader2, Lock, PenTool, CheckCircle } from 'lucide-react'
import axios from 'axios'
import { toast } from './ui/Toaster'
import '../Styles/ListaPecas.css'

const API_URL = 'http://localhost:8080/api/service-orders'

// Componente de peças integrado com API real — recebe serviceOrderId e orderStatus como prop
export default function ListaPecas({ serviceOrderId, orderStatus, onUpdate }) {
    const [pecas, setPecas] = useState([])
    const [loading, setLoading] = useState(true)
    const [nomePeca, setNomePeca] = useState('')
    const [qtd, setQtd] = useState(1)
    const [valorUnitario, setValorUnitario] = useState('')
    
    // Estados para edição
    const [isEditing, setIsEditing] = useState(false)
    const [editingId, setEditingId] = useState(null)

    const [totalPecas, setTotalPecas] = useState(0)
    
    // Travas: Peças podem ser editadas/adicionadas em qualquer status não finalizado.
    const isLocked = orderStatus === 'PAGO' || orderStatus === 'CANCELADA'
    const isEditable = !isLocked

    const fetchParts = async () => {
        try {
            const res = await axios.get(`${API_URL}/${serviceOrderId}/parts`, { withCredentials: true })
            setPecas(res.data.parts || [])
            setTotalPecas(res.data.totalValue || 0)
        } catch (error) {
            console.error('Erro ao carregar peças', error)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        if (serviceOrderId) fetchParts()
    }, [serviceOrderId])

    const handleSave = async () => {
        if (isLocked) return
        if (!nomePeca || !valorUnitario) {
            toast('Preencha nome e valor da peça.', 'error')
            return
        }
        try {
            const payload = {
                partName: nomePeca,
                quantity: parseInt(qtd),
                unitPrice: parseFloat(valorUnitario)
            }

            if (isEditing) {
                await axios.put(`${API_URL}/${serviceOrderId}/parts/${editingId}`, payload, { withCredentials: true })
                toast('Peça atualizada!', 'success')
            } else {
                await axios.post(`${API_URL}/${serviceOrderId}/parts`, payload, { withCredentials: true })
                toast('Peça adicionada!', 'success')
            }

            resetForm()
            fetchParts()
            if (onUpdate) onUpdate()
        } catch (error) {
            toast(error.response?.data?.message || 'Erro ao salvar peça.', 'error')
        }
    }

    const resetForm = () => {
        setNomePeca('')
        setQtd(1)
        setValorUnitario('')
        setIsEditing(false)
        setEditingId(null)
    }

    const handleEditClick = (peca) => {
        setNomePeca(peca.partName)
        setQtd(peca.quantity)
        setValorUnitario(peca.unitPrice)
        setEditingId(peca.id)
        setIsEditing(true)
    }

    const handleRemove = async (partId) => {
        if (isLocked) return
        if (!window.confirm('Excluir esta peça?')) return
        try {
            await axios.delete(`${API_URL}/${serviceOrderId}/parts/${partId}`, { withCredentials: true })
            toast('Peça removida.', 'success')
            fetchParts()
            if (onUpdate) onUpdate()
        } catch (error) {
            toast('Erro ao remover peça.', 'error')
        }
    }

    if (loading) return <div style={{ textAlign: 'center', padding: '1rem' }}><Loader2 className="animate-spin" size={20} /></div>

    return (
        <div className="pecas-container">
            {isEditable && !isLocked ? (
                <div className="add-peca-form" style={{ backgroundColor: isEditing ? 'var(--primary-light)' : 'transparent', padding: isEditing ? '1rem' : '0', borderRadius: '8px', border: isEditing ? '1px solid var(--primary-color)' : 'none', marginBottom: isEditing ? '1rem' : '0' }}>
                    {isEditing && <h4 style={{ width: '100%', marginBottom: '0.5rem', color: 'var(--primary-color)' }}>Editando Peça</h4>}
                    <input
                        type="text"
                        placeholder="Nome da peça..."
                        className="input-peca"
                        value={nomePeca}
                        onChange={e => setNomePeca(e.target.value)}
                    />
                    <input
                        type="number"
                        placeholder="Qtd"
                        className="input-qtd"
                        min="1"
                        value={qtd}
                        onChange={e => setQtd(e.target.value)}
                    />
                    <input
                        type="number"
                        placeholder="R$ Unitário"
                        className="input-qtd"
                        step="0.01"
                        min="0"
                        value={valorUnitario}
                        onChange={e => setValorUnitario(e.target.value)}
                        style={{ width: '120px' }}
                    />
                    <button className="btn-add" onClick={handleSave}>
                        {isEditing ? <CheckCircle size={20} /> : <Plus size={20} />}
                    </button>
                    {isEditing && (
                        <button className="btn-secondary" onClick={resetForm} style={{ padding: '0.4rem', borderRadius: '4px', marginLeft: '0.5rem' }}>
                            Cancelar
                        </button>
                    )}
                </div>
            ) : (
                <div className="status-warning" style={{ 
                    display: 'flex', 
                    alignItems: 'center', 
                    justifyContent: 'center',
                    gap: '0.5rem', 
                    padding: '1rem',
                    backgroundColor: isLocked ? '#fee2e2' : '#fef3c7', 
                    color: isLocked ? '#991b1b' : '#92400e', 
                    borderRadius: '6px', 
                    marginBottom: '1rem', 
                    fontSize: '0.9rem',
                    fontWeight: '500'
                }}>
                    <Lock size={18} />
                    <span>{isLocked ? `OS ${orderStatus} - Edição bloqueada` : 'Edição permitida'}</span>
                </div>
            )}

            {pecas.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '1.5rem', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                    Nenhuma peça registrada.
                </div>
            ) : (
                <>
                    <ul className="lista-pecas">
                        {pecas.map(peca => (
                            <li key={peca.id} className="peca-item">
                                <div className="peca-info">
                                    <span className="peca-nome">{peca.partName}</span>
                                    <span className="peca-valor">R$ {peca.unitPrice.toFixed(2)} un.</span>
                                </div>
                                <div className="peca-actions">
                                    <span className="badge-qtd">{peca.quantity}x</span>
                                    <span className="peca-total">R$ {peca.totalPrice.toFixed(2)}</span>
                                    {isEditable && !isLocked && (
                                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                                            <button onClick={() => handleEditClick(peca)} style={{ background: 'none', border: 'none', color: 'var(--primary-color)', cursor: 'pointer', display: 'flex', alignItems: 'center' }}>
                                                <PenTool size={16} />
                                            </button>
                                            <button className="btn-remove" onClick={() => handleRemove(peca.id)}>
                                                <Trash2 size={16} />
                                            </button>
                                        </div>
                                    )}
                                </div>
                            </li>
                        ))}
                    </ul>
                    <div style={{ textAlign: 'right', marginTop: '0.5rem', fontWeight: 'bold', color: 'var(--primary-color)' }}>
                        Total Peças: R$ {totalPecas.toFixed(2)}
                    </div>
                </>
            )}
        </div>
    )
}