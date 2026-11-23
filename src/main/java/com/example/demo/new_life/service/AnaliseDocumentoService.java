package com.example.demo.new_life.service;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnaliseDocumentoService {
    private final JdbcClient jdbc;
    private final GeminiService gemini;

    private record Arquivo(int versao, String nome, byte[] conteudo) {}
    public record Analise(int versao, String nome, String resultado, OffsetDateTime data) {}

    public void analisar(Long id, Long ator) {
        // Uma única leitura identifica os bytes e a versão antes da chamada externa.
        var arquivo = jdbc.sql("""
                select coalesce((select max(v.numero_versao) from documento_versao v
                                 where v.documento_id = d.id), 0) + 1 as versao, d.nome_pdf, a.conteudo
                from documento d join anexo_pdf a on a.documento_id = d.id
                join startup_usuario u on u.startup_id = d.startup_id and u.usuario_id = :ator
                where d.id = :id
                """).param("id", id).param("ator", ator)
                .query((rs, linha) -> new Arquivo(rs.getInt("versao"), rs.getString("nome_pdf"), rs.getBytes("conteudo")))
                .optional().orElseThrow(() -> new RegraNegocioException("", "Anexe um PDF para solicitar a análise."));
        String resultado = gemini.analisar(arquivo.conteudo());
        int gravados = jdbc.sql("""
                insert into analise_pdf(documento_id, numero_versao, nome_arquivo, resultado)
                select d.id, :versao, :nome, :resultado from documento d
                join startup_usuario u on u.startup_id = d.startup_id and u.usuario_id = :ator where d.id = :id
                """).param("id", id).param("ator", ator).param("versao", arquivo.versao())
                .param("nome", arquivo.nome()).param("resultado", resultado).update();
        if (gravados == 0) throw new RegraNegocioException("", "O documento não está mais disponível.");
    }

    public List<Analise> historico(Long id, Long ator) {
        return jdbc.sql("""
                select a.numero_versao, a.nome_arquivo, a.resultado, a.criado_em
                from analise_pdf a join documento d on d.id = a.documento_id
                join startup_usuario u on u.startup_id = d.startup_id and u.usuario_id = :ator
                where d.id = :id order by a.id desc
                """).param("id", id).param("ator", ator)
                .query((rs, linha) -> new Analise(rs.getInt("numero_versao"), rs.getString("nome_arquivo"),
                        rs.getString("resultado"), rs.getObject("criado_em", OffsetDateTime.class))).list();
    }
}
