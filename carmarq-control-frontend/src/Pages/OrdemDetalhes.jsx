import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import Sidebar from '../Components/Sidebar'
import TabelaTempos from '../Components/TabelaTempos'
import ListaPecas from '../Components/ListaPecas'
import ListaDespesas from '../Components/ListaDespesas'
import ServicePhotos from '../Components/ServicePhotos'
import { useAuth } from '../contexts/AuthContext'
import { toast } from '../Components/ui/Toaster'
import { ArrowLeft, CheckCircle, Clock, PenTool, Camera, Loader2, AlertTriangle, Play, DollarSign, Lock, Download } from 'lucide-react'
import axios from 'axios'
import '../Styles/OrdemDetalhes.css'

const API_URL = 'http://localhost:8080/api/service-orders'

const typeLabels = {
    LASER: 'Laser',
    DOBRADEIRA: 'Dobradeira',
    GUILHOTINA: 'Guilhotina',
    CURVADORA_TUBO: 'Curvadora de Tubo',
    METALEIRA: 'Metaleira',
    CALANDRA: 'Calandra',
    GRAVADORA_LASER: 'Gravadora a Laser',
}

// Página de Detalhes da Ordem de Serviço — integrada com API real
export default function OrdemDetalhes() {
    const { id } = useParams()
    const navigate = useNavigate()
    const { user } = useAuth()

    const [osData, setOsData] = useState(null)
    const [loading, setLoading] = useState(true)
    const [activeTab, setActiveTab] = useState('detalhes')
    const [serviceDescription, setServiceDescription] = useState('')
    const [displacementKmInput, setDisplacementKmInput] = useState('')

    const fetchOS = async () => {
        try {
            const res = await axios.get(`${API_URL}/${id}`, { withCredentials: true })
            setOsData(res.data)
            setServiceDescription(res.data.serviceDescription || '')
            setDisplacementKmInput(res.data.displacementKm || '')
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
            await axios.put(`${API_URL}/${id}/description`, { serviceDescription }, { withCredentials: true })
            toast('Descrição salva!', 'success')
            fetchOS()
        } catch (error) {
            toast('Erro ao salvar descrição.', 'error')
        }
    }

    const handleSaveDisplacement = async () => {
        try {
            await axios.put(`${API_URL}/${id}/displacement`, { displacementKm: parseFloat(displacementKmInput) }, { withCredentials: true })
            toast('Km de deslocamento salvo!', 'success')
            fetchOS()
        } catch (error) {
            toast('Erro ao salvar deslocamento.', 'error')
        }
    }

    const handleDownloadReport = async () => {
        try {
            const response = await axios.get(`${API_URL}/${id}/report`, {
                responseType: 'blob',
                withCredentials: true
            });
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `OS_${id}_Relatorio.pdf`);
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (error) {
            toast('Erro ao gerar relatório PDF.', 'error');
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
                                <p>{typeLabels[osData.machineType] || osData.machineType} — {osData.machineName}</p>
                            </div>
                            <div className="detail-row">
                                <strong>Técnico Responsável:</strong>
                                <p>{osData.technicianName}</p>
                            </div>
                            <div className="detail-row">
                                <strong>Data do Atendimento:</strong>
                                <p>{osData.serviceDate ? new Date(osData.serviceDate).toLocaleDateString('pt-BR') : 'Não informada'}</p>
                            </div>
                            {osData.serviceType && (
                                <>
                                    <div className="detail-row">
                                        <strong>Tipo de Serviço:</strong>
                                        <p>{osData.serviceType}</p>
                                    </div>
                                    {osData.serviceType === 'MANUTENCAO' && (
                                        <div className="detail-row">
                                            <strong>Origem:</strong>
                                            <p>{osData.manutencaoOrigin === 'VALENTIM' ? 'Valentim - Garantia (R$ 185/h)' : 'Carmarq (R$ 250/h)'}</p>
                                        </div>
                                    )}
                                </>
                            )}
                            {osData.problemDescription && (
                                <div className="detail-row">
                                    <strong>Relato do Problema:</strong>
                                    <p>{osData.problemDescription}</p>
                                </div>
                            )}
                            {osData.distanceKm && (
                                <div className="detail-row" style={{ borderTop: '1px dashed #eee', marginTop: '0.5rem', paddingTop: '0.5rem' }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--primary-color)' }}>
                                        <Clock size={16} /> 
                                        <strong>Viagem Estimada (API):</strong>
                                        <span>{osData.distanceKm}km ({osData.estimatedMinutes} min)</span>
                                    </div>
                                </div>
                            )}
                            
                            {user?.role === 'TECNICO' && (
                                <div className="detail-row" style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginTop: '1rem', borderTop: '1px dashed #eee', paddingTop: '1rem' }}>
                                    <strong style={{ flex: 1 }}>Deslocamento: Km Percorridos:</strong>
                                    <input 
                                        type="number" 
                                        className="form-input" 
                                        value={displacementKmInput} 
                                        onChange={e => setDisplacementKmInput(e.target.value)}
                                        disabled={osData.status !== 'EM_ANDAMENTO'}
                                        style={{ width: '100px', margin: 0 }}
                                        placeholder="Ex: 50"
                                    />
                                    <button 
                                        className="btn-primary" 
                                        onClick={handleSaveDisplacement} 
                                        disabled={osData.status !== 'EM_ANDAMENTO'}
                                        style={{ padding: '0.4rem 1rem', fontSize: '0.8rem', whiteSpace: 'nowrap' }}
                                    >
                                        Salvar Km
                                    </button>
                                </div>
                            )}
                        </div>

                        {/* Descrição do Serviço Realizado — campo editável pelo técnico */}
                        <div className="card info-card" style={{ marginTop: '1rem' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
                                <h3 style={{ margin: 0 }}>Descrição do Serviço Realizado</h3>
                                {osData.status !== 'EM_ANDAMENTO' && (
                                    <span style={{ fontSize: '0.8rem', color: '#92400e', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                                        <Lock size={14} /> Somente em andamento
                                    </span>
                                )}
                            </div>
                            <textarea
                                className="form-input"
                                rows="3"
                                placeholder="Descreva o serviço realizado..."
                                value={serviceDescription}
                                onChange={e => setServiceDescription(e.target.value)}
                                disabled={osData.status !== 'EM_ANDAMENTO'}
                                style={{ 
                                    width: '100%', 
                                    marginBottom: '0.5rem',
                                    backgroundColor: osData.status !== 'EM_ANDAMENTO' ? '#f9fafb' : 'white',
                                    cursor: osData.status !== 'EM_ANDAMENTO' ? 'not-allowed' : 'text'
                                }}
                            />
                            <button 
                                className="btn-primary" 
                                onClick={handleSaveDescription} 
                                disabled={osData.status !== 'EM_ANDAMENTO'}
                                style={{ 
                                    fontSize: '0.85rem', 
                                    padding: '0.5rem 1rem',
                                    opacity: osData.status !== 'EM_ANDAMENTO' ? 0.6 : 1,
                                    cursor: osData.status !== 'EM_ANDAMENTO' ? 'not-allowed' : 'pointer'
                                }}
                            >
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
                                    className={`tab-btn ${activeTab === 'despesas' ? 'active' : ''}`}
                                    onClick={() => setActiveTab('despesas')}
                                >
                                    <DollarSign size={16} /> Despesas
                                </button>
                                <button
                                    className={`tab-btn ${activeTab === 'fotos' ? 'active' : ''}`}
                                    onClick={() => setActiveTab('fotos')}
                                >
                                    <Camera size={16} /> Fotos
                                </button>
                            </div>

                            <div className="tab-content">
                                {activeTab === 'detalhes' && <TabelaTempos serviceOrderId={id} userRole={user?.role} osTipo={osData.serviceType} onUpdate={fetchOS} />}
                                {activeTab === 'pecas' && <ListaPecas serviceOrderId={id} orderStatus={osData.status} />}
                                {activeTab === 'despesas' && <ListaDespesas serviceOrderId={id} orderStatus={osData.status} onUpdate={fetchOS} />}
                                {activeTab === 'fotos' && <ServicePhotos serviceOrderId={id} orderStatus={osData.status} />}
                            </div>
                        </div>
                    </div>

                    <div className="side-column">
                        <div className="card actions-card">
                            <h3 style={{ marginBottom: '1rem' }}>Ações</h3>
                            {user?.role === 'TECNICO' && (
                                <>
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
                                </>
                            )}

                            {/* ADMIN (Proprietário) pode apenas cancelar */}
                            {(osData.status === 'ABERTA' || osData.status === 'EM_ANDAMENTO' || osData.status === 'REQUER_INSPECAO') && (
                                <button
                                    className="btn-secondary btn-full"
                                    style={{ color: 'var(--danger-color)', border: '1px solid var(--danger-color)' }}
                                    onClick={() => handleStatusChange('CANCELADA')}
                                >
                                    Cancelar OS
                                </button>
                            )}
                            
                            {/* Mensagem informativa para Admin sobre restrição */}
                            {user?.role === 'PROPRIETARIO' && osData.status === 'ABERTA' && (
                                <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.5rem', textAlign: 'center' }}>
                                    Aguardando início pelo técnico responsável.
                                </p>
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
                                        <span>Deslocamento ({osData.displacementKm || 0} km)</span>
                                        <span>R$ {((osData.displacementKm || 0) * 2.20).toFixed(2)}</span>
                                    </div>
                                    <div className="finance-row">
                                        <span>Despesas</span>
                                        <span>R$ {(osData.expensesValue || 0).toFixed(2)}</span>
                                    </div>
                                    <div className="finance-row">
                                        <span>Peças</span>
                                        <span>R$ {(osData.partsValue || 0).toFixed(2)}</span>
                                    </div>
                                    <div className="finance-row" style={{ color: '#ef4444' }}>
                                        <span>Pgto Técnico (Despesa)</span>
                                        <span>- R$ {(osData.technicianPayment || 0).toFixed(2)}</span>
                                    </div>
                                    <div className="finance-divider"></div>
                                    <div className="finance-total">
                                        <span>Total Cobrado</span>
                                        <span style={{ color: '#10b981' }}>R$ {(osData.totalValue || 0).toFixed(2)}</span>
                                    </div>
                                    <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', textAlign: 'right', marginTop: '0.25rem' }}>
                                        Lucro Líquido: R$ {(osData.netProfit || 0).toFixed(2)}
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
                        <button 
                            className="btn-secondary btn-full" 
                            onClick={handleDownloadReport} 
                            style={{ 
                                display: 'flex', 
                                alignItems: 'center', 
                                justifyContent: 'center', 
                                gap: '0.5rem', 
                                marginTop: '1rem', 
                                fontSize: '0.85rem', 
                                padding: '0.5rem',   
                                color: 'var(--text-muted)'
                            }}
                        >
                            <Download size={16} /> Exportar Relatório PDF
                        </button>

                    </div>
                </div>
            </main>
        </div>
    )
}