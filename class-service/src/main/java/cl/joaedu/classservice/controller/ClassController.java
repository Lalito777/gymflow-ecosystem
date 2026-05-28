package cl.joaedu.classservice.controller;

import cl.joaedu.classservice.model.ClassReservation;
import cl.joaedu.classservice.service.ClassService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/classes")
public class ClassController {
    private final ClassService service;

    public ClassController(ClassService service) {
        this.service = service;
    }

    @PostMapping("/reserve")
    public ResponseEntity<ClassReservation> reserve(@RequestBody Map<String, Long> body) {
        return new ResponseEntity<>(service.reserve(body.get("userId"), body.get("classId")), HttpStatus.CREATED);
    }
}