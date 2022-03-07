package com.example.ecommerceservidorjava.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.adapter.ListaProdutoAdapter;
import com.example.ecommerceservidorjava.adapter.ListaUsuarioAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityListaProdutoBinding;
import com.example.ecommerceservidorjava.model.Produto;
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

public class ListaProdutoActivity extends AppCompatActivity implements ListaProdutoAdapter.OnClickLister, ListaProdutoAdapter.OnLongClickLister {
    ActivityListaProdutoBinding binding;
    private final List<Produto> produtoList = new ArrayList<>();
    List<Produto> filtroProdutoNomeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListaProdutoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        configSearchView();
        recuperaProdutos();
        binding.floatingActionButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), CadastroProdutoActivity.class);
            startActivity(intent);
        });
        binding.recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
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
            //binding.textInfo.setText("");
            edtSerachView.setText("");
            edtSerachView.clearFocus();
            ocultaTeclado();
            filtroProdutoNomeList.clear();
            configRvProdutos(produtoList);
        });

    }


    private void filtraProdutoNome(String pesquisa) {


        for (Produto produto : produtoList) {
            if (produto.getNome().toUpperCase(Locale.ROOT).contains(pesquisa.toUpperCase(Locale.ROOT))) {
                filtroProdutoNomeList.add(produto);
            }
        }


        configRvProdutos(filtroProdutoNomeList);
    }

    private void configRvProdutos(List<Produto> usuarioList) {
        binding.recycler.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        binding.recycler.setHasFixedSize(true);
        ListaProdutoAdapter lojaProdutoAdapter = new ListaProdutoAdapter(R.layout.item_produto_adapter, usuarioList, getApplicationContext(), true, this, this);
        binding.recycler.setAdapter(lojaProdutoAdapter);
    }


    private void recuperaProdutos() {
        SPM spm = new SPM(getApplicationContext());
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""))).child("produtos").orderByChild("nome");
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                produtoList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Produto produto = ds.getValue(Produto.class);
                        produtoList.add(produto);
                        binding.progressBar2.setVisibility(View.GONE);
                        binding.textVazio.setVisibility(View.GONE);
                    }
                    configRvProdutos(produtoList);
                }else {
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


    public void onClick(Produto produto) {
        //  Intent intent = new Intent(getContext(), ExibirVolumeActivity.class);
        //  intent.putExtra("numero", produto);
        // startActivity(intent);
        Toast.makeText(getApplicationContext(), produto.getNome(), Toast.LENGTH_SHORT).show();


    }

    @Override
    public void onLongClick(Produto usuario) {

    }


}