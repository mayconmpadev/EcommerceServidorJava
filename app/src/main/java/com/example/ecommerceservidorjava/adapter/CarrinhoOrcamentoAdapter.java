package com.example.ecommerceservidorjava.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ItemCartBinding;
import com.example.ecommerceservidorjava.model.ItemVenda;
import com.example.ecommerceservidorjava.util.Util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;


public class CarrinhoOrcamentoAdapter extends RecyclerView.Adapter<CarrinhoOrcamentoAdapter.MyViewHolder> {

    private final List<ItemVenda> itemVendaList;
    private final Context context;
    private final OnClickLister onClickLister;


    public CarrinhoOrcamentoAdapter(List<ItemVenda> itemVendaList, Context context, OnClickLister onClickLister) {


        this.itemVendaList = itemVendaList;
        this.context = context;

        this.onClickLister = onClickLister;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MyViewHolder(
                ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {


        ItemVenda itemVenda = itemVendaList.get(position);


        holder.binding.textNome.setText(itemVenda.getNome().substring(0, 1).toUpperCase().concat(itemVenda.getNome().substring(1)));
        holder.binding.textDescricao.setText(itemVenda.getDescricao());

        holder.binding.textPreco.setText(itemVenda.getPreco());
        holder.binding.textQuantidade.setText(String.valueOf(itemVenda.getQtd()));
        BigDecimal preco = Util.convertMoneEmBigDecimal(itemVenda.getPreco());
        BigDecimal total = new BigDecimal("0");
        preco = preco.divide(new BigDecimal("100"));
        total = total.add(new BigDecimal(itemVenda.getQtd()).multiply(preco));
        holder.binding.textValorTotal.setText(NumberFormat.getCurrencyInstance().format(total));
        Glide.with(context).load(itemVenda.getFoto()).centerCrop().placeholder(R.drawable.ic_action_visivel).into(holder.binding.image);

        holder.binding.imageExcluir.setOnClickListener(v -> onClickLister.onClick(position, itemVenda, "excluir"));
        holder.binding.btnMais.setOnClickListener(v -> onClickLister.onClick(position, itemVenda, "mais"));
        holder.binding.btnMenos.setOnClickListener(v -> onClickLister.onClick(position, itemVenda, "menos"));

    }

    @Override
    public int getItemCount() {
        return itemVendaList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemCartBinding binding;


        public MyViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public interface OnClickLister {
        void onClick(int position, ItemVenda itemVenda, String operacao);


    }

}
