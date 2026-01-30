import React, { useState, useEffect } from 'react'
import Sidebar from '../Components/Sidebar'
import { toast } from '../Components/ui/Toaster'
import { Plus, Trash2, UserPlus, Pencil, X } from 'lucide-react' // Adicionado Pencil e X
import '../Styles/Usuarios.css'

export default function Usuarios() {
    const [usuarios, setUsuarios] = useState([])
    const [loading, setLoading] = useState(true)
    const [showModal, setShowModal] = useState(false)

    // Estado para controlar se é Edição ou Criação
    const [editingUser, setEditingUser] = useState(null)

    // Form do usuário
    const [formData, setFormData] = useState({ nome: '', email: '', role: 'TECNICO', senha: '' })

    useEffect(() => {
        carregarUsuarios()
    }, [])

    async function carregarUsuarios() {
        try {
            const dados = await api.getUsuarios()
            setUsuarios(dados)
        } catch (err) {
            toast('Erro ao carregar usuários', 'error')
        } finally {
            setLoading(false)
        }
    }

    // Abre o modal limpo para CRIAR
    const handleNewUser = () => {
        setEditingUser(null)
        setFormData({ nome: '', email: '', role: 'TECNICO', senha: '' })
        setShowModal(true)
    }

    // Abre o modal preenchido para EDITAR
    const handleEditUser = (user) => {
        setEditingUser(user)
        // Preenche o form com os dados atuais (senha fica vazia por segurança)
        setFormData({
            nome: user.nome,
            email: user.email,
            role: user.role,
            senha: '' // Senha opcional na edição
        })
        setShowModal(true)
    }

    const handleDeleteUser = async (id) => {
        if(window.confirm("Tem certeza que deseja remover este usuário?")) {
            await api.deleteUsuario(id)
            toast('Usuário removido', 'success')
            carregarUsuarios()
        }
    }

    const handleSave = async (e) => {
        e.preventDefault()

        // Validação básica
        if(!formData.nome || !formData.email) {
            toast('Nome e E-mail são obrigatórios', 'error')
            return
        }

        // Se for criação, senha é obrigatória. Se for edição, é opcional.
        if(!editingUser && !formData.senha) {
            toast('Senha é obrigatória para novos usuários', 'error')
            return
        }

        try {
            if (editingUser) {
                // MODO EDIÇÃO
                await api.updateUsuario(editingUser.id, formData)
                toast(`Usuário ${formData.nome} atualizado!`, 'success')
            } else {
                // MODO CRIAÇÃO
                await api.createUsuario(formData)
                toast(`Usuário ${formData.nome} cadastrado!`, 'success')
            }

            setShowModal(false)
            carregarUsuarios()
        } catch (error) {
            toast('Erro ao salvar dados', 'error')
        }
    }

    return (
        <div className="dashboard-layout">
            <Sidebar />
            <main className="dashboard-content">
                <header className="page-header">
                    <div>
                        <h1 className="page-title">Gestão de Equipe</h1>
                        <p className="page-subtitle">Cadastre técnicos, financeiro e administradores</p>
                    </div>
                    <button className="btn-primary" onClick={handleNewUser}>
                        <UserPlus size={20} /> Novo Usuário
                    </button>
                </header>

                {loading ? (
                    <p>Carregando...</p>
                ) : (
                    <div className="users-grid">
                        {usuarios.map(user => (
                            <div key={user.id} className="user-card">
                                <div className="user-avatar-large">
                                    {user.nome[0].toUpperCase()}
                                </div>
                                <div className="user-info">
                                    <h3>{user.nome}</h3>
                                    <p title={user.email}>{user.email}</p>
                                    <span className={`role-badge role-${user.role.toLowerCase()}`}>
                                        {user.role}
                                    </span>
                                </div>

                                {/* Ações do Card */}
                                <div className="card-actions">
                                    <button
                                        className="btn-icon btn-edit"
                                        title="Editar Usuário"
                                        onClick={() => handleEditUser(user)}
                                    >
                                        <Pencil size={18} />
                                    </button>
                                    <button
                                        className="btn-icon btn-danger"
                                        title="Remover Usuário"
                                        onClick={() => handleDeleteUser(user.id)}
                                    >
                                        <Trash2 size={18} />
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                {/* Modal (Serve para Criar e Editar) */}
                {showModal && (
                    <div className="modal-overlay">
                        <div className="modal-content">
                            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem'}}>
                                <h2>{editingUser ? 'Editar Usuário' : 'Novo Usuário'}</h2>
                                <button onClick={() => setShowModal(false)} style={{background: 'none', border: 'none', cursor: 'pointer'}}>
                                    <X size={24} color="#6b7280" />
                                </button>
                            </div>

                            <form onSubmit={handleSave}>
                                <div className="form-group">
                                    <label>Nome Completo</label>
                                    <input
                                        type="text"
                                        className="form-input"
                                        value={formData.nome}
                                        onChange={e => setFormData({...formData, nome: e.target.value})}
                                    />
                                </div>
                                <div className="form-group">
                                    <label>E-mail de Acesso</label>
                                    <input
                                        type="email"
                                        className="form-input"
                                        value={formData.email}
                                        onChange={e => setFormData({...formData, email: e.target.value})}
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Função no Sistema</label>
                                    <select
                                        className="form-input"
                                        value={formData.role}
                                        onChange={e => setFormData({...formData, role: e.target.value})}
                                    >
                                        <option value="TECNICO">Técnico (Acesso Restrito)</option>
                                        <option value="FINANCEIRO">Financeiro (Ver Valores)</option>
                                        <option value="PROPRIETARIO">Proprietário (Admin Total)</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label>Senha {editingUser && '(Deixe em branco para não alterar)'}</label>
                                    <input
                                        type="password"
                                        className="form-input"
                                        placeholder={editingUser ? "Nova senha (opcional)" : "******"}
                                        value={formData.senha}
                                        onChange={e => setFormData({...formData, senha: e.target.value})}
                                    />
                                </div>

                                <div className="modal-actions">
                                    <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>Cancelar</button>
                                    <button type="submit" className="btn-primary">
                                        {editingUser ? 'Salvar Alterações' : 'Criar Usuário'}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                )}
            </main>
        </div>
    )
}