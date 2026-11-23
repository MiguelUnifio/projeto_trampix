
# Trampix — Plataforma de Contratação de Serviços

**Projeto Integrador — Backend REST API**

Backend desenvolvido em Java e Spring Boot para uma plataforma de contratação de serviços, conectando clientes a prestadores de serviços.

## Descrição da aplicação

O **Trampix** conecta clientes a prestadores de serviços como pedreiros, eletricistas, encanadores, pintores, diaristas e outros profissionais.

O cliente pode pesquisar serviços por categoria, selecionar uma data e horário e enviar uma solicitação de contratação. O prestador pode aceitar, recusar ou enviar uma contraproposta.

A plataforma controla a disponibilidade dos prestadores, impede conflitos de agenda, calcula preços conforme o tipo de cobrança e mantém o histórico das contratações.

## Objetivo do backend

O backend (`trampix-api`) é uma API REST responsável por centralizar os dados e as regras de negócio da aplicação.

Principais funcionalidades:

- Gerenciamento de usuários e perfis de prestadores.
- Gerenciamento de categorias e subcategorias.
- Cadastro, consulta, edição e exclusão de serviços.
- Controle de disponibilidades e horários de atendimento.
- Sistema de contratações e contrapropostas.
- Validação de dados, incluindo CPF e CNPJ.
- Controle de conflitos de agenda.
- Cálculo de preços por hora ou por diária.
- Expiração de solicitações após 7 dias.
- Autenticação e autorização com Spring Security.
- Histórico de alterações das contratações.
- Dashboard com indicadores calculados a partir do banco de dados.

## Integrantes do grupo

| Integrante | RA |
|---|---:|
| Carlos Henrique Beltramo Rodrigues | 272766 |
| Davi Ferreira Corsini | 272409 |
| Diego Marques Martins | 272392 |
| Miguel Fernando Santos Prevedeli | 272671 |
| Nathalia de Almeida Grandizoli | 273239 |
| Sthefany de Carvalho Pereira | 272940 |

## Tecnologias utilizadas

### Backend

| Tecnologia | Utilização |
|---|---|
| Java 21 | Linguagem de programação |
| Spring Boot 4.1.1 | Framework para API REST |
| Spring Data JPA | Persistência e consultas ao banco |
| Hibernate | ORM |
| Bean Validation | Validação de dados |
| Spring Security | Autenticação e autorização |
| HTTP Basic | Autenticação para endpoints protegidos |
| JWT (HS256) | Autenticação via token |
| MySQL 8 | Banco de dados relacional |
| Maven Wrapper | Gerenciamento e execução do projeto |
| Lombok | Redução de código repetitivo |
| JUnit 5 | Testes |
| Mockito | Testes de serviços |

### Frontend

- Angular 22
- TypeScript
- Angular Material
- RxJS
- NgModules

### Ferramentas

- Git e GitHub
- Postman
- Docker
- Visual Studio Code
- Dev Containers
- GitHub Codespaces

## Arquitetura

O projeto utiliza arquitetura em camadas, separando responsabilidades:

```text
Entidades (JPA)
      ↓
Repositórios (Spring Data)
      ↓
Services (Regras de negócio)
      ↓
Controllers REST + DTOs
```

- **Entidades:** representam os dados e relacionamentos do sistema.
- **Repositórios:** responsáveis pelas operações de persistência e consultas.
- **Services:** centralizam as regras de negócio.
- **Controllers:** recebem as requisições HTTP e retornam respostas da API.
- **DTOs:** controlam os dados de entrada e saída.
- **TratadorErros:** padroniza as respostas de erro.
- **RegraNegocioException:** representa conflitos e regras de negócio, retornando HTTP 409.

Os controllers não acessam diretamente os repositórios. As operações são realizadas por meio da camada de serviços.

### Modelo de dados

O sistema possui 8 entidades principais:

1. `Usuario`
2. `PrestadorPerfil`
3. `Categoria`
4. `Subcategoria`
5. `Servico`
6. `Disponibilidade`
7. `Contratacao`
8. `ContratacaoHistorico`

O diagrama do modelo de dados está disponível em:

`docs/diagrama-classes.png`

## Estrutura do repositório

```text
projeto_trampix/
├── docs/
│   ├── documentacao-api.md
│   └── diagrama-classes.png
├── postman/
│   ├── projeto-integrador.postman_collection.json
│   └── imagem-testes.png
├── trampix-api/
│   ├── src/
│   ├── pom.xml
│   └── mvnw
├── trampix/
│   └── frontend Angular (versão do projeto original)
├── .gitignore
├── README.md
├── popular_trampix.py
└── trampix_backup.sql
```

