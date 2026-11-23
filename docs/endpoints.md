# Documentação da API Trampix

- **URL base (local):** `http://localhost:8080`
- **Formato:** JSON (`Content-Type: application/json`)
- **Banco de dados:** MySQL 8
- **Datas:** `yyyy-MM-dd` · **Horas:** `HH:mm` · **Fuso:** `America/Sao_Paulo`

## 1. Autenticação e autorização

A API aceita dois esquemas, ambos no cabeçalho `Authorization`:

| Esquema | Cabeçalho | Como obter |
|---|---|---|
| HTTP Basic | `Authorization: Basic base64(email:senha)` | Qualquer conta ativa |
| JWT (Bearer) | `Authorization: Bearer <token>` | `POST /auth/login` (validade de 2 horas) |

**Usuários de teste** (configuráveis por variáveis de ambiente):

| Perfil | E-mail | Senha |
|---|---|---|
| `ADMIN` | `admin@trampix.local` | `AdminTrampix123!` |
| `USUARIO` | `usuario@trampix.local` | `UsuarioTrampix123!` |

**Perfis**

- `ADMIN`: gerencia categorias e subcategorias, lista usuários e vê qualquer contratação.
- `USUARIO`: atua como cliente e, se criar o perfil de prestador, também como prestador.

**Legenda da coluna "Acesso"**

- **Público**: sem autenticação.
- **Autenticado**: qualquer usuário logado.
- **Próprio ou ADMIN**: o dono do recurso (`{id}` igual ao id do usuário logado) ou um ADMIN.

## 2. Formato de erro

Todos os erros seguem o mesmo formato (tratado em `TratadorErros`):

```json
{
  "status": 400,
  "mensagem": "dados inválidos",
  "erros": [
    { "campo": "email", "mensagem": "deve ser um endereço de e-mail bem formado" }
  ]
}
```

O campo `erros` só aparece em erros de validação.

| Código | Quando ocorre |
|---|---|
| `400` | JSON ou campos inválidos (Bean Validation), parâmetro com tipo errado |
| `401` | Sem credenciais ou credenciais inválidas |
| `403` | Autenticado, mas sem permissão para a ação |
| `404` | Recurso inexistente |
| `409` | Violação de regra de negócio ou registro duplicado (`RegraNegocioException`) |
| `422` | Subcategoria ou categoria inativa ao cadastrar/editar serviço |
| `500` | Erro interno inesperado |

---

## 3. Endpoints

### 3.1 Autenticação

#### `POST /auth/login` — Público

Requisição:

```json
{ "email": "usuario@trampix.local", "senha": "UsuarioTrampix123!" }
```

