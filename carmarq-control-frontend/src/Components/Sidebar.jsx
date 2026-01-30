import React from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { LayoutDashboard, ClipboardList, LogOut, Users } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext' // Usa o contexto
import axios from 'axios'
import '../Styles/Sidebar.css'

export default function Sidebar() {
    const location = useLocation()
    const navigate = useNavigate()
    const { user, checkAuth } = useAuth() // Pega dados da memória

    const handleLogout = async () => {
        try {
            await axios.post('http://localhost:8080/api/auth/logout', {}, { withCredentials: true })
            localStorage.removeItem('userName')
            await checkAuth() // Limpa o estado
            navigate('/')
        } catch (error) {
            console.error("Erro no logout", error)
            navigate('/')
        }
    }

    const getLinkClass = (path) => {
        return location.pathname === path ? 'nav-link active' : 'nav-link'
    }

    // Se o usuário ainda não carregou, exibe skeleton ou nada
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

                {/* Verifica a ROLE direto da memória RAM */}
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