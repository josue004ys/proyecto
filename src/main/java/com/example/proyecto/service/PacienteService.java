package com.example.proyecto.service;

import java.util.List;
import java.util.Optional;

import com.example.proyecto.entity.Paciente;

public interface PacienteService {
    List<Paciente> obtenerTodosPacientes();
    Paciente registrarPaciente(Paciente paciente);
    Optional<Paciente> buscarPorCorreo(String correo);
}