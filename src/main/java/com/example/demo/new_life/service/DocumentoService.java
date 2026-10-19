package com.example.demo.new_life.service;

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
        Documento documento = buscarPorId(id);
        if (documento == null) return null;
        return salvar(documento, form);
    }

    @Transactional
    public boolean excluir(Long id) {
        Documento documento = buscarPorId(id);
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
        documento.setStatus(form.getStatus());
        documento.getTags().clear();
        documento.getTags().addAll(tags);
        try {
            return documentoRepository.saveAndFlush(documento);
        } catch (DataIntegrityViolationException ex) {
            throw new RegraNegocioException("", "Não foi possível salvar. Confira se os cadastros vinculados ainda existem.");
        }
    }
}

