package cl.joaedu.qrgeneratorservice.controller;

import cl.joaedu.qrgeneratorservice.dto.ErrorResponse;
import cl.joaedu.qrgeneratorservice.dto.GenerateQrRequest;
import cl.joaedu.qrgeneratorservice.dto.QrResponse;
import cl.joaedu.qrgeneratorservice.service.QRService;
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
@RequestMapping("/api/qr")
@Tag(name = "Generador de QR", description = "Generacion de imagenes QR (ZXing) para tokens de acceso")
public class QRController {
    private final QRService service;

    public QRController(QRService service) {
        this.service = service;
    }

    @PostMapping("/create")
    @Operation(summary = "Genera la imagen QR de un token de acceso", description = "Devuelve la imagen codificada en Base64 (data:image/png;base64,...).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "QR generado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<QrResponse> create(@Valid @RequestBody GenerateQrRequest request) throws Exception {
        QrResponse response = service.generateQR(request.accessTokenId(), request.contenido());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
