package com.example.psicopedagogiaandroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN_FIREBASE";

    private FirebaseAuth auth;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> login());
        findViewById(R.id.tvCrearCuenta).setOnClickListener(v -> registrar());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            irAHome();
        }
    }

    private void login() {
        String email = "";
        String password = "";

        if (tilEmail.getEditText() != null) {
            email = tilEmail.getEditText().getText().toString().trim();
        }

        if (tilPassword.getEditText() != null) {
            password = tilPassword.getEditText().getText().toString();
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Ingresá el email");
            return;
        } else {
            tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Ingresá la contraseña");
            return;
        } else {
            tilPassword.setError(null);
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Ingreso exitoso", Toast.LENGTH_SHORT).show();
                    irAHome();
                })
                .addOnFailureListener(e -> {
                    String mensaje;
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        mensaje = "La contraseña es incorrecta";
                    } else if (e instanceof FirebaseAuthInvalidUserException) {
                        mensaje = "No existe una cuenta con ese email";
                    } else {
                        mensaje = "Error al iniciar sesión. Intentá nuevamente";
                    }
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
                });
    }

    private void registrar() {
        String email = "";
        String password = "";

        if (tilEmail.getEditText() != null) {
            email = tilEmail.getEditText().getText().toString().trim();
        }

        if (tilPassword.getEditText() != null) {
            password = tilPassword.getEditText().getText().toString();
        }

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Completá email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Cuenta creada", Toast.LENGTH_SHORT).show();
                    irAHome();
                })
                .addOnFailureListener(e -> {
                    String mensaje;
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        mensaje = "Ese email ya está registrado";
                    } else {
                        mensaje = "Error al crear la cuenta. Intentá nuevamente";
                    }
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
                });
    }

    private void irAHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
