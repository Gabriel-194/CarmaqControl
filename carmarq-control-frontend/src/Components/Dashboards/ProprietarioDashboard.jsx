import React from 'react'
import { DollarSign, Users, ClipboardList, TrendingUp } from 'lucide-react'
import '../../Styles/Dashboards.css'

export function ProprietarioDashboard() {
    return (
        <div>
            <h1 className="page-title" style={{ marginBottom: '1.5rem' }}>Visão Geral</h1>

            <div className="dashboard-grid">
                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">Receita Total</span>
                        <DollarSign className="stat-icon" size={20} color="#10b981" />
                    </div>
                    <span className="stat-value">R$ 45.231,89</span>
                    <span className="stat-desc" style={{ color: '#10b981' }}>+20.1% este mês</span>
                </div>

                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">Ordens Ativas</span>
                        <ClipboardList className="stat-icon" size={20} color="#10b981" />
                    </div>
                    <span className="stat-value">57</span>
                    <span className="stat-desc">12 novas hoje</span>
                </div>

                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">Técnicos Online</span>
                        <Users className="stat-icon" size={20} color="#10b981" />
                    </div>
                    <span className="stat-value">8</span>
                    <span className="stat-desc">Equipe completa</span>
                </div>
            </div>

            <div className="dashboard-section">
                <h3 className="section-title">Últimas Atividades</h3>
                <ul className="dashboard-list">
                    <li className="list-item">
                        <div className="item-info">
                            <h4>Ordem #1045 - Revisão Freios</h4>
                            <p>Finalizado por Carlos Silva</p>
                        </div>
                        <span style={{ fontWeight: 'bold', color: '#10b981' }}>R$ 450,00</span>
                    </li>
                    <li className="list-item">
                        <div className="item-info">
                            <h4>Ordem #1046 - Troca de Óleo</h4>
                            <p>Nova solicitação balcão</p>
                        </div>
                        <span className="status-badge status-aberto">Aberto</span>
                    </li>
                </ul>
            </div>
        </div>
    )
}