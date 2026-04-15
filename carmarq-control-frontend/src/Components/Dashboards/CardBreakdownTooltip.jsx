import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import '../../Styles/MachineTooltip.css';

const API_URL = 'http://localhost:8080/api/dashboard/card-breakdown';

/**
 * CardBreakdownTooltip Component
 * Mostra o detalhamento financeiro por OS ao passar o mouse sobre um card do dashboard.
 * Busca os dados sob demanda via API.
 */
const CardBreakdownTooltip = ({ cardType, month, year, children }) => {
    const [breakdown, setBreakdown] = useState(null);
    const [loading, setLoading] = useState(false);
    const [visible, setVisible] = useState(false);
    const [mousePos, setMousePos] = useState({ x: 0, y: 0 });
    const timeoutRef = useRef(null);
    const tooltipRef = useRef(null);
    const [adjustedPos, setAdjustedPos] = useState({ top: 0, left: 0 });

    const fetchBreakdown = async () => {
        try {
            setLoading(true);
            let url = `${API_URL}?card=${cardType}`;
            if (year) url += `&year=${year}`;
            if (month) url += `&month=${month}`;
            const res = await axios.get(url, { withCredentials: true });
            setBreakdown(res.data);
        } catch (err) {
            console.error('Erro ao carregar breakdown:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleMouseEnter = () => {
        timeoutRef.current = setTimeout(() => {
            setVisible(true);
            fetchBreakdown();
        }, 500);
    };

    const handleMouseLeave = () => {
        if (timeoutRef.current) clearTimeout(timeoutRef.current);
        setVisible(false);
        setBreakdown(null);
    };

    const handleMouseMove = (e) => {
        setMousePos({ x: e.clientX, y: e.clientY });
    };

    // Ajuste de posição para não sair da tela
    useEffect(() => {
        if (tooltipRef.current && visible) {
            const { offsetWidth, offsetHeight } = tooltipRef.current;
            const padding = 20;
            let x = mousePos.x + 15;
            let y = mousePos.y + 15;

            if (x + offsetWidth > window.innerWidth - padding) {
                x = mousePos.x - offsetWidth - 15;
            }
            if (y + offsetHeight > window.innerHeight - padding) {
                y = mousePos.y - offsetHeight - 15;
            }

            setAdjustedPos({ top: y, left: x });
        }
    }, [mousePos, visible, breakdown]);

    const formatCurrency = (val) => {
        return (val || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    };

    return (
        <div
            onMouseEnter={handleMouseEnter}
            onMouseLeave={handleMouseLeave}
            onMouseMove={handleMouseMove}
            style={{ cursor: 'pointer' }}
        >
            {children}

            {visible && (
                <div
                    ref={tooltipRef}
                    className="machine-tooltip"
                    style={{
                        top: `${adjustedPos.top}px`,
                        left: `${adjustedPos.left}px`,
                        minWidth: '400px',
                        maxWidth: '500px',
                        maxHeight: '400px',
                        overflowY: 'auto',
                        zIndex: 9999
                    }}
                >
                    {loading ? (
                        <div style={{ padding: '1rem', textAlign: 'center', color: '#999', fontSize: '0.8rem' }}>
                            Carregando detalhamento...
                        </div>
                    ) : !breakdown || !breakdown.orders || breakdown.orders.length === 0 ? (
                        <div style={{ padding: '1rem', textAlign: 'center', color: '#999', fontSize: '0.8rem' }}>
                            Nenhuma OS encontrada para este período.
                        </div>
                    ) : (
                        <>
                            <div className="tooltip-header">
                                <h3 className="tooltip-title">Detalhamento por OS</h3>
                                <div className="tooltip-subtitle">
                                    {breakdown.orders.length} ordem(ns) • Total: {formatCurrency(breakdown.total)}
                                </div>
                            </div>
                            <div className="tooltip-body" style={{ padding: 0 }}>
                                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.75rem' }}>
                                    <thead>
                                        <tr style={{ borderBottom: '1px solid rgba(0,0,0,0.08)', background: 'rgba(0,0,0,0.02)' }}>
                                            <th style={{ padding: '0.4rem 0.6rem', textAlign: 'left', fontWeight: 600, color: '#555' }}>OS</th>
                                            <th style={{ padding: '0.4rem 0.6rem', textAlign: 'left', fontWeight: 600, color: '#555' }}>Cliente</th>
                                            <th style={{ padding: '0.4rem 0.6rem', textAlign: 'right', fontWeight: 600, color: '#555' }}>Valor</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {breakdown.orders.map((order, idx) => (
                                            <tr
                                                key={order.id || idx}
                                                style={{
                                                    borderBottom: '1px solid rgba(0,0,0,0.04)',
                                                    background: idx % 2 === 0 ? 'transparent' : 'rgba(0,0,0,0.015)'
                                                }}
                                            >
                                                <td style={{ padding: '0.35rem 0.6rem', fontWeight: 600, color: 'var(--primary-color, #10b981)', whiteSpace: 'nowrap' }}>
                                                    {order.osCode}
                                                </td>
                                                <td style={{ padding: '0.35rem 0.6rem', color: '#333', maxWidth: '160px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                                    {order.clientName}
                                                </td>
                                                <td style={{
                                                    padding: '0.35rem 0.6rem',
                                                    textAlign: 'right',
                                                    fontWeight: 600,
                                                    whiteSpace: 'nowrap',
                                                    color: order.value >= 0 ? '#059669' : '#ef4444'
                                                }}>
                                                    {formatCurrency(order.value)}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </>
                    )}
                </div>
            )}
        </div>
    );
};

export default CardBreakdownTooltip;
