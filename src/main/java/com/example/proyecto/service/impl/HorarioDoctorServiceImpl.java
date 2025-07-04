package com.example.proyecto.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.entity.Doctor;
import com.example.proyecto.entity.HorarioDoctor;
import com.example.proyecto.repository.CitaRepository;
import com.example.proyecto.repository.DoctorRepository;
import com.example.proyecto.repository.HorarioDoctorRepository;
import com.example.proyecto.service.HorarioDoctorService;

@Service
@Transactional
public class HorarioDoctorServiceImpl implements HorarioDoctorService {

    @Autowired
    private HorarioDoctorRepository horarioRepo;
    
    @Autowired
    private DoctorRepository doctorRepo;
    
    @Autowired
    private CitaRepository citaRepo;

    @Override
    @Transactional(readOnly = true)
    public List<HorarioDoctor> obtenerTodosHorarios() {
        return horarioRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorarioDoctor> obtenerHorarioPorDia(DayOfWeek dia) {
        return horarioRepo.findByDiaAndEstado(dia, HorarioDoctor.EstadoHorario.ACTIVO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorarioDoctor> obtenerHorariosPorDoctor(Long doctorId) {
        return horarioRepo.findByDoctorIdOrderByDiaAsc(doctorId);
    }

    @Override
    @Transactional
    public HorarioDoctor crearHorario(Long doctorId, DayOfWeek dia, LocalTime horaInicio, LocalTime horaFin, Integer duracionCita) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));

        // Verificar que no exista conflicto de horarios
        List<HorarioDoctor> horariosExistentes = horarioRepo.findByDoctorIdAndDia(doctorId, dia);
        for (HorarioDoctor horarioExistente : horariosExistentes) {
            if (hayConflictoHorario(horaInicio, horaFin, horarioExistente.getHoraInicio(), horarioExistente.getHoraFin())) {
                throw new RuntimeException("Conflicto de horarios detectado para el día " + dia);
            }
        }

        HorarioDoctor nuevoHorario = new HorarioDoctor();
        nuevoHorario.setDoctor(doctor);
        nuevoHorario.setDia(dia);
        nuevoHorario.setHoraInicio(horaInicio);
        nuevoHorario.setHoraFin(horaFin);
        nuevoHorario.setDuracionCita(duracionCita != null ? duracionCita : 30);
        nuevoHorario.setEstado(HorarioDoctor.EstadoHorario.ACTIVO);

        return horarioRepo.save(nuevoHorario);
    }

    @Override
    @Transactional
    public HorarioDoctor actualizarHorario(Long horarioId, LocalTime horaInicio, LocalTime horaFin, Integer duracionCita) {
        HorarioDoctor horario = horarioRepo.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        // Verificar conflictos con otros horarios del mismo doctor
        List<HorarioDoctor> otrosHorarios = horarioRepo.findByDoctorIdAndDiaAndIdNot(
            horario.getDoctor().getId(), horario.getDia(), horarioId);
        
        for (HorarioDoctor otroHorario : otrosHorarios) {
            if (hayConflictoHorario(horaInicio, horaFin, otroHorario.getHoraInicio(), otroHorario.getHoraFin())) {
                throw new RuntimeException("Conflicto de horarios detectado");
            }
        }

        horario.setHoraInicio(horaInicio);
        horario.setHoraFin(horaFin);
        horario.setDuracionCita(duracionCita != null ? duracionCita : horario.getDuracionCita());

        return horarioRepo.save(horario);
    }

    @Override
    @Transactional
    public void eliminarHorario(Long horarioId) {
        HorarioDoctor horario = horarioRepo.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        // Verificar si hay citas futuras programadas
        boolean tieneCitasFuturas = citaRepo.existsCitasFuturasPorHorario(
            horario.getDoctor().getId(), horario.getDia(), LocalDate.now());
        
        if (tieneCitasFuturas) {
            throw new RuntimeException("No se puede eliminar el horario porque tiene citas programadas");
        }

        horarioRepo.delete(horario);
    }

    @Override
    @Transactional
    public void bloquearHorario(Long horarioId, String motivo) {
        HorarioDoctor horario = horarioRepo.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        horario.setEstado(HorarioDoctor.EstadoHorario.BLOQUEADO);
        horario.setObservaciones(motivo);
        horarioRepo.save(horario);
    }

    @Override
    @Transactional
    public void activarHorario(Long horarioId) {
        HorarioDoctor horario = horarioRepo.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        horario.setEstado(HorarioDoctor.EstadoHorario.ACTIVO);
        horario.setObservaciones(null);
        horarioRepo.save(horario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> obtenerHorasDisponibles(Long doctorId, LocalDate fecha) {
        DayOfWeek diaSemana = fecha.getDayOfWeek();
        List<HorarioDoctor> horarios = horarioRepo.findByDoctorIdAndDiaAndEstado(
            doctorId, diaSemana, HorarioDoctor.EstadoHorario.ACTIVO);

        List<LocalTime> horasDisponibles = new ArrayList<>();

        for (HorarioDoctor horario : horarios) {
            LocalTime horaActual = horario.getHoraInicio();
            while (horaActual.isBefore(horario.getHoraFin())) {
                // Verificar si ya hay una cita en esta hora
                if (!citaRepo.existsByDoctorIdAndFechaAndHora(doctorId, fecha, horaActual)) {
                    horasDisponibles.add(horaActual);
                }
                horaActual = horaActual.plusMinutes(horario.getDuracionCita());
            }
        }

        return horasDisponibles;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorarioDoctor> obtenerHorariosSemana(Long doctorId) {
        return horarioRepo.findByDoctorIdOrderByDiaAscHoraInicioAsc(doctorId);
    }

    @Override
    @Transactional
    public void cambiarEstadoHorario(Long horarioId, HorarioDoctor.EstadoHorario estado) {
        HorarioDoctor horario = horarioRepo.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));
        horario.setEstado(estado);
        horarioRepo.save(horario);
    }

    @Override
    @Transactional
    public HorarioDoctor guardarHorario(HorarioDoctor horario) {
        return horarioRepo.save(horario);
    }
    
    @Override
    @Transactional(readOnly = true)
    public HorarioDoctor obtenerHorarioPorId(Long horarioId) {
        return horarioRepo.findById(horarioId).orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<HorarioDoctor> obtenerHorarioPorIdOptional(Long horarioId) {
        return horarioRepo.findById(horarioId);
    }
    
    @Override
    @Transactional
    public HorarioDoctor actualizarHorario(HorarioDoctor horario) {
        return horarioRepo.save(horario);
    }

    private boolean hayConflictoHorario(LocalTime inicio1, LocalTime fin1, LocalTime inicio2, LocalTime fin2) {
        return inicio1.isBefore(fin2) && fin1.isAfter(inicio2);
    }
}