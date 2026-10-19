package com.example.demo.new_life.controller;

import com.example.demo.new_life.entity.Documento;
import com.example.demo.new_life.model.DocumentoForm;
import com.example.demo.new_life.model.FiltroDocumento;
import com.example.demo.new_life.service.*;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final DocumentoService documentoService;
    private final CadastrosService cadastrosService;

    @GetMapping({"/", "/inicio"})
    public String inicio() {
        return "index";
    }

    @GetMapping("/documentos")
    public String documentos(@RequestParam(name = "termo", defaultValue = "") String busca, Model model) {
        model.addAttribute("documentos", documentoService.listar(busca));
        model.addAttribute("categorias", cadastrosService.listarCategorias());
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
        return salvar(null, formulario, result, model, redirectAttributes);
    }

    @GetMapping("/documentos/{id}")
    public String detalhes(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Documento documento = documentoService.buscarPorId(id);
        if (documento == null) return naoEncontrado(redirectAttributes);
        model.addAttribute("documento", documento);
        return "detalhes";
    }

    @GetMapping("/documentos/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Documento documento = documentoService.buscarPorId(id);
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
        Documento documento = documentoService.buscarPorId(id);
        if (documento == null) return naoEncontrado(redirectAttributes);
        model.addAttribute("documentoId", id);
        return salvar(id, formulario, result, model, redirectAttributes);
    }

    @PostMapping("/documentos/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (!documentoService.excluir(id)) return naoEncontrado(redirectAttributes);
            redirectAttributes.addFlashAttribute("mensagem", "Documento excluído com sucesso!");
        } catch (RegraNegocioException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/documentos";
    }

    private String salvar(Long id, DocumentoForm form, BindingResult result,
            Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            prepararFormulario(model);
            return "cadastro";
        }
        try {
            Documento salvo = id == null ? documentoService.cadastrar(form) : documentoService.atualizar(id, form);
            if (salvo == null) return naoEncontrado(redirectAttributes);
        } catch (RegraNegocioException ex) {
            ex.getErros().forEach((campo, mensagem) -> {
                if (campo.isEmpty()) model.addAttribute("erro", mensagem);
                else result.rejectValue(campo, "negocio", mensagem);
            });
            prepararFormulario(model);
            return "cadastro";
        }
        redirectAttributes.addFlashAttribute("mensagem", id != null
                ? "Documento atualizado com sucesso!" : "Documento cadastrado com sucesso!");
        return "redirect:/documentos";
    }

    private void prepararFormulario(Model model) {
        model.addAttribute("categorias", cadastrosService.listarCategorias());
        model.addAttribute("startups", cadastrosService.listarStartups());
        model.addAttribute("usuarios", cadastrosService.listarUsuarios());
    }

    private String naoEncontrado(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erro", "Documento não encontrado.");
        return "redirect:/documentos";
    }
}
