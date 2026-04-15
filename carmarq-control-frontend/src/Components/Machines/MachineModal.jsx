import React, { useState, useEffect } from 'react'
import { X, Save } from 'lucide-react'

import { typeLabels, fieldLabels, fieldsByType } from '../../Constants/MachineConstants'

const fieldPlaceholders = {
    laserSize: 'Ex: 3000x1500mm',
    laserPower: 'Ex: 3000',
    machineSize: 'Ex: 3 metros',
    tonnage: 'Ex: 100',
    command: 'Ex: Delem DA-53T',
    force: 'Ex: 50 kN',
    diameter: 'Ex: 60',
    rollerCount: 'Ex: 3',
}

export default function MachineModal({ machine, onClose, onSave, errors = {} }) {
    const [formData, setFormData] = useState({
        machineType: '',
        model: '',
        serialNumber: '',
        installationPrice: '',
        description: '',
        laserSize: '',
        laserKind: '',
        laserPower: '',
        machineSize: '',
        tonnage: '',
        command: '',
        force: '',
        diameter: '',
        rollerCount: ''
    })

    useEffect(() => {
        if (machine) {
            setFormData({
                machineType: machine.machineType || '',
                model: machine.model || '',
                serialNumber: machine.serialNumber || '',
                installationPrice: machine.installationPrice || '',
                description: machine.description || '',
                laserSize: machine.laserSize || '',
                laserKind: machine.laserKind || '',
                laserPower: machine.laserPower || '',
                machineSize: machine.machineSize || '',
                tonnage: machine.tonnage || '',
                command: machine.command || '',
                force: machine.force || '',
                diameter: machine.diameter || '',
                rollerCount: machine.rollerCount || ''
            })
        }
    }, [machine])

    const handleChange = (e) => {
        const { name, value } = e.target
        setFormData(prev => ({ ...prev, [name]: value }))
    }

    const handleSubmit = (e) => {
        e.preventDefault()
        
        // 1. Iniciar os dados com os campos comuns obrigatórios
        const cleanedData = {
            machineType: formData.machineType,
            model: formData.model,
            serialNumber: formData.serialNumber,
            installationPrice: formData.installationPrice === '' ? null : Number(formData.installationPrice),
            description: formData.description
        }
        
        // 2. Adicionar apenas os campos técnicos permitidos para esse tipo
        const allowedFields = fieldsByType[formData.machineType] || []
        
        allowedFields.forEach(field => {
            const value = formData[field]
            
            // Campos numéricos técnicos
            const numericFields = ['laserPower', 'tonnage', 'force', 'diameter', 'rollerCount']
            
            if (numericFields.includes(field)) {
                cleanedData[field] = (value === '' || value === null || value === undefined) 
                    ? null 
                    : Number(value)
            } else {
                cleanedData[field] = value
            }
        })

        onSave(cleanedData)
    }

    const selectedTypeFields = fieldsByType[formData.machineType] || []

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
                            <select
                                name="machineType"
                                className={`form-input ${errors.machineType ? 'input-error' : ''}`}
                                value={formData.machineType}
                                onChange={handleChange}
                                required
                            >
                                <option value="">Selecione...</option>
                                {Object.keys(typeLabels).map(type => (
                                    <option key={type} value={type}>{typeLabels[type]}</option>
                                ))}
                            </select>
                            {errors.machineType && <span className="error-message">{errors.machineType}</span>}
                        </div>
                    </div>


                    <div className="form-row">
                        <div className="form-group">
                            <label>Modelo *</label>
                            <input
                                type="text"
                                name="model"
                                className={`form-input ${errors.model ? 'input-error' : ''}`}
                                placeholder="Ex: TruLaser 3030"
                                value={formData.model}
                                onChange={handleChange}
                                required
                            />
                            {errors.model && <span className="error-message">{errors.model}</span>}
                        </div>
                        <div className="form-group">
                             <label>Número de Série *</label>
                             <input
                                 type="text"
                                 name="serialNumber"
                                 className={`form-input ${errors.serialNumber ? 'input-error' : ''}`}
                                 placeholder="S/N"
                                 value={formData.serialNumber}
                                 onChange={handleChange}
                                 required
                             />
                             {errors.serialNumber && <span className="error-message">{errors.serialNumber}</span>}
                         </div>
                     </div>
 
                     <div className="form-row">
                         <div className="form-group">
                             <label>Preço de Instalação (R$) *</label>
                             <input
                                 type="number"
                                 name="installationPrice"
                                 className={`form-input ${errors.installationPrice ? 'input-error' : ''}`}
                                 placeholder="0.00"
                                 step="0.01"
                                 value={formData.installationPrice}
                                 onChange={handleChange}
                                 required
                             />
                             {errors.installationPrice && <span className="error-message">{errors.installationPrice}</span>}
                         </div>
                         <div className="form-group" style={{ flex: 1 }}></div>
                     </div>

                    {selectedTypeFields.length > 0 && (
                        <div className="form-section-title">Especificações Técnicas</div>
                    )}

                    <div className="form-grid-2">
                        {selectedTypeFields.map(field => (
                            <div className="form-group" key={field}>
                                <label>{fieldLabels[field]}</label>
                                {field === 'laserKind' ? (
                                    <select
                                        name={field}
                                        className="form-input"
                                        value={formData[field]}
                                        onChange={handleChange}
                                    >
                                        <option value="">Selecione...</option>
                                        <option value="ABERTA">Aberta</option>
                                        <option value="FECHADA">Fechada</option>
                                    </select>
                                ) : (
                                    <input
                                        type={field === 'laserPower' || field === 'tonnage' || field === 'force' || field === 'diameter' || field === 'rollerCount' ? 'number' : 'text'}
                                        name={field}
                                        className="form-input"
                                        placeholder={fieldPlaceholders[field] || ''}
                                        step="0.01"
                                        value={formData[field]}
                                        onChange={handleChange}
                                    />
                                )}
                            </div>
                        ))}
                    </div>

                    <div className="form-group" style={{ marginTop: '1rem' }}>
                        <label>Descrição</label>
                        <textarea
                            name="description"
                            className="form-input"
                            rows="2"
                            placeholder="Informações adicionais..."
                            value={formData.description}
                            onChange={handleChange}
                        ></textarea>
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
