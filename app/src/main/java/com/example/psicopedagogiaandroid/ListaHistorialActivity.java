package com.example.psicopedagogiaandroid;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ListaHistorialActivity extends BaseActivity {

    private static final String TAG = "LISTA_HISTORIAL";

    private ArrayList<Historial> historial;
    private ArrayList<Paciente> pacientes;
    private Paciente pacienteSeleccionado;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_historial);

        // HEADER: muestra email + logout
        setupHeaderUsuario();

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        historial = (ArrayList<Historial>) intent.getSerializableExtra("historial");
        pacientes = (ArrayList<Paciente>) intent.getSerializableExtra("pacientes");
        pacienteSeleccionado = (Paciente) intent.getSerializableExtra("pacienteSeleccionado");

        if (historial == null) historial = new ArrayList<>();
        if (pacientes == null) pacientes = new ArrayList<>();

        ImageButton btnAdd = findViewById(R.id.btnAddHistorial);
        btnAdd.setOnClickListener(v -> {
            Intent i = new Intent(this, CargarHistorialActivity.class);
            i.putExtra("historial", historial);
            i.putExtra("pacientes", pacientes);
            i.putExtra("pacienteSeleccionado", pacienteSeleccionado);
            startActivity(i);
        });

        ImageButton btnAtras = findViewById(R.id.btnAtrasHistorial);
        btnAtras.setOnClickListener(v -> volverADetalle());

        cargarHistorialDesdeFirestore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupHeaderUsuario();
        cargarHistorialDesdeFirestore();
    }

    private void cargarHistorialDesdeFirestore() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            historial.clear();
            renderTabla();
            return;
        }

        if (pacienteSeleccionado == null || pacienteSeleccionado.getDni() == null || pacienteSeleccionado.getDni().trim().isEmpty()) {
            historial.clear();
            renderTabla();
            return;
        }

        String pacienteId = pacienteSeleccionado.getDni().trim();

        db.collection("historiales")
                .whereEqualTo("pacienteId", pacienteId)
                .whereEqualTo("terapeutaUid", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    historial.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Date fecha = doc.getDate("fecha");
                        String tipo = doc.getString("tipoRegistro");
                        String desc = doc.getString("descripcion");

                        Historial h = new Historial(pacienteSeleccionado, fecha, tipo, desc);
                        h.setId(doc.getId());
                        historial.add(h);
                    }

                    renderTabla();
                })
                .addOnFailureListener(e -> {
                    historial.clear();
                    renderTabla();
                });
    }

    private void renderTabla() {
        TableLayout table = findViewById(R.id.tableHistorial);
        table.removeAllViews();
        table.setStretchAllColumns(true);

        int colorTexto = Color.parseColor("#edf8f9");
        int colorFondo = Color.parseColor("#3d5a80");
        int colorBorde = Color.parseColor("#edf8f9");

        TableRow header = new TableRow(this);
        header.setBackgroundColor(colorFondo);

        String[] titulos = {"Paciente", "Fecha", "Tipo"};
        for (String t : titulos) {
            TextView tv = new TextView(this);
            tv.setText(t);
            tv.setTextColor(colorTexto);
            tv.setPadding(8, 8, 8, 8);
            tv.setGravity(Gravity.CENTER);
            tv.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            header.addView(tv);
        }

        table.addView(header);

        android.view.View headerDivider = new android.view.View(this);
        headerDivider.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
        headerDivider.setBackgroundColor(colorBorde);
        table.addView(headerDivider);

        for (int i = 0; i < historial.size(); i++) {
            Historial h = historial.get(i);

            TableRow row = new TableRow(this);
            row.setBackgroundColor(colorFondo);
            row.setPadding(1, 1, 1, 1);

            String pacienteTxt = "";
            if (h.getPaciente() != null) {
                String nom = h.getPaciente().getNombre() != null ? h.getPaciente().getNombre() : "";
                String ape = h.getPaciente().getApellido() != null ? h.getPaciente().getApellido() : "";
                pacienteTxt = (nom + " " + ape).trim();
            }

            String fechaTxt = h.getFecha() != null ? sdf.format(h.getFecha()) : "";
            String tipoTxt = h.getTipoRegistro() != null ? h.getTipoRegistro() : "";

            String[] columnas = {pacienteTxt, fechaTxt, tipoTxt};

            for (int c = 0; c < columnas.length; c++) {
                TextView tv = new TextView(this);
                tv.setText(columnas[c]);
                tv.setTextColor(colorTexto);
                tv.setPadding(8, 8, 8, 8);
                tv.setGravity(Gravity.CENTER);
                tv.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(tv);

                if (c == 0) {
                    int index = i;
                    tv.setOnClickListener(v -> {
                        Intent d = new Intent(this, DetalleHistorialActivity.class);
                        d.putExtra("historial", historial);
                        d.putExtra("historialItem", historial.get(index));
                        d.putExtra("indice", index);
                        d.putExtra("pacientes", pacientes);
                        d.putExtra("pacienteSeleccionado", pacienteSeleccionado);
                        startActivity(d);
                    });
                }
            }

            android.view.View divider = new android.view.View(this);
            divider.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(colorBorde);

            table.addView(row);
            table.addView(divider);
        }
    }

    private void volverADetalle() {
        Intent i = new Intent(this, DetallePacienteActivity.class);
        i.putExtra("paciente", pacienteSeleccionado);
        i.putExtra("pacientes", pacientes);
        i.putExtra("historial", historial);

        int indice = -1;
        if (pacientes != null && !pacientes.isEmpty() && pacienteSeleccionado != null && pacienteSeleccionado.getDni() != null) {
            String dniSel = pacienteSeleccionado.getDni();
            for (int x = 0; x < pacientes.size(); x++) {
                Paciente p = pacientes.get(x);
                if (p != null && dniSel.equals(p.getDni())) {
                    indice = x;
                    break;
                }
            }
        }

        i.putExtra("indice", indice);
        startActivity(i);
        finish();
    }
}