Resposta `200`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEm": "2026-11-22T23:41:00Z",
  "usuario": { "id": 2, "nome": "Usuário Demo", "email": "usuario@trampix.local", "perfil": "USUARIO", "status": "ATIVO" }
}
```

Erros: `401` e-mail ou senha inválidos · `403` conta que não está `ATIVO`.

---

### 3.2 Usuários

| Método | Endpoint | Descrição | Acesso | Sucesso |
|---|---|---|---|---|
| `POST` | `/usuarios` | Cadastra usuário | Público | `201` |
| `GET` | `/usuarios` | Lista todos os usuários | ADMIN | `200` |
| `GET` | `/usuarios/{id}` | Consulta um usuário | Próprio ou ADMIN | `200` |
| `PUT` | `/usuarios/{id}` | Atualiza os dados | Próprio ou ADMIN | `200` |
| `DELETE` | `/usuarios/{id}` | Desativa a conta (exclusão lógica) | Próprio ou ADMIN | `204` |

**Corpo de `POST /usuarios`:**

```json
{
  "nome": "Maria da Silva",
  "email": "maria@email.com",
  "senha": "SenhaForte123",
  "documento": "52998224725",
  "tipoDocumento": "CPF",
  "dataNascimento": "1995-04-10",
  "telefone": "14999998888",
  "cep": "19900000",
  "logradouro": "Rua das Flores",
  "numero": "120",
  "bairro": "Centro",
  "cidade": "Ourinhos",
  "uf": "SP"
}
```

| Campo | Regra |
|---|---|
| `nome` | Obrigatório, até 120 caracteres |
| `email` | Obrigatório, e-mail válido e **único** |
| `senha` | Obrigatória no cadastro, de 8 a 72 caracteres (nunca é devolvida) |
| `documento` | CPF (11 dígitos) ou CNPJ (14 dígitos) válido e **único**; aceita máscara |
| `tipoDocumento` | `CPF` ou `CNPJ` |
| `dataNascimento` | Data no passado |
| `telefone` | DDD + número (10 ou 11 dígitos) |
| `cep` | 8 dígitos |
| `uf` | 2 letras maiúsculas |

Resposta `201`: o usuário criado, com `perfil: "USUARIO"` e `status: "ATIVO"`. O perfil não pode ser definido pelo cliente.

Erros: `400` validação · `409` e-mail ou documento já cadastrado.

`DELETE /usuarios/{id}` retorna `409` se o usuário tiver contratação em andamento (`PENDENTE`, `CONTRAPROPOSTA` ou `ACEITA`).

---

### 3.3 Perfil de prestador

Rota base: `/usuarios/{usuarioId}/prestador` — acesso **Próprio ou ADMIN**.

| Método | Endpoint | Descrição | Sucesso |
|---|---|---|---|
| `POST` | `/usuarios/{usuarioId}/prestador` | Cria o perfil e o primeiro serviço | `201` |
| `GET` | `/usuarios/{usuarioId}/prestador` | Consulta o perfil | `200` |
| `PUT` | `/usuarios/{usuarioId}/prestador` | Atualiza biografia, raio e agenda cheia | `200` |
| `DELETE` | `/usuarios/{usuarioId}/prestador` | Encerra o perfil (exclusão lógica) | `204` |

**Corpo de `POST`:**

```json
{
  "biografia": "Eletricista há 10 anos.",
  "raioAtendimentoKm": 30,
  "servico": {
    "categoriaId": 4,
    "novaSubcategoria": "Chuveiro elétrico",
    "titulo": "Instalação de chuveiro",
    "descricao": "Instalação completa com fiação.",
    "preco": 120.00,
    "tipoCobranca": "POR_TRABALHO"
  }
}
```

Resposta `201`: o serviço criado (veja 3.6).

**Corpo de `PUT`:**

```json
{ "biografia": "Atendo toda a região.", "raioAtendimentoKm": 40, "agendaCheia": false }
```

Regras: só usuário `ATIVO` pode virar prestador; `409` se já for prestador; `DELETE` retorna `409` enquanto houver contratação em andamento. Ao encerrar, os serviços ativos ficam `INATIVO` e as disponibilidades são removidas.

---

### 3.4 Disponibilidades (horários de atendimento)

Rota base: `/usuarios/{usuarioId}/disponibilidades`

| Método | Endpoint | Descrição | Acesso | Sucesso |
|---|---|---|---|---|
| `GET` | `/usuarios/{usuarioId}/disponibilidades` | Lista os horários | Público | `200` |
| `GET` | `/usuarios/{usuarioId}/disponibilidades/{id}` | Consulta um horário | Público | `200` |
| `POST` | `/usuarios/{usuarioId}/disponibilidades` | Adiciona um horário | Próprio ou ADMIN | `201` |
| `PUT` | `/usuarios/{usuarioId}/disponibilidades` | Substitui todos os horários | Próprio ou ADMIN | `200` |
| `PUT` | `/usuarios/{usuarioId}/disponibilidades/{id}` | Altera um horário | Próprio ou ADMIN | `200` |
| `DELETE` | `/usuarios/{usuarioId}/disponibilidades/{id}` | Remove um horário | Próprio ou ADMIN | `204` |

**Corpo de `POST` e `PUT /{id}`:**

```json
{ "diaSemana": "SEGUNDA", "horaInicio": "08:00", "horaFim": "12:00" }
```

**Corpo de `PUT` (substituir todos):**

```json
{
  "disponibilidades": [
    { "diaSemana": "SEGUNDA", "horaInicio": "08:00", "horaFim": "12:00" },
    { "diaSemana": "SEGUNDA", "horaInicio": "14:00", "horaFim": "18:00" }
  ]
}
```

`diaSemana`: `SEGUNDA`, `TERCA`, `QUARTA`, `QUINTA`, `SEXTA`, `SABADO`, `DOMINGO`.

Regras: `horaFim` deve ser maior que `horaInicio` (`400`); horários do mesmo dia não podem se sobrepor (`409`); só quem é prestador ativo tem disponibilidade (`409` na escrita, `404` na leitura).

---

### 3.5 Categorias

| Método | Endpoint | Descrição | Acesso | Sucesso |
|---|---|---|---|---|
| `GET` | `/categorias` | Lista categorias ativas com subcategorias ativas | Público | `200` |
| `GET` | `/categorias/{id}` | Consulta uma categoria | Público | `200` |
| `POST` | `/categorias` | Cria categoria | ADMIN | `201` |
| `PUT` | `/categorias/{id}` | Atualiza categoria | ADMIN | `200` |
| `DELETE` | `/categorias/{id}` | Inativa categoria | ADMIN | `204` |

**Corpo de `POST` e `PUT`:**

```json
{ "nome": "Jardineiro", "descricao": "Poda, corte de grama e paisagismo." }
```

Resposta:

```json
{
  "id": 10,
  "nome": "Jardineiro",
  "descricao": "Poda, corte de grama e paisagismo.",
  "subcategorias": []
}
```

Regras: `nome` obrigatório (até 80 caracteres) e único, ignorando maiúsculas (`409`); `DELETE` retorna `409` se houver serviços ativos na categoria.

---

### 3.5.1 Subcategorias

Rota base: `/categorias/{categoriaId}/subcategorias`

| Método | Endpoint | Descrição | Acesso | Sucesso |
|---|---|---|---|---|
| `GET` | `/categorias/{categoriaId}/subcategorias` | Lista as ativas | Público | `200` |
| `GET` | `/categorias/{categoriaId}/subcategorias/{id}` | Consulta uma | Público | `200` |
| `POST` | `/categorias/{categoriaId}/subcategorias` | Cria (ou devolve a existente com o mesmo nome) | Prestador ou ADMIN | `201` |
| `PUT` | `/categorias/{categoriaId}/subcategorias/{id}` | Atualiza | ADMIN | `200` |
| `DELETE` | `/categorias/{categoriaId}/subcategorias/{id}` | Inativa | ADMIN | `204` |

**Corpo de `POST`:** `{ "nome": "Poda", "descricao": "Poda de árvores e arbustos." }`
**Corpo de `PUT`:** igual ao anterior, mais o campo opcional `status` (`ATIVA` ou `INATIVA`).

Regras: `nome` de 3 a 40 caracteres e único dentro da categoria (`409`); `POST` por usuário que não é prestador nem ADMIN retorna `403`.

---

### 3.6 Serviços

| Método | Endpoint | Descrição | Acesso | Sucesso |
|---|---|---|---|---|
| `GET` | `/servicos?categoriaId=&subcategoriaId=` | Pesquisa serviços **ativos** (filtros opcionais) | Público | `200` |
| `GET` | `/servicos/{id}` | Detalhe de um serviço ativo | Público | `200` |
| `GET` | `/servicos/meus` | Serviços do prestador logado, em qualquer status | Autenticado (prestador) | `200` |
| `POST` | `/servicos` | Cria serviço | Prestador | `201` |
| `PUT` | `/servicos/{id}` | Atualiza serviço | Dono ou ADMIN | `200` |
| `DELETE` | `/servicos/{id}` | Inativa serviço | Dono ou ADMIN | `204` |

**Corpo de `POST` e `PUT`:**

```json
{
  "categoriaId": 4,
  "subcategoriaId": 18,
  "titulo": "Troca de tomadas",
  "descricao": "Troca e instalação de tomadas e interruptores.",
  "preco": 45.00,
  "tipoCobranca": "POR_HORA"
}
```

Informe a subcategoria de uma destas formas (nunca as duas):

- `subcategoriaId` (opcional `categoriaId` para conferência);
- `categoriaId` + `novaSubcategoria` (cria ou reaproveita a subcategoria).

| Campo | Regra |
|---|---|
| `titulo` | Obrigatório, até 120 caracteres |
| `descricao` | Obrigatória, até 2000 caracteres |
| `preco` | Maior que 0,00, até 8 inteiros e 2 decimais |
| `tipoCobranca` | `POR_HORA`, `POR_DIA`, `POR_M2` ou `POR_TRABALHO` |

Resposta `200/201`:

```json
{
  "id": 7,
  "titulo": "Troca de tomadas",
  "descricao": "Troca e instalação de tomadas e interruptores.",
  "preco": 45.00,
  "tipoCobranca": "POR_HORA",
  "status": "ATIVO",
  "categoria": { "id": 4, "nome": "Eletricista" },
  "subcategoria": { "id": 18, "nome": "Tomadas" },
  "prestador": { "id": 3, "usuarioId": 5, "nome": "João", "cidade": "Ourinhos", "uf": "SP", "verificado": false },
  "criadoEm": "2026-11-22T10:00:00",
  "atualizadoEm": "2026-11-22T10:00:00"
}
```

Regras: um prestador só pode ter **um serviço ativo por subcategoria** (`409`); quem não é prestador recebe `403` em `POST /servicos` e `GET /servicos/meus`; serviço `INATIVO` não pode ser editado (`409`).

---

### 3.7 Contratações (funcionalidade específica)

| Método | Endpoint | Descrição | Acesso | Sucesso |
|---|---|---|---|---|
| `POST` | `/contratacoes` | Cliente solicita um serviço | Autenticado | `201` |
| `GET` | `/contratacoes` | Lista as contratações do usuário logado | Autenticado | `200` |
| `GET` | `/contratacoes/{id}` | Consulta uma contratação | Cliente, prestador ou ADMIN | `200` |
| `PUT` | `/contratacoes/{id}` | Cliente edita o pedido enquanto `PENDENTE` | Cliente | `200` |
| `GET` | `/contratacoes/{id}/historico` | Histórico de mudanças | Cliente, prestador ou ADMIN | `200` |
| `GET` | `/contratacoes/{id}/historico/{historicoId}` | Um registro do histórico | Cliente, prestador ou ADMIN | `200` |
| `POST` | `/contratacoes/{id}/aceitar` | Aceita o pedido | Prestador | `200` |
| `POST` | `/contratacoes/{id}/contraproposta` | Envia contraproposta | Prestador | `200` |
| `POST` | `/contratacoes/{id}/aceitar-contraproposta` | Aceita a contraproposta | Cliente | `200` |
| `POST` | `/contratacoes/{id}/recusar` | Recusa pedido (prestador) ou contraproposta (cliente) | Cliente ou prestador | `200` |
| `POST` | `/contratacoes/{id}/cancelar` | Cancela | Cliente ou prestador | `200` |
| `POST` | `/contratacoes/{id}/concluir` | Conclui (a partir da data de início) | Prestador | `200` |
| `DELETE` | `/contratacoes/{id}` | Cancelamento lógico, preservando o histórico | Cliente, prestador ou ADMIN | `204` |

**Filtros de `GET /contratacoes`:** `papel` (`cliente` ou `prestador`), e, somente para ADMIN, `usuarioId` e `servicoId`. Usar `usuarioId` ou `servicoId` sendo USUARIO retorna `403`.

**Corpo de `POST /contratacoes`:**

Serviço **por hora** (`POR_HORA`): informe `horaInicio` e `quantidadeHoras` (1 a 24), no mesmo dia.

```json
{
  "servicoId": 7,
  "dataInicio": "2026-12-01",
  "horaInicio": "09:00",
  "quantidadeHoras": 2,
  "observacao": "Casa de dois andares."
}
```

Serviço **por dia, por m² ou por trabalho**: informe só `dataInicio` e, opcionalmente, `dataFim`, sem hora.

```json
{ "servicoId": 8, "dataInicio": "2026-12-01", "dataFim": "2026-12-03" }
```

`PUT /contratacoes/{id}` usa os mesmos campos, exceto `servicoId`.

**Contraproposta:**

```json
{
  "dataInicio": "2026-12-02",
  "horaInicio": "10:00",
  "quantidadeHoras": 3,
  "precoCombinado": 150.00,
  "motivo": "Preciso de mais tempo para o serviço."
}
```

`motivo` é obrigatório (10 a 500 caracteres). Se `precoCombinado` for omitido, o sistema recalcula o preço.

**Recusa (corpo opcional):** `{ "motivo": "Não posso nessa data." }` (até 500 caracteres).

**Resposta de contratação:**

```json
{
  "id": 21,
  "status": "PENDENTE",
  "dataInicio": "2026-12-01",
  "dataFim": "2026-12-01",
  "horaInicio": "09:00:00",
  "horaFim": "11:00:00",
  "observacao": "Casa de dois andares.",
  "precoCombinado": 90.00,
  "diasContados": 1,
  "motivoContraproposta": null,
  "contrapropostaEm": null,
  "motivoRecusa": null,
  "expiraEm": "2026-11-29T10:00:00",
  "servico": { "id": 7, "titulo": "Troca de tomadas", "tipoCobranca": "POR_HORA", "preco": 45.00,
               "categoria": { "id": 4, "nome": "Eletricista" }, "subcategoria": { "id": 18, "nome": "Tomadas" } },
  "cliente": { "id": 9, "nome": "Maria", "cidade": "Ourinhos", "uf": "SP", "telefone": null, "endereco": null },
  "prestador": { "id": 5, "nome": "João", "cidade": "Ourinhos", "uf": "SP", "telefone": null, "endereco": null },
  "criadoEm": "2026-11-22T10:00:00",
  "atualizadoEm": "2026-11-22T10:00:00"
}
```

**Privacidade:** telefone e endereço só são liberados depois que a contratação é `ACEITA` ou `CONCLUIDA`. O prestador vê telefone e endereço do cliente; o cliente vê o telefone do prestador.

#### Cálculo de preço

| Tipo de cobrança | Preço |
|---|---|
| `POR_HORA` | `preço × quantidadeHoras` |
| `POR_DIA` | `preço × dias atendidos` (conta só os dias em que o prestador tem disponibilidade) |
| `POR_M2` e `POR_TRABALHO` | `preço` do serviço |

#### Fluxo de status

```text
PENDENTE ──aceitar──────────────► ACEITA ──concluir──► CONCLUIDA
   │  ├──contraproposta──► CONTRAPROPOSTA ──aceitar-contraproposta──► ACEITA
   │  ├──recusar──► RECUSADA          └──recusar (cliente)──► RECUSADA
   │  └──7 dias sem resposta──► EXPIRADA   (vale também para CONTRAPROPOSTA)
   └──cancelar / DELETE──► CANCELADA   (também a partir de CONTRAPROPOSTA e ACEITA)
