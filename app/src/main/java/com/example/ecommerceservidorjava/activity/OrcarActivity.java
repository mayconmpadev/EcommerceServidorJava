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
import com.example.ecommerceservidorjava.util.Base64Custom;
import com.example.ecommerceservidorjava.util.FirebaseHelper;
import com.example.ecommerceservidorjava.util.SPM;
import com.example.ecommerceservidorjava.util.Util;
import com.google.firebase.database.DatabaseReference;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class OrcarActivity extends AppCompatActivity implements ListaPecasAdapter.OnClickLister, ListaPecasAdapter.OnLongClickLister {
    private ActivityOrcarBinding binding;
    private OrdemServico ordemServico;
    ListaPecasAdapter produtoAdapter;
    private ArrayList<ItemVenda> itemVendaList;
    private SPM spm = new SPM(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrcarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
        binding.include.include.ibVoltar.setOnClickListener(v -> {
            finish();
        });
        binding.floatingActionButton.setOnClickListener(v -> {
            if (!binding.editDefeito.getText().toString().isEmpty()) {
                ordemServico.setDefeitoEncontrado(binding.editDefeito.getText().toString());
            }
            if (!binding.editValorServico.getText().toString().isEmpty()) {
                ordemServico.setValorMaoDeObra(binding.editValorServico.getText().toString());
            }
            Intent intent = new Intent(getApplicationContext(), OrcarPecasActivity.class);
            intent.putExtra("ordemServiçoSelecionada", ordemServico);
            startActivity(intent);
        });

        binding.btnSalvar.setOnClickListener(v -> {
            salvar();
        });

        binding.editValorServico.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                BigDecimal total;
                if (itemVendaList != null) {
                    total = Util.convertMoneEmBigDecimal(total());
                } else {
                    total = Util.convertMoneEmBigDecimal("R$0,00");
                }

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

    private void salvar() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ordemServico.setItens(itemVendaList);
        ordemServico.setStatus("Orçada, esperando aprovação");
        ordemServico.setMaoDeObra(binding.spinner.getSelectedItem().toString());
        ordemServico.setValorMaoDeObra(binding.editValorServico.getText().toString());
        ordemServico.setDefeitoEncontrado(binding.editDefeito.getText().toString());
        ordemServico.setTotal(binding.textValorTotal.getText().toString());

        String caminho = Base64Custom.codificarBase64(spm.getPreferencia("PREFERENCIAS", "CAMINHO", ""));
        DatabaseReference databaseReference = FirebaseHelper.getDatabaseReference().child("empresas")
                .child(caminho)
                .child("ordens_servicos").child(ordemServico.getId());

        databaseReference.setValue(ordemServico).addOnCompleteListener(task1 -> {

            if (task1.isSuccessful()) {
                binding.progressBar.setVisibility(View.GONE);
                finishAffinity();
                Intent intent = new Intent(getApplicationContext(), ListaOrdemServicoActivity.class);

                startActivity(intent);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "erro de foto", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void recuperarIntent() {

        itemVendaList = (ArrayList<ItemVenda>) getIntent().getSerializableExtra("itemVenda");
        ordemServico = (OrdemServico) getIntent().getSerializableExtra("ordemServiçoSelecionada");


        if (ordemServico != null) {
            binding.textNome.setText("");
            binding.textEquipamento.setText("");
            binding.textDefeito.setText("");
            binding.textGarantia.setText("");

            Util.textoNegrito(binding.textNome.getText() + "nome:  *" + ordemServico.getIdCliente().getNome() + "*", binding.textNome, null);
            Util.textoNegrito(binding.textEquipamento.getText() + "equipamento:  *" + ordemServico.getEquipamento() + "*", binding.textEquipamento, null);
            Util.textoNegrito(binding.textDefeito.getText() + "defeito:  *" + ordemServico.getDefeitoRelatado() + "*", binding.textDefeito, null);
            binding.editDefeito.setText(ordemServico.getDefeitoEncontrado());


            binding.editValorServico.setText(ordemServico.getValorMaoDeObra());
            if (binding.editValorServico.getText().toString().isEmpty()) {
                binding.editValorServico.setText("0,00");
            }


            if (ordemServico.isGarantia()) {

                Util.textoNegrito(binding.textGarantia.getText() + "garantia:  " + "*sim*", binding.textGarantia, null);

            } else {
                Util.textoNegrito(binding.textGarantia.getText() + "garantia:  " + "*não*", binding.textGarantia, null);
            }

            String sUnidade = ordemServico.getMaoDeObra();
            String[] arrayUnidade = getResources().getStringArray(R.array.tipo_servico);
            for (int i = 0; i < arrayUnidade.length; i++) {
                if (arrayUnidade[i].equals(sUnidade)) {
                    binding.spinner.setSelection(i);
                    break;
                }
            }
        }

        if (itemVendaList != null) {
            Toast.makeText(this, "1", Toast.LENGTH_SHORT).show();
            configRvProdutos(itemVendaList);
            binding.textValorPecas.setText(total());

            BigDecimal total = Util.convertMoneEmBigDecimal(total());
            total = total.divide(new BigDecimal("100"));
            BigDecimal preco = Util.convertMoneEmBigDecimal(binding.editValorServico.getText().toString());
            preco = preco.divide(new BigDecimal("100"));
            total = total.add(preco);
            binding.textValorTotal.setText(NumberFormat.getCurrencyInstance().format(total));

        } else if (ordemServico.getItens() != null) {
            Toast.makeText(this, "2", Toast.LENGTH_SHORT).show();
            itemVendaList = new ArrayList<>(ordemServico.getItens());
            configRvProdutos(itemVendaList);
            binding.textValorPecas.setText(total());

            BigDecimal total = Util.convertMoneEmBigDecimal(total());
            total = total.divide(new BigDecimal("100"));
            BigDecimal preco = Util.convertMoneEmBigDecimal(binding.editValorServico.getText().toString());
            preco = preco.divide(new BigDecimal("100"));
            total = total.add(preco);
            binding.textValorTotal.setText(NumberFormat.getCurrencyInstance().format(total));

        } else {
            Toast.makeText(this, "3", Toast.LENGTH_SHORT).show();
            binding.textValorTotal.setText(ordemServico.getTotal());
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
    public void onClick(ItemVenda itemVenda) {
        Toast.makeText(this, itemVenda.getNome(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLongClick(ItemVenda itemVenda) {
        Toast.makeText(this, "onlongclick", Toast.LENGTH_SHORT).show();
        itemVendaList.remove(itemVenda);
        recuperarIntent();
    }
}