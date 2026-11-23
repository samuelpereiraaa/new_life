package com.example.demo.new_life.controller;

import com.example.demo.new_life.repository.UsuarioRepository;
import jakarta.servlet.http.*;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class ControleSessao implements WebMvcConfigurer, HandlerInterceptor {
    private final UsuarioRepository usuarios;
    private final String adminEmail;

    public ControleSessao(UsuarioRepository usuarios, @Value("${NEWLIFE_ADMIN_EMAIL:}") String adminEmail) {
        this.usuarios = usuarios;
        this.adminEmail = adminEmail.strip();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod metodo)
                || !metodo.getBeanType().getPackageName().equals(ControleSessao.class.getPackageName())) return true;
        boolean login = metodo.getBeanType() == SessaoController.class && java.util.Set.of("login", "entrar", "cadastro", "cadastrar").contains(metodo.getMethod().getName());
        var sessao = req.getSession(login);
        Long id = sessao == null ? null : (Long) sessao.getAttribute("usuarioId");
        var usuario = java.util.Optional.ofNullable(id).flatMap(usuarios::findById);
        res.setHeader("Cache-Control", "no-store");
        if (!login && usuario.isEmpty()) {
            res.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        String token = (String) sessao.getAttribute("csrf");
        if (token == null) {
            token = UUID.randomUUID().toString();
            sessao.setAttribute("csrf", token);
        }
        req.setAttribute("csrf", token);
        if ("POST".equals(req.getMethod()) && !token.equals(req.getParameter("_csrf"))) {
            res.sendError(403);
            return false;
        }
        boolean admin = usuario.map(u -> u.getEmail().equalsIgnoreCase(adminEmail)).orElse(false);
        req.setAttribute("admin", admin);
        if (metodo.getBeanType() == CadastrosController.class && "POST".equals(req.getMethod()) && !admin) {
            res.sendError(403);
            return false;
        }
        return true;
    }
}
