package com.sooft.challenge.application.service;

import com.sooft.challenge.domain.exception.CuitDuplicadoException;
import com.sooft.challenge.domain.exception.FechaAdhesionException;
import com.sooft.challenge.domain.model.Cuit;
import com.sooft.challenge.domain.model.Empresa;
import com.sooft.challenge.domain.port.out.EmpresaRepositoryPort;
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
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepositoryPort empresaRepositoryPort;

    private Clock clock;

    private EmpresaService empresaService;

    private final Cuit CUIT_VALIDO = Cuit.of("30112233445");
    private final String RAZON_SOCIAL = "Empresa de Prueba S.A.";
    private final BigDecimal SALDO_INICIAL = new BigDecimal("10000.00");

    @BeforeEach
    void setUp() {

        var fechaFija = Instant.parse("2024-05-20T10:00:00Z");
        clock = Clock.fixed(fechaFija, ZoneId.of("UTC"));

        empresaService = new EmpresaService(empresaRepositoryPort, clock);
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

        when(empresaRepositoryPort.findByCuit(Cuit.of(CUIT_VALIDO.getValor()))).thenReturn(Optional.empty());
        when(empresaRepositoryPort.save(any(Empresa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var empresaAdherida = empresaService.adherirEmpresa(empresaParaAdherir);

        assertNotNull(empresaAdherida);
        assertEquals(CUIT_VALIDO, empresaAdherida.getCuit());
        assertNotNull(empresaAdherida.getCodigo());
        assertNotNull(empresaAdherida.getNumeroCuenta());
        verify(empresaRepositoryPort).save(any(Empresa.class));
    }

    @Test
    @DisplayName("Debe lanzar CuitDuplicadoException si el CUIT ya existe")
    void adherirEmpresa_cuitDuplicado() {

        var empresaExistente = Empresa.builder().cuit(CUIT_VALIDO).build();
        var empresaParaAdherir = Empresa.builder()
                .cuit(CUIT_VALIDO)
                .fechaAdhesion(LocalDate.now(clock))
                .build();

        when(empresaRepositoryPort.findByCuit(Cuit.of(CUIT_VALIDO.getValor()))).thenReturn(Optional.of(empresaExistente));

        assertThrows(CuitDuplicadoException.class, () -> {
            empresaService.adherirEmpresa(empresaParaAdherir);
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

        // CORRECCIÓN 1: El método findByCuit del port espera un String.
        // CORRECCIÓN 2: La sintaxis de when(...).thenReturn(...) estaba mal formada.
        when(empresaRepositoryPort.findByCuit(Cuit.of(CUIT_VALIDO.getValor()))).thenReturn(Optional.empty());

        assertThrows(FechaAdhesionException.class, () -> {
            empresaService.adherirEmpresa(empresaConFechaFutura);
        });
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
