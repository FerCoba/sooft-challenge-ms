package com.sooft.challenge.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class IdempotencyRecord {
    private final String idempotencyKey;
    private final String responseBody;
    private final int responseStatus;
    private final LocalDateTime createdAt;
}