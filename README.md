# Trampix — Plataforma de contratação de serviços (Projeto Integrador)

O **Trampix** conecta clientes a prestadores de serviços (pedreiro, eletricista, diarista etc.). O cliente pesquisa serviços, escolhe data e horário, e o prestador aceita, recusa ou envia uma contraproposta; o sistema controla disponibilidade, conflitos de agenda, preços e o histórico de cada contratação.

**Objetivo do backend:** API REST (Spring Boot) que gerencia usuários, prestadores, categorias, subcategorias, serviços, disponibilidades e contratações, com regras de negócio, validação, segurança e consultas personalizadas. **Objetivo do frontend:** aplicação Angular que executa o fluxo principal do início ao fim pela interface.

## Integrantes

> **PREENCHER antes da entrega** — nome completo e ID acadêmico de todos os integrantes.

| Nome completo | ID acadêmico |
|---|---|
| PREENCHER | PREENCHER |
| PREENCHER | PREENCHER |

## Tecnologias

- **Backend:** Java 21, Spring Boot, Spring Data JPA/Hibernate, Bean Validation, Spring Security (HTTP Basic; JWT apenas para o frontend), MySQL, Maven, Lombok, JUnit/Mockito.
- **Frontend:** Angular (NgModules, `standalone: false`, roteamento, sem SSR), Angular Material, TypeScript, RxJS.
- **Testes manuais:** Postman.

## Estrutura do repositório

```text
├── README.md
├── docs/
│   ├── documentacao-api.md      # endpoints, segurança, regras de negócio
│   ├── modelo-dados.png         # diagrama entidade-relacionamento
│   └── modelo-dados.dot         # fonte editável do diagrama
├── postman/
│   └── projeto-integrador.postman_collection.json
├── trampix-api/                 # backend Spring Boot (pom.xml)
└── trampix/                     # frontend Angular
```

## Como executar o backend

1. Instale Java 21 e MySQL 8 (ou use o Dev Container em `trampix-api/.devcontainer`).
2. O banco `trampix` é criado automaticamente. Defina `DB_USERNAME` e `DB_PASSWORD` (padrão: `root` e senha vazia).
3. Na pasta `trampix-api`: `./mvnw spring-boot:run` (Linux/macOS) ou `mvnw.cmd spring-boot:run` (Windows). Para rodar os testes: `./mvnw test`.
4. A API sobe em `http://localhost:8080`. Na primeira execução são criadas as categorias padrão e os usuários de teste.

Variáveis opcionais: `DB_URL`, `TRAMPIX_CORS_ORIGENS` (origens do frontend, separadas por vírgula; padrão `http://localhost:4200`), `TRAMPIX_JWT_SEGREDO`, `TRAMPIX_ADMIN_EMAIL`/`TRAMPIX_ADMIN_SENHA`.

### Usuários HTTP Basic (somente teste)

| Perfil | Usuário | Senha |
|---|---|---|
| ADMIN | `admin@trampix.local` | `AdminTrampix123!` |
| USUARIO | `usuario@trampix.local` | `UsuarioTrampix123!` |

Configurados em memória e espelhados no banco. Contas criadas por `POST /usuarios` também podem usar Basic. `401` = sem credenciais/credenciais inválidas; `403` = autenticado sem permissão.

## Como executar o frontend

1. Node.js 22.22+ (requisito do Angular 22).
2. Na pasta `trampix`: `npm ci` e `npm start` → `http://localhost:4200` (usa `http://localhost:8080` como API).

## Publicação (versão publicada do sistema)

