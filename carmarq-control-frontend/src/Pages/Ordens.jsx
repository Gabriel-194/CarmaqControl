import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Sidebar from '../Components/Sidebar'
import { useAuth } from '../contexts/AuthContext'
import { Search, Filter, Plus, Loader2, ClipboardList, Download } from 'lucide-react'
import axios from 'axios'
import { statusMap } from '../utils/statusUtils'
import '../Styles/Ordens.css'

const API_URL = 'http://localhost:8080/api/service-orders'

const typeLabels = {
    LASER_CHAPA: 'Laser Chapa',
    LASER_TUBO: 'Laser Tubo',
    DOBRADEIRA: 'Dobradeira',
    GUILHOTINA: 'Guilhotina',
    CURVADORA_TUBO: 'Curvadora de Tubo',
    METALEIRA: 'Metaleira',
    CALANDRA: 'Calandra',
    GRAVADORA_LASER: 'Gravadora a Laser',
}

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

    return (
        <div className="dashboard-layout ordens-page">
            <Sidebar />
            <main className="dashboard-content">
                <header className="page-header" style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', justifyContent: 'space-between' }}>
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
                            placeholder="Buscar por cliente, chamado, código OS, técnico ou tipo de máquina..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="search-input"
                        />
                    </div>

                    <div style={{ display: 'flex', gap: '0.5rem' }}>

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
                    <div className="table-container responsive-table-wrapper">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Código</th>
                                    <th>Chamado</th>
                                    <th>Cliente</th>
                                    <th>Tipo</th>
                                    <th>Origem</th>
                                    <th>Máquina</th>
                                    <th>Técnico</th>
                                    <th>Data</th>
                                    {(user?.role === 'PROPRIETARIO' || user?.role === 'FINANCEIRO') ? (
                                        <>
                                            <th style={{ textAlign: 'right' }}>Total (R$)</th>
                                            <th style={{ textAlign: 'right' }}>Lucro (R$)</th>
                                        </>
                                    ) : (
                                        <th style={{ textAlign: 'right' }}>Seu Pagamento (R$)</th>
                                    )}
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.map((os) => (
                                    <tr key={os.id} onClick={() => navigate(`/ordens/${os.id}`)}>
                                        <td className="font-bold">{os.osCode || `#${os.id}`}</td>
                                        <td className="font-mono" style={{ fontSize: '0.85rem' }}>{os.numeroChamado || '—'}</td>
                                        <td className="font-bold">{os.clientName}</td>
                                        <td>
                                            <span className={`service-type-badge ${os.serviceType === 'INSTALACAO' ? 'service-type-installation' : 'service-type-maintenance'}`}>
                                                {os.serviceType === 'INSTALACAO' ? 'Instalação' : 'Manutenção'}
                                            </span>
                                        </td>
                                        <td>
                                            <span style={{
                                                fontSize: '0.75rem',
                                                color: (os.serviceType === 'INSTALACAO' || os.manutencaoOrigin === 'VALENTIM') ? '#059669' : '#0284c7',
                                                fontWeight: '700'
                                            }}>
                                                {os.serviceType === 'INSTALACAO' ? 'VALENTIM' : (os.manutencaoOrigin === 'VALENTIM' ? 'VALENTIM' : 'CARMARQ')}
                                            </span>
                                        </td>
                                        <td>{typeLabels[os.machineType] || os.machineType || os.machineName}</td>
                                        <td>{os.technicianName}</td>
                                        <td style={{ whiteSpace: 'nowrap' }}>{formatDate(os.serviceDate)}</td>
                                        {(user?.role === 'PROPRIETARIO' || user?.role === 'FINANCEIRO') ? (
                                            <>
                                                <td style={{ textAlign: 'right', fontWeight: '500' }}>
                                                    R$ {(os.totalValue || 0).toFixed(2)}
                                                </td>
                                                <td style={{ textAlign: 'right', fontWeight: '600', color: (os.netProfit || 0) >= 0 ? '#059669' : '#ef4444' }}>
                                                    R$ {(os.netProfit || 0).toFixed(2)}
                                                </td>
                                            </>
                                        ) : (
                                            <td style={{ textAlign: 'right', fontWeight: '600', color: 'var(--primary-color)' }}>
                                                R$ {(os.technicianPayment || 0).toFixed(2)}
                                            </td>
                                        )}
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
