import React, { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import Sidebar from '../Components/Sidebar'
import { toast } from '../Components/ui/Toaster'
import { Save, Loader2, UserCog, Zap, DollarSign, Clock, MapPin, Wrench, Lightbulb, Package, FileText } from 'lucide-react'
import axios from 'axios'
import '../Styles/Form.css'
import '../Styles/NovaOS.css'

const API_BASE = 'http://localhost:8080/api'

// Página de criação de OS com Automação Inteligente
// Selecionar uma máquina aciona sugestões automáticas via API
export default function NovaOS() {
    const navigate = useNavigate()

    const [listaClientes, setListaClientes] = useState([])
    const [listaMaquinas, setListaMaquinas] = useState([])
    const [listaTecnicos, setListaTecnicos] = useState([])
    const [loading, setLoading] = useState(true)
    const [submitting, setSubmitting] = useState(false)
    const [formErrors, setFormErrors] = useState({})
    const [suggestions, setSuggestions] = useState(null)

    const [formData, setFormData] = useState({
        clienteId: '',
        maquinaId: '',
        tecnicoId: '',
        descricaoProblema: '',
        prioridade: 'NORMAL',
        observacoes: '',
        custoDeslocamento: '',
        tipoServico: '',
        valorServico: ''
    })

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

    // Busca sugestões automáticas ao selecionar uma máquina
    const fetchSuggestions = useCallback(async (machineId) => {
        if (!machineId) {
            setSuggestions(null)
            return
        }
        try {
            const res = await axios.get(`${API_BASE}/service-orders/suggestions`, {
                params: { machineId },
                withCredentials: true
            })
            setSuggestions(res.data)
        } catch (error) {
            console.error('Erro ao buscar sugestões', error)
        }
    }, [])

    // Quando a máquina muda, busca sugestões
    const handleMachineChange = (machineId) => {
        setFormData(prev => ({ ...prev, maquinaId: machineId }))
        fetchSuggestions(machineId)
    }

    // Submissão do formulário
    const handleSave = async (e) => {
        e.preventDefault()
        if (!formData.clienteId || !formData.maquinaId || !formData.tecnicoId) {
            toast('Preencha Cliente, Máquina e Técnico!', 'error')
            return
        }

        setSubmitting(true)
        setFormErrors({})
        const payload = {
            clientId: parseInt(formData.clienteId),
            machineId: parseInt(formData.maquinaId),
            technicianId: parseInt(formData.tecnicoId),
            problemDescription: formData.descricaoProblema,
            priority: formData.prioridade,
            observations: formData.observacoes,
            travelCost: formData.custoDeslocamento ? parseFloat(formData.custoDeslocamento) : null,
            serviceType: formData.tipoServico,
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

    // Calcula totais em tempo real usando valor manual
    const clienteSelecionado = listaClientes.find(c => c.id === parseInt(formData.clienteId))
    const serviceValue = formData.valorServico ? parseFloat(formData.valorServico) : 0
    const travelCost = formData.custoDeslocamento ? parseFloat(formData.custoDeslocamento) : 0
    const totalEstimado = serviceValue + travelCost
    const technicianPayment = serviceValue * 0.10

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
                                <label>Cliente / Empresa *</label>
                                <select
                                    className={`form-input ${formErrors.clientId ? 'input-error' : ''}`}
                                    value={formData.clienteId}
                                    onChange={e => setFormData({ ...formData, clienteId: e.target.value })}
                                >
                                    <option value="">Selecione o cliente...</option>
                                    {listaClientes.map(c => (
                                        <option key={c.id} value={c.id}>{c.companyName}</option>
                                    ))}
                                </select>
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
                                            {m.machineType} — {m.model} {m.brand ? `(${m.brand})` : ''}
                                        </option>
                                    ))}
                                </select>
                                {formErrors.machineId && <span className="error-message">{formErrors.machineId}</span>}
                            </div>

                            {/* Sugestão de tipo de serviço */}
                            {suggestions && (
                                <div className="service-type-badge">
                                    <Wrench size={14} /> {suggestions.suggestedServiceType}
                                </div>
                            )}

                            <div className="form-grid">
                                {/* Seleção de Técnico */}
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

                                {/* Prioridade */}
                                <div className="form-group">
                                    <label>Prioridade</label>
                                    <select
                                        className="form-input"
                                        value={formData.prioridade}
                                        onChange={e => setFormData({ ...formData, prioridade: e.target.value })}
                                    >
                                        <option value="BAIXA">🟢 Baixa</option>
                                        <option value="NORMAL">🔵 Normal</option>
                                        <option value="ALTA">🟠 Alta</option>
                                        <option value="URGENTE">🔴 Urgente</option>
                                    </select>
                                </div>

                                {/* Custo de deslocamento */}
                                <div className="form-group">
                                    <label>Custo de Deslocamento (R$)</label>
                                    <input
                                        type="number"
                                        step="0.01"
                                        className="form-input"
                                        placeholder="0.00"
                                        value={formData.custoDeslocamento}
                                        onChange={e => setFormData({ ...formData, custoDeslocamento: e.target.value })}
                                    />
                                </div>

                                {/* Tipo de Serviço */}
                                <div className="form-group">
                                    <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                        <Wrench size={16} /> Tipo de Serviço *
                                    </label>
                                    <input
                                        type="text"
                                        className={`form-input ${formErrors.serviceType ? 'input-error' : ''}`}
                                        placeholder={suggestions?.suggestedServiceType || 'Ex: Manutenção Preventiva, Reparo...'}
                                        value={formData.tipoServico}
                                        onChange={e => setFormData({ ...formData, tipoServico: e.target.value })}
                                    />
                                    {formErrors.serviceType && <span className="error-message">{formErrors.serviceType}</span>}
                                </div>
                            </div>

                            {/* Valor do Serviço (Mão de Obra) */}
                            <div className="form-group" style={{ marginTop: '1rem' }}>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                    <DollarSign size={16} color="var(--primary-color)" /> Valor do Serviço (Mão de Obra) * (R$)
                                </label>
                                <input
                                    type="number"
                                    step="0.01"
                                    className={`form-input ${formErrors.serviceValue ? 'input-error' : ''}`}
                                    placeholder={suggestions ? `Sugestão: R$ ${suggestions.estimatedServiceValue?.toFixed(2)}` : '0.00'}
                                    style={formData.valorServico ? { borderColor: 'var(--primary-color)', backgroundColor: '#f0fdf4' } : {}}
                                    value={formData.valorServico}
                                    onChange={e => setFormData({ ...formData, valorServico: e.target.value })}
                                />
                                {formErrors.serviceValue && <span className="error-message">{formErrors.serviceValue}</span>}
                                {suggestions && !formData.valorServico && !formErrors.serviceValue && (
                                    <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                                        💡 Valor sugerido com base na máquina: R$ {suggestions.estimatedServiceValue?.toFixed(2)}
                                    </p>
                                )}
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
                                    placeholder={suggestions?.autoObservation || 'Observações adicionais...'}
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

                        {/* Coluna lateral — Painel de Sugestões Automáticas */}
                        <div className="suggestions-panel">
                            {suggestions ? (
                                <>
                                    {/* Resumo Financeiro Automático */}
                                    <div className="suggestion-card finance-summary">
                                        <h3><DollarSign size={16} /> Resumo Financeiro</h3>
                                        <div className="finance-line">
                                            <span>Mão de Obra</span>
                                            <span className="value">R$ {serviceValue.toFixed(2)}</span>
                                        </div>
                                        <div className="finance-line">
                                            <span>Deslocamento</span>
                                            <span className="value">R$ {travelCost.toFixed(2)}</span>
                                        </div>
                                        <div className="finance-line">
                                            <span>Pgto Técnico (10%)</span>
                                            <span className="value">R$ {technicianPayment.toFixed(2)}</span>
                                        </div>
                                        <div className="finance-line total-line">
                                            <span>Total Estimado</span>
                                            <span className="value">R$ {totalEstimado.toFixed(2)}</span>
                                        </div>
                                    </div>

                                    {/* Estimativa de Tempo */}
                                    <div className="suggestion-card">
                                        <h3><Clock size={16} /> Estimativa de Tempo</h3>
                                        <div style={{ fontSize: '2rem', fontWeight: '700', color: 'var(--primary-color)' }}>
                                            {suggestions.estimatedHours}h
                                        </div>
                                        <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                                            R$ {suggestions.hourlyRate.toFixed(2)}/hora
                                        </p>
                                    </div>

                                    {/* Peças Sugeridas */}
                                    {suggestions.suggestedParts && suggestions.suggestedParts.length > 0 && (
                                        <div className="suggestion-card">
                                            <h3><Package size={16} /> Peças Frequentes</h3>
                                            <ul className="suggested-parts-list">
                                                {suggestions.suggestedParts.map((part, idx) => (
                                                    <li key={idx}>
                                                        <span className="part-dot"></span>
                                                        {part}
                                                    </li>
                                                ))}
                                            </ul>
                                            <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.5rem' }}>
                                                * Peças podem ser adicionadas depois de criar a OS
                                            </p>
                                        </div>
                                    )}

                                    {/* Info da Máquina */}
                                    <div className="suggestion-card">
                                        <h3><Lightbulb size={16} /> Detalhes da Máquina</h3>
                                        <p style={{ fontSize: '0.9rem', fontWeight: 600 }}>{suggestions.machineType}</p>
                                        <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                                            {suggestions.machineModel}
                                            {suggestions.machineBrand && ` • ${suggestions.machineBrand}`}
                                        </p>
                                        {suggestions.machineDescription && (
                                            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.5rem' }}>
                                                {suggestions.machineDescription}
                                            </p>
                                        )}
                                    </div>
                                </>
                            ) : (
                                <div className="suggestion-card suggestions-empty">
                                    <Zap size={32} />
                                    <p style={{ fontWeight: 600 }}>Automação Inteligente</p>
                                    <p style={{ marginTop: '0.5rem' }}>
                                        Selecione uma <strong>máquina</strong> para ativar as sugestões automáticas de valor, tempo estimado e peças frequentes.
                                    </p>
                                </div>
                            )}
                        </div>
                    </div>
                )}
            </main>
        </div>
    )
}