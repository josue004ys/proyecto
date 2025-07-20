package com.example.proyecto.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import com.example.proyecto.dto.CitaDTO;
import com.example.proyecto.entity.Cita;
import com.example.proyecto.entity.Doctor;
import com.example.proyecto.entity.Paciente;

public interface CitaService {
    Cita agendarCita(Paciente paciente, LocalDate fecha, LocalTime hora, Long doctorId, String motivoConsulta);
    List<Cita> listarCitasPorPaciente(Paciente paciente);
    List<Cita> listarCitasPorDoctor(Doctor doctor);
    boolean estaDisponible(LocalDate fecha, LocalTime hora, Long doctorId);
    Cita marcarComoAtendido(Long id, String diagnostico, String tratamiento, String observacionesDoctor);
    void cancelarCita(Long id, String motivo);
    Cita confirmarCita(Long id);
    List<Cita> obtenerCitasDelDia(LocalDate fecha);
    List<Cita> obtenerTodasLasCitas();
    
    // ========= NUEVOS MÉTODOS PARA GESTIÓN DE CITAS =========
    
    /**
     * Reprogramar una cita cuando el doctor no puede atender
     */
    CitaDTO reprogramarCita(Long citaId, LocalDate nuevaFecha, LocalTime nuevaHora, String motivo, String mensajePaciente);
    
    /**
     * Cancelar una cita por parte del doctor con notificación al paciente
     */
    CitaDTO cancelarCitaDoctor(Long citaId, String motivo, String mensajePaciente);
    
    /**
     * Reasignar una cita a otro doctor de la misma especialidad
     */
    CitaDTO reasignarCita(Long citaId, Long nuevoDoctorId, String motivo, String mensajePaciente);
    
    /**
     * Obtener doctores disponibles de la misma especialidad para reasignación
     */
    List<Doctor> obtenerDoctoresDisponiblesParaReasignacion(Long citaId);
    
    /**
     * Obtener historial de cambios de una cita
     */
    List<Map<String, Object>> obtenerHistorialCambios(Long citaId);
    
    /**
     * Obtener horarios disponibles para un doctor en una fecha específica
     */
    List<LocalTime> obtenerHorariosDisponibles(Long doctorId, LocalDate fecha);
    
    /**
     * Obtener días de la semana en que el doctor tiene horarios disponibles
     */
    List<String> obtenerDiasDisponibles(Long doctorId);

    /**
     * Obtener una cita por su ID
     */
    Cita obtenerCitaPorId(Long citaId);
    
    /**
     * Verificar disponibilidad de un doctor en fecha y hora específica
     */
    boolean verificarDisponibilidad(LocalDate fecha, LocalTime hora, Long doctorId);
    
    /**
     * Verificar si ya existe una cita para el mismo paciente con el mismo doctor en la misma fecha
     */
    boolean verificarCitaExistenteMismoDia(Long pacienteId, Long doctorId, LocalDate fecha);
}