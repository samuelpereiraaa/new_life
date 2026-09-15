# Resumo das mudanças — New Life (Maven + Thymeleaf)

## Rotas e controller

- `controller/PageController.java:18-21` — `/` retorna o template `index`.
- `controller/PageController.java:23-40` — `/documentos` monta os dados e retorna `documentos`.
- `controller/PageController.java:43-53` — `dadosDeExemplo()` cria uma `List<Documento>` para a listagem.

## Dados vindos do Java

- `model/Documento.java` — classe simples com nome, categoria, responsável e status.
- `PageController.java:25` — `List<String> categorias` fornece categorias à página.
- `PageController.java:26` — `List<Documento> documentos` fornece objetos à tabela.
- O Thymeleaf percorre essa lista; os dados não ficam escritos linha por linha no HTML.

## Recursos Thymeleaf usados

- `templates/documentos.html:15` — `th:src` carrega a logo.
- `templates/documentos.html:25-29` — `th:href` cria links para as rotas.
- `templates/documentos.html:51` — `th:action` envia o formulário para `/documentos`.
- `templates/documentos.html:51` — `th:object` conecta o formulário ao objeto `filtro`.
- `templates/documentos.html:54` — `th:field` conecta o input ao campo `termo`.
- `templates/documentos.html:61` — `th:each` percorre a lista de categorias.
- `templates/documentos.html:70` — `th:each` percorre a lista de documentos.
- `templates/documentos.html:66`, `71-74` — `th:text` imprime os valores dos objetos Java.
- `model/Documento.java` — `getStatusClass()` escolhe a classe visual do status.
- `templates/documentos.html:71` — `th:if` mostra uma mensagem quando a lista está vazia.

## JavaScript

- O JavaScript não troca mais páginas.
- `static/js/index.js` cuida apenas do menu responsivo, filtro visual e upload.
- A busca principal é processada pelo controller usando o parâmetro `busca`.
