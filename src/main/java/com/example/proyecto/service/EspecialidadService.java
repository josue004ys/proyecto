package com.example.proyecto.service;

import com.example.proyecto.entity.Especialidad;
import java.util.List;
import java.util.Optional;

public interface EspecialidadService {
    List<Especialidad> obtenerTodasLasEspecialidades();
    Optional<Especialidad> obtenerEspecialidadPorId(Long id);
    Especialidad guardarEspecialidad(Especialidad especialidad);
    void eliminarEspecialidad(Long id);
    List<Especialidad> obtenerEspecialidadesActivas();
    Optional<Especialidad> obtenerEspecialidadPorNombre(String nombre);
}
