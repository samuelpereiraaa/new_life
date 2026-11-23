package com.example.demo.new_life.repository;

import com.example.demo.new_life.entity.HistoricoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoStatusRepository extends JpaRepository<HistoricoStatus, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "alteradoPor")
    java.util.List<HistoricoStatus> findByDocumentoIdOrderByIdDesc(Long id);
}

