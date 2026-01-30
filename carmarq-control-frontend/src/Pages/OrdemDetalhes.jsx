import React, { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import Sidebar from '../Components/Sidebar'
import TabelaTempos from '../Components/TabelaTempos'
import ListaPecas from '../Components/ListaPecas'
import { ArrowLeft, CheckCircle, Clock, PenTool } from 'lucide-react'
import '../Styles/OrdemDetalhes.css'

export default function OrdemDetalhes() {
    const { id } = useParams()
    const navigate = useNavigate()

    // Ler usuário do localStorage
    const user = JSON.parse(localStorage.getItem('user'))

    const [activeTab, setActiveTab] = useState('detalhes')

    // Mock
    const osData = {
        id: id,
        cliente: 'Acme Corp',
        endereco: 'Av. Industrial, 1000 - Galpão 3',
        equipamento: 'Ar Condicionado Split 24000 BTUs',
        defeito: 'Equipamento não gela, apresenta ruído alto no compressor.',
        status: 'Em Andamento',
        valorServico: 450.00,
        valorPecas: 120.00,
        total: 570.00
    }

    return (
        <div className="dashboard-layout">
            <Sidebar />
            <main className="dashboard-content">
                <div className="details-header">
                    <button onClick={() => navigate('/ordens')} className="btn-back">
                        <ArrowLeft size={20} /> Voltar
                    </button>
                    <div className="header-actions">
                        <span className="os-id">OS #{id}</span>
                        <span className="status-badge status-em-andamento">{osData.status}</span>
                    </div>
                </div>

                <div className="details-grid">
                    {/* ... (O restante do layout é igual ao anterior) ... */}
                    <div className="main-column">
                        <div className="card info-card">
                            <h2>{osData.cliente}</h2>
                            <p className="address">{osData.endereco}</p>
                            <div className="info-divider"></div>
                            <div className="detail-row">
                                <strong>Equipamento:</strong>
                                <p>{osData.equipamento}</p>
                            </div>
                            <div className="detail-row">
                                <strong>Relato do Defeito:</strong>
                                <p>{osData.defeito}</p>
                            </div>
                        </div>

                        <div className="tabs-container">
                            <div className="tabs-header">
                                <button
                                    className={`tab-btn ${activeTab === 'detalhes' ? 'active' : ''}`}
                                    onClick={() => setActiveTab('detalhes')}
                                >
                                    <Clock size={16} /> Execução
                                </button>
                                <button
                                    className={`tab-btn ${activeTab === 'pecas' ? 'active' : ''}`}
                                    onClick={() => setActiveTab('pecas')}
                                >
                                    <PenTool size={16} /> Peças Utilizadas
                                </button>
                            </div>

                            <div className="tab-content">
                                {activeTab === 'detalhes' && <TabelaTempos />}
                                {activeTab === 'pecas' && <ListaPecas />}
                            </div>
                        </div>
                    </div>

                    <div className="side-column">
                        <div className="card actions-card">
                            <h3>Ações</h3>
                            <button className="btn-primary btn-full btn-success mb-2">
                                <CheckCircle size={18} /> Finalizar Serviço
                            </button>
                            <button className="btn-secondary btn-full">
                                Pausar Execução
                            </button>
                        </div>

                        {/* Verifica ROLE via user do localStorage */}
                        {user?.role !== 'tecnico' && (
                            <div className="card finance-card">
                                <h3>Resumo Financeiro</h3>
                                <div className="finance-row">
                                    <span>Mão de Obra</span>
                                    <span>R$ {osData.valorServico.toFixed(2)}</span>
                                </div>
                                <div className="finance-row">
                                    <span>Peças</span>
                                    <span>R$ {osData.valorPecas.toFixed(2)}</span>
                                </div>
                                <div className="finance-divider"></div>
                                <div className="finance-total">
                                    <span>Total</span>
                                    <span>R$ {osData.total.toFixed(2)}</span>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </main>
        </div>
    )
}