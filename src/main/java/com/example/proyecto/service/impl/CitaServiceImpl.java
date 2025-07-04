package com.example.proyecto.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return citaRepo.findByPacienteOrderByFechaDescHoraDesc(paciente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitasPorDoctor(Doctor doctor) {
        return citaRepo.findByDoctorOrderByFechaAscHoraAsc(doctor);
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
        return citaRepo.findByFechaOrderByHoraAsc(fecha);
    }
}