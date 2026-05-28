package cl.joaedu.capacityservice.service;

import cl.joaedu.capacityservice.model.CapacityCounter;
import cl.joaedu.capacityservice.repository.CapacityCounterRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class CapacityService {
    private final CapacityCounterRepository repo;

    public CapacityService(CapacityCounterRepository repo) {
        this.repo = repo;
    }

    public CapacityCounter increment(Long branchId) {
        CapacityCounter c = repo.findByBranchId(branchId).orElse(new CapacityCounter(branchId, 0, 100, 0.0, LocalDateTime.now()));
        c.setPersonasActuales(c.getPersonasActuales() + 1);
        c.setPorcentajeOcupacion((c.getPersonasActuales() * 100.0) / c.getCapacidadMaxima());
        c.setUltimaActualizacion(LocalDateTime.now());
        return repo.save(c);
    }

    public CapacityCounter decrement(Long branchId) {
        CapacityCounter c = repo.findByBranchId(branchId).orElse(new CapacityCounter(branchId, 0, 100, 0.0, LocalDateTime.now()));
        if (c.getPersonasActuales() > 0) {
            c.setPersonasActuales(c.getPersonasActuales() - 1);
        }
        c.setPorcentajeOcupacion((c.getPersonasActuales() * 100.0) / c.getCapacidadMaxima());
        c.setUltimaActualizacion(LocalDateTime.now());
        return repo.save(c);
    }
}