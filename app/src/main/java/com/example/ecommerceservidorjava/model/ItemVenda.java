package com.example.ecommerceservidorjava.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ItemVenda implements Serializable {
    private int id;
    private String idProduto;
    private String codigo;
    private String nome;
    private String descricao;
    private String foto;
    private String preco_venda;
    private String preco_custo;
    private int qtd = 0;
    private List<String> idsCategorias = new ArrayList<>();
    public ItemVenda() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(String idProduto) {
        this.idProduto = idProduto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getPreco_venda() {
        return preco_venda;
    }

    public void setPreco_venda(String preco_venda) {
        this.preco_venda = preco_venda;
    }

    public String getPreco_custo() {
        return preco_custo;
    }

    public void setPreco_custo(String preco_custo) {
        this.preco_custo = preco_custo;
    }

    public int getQtd() {
        return qtd;
    }

    public void setQtd(int qtd) {
        this.qtd = qtd;
    }

    public List<String> getIdsCategorias() {
        return idsCategorias;
    }

    public void setIdsCategorias(List<String> idsCategorias) {
        this.idsCategorias = idsCategorias;
    }
}
