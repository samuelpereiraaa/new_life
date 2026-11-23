package com.example.demo.new_life.service;

import com.example.demo.new_life.model.UsuarioForm;
import com.example.demo.new_life.repository.UsuarioRepository;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class PrimeiroAcesso implements ApplicationRunner {
    private final UsuarioRepository usuarios;
    private final CadastrosService cadastros;
    private final Validator validator;
    private final String email;
    private final String senha;

    public PrimeiroAcesso(UsuarioRepository usuarios, CadastrosService cadastros, Validator validator,
            @Value("${NEWLIFE_ADMIN_EMAIL:}") String email,
            @Value("${NEWLIFE_ADMIN_PASSWORD:}") String senha) {
        this.usuarios = usuarios;
        this.cadastros = cadastros;
        this.validator = validator;
        this.email = email.strip();
        this.senha = senha;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isEmpty() || senha.isEmpty() || usuarios.findByEmailIgnoreCase(email).isPresent()) return;
        var form = new UsuarioForm();
        form.setNome("Administrador");
        form.setEmail(email);
        form.setSenha(senha);
        if (!validator.validate(form).isEmpty()) {
            throw new IllegalArgumentException("Confira o e-mail e a senha inicial do administrador.");
        }
        cadastros.cadastrarUsuario(form);
    }
}
