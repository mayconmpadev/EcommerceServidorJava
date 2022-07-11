package com.example.ecommerceservidorjava.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ActivityCadastroOrdemServicoBinding;
import com.example.ecommerceservidorjava.databinding.ActivityListaOrdemServicoBinding;

public class ListaOrdemServicoActivity extends AppCompatActivity {
    ActivityListaOrdemServicoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      binding = ActivityListaOrdemServicoBinding.inflate(getLayoutInflater());
      setContentView(binding.getRoot());

      binding.floatingActionButton.setOnClickListener(view -> {
          Intent intent = new Intent(getApplicationContext(), CadastroOrdemServicoActivity.class);
          startActivity(intent);
      });
    }
}