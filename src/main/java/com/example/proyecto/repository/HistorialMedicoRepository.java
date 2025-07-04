package com.example.proyecto.repository;

import com.example.proyecto.entity.HistorialMedico;
import com.example.proyecto.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HistorialMedicoRepository extends JpaRepository<HistorialMedico, Long> {
    
    Optional<HistorialMedico> findByPaciente(Paciente paciente);
}
