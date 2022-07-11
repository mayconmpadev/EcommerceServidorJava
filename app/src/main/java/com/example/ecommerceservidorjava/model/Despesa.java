package com.example.ecommerceservidorjava.model;

import java.io.Serializable;

public class Despesa implements Serializable {

    private String id;
    private String descricao;
    private String valor;
    private int qtd_parcelas;
    private String valor_parcela;
    private int parcela_paga;
    private String data;
    private String data_vencimento;
    private String categoria;
    private String tipoPagamento;
    private String instituicao;
    private String status;


    public Despesa() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public int getQtd_parcelas() {
        return qtd_parcelas;
    }

    public void setQtd_parcelas(int qtd_parcelas) {
        this.qtd_parcelas = qtd_parcelas;
    }

    public String getValor_parcela() {
        return valor_parcela;
    }

    public void setValor_parcela(String valor_parcela) {
        this.valor_parcela = valor_parcela;
    }

    public int getParcela_paga() {
        return parcela_paga;
    }

    public void setParcela_paga(int parcela_paga) {
        this.parcela_paga = parcela_paga;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData_vencimento() {
        return data_vencimento;
    }

    public void setData_vencimento(String data_vencimento) {
        this.data_vencimento = data_vencimento;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getTipoPagamento() {
        return tipoPagamento;
    }

    public void setTipoPagamento(String tipoPagamento) {
        this.tipoPagamento = tipoPagamento;
    }

    public String getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(String instituicao) {
        this.instituicao = instituicao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
