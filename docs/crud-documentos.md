# Gerenciamento de documentos

O módulo permite cadastrar, listar, filtrar, visualizar, editar e excluir documentos.
Cada documento recebe um ID que permanece estável durante sua existência na aplicação.
Filtros e exclusões não alteram os identificadores dos demais registros.

## Rotas

| Método | Rota | Operação |
| --- | --- | --- |
| GET | `/documentos` | Listar e filtrar por nome, categoria ou responsável |
| GET | `/documentos/novo` | Abrir formulário de cadastro |
| POST | `/documentos` | Cadastrar documento |
| GET | `/documentos/{id}` | Consultar detalhes |
| GET | `/documentos/{id}/editar` | Abrir formulário com os dados atuais |
| POST | `/documentos/{id}/editar` | Atualizar documento preservando seu ID |
| POST | `/documentos/{id}/excluir` | Excluir documento |

Após uma alteração bem-sucedida, a aplicação redireciona para a listagem e exibe
uma mensagem de confirmação. Quando o ID não existe, retorna à listagem com
uma mensagem de erro. A interface solicita confirmação antes da exclusão.

## Armazenamento e limitações

Os registros ficam em memória no controller. Ao reiniciar a aplicação, os dados
cadastrados são perdidos e a lista de exemplos é restaurada. O módulo ainda não
oferece persistência, autenticação, validação completa no backend ou garantias
para alterações concorrentes.

## Verificação

Execute `./mvnw test` (ou `.\mvnw.cmd test` no Windows).
O teste `DocumentoCrudTests` verifica o fluxo HTTP de cadastro, consulta,
edição e exclusão, a renderização dos formulários, mensagens de feedback,
IDs estáveis após filtros e remoções e o tratamento de registros inexistentes.
