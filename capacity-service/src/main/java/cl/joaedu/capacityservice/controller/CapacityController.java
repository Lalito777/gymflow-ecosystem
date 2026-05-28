package cl.joaedu.capacityservice.controller;

import cl.joaedu.capacityservice.model.CapacityCounter;
import cl.joaedu.capacityservice.service.CapacityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/capacity")
public class CapacityController {
    private final CapacityService service;

    public CapacityController(CapacityService service) {
        this.service = service;
    }

    @PostMapping("/{branchId}/increment")
    public ResponseEntity<CapacityCounter> increment(@PathVariable Long branchId) {
        return ResponseEntity.ok(service.increment(branchId));
    }

    @PostMapping("/{branchId}/decrement")
    public ResponseEntity<CapacityCounter> decrement(@PathVariable Long branchId) {
        return ResponseEntity.ok(service.decrement(branchId));
    }
}