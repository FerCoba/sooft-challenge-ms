package com.sooft.challenge.application.service;

import com.sooft.challenge.domain.exception.EmpresaNotFoundException;
import com.sooft.challenge.domain.exception.FondosInsuficientesException;
import com.sooft.challenge.domain.exception.TransferenciaException;
import com.sooft.challenge.domain.model.Empresa;
import com.sooft.challenge.domain.model.NumeroCuenta;
import com.sooft.challenge.domain.model.Transferencia;
import com.sooft.challenge.domain.port.out.EmpresaRepositoryPort;
import com.sooft.challenge.domain.port.out.TransferenciaRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock
    private EmpresaRepositoryPort empresaRepositoryPort;

    @Mock
    private TransferenciaRepositoryPort transferenciaRepositoryPort;

    @InjectMocks
    private TransferenciaService transferenciaService;

    private final String CUENTA_ORIGEN_STR = "1-111111-11";
    private final String CUENTA_DESTINO_STR = "2-222222-22";
    private final String CODIGO_EMPRESA_DESTINO = "EMP-B";
    private final BigDecimal MONTO = new BigDecimal("200.00");

    @Test
    @DisplayName("Debe realizar una transferencia exitosa y actualizar saldos")
    void realizarTransferencia_exitosa() {

        Empresa empresaOrigen = Empresa.builder()
                .id(UUID.randomUUID().toString())
                .numeroCuenta(NumeroCuenta.of(CUENTA_ORIGEN_STR))
                .saldo(new BigDecimal("1000.00"))
                .build();

        Empresa empresaDestino = Empresa.builder()
                .id(UUID.randomUUID().toString())
                .codigo(CODIGO_EMPRESA_DESTINO)
                .numeroCuenta(NumeroCuenta.of(CUENTA_DESTINO_STR))
                .saldo(new BigDecimal("500.00"))
                .razonSocial("Empresa Destino")
                .build();

        when(empresaRepositoryPort.findByNumeroCuenta(NumeroCuenta.of(CUENTA_ORIGEN_STR))).thenReturn(Optional.of(empresaOrigen));
        when(empresaRepositoryPort.findByCodigo(CODIGO_EMPRESA_DESTINO)).thenReturn(Optional.of(empresaDestino));
        when(transferenciaRepositoryPort.save(any(Transferencia.class))).thenAnswer(inv -> inv.getArgument(0));

        transferenciaService.realizarTransferencia(CUENTA_ORIGEN_STR, CODIGO_EMPRESA_DESTINO, CUENTA_DESTINO_STR, MONTO);

        assertEquals(0, empresaOrigen.getSaldo().compareTo(new BigDecimal("800.00")));
        assertEquals(0, empresaDestino.getSaldo().compareTo(new BigDecimal("700.00")));

        verify(empresaRepositoryPort, times(1)).save(empresaOrigen);
        verify(empresaRepositoryPort, times(1)).save(empresaDestino);
        verify(transferenciaRepositoryPort, times(1)).save(any(Transferencia.class));
    }

    @Test
    @DisplayName("Debe lanzar FondosInsuficientesException si el saldo es insuficiente")
    void realizarTransferencia_saldoInsuficiente() {

        Empresa empresaOrigen = Empresa.builder()
                .id(UUID.randomUUID().toString())
                .numeroCuenta(NumeroCuenta.of(CUENTA_ORIGEN_STR))
                .saldo(new BigDecimal("100.00"))
                .build();

        Empresa empresaDestino = Empresa.builder()
                .id(UUID.randomUUID().toString())
                .codigo(CODIGO_EMPRESA_DESTINO)
                .numeroCuenta(NumeroCuenta.of(CUENTA_DESTINO_STR))
                .razonSocial("Empresa Destino")
                .build();

        when(empresaRepositoryPort.findByNumeroCuenta(NumeroCuenta.of(CUENTA_ORIGEN_STR))).thenReturn(Optional.of(empresaOrigen));
        when(empresaRepositoryPort.findByCodigo(CODIGO_EMPRESA_DESTINO)).thenReturn(Optional.of(empresaDestino));

        assertThrows(FondosInsuficientesException.class, () -> {
            transferenciaService.realizarTransferencia(CUENTA_ORIGEN_STR, CODIGO_EMPRESA_DESTINO, CUENTA_DESTINO_STR, MONTO);
        });

        verify(empresaRepositoryPort, never()).save(any(Empresa.class));
        verify(transferenciaRepositoryPort, never()).save(any(Transferencia.class));
    }

    @Test
    @DisplayName("Debe lanzar EmpresaNotFoundException si la cuenta de origen no existe")
    void realizarTransferencia_cuentaOrigenNoEncontrada() {
        when(empresaRepositoryPort.findByNumeroCuenta(NumeroCuenta.of(CUENTA_ORIGEN_STR))).thenReturn(Optional.empty());

        assertThrows(EmpresaNotFoundException.class, () -> {
            transferenciaService.realizarTransferencia(CUENTA_ORIGEN_STR, CODIGO_EMPRESA_DESTINO, CUENTA_DESTINO_STR, MONTO);
        });
    }

    @Test
    @DisplayName("Debe lanzar EmpresaNotFoundException si la empresa de destino no existe")
    void realizarTransferencia_empresaDestinoNoEncontrada() {
        Empresa empresaOrigen = Empresa.builder().numeroCuenta(NumeroCuenta.of(CUENTA_ORIGEN_STR)).build();
        when(empresaRepositoryPort.findByNumeroCuenta(NumeroCuenta.of(CUENTA_ORIGEN_STR))).thenReturn(Optional.of(empresaOrigen));
        when(empresaRepositoryPort.findByCodigo(CODIGO_EMPRESA_DESTINO)).thenReturn(Optional.empty());

        assertThrows(EmpresaNotFoundException.class, () -> {
            transferenciaService.realizarTransferencia(CUENTA_ORIGEN_STR, CODIGO_EMPRESA_DESTINO, CUENTA_DESTINO_STR, MONTO);
        });
    }

    @Test
    @DisplayName("Debe lanzar TransferenciaException si las empresas son la misma")
    void realizarTransferencia_mismaEmpresa() {
        String idUnico = UUID.randomUUID().toString();
        Empresa empresaUnica = Empresa.builder()
                .id(idUnico)
                .numeroCuenta(NumeroCuenta.of(CUENTA_ORIGEN_STR))
                .codigo(CODIGO_EMPRESA_DESTINO)
                .build();

        when(empresaRepositoryPort.findByNumeroCuenta(NumeroCuenta.of(CUENTA_ORIGEN_STR))).thenReturn(Optional.of(empresaUnica));
        when(empresaRepositoryPort.findByCodigo(CODIGO_EMPRESA_DESTINO)).thenReturn(Optional.of(empresaUnica));

        assertThrows(TransferenciaException.class, () -> {
            transferenciaService.realizarTransferencia(CUENTA_ORIGEN_STR, CODIGO_EMPRESA_DESTINO, CUENTA_DESTINO_STR, MONTO);
        });
    }

    @Test
    @DisplayName("Debe lanzar TransferenciaException si la cuenta de crédito no coincide")
    void realizarTransferencia_cuentaCreditoNoCoincide() {
        Empresa empresaOrigen = Empresa.builder()
                .id(UUID.randomUUID().toString())
                .numeroCuenta(NumeroCuenta.of(CUENTA_ORIGEN_STR))
                .build();

        Empresa empresaDestino = Empresa.builder()
                .id(UUID.randomUUID().toString())
                .codigo(CODIGO_EMPRESA_DESTINO)
                .numeroCuenta(NumeroCuenta.of("9-999999-99"))
                .razonSocial("Empresa Destino")
                .build();

        when(empresaRepositoryPort.findByNumeroCuenta(NumeroCuenta.of(CUENTA_ORIGEN_STR))).thenReturn(Optional.of(empresaOrigen));
        when(empresaRepositoryPort.findByCodigo(CODIGO_EMPRESA_DESTINO)).thenReturn(Optional.of(empresaDestino));

        assertThrows(TransferenciaException.class, () -> {
            transferenciaService.realizarTransferencia(CUENTA_ORIGEN_STR, CODIGO_EMPRESA_DESTINO, CUENTA_DESTINO_STR, MONTO);
        });
    }
}