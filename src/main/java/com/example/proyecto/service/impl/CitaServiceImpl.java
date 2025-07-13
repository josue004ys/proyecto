package com.example.proyecto.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.dto.CitaDTO;
import com.example.proyecto.entity.Cita;
import com.example.proyecto.entity.Doctor;
import com.example.proyecto.entity.FacturacionCita;
import com.example.proyecto.entity.Paciente;
import com.example.proyecto.entity.Transaccion;
import com.example.proyecto.repository.CitaRepository;
import com.example.proyecto.repository.DoctorRepository;
import com.example.proyecto.repository.TransaccionRepository;
import com.example.proyecto.service.CitaService;

@Service
@Transactional
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaRepository citaRepo;
    
    @Autowired
    private DoctorRepository doctorRepo;
    
    @Autowired
    private TransaccionRepository transaccionRepo;

    @Override
    @Transactional
    public Cita agendarCita(Paciente paciente, LocalDate fecha, LocalTime hora, Long doctorId, String motivoConsulta) {
        // Verificar disponibilidad
        if (!estaDisponible(fecha, hora, doctorId)) {
            throw new RuntimeException("La hora ya está ocupada o el doctor no está disponible");
        }

        // Buscar el doctor
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));

        // Crear la cita
        Cita cita = new Cita();
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setEstado(Cita.EstadoCita.PENDIENTE);
        cita.setPaciente(paciente);
        cita.setDoctor(doctor);
        cita.setMotivoConsulta(motivoConsulta);
        cita.setFechaCreacion(LocalDateTime.now());

        // Configurar facturación
        FacturacionCita facturacion = new FacturacionCita();
        facturacion.setCosto(new BigDecimal("50.00")); // Precio base
        cita.setFacturacion(facturacion);

        // Guardar la cita
        Cita citaGuardada = citaRepo.save(cita);

        // Crear transacción inicial
        Transaccion transaccion = new Transaccion();
        transaccion.setCita(citaGuardada);
        transaccion.setPaciente(paciente);
        transaccion.setMonto(facturacion.getCosto());
        transaccion.setEstado(Transaccion.EstadoTransaccion.PROCESANDO);
        transaccion.setDescripcion("Pago por consulta médica - " + doctor.getNombre());
        transaccion.setNumeroTransaccion("TXN-" + System.currentTimeMillis());
        
        transaccionRepo.save(transaccion);

        return citaGuardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitasPorPaciente(Paciente paciente) {
        return citaRepo.findByPacienteWithDetailsOrderByFechaDescHoraDesc(paciente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitasPorDoctor(Doctor doctor) {
        return citaRepo.findByDoctorWithDetailsOrderByFechaAscHoraAsc(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaDisponible(LocalDate fecha, LocalTime hora, Long doctorId) {
        // Verificar si ya existe una cita en esa fecha y hora para el doctor
        boolean existeCita = citaRepo.existsByFechaAndHoraAndDoctorIdAndEstadoNot(
            fecha, hora, doctorId, Cita.EstadoCita.CANCELADA);
        
        if (existeCita) {
            return false;
        }
        
        // Verificar si el doctor tiene horario disponible en ese día y hora
        return doctorRepo.existsHorarioDisponible(doctorId, fecha.getDayOfWeek(), hora);
    }

    @Override
    @Transactional
    public Cita marcarComoAtendido(Long id, String diagnostico, String tratamiento, String observacionesDoctor) {
        Cita cita = citaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        
        cita.setEstado(Cita.EstadoCita.ATENDIDA);
        cita.setDiagnostico(diagnostico);
        cita.setTratamiento(tratamiento);
        cita.setObservacionesDoctor(observacionesDoctor);
        cita.setFechaModificacion(LocalDateTime.now());
        
        // Actualizar transacción a completada
        Transaccion transaccion = transaccionRepo.findByCita(cita);
        if (transaccion != null) {
            transaccion.setEstado(Transaccion.EstadoTransaccion.EXITOSA);
            transaccionRepo.save(transaccion);
        }
        
        return citaRepo.save(cita);
    }

    @Override
    @Transactional
    public void cancelarCita(Long id, String motivo) {
        Cita cita = citaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        cita.setObservacionesDoctor(motivo);
        cita.setFechaModificacion(LocalDateTime.now());
        
        // Crear transacción de reembolso si es necesario
        Transaccion transaccionOriginal = transaccionRepo.findByCita(cita);
        if (transaccionOriginal != null && 
            transaccionOriginal.getEstado() == Transaccion.EstadoTransaccion.EXITOSA) {
            
            Transaccion reembolso = new Transaccion();
            reembolso.setCita(cita);
            reembolso.setPaciente(cita.getPaciente());
            reembolso.setMonto(transaccionOriginal.getMonto());
            reembolso.setTipo(Transaccion.TipoTransaccion.REEMBOLSO);
            reembolso.setEstado(Transaccion.EstadoTransaccion.PROCESANDO);
            reembolso.setDescripcion("Reembolso por cancelación de cita");
            reembolso.setNumeroTransaccion("REF-" + System.currentTimeMillis());
            
            transaccionRepo.save(reembolso);
        }
        
        citaRepo.save(cita);
    }

    @Override
    @Transactional
    public Cita confirmarCita(Long id) {
        Cita cita = citaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        
        cita.setEstado(Cita.EstadoCita.CONFIRMADA);
        cita.setFechaModificacion(LocalDateTime.now());
        
        return citaRepo.save(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> obtenerCitasDelDia(LocalDate fecha) {
        return citaRepo.findByFechaWithDetailsOrderByHoraAsc(fecha);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> obtenerTodasLasCitas() {
        return citaRepo.findAll();
    }

    // ========= IMPLEMENTACIÓN DE NUEVOS MÉTODOS =========

    @Override
    @Transactional
    public CitaDTO reprogramarCita(Long citaId, LocalDate nuevaFecha, LocalTime nuevaHora, String motivo, String mensajePaciente) {
        Cita cita = citaRepo.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        
        // Verificar que la nueva fecha/hora esté disponible
        if (!estaDisponible(nuevaFecha, nuevaHora, cita.getDoctor().getId())) {
            throw new RuntimeException("La nueva fecha/hora no está disponible");
        }
        
        // Guardar información original para el historial
        LocalDate fechaOriginal = cita.getFecha();
        LocalTime horaOriginal = cita.getHora();
        
        // Actualizar la cita
        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(Cita.EstadoCita.CONFIRMADA);
        cita.setFechaModificacion(LocalDateTime.now());
        cita.setObservacionesDoctor(cita.getObservacionesDoctor() + 
            "\n[REPROGRAMADA] Motivo: " + motivo + 
            "\nFecha original: " + fechaOriginal + " " + horaOriginal +
            "\nMensaje al paciente: " + mensajePaciente);
        
        cita = citaRepo.save(cita);
        
        // Aquí puedes agregar lógica para enviar notificación al paciente
        enviarNotificacionReprogramacion(cita, fechaOriginal, horaOriginal, mensajePaciente);
        
        return convertirACitaDTO(cita);
    }
    
    @Override
    @Transactional
    public CitaDTO cancelarCitaDoctor(Long citaId, String motivo, String mensajePaciente) {
        Cita cita = citaRepo.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        cita.setFechaModificacion(LocalDateTime.now());
        cita.setObservacionesDoctor(cita.getObservacionesDoctor() + 
            "\n[CANCELADA POR DOCTOR] Motivo: " + motivo +
            "\nMensaje al paciente: " + mensajePaciente);
        
        cita = citaRepo.save(cita);
        
        // Enviar notificación al paciente
        enviarNotificacionCancelacion(cita, motivo, mensajePaciente);
        
        return convertirACitaDTO(cita);
    }
    
    @Override
    @Transactional
    public CitaDTO reasignarCita(Long citaId, Long nuevoDoctorId, String motivo, String mensajePaciente) {
        Cita cita = citaRepo.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        
        Doctor doctorOriginal = cita.getDoctor();
        Doctor nuevoDoctor = doctorRepo.findById(nuevoDoctorId)
            .orElseThrow(() -> new RuntimeException("Nuevo doctor no encontrado"));
        
        // Verificar que ambos doctores tengan la misma especialidad
        if (!doctorOriginal.getEspecialidad().equals(nuevoDoctor.getEspecialidad())) {
            throw new RuntimeException("El nuevo doctor debe tener la misma especialidad");
        }
        
        // Verificar disponibilidad del nuevo doctor en la misma fecha/hora
        if (!estaDisponible(cita.getFecha(), cita.getHora(), nuevoDoctorId)) {
            throw new RuntimeException("El nuevo doctor no está disponible en esa fecha/hora");
        }
        
        cita.setDoctor(nuevoDoctor);
        cita.setEstado(Cita.EstadoCita.CONFIRMADA);
        cita.setFechaModificacion(LocalDateTime.now());
        cita.setObservacionesDoctor(cita.getObservacionesDoctor() + 
            "\n[REASIGNADA] Doctor original: " + doctorOriginal.getNombre() +
            "\nNuevo doctor: " + nuevoDoctor.getNombre() +
            "\nMotivo: " + motivo +
            "\nMensaje al paciente: " + mensajePaciente);
        
        cita = citaRepo.save(cita);
        
        // Enviar notificación al paciente
        enviarNotificacionReasignacion(cita, doctorOriginal, nuevoDoctor, mensajePaciente);
        
        return convertirACitaDTO(cita);
    }
    
    @Override
    public List<Doctor> obtenerDoctoresDisponiblesParaReasignacion(Long citaId) {
        Cita cita = citaRepo.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        
        // Obtener doctores de la misma especialidad, excluyendo el doctor actual
        return doctorRepo.findByEspecialidad(cita.getDoctor().getEspecialidad())
            .stream()
            .filter(doctor -> !doctor.getId().equals(cita.getDoctor().getId()))
            .filter(doctor -> doctor.getEstado() == Doctor.EstadoDoctor.ACTIVO)
            .toList();
    }
    
    @Override
    public List<Map<String, Object>> obtenerHistorialCambios(Long citaId) {
        // Por simplicidad, extraemos el historial de las observaciones del doctor
        Cita cita = citaRepo.findById(citaId)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        
        List<Map<String, Object>> historial = new ArrayList<>();
        
        // Agregar evento de creación
        historial.add(Map.of(
            "fecha", cita.getFechaCreacion(),
            "evento", "CREADA",
            "descripcion", "Cita agendada inicialmente",
            "usuario", "Sistema"
        ));
        
        // Si hay modificaciones, agregarlas
        if (cita.getFechaModificacion() != null) {
            historial.add(Map.of(
                "fecha", cita.getFechaModificacion(),
                "evento", "MODIFICADA",
                "descripcion", "Cita modificada por el doctor",
                "observaciones", cita.getObservacionesDoctor() != null ? cita.getObservacionesDoctor() : ""
            ));
        }
        
        return historial;
    }
    
    // ========= MÉTODOS AUXILIARES =========
    
    private CitaDTO convertirACitaDTO(Cita cita) {
        CitaDTO dto = new CitaDTO();
        dto.setId(cita.getId());
        
        // Convertir LocalDateTime a String
        LocalDateTime fechaHora = cita.getFecha().atTime(cita.getHora());
        dto.setFechaHora(fechaHora.toString());
        
        dto.setEstado(cita.getEstado().name());
        dto.setMotivoConsulta(cita.getMotivoConsulta());
        
        if (cita.getDoctor() != null) {
            dto.setDoctorNombre(cita.getDoctor().getNombre());
            dto.setDoctorCorreo(cita.getDoctor().getCorreo());
            dto.setEspecialidad(cita.getDoctor().getEspecialidad() != null ? 
                cita.getDoctor().getEspecialidad() : "Sin especialidad");
        }
        
        if (cita.getPaciente() != null) {
            dto.setPacienteNombre(cita.getPaciente().getNombre());
            dto.setPacienteCorreo(cita.getPaciente().getCorreo());
        }
        
        dto.setDiagnostico(cita.getDiagnostico());
        dto.setTratamiento(cita.getTratamiento());
        dto.setObservacionesDoctor(cita.getObservacionesDoctor());
        dto.setTipoConsulta("PRESENCIAL"); // Valor por defecto
        
        return dto;
    }
    
    private void enviarNotificacionReprogramacion(Cita cita, LocalDate fechaOriginal, LocalTime horaOriginal, String mensaje) {
        // Implementar lógica de notificación (email, SMS, etc.)
        System.out.println("=== NOTIFICACIÓN DE REPROGRAMACIÓN ===");
        System.out.println("Para: " + cita.getPaciente().getCorreo());
        System.out.println("Su cita del " + fechaOriginal + " a las " + horaOriginal + 
                          " ha sido reprogramada para el " + cita.getFecha() + " a las " + cita.getHora());
        System.out.println("Mensaje del doctor: " + mensaje);
        System.out.println("=======================================");
    }
    
    private void enviarNotificacionCancelacion(Cita cita, String motivo, String mensaje) {
        // Implementar lógica de notificación
        System.out.println("=== NOTIFICACIÓN DE CANCELACIÓN ===");
        System.out.println("Para: " + cita.getPaciente().getCorreo());
        System.out.println("Su cita del " + cita.getFecha() + " a las " + cita.getHora() + " ha sido cancelada");
        System.out.println("Motivo: " + motivo);
        System.out.println("Mensaje del doctor: " + mensaje);
        System.out.println("====================================");
    }
    
    private void enviarNotificacionReasignacion(Cita cita, Doctor doctorOriginal, Doctor nuevoDoctor, String mensaje) {
        // Implementar lógica de notificación
        System.out.println("=== NOTIFICACIÓN DE REASIGNACIÓN ===");
        System.out.println("Para: " + cita.getPaciente().getCorreo());
        System.out.println("Su cita del " + cita.getFecha() + " a las " + cita.getHora());
        System.out.println("Doctor original: " + doctorOriginal.getNombre());
        System.out.println("Nuevo doctor: " + nuevoDoctor.getNombre());
        System.out.println("Mensaje: " + mensaje);
        System.out.println("====================================");
    }
}