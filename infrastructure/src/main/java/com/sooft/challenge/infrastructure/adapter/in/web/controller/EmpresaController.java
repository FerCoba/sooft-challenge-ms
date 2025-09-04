package com.sooft.challenge.infrastructure.adapter.in.web.controller;

import com.sooft.challenge.domain.model.Cuit;
import com.sooft.challenge.domain.model.Empresa;

import com.sooft.challenge.domain.port.in.AdherirEmpresaUseCase;
import com.sooft.challenge.domain.port.in.BuscarEmpresaPorIdUseCase;
import com.sooft.challenge.domain.port.in.EmpresasAdheridasUltimoMesUseCase;
import com.sooft.challenge.domain.port.in.EmpresasConTransferenciasRecientesUseCase;
import com.sooft.challenge.domain.port.in.BuscarTodasLasEmpresasUseCase;
import com.sooft.challenge.infrastructure.adapter.in.web.dto.CrearEmpresaRequest;
import com.sooft.challenge.infrastructure.adapter.in.web.dto.EmpresaResponseDTO;
import com.sooft.challenge.infrastructure.adapter.in.web.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/empresas")
@RequiredArgsConstructor
@Tag(name = "Empresas Controller", description = "Crear y obtener información de empresas")
public class EmpresaController {

    private final AdherirEmpresaUseCase adherirEmpresaUseCase;
    private final EmpresasAdheridasUltimoMesUseCase empresasAdheridasUltimoMesUseCase;
    private final EmpresasConTransferenciasRecientesUseCase empresasConTransferenciasRecientesUseCase;
    private final BuscarEmpresaPorIdUseCase buscarEmpresaPorIdUseCase;
    private final BuscarTodasLasEmpresasUseCase buscarTodasLasEmpresasUseCase;

    @PostMapping
    @Operation(summary = "Crear una nueva empresa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empresa creada exitosamente."),
            @ApiResponse(responseCode = "400", description = """
                Solicitud inválida. Posibles errores:
                - La fecha de adhesión no puede ser posterior a la fecha actual.
                - Si falta alguno de los parametros del body
                """,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "409", description = """
                Conflicto. Posibles errores:
                - Ya existe una empresa registrada con el CUIT.
                """,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "5XX", description = """
                Errores del servidor. Posibles errores:
                - Problemas con el servidor.
                """,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
        })
    public ResponseEntity<EmpresaResponseDTO> crearEmpresa(@Valid @RequestBody CrearEmpresaRequest request) {
        var nuevaEmpresa = Empresa.builder()
                .cuit(Cuit.of(request.getCuit()))
                .razonSocial(request.getRazonSocial())
                .fechaAdhesion(request.getFechaAdhesion())
                .saldo(request.getSaldo())
                .build();

        var empresaCreada = adherirEmpresaUseCase.adherirEmpresa(nuevaEmpresa);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertirDTO(empresaCreada));
    }

    @GetMapping("/reportes/adheridas-ultimo-mes")
    @Operation(summary = "Obtener empresas adheridas en el último mes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtiene una lista de empresas adheridas en el último mes (Paginado)."),
            @ApiResponse(responseCode = "5XX", description = """
                Errores del servidor. Posibles errores:
                - Problemas con el servidor.
                """,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )})
    public ResponseEntity<Page<EmpresaResponseDTO>> getEmpresasAdheridasUltimoMes(@ParameterObject @PageableDefault(sort = "fechaAdhesion",
            direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Empresa> empresaPage = empresasAdheridasUltimoMesUseCase.findEmpresasAdheridasRecientemente(pageable);
        Page<EmpresaResponseDTO> respuesta = empresaPage.map(this::convertirDTO);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/reportes/transferencias-ultimo-mes")
    @Operation(summary = "Reporte de empresas con transferencias en el último mes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtiene una lista de las transferencias realizadas el ultimo mes.(Paginado)."),
            @ApiResponse(responseCode = "5XX", description = """
                Errores del servidor. Posibles errores:
                - Problemas con el servidor.
                """,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )})
    public ResponseEntity<Page<EmpresaResponseDTO>> getEmpresasConTransferenciasUltimoMes(@ParameterObject @PageableDefault(sort = "empresa.razonSocial",
            direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Empresa> empresaPage = empresasConTransferenciasRecientesUseCase.findEmpresasConTransferenciasRecientes(pageable);
        Page<EmpresaResponseDTO> respuesta = empresaPage.map(this::convertirDTO);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar empresa por su id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se obtiene la empresa por su id."),
            @ApiResponse(responseCode = "404", description = """
                Recurso no encontrado. Posibles errores:
                - No existe empresa con id: {id}.
                """,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "5XX", description = """
                Errores del servidor. Posibles errores:
                - Problemas con el servidor.
                """,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )})
    public ResponseEntity<EmpresaResponseDTO> getEmpresaById(@PathVariable String id) {
        Empresa empresa = buscarEmpresaPorIdUseCase.findById(id).get();
        return ResponseEntity.ok(convertirDTO(empresa));
    }


    @GetMapping
    @Operation(summary = "Obtener una lista de todas las empresas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtiene todas las empresas.(Paginado)."),
            @ApiResponse(responseCode = "5XX", description = """
                Errores del servidor. Posibles errores:
                - Problemas con el servidor.
                """,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )})
    public ResponseEntity<Page<EmpresaResponseDTO>> getAllEmpresas(
            @ParameterObject @PageableDefault(sort = "razonSocial", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<Empresa> empresaPage = buscarTodasLasEmpresasUseCase.findAll(pageable);
        Page<EmpresaResponseDTO> respuesta = empresaPage.map(this::convertirDTO);
        return ResponseEntity.ok(respuesta);
    }

    private EmpresaResponseDTO convertirDTO(Empresa empresa) {
        return new EmpresaResponseDTO(
                empresa.getCodigo(),
                empresa.getRazonSocial(),
                empresa.getCuit().getValor(),
                empresa.getFechaAdhesion(),
                empresa.getSaldo(),
                empresa.getNumeroCuenta().getValor()
        );
    }
}