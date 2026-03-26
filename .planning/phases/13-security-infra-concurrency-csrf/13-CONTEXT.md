# Phase 13: Hardening: Infraestrutura, Concorrência e CSRF - Context

**Gathered:** 2026-03-26
**Status:** Ready for planning

<domain>
## Phase Boundary

Esta fase resolve as vulnerabilidades de infraestrutura e lógica concorrente identificadas no relatório de auditoria (Vulnerabilidades 4, 5 e 6):
1. Condições de corrida no cálculo de valores de OS.
2. Bypass de Rate Limit via IP Spoofing (X-Forwarded-For).
3. Vulnerabilidade a CSRF devido ao uso de Autenticação baseada em Cookies.

</domain>

<decisions>
## Implementation Decisions

### D-01: Proteção contra Condição de Corrida (Optimistic Locking)
- Adicionar um campo `@Version private Long version` na entidade `ServiceOrder`.
- Isso garantirá que qualquer tentativa de salvar uma OS com dados estritos (como totais recalculados) falhará se outro processo tiver alterado a OS entre a leitura e a escrita.

### D-02: Hardening do RateLimitingFilter (Anti-Spoofing)
- Modificar a lógica de obtenção de IP para ignorar o `X-Forwarded-For` se ele não for proveniente de um proxy conhecido (para o ambiente local, usaremos `getRemoteAddr` como fallback seguro).
- Implementar uma validação básica para garantir que o IP extraído seja um formato válido.

### D-03: Ativação de Proteção CSRF
- Habilitar CSRF no `SecurityConfig`.
- Configurar o `CookieCsrfTokenRepository.withHttpOnlyFalse()` para que o frontend/Vite possa ler o token e enviá-lo no header `X-XSRF-TOKEN`.
- Manter o `CsrfCookieFilter` existente para garantir a persistência do cookie.

### the agent's Discretion
- A escolha de quais IPs considerar como "Trusted Proxies" pode ser parametrizada via `application.properties` ou deixada como uma lista vazia por padrão (forçando `getRemoteAddr` no ambiente de desenvolvimento).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Security
- `security_audit_report.md` — Relatório base com as vulnerabilidades detalhadas.
- `src/main/java/com/example/Models/ServiceOrder.java` — Alvo do Optimistic Locking.
- `src/main/java/com/example/Config/RateLimitingFilter.java` — Alvo do Anti-Spoofing.
- `src/main/java/com/example/Config/SecurityConfig.java` — Alvo da configuração de CSRF.

</canonical_refs>
