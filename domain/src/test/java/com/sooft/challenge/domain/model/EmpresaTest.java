package com.sooft.challenge.domain.model;

import com.sooft.challenge.domain.exception.FondosInsuficientesException;
import com.sooft.challenge.domain.exception.MontoNegativoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DisplayName("Pruebas Unitarias para el Modelo de Dominio Empresa")
class EmpresaTest {

    private Empresa empresa;
    private static final String NUMERO_CUENTA_PRUEBA = "REWRWE2312312123";

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id("1")
                .codigo("EMP-001")
                .razonSocial("Empresa de Prueba S.A.")
                .cuit("30-12345678-9")
                .fechaAdhesion(LocalDate.now())
                .saldo(new BigDecimal("1000.00"))
                .numeroCuenta(NUMERO_CUENTA_PRUEBA)
                .build();
    }

    @Test
    @DisplayName("Debitar un monto válido con fondos suficientes debe reducir el saldo")
    void debitar_conFondosSuficientes_debeReducirElSaldo() {
        empresa.debitar(new BigDecimal("300.00"));
        assertEquals(0, empresa.getSaldo().compareTo(new BigDecimal("700.00")));
    }

    @Test
    @DisplayName("Debitar un monto mayor al saldo debe lanzar FondosInsuficientesException")
    void debitar_conFondosInsuficientes_debeLanzarFondosInsuficientesException() {
        BigDecimal montoADebitar = new BigDecimal("1500.00");
        String mensajeEsperado = "Fondos insuficientes en la cuenta ".concat(empresa.getNumeroCuenta());

        FondosInsuficientesException exception = assertThrows(FondosInsuficientesException.class, () -> {
            empresa.debitar(montoADebitar);
        });

        assertEquals(mensajeEsperado, exception.getMessage());
    }

    @Test
    @DisplayName("Debitar un monto negativo debe lanzar MontoNegativoException")
    void debitar_conMontoNegativo_debeLanzarMontoNegativoException() {
        assertThrows(MontoNegativoException.class, () -> {
            empresa.debitar(new BigDecimal("-100.00"));
        });
    }

    @Test
    @DisplayName("Acreditar un monto positivo debe incrementar el saldo")
    void acreditar_conMontoPositivo_debeIncrementarElSaldo() {
        empresa.acreditar(new BigDecimal("500.00"));
        assertEquals(0, empresa.getSaldo().compareTo(new BigDecimal("1500.00")));
    }

    @Test
    @DisplayName("Acreditar un monto negativo debe lanzar MontoNegativoException")
    void acreditar_conMontoNegativo_debeLanzarMontoNegativoException() {
        assertThrows(MontoNegativoException.class, () -> {
            empresa.acreditar(new BigDecimal("-200.00"));
        });
    }

    @Test
    @DisplayName("Probar Getters y Setters generados por Lombok")
    void testGettersAndSetters() {
        Empresa nuevaEmpresa = new Empresa();
        String nuevaRazonSocial = "Nueva Razón Social S.L.";
        LocalDate nuevaFecha = LocalDate.of(2023, 1, 1);

        nuevaEmpresa.setRazonSocial(nuevaRazonSocial);
        nuevaEmpresa.setFechaAdhesion(nuevaFecha);

        assertEquals(nuevaRazonSocial, nuevaEmpresa.getRazonSocial());
        assertEquals(nuevaFecha, nuevaEmpresa.getFechaAdhesion());
    }

    @Test
    @DisplayName("Probar constructores (sin argumentos y con todos los argumentos)")
    void testConstructors() {
        Empresa empresaSinDatos = new Empresa();
        assertNotNull(empresaSinDatos);

        Empresa empresaConDatos = new Empresa("1","ID-002","30-98765432-1","Llena S.A.", LocalDate.now(), BigDecimal.TEN, "987654321");

        assertEquals("1", empresaConDatos.getId());
        assertEquals("Llena S.A.", empresaConDatos.getRazonSocial());
    }

    @Test
    @DisplayName("Probar el contrato de equals() y hashCode()")
    void testEqualsAndHashCode() {
        Empresa empresaCopia = Empresa.builder()
                .id("1")
                .codigo("EMP-001")
                .razonSocial("Empresa de Prueba S.A.")
                .cuit("30-12345678-9")
                .fechaAdhesion(empresa.getFechaAdhesion())
                .saldo(new BigDecimal("1000.00"))
                .numeroCuenta(NUMERO_CUENTA_PRUEBA)
                .build();

        Empresa empresaDiferente = Empresa.builder()
                .id("2")
                .codigo("EMP-002")
                .razonSocial("Otra Empresa S.A.")
                .cuit("30-00000000-0")
                .fechaAdhesion(LocalDate.now())
                .saldo(new BigDecimal("2000.00"))
                .numeroCuenta("999999999")
                .build();

        assertEquals(empresa, empresa);
        assertEquals(empresa, empresaCopia);
        assertEquals(empresa.hashCode(), empresaCopia.hashCode());
        assertNotEquals(null, empresa);
        assertNotEquals(empresa, empresaDiferente);
    }

    @Test
    @DisplayName("Probar el método toString() generado")
    void testToString() {
        String toStringResult = empresa.toString();

        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("Empresa"));
        assertTrue(toStringResult.contains(empresa.getRazonSocial()));
        assertTrue(toStringResult.contains(empresa.getCuit()));
    }
}