```

#### Regras de negócio (`409` quando violadas)

- O serviço precisa estar `ATIVO`, e cliente e prestador precisam estar com status `ATIVO`.
- Não é permitido contratar o próprio serviço.
- A `dataInicio` deve ser hoje ou futura; em serviço por hora, o horário não pode já ter passado.
- O prestador precisa ter disponibilidade nos dias e horários pedidos.
- Um prestador **não pode ter duas contratações `ACEITA` em conflito** de data ou horário.
- O prestador só pode fazer **uma** contraproposta por pedido.
- Só é possível `concluir` a partir da data de início.
- Transições fora do fluxo acima são recusadas.

**Histórico (`GET /contratacoes/{id}/historico`):**

```json
[
  { "id": 1, "statusAnterior": null, "statusNovo": "PENDENTE",
    "autor": { "id": 9, "nome": "Maria" }, "dataInicio": "2026-12-01", "dataFim": "2026-12-01",
    "horaInicio": "09:00:00", "horaFim": "11:00:00", "precoCombinado": 90.00,
    "diasContados": 1, "motivo": null, "criadoEm": "2026-11-22T10:00:00" }
]
```

O histórico é uma **trilha de auditoria imutável**: é criado pelo sistema a cada mudança de status e só pode ser consultado.

---

### 3.8 Dashboard

#### `GET /dashboard` — Autenticado

```json
{
  "totalUsuarios": 25,
  "totalPrestadores": 8,
  "servicosAtivos": 14,
  "pedidosPendentes": 3,
  "contratacoesConcluidas": 6
}
```

Os valores são calculados por contagens no banco de dados.

---

## 4. Consultas personalizadas (Spring Data JPA)

| Repositório | Consulta | Uso |
|---|---|---|
| `ServicoRepositorio` | `filtrar` (JPQL) | Serviços ativos por categoria e/ou subcategoria |
| `ServicoRepositorio` | `existeNaSubcategoria` (JPQL) | Um serviço ativo por prestador em cada subcategoria |
| `ContratacaoRepositorio` | `filtrar` (JPQL) | Contratações por usuário (como cliente ou prestador) e por serviço |
| `ContratacaoRepositorio` | `doPrestadorNasDatas` (JPQL) | Detecta conflito de agenda do prestador |
| `ContratacaoRepositorio` | `vencidas` (JPQL) | Pedidos e contrapropostas sem resposta no prazo |
| `ContratacaoRepositorio` | `trocarStatus` (JPQL `update`) | Troca de status condicionada ao status anterior |
| Vários | Métodos derivados | `existsByEmail`, `existsByClienteIdAndStatusIn`, `findByUsuarioIdAndAtivoTrue`, `countByStatus` etc. |

## 5. Exclusão lógica

Usuários, categorias, subcategorias, serviços, perfis de prestador e contratações **não são apagados do banco**: recebem um status ou flag (`INATIVO`, `INATIVA`, `ativa=false`, `CANCELADA`). Isso preserva os vínculos e o histórico. Apenas as **disponibilidades** são removidas fisicamente.
