import React, { useState, useEffect } from 'react'
import { Play, Square, Plus, Loader2 } from 'lucide-react'
import axios from 'axios'
import { toast } from './ui/Toaster'
import '../Styles/TabelaTempos.css'

const API_URL = 'http://localhost:8080/api/service-orders'

// Componente de tempos integrado com API real
export default function TabelaTempos({ serviceOrderId, userRole, osTipo, orderStatus, onUpdate }) {
    const isLocked = orderStatus === 'PAGO' || orderStatus === 'CANCELADA';
    const [registros, setRegistros] = useState([])
    const [loading, setLoading] = useState(true)
    const [timerType, setTimerType] = useState('TRABALHO')
    const [showManual, setShowManual] = useState(false)
    const [isEditing, setIsEditing] = useState(false)
    const [editingId, setEditingId] = useState(null)
    const [manualData, setManualData] = useState({
        type: 'TRABALHO',
        registeredDate: new Date().toISOString().split('T')[0],
        startTime: '',
        endTime: '',
        description: ''
    })

    const [totalFormatted, setTotalFormatted] = useState('00:00')

    const fetchTimes = async () => {
        try {
            const res = await axios.get(`${API_URL}/${serviceOrderId}/times`, { withCredentials: true })
            setRegistros(res.data.records || [])
            setTotalFormatted(res.data.totalFormatted || '00:00')
        } catch (error) {
            console.error('Erro ao carregar tempos', error)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        if (serviceOrderId) fetchTimes()
    }, [serviceOrderId])


    // Edição de registro existente
    const handleEditClick = (reg) => {
        setManualData({
            type: reg.type,
            registeredDate: reg.registeredDate || new Date().toISOString().split('T')[0],
            startTime: reg.startTime ? reg.startTime.substring(11, 16) : '',
            endTime: reg.endTime ? reg.endTime.substring(11, 16) : '',
            description: reg.description || ''
        })
        setEditingId(reg.id)
        setIsEditing(true)
        setShowManual(true)
        window.scrollTo({ top: 300, behavior: 'smooth' })
    }

    // Exclusão de registro
    const handleDeleteClick = async (id) => {
        if (!window.confirm('Tem certeza que deseja excluir este registro de tempo? (Irreversível)')) return
        try {
            await axios.delete(`${API_URL}/${serviceOrderId}/times/${id}`, { withCredentials: true })
            toast('Registro excluído com sucesso!', 'success')
            fetchTimes()
            if (onUpdate) onUpdate()
        } catch (error) {
            toast('Erro ao excluir registro.', 'error')
        }
    }

    // Lançamento manual ou salvamento de edição
    const handleManualSave = async () => {
        if (!manualData.registeredDate || !manualData.startTime || !manualData.endTime) {
            toast('Preencha data, início e fim.', 'error')
            return
        }
        try {
            const payload = {
                type: manualData.type,
                registeredDate: manualData.registeredDate,
                startTime: `${manualData.registeredDate}T${manualData.startTime}:00`,
                endTime: `${manualData.registeredDate}T${manualData.endTime}:00`,
                description: manualData.description
            }

            if (isEditing) {
                await axios.put(`${API_URL}/${serviceOrderId}/times/${editingId}`, payload, { withCredentials: true })
                toast('Tempo atualizado com sucesso!', 'success')
            } else {
                await axios.post(`${API_URL}/${serviceOrderId}/times`, payload, { withCredentials: true })
                toast('Tempo registrado!', 'success')
            }

            setShowManual(false)
            setIsEditing(false)
            setEditingId(null)
            setManualData({ type: 'TRABALHO', registeredDate: new Date().toISOString().split('T')[0], startTime: '', endTime: '', description: '' })
            fetchTimes()
            if (onUpdate) onUpdate()
        } catch (error) {
            toast('Erro ao salvar tempo.', 'error')
        }
    }

    // Mapeamento de tipos para label
    const typeLabels = {
        'SAIDA_SEDE': 'Saída da Sede',
        'SAIDA_HOTEL': 'Saída do Hotel',
        'CHEGADA_CLIENTE': 'Chegada ao Cliente',
        'TRABALHO': 'Trabalho',
        'RETORNO_HOTEL': 'Retorno ao Hotel',
        'RETORNO_SEDE': 'Retorno à Sede'
    }

    if (loading) return <div style={{ textAlign: 'center', padding: '1rem' }}><Loader2 className="animate-spin" size={20} /></div>

    if (osTipo === 'INSTALACAO' && userRole === 'TECNICO' && !registros.length && !showManual) {
        // Mostramos um aviso inicial apenas se não houver registros, mas permitimos o uso
        // para registrar deslocamento.
    }

    const isOrderLocked = registros.some(r => r.orderStatus === 'PAGO' || r.orderStatus === 'CANCELADA') 
        || (registros.length > 0 && registros[0].orderStatus === 'PAGO'); 
    // Nota: O backend já valida, mas aqui no UI usamos a prop orderStatus se disponível ou checamos via osTipo se necessário.
    // Na verdade, OrdemDetalhes passa orderStatus? Não, passa userRole e osTipo.
    // Vou adicionar a prop 'orderStatus' no OrdemDetalhes.jsx para facilitar aqui.

    return (
        <div className="times-container">
            {userRole === 'TECNICO' && (
                <div className="timer-control" style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
                    {!isLocked ? (
                        <>
                            <button 
                                className="btn-primary" 
                                onClick={() => {
                                    setIsEditing(false)
                                    setEditingId(null)
                                    setManualData({ type: 'TRABALHO', registeredDate: new Date().toISOString().split('T')[0], startTime: '', endTime: '', description: '' })
                                    setShowManual(!showManual)
                                }}
                            >
                                <Plus size={16} /> {showManual ? 'Cancelar Lançamento' : 'Novo Lançamento de Tempo'}
                            </button>
                            {!showManual && (
                                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', margin: 0 }}>
                                    O cronômetro automático foi removido. Utilize o lançamento manual para registrar suas horas.
                                </p>
                            )}
                        </>
                    ) : (
                        <div className="status-warning" style={{ 
                            display: 'flex', 
                            alignItems: 'center', 
                            gap: '0.5rem', 
                            padding: '0.75rem 1rem',
                            backgroundColor: '#fee2e2', 
                            color: '#991b1b', 
                            borderRadius: '6px', 
                            fontSize: '0.9rem',
                            fontWeight: '500'
                        }}>
                            <Plus size={18} opacity={0.5} />
                            <span>OS {orderStatus} - Lançamento de horas bloqueado</span>
                        </div>
                    )}
                </div>
            )}

            {/* Formulário de lançamento manual / edição */}
            {showManual && (
                <div style={{ background: '#f9fafb', padding: '1rem', borderRadius: '8px', marginBottom: '1rem', border: '1px solid var(--border-color)' }}>
                    <h4 style={{ marginBottom: '1rem', color: 'var(--text-color)' }}>
                        {isEditing ? 'Editar Registro de Tempo' : 'Novo Lançamento Manual'}
                    </h4>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                        <div>
                            <label style={{ fontSize: '0.8rem', fontWeight: 600 }}>Tipo</label>
                            <select className="form-input" value={manualData.type} onChange={e => setManualData({ ...manualData, type: e.target.value })}>
                                <option value="SAIDA_SEDE">Saída da Sede</option>
                                <option value="SAIDA_HOTEL">Saída do Hotel</option>
                                <option value="TRABALHO">Trabalho</option>
                                <option value="RETORNO_HOTEL">Retorno ao Hotel</option>
                                <option value="RETORNO_SEDE">Retorno à Sede</option>
                            </select>
                        </div>
                        <div>
                            <label style={{ fontSize: '0.8rem', fontWeight: 600 }}>Data Específica</label>
                            <input type="date" className="form-input" value={manualData.registeredDate} onChange={e => setManualData({ ...manualData, registeredDate: e.target.value })} />
                        </div>
                        <div>
                            <label style={{ fontSize: '0.8rem', fontWeight: 600 }}>Descrição</label>
                            <input type="text" className="form-input" placeholder="Descrição da atividade" value={manualData.description} onChange={e => setManualData({ ...manualData, description: e.target.value })} />
                        </div>
                        <div>
                            <label style={{ fontSize: '0.8rem', fontWeight: 600 }}>Início</label>
                            <input type="time" className="form-input" value={manualData.startTime} onChange={e => setManualData({ ...manualData, startTime: e.target.value })} />
                        </div>
                        <div>
                            <label style={{ fontSize: '0.8rem', fontWeight: 600 }}>Fim</label>
                            <input type="time" className="form-input" value={manualData.endTime} onChange={e => setManualData({ ...manualData, endTime: e.target.value })} />
                        </div>
                    </div>
                    <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                        <button className="btn-primary" style={{ fontSize: '0.85rem' }} onClick={handleManualSave}>
                            {isEditing ? 'Atualizar Registro' : 'Salvar Registro'}
                        </button>
                        {isEditing && (
                            <button className="btn-secondary" style={{ fontSize: '0.85rem' }} onClick={() => {
                                setIsEditing(false)
                                setEditingId(null)
                                setShowManual(false)
                                setManualData({ type: 'TRABALHO', registeredDate: new Date().toISOString().split('T')[0], startTime: '', endTime: '', description: '' })
                            }}>
                                Cancelar Edição
                            </button>
                        )}
                    </div>
                </div>
            )}

            <table className="simple-table">
                <thead>
                    <tr>
                        <th>Tipo</th>
                        <th>Descrição</th>
                        <th>Início</th>
                        <th>Fim</th>
                        <th className="text-right">Duração</th>
                        {userRole === 'TECNICO' && <th className="text-right">Ações</th>}
                    </tr>
                </thead>
                <tbody>
                    {registros.length === 0 ? (
                        <tr><td colSpan={5} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Nenhum registro de tempo.</td></tr>
                    ) : (
                        registros.map(reg => (
                            <tr key={reg.id}>
                                <td>
                                    <span style={{ fontSize: '0.8rem', background: '#f0fdf4', padding: '0.2rem 0.5rem', borderRadius: '4px', color: 'var(--primary-color)', display: 'block', marginBottom: '4px' }}>
                                        {typeLabels[reg.type] || reg.type}
                                    </span>
                                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                                        {reg.registeredDate ? reg.registeredDate.split('-').reverse().join('/') : ''}
                                    </span>
                                </td>
                                <td>{reg.description || '—'}</td>
                                <td>{reg.startTimeFormatted}</td>
                                <td>{reg.endTimeFormatted}</td>
                                <td className="text-right font-mono">{reg.durationFormatted}</td>
                                {userRole === 'TECNICO' && (
                                    <td className="text-right">
                                        <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                                            <button 
                                                onClick={() => handleEditClick(reg)} 
                                                style={{ padding: '0.2rem 0.5rem', fontSize: '0.75rem', color: 'var(--primary-color)', background: '#ecfdf5', border: '1px solid #a7f3d0', borderRadius: '4px', cursor: 'pointer' }}
                                            >
                                                Editar
                                            </button>
                                            <button 
                                                onClick={() => handleDeleteClick(reg.id)} 
                                                style={{ padding: '0.2rem 0.5rem', fontSize: '0.75rem', color: '#dc2626', background: '#fef2f2', border: '1px solid #fecaca', borderRadius: '4px', cursor: 'pointer' }}
                                            >
                                                Excluir
                                            </button>
                                        </div>
                                    </td>
                                )}
                            </tr>
                        ))
                    )}
                    {registros.length > 0 && (
                        <tr className="row-total">
                            <td colSpan={userRole === 'TECNICO' ? 4 : 4}>Total Trabalhado</td>
                            <td className="text-right font-bold">{totalFormatted}</td>
                            {userRole === 'TECNICO' && <td></td>}
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    )
}