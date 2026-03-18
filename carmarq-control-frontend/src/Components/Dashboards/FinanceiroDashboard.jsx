import React, { useState, useEffect } from 'react'
import { DollarSign, AlertCircle, ClipboardList, Loader2, Eye } from 'lucide-react'
import axios from 'axios'
import '../../Styles/Dashboards.css'

const API_URL = 'http://localhost:8080/api/dashboard/stats'

// Dashboard Financeiro — métricas de receita e monitoramento de OS
export function FinanceiroDashboard() {
    const [stats, setStats] = useState(null)
    const [loading, setLoading] = useState(true)
    const [month, setMonth] = useState('') // "Todos os meses" por padrão
    const [year, setYear] = useState(new Date().getFullYear())

    const fetchStats = async () => {
        try {
            setLoading(true)
            let url = `${API_URL}?year=${year}`
            if (month) url += `&month=${month}`
            const res = await axios.get(url, { withCredentials: true })
            setStats(res.data)
        } catch (error) {
            console.error('Erro ao carregar métricas', error)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchStats()
    }, [month, year])

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
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <h1 className="page-title" style={{ margin: 0 }}>Financeiro Carmarq</h1>
                
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <select 
                        value={month} 
                        onChange={(e) => setMonth(e.target.value === '' ? '' : parseInt(e.target.value))}
                        className="filter-select"
                        style={{ padding: '0.4rem', borderRadius: '4px', border: '1px solid #ddd' }}
                    >
                        <option value="">Todos os meses</option>
                        <option value={1}>Janeiro</option>
                        <option value={2}>Fevereiro</option>
                        <option value={3}>Março</option>
                        <option value={4}>Abril</option>
                        <option value={5}>Maio</option>
                        <option value={6}>Junho</option>
                        <option value={7}>Julho</option>
                        <option value={8}>Agosto</option>
                        <option value={9}>Setembro</option>
                        <option value={10}>Outubro</option>
                        <option value={11}>Novembro</option>
                        <option value={12}>Dezembro</option>
                    </select>

                    <select 
                        value={year} 
                        onChange={(e) => setYear(parseInt(e.target.value))}
                        className="filter-select"
                        style={{ padding: '0.4rem', borderRadius: '4px', border: '1px solid #ddd' }}
                    >
                        {[2024, 2025, 2026, 2027, 2028].map(y => (
                            <option key={y} value={y}>{y}</option>
                        ))}
                    </select>
                </div>
            </div>

            <div className="dashboard-grid">
                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">Receita Total</span>
                        <DollarSign className="stat-icon" size={20} color="#10b981" />
                    </div>
                    <span className="stat-value" style={{ color: '#10b981' }}>{formatCurrency(stats.totalRevenue)}</span>
                    <span className="stat-desc">Bruto de serviços concluídos</span>
                </div>

                <div className="stat-card" style={{ borderLeft: '4px solid #ef4444' }}>
                    <div className="stat-header">
                        <span className="stat-title">Despesas (Técnicos)</span>
                        <DollarSign className="stat-icon" size={20} color="#ef4444" style={{ transform: 'rotate(180deg)' }} />
                    </div>
                    <span className="stat-value" style={{ color: '#ef4444' }}>- {formatCurrency(stats.totalExpenses)}</span>
                    <span className="stat-desc">Repasses aos técnicos</span>
                </div>

                <div className="stat-card" style={{ borderLeft: '4px solid #10b981' }}>
                    <div className="stat-header">
                        <span className="stat-title">Lucro Líquido</span>
                        <DollarSign className="stat-icon" size={20} color="#059669" />
                    </div>
                    <span className="stat-value" style={{ color: '#059669' }}>{formatCurrency(stats.totalProfit)}</span>
                    <span className="stat-desc">Receita líquida da empresa</span>
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