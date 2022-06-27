package com.example.ecommerceservidorjava.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.adapter.CadastroVendaAdapter;
import com.example.ecommerceservidorjava.adapter.ListaCategoriaHorizontalAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityCadastroVendaBinding;
import com.example.ecommerceservidorjava.model.Categoria;
import com.example.ecommerceservidorjava.model.ItemVenda;
import com.example.ecommerceservidorjava.model.Produto;
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.example.ecommerceservidorjava.util.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CadastroVendaActivity extends AppCompatActivity implements CadastroVendaAdapter.OnClickLister, ListaCategoriaHorizontalAdapter.OnClickLister {
    ActivityCadastroVendaBinding binding;
    CadastroVendaAdapter produtoAdapter;

    private final List<Produto> produtoList = new ArrayList<>();
    private final List<ItemVenda> itemVendaList = new ArrayList<>();
    private final List<Categoria> categoriaList = new ArrayList<>();
    private final List<Produto> filtroList = new ArrayList<>();
    private final List<ItemVenda> filtroItemVendaList = new ArrayList<>();
    private List<ItemVenda> filtroItemVendaCategotia = new ArrayList<>();
    private List<Produto> filtroProdutoCategoriaList = new ArrayList<>();

    private Categoria categoriaSelecionada;
    private int quantidade = 0;
    private String pesquisa = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroVendaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        configSearchView();
        recuperaProdutos();
        recuperaCategotia();

        binding.includeSheet.btnContinue.setOnClickListener(view -> {
            selecionarItems();

        });

    }

    private void configSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String texto) {
                pesquisa = texto;
                ocultaTeclado();
                filtroList.clear();
                filtroItemVendaList.clear();
                filtraProdutoNome(texto);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        binding.searchView.findViewById(R.id.search_close_btn).setOnClickListener(v -> {
            EditText edtSerachView = binding.searchView.findViewById(R.id.search_src_text);
            binding.textVazio.setVisibility(View.GONE);
            edtSerachView.setText("");
            pesquisa = "";
            edtSerachView.clearFocus();
            ocultaTeclado();

            configRvProdutos(filtroProdutoCategoriaList, filtroItemVendaCategotia);
        });

    }


    private void filtraProdutoNome(String pesquisa) {

        filtroList.clear();
        for (Produto produto : filtroProdutoCategoriaList) {
            if (produto.getNome().toUpperCase(Locale.ROOT).contains(pesquisa.toUpperCase(Locale.ROOT))) {
                filtroList.add(produto);

            }
        }

        for (ItemVenda itemVenda : filtroItemVendaCategotia) {
            if (itemVenda.getNome().toUpperCase(Locale.ROOT).contains(pesquisa.toUpperCase(Locale.ROOT))) {
                filtroItemVendaList.add(itemVenda);

            }
        }


        configRvProdutos(filtroList, filtroItemVendaList);


        if (filtroList.isEmpty()) {
            binding.textVazio.setVisibility(View.VISIBLE);
            binding.textVazio.setText("Nenhum produto encontrado.");
        } else {
            binding.textVazio.setVisibility(View.GONE);
        }
    }

    private void configRvProdutos(List<Produto> produtoList, List<ItemVenda> itemVendaList) {
        binding.recycler.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        binding.recycler.setHasFixedSize(true);
        produtoAdapter = new CadastroVendaAdapter(produtoList, itemVendaList, getApplicationContext(), this);
        binding.recycler.setAdapter(produtoAdapter);
    }

    private void configRvCategoria() {
        binding.rvCategorias.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategorias.setHasFixedSize(true);
        ListaCategoriaHorizontalAdapter listaCategoriaAdapter = new ListaCategoriaHorizontalAdapter(R.layout.item_lista_usuario, categoriaList, getApplicationContext(), true, this, true);
        binding.rvCategorias.setAdapter(listaCategoriaAdapter);
    }


    private void recuperaCategotia() {
        SPM spm = new SPM(getApplicationContext());
        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(caminho).child("categorias").orderByChild("todas");
        produtoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                categoriaList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Categoria categoria = ds.getValue(Categoria.class);
                        categoriaList.add(categoria);

                        if (categoria.isTodas() && categoriaSelecionada == null) {
                            categoriaSelecionada = categoria;
                        }
                    }
                    Collections.reverse(categoriaList);
                    configRvCategoria();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void recuperaProdutos() {
        SPM spm = new SPM(getApplicationContext());
        Query produtoRef = FirebaseHelper.getDatabaseReference()
                .child("empresas").child(Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", "")))
                .child("produtos").orderByChild("nome");
        produtoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                produtoList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Produto produto = ds.getValue(Produto.class);

                        produtoList.add(produto);
                        filtroProdutoCategoriaList.add(produto);
                        ItemVenda itemVenda = new ItemVenda();
                        itemVenda.setIdProduto(produto.getId());
                        itemVenda.setIdsCategorias(produto.getIdsCategorias());
                        itemVenda.setCodigo(produto.getCodigo());
                        itemVenda.setNome(produto.getNome());
                        itemVenda.setPreco(produto.getPrecoVenda());
                        itemVenda.setDescricao(produto.getDescricao());
                        itemVenda.setFoto(produto.getUrlImagem0());
                        itemVendaList.add(itemVenda);
                        filtroItemVendaCategotia.add(itemVenda);


                        binding.progressBar2.setVisibility(View.GONE);
                        binding.textVazio.setVisibility(View.GONE);
                    }
                    configRvProdutos(produtoList, itemVendaList);
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

    private void filtraProdutoCategoria() {
        if (!categoriaSelecionada.isTodas()) {
            filtroProdutoCategoriaList.clear();
            filtroItemVendaCategotia.clear();
            for (Produto produto : produtoList) {
                if (produto.getIdsCategorias().contains(categoriaSelecionada.getId())) {
                    if (!filtroProdutoCategoriaList.contains(produto)) {
                        filtroProdutoCategoriaList.add(produto);
                    }
                }
            }


            for (ItemVenda itemVenda : itemVendaList) {
                if (itemVenda.getIdsCategorias().contains(categoriaSelecionada.getId())) {
                    if (!filtroItemVendaCategotia.contains(itemVenda)) {
                        filtroItemVendaCategotia.add(itemVenda);
                    }
                }
            }

            if (!pesquisa.equals("")) {
                filtraProdutoNome(pesquisa);
            } else {
                configRvProdutos(filtroProdutoCategoriaList, filtroItemVendaCategotia);
            }

        } else {
            filtroProdutoCategoriaList = new ArrayList<>(produtoList);
            filtroItemVendaCategotia = new ArrayList<>(itemVendaList);
            if (!pesquisa.equals("")) {
                filtraProdutoNome(pesquisa);
            } else {
                configRvProdutos(filtroProdutoCategoriaList, filtroItemVendaCategotia);
            }
        }
    }

    private void listVazia() {
        if (produtoList.isEmpty()) {
            binding.textVazio.setText("Nenhum produto cadastrado.");
        } else {
            binding.textVazio.setText("");
        }
    }

    private void somar(int position, ItemVenda itemVenda) {

        itemVenda.setQtd(itemVenda.getQtd() + 1);
        quantidade = quantidade + 1;
        binding.includeSheet.counterBadge.setText(String.valueOf(quantidade));

        produtoAdapter.notifyItemChanged(position);
        binding.lytCartSheet.setVisibility(View.VISIBLE);

        binding.includeSheet.tvTotalCart.setText(total());
    }

    private void subtrair(int position, ItemVenda itemVenda) {
        itemVenda.setQtd(itemVenda.getQtd() - 1);
        quantidade = quantidade - 1;
        binding.includeSheet.counterBadge.setText(String.valueOf(quantidade));
        produtoAdapter.notifyItemChanged(position);
        binding.includeSheet.tvTotalCart.setText(total());
        if (quantidade == 0) {
            binding.lytCartSheet.setVisibility(View.GONE);
        }

    }

    private String total() {
        BigDecimal total = new BigDecimal("0");

        for (int i = 0; i < itemVendaList.size(); i++) {
            if (itemVendaList.get(i).getQtd() != 0) {
                BigDecimal preco = Util.convertMoneEmBigDecimal(itemVendaList.get(i).getPreco());
                preco = preco.divide(new BigDecimal("100"));
                total = total.add(new BigDecimal(itemVendaList.get(i).getQtd()).multiply(preco));
            }

        }
        return NumberFormat.getCurrencyInstance().format(total);
    }

    private void selecionarItems() {
        ArrayList<ItemVenda> arrayList = new ArrayList<>();
        for (int i = 0; i < itemVendaList.size(); i++) {
            if (itemVendaList.get(i).getQtd() != 0) {
                arrayList.add(itemVendaList.get(i));
            }
        }

        Intent intent = new Intent(getApplicationContext(), CarrinhoVendaActivity.class);
        intent.putExtra("itemVenda", arrayList);
        startActivity(intent);
    }

    // Oculta o teclado do dispotivo
    private void ocultaTeclado() {
        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(binding.searchView.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }


    @Override
    public void onClick(Categoria categoria) {
        this.categoriaSelecionada = categoria;
        filtraProdutoCategoria();
    }

    @Override
    public void onLongClick(Categoria categoria) {

    }

    @Override
    public void onClick(int position, ItemVenda itemVenda, String operacao) {

        if (operacao.equals("mais")) {
            somar(position, itemVenda);
        }
        if (operacao.equals("menos")) {
            subtrair(position, itemVenda);
        }

    }
}

