package com.example.ecommerceservidorjava.model;

import java.io.Serializable;
import java.util.List;

public class OrdemServico implements Serializable {
    private String id;
    private Usuario idUsuario;
    private Cliente idCliente;
    private String telefone;
    private String equipamento;
    private String marca;
    private String modelo;
    private String defeitoRelatado;
    private String numeroOs;
    private String defeitoEncontrado;
    private String status;
    private String tecnico;

    private String observacao;
    private boolean garantia;

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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEquipamento() {
        return equipamento;
    }

    public void setEquipamento(String equipamento) {
        this.equipamento = equipamento;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getDefeitoRelatado() {
        return defeitoRelatado;
    }

    public void setDefeitoRelatado(String defeitoRelatado) {
        this.defeitoRelatado = defeitoRelatado;
    }

    public String getNumeroOs() {
        return numeroOs;
    }

    public void setNumeroOs(String numeroOs) {
        this.numeroOs = numeroOs;
    }

    public String getDefeitoEncontrado() {
        return defeitoEncontrado;
    }

    public void setDefeitoEncontrado(String defeitoEncontrado) {
        this.defeitoEncontrado = defeitoEncontrado;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTecnico() {
        return tecnico;
    }

    public void setTecnico(String tecnico) {
        this.tecnico = tecnico;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public boolean isGarantia() {
        return garantia;
    }

    public void setGarantia(boolean garantia) {
        this.garantia = garantia;
    }

    public String getDesconto() {
        return desconto;
    }

    public void setDesconto(String desconto) {
        this.desconto = desconto;
    }

    public String getMaoDeObra() {
        return maoDeObra;
    }

    public void setMaoDeObra(String maoDeObra) {
        this.maoDeObra = maoDeObra;
    }

    public String getValorMaoDeObra() {
        return valorMaoDeObra;
    }

    public void setValorMaoDeObra(String valorMaoDeObra) {
        this.valorMaoDeObra = valorMaoDeObra;
    }

    public String getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(String dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public String getDataStatusPedido() {
        return dataStatusPedido;
    }

    public void setDataStatusPedido(String dataStatusPedido) {
        this.dataStatusPedido = dataStatusPedido;
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
}
