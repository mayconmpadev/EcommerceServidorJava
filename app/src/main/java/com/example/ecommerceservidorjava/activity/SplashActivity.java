package com.example.ecommerceservidorjava.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {
    SPM spm = new SPM(this);
    String caminho = "";
    boolean a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler(getMainLooper()).postDelayed(this::salvarCaminho, 1000);
        caminho = spm.getPreferencia("PREFERENCIAS", "CAMINHO", "");

    }

    private void verificaAcesso() {
        if (caminho.equals("") ) {
            finish();
            startActivity(new Intent(this, CaminhoServidorActivity.class));

        } else if (FirebaseHelper.getAutenticado()) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        } else {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void salvarCaminho() {

        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(Base64Custom.codificarBase64(caminho));
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    verificaAcesso();
                } else {
                    finish();
                    startActivity(new Intent(getApplicationContext(), CaminhoServidorActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}