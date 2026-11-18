package com.example.demo.new_life.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "anexo_pdf")
@Getter
@Setter
public class AnexoPdf {
    @Id
    private Long documentoId;

    @Column(nullable = false)
    private byte[] conteudo;
}
