package com.example.demo.new_life.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class StartupUsuarioId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "startup_id")
    private Long startupId;

    @Column(name = "usuario_id")
    private Long usuarioId;
}
