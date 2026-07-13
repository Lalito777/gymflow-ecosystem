package cl.joaedu.routineservice.controller;

import cl.joaedu.routineservice.dto.ErrorResponse;
import cl.joaedu.routineservice.dto.RoutineRequest;
import cl.joaedu.routineservice.dto.RoutineResponse;
import cl.joaedu.routineservice.service.RoutineService;
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
@RequestMapping("/api/routines")
@Tag(name = "Rutinas", description = "Rutinas de entrenamiento de los socios")
public class RoutineController {

    private final RoutineService service;

    public RoutineController(RoutineService service) {
        this.service = service;
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Lista las rutinas de un socio")
    @ApiResponse(responseCode = "200", description = "Listado de rutinas (puede ser vacio)")
    public ResponseEntity<List<RoutineResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getRoutinesByUser(userId));
    }

    @PostMapping
    @Operation(summary = "Crea una rutina de entrenamiento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rutina creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RoutineResponse> create(@Valid @RequestBody RoutineRequest request) {
        RoutineResponse response = service.createRoutine(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
