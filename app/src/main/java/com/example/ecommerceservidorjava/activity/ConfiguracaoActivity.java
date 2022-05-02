package com.example.ecommerceservidorjava.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecommerceservidorjava.databinding.ActivityConfiguracaoBinding;
import com.example.ecommerceservidorjava.model.Configuracao;
import com.example.ecommerceservidorjava.model.Endereco;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ConfiguracaoActivity extends AppCompatActivity {
    ActivityConfiguracaoBinding binding;
    private Configuracao configuracao;
    private Endereco endereco;
    private SPM spm = new SPM(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfiguracaoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.include.textTitulo.setText("Perfil Empresa");
        configClicks();

        recuperarPerfil();

    }


    private void recuperarPerfil() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));


        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("configuracao");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.progressBar.setVisibility(View.GONE);
                    configuracao = snapshot.getValue(Configuracao.class);
                    binding.edtDescontoDinheiro.setText(String.valueOf(configuracao.getDesconto_dinheiro()));
                    binding.edtDescontoDebito.setText(String.valueOf(configuracao.getDesconto_debito()));
                    binding.edtParcelas.setText(String.valueOf(configuracao.getQtd_parcelas()));
                    binding.edtRodape.setText(configuracao.getRodape());


                } else {
                    configuracao = new Configuracao();
                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //---------------------------------------------------- SALVAR IMAGEM E DADOS -----------------------------------------------------------------
    public void salvar() {

        binding.progressBar.setVisibility(View.VISIBLE);
        configuracao.setDesconto_dinheiro(Integer.parseInt(binding.edtDescontoDinheiro.getText().toString()));
        configuracao.setDesconto_debito(Integer.parseInt(binding.edtDescontoDebito.getText().toString()));
        configuracao.setQtd_parcelas(Integer.parseInt(binding.edtParcelas.getText().toString()));
        configuracao.setRodape(binding.edtRodape.getText().toString());
        configuracao.setId("configuracao");

        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));

        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child(configuracao.getId());
        databaseReference.setValue(configuracao).addOnSuccessListener(unused -> binding.progressBar.setVisibility(View.GONE))
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });


    }


    private void configClicks() {
        binding.edtDescontoDinheiro.setTransformationMethod(null);
        binding.edtDescontoDebito.setTransformationMethod(null);
        binding.edtParcelas.setTransformationMethod(null);
        binding.include.include.ibVoltar.setOnClickListener(view -> finish());
        binding.btnCriarConta.setOnClickListener(view -> salvar());

    }


}