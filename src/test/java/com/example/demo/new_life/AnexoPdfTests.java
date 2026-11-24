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
    @jakarta.persistence.PersistenceContext jakarta.persistence.EntityManager entityManager;
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    com.example.demo.new_life.service.GeminiService gemini;

    @Test
    void criaContaEEntraSemVinculo() throws Exception {
        var mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("apresentacao"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Criar minha conta")));
        mvc.perform(get("/inicio")).andExpect(redirectedUrl("/login"));
        var sessao = (org.springframework.mock.web.MockHttpSession) mvc.perform(get("/login"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/criar-conta")))
                .andReturn().getRequest().getSession();
        String csrf = (String) sessao.getAttribute("csrf");
        mvc.perform(get("/criar-conta").session(sessao)).andExpect(status().isOk());
        var autenticada = (org.springframework.mock.web.MockHttpSession) mvc.perform(post("/criar-conta").session(sessao)
                .param("_csrf", csrf).param("nome", "Nova conta").param("email", "nova@example.org").param("senha", "MinhaSenha123"))
                .andExpect(redirectedUrl("/inicio")).andReturn().getRequest().getSession();
        mvc.perform(get("/startups").session(autenticada)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Peça ao administrador")));
    }

    @Test
    void cadastraPdfEBaixaMesmoConteudo() throws Exception {
        jdbc.update("insert into startup(id,nome) values(71001,'Startup PDF')");
        jdbc.update("insert into categoria(id,nome) values(71001,'Categoria PDF')");
        jdbc.update("insert into usuario(id,nome,email,senha_hash) values(71001,'Ana','pdf@example.org','hash')");
        jdbc.update("insert into startup_usuario values(71001,71001)");
        var publico = MockMvcBuilders.webAppContextSetup(contexto).build();
        publico.perform(get("/documentos")).andExpect(redirectedUrl("/login"));
        var anonima = (org.springframework.mock.web.MockHttpSession) publico.perform(get("/login"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession();
        jdbc.update("update usuario set senha_hash=? where id=71001",
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("SenhaTeste123"));
        var sessao = (org.springframework.mock.web.MockHttpSession) publico.perform(post("/login").session(anonima)
                .param("_csrf", (String) anonima.getAttribute("csrf")).param("email","pdf@example.org")
                .param("senha","SenhaTeste123")).andExpect(redirectedUrl("/inicio")).andReturn().getRequest().getSession();
        publico.perform(get("/inicio").session(sessao)).andExpect(status().isOk());
        var mvc = MockMvcBuilders.webAppContextSetup(contexto).defaultRequest(get("/inicio").session(sessao)
                .param("_csrf",(String) sessao.getAttribute("csrf"))).build();
        byte[] bytes = "%PDF-1.4\nconteudo\n%%EOF".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        var pdf = new MockMultipartFile("arquivo", "teste.pdf", "application/pdf", bytes);
        mvc.perform(multipart("/documentos").file(pdf).param("nome","Teste PDF")
                .param("categoriaId","71001").param("startupId","71001")
                .param("responsavelId","71001").param("status","Pendente"))
                .andExpect(redirectedUrl("/documentos"));
        Long id = jdbc.queryForObject("select id from documento where nome_pdf='teste.pdf'", Long.class);
        mvc.perform(get("/documentos/{id}/arquivo", id)).andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf")).andExpect(content().bytes(bytes));
        mvc.perform(get("/inicio")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/startups")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<strong>0</strong><span>Documentos em análise")));
        mvc.perform(get("/documentos").param("status", "Em análise")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Teste PDF"))));
        jdbc.update("insert into usuario(id,nome,email,senha_hash) values(71003,'Bruna','bruna@example.org','hash')");
        jdbc.update("insert into startup_usuario values(71001,71003)");
        var colega = new org.springframework.mock.web.MockHttpSession();
        colega.setAttribute("usuarioId",71003L);
        publico.perform(get("/inicio").session(colega)).andExpect(status().isOk());
        jdbc.update("insert into startup(id,nome) values(71004,'Segunda startup')");
        jdbc.update("insert into startup_usuario values(71004,71001)");
        jdbc.update("insert into documento(startup_id,categoria_id,responsavel_id,nome) values(71004,71001,71001,'Segundo documento')");
        mvc.perform(get("/startups")).andExpect(content().string(org.hamcrest.Matchers.containsString("/documentos?startupId=71004")));
        mvc.perform(get("/documentos").param("startupId","71004").param("status","Pendente").param("termo","documento"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Segundo documento")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Teste PDF"))));
        publico.perform(get("/documentos").session(colega).param("startupId","71004")).andExpect(status().isForbidden());
        entityManager.flush();
        org.mockito.Mockito.when(gemini.analisar(org.mockito.ArgumentMatchers.any(byte[].class))).thenReturn("Parecer da primeira versão");
        mvc.perform(post("/documentos/{id}/analise", id)).andExpect(redirectedUrl("/documentos/" + id + "#analises"))
                .andExpect(flash().attributeCount(0));
        var novo = new MockMultipartFile("arquivo", "novo.pdf", "application/pdf", "%PDF-novo".getBytes());
        publico.perform(multipart("/documentos/{id}/editar", id).file(novo).session(colega)
                .param("_csrf",(String) colega.getAttribute("csrf")).param("nome", "Teste PDF")
                .param("categoriaId", "71001").param("startupId", "71001")
                .param("responsavelId", "71001").param("status", "Em análise"))
                .andExpect(redirectedUrl("/documentos"));
        mvc.perform(get("/inicio")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<strong>1</strong><span>Documentos em análise")));
        mvc.perform(get("/documentos").param("status", "Em análise").param("termo", "Teste"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Teste PDF")));
        mvc.perform(post("/documentos/{id}/comentarios", id).param("autorId", "99999")
                .param("texto", "Revisado")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/documentos/{id}", id)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Parecer da primeira versão")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("teste.pdf · Versão 1")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Revisado")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Pendente → Em análise")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("teste.pdf")));
        mvc.perform(get("/documentos/{id}/versoes/1/arquivo", id)).andExpect(status().isOk())
                .andExpect(content().bytes(bytes));
        mvc.perform(get("/documentos/{id}/arquivo", id)).andExpect(status().isOk())
                .andExpect(content().bytes(novo.getBytes()));
        org.junit.jupiter.api.Assertions.assertEquals(71001L, jdbc.queryForObject(
                "select autor_id from comentario where documento_id=?", Long.class, id));
        org.junit.jupiter.api.Assertions.assertEquals(71003L, jdbc.queryForObject(
                "select alterado_por from historico_status where documento_id=?", Long.class, id));
        org.junit.jupiter.api.Assertions.assertEquals(71001L, jdbc.queryForObject(
                "select enviado_por from documento_versao where documento_id=?", Long.class, id));
        org.junit.jupiter.api.Assertions.assertEquals(71003L, jdbc.queryForObject(
                "select enviado_por from anexo_pdf where documento_id=?", Long.class, id));
        jdbc.update("insert into usuario(id,nome,email,senha_hash) values(71002,'Outro','outro@example.org','hash')");
        var outra = new org.springframework.mock.web.MockHttpSession();
        outra.setAttribute("usuarioId",71002L);
        publico.perform(get("/documentos/{id}/arquivo", id).session(outra)).andExpect(status().isForbidden());
        publico.perform(get("/documentos/{id}/versoes/1/arquivo", id).session(outra)).andExpect(status().isForbidden());
        publico.perform(get("/documentos").session(outra)).andExpect(content().string(
                org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Teste PDF"))));
        String token = (String) outra.getAttribute("csrf");
        publico.perform(post("/documentos/{id}/excluir", id).session(outra).param("_csrf",token))
                .andExpect(status().isForbidden());
        publico.perform(post("/cadastros/vinculos").session(outra).param("_csrf",token)
                .param("startupId","71001").param("usuarioId","71002")).andExpect(status().isForbidden());
        publico.perform(post("/logout").session(outra)).andExpect(status().isForbidden());
        mvc.perform(post("/logout")).andExpect(redirectedUrl("/"));
    }
}
