package com.example.demo.new_life.model;

public class Documento {

    private final Long id;
    private String nome;
    private String categoria;
    private String responsavel;
    private String status;

    public Documento(Long id, String nome, String categoria, String responsavel, String status) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.responsavel = responsavel;
        this.status = status;
    }

    public String getNome() {
        return nome;
    }

    public Long getId() {
        return id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public void setStatus(String status) {
        this.status = status;
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
