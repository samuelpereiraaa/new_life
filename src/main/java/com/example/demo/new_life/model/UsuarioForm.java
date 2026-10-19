package com.example.demo.new_life.model;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioForm {
    @NotBlank(message = "Informe o nome do usuário.")
    @Size(max = 150, message = "O nome deve ter até 150 caracteres.")
    private String nome;

    @NotBlank(message = "Informe o e-mail.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 254, message = "O e-mail deve ter até 254 caracteres.")
    private String email;

    @NotBlank(message = "Informe a senha.")
    @Size(max = 72, message = "A senha deve ter até 72 caracteres.")
    private String senha;
}

