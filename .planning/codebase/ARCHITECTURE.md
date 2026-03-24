# Architecture

## Overview
The system follows a classic client-server architecture with a clear separation between the frontend (React) and backend (Spring Boot).

## Backend Layers
- **Controller:** REST endpoints handling HTTP requests and mapping to DTOs.
- **Service:** Business logic and orchestration.
- **Repository:** Data access layer using Spring Data JPA.
- **Models:** Entity definitions representing the database schema.
- **DTOs:** Data Transfer Objects for decoupled API communication.
- **Config:** Security, JWT, filter, and error handling configurations.

## Frontend Architecture
- **Pages:** Top-level components representing routes.
- **Components:** Reusable UI elements.
- **Services:** API call wrappers using Axios.
- **Contexts:** Global state management (e.g., Auth).
- **Utils:** Helper functions (e.g., status formatting).

## Communication
- RESTful API over HTTPS (in production).
- JWT-based authentication for all protected endpoints.
