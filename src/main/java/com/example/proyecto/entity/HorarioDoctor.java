package com.example.proyecto.entity;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "horarios_doctor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioDoctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    @JsonBackReference
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dia; // LUNES, MARTES, etc.

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private Integer duracionCita = 30; // Duración en minutos por cita

    @Enumerated(EnumType.STRING)
    private EstadoHorario estado = EstadoHorario.ACTIVO;

    private String observaciones;

    public enum EstadoHorario {
        ACTIVO("Activo"),
        INACTIVO("Inactivo"),
        BLOQUEADO("Bloqueado");

        private final String descripcion;

        EstadoHorario(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}