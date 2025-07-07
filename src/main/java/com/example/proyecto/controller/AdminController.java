package com.example.proyecto.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.dto.DoctorDTO;
import com.example.proyecto.entity.Doctor;
import com.example.proyecto.repository.DoctorRepository;
import com.example.proyecto.repository.EspecialidadRepository;
import com.example.proyecto.repository.PacienteRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class AdminController {

    @Autowired
    private EspecialidadRepository especialidadRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;

    @GetMapping("/verificar-estado")
    public ResponseEntity<Map<String, Object>> verificarEstado() {
        Map<String, Object> response = new HashMap<>();
        
        long totalEspecialidades = especialidadRepository.count();
        long totalPacientes = pacienteRepository.count();
        
        response.put("especialidades", totalEspecialidades);
        response.put("pacientes", totalPacientes);
        response.put("sistemaLimpio", true);
        response.put("mensaje", "Sistema limpio - Listo para gestión manual");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/doctores")
    public ResponseEntity<Map<String, Object>> crearDoctor(@RequestBody DoctorDTO doctorDTO) {
        try {
            Doctor doctor = new Doctor();
            doctor.setNombre(doctorDTO.getNombre());
            doctor.setCorreo(doctorDTO.getCorreo());
            doctor.setPassword(doctorDTO.getPassword()); // En producción, esto debe ser hasheado
            doctor.setEspecialidad(doctorDTO.getEspecialidad());
            doctor.setTelefono(doctorDTO.getTelefono());
            doctor.setNumeroLicencia(doctorDTO.getNumeroLicencia());
            doctor.setEstado(Doctor.EstadoDoctor.ACTIVO);
            
            Doctor doctorGuardado = doctorRepository.save(doctor);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", doctorGuardado.getId());
            response.put("nombre", doctorGuardado.getNombre());
            response.put("correo", doctorGuardado.getCorreo());
            response.put("especialidad", doctorGuardado.getEspecialidad());
            response.put("telefono", doctorGuardado.getTelefono());
            response.put("numeroLicencia", doctorGuardado.getNumeroLicencia());
            response.put("estado", doctorGuardado.getEstado().toString());
            response.put("mensaje", "Doctor creado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al crear doctor: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
