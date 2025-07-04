package com.example.proyecto.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.entity.Especialidad;
import com.example.proyecto.entity.Paciente;
import com.example.proyecto.entity.RolUsuario;
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
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/inicializar-sistema")
    public ResponseEntity<Map<String, Object>> inicializarSistema() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int especialidadesCreadas = 0;
            boolean adminCreado = false;
            
            // Crear especialidades si no existen
            if (especialidadRepository.count() == 0) {
                especialidadesCreadas = crearEspecialidadesBasicas();
            }
            
            // Crear admin si no existe
            if (!pacienteRepository.findByCorreo("admin@hospital.com").isPresent()) {
                adminCreado = crearUsuarioAdmin();
            }
            
            response.put("success", true);
            response.put("message", "Sistema inicializado correctamente");
            response.put("especialidadesCreadas", especialidadesCreadas);
            response.put("adminCreado", adminCreado);
            response.put("credencialesAdmin", Map.of(
                "email", "admin@hospital.com",
                "password", "admin123"
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al inicializar sistema: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private int crearEspecialidadesBasicas() {
        String[] especialidades = {
            "Medicina General",
            "Cardiología", 
            "Dermatología",
            "Pediatría",
            "Ginecología",
            "Neurología",
            "Traumatología",
            "Oftalmología",
            "Psiquiatría",
            "Endocrinología"
        };

        for (String nombreEsp : especialidades) {
            Especialidad especialidad = new Especialidad();
            especialidad.setNombre(nombreEsp);
            especialidad.setDescripcion("Especialidad médica de " + nombreEsp.toLowerCase());
            especialidad.setActiva(true);
            
            especialidadRepository.save(especialidad);
        }
        
        return especialidades.length;
    }

    private boolean crearUsuarioAdmin() {
        Paciente admin = new Paciente();
        admin.setNombre("Administrador del Sistema");
        admin.setCorreo("admin@hospital.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setTelefono("000-000-0000");
        admin.setRol(RolUsuario.ADMINISTRADOR);
        
        pacienteRepository.save(admin);
        return true;
    }

    @PostMapping("/verificar-estado")
    public ResponseEntity<Map<String, Object>> verificarEstado() {
        Map<String, Object> response = new HashMap<>();
        
        long totalEspecialidades = especialidadRepository.count();
        long totalPacientes = pacienteRepository.count();
        boolean adminExiste = pacienteRepository.findByCorreo("admin@hospital.com").isPresent();
        
        response.put("especialidades", totalEspecialidades);
        response.put("pacientes", totalPacientes);
        response.put("adminExiste", adminExiste);
        response.put("sistemaInicializado", totalEspecialidades > 0 && adminExiste);
        
        return ResponseEntity.ok(response);
    }
}
