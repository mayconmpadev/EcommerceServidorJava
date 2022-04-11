package com.example.ecommerceservidorjava.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ItemListaOrcamentoBinding;
import com.example.ecommerceservidorjava.model.Orcamento;


import java.util.List;


public class ListaOrcamentoAdapter extends RecyclerView.Adapter<ListaOrcamentoAdapter.MyViewHolder> {

    private final int layout;
    private final List<Orcamento> clienteList;
    private final Context context;
    private final boolean favorito;

    private final OnClickLister onClickLister;
    private final OnLongClickLister onLongClickLister;

    public ListaOrcamentoAdapter(int layout, List<Orcamento> clienteList, Context context, boolean favorito, OnClickLister onClickLister, OnLongClickLister onLongClickLister) {
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
        Orcamento orcamento = clienteList.get(position);

        holder.binding.textNome.setText(orcamento.getIdCliente().getNome().substring(0, 1).toUpperCase().concat(orcamento.getIdCliente().getNome().substring(1)));
        holder.binding.textEmeil.setText(orcamento.getIdCliente().getEmail());
        if (orcamento.getStatus().equals("bronze")){
            holder.binding.viewStatus.setBackgroundResource(R.color.color_verde);
        }else if (orcamento.getStatus().equals("prata")){
            holder.binding.viewStatus.setBackgroundResource(R.color.color_verde);
        }else {
            holder.binding.viewStatus.setBackgroundResource(R.color.color_verde);
        }

        Glide.with(context).load(orcamento.getIdCliente().getUrlImagem()).centerCrop().placeholder(R.drawable.user_123).into(holder.binding.imgFoto);



        holder.binding.root.setOnClickListener(v -> onClickLister.onClick(orcamento));
        holder.itemView.setOnLongClickListener(v -> {
            onLongClickLister.onLongClick(orcamento);
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
        void onClick(Orcamento usuario);

        void onLongClick(Orcamento usuario);

    }

    public interface OnLongClickLister {

        void onLongClick(Orcamento usuario);

    }

}
