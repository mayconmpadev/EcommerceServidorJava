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

import com.example.ecommerceservidorjava.model.Boleto;
import com.example.ecommerceservidorjava.util.Timestamp;

import java.util.List;


public class ListaBoletoAdapter extends RecyclerView.Adapter<ListaBoletoAdapter.MyViewHolder> {

    private final int layout;
    private final List<Boleto> boletoList;
    private final Context context;
    private final boolean favorito;

    private final OnClickLister onClickLister;
    private final OnLongClickLister onLongClickLister;

    public ListaBoletoAdapter(int layout, List<Boleto> boletoList, Context context, boolean favorito, OnClickLister onClickLister, OnLongClickLister onLongClickLister) {
        this.layout = layout;
        this.boletoList = boletoList;
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
        Boleto boleto = boletoList.get(position);
        if (boleto.getTipo().equals("venda")) {
            holder.binding.textNome.setText(boleto.getIdVenda().getIdCliente().getNome().substring(0, 1).toUpperCase().concat(boleto.getIdVenda().getIdCliente().getNome().substring(1)));
            holder.binding.textEmeil.setText(boleto.getIdVenda().getIdCliente().getTelefone1());
            holder.binding.textTotal.setText(boleto.getIdVenda().getTotal());
            Glide.with(context).load(boleto.getIdVenda().getIdCliente().getUrlImagem()).centerCrop().placeholder(R.drawable.user_123).into(holder.binding.imgFoto);
        } else {
            holder.binding.textNome.setText(boleto.getIdOrdenServico().getIdCliente().getNome().substring(0, 1).toUpperCase().concat(boleto.getIdVenda().getIdCliente().getNome().substring(1)));
            holder.binding.textEmeil.setText(boleto.getIdOrdenServico().getIdCliente().getTelefone1());
            holder.binding.textTotal.setText(boleto.getIdOrdenServico().getTotal());
            Glide.with(context).load(boleto.getIdOrdenServico().getIdCliente().getUrlImagem()).centerCrop().placeholder(R.drawable.user_123).into(holder.binding.imgFoto);
        }

        if (boleto.getStatus().equals("Aguardando retirada")) {
            holder.binding.viewStatus.setBackgroundResource(R.color.ouro);
        } else if (boleto.getStatus().equals("Finalizada")) {
            holder.binding.viewStatus.setBackgroundResource(R.color.color_verde);
        } else {
            holder.binding.viewStatus.setBackgroundResource(R.color.red);
        }

        holder.binding.textData.setText(Timestamp.getFormatedDateTime(Long.parseLong(boleto.getData()), "dd/MM/yyyy - HH:mm"));


        holder.binding.root.setOnClickListener(v -> onClickLister.onClick(boleto, position));
        holder.itemView.setOnLongClickListener(v -> {
            onLongClickLister.onLongClick(boleto);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return boletoList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemListaOrcamentoBinding binding;


        public MyViewHolder(ItemListaOrcamentoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public interface OnClickLister {
        void onClick(Boleto boleto, int position);


    }

    public interface OnLongClickLister {

        void onLongClick(Boleto boleto);

    }

}
