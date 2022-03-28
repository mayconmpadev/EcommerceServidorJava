package com.example.ecommerceservidorjava.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import com.example.ecommerceservidorjava.databinding.ActivityCaminhoServidorBinding;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class CaminhoServidorActivity extends AppCompatActivity {
    private ActivityCaminhoServidorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCaminhoServidorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        configClicks();
    }

    private void salvarCaminho(){
        binding.progressBar.setVisibility(View.VISIBLE);
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(Base64Custom.codificarBase64(binding.editCaminhoBanco.getText().toString().trim()));
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String caminho = binding.editCaminhoBanco.getText().toString().trim();
                    SPM spm = new SPM(getApplicationContext());
                    spm.setPreferencia("PREFERENCIAS","CAMINHO", caminho);
                    if (!spm.getPreferencia("PREFERENCIAS", "CAMINHO","").equals("")){
                        binding.progressBar.setVisibility(View.GONE);
                        Intent intent =  new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }else {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getBaseContext(), "Esse caminho não existe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void configClicks(){
        binding.btnSalvar.setOnClickListener(view -> salvarCaminho());
    }
}