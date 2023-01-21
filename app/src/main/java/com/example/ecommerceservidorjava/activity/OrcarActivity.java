package com.example.ecommerceservidorjava.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.ecommerceservidorjava.databinding.ActivityOrcarBinding;

public class OrcarActivity extends AppCompatActivity {
    private ActivityOrcarBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrcarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), OrcarPecasActivity.class);
            startActivity(intent);
        });

    }
}