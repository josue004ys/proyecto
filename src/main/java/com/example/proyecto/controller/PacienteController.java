package com.example.proyecto.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.entity.Paciente;
import com.example.proyecto.service.PacienteService;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "http://localhost:4200") // Puerto específico de Angular
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @GetMapping
    public ResponseEntity<List<Paciente>> obtenerTodosPacientes() {
        List<Paciente> pacientes = pacienteService.obtenerTodosPacientes();
        return ResponseEntity.ok(pacientes);
    }

    @PostMapping("/registrar")
    public ResponseEntity<Paciente> registrar(@RequestBody Paciente paciente) {
        Paciente nuevo = pacienteService.registrarPaciente(paciente);
        return ResponseEntity.ok(nuevo);
    }

    @GetMapping("/buscar")
    public ResponseEntity<Paciente> buscarPorCorreo(@RequestParam String correo) {
        return pacienteService.buscarPorCorreo(correo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}