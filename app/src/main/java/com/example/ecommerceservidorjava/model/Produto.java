package com.example.ecommerceservidorjava.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Produto implements Serializable {
    private String id;
    private String nome;
    private String descricao;
    private String precoCusto;
    private String precoVenda;
    private String lucro;
    private String desconto;

    private List<String> idsCategorias = new ArrayList<>();

    private String urlImagem0;
    private String urlImagem1;
    private String urlImagem2;
    private String quantidadeEtoque;
    private String quantidadeMinima;

    private String status;
    private String unidade;
    private String codigo;
    private String observacao;

    public Produto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPrecoCusto() {
        return precoCusto;
    }

    public void setPrecoCusto(String precoCusto) {
        this.precoCusto = precoCusto;
    }

    public String getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(String precoVenda) {
        this.precoVenda = precoVenda;
    }

    public String getLucro() {
        return lucro;
    }

    public void setLucro(String lucro) {
        this.lucro = lucro;
    }

    public String getDesconto() {
        return desconto;
    }

    public void setDesconto(String desconto) {
        this.desconto = desconto;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public List<String> getIdsCategorias() {
        return idsCategorias;
    }

    public void setIdsCategorias(List<String> idsCategorias) {
        this.idsCategorias = idsCategorias;
    }

    public String getUrlImagem0() {
        return urlImagem0;
    }

    public void setUrlImagem0(String urlImagem0) {
        this.urlImagem0 = urlImagem0;
    }

    public String getUrlImagem1() {
        return urlImagem1;
    }

    public void setUrlImagem1(String urlImagem1) {
        this.urlImagem1 = urlImagem1;
    }

    public String getUrlImagem2() {
        return urlImagem2;
    }

    public void setUrlImagem2(String urlImagem2) {
        this.urlImagem2 = urlImagem2;
    }

    public String getQuantidadeEtoque() {
        return quantidadeEtoque;
    }

    public void setQuantidadeEtoque(String quantidadeEtoque) {
        this.quantidadeEtoque = quantidadeEtoque;
    }

    public String getQuantidadeMinima() {
        return quantidadeMinima;
    }

    public void setQuantidadeMinima(String quantidadeMinima) {
        this.quantidadeMinima = quantidadeMinima;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
