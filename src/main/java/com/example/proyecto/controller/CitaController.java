package com.example.proyecto.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.dto.CitaDTO;
import com.example.proyecto.entity.Cita;
import com.example.proyecto.entity.Doctor;
import com.example.proyecto.entity.Paciente;
import com.example.proyecto.entity.RolUsuario;
import com.example.proyecto.service.CitaService;
import com.example.proyecto.service.DoctorService;
import com.example.proyecto.service.PacienteService;

@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "*")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Método principal para agendar citas (JSON)
    @PostMapping("/agendar")
    public ResponseEntity<?> agendarCita(@RequestBody Map<String, Object> solicitud) {
        try {
            // Validaciones básicas
            if (!solicitud.containsKey("doctorId") || !solicitud.containsKey("fecha") || 
                !solicitud.containsKey("hora") || !solicitud.containsKey("motivoConsulta")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Faltan datos requeridos: doctorId, fecha, hora, motivoConsulta"
                ));
            }

            // Extraer datos de la solicitud
            Long doctorId = Long.parseLong(solicitud.get("doctorId").toString());
            String fecha = solicitud.get("fecha").toString();
            String hora = solicitud.get("hora").toString();
            String motivoConsulta = solicitud.get("motivoConsulta").toString();
            
            // Datos del paciente (pueden venir si es asistente)
            String correoPaciente = solicitud.containsKey("correoPaciente") ? 
                solicitud.get("correoPaciente").toString() : "paciente@test.com"; // Default para demo
            String nombrePaciente = solicitud.containsKey("nombrePaciente") ? 
                solicitud.get("nombrePaciente").toString() : "María González"; // Default para demo

            // Buscar o crear paciente
            Paciente paciente = pacienteService.buscarPorCorreo(correoPaciente)
                .orElseGet(() -> {
                    // Crear paciente si no existe
                    Paciente nuevoPaciente = new Paciente();
                    nuevoPaciente.setCorreo(correoPaciente);
                    nuevoPaciente.setNombre(nombrePaciente);
                    nuevoPaciente.setPassword(passwordEncoder.encode("123456"));
                    nuevoPaciente.setRol(RolUsuario.PACIENTE);
                    return pacienteService.registrarPaciente(nuevoPaciente);
                });

            // Convertir strings a tipos apropiados
            LocalDate fechaCita = LocalDate.parse(fecha);
            LocalTime horaCita = LocalTime.parse(hora);

            // Agendar la cita
            Cita cita = citaService.agendarCita(paciente, fechaCita, horaCita, doctorId, motivoConsulta);

            return ResponseEntity.ok(Map.of(
                "mensaje", "Cita agendada exitosamente",
                "citaId", cita.getId(),
                "fecha", cita.getFecha(),
                "hora", cita.getHora(),
                "doctor", cita.getDoctor().getNombre(),
                "paciente", cita.getPaciente().getNombre()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/paciente/{correo}")
    public ResponseEntity<?> listarPorPaciente(@PathVariable String correo) {
        try {
            Paciente paciente = pacienteService.buscarPorCorreo(correo)
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            
            List<Cita> citas = citaService.listarCitasPorPaciente(paciente);
            List<CitaDTO> citasDTO = citas.stream()
                    .map(CitaDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(citasDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> listarPorDoctor(@PathVariable Long doctorId) {
        try {
            Doctor doctor = doctorService.obtenerDoctorPorId(doctorId);
            List<Cita> citas = citaService.listarCitasPorDoctor(doctor);
            List<CitaDTO> citasDTO = citas.stream()
                    .map(CitaDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(citasDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/disponible")
    public ResponseEntity<?> verificarDisponibilidad(
            @RequestParam String fecha,
            @RequestParam String hora,
            @RequestParam Long doctorId) {
        try {
            boolean disponible = citaService.estaDisponible(
                LocalDate.parse(fecha), 
                LocalTime.parse(hora), 
                doctorId
            );
            return ResponseEntity.ok(Map.of("disponible", disponible));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/atender")
    public ResponseEntity<?> marcarComoAtendido(
            @PathVariable Long id,
            @RequestBody Map<String, String> datos) {
        try {
            String diagnostico = datos.get("diagnostico");
            String tratamiento = datos.get("tratamiento");
            String observaciones = datos.get("observaciones");
            
            Cita cita = citaService.marcarComoAtendido(id, diagnostico, tratamiento, observaciones);
            return ResponseEntity.ok(cita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/confirmar")
    public ResponseEntity<?> confirmarCita(@PathVariable Long id) {
        try {
            Cita cita = citaService.confirmarCita(id);
            return ResponseEntity.ok(cita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelarCita(
            @PathVariable Long id,
            @RequestBody Map<String, String> datos) {
        try {
            String motivo = datos.getOrDefault("motivo", "Cancelación por el usuario");
            citaService.cancelarCita(id, motivo);
            return ResponseEntity.ok(Map.of("mensaje", "Cita cancelada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/hoy")
    public ResponseEntity<?> obtenerCitasHoy() {
        try {
            List<Cita> citas = citaService.obtenerCitasDelDia(LocalDate.now());
            List<CitaDTO> citasDTO = citas.stream()
                    .map(CitaDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(citasDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> obtenerTodasLasCitas() {
        try {
            List<Cita> citas = citaService.obtenerTodasLasCitas();
            return ResponseEntity.ok(citas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
