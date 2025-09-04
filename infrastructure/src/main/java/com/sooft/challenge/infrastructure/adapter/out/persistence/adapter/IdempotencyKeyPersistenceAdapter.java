package com.sooft.challenge.infrastructure.adapter.out.persistence.adapter;

import com.sooft.challenge.domain.model.IdempotencyRecord;
import com.sooft.challenge.domain.port.out.IdempotencyKeyPort;
import com.sooft.challenge.infrastructure.adapter.out.persistence.mapper.IdempotencyKeyMapper;
import com.sooft.challenge.infrastructure.adapter.out.persistence.repository.IdempotencyKeyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IdempotencyKeyPersistenceAdapter implements IdempotencyKeyPort {

    private final IdempotencyKeyJpaRepository repository;
    private final IdempotencyKeyMapper mapper;

    @Override
    public Optional<IdempotencyRecord> findById(String idempotencyKey) {
        return repository.findById(idempotencyKey)
                .map(mapper::toDomain);
    }

    @Override
    public void save(IdempotencyRecord idempotencyRecord) {
        repository.save(mapper.toEntity(idempotencyRecord));
    }
}
