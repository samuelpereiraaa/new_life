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
        request.getSession().invalidate();
        request.getSession(true).setAttribute("usuarioId", usuario.get().getId());
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String sair(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/login";
    }
}
