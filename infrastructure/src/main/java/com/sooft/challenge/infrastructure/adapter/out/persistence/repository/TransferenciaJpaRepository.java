package com.sooft.challenge.infrastructure.adapter.out.persistence.repository;

import com.sooft.challenge.infrastructure.adapter.out.persistence.entity.TransferenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferenciaJpaRepository extends JpaRepository<TransferenciaEntity, Long> {
}
