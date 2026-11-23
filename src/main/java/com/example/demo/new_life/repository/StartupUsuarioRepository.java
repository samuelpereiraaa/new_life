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

    @org.springframework.data.jpa.repository.Query("select v.startup from StartupUsuario v where v.id.usuarioId = :ator order by v.startup.nome")
    List<com.example.demo.new_life.entity.Startup> startupsDoUsuario(Long ator);

    @org.springframework.data.jpa.repository.Query("select distinct v.usuario from StartupUsuario v where v.id.startupId in (select m.id.startupId from StartupUsuario m where m.id.usuarioId = :ator)")
    List<com.example.demo.new_life.entity.Usuario> colegas(Long ator);

    @Override
    @EntityGraph(attributePaths = {"startup", "usuario"})
    Optional<StartupUsuario> findById(StartupUsuarioId id);
}

