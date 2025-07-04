package com.example.proyecto.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.proyecto.entity.Especialidad;
import com.example.proyecto.entity.Paciente;
import com.example.proyecto.entity.RolUsuario;
import com.example.proyecto.repository.EspecialidadRepository;
import com.example.proyecto.repository.PacienteRepository;

/**
 * Inicializador mínimo para datos administrativos básicos
 * Solo crea especialidades básicas y un usuario administrador
 */
@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private EspecialidadRepository especialidadRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Solo ejecutar si no hay datos (sistema completamente limpio)
        if (especialidadRepository.count() == 0) {
            System.out.println("🔧 Inicializando datos administrativos mínimos...");
            
            crearEspecialidadesBasicas();
            crearUsuarioAdmin();
            
            System.out.println("✅ Datos administrativos inicializados correctamente");
        } else {
            System.out.println("ℹ️ Datos administrativos ya existen, no se inicializarán");
        }
    }

    private void crearEspecialidadesBasicas() {
        // Crear especialidades médicas básicas
        String[] especialidades = {
            "Medicina General",
            "Cardiología", 
            "Dermatología",
            "Pediatría",
            "Ginecología",
            "Neurología",
            "Traumatología"
        };

        System.out.println("📋 Creando especialidades médicas básicas...");
        
        for (String nombreEsp : especialidades) {
            Especialidad especialidad = new Especialidad();
            especialidad.setNombre(nombreEsp);
            especialidad.setDescripcion("Especialidad médica de " + nombreEsp.toLowerCase());
            especialidad.setActiva(true);
            
            especialidadRepository.save(especialidad);
            System.out.println("   ✓ " + nombreEsp);
        }
    }

    private void crearUsuarioAdmin() {
        // Crear usuario administrador básico
        System.out.println("👨‍💼 Creando usuario administrador...");
        
        Paciente admin = new Paciente();
        admin.setNombre("Administrador del Sistema");
        admin.setCorreo("admin@hospital.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setTelefono("000-000-0000");
        admin.setRol(RolUsuario.ADMINISTRADOR);
        
        pacienteRepository.save(admin);
        System.out.println("   ✓ Usuario administrador creado");
        System.out.println("   📧 Email: admin@hospital.com");
        System.out.println("   🔑 Password: admin123");
    }
}
