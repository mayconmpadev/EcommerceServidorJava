package com.example.ecommerceservidorjava.model;

import java.io.Serializable;

public class Despesa implements Serializable {

    private String id;

    private String valor;
    private String data;
    private String tipo;
    private String descricao;
    private String status;
    private int qtd_parcelas;



    public Despesa() {
    }


}
