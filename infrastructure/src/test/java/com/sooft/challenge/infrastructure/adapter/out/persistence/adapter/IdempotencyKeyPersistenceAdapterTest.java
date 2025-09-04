package com.sooft.challenge.infrastructure.adapter.out.persistence.adapter;

import com.sooft.challenge.domain.model.IdempotencyRecord;
import com.sooft.challenge.infrastructure.adapter.out.persistence.mapper.IdempotencyKeyMapperImpl;
import com.sooft.challenge.infrastructure.adapter.out.persistence.repository.IdempotencyKeyJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({IdempotencyKeyPersistenceAdapter.class, IdempotencyKeyMapperImpl.class})
class IdempotencyKeyPersistenceAdapterTest {

    @Autowired
    private IdempotencyKeyPersistenceAdapter idempotencyKeyPersistenceAdapter;

    @Autowired
    private IdempotencyKeyJpaRepository idempotencyKeyJpaRepository;

    @BeforeEach
    void setUp() {
        idempotencyKeyJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Debe guardar un IdempotencyRecord y luego encontrarlo por su clave")
    void shouldSaveAndFindIdempotencyRecord() {
        String key = UUID.randomUUID().toString();
        IdempotencyRecord recordToSave = IdempotencyRecord.builder()
                .idempotencyKey(key)
                .responseBody("{\"status\":\"ok\"}")
                .responseStatus(200)
                .createdAt(LocalDateTime.now())
                .build();

        idempotencyKeyPersistenceAdapter.save(recordToSave);

        Optional<IdempotencyRecord> foundRecordOpt = idempotencyKeyPersistenceAdapter.findById(key);

        assertTrue(foundRecordOpt.isPresent(), "El registro debería haber sido encontrado.");
        IdempotencyRecord foundRecord = foundRecordOpt.get();

        assertEquals(recordToSave.getIdempotencyKey(), foundRecord.getIdempotencyKey());
        assertEquals(recordToSave.getResponseBody(), foundRecord.getResponseBody());
        assertEquals(recordToSave.getResponseStatus(), foundRecord.getResponseStatus());
        assertEquals(recordToSave.getCreatedAt().withNano(0), foundRecord.getCreatedAt().withNano(0));

        assertEquals(1, idempotencyKeyJpaRepository.count());
    }

    @Test
    @DisplayName("Debe devolver un Optional vacío si la clave no existe")
    void shouldReturnEmptyWhenKeyNotFound() {
        String nonExistentKey = "non-existent-key";

        Optional<IdempotencyRecord> foundRecordOpt = idempotencyKeyPersistenceAdapter.findById(nonExistentKey);

        assertTrue(foundRecordOpt.isEmpty(), "No se debería encontrar ningún registro.");
    }
}