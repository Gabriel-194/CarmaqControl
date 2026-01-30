import React, { useState } from 'react'
import { Plus, Trash2 } from 'lucide-react'
import '../Styles/ListaPecas.css'

export default function ListaPecas() {
    const [pecas, setPecas] = useState([
        { id: 1, nome: 'Capacitor 45uF', qtd: 1, valor: 45.00 },
        { id: 2, nome: 'Gás Refrigerante R410 (kg)', qtd: 0.5, valor: 75.00 },
    ])

    return (
        <div className="pecas-container">
            <div className="add-peca-form">
                <input type="text" placeholder="Buscar peça no estoque..." className="input-peca" />
                <input type="number" placeholder="Qtd" className="input-qtd" />
                <button className="btn-add">
                    <Plus size={20} />
                </button>
            </div>

            <ul className="lista-pecas">
                {pecas.map(peca => (
                    <li key={peca.id} className="peca-item">
                        <div className="peca-info">
                            <span className="peca-nome">{peca.nome}</span>
                            <span className="peca-valor">R$ {peca.valor.toFixed(2)} un.</span>
                        </div>
                        <div className="peca-actions">
                            <span className="badge-qtd">{peca.qtd}x</span>
                            <span className="peca-total">R$ {(peca.valor * peca.qtd).toFixed(2)}</span>
                            <button className="btn-remove">
                                <Trash2 size={16} />
                            </button>
                        </div>
                    </li>
                ))}
            </ul>
        </div>
    )
}