package com.example.demo.new_life.controller;

import com.example.demo.new_life.model.*;
import com.example.demo.new_life.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cadastros")
@RequiredArgsConstructor
public class CadastrosController {
    private final CadastrosService cadastrosService;

    @ModelAttribute("startupForm")
    public StartupForm startupForm() { return new StartupForm(); }
    @ModelAttribute("usuarioForm")
    public UsuarioForm usuarioForm() { return new UsuarioForm(); }
    @ModelAttribute("categoriaForm")
    public CategoriaForm categoriaForm() { return new CategoriaForm(); }
    @ModelAttribute("vinculoForm")
    public VinculoForm vinculoForm() { return new VinculoForm(); }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("startups", cadastrosService.listarStartups());
        model.addAttribute("usuarios", cadastrosService.listarUsuarios());
        model.addAttribute("categorias", cadastrosService.listarCategorias());
        model.addAttribute("vinculos", cadastrosService.listarVinculos());
        return "cadastros";
    }

    @PostMapping("/startups")
    public String startup(@Valid @ModelAttribute("startupForm") StartupForm form,
            BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) return listar(model);
        try {
            cadastrosService.cadastrarStartup(form);
        } catch (RegraNegocioException ex) {
            ex.getErros().forEach((campo, mensagem) -> result.rejectValue(campo, "negocio", mensagem));
            return listar(model);
        }
        return sucesso(flash);
    }

    @PostMapping("/usuarios")
    public String usuario(@Valid @ModelAttribute("usuarioForm") UsuarioForm form,
            BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            form.setSenha(null);
            return listar(model);
        }
        try {
            cadastrosService.cadastrarUsuario(form);
        } catch (RegraNegocioException ex) {
            ex.getErros().forEach((campo, mensagem) -> result.rejectValue(campo, "negocio", mensagem));
            form.setSenha(null);
            return listar(model);
        }
        form.setSenha(null);
        return sucesso(flash);
    }

    @PostMapping("/categorias")
    public String categoria(@Valid @ModelAttribute("categoriaForm") CategoriaForm form,
            BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) return listar(model);
        try {
            cadastrosService.cadastrarCategoria(form);
        } catch (RegraNegocioException ex) {
            ex.getErros().forEach((campo, mensagem) -> result.rejectValue(campo, "negocio", mensagem));
            return listar(model);
        }
        return sucesso(flash);
    }

    @PostMapping("/vinculos")
    public String vinculo(@Valid @ModelAttribute("vinculoForm") VinculoForm form,
            BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) return listar(model);
        try {
            cadastrosService.vincularUsuario(form);
        } catch (RegraNegocioException ex) {
            ex.getErros().forEach((campo, mensagem) -> result.rejectValue(campo, "negocio", mensagem));
            return listar(model);
        }
        return sucesso(flash);
    }

    private String sucesso(RedirectAttributes flash) {
        flash.addFlashAttribute("mensagem", "Cadastro realizado com sucesso!");
        return "redirect:/cadastros";
    }
}
