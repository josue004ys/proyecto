package com.example.proyecto.service.impl;

import com.example.proyecto.entity.Especialidad;
import com.example.proyecto.repository.EspecialidadRepository;
import com.example.proyecto.service.EspecialidadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EspecialidadServiceImpl implements EspecialidadService {

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Override
    public List<Especialidad> obtenerTodasLasEspecialidades() {
        return especialidadRepository.findAll();
    }

    @Override
    public Optional<Especialidad> obtenerEspecialidadPorId(Long id) {
        return especialidadRepository.findById(id);
    }

    @Override
    public Especialidad guardarEspecialidad(Especialidad especialidad) {
        return especialidadRepository.save(especialidad);
    }

    @Override
    public void eliminarEspecialidad(Long id) {
        especialidadRepository.deleteById(id);
    }

    @Override
    public List<Especialidad> obtenerEspecialidadesActivas() {
        return especialidadRepository.findByActivaTrue();
    }

    @Override
    public Optional<Especialidad> obtenerEspecialidadPorNombre(String nombre) {
        return especialidadRepository.findByNombre(nombre);
    }
}
