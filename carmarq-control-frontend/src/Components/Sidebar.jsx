import React from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { LayoutDashboard, ClipboardList, LogOut, Users, Building2, Cog } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import axios from 'axios'
import '../Styles/Sidebar.css'

export default function Sidebar() {
    const location = useLocation()
    const navigate = useNavigate()
    const { user, checkAuth } = useAuth()

    const handleLogout = async () => {
        try {
            await axios.post('http://localhost:8080/api/auth/logout', {}, { withCredentials: true })
            localStorage.removeItem('userName')
            await checkAuth()
            navigate('/')
        } catch (error) {
            console.error("Erro no logout", error)
            navigate('/')
        }
    }

    const getLinkClass = (path) => {
        return location.pathname === path ? 'nav-link active' : 'nav-link'
    }

    // Se o usuário ainda não carregou, não exibe
    if (!user) return null

    return (
        <aside className="sidebar">
            <div className="sidebar-header">
                <h2 className="sidebar-title">CarmarqControl</h2>
            </div>

            <nav className="sidebar-nav">
                <Link to="/dashboard" className={getLinkClass('/dashboard')}>
                    <LayoutDashboard className="icon" />
                    Dashboard
                </Link>

                <Link to="/ordens" className={getLinkClass('/ordens')}>
                    <ClipboardList className="icon" />
                    Ordens de Serviço
                </Link>

                {/* Nova OS — visível para PROPRIETARIO e TECNICO */}
                {(user.role === 'PROPRIETARIO' || user.role === 'TECNICO') && (
                    <Link to="/nova-os" className={getLinkClass('/nova-os')}>
                        <ClipboardList className="icon" />
                        Nova OS
                    </Link>
                )}

                {/* Clientes — visível para todos os cargos autorizados */}
                <Link to="/clientes" className={getLinkClass('/clientes')}>
                    <Building2 className="icon" />
                    Clientes
                </Link>

                {/* Biblioteca de Máquinas — visível apenas para PROPRIETARIO e FINANCEIRO */}
                {user.role !== 'TECNICO' && (
                    <Link to="/maquinas" className={getLinkClass('/maquinas')}>
                        <Cog className="icon" />
                        Máquinas
                    </Link>
                )}

                {/* Equipe — apenas PROPRIETARIO */}
                {user.role === 'PROPRIETARIO' && (
                    <Link to="/usuarios" className={getLinkClass('/usuarios')}>
                        <Users className="icon" />
                        Equipe
                    </Link>
                )}
            </nav>

            <div className="sidebar-footer">
                <div className="user-info">
                    <div className="user-avatar" style={{ backgroundColor: 'var(--primary-light)', color: 'var(--primary-color)' }}>
                        {user.name?.[0] || 'U'}
                    </div>
                    <div className="user-details">
                        <p><strong>{user.name}</strong></p>
                        <p className="user-role">{user.role}</p>
                    </div>
                </div>
                <button onClick={handleLogout} className="btn-logout">
                    <LogOut className="icon" />
                    Sair
                </button>
            </div>
        </aside>
    )
}