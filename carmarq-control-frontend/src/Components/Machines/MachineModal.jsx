import React, { useState, useEffect } from 'react'
import { X, Save } from 'lucide-react'

// Modal para criar/editar máquina na Biblioteca de Máquinas
export default function MachineModal({ machine, onClose, onSave, errors = {} }) {
    const [formData, setFormData] = useState({
        machineType: '',
        model: '',
        brand: '',
        description: '',
        hourlyRate: '',
        estimatedHours: ''
    })

    useEffect(() => {
        if (machine) {
            setFormData({
                machineType: machine.machineType || '',
                model: machine.model || '',
                brand: machine.brand || '',
                description: machine.description || '',
                hourlyRate: machine.hourlyRate || '',
                estimatedHours: machine.estimatedHours || ''
            })
        }
    }, [machine])

    const handleChange = (e) => {
        const { name, value } = e.target
        setFormData(prev => ({ ...prev, [name]: value }))
    }

    const handleSubmit = (e) => {
        e.preventDefault()
        onSave({
            ...formData,
            hourlyRate: parseFloat(formData.hourlyRate),
            estimatedHours: parseFloat(formData.estimatedHours)
        })
    }

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>{machine ? 'Editar Máquina' : 'Nova Máquina'}</h2>
                    <button className="modal-close" onClick={onClose}>
                        <X size={20} />
                    </button>
                </div>

                <form className="modal-form" onSubmit={handleSubmit}>
                    <div className="form-row">
                        <div className="form-group">
                            <label>Tipo da Máquina *</label>
                            <input
                                type="text"
                                name="machineType"
                                className={`form-input ${errors.machineType ? 'input-error' : ''}`}
                                placeholder="Ex: Ar Condicionado, Compressor..."
                                value={formData.machineType}
                                onChange={handleChange}
                                required
                            />
                            {errors.machineType && <span className="error-message">{errors.machineType}</span>}
                        </div>
                        <div className="form-group">
                            <label>Modelo *</label>
                            <input
                                type="text"
                                name="model"
                                className={`form-input ${errors.model ? 'input-error' : ''}`}
                                placeholder="Ex: Split 24000 BTUs"
                                value={formData.model}
                                onChange={handleChange}
                                required
                            />
                            {errors.model && <span className="error-message">{errors.model}</span>}
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label>Marca</label>
                            <input
                                type="text"
                                name="brand"
                                className="form-input"
                                placeholder="Ex: Samsung, LG..."
                                value={formData.brand}
                                onChange={handleChange}
                            />
                        </div>
                        <div className="form-group">
                            <label>Descrição</label>
                            <input
                                type="text"
                                name="description"
                                className="form-input"
                                placeholder="Informações técnicas adicionais"
                                value={formData.description}
                                onChange={handleChange}
                            />
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label>Valor Hora Técnica (R$) *</label>
                            <input
                                type="number"
                                name="hourlyRate"
                                className="form-input"
                                step="0.01"
                                min="0"
                                placeholder="0.00"
                                value={formData.hourlyRate}
                                onChange={handleChange}
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label>Estimativa de Horas *</label>
                            <input
                                type="number"
                                name="estimatedHours"
                                className="form-input"
                                step="0.5"
                                min="0"
                                placeholder="0"
                                value={formData.estimatedHours}
                                onChange={handleChange}
                                required
                            />
                        </div>
                    </div>

                    <div className="modal-actions">
                        <button type="button" className="btn-cancel" onClick={onClose}>
                            Cancelar
                        </button>
                        <button type="submit" className="btn-save">
                            <Save size={18} /> {machine ? 'Salvar Alterações' : 'Criar Máquina'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    )
}
