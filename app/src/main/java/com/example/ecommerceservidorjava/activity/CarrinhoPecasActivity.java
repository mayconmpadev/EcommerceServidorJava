package com.example.ecommerceservidorjava.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ecommerceservidorjava.adapter.CarrinhoOrcamentoAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityCarrinhoOrcamentoBinding;
import com.example.ecommerceservidorjava.databinding.ActivityCarrinhoPecasBinding;
import com.example.ecommerceservidorjava.model.ItemVenda;
import com.example.ecommerceservidorjava.util.Util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class CarrinhoPecasActivity extends AppCompatActivity implements CarrinhoOrcamentoAdapter.OnClickLister {
    private ActivityCarrinhoPecasBinding binding;
    private CarrinhoOrcamentoAdapter carrinhoOrcamentoAdapter;
    private ArrayList<ItemVenda> itemVendaList;
    private int quantidade = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCarrinhoPecasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        configRvProdutos(itemVendaList);
        binding.include.textTitulo.setText("Carrinho");
        binding.includeSheet.btnContinue.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), OrcarActivity.class);

            intent.putExtra("itemVenda", itemVendaList);
            startActivity(intent);
        });
    }

    private void recuperarIntent() {

        itemVendaList = (ArrayList<ItemVenda>) getIntent().getSerializableExtra("itemVenda");
        binding.includeSheet.tvTotalCart.setText(total());
        for (int i = 0; i < itemVendaList.size(); i++) {
            quantidade = quantidade + itemVendaList.get(i).getQtd();

        }
        binding.includeSheet.counterBadge.setText(String.valueOf(quantidade));

    }


    private void configRvProdutos(List<ItemVenda> itemVendas) {
        binding.recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recycler.setHasFixedSize(true);
        carrinhoOrcamentoAdapter = new CarrinhoOrcamentoAdapter(itemVendas, getApplicationContext(), this);
        binding.recycler.setAdapter(carrinhoOrcamentoAdapter);
    }

    private void somar(int position, ItemVenda itemVenda) {

        itemVenda.setQtd(itemVenda.getQtd() + 1);
        quantidade = quantidade + 1;
        binding.includeSheet.counterBadge.setText(String.valueOf(quantidade));
        carrinhoOrcamentoAdapter.notifyItemChanged(position);
        binding.lytCartSheet.setVisibility(View.VISIBLE);
        binding.includeSheet.tvTotalCart.setText(total());

    }

    private void subtrair(int position, ItemVenda itemVenda) {

        itemVenda.setQtd(itemVenda.getQtd() - 1);
        if (itemVenda.getQtd() == 0) {
            excluir(position, itemVenda);
        }
        quantidade = quantidade - 1;
        binding.includeSheet.counterBadge.setText(String.valueOf(quantidade));
        carrinhoOrcamentoAdapter.notifyItemChanged(position);
        binding.includeSheet.tvTotalCart.setText(total());


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

    private void excluir(int position, ItemVenda itemVenda) {
        quantidade = quantidade - itemVenda.getQtd();
        binding.includeSheet.counterBadge.setText(String.valueOf(quantidade));
        itemVendaList.remove(itemVenda);
        if (itemVendaList.size() == 0){
            binding.lytCartSheet.setVisibility(View.GONE);
        }
        binding.includeSheet.tvTotalCart.setText(total());
        carrinhoOrcamentoAdapter.notifyDataSetChanged();


    }


    @Override
    public void onClick(int position, ItemVenda itemVenda, String operacao) {
        if (operacao.equals("mais")) {
            somar(position, itemVenda);
        }
        if (operacao.equals("menos")) {
            subtrair(position, itemVenda);
        }

        if (operacao.equals("excluir")) {
            excluir(position, itemVenda);
        }
    }
}