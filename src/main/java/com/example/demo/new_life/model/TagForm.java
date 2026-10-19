package com.example.demo.new_life.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagForm {
    @NotBlank(message = "Informe o nome da tag.")
    @Size(max = 100, message = "O nome deve ter até 100 caracteres.")
    private String nome;
}
