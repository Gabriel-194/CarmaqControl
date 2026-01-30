import React from 'react'
import { DollarSign, AlertCircle, PieChart } from 'lucide-react'
import '../../Styles/Dashboards.css'

export function FinanceiroDashboard() {
    return (
        <div>
            <h1 className="page-title" style={{ marginBottom: '1.5rem' }}>Financeiro Carmarq</h1>

            <div className="dashboard-grid">
                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">Entradas Hoje</span>
                        <DollarSign className="stat-icon" size={20} color="#10b981" />
                    </div>
                    <span className="stat-value">R$ 2.450,00</span>
                    <span className="stat-desc">8 pagamentos recebidos</span>
                </div>

                <div className="stat-card">
                    <div className="stat-header">
                        <span className="stat-title">Pendências</span>
                        <AlertCircle className="stat-icon" size={20} color="#ef4444" />
                    </div>
                    <span className="stat-value">R$ 450,00</span>
                    <span className="stat-desc" style={{ color: '#ef4444' }}>Atrasados</span>
                </div>
            </div>

            <div className="dashboard-section">
                <h3 className="section-title">Fluxo de Caixa Recente</h3>
                <ul className="dashboard-list">
                    <li className="list-item">
                        <div className="item-info">
                            <h4>Pagamento OS #1001</h4>
                            <p>PIX - Cliente: João Silva</p>
                        </div>
                        <span style={{ color: '#10b981', fontWeight: 'bold' }}>+ R$ 1.200,00</span>
                    </li>
                    <li className="list-item">
                        <div className="item-info">
                            <h4>Compra de Peças</h4>
                            <p>AutoPeças Distribuidora</p>
                        </div>
                        <span style={{ color: '#ef4444', fontWeight: 'bold' }}>- R$ 320,00</span>
                    </li>
                </ul>
            </div>
        </div>
    )
}