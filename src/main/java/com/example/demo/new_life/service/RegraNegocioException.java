package com.example.demo.new_life.service;

import java.util.Map;

public class RegraNegocioException extends RuntimeException {
    private final Map<String, String> erros;

    public RegraNegocioException(String campo, String mensagem) {
        this(Map.of(campo, mensagem));
    }

    public RegraNegocioException(Map<String, String> erros) {
        super(String.join(" ", erros.values()));
        this.erros = Map.copyOf(erros);
    }

    public Map<String, String> getErros() {
        return erros;
    }
}

