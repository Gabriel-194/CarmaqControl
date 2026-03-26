# Planejamento de Auditoria - Milestone v1.1

O objetivo desta fase é realizar uma revisão completa de todas as alterações feitas nas Fases 5 e 6, garantindo consistência financeira, segurança e UX aprimorada.

## Itens de Verificação

### 1. Consistência Financeira (Fase 6)
- [ ] Validar que o `technicianPayment` é sempre 10% de (Mão de Obra + Viagem).
- [ ] Validar que `totalValue` reflete a soma de todos os 5 campos (MoO, Viagem, Km, Peças, Despesas) menos descontos.
- [ ] Verificar se as queries de Dashboard no Repository estão batendo com os valores individuais.

### 2. Travas de Segurança (Fase 5)
- [ ] Confirmar que o status `PAGO` bloqueia edições em todos os sub-itens (Peças, Despesas, Fotos).
- [ ] Testar o limite de payload de 1MB com uma imagem grande.
- [ ] Verificar se o Rate Limiting está ativo.

### 3. Documentação e Relatórios
- [ ] Verificar se o `ReportService` (PDF/Excel) foi atualizado para mostrar a nova quebra financeira.
- [ ] Finalizar o `walkthrough.md` consolidado da V1.1.

## Plano de Execução
1. Executar testes de integração para as queries do Repository.
2. Realizar testes manuais de exportação de relatórios.
3. Gerar artefato de auditoria final.
4. Arquivar milestone v1.1.
