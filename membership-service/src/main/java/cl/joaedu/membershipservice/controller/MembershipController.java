package cl.joaedu.membershipservice.controller;

import cl.joaedu.membershipservice.dto.ErrorResponse;
import cl.joaedu.membershipservice.dto.MembershipRequest;
import cl.joaedu.membershipservice.dto.MembershipResponse;
import cl.joaedu.membershipservice.dto.MembershipStatusResponse;
import cl.joaedu.membershipservice.service.MembershipService;
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
@RequestMapping("/api/membership")
@Tag(name = "Membresias", description = "Planes y membresias de los socios")
public class MembershipController {
    private final MembershipService service;

    public MembershipController(MembershipService service) {
        this.service = service;
    }

    @GetMapping("/status/{userId}")
    @Operation(
            summary = "Consulta si un socio tiene membresia activa",
            description = "Consumido en vivo por access-service (Feign) antes de emitir un token de acceso, y por class-service (RestClient) antes de confirmar una reserva de clase."
    )
    @ApiResponse(responseCode = "200", description = "Estado de la membresia (activa: true/false)")
    public ResponseEntity<MembershipStatusResponse> getStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getStatus(userId));
    }

    @PostMapping
    @Operation(summary = "Crea una membresia para un socio", description = "Calcula la fecha de vencimiento en base a la duracion del plan indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Membresia creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "El plan indicado no existe", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MembershipResponse> create(@Valid @RequestBody MembershipRequest request) {
        MembershipResponse response = service.createMembership(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
