package com.example.demo.new_life.controller;

import com.example.demo.new_life.model.Documento;
import com.example.demo.new_life.model.DocumentoForm;
import com.example.demo.new_life.model.FiltroDocumento;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Rotas das páginas públicas do protótipo.
 */
@Controller
public class PageController {

    // Lista em memória: substitui um banco de dados neste protótipo.
    private long proximoId = 1L;
    private final List<Documento> documentos = new ArrayList<>(dadosDeExemplo());

    @GetMapping({"/", "/inicio"})
    public String inicio() {
        return "index";
    }

    @GetMapping("/documentos")
    public String documentos(@RequestParam(name = "termo", defaultValue = "") String busca, Model model) {
        List<String> categorias = List.of("Financeiro", "Legal", "Operacional");
        List<Documento> documentosVisiveis = new ArrayList<>(documentos);

        if (!busca.isBlank()) {
            String termo = busca.toLowerCase();
            documentosVisiveis = documentos.stream()
                    .filter(documento -> documento.getNome().toLowerCase().contains(termo)
                            || documento.getCategoria().toLowerCase().contains(termo)
                            || documento.getResponsavel().toLowerCase().contains(termo))
                    .collect(Collectors.toList());
        }

        model.addAttribute("categorias", categorias);
        model.addAttribute("documentos", documentosVisiveis);
        model.addAttribute("filtro", new FiltroDocumento(busca));
        return "documentos";
    }

    @GetMapping("/documentos/novo")
    public String novoDocumento(Model model) {
        model.addAttribute("documentoForm", new DocumentoForm());
        return "cadastro";
    }

    @PostMapping("/documentos")
    public String cadastrarDocumento(@ModelAttribute("documentoForm") DocumentoForm formulario,
            RedirectAttributes redirectAttributes) {
        documentos.add(criarDocumento(formulario.getNome(), formulario.getCategoria(),
                formulario.getResponsavel(), formulario.getStatus()));
        redirectAttributes.addFlashAttribute("mensagem", "Documento cadastrado com sucesso!");
        return "redirect:/documentos";
    }

    @GetMapping("/documentos/{id}")
    public String detalhes(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Documento documento = buscarPorId(id);
        if (documento == null) {
            return naoEncontrado(redirectAttributes);
        }
        model.addAttribute("documento", documento);
        return "detalhes";
    }

    @GetMapping("/documentos/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Documento documento = buscarPorId(id);
        if (documento == null) {
            return naoEncontrado(redirectAttributes);
        }
        DocumentoForm formulario = new DocumentoForm();
        formulario.setNome(documento.getNome());
        formulario.setCategoria(documento.getCategoria());
        formulario.setResponsavel(documento.getResponsavel());
        formulario.setStatus(documento.getStatus());
        model.addAttribute("documentoForm", formulario);
        model.addAttribute("documentoId", id);
        return "cadastro";
    }

    @PostMapping("/documentos/{id}/editar")
    public String atualizar(@PathVariable Long id,
            @ModelAttribute("documentoForm") DocumentoForm formulario,
            RedirectAttributes redirectAttributes) {
        Documento documento = buscarPorId(id);
        if (documento == null) {
            return naoEncontrado(redirectAttributes);
        }
        documento.setNome(formulario.getNome());
        documento.setCategoria(formulario.getCategoria());
        documento.setResponsavel(formulario.getResponsavel());
        documento.setStatus(formulario.getStatus());
        redirectAttributes.addFlashAttribute("mensagem", "Documento atualizado com sucesso!");
        return "redirect:/documentos";
    }

    @PostMapping("/documentos/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Documento documento = buscarPorId(id);
        if (documento == null) {
            return naoEncontrado(redirectAttributes);
        }
        documentos.remove(documento);
        redirectAttributes.addFlashAttribute("mensagem", "Documento excluído com sucesso!");
        return "redirect:/documentos";
    }

    private Documento buscarPorId(Long id) {
        for (Documento documento : documentos) {
            if (documento.getId().equals(id)) {
                return documento;
            }
        }
        return null;
    }

    private String naoEncontrado(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erro", "Documento não encontrado.");
        return "redirect:/documentos";
    }

    private Documento criarDocumento(String nome, String categoria, String responsavel, String status) {
        return new Documento(proximoId++, nome, categoria, responsavel, status);
    }

    private List<Documento> dadosDeExemplo() {
        return List.of(
                criarDocumento("Fluxo de caixa 2025", "Financeiro", "Marina", "Aprovado"),
                criarDocumento("Projeção de receitas", "Financeiro", "Samuel", "Em análise"),
                criarDocumento("Orçamento anual", "Financeiro", "Marina", "Pendente"),
                criarDocumento("Contrato social", "Legal", "Rafael", "Aprovado"),
                criarDocumento("Termos de uso", "Legal", "Rafael", "Em análise"),
                criarDocumento("Política de privacidade", "Legal", "Joana", "Pendente"),
                criarDocumento("Plano de negócios", "Operacional", "Samuel", "Aprovado"),
                criarDocumento("Manual do processo", "Operacional", "Joana", "Em análise"),
                criarDocumento("Checklist de onboarding", "Operacional", "Marina", "Pendente"));
    }
}
