package com.sooft.challenge.application.service;

import com.sooft.challenge.domain.exception.CuitDuplicadoException;
import com.sooft.challenge.domain.exception.EmpresaNotFoundException;
import com.sooft.challenge.domain.exception.FechaAdhesionException;
import com.sooft.challenge.domain.model.Empresa;
import com.sooft.challenge.domain.port.out.EmpresaRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepositoryPort empresaRepositoryPort;

    @InjectMocks
    private EmpresaService empresaService;

    @Test
    @DisplayName("Debe adherir una empresa exitosamente y generar código y número de cuenta")
    void givenValidEmpresa_whenAdherirEmpresa_thenEmpresaIsSaved() {

        Empresa empresaSinGuardar = Empresa.builder()
                .cuit("20-12345678-9")
                .fechaAdhesion(LocalDate.now())
                .build();

        when(empresaRepositoryPort.findByCuit(empresaSinGuardar.getCuit())).thenReturn(Optional.empty());

        when(empresaRepositoryPort.save(any(Empresa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Empresa empresaGuardada = empresaService.adherirEmpresa(empresaSinGuardar);

        ArgumentCaptor<Empresa> empresaCaptor = ArgumentCaptor.forClass(Empresa.class);
        verify(empresaRepositoryPort).save(empresaCaptor.capture());
        Empresa empresaCapturada = empresaCaptor.getValue();

        assertNotNull(empresaGuardada);
        assertNotNull(empresaCapturada.getCodigo());
        assertNotNull(empresaCapturada.getNumeroCuenta());
        assertEquals(6, empresaCapturada.getCodigo().length());
        assertEquals(15, empresaCapturada.getNumeroCuenta().length());
    }

    @Test
    @DisplayName("Debe lanzar CuitAlreadyExistsException si el CUIT ya existe")
    void givenExistingCuit_whenAdherirEmpresa_thenThrowsCuitAlreadyExistsException() {

        Empresa empresaExistente = Empresa.builder().cuit("20-11111111-1").build();
        when(empresaRepositoryPort.findByCuit(empresaExistente.getCuit())).thenReturn(Optional.of(empresaExistente));

        assertThrows(CuitDuplicadoException.class, () -> {
            empresaService.adherirEmpresa(empresaExistente);
        });

        verify(empresaRepositoryPort, never()).save(any(Empresa.class));
    }

    @Test
    @DisplayName("Debe lanzar FechaAdhesionException si la fecha es futura")
    void givenFutureDate_whenAdherirEmpresa_thenThrowsFechaAdhesionException() {
        Empresa empresaConFechaFutura = Empresa.builder()
                .cuit("20-22222222-2")
                .fechaAdhesion(LocalDate.now().plusDays(1))
                .build();
        when(empresaRepositoryPort.findByCuit(empresaConFechaFutura.getCuit())).thenReturn(Optional.empty());

        assertThrows(FechaAdhesionException.class, () -> {
            empresaService.adherirEmpresa(empresaConFechaFutura);
        });

        verify(empresaRepositoryPort, never()).save(any(Empresa.class));
    }

    @Test
    @DisplayName("Debe devolver una empresa cuando se busca por un código existente")
    void givenExistingCode_whenFindById_thenReturnsEmpresa() {

        String codigo = "ABC123";
        Empresa empresa = Empresa.builder().codigo(codigo).build();
        when(empresaRepositoryPort.findByCodigo(codigo)).thenReturn(Optional.of(empresa));

        Optional<Empresa> resultado = empresaService.findById(codigo);

        assertTrue(resultado.isPresent());
        assertEquals(codigo, resultado.get().getCodigo());
        verify(empresaRepositoryPort).findByCodigo(codigo);
    }

    @Test
    @DisplayName("Debe lanzar EmpresaNotFoundException cuando se busca por un código inexistente")
    void givenNonExistingCode_whenFindById_thenThrowsEmpresaNotFoundException() {

        String codigo = "XYZ987";
        when(empresaRepositoryPort.findByCodigo(codigo)).thenReturn(Optional.empty());

        assertThrows(EmpresaNotFoundException.class, () -> {
            empresaService.findById(codigo);
        });

        verify(empresaRepositoryPort).findByCodigo(codigo);
    }

    @Test
    @DisplayName("Debe llamar al repositorio con Pageable para empresas adheridas")
    void whenFindEmpresasAdheridas_thenRepositoryIsCalledWithPageable() {

        Pageable pageable = PageRequest.of(0, 10);
        List<Empresa> empresas = Collections.singletonList(Empresa.builder().build());
        Page<Empresa> empresaPage = new PageImpl<>(empresas, pageable, 1);
        when(empresaRepositoryPort.findEmpresasAdheridasEnElUltimoMes(pageable)).thenReturn(empresaPage);

        Page<Empresa> resultado = empresaService.findEmpresasAdheridasRecientemente(pageable);

        assertEquals(1, resultado.getTotalElements());
        verify(empresaRepositoryPort).findEmpresasAdheridasEnElUltimoMes(pageable);
    }

    @Test
    @DisplayName("Debe llamar al repositorio con Pageable para empresas con transferencias")
    void whenFindEmpresasConTransferencias_thenRepositoryIsCalledWithPageable() {

        Pageable pageable = PageRequest.of(1, 5);
        Page<Empresa> empresaPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(empresaRepositoryPort.findEmpresasConTransferenciasEnElUltimoMes(pageable)).thenReturn(empresaPage);

        Page<Empresa> resultado = empresaService.findEmpresasConTransferenciasRecientes(pageable);

        assertEquals(0, resultado.getTotalElements());
        verify(empresaRepositoryPort).findEmpresasConTransferenciasEnElUltimoMes(pageable);
    }

    @Test
    @DisplayName("Debe llamar al repositorio y devolver una página de empresas")
    void whenFindAll_thenCallsRepositoryAndReturnsPage() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<Empresa> expectedPage = new PageImpl<>(Collections.emptyList());

        when(empresaRepositoryPort.findAll(pageable)).thenReturn(expectedPage);
        Page<Empresa> actualPage = empresaService.findAll(pageable);

        assertNotNull(actualPage);
        assertEquals(expectedPage, actualPage);
        verify(empresaRepositoryPort, times(1)).findAll(pageable);
    }
}