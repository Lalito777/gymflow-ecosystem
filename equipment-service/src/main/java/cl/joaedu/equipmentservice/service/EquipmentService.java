package cl.joaedu.equipmentservice.service;

import cl.joaedu.equipmentservice.dto.EquipmentRequest;
import cl.joaedu.equipmentservice.dto.EquipmentResponse;
import cl.joaedu.equipmentservice.model.Equipment;
import cl.joaedu.equipmentservice.repository.EquipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Capa service que faltaba en equipment-service: antes el controller llamaba
 * al repository directamente. Ahora toda la logica (incluida la validacion
 * del estado del equipo) vive aca.
 */
@Service
public class EquipmentService {

    private static final Logger log = LoggerFactory.getLogger(EquipmentService.class);

    private static final Set<String> ESTADOS_VALIDOS = Set.of("DISPONIBLE", "EN_MANTENCION", "FUERA_DE_SERVICIO");

    private final EquipmentRepository repository;

    public EquipmentService(EquipmentRepository repository) {
        this.repository = repository;
    }

    public List<EquipmentResponse> getByBranch(Long branchId) {
        return repository.findByBranchId(branchId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Regla de negocio: el estado del equipo debe ser uno de los valores del dominio.
     */
    public EquipmentResponse create(EquipmentRequest request) {
        if (!ESTADOS_VALIDOS.contains(request.estado())) {
            throw new IllegalArgumentException("Estado invalido: " + request.estado() +
                    ". Valores permitidos: " + ESTADOS_VALIDOS);
        }

        Equipment equipment = new Equipment(request.nombre(), request.categoria(), request.branchId(), request.estado());
        Equipment saved = repository.save(equipment);
        log.info("Equipo creado: ID #{} - {} - sucursal #{}", saved.getId(), saved.getNombre(), saved.getBranchId());
        return mapToResponse(saved);
    }

    private EquipmentResponse mapToResponse(Equipment equipment) {
        return new EquipmentResponse(equipment.getId(), equipment.getNombre(), equipment.getCategoria(),
                equipment.getBranchId(), equipment.getEstado());
    }
}
