package com.example.proyecto.controller;

import com.example.proyecto.entity.Doctor;
import com.example.proyecto.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctores")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<Doctor>> obtenerTodosLosDoctores() {
        List<Doctor> doctores = doctorService.obtenerTodosLosDoctores();
        return ResponseEntity.ok(doctores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> obtenerDoctorPorId(@PathVariable Long id) {
        Doctor doctor = doctorService.obtenerDoctorPorId(id);
        return ResponseEntity.ok(doctor);
    }

    @PostMapping
    public ResponseEntity<Doctor> crearDoctor(@RequestBody Doctor doctor) {
        Doctor nuevoDoctor = doctorService.crearDoctor(doctor);
        return ResponseEntity.ok(nuevoDoctor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> actualizarDoctor(@PathVariable Long id, @RequestBody Doctor doctor) {
        Doctor doctorActualizado = doctorService.actualizarDoctor(id, doctor);
        return ResponseEntity.ok(doctorActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDoctor(@PathVariable Long id) {
        doctorService.eliminarDoctor(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Doctor> cambiarEstadoDoctor(@PathVariable Long id, @RequestParam Doctor.EstadoDoctor estado) {
        doctorService.cambiarEstadoDoctor(id, estado);
        Doctor doctorActualizado = doctorService.obtenerDoctorPorId(id);
        return ResponseEntity.ok(doctorActualizado);
    }
}
