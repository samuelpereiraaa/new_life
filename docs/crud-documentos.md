# Gerenciamento de documentos

O módulo permite cadastrar, listar, filtrar, visualizar, editar e excluir documentos.
Os dados são persistidos no PostgreSQL e os IDs são gerados pelo banco.

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
| GET | `/cadastros` | Abrir cadastros auxiliares |
| POST | `/cadastros/startups` | Cadastrar startup |
| POST | `/cadastros/usuarios` | Cadastrar usuário |
| POST | `/cadastros/categorias` | Cadastrar categoria |
| POST | `/cadastros/vinculos` | Vincular usuário à startup |

Após uma alteração bem-sucedida, a aplicação redireciona para a listagem e exibe
uma mensagem de confirmação. IDs inexistentes retornam à lista com uma mensagem
de erro. Referências inválidas devolvem o formulário sem salvar o documento.

Cada documento exige uma categoria existente e um usuário vinculado à startup
selecionada. O formulário envia IDs; o controller busca as entidades correspondentes
nos repositories. A chave estrangeira composta também garante esse vínculo no banco.

Documentos com registros dependentes não podem ser excluídos. Nenhum cascade de
remoção é aplicado aos usuários, startups, categorias ou registros dependentes.

Consulte o README para configurar a conexão, iniciar o banco e executar os testes.
