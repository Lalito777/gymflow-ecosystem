package cl.joaedu.userservice.controller;

import cl.joaedu.userservice.dto.ErrorResponse;
import cl.joaedu.userservice.dto.UserRequest;
import cl.joaedu.userservice.dto.UserResponse;
import cl.joaedu.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Registro y consulta de socios/administradores de GymFlow")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @SecurityRequirement(name = "basicAuth")
    @Operation(
            summary = "Lista todos los usuarios",
            description = "Requiere rol ADMIN. Devuelve todos los socios y administradores registrados, sin exponer contraseñas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Autenticado pero sin rol ADMIN", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<UserResponse> getAll() {
        return userService.findAll();
    }

    @PostMapping
    @Operation(
            summary = "Registra un nuevo usuario",
            description = "Endpoint publico. Valida que la sucursal (branchId) exista en branch-service via Feign antes de crear el usuario. La contraseña se guarda con BCrypt."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos (campos faltantes, rol no permitido, etc.)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "La sucursal indicada no existe", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "branch-service no respondio para validar la sucursal", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
