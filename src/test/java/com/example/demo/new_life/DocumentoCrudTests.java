package com.example.demo.new_life;

import com.example.demo.new_life.entity.Comentario;
import com.example.demo.new_life.entity.Documento;
import com.example.demo.new_life.repository.*;
import jakarta.persistence.EntityManagerFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DocumentoCrudTests {
    @Value("${local.server.port}")
    private int port;
    @Autowired private DocumentoRepository documentos;
    @Autowired private StartupRepository startups;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private CategoriaRepository categorias;
    @Autowired private ComentarioRepository comentarios;
    @Autowired private EntityManagerFactory entityManagerFactory;
    @Autowired private JdbcTemplate jdbc;
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void crudPersisteRelacionamentosETrataFalhas() throws Exception {
        try (var connection = jdbc.getDataSource().getConnection()) {
            String url = connection.getMetaData().getURL();
            assertTrue(url.startsWith("jdbc:h2:mem:newlife-test")
                    || url.matches("jdbc:postgresql://(127\\.0\\.0\\.1|localhost):\\d+/newlife_test"),
                    "A limpeza so pode executar no banco isolado de testes.");
        }
        // Limpa apenas o banco de testes selecionado pelo perfil test.
        for (String tabela : new String[]{"comentario", "documento_versao", "historico_status",
                "documento", "startup_usuario", "categoria", "usuario", "startup"}) {
            jdbc.update("DELETE FROM " + tabela);
        }
        assertTrue(get("/documentos").body().contains("Nenhum documento encontrado"));
        assertTrue(get("/documentos/novo").body().contains("cadastros auxiliares"));
        assertTrue(get("/cadastros").body().contains("Nenhum vínculo cadastrado"));
        var camposVazios = post("/documentos", "nome=+++&status=Invalido");
        assertEquals(200, camposVazios.statusCode());
        assertTrue(camposVazios.body().contains("Informe o nome do documento."));
        assertTrue(camposVazios.body().contains("Selecione uma categoria."));
        assertTrue(camposVazios.body().contains("Selecione um status válido."));
        assertEquals(0, documentos.count());
        var startupInvalida = post("/cadastros/startups", "nome=+++&cnpj=123456789012345");
        assertEquals(200, startupInvalida.statusCode());
        assertTrue(startupInvalida.body().contains("Informe o nome da startup."));
        assertTrue(startupInvalida.body().contains("O CNPJ deve ter até 14 caracteres"));
        assertEquals(0, startups.count());
        var emailInvalido = post("/cadastros/usuarios", "nome=Nome+preservado&email=invalido&senha=Segredo123");
        assertEquals(200, emailInvalido.statusCode());
        assertTrue(emailInvalido.body().contains("Informe um e-mail válido."));
        assertTrue(emailInvalido.body().contains("value=\"Nome preservado\""));
        assertFalse(emailInvalido.body().contains("Segredo123"));
        assertEquals(0, usuarios.count());
        var senhaLonga = post("/cadastros/usuarios", "nome=Ana&email=ana%40teste.local&senha=" + "%C3%A1".repeat(37));
        assertEquals(200, senhaLonga.statusCode());
        assertTrue(senhaLonga.body().contains("72 bytes"));
        assertEquals(0, usuarios.count());
        var categoriaInvalida = post("/cadastros/categorias", "nome=" + "x".repeat(81));
        assertEquals(200, categoriaInvalida.statusCode());
        assertTrue(categoriaInvalida.body().contains("O nome deve ter até 80 caracteres."));
        assertEquals(0, categorias.count());
        var vinculoInvalido = post("/cadastros/vinculos", "startupId=abc&usuarioId=");
        assertEquals(200, vinculoInvalido.statusCode());
        assertTrue(vinculoInvalido.body().contains("Selecione uma opção válida."));
        assertTrue(vinculoInvalido.body().contains("Selecione um usuário."));
        assertRedirect(post("/cadastros/startups", "nome=Startup+Teste&cnpj="), "/cadastros");
        assertRedirect(post("/cadastros/usuarios", "nome=Ana&email=ana%40teste.local&senha=SenhaTeste123"), "/cadastros");
        assertRedirect(post("/cadastros/categorias", "nome=Legal"), "/cadastros");
        long startupId = startups.findAll().get(0).getId();
        long usuarioId = usuarios.findAll().get(0).getId();
        long categoriaId = categorias.findAll().get(0).getId();
        var duplicado = post("/cadastros/usuarios", "nome=Outra+Ana&email=ana%40teste.local&senha=OutroSegredo123");
        assertEquals(200, duplicado.statusCode());
        assertTrue(duplicado.body().contains("E-mail já cadastrado."));
        assertTrue(duplicado.body().contains("value=\"Outra Ana\""));
        assertFalse(duplicado.body().contains("OutroSegredo123"));
        assertEquals(1, usuarios.count());
        assertTrue(new BCryptPasswordEncoder().matches("SenhaTeste123", usuarios.findById(usuarioId).orElseThrow().getSenhaHash()));
        String campos = "&categoriaId=" + categoriaId + "&startupId=" + startupId
                + "&responsavelId=" + usuarioId + "&status=Pendente";
        // O usuario existe, mas ainda nao pertence a startup.
        assertEquals(200, post("/documentos", "nome=Sem+vinculo" + campos).statusCode());
        assertEquals(0, documentos.count());
        assertRedirect(post("/cadastros/vinculos", "startupId=" + startupId + "&usuarioId=" + usuarioId), "/cadastros");
        assertTrue(get("/cadastros").body().contains("Startup Teste"));
        HttpResponse<String> criado = post("/documentos", "nome=Contrato+social" + campos);
        assertRedirect(criado, "/documentos");
        assertFlash(criado, "Documento cadastrado com sucesso!");
        Documento documento = documentos.findAll().get(0);
        long id = documento.getId();
        assertNotNull(documento.getCriadoEm());
        assertEquals(startupId, documento.getStartup().getId());
        // Uma sessao independente confirma a leitura do registro no banco.
        var em = entityManagerFactory.createEntityManager();
        try { assertEquals("Contrato social", em.find(Documento.class, id).getNome()); }
        finally { em.close(); }
        assertTrue(get("/documentos?termo=Contrato").body().contains("href=\"/documentos/" + id + "\""));
        assertTrue(get("/documentos/" + id).body().contains("Startup Teste"));
        String editar = get("/documentos/" + id + "/editar").body();
        assertTrue(editar.contains("value=\"Contrato social\""));
        assertTrue(editar.contains("action=\"/documentos/" + id + "/editar\""));
        var atualizado = post("/documentos/" + id + "/editar", "nome=Contrato+editado" + campos.replace("Pendente", "Aprovado"));
        assertRedirect(atualizado, "/documentos");
        assertFlash(atualizado, "Documento atualizado com sucesso!");
        Documento salvo = documentos.findById(id).orElseThrow();
        assertEquals("Aprovado", salvo.getStatus());
        assertEquals(documento.getCriadoEm().toInstant(), salvo.getCriadoEm().toInstant());
        assertEquals(1, documentos.count());
        var edicaoVazia = post("/documentos/" + id + "/editar", "nome=+++" + campos);
        assertEquals(200, edicaoVazia.statusCode());
        assertTrue(edicaoVazia.body().contains("Informe o nome do documento."));
        assertTrue(edicaoVazia.body().contains("action=\"/documentos/" + id + "/editar\""));
        assertTrue(edicaoVazia.body().contains("value=\"" + categoriaId + "\" selected=\"selected\""));
        assertEquals("Contrato editado", documentos.findById(id).orElseThrow().getNome());
        var invalido = post("/documentos/" + id + "/editar", "nome=Invalido&categoriaId=999999&startupId="
                + startupId + "&responsavelId=" + usuarioId + "&status=Pendente");
        assertEquals(200, invalido.statusCode());
        assertTrue(invalido.body().contains("Selecione uma categoria existente"));
        assertEquals("Contrato editado", documentos.findById(id).orElseThrow().getNome());
        assertEquals(200, post("/documentos", "nome=Invalido" + campos.replace("Pendente", "Desconhecido")).statusCode());
        assertEquals(200, post("/documentos", "nome=Invalido" + campos.replace("categoriaId=" + categoriaId, "categoriaId=abc")).statusCode());
        assertEquals(1, documentos.count());
        assertEquals(405, get("/documentos/" + id + "/excluir").statusCode());
        Comentario comentario = new Comentario();
        comentario.setDocumento(salvo);
        comentario.setAutor(usuarios.findById(usuarioId).orElseThrow());
        comentario.setConteudo("Revisar");
        comentarios.saveAndFlush(comentario);
        var bloqueado = post("/documentos/" + id + "/excluir", "");
        assertRedirect(bloqueado, "/documentos");
        assertFlash(bloqueado, "Não é possível excluir");
        assertTrue(documentos.existsById(id));
        comentarios.deleteById(comentario.getId());
        var excluido = post("/documentos/" + id + "/excluir", "");
        assertRedirect(excluido, "/documentos");
        assertFlash(excluido, "Documento excluído com sucesso!");
        assertFalse(documentos.existsById(id));
        for (String rota : new String[]{"/documentos/999999", "/documentos/999999/editar"}) {
            var ausente = get(rota);
            assertRedirect(ausente, "/documentos");
            assertFlash(ausente, "Documento não encontrado.");
        }
        for (String acao : new String[]{"editar", "excluir"}) {
            var ausente = post("/documentos/999999/" + acao, "nome=Ausente");
            assertRedirect(ausente, "/documentos");
            assertFlash(ausente, "Documento não encontrado.");
        }
    }

    private void assertFlash(HttpResponse<String> response, String mensagem) throws Exception {
        String cookie = response.headers().firstValue("set-cookie").orElseThrow().split(";", 2)[0];
        var pagina = client.send(HttpRequest.newBuilder(uri("/documentos"))
                .header("Cookie", cookie).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, pagina.statusCode());
        assertTrue(pagina.body().contains(mensagem));
    }

    private void assertRedirect(HttpResponse<String> response, String path) {
        assertEquals(302, response.statusCode(), response.body());
        URI destino = URI.create(response.headers().firstValue("location").orElseThrow());
        assertEquals(path, destino.getPath().split(";", 2)[0]);
    }

    private URI uri(String path) { return URI.create("http://localhost:" + port + path); }

    private HttpResponse<String> get(String path) throws Exception {
        var response = client.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertNotEquals(500, response.statusCode(), response.body());
        return response;
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
}
