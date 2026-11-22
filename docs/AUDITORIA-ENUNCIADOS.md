# Auditoria do Trampix — Trabalhos 1 e 2

**Auditoria estática realizada em 22/11/2026.** O código e os documentos foram inspecionados. Build e execução integrada não puderam ser concluídos neste ambiente, portanto itens que dependem de execução são marcados como **não verificados**.

Legenda: **Presente (inspeção)** = encontrado no código/documentação; **Parcial** = existe, mas depende de confirmação/ajuste; **Não verificado** = exige execução ou serviço externo.

## Trabalho 1 — Backend REST Spring Boot

| Requisito | Situação | Observação |
|---|---|---|
| Java + Spring Boot | Presente (inspeção) | Maven configurado para Java 21 e Spring Boot 4.1.1. |
| MySQL + JPA | Presente (inspeção) | Configuração por `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`; Hibernate `ddl-auto=update`. |
| Mínimo 5 entidades e relacionamentos | Presente (inspeção) | 8 entidades JPA: Usuario, PrestadorPerfil, Categoria, Subcategoria, Servico, Disponibilidade, Contratacao, ContratacaoHistorico. Diagrama acompanha os arquivos. |
| CRUD e consultas | Presente (inspeção) | Recursos REST e services/repositories; ContratacaoHistorico é audit trail de leitura e criação automática, sem edição/exclusão manual por regra de domínio. |
| Arquitetura em camadas | Presente (inspeção) | Controllers/Recursos chamam Services; repositories ficam na camada de acesso a dados. |
| Regras de negócio (mínimo 2) | Presente (inspeção) | Regras de contratação, conflitos de agenda, transições, exclusão lógica, preço, etc. |
| Bean Validation e `@Valid` | Presente (inspeção) | DTOs e recursos possuem validações; requer confirmação de execução. |
| Exceção personalizada + advice | Presente (inspeção) | `RegraNegocioException` e `TratadorErros` (`@RestControllerAdvice`). |
| HTTP 400/404/409 | Presente (inspeção) | Tratamento de validação, recurso ausente e conflito/regra de negócio. |
| Consultas JPA personalizadas | Presente (inspeção) | JPQL em repositories, incluindo filtros de serviços e contratações e consulta de conflito de agenda. |
| HTTP Basic, 2 usuários, 2 perfis | Presente (inspeção) | Basic de demonstração ADMIN/USUARIO; JWT também existe e é usado pelo frontend. |
| Proteção e autorização | Presente (inspeção) | Security config e regras por perfil; operações administrativas restringidas. |
| Funcionalidade específica do PI | Presente (inspeção) | Fluxo de contratação: solicitação, aceite, contraproposta, recusa, cancelamento, conclusão, expiração e histórico. |
| Postman com 12 cenários exigidos | Presente (inspeção) | Coleção com 74 requisições, incluindo scripts de teste/status esperados. |
| Respostas reais salvas (“Save Response”) | **Pendente** | Nenhuma requisição da coleção tem exemplo/resposta salva; só pode ser preenchido após API e banco rodarem e os requests serem executados. |
| Testes automatizados de backend | Parcial | Existem 3 classes de testes de service (Categoria, Disponibilidade, Dashboard), mas a suíte não pôde ser executada. |
| Build e execução com MySQL | **Não verificado** | Maven não está instalado no ambiente e o Maven Wrapper não conseguiu baixar sua distribuição por falha de acesso ao host. |

## Trabalho 2 — Frontend Angular

| Requisito | Situação | Observação |
|---|---|---|
| Angular com `standalone: false` e NgModules | Presente (inspeção) | Componentes e módulos usam NgModules; componentes inspecionados declarados como não standalone. |
| Roteamento habilitado | Presente (inspeção) | AppRoutingModule e módulos de rotas, incluindo lazy loading. |
| SSR/SSG desabilitado | Presente (inspeção) | Configuração Angular usa aplicação browser; não há setup SSR/SSG. |
| Angular Material | Presente (inspeção) | Módulos Material usados nas telas. |
| 4+ entidades relacionadas | Presente (inspeção) | Entidades e fluxos cliente/prestador, serviço, categoria, disponibilidade e contratação. |
| CRUD de entidades principais na interface | Presente (inspeção) | Telas/serviços para cadastros e gestão foram encontrados; funcionamento integral não foi validado no navegador. |
| Dashboard com 3+ KPIs reais | Presente (inspeção) | Dashboard chama API e apresenta indicadores fornecidos pelo backend; não há mocks no serviço de consulta inspecionado. |
| Fluxo principal de ponta a ponta | Presente (inspeção), **não verificado** | Telas para descoberta, solicitação e gestão de contratação existem; precisa teste real com API e banco. |
| Validação, erros e responsividade | Parcial / não verificado | Componentes e interceptor de erro existem; comportamento visual e acessibilidade/responsividade exigem execução. |
| Build e testes Angular | **Não verificado** | Node do ambiente é v22.16.0, inferior ao mínimo indicado pelas dependências Angular 22 (22.22.3+). `npm ci` não terminou no limite de execução; CLI não ficou instalada de forma utilizável e `npm run build` falhou por `ng` ausente. |

## Documentos e organização


- README e documentação de endpoints: presentes; README inclui comandos de execução e campos de publicação/identificação dos integrantes.
- Diagrama: `docs/modelo-dados.png` e fonte `docs/modelo-dados.dot` presentes.
- Coleção Postman: `postman/projeto-integrador.postman_collection.json` presente, com 74 requisições e scripts; **sem respostas reais salvas**.
- Dados de integrantes, IDs acadêmicos e URLs públicas: continuam como placeholders, não foram inventados.


## Ações restantes antes de entregar

1. Preencher nome completo e ID acadêmico de todos os integrantes no README.
2. Instalar Node compatível (22.22.3 ou mais recente dentro da linha 22) e executar `npm ci`, `npm run build`, `npm test`.
3. Instalar/usar Maven Wrapper em ambiente com acesso ao Maven Central, iniciar MySQL, executar `./mvnw test` e iniciar API.
4. Validar manualmente fluxo de login, busca, cadastro/gestão de serviço e contratação (solicitar, aceitar/contraproposta, concluir), incluindo permissões e mensagens de erro.
5. Executar os 74 requests do Postman, revisar resultados e salvar as respostas reais com **Save Response**; exportar a coleção atualizada.
6. Publicar frontend, backend e banco; preencher URLs e testar a versão publicada.
7. Conferir o repositório GitHub e enviar o link no Ambiente Conhecer.


## Limitações desta auditoria

A inspeção confirma a presença e a organização dos recursos no código, mas não substitui build, execução com MySQL, testes de integração, testes no navegador, execução da coleção Postman ou validação da publicação. Nenhum desses resultados foi inventado ou marcado como aprovado.
