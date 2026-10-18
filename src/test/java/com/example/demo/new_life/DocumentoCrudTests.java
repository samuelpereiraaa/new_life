package com.example.demo.new_life;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DocumentoCrudTests {

    @Value("${local.server.port}")
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void crudPreservaIdsRenderizaFormulariosETrataAusencias() throws Exception {
        // Um resultado filtrado precisa apontar para o ID original, nao para o indice zero.
        String filtrados = get("/documentos?termo=Contrato").body();
        assertTrue(filtrados.contains("href=\"/documentos/4\""));
        assertTrue(get("/documentos/4").body().contains("Contrato social"));

        String novo = get("/documentos/novo").body();
        assertTrue(novo.contains("action=\"/documentos\""));
        assertTrue(novo.contains("Novo documento"));

        HttpResponse<String> cadastro = post("/documentos",
                "nome=Documento+de+teste&categoria=Legal&responsavel=Ana&status=Pendente");
        assertRedirect(cadastro);
        String cookie = cadastro.headers().firstValue("set-cookie").orElseThrow().split(";", 2)[0];
        HttpResponse<String> lista = client.send(HttpRequest.newBuilder(uri("/documentos"))
                .header("Cookie", cookie).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertTrue(lista.body().contains("Documento cadastrado com sucesso!"));

        var link = Pattern.compile("href=\"/documentos/(\\d+)\"[^>]*>Documento de teste</a>")
                .matcher(lista.body());
        assertTrue(link.find());
        long id = Long.parseLong(link.group(1));
        assertTrue(id > 9);

        String editar = get("/documentos/" + id + "/editar").body();
        assertTrue(editar.contains("action=\"/documentos/" + id + "/editar\""));
        assertTrue(editar.contains("value=\"Documento de teste\""));
        assertTrue(editar.contains("value=\"Legal\" selected=\"selected\""));

        HttpResponse<String> atualizacao = post("/documentos/" + id + "/editar",
                "nome=Documento+editado&categoria=Financeiro&responsavel=Bruno&status=Aprovado");
        assertRedirect(atualizacao);
        assertFlash(atualizacao, "Documento atualizado com sucesso!");
        String detalhes = get("/documentos/" + id).body();
        assertTrue(detalhes.contains("Documento editado"));
        assertTrue(detalhes.contains("Bruno"));
        assertTrue(detalhes.contains("Aprovado"));

        HttpResponse<String> exclusao = post("/documentos/1/excluir", "");
        assertRedirect(exclusao);
        assertFlash(exclusao, "Documento excluído com sucesso!");
        assertTrue(get("/documentos/4").body().contains("Contrato social"));
        assertFalse(get("/documentos").body().contains("href=\"/documentos/1\""));

        assertRedirect(post("/documentos/" + id + "/excluir", ""));
        assertFalse(get("/documentos").body().contains("Documento editado"));
        assertEquals(405, get("/documentos/4/excluir").statusCode());

        for (String rota : new String[]{"/documentos/999999", "/documentos/999999/editar"}) {
            HttpResponse<String> ausente = get(rota);
            assertRedirect(ausente);
            assertFlash(ausente, "Documento não encontrado.");
        }
        for (String acao : new String[]{"editar", "excluir"}) {
            HttpResponse<String> ausente = post("/documentos/999999/" + acao, "nome=Ausente");
            assertRedirect(ausente);
            assertFlash(ausente, "Documento não encontrado.");
        }

        assertRedirect(post("/documentos", "nome=Outro&categoria=Legal&responsavel=Ana&status=Pendente"));
        String ultimaLista = get("/documentos").body();
        assertTrue(ultimaLista.contains("href=\"/documentos/" + (id + 1) + "\""));
    }

    private void assertFlash(HttpResponse<String> response, String mensagem) throws Exception {
        String cookie = response.headers().firstValue("set-cookie").orElseThrow().split(";", 2)[0];
        HttpResponse<String> pagina = client.send(HttpRequest.newBuilder(uri("/documentos"))
                .header("Cookie", cookie).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, pagina.statusCode());
        assertTrue(pagina.body().contains(mensagem));
    }

    private void assertRedirect(HttpResponse<String> response) {
        assertEquals(302, response.statusCode());
        URI destino = URI.create(response.headers().firstValue("location").orElseThrow());
        // O servidor pode incluir o ID da sessao no redirect antes do primeiro cookie.
        assertEquals("/documentos", destino.getPath().split(";", 2)[0]);
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri(path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertNotEquals(500, response.statusCode(), response.body());
        return response;
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
}
