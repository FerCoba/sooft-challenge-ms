package com.sooft.challenge.infrastructure.adapter.out.persistence.adapter;

import com.sooft.challenge.domain.model.Transferencia;
import com.sooft.challenge.domain.port.out.TransferenciaRepositoryPort;
import com.sooft.challenge.infrastructure.adapter.out.persistence.entity.TransferenciaEntity;
import com.sooft.challenge.infrastructure.adapter.out.persistence.mapper.TransferenciaMapper;
import com.sooft.challenge.infrastructure.adapter.out.persistence.repository.TransferenciaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransferenciaPersistenceAdapter implements TransferenciaRepositoryPort {

    private final TransferenciaJpaRepository transferenciaJpaRepository;
    private final TransferenciaMapper transferenciaMapper;

    @Override
    public Transferencia save(Transferencia transferencia) {

        TransferenciaEntity transferenciaEntity = transferenciaMapper.toEntity(transferencia);

        TransferenciaEntity savedEntity = transferenciaJpaRepository.save(transferenciaEntity);

        return transferenciaMapper.toDomain(savedEntity);
    }
}