# Trampix — Plataforma de contratação de serviços

O Trampix conecta clientes a prestadores de serviços. O backend disponibiliza uma API REST para usuários, perfis de prestadores, categorias, subcategorias, serviços, disponibilidades e contratações. O frontend Angular permite pesquisar serviços, cadastrar clientes e prestadores, configurar disponibilidade, solicitar contratações e acompanhar os pedidos.

## Tecnologias

- Backend: Java 21, Spring Boot, Spring Data JPA, Hibernate, Bean Validation, Spring Security, MySQL e Maven.
- Frontend: Angular, TypeScript, Angular Material e RxJS.
- Testes manuais: Postman.

## Estrutura

```text
Projeto_Final_Trampix-main/
├── README.md
├── docs/
│   ├── documentacao-api.md
│   ├── modelo-dados.dot
│   └── modelo-dados.png
├── postman/
│   └── trampix.postman_collection.json
├── trampix-api/  # API Spring Boot
└── trampix/      # Aplicação Angular
```

## Integrantes

> Preencher com os nomes completos e IDs acadêmicos de todos os integrantes antes da entrega.

| Nome completo | ID acadêmico |
|---|---|
| PREENCHER | PREENCHER |

## Como executar o backend

1. Instale Java 21 e MySQL.
2. Crie um banco chamado `trampix` ou permita a criação automática pela URL JDBC.
3. Na pasta `trampix-api`, configure `DB_USERNAME` e `DB_PASSWORD` conforme seu MySQL.
4. Execute `./mvnw spring-boot:run` (Linux/macOS) ou `mvnw.cmd spring-boot:run` (Windows).
5. A API ficará disponível em `http://localhost:8080`.

O Hibernate está configurado com `ddl-auto=update` para desenvolvimento. Para publicação, use migrações versionadas e credenciais seguras.

## Usuários HTTP Basic para avaliação

- ADMIN: `admin@trampix.local` / `AdminTrampix123!`
- USUARIO: `usuario@trampix.local` / `UsuarioTrampix123!`

As credenciais são de demonstração local. Podem ser alteradas com `TRAMPIX_BASIC_ADMIN_USUARIO`, `TRAMPIX_BASIC_ADMIN_SENHA`, `TRAMPIX_BASIC_USUARIO` e `TRAMPIX_BASIC_USUARIO_SENHA`.

O login JWT original permanece disponível para o frontend. HTTP Basic foi adicionado para a demonstração acadêmica de autenticação/autorização.

## Como executar o frontend

1. Instale Node.js 22.22.3 ou superior (requisito do Angular 22 usado neste projeto).
2. Entre na pasta `trampix`.
3. Execute `npm ci`.
4. Confira `src/environments/environment.ts` e ajuste `apiUrl` se a API estiver em outro endereço.
5. Execute `npm start` e acesse `http://localhost:4200`.

## Arquitetura em camadas

Os controllers delegam operações às classes de `services/`. As regras de negócio de cadastro de usuários, categorias, serviços, disponibilidade e transições/conflitos de contratação foram organizadas nessa camada. Os repositórios concentram o acesso Spring Data JPA.

## Documentação e testes

- Modelo de dados: `docs/modelo-dados.png` (fonte editável em `docs/modelo-dados.dot`).
- Endpoints, autenticação e respostas: `docs/documentacao-api.md`.
- Coleção Postman: `postman/trampix.postman_collection.json`.

Importe a coleção no Postman, confira as variáveis e execute os cenários com a API e o MySQL ativos. As respostas de exemplo incluídas na coleção são modelos ilustrativos; substitua/salve as respostas reais da sua execução pelo recurso Save Response antes da submissão final.
