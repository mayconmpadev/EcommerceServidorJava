package com.example.ecommerceservidorjava.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.example.ecommerceservidorjava.activity.CadastroDespesaActivity;
import com.example.ecommerceservidorjava.activity.ConfiguracaoActivity;
import com.example.ecommerceservidorjava.activity.ListaCategoriaActivity;
import com.example.ecommerceservidorjava.activity.ListaClienteActivity;
import com.example.ecommerceservidorjava.activity.ListaDespesaActivity;
import com.example.ecommerceservidorjava.activity.ListaOrcamentoActivity;
import com.example.ecommerceservidorjava.activity.ListaUsuarioActivity;
import com.example.ecommerceservidorjava.activity.ListaProdutoActivity;
import com.example.ecommerceservidorjava.activity.ListaVendaActivity;
import com.example.ecommerceservidorjava.activity.PerfilEmpresaActivity;
import com.example.ecommerceservidorjava.databinding.FragmentGeralBinding;
import com.example.ecommerceservidorjava.model.Configuracao;
import com.example.ecommerceservidorjava.util.FirebaseHelper;


public class GeralFragment extends Fragment {

private FragmentGeralBinding binding;



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       configClicks();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentGeralBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void configClicks() {
        binding.cardUsuario.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), ListaUsuarioActivity.class);
            startActivity(intent);

        });

        binding.cardPedidos.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), ListaVendaActivity.class);
            startActivity(intent);

        });

        binding.cardProdutos.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), ListaProdutoActivity.class);
            startActivity(intent);

        });

        binding.cardCategoria.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), ListaCategoriaActivity.class);
            startActivity(intent);

        });

        binding.cardClientes.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), ListaClienteActivity.class);
            startActivity(intent);

        });

        binding.cardOrcamento.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), ListaOrcamentoActivity.class);
            startActivity(intent);

        });

        binding.perfilEmpresa.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), PerfilEmpresaActivity.class);
            startActivity(intent);

        });

        binding.ajustes.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), ConfiguracaoActivity.class);
            startActivity(intent);

        });

        binding.despesa.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), ListaDespesaActivity.class);
            startActivity(intent);

        });
    }
}