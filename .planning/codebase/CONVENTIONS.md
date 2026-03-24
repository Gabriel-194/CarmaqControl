# Conventions

## Backend (Java/Spring Boot)
- **Names:** PascalCase for classes, camelCase for methods/variables.
- **DTOs:** Always use DTOs for API input/output; avoid exposing entities directly.
- **Annotations:** Heavy use of Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`) to reduce boilerplate.
- **Persistence:** JPA/Hibernate for database operations.
- **Error Handling:** Centralized via `@RestControllerAdvice` in `GlobalExceptionHandler.java`.
- **Language:** Code comments and responses are typically in Portuguese (Brazil) as per user rules.

## Frontend (React)
- **Components:** Functional components with Hooks.
- **Styling:** Vanilla CSS (located in `Styles/` directory).
- **Icons:** Consistent use of `lucide-react`.
- **API:** Axios services located in `services/`.
- **State:** Context API for global state like Authentication.

## General
- **Language:** Portuguese (Brazil) for all user-facing strings and documentation.
