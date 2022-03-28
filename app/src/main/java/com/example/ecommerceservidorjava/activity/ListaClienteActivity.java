package com.example.ecommerceservidorjava.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.adapter.ListaClienteAdapter;
import com.example.ecommerceservidorjava.adapter.ListaUsuarioAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityListaClienteBinding;
import com.example.ecommerceservidorjava.model.Cliente;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListaClienteActivity extends AppCompatActivity implements ListaClienteAdapter.OnClickLister, ListaClienteAdapter.OnLongClickLister {
    ActivityListaClienteBinding binding;
    private final List<Cliente> clienteList = new ArrayList<>();
    List<Cliente> filtroProdutoNomeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityListaClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        configSearchView();
        recuperaProdutos();
        binding.floatingActionButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), CadastroClienteActivity.class);
            startActivity(intent);
        });
        binding.recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && binding.floatingActionButton.getVisibility() == View.VISIBLE) {
                    binding.floatingActionButton.hide();
                } else if (dy < 0 && binding.floatingActionButton.getVisibility() != View.VISIBLE) {
                    binding.floatingActionButton.show();
                }
            }
        });
    }

    private void configSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String pesquisa) {
                ocultaTeclado();

                filtraProdutoNome(pesquisa);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        binding.searchView.findViewById(R.id.search_close_btn).setOnClickListener(v -> {
            EditText edtSerachView = binding.searchView.findViewById(R.id.search_src_text);
            edtSerachView.setText("");
            edtSerachView.clearFocus();
            ocultaTeclado();
            filtroProdutoNomeList.clear();
            configRvProdutos(clienteList);
        });

    }


    private void filtraProdutoNome(String pesquisa) {


        for (Cliente cliente : clienteList) {
            if (cliente.getNome().toUpperCase(Locale.ROOT).contains(pesquisa.toUpperCase(Locale.ROOT))) {
                filtroProdutoNomeList.add(cliente);
            }
        }


        configRvProdutos(filtroProdutoNomeList);
    }

    private void configRvProdutos(List<Cliente> clienteList) {
        binding.recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recycler.setHasFixedSize(true);
        ListaClienteAdapter clienteAdapter = new ListaClienteAdapter(R.layout.item_lista_usuario, clienteList, getApplicationContext(), true, this, this);
        binding.recycler.setAdapter(clienteAdapter);
    }


    private void recuperaProdutos() {
        SPM spm = new SPM(getApplicationContext());
        //String user = FirebaseHelper.getAuth().getCurrentUser().getUid();
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""))).child("clientes").orderByChild("nome");
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                clienteList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Cliente cliente = ds.getValue(Cliente.class);
                        clienteList.add(cliente);
                        binding.progressBar2.setVisibility(View.GONE);
                    }
                    configRvProdutos(clienteList);
                } else {
                    binding.progressBar2.setVisibility(View.GONE);
                    binding.textVazio.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    // Oculta o teclado do dispotivo
    private void ocultaTeclado() {
        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(binding.searchView.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }


    public void onClick(Cliente cliente) {
        Intent intent = new Intent(getApplicationContext(), CadastroClienteActivity.class);
        intent.putExtra("clienteSelecionado", cliente);
        startActivity(intent);


    }

    @Override
    public void onLongClick(Cliente cliente) {
        //   Intent intent = new Intent(getContext(), EditarVolumeActivity.class);
        // intent.putExtra("numero", volume);
        // startActivity(intent);
    }


}