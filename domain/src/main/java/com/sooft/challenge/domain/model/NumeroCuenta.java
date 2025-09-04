package com.sooft.challenge.domain.model;

import lombok.Value;
import org.springframework.util.Assert;

import java.io.Serializable;

@Value
public class NumeroCuenta implements Serializable {

    String valor;

    private NumeroCuenta(String valor) {
        this.valor = valor;
    }

    public static NumeroCuenta of(String valor) {
        Assert.hasText(valor, "El número de cuenta no puede estar vacío.");
        return new NumeroCuenta(valor.trim());
    }
}