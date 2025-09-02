package com.sooft.challenge.infrastructure.adapter.in.web.controller;

import com.sooft.challenge.domain.model.Transferencia;
import com.sooft.challenge.domain.port.in.RealizarTransferenciaUseCase;
import com.sooft.challenge.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.sooft.challenge.infrastructure.adapter.in.web.dto.RealizarTransferenciaRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transferencias")
@RequiredArgsConstructor
@Tag(name = "Transferencias Controller", description = "Crear transferencias entre empresas")
public class TransferenciaController {

    private final RealizarTransferenciaUseCase realizarTransferenciaUseCase;

    @PostMapping
    @Operation(summary = "Crear una nueva transferencia")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transferencia realizada exitosamente."),
            @ApiResponse(responseCode = "400", description = """
                Solicitud inválida. Posibles errores:
                - La cuenta de débito y crédito no pueden pertenecer a la misma empresa.
                - La cuenta de crédito no pertenece a la empresa.
                - El monto a debitar debe ser positivo.
                """,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = """
                Recurso no encontrado. Posibles errores:
                - La cuenta de débito no existe.
                - La empresa de crédito no existe.
                """,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "5XX", description = """
                Errores del servidor. Posibles errores:
                - Problemas con el servidor.
                """,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )})
    public ResponseEntity<Transferencia> realizarTransferencia(@Valid @RequestBody RealizarTransferenciaRequest request) {
        Transferencia transferenciaRealizada = realizarTransferenciaUseCase.realizarTransferencia(
                request.cuentaDebito(),
                request.idEmpresa(),
                request.cuentaCredito(),
                request.importe()
        );
    return ResponseEntity.status(HttpStatus.CREATED).body(transferenciaRealizada);
    }
}