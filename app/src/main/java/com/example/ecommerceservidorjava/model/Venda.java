package com.example.ecommerceservidorjava.model;

import java.io.Serializable;
import java.util.List;

public class Venda implements Serializable {
    private String id;
    private Usuario idUsuario;
    private Cliente idCliente;
    private Endereco idEndereco;
    private String data;
    private String dataStatusPedido;
    private String status;
    private String total;
    private String subTotal;
    private String tipoPagamento;
    private String desconto;
    private String acrescimo;
    private List<ItemVenda> itens;

    public Venda() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Usuario getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Usuario idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Cliente getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Cliente idCliente) {
        this.idCliente = idCliente;
    }

    public Endereco getIdEndereco() {
        return idEndereco;
    }

    public void setIdEndereco(Endereco idEndereco) {
        this.idEndereco = idEndereco;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDataStatusPedido() {
        return dataStatusPedido;
    }

    public void setDataStatusPedido(String dataStatusPedido) {
        this.dataStatusPedido = dataStatusPedido;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(String subTotal) {
        this.subTotal = subTotal;
    }

    public String getTipoPagamento() {
        return tipoPagamento;
    }

    public void setTipoPagamento(String tipoPagamento) {
        this.tipoPagamento = tipoPagamento;
    }

    public String getDesconto() {
        return desconto;
    }

    public void setDesconto(String desconto) {
        this.desconto = desconto;
    }

    public String getAcrescimo() {
        return acrescimo;
    }

    public void setAcrescimo(String acrescimo) {
        this.acrescimo = acrescimo;
    }

    public List<ItemVenda> getItens() {
        return itens;
    }

    public void setItens(List<ItemVenda> itens) {
        this.itens = itens;
    }
}
