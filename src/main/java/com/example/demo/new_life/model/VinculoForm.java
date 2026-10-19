package com.example.demo.new_life.model;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VinculoForm {
    @NotNull(message = "Selecione uma startup.")
    @Positive(message = "Selecione uma startup válida.")
    private Long startupId;

    @NotNull(message = "Selecione um usuário.")
    @Positive(message = "Selecione um usuário válido.")
    private Long usuarioId;
}

