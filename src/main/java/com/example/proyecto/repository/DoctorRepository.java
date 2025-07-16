package com.example.proyecto.repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.proyecto.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    Optional<Doctor> findByCorreo(String correo);
    
    List<Doctor> findByEspecialidad(String especialidad);
    
    List<Doctor> findByEstado(Doctor.EstadoDoctor estado);
    
    // Métodos para reportes
    long countByEstado(Doctor.EstadoDoctor estado);
    
    long countByEspecialidad(String especialidad);
    
    @Query("SELECT d FROM Doctor d WHERE d.especialidad = :especialidad AND d.estado = 'ACTIVO'")
    List<Doctor> findDoctoresActivosPorEspecialidad(@Param("especialidad") String especialidad);
    
    @Query("SELECT d FROM Doctor d WHERE d.nombre LIKE %:nombre% AND d.estado = 'ACTIVO'")
    List<Doctor> buscarDoctoresPorNombre(@Param("nombre") String nombre);
    
    // Verificar si el doctor tiene horario disponible
    @Query("SELECT COUNT(h) > 0 FROM HorarioDoctor h WHERE h.doctor.id = :doctorId " +
           "AND h.dia = :diaSemana AND h.estado = 'ACTIVO' " +
           "AND h.horaInicio <= :hora AND h.horaFin > :hora")
    boolean existsHorarioDisponible(
        @Param("doctorId") Long doctorId, 
        @Param("diaSemana") DayOfWeek diaSemana, 
        @Param("hora") LocalTime hora);
    
    // Método para obtener doctores de la misma especialidad excluyendo uno específico  
    @Query("SELECT d FROM Doctor d WHERE d.especialidad = :especialidad AND d.id != :doctorId AND d.estado = 'ACTIVO'")
    List<Doctor> findByEspecialidadAndIdNot(@Param("especialidad") String especialidad, @Param("doctorId") Long doctorId);

}
