package com.example.proyecto.config;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.proyecto.entity.Doctor;
import com.example.proyecto.entity.Doctor.EstadoDoctor;
import com.example.proyecto.entity.Especialidad;
import com.example.proyecto.entity.HorarioDoctor;
import com.example.proyecto.entity.HorarioDoctor.EstadoHorario;
import com.example.proyecto.entity.Paciente;
import com.example.proyecto.entity.RolUsuario;
import com.example.proyecto.repository.DoctorRepository;
import com.example.proyecto.repository.EspecialidadRepository;
import com.example.proyecto.repository.HorarioDoctorRepository;
import com.example.proyecto.repository.PacienteRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private HorarioDoctorRepository horarioDoctorRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;
    
    @Autowired
    private EspecialidadRepository especialidadRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeEspecialidades();
        initializeDoctors();
        initializeAdminUser();
    }

    private void initializeDoctors() {
        // Solo crear doctores si no existen
        if (doctorRepository.count() == 0) {
            System.out.println("🏥 Inicializando doctores del sistema...");
            
            // Doctor 1 - Cardiología
            Doctor doctor1 = new Doctor();
            doctor1.setNombre("Dr. Carlos Mendoza");
            doctor1.setCorreo("carlos.mendoza@hospital.com");
            doctor1.setPassword(passwordEncoder.encode("doctor123"));
            doctor1.setEspecialidad("Cardiología");
            doctor1.setTelefono("+502 2234-5678");
            doctor1.setNumeroLicencia("LIC-CARD-001");
            doctor1.setEstado(EstadoDoctor.ACTIVO);
            doctorRepository.save(doctor1);
            
            // Doctor 2 - Pediatría
            Doctor doctor2 = new Doctor();
            doctor2.setNombre("Dra. María González");
            doctor2.setCorreo("maria.gonzalez@hospital.com");
            doctor2.setPassword(passwordEncoder.encode("doctor123"));
            doctor2.setEspecialidad("Pediatría");
            doctor2.setTelefono("+502 2234-5679");
            doctor2.setNumeroLicencia("LIC-PED-002");
            doctor2.setEstado(EstadoDoctor.ACTIVO);
            doctorRepository.save(doctor2);
            
            // Doctor 3 - Medicina General
            Doctor doctor3 = new Doctor();
            doctor3.setNombre("Dr. Roberto Silva");
            doctor3.setCorreo("roberto.silva@hospital.com");
            doctor3.setPassword(passwordEncoder.encode("doctor123"));
            doctor3.setEspecialidad("Medicina General");
            doctor3.setTelefono("+502 2234-5680");
            doctor3.setNumeroLicencia("LIC-MED-003");
            doctor3.setEstado(EstadoDoctor.ACTIVO);
            doctorRepository.save(doctor3);
            
            // Doctor 4 - Ginecología
            Doctor doctor4 = new Doctor();
            doctor4.setNombre("Dra. Ana López");
            doctor4.setCorreo("ana.lopez@hospital.com");
            doctor4.setPassword(passwordEncoder.encode("doctor123"));
            doctor4.setEspecialidad("Ginecología");
            doctor4.setTelefono("+502 2234-5681");
            doctor4.setNumeroLicencia("LIC-GIN-004");
            doctor4.setEstado(EstadoDoctor.ACTIVO);
            doctorRepository.save(doctor4);
            
            // Doctor 5 - Dermatología
            Doctor doctor5 = new Doctor();
            doctor5.setNombre("Dr. Luis Ramírez");
            doctor5.setCorreo("luis.ramirez@hospital.com");
            doctor5.setPassword(passwordEncoder.encode("doctor123"));
            doctor5.setEspecialidad("Dermatología");
            doctor5.setTelefono("+502 2234-5682");
            doctor5.setNumeroLicencia("LIC-DER-005");
            doctor5.setEstado(EstadoDoctor.ACTIVO);
            doctorRepository.save(doctor5);
            
            System.out.println("✅ Se crearon " + doctorRepository.count() + " doctores exitosamente");
            
            // Crear horarios básicos para todos los doctores
            createBasicSchedules();
        } else {
            System.out.println("ℹ️  Ya existen doctores en el sistema (" + doctorRepository.count() + " doctores)");
        }
    }
    
    private void createBasicSchedules() {
        // Crear horarios básicos para todos los doctores (Lunes a Viernes, 8:00 AM - 5:00 PM)
        doctorRepository.findAll().forEach(doctor -> {
            List<HorarioDoctor> horariosExistentes = horarioDoctorRepository.findByDoctorIdOrderByDiaAsc(doctor.getId());
            
            if (horariosExistentes.isEmpty()) {
                // Crear horarios para Lunes a Viernes
                DayOfWeek[] diasSemana = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY};
                
                for (DayOfWeek dia : diasSemana) {
                    HorarioDoctor horario = new HorarioDoctor();
                    horario.setDoctor(doctor);
                    horario.setDia(dia); // Usar setDia en lugar de setDiaSemana
                    horario.setHoraInicio(LocalTime.of(8, 0)); // 8:00 AM
                    horario.setHoraFin(LocalTime.of(17, 0));   // 5:00 PM
                    horario.setEstado(EstadoHorario.ACTIVO); // Usar setEstado en lugar de setActivo
                    horarioDoctorRepository.save(horario);
                }
                System.out.println("✅ Horarios creados para: " + doctor.getNombre());
            }
        });
    }
    
    private void initializeAdminUser() {
        // Crear usuario administrador si no existe
        if (pacienteRepository.findByCorreo("admin@hospital.com").isEmpty()) {
            Paciente admin = new Paciente();
            admin.setNombre("Administrador del Sistema");
            admin.setCorreo("admin@hospital.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setTelefono("+502 2234-0000");
            admin.setRol(RolUsuario.MEDICO); // Admin con permisos de médico
            pacienteRepository.save(admin);
            
            System.out.println("✅ Usuario administrador creado: admin@hospital.com / admin123");
        }
    }

    private void initializeEspecialidades() {
        // Solo crear especialidades si no existen
        if (especialidadRepository.count() == 0) {
            System.out.println("🏥 Inicializando especialidades médicas...");
            
            // Crear especialidades médicas comunes
            String[] nombreEspecialidades = {
                "Cardiología",
                "Pediatría", 
                "Medicina General",
                "Ginecología",
                "Dermatología",
                "Neurología",
                "Traumatología",
                "Oftalmología",
                "Otorrinolaringología",
                "Psiquiatría",
                "Urología",
                "Endocrinología"
            };
            
            String[] descripcionEspecialidades = {
                "Especialidad médica que se encarga del diagnóstico y tratamiento de las enfermedades del corazón",
                "Especialidad médica que se centra en la salud y enfermedades de los bebés, niños y adolescentes",
                "Atención médica integral para adultos y adolescentes, diagnóstico y tratamiento de enfermedades comunes",
                "Especialidad médica que trata enfermedades del sistema reproductor femenino",
                "Especialidad médica dedicada al estudio de la estructura y función de la piel",
                "Especialidad médica que trata los trastornos del sistema nervioso",
                "Especialidad médica que se ocupa de las lesiones del sistema musculoesquelético",
                "Especialidad médica que trata las enfermedades de los ojos y la visión",
                "Especialidad médica que trata las enfermedades del oído, nariz y garganta",
                "Especialidad médica dedicada al estudio, diagnóstico y tratamiento de los trastornos mentales",
                "Especialidad médica que trata las enfermedades del sistema urinario y reproductor masculino",
                "Especialidad médica que estudia las hormonas y las glándulas que las producen"
            };
            
            for (int i = 0; i < nombreEspecialidades.length; i++) {
                Especialidad especialidad = new Especialidad();
                especialidad.setNombre(nombreEspecialidades[i]);
                especialidad.setDescripcion(descripcionEspecialidades[i]);
                especialidad.setActiva(true);
                especialidad.setFechaCreacion(java.time.LocalDate.now());
                especialidadRepository.save(especialidad);
            }
            
            System.out.println("✅ Se crearon " + especialidadRepository.count() + " especialidades exitosamente");
        } else {
            System.out.println("ℹ️  Ya existen especialidades en el sistema (" + especialidadRepository.count() + " especialidades)");
        }
    }
}
