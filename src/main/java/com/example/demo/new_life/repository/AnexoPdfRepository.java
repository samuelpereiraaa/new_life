package com.example.demo.new_life.repository;

import com.example.demo.new_life.entity.AnexoPdf;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnexoPdfRepository extends JpaRepository<AnexoPdf, Long> {
    @org.springframework.data.jpa.repository.Query("select u.nome from AnexoPdf a join a.enviadoPor u where a.documentoId = :id")
    java.util.Optional<String> buscarAutor(Long id);
}
