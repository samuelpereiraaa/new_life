# New Life

Protótipo de organização de documentos de startups, integrado com Spring Boot e Thymeleaf.

## Tecnologias

- Java 17
- Spring Boot
- Spring Web MVC
- Thymeleaf
- HTML, CSS e JavaScript

## Como executar

1. Instale e selecione o JDK 17.
2. Na raiz do projeto, execute `./mvnw spring-boot:run`.
3. Acesse `http://localhost:8080`.

## Rotas

- `GET /` — página inicial e upload simulado.
- `GET /documentos` — listagem dinâmica recebida do controller.
- `GET /documentos/novo` — formulário de cadastro.
- `POST /documentos` — cadastra um documento em memória e redireciona para a lista.
- `GET /documentos/{indice}` — detalhes de um documento.

Os documentos cadastrados são armazenados apenas em memória e são perdidos quando a aplicação é encerrada.
