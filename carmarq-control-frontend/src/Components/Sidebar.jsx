import React, { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { LayoutDashboard, ClipboardList, LogOut, Users, Building2, Cog, Menu, X, Moon, Sun, CirclePlus } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { useTheme } from '../contexts/ThemeContext'
import axios from 'axios'
import '../Styles/Sidebar.css'
import logo from '../assets/logo-carmaq.png'

export default function Sidebar() {
    const location = useLocation()
    const navigate = useNavigate()
    const { user, checkAuth } = useAuth()
    const { theme, toggleTheme } = useTheme()
    const [isOpen, setIsOpen] = useState(false)

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

    const closeSidebar = () => {
        if (window.innerWidth <= 768) {
            setIsOpen(false)
        }
    }

    if (!user) return null

    return (
        <>
            {/* Cabecalho Mobile (Somente aparece em telas pequenas) */}
            <div className="mobile-header">
                <button className="mobile-toggle" onClick={() => setIsOpen(true)}>
                    <Menu size={24} />
                </button>
                <h2 className="mobile-title">Carmaq Control</h2>
            </div>

            {/* Overlay para escurecer o fundo no mobile */}
            {isOpen && <div className="sidebar-overlay" onClick={closeSidebar}></div>}

            <aside className={`sidebar ${isOpen ? 'open' : ''}`}>
                <div className="sidebar-header">
                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100%', marginBottom: '1rem' }}>
                        <img src={logo} alt="Carmaq Logo" className="sidebar-logo" style={{ maxWidth: '160px' }} />
                        <span style={{ fontSize: '1.25rem', fontWeight: 'bold', color: 'var(--primary-color)', marginTop: '0.75rem' }}>Carmaq Control</span>
                    </div>
                    <button className="mobile-close" onClick={closeSidebar}>
                        <X size={24} />
                    </button>
                </div>

                <nav className="sidebar-nav">
                    <Link to="/dashboard" className={getLinkClass('/dashboard')} onClick={closeSidebar}>
                        <LayoutDashboard className="icon" />
                        Dashboard
                    </Link>

                    <Link to="/ordens" className={getLinkClass('/ordens')} onClick={closeSidebar}>
                        <ClipboardList className="icon" />
                        Ordens de Serviço
                    </Link>

                    {(user.role === 'PROPRIETARIO' || user.role === 'TECNICO') && (
                        <Link to="/nova-os" className={getLinkClass('/nova-os')} onClick={closeSidebar}>
                            <CirclePlus className="icon" />
                            Nova OS
                        </Link>
                    )}

                    <Link to="/clientes" className={getLinkClass('/clientes')} onClick={closeSidebar}>
                        <Building2 className="icon" />
                        Clientes
                    </Link>

                    {user.role !== 'TECNICO' && (
                        <Link to="/maquinas" className={getLinkClass('/maquinas')} onClick={closeSidebar}>
                            <Cog className="icon" />
                            Máquinas
                        </Link>
                    )}

                    {user.role === 'PROPRIETARIO' && (
                        <Link to="/usuarios" className={getLinkClass('/usuarios')} onClick={closeSidebar}>
                            <Users className="icon" />
                            Equipe
                        </Link>
                    )}
                </nav>

                <div className="sidebar-footer">
                    <div className="sidebar-user-profile">
                        <div className="sidebar-user-avatar" style={{ backgroundColor: 'var(--primary-light)', color: 'var(--primary-color)' }}>
                            {user.name?.[0] || 'U'}
                        </div>
                        <div className="sidebar-user-details">
                            <p><strong>{user.name}</strong></p>
                            <p className="sidebar-user-role">{user.role}</p>
                        </div>
                    </div>
                    <button onClick={handleLogout} className="btn-logout">
                        <LogOut className="icon" />
                        Sair
                    </button>
                    <button onClick={toggleTheme} className="btn-theme-toggle">
                        {theme === 'dark' ? <Sun className="icon" /> : <Moon className="icon" />}
                        {theme === 'dark' ? 'Modo Claro' : 'Modo Escuro'}
                    </button>
                </div>
            </aside>
        </>
    )
}
