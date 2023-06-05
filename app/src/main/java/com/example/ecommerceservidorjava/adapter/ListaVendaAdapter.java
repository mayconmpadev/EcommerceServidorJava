package com.example.ecommerceservidorjava.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ItemListaOrcamentoBinding;

import com.example.ecommerceservidorjava.model.Venda;
import com.example.ecommerceservidorjava.util.Timestamp;
import com.example.ecommerceservidorjava.util.Util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;


public class ListaVendaAdapter extends RecyclerView.Adapter<ListaVendaAdapter.MyViewHolder> {

    private final int layout;
    private final List<Venda> clienteList;
    private final Context context;
    private final boolean favorito;

    private final OnClickLister onClickLister;
    private final OnLongClickLister onLongClickLister;

    public ListaVendaAdapter(int layout, List<Venda> clienteList, Context context, boolean favorito, OnClickLister onClickLister, OnLongClickLister onLongClickLister) {
        this.layout = layout;
        this.clienteList = clienteList;
        this.context = context;
        this.favorito = favorito;
        this.onClickLister = onClickLister;
        this.onLongClickLister = onLongClickLister;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MyViewHolder(
                ItemListaOrcamentoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @SuppressLint("UseCompatTextViewDrawableApis")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Venda venda = clienteList.get(position);
        BigDecimal parcela1 = new BigDecimal("0");
        BigDecimal parcela2 = new BigDecimal("0");
        BigDecimal parcela3 = new BigDecimal("0");
        BigDecimal totalPagar = new BigDecimal("0");
        BigDecimal dividir = new BigDecimal("100");
        BigDecimal total = new BigDecimal("0");

        parcela1 = Util.convertMoneEmBigDecimal(venda.getParcela1());
        parcela2 = Util.convertMoneEmBigDecimal(venda.getParcela2());
        parcela3 = Util.convertMoneEmBigDecimal(venda.getParcela3());
        totalPagar = Util.convertMoneEmBigDecimal(venda.getTotal());
        total = total.add(parcela1).add(parcela2).add(parcela3);
        holder.binding.textNome.setText(venda.getIdCliente().getNome().substring(0, 1).toUpperCase().concat(venda.getIdCliente().getNome().substring(1)));
        holder.binding.textEmeil.setText(venda.getIdCliente().getTelefone1());
        if (total.compareTo(BigDecimal.ZERO) == 0){
            holder.binding.textTotal.setText(venda.getTotal());
        }else {
           // totalPagar = totalPagar.subtract(total);
           // holder.binding.textTotal.setText(NumberFormat.getCurrencyInstance().format(totalPagar.divide(dividir)));
            holder.binding.textTotal.setText(venda.getTotal());
        }

        if (venda.getStatus().equals("Aguardando retirada")){
            holder.binding.viewStatus.setBackgroundResource(R.color.ouro);
        }else if(venda.getStatus().equals("Finalizada")){
            holder.binding.viewStatus.setBackgroundResource(R.color.color_verde);
        }else {
            holder.binding.viewStatus.setBackgroundResource(R.color.red);
        }

        if (!venda.isBoletoPago()){
            holder.binding.root.setBackgroundResource(R.color.color_cinza_claro);
        }else {
            holder.binding.root.setBackgroundResource(R.color.branco);
        }

        holder.binding.textData.setText(Timestamp.getFormatedDateTime(Long.parseLong(venda.getData()),"dd/MM/yyyy - HH:mm"));



        Glide.with(context).load(venda.getIdCliente().getUrlImagem()).centerCrop().placeholder(R.drawable.user_123).into(holder.binding.imgFoto);



        holder.binding.root.setOnClickListener(v -> onClickLister.onClick(venda, position));
        holder.itemView.setOnLongClickListener(v -> {
            onLongClickLister.onLongClick(venda);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return clienteList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemListaOrcamentoBinding binding;


        public MyViewHolder(ItemListaOrcamentoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public interface OnClickLister {
        void onClick(Venda usuario, int position);



    }

    public interface OnLongClickLister {

        void onLongClick(Venda usuario);

    }

}
