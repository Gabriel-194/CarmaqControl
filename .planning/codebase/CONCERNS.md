# Concerns

## Security
- **Hardcoded Secrets:** `application.properties` contains a fallback JWT secret. This should be strictly managed via environment variables in production.
- **CORS Configuration:** `SecurityConfig.java` has hardcoded local origins. This needs to be parameterized for different environments.
- **Error Exposure:** `GlobalExceptionHandler.java` returns `e.getMessage()` for general exceptions, which might leak internal system details.

## Reliability
- **Database Migrations:** Currently using `hibernate.ddl-auto=update`. For a growing system, a migration tool like Flyway or Liquibase is recommended.
- **Rate Limiting:** `Bucket4j` is implemented, but its effectiveness depends on the deployment architecture (e.g., sticky sessions vs. distributed cache).

## Maintainability
- **Frontend Testing:** Absence of automated tests for the React frontend increases the risk of regressions during UI changes.
- **Documentation:** Many files lack Javadoc or detailed comments, though the code structure is relatively standard.

## Scalability
- **Local Storage:** `uploads/` is currently local. If the system scales to multiple instances, a shared storage solution (S3, Cloud Storage) will be needed.
