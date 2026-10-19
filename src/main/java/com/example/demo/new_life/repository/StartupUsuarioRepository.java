package com.example.demo.new_life.repository;

import com.example.demo.new_life.entity.StartupUsuario;
import com.example.demo.new_life.entity.StartupUsuarioId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;

public interface StartupUsuarioRepository extends JpaRepository<StartupUsuario, StartupUsuarioId> {

    @Override
    @EntityGraph(attributePaths = {"startup", "usuario"})
    List<StartupUsuario> findAll();

    @Override
    @EntityGraph(attributePaths = {"startup", "usuario"})
    Optional<StartupUsuario> findById(StartupUsuarioId id);
}

