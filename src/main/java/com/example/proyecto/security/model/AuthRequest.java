package com.example.proyecto.security.model;

public class AuthRequest {
    private String correo;
    private String password;

    // Constructors
    public AuthRequest() {}

    public AuthRequest(String correo, String password) {
        this.correo = correo;
        this.password = password;
    }

    // Getters and setters
    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
