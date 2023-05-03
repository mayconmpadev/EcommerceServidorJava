package com.example.ecommerceservidorjava.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerceservidorjava.R;
import com.example.ecommerceservidorjava.databinding.ItemListaOrcamentoBinding;

import com.example.ecommerceservidorjava.databinding.ItemListaOrdemServicoBinding;
import com.example.ecommerceservidorjava.model.OrdemServico;
import com.example.ecommerceservidorjava.util.Timestamp;

import java.util.List;


public class ListaOrdemServicoAdapter extends RecyclerView.Adapter<ListaOrdemServicoAdapter.MyViewHolder> {

    private final int layout;
    private final List<OrdemServico> ordemServicoList;
    private final Context context;
    private final boolean favorito;

    private final OnClickLister onClickLister;
    private final OnLongClickLister onLongClickLister;

    public ListaOrdemServicoAdapter(int layout, List<OrdemServico> ordemServicoList, Context context, boolean favorito, OnClickLister onClickLister, OnLongClickLister onLongClickLister) {
        this.layout = layout;
        this.ordemServicoList = ordemServicoList;
        this.context = context;
        this.favorito = favorito;
        this.onClickLister = onClickLister;
        this.onLongClickLister = onLongClickLister;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MyViewHolder(
                ItemListaOrdemServicoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @SuppressLint("UseCompatTextViewDrawableApis")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        OrdemServico ordemServico = ordemServicoList.get(position);

        holder.binding.textEmeil.setText(ordemServico.getIdCliente().getNome().substring(0, 1).toUpperCase().concat(ordemServico.getIdCliente().getNome().substring(1)));
        holder.binding.textNome.setText(ordemServico.getEquipamento());
        holder.binding.textNumeroOs.setText(ordemServico.getNumeroOs());
        holder.binding.textTotal.setText(ordemServico.getTotal());
        if (ordemServico.isEntregue()){
            holder.binding.textEntregue.setVisibility(View.VISIBLE);
        }else {
            holder.binding.textEntregue.setVisibility(View.GONE);
        }
        if (ordemServico.getStatus().equals("Em analise")){
            holder.binding.viewStatus.setBackgroundResource(R.color.ouro);
        }else if(ordemServico.getStatus().equals("Aprovado")){
            holder.binding.viewStatus.setBackgroundResource(R.color.color_verde);
        }else if(ordemServico.getStatus().equals("Orçada, esperando aprovação")) {
            holder.binding.viewStatus.setBackgroundResource(R.color.color_laranja);
        }else {
            holder.binding.viewStatus.setBackgroundResource(R.color.red);
        }

        holder.binding.textData.setText(Timestamp.getFormatedDateTime(Long.parseLong(ordemServico.getDataEntrada()),"dd/MM/yyyy - HH:mm"));



        Glide.with(context).load(ordemServico.getIdCliente().getUrlImagem()).centerCrop().placeholder(R.drawable.user_123).into(holder.binding.imgFoto);



        holder.binding.root.setOnClickListener(v -> onClickLister.onClick(ordemServico, position));
        holder.itemView.setOnLongClickListener(v -> {
            onLongClickLister.onLongClick(ordemServico);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return ordemServicoList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemListaOrdemServicoBinding binding;


        public MyViewHolder(ItemListaOrdemServicoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    public interface OnClickLister {
        void onClick(OrdemServico ordemServico, int position);



    }

    public interface OnLongClickLister {

        void onLongClick(OrdemServico ordemServico);

    }

}
