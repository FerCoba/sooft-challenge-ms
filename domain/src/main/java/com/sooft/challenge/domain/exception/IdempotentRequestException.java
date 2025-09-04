package com.sooft.challenge.domain.exception;

import lombok.Getter;

@Getter
public class IdempotentRequestException extends RuntimeException {

    private final String responseBody;
    private final int responseStatus;

    public IdempotentRequestException(String responseBody, int responseStatus) {
        super("La solicitud ya fue procesada anteriormente.");
        this.responseBody = responseBody;
        this.responseStatus = responseStatus;
    }
}