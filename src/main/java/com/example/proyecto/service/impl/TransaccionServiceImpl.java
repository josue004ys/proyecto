package com.example.proyecto.service.impl;

import com.example.proyecto.entity.Transaccion;
import com.example.proyecto.entity.Cita;
import com.example.proyecto.entity.Paciente;
import com.example.proyecto.repository.TransaccionRepository;
import com.example.proyecto.service.TransaccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransaccionServiceImpl implements TransaccionService {

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Override
    public Transaccion procesarPago(Cita cita, BigDecimal monto, Transaccion.MetodoPago metodoPago) {
        Transaccion transaccion = new Transaccion();
        transaccion.setCita(cita);
        transaccion.setPaciente(cita.getPaciente());
        transaccion.setMonto(monto);
        transaccion.setTipo(Transaccion.TipoTransaccion.PAGO);
        transaccion.setMetodoPago(metodoPago);
        transaccion.setNumeroTransaccion(generarNumeroTransaccion());
        transaccion.setDescripcion("Pago por consulta médica - Cita ID: " + cita.getId());
        transaccion.setEstado(Transaccion.EstadoTransaccion.PROCESANDO);

        // Simular proceso de pago (en producción sería integración con pasarela de pago)
        if (simularProcesoPago(metodoPago)) {
            transaccion.setEstado(Transaccion.EstadoTransaccion.EXITOSA);
            transaccion.setNumeroAutorizacion(generarNumeroAutorizacion());
            
            // Actualizar información de facturación en la cita
            if (cita.getFacturacion() == null) {
                cita.setFacturacion(new com.example.proyecto.entity.FacturacionCita());
            }
            cita.getFacturacion().setCosto(monto);
            cita.getFacturacion().setEstadoPago(com.example.proyecto.entity.FacturacionCita.EstadoPago.PAGADO);
            cita.getFacturacion().setMetodoPago(convertirMetodoPago(metodoPago));
            cita.getFacturacion().setNumeroTransaccion(transaccion.getNumeroTransaccion());
            cita.getFacturacion().setFechaPago(LocalDateTime.now());
        } else {
            transaccion.setEstado(Transaccion.EstadoTransaccion.FALLIDA);
            transaccion.setObservaciones("Error en el procesamiento del pago");
        }

        return transaccionRepository.save(transaccion);
    }

    @Override
    public Transaccion procesarReembolso(Transaccion transaccionOriginal, BigDecimal monto, String motivo) {
        if (transaccionOriginal.getEstado() != Transaccion.EstadoTransaccion.EXITOSA) {
            throw new RuntimeException("Solo se pueden reembolsar transacciones exitosas");
        }

        Transaccion reembolso = new Transaccion();
        reembolso.setCita(transaccionOriginal.getCita());
        reembolso.setPaciente(transaccionOriginal.getPaciente());
        reembolso.setMonto(monto.negate()); // Monto negativo para reembolso
        reembolso.setTipo(Transaccion.TipoTransaccion.REEMBOLSO);
        reembolso.setMetodoPago(transaccionOriginal.getMetodoPago());
        reembolso.setNumeroTransaccion(generarNumeroTransaccion());
        reembolso.setDescripcion("Reembolso - Transacción original: " + transaccionOriginal.getNumeroTransaccion());
        reembolso.setObservaciones(motivo);
        reembolso.setEstado(Transaccion.EstadoTransaccion.EXITOSA);
        reembolso.setNumeroAutorizacion(generarNumeroAutorizacion());

        // Actualizar estado de pago en la cita
        if (transaccionOriginal.getCita().getFacturacion() != null) {
            transaccionOriginal.getCita().getFacturacion().setEstadoPago(
                com.example.proyecto.entity.FacturacionCita.EstadoPago.REEMBOLSADO);
        }

        return transaccionRepository.save(reembolso);
    }

    @Override
    public List<Transaccion> obtenerTransaccionesPorPaciente(Paciente paciente) {
        return transaccionRepository.findByPaciente(paciente);
    }

    @Override
    public List<Transaccion> obtenerTransaccionesEnPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        return transaccionRepository.findTransaccionesEnPeriodo(inicio, fin);
    }

    @Override
    public BigDecimal calcularTotalRecaudado(LocalDateTime inicio, LocalDateTime fin) {
        List<Transaccion> transacciones = transaccionRepository.findTransaccionesEnPeriodo(inicio, fin);
        return transacciones.stream()
                .filter(t -> t.getEstado() == Transaccion.EstadoTransaccion.EXITOSA && 
                           t.getTipo() == Transaccion.TipoTransaccion.PAGO)
                .map(Transaccion::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calcularTotalPagadoPorPaciente(Paciente paciente) {
        return transaccionRepository.calcularTotalPagadoPorPaciente(paciente);
    }

    @Override
    public Transaccion buscarPorId(Long id) {
        return transaccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));
    }

    @Override
    public void marcarTransaccionComoExitosa(Long transaccionId, String numeroAutorizacion) {
        Transaccion transaccion = buscarPorId(transaccionId);
        transaccion.setEstado(Transaccion.EstadoTransaccion.EXITOSA);
        transaccion.setNumeroAutorizacion(numeroAutorizacion);
        transaccionRepository.save(transaccion);
    }

    @Override
    public void marcarTransaccionComoFallida(Long transaccionId, String motivoFallo) {
        Transaccion transaccion = buscarPorId(transaccionId);
        transaccion.setEstado(Transaccion.EstadoTransaccion.FALLIDA);
        transaccion.setObservaciones(motivoFallo);
        transaccionRepository.save(transaccion);
    }

    @Override
    public List<Transaccion> obtenerTransaccionesPendientes() {
        return transaccionRepository.findByEstado(Transaccion.EstadoTransaccion.PROCESANDO);
    }

    // Métodos auxiliares privados
    private String generarNumeroTransaccion() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generarNumeroAutorizacion() {
        return "AUTH-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private boolean simularProcesoPago(Transaccion.MetodoPago metodoPago) {
        // Simulación simple - en producción sería integración real con pasarela de pago
        // Simular 95% de éxito para efectivo, 90% para tarjetas
        double probabilidadExito = metodoPago == Transaccion.MetodoPago.EFECTIVO ? 0.95 : 0.90;
        return Math.random() < probabilidadExito;
    }

    private com.example.proyecto.entity.FacturacionCita.MetodoPago convertirMetodoPago(Transaccion.MetodoPago metodoPago) {
        return switch (metodoPago) {
            case EFECTIVO -> com.example.proyecto.entity.FacturacionCita.MetodoPago.EFECTIVO;
            case TARJETA_CREDITO -> com.example.proyecto.entity.FacturacionCita.MetodoPago.TARJETA_CREDITO;
            case TARJETA_DEBITO -> com.example.proyecto.entity.FacturacionCita.MetodoPago.TARJETA_DEBITO;
            case TRANSFERENCIA -> com.example.proyecto.entity.FacturacionCita.MetodoPago.TRANSFERENCIA;
            case SEGURO_MEDICO -> com.example.proyecto.entity.FacturacionCita.MetodoPago.SEGURO_MEDICO;
        };
    }
}
