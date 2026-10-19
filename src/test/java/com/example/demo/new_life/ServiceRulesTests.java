package com.example.demo.new_life;

import com.example.demo.new_life.entity.*;
import com.example.demo.new_life.model.*;
import com.example.demo.new_life.repository.*;
import com.example.demo.new_life.service.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceRulesTests {

    @Test
    void responsavelSemVinculoNaoAlteraDocumentoExistente() {
        var documentos = mock(DocumentoRepository.class);
        var categorias = mock(CategoriaRepository.class);
        var vinculos = mock(StartupUsuarioRepository.class);
        var service = new DocumentoService(documentos, categorias, vinculos);
        Documento existente = new Documento();
        existente.setNome("Original");
        when(documentos.findById(1L)).thenReturn(Optional.of(existente));
        when(categorias.findById(2L)).thenReturn(Optional.of(new Categoria()));
        when(vinculos.findById(new StartupUsuarioId(3L, 4L))).thenReturn(Optional.empty());
        DocumentoForm form = new DocumentoForm();
        form.setNome("Alterado");
        form.setCategoriaId(2L);
        form.setStartupId(3L);
        form.setResponsavelId(4L);
        var erro = assertThrows(RegraNegocioException.class, () -> service.atualizar(1L, form));
        assertTrue(erro.getErros().containsKey("responsavelId"));
        assertEquals("Original", existente.getNome());
        verify(documentos, never()).saveAndFlush(any());
    }

    @Test
    void atualizacaoDeIdInexistenteNaoCriaDocumento() {
        var documentos = mock(DocumentoRepository.class);
        var service = new DocumentoService(documentos, mock(CategoriaRepository.class), mock(StartupUsuarioRepository.class));
        when(documentos.findById(999L)).thenReturn(Optional.empty());
        assertNull(service.atualizar(999L, new DocumentoForm()));
        verify(documentos, never()).saveAndFlush(any());
    }

    @Test
    void usuarioTemEmailNormalizadoESenhaEmHash() {
        var usuarios = mock(UsuarioRepository.class);
        var service = new CadastrosService(mock(StartupRepository.class), usuarios,
                mock(CategoriaRepository.class), mock(StartupUsuarioRepository.class));
        UsuarioForm form = new UsuarioForm();
        form.setNome(" Ana ");
        form.setEmail("ANA@TESTE.LOCAL");
        form.setSenha("SenhaTeste123");
        service.cadastrarUsuario(form);
        var captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).saveAndFlush(captor.capture());
        assertEquals("Ana", captor.getValue().getNome());
        assertEquals("ana@teste.local", captor.getValue().getEmail());
        assertTrue(new BCryptPasswordEncoder().matches("SenhaTeste123", captor.getValue().getSenhaHash()));
    }

    @Test
    void senhaMultibyteAcimaDoLimiteNaoEGravada() {
        var usuarios = mock(UsuarioRepository.class);
        var service = new CadastrosService(mock(StartupRepository.class), usuarios,
                mock(CategoriaRepository.class), mock(StartupUsuarioRepository.class));
        UsuarioForm form = new UsuarioForm();
        form.setSenha("á".repeat(37));
        var erro = assertThrows(RegraNegocioException.class, () -> service.cadastrarUsuario(form));
        assertTrue(erro.getErros().containsKey("senha"));
        verifyNoInteractions(usuarios);
    }
}
