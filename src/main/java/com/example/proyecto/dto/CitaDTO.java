package com.example.proyecto.dto;

import java.time.LocalDateTime;

import com.example.proyecto.entity.Cita;

public class CitaDTO {
    private Long id;
    private String fechaHora; // Combinación de fecha y hora
    private String estado;
    private String motivoConsulta;
    private String doctorNombre;
    private String doctorCorreo;
    private String especialidad;
    private String pacienteNombre;
    private String pacienteCorreo;
    private String diagnostico;
    private String tratamiento;
    private String observacionesDoctor;
    private String tipoConsulta = "PRESENCIAL"; // Valor por defecto
    
    // Constructor vacío
    public CitaDTO() {}
    
    // Constructor que convierte de Cita a CitaDTO
    public CitaDTO(Cita cita) {
        this.id = cita.getId();
        
        // Combinar fecha y hora en un solo campo
        if (cita.getFecha() != null && cita.getHora() != null) {
            this.fechaHora = cita.getFecha().atTime(cita.getHora()).toString();
        }
        
        this.estado = cita.getEstado() != null ? cita.getEstado().name() : "PENDIENTE";
        this.motivoConsulta = cita.getMotivoConsulta();
        this.diagnostico = cita.getDiagnostico();
        this.tratamiento = cita.getTratamiento();
        this.observacionesDoctor = cita.getObservacionesDoctor();
        
        // Información del doctor
        if (cita.getDoctor() != null) {
            this.doctorNombre = cita.getDoctor().getNombre();
            this.doctorCorreo = cita.getDoctor().getCorreo();
            this.especialidad = cita.getDoctor().getEspecialidad();
        }
        
        // Información del paciente
        if (cita.getPaciente() != null) {
            this.pacienteNombre = cita.getPaciente().getNombre();
            this.pacienteCorreo = cita.getPaciente().getCorreo();
        }
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getMotivoConsulta() { return motivoConsulta; }
    public void setMotivoConsulta(String motivoConsulta) { this.motivoConsulta = motivoConsulta; }
    
    public String getDoctorNombre() { return doctorNombre; }
    public void setDoctorNombre(String doctorNombre) { this.doctorNombre = doctorNombre; }
    
    public String getDoctorCorreo() { return doctorCorreo; }
    public void setDoctorCorreo(String doctorCorreo) { this.doctorCorreo = doctorCorreo; }
    
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    
    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }
    
    public String getPacienteCorreo() { return pacienteCorreo; }
    public void setPacienteCorreo(String pacienteCorreo) { this.pacienteCorreo = pacienteCorreo; }
    
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    
    public String getObservacionesDoctor() { return observacionesDoctor; }
    public void setObservacionesDoctor(String observacionesDoctor) { this.observacionesDoctor = observacionesDoctor; }
    
    public String getTipoConsulta() { return tipoConsulta; }
    public void setTipoConsulta(String tipoConsulta) { this.tipoConsulta = tipoConsulta; }
}
