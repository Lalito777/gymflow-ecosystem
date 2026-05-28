package cl.joaedu.accessservice.controller;

import cl.joaedu.accessservice.model.AccessLog;
import cl.joaedu.accessservice.model.AccessToken;
import cl.joaedu.accessservice.service.AccessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/access")
public class AccessController {
    private final AccessService service;

    public AccessController(AccessService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public ResponseEntity<AccessToken> generate(@RequestBody Map<String, Long> body) {
        return new ResponseEntity<>(service.generateToken(body.get("userId"), body.get("branchId")), HttpStatus.CREATED);
    }

    @PostMapping("/validate")
    public ResponseEntity<AccessLog> validate(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.validateEntry((String) body.get("qrCode"), Long.parseLong(body.get("branchId").toString())));
    }
}