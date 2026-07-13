package cl.joaedu.classservice.controller;

import cl.joaedu.classservice.dto.ClassReservationResponse;
import cl.joaedu.classservice.dto.ErrorResponse;
import cl.joaedu.classservice.dto.ReserveClassRequest;
import cl.joaedu.classservice.service.ClassService;
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
@RequestMapping("/api/classes")
@Tag(name = "Clases", description = "Reserva de clases grupales")
public class ClassController {

    private final ClassService service;

    public ClassController(ClassService service) {
        this.service = service;
    }

    @PostMapping("/reserve")
    @Operation(
            summary = "Reserva un cupo en una clase",
            description = "Valida en vivo (RestClient) que el socio tenga membresia activa en membership-service, y que no tenga ya una reserva confirmada para la misma clase."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Sin membresia activa o reserva duplicada", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClassReservationResponse> reserve(@Valid @RequestBody ReserveClassRequest request) {
        ClassReservationResponse response = service.reserve(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
