package com.example.demo.new_life.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "documento")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
        @JoinColumn(name = "startup_id", referencedColumnName = "startup_id", nullable = false),
        @JoinColumn(name = "responsavel_id", referencedColumnName = "usuario_id", nullable = false)
    })
    private StartupUsuario responsavel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToMany
    @JoinTable(name = "documento_tag",
            joinColumns = @JoinColumn(name = "documento_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @OrderBy("nome ASC")
    private Set<Tag> tags = new LinkedHashSet<>();

    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "Pendente";

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    protected void preencherData() {
        if (criadoEm == null) {
            criadoEm = OffsetDateTime.now();
        }
    }

    public Startup getStartup() {
        return responsavel == null ? null : responsavel.getStartup();
    }

    public String getStatusClass() {
        if ("Aprovado".equals(status)) return " done";
        if ("Pendente".equals(status)) return " pending";
        return " review";
    }
}
