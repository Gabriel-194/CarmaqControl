import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { Loader2 } from 'lucide-react';

const PrivateRoute = ({ children, roles = [] }) => {
    const { user, loading } = useAuth();

    // 1. Enquanto o AuthContext verifica o cookie no backend, mostra Loading
    if (loading) {
        return (
            <div style={{height: '100vh', display: 'flex', alignItems:'center', justifyContent:'center'}}>
                <Loader2 className="animate-spin" size={40} color="#10b981"/>
            </div>
        );
    }

    // 2. Se terminou de carregar e não tem usuário, não está logado -> Login
    if (!user) {
        return <Navigate to="/" replace />;
    }

    // 3. Validação de Cargo (Role)
    // Se a rota exige roles específicas e o usuário não tem a role certa -> Dashboard
    if (roles.length > 0 && !roles.includes(user.role)) {
        return <Navigate to="/dashboard" replace />;
    }

    // 4. Tudo certo, renderiza a página
    return children;
};

export default PrivateRoute;