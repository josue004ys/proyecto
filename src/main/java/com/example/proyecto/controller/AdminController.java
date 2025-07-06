package com.example.proyecto.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
