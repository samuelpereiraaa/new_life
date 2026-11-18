package com.example.demo.new_life.service;

import com.example.demo.new_life.entity.*;
import org.springframework.util.StringUtils;
import java.io.IOException;
import com.example.demo.new_life.entity.Documento;
import com.example.demo.new_life.entity.StartupUsuarioId;
import com.example.demo.new_life.model.DocumentoForm;
import com.example.demo.new_life.repository.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentoService {
    private final DocumentoRepository documentoRepository;
    private final CategoriaRepository categoriaRepository;
    private final StartupUsuarioRepository vinculoRepository;
    private final TagRepository tagRepository;
    private final AnexoPdfRepository anexos;
    private final HistoricoStatusRepository historico;
    private final ComentarioRepository comentarios;
    private final UsuarioRepository usuarios;
    private final DocumentoVersaoRepository versoes;

    public List<HistoricoStatus> historico(Long id) {
        return historico.findByDocumentoIdOrderByIdDesc(id);
    }

    public List<Comentario> comentarios(Long id) {
        return comentarios.findByDocumentoIdOrderByIdDesc(id);
    }

    public List<DocumentoVersaoRepository.Resumo> versoes(Long id) {
        return versoes.listar(id);
    }

    public Optional<DocumentoVersao> buscarVersao(Long id, Integer numero) {
        return versoes.findByDocumentoIdAndNumeroVersao(id, numero);
    }

    @Transactional
    public boolean comentar(Long id, Long autorId, String texto) {
        var documento = documentoRepository.findDocumentoById(id).orElse(null);
        if (documento == null) return false;
        if (texto == null || texto.isBlank() || texto.length() > 4000)
            throw new RegraNegocioException("", "Escreva um comentário de até 4.000 caracteres.");
        var autor = Optional.ofNullable(autorId).flatMap(usuarios::findById)
                .orElseThrow(() -> new RegraNegocioException("", "Selecione um autor cadastrado."));
        var comentario = new Comentario();
        comentario.setDocumento(documento);
        comentario.setAutor(autor);
        comentario.setConteudo(texto.strip());
        comentarios.save(comentario);
        return true;
    }

    public Optional<AnexoPdf> buscarAnexo(Long id) {
        return anexos.findById(id);
    }

    public List<Documento> listar(String busca) {
        String termo = busca == null ? "" : busca.toLowerCase(Locale.ROOT).trim();
        return documentoRepository.findAll().stream()
                .filter(d -> d.getNome().toLowerCase(Locale.ROOT).contains(termo)
                        || d.getCategoria().getNome().toLowerCase(Locale.ROOT).contains(termo)
                        || d.getResponsavel().getUsuario().getNome().toLowerCase(Locale.ROOT).contains(termo))
                .sorted(Comparator.comparing(Documento::getId)).toList();
    }

    public Documento buscarPorId(Long id) {
        return documentoRepository.findById(id).orElse(null);
    }

    @Transactional
    public Documento cadastrar(DocumentoForm form) {
        return salvar(new Documento(), form);
    }

    @Transactional
    public Documento atualizar(Long id, DocumentoForm form) {
        Documento documento = documentoRepository.findDocumentoById(id).orElse(null);
        if (documento == null) return null;
        return salvar(documento, form);
    }

    @Transactional
    public boolean excluir(Long id) {
        Documento documento = documentoRepository.findDocumentoById(id).orElse(null);
        if (documento == null) return false;
        try {
            documentoRepository.delete(documento);
            documentoRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new RegraNegocioException("", "Não é possível excluir um documento com versões, comentários ou histórico vinculados.");
        }
        return true;
    }

    private Documento salvar(Documento documento, DocumentoForm form) {
        var categoria = form.getCategoriaId() == null ? null
                : categoriaRepository.findById(form.getCategoriaId()).orElse(null);
        var vinculo = form.getStartupId() == null || form.getResponsavelId() == null ? null
                : vinculoRepository.findById(new StartupUsuarioId(form.getStartupId(), form.getResponsavelId())).orElse(null);
        Map<String, String> erros = new LinkedHashMap<>();
        var tagIds = form.getTagIds() == null ? Collections.<Long>emptySet() : form.getTagIds();
        if (tagIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new RegraNegocioException("tagIds", "Selecione tags válidas.");
        }
        var tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) erros.put("tagIds", "Uma das tags selecionadas não existe mais.");
        if (categoria == null) erros.put("categoriaId", "Selecione uma categoria existente.");
        if (vinculo == null) erros.put("responsavelId", "Selecione um responsável vinculado à startup.");
        if (form.getStatus() == null || !List.of("Pendente", "Em análise", "Aprovado").contains(form.getStatus())) {
            erros.put("status", "Selecione um status válido.");
        }
        if (!erros.isEmpty()) throw new RegraNegocioException(erros);
        documento.setNome(form.getNome().trim());
        documento.setCategoria(categoria);
        documento.setResponsavel(vinculo);
        if (documento.getId() != null && !Objects.equals(documento.getStatus(), form.getStatus())) {
            var registro = new HistoricoStatus();
            registro.setDocumento(documento);
            registro.setStatusAnterior(documento.getStatus());
            registro.setStatusNovo(form.getStatus());
            historico.save(registro);
        }
        documento.setStatus(form.getStatus());
        documento.getTags().clear();
        documento.getTags().addAll(tags);
        try {
            var arquivo = form.getArquivo();
            if (arquivo == null || (arquivo.isEmpty() && "".equals(arquivo.getOriginalFilename()))) {
                return documentoRepository.saveAndFlush(documento);
            }
            String nome = StringUtils.getFilename(
                    StringUtils.cleanPath(Objects.toString(arquivo.getOriginalFilename(), "")));
            if (nome == null || nome.length() > 255 || nome.chars().anyMatch(Character::isISOControl)
                    || !nome.toLowerCase(Locale.ROOT).endsWith(".pdf") || arquivo.getSize() > 5 * 1024 * 1024) {
                throw new RegraNegocioException("arquivo", "Selecione um PDF de até 5 MB com um nome válido.");
            }
            byte[] bytes = arquivo.getBytes();
            if (bytes.length < 5 || bytes[0] != '%' || bytes[1] != 'P' || bytes[2] != 'D'
                    || bytes[3] != 'F' || bytes[4] != '-') {
                throw new RegraNegocioException("arquivo", "O arquivo selecionado precisa ser um PDF.");
            }
            if (documento.getId() != null) {
                anexos.findById(documento.getId()).ifPresent(atual -> {
                    var anteriores = versoes.listar(documento.getId());
                    int numero = anteriores.isEmpty() ? 1 : anteriores.get(0).getNumeroVersao() + 1;
                    var versao = new DocumentoVersao();
                    versao.setDocumento(documento);
                    versao.setNumeroVersao(numero);
                    versao.setNomeArquivo(documento.getNomePdf());
                    versao.setLocalizacaoArquivo("banco");
                    versao.setConteudo(atual.getConteudo());
                    versoes.save(versao);
                });
            }
            documento.setNomePdf(nome);
            Documento salvo = documentoRepository.saveAndFlush(documento);
            var anexo = new AnexoPdf();
            anexo.setDocumentoId(salvo.getId());
            anexo.setConteudo(bytes);
            anexos.save(anexo);
            return salvo;
        } catch (IOException ex) {
            throw new RegraNegocioException("arquivo", "Não foi possível ler o PDF. Selecione o arquivo novamente.");
        } catch (DataIntegrityViolationException ex) {
            throw new RegraNegocioException("", "Não foi possível salvar. Confira se os cadastros vinculados ainda existem.");
        }
    }
}

