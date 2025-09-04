package com.sooft.challenge.domain.model;

import lombok.Value;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.regex.Pattern;

@Value
public class Cuit implements Serializable {

    private static final Pattern CUIT_PATTERN = Pattern.compile("^\\d{11}$");

    String valor;

    private Cuit(String valor) {
        this.valor = valor;
    }

    public static Cuit of(String valor) {
        Assert.notNull(valor, "El CUIT no puede ser nulo");
        String cuitNormalizado = valor.replace("-", "").trim();

        Assert.isTrue(CUIT_PATTERN.matcher(cuitNormalizado).matches(), "El formato del CUIT es inválido. Debe contener 11 dígitos.");

        return new Cuit(valor);
    }

    public String getValorNormalizado() {
        return this.valor.replace("-", "").trim();
    }
}
