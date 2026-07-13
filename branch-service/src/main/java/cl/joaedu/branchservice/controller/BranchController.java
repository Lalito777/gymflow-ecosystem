package cl.joaedu.branchservice.controller;

import cl.joaedu.branchservice.dto.BranchRequest;
import cl.joaedu.branchservice.dto.BranchResponse;
import cl.joaedu.branchservice.dto.ErrorResponse;
import cl.joaedu.branchservice.service.BranchService;
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
@RequestMapping("/api/branches")
@Tag(name = "Sucursales", description = "Gestion de sedes de la cadena GymFlow")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping
    @Operation(summary = "Lista todas las sucursales", description = "Usado por otros microservicios (ej. user-service) para mostrar opciones de sucursal.")
    @ApiResponse(responseCode = "200", description = "Listado de sucursales")
    public List<BranchResponse> getAll() {
        return branchService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una sucursal por ID", description = "Consumido en vivo por user-service via Feign para validar que exista antes de registrar un socio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucursal encontrada"),
            @ApiResponse(responseCode = "404", description = "La sucursal no existe", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BranchResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(branchService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crea una nueva sucursal", description = "Valida nombre, direccion y capacidad maxima con Bean Validation.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sucursal creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BranchResponse> create(@Valid @RequestBody BranchRequest request) {
        BranchResponse response = branchService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
