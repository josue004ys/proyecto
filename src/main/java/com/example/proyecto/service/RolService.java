package com.example.proyecto.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.proyecto.entity.RolUsuario;

@Service
public class RolService {

    /**
     * Obtiene todos los roles disponibles
     */
    public List<RolUsuario> obtenerTodosLosRoles() {
        return Arrays.asList(RolUsuario.values());
    }

    /**
     * Obtiene roles que pueden registrarse públicamente
     */
    public List<RolUsuario> obtenerRolesPublicos() {
        return Arrays.asList(RolUsuario.PACIENTE);
    }

    /**
     * Obtiene roles del personal médico
     */
    public List<RolUsuario> obtenerRolesPersonalMedico() {
        return Arrays.stream(RolUsuario.values())
                .filter(RolUsuario::esPersonalMedico)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene roles administrativos
     */
    public List<RolUsuario> obtenerRolesAdministrativos() {
        return Arrays.stream(RolUsuario.values())
                .filter(RolUsuario::esAdministrativo)
                .collect(Collectors.toList());
    }

    /**
     * Verifica si un usuario con cierto rol puede realizar una acción
     */
    public boolean puedeRealizarAccion(RolUsuario rol, String accion) {
        switch (accion) {
            case "GESTIONAR_USUARIOS":
                return rol.esAdministrativo();
            
            case "GESTIONAR_CITAS":
                return rol.puedeGestionarCitas() || rol == RolUsuario.MEDICO;
            
            case "VER_TODAS_LAS_CITAS":
                return rol.esAdministrativo() || rol == RolUsuario.RECEPCIONISTA;
            
            case "GESTIONAR_HORARIOS":
                return rol == RolUsuario.MEDICO || rol.esAdministrativo();
            
            case "GESTIONAR_ESPECIALIDADES":
                return rol.esAdministrativo();
            
            case "VER_REPORTES":
                return rol.esAdministrativo() || rol == RolUsuario.DIRECTOR_MEDICO;
            
            case "AGENDAR_CITAS_PROPIAS":
                return rol == RolUsuario.PACIENTE;
            
            case "VER_CITAS_PROPIAS":
                return rol == RolUsuario.PACIENTE || rol == RolUsuario.MEDICO;
            
            default:
                return false;
        }
    }

    /**
     * Obtiene la descripción completa de permisos de un rol
     */
    public String obtenerDescripcionPermisos(RolUsuario rol) {
        switch (rol) {
            case PACIENTE:
                return "Puede agendar citas, ver sus citas, actualizar su perfil";
            
            case MEDICO:
                return "Puede gestionar sus horarios, ver sus citas, gestionar consultas";
            
            case ASISTENTE:
                return "Puede ayudar con el agendamiento de citas, gestión básica";
            
            case RECEPCIONISTA:
                return "Puede gestionar citas de todos los pacientes, registro de usuarios";
            
            case ENFERMERO:
                return "Puede ver citas, asistir en consultas, gestión de pacientes";
            
            case ADMINISTRADOR:
                return "Acceso completo al sistema, gestión de usuarios, configuración";
            
            case DIRECTOR_MEDICO:
                return "Gestión médica, reportes, supervisión del personal médico";
            
            default:
                return "Sin permisos especificados";
        }
    }
}
