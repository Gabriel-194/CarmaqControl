import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Sidebar from '../Components/Sidebar'
import { useAuth } from '../contexts/AuthContext'
import { Search, Filter, Plus, Loader2, ClipboardList, Download } from 'lucide-react'
import axios from 'axios'
import { statusMap } from '../utils/statusUtils'
import '../Styles/Ordens.css'

const API_URL = 'http://localhost:8080/api/service-orders'

// Página de listagem de Ordens de Serviço — conectada com API real
export default function Ordens() {
    const navigate = useNavigate()
    const { user } = useAuth()
    const [ordens, setOrdens] = useState([])
    const [loading, setLoading] = useState(true)
    const [searchTerm, setSearchTerm] = useState('')
    const [statusFilter, setStatusFilter] = useState('')
    const [month, setMonth] = useState('')
    const [year, setYear] = useState(new Date().getFullYear())
    const [page, setPage] = useState(0)
    const [totalPages, setTotalPages] = useState(0)

    const fetchOrdens = async () => {
        try {
            setLoading(true)
            const params = {
                page: page,
                size: 10,
                search: searchTerm || undefined,
                status: statusFilter || undefined,
                month: month || undefined,
                year: year || undefined
            }
            
            const res = await axios.get(API_URL, { params, withCredentials: true })
            setOrdens(res.data.content || [])
            setTotalPages(res.data.totalPages || 0)
        } catch (error) {
            console.error('Erro ao carregar ordens', error)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        const delayDebounceFn = setTimeout(() => {
            fetchOrdens()
        }, 400)

        return () => clearTimeout(delayDebounceFn)
    }, [searchTerm, statusFilter, month, year, page])

    const filtered = ordens



    const formatDate = (dateStr) => {
        if (!dateStr) return '—'
        return new Date(dateStr).toLocaleDateString('pt-BR')
    }

    const handleExportExcel = async () => {
        try {
            const params = {
                search: searchTerm || undefined,
                status: statusFilter || undefined,
                month: month || undefined,
                year: year || undefined
            }
            const response = await axios.get(`${API_URL}/export-excel`, {
                params,
                responseType: 'blob',
                withCredentials: true
            })
            const url = window.URL.createObjectURL(new Blob([response.data]))
            const link = document.createElement('a')
            link.href = url
            link.setAttribute('download', 'Relatorio_Ordens_de_Servico.xlsx')
            document.body.appendChild(link)
            link.click()
            link.remove()
        } catch (error) {
            console.error('Erro ao exportar excel', error)
        }
    }

    return (
        <div className="dashboard-layout">
            <Sidebar />
            <main className="dashboard-content">
                <header className="page-header">
                    <div>
                        <h1 className="page-title">Ordens de Serviço</h1>
                        <p className="page-subtitle">Gerencie e acompanhe as solicitações</p>
                    </div>
                    {(user?.role === 'PROPRIETARIO' || user?.role === 'TECNICO') && (
                        <button
                            className="btn-primary btn-success"
                            onClick={() => navigate('/nova-os')}
                        >
                            <Plus size={20} /> Nova OS
                        </button>
                    )}
                </header>

                <div className="filters-bar" style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem' }}>
                    <div className="search-group" style={{ flex: 1, minWidth: '250px' }}>
                        <Search className="search-icon" />
                        <input
                            type="text"
                            placeholder="Buscar por cliente, chamado, ID ou técnico..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="search-input"
                        />
                    </div>
                    
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                        {(user?.role === 'PROPRIETARIO' || user?.role === 'FINANCEIRO') && (
                            <button
                                className="btn-secondary"
                                onClick={handleExportExcel}
                                style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', height: '100%', padding: '0 1rem' }}
                                title="Exportar Tabela de Ordens em Planilha Excel"
                            >
                                <Download size={18} /> Excel
                            </button>
                        )}
                        <select
                            className="btn-secondary"
                            value={statusFilter}
                            onChange={(e) => setStatusFilter(e.target.value)}
                            style={{ cursor: 'pointer', height: '100%' }}
                        >
                            <option value="">Todos os Status</option>
                            <option value="ABERTA">Aberta</option>
                            <option value="EM_ANDAMENTO">Em Andamento</option>
                            <option value="CONCLUIDA">Concluída</option>
                            <option value="CANCELADA">Cancelada</option>
                            <option value="COM_PROBLEMA">Com Problema</option>
                            <option value="REQUER_INSPECAO">Requer Inspeção</option>
                        </select>

                        <select 
                            value={month} 
                            onChange={(e) => setMonth(e.target.value)}
                            className="btn-secondary"
                            style={{ cursor: 'pointer', height: '100%' }}
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
                            className="btn-secondary"
                            style={{ cursor: 'pointer', height: '100%' }}
                        >
                            {[2024, 2025, 2026, 2027, 2028].map(y => (
                                <option key={y} value={y}>{y}</option>
                            ))}
                        </select>
                    </div>
                </div>

                {loading ? (
                    <div style={{ textAlign: 'center', padding: '3rem' }}>
                        <Loader2 className="animate-spin" size={32} /> Carregando...
                    </div>
                ) : filtered.length === 0 ? (
                    <div className="empty-state">
                        <ClipboardList size={48} style={{ color: 'var(--text-muted)' }} />
                        <p>Nenhuma ordem de serviço encontrada.</p>
                    </div>
                ) : (
                    <div className="table-container">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Chamado</th>
                                    <th>Cliente</th>
                                    <th>Tipo</th>
                                    <th>Origem</th>
                                    <th>Máquina</th>
                                    <th>Técnico</th>
                                    <th>Data</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.map((os) => (
                                    <tr key={os.id} onClick={() => navigate(`/ordens/${os.id}`)}>
                                        <td>#{os.id}</td>
                                        <td className="font-mono" style={{ fontSize: '0.85rem' }}>{os.numeroChamado || '—'}</td>
                                        <td className="font-bold">{os.clientName}</td>
                                        <td>
                                            <span style={{ 
                                                fontSize: '0.75rem', 
                                                padding: '0.2rem 0.5rem', 
                                                borderRadius: '4px',
                                                backgroundColor: os.serviceType === 'INSTALACAO' ? '#eff6ff' : '#fff7ed',
                                                color: os.serviceType === 'INSTALACAO' ? '#1e40af' : '#9a3412',
                                                fontWeight: '600'
                                            }}>
                                                {os.serviceType === 'INSTALACAO' ? 'Instalação' : 'Manutenção'}
                                            </span>
                                        </td>
                                        <td>
                                            {os.serviceType === 'MANUTENCAO' ? (
                                                <span style={{ 
                                                    fontSize: '0.75rem', 
                                                    color: os.manutencaoOrigin === 'VALENTIM' ? '#059669' : '#0284c7',
                                                    fontWeight: '500'
                                                }}>
                                                    {os.manutencaoOrigin === 'VALENTIM' ? 'Garantia' : 'Carmarq'}
                                                </span>
                                            ) : '—'}
                                        </td>
                                        <td>{os.machineName}</td>
                                        <td>{os.technicianName}</td>
                                        <td style={{ whiteSpace: 'nowrap' }}>{formatDate(os.serviceDate)}</td>
                                        <td>
                                            <span className={`status-badge ${statusMap[os.status]?.css || ''}`}>
                                                {statusMap[os.status]?.label || os.status}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>

                        <div className="pagination-controls" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '1rem', marginTop: '1.5rem', padding: '1rem' }}>
                            <button 
                                className="btn-secondary" 
                                onClick={() => setPage(prev => Math.max(0, prev - 1))}
                                disabled={page === 0 || loading}
                            >
                                Anterior
                            </button>
                            <span style={{ fontWeight: '500' }}>Página {page + 1} de {totalPages}</span>
                            <button 
                                className="btn-secondary" 
                                onClick={() => setPage(prev => prev + 1)}
                                disabled={(page + 1) >= totalPages || loading}
                            >
                                Próxima
                            </button>
                        </div>
                    </div>
                )}
            </main>
        </div>
    )
}