package com.example.proyecto.service;

import com.example.proyecto.entity.Doctor;

import java.util.List;
import java.util.Optional;

public interface DoctorService {
    
    Doctor registrarDoctor(Doctor doctor);
    
    Optional<Doctor> buscarPorCorreo(String correo);
    
    List<Doctor> listarDoctoresActivos();
    
    List<Doctor> buscarPorEspecialidad(String especialidad);
    
    Doctor actualizarDoctor(Long id, Doctor doctor);
    
    void cambiarEstadoDoctor(Long id, Doctor.EstadoDoctor estado);
    
    List<Doctor> obtenerTodosLosDoctores();
    Doctor obtenerDoctorPorId(Long id);
    Optional<Doctor> obtenerDoctorPorIdOptional(Long id);
    Doctor crearDoctor(Doctor doctor);
    void eliminarDoctor(Long id);
    
    // Métodos para gestión de perfil por el doctor
    Doctor actualizarPerfil(Long doctorId, String nombre, String telefono, String especialidad);
    void cambiarPassword(Long doctorId, String passwordActual, String passwordNueva);
    
    // Métodos de estadísticas para el doctor
    Long contarCitasDelMes(Long doctorId, int mes, int año);
    Long contarPacientesAtendidos(Long doctorId);
}
