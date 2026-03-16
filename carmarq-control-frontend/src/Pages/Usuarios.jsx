import React, { useState, useEffect } from 'react'
import Sidebar from '../Components/Sidebar'
import { toast } from '../Components/ui/Toaster'
import { Plus, Trash2, UserPlus, Pencil, X, RefreshCw } from 'lucide-react'
import axios from 'axios'
import '../Styles/Usuarios.css'

export default function Usuarios() {
    const [usuarios, setUsuarios] = useState([])
    const [loading, setLoading] = useState(true)
    const [showModal, setShowModal] = useState(false)

    // Estado para controlar se é Edição ou Criação
    const [editingUser, setEditingUser] = useState(null)

    // Form do usuário
    const [formData, setFormData] = useState({ nome: '', email: '', telefone: '', role: 'TECNICO', senha: '' })
    const [formErrors, setFormErrors] = useState({})

    useEffect(() => {
        carregarUsuarios()
    }, [])

    async function carregarUsuarios() {
        try {
            const response = await axios.get('http://localhost:8080/api/users', { withCredentials: true })
            setUsuarios(response.data)
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
            telefone: user.telefone || '',
            role: user.role,
            senha: '' // Senha opcional na edição
        })
        setShowModal(true)
    }

    const handleDeleteUser = async (id) => {
        if(window.confirm("Tem certeza que deseja desativar este usuário?")) {
            try {
                await axios.delete(`http://localhost:8080/api/users/${id}`, { withCredentials: true })
                toast('Usuário desativado', 'success')
                carregarUsuarios()
            } catch (err) {
                toast('Erro ao desativar usuário', 'error')
            }
        }
    }

    const handleRestoreUser = async (id) => {
        if(window.confirm("Deseja restaurar o acesso deste usuário?")) {
            try {
                await axios.put(`http://localhost:8080/api/users/${id}/restore`, {}, { withCredentials: true })
                toast('Usuário restaurado com sucesso', 'success')
                carregarUsuarios()
            } catch (err) {
                toast('Erro ao restaurar usuário', 'error')
            }
        }
    }

    const handleSave = async (e) => {
        e.preventDefault()
        setFormErrors({})

        // Validação básica local (opcional agora que temos backend forte, mas bom deixar)
        if(!formData.nome || !formData.email) {
            toast('Nome e E-mail são obrigatórios', 'error')
            return
        }

        try {
            if (editingUser) {
                await axios.put(`http://localhost:8080/api/users/${editingUser.id}`, formData, { withCredentials: true })
                toast(`Usuário ${formData.nome} atualizado!`, 'success')
            } else {
                await axios.post('http://localhost:8080/api/users', formData, { withCredentials: true })
                toast(`Usuário ${formData.nome} cadastrado!`, 'success')
            }

            setShowModal(false)
            carregarUsuarios()
        } catch (error) {
            if (error.response && error.response.status === 400 && error.response.data.errors) {
                setFormErrors(error.response.data.errors)
                toast('Verifique os campos obrigatórios.', 'error')
            } else if (error.response && error.response.status === 409) {
                setFormErrors({ email: error.response.data.message })
                toast(error.response.data.message, 'error')
            } else {
                toast('Erro ao salvar dados', 'error')
            }
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
                                    <h3>{user.nome} {!user.ativo && <span style={{color:'red', fontSize:'12px'}}>(Inativo)</span>}</h3>
                                    <p title={user.email}>{user.email}</p>
                                    <span className={`role-badge role-${user.role.toLowerCase()}`}>
                                        {user.role}
                                    </span>
                                </div>

                                {/* Ações do Card */}
                                <div className="card-actions">
                                    {user.ativo ? (
                                        <>
                                            <button
                                                className="btn-icon btn-edit"
                                                title="Editar Usuário"
                                                onClick={() => handleEditUser(user)}
                                            >
                                                <Pencil size={18} />
                                            </button>
                                            <button
                                                className="btn-icon btn-danger"
                                                title="Desativar Usuário"
                                                onClick={() => handleDeleteUser(user.id)}
                                            >
                                                <Trash2 size={18} />
                                            </button>
                                        </>
                                    ) : (
                                        <button
                                            className="btn-icon"
                                            style={{color: '#10b981', border: '1px solid #10b981', background: 'transparent'}}
                                            title="Recuperar Usuário"
                                            onClick={() => handleRestoreUser(user.id)}
                                        >
                                            <RefreshCw size={18} />
                                        </button>
                                    )}
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
                                        className={`form-input ${formErrors.nome ? 'input-error' : ''}`}
                                        value={formData.nome}
                                        onChange={e => setFormData({...formData, nome: e.target.value})}
                                    />
                                    {formErrors.nome && <span className="error-message">{formErrors.nome}</span>}
                                </div>
                                <div className="form-group">
                                    <label>E-mail de Acesso</label>
                                    <input
                                        type="email"
                                        className={`form-input ${formErrors.email ? 'input-error' : ''}`}
                                        value={formData.email}
                                        onChange={e => setFormData({...formData, email: e.target.value})}
                                    />
                                    {formErrors.email && <span className="error-message">{formErrors.email}</span>}
                                </div>
                                <div className="form-group">
                                    <label>Telefone / WhatsApp</label>
                                    <input
                                        type="text"
                                        className="form-input"
                                        placeholder="(00) 00000-0000"
                                        value={formData.telefone}
                                        onChange={e => setFormData({...formData, telefone: e.target.value})}
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