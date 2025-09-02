package com.sooft.challenge.domain.exception;

public class CuitDuplicadoException extends RuntimeException {

    public CuitDuplicadoException(String cuit) {
        super("Ya existe una empresa registrada con el CUIT: " + cuit);
    }
}