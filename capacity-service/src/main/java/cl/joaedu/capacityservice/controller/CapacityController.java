package cl.joaedu.capacityservice.controller;

import cl.joaedu.capacityservice.dto.CapacityResponse;
import cl.joaedu.capacityservice.dto.ErrorResponse;
import cl.joaedu.capacityservice.service.CapacityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/capacity")
@Validated
@Tag(name = "Aforo", description = "Aforo en tiempo real por sucursal")
public class CapacityController {

    private final CapacityService service;

    public CapacityController(CapacityService service) {
        this.service = service;
    }

    @PostMapping("/{branchId}/increment")
    @Operation(summary = "Incrementa el aforo de una sucursal", description = "Llamado en vivo por access-service al validar una entrada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aforo actualizado"),
            @ApiResponse(responseCode = "400", description = "branchId invalido", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CapacityResponse> increment(@PathVariable @Positive(message = "branchId debe ser positivo") Long branchId) {
        return ResponseEntity.ok(service.increment(branchId));
    }

    @PostMapping("/{branchId}/decrement")
    @Operation(summary = "Decrementa el aforo de una sucursal", description = "El aforo nunca baja de 0.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aforo actualizado"),
            @ApiResponse(responseCode = "400", description = "branchId invalido", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CapacityResponse> decrement(@PathVariable @Positive(message = "branchId debe ser positivo") Long branchId) {
        return ResponseEntity.ok(service.decrement(branchId));
    }
}
