import React, { useState, useEffect } from 'react'
import { ClipboardList, Clock, CheckCircle, Loader2, DollarSign, Wallet, Play, Check } from 'lucide-react'
import axios from 'axios'
import '../../Styles/Dashboards.css'
import { toast } from '../ui/Toaster'

const API_URL = 'http://localhost:8080/api/dashboard/stats'
const OS_API_URL = 'http://localhost:8080/api/service-orders'

// Dashboard do Técnico — vê apenas suas OS e métricas
export function TecnicoDashboard() {
    const [stats, setStats] = useState(null)
    const [loading, setLoading] = useState(true)

    const fetchStats = async () => {
        try {
            const res = await axios.get(API_URL, { withCredentials: true })
            setStats(res.data)
        } catch (error) {
            console.error('Erro ao carregar métricas', error)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchStats()
    }, [])

    const handleConfirmPayment = async (orderId) => {
        if (!window.confirm('Você confirma que recebeu o pagamento desta OS?')) return
        try {
            await axios.put(`${OS_API_URL}/${orderId}/mark-received`, {}, { withCredentials: true })
            toast('Recebimento confirmado!', 'success')
            fetchStats() // Recarrega para atualizar os cards de valores
        } catch (error) {
            toast(error.response?.data?.message || 'Erro ao confirmar recebimento.', 'error')
        }
    }

    // Mapeia status para label e classe CSS (Padronizado)
    const statusMap = {
        'ABERTA': { label: 'Aberta', css: 'status-aberto' },
        'EM_ANDAMENTO': { label: 'Em Andamento', css: 'status-em-andamento' },
        'CONCLUIDA': { label: 'Concluída', css: 'status-concluido' },
        'CANCELADA': { label: 'Cancelada', css: 'status-cancelada' },
        'REQUER_INSPECAO': { label: 'Requer Inspeção', css: 'status-inspecao' }
    }

    if (loading) return <div style={{ textAlign: 'center', padding: '3rem' }}><Loader2 className="animate-spin" size={32} /></div>
    if (!stats) return <div style={{ padding: '2rem' }}>Erro ao carregar dados.</div>

    const formatCurrency = (val) => {
        return (val || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
    }

    return (
        <div>
            <h1 className="page-title" style={{ marginBottom: '1.5rem' }}>Meu Dashboard</h1>

            <div className="dashboard-grid">
                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">A Receber</span>
                        <Clock className="stat-icon" size={20} color="#f59e0b" />
                    </div>
                    <span className="stat-value">{formatCurrency(stats.technicianPendingPayment)}</span>
                    <span className="stat-desc">Aguardando confirmação</span>
                </div>

                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">Já Recebido</span>
                        <Wallet className="stat-icon" size={20} color="#10b981" />
                    </div>
                    <span className="stat-value" style={{ color: '#10b981' }}>{formatCurrency(stats.technicianEarnings)}</span>
                    <span className="stat-desc">Total acumulado</span>
                </div>

                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">Ordens Totais</span>
                        <ClipboardList className="stat-icon" size={20} color="var(--primary-color)" />
                    </div>
                    <span className="stat-value">{stats.totalOrders || 0}</span>
                </div>

                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">Em Atendimento</span>
                        <Play className="stat-icon" size={20} color="#f59e0b" />
                    </div>
                    <span className="stat-value">{stats.inProgressOrders || 0}</span>
                    <span className="stat-desc">{stats.openOrders || 0} novas</span>
                </div>
            </div>

            <div className="dashboard-section">
                <h3 className="section-title">Suas Últimas OS</h3>
                <ul className="dashboard-list">
                    {(stats.recentOrders || []).length === 0 ? (
                        <li className="list-item" style={{ color: 'var(--text-muted)', justifyContent: 'center' }}>
                            Nenhuma OS atribuída.
                        </li>
                    ) : (
                        stats.recentOrders.map(order => (
                            <li key={order.id} className="list-item">
                                <div className="item-info">
                                    <h4>OS #{order.id} — {order.machineName}</h4>
                                    <p>{order.clientName} • {order.openedAt}</p>
                                </div>
                                <div className="item-actions" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                                    {order.status === 'CONCLUIDA' && order.technicianPaymentStatus === 'A_RECEBER' && (
                                        <button 
                                            className="btn-success" 
                                            onClick={() => handleConfirmPayment(order.id)}
                                            title="Confirmar Recebimento"
                                            style={{ 
                                                padding: '4px 8px', 
                                                fontSize: '0.75rem', 
                                                display: 'flex', 
                                                alignItems: 'center', 
                                                gap: '4px',
                                                borderRadius: '4px',
                                                border: 'none',
                                                backgroundColor: '#10b981',
                                                color: 'white',
                                                cursor: 'pointer'
                                            }}
                                        >
                                            <Check size={14} /> Confirmar Pgto
                                        </button>
                                    )}
                                    <span className={`status-badge ${statusMap[order.status]?.css || ''}`}>
                                        {statusMap[order.status]?.label || order.status}
                                    </span>
                                </div>
                            </li>
                        ))
                    )}
                </ul>
            </div>
        </div>
    )
}