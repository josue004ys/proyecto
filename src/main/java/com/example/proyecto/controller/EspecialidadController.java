package com.example.proyecto.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.proyecto.entity.Especialidad;
import com.example.proyecto.service.EspecialidadService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/especialidades")
@CrossOrigin(origins = "*")
public class EspecialidadController {

    @Autowired
    private EspecialidadService especialidadService;

    @GetMapping
    public ResponseEntity<List<Especialidad>> obtenerTodasLasEspecialidades() {
        List<Especialidad> especialidades = especialidadService.obtenerTodasLasEspecialidades();
        return ResponseEntity.ok(especialidades);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Especialidad> obtenerEspecialidadPorId(@PathVariable Long id) {
        Optional<Especialidad> especialidad = especialidadService.obtenerEspecialidadPorId(id);
        return especialidad.map(ResponseEntity::ok)
                           .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Especialidad> crearEspecialidad(@RequestBody Especialidad especialidad) {
        Especialidad nuevaEspecialidad = especialidadService.guardarEspecialidad(especialidad);
        return ResponseEntity.ok(nuevaEspecialidad);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Especialidad> actualizarEspecialidad(@PathVariable Long id, @RequestBody Especialidad especialidad) {
        Optional<Especialidad> especialidadExistente = especialidadService.obtenerEspecialidadPorId(id);
        if (especialidadExistente.isPresent()) {
            especialidad.setId(id);
            Especialidad especialidadActualizada = especialidadService.guardarEspecialidad(especialidad);
            return ResponseEntity.ok(especialidadActualizada);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEspecialidad(@PathVariable Long id) {
        if (especialidadService.obtenerEspecialidadPorId(id).isPresent()) {
            especialidadService.eliminarEspecialidad(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/activas")
    public ResponseEntity<List<Especialidad>> obtenerEspecialidadesActivas() {
        List<Especialidad> especialidades = especialidadService.obtenerEspecialidadesActivas();
        return ResponseEntity.ok(especialidades);
    }
}
