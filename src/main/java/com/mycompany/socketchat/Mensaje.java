package com.mycompany.socketchatV02;

import java.io.Serializable;

public class Mensaje implements Serializable {
    private String texto;
    private Cliente cliente;


    public Mensaje() {
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Mensaje(Cliente cliente,String texto ) {
        this.texto = texto;
        this.cliente = cliente;
    }
    @Override
    public String toString() {
        return texto;
    }
}
