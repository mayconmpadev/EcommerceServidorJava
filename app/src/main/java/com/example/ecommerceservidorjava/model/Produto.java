package com.example.ecommerceservidorjava.model;

import java.io.Serializable;

public class Produto implements Serializable {
    private String id;
    private String nome;
    private String precoCusto;
    private String precoVenda;
    private String categoria;
    private String status;
    private String unidade;

    private String caminhoImagem1;
    private String caminhoImagem2;
    private String caminhoImagem3;
    private String modelo;

    private String quantidadeEtoque;
    private String quantidadeMinima;
    private String detalhe;
    private String fabricante;
    private String fornecedor;
    private String codigo;
    private String observacao;
}
