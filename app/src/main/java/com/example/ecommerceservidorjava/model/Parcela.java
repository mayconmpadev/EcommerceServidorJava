package com.example.ecommerceservidorjava.model;

import java.io.Serializable;

public class Parcela implements Serializable {
    private int qtd;
    private boolean status;
    private String data;

    public Parcela() {
    }

    public int getQtd() {
        return qtd;
    }

    public void setQtd(int qtd) {
        this.qtd = qtd;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
