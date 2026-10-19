package com.example.demo.new_life.controller;

import com.example.demo.new_life.entity.Documento;
import com.example.demo.new_life.entity.StartupUsuarioId;
import com.example.demo.new_life.model.DocumentoForm;
import com.example.demo.new_life.model.FiltroDocumento;
import com.example.demo.new_life.repository.*;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final DocumentoRepository documentoRepository;
    private final CategoriaRepository categoriaRepository;
    private final StartupRepository startupRepository;
    private final UsuarioRepository usuarioRepository;
    private final StartupUsuarioRepository vinculoRepository;

    @GetMapping({"/", "/inicio"})
    public String inicio() {
        return "index";
    }

    @GetMapping("/documentos")
    public String documentos(@RequestParam(name = "termo", defaultValue = "") String busca, Model model) {
        String termo = busca.toLowerCase(Locale.ROOT).trim();
        List<Documento> documentos = documentoRepository.findAll().stream()
                .filter(d -> d.getNome().toLowerCase(Locale.ROOT).contains(termo)
                        || d.getCategoria().getNome().toLowerCase(Locale.ROOT).contains(termo)
                        || d.getResponsavel().getUsuario().getNome().toLowerCase(Locale.ROOT).contains(termo))
                .sorted(Comparator.comparing(Documento::getId)).toList();
        model.addAttribute("documentos", documentos);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("filtro", new FiltroDocumento(busca));
        return "documentos";
    }

    @GetMapping("/documentos/novo")
    public String novoDocumento(Model model) {
        model.addAttribute("documentoForm", new DocumentoForm());
        prepararFormulario(model);
        return "cadastro";
    }

    @PostMapping("/documentos")
    public String cadastrarDocumento(@Valid @ModelAttribute("documentoForm") DocumentoForm formulario,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        return salvar(new Documento(), formulario, result, model, redirectAttributes, false);
    }

    @GetMapping("/documentos/{id}")
    public String detalhes(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Documento documento = documentoRepository.findById(id).orElse(null);
        if (documento == null) return naoEncontrado(redirectAttributes);
        model.addAttribute("documento", documento);
        return "detalhes";
    }

    @GetMapping("/documentos/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Documento documento = documentoRepository.findById(id).orElse(null);
        if (documento == null) return naoEncontrado(redirectAttributes);
        DocumentoForm formulario = new DocumentoForm();
        formulario.setNome(documento.getNome());
        formulario.setCategoriaId(documento.getCategoria().getId());
        formulario.setStartupId(documento.getResponsavel().getId().getStartupId());
        formulario.setResponsavelId(documento.getResponsavel().getId().getUsuarioId());
        formulario.setStatus(documento.getStatus());
        model.addAttribute("documentoForm", formulario);
        model.addAttribute("documentoId", id);
        prepararFormulario(model);
        return "cadastro";
    }

    @PostMapping("/documentos/{id}/editar")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("documentoForm") DocumentoForm formulario,
            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        Documento documento = documentoRepository.findById(id).orElse(null);
        if (documento == null) return naoEncontrado(redirectAttributes);
        model.addAttribute("documentoId", id);
        return salvar(documento, formulario, result, model, redirectAttributes, true);
    }

    @PostMapping("/documentos/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Documento documento = documentoRepository.findById(id).orElse(null);
        if (documento == null) return naoEncontrado(redirectAttributes);
        try {
            documentoRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagem", "Documento excluído com sucesso!");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("erro",
                    "Não é possível excluir um documento com versões, comentários ou histórico vinculados.");
        }
        return "redirect:/documentos";
    }

    private String salvar(Documento documento, DocumentoForm form, BindingResult result,
            Model model, RedirectAttributes redirectAttributes, boolean edicao) {
        // A integridade das referencias precisa ser verificada antes de persistir.
        if (result.hasErrors()) {
            prepararFormulario(model);
            return "cadastro";
        }
        var categoria = categoriaRepository.findById(form.getCategoriaId()).orElse(null);
        var vinculo = vinculoRepository.findById(
                new StartupUsuarioId(form.getStartupId(), form.getResponsavelId())).orElse(null);
        if (categoria == null || vinculo == null) {
            if (categoria == null) result.rejectValue("categoriaId", "inexistente", "Selecione uma categoria existente.");
            if (vinculo == null) result.rejectValue("responsavelId", "inexistente", "Selecione um responsável vinculado à startup.");
            prepararFormulario(model);
            return "cadastro";
        }
        documento.setNome(form.getNome().trim());
        documento.setCategoria(categoria);
        documento.setResponsavel(vinculo);
        documento.setStatus(form.getStatus());
        try {
            documentoRepository.saveAndFlush(documento);
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("erro", "Não foi possível salvar. Confira se os cadastros vinculados ainda existem.");
            prepararFormulario(model);
            return "cadastro";
        }
        redirectAttributes.addFlashAttribute("mensagem", edicao
                ? "Documento atualizado com sucesso!" : "Documento cadastrado com sucesso!");
        return "redirect:/documentos";
    }

    private void prepararFormulario(Model model) {
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("startups", startupRepository.findAll());
        model.addAttribute("usuarios", usuarioRepository.findAll());
    }

    private String naoEncontrado(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erro", "Documento não encontrado.");
        return "redirect:/documentos";
    }
}
