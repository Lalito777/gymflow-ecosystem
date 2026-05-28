package cl.joaedu.branchservice.controller;

import cl.joaedu.branchservice.dto.BranchRequest;
import cl.joaedu.branchservice.dto.BranchResponse;
import cl.joaedu.branchservice.service.BranchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping
    public List<BranchResponse> getAll() {
        return branchService.findAll();
    }

    @PostMapping
    public ResponseEntity<BranchResponse> create(@RequestBody BranchRequest request) {
        BranchResponse response = branchService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}