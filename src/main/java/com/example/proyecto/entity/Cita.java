package com.example.proyecto.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "citas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;

    private LocalTime hora;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado = EstadoCita.PENDIENTE;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    @JsonBackReference("paciente-citas")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @JsonBackReference("doctor-citas")
    private Doctor doctor;

    private String motivoConsulta;

    private String observacionesPaciente;

    private String diagnostico; // Solo puede ser llenado por el doctor

    private String tratamiento; // Solo puede ser llenado por el doctor

    private String observacionesDoctor; // Solo puede ser llenado por el doctor

    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private LocalDateTime fechaModificacion;

    // Para sistema de pagos
    @Embedded
    private FacturacionCita facturacion;

    public enum EstadoCita {
        PENDIENTE("Pendiente"),
        CONFIRMADA("Confirmada"),
        EN_ATENCION("En Atención"),
        ATENDIDA("Atendida"),
        CANCELADA("Cancelada"),
        NO_ASISTIO("No Asistió");

        private final String descripcion;

        EstadoCita(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}