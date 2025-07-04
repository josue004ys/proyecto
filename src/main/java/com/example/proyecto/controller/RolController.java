package com.example.proyecto.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.entity.RolUsuario;
import com.example.proyecto.service.RolService;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"})
public class RolController {

    @Autowired
    private RolService rolService;

    @GetMapping("/todos")
    public ResponseEntity<List<RolUsuario>> obtenerTodosLosRoles() {
        return ResponseEntity.ok(rolService.obtenerTodosLosRoles());
    }

    @GetMapping("/publicos")
    public ResponseEntity<List<RolUsuario>> obtenerRolesPublicos() {
        return ResponseEntity.ok(rolService.obtenerRolesPublicos());
    }

    @GetMapping("/personal-medico")
    public ResponseEntity<List<RolUsuario>> obtenerRolesPersonalMedico() {
        return ResponseEntity.ok(rolService.obtenerRolesPersonalMedico());
    }

    @GetMapping("/administrativos")
    public ResponseEntity<List<RolUsuario>> obtenerRolesAdministrativos() {
        return ResponseEntity.ok(rolService.obtenerRolesAdministrativos());
    }

    @GetMapping("/{rol}/permisos")
    public ResponseEntity<Map<String, Object>> obtenerPermisosRol(@PathVariable String rol) {
        try {
            RolUsuario rolUsuario = RolUsuario.valueOf(rol.toUpperCase());
            
            Map<String, Object> response = new HashMap<>();
            response.put("rol", rolUsuario);
            response.put("descripcion", rolUsuario.getDescripcion());
            response.put("permisos", rolService.obtenerDescripcionPermisos(rolUsuario));
            response.put("esAdministrativo", rolUsuario.esAdministrativo());
            response.put("esPersonalMedico", rolUsuario.esPersonalMedico());
            response.put("puedeGestionarCitas", rolUsuario.puedeGestionarCitas());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Rol no encontrado: " + rol);
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/matriz-permisos")
    public ResponseEntity<Map<String, Object>> obtenerMatrizPermisos() {
        Map<String, Object> matriz = new HashMap<>();
        
        String[] acciones = {
            "GESTIONAR_USUARIOS",
            "GESTIONAR_CITAS", 
            "VER_TODAS_LAS_CITAS",
            "GESTIONAR_HORARIOS",
            "GESTIONAR_ESPECIALIDADES",
            "VER_REPORTES",
            "AGENDAR_CITAS_PROPIAS",
            "VER_CITAS_PROPIAS"
        };
        
        for (RolUsuario rol : RolUsuario.values()) {
            Map<String, Boolean> permisosRol = new HashMap<>();
            
            for (String accion : acciones) {
                permisosRol.put(accion, rolService.puedeRealizarAccion(rol, accion));
            }
            
            matriz.put(rol.name(), permisosRol);
        }
        
        return ResponseEntity.ok(matriz);
    }
}
