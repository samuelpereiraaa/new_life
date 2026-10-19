package com.example.demo.new_life.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "documento_versao", uniqueConstraints = @UniqueConstraint(columnNames = {"documento_id", "numero_versao"}))
public class DocumentoVersao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

    @Column(name = "numero_versao", nullable = false)
    private Integer numeroVersao;

    @Column(name = "nome_arquivo", nullable = false, length = 255)
    private String nomeArquivo;

    @Column(name = "localizacao_arquivo", nullable = false, columnDefinition = "TEXT")
    private String localizacaoArquivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enviado_por", nullable = false)
    private Usuario enviadoPor;

    @Column(name = "enviado_em", nullable = false)
    private OffsetDateTime enviadoEm;

    @PrePersist
    protected void preencherData() {
        if (enviadoEm == null) {
            enviadoEm = OffsetDateTime.now();
        }
    }
}
