package com.example.psicopedagogiaandroid;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class cargarPacienteActivity extends BaseActivity {

    private static final String TAG = "CARGAR_PACIENTE";

    private EditText nombre, apellido, dni, telefono, motivoconsulta, fechaNac;
    private AutoCompleteTextView nivelEdu, curso;
    private ArrayList<Paciente> pacientes;
    private final Calendar calendario = Calendar.getInstance();
    private final String[] niveles = new String[]{"Inicial", "Primario", "Secundario", "Terciario"};
    private final String[] cursos = new String[]{"1°", "2°", "3°", "4°", "5°", "6°", "7°"};
    private int indiceEdicion = -1;
    private Paciente pacienteEditar;
    private Button btnAgregar;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar_paciente);

        // HEADER: muestra email + logout
        setupHeaderUsuario();

        db = FirebaseFirestore.getInstance();

        pacientes = (ArrayList<Paciente>) getIntent().getSerializableExtra("pacientes");
        if (pacientes == null) pacientes = new ArrayList<>();

        pacienteEditar = (Paciente) getIntent().getSerializableExtra("paciente");
        indiceEdicion = getIntent().getIntExtra("indice", -1);

        nombre = findViewById(R.id.nombre);
        apellido = findViewById(R.id.apellido);
        dni = findViewById(R.id.dni);
        telefono = findViewById(R.id.telefono);
        fechaNac = findViewById(R.id.fechaNac);
        motivoconsulta = findViewById(R.id.motivoconsulta);
        nivelEdu = findViewById(R.id.nivelEdu);
        curso = findViewById(R.id.curso);
        btnAgregar = findViewById(R.id.btnAgregar);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> volverALista());

        ArrayAdapter<String> adapterNivel = new ArrayAdapter<>(this, R.layout.spinner_item, niveles);
        adapterNivel.setDropDownViewResource(R.layout.spinner_dropdown_item);
        nivelEdu.setAdapter(adapterNivel);
        nivelEdu.setOnClickListener(v -> nivelEdu.showDropDown());

        ArrayAdapter<String> adapterCurso = new ArrayAdapter<>(this, R.layout.spinner_item, cursos);
        adapterCurso.setDropDownViewResource(R.layout.spinner_dropdown_item);
        curso.setAdapter(adapterCurso);
        curso.setOnClickListener(v -> curso.showDropDown());

        fechaNac.setOnClickListener(v -> mostrarDatePicker());

        TextView titulo = findViewById(R.id.textView2);

        if (pacienteEditar != null) {
            cargarDatosPaciente();
            if (titulo != null) titulo.setText("EDITAR PACIENTE");
            btnAgregar.setText("EDITAR");
        } else {
            if (titulo != null) titulo.setText("AGREGAR PACIENTE");
            btnAgregar.setText("AGREGAR");
        }

        btnAgregar.setOnClickListener(v -> guardarPaciente());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupHeaderUsuario();
    }

    private void mostrarDatePicker() {
        int dark = getResources().getColor(R.color.colorPrimary);
        int light = getResources().getColor(R.color.colorPrimaryLight);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth,
                (DatePicker view, int year, int month, int day) -> {
                    calendario.set(Calendar.YEAR, year);
                    calendario.set(Calendar.MONTH, month);
                    calendario.set(Calendar.DAY_OF_MONTH, day);
                    SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    fechaNac.setText(formato.format(calendario.getTime()));
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );

        dialog.setOnShowListener(d -> {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(dark);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(dark);

            int titleId = getResources().getIdentifier("alertTitle", "id", "android");
            if (titleId != 0) {
                TextView titleView = dialog.findViewById(titleId);
                if (titleView != null) titleView.setTextColor(dark);
            }

            try {
                Field datePickerField = dialog.getClass().getDeclaredField("mDatePicker");
                datePickerField.setAccessible(true);
                DatePicker datePicker = (DatePicker) datePickerField.get(dialog);

                int dayId = getResources().getIdentifier("day", "id", "android");
                int monthId = getResources().getIdentifier("month", "id", "android");
                int yearId = getResources().getIdentifier("year", "id", "android");

                EditText dayView = dialog.findViewById(dayId);
                EditText monthView = dialog.findViewById(monthId);
                EditText yearView = dialog.findViewById(yearId);

                if (dayView != null) dayView.setTextColor(light);
                if (monthView != null) monthView.setTextColor(light);
                if (yearView != null) yearView.setTextColor(light);

                int headerId = getResources().getIdentifier("date_picker_header", "id", "android");
                if (headerId != 0) {
                    try {
                        dialog.findViewById(headerId).setBackgroundColor(dark);
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
        });

        dialog.show();
    }

    private void cargarDatosPaciente() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        nombre.setText(pacienteEditar.getNombre());
        apellido.setText(pacienteEditar.getApellido());
        dni.setText(pacienteEditar.getDni());
        telefono.setText(pacienteEditar.getTelefono());
        motivoconsulta.setText(pacienteEditar.getMotivoConsulta());

        if (pacienteEditar.getFechaNac() != null) {
            fechaNac.setText(sdf.format(pacienteEditar.getFechaNac()));
        }

        if (pacienteEditar.getNivelEducativo() != null) {
            nivelEdu.setText(pacienteEditar.getNivelEducativo(), false);
        }

        int grado = pacienteEditar.getGradoCurso();
        if (grado >= 1 && grado <= cursos.length) {
            curso.setText(cursos[grado - 1], false);
        }
    }

    private void guardarPaciente() {
        String n = nombre.getText().toString();
        String a = apellido.getText().toString();
        String d = dni.getText().toString();
        String t = telefono.getText().toString();
        String f = fechaNac.getText().toString();
        String m = motivoconsulta.getText().toString();
        String niv = nivelEdu.getText().toString();
        String c = curso.getText().toString();

        ArrayList<String> errores = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        java.util.Date fechaParseada = null;
        try { fechaParseada = sdf.parse(f); } catch (Exception ignored) {}

        Paciente p = new Paciente();

        try { p.setNombre(n); } catch (Exception e) { errores.add(e.getMessage()); }
        try { p.setApellido(a); } catch (Exception e) { errores.add(e.getMessage()); }
        try { p.setDni(d); } catch (Exception e) { errores.add(e.getMessage()); }
        try { p.setTelefono(t); } catch (Exception e) { errores.add(e.getMessage()); }

        if (fechaParseada == null) errores.add("La fecha de nacimiento no es válida");
        else {
            try { p.setFechaNac(fechaParseada); } catch (Exception e) { errores.add(e.getMessage()); }
        }

        int gradoValor = 0;
        if (!c.trim().isEmpty()) {
            try { gradoValor = Integer.parseInt(c.replaceAll("[^0-9]", "")); }
            catch (Exception ignored) { errores.add("El grado/curso no es válido"); }
        } else errores.add("Debe seleccionar un grado/curso");

        if (gradoValor > 0) {
            try { p.setGradoCurso(gradoValor); } catch (Exception e) { errores.add(e.getMessage()); }
        }

        try { p.setNivelEducativo(niv); } catch (Exception e) { errores.add(e.getMessage()); }
        try { p.setMotivoConsulta(m); } catch (Exception e) { errores.add(e.getMessage()); }

        if (!errores.isEmpty()) {
            mostrarAlertaErrores(errores);
            return;
        }

        if (indiceEdicion >= 0 && indiceEdicion < pacientes.size()) {
            pacientes.set(indiceEdicion, p);
        } else {
            pacientes.add(p);
        }

        btnAgregar.setEnabled(false);

        String pacienteId = p.getDni();

        db.collection("pacientes")
                .document(pacienteId)
                .set(PacienteMapper.toMap(p))
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Paciente guardado en Firestore: " + pacienteId);
                    btnAgregar.setEnabled(true);
                    volverALista();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error guardando paciente: " + e.getMessage());
                    btnAgregar.setEnabled(true);
                    ArrayList<String> errs = new ArrayList<>();
                    errs.add("No se pudo guardar en la nube. Revisá tu conexión e intentá de nuevo.");
                    mostrarAlertaErrores(errs);
                });
    }

    private void volverALista() {
        Intent i = new Intent(this, ListaPacientesActivity.class);
        i.putExtra("pacientes", pacientes);
        startActivity(i);
        finish();
    }

    private void mostrarAlertaErrores(ArrayList<String> errores) {
        StringBuilder mensaje = new StringBuilder();
        for (String e : errores) mensaje.append("• ").append(e).append("\n");
        new AlertDialog.Builder(this)
                .setTitle("Errores en el formulario")
                .setMessage(mensaje.toString())
                .setPositiveButton("Aceptar", null)
                .show();
    }
}
