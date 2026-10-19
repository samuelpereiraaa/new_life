package com.example.demo.new_life.model;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartupForm {
    @NotBlank(message = "Informe o nome da startup.")
    @Size(max = 150, message = "O nome deve ter até 150 caracteres.")
    private String nome;

    @Size(max = 14, message = "O CNPJ deve ter até 14 caracteres, sem pontuação.")
    private String cnpj;
}

