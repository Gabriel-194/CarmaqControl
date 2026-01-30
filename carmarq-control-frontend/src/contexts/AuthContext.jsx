import React, { createContext, useContext, useState, useEffect } from 'react'
import axios from 'axios'

const AuthContext = createContext({})

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null)
    const [loading, setLoading] = useState(true)

    const checkAuth = async () => {
        try {
            const response = await axios.post('http://localhost:8080/api/auth/validate', {}, {
                withCredentials: true
            })

            if (response.data.valid) {
                setUser({
                    // Pegamos os dados que o Backend extraiu do Token/Banco
                    name: response.data.nome,
                    role: response.data.role,
                    isAuthenticated: true
                })
            } else {
                setUser(null)
            }
        } catch (error) {
            setUser(null)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        if (window.location.pathname === '/' || window.location.pathname === '/login') {
            setLoading(false)
            return
        }
        checkAuth()
    }, [])

    return (
        <AuthContext.Provider value={{ user, loading, checkAuth, setUser }}>
            {children}
        </AuthContext.Provider>
    )
}

export const useAuth = () => useContext(AuthContext)