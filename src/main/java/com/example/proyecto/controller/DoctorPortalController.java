package com.example.proyecto.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.entity.Cita;
import com.example.proyecto.entity.Doctor;
import com.example.proyecto.entity.HorarioDoctor;
import com.example.proyecto.service.CitaService;
import com.example.proyecto.service.DoctorService;
import com.example.proyecto.service.HorarioDoctorService;

@RestController
@RequestMapping("/api/doctor/portal")
@CrossOrigin(origins = "*")
public class DoctorPortalController {

    @Autowired
    private HorarioDoctorService horarioDoctorService;
    
    @Autowired
    private CitaService citaService;
    
    @Autowired
    private DoctorService doctorService;

    // ============= GESTIÓN DE HORARIOS =============
    
    @GetMapping("/mis-horarios")
    public ResponseEntity<List<HorarioDoctor>> obtenerMisHorarios(Authentication auth) {
        Long doctorId = obtenerDoctorIdDelToken(auth);
        List<HorarioDoctor> horarios = horarioDoctorService.obtenerHorariosPorDoctor(doctorId);
        return ResponseEntity.ok(horarios);
    }

    @PostMapping("/horarios")
    public ResponseEntity<?> crearHorario(
            @RequestBody Map<String, Object> horarioData, 
            Authentication auth) {
        try {
            Long doctorId = obtenerDoctorIdDelToken(auth);
            
            DayOfWeek dia = DayOfWeek.valueOf(horarioData.get("dia").toString().toUpperCase());
            LocalTime horaInicio = LocalTime.parse(horarioData.get("horaInicio").toString());
            LocalTime horaFin = LocalTime.parse(horarioData.get("horaFin").toString());
            Integer duracionCita = Integer.parseInt(horarioData.get("duracionCita").toString());

            HorarioDoctor horario = horarioDoctorService.crearHorario(doctorId, dia, horaInicio, horaFin, duracionCita);
            return ResponseEntity.ok(horario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/horarios/{horarioId}")
    public ResponseEntity<?> actualizarHorario(
            @PathVariable Long horarioId,
            @RequestBody Map<String, Object> horarioData,
            Authentication auth) {
        try {
            // Verificar que el horario pertenece al doctor autenticado
            Long doctorId = obtenerDoctorIdDelToken(auth);
            if (!esHorarioDelDoctor(horarioId, doctorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No tienes permiso para modificar este horario"));
            }

            LocalTime horaInicio = LocalTime.parse(horarioData.get("horaInicio").toString());
            LocalTime horaFin = LocalTime.parse(horarioData.get("horaFin").toString());
            Integer duracionCita = Integer.parseInt(horarioData.get("duracionCita").toString());

            HorarioDoctor horario = horarioDoctorService.actualizarHorario(horarioId, horaInicio, horaFin, duracionCita);
            return ResponseEntity.ok(horario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/horarios/{horarioId}")
    public ResponseEntity<?> eliminarHorario(@PathVariable Long horarioId, Authentication auth) {
        try {
            Long doctorId = obtenerDoctorIdDelToken(auth);
            if (!esHorarioDelDoctor(horarioId, doctorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No tienes permiso para eliminar este horario"));
            }

            horarioDoctorService.eliminarHorario(horarioId);
            return ResponseEntity.ok(Map.of("mensaje", "Horario eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/horarios/{horarioId}/bloquear")
    public ResponseEntity<?> bloquearHorario(
            @PathVariable Long horarioId,
            @RequestBody Map<String, String> data,
            Authentication auth) {
        try {
            Long doctorId = obtenerDoctorIdDelToken(auth);
            if (!esHorarioDelDoctor(horarioId, doctorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No tienes permiso para bloquear este horario"));
            }

            String motivo = data.get("motivo");
            horarioDoctorService.bloquearHorario(horarioId, motivo);
            return ResponseEntity.ok(Map.of("mensaje", "Horario bloqueado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/horarios/{horarioId}/activar")
    public ResponseEntity<?> activarHorario(@PathVariable Long horarioId, Authentication auth) {
        try {
            Long doctorId = obtenerDoctorIdDelToken(auth);
            if (!esHorarioDelDoctor(horarioId, doctorId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No tienes permiso para activar este horario"));
            }

            horarioDoctorService.activarHorario(horarioId);
            return ResponseEntity.ok(Map.of("mensaje", "Horario activado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============= GESTIÓN DE CITAS =============
    
    @GetMapping("/mis-citas")
    public ResponseEntity<List<Cita>> obtenerMisCitas(Authentication auth) {
        Long doctorId = obtenerDoctorIdDelToken(auth);
        Doctor doctor = doctorService.obtenerDoctorPorId(doctorId);
        List<Cita> citas = citaService.listarCitasPorDoctor(doctor);
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/citas-hoy")
    public ResponseEntity<List<Cita>> obtenerCitasHoy(Authentication auth) {
        List<Cita> citasHoy = citaService.obtenerCitasDelDia(LocalDate.now());
        Long doctorId = obtenerDoctorIdDelToken(auth);
        
        // Filtrar solo las citas del doctor autenticado
        List<Cita> misCitasHoy = citasHoy.stream()
                .filter(cita -> cita.getDoctor().getId().equals(doctorId))
                .toList();
        
        return ResponseEntity.ok(misCitasHoy);
    }

    @PutMapping("/citas/{citaId}/atender")
    public ResponseEntity<?> atenderCita(
            @PathVariable Long citaId,
            @RequestBody Map<String, String> data,
            Authentication auth) {
        try {
            Long doctorId = obtenerDoctorIdDelToken(auth);
            
            // Verificar que la cita pertenece al doctor
            // (Esta verificación se debería implementar en el servicio)
            
            String diagnostico = data.get("diagnostico");
            String tratamiento = data.get("tratamiento");
            String observaciones = data.get("observaciones");

            Cita cita = citaService.marcarComoAtendido(citaId, diagnostico, tratamiento, observaciones);
            return ResponseEntity.ok(cita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/citas/{citaId}/confirmar")
    public ResponseEntity<?> confirmarCita(@PathVariable Long citaId, Authentication auth) {
        try {
            Cita cita = citaService.confirmarCita(citaId);
            return ResponseEntity.ok(cita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/citas/{citaId}/cancelar")
    public ResponseEntity<?> cancelarCita(
            @PathVariable Long citaId,
            @RequestBody Map<String, String> data,
            Authentication auth) {
        try {
            String motivo = data.get("motivo");
            citaService.cancelarCita(citaId, motivo);
            return ResponseEntity.ok(Map.of("mensaje", "Cita cancelada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============= PERFIL DEL DOCTOR =============
    
    @GetMapping("/perfil")
    public ResponseEntity<Doctor> obtenerPerfil(Authentication auth) {
        Long doctorId = obtenerDoctorIdDelToken(auth);
        Doctor doctor = doctorService.obtenerDoctorPorId(doctorId);
        return ResponseEntity.ok(doctor);
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> actualizarPerfil(
            @RequestBody Map<String, String> data,
            Authentication auth) {
        try {
            Long doctorId = obtenerDoctorIdDelToken(auth);
            String nombre = data.get("nombre");
            String telefono = data.get("telefono");
            String especialidad = data.get("especialidad");

            Doctor doctor = doctorService.actualizarPerfil(doctorId, nombre, telefono, especialidad);
            return ResponseEntity.ok(doctor);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============= ESTADÍSTICAS =============
    
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(Authentication auth) {
        Long doctorId = obtenerDoctorIdDelToken(auth);
        
        // Obtener estadísticas del mes actual
        LocalDate fechaActual = LocalDate.now();
        Long citasEsteMes = doctorService.contarCitasDelMes(doctorId, fechaActual.getMonthValue(), fechaActual.getYear());
        Long totalPacientes = doctorService.contarPacientesAtendidos(doctorId);
        
        Map<String, Object> estadisticas = Map.of(
            "citasEsteMes", citasEsteMes,
            "totalPacientesAtendidos", totalPacientes,
            "horariosConfigurados", horarioDoctorService.obtenerHorariosPorDoctor(doctorId).size()
        );
        
        return ResponseEntity.ok(estadisticas);
    }

    // ============= MÉTODOS AUXILIARES =============
    
    private Long obtenerDoctorIdDelToken(Authentication auth) {
    
        return 1L;
    }
    
    private boolean esHorarioDelDoctor(Long horarioId, Long doctorId) {
        // Verificar que el horario pertenece al doctor
        List<HorarioDoctor> horarios = horarioDoctorService.obtenerHorariosPorDoctor(doctorId);
        return horarios.stream().anyMatch(h -> h.getId().equals(horarioId));
    }
}
