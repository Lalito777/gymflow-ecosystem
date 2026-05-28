package cl.joaedu.qrgeneratorservice.controller;

import cl.joaedu.qrgeneratorservice.model.QRData;
import cl.joaedu.qrgeneratorservice.service.QRService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/qr")
public class QRController {
    private final QRService service;

    public QRController(QRService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<QRData> create(@RequestBody Map<String, Object> body) throws Exception {
        Long tokenId = Long.parseLong(body.get("accessTokenId").toString());
        String contenido = (String) body.get("contenido");
        return new ResponseEntity<>(service.generateQR(tokenId, contenido), HttpStatus.CREATED);
    }
}