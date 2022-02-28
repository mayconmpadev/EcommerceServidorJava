package com.example.ecommerceservidorjava.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;

public class SplashActivity extends AppCompatActivity {
    SPM spm = new SPM(this);
    String caminho = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler(getMainLooper()).postDelayed(this::verificaAcesso, 3000);
caminho = spm.getPreferencia("PREFERENCIAS","CAMINHO", "");

    }

    private void verificaAcesso() {
        if (caminho.equals("")) {
            finish();
            startActivity(new Intent(this, CaminhoServidorActivity.class));

        } else if( FirebaseHelper.getAutenticado()){
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }else {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}