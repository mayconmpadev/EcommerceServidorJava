package com.example.ecommerceservidorjava.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ItemProdutoOrcamentoBinding;
import com.example.ecommerceservidorjava.model.ItemVenda;
import com.example.ecommerceservidorjava.model.Produto;
import com.example.ecommerceservidorjava.util.Util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;


public class CadastroVendaAdapter extends RecyclerView.Adapter<CadastroVendaAdapter.MyViewHolder> {


    private final List<Produto> produtoList;
    private final List<ItemVenda> itemVendaList;
    private final Context context;
    private final OnClickLister onClickLister;


    public CadastroVendaAdapter(List<Produto> produtoList, List<ItemVenda> itemVendaList, Context context, OnClickLister onClickLister) {

        this.produtoList = produtoList;
        this.itemVendaList = itemVendaList;
        this.context = context;

        this.onClickLister = onClickLister;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MyViewHolder(
                ItemProdutoOrcamentoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Produto produto = produtoList.get(position);
        ItemVenda itemVenda = itemVendaList.get(position);
        if (itemVenda.getQtd() > 0) {
            holder.binding.btnComprar.setVisibility(View.GONE);
            holder.binding.lytQuantidade.setVisibility(View.VISIBLE);
            holder.binding.textQuantidade.setText(String.valueOf(itemVenda.getQtd()));
            holder.binding.lytPlusMinus.setVisibility(View.VISIBLE);


        }else {
            holder.binding.btnComprar.setVisibility(View.VISIBLE);
            holder.binding.lytQuantidade.setVisibility(View.GONE);
            holder.binding.lytPlusMinus.setVisibility(View.GONE);
        }

        holder.binding.textNome.setText(produto.getNome().substring(0, 1).toUpperCase().concat(produto.getNome().substring(1)));
        holder.binding.textDescricao.setText(produto.getDescricao());
        if (produto.getDesconto().equals("0")) {

            holder.binding.textValorAntigo.setVisibility(View.GONE);
        }else {
            BigDecimal mult = new BigDecimal(100);
            BigDecimal porcentagem = Util.convertMoneEmBigDecimal(produto.getDesconto());
            BigDecimal preco = Util.convertMoneEmBigDecimal(produto.getPrecoVenda());
            preco = preco.divide(new BigDecimal("100"));
            BigDecimal desconto = porcentagem.divide(mult).multiply(preco);
            preco = preco.add(desconto);

            holder.binding.textValorAntigo.setText(NumberFormat.getCurrencyInstance().format(preco));
            holder.binding.textValorAntigo.setPaintFlags(  holder.binding.textValorAntigo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        }
        holder.binding.textValorNovo.setText(produto.getPrecoVenda());

        Glide.with(context).load(produto.getUrlImagem0()).centerCrop().placeholder(R.drawable.ic_action_visivel).into(holder.binding.imagemProduto);
        holder.binding.lytParent.setOnClickListener(v -> onClickLister.onClick(position, itemVenda, "detalhe"));
        holder.binding.btnComprar.setOnClickListener(v -> onClickLister.onClick(position, itemVenda, "mais"));
        holder.binding.btnMais.setOnClickListener(v -> onClickLister.onClick(position, itemVenda, "mais"));
        holder.binding.btnMenos.setOnClickListener(v -> onClickLister.onClick(position, itemVenda, "menos"));

    }

    @Override
    public int getItemCount() {
        return produtoList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemProdutoOrcamentoBinding binding;


        public MyViewHolder(ItemProdutoOrcamentoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public interface OnClickLister {
        void onClick(int position, ItemVenda itemVenda, String operacao);

    }

}
