# Alterações realizadas e conferências antes da entrega

## Alterações implementadas nesta revisão

### Backend

- Criada a camada `services/` e os serviços `AuthService`, `UsuarioService`, `CategoriaService`, `SubcategoriaService`, `PrestadorService`, `ServicoService`, `DisponibilidadeService`, `ContratacaoService` e `DashboardService`.
- Os controllers REST foram ajustados para delegar as operações aos serviços, sem injetar repositórios diretamente.
- Criada a exceção personalizada `RegraNegocioException`, tratada centralmente como HTTP 409.
- Regras de duplicidade, disponibilidade e conflito de agenda foram organizadas na camada de serviço.
- Adicionado suporte HTTP Basic sem retirar o login JWT já utilizado pelo Angular.
- Configurados dois usuários em memória para demonstração: ADMIN e USUARIO.
- Adicionado `GET /dashboard` com cinco indicadores calculados por contagens no banco.
- Adicionada operação `DELETE /contratacoes/{id}` como cancelamento lógico, preservando o histórico.
- Criado teste unitário de `DashboardService`.
- Removidas credenciais do MySQL do `application.properties`; a configuração usa variáveis de ambiente. O Dev Container define credenciais locais de desenvolvimento.

### Frontend

- Criado dashboard com cinco KPIs consumindo `GET /dashboard`.
- Criada tela de gestão de categorias e subcategorias, com cadastro, edição e inativação.
- Criada tela `Meus serviços`, com cadastro, edição, listagem e inativação de serviços do prestador.
- Criada tela `Seja um prestador`, para cadastrar o perfil profissional e o primeiro serviço pela interface.
- Criada tela `Minha disponibilidade`, para configurar os horários de atendimento necessários ao fluxo de contratação.
- Criada tela `Minha conta`, com edição e desativação da conta.
- Adicionadas rotas protegidas e links de navegação para as novas telas.
- Mantida a arquitetura baseada em NgModules, `standalone: false` e Angular Material.

### Documentação e avaliação

- Criado README geral na raiz.
- Criada documentação de endpoints e autenticação.
- Criado diagrama lógico em PNG e fonte DOT editável.
- Criada coleção Postman com variáveis, cenários e exemplos de resposta.

## Passos obrigatórios antes de enviar ao professor

1. Preencher no README o nome completo e ID acadêmico de cada integrante.
2. Configurar `DB_USERNAME` e `DB_PASSWORD` de acordo com o MySQL do ambiente.
3. Executar backend e frontend com versões compatíveis (Java 21 e Node.js 22.22.3+ para Angular 22).
4. Executar `./mvnw test` no backend e `npm ci` + `npm run build` no frontend.
5. Corrigir qualquer incompatibilidade que apareça no ambiente real. A compilação integrada não pôde ser confirmada nesta revisão porque o ambiente de execução não conseguiu baixar as dependências Maven/NPM.
6. Testar as rotas com MySQL ativo e executar os fluxos com contas reais do banco, inclusive cadastro de prestador, disponibilidade e contratação.
7. Importar a coleção Postman, revisar os IDs e credenciais, executar cada requisição e salvar as respostas reais com **Save Response**. Os exemplos que acompanham a coleção são referências ilustrativas, não resultados de execução.
8. Confirmar que a variável `jwtToken` contém um token obtido pelo login de um usuário do banco e `jwtTokenPrestador` contém um token de um prestador do banco.
9. Confirmar que o usuário ADMIN de domínio (armazenado no banco, para o login JWT do frontend) existe se a tela de gestão de categorias for apresentada pela interface. Os usuários HTTP Basic em memória são destinados à demonstração da segurança no Postman.
10. Atualizar `apiUrl` em `trampix/src/environments/environment.ts` se a API for publicada em outro endereço.

## Limitações conscientes do domínio

- `ContratacaoHistorico` é uma entidade de auditoria imutável; não possui edição nem exclusão por desenho do domínio. O histórico deve ser preservado.
- Exclusões de usuários, categorias e serviços são lógicas/inativações para preservar referências e registros históricos.
- Contratações são canceladas por transição de estado; não são apagadas fisicamente.
- A tela de gestão de categorias é exibida para perfil ADMIN do login JWT. O usuário HTTP Basic administrativo é separado do cadastro de usuários no banco.
