package com.example.demo.new_life.repository;

import com.example.demo.new_life.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    @org.springframework.data.jpa.repository.Query("select count(d) from Documento d where d.responsavel.id.startupId in (select v.id.startupId from StartupUsuario v where v.id.usuarioId = :ator) and (:status = '' or d.status = :status)")
    long contarVisiveis(Long ator, String status);

    @EntityGraph(attributePaths = {"tags", "categoria", "responsavel", "responsavel.startup", "responsavel.usuario"})
    @org.springframework.data.jpa.repository.Query("select d from Documento d where d.responsavel.id.startupId in (select v.id.startupId from StartupUsuario v where v.id.usuarioId = :ator) and (:status = '' or d.status = :status) and d.responsavel.id.startupId = coalesce(:startupId, d.responsavel.id.startupId)")
    List<Documento> listarVisiveis(Long ator, String status, Long startupId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<Documento> findDocumentoById(Long id);


    @Override
    @EntityGraph(attributePaths = {"tags", "categoria", "responsavel", "responsavel.startup", "responsavel.usuario"})
    List<Documento> findAll();

    @Override
    @EntityGraph(attributePaths = {"tags", "categoria", "responsavel", "responsavel.startup", "responsavel.usuario"})
    Optional<Documento> findById(Long id);
}

