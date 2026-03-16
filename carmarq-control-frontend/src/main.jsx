import React from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'

import './Styles/global.css'
import Toaster from './Components/ui/Toaster'
import PrivateRoute from './Components/PrivateRoute'

import Login from './Pages/Login'
import Dashboard from './Pages/Dashboard'
import Ordens from './Pages/Ordens'
import OrdemDetalhes from './Pages/OrdemDetalhes'
import NovaOS from './Pages/NovaOS'
import Usuarios from './Pages/Usuarios'
import Clients from './Pages/Clients'
import Machines from './Pages/Machines'

createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <AuthProvider>
            <BrowserRouter>
                <Toaster />
                <Routes>
                    <Route path="/" element={<Login />} />

                    {/* Rotas Protegidas */}
                    <Route path="/dashboard" element={
                        <PrivateRoute><Dashboard /></PrivateRoute>
                    } />

                    <Route path="/ordens" element={
                        <PrivateRoute><Ordens /></PrivateRoute>
                    } />

                    <Route path="/ordens/:id" element={
                        <PrivateRoute><OrdemDetalhes /></PrivateRoute>
                    } />

                    <Route path="/nova-os" element={
                        <PrivateRoute roles={['PROPRIETARIO']}><NovaOS /></PrivateRoute>
                    } />

                    <Route path="/usuarios" element={
                        <PrivateRoute roles={['PROPRIETARIO']}><Usuarios /></PrivateRoute>
                    } />

                    <Route path="/clientes" element={
                        <PrivateRoute roles={['PROPRIETARIO', 'FINANCEIRO', 'TECNICO']}><Clients /></PrivateRoute>
                    } />

                    {/* Nova rota: Biblioteca de Máquinas */}
                    <Route path="/maquinas" element={
                        <PrivateRoute roles={['PROPRIETARIO', 'FINANCEIRO', 'TECNICO']}><Machines /></PrivateRoute>
                    } />

                    <Route path="*" element={<Navigate to="/" />} />
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    </React.StrictMode>
)