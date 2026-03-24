import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { toast } from './ui/Toaster';
import { Trash2, Plus, Receipt, Loader2, DollarSign } from 'lucide-react';
import { expenseTypeLabels } from '../utils/statusUtils';
import '../Styles/ListaDespesas.css';

const API_URL = 'http://localhost:8080/api/service-orders';

export default function ListaDespesas({ serviceOrderId, orderStatus, onUpdate }) {
    const [despesas, setDespesas] = useState([]);
    const [totalValue, setTotalValue] = useState(0);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    
    const [formData, setFormData] = useState({
        expenseType: 'DESLOCAMENTO_KM',
        quantityKm: '',
        value: '',
        description: ''
    });

    const isEditable = orderStatus === 'EM_ANDAMENTO';

    const fetchDespesas = async () => {
        try {
            setLoading(true);
            const res = await axios.get(`${API_URL}/${serviceOrderId}/expenses`, { withCredentials: true });
            setDespesas(res.data.expenses || []);
            setTotalValue(res.data.totalValue || 0);
        } catch (error) {
            console.error('Erro ao buscar despesas', error);
            toast('Erro ao buscar despesas.', 'error');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (serviceOrderId) {
            fetchDespesas();
        }
    }, [serviceOrderId]);

    const handleAddExpense = async (e) => {
        e.preventDefault();
        
        let val = parseFloat(formData.value);
        let qty = formData.expenseType === 'DESLOCAMENTO_KM' ? parseFloat(formData.quantityKm) : null;

        if (isNaN(val) || val <= 0) {
            toast('Por favor, informe um valor válido.', 'error');
            return;
        }

        if (formData.expenseType === 'DESLOCAMENTO_KM' && (isNaN(qty) || qty <= 0)) {
            toast('Para deslocamento, informe a quantidade de KM.', 'error');
            return;
        }

        setSubmitting(true);
        try {
            await axios.post(`${API_URL}/${serviceOrderId}/expenses`, {
                expenseType: formData.expenseType,
                quantityKm: qty,
                value: val,
                description: formData.description
            }, { withCredentials: true });
            
            toast('Despesa adicionada com sucesso!', 'success');
            setFormData({
                expenseType: 'DESLOCAMENTO_KM',
                quantityKm: '',
                value: '',
                description: ''
            });
            fetchDespesas();
            if (onUpdate) onUpdate();
        } catch (error) {
            toast(error.response?.data?.message || 'Erro ao adicionar despesa.', 'error');
        } finally {
            setSubmitting(false);
        }
    };

    const handleDelete = async (expenseId) => {
        if (!window.confirm('Deseja realmente excluir esta despesa?')) return;
        
        try {
            await axios.delete(`${API_URL}/${serviceOrderId}/expenses/${expenseId}`, { withCredentials: true });
            toast('Despesa removida.', 'success');
            fetchDespesas();
            if (onUpdate) onUpdate();
        } catch (error) {
            toast('Erro ao remover despesa.', 'error');
        }
    };

    if (loading) {
        return <div className="loading-spinner"><Loader2 className="animate-spin" size={24} /> Carregando despesas...</div>;
    }

    return (
        <div className="lista-despesas-container">
            {isEditable && (
                <div className="despesa-form-card">
                    <h4 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                        <Plus size={16} /> Lançar Nova Despesa
                    </h4>
                    <form onSubmit={handleAddExpense} className="despesa-form">
                        <div className="despesa-row">
                            <div className="form-group" style={{ flex: 1 }}>
                                <label>Tipo de Despesa</label>
                                <select 
                                    className="form-input"
                                    value={formData.expenseType}
                                    onChange={(e) => setFormData({...formData, expenseType: e.target.value})}
                                >
                                    {Object.entries(expenseTypeLabels).map(([key, label]) => (
                                        <option key={key} value={key}>{label}</option>
                                    ))}
                                </select>
                            </div>

                            {formData.expenseType === 'DESLOCAMENTO_KM' && (
                                <div className="form-group" style={{ width: '120px' }}>
                                    <label>Qtd. KM</label>
                                    <input 
                                        type="number" 
                                        step="0.1"
                                        className="form-input" 
                                        placeholder="Ex: 50.5"
                                        value={formData.quantityKm}
                                        onChange={(e) => setFormData({
                                            ...formData, 
                                            quantityKm: e.target.value,
                                            value: e.target.value ? (parseFloat(e.target.value) * 2.20).toFixed(2) : ''
                                        })}
                                    />
                                    <span style={{ fontSize: '0.65rem', color: 'var(--primary-color)' }}>Taxa: R$ 2,20/KM</span>
                                </div>
                            )}

                            <div className="form-group" style={{ width: '150px' }}>
                                <label>Valor (R$)</label>
                                <input 
                                    type="number" 
                                    step="0.01"
                                    disabled={formData.expenseType === 'DESLOCAMENTO_KM'}
                                    className="form-input" 
                                    style={formData.expenseType === 'DESLOCAMENTO_KM' ? { backgroundColor: '#f0fdf4', color: 'var(--primary-color)', fontWeight: 'bold' } : {}}
                                    placeholder="0.00"
                                    value={formData.value}
                                    onChange={(e) => setFormData({...formData, value: e.target.value})}
                                    required
                                />
                            </div>
                        </div>

                        <div className="form-group">
                            <label>Descrição Opcional</label>
                            <input 
                                type="text" 
                                className="form-input" 
                                placeholder="Detalhes adicionais do gasto..."
                                value={formData.description}
                                onChange={(e) => setFormData({...formData, description: e.target.value})}
                            />
                        </div>

                        <button 
                            type="submit" 
                            className="btn-primary" 
                            disabled={submitting}
                            style={{ alignSelf: 'flex-start', marginTop: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}
                        >
                            {submitting ? <Loader2 className="animate-spin" size={16} /> : <Plus size={16} />}
                            Adicionar Despesa
                        </button>
                    </form>
                </div>
            )}

            <div className="despesas-list">
                <div className="despesas-header-list" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', borderBottom: '1px solid #edf2f7', paddingBottom: '0.5rem' }}>
                    <h4 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', margin: 0 }}>
                        <Receipt size={18} /> Histórico de Despesas
                    </h4>
                    <span style={{ backgroundColor: 'var(--primary-light)', color: 'var(--primary-color)', padding: '0.25rem 0.75rem', borderRadius: '1rem', fontWeight: 600, fontSize: '0.9rem' }}>
                        Total: R$ {totalValue.toFixed(2)}
                    </span>
                </div>
                
                {despesas.length === 0 ? (
                    <div className="empty-despesas" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                        <DollarSign size={32} style={{ opacity: 0.5, marginBottom: '0.5rem' }} />
                        <p>Nenhuma despesa lançada nesta OS.</p>
                    </div>
                ) : (
                    <ul className="items-ul" style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                        {despesas.map(d => (
                            <li key={d.id} className="item-li" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem', border: '1px solid #edf2f7', borderRadius: '0.5rem', marginBottom: '0.75rem', backgroundColor: '#fff' }}>
                                <div className="item-info">
                                    <div className="item-title" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                                        <strong style={{ fontSize: '0.95rem' }}>{d.expenseTypeLabel}</strong>
                                        {d.quantityKm && (
                                            <span style={{ fontSize: '0.75rem', backgroundColor: '#e2e8f0', padding: '0.1rem 0.4rem', borderRadius: '0.25rem' }}>
                                                {d.quantityKm} km
                                            </span>
                                        )}
                                    </div>
                                    {d.description && <p style={{ fontSize: '0.85rem', margin: 0, color: 'var(--text-muted)' }}>{d.description}</p>}
                                </div>
                                <div className="item-actions" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                                    <span style={{ fontWeight: 600, color: 'var(--text-color)' }}>R$ {d.value.toFixed(2)}</span>
                                    {isEditable && (
                                        <button 
                                            onClick={() => handleDelete(d.id)}
                                            title="Remover Despesa"
                                            style={{ background: 'none', border: 'none', color: 'var(--danger-color)', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '0.25rem' }}
                                        >
                                            <Trash2 size={18} />
                                        </button>
                                    )}
                                </div>
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </div>
    );
}
