package com.quantum.tareas.modelos;

public class Usuario {
    private String id;
    private String email;
    private String password; // Puede que no necesites almacenar esto

    public Usuario() {
        // Constructor vacío requerido para Firebase
    }

    public Usuario(String id, String email) {
        this.id = id;
        this.email = email;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
