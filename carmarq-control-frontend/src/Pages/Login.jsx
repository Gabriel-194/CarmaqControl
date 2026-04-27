import React, { useState } from "react"
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import { useAuth } from '../contexts/AuthContext'
import { AlertCircle, Loader2, Eye, EyeOff } from 'lucide-react'
import logo from '../assets/logo-carmaq.png'
import '../Styles/Login.css'

export default function Login() {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [isLoading, setIsLoading] = useState(false)
    const [showPassword, setShowPassword] = useState(false)

    const navigate = useNavigate()
    const { checkAuth } = useAuth() // Pega a função do contexto

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError('')
        setIsLoading(true)

        try {
            // 1. Faz Login no Java (Cookie é setado automaticamente pelo navegador)
            const response = await axios.post('http://localhost:8080/api/auth/login',
                {
                    email: email,
                    senha: password
                },
                {
                    withCredentials: true // OBRIGATÓRIO: Permite receber/enviar cookies
                }
            )

            // 2. Se o backend confirmou o sucesso
            if (response.data.success) {
                // 3. Força uma validação imediata para atualizar o estado global (Contexto)
                await checkAuth()

                // 4. Redireciona para o Dashboard
                navigate('/dashboard')
            } else {
                setError('Credenciais inválidas.')
            }

        } catch (err) {
            console.error(err)
            if (err.response && err.response.status === 401) {
                setError('E-mail ou senha incorretos.')
            } else {
                setError('Erro de conexão com o servidor.')
            }
        } finally {
            setIsLoading(false)
        }
    }

    return (
        <main className="login-container">
            <div className="login-wrapper">
                <div className="login-header">
                    <div className="logo-box" style={{ background: 'transparent', height: 'auto', width: 'auto', display: 'flex', justifyContent: 'center' }}>
                        <img src={logo} alt="Carmaq" style={{ maxWidth: '240px', height: 'auto' }} />
                    </div>
                    <h1 style={{ marginBottom: '0.25rem', marginTop: '0.5rem', color: 'var(--text-color)', fontSize: '1.6rem' }}>CarmarqControl</h1>
                    <p style={{ color: 'var(--text-muted)' }}>Acesso ao Sistema</p>
                </div>

                <div className="login-card">
                    <h2 className="login-title">Entrar</h2>
                    <p className="login-subtitle">Digite suas credenciais para acessar</p>

                    <form onSubmit={handleSubmit}>
                        {error && (
                            <div className="error-alert">
                                <AlertCircle size={16} />
                                <span>{error}</span>
                            </div>
                        )}

                        <div className="form-group">
                            <label htmlFor="email">E-mail</label>
                            <input
                                id="email"
                                type="email"
                                className="form-input"
                                placeholder="seu@email.com"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="password">Senha</label>
                            <div style={{ position: 'relative' }}>
                                <input
                                    id="password"
                                    type={showPassword ? "text" : "password"}
                                    className="form-input"
                                    placeholder="••••••••"
                                    style={{ paddingRight: '2.5rem' }}
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                />
                                <button
                                    type="button"
                                    className="password-toggle"
                                    onClick={() => setShowPassword(!showPassword)}
                                    style={{
                                        position: 'absolute',
                                        right: '0.75rem',
                                        top: '50%',
                                        transform: 'translateY(-50%)',
                                        background: 'none',
                                        border: 'none',
                                        cursor: 'pointer',
                                        color: 'var(--text-muted)',
                                        display: 'flex',
                                        alignItems: 'center',
                                        padding: 0
                                    }}
                                >
                                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                                </button>
                            </div>
                        </div>

                        <button type="submit" className="btn-primary" disabled={isLoading}>
                            {isLoading ? (
                                <>
                                    <Loader2 className="icon animate-spin" />
                                    Entrando...
                                </>
                            ) : (
                                'Acessar Conta'
                            )}
                        </button>
                    </form>
                </div>
            </div>
        </main>
    )
}