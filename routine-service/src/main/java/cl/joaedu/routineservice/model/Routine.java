package cl.joaedu.routineservice.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "routines")
public class Routine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String nombre;
    private String objetivo;
    private String creadoPor;
    private LocalDate fechaCreacion;

    public Routine() {}
    public Routine(Long userId, String nombre, String objetivo, String creadoPor, LocalDate fechaCreacion) {
        this.userId = userId;
        this.nombre = nombre;
        this.objetivo = objetivo;
        this.creadoPor = creadoPor;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getNombre() { return nombre; }
    public String getObjetivo() { return objetivo; }
    public String getCreadoPor() { return creadoPor; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }
}