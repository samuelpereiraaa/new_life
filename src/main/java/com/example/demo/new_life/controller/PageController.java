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
        documentos.add(new Documento(formulario.getNome(), formulario.getCategoria(),
                formulario.getResponsavel(), formulario.getStatus()));
        redirectAttributes.addFlashAttribute("mensagem", "Documento cadastrado com sucesso!");
        return "redirect:/documentos";
    }

    @GetMapping("/documentos/{indice}")
    public String detalhes(@PathVariable int indice, Model model) {
        if (indice < 0 || indice >= documentos.size()) {
            return "redirect:/documentos";
        }
        model.addAttribute("documento", documentos.get(indice));
        return "detalhes";
    }

    private List<Documento> dadosDeExemplo() {
        return List.of(
                new Documento("Fluxo de caixa 2025", "Financeiro", "Marina", "Aprovado"),
                new Documento("Projeção de receitas", "Financeiro", "Samuel", "Em análise"),
                new Documento("Orçamento anual", "Financeiro", "Marina", "Pendente"),
                new Documento("Contrato social", "Legal", "Rafael", "Aprovado"),
                new Documento("Termos de uso", "Legal", "Rafael", "Em análise"),
                new Documento("Política de privacidade", "Legal", "Joana", "Pendente"),
                new Documento("Plano de negócios", "Operacional", "Samuel", "Aprovado"),
                new Documento("Manual do processo", "Operacional", "Joana", "Em análise"),
                new Documento("Checklist de onboarding", "Operacional", "Marina", "Pendente"));
    }
}
