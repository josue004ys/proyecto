package com.example.proyecto.repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.proyecto.entity.Cita;
import com.example.proyecto.entity.Doctor;
import com.example.proyecto.entity.Paciente;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Búsquedas básicas
    List<Cita> findByPacienteOrderByFechaDescHoraDesc(Paciente paciente);
    List<Cita> findByDoctorOrderByFechaAscHoraAsc(Doctor doctor);
    List<Cita> findByFechaOrderByHoraAsc(LocalDate fecha);
    
    // Búsquedas con fetch join para cargar las relaciones
    @Query("SELECT c FROM Cita c JOIN FETCH c.doctor JOIN FETCH c.paciente WHERE c.paciente = :paciente ORDER BY c.fecha DESC, c.hora DESC")
    List<Cita> findByPacienteWithDetailsOrderByFechaDescHoraDesc(@Param("paciente") Paciente paciente);
    
    @Query("SELECT c FROM Cita c JOIN FETCH c.doctor JOIN FETCH c.paciente WHERE c.doctor = :doctor ORDER BY c.fecha ASC, c.hora ASC")
    List<Cita> findByDoctorWithDetailsOrderByFechaAscHoraAsc(@Param("doctor") Doctor doctor);
    
    @Query("SELECT c FROM Cita c JOIN FETCH c.doctor JOIN FETCH c.paciente WHERE c.fecha = :fecha ORDER BY c.hora ASC")
    List<Cita> findByFechaWithDetailsOrderByHoraAsc(@Param("fecha") LocalDate fecha);
    
    // Verificaciones de disponibilidad
    boolean existsByFechaAndHoraAndDoctorIdAndEstadoNot(
        LocalDate fecha, LocalTime hora, Long doctorId, Cita.EstadoCita estado);
    
    boolean existsByDoctorIdAndFechaAndHora(Long doctorId, LocalDate fecha, LocalTime hora);
    
    // Búsquedas por doctor y fecha
    List<Cita> findByDoctorAndFechaOrderByHoraAsc(Doctor doctor, LocalDate fecha);
    
    // Verificar citas futuras para horarios
    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE c.doctor.id = :doctorId " +
           "AND c.fecha >= :fechaMinima " +
           "AND FUNCTION('DAYOFWEEK', c.fecha) = :diaSemana " +
           "AND c.estado NOT IN ('CANCELADA')")
    boolean existsCitasFuturasPorHorario(
        @Param("doctorId") Long doctorId, 
        @Param("diaSemana") DayOfWeek diaSemana, 
        @Param("fechaMinima") LocalDate fechaMinima);
    
    // Estadísticas
    @Query("SELECT COUNT(c) FROM Cita c WHERE c.doctor.id = :doctorId " +
           "AND MONTH(c.fecha) = :mes AND YEAR(c.fecha) = :año " +
           "AND c.estado = 'ATENDIDA'")
    Long contarCitasAtendidasPorMes(
        @Param("doctorId") Long doctorId, 
        @Param("mes") int mes, 
        @Param("año") int año);
    
    @Query("SELECT COUNT(DISTINCT c.paciente.id) FROM Cita c WHERE c.doctor.id = :doctorId " +
           "AND c.estado = 'ATENDIDA'")
    Long contarPacientesAtendidosPorDoctor(@Param("doctorId") Long doctorId);
    
    // Búsquedas por estado
    List<Cita> findByEstadoAndFechaOrderByHoraAsc(Cita.EstadoCita estado, LocalDate fecha);
    
    // Citas pendientes de un doctor
    List<Cita> findByDoctorAndEstadoOrderByFechaAscHoraAsc(Doctor doctor, Cita.EstadoCita estado);
    
    // Verificar si ya existe una cita para el mismo paciente con el mismo doctor en la misma fecha
    List<Cita> findByPacienteIdAndDoctorIdAndFecha(Long pacienteId, Long doctorId, LocalDate fecha);
}
