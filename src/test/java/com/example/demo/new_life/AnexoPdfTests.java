package com.example.demo.new_life;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AnexoPdfTests {
    @Autowired WebApplicationContext contexto;
    @Autowired JdbcTemplate jdbc;

    @Test
    void cadastraPdfEBaixaMesmoConteudo() throws Exception {
        jdbc.update("insert into startup(id,nome) values(71001,'Startup PDF')");
        jdbc.update("insert into categoria(id,nome) values(71001,'Categoria PDF')");
        jdbc.update("insert into usuario(id,nome,email,senha_hash) values(71001,'Ana','pdf@example.org','hash')");
        jdbc.update("insert into startup_usuario values(71001,71001)");
        var mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
        byte[] bytes = "%PDF-1.4\nconteudo\n%%EOF".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        var pdf = new MockMultipartFile("arquivo", "teste.pdf", "application/pdf", bytes);
        mvc.perform(multipart("/documentos").file(pdf).param("nome","Teste PDF")
                .param("categoriaId","71001").param("startupId","71001")
                .param("responsavelId","71001").param("status","Pendente"))
                .andExpect(redirectedUrl("/documentos"));
        Long id = jdbc.queryForObject("select id from documento where nome_pdf='teste.pdf'", Long.class);
        mvc.perform(get("/documentos/{id}/arquivo", id)).andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf")).andExpect(content().bytes(bytes));
        var novo = new MockMultipartFile("arquivo", "novo.pdf", "application/pdf", "%PDF-novo".getBytes());
        mvc.perform(multipart("/documentos/{id}/editar", id).file(novo).param("nome", "Teste PDF")
                .param("categoriaId", "71001").param("startupId", "71001")
                .param("responsavelId", "71001").param("status", "Aprovado"))
                .andExpect(redirectedUrl("/documentos"));
        mvc.perform(post("/documentos/{id}/comentarios", id).param("autorId", "71001")
                .param("texto", "Revisado")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/documentos/{id}", id)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Revisado")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Pendente → Aprovado")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("teste.pdf")));
        mvc.perform(get("/documentos/{id}/versoes/1/arquivo", id)).andExpect(status().isOk())
                .andExpect(content().bytes(bytes));
        mvc.perform(get("/documentos/{id}/arquivo", id)).andExpect(status().isOk())
                .andExpect(content().bytes(novo.getBytes()));
    }
}
