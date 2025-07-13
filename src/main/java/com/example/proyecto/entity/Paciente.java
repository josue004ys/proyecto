package com.example.proyecto.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "pacientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private RolUsuario rol = RolUsuario.PACIENTE; // Por defecto es paciente
    
    private String nombre;

    @Column(unique = true)
    private String correo;

    private String password;

    // Información adicional del paciente
    private String apellidos;

    private String telefono;

    private String direccion;

    private String ciudad;

    private String codigoPostal;

    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    private Genero genero;

    private String numeroIdentificacion;

    @Enumerated(EnumType.STRING)
    private TipoIdentificacion tipoIdentificacion;

    private String ocupacion;

    private String estadoCivil;

    // Información de seguro médico
    private String seguroMedico;

    private String numeroPoliza;

    // Relaciones
    @OneToOne(mappedBy = "paciente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private HistorialMedico historialMedico;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Cita> citas;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Transaccion> transacciones;

    // Método auxiliar para obtener el rol como String
    public String getRolString() {
        return this.rol != null ? this.rol.getRol() : RolUsuario.PACIENTE.getRol();
    }

    // Enums
    public enum Genero {
        MASCULINO("Masculino"),
        FEMENINO("Femenino"),
        OTRO("Otro");

        private final String descripcion;

        Genero(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    public enum TipoIdentificacion {
        CEDULA("Cédula de Ciudadanía"),
        PASAPORTE("Pasaporte"),
        TARJETA_IDENTIDAD("Tarjeta de Identidad"),
        CEDULA_EXTRANJERIA("Cédula de Extranjería");

        private final String descripcion;

        TipoIdentificacion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}