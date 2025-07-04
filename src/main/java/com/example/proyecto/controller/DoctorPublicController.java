package com.example.proyecto.controller;

import com.example.proyecto.entity.Doctor;
import com.example.proyecto.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/doctores")
@CrossOrigin(origins = "*")
public class DoctorPublicController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/activos")
    public ResponseEntity<List<Doctor>> obtenerDoctoresActivos() {
        List<Doctor> doctores = doctorService.listarDoctoresActivos();
        return ResponseEntity.ok(doctores);
    }

    @GetMapping("/especialidad/{especialidad}")
    public ResponseEntity<List<Doctor>> obtenerDoctoresPorEspecialidad(@PathVariable String especialidad) {
        List<Doctor> doctores = doctorService.buscarPorEspecialidad(especialidad);
        return ResponseEntity.ok(doctores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> obtenerDoctorPorId(@PathVariable Long id) {
        try {
            Doctor doctor = doctorService.obtenerDoctorPorId(id);
            return ResponseEntity.ok(doctor);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Doctor>> obtenerTodosLosDoctores() {
        List<Doctor> doctores = doctorService.obtenerTodosLosDoctores();
        return ResponseEntity.ok(doctores);
    }
}
