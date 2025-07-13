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
            Long doctorId = Long.valueOf(solicitud.get("doctorId").toString());
            String fecha = solicitud.get("fecha").toString();
            String hora = solicitud.get("hora").toString();
            String motivoConsulta = solicitud.get("motivoConsulta").toString();
            
            // Datos del paciente - REQUERIDOS
            if (!solicitud.containsKey("correoPaciente")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "El correo del paciente es requerido"
                ));
            }
            
            String correoPaciente = solicitud.get("correoPaciente").toString();
            String nombrePaciente = solicitud.containsKey("nombrePaciente") ? 
                solicitud.get("nombrePaciente").toString() : "Usuario";

            System.out.println("🏥 Agendando cita para paciente: " + correoPaciente);

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

    // ========= GESTIÓN DE CITAS CUANDO EL DOCTOR NO PUEDE ATENDER =========

    /**
     * Reprogramar una cita cuando el doctor no puede atender
     */
    @PutMapping("/{citaId}/reprogramar")
    public ResponseEntity<?> reprogramarCita(
            @PathVariable Long citaId,
            @RequestBody Map<String, Object> reprogramacionData) {
        try {
            String nuevaFecha = (String) reprogramacionData.get("nuevaFecha");
            String nuevaHora = (String) reprogramacionData.get("nuevaHora");
            String motivo = (String) reprogramacionData.get("motivo");
            String mensajePaciente = (String) reprogramacionData.get("mensajePaciente");

            CitaDTO citaReprogramada = citaService.reprogramarCita(
                citaId, 
                LocalDate.parse(nuevaFecha), 
                LocalTime.parse(nuevaHora),
                motivo,
                mensajePaciente
            );

            return ResponseEntity.ok(Map.of(
                "mensaje", "Cita reprogramada exitosamente",
                "cita", citaReprogramada,
                "notificacionEnviada", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al reprogramar la cita: " + e.getMessage()
            ));
        }
    }

    /**
     * Cancelar una cita por parte del doctor con notificación al paciente
     */
    @PutMapping("/{citaId}/cancelar-doctor")
    public ResponseEntity<?> cancelarCitaDoctor(
            @PathVariable Long citaId,
            @RequestBody Map<String, String> cancelacionData) {
        try {
            String motivo = cancelacionData.get("motivo");
            String mensajePaciente = cancelacionData.get("mensajePaciente");
            
            CitaDTO citaCancelada = citaService.cancelarCitaDoctor(citaId, motivo, mensajePaciente);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Cita cancelada exitosamente",
                "cita", citaCancelada,
                "notificacionEnviada", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al cancelar la cita: " + e.getMessage()
            ));
        }
    }

    /**
     * Reasignar una cita a otro doctor de la misma especialidad
     */
    @PutMapping("/{citaId}/reasignar")
    public ResponseEntity<?> reasignarCita(
            @PathVariable Long citaId,
            @RequestBody Map<String, Object> reasignacionData) {
        try {
            Long nuevoDoctorId = Long.valueOf(reasignacionData.get("nuevoDoctorId").toString());
            String motivo = (String) reasignacionData.get("motivo");
            String mensajePaciente = (String) reasignacionData.get("mensajePaciente");
            
            // Verificar que el nuevo doctor existe y tiene la misma especialidad
            Doctor nuevoDoctor = doctorService.obtenerDoctorPorId(nuevoDoctorId);
            if (nuevoDoctor == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Doctor no encontrado"
                ));
            }

            CitaDTO citaReasignada = citaService.reasignarCita(citaId, nuevoDoctorId, motivo, mensajePaciente);
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Cita reasignada exitosamente",
                "cita", citaReasignada,
                "nuevoDoctor", nuevoDoctor.getNombre(),
                "notificacionEnviada", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al reasignar la cita: " + e.getMessage()
            ));
        }
    }

    /**
     * Obtener doctores disponibles de la misma especialidad para reasignación
     */
    @GetMapping("/{citaId}/doctores-disponibles")
    public ResponseEntity<?> obtenerDoctoresDisponibles(@PathVariable Long citaId) {
        try {
            List<Doctor> doctoresDisponibles = citaService.obtenerDoctoresDisponiblesParaReasignacion(citaId);
            
            List<Map<String, Object>> doctoresData = doctoresDisponibles.stream()
                .map(doctor -> {
                    Map<String, Object> doctorMap = new java.util.HashMap<>();
                    doctorMap.put("id", doctor.getId());
                    doctorMap.put("nombre", doctor.getNombre() != null ? doctor.getNombre() : "Sin nombre");
                    doctorMap.put("especialidad", doctor.getEspecialidad() != null ? doctor.getEspecialidad() : "Sin especialidad");
                    doctorMap.put("correo", doctor.getCorreo() != null ? doctor.getCorreo() : "Sin correo");
                    return doctorMap;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(doctoresData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al obtener doctores disponibles: " + e.getMessage()
            ));
        }
    }

    /**
     * Obtener historial de cambios de una cita
     */
    @GetMapping("/{citaId}/historial")
    public ResponseEntity<?> obtenerHistorialCita(@PathVariable Long citaId) {
        try {
            List<Map<String, Object>> historial = citaService.obtenerHistorialCambios(citaId);
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al obtener historial: " + e.getMessage()
            ));
        }
    }
}
