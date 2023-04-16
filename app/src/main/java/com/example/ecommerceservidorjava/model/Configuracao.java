package com.example.ecommerceservidorjava.model;

import java.io.Serializable;

public class Configuracao implements Serializable {

    private String id;
    // vendas
    private int desconto_dinheiro;
    private int desconto_debito;
    private int acrecimo_boleto;
    private int qtd_parcelas;
    private int lucro;
    // impressão
    private String rodape;


    public Configuracao() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDesconto_dinheiro() {
        return desconto_dinheiro;
    }

    public void setDesconto_dinheiro(int desconto_dinheiro) {
        this.desconto_dinheiro = desconto_dinheiro;
    }

    public int getDesconto_debito() {
        return desconto_debito;
    }

    public void setDesconto_debito(int desconto_debito) {
        this.desconto_debito = desconto_debito;
    }

    public int getAcrecimo_boleto() {
        return acrecimo_boleto;
    }

    public void setAcrecimo_boleto(int acrecimo_boleto) {
        this.acrecimo_boleto = acrecimo_boleto;
    }

    public int getQtd_parcelas() {
        return qtd_parcelas;
    }

    public void setQtd_parcelas(int qtd_parcelas) {
        this.qtd_parcelas = qtd_parcelas;
    }

    public String getRodape() {
        return rodape;
    }

    public void setRodape(String rodape) {
        this.rodape = rodape;
    }

    public int getLucro() {
        return lucro;
    }

    public void setLucro(int lucro) {
        this.lucro = lucro;
    }
}
