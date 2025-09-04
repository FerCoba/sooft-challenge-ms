package com.sooft.challenge.application.service;

import com.sooft.challenge.domain.exception.CuitDuplicadoException;
import com.sooft.challenge.domain.exception.FechaAdhesionException;
import com.sooft.challenge.domain.exception.IdempotentRequestException;
import com.sooft.challenge.domain.model.IdempotencyRecord;
import com.sooft.challenge.domain.model.Cuit;
import com.sooft.challenge.domain.model.Empresa;
import com.sooft.challenge.domain.port.out.EmpresaRepositoryPort;
import com.sooft.challenge.domain.port.out.IdempotencyKeyPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepositoryPort empresaRepositoryPort;

    @Mock
    private IdempotencyKeyPort idempotencyKeyPort;

    private Clock clock;

    private EmpresaService empresaService;

    private final Cuit CUIT_VALIDO = Cuit.of("30112233445");
    private final String RAZON_SOCIAL = "Empresa de Prueba S.A.";
    private final BigDecimal SALDO_INICIAL = new BigDecimal("10000.00");
    private final String IDEMPOTENCY_KEY = "test-key-123";
    private final Function<Empresa, String> MOCK_SERIALIZER = empresa -> "{\"id\":\"" + empresa.getId() + "\"}";


    @BeforeEach
    void setUp() {
        var fechaFija = Instant.parse("2024-05-20T10:00:00Z");
        clock = Clock.fixed(fechaFija, ZoneId.of("UTC"));

        empresaService = new EmpresaService(empresaRepositoryPort, clock, idempotencyKeyPort);
    }

    @Test
    @DisplayName("Debe adherir una empresa exitosamente si los datos son válidos")
    void adherirEmpresa_exitosa() {
        var fechaAdhesionValida = LocalDate.now(clock).minusDays(10);
        var empresaParaAdherir = Empresa.builder()
                .cuit(CUIT_VALIDO)
                .razonSocial(RAZON_SOCIAL)
                .fechaAdhesion(fechaAdhesionValida)
                .saldo(SALDO_INICIAL)
                .build();

        when(idempotencyKeyPort.findById(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
        when(empresaRepositoryPort.findByCuit(any(Cuit.class))).thenReturn(Optional.empty());
        when(empresaRepositoryPort.save(any(Empresa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var empresaAdherida = empresaService.adherirEmpresa(empresaParaAdherir, IDEMPOTENCY_KEY, MOCK_SERIALIZER);

        assertNotNull(empresaAdherida);
        assertEquals(CUIT_VALIDO, empresaAdherida.getCuit());
        assertNotNull(empresaAdherida.getCodigo());
        assertNotNull(empresaAdherida.getNumeroCuenta());

        verify(empresaRepositoryPort).save(any(Empresa.class));
        verify(idempotencyKeyPort).save(any(IdempotencyRecord.class));
    }

    @Test
    @DisplayName("Debe lanzar CuitDuplicadoException si el CUIT ya existe")
    void adherirEmpresa_cuitDuplicado() {
        var empresaExistente = Empresa.builder().cuit(CUIT_VALIDO).build();
        var empresaParaAdherir = Empresa.builder()
                .cuit(CUIT_VALIDO)
                .fechaAdhesion(LocalDate.now(clock))
                .build();

        when(idempotencyKeyPort.findById(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
        when(empresaRepositoryPort.findByCuit(Cuit.of(CUIT_VALIDO.getValor()))).thenReturn(Optional.of(empresaExistente));

        assertThrows(CuitDuplicadoException.class, () -> {
            empresaService.adherirEmpresa(empresaParaAdherir, IDEMPOTENCY_KEY, MOCK_SERIALIZER);
        });
    }

    @Test
    @DisplayName("Debe lanzar FechaAdhesionException si la fecha de adhesión es futura")
    void adherirEmpresa_fechaFutura() {
        var fechaFutura = LocalDate.now(clock).plusDays(1);
        var empresaConFechaFutura = Empresa.builder()
                .cuit(CUIT_VALIDO)
                .razonSocial(RAZON_SOCIAL)
                .fechaAdhesion(fechaFutura)
                .saldo(SALDO_INICIAL)
                .build();

        when(idempotencyKeyPort.findById(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
        when(empresaRepositoryPort.findByCuit(Cuit.of(CUIT_VALIDO.getValor()))).thenReturn(Optional.empty());

        assertThrows(FechaAdhesionException.class, () -> {
            empresaService.adherirEmpresa(empresaConFechaFutura, IDEMPOTENCY_KEY, MOCK_SERIALIZER);
        });
    }

    @Test
    @DisplayName("Debe lanzar IdempotentRequestException si la clave de idempotencia ya existe")
    void adherirEmpresa_lanzaExcepcionSiClaveIdempotenciaExiste() {

        var existingRecord = IdempotencyRecord.builder()
                .idempotencyKey(IDEMPOTENCY_KEY)
                .responseBody("{\"message\":\"ya procesado\"}")
                .responseStatus(201)
                .createdAt(LocalDateTime.now(clock).minusMinutes(5))
                .build();

        when(idempotencyKeyPort.findById(IDEMPOTENCY_KEY)).thenReturn(Optional.of(existingRecord));

        var empresaParaAdherir = Empresa.builder().cuit(CUIT_VALIDO).build();

        var exception = assertThrows(IdempotentRequestException.class, () -> {
            empresaService.adherirEmpresa(empresaParaAdherir, IDEMPOTENCY_KEY, MOCK_SERIALIZER);
        });

        assertEquals(201, exception.getResponseStatus());
        assertEquals("{\"message\":\"ya procesado\"}", exception.getResponseBody());

        verify(empresaRepositoryPort, never()).save(any());
    }


    @Test
    @DisplayName("getEmpresasAdheridasUltimoMes debe llamar al repositorio correctamente")
    void getEmpresasAdheridasUltimoMes_llamaAlRepositorio() {
        var pageable = PageRequest.of(0, 10);
        var pageVacia = new PageImpl<>(Collections.<Empresa>emptyList());

        doReturn(pageVacia).when(empresaRepositoryPort).findEmpresasAdheridasEnElUltimoMes(pageable);

        var resultado = empresaService.findEmpresasAdheridasRecientemente(pageable);

        assertNotNull(resultado);
        verify(empresaRepositoryPort, times(1)).findEmpresasAdheridasEnElUltimoMes(pageable);
    }

    @Test
    @DisplayName("getEmpresasConTransferenciasUltimoMes debe llamar al repositorio")
    void getEmpresasConTransferenciasUltimoMes_llamaAlRepositorio() {
        var pageable = PageRequest.of(0, 10);
        Page<Empresa> pageVacia = new PageImpl<>(Collections.emptyList());
        when(empresaRepositoryPort.findEmpresasConTransferenciasEnElUltimoMes(pageable)).thenReturn(pageVacia);

        Page<Empresa> resultado = empresaService.findEmpresasConTransferenciasRecientes(pageable);

        assertNotNull(resultado);
        verify(empresaRepositoryPort).findEmpresasConTransferenciasEnElUltimoMes(pageable);
    }
}
