import React, { useState, useEffect } from 'react'
import { Plus, Trash2, Loader2, Lock } from 'lucide-react'
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

    const [totalPecas, setTotalPecas] = useState(0)
    const isEditable = orderStatus === 'EM_ANDAMENTO'

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

    const handleAdd = async () => {
        if (!isEditable) return
        if (!nomePeca || !valorUnitario) {
            toast('Preencha nome e valor da peça.', 'error')
            return
        }
        try {
            await axios.post(`${API_URL}/${serviceOrderId}/parts`, {
                partName: nomePeca,
                quantity: parseInt(qtd),
                unitPrice: parseFloat(valorUnitario)
            }, { withCredentials: true })
            toast('Peça adicionada!', 'success')
            setNomePeca('')
            setQtd(1)
            setValorUnitario('')
            fetchParts()
            if (onUpdate) onUpdate()
        } catch (error) {
            toast('Erro ao adicionar peça.', 'error')
        }
    }

    const handleRemove = async (partId) => {
        if (!isEditable) return
        try {
            await axios.delete(`${API_URL}/${serviceOrderId}/parts/${partId}`, { withCredentials: true })
            toast('Peça removida.', 'success')
            fetchParts()
            if (onUpdate) onUpdate()
        } catch (error) {
            toast('Erro ao remover peça.', 'error')
        }
    }

    return (
        <div className="pecas-container">
            {isEditable ? (
                <div className="add-peca-form">
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
                    <button className="btn-add" onClick={handleAdd}>
                        <Plus size={20} />
                    </button>
                </div>
            ) : (
                <div className="status-warning" style={{ 
                    display: 'flex', 
                    alignItems: 'center', 
                    justifyContent: 'center', // Centraliza o conteúdo para preencher o espaço
                    gap: '0.5rem', 
                    padding: '1rem', // Aumenta o espaço (padding) igual a área de fotos
                    backgroundColor: '#fef3c7', 
                    color: '#92400e', 
                    borderRadius: '6px', 
                    marginBottom: '1rem', 
                    fontSize: '0.9rem',
                    fontWeight: '500'
                }}>
                    <Lock size={18} />
                    <span>Edição de peças bloqueada (OS deve estar em Andamento)</span>
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
                                    {isEditable && (
                                        <button className="btn-remove" onClick={() => handleRemove(peca.id)}>
                                            <Trash2 size={16} />
                                        </button>
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