- **Frontend:** `npm run build` gera `trampix/dist/trampix/browser`. Antes, troque `apiUrl` em `trampix/src/environments/environment.ts` pela URL pública da API. O projeto já traz `public/_redirects` (Netlify) e `vercel.json` para as rotas do Angular funcionarem ao recarregar a página. Publique essa pasta em Netlify, Vercel, GitHub Pages ou similar.
- **Backend:** publique `trampix-api` em um serviço Java (Render, Railway, Azure...) com um MySQL gerenciado, definindo `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `TRAMPIX_JWT_SEGREDO` e `TRAMPIX_CORS_ORIGENS` com a URL do frontend publicado.
- Registre abaixo as URLs finais:
  - Frontend: _PREENCHER_
  - API: _PREENCHER_

## Auditoria dos enunciados

Consulte `docs/AUDITORIA-ENUNCIADOS.md` para a checklist requisito por requisito dos Trabalhos 1 e 2, com os itens inspecionados, pendências de entrega e limitações de build/teste deste ambiente.

## Modelo de dados

Diagrama em `docs/modelo-dados.png`: **8 entidades** — `Usuario`, `PrestadorPerfil`, `Categoria`, `Subcategoria`, `Servico`, `Disponibilidade`, `Contratacao` e `ContratacaoHistorico` — com relacionamentos 1:N e 1:0..1 mapeados com JPA/MySQL.

## Arquitetura em camadas

- **entidades** (JPA) → **repositorios** (Spring Data JPA) → **services** (regras de negócio e transações) → **recursos** (controllers REST e DTOs).
- Os controllers **não acessam repositories**: só chamam os services. Toda regra de negócio (validações de domínio, conflito de agenda, transições de status, cálculo de preço) está em `services/`.
- Erros tratados em `TratadorErros` (`@RestControllerAdvice`) com a exceção personalizada `RegraNegocioException` (409).

## CRUD por entidade

| Entidade | Criar | Consultar | Atualizar | Excluir |
|---|---|---|---|---|
| Usuario | `POST /usuarios` | `GET /usuarios`, `/{id}` | `PUT /usuarios/{id}` | `DELETE` (desativa) |
| PrestadorPerfil | `POST /usuarios/{id}/prestador` | `GET` | `PUT` | `DELETE` (encerra) |
| Categoria | `POST /categorias` | `GET` lista/id | `PUT` | `DELETE` (inativa) |
| Subcategoria | `POST .../subcategorias` | `GET` lista/id | `PUT` | `DELETE` (inativa) |
| Servico | `POST /servicos` | `GET` lista/id/meus | `PUT` | `DELETE` (inativa) |
| Disponibilidade | `POST` (um) / `PUT` (todos) | `GET` lista/id | `PUT .../{id}` | `DELETE .../{id}` |
| Contratacao | `POST /contratacoes` | `GET` lista/id | `PUT` (pedido PENDENTE) | `DELETE` (cancela) |
| ContratacaoHistorico | gerado pelo sistema | `GET .../historico` | — | — |

`ContratacaoHistorico` é trilha de auditoria imutável: por regra de domínio só é criada pelo sistema e consultada. Detalhes em `docs/documentacao-api.md`.

## Testes com Postman

1. Importe `postman/projeto-integrador.postman_collection.json` (as variáveis já vão incluídas).
2. Com API e MySQL no ar, execute no **Collection Runner** na ordem (a requisição `00` gera dados únicos).
3. A coleção cobre: CRUD de todas as entidades, consultas personalizadas, regras de negócio (409), validação (400), inexistentes (404), Basic válido, sem autenticação (401), sem permissão (403) e o fluxo de contratação. Cada requisição já valida o status esperado.
4. **Para a entrega:** após rodar, use **Save Response** em cada requisição para guardar as respostas reais e exporte a coleção novamente (marcando *Share* das variáveis) sobre este arquivo.

## Funcionalidade específica

**Contratação de serviços com controle de disponibilidade:** o prestador define horários; o cliente solicita por dia ou por hora; o sistema valida data e disponibilidade, calcula o preço, impede conflito de agenda e conduz o pedido por aceite, contraproposta, recusa, cancelamento, expiração (7 dias) e conclusão, registrando o histórico.

## Dashboard (frontend)

Tela `/dashboard` com 5 KPIs (usuários, prestadores, serviços ativos, pedidos pendentes e contratações concluídas), calculados no backend a partir do banco.
