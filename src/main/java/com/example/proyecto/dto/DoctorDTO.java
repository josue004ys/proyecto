package com.example.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDTO {
    private String nombre;
    private String correo;
    private String password;
    private String especialidad;
    private String telefono;
    private String numeroLicencia;
}
