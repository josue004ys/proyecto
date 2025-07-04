package com.example.proyecto.service.impl;

import com.example.proyecto.entity.Doctor;
import com.example.proyecto.repository.CitaRepository;
import com.example.proyecto.repository.DoctorRepository;
import com.example.proyecto.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Doctor registrarDoctor(Doctor doctor) {
        // Verificar si el correo ya está en uso
        if (doctorRepository.findByCorreo(doctor.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado para otro doctor.");
        }

        // Encriptar la contraseña
        doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
        doctor.setEstado(Doctor.EstadoDoctor.ACTIVO);

        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Doctor> buscarPorCorreo(String correo) {
        return doctorRepository.findByCorreo(correo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> listarDoctoresActivos() {
        return doctorRepository.findByEstado(Doctor.EstadoDoctor.ACTIVO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> buscarPorEspecialidad(String especialidad) {
        return doctorRepository.findDoctoresActivosPorEspecialidad(especialidad);
    }

    @Override
    @Transactional
    public Doctor actualizarDoctor(Long id, Doctor doctorActualizado) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));

        // Actualizar campos permitidos
        doctor.setNombre(doctorActualizado.getNombre());
        doctor.setTelefono(doctorActualizado.getTelefono());
        doctor.setEspecialidad(doctorActualizado.getEspecialidad());
        doctor.setNumeroLicencia(doctorActualizado.getNumeroLicencia());

        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public void cambiarEstadoDoctor(Long id, Doctor.EstadoDoctor estado) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));

        doctor.setEstado(estado);
        doctorRepository.save(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctor> obtenerTodosLosDoctores() {
        return doctorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Doctor obtenerDoctorPorId(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Doctor> obtenerDoctorPorIdOptional(Long id) {
        return doctorRepository.findById(id);
    }

    @Override
    @Transactional
    public Doctor crearDoctor(Doctor doctor) {
        return registrarDoctor(doctor);
    }

    @Override
    @Transactional
    public void eliminarDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));

        // Verificar si el doctor tiene citas futuras
        // Si las tiene, cambiar estado a INACTIVO en lugar de eliminar
        doctor.setEstado(Doctor.EstadoDoctor.INACTIVO);
        doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public Doctor actualizarPerfil(Long doctorId, String nombre, String telefono, String especialidad) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));

        if (nombre != null && !nombre.trim().isEmpty()) {
            doctor.setNombre(nombre.trim());
        }
        if (telefono != null && !telefono.trim().isEmpty()) {
            doctor.setTelefono(telefono.trim());
        }
        if (especialidad != null && !especialidad.trim().isEmpty()) {
            doctor.setEspecialidad(especialidad.trim());
        }

        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public void cambiarPassword(Long doctorId, String passwordActual, String passwordNueva) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));

        // Verificar password actual
        if (!passwordEncoder.matches(passwordActual, doctor.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Validar nueva contraseña
        if (passwordNueva == null || passwordNueva.length() < 6) {
            throw new RuntimeException("La nueva contraseña debe tener al menos 6 caracteres");
        }

        // Actualizar contraseña
        doctor.setPassword(passwordEncoder.encode(passwordNueva));
        doctorRepository.save(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarCitasDelMes(Long doctorId, int mes, int año) {
        return citaRepository.contarCitasAtendidasPorMes(doctorId, mes, año);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarPacientesAtendidos(Long doctorId) {
        return citaRepository.contarPacientesAtendidosPorDoctor(doctorId);
    }

}
