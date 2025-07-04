package com.example.proyecto.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultas_medicas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cita_id")
    private Cita cita;

    @ManyToOne
    @JoinColumn(name = "historial_id")
    private HistorialMedico historialMedico;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(length = 2000)
    private String motivoConsulta;

    @Column(length = 2000)
    private String sintomas;

    @Column(length = 2000)
    private String examenFisico;

    @Column(length = 2000)
    private String diagnostico;

    @Column(length = 2000)
    private String tratamiento;

    @Column(length = 2000)
    private String medicamentosRecetados;

    @Column(length = 1000)
    private String indicacionesGenerales;

    private LocalDateTime fechaConsulta = LocalDateTime.now();

    private String signosVitales; // JSON o formato estructurado

    @Enumerated(EnumType.STRING)
    private EstadoConsulta estado = EstadoConsulta.EN_PROGRESO;

    public enum EstadoConsulta {
        EN_PROGRESO("En Progreso"),
        COMPLETADA("Completada"),
        CANCELADA("Cancelada");

        private final String descripcion;

        EstadoConsulta(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}
