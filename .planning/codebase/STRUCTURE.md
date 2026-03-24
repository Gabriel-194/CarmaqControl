# Structure

## Directory Tree
```text
CarmaqControl/
├── .planning/               # GSD planning and codebase map
├── src/main/java/           # Backend (Spring Boot)
│   └── com/example/
│       ├── Config/          # Security, JWT, Filters, Error Handling
│       ├── Controller/      # REST API Endpoints
│       ├── DTOs/            # Request/Response Data Transfer Objects
│       ├── Domain/          # Enums and Value Objects
│       ├── Models/          # JPA Entities
│       ├── Repository/      # Spring Data JPA Repositories
│       └── Service/         # Business Logic
├── src/main/resources/      # Backend Resources (Props, Static, Templates)
├── carmarq-control-frontend/ # Frontend (React + Vite)
│   ├── src/
│   │   ├── Components/      # UI Components
│   │   ├── Pages/           # Route Pages
│   │   ├── Styles/          # CSS Styles
│   │   ├── contexts/        # React Contexts (Auth)
│   │   ├── services/        # API Clients (Axios)
│   │   └── utils/           # Helper Utilities
├── uploads/                 # Local storage for uploaded files
└── pom.xml                  # Maven Configuration
```

## Module Roles
- **Backend:** Provides a robust REST API for managing users, clients, machines, and service orders. Handles authentication, rate limiting, and PDF/Excel generation.
- **Frontend:** A modern SPA providing a user-friendly interface for different roles. Handles client-side routing and state management.
