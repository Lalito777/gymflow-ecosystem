package cl.joaedu.accessservice.controller;

import cl.joaedu.accessservice.dto.AccessLogResponse;
import cl.joaedu.accessservice.dto.AccessTokenResponse;
import cl.joaedu.accessservice.dto.ErrorResponse;
import cl.joaedu.accessservice.dto.GenerateTokenRequest;
import cl.joaedu.accessservice.dto.ValidateEntryRequest;
import cl.joaedu.accessservice.service.AccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/access")
@Tag(name = "Control de acceso", description = "Emision y validacion de tokens QR de entrada a sucursales")
public class AccessController {
    private final AccessService service;

    public AccessController(AccessService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    @Operation(
            summary = "Genera un token de acceso",
            description = "Valida en vivo (Feign) que el socio tenga membresia activa en membership-service antes de emitir el token. El token expira en 24 horas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Token generado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El socio no tiene membresia activa", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AccessTokenResponse> generate(@Valid @RequestBody GenerateTokenRequest request) {
        AccessTokenResponse response = service.generateToken(request.userId(), request.branchId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/validate")
    @Operation(
            summary = "Valida un QR de entrada",
            description = "Marca el token como USADO (evita reuso) y notifica a capacity-service para incrementar el aforo de la sucursal (tolerante a fallas: si capacity-service no responde, la entrada igual se registra)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrada registrada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El QR no existe", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El QR ya fue usado o expiro", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AccessLogResponse> validate(@Valid @RequestBody ValidateEntryRequest request) {
        AccessLogResponse response = service.validateEntry(request.qrCode(), request.branchId());
        return ResponseEntity.ok(response);
    }
}
