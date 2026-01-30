import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Sidebar from '../Components/Sidebar'
import { toast } from '../Components/ui/Toaster'
import { Save, Loader2, UserCog } from 'lucide-react'
import '../Styles/Form.css'

export default function NovaOS() {
    const navigate = useNavigate()

    const [listaClientes, setListaClientes] = useState([])
    const [listaMaquinas, setListaMaquinas] = useState([])
    const [listaTecnicos, setListaTecnicos] = useState([]) // Novo estado
    const [loading, setLoading] = useState(true)

    const [formData, setFormData] = useState({
        clienteId: '',
        maquinaId: '',
        tecnicoId: '', // Novo campo
        descricaoProblema: '',
        prioridade: 'normal'
    })

    useEffect(() => {
        async function carregarDados() {
            try {
                // Carrega tudo em paralelo
                const [clientes, maquinas, tecnicos] = await Promise.all([
                    api.getClientes(),
                    api.getMaquinas(),
                    api.getTecnicos()
                ])

                setListaClientes(clientes)
                setListaMaquinas(maquinas)
                setListaTecnicos(tecnicos)
                setLoading(false)
            } catch (error) {
                toast('Erro ao carregar dados.', 'error')
                setLoading(false)
            }
        }
        carregarDados()
    }, [])

    const handleSave = async (e) => {
        e.preventDefault()
        if(!formData.clienteId || !formData.maquinaId || !formData.tecnicoId) {
            toast('Preencha Cliente, Máquina e Técnico!', 'error')
            return
        }

        const payload = {
            empresa_id: formData.clienteId,
            maquina_id: formData.maquinaId,
            tecnico_responsavel_id: formData.tecnicoId, // Enviando técnico
            descricao: formData.descricaoProblema,
            prioridade: formData.prioridade,
            status: 'ABERTA',
            data_abertura: new Date().toISOString()
        }

        await api.createOS(payload)
        toast('OS Criada e Técnico Notificado!', 'success')
        navigate('/ordens')
    }

    const maquinaSelecionada = listaMaquinas.find(m => m.id === parseInt(formData.maquinaId))

    return (
        <div className="dashboard-layout">
            <Sidebar />
            <main className="dashboard-content">
                <h1 className="page-title">Nova Ordem de Serviço</h1>
                <p className="page-subtitle">Atribuição de equipe e maquinário</p>

                {loading ? (
                    <div className="loading-container"><Loader2 className="animate-spin" /> Carregando...</div>
                ) : (
                    <form className="form-card" onSubmit={handleSave} style={{marginTop: '1.5rem'}}>
                        <div className="form-grid">

                            <div className="form-group">
                                <label>Cliente / Empresa</label>
                                <select
                                    className="form-input"
                                    value={formData.clienteId}
                                    onChange={e => setFormData({...formData, clienteId: e.target.value})}
                                >
                                    <option value="">Selecione...</option>
                                    {listaClientes.map(c => (
                                        <option key={c.id} value={c.id}>{c.nome}</option>
                                    ))}
                                </select>
                            </div>

                            <div className="form-group">
                                <label>Máquina</label>
                                <select
                                    className="form-input"
                                    value={formData.maquinaId}
                                    onChange={e => setFormData({...formData, maquinaId: e.target.value})}
                                >
                                    <option value="">Selecione...</option>
                                    {listaMaquinas.map(m => (
                                        <option key={m.id} value={m.id}>{m.modelo}</option>
                                    ))}
                                </select>
                            </div>

                            {/* SELEÇÃO DE TÉCNICO (Novo) */}
                            <div className="form-group" style={{gridColumn: '1 / -1'}}>
                                <label style={{display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--primary-color)'}}>
                                    <UserCog size={18} /> Atribuir Técnico Responsável
                                </label>
                                <select
                                    className="form-input"
                                    style={{borderColor: 'var(--primary-color)', backgroundColor: '#f0fdf4'}}
                                    value={formData.tecnicoId}
                                    onChange={e => setFormData({...formData, tecnicoId: e.target.value})}
                                >
                                    <option value="">Selecione o Técnico...</option>
                                    {listaTecnicos.map(t => (
                                        <option key={t.id} value={t.id}>{t.nome}</option>
                                    ))}
                                </select>
                            </div>

                        </div>

                        {maquinaSelecionada && (
                            <div className="info-box-green">
                                <strong>Info Automática:</strong> {maquinaSelecionada.tipo_maquina} - Hora Técnica: R$ {maquinaSelecionada.valor_hora_tecnico.toFixed(2)}
                            </div>
                        )}

                        <div className="form-group">
                            <label>Descrição e Observações</label>
                            <textarea
                                className="form-input"
                                rows="3"
                                value={formData.descricaoProblema}
                                onChange={e => setFormData({...formData, descricaoProblema: e.target.value})}
                            ></textarea>
                        </div>

                        <button type="submit" className="btn-primary" style={{width: '100%', marginTop: '1rem'}}>
                            <Save size={20} /> Criar e Disparar Alerta
                        </button>
                    </form>
                )}
            </main>
        </div>
    )
}