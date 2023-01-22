package com.example.ecommerceservidorjava.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.adapter.ListaPecasAdapter;
import com.example.ecommerceservidorjava.adapter.ListaProdutoAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityOrcarBinding;
import com.example.ecommerceservidorjava.model.ItemVenda;
import com.example.ecommerceservidorjava.model.Produto;
import com.example.ecommerceservidorjava.util.Util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class OrcarActivity extends AppCompatActivity implements ListaPecasAdapter.OnClickLister, ListaPecasAdapter.OnLongClickLister {
    private ActivityOrcarBinding binding;
    ListaPecasAdapter produtoAdapter;
    private ArrayList<ItemVenda> itemVendaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrcarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        binding.floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), OrcarPecasActivity.class);
            startActivity(intent);
        });

    }

    private void recuperarIntent() {

        itemVendaList = (ArrayList<ItemVenda>) getIntent().getSerializableExtra("itemVenda");

        if (itemVendaList != null) {
            configRvProdutos(itemVendaList);
            binding.textValorPecas.setText(total());
        }


    }

    private void configRvProdutos(List<ItemVenda> itemVendas) {
        binding.recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recycler.setHasFixedSize(true);
        produtoAdapter = new ListaPecasAdapter(R.layout.item_produto_adapter, itemVendas, getApplicationContext(), true, this, this);
        binding.recycler.setAdapter(produtoAdapter);
    }

    private String total() {
        BigDecimal total = new BigDecimal("0");

        for (int i = 0; i < itemVendaList.size(); i++) {
            if (itemVendaList.get(i).getQtd() != 0) {
                BigDecimal preco = Util.convertMoneEmBigDecimal(itemVendaList.get(i).getPreco_venda());
                preco = preco.divide(new BigDecimal("100"));
                total = total.add(new BigDecimal(itemVendaList.get(i).getQtd()).multiply(preco));
            }

        }
        return NumberFormat.getCurrencyInstance().format(total);
    }

    @Override
    public void onClick(ItemVenda usuario) {
        Toast.makeText(this, usuario.getNome(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLongClick(ItemVenda usuario) {
        Toast.makeText(this, "onlongclick", Toast.LENGTH_SHORT).show();
    }
}