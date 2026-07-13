package cl.joaedu.capacityservice.service;

import cl.joaedu.capacityservice.dto.CapacityResponse;
import cl.joaedu.capacityservice.model.CapacityCounter;
import cl.joaedu.capacityservice.repository.CapacityCounterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CapacityService {

    private static final Logger log = LoggerFactory.getLogger(CapacityService.class);

    private final CapacityCounterRepository repo;

    public CapacityService(CapacityCounterRepository repo) {
        this.repo = repo;
    }

    public CapacityResponse increment(Long branchId) {
        CapacityCounter c = repo.findByBranchId(branchId)
                .orElseGet(() -> new CapacityCounter(branchId, 0, 100, 0.0, LocalDateTime.now()));
        c.setPersonasActuales(c.getPersonasActuales() + 1);
        c.setPorcentajeOcupacion((c.getPersonasActuales() * 100.0) / c.getCapacidadMaxima());
        c.setUltimaActualizacion(LocalDateTime.now());
        CapacityCounter saved = repo.save(c);
        log.info("Aforo incrementado sucursal #{}: {} personas ({}%)", branchId, saved.getPersonasActuales(), saved.getPorcentajeOcupacion());
        return mapToResponse(branchId, saved);
    }

    public CapacityResponse decrement(Long branchId) {
        CapacityCounter c = repo.findByBranchId(branchId)
                .orElseGet(() -> new CapacityCounter(branchId, 0, 100, 0.0, LocalDateTime.now()));
        if (c.getPersonasActuales() > 0) {
            c.setPersonasActuales(c.getPersonasActuales() - 1);
        }
        c.setPorcentajeOcupacion((c.getPersonasActuales() * 100.0) / c.getCapacidadMaxima());
        c.setUltimaActualizacion(LocalDateTime.now());
        CapacityCounter saved = repo.save(c);
        log.info("Aforo decrementado sucursal #{}: {} personas ({}%)", branchId, saved.getPersonasActuales(), saved.getPorcentajeOcupacion());
        return mapToResponse(branchId, saved);
    }

    private CapacityResponse mapToResponse(Long branchId, CapacityCounter c) {
        return new CapacityResponse(c.getId(), branchId, c.getPersonasActuales(), c.getCapacidadMaxima(),
                c.getPorcentajeOcupacion(), c.getUltimaActualizacion());
    }
}
