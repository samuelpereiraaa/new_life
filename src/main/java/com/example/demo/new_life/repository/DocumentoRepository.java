package com.example.demo.new_life.repository;

import com.example.demo.new_life.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    @Override
    @EntityGraph(attributePaths = {"categoria", "responsavel", "responsavel.startup", "responsavel.usuario"})
    List<Documento> findAll();

    @Override
    @EntityGraph(attributePaths = {"categoria", "responsavel", "responsavel.startup", "responsavel.usuario"})
    Optional<Documento> findById(Long id);
}

