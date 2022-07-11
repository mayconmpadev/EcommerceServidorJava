package com.example.ecommerceservidorjava.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrdemServico implements Serializable {
    private String id;
    private Usuario idUsuario;
    private Cliente idCliente;
    private String equipamento;
    private String marca;
    private String modelo;
    private String defeitoRelatado;
    private String codigo;
    private String defeitoEncontrado;
    private String status;
    private String observacao;

    private String desconto;
    private String maoDeObra;
    private String valorMaoDeObra;
    private String dataEntrada;
    private String dataStatusPedido;
    private String total;
    private String subTotal;
    private String tipoPagamento;
    private String acrescimo;
    private List<ItemVenda> itens;
    private String urlImagem0;
    private String urlImagem1;
    private String urlImagem2;


    public OrdemServico() {
    }


}
