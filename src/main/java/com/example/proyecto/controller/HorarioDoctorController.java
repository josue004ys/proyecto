package com.example.proyecto.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.example.proyecto.entity.Doctor;
import com.example.proyecto.entity.HorarioDoctor;
import com.example.proyecto.service.DoctorService;
import com.example.proyecto.service.HorarioDoctorService;

@RestController
@RequestMapping("/api/horarios")
@CrossOrigin(origins = "*")
public class HorarioDoctorController {

    @Autowired
    private HorarioDoctorService horarioService;

    @Autowired
    private DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<HorarioDoctor>> obtenerTodosHorarios() {
        List<HorarioDoctor> horarios = horarioService.obtenerTodosHorarios();
        return ResponseEntity.ok(horarios);
    }

    @GetMapping("/mis-horarios")
    public ResponseEntity<List<HorarioDoctor>> obtenerMisHorarios() {
        // Por ahora devolvemos horarios del primer doctor disponible
        // En implementación real se obtendría el doctorId del usuario autenticado
        List<Doctor> doctores = doctorService.obtenerTodosLosDoctores();
        if (doctores.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Long doctorId = doctores.get(0).getId();
        List<HorarioDoctor> horarios = horarioService.obtenerHorariosPorDoctor(doctorId);
        return ResponseEntity.ok(horarios);
    }

    @GetMapping("/dia/{dia}")
    public ResponseEntity<List<HorarioDoctor>> obtenerHorariosPorDia(@PathVariable String dia) {
        try {
            DayOfWeek diaSemana = DayOfWeek.valueOf(dia.toUpperCase());
            List<HorarioDoctor> horarios = horarioService.obtenerHorarioPorDia(diaSemana);
            return ResponseEntity.ok(horarios);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<HorarioDoctor>> obtenerHorariosPorDoctor(@PathVariable Long doctorId) {
        List<HorarioDoctor> horarios = horarioService.obtenerHorariosPorDoctor(doctorId);
        return ResponseEntity.ok(horarios);
    }

    @GetMapping("/doctor/{doctorId}/disponibles")
    public ResponseEntity<List<LocalTime>> obtenerHorasDisponibles(
            @PathVariable Long doctorId,
            @RequestParam String fecha) {
        try {
            LocalDate fechaConsulta = LocalDate.parse(fecha);
            List<LocalTime> horasDisponibles = horarioService.obtenerHorasDisponibles(doctorId, fechaConsulta);
            return ResponseEntity.ok(horasDisponibles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping
    public ResponseEntity<?> crearHorario(@RequestBody Map<String, Object> horarioData) {
        try {
            // Obtener datos del request
            Long doctorId = Long.valueOf(horarioData.get("doctorId").toString());
            String diaStr = horarioData.get("dia").toString();
            String horaInicioStr = horarioData.get("horaInicio").toString();
            String horaFinStr = horarioData.get("horaFin").toString();
            
            // Validar que el doctor existe
            Optional<Doctor> doctorOpt = doctorService.obtenerDoctorPorIdOptional(doctorId);
            if (!doctorOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Doctor no encontrado"
                ));
            }

            // Convertir datos
            DayOfWeek dia = DayOfWeek.valueOf(diaStr.toUpperCase());
            LocalTime horaInicio = LocalTime.parse(horaInicioStr);
            LocalTime horaFin = LocalTime.parse(horaFinStr);

            // Validar que no existe un horario para el mismo día
            List<HorarioDoctor> horariosExistentes = horarioService.obtenerHorariosPorDoctor(doctorId);
            boolean existeHorario = horariosExistentes.stream()
                .anyMatch(h -> h.getDia().equals(dia) && 
                          h.getEstado() == HorarioDoctor.EstadoHorario.ACTIVO);

            if (existeHorario) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Ya existe un horario activo para este día"
                ));
            }

            // Crear nuevo horario
            HorarioDoctor nuevoHorario = new HorarioDoctor();
            nuevoHorario.setDoctor(doctorOpt.get());
            nuevoHorario.setDia(dia);
            nuevoHorario.setHoraInicio(horaInicio);
            nuevoHorario.setHoraFin(horaFin);
            nuevoHorario.setEstado(HorarioDoctor.EstadoHorario.ACTIVO);
            
            // Establecer duración por defecto si no se proporciona
            if (horarioData.containsKey("duracionCita")) {
                nuevoHorario.setDuracionCita(Integer.valueOf(horarioData.get("duracionCita").toString()));
            }
            
            if (horarioData.containsKey("observaciones")) {
                nuevoHorario.setObservaciones(horarioData.get("observaciones").toString());
            }

            HorarioDoctor horarioGuardado = horarioService.guardarHorario(nuevoHorario);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Horario creado exitosamente",
                "horario", horarioGuardado
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error al crear horario: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarHorario(@PathVariable Long id, @RequestBody Map<String, Object> horarioData) {
        try {
            Optional<HorarioDoctor> horarioOpt = horarioService.obtenerHorarioPorIdOptional(id);
            if (!horarioOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            HorarioDoctor horario = horarioOpt.get();

            // Actualizar campos si están presentes
            if (horarioData.containsKey("dia")) {
                DayOfWeek dia = DayOfWeek.valueOf(horarioData.get("dia").toString().toUpperCase());
                horario.setDia(dia);
            }
            
            if (horarioData.containsKey("horaInicio")) {
                LocalTime horaInicio = LocalTime.parse(horarioData.get("horaInicio").toString());
                horario.setHoraInicio(horaInicio);
            }
            
            if (horarioData.containsKey("horaFin")) {
                LocalTime horaFin = LocalTime.parse(horarioData.get("horaFin").toString());
                horario.setHoraFin(horaFin);
            }
            
            if (horarioData.containsKey("estado")) {
                String estadoStr = horarioData.get("estado").toString();
                HorarioDoctor.EstadoHorario estado = HorarioDoctor.EstadoHorario.valueOf(estadoStr.toUpperCase());
                horario.setEstado(estado);
            }
            
            if (horarioData.containsKey("duracionCita")) {
                horario.setDuracionCita(Integer.valueOf(horarioData.get("duracionCita").toString()));
            }
            
            if (horarioData.containsKey("observaciones")) {
                horario.setObservaciones(horarioData.get("observaciones").toString());
            }

            HorarioDoctor horarioActualizado = horarioService.actualizarHorario(horario);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Horario actualizado exitosamente",
                "horario", horarioActualizado
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error al actualizar horario: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarHorario(@PathVariable Long id) {
        try {
            Optional<HorarioDoctor> horarioOpt = horarioService.obtenerHorarioPorIdOptional(id);
            if (!horarioOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            horarioService.eliminarHorario(id);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Horario eliminado exitosamente"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error al eliminar horario: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/toggle-estado")
    public ResponseEntity<?> toggleEstadoHorario(@PathVariable Long id) {
        try {
            Optional<HorarioDoctor> horarioOpt = horarioService.obtenerHorarioPorIdOptional(id);
            if (!horarioOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            HorarioDoctor horario = horarioOpt.get();
            
            // Alternar entre ACTIVO e INACTIVO
            if (horario.getEstado() == HorarioDoctor.EstadoHorario.ACTIVO) {
                horario.setEstado(HorarioDoctor.EstadoHorario.INACTIVO);
            } else {
                horario.setEstado(HorarioDoctor.EstadoHorario.ACTIVO);
            }

            HorarioDoctor horarioActualizado = horarioService.actualizarHorario(horario);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Estado del horario cambiado exitosamente",
                "horario", horarioActualizado
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error al cambiar estado del horario: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/estadisticas/{doctorId}")
    public ResponseEntity<?> obtenerEstadisticasHorarios(@PathVariable Long doctorId) {
        try {
            List<HorarioDoctor> horarios = horarioService.obtenerHorariosPorDoctor(doctorId);
            
            long horariosActivos = horarios.stream()
                .filter(h -> h.getEstado() == HorarioDoctor.EstadoHorario.ACTIVO)
                .count();
            
            long horariosInactivos = horarios.stream()
                .filter(h -> h.getEstado() == HorarioDoctor.EstadoHorario.INACTIVO)
                .count();

            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalHorarios", horarios.size());
            estadisticas.put("horariosActivos", horariosActivos);
            estadisticas.put("horariosInactivos", horariosInactivos);
            estadisticas.put("diasConHorarios", horarios.stream()
                .map(h -> h.getDia().name())
                .distinct()
                .count());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "estadisticas", estadisticas
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error al obtener estadísticas: " + e.getMessage()
            ));
        }
    }
}
