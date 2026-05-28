package cl.joaedu.capacityservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "capacity_counters")
public class CapacityCounter {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long branchId;
    private Integer personasActuales;
    private Integer capacidadMaxima;
    private Double porcentajeOcupacion;
    private LocalDateTime ultimaActualizacion;

    public CapacityCounter() {}
    public CapacityCounter(Long branchId, Integer personasActuales, Integer capacidadMaxima, Double porcentajeOcupacion, LocalDateTime ultimaActualizacion) {
        this.branchId = branchId;
        this.personasActuales = personasActuales;
        this.capacidadMaxima = capacidadMaxima;
        this.porcentajeOcupacion = porcentajeOcupacion;
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public Long getId() { return id; }
    public Integer getPersonasActuales() { return personasActuales; }
    public void setPersonasActuales(Integer personasActuales) { this.personasActuales = personasActuales; }
    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public void setPorcentajeOcupacion(Double porcentajeOcupacion) { this.porcentajeOcupacion = porcentajeOcupacion; }
    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }
    public Double getPorcentajeOcupacion() { return porcentajeOcupacion; }
}