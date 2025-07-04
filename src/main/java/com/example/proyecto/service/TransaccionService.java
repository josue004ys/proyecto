package com.example.proyecto.service;

import com.example.proyecto.entity.Transaccion;
import com.example.proyecto.entity.Cita;
import com.example.proyecto.entity.Paciente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransaccionService {
    
    Transaccion procesarPago(Cita cita, BigDecimal monto, Transaccion.MetodoPago metodoPago);
    
    Transaccion procesarReembolso(Transaccion transaccionOriginal, BigDecimal monto, String motivo);
    
    List<Transaccion> obtenerTransaccionesPorPaciente(Paciente paciente);
    
    List<Transaccion> obtenerTransaccionesEnPeriodo(LocalDateTime inicio, LocalDateTime fin);
    
    BigDecimal calcularTotalRecaudado(LocalDateTime inicio, LocalDateTime fin);
    
    BigDecimal calcularTotalPagadoPorPaciente(Paciente paciente);
    
    Transaccion buscarPorId(Long id);
    
    void marcarTransaccionComoExitosa(Long transaccionId, String numeroAutorizacion);
    
    void marcarTransaccionComoFallida(Long transaccionId, String motivoFallo);
    
    List<Transaccion> obtenerTransaccionesPendientes();
}
