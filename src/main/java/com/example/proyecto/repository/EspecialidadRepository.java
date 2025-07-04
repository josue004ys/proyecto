package com.example.proyecto.repository;

import com.example.proyecto.entity.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
    List<Especialidad> findByActivaTrue();
    Optional<Especialidad> findByNombre(String nombre);
}
