import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import Sidebar from '../Components/Sidebar'
import TabelaTempos from '../Components/TabelaTempos'
import ListaPecas from '../Components/ListaPecas'
import ServicePhotos from '../Components/ServicePhotos'
import { useAuth } from '../contexts/AuthContext'
import { toast } from '../Components/ui/Toaster'
import { ArrowLeft, CheckCircle, Clock, PenTool, Camera, Loader2, AlertTriangle, Play, DollarSign } from 'lucide-react'
import axios from 'axios'
import '../Styles/OrdemDetalhes.css'

const API_URL = 'http://localhost:8080/api/service-orders'

// Página de Detalhes da Ordem de Serviço — integrada com API real
export default function OrdemDetalhes() {
    const { id } = useParams()
    const navigate = useNavigate()
    const { user } = useAuth()

    const [osData, setOsData] = useState(null)
    const [loading, setLoading] = useState(true)
    const [activeTab, setActiveTab] = useState('detalhes')
    const [serviceDescription, setServiceDescription] = useState('')

    const fetchOS = async () => {
        try {
            const res = await axios.get(`${API_URL}/${id}`, { withCredentials: true })
            setOsData(res.data)
            setServiceDescription(res.data.serviceDescription || '')
        } catch (error) {
            toast('Erro ao carregar detalhes da OS.', 'error')
            navigate('/ordens')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchOS()
    }, [id])

    // Alterar status da OS
    const handleStatusChange = async (newStatus) => {
        try {
            await axios.put(`${API_URL}/${id}/status`, { status: newStatus }, { withCredentials: true })
            toast(`Status alterado para ${newStatus}`, 'success')
            fetchOS()
        } catch (error) {
            toast('Erro ao alterar status.', 'error')
        }
    }

    // Salvar descrição do serviço realizado
    const handleSaveDescription = async () => {
        try {
            await axios.put(`${API_URL}/${id}`, { serviceDescription }, { withCredentials: true })
            toast('Descrição salva!', 'success')
        } catch (error) {
            toast('Erro ao salvar descrição.', 'error')
        }
    }

    // Marcar pagamento como recebido (ação irreversível para o técnico)
    const handleMarkReceived = async () => {
        if (!window.confirm('Confirmar recebimento do pagamento? Esta ação não pode ser desfeita.')) return
        try {
            await axios.put(`${API_URL}/${id}/mark-received`, {}, { withCredentials: true })
            toast('Pagamento marcado como recebido!', 'success')
            fetchOS()
        } catch (error) {
            toast(error.response?.data?.message || 'Erro ao marcar como recebido.', 'error')
        }
    }

    const statusMap = {
        'ABERTA': { label: 'Aberta', css: 'status-aberto' },
        'EM_ANDAMENTO': { label: 'Em Andamento', css: 'status-em-andamento' },
        'CONCLUIDA': { label: 'Concluída', css: 'status-concluido' },
        'CANCELADA': { label: 'Cancelada', css: 'status-cancelada' },
        'REQUER_INSPECAO': { label: 'Requer Inspeção', css: 'status-inspecao' }
    }

    if (loading) {
        return (
            <div className="dashboard-layout">
                <Sidebar />
                <main className="dashboard-content" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    <Loader2 className="animate-spin" size={32} />
                </main>
            </div>
        )
    }

    if (!osData) return null

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
                        <span className={`status-badge ${statusMap[osData.status]?.css || ''}`}>
                            {statusMap[osData.status]?.label || osData.status}
                        </span>
                    </div>
                </div>

                <div className="details-grid">
                    <div className="main-column">
                        <div className="card info-card">
                            <h2>{osData.clientName}</h2>
                            <p className="address">{osData.clientAddress || 'Endereço não informado'}</p>
                            <div className="info-divider"></div>
                            <div className="detail-row">
                                <strong>Máquina:</strong>
                                <p>{osData.machineType} — {osData.machineName}</p>
                            </div>
                            <div className="detail-row">
                                <strong>Técnico Responsável:</strong>
                                <p>{osData.technicianName}</p>
                            </div>
                            <div className="detail-row">
                                <strong>Prioridade:</strong>
                                <p>{osData.priority}</p>
                            </div>
                            {osData.serviceType && (
                                <div className="detail-row">
                                    <strong>Tipo de Serviço:</strong>
                                    <p>{osData.serviceType}</p>
                                </div>
                            )}
                            {osData.problemDescription && (
                                <div className="detail-row">
                                    <strong>Relato do Problema:</strong>
                                    <p>{osData.problemDescription}</p>
                                </div>
                            )}
                        </div>

                        {/* Descrição do Serviço Realizado — campo editável pelo técnico */}
                        <div className="card info-card" style={{ marginTop: '1rem' }}>
                            <h3 style={{ marginBottom: '0.75rem' }}>Descrição do Serviço Realizado</h3>
                            <textarea
                                className="form-input"
                                rows="3"
                                placeholder="Descreva o serviço realizado..."
                                value={serviceDescription}
                                onChange={e => setServiceDescription(e.target.value)}
                                style={{ width: '100%', marginBottom: '0.5rem' }}
                            />
                            <button className="btn-primary" onClick={handleSaveDescription} style={{ fontSize: '0.85rem', padding: '0.5rem 1rem' }}>
                                Salvar Descrição
                            </button>
                        </div>

                        {/* Abas: Execução / Peças / Fotos */}
                        <div className="tabs-container">
                            <div className="tabs-header">
                                <button
                                    className={`tab-btn ${activeTab === 'detalhes' ? 'active' : ''}`}
                                    onClick={() => setActiveTab('detalhes')}
                                >
                                    <Clock size={16} /> Tempos
                                </button>
                                <button
                                    className={`tab-btn ${activeTab === 'pecas' ? 'active' : ''}`}
                                    onClick={() => setActiveTab('pecas')}
                                >
                                    <PenTool size={16} /> Peças
                                </button>
                                <button
                                    className={`tab-btn ${activeTab === 'fotos' ? 'active' : ''}`}
                                    onClick={() => setActiveTab('fotos')}
                                >
                                    <Camera size={16} /> Fotos
                                </button>
                            </div>

                            <div className="tab-content">
                                {activeTab === 'detalhes' && <TabelaTempos serviceOrderId={id} />}
                                {activeTab === 'pecas' && <ListaPecas serviceOrderId={id} orderStatus={osData.status} />}
                                {activeTab === 'fotos' && <ServicePhotos serviceOrderId={id} orderStatus={osData.status} />}
                            </div>
                        </div>
                    </div>

                    <div className="side-column">
                        <div className="card actions-card">
                            <h3>Ações</h3>
                            {osData.status === 'ABERTA' && (
                                <button
                                    className="btn-primary btn-full mb-2"
                                    onClick={() => handleStatusChange('EM_ANDAMENTO')}
                                    style={{ background: '#f59e0b' }}
                                >
                                    <Play size={18} /> Iniciar Serviço
                                </button>
                            )}
                            {osData.status === 'EM_ANDAMENTO' && (
                                <button
                                    className="btn-primary btn-full btn-success mb-2"
                                    onClick={() => handleStatusChange('CONCLUIDA')}
                                >
                                    <CheckCircle size={18} /> Finalizar Serviço
                                </button>
                            )}
                            {(osData.status === 'ABERTA' || osData.status === 'EM_ANDAMENTO') && (
                                <button
                                    className="btn-secondary btn-full mb-2"
                                    onClick={() => handleStatusChange('REQUER_INSPECAO')}
                                >
                                    <AlertTriangle size={18} /> Requer Inspeção
                                </button>
                            )}
                            {osData.status !== 'CANCELADA' && osData.status !== 'CONCLUIDA' && (
                                <button
                                    className="btn-secondary btn-full"
                                    style={{ color: 'var(--danger-color)' }}
                                    onClick={() => handleStatusChange('CANCELADA')}
                                >
                                    Cancelar OS
                                </button>
                            )}
                        </div>

                        {/* Resumo Financeiro — oculto para TECNICO (exceto pagamento dele) */}
                        <div className="card finance-card">
                            <h3>Resumo Financeiro</h3>
                            {user?.role !== 'TECNICO' ? (
                                <>
                                    <div className="finance-row">
                                        <span>Mão de Obra</span>
                                        <span>R$ {(osData.serviceValue || 0).toFixed(2)}</span>
                                    </div>
                                    <div className="finance-row">
                                        <span>Peças</span>
                                        <span>R$ {(osData.partsValue || 0).toFixed(2)}</span>
                                    </div>
                                    <div className="finance-row">
                                        <span>Deslocamento</span>
                                        <span>R$ {(osData.travelCost || 0).toFixed(2)}</span>
                                    </div>
                                    <div className="finance-row">
                                        <span>Pgto Técnico (10%)</span>
                                        <span>R$ {(osData.technicianPayment || 0).toFixed(2)}</span>
                                    </div>
                                    <div className="finance-divider"></div>
                                    <div className="finance-total">
                                        <span>Total</span>
                                        <span>R$ {(osData.totalValue || 0).toFixed(2)}</span>
                                    </div>
                                </>
                            ) : (
                                <>
                                    <div className="finance-row" style={{ marginTop: '0.5rem' }}>
                                        <span>Seu Pagamento</span>
                                        <span style={{ color: 'var(--primary-color)', fontWeight: 'bold' }}>
                                            R$ {(osData.technicianPayment || 0).toFixed(2)}
                                        </span>
                                    </div>
                                    <div className="finance-row" style={{ marginTop: '0.25rem' }}>
                                        <span>Status</span>
                                        <span style={{
                                            color: osData.technicianPaymentStatus === 'RECEBIDO' ? '#10b981' : '#f59e0b',
                                            fontWeight: 'bold'
                                        }}>
                                            {osData.technicianPaymentStatus === 'RECEBIDO' ? '✅ Recebido' : '⏳ A Receber'}
                                        </span>
                                    </div>
                                    {osData.status === 'CONCLUIDA' && osData.technicianPaymentStatus === 'A_RECEBER' && (
                                        <button
                                            className="btn-primary btn-full btn-success"
                                            style={{ marginTop: '0.75rem', fontSize: '0.85rem' }}
                                            onClick={handleMarkReceived}
                                        >
                                            <DollarSign size={16} /> Marcar como Recebido
                                        </button>
                                    )}
                                </>
                            )}
                        </div>
                    </div>
                </div>
            </main>
        </div>
    )
}