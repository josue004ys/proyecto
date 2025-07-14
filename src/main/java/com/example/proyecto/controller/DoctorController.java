package com.example.proyecto.controller;

import java.util.List;
import java.util.Map;

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

    // Endpoint para buscar doctor por correo
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarDoctorPorCorreo(@RequestParam String correo) {
        try {
            var doctorOpt = doctorService.buscarPorCorreo(correo);
            if (doctorOpt.isPresent()) {
                Doctor doctor = doctorOpt.get();
                return ResponseEntity.ok(Map.of(
                    "id", doctor.getId(),
                    "nombre", doctor.getNombre(),
                    "especialidad", doctor.getEspecialidad(),
                    "correo", doctor.getCorreo(),
                    "estado", doctor.getEstado().name()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Doctor no encontrado con el correo: " + correo));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
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
