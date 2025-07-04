package com.example.proyecto.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.example.proyecto.entity.HorarioDoctor;

public interface HorarioDoctorService {
    List<HorarioDoctor> obtenerTodosHorarios();
    List<HorarioDoctor> obtenerHorarioPorDia(DayOfWeek dia);
    List<HorarioDoctor> obtenerHorariosPorDoctor(Long doctorId);
    HorarioDoctor crearHorario(Long doctorId, DayOfWeek dia, LocalTime horaInicio, LocalTime horaFin, Integer duracionCita);
    HorarioDoctor actualizarHorario(Long horarioId, LocalTime horaInicio, LocalTime horaFin, Integer duracionCita);
    HorarioDoctor actualizarHorario(HorarioDoctor horario);
    void eliminarHorario(Long horarioId);
    void bloquearHorario(Long horarioId, String motivo);
    void activarHorario(Long horarioId);
    void cambiarEstadoHorario(Long horarioId, HorarioDoctor.EstadoHorario estado);
    List<LocalTime> obtenerHorasDisponibles(Long doctorId, LocalDate fecha);
    List<HorarioDoctor> obtenerHorariosSemana(Long doctorId);
    
    // Nuevos métodos para el dashboard
    HorarioDoctor guardarHorario(HorarioDoctor horario);
    HorarioDoctor obtenerHorarioPorId(Long horarioId);
    Optional<HorarioDoctor> obtenerHorarioPorIdOptional(Long horarioId);
}