O frontend também possui um repositório separado:

**GitHub:** https://github.com/MiguelUnifio/trampix-front

## Como configurar e executar o projeto

### Pré-requisitos

Para executar o projeto, são necessários:

- Git
- Java JDK 21
- Docker
- Node.js e npm (para executar o frontend)
- Postman (opcional, para testes da API)

Não é necessário instalar o MySQL diretamente no computador. O banco de dados será executado em um container Docker.

### 1. Clonar o repositório

Abra o terminal e execute:

```bash
git clone https://github.com/MiguelUnifio/projeto_trampix.git
cd projeto_trampix
```

### 2. Criar o banco MySQL utilizando Docker

Certifique-se de que o Docker esteja aberto e em execução.

Crie o container MySQL com o banco `trampix`, utilizando as credenciais padrão do projeto:

- **Banco:** `trampix`
- **Usuário:** `root`
- **Senha:** `trampix123`
- **Porta:** `3306`

Execute:

```bash
docker run -d --name trampix-mysql -e MYSQL_ROOT_PASSWORD=trampix123 -e MYSQL_DATABASE=trampix -p 3306:3306 mysql:8.0
```

Verifique se o container está rodando:

```bash
docker ps
```

O container `trampix-mysql` deverá aparecer na lista.

> Caso o container já exista, não execute novamente o comando de criação. Verifique se ele está ativo com `docker ps -a` e, se estiver parado, inicie com `docker start trampix-mysql`.

### 3. Importar o backup do banco de dados

O projeto possui um backup SQL com dados de exemplo, usuários, categorias, serviços e registros utilizados para facilitar a demonstração.

Arquivo:

```text
trampix_backup.sql
```

**Importante:** a importação deve ser executada na pasta raiz do repositório, onde está localizado o arquivo `trampix_backup.sql`, e não dentro da pasta `trampix-api`.

A importação deve ser realizada após a criação e inicialização do container MySQL.

#### Windows (PowerShell)

Na pasta raiz do projeto, execute:

```powershell
Get-Content .\trampix_backup.sql -Raw | docker exec -i trampix-mysql mysql -uroot -ptrampix123 trampix
```

#### Linux / macOS / Git Bash / Codespaces

Na pasta raiz do projeto, execute:

```bash
docker exec -i trampix-mysql mysql -uroot -ptrampix123 trampix < trampix_backup.sql
```

Aguarde o comando finalizar. Se não houver mensagens de erro, a importação foi executada.

> O aviso de utilização de senha na linha de comando pode aparecer durante a importação. Esse aviso é informativo e não significa que o procedimento falhou.

#### Verificar se os dados foram importados

Execute:

```bash
docker exec -it trampix-mysql mysql -uroot -ptrampix123 -D trampix
```

No prompt do MySQL, execute:

```sql
SHOW TABLES;
```

Para sair:

```sql
exit;
```

### 4. Configuração do backend

Acesse a pasta do backend:

```bash
cd trampix-api
```

As configurações padrão utilizam:

| Configuração | Valor |
|---|---|
| Banco | `trampix` |
| Host | `localhost` |
| Porta MySQL | `3306` |
| Usuário | `root` |
| Senha | `trampix123` |
| Porta da API | `8080` |

As configurações podem ser ajustadas por variáveis de ambiente.

| Variável | Valor padrão / finalidade |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/trampix?createDatabaseIfNotExist=true&serverTimezone=America/Sao_Paulo` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `trampix123` |
| `TRAMPIX_JWT_SEGREDO` | Segredo utilizado para assinatura JWT |
| `TRAMPIX_CORS_ORIGENS` | Origens autorizadas para acesso à API |
| `TRAMPIX_ADMIN_EMAIL` | E-mail para criação do administrador inicial, se necessário |
| `TRAMPIX_ADMIN_SENHA` | Senha do administrador inicial, se necessário |

As variáveis de ambiente são opcionais para a execução local, pois o projeto já possui valores padrão de desenvolvimento.

### 5. Executar o backend (Windows/Linux)

> **Está utilizando o GitHub Codespaces?** Pule esta seção e vá diretamente para a [Seção 6 — Executando o projeto no GitHub Codespaces](#6-executando-o-projeto-no-github-codespaces).

#### Windows

Dentro da pasta `trampix-api`, execute:

```powershell
.\mvnw.cmd spring-boot:run
```

#### Linux / macOS

Dentro da pasta `trampix-api`, execute:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

A API será disponibilizada em:

**http://localhost:8080**

O backend poderá inicializar as tabelas e dados básicos necessários à aplicação, conforme sua configuração. Com o backup importado, os registros de demonstração estarão disponíveis no banco.

Para executar os testes automatizados:

**Windows:**

```powershell
.\mvnw.cmd test
```

**Linux / macOS:**

```bash
./mvnw test
```

### 6. Executar o projeto no GitHub Codespaces

O projeto também pode ser executado diretamente pelo GitHub Codespaces, sem a necessidade de configurar manualmente Java, Maven e MySQL no computador.

Depois de criar um Codespace a partir do repositório, siga as etapas anteriores para criar o banco MySQL e importar o backup.

#### 6.1. Iniciar o backend

Abra um terminal no Codespace e acesse a pasta do backend:

```bash
cd /workspaces/projeto_trampix/trampix-api
```

Configure as credenciais do banco de dados:

```bash
export DB_USERNAME=root
export DB_PASSWORD=trampix123
```

Configure a origem autorizada para o frontend do próprio Codespace:

```bash
export TRAMPIX_CORS_ORIGENS="https://${CODESPACE_NAME}-4200.app.github.dev"
```

Conceda permissão de execução ao Maven Wrapper e inicie o Spring Boot:

```bash
chmod +x mvnw

