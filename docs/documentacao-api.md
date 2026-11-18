# Documentação da API Trampix

Base local: `http://localhost:8080`.

## Autenticação

A API mantém o login JWT utilizado pelo frontend e também disponibiliza HTTP Basic para a demonstração exigida no trabalho. Usuários de teste configuráveis por variáveis de ambiente:

| Perfil | Usuário padrão | Senha padrão |
|---|---|---|
| ADMIN | `admin@trampix.local` | `AdminTrampix123!` |
| USUARIO | `usuario@trampix.local` | `UsuarioTrampix123!` |

Envie HTTP Basic no Postman. O usuário ADMIN pode consultar `/usuarios` e administrar categorias. O usuário comum pode acessar `/dashboard`, mas recebe 403 ao tentar operações administrativas. Sem credenciais, recursos protegidos retornam 401.

> As credenciais acima são exclusivamente de desenvolvimento/avaliação. Altere-as por variáveis de ambiente antes de publicar.

## Endpoints principais

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/auth/login` | Login JWT legado do frontend | Público |
| POST | `/usuarios` | Cadastro de usuário | Público |
| GET | `/usuarios` | Lista usuários | ADMIN |
| GET/PUT/DELETE | `/usuarios/{id}` | Consulta, edição e inativação | Próprio usuário ou ADMIN |
| GET | `/categorias` | Lista categorias e subcategorias ativas | Público |
| GET | `/categorias/{id}` | Consulta categoria | Público |
| POST/PUT/DELETE | `/categorias[/{id}]` | Gerenciamento de categorias | ADMIN |
| GET | `/servicos` | Pesquisa serviços ativos; aceita `categoriaId` e `subcategoriaId` | Público |
| GET | `/servicos/{id}` | Detalhe de serviço | Público |
| GET | `/servicos/meus` | Serviços do prestador autenticado | Autenticado |
| POST/PUT/DELETE | `/servicos[/{id}]` | Gerenciamento de serviços | Prestador/dono ou ADMIN |
| GET/PUT | `/usuarios/{id}/disponibilidades` | Consulta e substituição da disponibilidade | Conforme autorização do recurso |
| GET/POST | `/usuarios/{id}/prestador` | Consulta/criação do perfil de prestador e primeiro serviço | Próprio usuário ou ADMIN |
| GET/PUT | `/usuarios/{id}/disponibilidades` | Consulta e substituição dos horários do prestador | Leitura pública; alteração pelo próprio usuário ou ADMIN |
| GET/POST | `/contratacoes` | Lista e solicita contratação | Autenticado |
| GET | `/contratacoes/{id}` | Consulta contratação | Participante ou ADMIN |
| GET | `/contratacoes/{id}/historico` | Histórico de auditoria | Participante ou ADMIN |
| POST | `/contratacoes/{id}/aceitar` | Aceita pedido | Prestador |
| POST | `/contratacoes/{id}/contraproposta` | Envia contraproposta | Prestador |
| POST | `/contratacoes/{id}/aceitar-contraproposta` | Aceita contraproposta | Cliente |
| POST | `/contratacoes/{id}/recusar` | Recusa pedido | Participante autorizado |
| POST | `/contratacoes/{id}/cancelar` | Cancela contratação | Participante autorizado |
| POST | `/contratacoes/{id}/concluir` | Conclui contratação | Participante autorizado |
| DELETE | `/contratacoes/{id}` | Cancela logicamente um pedido ainda cancelável e registra histórico | Cliente, prestador ou ADMIN |
| GET | `/dashboard` | KPIs operacionais | Autenticado |

## Dashboard

`GET /dashboard` retorna `totalUsuarios`, `totalPrestadores`, `servicosAtivos`, `pedidosPendentes` e `contratacoesConcluidas`, calculados diretamente com consultas de contagem no banco.

## Respostas de erro

- `400`: corpo JSON ou dados inválidos.
- `401`: ausência de autenticação ou credenciais inválidas.
- `403`: usuário autenticado sem permissão.
- `404`: recurso inexistente.
- `409`: conflito ou violação de regra de negócio.

Os corpos de erro seguem o formato `{ "status": 409, "mensagem": "...", "erros": null }`.

## Observação sobre exclusões

Usuários, categorias e serviços usam exclusão lógica/inativação para preservar vínculos históricos. `ContratacaoHistorico` é uma trilha de auditoria imutável (`@Immutable`): não possui edição ou exclusão deliberadamente, pois alterar registros históricos comprometeria a integridade do histórico. A contratação segue o fluxo de estados do domínio (cancelar/recusar/concluir), em vez de remoção física.
