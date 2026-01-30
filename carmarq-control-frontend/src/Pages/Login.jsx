import React, { useState } from "react"
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import { useAuth } from '../contexts/AuthContext'
import { Wrench, AlertCircle, Loader2 } from 'lucide-react'
import '../Styles/Login.css'

export default function Login() {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [isLoading, setIsLoading] = useState(false)

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
                    <div className="logo-box">
                        <Wrench size={32} />
                    </div>
                    <h1>CarmarqControl</h1>
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
                            <input
                                id="password"
                                type="password"
                                className="form-input"
                                placeholder="••••••••"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                            />
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