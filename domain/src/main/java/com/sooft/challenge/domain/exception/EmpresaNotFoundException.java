package com.sooft.challenge.domain.exception;

public class EmpresaNotFoundException extends RuntimeException {
    public EmpresaNotFoundException(String codigo) {
        super("No existe empresa con id: " + codigo);
    }
}
