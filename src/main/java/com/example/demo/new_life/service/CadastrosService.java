package com.example.demo.new_life.service;

import com.example.demo.new_life.entity.*;
import com.example.demo.new_life.model.*;
import com.example.demo.new_life.repository.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CadastrosService {
    private final StartupRepository startupRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final StartupUsuarioRepository vinculoRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public List<Startup> listarStartups() { return startupRepository.findAll(); }
    public List<Usuario> listarUsuarios() { return usuarioRepository.findAll(); }
    public List<Categoria> listarCategorias() { return categoriaRepository.findAll(); }
    public List<StartupUsuario> listarVinculos() { return vinculoRepository.findAll(); }

    @Transactional
    public void cadastrarStartup(StartupForm form) {
        Startup startup = new Startup();
        startup.setNome(form.getNome().trim());
        startup.setCnpj(form.getCnpj() == null || form.getCnpj().isBlank() ? null : form.getCnpj().trim());
        try { startupRepository.saveAndFlush(startup); }
        catch (DataIntegrityViolationException ex) {
            throw new RegraNegocioException("cnpj", "CNPJ já cadastrado.");
        }
    }

    @Transactional
    public void cadastrarUsuario(UsuarioForm form) {
        if (form.getSenha() == null || form.getSenha().isBlank()) {
            throw new RegraNegocioException("senha", "Informe a senha.");
        }
        if (form.getSenha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new RegraNegocioException("senha", "A senha deve ter até 72 bytes em UTF-8.");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(form.getNome().trim());
        usuario.setEmail(form.getEmail().trim().toLowerCase(Locale.ROOT));
        usuario.setSenhaHash(encoder.encode(form.getSenha()));
        try { usuarioRepository.saveAndFlush(usuario); }
        catch (DataIntegrityViolationException ex) {
            throw new RegraNegocioException("email", "E-mail já cadastrado.");
        }
    }

    @Transactional
    public void cadastrarCategoria(CategoriaForm form) {
        Categoria categoria = new Categoria();
        categoria.setNome(form.getNome().trim());
        try { categoriaRepository.saveAndFlush(categoria); }
        catch (DataIntegrityViolationException ex) {
            throw new RegraNegocioException("nome", "Categoria já cadastrada.");
        }
    }

    @Transactional
    public void vincularUsuario(VinculoForm form) {
        var startup = form.getStartupId() == null ? null : startupRepository.findById(form.getStartupId()).orElse(null);
        var usuario = form.getUsuarioId() == null ? null : usuarioRepository.findById(form.getUsuarioId()).orElse(null);
        Map<String, String> erros = new LinkedHashMap<>();
        if (startup == null) erros.put("startupId", "Selecione uma startup existente.");
        if (usuario == null) erros.put("usuarioId", "Selecione um usuário existente.");
        if (!erros.isEmpty()) throw new RegraNegocioException(erros);
        StartupUsuarioId id = new StartupUsuarioId(form.getStartupId(), form.getUsuarioId());
        if (vinculoRepository.existsById(id)) {
            throw new RegraNegocioException("usuarioId", "Este vínculo já existe.");
        }
        StartupUsuario vinculo = new StartupUsuario();
        vinculo.setStartup(startup);
        vinculo.setUsuario(usuario);
        try { vinculoRepository.saveAndFlush(vinculo); }
        catch (DataIntegrityViolationException ex) {
            throw new RegraNegocioException("usuarioId", "Não foi possível criar o vínculo. Confira os cadastros.");
        }
    }
}

