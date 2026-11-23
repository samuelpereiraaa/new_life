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

    private final AnaliseDocumentoService analises;
    private final DocumentoService documentoService;
    private final CadastrosService cadastrosService;

    private final jakarta.servlet.http.HttpServletRequest request;

    private Long ator() {
        return (Long) request.getSession(false).getAttribute("usuarioId");
    }

    @GetMapping({"/", "/inicio"})
    public String inicio(Model model) {
        model.addAttribute("totalDocumentos", documentoService.contarDocumentos(ator(), ""));
        model.addAttribute("totalStartups", cadastrosService.startupsDoUsuario(ator()).size());
        model.addAttribute("totalEmAnalise", documentoService.contarDocumentos(ator(), "Em análise"));
        return "index";
    }

    @GetMapping("/startups")
    public String minhasStartups(Model model) {
        model.addAttribute("startups", cadastrosService.startupsDoUsuario(ator()));
        return "minhas-startups";
    }

    @GetMapping("/documentos")
    public String documentos(@RequestParam(name = "termo", defaultValue = "") String busca,
            @RequestParam(defaultValue = "") String status, @RequestParam(required = false) Long startupId, Model model) {
        model.addAttribute("status", status);
        model.addAttribute("documentos", documentoService.listar(busca, status, ator(), startupId));
        model.addAttribute("startupId", startupId);
        model.addAttribute("startups", cadastrosService.startupsDoUsuario(ator()));
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
        Documento documento = documentoService.buscarPorId(id, ator());
        if (documento == null) return naoEncontrado(redirectAttributes);
        prepararDetalhes(documento, model);
        return "detalhes";
    }

    @PostMapping("/documentos/{id}/analise")
    public String analisar(@PathVariable Long id, RedirectAttributes redirect) {
        Documento documento = documentoService.buscarPorId(id, ator());
        if (documento == null) return naoEncontrado(redirect);
        try {
            analises.analisar(id, ator());
        } catch (RegraNegocioException ex) {
            redirect.addFlashAttribute("erroAnalise", ex.getMessage());
        }
        return "redirect:/documentos/" + id + "#analises";
    }

    @GetMapping("/documentos/{id}/arquivo")
    public ResponseEntity<byte[]> baixar(@PathVariable Long id) {
        Documento documento = documentoService.buscarPorId(id, ator());
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
        Documento documento = documentoService.buscarPorId(id, ator());
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
        Documento documento = documentoService.buscarPorId(id, ator());
        if (documento == null) return naoEncontrado(redirectAttributes);
        model.addAttribute("documentoId", id);
        return salvar(id, formulario, result, model, redirectAttributes);
    }

    @PostMapping("/documentos/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (!documentoService.excluir(id, ator())) return naoEncontrado(redirectAttributes);
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
            Documento salvo = id == null ? documentoService.cadastrar(form, ator()) : documentoService.atualizar(id, form, ator());
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
        model.addAttribute("analises", analises.historico(id, ator()));
        model.addAttribute("autorPdf", documentoService.buscarAutorPdf(id));
        model.addAttribute("comentarios", documentoService.comentarios(id));
        model.addAttribute("historico", documentoService.historico(id));
        model.addAttribute("versoes", documentoService.versoes(id));
    }

    @PostMapping("/documentos/{id}/comentarios")
    public String comentar(@PathVariable Long id,
            @RequestParam(defaultValue = "") String texto, RedirectAttributes redirect) {
        try {
            if (!documentoService.comentar(id, ator(), texto)) return naoEncontrado(redirect);
        } catch (RegraNegocioException ex) {
            redirect.addFlashAttribute("erroComentario", ex.getMessage());
            redirect.addFlashAttribute("texto", texto);
        }
        return "redirect:/documentos/" + id + "#comentarios";
    }

    @GetMapping("/documentos/{id}/versoes/{numero}/arquivo")
    public ResponseEntity<byte[]> baixarVersao(@PathVariable Long id, @PathVariable Integer numero) {
        if (documentoService.buscarPorId(id, ator()) == null) return ResponseEntity.notFound().build();
        return documentoService.buscarVersao(id, numero).filter(v -> v.getConteudo() != null)
                .map(v -> ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                        .header("Content-Disposition", ContentDisposition.attachment()
                                .filename(v.getNomeArquivo(), StandardCharsets.UTF_8).build().toString())
                        .body(v.getConteudo())).orElseGet(() -> ResponseEntity.notFound().build());
    }

    private void prepararFormulario(Model model) {
        model.addAttribute("tags", cadastrosService.listarTags());
        model.addAttribute("categorias", cadastrosService.listarCategorias());
        model.addAttribute("startups", cadastrosService.startupsDoUsuario(ator()));
        model.addAttribute("usuarios", cadastrosService.colegas(ator()));
    }

    private String naoEncontrado(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erro", "Documento não encontrado.");
        return "redirect:/documentos";
    }
}
