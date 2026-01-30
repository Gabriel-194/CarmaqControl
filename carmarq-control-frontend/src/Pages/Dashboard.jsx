import React from 'react'
import Sidebar from '../Components/Sidebar'
import { useAuth } from '../contexts/AuthContext'
import { ProprietarioDashboard } from '../Components/Dashboards/ProprietarioDashboard'
import { TecnicoDashboard } from '../Components/Dashboards/TecnicoDashboard'
import { FinanceiroDashboard } from '../Components/Dashboards/FinanceiroDashboard'
import '../Styles/Dashboards.css'
import '../Styles/dashboard.css'

export default function Dashboard() {
    const { user } = useAuth()

    if (!user) return null

    const renderContent = () => {
        // Agora user.role existe!
        switch (user.role) {
            case 'PROPRIETARIO':
                return <ProprietarioDashboard />

            case 'TECNICO':
                return <TecnicoDashboard />

            case 'FINANCEIRO':
                return <FinanceiroDashboard />

            default:
                return <div style={{padding: '2rem'}}>Bem vindo, {user.name}</div>
        }
    }

    return (
        <div className="dashboard-layout">
            <Sidebar />
            <main className="dashboard-content">
                {renderContent()}
            </main>
        </div>
    )
}