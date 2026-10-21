# New Life

Aplicação web para organizar documentos de startups, desenvolvida com Spring Boot, Thymeleaf e PostgreSQL.

## Funcionalidades

- Cadastro, listagem, pesquisa, edição e exclusão de documentos.
- Cadastro de startups, usuários, categorias e tags.
- Vínculo de usuários com startups.
- Associação de múltiplas tags aos documentos.
- Validação de formulários e mensagens de sucesso ou erro.
- Persistência dos dados no PostgreSQL.

## Tecnologias

- Java 17 ou superior
- Spring Boot
- Spring Data JPA / Hibernate
- Thymeleaf
- Bean Validation
- Lombok
- PostgreSQL
- Flyway
- Maven

## Organização do projeto

| Pasta | Responsabilidade |
|---|---|
| `controller` | Requisições e navegação entre páginas |
| `service` | Regras de negócio e transações |
| `repository` | Acesso aos dados |
| `entity` | Entidades mapeadas para o banco |
| `model` | Objetos de formulário e filtro |
| `templates` | Páginas HTML |
| `static` | Estilos, imagens e JavaScript |
| `db/migration` | Migrações do banco de dados |
