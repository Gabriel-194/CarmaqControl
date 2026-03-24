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

## Next Milestone Goals
- Integrate with an official Maps API for precise distance calculation (currently based on manual input + coordinates).
- Implement a more advanced inventory management for parts.
- Add technician performance dashboards.

*(Archive of previous v1.0 goals moved to milestone artifacts).*