./mvnw spring-boot:run
```

Aguarde até o Spring Boot informar que a aplicação foi iniciada.

#### 6.2. Tornar a porta 8080 pública

Após iniciar o backend:

1. Abra a aba **Ports** no VS Code do Codespace.
2. Localize a porta `8080`.
3. Clique com o botão direito sobre a porta ou abra o menu de opções.
4. Selecione **Port Visibility**.
5. Altere a visibilidade para **Public**.
6. Copie o endereço encaminhado da porta `8080`, se necessário.

**Importante:** a porta `8080` precisa estar pública para permitir o acesso ao backend pelo navegador e pelo frontend.

O endereço público será semelhante a:

```text
https://NOME-DO-CODESPACE-8080.app.github.dev
```

O nome e o endereço variam conforme o Codespace criado.


#### 6.3 Instalar as dependências do frontend

Abra um **novo terminal** no Codespace, sem encerrar o terminal em que o backend está rodando.

Acesse a pasta do Angular:

```bash
cd /workspaces/projeto_trampix/trampix
```

Instale as dependências:

```bash
npm install
```

Aguarde a instalação finalizar.

#### 6.4. Iniciar o frontend

Execute:

```bash
npm start -- --host 0.0.0.0
```

O Angular será iniciado na porta `4200`.

#### 6.6. Abrir o frontend no navegador

1. Abra a aba **Ports** no VS Code do Codespace.
2. Localize a porta `4200`.
3. Clique em **Open in Browser**.

O frontend será aberto no navegador e poderá realizar requisições ao backend iniciado na porta `8080`.

**Importante:** mantenha os terminais do backend e do frontend em execução enquanto estiver utilizando a aplicação.

## Usuários de demonstração

O banco de dados disponibiliza usuários de teste para demonstrar os níveis de acesso.

| Perfil | Usuário | Senha |
|---|---|---|
| ADMIN | `admin@trampix.local` | `AdminTrampix123!` |
| USUARIO | `usuario@trampix.local` | `UsuarioTrampix123!` |

Essas credenciais são destinadas exclusivamente à demonstração e aos testes do projeto.

Contas adicionais podem ser criadas pela API, utilizando o endpoint de cadastro de usuários.

## Testando a API

### Exemplos utilizando cURL

#### Endpoint público

Consultar categorias:

```bash
curl http://localhost:8080/categorias
```

No Codespaces, substitua o endereço local pelo endereço público da porta `8080`.

#### Endpoint autenticado

Consultar os serviços do usuário autenticado:

```bash
curl -u usuario@trampix.local:UsuarioTrampix123! http://localhost:8080/servicos/meus
```

### Postman

A coleção de testes está disponível em:

```text
postman/projeto-integrador.postman_collection.json
```

Para executar:

1. Abra o Postman.
2. Clique em **Import**.
3. Selecione o arquivo `projeto-integrador.postman_collection.json`.
4. Abra a coleção importada.
5. Confira as variáveis da coleção, caso solicitado.
6. Execute as requisições individualmente ou pelo **Collection Runner**.

A coleção contempla:

- CRUD das entidades.
- Cadastro e consulta de registros.
- Atualização e exclusão.
- Consultas personalizadas.
- Regras de negócio.
- Validação de dados.
- Recursos inexistentes.
- Autenticação.
- Requisições sem autenticação.
- Controle de permissões.
- Respostas HTTP 400, 401, 403, 404 e 409.
- Fluxo de contratação.

## Documentação da API

A documentação completa, contendo os endpoints, métodos HTTP, parâmetros, exemplos e regras de acesso, está disponível em:

`docs/documentacao-api.md`

A documentação completa dos endpoints está disponível em:

`docs/endpoints.md`

### Principais endpoints

| Recurso | Endpoints | Acesso |
|---|---|---|
| Autenticação | `POST /auth/login` | Público |
| Usuários | `/usuarios` | Cadastro público; demais operações protegidas |
| Prestador | `/usuarios/{id}/prestador` | Próprio usuário ou ADMIN |
| Disponibilidades | `/usuarios/{id}/disponibilidades` | Leitura pública; escrita protegida |
| Categorias | `/categorias` | Leitura pública; escrita ADMIN |
| Subcategorias | `/categorias/{id}/subcategorias` | Conforme permissões |
| Serviços | `/servicos` | Leitura pública; escrita protegida |
| Contratações | `/contratacoes` | Autenticado |
| Dashboard | `/dashboard` | Autenticado |

### Autenticação

O sistema oferece autenticação por HTTP Basic e JWT, conforme a configuração dos endpoints.

- **HTTP Basic:** envia e-mail e senha nas credenciais de autenticação.
- **JWT:** obtém um token por meio do endpoint de login e utiliza o token Bearer nas requisições protegidas.

As permissões controlam o acesso de usuários comuns e administradores.

## Funcionalidade específica — Contratação e controle de disponibilidade

O principal fluxo de negócio do Trampix é o sistema de contratação de serviços.

1. O prestador cadastra suas disponibilidades.
2. O cliente seleciona um serviço.
3. O cliente solicita uma data e horário.
4. O sistema verifica a disponibilidade do prestador.
5. O sistema calcula o preço conforme o tipo de cobrança.
6. A solicitação é registrada como pendente.
7. O prestador pode aceitar, recusar ou enviar uma contraproposta.
8. O sistema impede conflitos de agenda.
9. Contratações aceitas podem ser concluídas.
10. As mudanças de status são registradas no histórico.

### Estados da contratação

```text
PENDENTE
 ├── ACEITA
 │    └── CONCLUIDA
 ├── CONTRAPROPOSTA
 ├── RECUSADA
 ├── CANCELADA
 └── EXPIRADA
