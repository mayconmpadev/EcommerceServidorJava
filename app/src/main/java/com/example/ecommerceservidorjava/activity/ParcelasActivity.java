package com.example.ecommerceservidorjava.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ActivityCadastroDespesaBinding;
import com.example.ecommerceservidorjava.databinding.ActivityParcelasBinding;
import com.example.ecommerceservidorjava.model.Despesa;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.Timestamp;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Calendar;

public class ParcelasActivity extends AppCompatActivity {
    private ActivityParcelasBinding binding;
    private Despesa despesaSelecionado;
    int qtd = 1;
    ArrayList<TextView> listParcelas = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityParcelasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        parcelas(binding.textParcela1);
        parcelas(binding.textParcela2);
        parcelas(binding.textParcela3);
        parcelas(binding.textParcela4);
        parcelas(binding.textParcela5);
        parcelas(binding.textParcela6);
        parcelas(binding.textParcela7);
        parcelas(binding.textParcela8);
        parcelas(binding.textParcela9);
        parcelas(binding.textParcela10);
        parcelas(binding.textParcela11);
        parcelas(binding.textParcela12);
    }


    private void recuperarIntent() {
        listParcelas.clear();
        listParcelas.add(binding.textParcela1);
        listParcelas.add(binding.textParcela2);
        listParcelas.add(binding.textParcela3);
        listParcelas.add(binding.textParcela4);
        listParcelas.add(binding.textParcela5);
        listParcelas.add(binding.textParcela6);
        listParcelas.add(binding.textParcela7);
        listParcelas.add(binding.textParcela8);
        listParcelas.add(binding.textParcela9);
        listParcelas.add(binding.textParcela10);
        listParcelas.add(binding.textParcela11);
        listParcelas.add(binding.textParcela12);
        despesaSelecionado = (Despesa) getIntent().getSerializableExtra("despesaSelecionado");
        if (despesaSelecionado != null){
            for (int i = 0; i < despesaSelecionado.getQtd_parcelas(); i++) {
                listParcelas.get(i).setVisibility(View.VISIBLE);

            }

            for (int i = 0; i < despesaSelecionado.getParcela_paga() + qtd; i++) {
              listParcelas.get(i).setBackgroundTintList(ColorStateList.valueOf( ContextCompat.getColor(this, R.color.color_verde_claro)));
              listParcelas.get(i).setTextColor(Color.parseColor("#FFFFFF"));


            }


        }
    }

    private void parcelas(TextView textView){
        textView.setOnClickListener(view -> {
            qtd++;
            recuperarIntent();
        });

    }
}