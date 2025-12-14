package com.example.psicopedagogiaandroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class DetalleHistorialActivity extends AppCompatActivity {

    private static final String TAG = "DETALLE_HISTORIAL";

    private Historial historialItem;
    private ArrayList<Historial> historial;
    private ArrayList<Paciente> pacientes;
    private int indice;

    private FirebaseFirestore db;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_historial);

        db = FirebaseFirestore.getInstance();

        historialItem = (Historial) getIntent().getSerializableExtra("historialItem");
        historial = (ArrayList<Historial>) getIntent().getSerializableExtra("historial");
        pacientes = (ArrayList<Paciente>) getIntent().getSerializableExtra("pacientes");
        indice = getIntent().getIntExtra("indice", -1);

        if (historial == null) historial = new ArrayList<>();
        if (pacientes == null) pacientes = new ArrayList<>();

        TextView dhPaciente = findViewById(R.id.dhPaciente);
        TextView dhFecha = findViewById(R.id.dhFecha);
        TextView dhTipo = findViewById(R.id.dhTipo);
        TextView dhDesc = findViewById(R.id.dhDescripcion);

        String pacienteTxt = "";
        if (historialItem != null && historialItem.getPaciente() != null) {
            String nom = historialItem.getPaciente().getNombre() != null ? historialItem.getPaciente().getNombre() : "";
            String ape = historialItem.getPaciente().getApellido() != null ? historialItem.getPaciente().getApellido() : "";
            pacienteTxt = (nom + " " + ape).trim();
        }

        dhPaciente.setText("Paciente: " + pacienteTxt);

        if (historialItem != null && historialItem.getFecha() != null) {
            dhFecha.setText("Fecha: " + sdf.format(historialItem.getFecha()));
        } else {
            dhFecha.setText("Fecha: ");
        }

        dhTipo.setText("Tipo: " + (historialItem != null && historialItem.getTipoRegistro() != null ? historialItem.getTipoRegistro() : ""));
        dhDesc.setText("Descripción: " + (historialItem != null && historialItem.getDescripcion() != null ? historialItem.getDescripcion() : ""));

        ImageButton dhEditar = findViewById(R.id.dhEditar);
        ImageButton dhEliminar = findViewById(R.id.dhEliminar);
        ImageButton dhVolver = findViewById(R.id.dhVolver);

        dhEditar.setOnClickListener(v -> {
            if (historialItem == null) return;
            Intent i = new Intent(this, CargarHistorialActivity.class);
            i.putExtra("historial", historial);
            i.putExtra("pacientes", pacientes);
            i.putExtra("historialItem", historialItem);
            i.putExtra("indice", indice);
            if (historialItem.getPaciente() != null) {
                i.putExtra("pacienteSeleccionado", historialItem.getPaciente());
            }
            startActivity(i);
            finish();
        });

        dhEliminar.setOnClickListener(v -> {
            if (historialItem == null) {
                volverALista();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Eliminar registro")
                    .setMessage("¿Seguro que deseas eliminar este registro?")
                    .setPositiveButton("Eliminar", (d, w) -> eliminarHistorial())
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        dhVolver.setOnClickListener(v -> volverALista());
    }

    private void eliminarHistorial() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            mostrarError("Tenés que iniciar sesión para eliminar historiales.");
            return;
        }

        String docId = historialItem.getId();
        if (docId == null || docId.trim().isEmpty()) {
            if (indice >= 0 && indice < historial.size()) {
                historial.remove(indice);
            }
            volverALista();
            return;
        }

        db.collection("historiales")
                .document(docId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Historial eliminado: " + docId);
                    if (indice >= 0 && indice < historial.size()) {
                        historial.remove(indice);
                    }
                    volverALista();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error eliminando historial: " + e.getMessage());
                    mostrarError("No se pudo eliminar el registro. Revisá tu conexión e intentá nuevamente.");
                });
    }

    private void mostrarError(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    private void volverALista() {
        Intent i = new Intent(this, ListaHistorialActivity.class);
        i.putExtra("historial", historial);
        i.putExtra("pacientes", pacientes);
        if (historialItem != null && historialItem.getPaciente() != null) {
            i.putExtra("pacienteSeleccionado", historialItem.getPaciente());
        }
        startActivity(i);
        finish();
    }
}
