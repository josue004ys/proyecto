package com.example.proyecto.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.example.proyecto.service.DoctorService;

@RestController
@RequestMapping("/api/doctores")
@CrossOrigin(origins = "*")
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

    @PutMapping("/{id}/toggle-estado")
    public ResponseEntity<Doctor> toggleEstadoDoctor(@PathVariable Long id) {
        Doctor doctor = doctorService.obtenerDoctorPorId(id);
        Doctor.EstadoDoctor nuevoEstado = doctor.getEstado() == Doctor.EstadoDoctor.ACTIVO 
            ? Doctor.EstadoDoctor.INACTIVO 
            : Doctor.EstadoDoctor.ACTIVO;
        
        doctorService.cambiarEstadoDoctor(id, nuevoEstado);
        Doctor doctorActualizado = doctorService.obtenerDoctorPorId(id);
        return ResponseEntity.ok(doctorActualizado);
    }
}
