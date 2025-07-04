package com.example.proyecto.entity;

public enum RolUsuario {
    // Roles principales del sistema
    PACIENTE("ROLE_PACIENTE", "Paciente"),
    MEDICO("ROLE_MEDICO", "Médico/Doctor"),
    ASISTENTE("ROLE_ASISTENTE", "Asistente Médico"),
    ADMINISTRADOR("ROLE_ADMINISTRADOR", "Administrador del Sistema"),
    
    // Roles adicionales para un sistema completo
    RECEPCIONISTA("ROLE_RECEPCIONISTA", "Recepcionista"),
    ENFERMERO("ROLE_ENFERMERO", "Enfermero/a"),
    DIRECTOR_MEDICO("ROLE_DIRECTOR_MEDICO", "Director Médico");
    
    private final String rol;
    private final String descripcion;
    
    RolUsuario(String rol, String descripcion) {
        this.rol = rol;
        this.descripcion = descripcion;
    }
    
    public String getRol() {
        return rol;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    /**
     * Verifica si el rol tiene permisos administrativos
     */
    public boolean esAdministrativo() {
        return this == ADMINISTRADOR || this == DIRECTOR_MEDICO;
    }
    
    /**
     * Verifica si el rol es del personal médico
     */
    public boolean esPersonalMedico() {
        return this == MEDICO || this == ENFERMERO || this == DIRECTOR_MEDICO;
    }
    
    /**
     * Verifica si el rol puede gestionar citas
     */
    public boolean puedeGestionarCitas() {
        return this == RECEPCIONISTA || this == ASISTENTE || esAdministrativo();
    }
}
