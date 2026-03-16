import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Sidebar from '../Components/Sidebar'
import { useAuth } from '../contexts/AuthContext'
import { Search, Filter, Plus, Loader2, ClipboardList } from 'lucide-react'
import axios from 'axios'
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

    const fetchOrdens = async () => {
        try {
            const params = {}
            if (statusFilter) params.status = statusFilter
            const res = await axios.get(API_URL, { params, withCredentials: true })
            setOrdens(res.data)
        } catch (error) {
            console.error('Erro ao carregar ordens', error)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchOrdens()
    }, [statusFilter])

    const filtered = ordens.filter(os =>
        (os.clientName || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        (os.technicianName || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        String(os.id).includes(searchTerm)
    )

    // Mapeia status para label e classe CSS
    const statusMap = {
        'ABERTA': { label: 'Aberta', css: 'status-aberto' },
        'EM_ANDAMENTO': { label: 'Em Andamento', css: 'status-em-andamento' },
        'CONCLUIDA': { label: 'Concluída', css: 'status-concluido' },
        'CANCELADA': { label: 'Cancelada', css: 'status-cancelada' },
        'REQUER_INSPECAO': { label: 'Requer Inspeção', css: 'status-inspecao' }
    }

    const formatDate = (dateStr) => {
        if (!dateStr) return '—'
        return new Date(dateStr).toLocaleDateString('pt-BR')
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
                    {user?.role === 'PROPRIETARIO' && (
                        <button
                            className="btn-primary btn-success"
                            onClick={() => navigate('/nova-os')}
                        >
                            <Plus size={20} /> Nova OS
                        </button>
                    )}
                </header>

                <div className="filters-bar">
                    <div className="search-group">
                        <Search className="search-icon" />
                        <input
                            type="text"
                            placeholder="Buscar por cliente, ID ou técnico..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="search-input"
                        />
                    </div>
                    <select
                        className="btn-secondary"
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        style={{ cursor: 'pointer', minWidth: '150px' }}
                    >
                        <option value="">Todos os Status</option>
                        <option value="ABERTA">Aberta</option>
                        <option value="EM_ANDAMENTO">Em Andamento</option>
                        <option value="CONCLUIDA">Concluída</option>
                        <option value="CANCELADA">Cancelada</option>
                        <option value="REQUER_INSPECAO">Requer Inspeção</option>
                    </select>
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
                                    <th>Cliente</th>
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
                                        <td className="font-bold">{os.clientName}</td>
                                        <td>{os.machineName}</td>
                                        <td>{os.technicianName}</td>
                                        <td>{formatDate(os.openedAt)}</td>
                                        <td>
                                            <span className={`status-badge ${statusMap[os.status]?.css || ''}`}>
                                                {statusMap[os.status]?.label || os.status}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </main>
        </div>
    )
}