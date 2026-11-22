# Documentação da API Trampix

Base local: `http://localhost:8080` · Formato: JSON · Banco: MySQL.

## Autenticação e autorização

A API usa **Spring Security com HTTP Basic** (cabeçalho `Authorization: Basic base64(usuario:senha)`). O login JWT (`POST /auth/login`) continua disponível apenas porque o frontend Angular o utiliza; todos os endpoints aceitam Basic.

Usuários de **teste** (configuráveis por variáveis de ambiente, nunca use senhas reais):

| Perfil | Usuário | Senha |
|---|---|---|
| ADMIN | `admin@trampix.local` | `AdminTrampix123!` |
| USUARIO | `usuario@trampix.local` | `UsuarioTrampix123!` |

Esses dois usuários são configurados **em memória** (Basic) e também gravados no banco ao subir a aplicação (para que a API conheça o id e o perfil do usuário logado). Qualquer conta cadastrada em `POST /usuarios` também pode usar Basic com e-mail e senha.

| Situação | Resposta |
|---|---|
| Sem credenciais ou credenciais inválidas | `401 Unauthorized` |
| Autenticado, mas sem permissão (ex.: USUARIO em `POST /categorias`) | `403 Forbidden` |

Perfis: **ADMIN** (gerencia categorias/subcategorias, lista usuários, vê qualquer contratação) e **USUARIO** (cliente e/ou prestador).

## Endpoints

| Método | Endpoint | Descrição | Acesso | Sucesso |
|---|---|---|---|---|
| POST | `/auth/login` | Login JWT (usado pelo frontend) | Público | 200 |
| **Usuários** | | | | |
| POST | `/usuarios` | Cadastra usuário | Público | 201 |
| GET | `/usuarios` | Lista usuários | ADMIN | 200 |
| GET | `/usuarios/{id}` | Consulta usuário | Próprio ou ADMIN | 200 |
| PUT | `/usuarios/{id}` | Atualiza usuário | Próprio ou ADMIN | 200 |
| DELETE | `/usuarios/{id}` | Desativa a conta (exclusão lógica) | Próprio ou ADMIN | 204 |
| **Categorias** | | | | |
| GET | `/categorias`, `/categorias/{id}` | Lista / consulta | Público | 200 |
| POST | `/categorias` | Cria | ADMIN | 201 |
| PUT | `/categorias/{id}` | Atualiza | ADMIN | 200 |
| DELETE | `/categorias/{id}` | Inativa (409 se houver serviços ativos) | ADMIN | 204 |
| **Subcategorias** | | | | |
| GET | `/categorias/{categoriaId}/subcategorias` | Lista | Público | 200 |
| GET | `/categorias/{categoriaId}/subcategorias/{id}` | Consulta | Público | 200 |
| POST | `/categorias/{categoriaId}/subcategorias` | Cria | Prestador ou ADMIN | 201 |
| PUT | `/categorias/{categoriaId}/subcategorias/{id}` | Atualiza | ADMIN | 200 |
| DELETE | `/categorias/{categoriaId}/subcategorias/{id}` | Inativa | ADMIN | 204 |
| **Prestador** | | | | |
| POST | `/usuarios/{id}/prestador` | Cria o perfil de prestador + primeiro serviço | Próprio ou ADMIN | 201 |
| GET | `/usuarios/{id}/prestador` | Consulta o perfil | Próprio ou ADMIN | 200 |
| PUT | `/usuarios/{id}/prestador` | Atualiza biografia, raio e agenda cheia | Próprio ou ADMIN | 200 |
| DELETE | `/usuarios/{id}/prestador` | Encerra o perfil (409 se houver contratação em andamento) | Próprio ou ADMIN | 204 |
| **Disponibilidade** | | | | |
| GET | `/usuarios/{id}/disponibilidades` e `/{dispId}` | Lista / consulta horários | Público | 200 |
| POST | `/usuarios/{id}/disponibilidades` | Adiciona um horário (409 se sobrepor) | Próprio ou ADMIN | 201 |
| PUT | `/usuarios/{id}/disponibilidades` | Substitui todos os horários | Próprio ou ADMIN | 200 |
| PUT | `/usuarios/{id}/disponibilidades/{dispId}` | Altera um horário | Próprio ou ADMIN | 200 |
| DELETE | `/usuarios/{id}/disponibilidades/{dispId}` | Remove um horário | Próprio ou ADMIN | 204 |
| **Serviços** | | | | |
| GET | `/servicos?categoriaId=&subcategoriaId=` | Pesquisa serviços ativos (consulta personalizada) | Público | 200 |
| GET | `/servicos/{id}` | Detalhe | Público | 200 |
| GET | `/servicos/meus` | Serviços do prestador logado | Autenticado | 200 |
| POST | `/servicos` | Cria | Prestador | 201 |
| PUT | `/servicos/{id}` | Atualiza | Dono ou ADMIN | 200 |
| DELETE | `/servicos/{id}` | Inativa | Dono ou ADMIN | 204 |
| **Contratações (funcionalidade específica)** | | | | |
| POST | `/contratacoes` | Cliente solicita um serviço | Autenticado | 201 |
| GET | `/contratacoes` | Lista as minhas (ADMIN pode filtrar) | Autenticado | 200 |
| GET | `/contratacoes/{id}` | Consulta | Participante ou ADMIN | 200 |
| PUT | `/contratacoes/{id}` | Cliente edita o pedido enquanto PENDENTE | Cliente | 200 |
| GET | `/contratacoes/{id}/historico` e `/{historicoId}` | Histórico de auditoria | Participante ou ADMIN | 200 |
| POST | `/contratacoes/{id}/aceitar` | Aceita o pedido | Prestador | 200 |
| POST | `/contratacoes/{id}/contraproposta` | Envia contraproposta | Prestador | 200 |
| POST | `/contratacoes/{id}/aceitar-contraproposta` | Aceita contraproposta | Cliente | 200 |
| POST | `/contratacoes/{id}/recusar` | Recusa | Prestador (pedido) / Cliente (contraproposta) | 200 |
| POST | `/contratacoes/{id}/cancelar` | Cancela | Cliente ou prestador | 200 |
| POST | `/contratacoes/{id}/concluir` | Conclui (a partir da data de início) | Prestador | 200 |
| DELETE | `/contratacoes/{id}` | Cancela logicamente e registra no histórico | Cliente, prestador ou ADMIN | 204 |
| **Dashboard** | | | | |
| GET | `/dashboard` | KPIs operacionais | Autenticado | 200 |

