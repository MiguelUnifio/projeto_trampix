# Checklist final de entrega — Trampix

Status de inspeção: **22/11/2026**. Consulte `AUDITORIA-ENUNCIADOS.md` para detalhes e limitações da validação.

## Conferido por inspeção de arquivos

- [x] Backend e frontend existentes, em pastas separadas.
- [x] Arquitetura em camadas (entidades, repositories, services, recursos/controllers).
- [x] Oito entidades JPA e diagrama (`docs/modelo-dados.png` + `.dot`).
- [x] Regras de domínio e funcionalidade específica de contratação.
- [x] Validação e tratamento central de erros.
- [x] Spring Security: HTTP Basic e perfis de demonstração; JWT adicional para frontend.
- [x] Angular com NgModules, rotas, Angular Material e SSR/SSG não configurado.
- [x] Dashboard integrado ao serviço de consulta do backend.
- [x] Coleção Postman com 74 requests e scripts de teste.
- [x] README e documentação da API existentes.

## Pendências obrigatórias para o grupo

- [ ] Preencher nome completo e ID acadêmico de todos os integrantes no `README.md`.
- [ ] Instalar Node compatível com Angular 22 (mínimo 22.22.3; `.nvmrc` indica 22.22.3) e executar `npm ci`, `npm run build`, `npm test` em `trampix`.
- [ ] Executar `./mvnw test` e `./mvnw spring-boot:run` em `trampix-api` com MySQL disponível.
- [ ] Validar todos os fluxos pelo browser e confirmar integração frontend/API.
- [ ] Executar coleção Postman, revisar todos os status, salvar **respostas reais** usando Save Response e exportar JSON novamente (incluindo variáveis compartilhadas).
- [ ] Publicar backend + MySQL e frontend; configurar URL da API e CORS; preencher URLs no README.
- [ ] Conferir GitHub e entregar o link no Ambiente Conhecer.
- [ ] Todos os integrantes revisar as regras, arquitetura, autenticação Basic/JWT e fluxo de contratação para a apresentação.

**Não marque pendências de execução como concluídas apenas por inspeção.**
