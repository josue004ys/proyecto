package com.example.proyecto.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.proyecto.entity.RolUsuario;
import com.example.proyecto.entity.Usuario;
import com.example.proyecto.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Iniciando configuración del sistema...");
        
        // Verificar si ya existe un administrador
        boolean existeAdmin = usuarioRepository.existsByRol(RolUsuario.ADMINISTRADOR);
        
        if (!existeAdmin) {
            System.out.println("👤 No se encontró administrador. Creando usuario admin por defecto...");
            crearAdministradorPorDefecto();
        } else {
            System.out.println("✅ Administrador ya existe en el sistema.");
        }
        
        mostrarInformacionInicial();
    }
    
    private void crearAdministradorPorDefecto() {
        try {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador del Sistema");
            admin.setCorreo("admin@hospital.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol(RolUsuario.ADMINISTRADOR);
            admin.setEstado(Usuario.EstadoUsuario.ACTIVO);
            
            usuarioRepository.save(admin);
            
            System.out.println("✅ ¡Administrador creado exitosamente!");
            System.out.println("📧 Correo: admin@hospital.com");
            System.out.println("🔐 Contraseña: admin123");
            System.out.println("⚠️  IMPORTANTE: Cambie esta contraseña después del primer login");
            
        } catch (Exception e) {
            System.err.println("❌ Error al crear administrador: " + e.getMessage());
        }
    }
    
    private void mostrarInformacionInicial() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🏥 SISTEMA DE CITAS MÉDICAS - INFORMACIÓN DE ACCESO");
        System.out.println("=".repeat(60));
        
        // Mostrar información de administrador
        Usuario admin = usuarioRepository.findByRol(RolUsuario.ADMINISTRADOR).stream().findFirst().orElse(null);
        if (admin != null) {
            System.out.println("👨‍💼 ADMINISTRADOR:");
            System.out.println("   📧 Correo: " + admin.getCorreo());
            System.out.println("   🔐 Contraseña: admin123 (si es la primera vez)");
        }
        
        System.out.println("\n🌐 ACCESO AL SISTEMA:");
        System.out.println("   Frontend: http://localhost:4200");
        System.out.println("   Backend API: http://localhost:8081");
        
        System.out.println("\n📋 ROLES DISPONIBLES:");
        System.out.println("   • ADMINISTRADOR - Gestión completa del sistema");
        System.out.println("   • DOCTOR - Portal médico y gestión de citas");
        System.out.println("   • ASISTENTE - Agendar citas para pacientes");
        System.out.println("   • PACIENTE - Gestión personal de citas");
        
        System.out.println("\n🔧 CONFIGURACIÓN INICIAL:");
        System.out.println("   1. Acceda como administrador");
        System.out.println("   2. Registre especialidades médicas");
        System.out.println("   3. Registre doctores y personal");
        System.out.println("   4. Configure horarios de atención");
        
        System.out.println("=".repeat(60) + "\n");
    }
}
