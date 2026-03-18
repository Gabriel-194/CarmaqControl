import React, { useState, useEffect } from 'react'
import { Play, Square, Plus, Loader2 } from 'lucide-react'
import axios from 'axios'
import { toast } from './ui/Toaster'
import '../Styles/TabelaTempos.css'

const API_URL = 'http://localhost:8080/api/service-orders'

// Componente de tempos integrado com API real — recebe serviceOrderId como prop
export default function TabelaTempos({ serviceOrderId }) {
    const [registros, setRegistros] = useState([])
    const [loading, setLoading] = useState(true)
    const [isTimerRunning, setIsTimerRunning] = useState(false)
    const [showManual, setShowManual] = useState(false)
    const [manualData, setManualData] = useState({
        type: 'TRABALHO',
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
            
            // Verifica se há algum timer rodando (sem endTime)
            const hasRunning = (res.data.records || []).some(r => !r.endTime)
            setIsTimerRunning(hasRunning)
        } catch (error) {
            console.error('Erro ao carregar tempos', error)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        if (serviceOrderId) fetchTimes()
    }, [serviceOrderId])

    // Inicia um cronômetro (registra hora de início)
    const handleStartTimer = async () => {
        try {
            await axios.post(`${API_URL}/${serviceOrderId}/times`, {
                type: 'TRABALHO',
                startTime: new Date().toISOString(),
                description: 'Atividade em andamento'
            }, { withCredentials: true })
            setIsTimerRunning(true)
            toast('Cronômetro iniciado!', 'success')
            fetchTimes()
        } catch (error) {
            toast('Erro ao iniciar cronômetro.', 'error')
        }
    }

    // Para o cronômetro mais recente sem endTime
    const handleStopTimer = async () => {
        const running = registros.find(r => !r.endTime)
        if (running) {
            try {
                await axios.put(`${API_URL}/${serviceOrderId}/times/${running.id}`, {
                    endTime: new Date().toISOString()
                }, { withCredentials: true })
                setIsTimerRunning(false)
                toast('Atividade finalizada!', 'success')
                fetchTimes()
            } catch (error) {
                toast('Erro ao parar cronômetro.', 'error')
            }
        }
    }

    // Lançamento manual de tempo
    const handleManualSave = async () => {
        if (!manualData.startTime || !manualData.endTime) {
            toast('Preencha início e fim.', 'error')
            return
        }
        try {
            await axios.post(`${API_URL}/${serviceOrderId}/times`, {
                type: manualData.type,
                startTime: manualData.startTime,
                endTime: manualData.endTime,
                description: manualData.description
            }, { withCredentials: true })
            toast('Tempo registrado!', 'success')
            setShowManual(false)
            setManualData({ type: 'TRABALHO', startTime: '', endTime: '', description: '' })
            fetchTimes()
        } catch (error) {
            toast('Erro ao salvar tempo.', 'error')
        }
    }

    // Mapeamento de tipos para label
    const typeLabels = {
        'SAIDA_SEDE': 'Saída da Sede',
        'CHEGADA_CLIENTE': 'Chegada ao Cliente',
        'TRABALHO': 'Trabalho',
        'RETORNO_SEDE': 'Retorno à Sede'
    }

    if (loading) return <div style={{ textAlign: 'center', padding: '1rem' }}><Loader2 className="animate-spin" size={20} /></div>

    return (
        <div className="times-container">
            <div className="timer-control">
                {!isTimerRunning ? (
                    <button className="btn-timer btn-start" onClick={handleStartTimer}>
                        <Play size={20} /> Iniciar Cronômetro
                    </button>
                ) : (
                    <button className="btn-timer btn-stop" onClick={handleStopTimer}>
                        <Square size={20} /> Parar Atividade
                    </button>
                )}

                <button className="btn-secondary btn-manual" onClick={() => setShowManual(!showManual)}>
                    <Plus size={16} /> Lançamento Manual
                </button>
            </div>

            {/* Formulário de lançamento manual */}
            {showManual && (
                <div style={{ background: '#f9fafb', padding: '1rem', borderRadius: '8px', marginBottom: '1rem', border: '1px solid var(--border-color)' }}>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                        <div>
                            <label style={{ fontSize: '0.8rem', fontWeight: 600 }}>Tipo</label>
                            <select className="form-input" value={manualData.type} onChange={e => setManualData({ ...manualData, type: e.target.value })}>
                                <option value="SAIDA_SEDE">Saída da Sede</option>
                                <option value="CHEGADA_CLIENTE">Chegada ao Cliente</option>
                                <option value="TRABALHO">Trabalho</option>
                                <option value="RETORNO_SEDE">Retorno à Sede</option>
                            </select>
                        </div>
                        <div>
                            <label style={{ fontSize: '0.8rem', fontWeight: 600 }}>Descrição</label>
                            <input type="text" className="form-input" placeholder="Descrição da atividade" value={manualData.description} onChange={e => setManualData({ ...manualData, description: e.target.value })} />
                        </div>
                        <div>
                            <label style={{ fontSize: '0.8rem', fontWeight: 600 }}>Início</label>
                            <input type="datetime-local" className="form-input" value={manualData.startTime} onChange={e => setManualData({ ...manualData, startTime: e.target.value })} />
                        </div>
                        <div>
                            <label style={{ fontSize: '0.8rem', fontWeight: 600 }}>Fim</label>
                            <input type="datetime-local" className="form-input" value={manualData.endTime} onChange={e => setManualData({ ...manualData, endTime: e.target.value })} />
                        </div>
                    </div>
                    <button className="btn-primary" style={{ marginTop: '0.75rem', fontSize: '0.85rem' }} onClick={handleManualSave}>
                        Salvar Registro
                    </button>
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
                    </tr>
                </thead>
                <tbody>
                    {registros.length === 0 ? (
                        <tr><td colSpan={5} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Nenhum registro de tempo.</td></tr>
                    ) : (
                        registros.map(reg => (
                            <tr key={reg.id}>
                                <td><span style={{ fontSize: '0.8rem', background: '#f0fdf4', padding: '0.2rem 0.5rem', borderRadius: '4px', color: 'var(--primary-color)' }}>{typeLabels[reg.type] || reg.type}</span></td>
                                <td>{reg.description || '—'}</td>
                                <td>{reg.startTimeFormatted}</td>
                                <td>{reg.endTimeFormatted}</td>
                                <td className="text-right font-mono">{reg.durationFormatted}</td>
                            </tr>
                        ))
                    )}
                    {registros.length > 0 && (
                        <tr className="row-total">
                            <td colSpan={4}>Total Trabalhado</td>
                            <td className="text-right font-bold">{totalFormatted}</td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    )
}