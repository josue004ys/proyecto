package com.example.proyecto.repository;

import com.example.proyecto.entity.Transaccion;
import com.example.proyecto.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    
    List<Transaccion> findByPaciente(Paciente paciente);
    
    List<Transaccion> findByEstado(Transaccion.EstadoTransaccion estado);
    
    List<Transaccion> findByTipo(Transaccion.TipoTransaccion tipo);
    
    List<Transaccion> findByFechaTransaccionBetween(LocalDateTime inicio, LocalDateTime fin);
    
    @Query("SELECT SUM(t.monto) FROM Transaccion t WHERE t.paciente = :paciente AND t.estado = 'EXITOSA'")
    BigDecimal calcularTotalPagadoPorPaciente(@Param("paciente") Paciente paciente);
    
    @Query("SELECT t FROM Transaccion t WHERE t.paciente = :paciente AND t.estado = 'EXITOSA' ORDER BY t.fechaTransaccion DESC")
    List<Transaccion> findTransaccionesExitosasPorPaciente(@Param("paciente") Paciente paciente);
    
    @Query("SELECT t FROM Transaccion t WHERE t.fechaTransaccion BETWEEN :inicio AND :fin AND t.estado = 'EXITOSA'")
    List<Transaccion> findTransaccionesEnPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
    
    // Buscar transacción por cita
    Transaccion findByCita(com.example.proyecto.entity.Cita cita);
    
    // Buscar transacciones por número
    Transaccion findByNumeroTransaccion(String numeroTransaccion);
}
