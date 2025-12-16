package com.example.psicopedagogiaandroid;

import android.content.Intent;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BaseActivity extends AppCompatActivity {

    protected void setupHeaderUsuario() {
        TextView tv = findViewById(R.id.tvUsuarioEmail);
        if (tv == null) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user != null && user.getEmail() != null ? user.getEmail() : getString(R.string.sin_sesion);
        tv.setText(email);

        tv.setOnClickListener(v -> mostrarDialogoLogout());
    }

    private void mostrarDialogoLogout() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.cerrar_sesi_n))
                .setMessage(getString(R.string.pregunta_cerrar_sesion))
                .setPositiveButton(getString(R.string.si), (d, w) -> logout())
                .setNegativeButton(getString(R.string.no), (d, w) -> d.dismiss())
                .show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
