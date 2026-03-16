import React, { useState, useEffect } from 'react'
import { DollarSign, AlertCircle, ClipboardList, Loader2, Eye } from 'lucide-react'
import axios from 'axios'
import '../../Styles/Dashboards.css'

const API_URL = 'http://localhost:8080/api/dashboard/stats'

// Dashboard Financeiro — métricas de receita e monitoramento de OS
export function FinanceiroDashboard() {
    const [stats, setStats] = useState(null)
    const [loading, setLoading] = useState(true)

    useEffect(() => {
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
        fetchStats()
    }, [])

    if (loading) return <div style={{ textAlign: 'center', padding: '3rem' }}><Loader2 className="animate-spin" size={32} /></div>
    if (!stats) return <div style={{ padding: '2rem' }}>Erro ao carregar dados.</div>

    const formatCurrency = (val) => {
        return (val || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
    }

    // Mapeia status para label e classe CSS (Padronizado)
    const statusMap = {
        'ABERTA': { label: 'Aberta', css: 'status-aberto' },
        'EM_ANDAMENTO': { label: 'Em Andamento', css: 'status-em-andamento' },
        'CONCLUIDA': { label: 'Concluída', css: 'status-concluido' },
        'CANCELADA': { label: 'Cancelada', css: 'status-cancelada' },
        'REQUER_INSPECAO': { label: 'Requer Inspeção', css: 'status-inspecao' }
    }

    return (
        <div>
            <h1 className="page-title" style={{ marginBottom: '1.5rem' }}>Financeiro Carmarq</h1>

            <div className="dashboard-grid">
                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">Receita Total</span>
                        <DollarSign className="stat-icon" size={20} color="#10b981" />
                    </div>
                    <span className="stat-value">{formatCurrency(stats.totalRevenue)}</span>
                    <span className="stat-desc">{formatCurrency(stats.monthlyRevenue)} este mês</span>
                </div>

                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">OS Concluídas</span>
                        <ClipboardList className="stat-icon" size={20} color="#10b981" />
                    </div>
                    <span className="stat-value">{stats.completedOrders || 0}</span>
                    <span className="stat-desc">Serviços finalizados</span>
                </div>

                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">Requer Inspeção</span>
                        <AlertCircle className="stat-icon" size={20} color="#ef4444" />
                    </div>
                    <span className="stat-value">{stats.requiresInspectionOrders || 0}</span>
                    <span className="stat-desc" style={{ color: '#ef4444' }}>Necessitam atenção</span>
                </div>
            </div>

            <div className="dashboard-section">
                <h3 className="section-title">Movimentação Recente</h3>
                <ul className="dashboard-list">
                    {(stats.recentOrders || []).length === 0 ? (
                        <li className="list-item" style={{ color: 'var(--text-muted)', justifyContent: 'center' }}>
                            Nenhuma movimentação recente.
                        </li>
                    ) : (
                        stats.recentOrders.map(order => (
                            <li key={order.id} className="list-item">
                                <div className="item-info">
                                    <h4>OS #{order.id} — {order.clientName}</h4>
                                    <p>{order.technicianName} • {order.openedAt}</p>
                                </div>
                                {order.totalValue ? (
                                    <span style={{ color: '#10b981', fontWeight: 'bold' }}>
                                        {formatCurrency(order.totalValue)}
                                    </span>
                                ) : (
                                    <span className={`status-badge ${statusMap[order.status]?.css || ''}`}>
                                        {statusMap[order.status]?.label || order.status}
                                    </span>
                                )}
                            </li>
                        ))
                    )}
                </ul>
            </div>
        </div>
    )
}