import React, { useState, useEffect } from 'react'
import Sidebar from '../Components/Sidebar'
import MachineModal from '../Components/Machines/MachineModal'
import MachineTooltip from '../Components/Machines/MachineTooltip'
import { typeLabels } from '../Constants/MachineConstants'
import { toast } from '../Components/ui/Toaster'
import { Search, Plus, Edit2, Trash2, RotateCcw, Loader2, Cog } from 'lucide-react'
import axios from 'axios'
import '../Styles/Machines.css'

const API_URL = 'http://localhost:8080/api/machines'

// Página CRUD da Biblioteca de Máquinas
export default function Machines() {
    const [machines, setMachines] = useState([])
    const [loading, setLoading] = useState(true)
    const [searchTerm, setSearchTerm] = useState('')
    const [showModal, setShowModal] = useState(false)
    const [editingMachine, setEditingMachine] = useState(null)
    const [showInactive, setShowInactive] = useState(false)
    const [modalErrors, setModalErrors] = useState({})
    
    // Tooltip state
    const [hoveredMachine, setHoveredMachine] = useState(null)
    const [mousePos, setMousePos] = useState({ x: 0, y: 0 })
    const [hoverTimeout, setHoverTimeout] = useState(null)

    const fetchMachines = async () => {
        try {
            const res = await axios.get(API_URL, {
                params: { includeInactive: showInactive },
                withCredentials: true
            })
            setMachines(res.data)
        } catch (error) {
            toast('Erro ao carregar máquinas.', 'error')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchMachines()
    }, [showInactive])

    const handleSave = async (data) => {
        setModalErrors({})
        try {
            if (editingMachine) {
                await axios.put(`${API_URL}/${editingMachine.id}`, data, { withCredentials: true })
                toast('Máquina atualizada com sucesso!', 'success')
            } else {
                await axios.post(API_URL, data, { withCredentials: true })
                toast('Máquina criada com sucesso!', 'success')
            }
            setShowModal(false)
            setEditingMachine(null)
            fetchMachines()
        } catch (error) {
            if (error.response && error.response.status === 400 && error.response.data.errors) {
                setModalErrors(error.response.data.errors)
                toast('Verifique os campos obrigatórios.', 'error')
            } else {
                toast('Erro ao salvar máquina.', 'error')
            }
        }
    }

    const handleDelete = async (id) => {
        if (!confirm('Desativar esta máquina?')) return
        try {
            await axios.delete(`${API_URL}/${id}`, { withCredentials: true })
            toast('Máquina desativada.', 'success')
            fetchMachines()
        } catch (error) {
            toast('Erro ao desativar máquina.', 'error')
        }
    }

    const handleReactivate = async (id) => {
        try {
            await axios.put(`${API_URL}/${id}/reactivate`, {}, { withCredentials: true })
            toast('Máquina reativada!', 'success')
            fetchMachines()
        } catch (error) {
            toast('Erro ao reativar máquina.', 'error')
        }
    }

    const openEdit = (machine) => {
        setEditingMachine(machine)
        setShowModal(true)
    }

    const openNew = () => {
        setEditingMachine(null)
        setShowModal(true)
    }

    const filtered = machines.filter(m => {
        const model = m.model?.toLowerCase() || ''
        const type = m.machineType?.toLowerCase() || ''
        const serial = m.serialNumber?.toLowerCase() || ''
        const search = searchTerm.toLowerCase()
        
        return model.includes(search) || type.includes(search) || serial.includes(search)
    })

    // Tooltip handlers
    const handleMouseEnter = (machine) => {
        const timeout = setTimeout(() => {
            setHoveredMachine(machine)
        }, 400) // 400ms delay as requested
        setHoverTimeout(timeout)
    }

    const handleMouseLeave = () => {
        if (hoverTimeout) clearTimeout(hoverTimeout)
        setHoveredMachine(null)
        setHoverTimeout(null)
    }

    const handleMouseMove = (e) => {
        setMousePos({ x: e.clientX, y: e.clientY })
    }

    return (
        <div className="dashboard-layout">
            <Sidebar />
            <main className="dashboard-content">
                <header className="page-header">
                    <div>
                        <h1 className="page-title">Biblioteca de Máquinas</h1>
                        <p className="page-subtitle">Gerencie os modelos e especificações técnicas</p>
                    </div>
                    <button className="btn-primary" onClick={openNew}>
                        <Plus size={20} /> Nova Máquina
                    </button>
                </header>

                <div className="filters-bar">
                    <div className="search-group">
                        <Search className="search-icon" />
                        <input
                            type="text"
                            placeholder="Buscar por nome, modelo, tipo ou serial..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="search-input"
                        />
                    </div>
                    <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.85rem', cursor: 'pointer' }}>
                        <input
                            type="checkbox"
                            checked={showInactive}
                            onChange={(e) => setShowInactive(e.target.checked)}
                        />
                        Mostrar inativas
                    </label>
                </div>

                {loading ? (
                    <div className="loading-container" style={{ textAlign: 'center', padding: '3rem' }}>
                        <Loader2 className="animate-spin" size={32} /> Carregando...
                    </div>
                ) : filtered.length === 0 ? (
                    <div className="empty-state">
                        <Cog size={48} style={{ color: 'var(--text-muted)' }} />
                        <p>Nenhuma máquina encontrada.</p>
                    </div>
                ) : (
                    <div className="table-container">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Tipo</th>
                                     <th>Modelo</th>
                                     <th>Núm. Série</th>
                                    <th>Instalação</th>
                                    <th>Status</th>
                                    <th className="text-right">Ações</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.map(m => (
                                    <tr 
                                        key={m.id}
                                        onMouseEnter={() => handleMouseEnter(m)}
                                        onMouseLeave={handleMouseLeave}
                                        onMouseMove={handleMouseMove}
                                    >
                                        <td className="font-bold">{typeLabels[m.machineType] || m.machineType}</td>
                                         <td>{m.model}</td>
                                         <td className="text-muted">{m.serialNumber}</td>
                                        <td className="font-bold">R$ {(m.installationPrice || 0).toFixed(2)}</td>
                                        <td>
                                            <span className={m.active ? 'badge-active' : 'badge-inactive'}>
                                                {m.active ? 'Ativa' : 'Inativa'}
                                            </span>
                                        </td>
                                        <td className="text-right">
                                            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                                                <button
                                                    className="btn-secondary"
                                                    style={{ padding: '0.4rem 0.6rem', fontSize: '0.8rem' }}
                                                    onClick={() => openEdit(m)}
                                                    title="Editar"
                                                >
                                                    <Edit2 size={14} />
                                                </button>
                                                {m.active ? (
                                                    <button
                                                        className="btn-secondary"
                                                        style={{ padding: '0.4rem 0.6rem', fontSize: '0.8rem', color: 'var(--danger-color)' }}
                                                        onClick={() => handleDelete(m.id)}
                                                        title="Desativar"
                                                    >
                                                        <Trash2 size={14} />
                                                    </button>
                                                ) : (
                                                    <button
                                                        className="btn-secondary"
                                                        style={{ padding: '0.4rem 0.6rem', fontSize: '0.8rem', color: 'var(--primary-color)' }}
                                                        onClick={() => handleReactivate(m.id)}
                                                        title="Reativar"
                                                    >
                                                        <RotateCcw size={14} />
                                                    </button>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}

                {showModal && (
                    <MachineModal
                        machine={editingMachine}
                        onClose={() => { setShowModal(false); setEditingMachine(null); setModalErrors({}) }}
                        onSave={handleSave}
                        errors={modalErrors}
                    />
                )}

                {hoveredMachine && (
                    <MachineTooltip machine={hoveredMachine} position={mousePos} />
                )}
            </main>
        </div>
    )
}
