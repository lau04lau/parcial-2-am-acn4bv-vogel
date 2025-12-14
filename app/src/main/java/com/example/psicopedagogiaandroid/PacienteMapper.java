package com.example.psicopedagogiaandroid;

import java.util.HashMap;
import java.util.Map;

public class PacienteMapper {

    public static Map<String, Object> toMap(Paciente p) {
        Map<String, Object> data = new HashMap<>();
        data.put("nombre", p.getNombre());
        data.put("apellido", p.getApellido());
        data.put("dni", p.getDni());
        data.put("telefono", p.getTelefono());
        data.put("fechaNac", p.getFechaNac());
        data.put("motivoConsulta", p.getMotivoConsulta());
        data.put("gradoCurso", p.getGradoCurso());
        data.put("nivelEducativo", p.getNivelEducativo());
        return data;
    }
}
