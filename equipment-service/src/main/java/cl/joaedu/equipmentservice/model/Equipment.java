package cl.joaedu.equipmentservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment")
public class Equipment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String categoria;
    private Long branchId;
    private String estado;

    public Equipment() {}
    public Equipment(String nombre, String categoria, Long branchId, String estado) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.branchId = branchId;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setId(Long id) { this.id = id; }
    public String getCategoria() { return categoria; }
    public Long getBranchId() { return branchId; }
    public String getEstado() { return estado; }
}