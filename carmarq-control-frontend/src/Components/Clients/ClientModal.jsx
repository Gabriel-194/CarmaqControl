import React, { useState, useEffect } from 'react';
import axios from 'axios';

const API_URL = 'http://localhost:8080/api/clients';

const ClientModal = ({ isOpen, onClose, client }) => {
    const [formData, setFormData] = useState({
        companyName: '',
        contactName: '',
        email: '',
        phone: '',
        cep: '',
        address: '',
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
                latitude: client.latitude || '',
                longitude: client.longitude || ''
            });
        }
    }, [client]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleCepChange = async (e) => {
        let cep = e.target.value.replace(/\D/g, '');
        if (cep.length > 8) cep = cep.slice(0, 8);
        
        let formattedCep = cep;
        if (cep.length >= 5) {
            formattedCep = cep.slice(0, 5) + '-' + cep.slice(5);
        }

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
                    <div className="modal-form-group">
                        <label>Nome da Empresa</label>
                        <input type="text" name="companyName" value={formData.companyName} onChange={handleChange} required className={`modal-input ${formErrors.companyName ? 'input-error' : ''}`} />
                        {formErrors.companyName && <span className="error-message">{formErrors.companyName}</span>}
                    </div>
                    <div className="modal-form-group">
                        <label>Contato (Nome)</label>
                        <input type="text" name="contactName" value={formData.contactName} onChange={handleChange} required className={`modal-input ${formErrors.contactName ? 'input-error' : ''}`} />
                        {formErrors.contactName && <span className="error-message">{formErrors.contactName}</span>}
                    </div>
                    <div className="modal-form-group">
                        <label>Email</label>
                        <input type="email" name="email" value={formData.email} onChange={handleChange} className="modal-input" />
                    </div>
                    <div className="modal-form-group">
                        <label>Telefone</label>
                        <input type="text" name="phone" value={formData.phone} onChange={handleChange} className="modal-input" />
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
