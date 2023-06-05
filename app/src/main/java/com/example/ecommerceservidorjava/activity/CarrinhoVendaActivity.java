package com.example.ecommerceservidorjava.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.adapter.CarrinhoVendaAdapter;
import com.example.ecommerceservidorjava.databinding.ActivityCarrinhoVendaBinding;
import com.example.ecommerceservidorjava.databinding.DialogPadraoOkCancelarValorBinding;
import com.example.ecommerceservidorjava.model.ItemVenda;
import com.example.ecommerceservidorjava.util.Util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class CarrinhoVendaActivity extends AppCompatActivity implements CarrinhoVendaAdapter.OnClickLister {
    private ActivityCarrinhoVendaBinding binding;
    private CarrinhoVendaAdapter carrinhoVendaAdapter;
    private ArrayList<ItemVenda> itemVendaList;
    private int quantidade = 0;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCarrinhoVendaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recuperarIntent();
binding.include.include.ibVoltar.setOnClickListener(v -> {
    selecionarItems();
    finish();

});
        binding.include.textTitulo.setText("Carrinho");
        binding.includeSheet.btnContinue.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), CheckoutVendaActivity.class);
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
        configRvProdutos(itemVendaList);
    }


    private void configRvProdutos(List<ItemVenda> itemVendas) {
        binding.recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recycler.setHasFixedSize(true);
        carrinhoVendaAdapter = new CarrinhoVendaAdapter(itemVendas, getApplicationContext(), this);
        binding.recycler.setAdapter(carrinhoVendaAdapter);
    }

    private void somar(int position, ItemVenda itemVenda) {

        itemVenda.setQtd(itemVenda.getQtd() + 1);
        quantidade = quantidade + 1;
        binding.includeSheet.counterBadge.setText(String.valueOf(quantidade));
        carrinhoVendaAdapter.notifyItemChanged(position);
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
        carrinhoVendaAdapter.notifyItemChanged(position);
        binding.includeSheet.tvTotalCart.setText(total());


    }

    private void preco(int position, ItemVenda itemVenda, String preco) {

        itemVenda.setPreco_venda(preco);
        binding.includeSheet.counterBadge.setText(String.valueOf(quantidade));
        carrinhoVendaAdapter.notifyItemChanged(position);
        binding.lytCartSheet.setVisibility(View.VISIBLE);
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
        if (itemVendaList.size() == 0) {
            binding.lytCartSheet.setVisibility(View.GONE);
        }
        binding.includeSheet.tvTotalCart.setText(total());
        carrinhoVendaAdapter.notifyDataSetChanged();


    }

    private void showDialogPreco(ItemVenda itemVenda, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                CarrinhoVendaActivity.this, R.style.CustomAlertDialog2);

        DialogPadraoOkCancelarValorBinding precoBinding = DialogPadraoOkCancelarValorBinding
                .inflate(LayoutInflater.from(CarrinhoVendaActivity.this));
        precoBinding.editPreco.setText(itemVenda.getPreco_venda());
        precoBinding.dialogPadraoBtnDireita.setOnClickListener(v -> {
            preco(position, itemVenda, precoBinding.editPreco.getText().toString());
            dialog.dismiss();
        });

        precoBinding.dialogPadraoBtnEsquerda.setOnClickListener(v -> {
            dialog.dismiss();

        });

        builder.setView(precoBinding.getRoot());

        dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);// impede fechamento com clique externo.
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

        if (operacao.equals("preco")) {
            showDialogPreco(itemVenda, position);
        }
    }

    private void selecionarItems() {


        Intent intent = new Intent(getApplicationContext(), CadastroVendaActivity.class);
        intent.putExtra("itemVenda2", itemVendaList);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
      selecionarItems();
    }
}