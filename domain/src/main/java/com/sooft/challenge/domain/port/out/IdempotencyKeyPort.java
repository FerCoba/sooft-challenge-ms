package com.sooft.challenge.domain.port.out;

import com.sooft.challenge.domain.model.IdempotencyRecord;
import java.util.Optional;

public interface IdempotencyKeyPort {
    Optional<IdempotencyRecord> findById(String idempotencyKey);
    void save(IdempotencyRecord idempotencyRecord);
}