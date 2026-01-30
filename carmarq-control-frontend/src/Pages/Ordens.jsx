import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Sidebar from '../Components/Sidebar'
import { Search, Filter, Plus } from 'lucide-react'
import '../Styles/Ordens.css'

const MOCK_ORDENS = [
    { id: 1, cliente: 'Acme Corp', equipamento: 'Ar Condicionado Split', status: 'Em Andamento', tecnico: 'Carlos Silva', data: '28/10/2023' },
    { id: 2, cliente: 'Padaria do João', equipamento: 'Refrigerador Industrial', status: 'Aberto', tecnico: 'Pendente', data: '29/10/2023' },
    { id: 3, cliente: 'Tech Solutions', equipamento: 'Servidor Rack', status: 'Concluído', tecnico: 'Carlos Silva', data: '25/10/2023' },
]

export default function Ordens() {
    const navigate = useNavigate()
    const [searchTerm, setSearchTerm] = useState('')

    const handleRowClick = (id) => {
        navigate(`/ordens/${id}`)
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
                    {/* ADICIONADO O ONCLICK AQUI */}
                    <button
                        className="btn-primary btn-success"
                        onClick={() => navigate('/nova-os')}
                    >
                        <Plus size={20} /> Nova OS
                    </button>
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
                    <button className="btn-secondary">
                        <Filter size={18} /> Filtros
                    </button>
                </div>

                <div className="table-container">
                    <table className="data-table">
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Cliente</th>
                            <th>Equipamento</th>
                            <th>Técnico</th>
                            <th>Data</th>
                            <th>Status</th>
                        </tr>
                        </thead>
                        <tbody>
                        {MOCK_ORDENS.map((os) => (
                            <tr key={os.id} onClick={() => handleRowClick(os.id)}>
                                <td>#{os.id}</td>
                                <td className="font-bold">{os.cliente}</td>
                                <td>{os.equipamento}</td>
                                <td>{os.tecnico}</td>
                                <td>{os.data}</td>
                                <td>
                                    <span className={`status-badge status-${os.status.toLowerCase().replace(/ /g, '-')}`}>
                                    {os.status}
                                    </span>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </main>
        </div>
    )
}