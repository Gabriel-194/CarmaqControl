import React, { useState, useEffect } from 'react'
import { DollarSign, Users, ClipboardList, TrendingUp, Loader2, BarChart3 } from 'lucide-react'
import axios from 'axios'
import '../../Styles/Dashboards.css'

const API_URL = 'http://localhost:8080/api/dashboard/stats'

// Dashboard do Proprietário — métricas reais do sistema
export function ProprietarioDashboard() {
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
            <h1 className="page-title" style={{ marginBottom: '1.5rem' }}>Visão Geral</h1>

            <div className="dashboard-grid">
                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">Faturamento Total</span>
                        <DollarSign className="stat-icon" size={20} color="#6b7280" />
                    </div>
                    <span className="stat-value" style={{ color: '#6b7280' }}>{formatCurrency(stats.totalRevenue)}</span>
                    <span className="stat-desc">
                        Bruto de serviços concluídos
                    </span>
                </div>

                <div className="stat-card" style={{ borderLeft: '4px solid #10b981' }}>
                    <div className="stat-header">
                        <span className="stat-title">Lucro Líquido (90%)</span>
                        <TrendingUp className="stat-icon" size={20} color="#10b981" />
                    </div>
                    <span className="stat-value" style={{ color: '#10b981' }}>{formatCurrency(stats.totalProfit)}</span>
                    <span className="stat-desc">
                        Após repasse aos técnicos
                    </span>
                </div>

                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">Ordens Ativas</span>
                        <ClipboardList className="stat-icon" size={20} color="#10b981" />
                    </div>
                    <span className="stat-value">{(stats.openOrders || 0) + (stats.inProgressOrders || 0)}</span>
                    <span className="stat-desc">{stats.openOrders || 0} abertas, {stats.inProgressOrders || 0} em andamento</span>
                </div>

                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">Equipe</span>
                        <Users className="stat-icon" size={20} color="#10b981" />
                    </div>
                    <span className="stat-value">{stats.totalTechnicians || 0}</span>
                    <span className="stat-desc">{stats.totalClients || 0} clientes cadastrados</span>
                </div>
            </div>

            <div className="dashboard-section">
                <h3 className="section-title">Últimas Atividades</h3>
                <ul className="dashboard-list">
                    {(stats.recentOrders || []).length === 0 ? (
                        <li className="list-item" style={{ color: 'var(--text-muted)', justifyContent: 'center' }}>
                            Nenhuma atividade recente.
                        </li>
                    ) : (
                        stats.recentOrders.map(order => (
                            <li key={order.id} className="list-item">
                                <div className="item-info">
                                    <h4>OS #{order.id} — {order.machineName}</h4>
                                    <p>{order.clientName} • {order.technicianName}</p>
                                </div>
                                {order.totalValue ? (
                                    <span style={{ fontWeight: 'bold', color: '#10b981' }}>
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