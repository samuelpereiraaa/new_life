package com.example.demo.new_life.model;

public class Documento {

    private final String nome;
    private final String categoria;
    private final String responsavel;
    private final String status;

    public Documento(String nome, String categoria, String responsavel, String status) {
        this.nome = nome;
        this.categoria = categoria;
        this.responsavel = responsavel;
        this.status = status;
    }

    public String getNome() {
        return nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusClass() {
        if ("Aprovado".equals(status)) {
            return " done";
        }
        if ("Pendente".equals(status)) {
            return " pending";
        }
        return " review";
    }
}
