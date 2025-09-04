package com.sooft.challenge.infrastructure.adapter.out.persistence.mapper;

import com.sooft.challenge.domain.model.IdempotencyRecord;
import com.sooft.challenge.infrastructure.adapter.out.persistence.entity.IdempotencyKeyEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IdempotencyKeyMapper {
    IdempotencyRecord toDomain(IdempotencyKeyEntity entity);
    IdempotencyKeyEntity toEntity(IdempotencyRecord domain);
}