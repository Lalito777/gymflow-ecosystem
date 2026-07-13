package cl.joaedu.notificationservice.controller;

import cl.joaedu.notificationservice.dto.ErrorResponse;
import cl.joaedu.notificationservice.dto.NotificationRequest;
import cl.joaedu.notificationservice.dto.NotificationResponse;
import cl.joaedu.notificationservice.service.NotificationService;
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
@RequestMapping("/api/notify")
@Tag(name = "Notificaciones", description = "Envio de notificaciones a socios")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            summary = "Envia una notificacion",
            description = "El tipo debe ser uno de: EMAIL, SMS, PUSH."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notificacion registrada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o tipo de canal no soportado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = service.send(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
