# Testing

## Backend Testing
- **Unit Tests:** Located in `src/test/java`.
- **Persistence Tests:** Use `@DataJpaTest` with an in-memory database or a test PostgreSQL instance.
- **Controller Tests:** Use `@WebMvcTest` with `MockMvc`.
- **Security Tests:** `spring-boot-starter-security-test` is included.

## Frontend Testing
- **Status:** No explicit testing framework (like Jest or Vitest) is visible in `package.json`.
- **Manual Verification:** Currently relies on manual testing via Vite dev server.

## Continuous Integration
- No visible CI/CD pipeline configuration (e.g., GitHub Actions) in the root.
