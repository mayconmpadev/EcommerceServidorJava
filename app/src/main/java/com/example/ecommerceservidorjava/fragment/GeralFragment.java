package com.example.ecommerceservidorjava.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

            // Get a PrintManager instance
            PrintManager printManager = (PrintManager) getActivity()
                    .getSystemService(Context.PRINT_SERVICE);

            // Set job name, which will be displayed in the print queue
            String jobName = getActivity().getString(R.string.app_name) + " Document";

            // Start a print job, passing in a PrintDocumentAdapter implementation
            // to handle the generation of a print document


        });
    }

    private void verificacaoCadastro() {
        //a = false;
        binding.cardOrcamento.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.branco));
        binding.cardPedidos.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.branco));
        binding.cardOrcamento.setEnabled(true);
        binding.cardPedidos.setEnabled(true);
        List<String> caminhos = new ArrayList<>();
        caminhos.add("perfil_empresa");
        caminhos.add("produtos");
        caminhos.add("categorias");


        for (int i = 0; i < caminhos.size(); i++) {


            DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference()
                    .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                    .child(caminhos.get(i));
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {

                        a = true;
                        binding.cardOrcamento.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.color_cinza));
                        binding.cardPedidos.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.color_cinza));
                        binding.cardOrcamento.setEnabled(false);
                        binding.cardPedidos.setEnabled(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            if (a) {
                break;
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();


        verificacaoCadastro();
    }
}