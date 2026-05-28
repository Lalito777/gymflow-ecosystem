package cl.joaedu.equipmentservice.controller;

import cl.joaedu.equipmentservice.model.Equipment;
import cl.joaedu.equipmentservice.repository.EquipmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {
    private final EquipmentRepository repository;

    public EquipmentController(EquipmentRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<Equipment>> getByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(repository.findByBranchId(branchId));
    }

    @PostMapping
    public ResponseEntity<Equipment> create(@RequestBody Equipment eq) {
        return new ResponseEntity<>(repository.save(eq), HttpStatus.CREATED);
    }
}