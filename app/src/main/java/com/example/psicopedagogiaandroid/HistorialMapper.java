package com.example.psicopedagogiaandroid;

import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class HistorialMapper {

    public static Map<String, Object> toMap(String pacienteId, Historial h) {
        String uid = FirebaseAuth.getInstance().getUid();

        Map<String, Object> data = new HashMap<>();
        data.put("pacienteId", pacienteId);
        data.put("terapeutaUid", uid);
        data.put("fecha", h.getFecha());
        data.put("tipoRegistro", h.getTipoRegistro());
        data.put("descripcion", h.getDescripcion());
        return data;
    }
}
