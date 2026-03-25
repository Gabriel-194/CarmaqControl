# Integrations

## Database
- **PostgreSQL:** Primary data store via JPA/Hibernate.
- **In-memory/Test:** `spring-boot-starter-data-jpa-test` used for testing.

## External APIs
- **ViaCEP:** Usado no frontend para busca de endereço por CEP.
- **OpenStreetMap (Nominatim):** Usado no frontend para obtenção de coordenadas (latitude/longitude) a partir do endereço.
- **CNPJ.ws / BrasilAPI:** Usado no frontend para preenchimento automático de dados da empresa a partir do CNPJ.

## Authentication
- **JWT:** Stateless authentication using JSON Web Tokens.
- **Spring Security:** Role-based access control (Admin, Technician, Financial).

## File System
- **Local Uploads:** Directory `uploads/` in the root for machine photos and other documents.
