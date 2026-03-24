import React, { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import Sidebar from '../Components/Sidebar'
import ClientModal from '../Components/Clients/ClientModal'
import { useAuth } from '../contexts/AuthContext'
import { toast } from '../Components/ui/Toaster'
import { Plus, Save, Loader2, UserCog, Zap, DollarSign, Clock, MapPin, Wrench, Lightbulb, Package, FileText } from 'lucide-react'
import axios from 'axios'
import '../Styles/Form.css'
import '../Styles/NovaOS.css'

const API_BASE = 'http://localhost:8080/api'

const typeLabels = {
    LASER: 'Laser',
    DOBRADEIRA: 'Dobradeira',
    GUILHOTINA: 'Guilhotina',
    CURVADORA_TUBO: 'Curvadora de Tubo',
    METALEIRA: 'Metaleira',
    CALANDRA: 'Calandra',
    GRAVADORA_LASER: 'Gravadora a Laser',
}

// Página de criação de OS com Automação Inteligente
// Selecionar uma máquina aciona sugestões automáticas via API
export default function NovaOS() {
    const navigate = useNavigate()
    const { user } = useAuth()
    const isTecnico = user?.role === 'TECNICO'

    const [listaClientes, setListaClientes] = useState([])
    const [listaMaquinas, setListaMaquinas] = useState([])
    const [listaTecnicos, setListaTecnicos] = useState([])
    const [loading, setLoading] = useState(true)
    const [submitting, setSubmitting] = useState(false)
    const [formErrors, setFormErrors] = useState({})
    const [previewData, setPreviewData] = useState(null)
    const [isClientModalOpen, setIsClientModalOpen] = useState(false)
    const [clientSearchTerm, setClientSearchTerm] = useState('')

    const [formData, setFormData] = useState({
        clienteId: '',
        maquinaId: '',
        tecnicoId: '',
        numeroChamado: '',
        descricaoProblema: '',
        observacoes: '',
        tipoServico: '',
        manutencaoOrigin: '',
        valorServico: '',
        serviceDate: ''
    })

    const handleClientSearch = (value) => {
        setClientSearchTerm(value)
        const selected = listaClientes.find(c => c.companyName.toLowerCase() === value.toLowerCase())
        setFormData(prev => ({ ...prev, clienteId: selected ? selected.id : '' }))
    }

    const clienteSelecionado = listaClientes.find(c => c.id === parseInt(formData.clienteId))


    const carregarClientes = async () => {
        try {
            const res = await axios.get(`${API_BASE}/clients`, { withCredentials: true })
            setListaClientes(res.data)
        } catch (error) {
            console.error("Erro ao carregar clientes", error)
        }
    }

    // Carrega dados iniciais em paralelo
    useEffect(() => {
        async function carregarDados() {
            try {
                const [clientesRes, maquinasRes, tecnicosRes] = await Promise.all([
                    axios.get(`${API_BASE}/clients`, { withCredentials: true }),
                    axios.get(`${API_BASE}/machines`, { withCredentials: true }),
                    axios.get(`${API_BASE}/users/technicians`, { withCredentials: true })
                ])
                setListaClientes(clientesRes.data)
                setListaMaquinas(maquinasRes.data)
                setListaTecnicos(tecnicosRes.data)
            } catch (error) {
                toast('Erro ao carregar dados.', 'error')
            } finally {
                setLoading(false)
            }
        }
        carregarDados()
    }, [])



    // Busca prévia de valores financeiros do backend
    useEffect(() => {
        const fetchPreview = async () => {
            // Só busca se tiver o básico (valor ou deslocamento ou IDs essenciais)
            if (!formData.clienteId && !formData.maquinaId && !formData.valorServico) return

            try {
                const payload = {
                    clientId: formData.clienteId ? parseInt(formData.clienteId) : null,
                    machineId: formData.maquinaId ? parseInt(formData.maquinaId) : null,
                    serviceValue: formData.valorServico ? parseFloat(formData.valorServico) : 0
                }
                const res = await axios.post(`${API_BASE}/service-orders/preview`, payload, { withCredentials: true })
                setPreviewData(res.data)
            } catch (error) {
                console.error("Erro ao buscar preview", error)
            }
        }

        const timer = setTimeout(fetchPreview, 500) // Debounce de 500ms
        return () => clearTimeout(timer)
    }, [formData.clienteId, formData.maquinaId, formData.valorServico])

    // Quando a máquina muda, busca sugestões e atualiza valor de instalação se já selecionado
    const handleMachineChange = (machineId) => {
        setFormData(prev => {
            let newValor = prev.valorServico;
            if (prev.tipoServico === 'INSTALACAO') {
                const maquinaSelecionada = listaMaquinas.find(m => m.id === parseInt(machineId));
                newValor = maquinaSelecionada?.installationPrice || 0;
            }
            return { ...prev, maquinaId: machineId, valorServico: newValor };
        });
    }
    
    // Gerencia a troca de tipo de serviço
    const handleTipoServicoChange = (tipo) => {
        let newValor = formData.valorServico;
        const maquinaSelecionada = listaMaquinas.find(m => m.id === parseInt(formData.maquinaId));
        
        if (tipo === 'INSTALACAO' && maquinaSelecionada) {
            newValor = maquinaSelecionada.installationPrice || 0;
        } else if (tipo === 'MANUTENCAO') {
            newValor = ''; // Fica bloqueado para ser preenchido pela soma de horas no backend
        }
        
        setFormData(prev => ({ 
            ...prev, 
            tipoServico: tipo, 
            valorServico: newValor,
            manutencaoOrigin: tipo === 'INSTALACAO' ? '' : prev.manutencaoOrigin
        }));
    }

    // Submissão do formulário
    const handleSave = async (e) => {
        e.preventDefault()
        // Removida validação manual: o backend retornará 400 se faltar algo funcional

        setSubmitting(true)
        setFormErrors({})
        const payload = {
            clientId: parseInt(formData.clienteId),
            machineId: parseInt(formData.maquinaId),
            technicianId: isTecnico ? null : parseInt(formData.tecnicoId),
            numeroChamado: formData.numeroChamado,
            problemDescription: formData.descricaoProblema,
            serviceDate: formData.serviceDate,
            observations: formData.observacoes,
            serviceType: formData.tipoServico,
            manutencaoOrigin: formData.tipoServico === 'MANUTENCAO' ? formData.manutencaoOrigin : null,
            serviceValue: formData.valorServico ? parseFloat(formData.valorServico) : null
        }

        try {
            await axios.post(`${API_BASE}/service-orders`, payload, { withCredentials: true })
            toast('OS Criada com sucesso!', 'success')
            navigate('/ordens')
        } catch (error) {
            if (error.response && error.response.status === 400 && error.response.data.errors) {
                setFormErrors(error.response.data.errors)
                toast('Verifique os campos obrigatórios.', 'error')
            } else {
                toast('Erro ao criar OS.', 'error')
            }
            setSubmitting(false)
        }
    }

    // Removido cálculos locais redundantes (totalEstimado, technicianPayment)
    // Agora utilizamos o que vem do previewData ou suggestions

    return (
        <div className="dashboard-layout">
            <Sidebar />
            <main className="dashboard-content">
                <header className="page-header">
                    <div>
                        <h1 className="page-title">Nova Ordem de Serviço</h1>
                        <p className="page-subtitle">Defina o tipo de serviço e valor manualmente</p>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--primary-color)' }}>
                        <FileText size={20} /> Precificação Manual
                    </div>
                </header>

                {loading ? (
                    <div className="loading-container"><Loader2 className="animate-spin" /> Carregando dados...</div>
                ) : (
                    <div className="nova-os-container">
                        {/* Coluna principal — Formulário */}
                        <form className="form-card" onSubmit={handleSave}>
                            {/* Seleção do Cliente */}
                            <div className="form-group">
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                                    <label style={{ margin: 0 }}>Cliente / Empresa *</label>
                                    <button 
                                        type="button" 
                                        onClick={() => setIsClientModalOpen(true)}
                                        className="text-btn"
                                        style={{ 
                                            background: 'none', 
                                            border: 'none', 
                                            color: 'var(--primary-color)', 
                                            cursor: 'pointer', 
                                            fontSize: '0.85rem',
                                            fontWeight: '600',
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '0.25rem'
                                        }}
                                    >
                                        <Plus size={14} /> Novo Cliente
                                    </button>
                                </div>
                                <input
                                    list="clients-datalist"
                                    className={`form-input ${formErrors.clientId ? 'input-error' : ''}`}
                                    placeholder="Digite para pesquisar o cliente..."
                                    value={clientSearchTerm}
                                    onChange={e => handleClientSearch(e.target.value)}
                                    autoComplete="off"
                                />
                                <datalist id="clients-datalist">
                                    {listaClientes.map(c => (
                                        <option key={c.id} value={c.companyName} />
                                    ))}
                                </datalist>
                                {formErrors.clientId && <span className="error-message">{formErrors.clientId}</span>}
                            </div>

                            {/* Preview de info do cliente */}
                            {clienteSelecionado && (
                                <div className="client-info-preview">
                                    <MapPin size={16} />
                                    <span>
                                        <strong>{clienteSelecionado.companyName}</strong>
                                        {clienteSelecionado.address && ` — ${clienteSelecionado.address}`}
                                        {clienteSelecionado.contactName && ` • Contato: ${clienteSelecionado.contactName}`}
                                    </span>
                                </div>
                            )}

                            {/* Seleção da Máquina — aciona sugestões automáticas */}
                            <div className="form-group" style={{ marginTop: '1rem' }}>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                    <Zap size={16} color="var(--primary-color)" /> Máquina (aciona automação) *
                                </label>
                                <select
                                    className={`form-input ${formErrors.machineId ? 'input-error' : ''}`}
                                    style={formData.maquinaId ? { borderColor: 'var(--primary-color)', backgroundColor: '#f0fdf4' } : {}}
                                    value={formData.maquinaId}
                                    onChange={e => handleMachineChange(e.target.value)}
                                >
                                    <option value="">Selecione a máquina...</option>
                                    {listaMaquinas.map(m => (
                                        <option key={m.id} value={m.id}>
                                            {typeLabels[m.machineType] || m.machineType} — {m.name} ({m.model})
                                        </option>
                                    ))}
                                </select>
                                {formErrors.machineId && <span className="error-message">{formErrors.machineId}</span>}
                            </div>



                            <div className="form-grid">
                                {/* Seleção de Técnico */}
                                {!isTecnico && (
                                    <div className="form-group">
                                        <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                            <UserCog size={16} /> Técnico Responsável *
                                        </label>
                                        <select
                                            className={`form-input ${formErrors.technicianId ? 'input-error' : ''}`}
                                            value={formData.tecnicoId}
                                            onChange={e => setFormData({ ...formData, tecnicoId: e.target.value })}
                                        >
                                            <option value="">Selecione o Técnico...</option>
                                            {listaTecnicos.map(t => (
                                                <option key={t.id} value={t.id}>{t.nome}</option>
                                            ))}
                                        </select>
                                        {formErrors.technicianId && <span className="error-message">{formErrors.technicianId}</span>}
                                    </div>
                                )}

                                {/* Data do Atendimento */}
                                <div className="form-group">
                                    <label>Data do Atendimento *</label>
                                    <input
                                        type="date"
                                        className={`form-input ${formErrors.serviceDate ? 'input-error' : ''}`}
                                        value={formData.serviceDate}
                                        onChange={e => setFormData({ ...formData, serviceDate: e.target.value })}
                                    />
                                    {formErrors.serviceDate && <span className="error-message">{formErrors.serviceDate}</span>}
                                </div>

                                {/* Custo de Deslocamento Removido */}

                                {/* Campo Número do Chamado */}
                                <div className="form-group">
                                    <label>Número do Chamado *</label>
                                    <input
                                        type="text"
                                        className={`form-input ${formErrors.numeroChamado ? 'input-error' : ''}`}
                                        value={formData.numeroChamado}
                                        onChange={e => setFormData({ ...formData, numeroChamado: e.target.value })}
                                        placeholder="Ex: CHAM-1234"
                                    />
                                    {formErrors.numeroChamado && <span className="error-message">{formErrors.numeroChamado}</span>}
                                </div>

                                {/* Tipo de Serviço */}
                                <div className="form-group">
                                    <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                        <Wrench size={16} /> Tipo de Serviço *
                                    </label>
                                    <select
                                        className={`form-input ${formErrors.serviceType ? 'input-error' : ''}`}
                                        value={formData.tipoServico}
                                        onChange={e => handleTipoServicoChange(e.target.value)}
                                    >
                                        <option value="">Selecione o tipo...</option>
                                        <option value="INSTALACAO">Instalação (Valor Fixo)</option>
                                        <option value="MANUTENCAO">Manutenção (Por Hora)</option>
                                    </select>
                                    {formErrors.serviceType && <span className="error-message">{formErrors.serviceType}</span>}
                                </div>
                                
                                {/* Origem da Manutenção */}
                                {formData.tipoServico === 'MANUTENCAO' && (
                                    <div className="form-group">
                                        <label>Origem do Atendimento *</label>
                                        <select
                                            className={`form-input ${formErrors.manutencaoOrigin ? 'input-error' : ''}`}
                                            value={formData.manutencaoOrigin}
                                            onChange={e => setFormData({ ...formData, manutencaoOrigin: e.target.value })}
                                        >
                                            <option value="">Indique a Origem...</option>
                                            <option value="CARMARQ">Carmarq (R$ 250/h)</option>
                                            <option value="VALENTIM">Valentim - Garantia (R$ 185/h)</option>
                                        </select>
                                        {formErrors.manutencaoOrigin && <span className="error-message">{formErrors.manutencaoOrigin}</span>}
                                    </div>
                                )}
                            </div>

                            {/* Valor do Serviço (Mão de Obra) */}
                            <div className="form-group" style={{ marginTop: '1rem' }}>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                    <DollarSign size={16} color="var(--primary-color)" /> 
                                    Valor do Serviço (Mão de Obra) {formData.tipoServico === 'MANUTENCAO' ? '(Bloqueado)' : '*'}
                                </label>
                                
                                {formData.tipoServico === 'MANUTENCAO' && (
                                    <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                                        Em modo Manutenção, o valor é gerado automaticamente lançando horas de trabalho.
                                    </p>
                                )}
                                {formData.tipoServico === 'INSTALACAO' && isTecnico && (
                                    <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                                        Valor herdado de Tabela. O valor final será confirmado pelo administrador.
                                    </p>
                                )}
                                
                                <input
                                    type="number"
                                    step="0.01"
                                    disabled={formData.tipoServico === 'INSTALACAO' || formData.tipoServico === 'MANUTENCAO'}
                                    className={`form-input ${formErrors.serviceValue ? 'input-error' : ''}`}
                                    placeholder={formData.tipoServico === 'MANUTENCAO' ? 'Cálculo dinâmico por horas...' : '0.00'}
                                    style={(formData.tipoServico === 'INSTALACAO' || formData.tipoServico === 'MANUTENCAO') ? { backgroundColor: '#f3f4f6', cursor: 'not-allowed' } : {}}
                                    value={formData.valorServico}
                                    onChange={e => setFormData({ ...formData, valorServico: e.target.value })}
                                />
                                {formErrors.serviceValue && <span className="error-message">{formErrors.serviceValue}</span>}
                            </div>

                            {/* Descrição do Problema */}
                            <div className="form-group">
                                <label>Descrição do Problema</label>
                                <textarea
                                    className="form-input"
                                    rows="3"
                                    placeholder="Descreva o problema relatado pelo cliente..."
                                    value={formData.descricaoProblema}
                                    onChange={e => setFormData({ ...formData, descricaoProblema: e.target.value })}
                                ></textarea>
                            </div>

                            {/* Observações */}
                            <div className="form-group">
                                <label>Observações Adicionais</label>
                                <textarea
                                    className="form-input"
                                    rows="2"
                                    placeholder="Observações adicionais..."
                                    value={formData.observacoes}
                                    onChange={e => setFormData({ ...formData, observacoes: e.target.value })}
                                ></textarea>
                            </div>

                            <button
                                type="submit"
                                className="btn-primary"
                                disabled={submitting}
                                style={{ width: '100%', marginTop: '1rem', padding: '0.85rem', fontSize: '1rem' }}
                            >
                                {submitting ? (
                                    <><Loader2 className="animate-spin" size={20} /> Criando...</>
                                ) : (
                                    <><Save size={20} /> Criar Ordem de Serviço</>
                                )}
                            </button>
                        </form>

                        {/* Coluna lateral — Painel Financeiro e Logístico */}
                        <div className="suggestions-panel">
                            {previewData ? (
                                <>
                                    {/* Resumo Financeiro Real (Vindo do Backend /Preview) */}
                                    <div className="suggestion-card finance-summary">
                                        <h3><DollarSign size={16} /> Resumo Financeiro</h3>
                                        <div className="finance-line">
                                            <span>Mão de Obra</span>
                                            <span className="value">R$ {(previewData.serviceValue || 0).toFixed(2)}</span>
                                        </div>
                                        <div className="finance-line">
                                            <span>Despesas</span>
                                            <span className="value">R$ {(previewData.expensesValue || 0).toFixed(2)}</span>
                                        </div>
                                        <div className="finance-line" style={{ color: '#ef4444' }}>
                                            <span>Pgto Técnico (Despesa 10%)</span>
                                            <span className="value">- R$ {(previewData.technicianPayment || 0).toFixed(2)}</span>
                                        </div>
                                        <div className="finance-line total-line">
                                            <span>Total Líquido Estimado</span>
                                            <span className="value" style={{ color: 'var(--primary-color)', fontWeight: 'bold' }}>
                                                R$ {(previewData.netProfit || 0).toFixed(2)}
                                            </span>
                                        </div>
                                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textAlign: 'right', marginTop: '0.5rem' }}>
                                            Total Cobrado: R$ {(previewData.totalValue || 0).toFixed(2)}
                                        </div>
                                    </div>

                                    {/* Estimativa de Viagem (Calculada) */}
                                    {previewData.distanceKm && (
                                        <div className="suggestion-card travel-estimate">
                                            <h3><MapPin size={16} /> Logística de Deslocamento</h3>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                                                <div>
                                                    <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Distância</p>
                                                    <p style={{ fontSize: '1.2rem', fontWeight: 700 }}>{previewData.distanceKm} km</p>
                                                </div>
                                                <div style={{ textAlign: 'right' }}>
                                                    <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Tempo Est.</p>
                                                    <p style={{ fontSize: '1.2rem', fontWeight: 700 }}>{previewData.estimatedMinutes} min</p>
                                                </div>
                                            </div>
                                            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', borderTop: '1px solid #eee', paddingTop: '0.5rem' }}>
                                                💡 Custo sugerido: <strong>R$ {(previewData.estimatedTravelCost || 0).toFixed(2)}</strong> (calculado)
                                            </p>
                                        </div>
                                    )}
                                </>
                            ) : (
                                <div className="suggestion-card suggestions-empty">
                                    <DollarSign size={32} />
                                    <p style={{ fontWeight: 600 }}>Resumo Financeiro</p>
                                    <p style={{ marginTop: '0.5rem' }}>
                                        Preencha a <strong>máquina</strong> e o <strong>cliente</strong> para visualizar as estimativas financeiras da OS.
                                    </p>
                                </div>
                            )}
                        </div>
                    </div>
                )}
            </main>

            <ClientModal 
                isOpen={isClientModalOpen}
                onClose={() => {
                    setIsClientModalOpen(false)
                    carregarClientes() // Refresh list after closing
                }}
            />
        </div>
    )
}