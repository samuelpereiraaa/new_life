package com.example.demo.new_life.controller;

import com.example.demo.new_life.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class SessaoController {
    private final UsuarioRepository usuarios;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private final com.example.demo.new_life.service.CadastrosService cadastros;

    @org.springframework.beans.factory.annotation.Value("${NEWLIFE_ADMIN_EMAIL:}")
    private String adminEmail;

    @GetMapping("/")
    public String apresentacao(HttpServletRequest request) {
        var sessao = request.getSession(false);
        return sessao != null && sessao.getAttribute("usuarioId") != null ? "redirect:/inicio" : "apresentacao";
    }

    @GetMapping("/criar-conta")
    public String cadastro(@ModelAttribute("conta") com.example.demo.new_life.model.UsuarioForm conta) {
        return "criar-conta";
    }

    @PostMapping("/criar-conta")
    public String cadastrar(@jakarta.validation.Valid @ModelAttribute("conta") com.example.demo.new_life.model.UsuarioForm conta,
            org.springframework.validation.BindingResult erros,
            HttpServletRequest request) {
        if (!erros.hasFieldErrors("email") && adminEmail.strip().equalsIgnoreCase(conta.getEmail().strip())) {
            erros.rejectValue("email", "reservado", "Este e-mail está reservado para a administração.");
        }
        if (!erros.hasErrors()) {
            try {
                return iniciarSessao(request, cadastros.cadastrarUsuario(conta).getId());
            } catch (com.example.demo.new_life.service.RegraNegocioException ex) {
                ex.getErros().forEach((campo, mensagem) -> erros.rejectValue(campo, "cadastro", mensagem));
            }
        }
        conta.setSenha(null);
        return "criar-conta";
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @PostMapping("/login")
    public String entrar(@RequestParam String email, @RequestParam String senha, HttpServletRequest request, Model model) {
        var usuario = usuarios.findByEmailIgnoreCase(email.strip());
        if (usuario.isEmpty() || senha.getBytes(StandardCharsets.UTF_8).length > 72
                || !encoder.matches(senha, usuario.get().getSenhaHash())) {
            model.addAttribute("erro", "E-mail ou senha inválidos.");
            return "login";
        }
        return iniciarSessao(request, usuario.get().getId());
    }

    private String iniciarSessao(HttpServletRequest request, Long id) {
        request.getSession().invalidate();
        request.getSession(true).setAttribute("usuarioId", id);
        return "redirect:/inicio";
    }

    @PostMapping("/logout")
    public String sair(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/";
    }
}
