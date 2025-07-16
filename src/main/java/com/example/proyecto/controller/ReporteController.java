package com.example.proyecto.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.entity.Cita;
import com.example.proyecto.entity.Doctor;
import com.example.proyecto.entity.Especialidad;
import com.example.proyecto.repository.CitaRepository;
import com.example.proyecto.repository.DoctorRepository;
import com.example.proyecto.repository.EspecialidadRepository;
import com.example.proyecto.repository.PacienteRepository;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "http://localhost:4200")
public class ReporteController {
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private EspecialidadRepository especialidadRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private CitaRepository citaRepository;

    /**
     * 📈 Reporte General del Sistema
     * Devuelve estadísticas generales del sistema médico
     */
    @GetMapping("/general")
    public ResponseEntity<Map<String, Object>> obtenerReporteGeneral() {
        Map<String, Object> reporte = new HashMap<>();
        
        try {
            // Datos básicos del sistema
            long totalDoctores = doctorRepository.count();
            long totalEspecialidades = especialidadRepository.count();
            long totalPacientes = pacienteRepository.count();
            long totalCitas = citaRepository.count();
            long doctoresActivos = doctorRepository.countByEstado(Doctor.EstadoDoctor.ACTIVO);
            
            // Estadísticas de citas
            LocalDate hoy = LocalDate.now();
            long citasHoy = citaRepository.findAll().stream()
                .filter(cita -> cita.getFecha().equals(hoy))
                .count();
            
            long citasPendientes = citaRepository.findAll().stream()
                .filter(cita -> cita.getEstado() == Cita.EstadoCita.PENDIENTE)
                .count();
            
            // Estadísticas por especialidad
            List<Especialidad> especialidades = especialidadRepository.findAll();
            List<Map<String, Object>> especialidadesStats = especialidades.stream()
                .map(esp -> {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("nombre", esp.getNombre());
                    stat.put("doctores", doctorRepository.countByEspecialidad(esp.getNombre()));
                    return stat;
                })
                .sorted((a, b) -> Long.compare((Long)b.get("doctores"), (Long)a.get("doctores")))
                .collect(Collectors.toList());
            
            // Estructura del reporte
            Map<String, Object> datos = new HashMap<>();
            datos.put("totalUsuarios", totalPacientes + totalDoctores + 1); // +1 admin
            datos.put("totalDoctores", totalDoctores);
            datos.put("totalPacientes", totalPacientes);
            datos.put("totalCitas", totalCitas);
            datos.put("citasHoy", citasHoy);
            datos.put("citasPendientes", citasPendientes);
            datos.put("doctoresActivos", doctoresActivos);
            datos.put("totalEspecialidades", totalEspecialidades);
            datos.put("especialidadesStats", especialidadesStats);
            
            reporte.put("tipo", "general");
            reporte.put("titulo", "📈 Reporte General del Sistema");
            reporte.put("fechaGeneracion", LocalDateTime.now().toString());
            reporte.put("datos", datos);
            reporte.put("success", true);
            
            return ResponseEntity.ok(reporte);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al generar reporte general: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 👨‍⚕️ Reporte de Doctores
     * Lista detallada de doctores con filtros opcionales
     */
    @GetMapping("/doctores")
    public ResponseEntity<Map<String, Object>> obtenerReporteDoctores(
            @RequestParam(required = false) String especialidad,
            @RequestParam(required = false) String estado) {
        
        Map<String, Object> reporte = new HashMap<>();
        
        try {
            List<Doctor> doctores = doctorRepository.findAll();
            
            // Aplicar filtros si se proporcionan
            if (especialidad != null && !especialidad.trim().isEmpty()) {
                doctores = doctores.stream()
                    .filter(d -> d.getEspecialidad().equalsIgnoreCase(especialidad.trim()))
                    .collect(Collectors.toList());
            }
            
            if (estado != null && !estado.trim().isEmpty()) {
                Doctor.EstadoDoctor estadoEnum = Doctor.EstadoDoctor.valueOf(estado.toUpperCase());
                doctores = doctores.stream()
                    .filter(d -> d.getEstado().equals(estadoEnum))
                    .collect(Collectors.toList());
            }
            
            // Convertir a formato simplificado para el frontend
            List<Map<String, Object>> doctoresData = doctores.stream()
                .map(doctor -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", doctor.getId());
                    data.put("nombre", doctor.getNombre());
                    data.put("correo", doctor.getCorreo());
                    data.put("especialidad", doctor.getEspecialidad());
                    data.put("telefono", doctor.getTelefono());
                    data.put("numeroLicencia", doctor.getNumeroLicencia());
                    data.put("estado", doctor.getEstado().toString());
                    return data;
                })
                .collect(Collectors.toList());
            
            String titulo = "👨‍⚕️ Reporte de Doctores";
            if (especialidad != null && !especialidad.trim().isEmpty()) {
                titulo += " - " + especialidad;
            }
            
            reporte.put("tipo", "doctores");
            reporte.put("titulo", titulo);
            reporte.put("fechaGeneracion", LocalDateTime.now().toString());
            reporte.put("datos", doctoresData);
            reporte.put("totalRegistros", doctoresData.size());
            reporte.put("filtros", Map.of(
                "especialidad", especialidad != null ? especialidad : "",
                "estado", estado != null ? estado : ""
            ));
            reporte.put("success", true);
            
            return ResponseEntity.ok(reporte);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al generar reporte de doctores: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 📊 Estadísticas Avanzadas del Sistema
     * Análisis detallado y métricas calculadas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> reporte = new HashMap<>();
        
        try {
            List<Doctor> doctores = doctorRepository.findAll();
            List<Especialidad> especialidades = especialidadRepository.findAll();
            
            // Calcular especialidad más popular
            Map<String, Long> conteoEspecialidades = doctores.stream()
                .collect(Collectors.groupingBy(
                    Doctor::getEspecialidad,
                    Collectors.counting()
                ));
            
            String especialidadMasPopular = conteoEspecialidades.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
            
            Long cantidadEspecialidadMasPopular = conteoEspecialidades.getOrDefault(especialidadMasPopular, 0L);
            
            // Ranking de especialidades
            List<Map<String, Object>> rankingEspecialidades = conteoEspecialidades.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> ranking = new HashMap<>();
                    ranking.put("nombre", entry.getKey());
                    ranking.put("doctores", entry.getValue());
                    return ranking;
                })
                .sorted((a, b) -> Long.compare((Long)b.get("doctores"), (Long)a.get("doctores")))
                .limit(5)
                .collect(Collectors.toList());
            
            // Promedio de doctores por especialidad
            double promedioDoctoresPorEspecialidad = !especialidades.isEmpty() ? 
                (double) doctores.size() / especialidades.size() : 0;
            
            // Porcentaje de cobertura (especialidades con al menos un doctor)
            long especialidadesConDoctores = conteoEspecialidades.size();
            double porcentajeCobertura = !especialidades.isEmpty() ? 
                (double) especialidadesConDoctores / especialidades.size() * 100 : 0;
            
            // Simular doctores registrados hoy (en una implementación real, filtrarías por fecha)
            int doctoresHoy = (int) (Math.random() * 3) + 1;
            
            // Datos del reporte
            Map<String, Object> datos = new HashMap<>();
            datos.put("especialidadMasPopular", Map.of(
                "nombre", especialidadMasPopular,
                "cantidad", cantidadEspecialidadMasPopular
            ));
            datos.put("promedioDoctoresPorEspecialidad", Math.round(promedioDoctoresPorEspecialidad * 100.0) / 100.0);
            datos.put("doctoresHoy", doctoresHoy);
            datos.put("porcentajeCobertura", Math.round(porcentajeCobertura * 100.0) / 100.0);
            datos.put("rankingEspecialidades", rankingEspecialidades);
            datos.put("totalDoctores", doctores.size());
            datos.put("totalEspecialidades", especialidades.size());
            
            reporte.put("tipo", "estadisticas");
            reporte.put("titulo", "📊 Estadísticas y Análisis del Sistema");
            reporte.put("fechaGeneracion", LocalDateTime.now().toString());
            reporte.put("datos", datos);
            reporte.put("success", true);
            
            return ResponseEntity.ok(reporte);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al generar estadísticas: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 📅 Reporte de Citas
     * Análisis completo de citas médicas del sistema
     */
    @GetMapping("/citas")
    public ResponseEntity<Map<String, Object>> obtenerReporteCitas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String especialidad,
            @RequestParam(required = false) String estado) {
        
        Map<String, Object> reporte = new HashMap<>();
        
        try {
            // Obtener todas las citas
            List<Cita> todasLasCitas = citaRepository.findAll();
            List<Cita> citasFiltradas = todasLasCitas;
            
            // Aplicar filtro de fechas si se proporcionan
            if (fechaDesde != null && fechaHasta != null) {
                citasFiltradas = citasFiltradas.stream()
                    .filter(cita -> !cita.getFecha().isBefore(fechaDesde) && !cita.getFecha().isAfter(fechaHasta))
                    .collect(Collectors.toList());
            } else if (fechaDesde != null) {
                citasFiltradas = citasFiltradas.stream()
                    .filter(cita -> !cita.getFecha().isBefore(fechaDesde))
                    .collect(Collectors.toList());
            } else if (fechaHasta != null) {
                citasFiltradas = citasFiltradas.stream()
                    .filter(cita -> !cita.getFecha().isAfter(fechaHasta))
                    .collect(Collectors.toList());
            }
            
            // Aplicar filtro de especialidad si se proporciona
            if (especialidad != null && !especialidad.trim().isEmpty()) {
                citasFiltradas = citasFiltradas.stream()
                    .filter(cita -> cita.getDoctor() != null && 
                           cita.getDoctor().getEspecialidad().equalsIgnoreCase(especialidad.trim()))
                    .collect(Collectors.toList());
            }
            
            // Aplicar filtro de estado si se proporciona
            if (estado != null && !estado.trim().isEmpty()) {
                try {
                    Cita.EstadoCita estadoEnum = Cita.EstadoCita.valueOf(estado.trim().toUpperCase());
                    citasFiltradas = citasFiltradas.stream()
                        .filter(cita -> cita.getEstado().equals(estadoEnum))
                        .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    // Si el estado no es válido, ignorar el filtro
                    System.out.println("Estado de cita no válido: " + estado);
                }
            }
            
            // Calcular estadísticas
            long totalCitas = citasFiltradas.size();
            LocalDate hoy = LocalDate.now();
            
            // Citas por estado
            Map<String, Long> citasPorEstado = citasFiltradas.stream()
                .collect(Collectors.groupingBy(
                    cita -> cita.getEstado().getDescripcion(),
                    Collectors.counting()
                ));
            
            // Citas de hoy
            long citasHoy = citasFiltradas.stream()
                .filter(cita -> cita.getFecha().equals(hoy))
                .count();
            
            // Citas por especialidad
            List<Map<String, Object>> citasPorEspecialidad = citasFiltradas.stream()
                .filter(cita -> cita.getDoctor() != null)
                .collect(Collectors.groupingBy(
                    cita -> cita.getDoctor().getEspecialidad(),
                    Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("especialidad", entry.getKey());
                    item.put("cantidad", entry.getValue());
                    return item;
                })
                .sorted((a, b) -> Long.compare((Long)b.get("cantidad"), (Long)a.get("cantidad")))
                .collect(Collectors.toList());
            
            // Citas próximas (próximos 7 días)
            LocalDate proximaSemana = hoy.plusDays(7);
            long citasProximas = citasFiltradas.stream()
                .filter(cita -> cita.getFecha().isAfter(hoy) && !cita.getFecha().isAfter(proximaSemana))
                .count();
            
            // Lista de citas para mostrar (últimas 50)
            List<Map<String, Object>> listaCitas = citasFiltradas.stream()
                .sorted((a, b) -> {
                    int fechaComp = b.getFecha().compareTo(a.getFecha());
                    if (fechaComp != 0) return fechaComp;
                    return b.getHora().compareTo(a.getHora());
                })
                .limit(50)
                .map(cita -> {
                    Map<String, Object> citaData = new HashMap<>();
                    citaData.put("id", cita.getId());
                    citaData.put("fecha", cita.getFecha().toString());
                    citaData.put("hora", cita.getHora().toString());
                    citaData.put("estado", cita.getEstado().getDescripcion());
                    citaData.put("motivoConsulta", cita.getMotivoConsulta());
                    
                    if (cita.getPaciente() != null) {
                        citaData.put("paciente", cita.getPaciente().getNombre());
                    }
                    
                    if (cita.getDoctor() != null) {
                        citaData.put("doctor", cita.getDoctor().getNombre());
                        citaData.put("especialidad", cita.getDoctor().getEspecialidad());
                    }
                    
                    return citaData;
                })
                .collect(Collectors.toList());
            
            // Estructurar datos del reporte
            Map<String, Object> datos = new HashMap<>();
            datos.put("totalCitas", totalCitas);
            datos.put("citasHoy", citasHoy);
            datos.put("citasProximas", citasProximas);
            datos.put("citasPorEstado", citasPorEstado);
            datos.put("citasPorEspecialidad", citasPorEspecialidad);
            datos.put("listaCitas", listaCitas);
            
            // Agregar totales por estado
            datos.put("citasPendientes", citasPorEstado.getOrDefault("Pendiente", 0L));
            datos.put("citasConfirmadas", citasPorEstado.getOrDefault("Confirmada", 0L));
            datos.put("citasAtendidas", citasPorEstado.getOrDefault("Atendida", 0L));
            datos.put("citasCanceladas", citasPorEstado.getOrDefault("Cancelada", 0L));
            
            String titulo = "📅 Reporte de Citas Médicas";
            if (especialidad != null && !especialidad.trim().isEmpty()) {
                titulo += " - " + especialidad;
            }
            if (fechaDesde != null || fechaHasta != null) {
                titulo += " (Filtrado por fechas)";
            }
            
            reporte.put("tipo", "citas");
            reporte.put("titulo", titulo);
            reporte.put("fechaGeneracion", LocalDateTime.now().toString());
            reporte.put("datos", datos);
            reporte.put("totalRegistros", totalCitas);
            reporte.put("filtros", Map.of(
                "fechaDesde", fechaDesde != null ? fechaDesde.toString() : "",
                "fechaHasta", fechaHasta != null ? fechaHasta.toString() : "",
                "especialidad", especialidad != null ? especialidad : "",
                "estado", estado != null ? estado : ""
            ));
            reporte.put("success", true);
            
            return ResponseEntity.ok(reporte);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al generar reporte de citas: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 📋 Resumen Ejecutivo
     * Métricas clave para dashboard administrativo
     */
    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Object>> obtenerResumenEjecutivo() {
        Map<String, Object> resumen = new HashMap<>();
        
        try {
            long totalDoctores = doctorRepository.count();
            long doctoresActivos = doctorRepository.countByEstado(Doctor.EstadoDoctor.ACTIVO);
            long totalEspecialidades = especialidadRepository.count();
            long totalPacientes = pacienteRepository.count();
            
            // Crecimiento (simulado)
            Map<String, Object> crecimiento = new HashMap<>();
            crecimiento.put("doctoresNuevosEsteMes", (int) (Math.random() * 5) + 2);
            crecimiento.put("pacientesNuevosEsteMes", (int) (Math.random() * 20) + 10);
            crecimiento.put("citasEsteMes", (int) (Math.random() * 100) + 50);
            
            resumen.put("totalDoctores", totalDoctores);
            resumen.put("doctoresActivos", doctoresActivos);
            resumen.put("totalEspecialidades", totalEspecialidades);
            resumen.put("totalPacientes", totalPacientes);
            resumen.put("crecimiento", crecimiento);
            resumen.put("fechaGeneracion", LocalDateTime.now().toString());
            resumen.put("success", true);
            
            return ResponseEntity.ok(resumen);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al generar resumen ejecutivo: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
