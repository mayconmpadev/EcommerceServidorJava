package com.example.ecommerceservidorjava.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.activity.ConfiguracaoActivity;
import com.example.ecommerceservidorjava.activity.ListaBoletoActivity;
import com.example.ecommerceservidorjava.activity.ListaCategoriaActivity;
import com.example.ecommerceservidorjava.activity.ListaClienteActivity;
import com.example.ecommerceservidorjava.activity.ListaDespesaActivity;
import com.example.ecommerceservidorjava.activity.ListaOrcamentoActivity;
import com.example.ecommerceservidorjava.activity.ListaOrdemServicoActivity;
import com.example.ecommerceservidorjava.activity.ListaProdutoActivity;
import com.example.ecommerceservidorjava.activity.ListaUsuarioActivity;
import com.example.ecommerceservidorjava.activity.ListaVendaActivity;
import com.example.ecommerceservidorjava.activity.PerfilEmpresaActivity;
import com.example.ecommerceservidorjava.databinding.FragmentGeralBinding;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class GeralFragment extends Fragment {
    private SPM spm;
    private FragmentGeralBinding binding;
    boolean a = false;
//TODO: redesenhar essa tela

    @SuppressLint("ResourceAsColor")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        spm = new SPM(getContext());
        verificacaoCadastro();
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

        binding.ordenServico.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), ListaOrdemServicoActivity.class);
            startActivity(intent);

        });

        binding.boletos.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), ListaBoletoActivity.class);
            startActivity(intent);

        });
    }

    private void verificacaoCadastro() {

        List<String> caminhos = new ArrayList<>();
        caminhos.add("" +
                "");
        caminhos.add("produtos");
        caminhos.add("categorias");
        caminhos.add("perfil_empresa");

        for (int i = 0; i < caminhos.size(); i++) {


            DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference()
                    .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                    .child(caminhos.get(i));
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        binding.cardOrcamento.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.color_cinza));
                        binding.cardPedidos.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.color_cinza));
                        binding.cardOrcamento.setEnabled(false);
                        binding.cardPedidos.setEnabled(false);
                        a = true;

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            if (a){
                break;
            }
        }

    }
}