import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { toast } from './ui/Toaster';
import { Trash2, Plus, Receipt, Loader2, DollarSign, PenTool, CheckCircle, Lock } from 'lucide-react';
import { expenseTypeLabels } from '../utils/statusUtils';
import '../Styles/ListaDespesas.css';

const API_URL = 'http://localhost:8080/api/service-orders';

export default function ListaDespesas({ serviceOrderId, orderStatus, userRole, serviceType, manutencaoOrigin, onUpdate }) {
    const [despesas, setDespesas] = useState([]);
    const [totalValue, setTotalValue] = useState(0);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    
    // Estados para edição
    const [isEditing, setIsEditing] = useState(false);
    const [editingId, setEditingId] = useState(null);

    const [formData, setFormData] = useState({
        expenseType: 'DESLOCAMENTO_KM',
        quantity: '',
        unitValue: '',
        value: '',
        description: ''
    });

    const isAdmin = userRole === 'PROPRIETARIO' || userRole === 'FINANCEIRO';
    const isLocked = orderStatus === 'PAGO' || orderStatus === 'CANCELADA';
    
    // Novo: Tanto Proprietário quanto Técnico podem editar se não estiver PAGO ou CANCELADA.
    const isEditable = !isLocked;

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

    const handleSave = async (e) => {
        if (e) e.preventDefault();
        if (isLocked) return;
        
        let val = parseFloat(formData.value);
        let qty = (formData.expenseType === 'DESLOCAMENTO_KM' || formData.expenseType === 'PEDAGIO' || formData.expenseType === 'HOSPEDAGEM' || formData.expenseType === 'ALIMENTACAO') 
            ? parseFloat(formData.quantity) 
            : null;

        if (isNaN(val) || val <= 0) {
            toast('Por favor, informe um valor válido.', 'error');
            return;
        }

        if (formData.expenseType === 'DESLOCAMENTO_KM' && (isNaN(qty) || qty <= 0)) {
            toast('Para deslocamento, informe a quantidade de KM.', 'error');
            return;
        }
        
        if (formData.expenseType === 'OUTRO' && (!formData.description || formData.description.trim() === '')) {
            toast('Descrição é obrigatória para despesas do tipo OUTRO.', 'error');
            return;
        }

        setSubmitting(true);
        try {
            const payload = {
                expenseType: formData.expenseType,
                quantity: qty,
                value: val,
                description: formData.description
            };

            if (isEditing) {
                await axios.put(`${API_URL}/${serviceOrderId}/expenses/${editingId}`, payload, { withCredentials: true });
                toast('Despesa atualizada com sucesso!', 'success');
            } else {
                await axios.post(`${API_URL}/${serviceOrderId}/expenses`, payload, { withCredentials: true });
                toast('Despesa adicionada com sucesso!', 'success');
            }
            
            resetForm();
            fetchDespesas();
            if (onUpdate) onUpdate();
        } catch (error) {
            toast(error.response?.data?.message || 'Erro ao salvar despesa.', 'error');
        } finally {
            setSubmitting(false);
        }
    };

    const resetForm = () => {
        setFormData({
            expenseType: 'DESLOCAMENTO_KM',
            quantity: '',
            unitValue: '',
            value: '',
            description: ''
        });
        setIsEditing(false);
        setEditingId(null);
    };

    const handleEditClick = (d) => {
        setFormData({
            expenseType: d.expenseType,
            quantity: d.quantity || '',
            unitValue: (d.quantity && d.quantity > 0) ? (d.value / d.quantity).toFixed(2) : d.value,
            value: d.value,
            description: d.description || ''
        });
        setEditingId(d.id);
        setIsEditing(true);
        window.scrollTo({ top: 300, behavior: 'smooth' });
    };

    const handleDelete = async (expenseId) => {
        if (isLocked) return;
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
            {isEditable && !isLocked ? (
                <div className="despesa-form-card" style={{ border: isEditing ? '1px solid var(--primary-color)' : 'none', backgroundColor: isEditing ? 'var(--primary-light)' : 'var(--card-bg)' }}>
                    <h4 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', color: isEditing ? 'var(--primary-color)' : 'inherit' }}>
                        {isEditing ? <PenTool size={16} /> : <Plus size={16} />} 
                        {isEditing ? 'Editando Despesa' : 'Lançar Nova Despesa'}
                    </h4>
                    <form onSubmit={handleSave} className="despesa-form">
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

                            {/* Campo de Quantidade Dinâmico */}
                            {(formData.expenseType === 'DESLOCAMENTO_KM' || formData.expenseType === 'PEDAGIO' || formData.expenseType === 'HOSPEDAGEM' || formData.expenseType === 'ALIMENTACAO') && (
                                <div className="form-group" style={{ width: '120px' }}>
                                    <label>
                                        {formData.expenseType === 'DESLOCAMENTO_KM' ? 'Qtd. KM' : 
                                         formData.expenseType === 'PEDAGIO' ? 'Qtd. Pedágios' : 'Dias'}
                                    </label>
                                    <input 
                                        type="number" 
                                        step={formData.expenseType === 'DESLOCAMENTO_KM' ? "0.1" : "1"}
                                        className="form-input" 
                                        placeholder={formData.expenseType === 'DESLOCAMENTO_KM' ? "Ex: 50.5" : "Ex: 5"}
                                        value={formData.quantity}
                                        onChange={(e) => {
                                            const qty = e.target.value;
                                            if (formData.expenseType === 'DESLOCAMENTO_KM') {
                                                const rate = serviceType === 'INSTALACAO' || (serviceType === 'MANUTENCAO' && manutencaoOrigin === 'VALENTIM') ? 2.20 : 2.50;
                                                setFormData({
                                                    ...formData, 
                                                    quantity: qty,
                                                    value: qty ? (parseFloat(qty) * rate).toFixed(2) : ''
                                                });
                                            } else {
                                                const uv = parseFloat(formData.unitValue) || 0;
                                                setFormData({ 
                                                    ...formData, 
                                                    quantity: qty,
                                                    value: (qty && uv) ? (parseFloat(qty) * uv).toFixed(2) : formData.value
                                                });
                                            }
                                        }}
                                    />
                                    {formData.expenseType === 'DESLOCAMENTO_KM' && (
                                        <span style={{ fontSize: '0.65rem', color: 'var(--primary-color)' }}>
                                            Taxa: R$ {(serviceType === 'INSTALACAO' || (serviceType === 'MANUTENCAO' && manutencaoOrigin === 'VALENTIM')) ? '2,20' : '2,50'}/KM
                                        </span>
                                    )}
                                </div>
                            )}

                            <div className="form-group" style={{ width: '150px' }}>
                                <label>
                                    {(formData.expenseType === 'HOSPEDAGEM' || formData.expenseType === 'ALIMENTACAO' || formData.expenseType === 'PEDAGIO') ? 'Valor Unit./Diária (R$)' : 'Valor Bruto (R$)'}
                                </label>
                                <input 
                                    type="number" 
                                    step="0.01"
                                    disabled={formData.expenseType === 'DESLOCAMENTO_KM'}
                                    className="form-input" 
                                    style={formData.expenseType === 'DESLOCAMENTO_KM' ? { backgroundColor: 'var(--primary-light)', color: 'var(--primary-color)', fontWeight: 'bold' } : {}}
                                    placeholder="0.00"
                                    value={formData.expenseType === 'DESLOCAMENTO_KM' ? formData.value : formData.unitValue}
                                    onChange={(e) => {
                                        const uv = e.target.value;
                                        if (formData.expenseType !== 'DESLOCAMENTO_KM') {
                                            const qty = parseFloat(formData.quantity) || 1;
                                            setFormData({
                                                ...formData, 
                                                unitValue: uv,
                                                value: uv ? (parseFloat(uv) * qty).toFixed(2) : ''
                                            });
                                        }
                                    }}
                                    required
                                />
                            </div>
                            
                            {(formData.expenseType === 'HOSPEDAGEM' || formData.expenseType === 'ALIMENTACAO' || formData.expenseType === 'PEDAGIO') && (
                                <div className="form-group" style={{ width: '150px' }}>
                                    <label>Total Calculado</label>
                                    <input 
                                        type="number" 
                                        className="form-input" 
                                        style={{ backgroundColor: 'var(--border-color)', cursor: 'not-allowed', fontWeight: 'bold' }}
                                        value={formData.value}
                                        disabled
                                    />
                                </div>
                            )}
                        </div>

                        <div className="form-group">
                            <label>
                                {formData.expenseType === 'OUTRO' ? 'Descrição (Obrigatória)' : 'Descrição Opcional'}
                            </label>
                            <input 
                                type="text" 
                                className="form-input" 
                                style={formData.expenseType === 'OUTRO' ? { border: '1px solid var(--primary-color)' } : {}}
                                placeholder={formData.expenseType === 'OUTRO' ? "Descreva qual é a despesa..." : "Detalhes adicionais do gasto..."}
                                value={formData.description}
                                onChange={(e) => setFormData({...formData, description: e.target.value})}
                                required={formData.expenseType === 'OUTRO'}
                            />
                        </div>

                        <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem' }}>
                            <button 
                                type="submit" 
                                className="btn-primary" 
                                disabled={submitting}
                                style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}
                            >
                                {submitting ? <Loader2 className="animate-spin" size={16} /> : (isEditing ? <CheckCircle size={16} /> : <Plus size={16} />)}
                                {isEditing ? 'Atualizar Despesa' : 'Adicionar Despesa'}
                            </button>
                            {isEditing && (
                                <button type="button" className="btn-secondary" onClick={resetForm}>
                                    Cancelar
                                </button>
                            )}
                        </div>
                    </form>
                </div>
            ) : (
                <div className="status-warning" style={{ 
                    display: 'flex', 
                    alignItems: 'center', 
                    justifyContent: 'center',
                    gap: '0.5rem', 
                    padding: '1rem',
                    backgroundColor: isLocked ? '#fee2e2' : '#fef3c7', 
                    color: isLocked ? '#991b1b' : '#92400e', 
                    borderRadius: '6px', 
                    marginBottom: '1rem', 
                    fontSize: '0.9rem',
                    fontWeight: '500'
                }}>
                    <Lock size={18} />
                    <span>{isLocked ? `OS ${orderStatus} - Edição bloqueada` : 'Edição permitida'}</span>
                </div>
            )}

            <div className="despesas-list">
                <div className="despesas-header-list" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>
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
                            <li key={d.id} className="item-li" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem', border: '1px solid var(--border-color)', borderRadius: '0.5rem', marginBottom: '0.75rem', backgroundColor: 'var(--card-bg)' }}>
                                <div className="item-info">
                                    <div className="item-title" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                                        <strong style={{ fontSize: '0.95rem' }}>{d.expenseTypeLabel}</strong>
                                        {d.quantity && (
                                            <span style={{ fontSize: '0.75rem', backgroundColor: 'var(--border-color)', padding: '0.1rem 0.4rem', borderRadius: '0.25rem' }}>
                                                {d.quantity} {
                                                    d.expenseType === 'DESLOCAMENTO_KM' ? 'km' : 
                                                    (d.expenseType === 'HOSPEDAGEM' || d.expenseType === 'ALIMENTACAO') ? 'dias' : 'un'
                                                }
                                            </span>
                                        )}
                                    </div>
                                    {d.description && <p style={{ fontSize: '0.85rem', margin: 0, color: 'var(--text-muted)' }}>{d.description}</p>}
                                </div>
                                <div className="item-actions" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                                    <span style={{ fontWeight: 600, color: 'var(--text-color)' }}>R$ {d.value.toFixed(2)}</span>
                                    {isEditable && !isLocked && (
                                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                                            <button 
                                                onClick={() => handleEditClick(d)}
                                                title="Editar Despesa"
                                                style={{ background: 'none', border: 'none', color: 'var(--primary-color)', cursor: 'pointer', display: 'flex', alignItems: 'center', padding: '0.25rem' }}
                                            >
                                                <PenTool size={18} />
                                            </button>
                                            <button 
                                                onClick={() => handleDelete(d.id)}
                                                title="Remover Despesa"
                                                style={{ background: 'none', border: 'none', color: 'var(--danger-color)', cursor: 'pointer', display: 'flex', alignItems: 'center', padding: '0.25rem' }}
                                            >
                                                <Trash2 size={18} />
                                            </button>
                                        </div>
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
