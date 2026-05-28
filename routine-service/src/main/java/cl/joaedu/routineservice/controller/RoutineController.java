package cl.joaedu.routineservice.controller;

import cl.joaedu.routineservice.model.Routine;
import cl.joaedu.routineservice.service.RoutineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/routines")
public class RoutineController {
    private final RoutineService service;

    public RoutineController(RoutineService service) {
        this.service = service;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Routine>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getRoutinesByUser(userId));
    }

    @PostMapping
    public ResponseEntity<Routine> create(@RequestBody Routine routine) {
        return new ResponseEntity<>(service.createRoutine(routine), HttpStatus.CREATED);
    }
}