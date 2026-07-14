package cl.joaedu.branchservice.service;

import cl.joaedu.branchservice.dto.BranchRequest;
import cl.joaedu.branchservice.dto.BranchResponse;
import cl.joaedu.branchservice.model.Branch;
import cl.joaedu.branchservice.repository.BranchRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchService {

    private static final Logger log = LoggerFactory.getLogger(BranchService.class);
    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public List<BranchResponse> findAll() {
        log.debug("Listando sedes de la cadena");
        return branchRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BranchResponse findById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sede no encontrada: " + id));
        return mapToResponse(branch);
    }

    public BranchResponse create(BranchRequest request) {
        try {
            Branch branch = new Branch(
                    request.name(),
                    request.address(),
                    request.maxCapacity()
            );
            Branch savedBranch = branchRepository.save(branch);
            log.info("Sede guardada: ID #{} - Nombre: {}", savedBranch.getId(), savedBranch.getName());
            return mapToResponse(savedBranch);
        } catch (Exception e) {
            log.error("Fallo de guardado en sucursal {}: {}", request.name(), e.getMessage());
            throw e;
        }
    }

    private BranchResponse mapToResponse(Branch branch) {
        return new BranchResponse(
                branch.getId(),
                branch.getName(),
                branch.getAddress(),
                branch.getMaxCapacity()
        );
    }
}