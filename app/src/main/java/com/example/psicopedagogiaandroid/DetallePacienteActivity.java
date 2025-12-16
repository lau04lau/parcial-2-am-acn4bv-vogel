package com.example.psicopedagogiaandroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class DetallePacienteActivity extends BaseActivity {

    private static final String TAG = "DETALLE_PACIENTE";

    private Paciente paciente;
    private ArrayList<Paciente> pacientes;
    private ArrayList<Historial> historial;
    private int indice;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_paciente);

        // HEADER: muestra email + logout
        setupHeaderUsuario();

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        paciente = (Paciente) intent.getSerializableExtra("paciente");
        pacientes = (ArrayList<Paciente>) intent.getSerializableExtra("pacientes");
        historial = (ArrayList<Historial>) intent.getSerializableExtra("historial");
        indice = intent.getIntExtra("indice", -1);

        if (pacientes == null) pacientes = new ArrayList<>();
        if (historial == null) historial = new ArrayList<>();

        TextView tvNombre = findViewById(R.id.tvNombre);
        TextView tvApellido = findViewById(R.id.tvApellido);
        TextView tvDni = findViewById(R.id.tvDni);
        TextView tvTelefono = findViewById(R.id.tvTelefono);
        TextView tvNivel = findViewById(R.id.tvNivel);
        TextView tvCurso = findViewById(R.id.tvCurso);
        TextView tvFecha = findViewById(R.id.tvFecha);
        TextView tvMotivo = findViewById(R.id.tvMotivo);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        if (paciente != null) {
            tvNombre.setText(paciente.getNombre() != null ? paciente.getNombre() : "");
            tvApellido.setText(paciente.getApellido() != null ? paciente.getApellido() : "");
            tvDni.setText(paciente.getDni() != null ? paciente.getDni() : "");
            tvTelefono.setText(paciente.getTelefono() != null ? paciente.getTelefono() : "");
            tvNivel.setText(paciente.getNivelEducativo() != null ? paciente.getNivelEducativo() : "");
            tvCurso.setText(String.valueOf(paciente.getGradoCurso()));
            if (paciente.getFechaNac() != null) {
                tvFecha.setText(sdf.format(paciente.getFechaNac()));
            } else {
                tvFecha.setText("");
            }
            tvMotivo.setText(paciente.getMotivoConsulta() != null ? paciente.getMotivoConsulta() : "");
        }

        ImageButton btnAtras = findViewById(R.id.btnAtras);
        ImageButton btnEliminar = findViewById(R.id.btnEliminar);
        ImageButton btnEditar = findViewById(R.id.btnEditar);
        ImageButton btnHistorial = findViewById(R.id.btnHistorial);

        btnAtras.setOnClickListener(v -> volverALista());
        btnEliminar.setOnClickListener(this::onEliminar);
        btnEditar.setOnClickListener(this::onEditar);
        btnHistorial.setOnClickListener(this::onVerHistorial);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupHeaderUsuario();
    }

    private void volverALista() {
        Intent i = new Intent(this, ListaPacientesActivity.class);
        i.putExtra("pacientes", pacientes);
        i.putExtra("historial", historial);
        startActivity(i);
        finish();
    }

    public void onVerHistorial(View v) {
        if (paciente == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Sin paciente")
                    .setMessage("No se encontró el paciente para ver su historial.")
                    .setPositiveButton("Aceptar", null)
                    .show();
            return;
        }
        Intent i = new Intent(this, ListaHistorialActivity.class);
        i.putExtra("historial", historial);
        i.putExtra("pacientes", pacientes);
        i.putExtra("pacienteSeleccionado", paciente);
        startActivity(i);
        finish();
    }

    public void onEditar(View v) {
        if (paciente == null) return;
        Intent i = new Intent(this, cargarPacienteActivity.class);
        i.putExtra("pacientes", pacientes);
        i.putExtra("paciente", paciente);
        i.putExtra("indice", indice);
        startActivity(i);
        finish();
    }

    public void onEliminar(View v) {
        if (paciente == null) {
            volverALista();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar paciente")
                .setMessage("¿Deseás eliminar a este paciente?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarPacienteEnFirestore())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarPacienteEnFirestore() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            mostrarError("Tenés que iniciar sesión para eliminar pacientes.");
            return;
        }

        String dniPaciente = paciente.getDni();
        if (dniPaciente == null || dniPaciente.trim().isEmpty()) {
            eliminarLocalYVolver();
            return;
        }

        db.collection("pacientes")
                .document(dniPaciente.trim())
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Paciente eliminado: " + dniPaciente);
                    eliminarLocalYVolver();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error eliminando paciente: " + e.getMessage());
                    mostrarError("No se pudo eliminar el paciente. Revisá tu conexión e intentá nuevamente.");
                });
    }

    private void eliminarLocalYVolver() {
        if (pacientes != null && paciente != null) {
            String dniPaciente = paciente.getDni();
            if (dniPaciente != null) {
                for (int i = 0; i < pacientes.size(); i++) {
                    Paciente p = pacientes.get(i);
                    if (p != null && dniPaciente.equals(p.getDni())) {
                        pacientes.remove(i);
                        break;
                    }
                }
            } else if (indice >= 0 && indice < pacientes.size()) {
                pacientes.remove(indice);
            }
        }
        volverALista();
    }

    private void mostrarError(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }
}
