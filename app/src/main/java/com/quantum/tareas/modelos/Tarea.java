package com.quantum.tareas.modelos;

public class Tarea {
    private String id;
    private String titulo;
    private String descripcion;
    private String fechaLimite;
    private String categoria;
    private int prioridad;
    private String estado;

    public Tarea() {
        // Constructor vacío requerido para Firebase
    }

    public Tarea(String id, String titulo, String descripcion, String fechaLimite, String categoria, int prioridad, String estado) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaLimite = fechaLimite;
        this.categoria = categoria;
        this.prioridad = prioridad;
        this.estado = estado; // Inicializar el estado
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(String fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public String getEstado() {
        return estado; // Getter para el estado
    }

    public void setEstado(String estado) {
        this.estado = estado; // Setter para el estado
    }
}
