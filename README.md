New Life

Aplicação web para organizar documentos de startups, desenvolvida com Spring Boot, Thymeleaf e PostgreSQL.

Funcionalidades

Cadastro, listagem, pesquisa, edição e exclusão de documentos.

Cadastro de startups, usuários, categorias e tags.

Vínculo de usuários com startups.

Associação de múltiplas tags aos documentos.

Validação dos formulários e mensagens de sucesso ou erro.

Persistência dos dados no PostgreSQL.

Tecnologias

Java 17 ou superior

Spring Boot

Spring Data JPA / Hibernate

Thymeleaf

Bean Validation

Lombok

PostgreSQL

Flyway

Maven

Organização

controller: requisições e navegação entre páginas.

service: regras de negócio e transações.

repository: acesso aos dados.

entity: entidades mapeadas para o banco.

model: objetos de formulário e filtro.

templates: páginas HTML.

static: estilos, imagens e JavaScript.

db/migration: migrações do banco de dados.
