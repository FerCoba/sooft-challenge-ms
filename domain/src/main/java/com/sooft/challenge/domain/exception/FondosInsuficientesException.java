package com.sooft.challenge.domain.exception;

public class FondosInsuficientesException extends RuntimeException {
    public FondosInsuficientesException(String numeroCuenta) {
        super("Fondos insuficientes en la cuenta " + numeroCuenta);
    }
}
