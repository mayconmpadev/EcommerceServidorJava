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
    private String dataBoleto;
    private boolean boletoPago;
    private String parcela1;
    private String parcela2;
    private String parcela3;
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

    public String getDataBoleto() {
        return dataBoleto;
    }

    public void setDataBoleto(String dataBoleto) {
        this.dataBoleto = dataBoleto;
    }

    public boolean isBoletoPago() {
        return boletoPago;
    }

    public void setBoletoPago(boolean boletoPago) {
        this.boletoPago = boletoPago;
    }

    public String getParcela1() {
        return parcela1;
    }

    public void setParcela1(String parcela1) {
        this.parcela1 = parcela1;
    }

    public String getParcela2() {
        return parcela2;
    }

    public void setParcela2(String parcela2) {
        this.parcela2 = parcela2;
    }

    public String getParcela3() {
        return parcela3;
    }

    public void setParcela3(String parcela3) {
        this.parcela3 = parcela3;
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
