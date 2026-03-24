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
import { paymentStatusMap, statusMap } from '../utils/statusUtils'
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
    const [discountInput, setDiscountInput] = useState('')
    
    // States pro Modal de Rejeição de Pagamento
    const [isRejectModalOpen, setIsRejectModalOpen] = useState(false)
    const [rejectionReasonInput, setRejectionReasonInput] = useState('')

    const fetchOS = async () => {
        try {
            const res = await axios.get(`${API_URL}/${id}`, { withCredentials: true })
            setOsData(res.data)
            setServiceDescription(res.data.serviceDescription || '')
            setDiscountInput(res.data.discountValue || '')
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



    const handleSaveDiscount = async () => {
        try {
            await axios.put(`${API_URL}/${id}`, { ...osData, discountValue: parseFloat(discountInput) || 0.0 }, { withCredentials: true })
            toast('Desconto salvo!', 'success')
            fetchOS()
        } catch (error) {
            toast('Erro ao salvar desconto.', 'error')
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
            link.setAttribute('download', `OS_${id}_Manutencao.pdf`);
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (error) {
            toast('Erro ao gerar relatório PDF.', 'error');
        }
    }

    const handleDownloadInstalacaoExcel = async () => {
        try {
            const response = await axios.get(`${API_URL}/${id}/export/instalacao-excel`, {
                responseType: 'blob',
                withCredentials: true
            });
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `OS_${id}_Entrega_Tecnica.xlsx`);
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (error) {
            toast('Erro ao gerar Excel de Instalação.', 'error');
        }
    }

    const handleDownloadDespesasExcel = async () => {
        try {
            const response = await axios.get(`${API_URL}/${id}/export/despesas-excel`, {
                responseType: 'blob',
                withCredentials: true
            });
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `OS_${id}_Relatorio_Despesas.xlsx`);
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (error) {
            toast('Erro ao gerar Excel de Despesas.', 'error');
        }
    }

    // Aprovar pagamento ao técnico (ação estrutural para Financeiro/Proprietário)
    const handleApprovePayment = async () => {
        if (!window.confirm('Aprovar o repasse deste valor ao técnico? Esta ação não pode ser desfeita e indicará que o dinheiro foi transferido.')) return
        try {
            await axios.put(`${API_URL}/${id}/approve-payment`, {}, { withCredentials: true })
            toast('Pagamento aprovado e repassado ao técnico!', 'success')
            fetchOS()
        } catch (error) {
            toast(error.response?.data?.message || 'Erro ao aprovar repasse.', 'error')
        }
    }

    const handleRejectPayment = async () => {
        if (!rejectionReasonInput.trim()) {
            toast('Por favor, informe o motivo da rejeição.', 'error')
            return
        }
        try {
            await axios.put(`${API_URL}/${id}/reject-payment`, { reason: rejectionReasonInput }, { withCredentials: true })
            toast('Pagamento rejeitado.', 'success')
            setIsRejectModalOpen(false)
            setRejectionReasonInput('')
            fetchOS()
        } catch (error) {
            toast(error.response?.data?.message || 'Erro ao rejeitar repasse.', 'error')
        }
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
                                {activeTab === 'detalhes' && <TabelaTempos serviceOrderId={id} userRole={user?.role} onUpdate={fetchOS} />}
                                {activeTab === 'pecas' && <ListaPecas serviceOrderId={id} orderStatus={osData.status} onUpdate={fetchOS} />}
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
                                        <>
                                            <button
                                                className="btn-secondary btn-full mb-2"
                                                onClick={() => handleStatusChange('REQUER_INSPECAO')}
                                            >
                                                <AlertTriangle size={18} /> Requer Inspeção
                                            </button>
                                            <button
                                                className="btn-secondary btn-full mb-2"
                                                style={{ color: '#ef4444', borderColor: '#ef4444' }}
                                                onClick={() => handleStatusChange('COM_PROBLEMA')}
                                            >
                                                <AlertTriangle size={18} /> Relatar Problema
                                            </button>
                                        </>
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
                                        <span>Despesas</span>
                                        <span>R$ {(osData.expensesValue || 0).toFixed(2)}</span>
                                    </div>
                                    <div className="finance-row">
                                        <span>Peças</span>
                                        <span>R$ {(osData.partsValue || 0).toFixed(2)}</span>
                                    </div>
                                    <div className="finance-row" style={{ color: '#ef4444' }}>
                                        <span>Desconto Concedido</span>
                                        <span>- R$ {(osData.discountValue || 0).toFixed(2)}</span>
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

                                    <div className="finance-divider" style={{ margin: '1rem 0' }}></div>
                                    
                                    {/* Edição do Desconto (Proprietário/Financeiro) */}
                                    {user?.role !== 'TECNICO' && (
                                        <div className="detail-row" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                                            <strong style={{ flex: 1, fontSize: '0.85rem' }}>Conceder Desconto (R$):</strong>
                                            <input 
                                                type="number" 
                                                className="form-input" 
                                                value={discountInput} 
                                                onChange={e => setDiscountInput(e.target.value)}
                                                disabled={osData.status === 'CANCELADA' || osData.status === 'CONCLUIDA'}
                                                style={{ width: '80px', margin: 0, padding: '0.4rem' }}
                                                placeholder="0.00"
                                            />
                                            <button 
                                                className="btn-primary" 
                                                onClick={handleSaveDiscount} 
                                                disabled={osData.status === 'CANCELADA' || osData.status === 'CONCLUIDA'}
                                                style={{ padding: '0.4rem 0.75rem', fontSize: '0.8rem' }}
                                            >
                                                Aplicar
                                            </button>
                                        </div>
                                    )}

                                    <div className="finance-row" style={{ marginTop: '0.5rem' }}>
                                        <span>Status do Repasse:</span>
                                        <span style={{
                                            color: paymentStatusMap[osData.technicianPaymentStatus]?.color || '#6b7280',
                                            fontWeight: 'bold'
                                        }}>
                                            {paymentStatusMap[osData.technicianPaymentStatus]?.label || osData.technicianPaymentStatus}
                                        </span>
                                    </div>
                                    
                                    {osData.rejectionReason && (
                                        <div className="detail-row" style={{ backgroundColor: '#fef2f2', padding: '0.75rem', borderRadius: '4px', marginTop: '0.5rem' }}>
                                            <strong style={{ color: '#ef4444', fontSize: '0.85rem' }}>Motivo da Rejeição:</strong>
                                            <p style={{ color: '#991b1b', fontSize: '0.85rem', marginTop: '0.25rem' }}>{osData.rejectionReason}</p>
                                        </div>
                                    )}

                                    {/* Botões para o Financeiro aprovar/rejeitar */}
                                    {osData.technicianPaymentStatus === 'PENDENTE_APROVACAO' && (
                                        <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
                                            <button
                                                className="btn-primary btn-success"
                                                style={{ flex: 1, fontSize: '0.85rem', display: 'flex', justifyContent: 'center' }}
                                                onClick={handleApprovePayment}
                                            >
                                                <DollarSign size={16} /> Aprovar Pagamento
                                            </button>
                                            <button
                                                className="btn-secondary"
                                                style={{ flex: 1, fontSize: '0.85rem', color: '#ef4444', borderColor: '#ef4444', display: 'flex', justifyContent: 'center' }}
                                                onClick={() => setIsRejectModalOpen(true)}
                                            >
                                                Rejeitar
                                            </button>
                                        </div>
                                    )}
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
                                            color: paymentStatusMap[osData.technicianPaymentStatus]?.color || '#6b7280',
                                            fontWeight: 'bold'
                                        }}>
                                            {paymentStatusMap[osData.technicianPaymentStatus]?.label || osData.technicianPaymentStatus}
                                        </span>
                                    </div>
                                    
                                    {osData.technicianPaymentStatus === 'REJEITADO' && osData.rejectionReason && (
                                        <div className="detail-row" style={{ backgroundColor: '#fef2f2', padding: '0.75rem', borderRadius: '4px', marginTop: '0.5rem' }}>
                                            <strong style={{ color: '#ef4444', fontSize: '0.85rem' }}>Atenção - Pagamento Rejeitado:</strong>
                                            <p style={{ color: '#991b1b', fontSize: '0.85rem', marginTop: '0.25rem' }}>{osData.rejectionReason}</p>
                                            <p style={{ color: '#991b1b', fontSize: '0.75rem', marginTop: '0.5rem' }}>Corrija os apontamentos de horas/despesas e avise o seu Supervisor e o Financeiro.</p>
                                        </div>
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
                            <Download size={16} /> Exportar Manutenção (PDF)
                        </button>
                        <button 
                            className="btn-secondary btn-full" 
                            onClick={handleDownloadInstalacaoExcel} 
                            style={{ 
                                display: 'flex', 
                                alignItems: 'center', 
                                justifyContent: 'center', 
                                gap: '0.5rem', 
                                marginTop: '0.5rem', 
                                fontSize: '0.85rem', 
                                padding: '0.5rem',   
                                color: 'var(--text-muted)'
                            }}
                        >
                            <Download size={16} /> Exportar Instalação (Excel)
                        </button>
                        <button 
                            className="btn-secondary btn-full" 
                            onClick={handleDownloadDespesasExcel} 
                            style={{ 
                                display: 'flex', 
                                alignItems: 'center', 
                                justifyContent: 'center', 
                                gap: '0.5rem', 
                                marginTop: '0.5rem', 
                                fontSize: '0.85rem', 
                                padding: '0.5rem',   
                                color: 'var(--text-muted)'
                            }}
                        >
                            <DollarSign size={16} /> Exportar Despesas (Excel)
                        </button>

                    </div>
                </div>
            </main>

            {/* Modal de Rejeição de Pagamento */}
            {isRejectModalOpen && (
                <div className="modal-overlay" onClick={() => setIsRejectModalOpen(false)}>
                    <div className="modal-content" onClick={e => e.stopPropagation()} style={{ maxWidth: '400px' }}>
                        <div className="modal-header">
                            <h2>Motivo da Rejeição</h2>
                            <button className="btn-close" onClick={() => setIsRejectModalOpen(false)}>&times;</button>
                        </div>
                        <div className="modal-body">
                            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                                Descreva o motivo pelo qual este pagamento está sendo rejeitado (ex: KM excessivo, falta de comprovante).
                            </p>
                            <textarea
                                className="form-input"
                                rows="4"
                                placeholder="Motivo detalhado..."
                                value={rejectionReasonInput}
                                onChange={e => setRejectionReasonInput(e.target.value)}
                                style={{ width: '100%', resize: 'vertical' }}
                            />
                        </div>
                        <div className="modal-footer" style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.5rem' }}>
                            <button className="btn-secondary" onClick={() => setIsRejectModalOpen(false)}>Cancelar</button>
                            <button className="btn-primary" style={{ backgroundColor: '#ef4444' }} onClick={handleRejectPayment}>Confirmar Rejeição</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}