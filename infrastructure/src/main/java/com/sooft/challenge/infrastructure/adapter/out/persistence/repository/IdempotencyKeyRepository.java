package com.sooft.challenge.infrastructure.adapter.out.persistence.repository;

import com.sooft.challenge.infrastructure.adapter.out.persistence.entity.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, String> {
}