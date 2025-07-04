package com.example.proyecto.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "historiales_medicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    private String tipoSangre;

    private Double peso;

    private Double altura;

    @Column(length = 1000)
    private String alergias;

    @Column(length = 1000)
    private String medicamentosActuales;

    @Column(length = 1000)
    private String enfermedadesCronicas;

    @Column(length = 1000)
    private String antecedentesQuirurgicos;

    @Column(length = 1000)
    private String antecedentesFamiliares;

    private String contactoEmergencia;

    private String telefonoEmergencia;

    private LocalDate fechaCreacion = LocalDate.now();

    private LocalDate fechaActualizacion;

    // Relación con consultas médicas
    @OneToMany(mappedBy = "historialMedico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ConsultaMedica> consultas;
}
