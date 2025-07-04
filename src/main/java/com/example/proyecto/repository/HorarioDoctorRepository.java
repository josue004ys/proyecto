package com.example.proyecto.repository;

import com.example.proyecto.entity.HorarioDoctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface HorarioDoctorRepository extends JpaRepository<HorarioDoctor, Long> {
    
    // Búsquedas básicas
    List<HorarioDoctor> findByDia(DayOfWeek dia);
    List<HorarioDoctor> findByDiaAndEstado(DayOfWeek dia, HorarioDoctor.EstadoHorario estado);
    
    // Búsquedas por doctor
    List<HorarioDoctor> findByDoctorIdOrderByDiaAsc(Long doctorId);
    List<HorarioDoctor> findByDoctorIdOrderByDiaAscHoraInicioAsc(Long doctorId);
    List<HorarioDoctor> findByDoctorIdAndDia(Long doctorId, DayOfWeek dia);
    List<HorarioDoctor> findByDoctorIdAndDiaAndEstado(Long doctorId, DayOfWeek dia, HorarioDoctor.EstadoHorario estado);
    
    // Para verificar conflictos de horarios
    List<HorarioDoctor> findByDoctorIdAndDiaAndIdNot(Long doctorId, DayOfWeek dia, Long horarioId);
}