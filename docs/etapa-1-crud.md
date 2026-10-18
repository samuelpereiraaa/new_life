# Etapa 1 — CRUD em memória (aulas 9 a 11)

Esta etapa usa `model.Documento`, `DocumentoForm`, `PageController` e Thymeleaf.
Os dados ainda ficam na lista do controller e são reiniciados com a aplicação.
As entidades JPA serão integradas na etapa de persistência.

## Rotas

| Método | Rota | Operação |
| --- | --- | --- |
| GET | `/documentos` | Listar e filtrar |
| GET | `/documentos/novo` | Abrir formulário vazio |
| POST | `/documentos` | Cadastrar |
| GET | `/documentos/{id}` | Abrir detalhes |
| GET | `/documentos/{id}/editar` | Preencher formulário com dados existentes |
| POST | `/documentos/{id}/editar` | Atualizar sem mudar o ID |
| POST | `/documentos/{id}/excluir` | Excluir |

## Roteiro da apresentação

1. Cadastre um documento e mostre a mensagem de sucesso.
2. Pesquise por `Contrato`: o link deve apontar para o ID 4, mesmo sendo o
   primeiro resultado do filtro. Antes, o índice da lista filtrada abria outro documento.
3. Abra a edição, mostre os campos preenchidos e altere nome e status.
4. Confirme que o ID continua igual depois da edição.
5. Exclua um documento e mostre que os IDs dos demais não mudam.
6. Acesse `/documentos/999999`: a aplicação volta à lista com mensagem de erro.
7. Reinicie a aplicação para explicar a limitação do armazenamento em memória.

## Perguntas para ensaiar

- **Por que ID e não posição na lista?** Filtrar ou excluir muda posições;
  o ID identifica o mesmo documento durante a execução.
- **Quem gera o ID nesta etapa?** O contador `proximoId`. Na persistência,
  essa responsabilidade passará para o banco.
- **Para que serve `@PathVariable`?** Captura o ID informado na URL.
- **O que fazem `th:object` e `th:field`?** Associam o formulário a um objeto
  e seus campos; na edição, também apresentam os valores existentes.
- **Por que POST na exclusão?** A operação modifica dados; GET fica para consulta.
- **O que é `redirect`?** Pede ao navegador uma nova requisição GET após o POST.
- **Como a mensagem chega à próxima página?** `RedirectAttributes.addFlashAttribute`.
- **O formulário é o registro armazenado?** Não: `DocumentoForm` recebe os dados,
  e o controller copia os campos para o documento encontrado pelo ID da rota.

## Próximos PRs

1. Entidades com Lombok e persistência: alinhar o DDL já integrado à `main`
   com `develop`, configurar PostgreSQL, Flyway e repositories.
2. Validação dos formulários: Bean Validation, `@Valid`, `BindingResult` e `th:errors`.
3. Service e regras de negócio, com testes das operações permitidas e recusadas.
4. Relacionamentos nos formulários e exemplo explícito de `@ManyToMany`.

Cada etapa terá sua branch e PR para `develop`, revisão e demonstração.
Este CRUD é didático: não oferece persistência, autenticação nem garantias
de concorrência. As validações de entrada serão implementadas na etapa própria.
