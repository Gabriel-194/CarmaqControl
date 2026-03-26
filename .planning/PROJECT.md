# Project: CarmarqControl

## Context
Carmarq is a technical service company that needs to automate its operational and financial processes. The system manages clients, machines (library), and service orders (OS).

## Vision
To provide a clean, professional, and automated platform for technical service management, with clear role-based access for Owners, Technicians, and Financial staff.

## Tech Stack
- **Frontend:** React + Vite
- **Backend:** Spring Boot (Java 17)
- **Database:** PostgreSQL
- **Security:** Spring Security + JWT
- **Reporting:** iText7, Apache POI

## Current State
Milestone **v1.0** shipped. The system handles the complete operational cycle: Client/Machine management, OS creation, Time/Expense tracking (with KM rules), and automated PDF/Excel exports. Role-based security prevents Technicians from viewing corporate financial totals.

## Next Milestone Goals (v1.2)
- **Edição de Despesas**: Permitir correções por Administradores/Financeiro mesmo após conclusão técnica.
- **Lógica de Desconto**: Interface para aplicação de descontos no fechamento financeiro (Aprovar Pagamento).
- **Layout PDF**: Refinar a estética e usabilidade do relatório de manutenção.
- **Performance**: Otimizar queries e escalabilidade do banco.

*(Archive of previous v1.0 goals moved to milestone artifacts).*
