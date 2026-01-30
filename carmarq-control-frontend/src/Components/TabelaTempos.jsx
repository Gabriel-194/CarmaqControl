import React, { useState } from 'react'
import { Play, Square, Plus } from 'lucide-react'
import '../Styles/TabelaTempos.css'

export default function TabelaTempos() {
    const [isTimerRunning, setIsTimerRunning] = useState(false)

    const registros = [
        { id: 1, inicio: '08:30', fim: '09:15', duracao: '00:45', descricao: 'Diagnóstico inicial' },
        { id: 2, inicio: '09:30', fim: '10:30', duracao: '01:00', descricao: 'Limpeza química' },
    ]

    return (
        <div className="times-container">
            <div className="timer-control">
                {!isTimerRunning ? (
                    <button
                        className="btn-timer btn-start"
                        onClick={() => setIsTimerRunning(true)}
                    >
                        <Play size={20} /> Iniciar Cronômetro
                    </button>
                ) : (
                    <button
                        className="btn-timer btn-stop"
                        onClick={() => setIsTimerRunning(false)}
                    >
                        <Square size={20} /> Parar Atividade
                    </button>
                )}

                <button className="btn-secondary btn-manual">
                    <Plus size={16} /> Lançamento Manual
                </button>
            </div>

            <table className="simple-table">
                <thead>
                <tr>
                    <th>Descrição</th>
                    <th>Início</th>
                    <th>Fim</th>
                    <th className="text-right">Duração</th>
                </tr>
                </thead>
                <tbody>
                {registros.map(reg => (
                    <tr key={reg.id}>
                        <td>{reg.descricao}</td>
                        <td>{reg.inicio}</td>
                        <td>{reg.fim}</td>
                        <td className="text-right font-mono">{reg.duracao}</td>
                    </tr>
                ))}
                <tr className="row-total">
                    <td colSpan={3}>Total Trabalhado</td>
                    <td className="text-right font-bold">01:45</td>
                </tr>
                </tbody>
            </table>
        </div>
    )
}