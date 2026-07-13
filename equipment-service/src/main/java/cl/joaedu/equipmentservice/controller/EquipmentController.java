package cl.joaedu.equipmentservice.controller;

import cl.joaedu.equipmentservice.dto.EquipmentRequest;
import cl.joaedu.equipmentservice.dto.EquipmentResponse;
import cl.joaedu.equipmentservice.dto.ErrorResponse;
import cl.joaedu.equipmentservice.service.EquipmentService;
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

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@Tag(name = "Equipos", description = "Inventario de equipos de gimnasio por sucursal")
public class EquipmentController {

    private final EquipmentService service;

    public EquipmentController(EquipmentService service) {
        this.service = service;
    }

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "Lista el equipamiento de una sucursal")
    @ApiResponse(responseCode = "200", description = "Listado de equipos (puede ser vacio)")
    public ResponseEntity<List<EquipmentResponse>> getByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(service.getByBranch(branchId));
    }

    @PostMapping
    @Operation(
            summary = "Registra un equipo",
            description = "El estado debe ser uno de: DISPONIBLE, EN_MANTENCION, FUERA_DE_SERVICIO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Equipo creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o estado fuera del dominio permitido", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EquipmentResponse> create(@Valid @RequestBody EquipmentRequest request) {
        EquipmentResponse response = service.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
