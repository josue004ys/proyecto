package com.example.proyecto.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.entity.Doctor;
import com.example.proyecto.entity.Paciente;
import com.example.proyecto.entity.RolUsuario;
import com.example.proyecto.repository.DoctorRepository;
import com.example.proyecto.repository.PacienteRepository;
import com.example.proyecto.service.PacienteService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private PacienteRepository pacienteRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest request) {
        try {
            // Primero buscar en doctores
            Doctor doctor = doctorRepo.findByCorreo(request.getCorreo())
                .orElse(null);
            
            if (doctor != null) {
                // Verificar contraseña del doctor (sin hash por ahora)
                if (doctor.getPassword().equals(request.getPassword())) {
                    LoginResponse response = new LoginResponse(
                        doctor.getCorreo(),
                        doctor.getNombre(),
                        "MEDICO", // Rol del doctor (consistente con frontend)
                        "Médico", // Descripción del rol
                        "Login exitoso como doctor",
                        doctor.getId() // Incluir doctorId
                    );
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.badRequest().body("Contraseña incorrecta");
                }
            }
            
            // Si no es doctor, buscar en pacientes
            Paciente paciente = pacienteRepo.findByCorreo(request.getCorreo())
                .orElse(null);
            
            if (paciente == null) {
                return ResponseEntity.badRequest().body("Usuario no encontrado");
            }
            
            // Verificar contraseña usando BCrypt para pacientes
            if (passwordEncoder.matches(request.getPassword(), paciente.getPassword())) {
                
                // Devolver datos del paciente incluyendo rol
                LoginResponse response = new LoginResponse(
                    paciente.getCorreo(),
                    paciente.getNombre(),
                    paciente.getRol().name(), // Agregar rol
                    paciente.getRol().getDescripcion(), // Agregar descripción del rol
                    "Login exitoso"
                );
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body("Contraseña incorrecta");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error en login: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisterRequest request) {
        try {
            // Validaciones básicas
            if (request.getCorreo() == null || request.getCorreo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El correo es requerido");
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La contraseña es requerida");
            }
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre es requerido");
            }

            // Verificar si el usuario ya existe
            if (pacienteRepo.findByCorreo(request.getCorreo()).isPresent()) {
                return ResponseEntity.badRequest().body("El correo ya está registrado");
            }

            // Crear nuevo paciente
            Paciente paciente = new Paciente();
            paciente.setCorreo(request.getCorreo().trim());
            paciente.setPassword(request.getPassword());
            paciente.setNombre(request.getNombre().trim());
            
            // Asignar rol (por defecto PACIENTE si no se especifica)
            if (request.getRol() != null && !request.getRol().isEmpty()) {
                try {
                    RolUsuario rol = RolUsuario.valueOf(request.getRol().toUpperCase());
                    paciente.setRol(rol);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("Rol inválido: " + request.getRol());
                }
            }
            // Si no se especifica rol, queda como PACIENTE (valor por defecto)
            
            // Registrar usando el servicio
            pacienteService.registrarPaciente(paciente);
            
            return ResponseEntity.ok("Usuario registrado exitosamente como " + 
                paciente.getRol().getDescripcion());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al registrar usuario: " + e.getMessage());
        }
    }

    // Clase para la petición de login
    public static class LoginRequest {
        private String correo;
        private String password;
        
        public LoginRequest() {}
        
        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    // Clase para la petición de registro
    public static class RegisterRequest {
        private String correo;
        private String password;
        private String nombre;
        private String rol; // Nuevo campo para el rol
        
        public RegisterRequest() {}
        
        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getRol() { return rol; }
        public void setRol(String rol) { this.rol = rol; }
    }
    
    // Clase para la respuesta del login (sin JWT)
    public static class LoginResponse {
        private String correo;
        private String nombre;
        private String rol;
        private String rolDescripcion;
        private String mensaje;
        private Long doctorId; // Agregar doctorId para doctores
        
        public LoginResponse(String correo, String nombre, String rol, String rolDescripcion, String mensaje) {
            this.correo = correo;
            this.nombre = nombre;
            this.rol = rol;
            this.rolDescripcion = rolDescripcion;
            this.mensaje = mensaje;
        }
        
        public LoginResponse(String correo, String nombre, String rol, String rolDescripcion, String mensaje, Long doctorId) {
            this.correo = correo;
            this.nombre = nombre;
            this.rol = rol;
            this.rolDescripcion = rolDescripcion;
            this.mensaje = mensaje;
            this.doctorId = doctorId;
        }
        
        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getRol() { return rol; }
        public void setRol(String rol) { this.rol = rol; }
        
        public String getRolDescripcion() { return rolDescripcion; }
        public void setRolDescripcion(String rolDescripcion) { this.rolDescripcion = rolDescripcion; }
        
        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
        
        public Long getDoctorId() { return doctorId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    }
}
