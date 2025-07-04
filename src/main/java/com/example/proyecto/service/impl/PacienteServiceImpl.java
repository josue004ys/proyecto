package com.example.proyecto.service.impl;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.proyecto.entity.Paciente;
import com.example.proyecto.entity.RolUsuario;
import com.example.proyecto.repository.PacienteRepository;
import com.example.proyecto.service.PacienteService;

@Service
public class PacienteServiceImpl implements PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public List<Paciente> obtenerTodosPacientes() {
        return pacienteRepository.findAll();
    }

    public Paciente registrarPaciente(Paciente paciente) {
        // Verificar si el correo ya está en uso (opcional pero recomendado)
        if (pacienteRepository.findByCorreo(paciente.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado.");
        }

        // Encriptar la contraseña antes de guardar
        paciente.setPassword(passwordEncoder.encode(paciente.getPassword()));

        // Asegurar que tenga un rol válido, por defecto PACIENTE si no se especificó
        if (paciente.getRol() == null) {
            paciente.setRol(RolUsuario.PACIENTE);
        }

        return pacienteRepository.save(paciente);
    }

    @Override
    public Optional<Paciente> buscarPorCorreo(String correo) {
        return pacienteRepository.findByCorreo(correo);
    }
}