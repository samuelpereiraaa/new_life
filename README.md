# New Life

Aplicação de organização de documentos de startups com Spring Boot, Thymeleaf e PostgreSQL.

## Requisitos

- JDK 17 ou superior.
- PostgreSQL com um banco vazio chamado `new_life`.
- Maven Wrapper incluído no projeto.

## Configuração no IntelliJ

1. Recarregue o projeto Maven após abrir o `pom.xml`.
2. Em Run > Edit Configurations, selecione a execução de `NewLifeApplication`.
3. Em Modify options > Environment variables, adicione `DB_PASSWORD` com a senha
   do PostgreSQL. Salve somente na configuração local, sem compartilhar credenciais.
4. Os padrões são host `localhost`, porta `5432`, banco `new_life` e usuário `postgres`.
   Para alterar, configure `DB_URL` e `DB_USER` no mesmo local.
5. Execute a aplicação e abra `http://localhost:8080`.

Também é possível executar `./mvnw spring-boot:run` (Windows: `.\mvnw.cmd spring-boot:run`)
com as mesmas variáveis de ambiente. O arquivo `.env` não é carregado automaticamente.

## Estrutura e dados

Flyway executa `src/main/resources/db/migration/V1__estrutura_inicial.sql` na primeira
inicialização. A migration cria as oito tabelas do domínio, sem inserts. O Hibernate
usa `ddl-auto=validate` para conferir os mapeamentos, sem criar ou alterar tabelas.
Não execute o DDL manualmente antes da primeira inicialização. Migrations aplicadas
não devem ser editadas: mudanças posteriores precisam de uma nova versão.

A migration foi baseada no DDL já integrado à branch `main`. Usa
`TIMESTAMP WITH TIME ZONE`, equivalente a `TIMESTAMPTZ` no PostgreSQL. A transação
é controlada pelo Flyway, por isso o arquivo não contém `BEGIN`/`COMMIT` explícitos.

## Primeiro uso

1. Abra `/cadastros` e crie uma startup, um usuário e uma categoria.
2. Na mesma página, vincule o usuário à startup.
3. Abra `/documentos/novo` e selecione os cadastros existentes.
4. Cadastre, edite ou exclua documentos em `/documentos`.
5. Reinicie a aplicação e confira que os registros continuam disponíveis.

As senhas cadastradas são armazenadas como hashes BCrypt. O cadastro de usuários
não implementa login ou controle de acesso. A aplicação ainda não possui autenticação.

## Persistência

O pacote `entity` contém os mapeamentos JPA com Lombok. `StartupUsuarioId` representa
a chave composta. `Documento.responsavel` aponta para o vínculo entre usuário e startup.
O pacote `repository` contém interfaces Spring Data JPA para as oito entidades.
Os controllers consultam os repositories; não existe mais lista de documentos em memória.

As associações necessárias às telas são carregadas por `@EntityGraph`. O Open Session
in View está desativado. Datas são preenchidas por `@PrePersist` quando não informadas.
A exclusão de um documento com comentários, versões ou histórico é bloqueada e retorna
uma mensagem de erro, preservando os registros relacionados.

Há verificações básicas de campos e referências antes de salvar. Bean Validation,
camada Service, histórico automático de status e os fluxos de upload/comentários
não estão implementados. O upload da página inicial continua sendo uma simulação.

## Testes

Execute `./mvnw test` (Windows: `.\mvnw.cmd test`). O perfil `test` usa um banco H2
em memória, executa a mesma migration e valida os mapeamentos. Não usa a senha nem
os dados do banco de desenvolvimento. Os testes verificam cadastros auxiliares,
relacionamentos, CRUD HTTP, leitura em outra sessão JPA, restrições e feedback.

O mesmo teste pode ser executado contra um PostgreSQL descartável cujo banco se
chame `newlife_test`, sobrescrevendo as propriedades `spring.datasource` via Maven.
Os testes apagam os dados desse banco antes do cenário; nunca aponte testes para
um banco com dados que precisam ser preservados.
