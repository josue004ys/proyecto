package com.example.proyecto.service;

import com.example.proyecto.entity.Cita;
import com.example.proyecto.entity.Paciente;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ReprogramacionValidationService {

    // Constantes para las reglas de negocio
    private static final int MAX_REPROGRAMACIONES_POR_CITA = 2;
    private static final int HORAS_MINIMAS_ANTICIPACION = 24;
    private static final int MAX_REPROGRAMACIONES_POR_MES = 5; // Máximo 5 reprogramaciones por mes
    private static final int DIAS_BLOQUEO_POR_ABUSO = 30; // 30 días de bloqueo por abuso

    /**
     * Validar si una cita puede ser reprogramada
     */
    public ValidationResult validarReprogramacion(Cita cita, Paciente paciente) {
        // 1. Verificar si el paciente está bloqueado por abuso
        if (paciente.getBloqueadoPorAbuso() && 
            paciente.getFechaDesbloqueoPorAbuso() != null && 
            LocalDate.now().isBefore(paciente.getFechaDesbloqueoPorAbuso())) {
            return ValidationResult.error(
                "Temporalmente no puede reprogramar citas hasta el " + 
                paciente.getFechaDesbloqueoPorAbuso() + 
                " debido a reprogramaciones excesivas."
            );
        }

        // 2. Verificar límite de reprogramaciones por cita (máximo 2 veces)
        if (cita.getNumeroReprogramaciones() >= MAX_REPROGRAMACIONES_POR_CITA) {
            return ValidationResult.error(
                "Esta cita ya ha sido reprogramada " + MAX_REPROGRAMACIONES_POR_CITA + 
                " veces. No se pueden realizar más reprogramaciones."
            );
        }

        // 3. Verificar tiempo mínimo de anticipación (24 horas)
        LocalDateTime fechaHoraCita = LocalDateTime.of(cita.getFecha(), cita.getHora());
        long horasHastaaCita = ChronoUnit.HOURS.between(LocalDateTime.now(), fechaHoraCita);
        
        if (horasHastaaCita < HORAS_MINIMAS_ANTICIPACION) {
            return ValidationResult.error(
                "No se puede reprogramar con menos de " + HORAS_MINIMAS_ANTICIPACION + 
                " horas de anticipación. Quedan " + Math.max(0, horasHastaaCita) + " horas."
            );
        }

        // 4. Verificar límite mensual de reprogramaciones del paciente
        if (debeResetearContadorMensual(paciente)) {
            // Resetear contador si ha pasado un mes
            paciente.setReprogramacionesUltimoMes(0);
            paciente.setFechaUltimaReprogramacion(LocalDate.now());
        }

        if (paciente.getReprogramacionesUltimoMes() >= MAX_REPROGRAMACIONES_POR_MES) {
            // Bloquear al paciente por abuso
            paciente.setBloqueadoPorAbuso(true);
            paciente.setFechaDesbloqueoPorAbuso(LocalDate.now().plusDays(DIAS_BLOQUEO_POR_ABUSO));
            
            return ValidationResult.error(
                "Ha excedido el límite mensual de " + MAX_REPROGRAMACIONES_POR_MES + 
                " reprogramaciones. Su cuenta ha sido temporalmente restringida hasta el " +
                paciente.getFechaDesbloqueoPorAbuso() + "."
            );
        }

        return ValidationResult.success("La reprogramación es válida.");
    }

    /**
     * Actualizar contadores después de una reprogramación exitosa
     */
    public void actualizarContadoresReprogramacion(Cita cita, Paciente paciente) {
        // Actualizar contador de la cita
        cita.setNumeroReprogramaciones(cita.getNumeroReprogramaciones() + 1);
        cita.setUltimaReprogramacion(LocalDateTime.now());
        cita.setFechaModificacion(LocalDateTime.now());

        // Actualizar contador mensual del paciente
        if (debeResetearContadorMensual(paciente)) {
            paciente.setReprogramacionesUltimoMes(1);
        } else {
            paciente.setReprogramacionesUltimoMes(paciente.getReprogramacionesUltimoMes() + 1);
        }
        paciente.setFechaUltimaReprogramacion(LocalDate.now());
    }

    /**
     * Verificar si debe resetear el contador mensual
     */
    private boolean debeResetearContadorMensual(Paciente paciente) {
        if (paciente.getFechaUltimaReprogramacion() == null) {
            return true;
        }
        
        LocalDate fechaLimite = paciente.getFechaUltimaReprogramacion().plusMonths(1);
        return LocalDate.now().isAfter(fechaLimite);
    }

    /**
     * Desbloquear paciente si ya pasó la fecha de desbloqueo
     */
    public void verificarYDesbloquearPaciente(Paciente paciente) {
        if (paciente.getBloqueadoPorAbuso() && 
            paciente.getFechaDesbloqueoPorAbuso() != null &&
            LocalDate.now().isAfter(paciente.getFechaDesbloqueoPorAbuso())) {
            
            paciente.setBloqueadoPorAbuso(false);
            paciente.setFechaDesbloqueoPorAbuso(null);
            paciente.setReprogramacionesUltimoMes(0);
        }
    }

    /**
     * Clase para encapsular el resultado de la validación
     */
    public static class ValidationResult {
        private final boolean valido;
        private final String mensaje;

        private ValidationResult(boolean valido, String mensaje) {
            this.valido = valido;
            this.mensaje = mensaje;
        }

        public static ValidationResult success(String mensaje) {
            return new ValidationResult(true, mensaje);
        }

        public static ValidationResult error(String mensaje) {
            return new ValidationResult(false, mensaje);
        }

        public boolean isValido() { return valido; }
        public String getMensaje() { return mensaje; }
    }
}
