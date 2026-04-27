import React, { useState, useEffect } from 'react';
import Sidebar from '../Components/Sidebar';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';
import ClientModal from '../Components/Clients/ClientModal';
import ClientTooltip from '../Components/Clients/ClientTooltip';
import '../Styles/dashboard.css';
import '../Styles/Clients.css'; // Novo import

const API_URL = 'http://localhost:8080/api/clients';

const Clients = () => {
    const { user } = useAuth();
    const [clients, setClients] = useState([]);
    const [loading, setLoading] = useState(true);
    const [modalOpen, setModalOpen] = useState(false);
    const [editingClient, setEditingClient] = useState(null);
    const [showInactive, setShowInactive] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');

    // Tooltip state
    const [hoveredClient, setHoveredClient] = useState(null);
    const [mousePos, setMousePos] = useState({ x: 0, y: 0 });
    const [hoverTimeout, setHoverTimeout] = useState(null);

    const isOwner = user?.role === 'PROPRIETARIO';
    const canAdd = user?.role === 'PROPRIETARIO' || user?.role === 'TECNICO';

    useEffect(() => {
        loadClients();
    }, [showInactive]);

    const loadClients = async () => {
        setLoading(true);
        try {
            const response = await axios.get(`${API_URL}?includeInactive=${showInactive}`, { withCredentials: true });
            
            // Verifica se a estrutura é Array, senão usa [], evitando fatal loop de erro React.
            if (Array.isArray(response.data)) {
                setClients(response.data);
            } else {
                setClients([]);
            }
        } catch (error) {
            console.error("Erro ao carregar clientes", error);
        } finally {
            setLoading(false);
        }
    };

    const handleOpenModal = (client = null) => {
        setEditingClient(client);
        setModalOpen(true);
    };

    const handleCloseModal = () => {
        setEditingClient(null);
        setModalOpen(false);
        loadClients();
    };

    const handleDelete = async (id) => {
        if (!window.confirm("Deseja realmente excluir este cliente?")) return;
        try {
            await axios.delete(`${API_URL}/${id}`, { withCredentials: true });
            loadClients();
        } catch (error) {
            console.error("Erro ao deletar cliente", error);
            alert("Erro ao remover o cliente.");
        }
    };

    const handleReactivate = async (id) => {
        if (!window.confirm("Deseja reativar este cliente?")) return;
        try {
            await axios.put(`${API_URL}/${id}/reactivate`, {}, { withCredentials: true });
            loadClients();
        } catch (error) {
            console.error("Erro ao reativar cliente", error);
            alert("Erro ao reativar o cliente.");
        }
    };

    const filteredClients = clients.filter(client => 
        client.companyName?.toLowerCase().includes(searchTerm.toLowerCase()) || 
        client.contactName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        client.cnpj?.includes(searchTerm)
    );

    // Tooltip handlers
    const handleMouseEnter = (client) => {
        const timeout = setTimeout(() => {
            setHoveredClient(client);
        }, 400);
        setHoverTimeout(timeout);
    };

    const handleMouseLeave = () => {
        if (hoverTimeout) clearTimeout(hoverTimeout);
        setHoveredClient(null);
        setHoverTimeout(null);
    };

    const handleMouseMove = (e) => {
        setMousePos({ x: e.clientX, y: e.clientY });
    };

    return (
        <div className="dashboard-layout">
            <Sidebar />
            <main className="dashboard-content">
                <div className="clients-page-header" style={{ width: '100%', gap: '1rem', display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between' }}>
                    <h1 className="clients-page-title">Clientes</h1>
                    <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap', flex: '1 1 auto' }}>
                        <input 
                            type="text" 
                            placeholder="Buscar razao social, contato ou CNPJ..." 
                            className="form-input search-input"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            style={{ margin: 0, padding: '0.5rem', flex: '1 1 200px', borderRadius: '4px', border: '1px solid #ccc' }}
                        />
                        <label style={{ display: 'flex', alignItems: 'center', gap: '5px', fontSize: '14px', color: '#666', cursor: 'pointer' }}>
                            <input type="checkbox" checked={showInactive} onChange={(e) => setShowInactive(e.target.checked)} />
                            Ocultos/Inativos
                        </label>
                        {canAdd && (
                            <button onClick={() => handleOpenModal()} className="btn-primary">
                                Novo Cliente
                            </button>
                        )}
                    </div>
                </div>

                <div className="clients-container">
                    {loading ? (
                        <div className="clients-empty-state">Carregando dados dos clientes...</div>
                    ) : clients.length === 0 ? (
                        <div className="clients-empty-state">Nenhum cliente cadastrado no momento.</div>
                    ) : (
                        <div className="responsive-table-wrapper">
                            <table className="clients-table">
                                <thead>
                                    <tr>
                                        <th>Empresa</th>
                                        <th>Contato</th>
                                        <th>Contatos Fixos</th>
                                        <th>Endereço e CEP</th>
                                        <th>Status</th>
                                        {canAdd && (
                                            <th style={{ textAlign: 'right' }}>Ações</th>
                                        )}
                                    </tr>
                                </thead>
                                <tbody>
                                    {filteredClients.map(client => (
                                        <tr 
                                            key={client.id} 
                                            style={{ opacity: client.active ? 1 : 0.6 }}
                                            onMouseEnter={() => handleMouseEnter(client)}
                                            onMouseLeave={handleMouseLeave}
                                            onMouseMove={handleMouseMove}
                                        >
                                            <td className="td-company">{client.companyName}</td>
                                            <td className="td-contact">{client.contactName}</td>
                                            <td>
                                                <div>{client.email}</div>
                                                <div style={{ color: 'var(--text-muted)' }}>{client.phone}</div>
                                            </td>
                                            <td>
                                                <div style={{ fontWeight: 500 }}>{client.cep}</div>
                                                <div style={{ color: 'var(--text-muted)' }}>{client.address}</div>
                                            </td>
                                            <td>
                                                <span style={{color: client.active ? 'var(--primary-color, #10b981)' : '#ef4444', fontWeight: 600}}>
                                                    {client.active ? 'Ativo' : 'Inativo'}
                                                </span>
                                            </td>
                                            {canAdd && (
                                                <td>
                                                    <div className="action-buttons">
                                                        {client.active ? (
                                                            <>
                                                                <button onClick={() => handleOpenModal(client)} className="btn-edit">Editar</button>
                                                                {isOwner && <button onClick={() => handleDelete(client.id)} className="btn-delete">Excluir</button>}
                                                            </>
                                                        ) : (
                                                            isOwner && <button onClick={() => handleReactivate(client.id)} className="btn-edit" style={{ color: 'var(--primary-color, #10b981)' }}>Reativar</button>
                                                        )}
                                                    </div>
                                                </td>
                                            )}
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>

                {modalOpen && <ClientModal isOpen={modalOpen} onClose={handleCloseModal} client={editingClient} />}

                {hoveredClient && (
                    <ClientTooltip client={hoveredClient} position={mousePos} />
                )}
            </main>
        </div>
    );
};

export default Clients;
