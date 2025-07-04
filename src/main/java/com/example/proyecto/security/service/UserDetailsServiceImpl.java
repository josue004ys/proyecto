package com.example.proyecto.security.service;
import com.example.proyecto.entity.Paciente;
import com.example.proyecto.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private PacienteRepository pacienteRepo;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Paciente p = pacienteRepo.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Correo no encontrado"));
        return new User(p.getCorreo(), p.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(p.getRolString())));
    }
}
