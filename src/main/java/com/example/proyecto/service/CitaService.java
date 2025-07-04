package com.example.proyecto.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
}