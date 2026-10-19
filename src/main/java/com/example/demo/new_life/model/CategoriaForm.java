package com.example.demo.new_life.model;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaForm {
    @NotBlank(message = "Informe o nome da categoria.")
    @Size(max = 80, message = "O nome deve ter até 80 caracteres.")
    private String nome;
}

