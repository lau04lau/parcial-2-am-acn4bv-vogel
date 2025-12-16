package com.example.psicopedagogiaandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MAIN_ACTIVITY";

    private final ArrayList<Paciente> pacientes = new ArrayList<>();
    private final ArrayList<Historial> historial = new ArrayList<>();

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        setupHeaderUsuario();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.d(TAG, "No hay sesión, redirigiendo a Login");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            setupHeaderUsuario();
        }
    }

    public void btnPacientes(View v) {
        Intent i = new Intent(MainActivity.this, ListaPacientesActivity.class);
        i.putExtra("pacientes", pacientes);
        i.putExtra("historial", historial);
        startActivity(i);
    }
}
