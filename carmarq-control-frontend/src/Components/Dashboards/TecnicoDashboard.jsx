import React from 'react'
import { ClipboardList, CheckCircle, Clock } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import '../../Styles/Dashboards.css'

export function TecnicoDashboard() {
    const navigate = useNavigate()

    return (
        <div>
            <h1 className="page-title" style={{ marginBottom: '1.5rem' }}>Meus Serviços</h1>

            <div className="dashboard-grid">
                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">Na Fila</span>
                        <Clock className="stat-icon" size={20} color="#10b981" />
                    </div>
                    <span className="stat-value">3</span>
                    <span className="stat-desc">Aguardando início</span>
                </div>

                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">Finalizados Hoje</span>
                        <CheckCircle className="stat-icon" size={20} color="#10b981" />
                    </div>
                    <span className="stat-value">2</span>
                    <span className="stat-desc">Produtividade alta</span>
                </div>
            </div>

            <div className="dashboard-section">
                <h3 className="section-title">Minha Fila de Trabalho</h3>
                <table className="simple-table">
                    <thead>
                    <tr>
                        <th>OS</th>
                        <th>Veículo/Serviço</th>
                        <th>Status</th>
                        <th>Ação</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>#1040</td>
                        <td>Fiat Toro - Alinhamento</td>
                        <td><span className="status-badge status-em-andamento">Em Andamento</span></td>
                        <td>
                            <button
                                className="btn-primary"
                                style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }}
                                onClick={() => navigate('/ordens/1040')}
                            >
                                Continuar
                            </button>
                        </td>
                    </tr>
                    <tr>
                        <td>#1042</td>
                        <td>Honda Civic - Suspensão</td>
                        <td><span className="status-badge status-aberto">Aberto</span></td>
                        <td>
                            <button
                                className="btn-secondary"
                                style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }}
                                onClick={() => navigate('/ordens/1042')}
                            >
                                Iniciar
                            </button>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
    )
}