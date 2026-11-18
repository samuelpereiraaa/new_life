package com.example.demo.new_life.repository;

import com.example.demo.new_life.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "autor")
    java.util.List<Comentario> findByDocumentoIdOrderByIdDesc(Long id);
}

