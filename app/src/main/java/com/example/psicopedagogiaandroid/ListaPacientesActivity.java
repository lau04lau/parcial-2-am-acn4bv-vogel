package com.example.psicopedagogiaandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;

public class ListaPacientesActivity extends BaseActivity {

    private static final String TAG = "LISTA_PACIENTES";

    private ArrayList<Paciente> pacientes;
    private ArrayList<Historial> historial;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_pacientes);

        // HEADER: muestra email + logout
        setupHeaderUsuario();

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        pacientes = (ArrayList<Paciente>) intent.getSerializableExtra("pacientes");
        historial = (ArrayList<Historial>) intent.getSerializableExtra("historial");

        if (pacientes == null) pacientes = new ArrayList<>();
        if (historial == null) historial = new ArrayList<>();

        ImageButton btnAdd = findViewById(R.id.btnAddPatient);
        btnAdd.setOnClickListener(v -> {
            Intent i = new Intent(this, cargarPacienteActivity.class);
            i.putExtra("pacientes", pacientes);
            startActivity(i);
            finish();
        });

        cargarPacientesDesdeFirestore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupHeaderUsuario();
        cargarPacientesDesdeFirestore();
    }

    private void cargarPacientesDesdeFirestore() {
        db.collection("pacientes")
                .get()
                .addOnSuccessListener(snapshot -> {
                    pacientes.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Paciente p = new Paciente();

                        String dni = doc.getString("dni");
                        String nombre = doc.getString("nombre");
                        String apellido = doc.getString("apellido");
                        String telefono = doc.getString("telefono");
                        Date fechaNac = doc.getDate("fechaNac");
                        String motivo = doc.getString("motivoConsulta");
                        String nivel = doc.getString("nivelEducativo");

                        Long gradoLong = doc.getLong("gradoCurso");
                        int grado = gradoLong != null ? gradoLong.intValue() : 0;

                        try { if (nombre != null) p.setNombre(nombre); else p.setNombre("No informado"); } catch (Exception ignored) {}
                        try { if (apellido != null) p.setApellido(apellido); else p.setApellido("No informado"); } catch (Exception ignored) {}
                        try {
                            if (dni != null) p.setDni(dni);
                            else p.setDni(doc.getId());
                        } catch (Exception e) {
                            try { p.setDni("0000000"); } catch (Exception ignored) {}
                        }
                        try { if (telefono != null) p.setTelefono(telefono); else p.setTelefono("000000"); } catch (Exception ignored) {}
                        try { p.setFechaNac(fechaNac); } catch (Exception ignored) {}
                        try { p.setMotivoConsulta(motivo != null ? motivo : ""); } catch (Exception ignored) {}
                        try { p.setGradoCurso(grado); } catch (Exception ignored) {}
                        try { if (nivel != null) p.setNivelEducativo(nivel); else p.setNivelEducativo("Inicial"); } catch (Exception ignored) {}

                        pacientes.add(p);
                    }

                    Log.d(TAG, "Pacientes cargados: " + pacientes.size());
                    renderTabla();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error leyendo pacientes: " + e.getMessage());
                    renderTabla();
                });
    }

    private void renderTabla() {
        TableLayout table = findViewById(R.id.tablePacientes);
        table.removeAllViews();
        table.setStretchAllColumns(true);
        table.setShrinkAllColumns(false);

        int colorTexto = android.graphics.Color.parseColor("#edf8f9");
        int colorFondo = android.graphics.Color.parseColor("#3d5a80");
        int colorBorde = android.graphics.Color.parseColor("#edf8f9");

        TableRow header = new TableRow(this);
        header.setBackgroundColor(colorFondo);
        header.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        String[] hs = {"Nombre", "Apellido", "Teléfono"};
        for (String h : hs) {
            TextView tv = new TextView(this);
            tv.setText(h);
            tv.setTextColor(colorTexto);
            tv.setPadding(8, 8, 8, 8);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            header.addView(tv);
        }

        table.addView(header);

        android.view.View headerDivider = new android.view.View(this);
        headerDivider.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, 1));
        headerDivider.setBackgroundColor(colorBorde);
        table.addView(headerDivider);

        for (int index = 0; index < pacientes.size(); index++) {
            Paciente p = pacientes.get(index);

            TableRow row = new TableRow(this);
            row.setBackgroundColor(colorFondo);
            row.setPadding(1, 1, 1, 1);
            row.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));

            String[] cols = {
                    p.getNombre() != null ? p.getNombre() : "",
                    p.getApellido() != null ? p.getApellido() : "",
                    p.getTelefono() != null ? p.getTelefono() : ""
            };

            for (int i = 0; i < cols.length; i++) {
                TextView tv = new TextView(this);
                tv.setText(cols[i]);
                tv.setTextColor(colorTexto);
                tv.setPadding(8, 8, 8, 8);
                tv.setGravity(android.view.Gravity.CENTER);
                tv.setLayoutParams(new TableRow.LayoutParams(
                        0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(tv);

                if (i == 0) {
                    final int pos = index;
                    tv.setOnClickListener(v -> {
                        Intent d = new Intent(this, DetallePacienteActivity.class);
                        d.putExtra("paciente", pacientes.get(pos));
                        d.putExtra("pacientes", pacientes);
                        d.putExtra("indice", pos);
                        d.putExtra("historial", historial);
                        startActivity(d);
                        finish();
                    });
                }
            }

            android.view.View divider = new android.view.View(this);
            divider.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(colorBorde);

            table.addView(row);
            table.addView(divider);
        }
    }
}
