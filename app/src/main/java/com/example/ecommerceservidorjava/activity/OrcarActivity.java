package com.example.ecommerceservidorjava.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.adapter.ListaPecasAdapter;
import com.example.ecommerceservidorjava.adapter.ListaProdutoAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityOrcarBinding;
import com.example.ecommerceservidorjava.model.ItemVenda;
import com.example.ecommerceservidorjava.model.OrdemServico;
import com.example.ecommerceservidorjava.model.Produto;
import com.example.ecommerceservidorjava.util.Util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class OrcarActivity extends AppCompatActivity implements ListaPecasAdapter.OnClickLister, ListaPecasAdapter.OnLongClickLister {
    private ActivityOrcarBinding binding;
    private OrdemServico ordemServico;
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

        binding.editValorServico.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                BigDecimal total = Util.convertMoneEmBigDecimal(total());
                total = total.divide(new BigDecimal("100"));
                BigDecimal preco = Util.convertMoneEmBigDecimal(binding.editValorServico.getText().toString());
                preco = preco.divide(new BigDecimal("100"));
                total = total.add(preco);
                binding.textValorTotal.setText(NumberFormat.getCurrencyInstance().format(total));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void recuperarIntent() {

        itemVendaList = (ArrayList<ItemVenda>) getIntent().getSerializableExtra("itemVenda");
        ordemServico = (OrdemServico) getIntent().getSerializableExtra("ordemServiçoSelecionada");

        if (itemVendaList != null) {
            configRvProdutos(itemVendaList);
            binding.textValorPecas.setText(total());

            BigDecimal total = Util.convertMoneEmBigDecimal(total());
            total = total.divide(new BigDecimal("100"));
            BigDecimal preco = Util.convertMoneEmBigDecimal(binding.editValorServico.getText().toString());
            preco = preco.divide(new BigDecimal("100"));
            total = total.add(preco);
            binding.textValorTotal.setText(NumberFormat.getCurrencyInstance().format(total));

        }
        if (ordemServico != null){
            binding.textNome.setText(binding.textNome.getText() + "  " + ordemServico.getIdCliente().getNome());
            binding.textEquipamento.setText(binding.textEquipamento.getText() + "  " + ordemServico.getEquipamento());
            binding.textDefeito.setText(binding.textDefeito.getText() + "  " + ordemServico.getDefeitoRelatado());
            if (ordemServico.isGarantia()){
                binding.textGarantia.setText(binding.textGarantia.getText() + "  " + "sim");
            }else {
                binding.textGarantia.setText(binding.textGarantia.getText() + "  " + "não");
            }
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