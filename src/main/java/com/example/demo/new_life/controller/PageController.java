package com.example.demo.new_life.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
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

    private final GeminiService geminiService;
    private final DocumentoService documentoService;
    private final CadastrosService cadastrosService;

    @GetMapping({"/", "/inicio"})
    public String inicio(Model model) {
        model.addAttribute("totalDocumentos", documentoService.contarDocumentos());
        model.addAttribute("totalStartups", cadastrosService.contarStartups());
        model.addAttribute("totalEmAnalise", documentoService.contarEmAnalise());
        return "index";
    }

    @GetMapping("/documentos")
    public String documentos(@RequestParam(name = "termo", defaultValue = "") String busca,
            @RequestParam(defaultValue = "") String status, Model model) {
        model.addAttribute("status", status);
        model.addAttribute("documentos", documentoService.listar(busca, status));
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
        prepararDetalhes(documento, model);
        return "detalhes";
    }

    @PostMapping("/documentos/{id}/analise")
    public String analisar(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        Documento documento = documentoService.buscarPorId(id);
        if (documento == null) return naoEncontrado(redirect);
        prepararDetalhes(documento, model);
        try {
            var anexo = documentoService.buscarAnexo(id).orElseThrow(() ->
                    new RegraNegocioException("", "Anexe um PDF para solicitar a análise."));
            model.addAttribute("analise", geminiService.analisar(anexo.getConteudo()));
        } catch (RegraNegocioException ex) {
            model.addAttribute("erroAnalise", ex.getMessage());
        }
        return "detalhes";
    }

    @GetMapping("/documentos/{id}/arquivo")
    public ResponseEntity<byte[]> baixar(@PathVariable Long id) {
        Documento documento = documentoService.buscarPorId(id);
        if (documento == null || documento.getNomePdf() == null) {
            return ResponseEntity.notFound().build();
        }
        return documentoService.buscarAnexo(id).map(anexo -> ResponseEntity.ok()
                .header("Content-Disposition", ContentDisposition.attachment()
                        .filename(documento.getNomePdf(), StandardCharsets.UTF_8).build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(anexo.getConteudo())).orElseGet(() -> ResponseEntity.notFound().build());
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
        documento.getTags().forEach(tag -> formulario.getTagIds().add(tag.getId()));
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

    private void prepararDetalhes(Documento documento, Model model) {
        Long id = documento.getId();
        model.addAttribute("documento", documento);
        model.addAttribute("comentarios", documentoService.comentarios(id));
        model.addAttribute("usuarios", cadastrosService.listarUsuarios());
        model.addAttribute("historico", documentoService.historico(id));
        model.addAttribute("versoes", documentoService.versoes(id));
    }

    @PostMapping("/documentos/{id}/comentarios")
    public String comentar(@PathVariable Long id, @RequestParam(required = false) Long autorId,
            @RequestParam(defaultValue = "") String texto, RedirectAttributes redirect) {
        try {
            if (!documentoService.comentar(id, autorId, texto)) return naoEncontrado(redirect);
        } catch (RegraNegocioException ex) {
            redirect.addFlashAttribute("erroComentario", ex.getMessage());
            redirect.addFlashAttribute("texto", texto);
            redirect.addFlashAttribute("autorId", autorId);
        }
        return "redirect:/documentos/" + id + "#comentarios";
    }

    @GetMapping("/documentos/{id}/versoes/{numero}/arquivo")
    public ResponseEntity<byte[]> baixarVersao(@PathVariable Long id, @PathVariable Integer numero) {
        return documentoService.buscarVersao(id, numero).filter(v -> v.getConteudo() != null)
                .map(v -> ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                        .header("Content-Disposition", ContentDisposition.attachment()
                                .filename(v.getNomeArquivo(), StandardCharsets.UTF_8).build().toString())
                        .body(v.getConteudo())).orElseGet(() -> ResponseEntity.notFound().build());
    }

    private void prepararFormulario(Model model) {
        model.addAttribute("tags", cadastrosService.listarTags());
        model.addAttribute("categorias", cadastrosService.listarCategorias());
        model.addAttribute("startups", cadastrosService.listarStartups());
        model.addAttribute("usuarios", cadastrosService.listarUsuarios());
    }

    private String naoEncontrado(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erro", "Documento não encontrado.");
        return "redirect:/documentos";
    }
}
