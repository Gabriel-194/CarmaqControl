import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { maskPhone, maskCNPJ, maskCEP } from '../../utils/masks';

const API_URL = 'http://localhost:8080/api/clients';

const ClientModal = ({ isOpen, onClose, client }) => {
    const [formData, setFormData] = useState({
        companyName: '',
        contactName: '',
        email: '',
        phone: '',
        cep: '',
        address: '',
        cnpj: '',
        ie: '',
        latitude: '',
        longitude: ''
    });
    const [loading, setLoading] = useState(false);
    const [formErrors, setFormErrors] = useState({});

    useEffect(() => {
        if (client) {
            setFormData({
                companyName: client.companyName || '',
                contactName: client.contactName || '',
                email: client.email || '',
                phone: client.phone || '',
                cep: client.cep || '',
                address: client.address || '',
                cnpj: client.cnpj || '',
                ie: client.ie || '',
                latitude: client.latitude || '',
                longitude: client.longitude || ''
            });
        }
    }, [client]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    // Handler com máscara de telefone — suporta fixo e celular
    const handlePhoneChange = (e) => {
        const masked = maskPhone(e.target.value)
        setFormData(prev => ({ ...prev, phone: masked }))
    }

    // Handler com máscara de CNPJ
    const handleCnpjChange = (e) => {
        const masked = maskCNPJ(e.target.value)
        setFormData(prev => ({ ...prev, cnpj: masked }))
    }

    const handleCnpjSearch = async () => {
        const cnpj = formData.cnpj.replace(/\D/g, '');
        if (cnpj.length !== 14) {
            alert("CNPJ deve conter 14 dígitos.");
            return;
        }

        setLoading(true);
        try {
            // Usando CNPJ.ws que costuma retornar Inscrições Estaduais mais facilmente
            const res = await axios.get(`https://publica.cnpj.ws/cnpj/${cnpj}`);
            const data = res.data;
            
            // Pega a primeira IE disponível se existir
            let ieEncontrada = '';
            if (data.estabelecimento && data.estabelecimento.inscricoes_estaduais && data.estabelecimento.inscricoes_estaduais.length > 0) {
                ieEncontrada = data.estabelecimento.inscricoes_estaduais[0].inscricao_estadual;
            }

            setFormData(prev => ({
                ...prev,
                companyName: data.razao_social || prev.companyName,
                cnpj: maskCNPJ(data.estabelecimento.cnpj || prev.cnpj),
                ie: ieEncontrada || prev.ie,
                cep: maskCEP(data.estabelecimento.cep || prev.cep),
                address: `${data.estabelecimento.tipo_logradouro} ${data.estabelecimento.logradouro}, ${data.estabelecimento.numero}${data.estabelecimento.complemento ? ' - ' + data.estabelecimento.complemento : ''}, ${data.estabelecimento.bairro}, ${data.estabelecimento.cidade.nome} - ${data.estabelecimento.estado.sigla}`,
            }));

            // Tenta buscar as coordenadas pelo CEP se disponível
            if (data.estabelecimento.cep) {
                handleCepChange({ target: { value: data.estabelecimento.cep } });
            }
        } catch (error) {
            console.error("Erro ao buscar CNPJ (publica.cnpj.ws)", error);
            // Se falhar o CNPJ.ws, tentamos a BrasilAPI como fallback (mas sem a IE garantida)
            try {
                const res = await axios.get(`https://brasilapi.com.br/api/cnpj/v1/${cnpj}`);
                const data = res.data;
                setFormData(prev => ({
                    ...prev,
                    companyName: data.razao_social || data.nome_fantasia || prev.companyName,
                    cep: data.cep || prev.cep,
                    address: `${data.logradouro}, ${data.numero}${data.complemento ? ' - ' + data.complemento : ''}, ${data.bairro}, ${data.municipio} - ${data.uf}`,
                }));
            } catch (fallbackError) {
                alert("Erro ao buscar dados do CNPJ. Verifique se o número está correto.");
            }
        } finally {
            setLoading(false);
        }
    };

    const handleCepChange = async (e) => {
        const formattedCep = maskCEP(e.target.value)
        const cep = formattedCep.replace(/\D/g, '')

        setFormData(prev => ({ ...prev, cep: formattedCep }));

        if (cep.length === 8) {
            try {
                const viaCepResponse = await axios.get(`https://viacep.com.br/ws/${cep}/json/`);
                if (!viaCepResponse.data.erro) {
                    const data = viaCepResponse.data;
                    const fullAddress = `${data.logradouro}, ${data.bairro}, ${data.localidade} - ${data.uf}`;
                    setFormData(prev => ({ ...prev, address: fullAddress }));

                    try {
                        const osmResponse = await axios.get(`https://nominatim.openstreetmap.org/search`, {
                            params: { format: 'json', q: fullAddress, limit: 1 }
                        });
                        
                        if (osmResponse.data && osmResponse.data.length > 0) {
                            setFormData(prev => ({
                                ...prev,
                                latitude: parseFloat(osmResponse.data[0].lat),
                                longitude: parseFloat(osmResponse.data[0].lon)
                            }));
                        }
                    } catch (osmError) {
                        console.error("Erro Nominatim", osmError);
                    }
                }
            } catch (error) {
                console.error("Erro ViaCEP", error);
            }
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setFormErrors({});
        try {
            if (client && client.id) {
                await axios.put(`${API_URL}/${client.id}`, formData, { withCredentials: true });
            } else {
                await axios.post(API_URL, formData, { withCredentials: true });
            }
            onClose();
        } catch (error) {
            if (error.response && error.response.status === 400 && error.response.data.errors) {
                setFormErrors(error.response.data.errors);
            } else if (error.response && error.response.status === 409) {
                setFormErrors({ companyName: error.response.data.message });
            } else {
                console.error("Erro ao salvar cliente", error);
                alert("Erro ao salvar o cliente.");
            }
        } finally {
            setLoading(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <h2 className="modal-title">{client ? 'Editar Cliente' : 'Novo Cliente'}</h2>
                <form onSubmit={handleSubmit}>
                    <div className="modal-form-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                        <div className="modal-form-group">
                            <label>CNPJ</label>
                            <div style={{ display: 'flex', gap: '0.5rem' }}>
                                <input 
                                    type="text" 
                                    name="cnpj" 
                                    value={formData.cnpj} 
                                    onChange={handleCnpjChange} 
                                    placeholder="00.000.000/0000-00" 
                                    className="modal-input"
                                    maxLength={18}
                                />
                                <button 
                                    type="button" 
                                    onClick={handleCnpjSearch} 
                                    className="btn-secondary"
                                    style={{ padding: '0.5rem', minWidth: '80px' }}
                                    disabled={loading}
                                >
                                    {loading ? '...' : 'Buscar'}
                                </button>
                            </div>
                        </div>
                        <div className="modal-form-group">
                            <label>Inscrição Estadual (IE)</label>
                            <input type="text" name="ie" value={formData.ie} onChange={handleChange} className="modal-input" placeholder="Isento ou Número" />
                        </div>
                    </div>
                    <div className="modal-form-group">
                        <label>Nome da Empresa (Razão Social) *</label>
                        <input type="text" name="companyName" value={formData.companyName} onChange={handleChange} required className={`modal-input ${formErrors.companyName ? 'input-error' : ''}`} />
                        {formErrors.companyName && <span className="error-message">{formErrors.companyName}</span>}
                    </div>
                    <div className="modal-form-group">
                        <label>Contato (Nome) *</label>
                        <input type="text" name="contactName" value={formData.contactName} onChange={handleChange} required className={`modal-input ${formErrors.contactName ? 'input-error' : ''}`} />
                        {formErrors.contactName && <span className="error-message">{formErrors.contactName}</span>}
                    </div>
                    <div className="modal-form-group">
                        <label>Email</label>
                        <input type="email" name="email" value={formData.email} onChange={handleChange} className="modal-input" />
                    </div>
                    <div className="modal-form-group">
                        <label>Telefone</label>
                        <input
                            type="text"
                            name="phone"
                            value={formData.phone}
                            onChange={handlePhoneChange}
                            className="modal-input"
                            placeholder="(00) 00000-0000"
                            maxLength={15}
                        />
                    </div>
                    <div className="modal-form-group">
                        <label>CEP</label>
                        <input type="text" name="cep" value={formData.cep} onChange={handleCepChange} className="modal-input" placeholder="00000-000" />
                    </div>
                    <div className="modal-form-group">
                        <label>Endereço Completo</label>
                        <textarea name="address" value={formData.address} onChange={handleChange} className="modal-input" rows="3"></textarea>
                    </div>
                    
                    <div className="modal-actions">
                        <button type="button" onClick={onClose} disabled={loading} className="btn-secondary">
                            Cancelar
                        </button>
                        <button type="submit" disabled={loading} className="btn-primary">
                            {loading ? 'Salvando...' : 'Salvar'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ClientModal;
