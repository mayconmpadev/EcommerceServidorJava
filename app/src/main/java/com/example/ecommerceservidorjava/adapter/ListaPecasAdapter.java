package com.example.ecommerceservidorjava.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ItemPecas0rdemBinding;
import com.example.ecommerceservidorjava.databinding.ItemProdutoAdapterBinding;

import com.example.ecommerceservidorjava.model.ItemVenda;
import com.example.ecommerceservidorjava.util.Util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;


public class ListaPecasAdapter extends RecyclerView.Adapter<ListaPecasAdapter.MyViewHolder> {

    private final int layout;
    private final List<ItemVenda> produtoList;
    private final Context context;
    private final boolean favorito;

    private final OnClickLister onClickLister;
    private final OnLongClickLister onLongClickLister;

    public ListaPecasAdapter(int layout, List<ItemVenda> produtoList, Context context, boolean favorito, OnClickLister onClickLister, OnLongClickLister onLongClickLister) {
        this.layout = layout;
        this.produtoList = produtoList;
        this.context = context;
        this.favorito = favorito;
        this.onClickLister = onClickLister;
        this.onLongClickLister = onLongClickLister;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MyViewHolder(
                ItemPecas0rdemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ItemVenda itemVenda = produtoList.get(position);
        holder.binding.textQtd.setText(String.valueOf(itemVenda.getQtd()));
        holder.binding.textPecas.setText(itemVenda.getNome().substring(0, 1).toUpperCase().concat(itemVenda.getNome().substring(1)));
        holder.binding.textValor.setText(itemVenda.getPreco_venda());



        BigDecimal preco = Util.convertMoneEmBigDecimal(itemVenda.getPreco_venda());
        BigDecimal total = new BigDecimal("0");
        preco = preco.divide(new BigDecimal("100"));
        total = total.add(new BigDecimal(itemVenda.getQtd()).multiply(preco));
        holder.binding.textValorTotal.setText(NumberFormat.getCurrencyInstance().format(total));


        holder.binding.root.setOnClickListener(v -> onClickLister.onClick(itemVenda));
        holder.binding.root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View on) {
                onLongClickLister.onLongClick(itemVenda);
                return true;
            }
        });


    }

    @Override
    public int getItemCount() {
        return produtoList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemPecas0rdemBinding binding;


        public MyViewHolder(ItemPecas0rdemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public interface OnClickLister {
        void onClick(ItemVenda usuario);

    }

    public interface OnLongClickLister {

        void onLongClick(ItemVenda usuario);

    }

}
