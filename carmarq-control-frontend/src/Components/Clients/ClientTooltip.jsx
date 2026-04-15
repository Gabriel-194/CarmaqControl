import React from 'react';
import '../../Styles/MachineTooltip.css';

/**
 * ClientTooltip Component
 * Displays client details in a floating tooltip that follows the mouse.
 */
const ClientTooltip = ({ client, position }) => {
    if (!client) return null;

    // Viewport safety check
    const tooltipRef = React.useRef(null);
    const [adjustedPos, setAdjustedPos] = React.useState({ top: position.y, left: position.x });

    React.useEffect(() => {
        if (tooltipRef.current) {
            const { offsetWidth, offsetHeight } = tooltipRef.current;
            const padding = 20;
            let { x, y } = position;

            x += 15;
            y += 15;

            if (x + offsetWidth > window.innerWidth - padding) {
                x = position.x - offsetWidth - 15;
            }

            if (y + offsetHeight > window.innerHeight - padding) {
                y = position.y - offsetHeight - 15;
            }

            setAdjustedPos({ top: y, left: x });
        }
    }, [position, client]);

    return (
        <div 
            ref={tooltipRef}
            className="machine-tooltip"
            style={{ 
                top: `${adjustedPos.top}px`, 
                left: `${adjustedPos.left}px`,
                zIndex: 9999
            }}
        >
            <div className="tooltip-header">
                <h3 className="tooltip-title">{client.companyName}</h3>
                <div className="tooltip-subtitle">
                    CNPJ: {client.cnpj || 'Não cadastrado'} {client.ie && ` • IE: ${client.ie}`}
                </div>
            </div>

            <div className="tooltip-body">
                <div className="tooltip-tech-section">
                    <div className="tooltip-section-title">Informações de Contato</div>
                    <div className="tooltip-row">
                        <span className="tooltip-label">Responsável:</span>
                        <span className="tooltip-value">{client.contactName || 'N/A'}</span>
                    </div>
                    <div className="tooltip-row">
                        <span className="tooltip-label">Telefone:</span>
                        <span className="tooltip-value">{client.phone || 'N/A'}</span>
                    </div>
                    <div className="tooltip-row">
                        <span className="tooltip-label">E-mail:</span>
                        <span className="tooltip-value">{client.email || 'N/A'}</span>
                    </div>
                </div>

                <div className="tooltip-tech-section">
                    <div className="tooltip-section-title">Endereço</div>
                    <div className="tooltip-row">
                        <span className="tooltip-label">CEP:</span>
                        <span className="tooltip-value">{client.cep || 'N/A'}</span>
                    </div>
                    <div className="tooltip-row">
                        <span className="tooltip-label">Endereço Completo:</span>
                        <span className="tooltip-value" style={{ whiteSpace: 'normal', textAlign: 'right' }}>
                            {client.address || 'N/A'}
                        </span>
                    </div>
                </div>

                {client.latitude && client.longitude && (
                    <div className="tooltip-tech-section">
                        <div className="tooltip-section-title">Geolocalização (API)</div>
                        <div className="tooltip-row">
                            <span className="tooltip-label">Latitude:</span>
                            <span className="tooltip-value">{client.latitude}</span>
                        </div>
                        <div className="tooltip-row">
                            <span className="tooltip-label">Longitude:</span>
                            <span className="tooltip-value">{client.longitude}</span>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ClientTooltip;
