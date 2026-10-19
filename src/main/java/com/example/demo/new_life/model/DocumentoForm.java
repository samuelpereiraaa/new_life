package com.example.demo.new_life.model;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentoForm {
    @NotBlank(message = "Informe o nome do documento.")
    @Size(max = 255, message = "O nome deve ter até 255 caracteres.")
    private String nome;

    @NotNull(message = "Selecione uma categoria.")
    @Positive(message = "Selecione uma categoria válida.")
    private Long categoriaId;

    @NotNull(message = "Selecione uma startup.")
    @Positive(message = "Selecione uma startup válida.")
    private Long startupId;

    @NotNull(message = "Selecione um responsável.")
    @Positive(message = "Selecione um responsável válido.")
    private Long responsavelId;

    @NotBlank(message = "Selecione um status.")
    @Pattern(regexp = "Pendente|Em análise|Aprovado", message = "Selecione um status válido.")
    private String status = "Pendente";
}