```

Solicitações e contrapropostas sem resposta dentro do prazo estabelecido de 7 dias podem ser expiradas automaticamente.

Cada alteração relevante é registrada na entidade `ContratacaoHistorico`, permitindo consultar a evolução da contratação.

## Dashboard

O backend disponibiliza o endpoint `/dashboard`, responsável por fornecer indicadores calculados a partir dos dados do sistema.

Os indicadores incluem:

- Total de usuários.
- Total de prestadores ativos.
- Serviços ativos.
- Contratações pendentes.
- Contratações concluídas.

Esses dados são utilizados pelo frontend para exibir o resumo operacional da plataforma.

## Frontend e publicação

O frontend do Trampix está publicado na Vercel e possui um repositório independente.

### Aplicação publicada

**https://trampix-front.vercel.app/**

### Repositório do frontend

**https://github.com/MiguelUnifio/trampix-front**

A versão publicada é uma demonstração frontend independente, que simula as operações da plataforma utilizando dados locais do navegador (`localStorage`), permitindo visualizar e executar o fluxo principal da aplicação sem a necessidade de iniciar o backend.

O frontend localizado na pasta `trampix/` deste repositório pode ser executado em conjunto com o backend, conforme as instruções da seção de GitHub Codespaces.

A API REST real pode ser executada localmente ou no Codespaces e testada utilizando o Postman.

## Publicação do backend

O backend pode ser publicado em um serviço de hospedagem compatível com Java/Spring Boot, utilizando um banco MySQL gerenciado.

Para ambientes de produção, devem ser configuradas as variáveis:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `TRAMPIX_JWT_SEGREDO`
- `TRAMPIX_CORS_ORIGENS`

As credenciais padrão deste README são destinadas ao ambiente local de desenvolvimento e demonstração.

## Considerações finais

O Trampix foi desenvolvido como Projeto Integrador, aplicando conceitos de desenvolvimento de APIs REST, arquitetura em camadas, persistência relacional, autenticação, autorização, validação de dados e regras de negócio.

O projeto integra backend e frontend, oferecendo uma plataforma de demonstração para contratação de serviços, com controle de disponibilidade, gestão de solicitações e acompanhamento do processo de contratação.

---

**Trampix — Projeto Integrador**
