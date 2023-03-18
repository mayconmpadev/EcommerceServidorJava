package com.example.ecommerceservidorjava.model;

import java.io.Serializable;
import java.util.List;

public class Boleto implements Serializable {
    private String id;
    private Venda idVenda;
    private OrdemServico idOrdenServico;
    private String data;
    private String tipo;
    private String status;
    private String parcela1;
    private String parcela2;
    private String parcela3;
//teste
    public Boleto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Venda getIdVenda() {
        return idVenda;
    }

    public void setIdVenda(Venda idVenda) {
        this.idVenda = idVenda;
    }

    public OrdemServico getIdOrdenServico() {
        return idOrdenServico;
    }

    public void setIdOrdenServico(OrdemServico idOrdenServico) {
        this.idOrdenServico = idOrdenServico;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
