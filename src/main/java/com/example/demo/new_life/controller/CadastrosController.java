package com.example.demo.new_life.controller;

import com.example.demo.new_life.entity.*;
import com.example.demo.new_life.model.*;
import com.example.demo.new_life.repository.*;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cadastros")
@RequiredArgsConstructor
public class CadastrosController {
    private final StartupRepository startupRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final StartupUsuarioRepository vinculoRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @ModelAttribute("startupForm")
    public StartupForm startupForm() { return new StartupForm(); }
    @ModelAttribute("usuarioForm")
    public UsuarioForm usuarioForm() { return new UsuarioForm(); }
    @ModelAttribute("categoriaForm")
    public CategoriaForm categoriaForm() { return new CategoriaForm(); }
    @ModelAttribute("vinculoForm")
    public VinculoForm vinculoForm() { return new VinculoForm(); }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("startups", startupRepository.findAll());
        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("vinculos", vinculoRepository.findAll());
        return "cadastros";
    }

    @PostMapping("/startups")
    public String startup(@Valid @ModelAttribute("startupForm") StartupForm form,
            BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) return listar(model);
        Startup startup = new Startup();
        startup.setNome(form.getNome().trim());
        startup.setCnpj(form.getCnpj() == null || form.getCnpj().isBlank() ? null : form.getCnpj().trim());
        try { startupRepository.saveAndFlush(startup); }
        catch (DataIntegrityViolationException ex) {
            result.rejectValue("cnpj", "duplicado", "CNPJ já cadastrado.");
            return listar(model);
        }
        return sucesso(flash);
    }

    @PostMapping("/usuarios")
    public String usuario(@Valid @ModelAttribute("usuarioForm") UsuarioForm form,
            BindingResult result, Model model, RedirectAttributes flash) {
        // BCrypt limita bytes UTF-8, enquanto @Size conta caracteres.
        if (form.getSenha() != null && form.getSenha().getBytes(StandardCharsets.UTF_8).length > 72) {
            result.rejectValue("senha", "tamanhoBytes", "A senha deve ter até 72 bytes em UTF-8.");
        }
        if (result.hasErrors()) {
            form.setSenha(null);
            return listar(model);
        }
        Usuario usuario = new Usuario();
        usuario.setNome(form.getNome().trim());
        usuario.setEmail(form.getEmail().trim().toLowerCase(Locale.ROOT));
        usuario.setSenhaHash(encoder.encode(form.getSenha()));
        form.setSenha(null);
        try { usuarioRepository.saveAndFlush(usuario); }
        catch (DataIntegrityViolationException ex) {
            result.rejectValue("email", "duplicado", "E-mail já cadastrado.");
            return listar(model);
        }
        return sucesso(flash);
    }

    @PostMapping("/categorias")
    public String categoria(@Valid @ModelAttribute("categoriaForm") CategoriaForm form,
            BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) return listar(model);
        Categoria categoria = new Categoria();
        categoria.setNome(form.getNome().trim());
        try { categoriaRepository.saveAndFlush(categoria); }
        catch (DataIntegrityViolationException ex) {
            result.rejectValue("nome", "duplicado", "Categoria já cadastrada.");
            return listar(model);
        }
        return sucesso(flash);
    }

    @PostMapping("/vinculos")
    public String vinculo(@Valid @ModelAttribute("vinculoForm") VinculoForm form,
            BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) return listar(model);
        var startup = startupRepository.findById(form.getStartupId()).orElse(null);
        var usuario = usuarioRepository.findById(form.getUsuarioId()).orElse(null);
        if (startup == null) result.rejectValue("startupId", "inexistente", "Selecione uma startup existente.");
        if (usuario == null) result.rejectValue("usuarioId", "inexistente", "Selecione um usuário existente.");
        if (result.hasErrors()) return listar(model);
        StartupUsuarioId id = new StartupUsuarioId(form.getStartupId(), form.getUsuarioId());
        if (vinculoRepository.existsById(id)) {
            result.rejectValue("usuarioId", "duplicado", "Este vínculo já existe.");
            return listar(model);
        }
        StartupUsuario vinculo = new StartupUsuario();
        vinculo.setStartup(startup);
        vinculo.setUsuario(usuario);
        try { vinculoRepository.saveAndFlush(vinculo); }
        catch (DataIntegrityViolationException ex) {
            result.rejectValue("usuarioId", "integridade", "Não foi possível criar o vínculo. Confira os cadastros.");
            return listar(model);
        }
        return sucesso(flash);
    }

    private String sucesso(RedirectAttributes flash) {
        flash.addFlashAttribute("mensagem", "Cadastro realizado com sucesso!");
        return "redirect:/cadastros";
    }
}