### Dashboard

`GET /dashboard` devolve `totalUsuarios`, `totalPrestadores`, `servicosAtivos`, `pedidosPendentes` e `contratacoesConcluidas`, calculados por contagens no banco.

## Regras de negócio (camada `services`)

| Regra | Onde | Resposta |
|---|---|---|
| Nome de categoria único | `CategoriaService` | 409 |
| Não excluir categoria com serviços ativos | `CategoriaService` | 409 |
| E-mail e documento (CPF/CNPJ) únicos | `UsuarioService` | 409 |
| Não desativar conta com contratação em andamento | `UsuarioService` | 409 |
| Um serviço ativo por prestador em cada subcategoria | `ServicoService` | 409 |
| Horários de disponibilidade do mesmo dia não podem se sobrepor | `DisponibilidadeService` | 409 |
| Só usuário ATIVO vira prestador; não encerrar perfil com contratação em andamento | `PrestadorService` | 409 |
| Não contratar o próprio serviço; data de início hoje ou futura; prestador precisa atender nos dias/horas pedidos | `ContratacaoService` | 409 |
| Prestador não pode ter duas contratações aceitas no mesmo intervalo | `ContratacaoService` | 409 |
| Transições de status válidas (PENDENTE → ACEITA/CONTRAPROPOSTA/RECUSADA/CANCELADA/EXPIRADA; ACEITA → CONCLUIDA/CANCELADA) | `ContratacaoService` | 409 |
| Pedidos e contrapropostas sem resposta em 7 dias expiram | `ExpiradorContratacao` | — |

## Consultas personalizadas (Spring Data JPA)

- `ServicoRepositorio.filtrar` (JPQL): serviços ativos por categoria e/ou subcategoria.
- `ContratacaoRepositorio.filtrar` (JPQL): contratações por usuário (como cliente/prestador) e serviço.
- `ContratacaoRepositorio.doPrestadorNasDatas` (JPQL): conflito de agenda do prestador.
- `ContratacaoRepositorio.vencidas` (JPQL): pedidos/contrapropostas vencidos.
- Métodos derivados: `existsByCategoriaIdAndStatus`, `existsByServicoPrestadorIdAndStatusIn`, `findByUsuarioIdAndAtivoTrue`, `findByIdAndPrestadorId`, entre outros.

## Respostas de erro

| Código | Quando |
|---|---|
| 400 | JSON ou dados inválidos (Bean Validation: `@NotBlank`, `@Email`, `@Size`, `@DecimalMin`, `@CPF`...) |
| 401 | Sem autenticação ou credenciais inválidas |
| 403 | Autenticado, mas sem permissão |
| 404 | Recurso inexistente |
| 409 | Conflito / regra de negócio (`RegraNegocioException`) |

Formato: `{ "status": 409, "mensagem": "...", "erros": null }` (para 400 de validação, `erros` lista `campo` e `mensagem`). Tratamento centralizado em `TratadorErros` (`@RestControllerAdvice`).

## Sobre exclusões e `ContratacaoHistorico`

Usuários, categorias, subcategorias, serviços, perfis de prestador e contratações usam **exclusão lógica** (status/flag), para preservar vínculos e histórico. `ContratacaoHistorico` é uma trilha de auditoria **imutável** (`@Immutable`): oferece apenas criação (feita pelo sistema a cada mudança de status) e consulta (`GET .../historico`); editar ou apagar um registro de auditoria violaria a sua finalidade.
