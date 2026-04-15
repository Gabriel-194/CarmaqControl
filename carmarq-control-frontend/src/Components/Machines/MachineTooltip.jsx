import React from 'react';
import { typeLabels, fieldLabels, fieldsByType } from '../../Constants/MachineConstants';
import '../../Styles/MachineTooltip.css';

/**
 * MachineTooltip Component
 * Displays machine details in a floating tooltip that follows the mouse.
 */
const MachineTooltip = ({ machine, position }) => {
    if (!machine) return null;

    const selectedTypeFields = fieldsByType[machine.machineType] || [];

    const formatValue = (key, value) => {
        if (value === null || value === undefined || value === '') return 'N/A';
        
        if (key === 'installationPrice') {
            return `R$ ${parseFloat(value).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`;
        }
        
        if (key === 'laserPower') return `${value} W`;
        if (key === 'tonnage') return `${value} Ton`;
        if (key === 'diameter') return `${value} mm`;
        
        return value;
    };

    // Viewport safety check
    const tooltipRef = React.useRef(null);
    const [adjustedPos, setAdjustedPos] = React.useState({ top: position.y, left: position.x });

    React.useEffect(() => {
        if (tooltipRef.current) {
            const { offsetWidth, offsetHeight } = tooltipRef.current;
            const padding = 20;
            let { x, y } = position;

            // Offset from cursor
            x += 15;
            y += 15;

            // Prevent overflow right
            if (x + offsetWidth > window.innerWidth - padding) {
                x = position.x - offsetWidth - 15;
            }

            // Prevent overflow bottom
            if (y + offsetHeight > window.innerHeight - padding) {
                y = position.y - offsetHeight - 15;
            }

            setAdjustedPos({ top: y, left: x });
        }
    }, [position, machine]);

    return (
        <div 
            ref={tooltipRef}
            className="machine-tooltip"
            style={{ 
                top: `${adjustedPos.top}px`, 
                left: `${adjustedPos.left}px` 
            }}
        >
            <div className="tooltip-header">
                <h3 className="tooltip-title">{machine.model}</h3>
                <div className="tooltip-subtitle">
                    {typeLabels[machine.machineType] || machine.machineType} • S/N: {machine.serialNumber}
                </div>
            </div>

            <div className="tooltip-body">
                <div className="tooltip-row">
                    <span className="tooltip-label">Instalação</span>
                    <span className="tooltip-value tooltip-price">{formatValue('installationPrice', machine.installationPrice)}</span>
                </div>

                {selectedTypeFields.length > 0 && (
                    <div className="tooltip-tech-section">
                        <div className="tooltip-section-title">Especificações Técnicas</div>
                        {selectedTypeFields.map(field => (
                            <div key={field} className="tooltip-row">
                                <span className="tooltip-label">{fieldLabels[field]}</span>
                                <span className="tooltip-value">{formatValue(field, machine[field])}</span>
                            </div>
                        ))}
                    </div>
                )}
                
                {machine.description && (
                    <div className="tooltip-tech-section">
                        <div className="tooltip-section-title">Observações</div>
                        <div className="tooltip-value" style={{ textAlign: 'left', fontSize: '0.75rem', opacity: 0.8 }}>
                            {machine.description}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default MachineTooltip;
