package com.example.ecommerceservidorjava.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Produto implements Serializable {
    private String id;
    private String nome;
    private String precoCusto;
    private String precoVenda;
    private String desconto;
    private String status;
    private String unidade;
    private List<String> idsCategorias = new ArrayList<>();

    private String urlImagem1;
    private String urlImagem2;
    private String urlinhoImagem3;
    private String modelo;

    private String quantidadeEtoque;
    private String quantidadeMinima;
    private String detalhe;
    private String fabricante;
    private String fornecedor;
    private String codigo;
    private String observacao;

    public Produto() {
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public String getPrecoCusto() { return precoCusto; }

    public void setPrecoCusto(String precoCusto) { this.precoCusto = precoCusto; }

    public String getPrecoVenda() { return precoVenda; }

    public void setPrecoVenda(String precoVenda) { this.precoVenda = precoVenda; }

    public String getDesconto() { return desconto; }

    public void setDesconto(String desconto) { this.desconto = desconto; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getUnidade() { return unidade; }

    public void setUnidade(String unidade) { this.unidade = unidade; }

    public List<String> getIdsCategorias() { return idsCategorias; }

    public void setIdsCategorias(List<String> idsCategorias) { this.idsCategorias = idsCategorias; }

    public String getUrlImagem1() { return urlImagem1; }

    public void setUrlImagem1(String urlImagem1) { this.urlImagem1 = urlImagem1; }

    public String getUrlImagem2() { return urlImagem2; }

    public void setUrlImagem2(String urlImagem2) { this.urlImagem2 = urlImagem2; }

    public String getUrlinhoImagem3() { return urlinhoImagem3; }

    public void setUrlinhoImagem3(String urlinhoImagem3) { this.urlinhoImagem3 = urlinhoImagem3; }

    public String getModelo() { return modelo; }

    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getQuantidadeEtoque() { return quantidadeEtoque; }

    public void setQuantidadeEtoque(String quantidadeEtoque) { this.quantidadeEtoque = quantidadeEtoque; }

    public String getQuantidadeMinima() { return quantidadeMinima; }

    public void setQuantidadeMinima(String quantidadeMinima) { this.quantidadeMinima = quantidadeMinima; }

    public String getDetalhe() { return detalhe; }

    public void setDetalhe(String detalhe) { this.detalhe = detalhe; }

    public String getFabricante() { return fabricante; }

    public void setFabricante(String fabricante) { this.fabricante = fabricante; }

    public String getFornecedor() { return fornecedor; }

    public void setFornecedor(String fornecedor) { this.fornecedor = fornecedor; }

    public String getCodigo() { return codigo; }

    public void setCodigo(String codigo) { this.codigo = codigo; }
}
