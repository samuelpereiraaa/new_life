package com.example.demo.new_life.repository;

import com.example.demo.new_life.entity.DocumentoVersao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoVersaoRepository extends JpaRepository<DocumentoVersao, Long> {
    interface Resumo {
        Integer getNumeroVersao();
        String getNomeArquivo();
        boolean getDisponivel();
    }

    @org.springframework.data.jpa.repository.Query("""
            select v.numeroVersao as numeroVersao, v.nomeArquivo as nomeArquivo,
                   case when v.conteudo is null then false else true end as disponivel
            from DocumentoVersao v where v.documento.id = :id order by v.numeroVersao desc
            """)
    java.util.List<Resumo> listar(Long id);

    java.util.Optional<DocumentoVersao> findByDocumentoIdAndNumeroVersao(Long id, Integer numero);
}

