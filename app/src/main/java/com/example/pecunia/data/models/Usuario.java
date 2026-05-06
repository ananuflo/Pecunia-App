package com.example.pecunia.data.models; // Cambia esto por tu paquete real

public class Usuario {
    public String uid;
    public String nombre;
    public String email;
    public String fechaRegistro;
    public String fechaNac;
    public String tipo; // "Básico" o "Premium"

    // Constructor vacío requerido por Firestore
    public Usuario() {}

    // Constructor completo
    public Usuario(String uid, String nombre, String email, String fechaRegistro, String fechaNac, String tipo) {
        this.uid = uid;
        this.nombre = nombre;
        this.email = email;
        this.fechaRegistro = fechaRegistro;
        this.fechaNac = fechaNac;
        this.tipo = tipo;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
