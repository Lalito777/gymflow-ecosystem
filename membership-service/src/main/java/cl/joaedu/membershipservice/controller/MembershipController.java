package cl.joaedu.membershipservice.controller;

import cl.joaedu.membershipservice.model.Membership;
import cl.joaedu.membershipservice.service.MembershipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/membership")
public class MembershipController {
    private final MembershipService service;

    public MembershipController(MembershipService service) {
        this.service = service;
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<Map<String, Boolean>> getStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("activa", service.isMembershipActive(userId)));
    }

    @PostMapping
    public ResponseEntity<Membership> create(@RequestBody Map<String, Long> body) {
        return new ResponseEntity<>(service.createMembership(body.get("userId"), body.get("planId")), HttpStatus.CREATED);
    }
}