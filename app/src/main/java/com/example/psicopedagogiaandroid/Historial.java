package com.example.psicopedagogiaandroid;

import java.io.Serializable;
import java.util.Date;

public class Historial implements Serializable {

    private String id;
    private Paciente paciente;
    private Date fecha;
    private String tipoRegistro;
    private String descripcion;

    public Historial() {}

    public Historial(Paciente paciente, Date fecha, String tipoRegistro, String descripcion) {
        this.paciente = paciente;
        this.fecha = fecha;
        this.tipoRegistro = tipoRegistro;
        this.descripcion = descripcion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getTipoRegistro() {
        return tipoRegistro;
    }

    public void setTipoRegistro(String tipoRegistro) {
        this.tipoRegistro = tipoRegistro;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
